package FD;

import ch.javasoft.bitset.LongBitSet;

import java.util.ArrayList;
import java.util.List;

public class FunctionDependency {
    int totalCount;
    List<List<LongBitSet>> minFDs = new ArrayList<>();      // min HSs on each RHS

    public FunctionDependency(){
        totalCount = 0;
    }

    public void add(List<LongBitSet> fd){
        minFDs.add(fd);
        totalCount += fd.size();
    }

    public int getTotalCount(){
        return totalCount;
    }


}
