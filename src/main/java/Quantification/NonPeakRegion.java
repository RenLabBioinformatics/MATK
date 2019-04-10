package Quantification;

import Basic.GTF.ChromosomeRecord;
import Basic.GTF.FeatureRecord;
import Basic.GTF.TranscriptRecord;
import Quantification.ExtendedGTF.QuantifyChromosomeRecord;
import Quantification.ExtendedGTF.QuantifyFeatureRecord;
import Quantification.ExtendedGTF.QuantifyGeneRecord;
import Quantification.ExtendedGTF.QuantifyReadGtfFile;
import htsjdk.tribble.index.interval.IntervalTree;

import java.io.File;
import java.util.*;

/**
 * Created by Tong on 2018/4/17.
 */
public class NonPeakRegion {
    private QuantifyReadGtfFile readGTF;
    private HashMap<String, IntervalTree> intervalTreeHashMap = new HashMap<String, IntervalTree>();
    private HashMap<String, IntervalTree> intervalPeakTreeHashMap = new HashMap<String, IntervalTree>();

    public void SetGTF(String gtfFilePath) {
        readGTF = new QuantifyReadGtfFile();
        readGTF.ReadFromFile(new File(gtfFilePath));
    }

    public HashMap getChrMap() {
        return readGTF.getChromosomeMap();
    }

    public HashMap<String, IntervalTree> getIntervalTreeHashMap() {
        return intervalTreeHashMap;
    }

    public HashMap<String, IntervalTree> getIntervalPeakTreeHashMap() {
        return intervalPeakTreeHashMap;
    }

    public void MergeTranscript() {
        HashMap<String, QuantifyChromosomeRecord> chrMap = readGTF.getChromosomeMap();
        for (Map.Entry<String, QuantifyChromosomeRecord> entry : chrMap.entrySet()) {
//            String chr = entry.getKey();
            ChromosomeRecord chrRec = entry.getValue();
            LinkedList geneList = chrRec.getGeneList();
            for (Iterator<QuantifyGeneRecord> gene_iter = geneList.iterator(); gene_iter.hasNext(); ) {
                QuantifyGeneRecord geneRec = gene_iter.next();
//                System.out.println(geneRec.getGeneId());
                LinkedList<FeatureRecord> gene_feature_List = new LinkedList<FeatureRecord>();
                LinkedList<TranscriptRecord> transcriptList = geneRec.getTranscriptList();
                for (Iterator<TranscriptRecord> trans_iter = transcriptList.iterator(); trans_iter.hasNext(); ) {
                    TranscriptRecord transRec = trans_iter.next();
                    LinkedList<FeatureRecord> featureList = transRec.getFeatureList();
//                    gene_feature_List.clear();
                    for (Iterator<FeatureRecord> feature_iter = featureList.iterator(); feature_iter.hasNext(); ) {
                        FeatureRecord featureRec = feature_iter.next();
                        String featureString = featureRec.getFeature();
                        if (featureString.equals("exon")) {
                            gene_feature_List.add(featureRec);
                        }
                    }
                }
                Collections.sort(gene_feature_List, new FeatureComparator());
                LinkedList<FeatureRecord> gene_feature_mergeList = NonPeakRegion.MergeOneFeatureRecordList(gene_feature_List);

                geneRec.setMerge_featureList(gene_feature_mergeList);
            }
        }
    }


    public static LinkedList MergeOneFeatureRecordList(LinkedList<FeatureRecord> FeatureRecordList) {
        LinkedList<FeatureRecord> ConcordanceList = new LinkedList<FeatureRecord>();
        Iterator iterator = FeatureRecordList.iterator();
        FeatureRecord record = (FeatureRecord) iterator.next();
        int start = record.getStart();
        int end = record.getEnd();
        while (iterator.hasNext()) {
            FeatureRecord temp = (FeatureRecord) iterator.next();
            int temp_start = temp.getStart();
            int temp_end = temp.getEnd();
            int start_max = Math.max(start, temp_start);
            int end_min = Math.min(end, temp_end);
            if (start_max <= end_min) {
                start = Math.min(start, temp_start);
                end = Math.max(end, temp_end);
                record.setStart(start);
                record.setEnd(end);
            } else {
                FeatureRecord add_record = new FeatureRecord();
                add_record.setStart(record.getStart());
                add_record.setEnd(record.getEnd());
                ConcordanceList.add(add_record);
                record = temp;
                start = temp_start;
                end = temp_end;
            }
//            System.out.println(start + "\t" + temp_start + "\t" + end);
        }
        ConcordanceList.add(record);
        return ConcordanceList;
    }


    public static LinkedList MergeOnePeakRecordList(LinkedList<QuantifyPeakRecord> quantifyPeakRecordList) {
        LinkedList<QuantifyPeakRecord> ConcordanceList = new LinkedList<QuantifyPeakRecord>();
        Iterator iterator = quantifyPeakRecordList.iterator();
        QuantifyPeakRecord record = (QuantifyPeakRecord) iterator.next();
        int start = record.getPeakStart();
        int end = record.getPeakEnd();
        LinkedList<int[]> PeakRegionList = record.getPeakRegionList();
        while (iterator.hasNext()) {
            QuantifyPeakRecord temp = (QuantifyPeakRecord) iterator.next();
            int temp_start = temp.getPeakStart();
            int temp_end = temp.getPeakEnd();
            LinkedList<int[]> tempPeakRegionList = temp.getPeakRegionList();
//            System.out.println(start + "\t" + temp_start + "\t" + end);
            if (temp_start <= end) {
                if (temp_end > end) {
                    record.SetPeakRecord(record.getChr(), start, temp_end);
                    end = temp_end;
                    PeakRegionList.addAll(tempPeakRegionList);
                }
            } else {
//                System.out.println("add");
                record.setPeakRegionList(PeakRegionList);
                record.SortRegionList();
                ConcordanceList.add(record);
                record = temp;
                start = record.getPeakStart();
                end = record.getPeakEnd();
                PeakRegionList = tempPeakRegionList;
            }
        }
        ConcordanceList.add(record);
        return ConcordanceList;
    }

    public static LinkedList RemoveRegion(LinkedList<QuantifyPeakRecord> PeakList, int start, int end) {
        LinkedList<QuantifyFeatureRecord> NonPeakRegionList = new LinkedList<QuantifyFeatureRecord>();
        LinkedList<QuantifyFeatureRecord> temp_NonPeakRegionList = new LinkedList<QuantifyFeatureRecord>();
        QuantifyFeatureRecord nonPeakRegion = new QuantifyFeatureRecord();
        nonPeakRegion.setStart(start);
        nonPeakRegion.setEnd(end);
        temp_NonPeakRegionList.add(nonPeakRegion);
        for (Iterator iterator_Peak = PeakList.iterator(); iterator_Peak.hasNext(); ) {
            QuantifyPeakRecord peak = (QuantifyPeakRecord) iterator_Peak.next();
            int peak_start = peak.getPeakStart();
            int peak_end = peak.getPeakEnd();
            for (Iterator iterator_region = temp_NonPeakRegionList.iterator(); iterator_region.hasNext(); ) {
                QuantifyFeatureRecord temp_region = (QuantifyFeatureRecord) iterator_region.next();
                int temp_start = temp_region.getStart();
                int temp_end = temp_region.getEnd();
                int start_max = Math.max(temp_start, peak_start);
                int end_min = Math.min(temp_end, peak_end);
                if (start_max > end_min) {
                    NonPeakRegionList.add(temp_region);
                } else if (peak_start > temp_start) {
                    QuantifyFeatureRecord new_region = new QuantifyFeatureRecord();
                    new_region.setStart(temp_start);
                    new_region.setEnd(peak_start - 1);
                    NonPeakRegionList.add(new_region);
                    if (peak_end < temp_end) {
                        new_region = new QuantifyFeatureRecord();
                        new_region.setStart(peak_end + 1);
                        new_region.setEnd(temp_end);
                        NonPeakRegionList.add(new_region);
                    }
                } else if (peak_end < temp_end) {
                    QuantifyFeatureRecord new_region = new QuantifyFeatureRecord();
                    new_region.setStart(peak_end + 1);
                    new_region.setEnd(temp_end);
                    NonPeakRegionList.add(new_region);
                }
            }
            temp_NonPeakRegionList.clear();
//            System.out.println("NonPeakRegion\t"+ NonPeakRegionList.size());
            temp_NonPeakRegionList.addAll(NonPeakRegionList);
            NonPeakRegionList.clear();
//            System.out.println(temp_NonPeakRegionList.size());
        }
        NonPeakRegionList.addAll(temp_NonPeakRegionList);
        return NonPeakRegionList;
    }

    public void PeakAdd2Gene(HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap, int gap) {
        HashMap<String, QuantifyChromosomeRecord> chrMap = readGTF.getChromosomeMap();
        for (Map.Entry<String, QuantifyChromosomeRecord> entry : chrMap.entrySet()) {
            String chr = entry.getKey();
            IntervalTree intervalTree = new IntervalTree();
            if (PeakMap.containsKey(chr)) {
                LinkedList<QuantifyPeakRecord> PeakList = PeakMap.get(chr);
                ChromosomeRecord chrRec = entry.getValue();
                LinkedList geneList = chrRec.getGeneList();
                for(Iterator<QuantifyPeakRecord> peak_iter = PeakList.iterator(); peak_iter.hasNext();){
                    QuantifyPeakRecord peak = peak_iter.next();
                    int peak_start = peak.getPeakStart();
                    int peak_end = peak.getPeakEnd();
                    boolean peak2gene = false;
                    QuantifyGeneRecord temp_Gene = new QuantifyGeneRecord();
                    for(Iterator<QuantifyGeneRecord> gene_iter = geneList.iterator(); gene_iter.hasNext();){
                        QuantifyGeneRecord Gene = gene_iter.next();
                        int gene_start = Gene.getStart();
                        int gene_end = Gene.getEnd();
                        if (peak_start >= gene_start && peak_end <= gene_end) {
                            if(peak2gene){
                                String Biotype = Gene.getBioType();
//                                System.out.println(Biotype);
                                String temp_Biotype = temp_Gene.getBioType();
                                if(Biotype.equals("protein_coding") && temp_Biotype.equals("protein_coding")){
                                    temp_Gene = SelectGene(Gene,temp_Gene);
                                }else if(Biotype.equals("protein_coding")){
                                    temp_Gene = Gene;
                                }
                            }else{
                                temp_Gene = Gene;
                                peak2gene = true;
                            }
                        }
                    }
                    QuantifyPeakRecord new_Peak = new QuantifyPeakRecord(peak.getChr(), peak.getPeakStart(), peak.getPeakEnd(), peak.getBamfileword());
                    LinkedList regionList = peak.getPeakRegionList();
                    for (Iterator<int[]> region_iter = regionList.iterator(); region_iter.hasNext(); ) {
                        int[] region = region_iter.next();
                        new_Peak.AddPeakRegion(region[0], region[1]);
                    }
                    new_Peak.setPeakBinList(gap);
                    BedInterval interval = new BedInterval(new_Peak);
                    intervalTree.insert(interval);
                    temp_Gene.AddPeak(new_Peak);
                }
            }
            intervalPeakTreeHashMap.put(chr, intervalTree);
        }
    }

    public void NonPeakBin(int gap) {
        HashMap<String, QuantifyChromosomeRecord> chrMap = readGTF.getChromosomeMap();
        for (Map.Entry<String, QuantifyChromosomeRecord> entry : chrMap.entrySet()) {
            ChromosomeRecord chrRec = entry.getValue();
            String chr = chrRec.getChromosomeName();
            LinkedList geneList = chrRec.getGeneList();
            IntervalTree intervalTree = new IntervalTree();
            for (Iterator<QuantifyGeneRecord> gene_iter = geneList.iterator(); gene_iter.hasNext(); ) {
                QuantifyGeneRecord gene = gene_iter.next();
                gene.RemovePeakRegion();
                gene.setBinList(gap);

                LinkedList<QuantifyFeatureRecord> merge_exceptPeakList = gene.getMerge_exceptPeakList();
                for (Iterator<QuantifyFeatureRecord> nonPeakFeature_iter = merge_exceptPeakList.iterator(); nonPeakFeature_iter.hasNext(); ) {
                    QuantifyFeatureRecord nonPeakFeature = nonPeakFeature_iter.next();
                    nonPeakFeature.setBinList(gap);
                    BedInterval interval = new BedInterval(nonPeakFeature);
                    intervalTree.insert(interval);
                }
            }
            intervalTreeHashMap.put(chr, intervalTree);
        }
    }

    public void BamInformation2NonPeakTree(boolean isIP, boolean isTreat) {
        for (Map.Entry<String, IntervalTree> entry : intervalTreeHashMap.entrySet()) {
            IntervalTree tree = entry.getValue();
            List intervals = tree.getIntervals();
            if (isIP) {
                for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                    BedInterval interval = iterator.next();
                    interval.AddIPReadsCountList2Record(false, isTreat);
                }
            } else {
                for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                    BedInterval interval = iterator.next();
                    interval.AddInputCountList2Record(false, isTreat);
                }
            }
        }
    }

    public void BamInformation2PeakTree(boolean isIP, boolean isTreat) {
        for (Map.Entry<String, IntervalTree> entry : intervalPeakTreeHashMap.entrySet()) {
            IntervalTree tree = entry.getValue();
            List intervals = tree.getIntervals();
            if (isTreat) {
                if (isIP) {
                    for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                        BedInterval interval = iterator.next();
                        interval.AddIPReadsCountList2Record(true, isTreat);
                    }
                } else {
                    for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                        BedInterval interval = iterator.next();
                        interval.AddInputCountList2Record(true, isTreat);
                    }
                }
            } else {
                if (isIP) {
                    for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                        BedInterval interval = iterator.next();
                        interval.AddIPReadsCountList2Record(true, isTreat);
                    }
                } else {
                    for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                        BedInterval interval = iterator.next();
                        interval.AddInputCountList2Record(true, isTreat);
                    }
                }
            }
        }
    }

    public QuantifyGeneRecord SelectGene(QuantifyGeneRecord temp_Gene, QuantifyGeneRecord Gene){
        int transcript_length = 0;
        int temp_transcript_length = 0;
        int CDS_length = 0;
        int temp_CDS_length = 0;
        GetTranscriptCDSLength(temp_Gene,temp_transcript_length,temp_CDS_length);
        GetTranscriptCDSLength(Gene,transcript_length,CDS_length);
        if(transcript_length > temp_transcript_length){
            return Gene;
        }else if(transcript_length == temp_transcript_length && CDS_length >= temp_CDS_length){
            return Gene;
        }else{
            return temp_Gene;
        }
    }

    public void GetTranscriptCDSLength(QuantifyGeneRecord GeneRec, int transcript_length,int CDS_length){
        LinkedList<TranscriptRecord> transcriptRecords = GeneRec.getTranscriptList();
        for(Iterator<TranscriptRecord> iterator = transcriptRecords.iterator(); iterator.hasNext();){
            TranscriptRecord transcript = iterator.next();
            int transcript_start = transcript.getStart();
            int transcript_end = transcript.getEnd();
            LinkedList<FeatureRecord> featureRecords = transcript.getFeatureList();

            if(transcript_length <= Math.abs(transcript_end - transcript_start)){
                int CDS_length_record = 0;
                for(Iterator<FeatureRecord> featureIterator = featureRecords.iterator(); featureIterator.hasNext();){
                    FeatureRecord featureRec = featureIterator.next();
                    String featureString = featureRec.getFeature();
                    if(featureString.equals("CDS")){
                        int feature_start = featureRec.getStart();
                        int feature_end = featureRec.getEnd();
                        CDS_length_record = CDS_length_record + (feature_end - feature_start) + 1;
                    }
                }
                if(transcript_length < Math.abs(transcript_end - transcript_start)){
                    transcript_length = Math.abs(transcript_end - transcript_start);
                    CDS_length = CDS_length_record;
                }else if(CDS_length_record > CDS_length){
                    CDS_length = CDS_length_record;
                }
            }
        }
    }

}
