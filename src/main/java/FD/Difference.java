package FD;

import FastADC.evidence.evidenceSet.Evidence;
import ch.javasoft.bitset.LongBitSet;

import java.util.BitSet;

public class Difference {

    public long count;
    private long differenceValue;
    private LongBitSet bitset;
    private int nAttributes;

    public Difference(long _differenceValue, long _count, int _nAttributes) {
        differenceValue = _differenceValue;
        count = _count;
        nAttributes = _nAttributes;
        bitset =  longToBitSet(_nAttributes, _differenceValue);
    }

    public Difference(LongBitSet _bitset, long _count, int _nAttributes){
        bitset = _bitset;
        count = _count;
        nAttributes = _nAttributes;
        differenceValue = bitsetToLong(_nAttributes, _bitset);
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
