package FD;

import java.util.List;

public class DifferenceSet {

    private List<Difference> differences;

    private long totalCount;

    int nAttributes;

    public DifferenceSet(List<Difference> _differences, long _totalCount, int _nAttributes) {
        differences = _differences;
        totalCount = _totalCount;
        nAttributes = _nAttributes;
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
