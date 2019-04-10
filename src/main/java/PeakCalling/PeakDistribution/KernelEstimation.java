package PeakCalling.PeakDistribution;

import org.apache.commons.math3.distribution.NormalDistribution;

public class KernelEstimation {
    private double[] variable;
    private double bandWidth;

    public KernelEstimation(double[] variable) {
        this.variable = variable;
        //Compute average
        double sum = 0;
        for(int i=0; i<variable.length; i++)
            sum = sum + variable[i];
        double average = sum/((double) variable.length);
        //Compute standard error
        sum = 0;
        for(int i=0; i<variable.length; i++)
            sum = sum + Math.pow(variable[i] - average, 2);
        sum = sum / ((double)variable.length - 1);
        double stdErr = Math.sqrt(sum);
        //Compute bandwidth
        bandWidth = 1.06 * stdErr * Math.pow(variable.length, -1 * 1D/5D);
    }

    public double Density(double x) {
        NormalDistribution normalDistribution = new NormalDistribution();
        double sum = 0;
        for(int i=0; i<variable.length; i++) {
            sum = sum + normalDistribution.density((x - variable[i])/bandWidth);
        }
        return (1D/(variable.length*bandWidth))*sum;
    }
}
