package PeakCalling;

/**
 * Created by Ben on 2018/4/23.
 */
public class BinResult {
    //Variable
    private String chrName;
    private int index;
    private double score;
    private double probability;

    public BinResult(String chrName, int index, double score) {
        this.chrName = chrName;
        this.index = index;
        this.score = score;
    }

    public BinResult(String chrName, int index, double score, double probability) {
        this.chrName = chrName;
        this.index = index;
        this.score = score;
        this.probability = probability;
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
