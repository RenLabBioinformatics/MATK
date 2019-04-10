package SingleNucleotide.Data;

/**
 * Created by Ben on 2018/4/29.
 */
public class SiteRecord {
    private String chrName;
    private int chrStart;
    private int chrEnd;
    private int strand;
    private String sequence;
    private int label;
    private double[][] features;
    private double[] meripFeatures;
    private int[] ipCountVec, inputCountVec;
    private int curReplicateCount = 0; //Use for computing the average of MeRIP-seq feature

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getChrStart() {
        return chrStart;
    }

    public void setChrStart(int chrStart) {
        this.chrStart = chrStart;
    }

    public int getChrEnd() {
        return chrEnd;
    }

    public void setChrEnd(int chrEnd) {
        this.chrEnd = chrEnd;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public double[][] getFeatures() {
        return features;
    }

    public void setFeatures(double[][] features) {
        this.features = features;
    }

    public double[] getMeripFeatures() {
        return meripFeatures;
    }

    public void setMeripFeatures(double[] meripFeatures) {
        this.meripFeatures = meripFeatures;
    }

    public int[] getIpCountVec() {
        return ipCountVec;
    }

    public void setIpCountVec(int[] ipCountVec) {
        this.ipCountVec = ipCountVec;
    }

    public int[] getInputCountVec() {
        return inputCountVec;
    }

    public void setInputCountVec(int[] inputCountVec) {
        this.inputCountVec = inputCountVec;
    }

    public int getCurReplicateCount() {
        return curReplicateCount;
    }

    public void setCurReplicateCount(int curReplicateCount) {
        this.curReplicateCount = curReplicateCount;
    }
}
