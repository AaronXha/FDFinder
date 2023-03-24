package FD;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FunctionDependency {
    int nAttributes;

    List<List<BitSet>> minFDs = new ArrayList<>();      // min HSs on each RHS

    public FunctionDependency(){}

    public FunctionDependency(int _nAttributes){
        nAttributes = _nAttributes;
    }

    public void add(List<BitSet> fd){
        minFDs.add(fd);
    }
}
