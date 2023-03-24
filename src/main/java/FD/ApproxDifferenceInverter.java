package FD;

import FastADC.aei.LongBitSetTrie;
import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class ApproxDifferenceInverter {

    private int nAttributes;
    private List<Difference> differences;
    private LongBitSetTrie approxCovers;    // for subset searching
    //private LongBitSet[] mutexMap;   // i -> predicates concerning the same attribute pair with predicate i
    private AttributeOrganizer organizer; //re-order attributes by difference coverage to accelerate trie

    public ApproxDifferenceInverter(int _nAttributes) {
        nAttributes = _nAttributes;
        LongBitSetTrie.N = nAttributes;
    }

    public FunctionDependency buildFD(DifferenceSet differenceSet, double error) {
        if (error == 1) {
            FunctionDependency res = new FunctionDependency();
            return res;
        }

        organizer = new AttributeOrganizer(nAttributes, differenceSet);
        //mutexMap = organizer.transformMutexMap(mutexMap);

        /** for every attribute as RHS, get subset of differenceSet*/
        List<DifferenceSet> subsetDifferenceSet = getSubsetDifferenceSetList(differenceSet);

        /** for every attribute as RHS*/
        System.out.println("  [ADI] Inverting differences...");
        for (int i = 0; i < nAttributes; i++) {
            long target = (long) Math.ceil((1 - error) * subsetDifferenceSet.get(i).getTotalCount());
            differences = subsetDifferenceSet.get(i).getDifferences();
            differences.sort((o1, o2) -> Long.compare(o2.count, o1.count));
            inverseDifferenceSet(target);
        }


        /** collect resulted FD */
        List<LongBitSet> rawFDs = new ArrayList<>();
        approxCovers.forEachFD(transFD -> rawFDs.add(organizer.retransform(transFD.bitSet)));
        System.out.println("  [ADI] Min cover size: " + rawFDs.size());

        FunctionDependency fds = new FunctionDependency();
/*        for (LongBitSet rawFD : rawFDs)
            fds.add(new DenialConstraint(rawDC));
        System.out.println("  [ADI] Total FD size: " + fds.size());

        fds.minimize();
        System.out.println("  [ADI] Min FD size : " + fds.size());*/

        return fds;
    }

    List<DifferenceSet> getSubsetDifferenceSetList(DifferenceSet differenceSet){
        List<DifferenceSet> subsetDifferenceSetList = new ArrayList<>();
        List<HashLongLongMap> subsetDiffMapList = new ArrayList<>(nAttributes);

        /** generate differenceMap for every attribute*/
        for (Difference subset : differenceSet.getDifferences()) {
            long temp = subset.getDifferenceValue();
            HashLongLongMap diffMap = HashLongLongMaps.newMutableMap();
            int pos = 0;
            while (temp > 0) {
                if ((temp & 1) != 0) {
                    diffMap.addValue(temp & ~(1L << pos), subset.getCount(), 0L);
                    subsetDiffMapList.add(diffMap);
                }
                pos++;
                temp >>>= 1;
            }
        }
        /** generate subset differenceSet for every attribute*/
        for(int i = 0; i < nAttributes; i++){
            long diffCount = 0;
            List<Difference> subsetDifferences = new ArrayList<>();
            for (var entry : subsetDiffMapList.get(i).entrySet()) {
                subsetDifferences.add(new Difference(entry.getKey(), entry.getValue(), nAttributes));
                diffCount +=  entry.getValue();
            }

            subsetDifferenceSetList.add(new DifferenceSet(subsetDifferences, diffCount, nAttributes));
        }

        return subsetDifferenceSetList;
    }

    void inverseDifferenceSet(long target) {

        approxCovers = new LongBitSetTrie();
        LongBitSet fullMask = new LongBitSet(nAttributes);
        for (int i = 0; i < nAttributes; i++)
            fullMask.set(i);

        Stack<SearchNode> nodes = new Stack<>();    // manual stack, where evidences[node.e] needs to be hit
        LongBitSetTrie fdCandidates = new LongBitSetTrie();
        fdCandidates.add(new FDCandidate(new LongBitSet(), fullMask.clone()));

        walk(0, fullMask, fdCandidates, target, nodes, "");

        while (!nodes.isEmpty()) {
            SearchNode nd = nodes.pop();
            if (nd.e >= differences.size() || nd.addableAttributes.isEmpty())
                continue;
            hit(nd);    // hit differences[e]
            if (nd.target > 0)
                walk(nd.e + 1, nd.addableAttributes, nd.fdCandidates, nd.target, nodes, nd.H);
        }
    }

    void hit(SearchNode nd) {
        if (nd.e >= differences.size() || nd.addableAttributes.isSubSetOf(differences.get(nd.e).getBitSet()))
            return;

        nd.target -= differences.get(nd.e).count;

        LongBitSet diff = differences.get(nd.e).getBitSet();
        LongBitSetTrie fdCandidates = nd.fdCandidates;

        if (nd.target <= 0) {
            fdCandidates.forEach(fd -> approxCovers.add(fd));
            for (FDCandidate invalidFD : nd.invalidFDs) {
                LongBitSet canAdd = invalidFD.cand.getAndNot(diff);
                for (int i = canAdd.nextSetBit(0); i >= 0; i = canAdd.nextSetBit(i + 1)) {
                    FDCandidate validFD = new FDCandidate(invalidFD.bitSet.clone());
                    validFD.bitSet.set(i);
                    if (!approxCovers.containsSubset(validFD))
                        approxCovers.add(validFD);
                }
            }
        } else {
            for (FDCandidate invalidFD : nd.invalidFDs) {
                LongBitSet canAdd = invalidFD.cand.getAndNot(diff);
                for (int i = canAdd.nextSetBit(0); i >= 0; i = canAdd.nextSetBit(i + 1)) {
                    FDCandidate validFD = invalidFD.clone();
                    validFD.bitSet.set(i);
                    //validFD.cand.andNot(mutexMap[i]);
                    if (!fdCandidates.containsSubset(validFD) && !approxCovers.containsSubset(validFD)) {
                        if (!validFD.cand.isEmpty())
                            fdCandidates.add(validFD);
                        else if (isApproxCover(validFD.bitSet, nd.e + 1, nd.target))
                            approxCovers.add(validFD);
                    }
                }
            }
        }
    }

    void walk(int e, LongBitSet addableAttributes, LongBitSetTrie fdCandidates, long target, Stack<SearchNode> nodes, String status) {
        while (e < differences.size() && !fdCandidates.isEmpty()) {
            LongBitSet diff = differences.get(e).getBitSet();
            Collection<FDCandidate> unhitDiffFDs = fdCandidates.getAndRemoveGen(diff);

            // hit differences[e] later
            SearchNode nd = new SearchNode(e, addableAttributes.clone(), fdCandidates, unhitDiffFDs, target, status + e);
            nodes.push(nd);

            // unhit differences[e]
            if (unhitDiffFDs.isEmpty()) return;

            addableAttributes.and(diff);
            if (addableAttributes.isEmpty()) return;

            long maxCanHit = 0L;
            for (int i = e + 1; i < differences.size(); i++)
                if (!addableAttributes.isSubSetOf(differences.get(i).getBitSet()))
                    maxCanHit += differences.get(i).count;
            if (maxCanHit < target) return;

            LongBitSetTrie newCandidates = new LongBitSetTrie();
            for (FDCandidate fd : unhitDiffFDs) {
                LongBitSet unhitCand = fd.cand.getAnd(diff);
                if (!unhitCand.isEmpty())
                    newCandidates.add(new FDCandidate(fd.bitSet, unhitCand));
                else if (!approxCovers.containsSubset(fd) && isApproxCover(fd.bitSet, e + 1, target))
                    approxCovers.add(fd);
            }
            if (newCandidates.isEmpty()) return;

            e++;
            fdCandidates = newCandidates;
        }
    }

    boolean isApproxCover(LongBitSet fd, int e, long target) {
        if (target <= 0) return true;
        for (; e < differences.size(); e++) {
            if (!fd.isSubSetOf(differences.get(e).getBitSet())) {
                target -= differences.get(e).count;
                if (target <= 0) return true;
            }
        }
        return false;
    }



}
