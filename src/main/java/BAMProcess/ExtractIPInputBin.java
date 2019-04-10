package BAMProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2017/6/28.
 */
public class ExtractIPInputBin {
    private HashMap<String, ChromosomeBin> ipBinMap;
    private HashMap<String, ChromosomeBin> inputBinMap;
    private static final int upStream = 2;
    private static final int downstream = 2;

    public ExtractIPInputBin(File ipBAMFile, File inputBAMFile, int binSize) {
        BAMFileReader ipBAMFileReader = new BAMFileReader(ipBAMFile);
        ipBinMap = ipBAMFileReader.Binning(binSize);
        ipBAMFileReader.CloseBAMFile();

        BAMFileReader inputBAMFileReader = new BAMFileReader(inputBAMFile);
        inputBinMap = inputBAMFileReader.Binning(binSize);
        inputBAMFileReader.CloseBAMFile();
    }

    public void SavaIPInputFeature(File saveFile, File locationFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            FileWriter fwLoc = new FileWriter(locationFile);
            for (String chrName : ipBinMap.keySet()) {
                if (inputBinMap.containsKey(chrName)) {
                    ChromosomeBin ipChrBin = ipBinMap.get(chrName);
                    ChromosomeBin inputChrBin = inputBinMap.get(chrName);
                    LinkedList<Integer> ipIndexList = ipChrBin.getAllBinIndex();
                    if (ipIndexList.size() == 0) {
                        System.out.println("Reads in chromosome " + chrName + " is empty. Skipped!");
                        continue;
                    }
                    int curIndex = ipIndexList.get(0);//initial index
                    int lastIndex = ipIndexList.get(ipIndexList.size() - 1);
                    int index = curIndex;

                    while(curIndex <= lastIndex) {
                        double value;
                        String writeStr = "";
                        BAMBin ipBin = ipChrBin.getBAMBin(curIndex);
                        BAMBin inputBin = inputChrBin.getBAMBin(curIndex);
                        if( (ipBin != null) && (inputBin != null) ) {
                            //Get upstream bin
                            for(int i = upStream; i >= 1; i = i - 1) {
                                BAMBin upIPBin = ipChrBin.getBAMBin(curIndex - i);
                                BAMBin upInputBin = ipChrBin.getBAMBin(curIndex - i);
                                if( (upIPBin != null) && (upInputBin != null) ) {
                                    value = AudicModel.computeNegativeLogP((int) upInputBin.getReadCount(), (int) upInputBin.getReadCount());
                                    writeStr = writeStr + value + ",";
                                } else {
                                    writeStr = writeStr + 0 + ",";
                                }
                            }
                            //Get middle bin
                            value = AudicModel.computeNegativeLogP((int) ipBin.getReadCount(), (int) inputBin.getReadCount());
                            writeStr = writeStr + value + ",";
                            //Get downstream bin
                            for(int i = 1; i <= downstream; i++) {
                                BAMBin downIPBin = ipChrBin.getBAMBin(curIndex + i);
                                BAMBin downInputBin = ipChrBin.getBAMBin(curIndex + i);
                                if( (downIPBin != null) && (downInputBin != null) ) {
                                    value = AudicModel.computeNegativeLogP((int) downIPBin.getReadCount(), (int) downInputBin.getReadCount());
                                    writeStr = writeStr + value + ",";
                                } else {
                                    writeStr = writeStr + 0 + ",";
                                }
                            }
                            //Write data
                            writeStr = writeStr.substring(0, writeStr.length() - 1);
                            fw.write(writeStr + "\n");
                            fwLoc.write(chrName + "\t" + curIndex + "\n");

                        }
                        curIndex++;
                    }
                }
            }
            fw.close();
            fwLoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Save the computed log p-value for IP-Input pair
    //Use to draw a probability density function of Ip-Input bin pair
    public void SaveIpInputBinValue(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for (String chrName : ipBinMap.keySet()) {
                ChromosomeBin ipChrBin = ipBinMap.get(chrName);
                ChromosomeBin inputChrBin = inputBinMap.get(chrName);
                LinkedList<Integer> ipIndexList = ipChrBin.getAllBinIndex();
                if (ipIndexList.size() == 0) {
                    System.out.println("Reads in chromosome " + chrName + " is empty. Skipped!");
                    continue;
                }
                for (Integer index : ipIndexList) {
                    BAMBin ipBin = ipChrBin.getBAMBin(index);
                    BAMBin inputBin = inputChrBin.getBAMBin(index);
                    if ((ipBin != null) && (inputBin != null)) {
                        double value = AudicModel.computeNegativeLogP((int) ipBin.getReadCount(), (int) inputBin.getReadCount());
                        fw.write(value + "\n");
                    }
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ExtractIPInputBin extractIPInputBin = new ExtractIPInputBin(new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120870_sort_uniq.bam"),
                new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120874_sort_uniq.bam"),
                25);
//        extractIPInputBin.SaveBin(new File("E:\\DeepRIPTest\\Hela_shGFP_BAM\\Difference_IP_Input.txt"));
        extractIPInputBin.SavaIPInputFeature(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\SRR2120870_SRR2120874.csv"),
                new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\SRR2120870_SRR2120874.loc"));
//        extractIPInputBin.SaveIpInputBinValue(new File("E:\\DeepRIPTest\\KeBam\\ValueDistribution.txt"));
    }
}
