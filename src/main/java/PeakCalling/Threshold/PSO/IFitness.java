/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PeakCalling.Threshold.PSO;

/**
 *Fitness function interface
 * @author Ben
 */
public interface IFitness 
{
    public double CalFitness(double[] position);
}
