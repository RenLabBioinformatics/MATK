package BAMProcess;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2016/9/6.
 */
public class ChromosomeBin
{
    private HashMap<Integer, BAMBin> chrBinMap = new HashMap<Integer, BAMBin>();

    public void AddBAMBin(int binIndex, double alignmentCount)
    {
        if(chrBinMap.containsKey(binIndex))
        {
            BAMBin bin = chrBinMap.get(binIndex);
            double curCount = bin.getReadCount() + alignmentCount;
            bin.setReadCount(curCount);
        }
        else
        {
            BAMBin bin = new BAMBin();
            bin.setBinIndex(binIndex);
            bin.setReadCount(alignmentCount);
            chrBinMap.put(binIndex, bin);
        }
    }

    public BAMBin getBAMBin(int index)
    {
        return chrBinMap.get(index);
    }

    public LinkedList<Integer> getAllBinIndex()
    {
        LinkedList<Integer> binIndexList = new LinkedList<Integer>();
        for(Integer index : chrBinMap.keySet())
            binIndexList.add(index);
        Collections.sort(binIndexList);
        return binIndexList;
    }

    public void ReleaseMemory()
    {
        chrBinMap.clear();
    }
}
