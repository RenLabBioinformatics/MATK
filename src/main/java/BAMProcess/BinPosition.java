package BAMProcess;

/**
 * Created by Ben on 2016/9/23.
 */
public class BinPosition
{
    private String chromosome;
    private int start;
    private int end;

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString()
    {
        return chromosome + "\t" + start + "\t" + end;
    }
}
