import Basic.FASTA.FastaReader;
import Basic.FASTA.FastaRecord;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDRACHMotif {
    private LinkedList<FastaRecord> bgPeakSeqList, tgPeakSeqList;
    private double pValue, chiSquare, oddRatio;

    public TestDRACHMotif(File bgSeqFile, File tgSeqFile) {
        FastaReader bgFasReader = new FastaReader(bgSeqFile, FastaReader.Nucleotide);
        bgPeakSeqList = bgFasReader.getFastaRecList();

        FastaReader tgFasReader = new FastaReader(tgSeqFile, FastaReader.Nucleotide);
        tgPeakSeqList = tgFasReader.getFastaRecList();
    }

    public void OddRatioTest() {
        long bgNotDRACH = 0, bgDRACH = 0, tgNotDRACH = 0, tgDRACH = 0;
        Pattern DRACH = Pattern.compile("[AGT][GA]AC[ACT]");
        for(FastaRecord fasRec : bgPeakSeqList) {
            String seq = fasRec.getSequence();
            for(int i=0; i<seq.length() - 5; i++) {
                String subSeq = seq.substring(i, i + 5);
                Matcher matcher = DRACH.matcher(subSeq);
                if(matcher.matches())
                    bgDRACH++;
                else
                    bgNotDRACH++;
            }
        }
        for(FastaRecord fasRec : tgPeakSeqList) {
            String seq = fasRec.getSequence();
            for(int i=0; i<seq.length() - 5; i++) {
                String subSeq = seq.substring(i, i + 5);
                Matcher matcher = DRACH.matcher(subSeq);
                if(matcher.matches())
                    tgDRACH++;
                else
                    tgNotDRACH++;
            }
        }
        //
        long[][] table = new long[2][2];
        table[0][0] = tgDRACH;
        table[0][1] = tgNotDRACH;
        table[1][0] = bgDRACH;
        table[1][1] = bgNotDRACH;
        ChiSquareTest chiSquareTest = new ChiSquareTest();
        chiSquare = chiSquareTest.chiSquare(table);
        ChiSquaredDistribution chiSquareDis = new ChiSquaredDistribution(1);
        pValue = 1 - chiSquareDis.cumulativeProbability(chiSquare);
        oddRatio = ((double)tgDRACH/(double)tgNotDRACH)/((double)bgDRACH/(double)bgNotDRACH);
    }

    public double getOddRatio() {
        return oddRatio;
    }

    public double getpValue() {
        return pValue;
    }

    public double getChiSquare() {
        return chiSquare;
    }

    public static void main(String[] args) {
        TestDRACHMotif testDRACHMotif = new TestDRACHMotif( new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\Input-IP_Annotated.fa"),
                new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\peaksWithSite_Annotated.fa"));
        testDRACHMotif.OddRatioTest();
        System.out.println("Odd ratio = " + testDRACHMotif.getOddRatio()
                + "\tChi square = " + testDRACHMotif.getChiSquare()
                + "\tp value = " + testDRACHMotif.getpValue());
    }
}
