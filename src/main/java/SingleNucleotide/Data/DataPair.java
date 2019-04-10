package SingleNucleotide.Data;

/**
 * Created by Ben on 2018/5/3.
 */
public class DataPair {
    private double[][] features;
    private double[] labels;

    public double[][] getFeatures() {
        return features;
    }

    public void setFeatures(double[][] features) {
        this.features = features;
    }

    public double[] getLabels() {
        return labels;
    }

    public void setLabels(double[] labels) {
        this.labels = labels;
    }
}
