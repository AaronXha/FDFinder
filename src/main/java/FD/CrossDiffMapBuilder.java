package FD;

import FastADC.plishard.Pli;
import FastADC.plishard.PliShard;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.List;

import static FD.FDFinder.nAttributes;

public class CrossDiffMapBuilder {
    private final List<Pli> plis1, plis2;
    private final int tidBeg1, tidBeg2;
    private final int tidRange1, tidRange2;
    private final int differenceCount;

    public CrossDiffMapBuilder(PliShard shard1, PliShard shard2) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        tidBeg1 = shard1.beg;
        tidBeg2 = shard2.beg;
        tidRange1 = shard1.end - shard1.beg;
        tidRange2 = shard2.end - shard2.beg;
        differenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
    }

    public HashLongLongMap buildDiffMap(){
        long[] differenceValues = new long[differenceCount];   // plis1 -> plis2

        HashLongLongMap diffMap = HashLongLongMaps.newMutableMap();

        /** get all differenceValue*/
        /** for every attribute*/
        for(int e = 0; e < nAttributes; e++){
            Pli pivotPli = plis1.get(e);
            Pli probePli = plis2.get(e);
            long mask = 1L << e;
            /** for every cluster in pli*/
            int[] pivotKeys = pivotPli.getKeys();
            for (int i = 0; i < pivotKeys.length; i++) {
                Integer j = probePli.getClusterIdByKey(pivotKeys[i]);
                if (j != null) {
                    /** for every tuple in cluster*/
                    for (int tid1 : pivotPli.get(i).getRawCluster()) {
                        int t1 = tid1 - tidBeg1, r1 = t1 * tidRange2;
                        for (int tid2 : probePli.get(j).getRawCluster()) {
                            int t2 = tid2 - tidBeg2, r2 = t2 * tidRange1;
                            differenceValues[r1 + t2] |= mask;
                        }
                    }
                }
            }
        }

        /** put differenceValue and count to diffMap*/
        for(long differenceValue :differenceValues){
            diffMap.addValue(differenceValue, 1L, 0L);
        }

        return diffMap;

    }

}
