package SingleNucleotide.Data;

/**
 * Created by Ben on 2018/5/2.
 */
public class EncodeRecord {
    private double[][] feature;
    private int label;

    public EncodeRecord(double[][] feature, int label) {
        this.feature = feature;
        this.label = label;
    }

    public double[][] getFeature() {
        return feature;
    }

    public void setFeature(double[][] feature) {
        this.feature = feature;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }
}
