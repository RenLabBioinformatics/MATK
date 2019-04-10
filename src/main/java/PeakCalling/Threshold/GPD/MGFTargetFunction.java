package PeakCalling.Threshold.GPD;

import PeakCalling.Threshold.PSO.IFitness;

import java.util.Arrays;

/**
 * Created by Ben on 2017/12/15.
 */
public class MGFTargetFunction implements IFitness
{
    private double[] X;

    public MGFTargetFunction(double[] X)
    {
        this.X = X;
        Arrays.sort(X);//Sort the inputted value to construct an order vector
    }

    private double gTheta(double theta, int index, double thetaSum)
    {
        double retVal;
        double pow = X.length/thetaSum;
        retVal = 1 - Math.pow((1 - theta*X[index]), -1 * pow);
        return retVal;
    }

    public double CalFitness(double[] position)
    {
        double retVal;
        double sum = 0;

        double thetaSum = 0;
        for(int i=0; i<X.length; i++)
        {
            thetaSum = thetaSum + Math.log(1 - position[0]*X[i]);
        }

        for(int i=0; i<X.length; i++)
        {
            sum = sum + (2*i - 1)*Math.log(gTheta(position[0], i, thetaSum)) + (2*X.length + 1 - 2*i)*Math.log(1 - gTheta(position[0], i, thetaSum));
        }
        sum = sum/(double)X.length;
        retVal = -1*X.length - sum;
        return retVal;
    }

    public double getXn()
    {
        return X[X.length - 1];
    }
}
