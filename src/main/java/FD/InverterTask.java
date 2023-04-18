package FD;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;

import static FD.FDFinder.nAttributes;

public class InverterTask extends CountedCompleter<List<LongBitSet>> {
    final int taskBeg, taskEnd;
    List<DifferenceSet> subsetDifferenceSet;
    InverterTask sibling;
    List<LongBitSet> rawFDs;
    public InverterTask(InverterTask parent, List<DifferenceSet> _subsetDifferenceSet, int _beg, int _end){
        super(parent);
        subsetDifferenceSet = _subsetDifferenceSet;
        taskBeg = _beg;
        taskEnd = _end;
    }

    @Override
    public void compute() {
        if (taskEnd - taskBeg >= 2) {
            int mid = (taskBeg + taskEnd) >>> 1;
            InverterTask left = new InverterTask(this, subsetDifferenceSet, taskBeg, mid);
            InverterTask right = new InverterTask(this, subsetDifferenceSet, mid, taskEnd);
            left.sibling = right;
            right.sibling = left;
            setPendingCount(1);
            right.fork();
            left.compute();
        } else {
            if (taskEnd > taskBeg) {
                ApproxFDBuilder approxFDBuilder = new ApproxFDBuilder(subsetDifferenceSet.get(taskBeg), taskBeg);
                rawFDs = approxFDBuilder.build();
            }
            tryComplete();
        }
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller != this) {
            InverterTask child = (InverterTask) caller;
            InverterTask childSibling = child.sibling;

            rawFDs = child.rawFDs;
            if (childSibling != null && childSibling.rawFDs != null) {
                rawFDs.addAll(childSibling.rawFDs);
            }
        }
    }

    @Override
    public List<LongBitSet> getRawResult() {
        return rawFDs == null ? new ArrayList<>() : rawFDs;
    }
}
