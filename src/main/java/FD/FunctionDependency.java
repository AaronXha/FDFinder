package FD;

import ch.javasoft.bitset.LongBitSet;

import java.util.ArrayList;
import java.util.List;

public class FunctionDependency {
    int nAttributes;

    int totalCount;

    List<List<LongBitSet>> minFDs = new ArrayList<>();      // min HSs on each RHS

    public FunctionDependency(){
        totalCount = 0;
    }

    public void add(List<LongBitSet> fd){
        minFDs.add(fd);
        totalCount += fd.size();
    }

    public int getTotalCount(){
        return totalCount;
    }

/*    private static class MinimalFDCandidate {
        DenialConstraint fd;
        IBitSet bitset;

        public MinimalFDCandidate(DenialConstraint fd) {
            this.fd = fd;
            this.bitset = PredicateSetFactory.create(fd.getPredicateSet()).getBitset();
        }

        public boolean shouldReplace(MinimalFDCandidate prior) {
            if (prior == null)
                return true;
            if (fd.getPredicateCount() < prior.fd.getPredicateCount())
                return true;
            if (fd.getPredicateCount() > prior.fd.getPredicateCount())
                return false;

            return bitset.compareTo(prior.bitset) <= 0;
        }
    }
    public void minimize() {
        Map<PredicateSet, MinimalFDCandidate> constraintsClosureMap = new HashMap<>();
        for (DenialConstraint dc : constraints) {
            PredicateSet predicateSet = dc.getPredicateSet();
            Closure c = new Closure(predicateSet);
            if (c.construct()) {
                MinimalFDCandidate candidate = new MinimalFDCandidate(dc);
                PredicateSet closure = c.getClosure();
                MinimalFDCandidate prior = constraintsClosureMap.get(closure);
                if (candidate.shouldReplace(prior))
                    constraintsClosureMap.put(closure, candidate);
            }
        }

        List<Map.Entry<PredicateSet, MinimalFDCandidate>> constraints2 = new ArrayList<>(constraintsClosureMap.entrySet());

        constraints2.sort(Comparator
                .comparingInt((Map.Entry<PredicateSet, MinimalFDCandidate> entry) -> entry.getKey().size())
                .thenComparingInt(entry -> entry.getValue().fd.getPredicateCount())
                .thenComparing(entry -> entry.getValue().bitset));

        constraints = new HashSet<>();
        NTreeSearch tree = new NTreeSearch();
        for (Map.Entry<PredicateSet, MinimalFDCandidate> entry : constraints2) {
            if (tree.containsSubset(PredicateSetFactory.create(entry.getKey()).getBitset()))
                continue;

            DenialConstraint inv = entry.getValue().fd.getInvT1T2DC();
            if (inv != null) {
                Closure c = new Closure(inv.getPredicateSet());
                if (!c.construct())
                    continue;
                if (tree.containsSubset(PredicateSetFactory.create(c.getClosure()).getBitset()))
                    continue;
            }

            constraints.add(entry.getValue().fd);
            tree.add((LongBitSet) entry.getValue().bitset);
            if (inv != null)
                tree.add(PredicateSetFactory.create(inv.getPredicateSet()).getBitset());
        }
    }*/
}
