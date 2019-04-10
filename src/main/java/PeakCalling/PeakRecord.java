package PeakCalling;

/**
 * Created by Ben on 2018/4/25.
 */
public class PeakRecord {
    private String chrName;
    private int startIndex;
    private int endIndex;
    private double score;
    private double probability;

    public PeakRecord(String chrName, int startIndex, int endIndex, double score, double probability) {
        this.chrName = chrName;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.score = score;
        this.probability = probability;
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
