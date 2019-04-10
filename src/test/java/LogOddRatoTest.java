import org.apache.commons.math3.distribution.NormalDistribution;

public class LogOddRatoTest {
    private long bgDRACH, bgNotDRACH, tgDRACH, tgNotDRACH;
    private double oddRatio, pValueTwoTail, pValueRightTail, pValueLeftTail;

    public LogOddRatoTest(long bgDRACH, long bgNotDRACH, long tgDRACH, long tgNotDRACH) {
        this.bgDRACH = bgDRACH;
        this.bgNotDRACH = bgNotDRACH;
        this.tgDRACH = tgDRACH;
        this.tgNotDRACH = tgNotDRACH;
    }

    public void PerformTest() {
        oddRatio = ((double) tgDRACH/(double) tgNotDRACH)/((double)bgDRACH/(double)bgNotDRACH);
        double logOddRatio = Math.log(oddRatio);
        double std = Math.sqrt( (1D/((double) tgDRACH)) + (1D/((double) tgNotDRACH)) + (1D/((double) bgDRACH)) + (1D/((double) bgNotDRACH)) );
        NormalDistribution normalDistribution = new NormalDistribution(logOddRatio, std);

        if(logOddRatio <= 0)
            pValueTwoTail = 2 * (1 - normalDistribution.cumulativeProbability(logOddRatio));
        else
            pValueTwoTail = 2 * normalDistribution.cumulativeProbability(logOddRatio);

        pValueRightTail = 1 - normalDistribution.cumulativeProbability(logOddRatio);

        pValueLeftTail = normalDistribution.cumulativeProbability(logOddRatio);
    }

    public double getOddRatio() {
        return oddRatio;
    }

    public double getpValueTwoTail() {
        return pValueTwoTail;
    }

    public double getpValueRightTail() {
        return pValueRightTail;
    }

    public double getpValueLeftTail() {
        return pValueLeftTail;
    }
}
