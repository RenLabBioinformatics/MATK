package Quantification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Tong on 2018/4/17.
 */
public class PeakReader {
    private String BedFile;
    private HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap = new HashMap<String, LinkedList<QuantifyPeakRecord>>();

    public PeakReader(String bedFile) {
        this.BedFile = bedFile;
        PeakMap = PeakReader.BedFileReader(bedFile);
    }

    public HashMap getPeakMap(){
        return PeakMap;
    }

    public static HashMap BedFileReader(String bedFile) {
        HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap = new HashMap<String, LinkedList<QuantifyPeakRecord>>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(bedFile));
//            br.readLine();
            while (br.ready()) {
                String strLine = br.readLine();
                if(!strLine.startsWith("#")) {
                    String[] dataArr = strLine.split("\t");
                    String chr = dataArr[0];
                    LinkedList<QuantifyPeakRecord> PeakList = new LinkedList<QuantifyPeakRecord>();
                    if (PeakMap.containsKey(chr)) {
                        PeakList = PeakMap.get(chr);
                    }
                    int chr_start = Integer.parseInt(dataArr[1]);
                    int chr_end = Integer.parseInt(dataArr[2]);
                    String bamfileword = chr + "\t" + chr_start + "\t" + chr_end + "\t" + dataArr[4];
                    QuantifyPeakRecord peak = new QuantifyPeakRecord(chr, chr_start, chr_end, bamfileword);
                    if (dataArr.length < 10) {
                        peak.AddPeakRegion(chr_start, chr_end);
                    } else if (Integer.parseInt(dataArr[9]) < 2) {
                        peak.AddPeakRegion(chr_start, chr_end);
                    } else {
                        String[] peakstarts = dataArr[11].split(",");
                        String[] peaklengths = dataArr[10].split(",");
                        for (int i = 0; i < peakstarts.length; i++) {
                            int start = chr_start + Integer.parseInt(peakstarts[i]);
                            int end = start + Integer.parseInt(peaklengths[i]);
//                        String dataLine = dataArr[0] + "\t" + dataArr[1] + "\t" + dataArr[2] + "\t" + dataArr[3] + "\t" + dataArr[4] + "\t" + dataArr[5] + "\t" + dataArr[6] + "\t" + dataArr[7] + "\t" + dataArr[8] + "\t" + dataArr[9] + "\t" + peaklengths + "\t" + peakstarts;
                            peak.AddPeakRegion(start, end);
                        }
                    }
                    PeakList.add(peak);
                    PeakMap.put(chr, PeakList);
                }
            }
            br.close();
        } catch (IOException ex) {
            System.out.println("bedfile IOException");
        }
        return PeakMap;
    }

    public static HashMap MergePeak(String BedFile1, String BedFile2) {
        HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap1 = BedFileReader(BedFile1);
        HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap2 = BedFileReader(BedFile2);
        HashMap<String, LinkedList<QuantifyPeakRecord>> Merge_PeakMap = new HashMap<String, LinkedList<QuantifyPeakRecord>>();
        Iterator iter = PeakMap1.entrySet().iterator();
        ArrayList<String> ChrList = new ArrayList<>();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            String chr = (String) entry.getKey();
            if(!ChrList.contains(chr)){
                ChrList.add(chr);
            }
        }
        iter = PeakMap2.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            String chr = (String) entry.getKey();
            if(!ChrList.contains(chr)){
                ChrList.add(chr);
            }
        }
       for(int i =0; i<ChrList.size(); i++) {
            String chr = ChrList.get(i);
           if(PeakMap1.containsKey(chr) && PeakMap2.containsKey(chr)){
               LinkedList<QuantifyPeakRecord> RecordList1 = PeakMap1.get(chr);
               LinkedList<QuantifyPeakRecord> RecordList2 = PeakMap2.get(chr);
               Collections.sort(RecordList1, new PeakRecordComparator());
               Collections.sort(RecordList2, new PeakRecordComparator());
               LinkedList<QuantifyPeakRecord> Concordance_RecordList1 = NonPeakRegion.MergeOnePeakRecordList(RecordList1);
               LinkedList<QuantifyPeakRecord> Concordance_RecordList2 = NonPeakRegion.MergeOnePeakRecordList(RecordList2);
               LinkedList<QuantifyPeakRecord> All_RecordList = new LinkedList<QuantifyPeakRecord>();
               All_RecordList.addAll(Concordance_RecordList1);
               All_RecordList.addAll(Concordance_RecordList2);
//            System.out.println(Concordance_RecordList1.size() +"\t" + Concordance_RecordList2.size());
               Collections.sort(All_RecordList, new PeakRecordComparator());
               LinkedList<QuantifyPeakRecord> All_sort_RecordList = NonPeakRegion.MergeOnePeakRecordList(All_RecordList);
//            System.out.println(All_sort_RecordList.size());
               Merge_PeakMap.put(chr, All_sort_RecordList);
//            System.out.println(All_sort_RecordList.size());
           }else if(PeakMap1.containsKey(chr)){
               Merge_PeakMap.put(chr,PeakMap1.get(chr));
           }else{
               Merge_PeakMap.put(chr,PeakMap2.get(chr));
           }

        }
        return Merge_PeakMap;
    }
}


