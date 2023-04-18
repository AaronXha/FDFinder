package FD;

import java.util.List;

public class DifferenceSet {

    private final List<Difference> differences;

    private final long totalCount;


    public DifferenceSet(List<Difference> _differences, long _totalCount) {
        differences = _differences;
        totalCount = _totalCount;
    }

    public int size(){
        return differences.size();
    }

    public long getTotalCount(){
        return  totalCount;
    }

    public List<Difference> getDifferences(){
        return differences;
    }

}
