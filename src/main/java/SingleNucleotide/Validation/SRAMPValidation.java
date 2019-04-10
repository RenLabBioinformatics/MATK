package SingleNucleotide.Validation;

import java.io.*;
import java.util.LinkedList;

public class SRAMPValidation {
    private LinkedList<Double> positiveScoreList, negativeScoreList;
    private final double centerPosition = 108;
    private LinkedList<Performance> perfList;

    private void ReadSRAMPScore(File readFile, boolean isPositive) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(readFile));
            String strLine = br.readLine(); // skip the first line
            String[] strArr;
            while (br.ready()) {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                int position = Integer.parseInt(strArr[1]);
                double score = Double.parseDouble(strArr[4]);
                if(position == centerPosition) {
                    if(isPositive)
                        positiveScoreList.add(score);
                    else
                        negativeScoreList.add(score);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SRAMPValidation(File positiveFile, File negativeFile) {
        positiveScoreList = new LinkedList<>();
        negativeScoreList = new LinkedList<>();
        ReadSRAMPScore(positiveFile, true);
        ReadSRAMPScore(negativeFile, false);
        PerformanceCalculator perfCal = new PerformanceCalculator(positiveScoreList, negativeScoreList);
        perfCal.FullPerformance();
        perfList = perfCal.getPerformanceList();
    }

    public void SavePerformanceList(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("Ac\tSn\tSp\tMcc\tPr\n");
            for(Performance perf : perfList) {
                fw.write(perf + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SRAMPValidation srampValidation = new SRAMPValidation(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TestData\\Zebrafish_SRAMP_Positive.txt"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TestData\\Zebrafish_SRAMP_Negative.txt"));
        srampValidation.SavePerformanceList(new File("E:\\工作文档\\MATK\\SingleNucleotide\\TrainingResult\\MeRIPSeq\\Zebrafish_SRAMP_Performance.txt"));
    }
}
