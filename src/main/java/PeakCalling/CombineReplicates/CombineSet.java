package PeakCalling.CombineReplicates;

import Basic.BED.BEDRecord;
import org.bytedeco.javacpp.presets.opencv_core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class CombineSet {
    private LinkedList<BEDRecord> bedRecList;

    public CombineSet() {
        bedRecList = new LinkedList<>();
    }

    public void AddRecord(BEDRecord bedRec) {
        bedRecList.add(bedRec);
    }

    public LinkedList<BEDRecord> getBedRecList() {
        return bedRecList;
    }

    public CombineSet getIntersect(CombineSet otherSet) {
        CombineSet retCombineSet = new CombineSet();

        HashSet<String> otherBEDSet = new HashSet<>();
        LinkedList<BEDRecord> otherBEDList = otherSet.getBedRecList();
        for(BEDRecord otherBEDRec : otherBEDList) {
            String posStr = otherBEDRec.getChrName() + "|" + otherBEDRec.getChrStart() + "|" + otherBEDRec.getChrEnd();
            otherBEDSet.add(posStr);
        }
        for(BEDRecord thisBEDRec : bedRecList) {
            String posStr = thisBEDRec.getChrName() + "|" + thisBEDRec.getChrStart() + "|" + thisBEDRec.getChrEnd();
            if(otherBEDSet.contains(posStr)) {
                retCombineSet.AddRecord(thisBEDRec);
            }
        }

        return retCombineSet;
    }

    public void RemoveSet(CombineSet combineSet) {
        HashSet<String> removedSet = new HashSet<>();
        LinkedList<BEDRecord> removedBEDList = combineSet.getBedRecList();
        for(BEDRecord removedBEDRecord : removedBEDList) {
            String posStr = removedBEDRecord.getChrName() + "|" + removedBEDRecord.getChrStart() + "|" + removedBEDRecord.getChrEnd();
            removedSet.add(posStr);
        }

        for(Iterator<BEDRecord> itr = bedRecList.iterator(); itr.hasNext();) {
            BEDRecord bedRec = itr.next();
            String posStr = bedRec.getChrName() + "|" + bedRec.getChrStart() + "|" + bedRec.getChrEnd();
            if(removedSet.contains(posStr)) {
                itr.remove();
            }
        }
    }
}
