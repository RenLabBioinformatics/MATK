package Basic.BED;

import Basic.Genome.GenomeTools;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/25.
 */
public class BEDReader {
    private LinkedList<BEDRecord> bedRecList;

    public BEDReader(File bedFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(bedFile));
            String strLine;
            String[] strArr;
            bedRecList = new LinkedList<BEDRecord>();
            while(br.ready()) {
                strLine = br.readLine();
                //skip the annotation line
                if(strLine.startsWith("#"))
                    continue;
                strArr = strLine.split("\t");
                BEDRecord bedRec = new BEDRecord(strArr[0], Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]));

                if (strArr.length >= 4)
                    bedRec.setName(strArr[3]);

                if (strArr.length >= 5) {
                    if(!strArr[4].equals("."))
                        bedRec.setScore(Double.parseDouble(strArr[4]));
                }

                if (strArr.length >= 6) {
                    if(strArr[5].equals("+"))
                        bedRec.setStrand(GenomeTools.SenseStrand);
                    else if (strArr[5].equals("-"))
                        bedRec.setStrand(GenomeTools.AntiSenseStrand);
                    else
                        bedRec.setStrand(-1);
                }

                if (strArr.length >= 7) {
                    if(!strArr[7].equals("."))
                        bedRec.setThickStart(Integer.parseInt(strArr[6]));
                }

                if(strArr.length >= 8) {
                    if(!strArr[7].equals(".")) {
                        bedRec.setThickEnd(Integer.parseInt(strArr[7]));
                    }
                }

                if(strArr.length >= 9) {
                    if(strArr[8].equals(".")) {
                        String[] rgbArr = strArr[8].split(",");
                        RGBItem rgbItem = new RGBItem(Integer.parseInt(rgbArr[0]), Integer.parseInt(rgbArr[1]), Integer.parseInt(rgbArr[2]));
                        bedRec.setRgbItem(rgbItem);
                    }
                }
                //Only consider blocks when split attributes were larger than 12
                if(strArr.length >= 12) {
                    int blockCount = Integer.parseInt(strArr[9]);
                    String[] blockArr = strArr[10].split(",");
                    if(blockArr.length == blockCount) {
                        //Read block size
                        int[] blockSize = new int[blockCount];
                        for(int i=0; i<blockArr.length; i++) {
                            blockSize[i] = Integer.parseInt(blockArr[i]);
                        }
                        //Read block start
                        blockArr = strArr[11].split(",");
                        if(blockArr.length == blockCount) {
                            int[] blockStart = new int[blockCount];
                            for(int i=0; i<blockArr.length; i++) {
                                blockStart[i] = Integer.parseInt(blockArr[i]);
                            }
                            //Setup blocks
                            LinkedList<BLOCKRecord> blockRecList = new LinkedList<BLOCKRecord>();
                            for(int i=0; i<blockStart.length; i++) {
                                BLOCKRecord blockRec = new BLOCKRecord(bedRec.getChrName(), bedRec.getChrStart() + blockStart[i], bedRec.getChrStart() + blockStart[i] + blockSize[i] - 1);
                                blockRecList.add(blockRec);
                            }
                            bedRec.setBlockRecList(blockRecList);
                        } else {
                            System.err.println("Block count do not match blockStarts column!");
                        }
                    } else {
                        System.err.println("Block count do not match blockSize column!");
                    }
                }
                bedRecList.add(bedRec);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<BEDRecord> getBedRecList() {
        return bedRecList;
    }

    public HashMap<String, LinkedList<BEDRecord>> getBEDChromosomeMap() {
        HashMap<String, LinkedList<BEDRecord>> bedChrMap = new HashMap<String, LinkedList<BEDRecord>>();
        LinkedList<BEDRecord> tmpBedRecList;
        for(BEDRecord bedRecord : bedRecList) {
            String chrName = bedRecord.getChrName();
            if(bedChrMap.containsKey(chrName)) {
                tmpBedRecList = bedChrMap.get(chrName);
                tmpBedRecList.add(bedRecord);
            } else {
                tmpBedRecList = new LinkedList<BEDRecord>();
                tmpBedRecList.add(bedRecord);
                bedChrMap.put(chrName, tmpBedRecList);
            }
        }
        return bedChrMap;
    }

    public void SaveSimpleBedFile(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(BEDRecord bedRec : bedRecList) {
                LinkedList<BLOCKRecord> blockRecList = bedRec.getBlockRecList();
                if(blockRecList != null) {
                    if(!blockRecList.isEmpty()) {
                        for(BLOCKRecord blockRec : blockRecList) {
                            fw.write(blockRec.getChrName() + "\t" + blockRec.getChrStart() + "\t" + blockRec.getChrEnd() + "\n");
                        }
                    } else {
                        fw.write(bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\n");
                    }
                } else {
                    fw.write(bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
