package FD;

import FastADC.aei.LongBitSetTrie;
import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import static FD.FDFinder.nAttributes;

public class ApproxDifferenceInverter {
    public static long totalCount;
    public static double error;

    public ApproxDifferenceInverter() {
        LongBitSetTrie.N = nAttributes;
    }

    public FunctionDependency build(DifferenceSet differenceSet, double error, boolean linear){
        totalCount = differenceSet.getTotalCount();

        /** for every attribute as RHS, get subset of differenceSet*/
        List<DifferenceSet> subsetDifferenceSet = getSubsetDifferenceSetList(differenceSet);

        if (linear) return linearBuildFD(subsetDifferenceSet);
        else    return buildFD(subsetDifferenceSet);
    }

    public FunctionDependency linearBuildFD(List<DifferenceSet> subsetDifferenceSet) {
        FunctionDependency fds = new FunctionDependency();

        /** for every attribute as RHS*/
        System.out.println("  [ADI] Inverting differences...");
        for (int i = 0; i < nAttributes; i++) {
            ApproxFDBuilder approxFDBuilder = new ApproxFDBuilder(subsetDifferenceSet.get(i), i);
            fds.add(approxFDBuilder.build());
        }
        System.out.println("  [ADI] Total FD size: " + fds.getTotalCount());
        return fds;
    }

    public FunctionDependency buildFD(List<DifferenceSet> subsetDifferenceSet){
        FunctionDependency fds = new FunctionDependency();

        /** for every attribute as RHS*/
        System.out.println("  [ADI] Inverting differences...");

        InverterTask rootTask = new InverterTask(null, subsetDifferenceSet, 0,  nAttributes);
        fds.add(rootTask.invoke());

        System.out.println("  [ADI] Total FD size: " + fds.getTotalCount());

        return fds;
    }

    List<DifferenceSet> getSubsetDifferenceSetList(DifferenceSet differenceSet){
        List<DifferenceSet> subsetDifferenceSetList = new ArrayList<>();

        /** generate subset differenceSet for every attribute*/
        for(int i = 0; i < nAttributes; i++){
            HashLongLongMap subsetDiffMap = HashLongLongMaps.newMutableMap();
            long mask = 1L << i;

            /** generate differenceMap for every attribute*/
            for (Difference subset : differenceSet.getDifferences()) {
                long temp = subset.getDifferenceValue();
                if ((~temp & mask) != 0L) {
                    long value = temp | mask;
                    subsetDiffMap.addValue(value, subset.getCount(), 0L);
                }
            }
            /** generate subset differenceSet for every attribute*/
            long diffCount = 0;
            List<Difference> subsetDifferences = new ArrayList<>();
            for (var entry : subsetDiffMap.entrySet()) {
                subsetDifferences.add(new Difference(entry.getKey(), entry.getValue()));
                diffCount +=  entry.getValue();
            }
            subsetDifferenceSetList.add(new DifferenceSet(subsetDifferences, diffCount));
        }
        return subsetDifferenceSetList;
    }

}
