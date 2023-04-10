package FD;

import ch.javasoft.bitset.LongBitSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class AttributeOrganizer {
    private final int nAttributes;
    private final DifferenceSet differenceSet;
    private final int[] indexes;    // new index -> original index

    public AttributeOrganizer(int n, DifferenceSet _differenceSet) {
        nAttributes = n;
        differenceSet = _differenceSet;

        int[] coverages = getPredicateCoverage(differenceSet);
        indexes = createIndexArray(coverages);
    }

    private int[] getPredicateCoverage(DifferenceSet differenceSet) {
        int[] counts = new int[nAttributes];
        for (Difference difference : differenceSet.getDifferences()) {
            LongBitSet bitset = difference.getBitSet();
            for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
                counts[i]++;
            }
        }
        return counts;
    }

    public int[] createIndexArray(int[] coverages) {
        return IntStream.range(0, coverages.length)
                .boxed()
                .sorted(Comparator.comparingInt(i -> coverages[i]))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public List<Difference> transformDifferenceSet() {
        List<Difference> differences = new ArrayList<>();
        int n = 0;
        for (Difference e : differenceSet.getDifferences()) {
            differences.add(new Difference(transform(e.getBitSet()), e.count, nAttributes));
            n++;
        }
        return differences;
    }

    public LongBitSet transform(LongBitSet bitset) {
        LongBitSet bitset2 = new LongBitSet();
        for (Integer i : indexes) {
            if (bitset.get(indexes[i]))
                bitset2.set(i);
        }
        return bitset2;
    }

    public LongBitSet[] transformMutexMap(LongBitSet[] mutexMap) {
        LongBitSet[] transMutexMap = new LongBitSet[mutexMap.length];
        for (int i = 0; i < mutexMap.length; i++) {
            transMutexMap[transform(i)] = transform(mutexMap[i]);
        }
        return transMutexMap;
    }

    public int transform(int e) {
        for (int i : indexes)
            if (e == indexes[i]) return i;
        return -1;
    }

    public LongBitSet retransform(LongBitSet bitset) {
        LongBitSet valid = new LongBitSet();
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
            valid.set(indexes[i]);
        }
        return valid;
    }

}
