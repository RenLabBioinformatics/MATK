package Basic.BED;

import Basic.Genome.GenomeTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class BEDWriter {
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;

    public BEDWriter(LinkedList<BEDRecord> bedRecList) {
        bedRecMap = new HashMap<>();
        for(BEDRecord bedRecord : bedRecList) {
            String chrName = bedRecord.getChrName();
            LinkedList<BEDRecord> mapBEDRecList;
            if(bedRecMap.containsKey(chrName)) {
                mapBEDRecList = bedRecMap.get(chrName);
                mapBEDRecList.add(bedRecord);
            } else {
                mapBEDRecList = new LinkedList<>();
                mapBEDRecList.add(bedRecord);
                bedRecMap.put(chrName, mapBEDRecList);
            }
        }
    }

    public BEDWriter(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        this.bedRecMap = bedRecMap;
    }

    public void SaveInSimpleBED(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("#Chromosome\tStart\tEnd\tName\tScore\n");
            for(String chrName : bedRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    fw.write(chrName + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t" + bedRec.getName() + "\t" + bedRec.getScore() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveInFullBED(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("#Chromosome\tStart\tEnd\tName\tScore\tStrand\tthickStart\tthickEnd\titemRGB\tblockCount\tblockSizes\tblockStarts\n");
            for(String chrName : bedRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    fw.write(chrName + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd()
                            + "\t" + bedRec.getName() + "\t" + bedRec.getScore()
                            + "\t" + GenomeTools.FormatStrand(bedRec.getStrand()) +"\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd()
                            + "\t0");

                    LinkedList<BLOCKRecord> blockRecList = bedRec.getBlockRecList();
                    if( (blockRecList != null) && (!blockRecList.isEmpty()) ) {
                        fw.write("\t" + blockRecList.size() + "\t");
                        String blockSizeStr = "", blockStartStr = "";
                        for(BLOCKRecord blockRec : blockRecList) {
                            int blockSize = blockRec.getChrEnd() - blockRec.getChrStart() + 1;
                            int blockStart = blockRec.getChrStart() - bedRec.getChrStart();
                            blockSizeStr = blockSizeStr + blockSize + ",";
                            blockStartStr = blockStartStr + blockStart + ",";
                        }
                        fw.write(blockSizeStr.substring(0, blockSizeStr.length() - 1) + "\t" + blockStartStr.substring(0, blockStartStr.length() - 1) + "\n");
                    } else {
                        int blockSize = bedRec.getChrEnd() - bedRec.getChrStart() + 1;
                        int blockStart = 0;
                        fw.write("\t1\t" + blockSize + "\t" + blockStart + "\n");
                    }
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
