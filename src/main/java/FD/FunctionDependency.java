package FD;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;

import java.util.*;

public class FunctionDependency {
    int nAttributes;

    int totalCount;

    List<List<LongBitSet>> minFDs = new ArrayList<>();      // min HSs on each RHS

    public FunctionDependency(){
        totalCount = 0;
    }

    public FunctionDependency(int _nAttributes){
        nAttributes = _nAttributes;
    }

    public void add(List<LongBitSet> fd){
        minFDs.add(fd);
        totalCount += fd.size();
    }

    public int getTotalCount(){
        return totalCount;
    }

    /*private static class MinimalFDCandidate {
        DenialConstraint dc;
        IBitSet bitset;

        public MinimalFDCandidate(DenialConstraint dc) {
            this.dc = dc;
            this.bitset = PredicateSetFactory.create(dc.getPredicateSet()).getBitset();
        }

        public boolean shouldReplace(DenialConstraintSet.MinimalDCCandidate prior) {
            if (prior == null)
                return true;
            if (dc.getPredicateCount() < prior.dc.getPredicateCount())
                return true;
            if (dc.getPredicateCount() > prior.dc.getPredicateCount())
                return false;

            return bitset.compareTo(prior.bitset) <= 0;
        }
    }
    public void minimize() {
        Map<PredicateSet, DenialConstraintSet.MinimalDCCandidate> constraintsClosureMap = new HashMap<>();
        for (DenialConstraint dc : constraints) {
            PredicateSet predicateSet = dc.getPredicateSet();
            Closure c = new Closure(predicateSet);
            if (c.construct()) {
                DenialConstraintSet.MinimalDCCandidate candidate = new DenialConstraintSet.MinimalDCCandidate(dc);
                PredicateSet closure = c.getClosure();
                DenialConstraintSet.MinimalDCCandidate prior = constraintsClosureMap.get(closure);
                if (candidate.shouldReplace(prior))
                    constraintsClosureMap.put(closure, candidate);
            }
        }

        List<Map.Entry<PredicateSet, DenialConstraintSet.MinimalDCCandidate>> constraints2 = new ArrayList<>(constraintsClosureMap.entrySet());

        constraints2.sort(Comparator
                .comparingInt((Map.Entry<PredicateSet, DenialConstraintSet.MinimalDCCandidate> entry) -> entry.getKey().size())
                .thenComparingInt(entry -> entry.getValue().dc.getPredicateCount())
                .thenComparing(entry -> entry.getValue().bitset));

        constraints = new HashSet<>();
        NTreeSearch tree = new NTreeSearch();
        for (Map.Entry<PredicateSet, DenialConstraintSet.MinimalDCCandidate> entry : constraints2) {
            if (tree.containsSubset(PredicateSetFactory.create(entry.getKey()).getBitset()))
                continue;

            DenialConstraint inv = entry.getValue().dc.getInvT1T2DC();
            if (inv != null) {
                Closure c = new Closure(inv.getPredicateSet());
                if (!c.construct())
                    continue;
                if (tree.containsSubset(PredicateSetFactory.create(c.getClosure()).getBitset()))
                    continue;
            }

            constraints.add(entry.getValue().dc);
            tree.add((LongBitSet) entry.getValue().bitset);
            if (inv != null)
                tree.add(PredicateSetFactory.create(inv.getPredicateSet()).getBitset());
        }
    }*/
}
