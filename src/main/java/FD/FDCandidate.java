package FD;

import FastADC.aei.DCCandidate;
import ch.javasoft.bitset.LongBitSet;

public class FDCandidate {
    public LongBitSet bitSet;
    LongBitSet cand;

    public FDCandidate(LongBitSet bitSet) {
        this.bitSet = bitSet;
    }

    public FDCandidate(LongBitSet bitSet, LongBitSet cand) {
        this.bitSet = bitSet;
        this.cand = cand;
    }

    @Override
    public int hashCode() {
        return bitSet.hashCode();
    }

    @Override
    public FDCandidate clone() {
        return new FDCandidate(bitSet.clone(), cand.clone());
    }
}
