package PeakCalling.CombineReplicates;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MergePeaks {
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;

    public MergePeaks(CombineSet[] combineSets) {
        bedRecMap = new HashMap<>();
        for(int i=0; i<combineSets.length; i++) {
            LinkedList<BEDRecord> bedRecList = combineSets[i].getBedRecList();
            for(BEDRecord bedRec : bedRecList) {
                String chrName = bedRec.getChrName();
                LinkedList<BEDRecord> mapBEDRecList;
                if(bedRecMap.containsKey(chrName)) {
                    mapBEDRecList = bedRecMap.get(chrName);
                } else {
                    mapBEDRecList = new LinkedList<>();
                    bedRecMap.put(chrName, mapBEDRecList);
                }
                mapBEDRecList.add(bedRec);
            }
        }
    }

    private void MergeBEDList(LinkedList<BEDRecord> bedRecList) {
        int peakIndex = 1;
        Collections.sort(bedRecList, new CompareBEDRecord());
        BEDRecord foreBEDRec = null, curBEDRec = null;
        for(Iterator<BEDRecord> itr = bedRecList.iterator(); itr.hasNext();) {
            curBEDRec = itr.next();
            if(foreBEDRec != null) {
                BEDIntervalRecord foreBEDIntervalRec = new BEDIntervalRecord(foreBEDRec);
                BEDIntervalRecord curBEDIntervalRec = new BEDIntervalRecord(curBEDRec);
                if(foreBEDIntervalRec.overlaps(curBEDIntervalRec)) {
                    //set chromosome end position
                    if(foreBEDRec.getChrEnd() > curBEDRec.getChrEnd())
                        foreBEDRec.setChrEnd(foreBEDRec.getChrEnd());
                    else
                        foreBEDRec.setChrEnd(curBEDRec.getChrEnd());
                    //set probability
                    if(foreBEDRec.getScore() > curBEDRec.getScore())
                        foreBEDRec.setScore(curBEDRec.getScore());
                    else
                        foreBEDRec.setScore(foreBEDRec.getScore());
                    //remove the merged peak
                    itr.remove();
                } else {
                    //No need to merge this peak. Move forward to the next iteration.
                    foreBEDRec.setName(foreBEDRec.getChrName() + "_Peak_" + peakIndex);
                    foreBEDRec = curBEDRec;
                    peakIndex++;
                }
            } else {
                //The first round. Initialize the variable.
                foreBEDRec = curBEDRec;
            }
        }
    }

    public void Merge() {
        for(String chrName : bedRecMap.keySet()) {
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            MergeBEDList(bedRecList);
        }
    }

    public HashMap<String, LinkedList<BEDRecord>> getMergedBedRecMap() {
        return bedRecMap;
    }
}
