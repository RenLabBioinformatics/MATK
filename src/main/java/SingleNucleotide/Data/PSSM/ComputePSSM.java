package SingleNucleotide.Data.PSSM;

import SingleNucleotide.Data.SiteReader;
import SingleNucleotide.Data.SiteRecord;
import org.apache.commons.math3.stat.inference.TTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class ComputePSSM {
    private String alphabet;
    double[][] matrix;
    private LinkedList<String> positiveSiteList;
    private LinkedList<String> negativeSiteList;
    private final double backgroundFreq = 1e-100;

    public ComputePSSM(File positiveSiteFile, File negativeSiteFile, String alphabet) {
        SiteReader siteReader = new SiteReader();
        siteReader.ReadSites(positiveSiteFile, 1, true);
        siteReader.ReadSites(negativeSiteFile,0,true);

        positiveSiteList = new LinkedList<>();
        negativeSiteList = new LinkedList<>();

        for(SiteRecord siteRecord : siteReader.getSiteRecList()) {
            if(siteRecord.getLabel() == 1)
                positiveSiteList.add(siteRecord.getSequence());
            else
                negativeSiteList.add(siteRecord.getSequence());
        }

        this.alphabet = alphabet;
    }

    private double getPValue(char code, int position) {
        double[] positiveVec = new double[positiveSiteList.size()];
        double[] negativeVec = new double[negativeSiteList.size()];

        int i = 0;
        for(String sequence : positiveSiteList) {
            char seqCode = sequence.charAt(position);
            if(seqCode == code)
                positiveVec[i] = 1;
            else
                positiveVec[i] = 0;
            i++;
        }

        i = 0;
        for(String sequence : negativeSiteList) {
            char seqCode = sequence.charAt(position);
            if(seqCode == code)
                negativeVec[i] = 1;
            else
                negativeVec[i] = 0;
            i++;
        }

        TTest tTest = new TTest();
        return tTest.tTest(positiveVec, negativeVec);
    }

    private double[] CalculateFrequency(char code, int position) {
        double[] freqVec = new double[2];

        for(String sequence : positiveSiteList) {
            char curCode = sequence.charAt(position);
            if(curCode == code)
                freqVec[0]++;
        }
        freqVec[0] = freqVec[0]/((double) positiveSiteList.size());

        for(String sequence : negativeSiteList) {
            char curCode = sequence.charAt(position);
            if(curCode == code)
                freqVec[1]++;
        }
        freqVec[1] = freqVec[1]/((double) negativeSiteList.size());

        return freqVec;
    }

    public void Compute() {
        matrix = new double[alphabet.length()][positiveSiteList.getFirst().length()];
        //Compute probability matrix
        double[][] probabilityMatrix = new double[matrix.length][matrix[0].length];
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[i].length; j++) {
                probabilityMatrix[i][j] = getPValue(alphabet.charAt(i), j) + backgroundFreq;
            }
        }
        //Compute frequency matrix
        double[][] posFreqMatrix = new double[matrix.length][matrix[0].length];
        double[][] negFreqMatrix = new double[matrix.length][matrix[0].length];
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[i].length; j++) {
                double[] freq = CalculateFrequency(alphabet.charAt(i), j);
                posFreqMatrix[i][j] = freq[0];
                negFreqMatrix[i][j] = freq[1];
            }
        }
        //Compute PSSM matrix
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[i].length; j++) {
                double delta = (posFreqMatrix[i][j] - negFreqMatrix[i][j])/probabilityMatrix[i][j];
                if(delta >= 0)
                    matrix[i][j] = Math.log(Math.abs(delta) + 1);
                else
                    matrix[i][j] = -1 * Math.log(Math.abs(delta) + 1);
            }
        }
    }

    public void SavePSSM(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            //Write title
            String tmpStr = "";
            for(int i=0; i<matrix[0].length; i++) {
                tmpStr = tmpStr + "P" + (i+1) + "\t";
            }
            fw.write(tmpStr.substring(0, tmpStr.length() - 1) + "\n");
            //Write data
            for(int i=0; i<matrix.length; i++) {
                fw.write(alphabet.charAt(i));
                for(int j=0; j<matrix[0].length; j++) {
                    if(Double.isNaN(matrix[i][j]))
                        fw.write("\t0");
                    else
                        fw.write("\t" + matrix[i][j]);
                }
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ComputePSSM computePSSM = new ComputePSSM(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\MeRIPSeq\\Mouse\\Mouse_Combined_Positive.txt"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\MeRIPSeq\\Mouse\\Mouse_Combined_Negative.txt"),
                "ATCG");
        computePSSM.Compute();
        computePSSM.SavePSSM(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_PSSM.txt"));
    }
}
