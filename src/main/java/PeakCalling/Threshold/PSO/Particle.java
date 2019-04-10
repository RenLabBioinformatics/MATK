/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PeakCalling.Threshold.PSO;


/**
 *Particle in population
 * @author Ben
 */
public class Particle 
{
    private double[] curPosition;
    private double curFitness;
    private double[] curBestPosition;
    private double curBestFitness;
    private double[] velocity;
    private int dimension;
    private double w=1;
    private double phi1=2;
    private double phi2=2;
    private double vMax=1;
    private IFitness fitness;
    private double downLimit;
    private double upLimit;
    
    public Particle(int dimension, double weight, double phi1, double phi2, double vMax, double downLimit, double upLimit, IFitness fitness)
    {
        this.dimension = dimension;
        this.w = weight;
        this.phi1 = phi1;
        this.phi2 = phi2;
        this.vMax = vMax;
        this.downLimit = downLimit;
        this.upLimit = upLimit;
        this.fitness = fitness;
        
        curPosition = new double[this.dimension];
        curFitness = 0;
        curBestPosition = new double[this.dimension];
        curBestFitness = 0;
        velocity = new double[this.dimension];
    }
    
    public void Initialize()
    {
        for(int i=0;i<=this.dimension-1;i++)
        {
            curPosition[i] = RandomGenerator.randomDouble(downLimit, upLimit);
            curBestPosition[i] = curPosition[i];
        }
        curFitness = fitness.CalFitness(curBestPosition);
        curBestFitness = curFitness;
    }
    
    public void Move(double[] bestp)
    {
        for(int i=0;i<=dimension-1;i++)
        {
            velocity[i] = w*velocity[i]+RandomGenerator.randomDouble(0, phi1)*(curBestPosition[i]-curPosition[i])+RandomGenerator.randomDouble(0,phi2)*(bestp[i]-curPosition[i]);
            //
            if(velocity[i]>vMax)
            {
                velocity[i] = vMax;
            }
            else if(velocity[i]<-1*vMax)
            {
                velocity[i] = -1*vMax;
            }
            curPosition[i] = curPosition[i] + velocity[i];
        }
        double tmpFitness = fitness.CalFitness(curPosition);
        curFitness = tmpFitness;
        if(tmpFitness < curBestFitness)/*Optimize a minimum z score*/
        {
            for(int i=0;i<this.dimension;i++)
            {
                curBestPosition[i] = curPosition[i];
            }
            curBestFitness = curFitness;
        }
    }
    
    public double[] getBestPosition()
    {
        return curBestPosition;
    }
    
    public double getBestFitness()
    {
        return curBestFitness;
    }
    
    public double[] getCurPosition()
    {
        return curPosition;
    }
    
    public double getCurFitness()
    {
        return curFitness;
    }
}
