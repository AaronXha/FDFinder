package FD;

import ch.javasoft.bitset.LongBitSet;

import static FD.FDFinder.nAttributes;

public class Difference {

    public long count;
    private final long differenceValue;
    private final LongBitSet bitset;

    public Difference(long _differenceValue, long _count) {
        differenceValue = _differenceValue;
        count = _count;
        bitset =  longToBitSet(nAttributes, _differenceValue);
    }

    public Difference(LongBitSet _bitset, long _count){
        bitset = _bitset;
        count = _count;
        differenceValue = bitsetToLong(nAttributes, _bitset);
    }

    public long getDifferenceValue(){
        return differenceValue;
    }

    public LongBitSet getBitSet() {
        return bitset;
    }

    public long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Difference difference = (Difference) o;
        return differenceValue == difference.differenceValue;
    }

    @Override
    public int hashCode() {
        return (int) (differenceValue ^ (differenceValue >>> 32));
    }

    public LongBitSet longToBitSet(int nAttributes, long n) {
        LongBitSet bs = new LongBitSet(nAttributes);
        for (int i = 0; i < nAttributes; i++)
            if ((n & (1L << i)) != 0) bs.set(i);
        return bs;
    }

    public long bitsetToLong(int nAttributes, LongBitSet bs) {
        long x = 0;
        for (int i = 0; i < nAttributes; i++)
            if (bs.get(i)) x |= (1L << i);
        return x;
    }
}
