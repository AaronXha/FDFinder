package FD;

import FastADC.aei.LongBitSetTrie;
import ch.javasoft.bitset.LongBitSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import static FD.ApproxDifferenceInverter.error;
import static FD.ApproxDifferenceInverter.totalCount;
import static FD.FDFinder.nAttributes;

public class ApproxFDBuilder {
    private final DifferenceSet differenceSet;
    private List<Difference> differences;
    private LongBitSetTrie approxCovers;    // for subset searching
    private final long target;
    private final int attribute;
    private final List<LongBitSet> rawFDs = new ArrayList<>();

    public ApproxFDBuilder(DifferenceSet _differenceSet, int _attribute){
        differenceSet = _differenceSet;
        target = (long) Math.ceil(differenceSet.getTotalCount() - error * totalCount);
        attribute = _attribute;
    }

    public List<LongBitSet> build(){
        long t0 = System.currentTimeMillis();
        if(target <= 0){
            List<LongBitSet> res = new ArrayList<>();
            for(int j = 0; j < nAttributes; j++){
                if(attribute != j){
                    LongBitSet fd = new LongBitSet();
                    fd.set(j);
                    res.add(fd);
                }
            }
            System.out.println("   [ADI][" + attribute + "] ADI time: " + (System.currentTimeMillis() - t0));
            System.out.println("   [ADI][" + attribute + "] FD size: " + res.size());
        }

        differences = differenceSet.getDifferences();
        differences.sort((o1, o2) -> Long.compare(o2.count, o1.count));

        inverseDifferenceSet(target, attribute);

        /** collect resulted FD */
        approxCovers.forEachFD(transFD -> rawFDs.add(transFD.bitSet));
        System.out.println("   [ADI][" + attribute + "] ADI time: " + (System.currentTimeMillis() - t0));
        System.out.println("   [ADI][" + attribute + "] FD size: " + rawFDs.size());
        return rawFDs;
    }
    void inverseDifferenceSet(long target, int attribute) {

        approxCovers = new LongBitSetTrie();
        LongBitSet fullMask = new LongBitSet(nAttributes);
        for (int i = 0; i < nAttributes; i++){
            if(i != attribute)
                fullMask.set(i);
        }

        Stack<SearchNode> nodes = new Stack<>();    // manual stack, where differences[node.e] needs to be hit
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


    void walk(int e, LongBitSet addableAttributes, LongBitSetTrie fdCandidates, long target, Stack<SearchNode> nodes, String status) {
        while (e < differences.size() && !fdCandidates.isCandFDEmpty()) {
            LongBitSet diff = differences.get(e).getBitSet();
            Collection<FDCandidate> unhitDiffFDs = fdCandidates.getAndRemoveGen(diff);

            // hit differences[e] later
            SearchNode nd = new SearchNode(e, addableAttributes.clone(), fdCandidates, unhitDiffFDs, target, status + e);
            nodes.push(nd);

            // unhit differences[e]
            if (unhitDiffFDs.isEmpty())
                return;

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
            if (newCandidates.isCandFDEmpty()) return;

            e++;
            fdCandidates = newCandidates;
        }
    }


    void hit(SearchNode nd) {
        if (nd.e >= differences.size() || nd.addableAttributes.isSubSetOf(differences.get(nd.e).getBitSet()))
            return;

        nd.target -= differences.get(nd.e).count;

        LongBitSet diff = differences.get(nd.e).getBitSet();
        LongBitSetTrie fdCandidates = nd.fdCandidates;

        if (nd.target <= 0) {
            fdCandidates.forEachFD(fd -> approxCovers.add(fd));
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
                    LongBitSet mutex = new LongBitSet(nAttributes);
                    mutex.set(i);
                    validFD.cand.andNot(mutex);
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
