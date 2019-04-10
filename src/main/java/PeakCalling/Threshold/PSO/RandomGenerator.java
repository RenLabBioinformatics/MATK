/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PeakCalling.Threshold.PSO;

/**
 *Population
 * @author Ben
 */
public class RandomGenerator 
{
    public static double randomDouble(double down, double up)
    {
        return ((Math.random()*(up-down))+down);
    }
    
    public static int randomInteger(int down, int up)
    {
        return ( (int)Math.round((Math.random()*(up-down))+down) );
    }
    
    public static byte randomByte()
    {
        return ( (byte)Math.round(Math.random()));
    }
    
    public static void main(String[] args) 
    {
        for(int i=1;i<=100;i++)
        {
//            System.out.println(RandomGenerator.randomDouble(0, 7.5)+"\t"+RandomGenerator.randomInteger(0, 7));
            System.out.println(RandomGenerator.randomByte());
        }
    }
}
