package PeakCalling.Threshold.GPD;


import PeakCalling.Threshold.PSO.PSOParameters;
import PeakCalling.Threshold.PSO.PSOParametersReader;
import PeakCalling.Threshold.PSO.Swarm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Ben on 2017/12/15.
 */
public class EstimationParameters
{
    private Swarm psoSwarm;
    private double[]X;
    private double optimizedTheta, optimizedGamma, optimizedSigma;

    public EstimationParameters(String parameterFile, double[] X)
    {
        PSOParametersReader psoParametersReader = new PSOParametersReader();
        psoParametersReader.ReadFromFile(parameterFile);
        PSOParameters psoParameters = psoParametersReader.getPsoParam();
        //Initialized PSO algorithm
        MGFTargetFunction mgfTargetFunction = new MGFTargetFunction(X);
        psoSwarm = new Swarm(psoParameters.getSwarmSize(),psoParameters.getNeighborSize(),1,psoParameters.getInertiaWeight(),psoParameters.getRandomCoefficients1()
                ,psoParameters.getRandomCoefficients2(),psoParameters.getVmax(),-1000, 1D/mgfTargetFunction.getXn()
                ,mgfTargetFunction);

        this.X = X;
    }

    public void Optimize(double acc, int maxIter)
    {
        int iterCount = 0;
        double foreFitness = psoSwarm.getBestParticle().getBestFitness();
        double curFitness = foreFitness;
        System.out.println("Optimization start at fitness = " + curFitness);
        while(true)
        {
            psoSwarm.PSOStep();
            curFitness = psoSwarm.getBestParticle().getBestFitness();
            System.out.println("Optimized at fitness = " + curFitness);
            double diff = Math.abs(curFitness - foreFitness);
            foreFitness = curFitness;
            if(diff <= acc)
                iterCount++;
            if(iterCount >= maxIter)
            {
                optimizedTheta = psoSwarm.getBestParticle().getBestPosition()[0];
                System.out.println("Optimization converge.");
                break;
            }
        }
        //Compute gamma and sigma
        double sum = 0;
        for(int i=0; i<X.length; i++)
            sum = sum + Math.log(1 - optimizedTheta*X[i]);
        sum = sum/(double) X.length;
        optimizedGamma = -1 * sum;

        optimizedSigma = optimizedGamma/optimizedTheta;
    }

    public double getGamma()
    {
        return optimizedGamma;
    }

    public double getSigma()
    {
        return optimizedSigma;
    }

    public double[] getX() {
        return X;
    }
}
