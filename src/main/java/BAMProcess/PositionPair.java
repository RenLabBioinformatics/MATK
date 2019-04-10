package BAMProcess;

/**
 * Created by Ben on 2018/4/20.
 */
public class PositionPair {
    private int start;
    private int end;

    public PositionPair(int start, int end) {
        this.start = start;
        this.end = end;
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
}
