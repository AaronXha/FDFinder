package FD;

public class test {
    public static void main(String[] args) {
        String fp = "./dataset/airport.csv";
        double threshold = 0.01d;
        int rowLimit = 10;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 10;
        boolean linear = true;         // linear single-thread in DifferenceSetBuilder

        FDFinder fdFinder = new FDFinder(threshold, shardLength, linear);
        fdFinder.buildApproxFDs(fp, rowLimit);
        System.out.println();
    }
}
