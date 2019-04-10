package SingleNucleotide.Validation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/5.
 */
public class PerformanceCalculator {
    private LinkedList<Double> positiveScoreList;
    private LinkedList<Double> negativeScoreList;
    private LinkedList<Performance> performanceList = new LinkedList<Performance>();

    public PerformanceCalculator(LinkedList<Double> positiveScoreList, LinkedList<Double> negativeScoreList) {
        this.positiveScoreList = positiveScoreList;
        this.negativeScoreList = negativeScoreList;
    }

    public Performance getPerformance(double sp) {
        Collections.sort(negativeScoreList);
        int cutoffIndex = (int)(negativeScoreList.size() * sp);
        double cutoff = negativeScoreList.get(cutoffIndex);

        Collections.sort(positiveScoreList);
        int count = 0;
        for(double score : positiveScoreList) {
            count++;
            if(score > cutoff)
                break;
        }

        int tp = positiveScoreList.size() - count;
        int tn = cutoffIndex;
        int fp = count;
        int fn = negativeScoreList.size() - cutoffIndex;
        Performance perf = new Performance(tp, tn, fp, fn, cutoff);
        return perf;
    }

    public double getSn(double sp) {
        Collections.sort(negativeScoreList);
        int cutoffIndex = (int)(negativeScoreList.size() * sp);
        double cutoff = negativeScoreList.get(cutoffIndex);

        Collections.sort(positiveScoreList);
        int count = 0;
        for(double score : positiveScoreList) {
            count++;
            if(score > cutoff)
                break;
        }

        double sn = (double) (positiveScoreList.size() - count)/ (double) positiveScoreList.size();
        return sn;
    }

    public void FullPerformance() {
        performanceList.clear();
        for (double cutoff = 0; cutoff <= 1; cutoff = cutoff + 0.01) {
            int tp = 0, tn = 0, fp = 0, fn = 0;
            for (double score : positiveScoreList) {
                if (score >= cutoff)
                    tp++;
                else
                    fn++;
            }
            for (double score : negativeScoreList) {
                if (score >= cutoff)
                    fp++;
                else
                    tn++;
            }
            //Calculate Performance
            Performance perf = new Performance(tp, tn, fp, fn, cutoff);
            performanceList.add(perf);
        }
    }

    public LinkedList<Performance> getPerformanceList() {
        return performanceList;
    }

    public double CalculateAUC() {
        Collections.sort(performanceList, new CompareAUCPerformance());
        double sum = 0;
        double fore1_Sp = 0, foreSn = 0;
        for(Performance perf : performanceList) {
            double cur1_Sp = 1 - perf.getSp();
            double curSn = perf.getSn();
            sum = sum + (cur1_Sp - fore1_Sp)*( (curSn + foreSn)/2 );
            fore1_Sp = cur1_Sp;
            foreSn = curSn;
        }
        sum = sum + (fore1_Sp - 1) *( (foreSn + 1)/2 );
        return sum;
    }

    public void SavePerformance(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("Ac\tSn\tSp\tMcc\tPr\n");
            for(Performance perf : performanceList) {
                fw.write(perf + "\n");
            }
            fw.write("\nAUC = " + CalculateAUC() + "\n");
            fw.write("\nThreshold selection\n");
            fw.write("Cutoff\tAc\tSn\tSp\tMcc\tPr\n");
            fw.write("High:\t" + getPerformance(0.95).getPerformanceString() + "\n");
            fw.write("Medium:\t" + getPerformance(0.9).getPerformanceString() + "\n");
            fw.write("Low:\t" + getPerformance(0.85).getPerformanceString() + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
