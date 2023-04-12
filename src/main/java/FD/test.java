package FD;

public class test {
    public static void main(String[] args) {
        String fp = "./dataset/food.csv";
        double threshold = 0.01d;
        int rowLimit = 3000;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 200;
        boolean linear = false;         // linear single-thread in DifferenceSetBuilder

        String dsFp = "./dataset/DifferenceFile/horse_240.txt";
        int nAttributes = 28;

        //FDFinder fdFinder = new FDFinder(threshold, shardLength, linear);
        //fdFinder.buildApproxFDs(fp, rowLimit);

        FDFinder fdFinderFromFile = new FDFinder(threshold, shardLength, linear, nAttributes);
        fdFinderFromFile.buildApproxFDsFromFile(dsFp, rowLimit);

        System.out.println();
    }
}
