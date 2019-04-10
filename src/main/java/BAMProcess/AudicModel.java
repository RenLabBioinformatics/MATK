package BAMProcess;

import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 * Created by Ben on 2017/6/20.
 */
public class AudicModel
{
    /*Compute -ln(p-value) using Audic's model.
      The library size was regarded as the same
    */
    public static double computeNegativeLogP(int ip, int input)
    {
        double retVal = CombinatoricsUtils.factorialLog(ip + input) - CombinatoricsUtils.factorialLog(ip) - CombinatoricsUtils.factorialLog(input) - (ip + input + 1)*Math.log(2);
        if(ip > input)
            return -1*retVal;
        else
            return retVal;
    }

    public static double computeProbability(int ip, int input)
    {
        double retVal = CombinatoricsUtils.factorialLog(ip + input) - CombinatoricsUtils.factorialLog(ip) - CombinatoricsUtils.factorialLog(input) - (ip + input + 1)*Math.log(2);
        retVal = Math.exp(retVal);
        if(ip > input)
            retVal = 1D - retVal;
        else
            retVal = 0;
        return retVal;
    }

    public static double computeNegativeLogP(int ip, int input, int ipSize, int inputSize)
    {
        double retVal = input*Math.log(inputSize) - input*Math.log(ipSize) + CombinatoricsUtils.factorialLog(ip + input) - CombinatoricsUtils.factorialLog(ip) - CombinatoricsUtils.factorialLog(input) - (ip + input + 1)*Math.log(1 + (double)inputSize/(double) ipSize);
        double coverageIP = ((double) ip/(double) ipSize)*(1000000);
        double coverageInput = ((double) input/(double) inputSize)*(1000000);
        if(coverageIP > coverageInput)
            return -1*retVal;
        else
            return retVal;
    }

    public static double computeProbability(int ip, int input, int ipSize, int inputSize)
    {
        double retVal = input*Math.log(inputSize) - input*Math.log(ipSize) + CombinatoricsUtils.factorialLog(ip + input) - CombinatoricsUtils.factorialLog(ip) - CombinatoricsUtils.factorialLog(input) - (ip + input + 1)*Math.log(1 + (double)inputSize/(double) ipSize);
        retVal = Math.exp(retVal);
        double coverageIP = ((double) ip/(double) ipSize)*(1000000);
        double coverageInput = ((double) input/(double) inputSize)*(1000000);
        if(coverageIP > coverageInput)
            return 1D - retVal;
        else
            return retVal;
    }

    public static void main(String[] args)
    {
        System.out.println(AudicModel.computeNegativeLogP(100, 100, 1000, 10000));
    }
}
