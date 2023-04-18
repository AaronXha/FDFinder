package FD;

public class test {
    public static void main(String[] args) {
        String fp = "./dataset/CAB/CAB_34_10000.csv";
        double threshold = 0.01d;
        int rowLimit = -1;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 200;
        boolean linear_1 = false;         // linear single-thread in DifferenceSetBuilder
        boolean linear_2 = false;         // linear single-thread in ApproxDifferenceInverter

        String dsFp = "./dataset/DifferenceFile/CAB_34_10000.txt";
        int nAttributes = 34;

        //FDFinder fdFinder = new FDFinder(threshold, shardLength, linear_1, linear_2);
        //fdFinder.buildApproxFDs(fp, rowLimit);

        FDFinder fdFinderFromFile = new FDFinder(threshold, shardLength, nAttributes, linear_2);
        fdFinderFromFile.buildApproxFDsFromFile(dsFp, rowLimit);

        System.out.println();
    }
}
