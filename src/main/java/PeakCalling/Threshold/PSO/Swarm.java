/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PeakCalling.Threshold.PSO;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *Swarm
 * @author Ben
 */
public class Swarm 
{
    private int dimension;
    private double w=1;
    private double phi1=2;
    private double phi2=2;
    private double vMax=1;
    private double downLimit = -10;
    private double upLimit = 10;
    private IFitness fitness;
    private int swarmSize = 50;
    private int neighborSize = 7;
    private ArrayList<Particle> particleList;
    private int threadCount;
    
    public Swarm(int swarmSize, int neighborSize, int dimension, double weight, double phi1, double phi2, double vMax, double downLimit, double upLimit, IFitness fitness)
    {
        this.swarmSize = swarmSize;
        this.neighborSize = neighborSize;
        this.dimension = dimension;
        this.w = weight;
        this.phi1 = phi1;
        this.phi2 = phi2;
        this.vMax = vMax;
        this.downLimit = downLimit;
        this.upLimit = upLimit;
        this.fitness = fitness;
        
        particleList = new ArrayList(swarmSize);
        for(int i=1;i<=swarmSize;i++)
        {
            Particle particle = new Particle(this.dimension, this.w, this.phi1, this.phi2, this.vMax, this.downLimit, this.upLimit, this.fitness);
            particle.Initialize();
            particleList.add(particle);
        }
    }

    public Swarm(int swarmSize, int neighborSize, int dimension, double weight, double phi1, double phi2, double vMax, double downLimit, double upLimit, IFitness fitness, int threadCount)
    {
        this.swarmSize = swarmSize;
        this.neighborSize = neighborSize;
        this.dimension = dimension;
        this.w = weight;
        this.phi1 = phi1;
        this.phi2 = phi2;
        this.vMax = vMax;
        this.downLimit = downLimit;
        this.upLimit = upLimit;
        this.fitness = fitness;

        particleList = new ArrayList(swarmSize);
        for(int i=1;i<=swarmSize;i++)
        {
            Particle particle = new Particle(this.dimension, this.w, this.phi1, this.phi2, this.vMax, this.downLimit, this.upLimit, this.fitness);
            particle.Initialize();
            particleList.add(particle);
        }
        this.threadCount = threadCount;
    }
    
//    public Swarm(int swarmSize, int neighborSize, int dimension, double weight, double phi1, double phi2, double vMax, double downLimit, double upLimit, boolean IsWT, String positiveFile, String negativeFile, String matrixFile, boolean CenterValid, int threadCount)
//    {
//        this.swarmSize = swarmSize;
//        this.neighborSize = neighborSize;
//        this.dimension = dimension;
//        this.w = weight;
//        this.phi1 = phi1;
//        this.phi2 = phi2;
//        this.vMax = vMax;
//        this.downLimit = downLimit;
//        this.upLimit = upLimit;
//
//        particleList = new ArrayList(swarmSize);
//        for(int i=1;i<=swarmSize;i++)
//        {
//            if(IsWT)
//            {
//                PSOWeightSN psoWT = new PSOWeightSN(positiveFile, negativeFile, matrixFile, CenterValid);
//                Particle particle = new Particle(this.dimension, this.w, this.phi1, this.phi2, this.vMax, this.downLimit, this.upLimit, psoWT);
//                particle.Initialize();
//                particleList.add(particle);
//            }
//            else
//            {
//                PSOMatrixSN psoMM = new PSOMatrixSN(positiveFile, negativeFile, matrixFile);
//                Particle particle = new Particle(this.dimension, this.w, this.phi1, this.phi2, this.vMax, this.downLimit, this.upLimit, psoMM);
//                particle.Initialize();
//                particleList.add(particle);
//            }
//        }
//        this.threadCount = threadCount;
//    }
    
    private Particle getBestNeighbor(int particleIndex)
    {
        Particle ret=particleList.get(particleIndex);
        double bestFitness=Double.POSITIVE_INFINITY;
        int index;
        for(int i=1;i<=this.neighborSize;i++)
        {
            //
            index = particleIndex - i;
            if( index < 0 )
            {
                index = index + this.swarmSize;
            }
            if(particleList.get(index).getCurFitness()< bestFitness)//Minimized function
            {
                ret = particleList.get(index);
                bestFitness = particleList.get(index).getCurFitness();
            }
            //
            index = particleIndex + i;
            if(index > (swarmSize - 1) )
            {
                index = index - this.swarmSize;
            }
            if(particleList.get(index).getCurFitness()< bestFitness) // Minimized function
            {
                ret = particleList.get(index);
                bestFitness = particleList.get(index).getCurFitness();
            }
        }
        
        return ret;        
    }
    
    private ArrayList<Particle> getBestNeighborList()
    {
        ArrayList<Particle> bestNeighborList = new ArrayList();
        for(int i=0;i<=swarmSize-1;i++)
        {
            Particle bestNeighborParticle = getBestNeighbor(i);
            bestNeighborList.add(bestNeighborParticle);
        }
        return bestNeighborList;
    }
    
    private Particle getBestNeighborByRandom(int particleIndex)
    {
        int index;
        double bestFitness = Double.NEGATIVE_INFINITY;
        double tmpFitness;
        Particle ret=null;
        for( int i=1; i<=this.neighborSize; i++)
        {
            index = RandomGenerator.randomInteger(0, this.swarmSize-1);
            if( index != particleIndex )
            {
                tmpFitness = particleList.get(index).getCurFitness();
                if( tmpFitness > bestFitness  )
                {
                    bestFitness = tmpFitness;
                    ret = particleList.get(index);
                }
            }
        }
        return ret;
    }
    
    public void PSOStep()
    {
        for(int i=0;i<=swarmSize-1;i++)
        {
            Particle bestNeighborParticle = getBestNeighbor(i);
            particleList.get(i).Move(bestNeighborParticle.getCurPosition());
        }
    }
    
    public void PSOStepMulti()
    {
        ArrayList<Particle> bestNeighborList = getBestNeighborList();
        int part = this.swarmSize/threadCount;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for(int i=1; i<threadCount; i++)
        {
            ParticleMoveThread threadMove = new ParticleMoveThread(particleList, bestNeighborList,(i-1)*part,i*part-1);
            pool.submit(threadMove);
        }
        ParticleMoveThread threadMove = new ParticleMoveThread(particleList, bestNeighborList,(threadCount-1)*part,swarmSize-1);
        pool.submit(threadMove);
        pool.shutdown();

        try 
        {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } 
        catch (InterruptedException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    public Particle getBestParticle()
    {
        Particle ret = particleList.get(0);
        double bestFitness = particleList.get(0).getBestFitness();
        for(int i=1;i<=this.swarmSize-1;i++)
        {
            Particle tmpP = particleList.get(i);
            if(tmpP.getBestFitness() < bestFitness)/*Optimize a minimum Z score*/
            {
                ret = tmpP;
                bestFitness = tmpP.getBestFitness();
            }
        }
        return ret;        
    }
}
