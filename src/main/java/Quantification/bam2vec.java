package Quantification;

import htsjdk.samtools.*;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class bam2vec {
    private HashMap<Integer, Double> count_map = new HashMap<Integer, Double>();
    private SamReader sam_reader;

    public void bamfileReader(String file_name) {
        File bam_file = new File(file_name);
        SamReaderFactory factory = SamReaderFactory.makeDefault()
                .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
                .validationStringency(ValidationStringency.SILENT);
        sam_reader = factory.open(bam_file);
    }

    public void bamfileReaderClose() {
        try {
            sam_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double value2vec(int start, int end, String chr, int gap) {
        count_map.clear();
        double reads_count = 0;
        try {
            SAMRecordIterator sam_it = sam_reader.queryOverlapping(chr, start, end);
            while (sam_it.hasNext()) {
                reads_count++;
                SAMRecord record = sam_it.next();
                for (int i = start; i <= end; i = i + gap) {
                    int sub_start = Math.max(record.getStart(), start);
                    Cigar c = record.getCigar();
                    List<CigarElement> cigarList = c.getCigarElements();
                    for (Iterator iterator = cigarList.iterator(); iterator.hasNext(); ) {
                        CigarElement cigarElement = (CigarElement) iterator.next();
                        if (cigarElement.toString().endsWith("M")) {
                            int length = cigarElement.getLength();
                            int sub_end = Math.min(end, (sub_start + length));
                            if (sub_start <= i && sub_end >= i) {
                                count_sam(i, i + 1, gap);
                            }
                            sub_start = sub_start + length;
                        } else if (cigarElement.toString().endsWith("N") || cigarElement.toString().endsWith("H")) {
                            int length = cigarElement.getLength();
                            sub_start = sub_start + length;
                        } else {
                            int length = cigarElement.getLength();
                            int sub_end = Math.min(end, (sub_start + length));
                            if (sub_start <= i && sub_end >= i) {
                                count_sam(i, i + 1, gap);
                            }
                            sub_start = sub_start + length;
                        }
                    }
                }
            }

            sam_it.close();
        } catch (SAMException no_reads) {

        }
        return reads_count;
    }

    private void count_sam(int sam_start, int sam_end, int cut_length) {
        for (int count_index = sam_start; count_index < sam_end; count_index += cut_length) {
            if (count_map.containsKey(count_index)) {
                double count = count_map.get(count_index);
                count++;
                count_map.put(count_index, count);
            } else {
                count_map.put(count_index, 1.0);
            }
        }
    }

    public LinkedList GetCountList(int size) {
        LinkedList<Double> resultList = new LinkedList<Double>();
        Iterator<Map.Entry<Integer, Double>> it = count_map.entrySet().iterator();
        if (count_map.size() == 0) {
            for (int i = 0; i < size; i++) {
                resultList.add(0.0);
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<Integer, Double> entry = it.next();
                resultList.add(entry.getValue());
            }
        }
        return resultList;
    }

    public void Bam2Count(HashMap<String, IntervalTree> PeakIntervalTreeMap, HashMap<String, IntervalTree> NonPeakIntervalTreeMap, int gap, int background_gap) {
        SAMRecordIterator sam_it = sam_reader.iterator();
        while (sam_it.hasNext()) {
            SAMRecord samRecord = sam_it.next();
            int RecordStart = samRecord.getStart();
            int RecordEnd = samRecord.getEnd();
            Interval recordInterval = new Interval(RecordStart, RecordEnd);
            String chr = samRecord.getContig();
//            if(SamRecord_chr.equals(chr)) {
            List PeakIntervalList;
            List NonPeakIntervalList;
            if (PeakIntervalTreeMap.containsKey(chr)) {
                IntervalTree tree = PeakIntervalTreeMap.get(chr);
                PeakIntervalList = tree.findOverlapping(recordInterval);
                for (Iterator<BedInterval> Iterator = PeakIntervalList.iterator(); Iterator.hasNext(); ) {
                    BedInterval interval = Iterator.next();
                    interval.AddReadsCount(samRecord, gap);
                }
                PeakIntervalList.clear();
            }
            if (NonPeakIntervalTreeMap.containsKey(chr)) {
                IntervalTree tree = NonPeakIntervalTreeMap.get(chr);
                NonPeakIntervalList = tree.findOverlapping(recordInterval);
                for (Iterator<BedInterval> Iterator = NonPeakIntervalList.iterator(); Iterator.hasNext(); ) {
                    BedInterval interval = Iterator.next();
                    interval.AddBackReadsCount(samRecord, background_gap);
                }
                NonPeakIntervalList.clear();
            }
//            }
            recordInterval = null;
            samRecord.clearAttributes();
        }
        sam_it.close();
    }

}
