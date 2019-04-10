/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PeakCalling.Threshold.PSO;

import java.util.ArrayList;

/**
 *
 * @author Ben
 */
public class ParticleMoveThread implements Runnable {
    private ArrayList<Particle> bestNeighborList;
    private ArrayList<Particle> particleList;
    private int initIndex;
    private int endIndex;
    
    public ParticleMoveThread(ArrayList<Particle> particleList, ArrayList<Particle> bestNeighborList, int initIndex, int endIndex)
    {
        this.bestNeighborList = bestNeighborList;
        this.particleList = particleList;
        this.initIndex = initIndex;
        this.endIndex = endIndex;
    }

    public void run() 
    {
        for(int i = initIndex; i<= endIndex; i++)
        {
            particleList.get(i).Move(bestNeighborList.get(i).getCurPosition());
        }
        //System.out.println("Thread "+this.getName()+" terminated");
    }
    
}
