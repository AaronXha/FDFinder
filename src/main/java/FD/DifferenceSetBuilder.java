package FD;

import FastADC.plishard.PliShard;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import com.koloboke.function.LongLongConsumer;

import java.util.ArrayList;
import java.util.List;

public class DifferenceSetBuilder {
    private DifferenceSet fullDifferenceSet;
    private int nAttributes;
    private long totalCount;

    public DifferenceSetBuilder(int _nAttributes, long _totalCount){
        nAttributes = _nAttributes;
        totalCount = _totalCount;
    }

    public DifferenceSet build(PliShard[] pliShards, boolean linear) {

        if (pliShards.length != 0) {
            fullDifferenceSet = linear ? linearBuildDifferenceSet(pliShards) : buildDifferenceSet(pliShards);
        }
        return fullDifferenceSet;
    }

    private DifferenceSet linearBuildDifferenceSet(PliShard[] pliShards) {

        HashLongLongMap diffMap = HashLongLongMaps.newMutableMap();
        List<Difference> differences = new ArrayList<>();

        LongLongConsumer add = (k, v) -> diffMap.addValue(k, v, 0L);

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                HashLongLongMap partialDiffMap;
                if(i == j){
                    SingleDiffMapBuilder singleDiffMapBuilder = new SingleDiffMapBuilder(pliShards[i], nAttributes);
                    partialDiffMap = singleDiffMapBuilder.buildDiffMap();
                }
                else{
                    CrossDiffMapBuilder crossDiffMapBuilder = new CrossDiffMapBuilder(pliShards[i], pliShards[j], nAttributes);
                    partialDiffMap = crossDiffMapBuilder.buildDiffMap();
                }
                partialDiffMap.forEach(add);
            }
        }

        for (var entry : diffMap.entrySet()) {
            differences.add(new Difference(entry.getKey(), entry.getValue(), nAttributes));
        }


        return new DifferenceSet(differences, totalCount, nAttributes);
    }


    private DifferenceSet buildDifferenceSet(PliShard[] pliShards) {
        HashLongLongMap diffMap;
        List<Difference> differences = new ArrayList<>();

        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;

        DifferenceSetTask rootTask = new DifferenceSetTask(null, pliShards, 0, taskCount, nAttributes);
        diffMap = rootTask.invoke();

        for (var entry : diffMap.entrySet()) {
            differences.add(new Difference(entry.getKey(), entry.getValue(), nAttributes));
        }

        return new DifferenceSet(differences, totalCount, nAttributes);
    }
}
