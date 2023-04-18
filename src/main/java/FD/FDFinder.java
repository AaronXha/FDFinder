package FD;

import FastADC.plishard.PliShard;
import FastADC.plishard.PliShardBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;

public class FDFinder {
    public static int nAttributes;
    // configure of PliShardBuilder
    private final int shardLength;
    // configure of DifferenceSetBuilder
    private boolean linear_1;
    // configure of ApproxCoverSearcher
    private boolean linear_2;
    private final double threshold;
    private String dataFp;
    private DifferenceSetBuilder differenceSetBuilder;

    public FDFinder(double _threshold, int _len, int _nAttributes, boolean _linear_2){
        this.threshold = _threshold;
        this.shardLength = _len;
        nAttributes = _nAttributes;
        linear_2 = _linear_2;
    }

    public FDFinder(double _threshold, int _len, boolean _linear_1, boolean _linear_2){
        this.threshold = _threshold;
        this.shardLength = _len;
        linear_1 = _linear_1;
        linear_2 = _linear_2;
    }

    public void buildApproxFDsFromFile(String _dsFp, int sizeLimit){
        System.out.println("INPUT FILE: " + dataFp);
        System.out.println("ERROR THRESHOLD: " + threshold);
        System.out.println("LOAD DIFFERENCE FROM FILE");

        // Pre-process: load input data and build difference set
        long t00 = System.currentTimeMillis();
        differenceSetBuilder = new DifferenceSetBuilder();
        DifferenceSet differenceSet = differenceSetBuilder.buildFromFile(_dsFp, sizeLimit);
        long t_pre = System.currentTimeMillis() - t00;
        System.out.println(" [Attribute] Attribute number: " + nAttributes);
        System.out.println("[FDFinder] Pre-process time: " + t_pre + "ms");
        System.out.println(" [Difference] # of differences: " + differenceSet.size());
        System.out.println(" [Difference] Accumulated difference count: " + differenceSet.getTotalCount());

        // approx difference inversion
        long t20 = System.currentTimeMillis();
        ApproxDifferenceInverter differenceInverter = new ApproxDifferenceInverter();
        differenceInverter.build(differenceSet, threshold, linear_2);
        long t_adi = System.currentTimeMillis() - t20;
        System.out.println("[FDFinder] ADI total time: " + t_adi + "ms");

        System.out.println("[FDFinder] Total computing time: " + (t_pre + t_adi) + " ms\n");
    }

    public void buildApproxFDs(String _dataFp, int sizeLimit){
        dataFp = _dataFp;
        System.out.println("INPUT FILE: " + dataFp);
        System.out.println("ERROR THRESHOLD: " + threshold);
        System.out.println("BUILD DIFFERENCE SET");

        // Pre-process: load input data
        long t00 = System.currentTimeMillis();
        Input input = new Input(new RelationalInput(dataFp), sizeLimit);
        PliShardBuilder pliShardBuilder = new PliShardBuilder(shardLength, input.getParsedColumns());
        nAttributes = input.getColCount();
        long nTuples = input.getRowCount();
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getIntInput());
        long t_pre = System.currentTimeMillis() - t00;
        System.out.println(" [Attribute] Attribute number: " + nAttributes);
        System.out.println("[FDFinder] Pre-process time: " + t_pre + "ms");

        //build difference set
        long t10 = System.currentTimeMillis();
        long differenceCount = nTuples * (nTuples - 1) / 2;
        differenceSetBuilder = new DifferenceSetBuilder(differenceCount);
        DifferenceSet differenceSet = differenceSetBuilder.build(pliShards, linear_1);
        long t_diff = System.currentTimeMillis() - t10;
        System.out.println(" [Difference] # of differences: " + differenceSet.size());
        System.out.println(" [Difference] Accumulated difference count: " + differenceSet.getTotalCount());
        System.out.println("[FDFinder] Build differenceSet time: " + t_diff + "ms");

        // approx difference inversion
        long t20 = System.currentTimeMillis();
        ApproxDifferenceInverter differenceInverter = new ApproxDifferenceInverter();
        differenceInverter.build(differenceSet, threshold, linear_2);
        long t_adi = System.currentTimeMillis() - t20;
        System.out.println("[FDFinder] ADI time: " + t_adi + "ms");

        System.out.println("[FDFinder] Total computing time: " + (t_pre + t_diff + t_adi) + " ms\n");


    }

}
