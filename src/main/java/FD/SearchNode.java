package FD;

import FastADC.aei.DCCandidate;
import FastADC.aei.LongBitSetTrie;
import ch.javasoft.bitset.LongBitSet;

import java.util.Collection;

public class SearchNode {
    int e;
    LongBitSet addableAttributes;
    LongBitSetTrie fdCandidates;
    Collection<FDCandidate> invalidFDs;
    long target;

    String H;

    public SearchNode(int e, LongBitSet addableAttributes, LongBitSetTrie fdCandidates,
                      Collection<FDCandidate> invalidFDs, long target, String status) {
        this.e = e;
        this.addableAttributes = addableAttributes;
        this.fdCandidates = fdCandidates;
        this.invalidFDs = invalidFDs;
        this.target = target;
        H = status;
    }
}
