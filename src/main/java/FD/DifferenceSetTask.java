package FD;


import FastADC.plishard.PliShard;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.concurrent.CountedCompleter;

public class DifferenceSetTask extends CountedCompleter<HashLongLongMap> {
    private static int[] searchIndexes;

    private static void buildSearchIndex(int count) {
        if (searchIndexes == null || searchIndexes[searchIndexes.length - 1] < count) {
            int n = (int) Math.sqrt(2 * count + 2) + 3;
            searchIndexes = new int[n];
            for (int i = 1; i < n; i++)
                searchIndexes[i] = searchIndexes[i - 1] + i + 1;
        }
    }

    final int taskBeg, taskEnd;
    PliShard[] pliShards;

    DifferenceSetTask sibling;
    HashLongLongMap partialDiffMap;

    public DifferenceSetTask(DifferenceSetTask parent, PliShard[] _pliShards, int _beg, int _end) {
        super(parent);
        pliShards = _pliShards;
        taskBeg = _beg;
        taskEnd = _end;
        buildSearchIndex(taskEnd);
    }

    @Override
    public void compute() {
        if (taskEnd - taskBeg >= 2) {
            int mid = (taskBeg + taskEnd) >>> 1;
            DifferenceSetTask left = new DifferenceSetTask(this, pliShards, taskBeg, mid);
            DifferenceSetTask right = new DifferenceSetTask(this, pliShards, mid, taskEnd);
            left.sibling = right;
            right.sibling = left;

            setPendingCount(1);
            right.fork();

            left.compute();
        } else {
            if (taskEnd > taskBeg) {
                // taskID = i*(i+1)/2 + j
                int i = lowerBound(searchIndexes, taskBeg);
                int j = i - (searchIndexes[i] - taskBeg);

                if(i == j){
                    SingleDiffMapBuilder singleDiffMapBuilder = new SingleDiffMapBuilder(pliShards[i]);
                    partialDiffMap = singleDiffMapBuilder.buildDiffMap();
                }
                else{
                    CrossDiffMapBuilder crossDiffMapBuilder = new CrossDiffMapBuilder(pliShards[i], pliShards[j]);
                    partialDiffMap = crossDiffMapBuilder.buildDiffMap();
                }
            }
            tryComplete();
        }

    }


    // return the index of the first num that's >= target, or nums.length if no such num
    private int lowerBound(int[] nums, int target) {
        int l = 0, r = nums.length;
        while (l < r) {
            int m = l + ((r - l) >>> 1);
            if (nums[m] >= target) r = m;
            else l = m + 1;
        }
        return l;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller != this) {
            DifferenceSetTask child = (DifferenceSetTask) caller;
            DifferenceSetTask childSibling = child.sibling;

            partialDiffMap = child.partialDiffMap;
            if (childSibling != null && childSibling.partialDiffMap != null) {
                for (var e : childSibling.partialDiffMap.entrySet())
                    partialDiffMap.addValue(e.getKey(), e.getValue(), 0L);
            }
        }
    }

    @Override
    public HashLongLongMap getRawResult() {
        return partialDiffMap == null ? HashLongLongMaps.newMutableMap() : partialDiffMap;
    }
}
