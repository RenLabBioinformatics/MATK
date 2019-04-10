package PeakCalling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/25.
 */
public class ResultWriter {
    private HashMap<String, LinkedList<PeakRecord>> peakListMap;

    public ResultWriter(HashMap<String, LinkedList<PeakRecord>> peakListMap) {
        this.peakListMap = peakListMap;
    }

    public void SaveInTSV(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : peakListMap.keySet()) {
                LinkedList<PeakRecord> peakRecList = peakListMap.get(chrName);
                fw.write("Chromosome\tStart\tEnd\tScore\tProbability\n");
                for(PeakRecord peakRec : peakRecList) {
                    fw.write(chrName + "\t" + (peakRec.getStartIndex()*25 + 1) + "\t" +  ( (peakRec.getEndIndex() + 1)*25 ) + "\t" + peakRec.getScore() + "\t" + peakRec.getProbability() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveInBED(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : peakListMap.keySet()) {
                LinkedList<PeakRecord> peakRecList = peakListMap.get(chrName);
                int i = 0;
                for(PeakRecord peakRec : peakRecList) {
                    i++;
                    fw.write(chrName + "\t" + (peakRec.getStartIndex()*25) + "\t" +  ( (peakRec.getEndIndex() + 1)*25 - 1) + "\t" + "Peak" + i + "\t" + peakRec.getProbability() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
