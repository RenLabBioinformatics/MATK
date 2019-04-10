package SingleNucleotide.Validation;

import java.text.DecimalFormat;

/**
 * <p>Performance.java</p>
 * <p>Created on Jul 23, 2009, 10:20:06 PM</p>
 * <p>Copyright (c) 2007-2009. The CUCKOO Workgroup, USTC, P.R.China</p>
 * @author Ren Jian
 * @version 3.6
 */
public class Performance {

    private int tp;
    private int tn;
    private int fp;
    private int fn;
    private double cutoff;
    private DecimalFormat format = new DecimalFormat("#.####");

    public Performance(int tp, int tn, int fp, int fn, double cutoff) {
        this.tp = tp;
        this.tn = tn;
        this.fp = fp;
        this.fn = fn;
        this.cutoff = cutoff;
    }

    public Performance(int tp, int tn, int fp, int fn) {
        this.tp = tp;
        this.tn = tn;
        this.fp = fp;
        this.fn = fn;
    }

    public int getFn() {
        return fn;
    }

    public int getFp() {
        return fp;
    }

    public int getTn() {
        return tn;
    }

    public String getSample() {
        return tp + "\t" + tn + "\t" + fp + "\t" + fn;
    }

    public int getTp() {
        return tp;
    }

    public int getPositive() {
        return tp + fn;
    }

    public int getNegative() {
        return tn + fp;
    }

    public double getAc() {
        return ((double) (tp + tn)) / (tp + fp + tn + fn);
    }

    public double getSn() {
        return ((double) tp) / (tp + fn);
    }

    public double getSp() {
        return ((double) tn) / (tn + fp);
    }

    public double getMcc() {
        double mcc;
        double temp = ((double) (tp + fn)) * (tn + fp) * (tp + fp) * (tn + fn);
        if (temp == 0) {
            mcc = -2;
        } else {
            mcc = ((tp * tn) - (fn * fp)) / Math.sqrt(temp);
        }
        return mcc;
    }

    public double getPr() {
        return (double) tp / (tp + fp);
    }

    public double getCutoff() {
        return cutoff;
    }

    public String getMccString() {
        String mccStr = "";
        double mcc = getMcc();
        if (mcc == -2) {
            mccStr = "NaN";
        } else {
            mccStr = format.format(mcc);
        }
        return mccStr;
    }

    public String fullPerformance() {
        return getSample() + "\t" + format.format(getAc()) + "\t" + format.format(getSn()) + "\t" + format.format(getSp()) + "\t" + getMccString() + "\t" + format.format(getPr());
    }

    public String toString() {
        return format.format(getAc()) + "\t" + format.format(getSn()) + "\t" + format.format(getSp()) + "\t" + getMccString() + "\t" + format.format(getPr());
    }

    public String getPerformanceString() {
        return format.format(cutoff) + "\t" + format.format(getAc()) + "\t" + format.format(getSn()) + "\t" + format.format(getSp()) + "\t" + getMccString() + "\t" + format.format(getPr());
    }
}
