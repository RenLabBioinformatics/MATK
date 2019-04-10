package PeakCalling.Threshold.GPD;

/**
 * Created by Ben on 2017/12/15.
 */
public class GPDFunction {
    private double gamma, sigma;

    public GPDFunction(double gamma, double sigma) {
        this.gamma = gamma;
        this.sigma = sigma;
    }

    public double cdf(double x) {
        double retVal;

        if (gamma != 0) {
            retVal = 1 - Math.pow(1 - (gamma * x / sigma), 1D / gamma);
        } else {
            retVal = 1 - Math.exp(-1 * x / sigma);
        }

        return retVal;
    }

    public double pdf(double x) {
        double retVal;

        if (gamma != 0)
            retVal = Math.pow(sigma, -1) * Math.pow(1 - (gamma * x / sigma), 1 / gamma - 1);
        else
            retVal = Math.pow(sigma, -1) * Math.exp(-x / sigma);

        return retVal;
    }

    // Given a probability of q = P(x>=zq), computing the threshold zq
    // q is the desired cutoff;
    // t is the initial cutoff;
    // N is total the number of data;
    // Nt is the number of data that larger than the initial cutoff t
    public double QuantileCutoff(double q, double t, double N, double Nt) {
        double retVal;

        if(gamma == 0) {
            retVal = t - sigma*Math.log((q*N)/Nt);
        } else {
            retVal = t + (sigma/gamma)*(1-Math.pow((q*N)/Nt, gamma));
        }

        return retVal;
    }
}
