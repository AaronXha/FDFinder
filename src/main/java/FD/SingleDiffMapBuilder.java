package FD;

import FastADC.plishard.Pli;
import FastADC.plishard.PliShard;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.List;

import static FD.FDFinder.nAttributes;

public class SingleDiffMapBuilder {

    private final List<Pli> plis;
    private final int tidBeg;
    private final int differenceCount;

    public SingleDiffMapBuilder(PliShard shard) {
        plis = shard.plis;
        tidBeg = shard.beg;
        int tidRange = shard.end - shard.beg;
        differenceCount = (tidRange - 1) * tidRange / 2;
    }

    public HashLongLongMap buildDiffMap(){
        long[] differenceValues = new long[differenceCount];
        HashLongLongMap diffMap = HashLongLongMaps.newMutableMap();

        /** get all differenceValue*/
        /** for every attribute*/
        for(int e = 0; e < nAttributes; e++){
            Pli pliE = plis.get(e);
            long mask = 1L << e;
            /** for every cluster in pli*/
            for(int k = 0; k < pliE.size(); k++){
                if(pliE.get(k).size() > 1){
                    List<Integer> rawCluster = pliE.get(k).getRawCluster();
                    /** for every tuple in cluster*/
                    for(int i = 0; i < rawCluster.size() - 1; i++){
                        int t1 = rawCluster.get(i) - tidBeg;
                        for (int j = i + 1 ; j < rawCluster.size(); j++) {
                            int t2 = rawCluster.get(j) - tidBeg;
                            if(t1 == t2)   continue;
                            int tMin = Integer.min(t1, t2), tMax = Integer.max(t1, t2);
                            int pos = (tMax - 1) * tMax / 2 + tMin;
                            differenceValues[pos] |= mask;
                            //differenceValues[pos] &= ~mask;
                        }
                    }
                }
            }
        }
        /** first put differenceValue and count to diffMap*/
        for(long differenceValue :differenceValues){
            diffMap.addValue(differenceValue, 1L, 0L);
        }

        return diffMap;
    }

}
