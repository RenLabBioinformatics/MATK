package PeakCalling.Threshold.PSO;

/**
 * Created by Ben on 2016/8/25.
 */
public class PSOParameters
{
    private int swarmSize;
    private int neighborSize;
    private double InertiaWeight;
    private double randomCoefficients1;
    private double randomCoefficients2;
    private double Vmax;
    private double initialLowerLimit;
    private double initialUpperLimit;

    public int getSwarmSize() {
        return swarmSize;
    }

    public void setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;
    }

    public int getNeighborSize() {
        return neighborSize;
    }

    public void setNeighborSize(int neighborSize) {
        this.neighborSize = neighborSize;
    }

    public double getInertiaWeight() {
        return InertiaWeight;
    }

    public void setInertiaWeight(double inertiaWeight) {
        InertiaWeight = inertiaWeight;
    }

    public double getRandomCoefficients1() {
        return randomCoefficients1;
    }

    public void setRandomCoefficients1(double randomCoefficients1) {
        this.randomCoefficients1 = randomCoefficients1;
    }

    public double getRandomCoefficients2() {
        return randomCoefficients2;
    }

    public void setRandomCoefficients2(double randomCoefficients2) {
        this.randomCoefficients2 = randomCoefficients2;
    }

    public double getVmax() {
        return Vmax;
    }

    public void setVmax(double vmax) {
        Vmax = vmax;
    }

    public double getInitialLowerLimit() {
        return initialLowerLimit;
    }

    public void setInitialLowerLimit(double initialLowerLimit) {
        this.initialLowerLimit = initialLowerLimit;
    }

    public double getInitialUpperLimit() {
        return initialUpperLimit;
    }

    public void setInitialUpperLimit(double initialUpperLimit) {
        this.initialUpperLimit = initialUpperLimit;
    }
}
