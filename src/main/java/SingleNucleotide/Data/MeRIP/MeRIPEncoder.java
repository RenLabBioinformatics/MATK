package SingleNucleotide.Data.MeRIP;

import BAMProcess.AudicModel;
import BAMProcess.PositionPair;
import SingleNucleotide.Data.SiteRecord;
import htsjdk.samtools.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class MeRIPEncoder {
    private LinkedList<SiteRecord> siteRecList;
    private SiteIntervalTree siteIntervalTree;
    private int ipTotalReads, inputTotalReads;

    private LinkedList<PositionPair> SplitSAMRecord(SAMRecord samRecord) {
        LinkedList<PositionPair> retList = new LinkedList<PositionPair>();
        int alignmentStart = samRecord.getAlignmentStart();
        int alignmentEnd = samRecord.getAlignmentEnd();
        boolean isFirst = true;

        int curStart = alignmentStart;
        int curEnd = curStart;
        Cigar cigar = samRecord.getCigar();
        int numCigarElement = cigar.numCigarElements();
        for (int i = 0; i < numCigarElement; i++) {
            CigarElement cigarElement = cigar.getCigarElement(i);
            if (cigarElement.getOperator() == CigarOperator.N) {
                if (!isFirst) {
                    PositionPair posPair = new PositionPair(curStart, curEnd);
                    retList.add(posPair);
                    curStart = curEnd + cigarElement.getLength() + 1;
                    curEnd = curStart;
                } else {
                    curStart = curStart + cigarElement.getLength();
                    curEnd = curStart;
                }
            } else {
                curEnd = curEnd + cigarElement.getLength() - 1;
            }
            isFirst = false;
        }
        if (curEnd <= alignmentEnd) {
            PositionPair posPair = new PositionPair(curStart, curEnd);
            retList.add(posPair);
        }

        return retList;
    }

    private void CountSiteReads(SAMRecord samRecord, int upstream, int downstream, boolean isIP) {
        String chrName = samRecord.getContig();
        LinkedList<PositionPair> alignedReadsList = SplitSAMRecord(samRecord);
        for(PositionPair positionPair : alignedReadsList) {
            LinkedList<SiteRecord> overlappedSiteList = siteIntervalTree.QueryOverlappedSite(chrName, positionPair.getStart(), positionPair.getEnd());
            for(SiteRecord siteRecord : overlappedSiteList) {
                int overlapedStart, overlappedEnd;
                int siteStart = siteRecord.getChrEnd() - upstream - 2;
                int siteEnd = siteRecord.getChrEnd() + downstream + 2;
                //Computed overlapped region
                if(siteStart > positionPair.getStart())
                    overlapedStart = siteStart;
                else
                    overlapedStart = positionPair.getStart();
                if(siteEnd < positionPair.getEnd())
                    overlappedEnd = siteEnd;
                else
                    overlappedEnd = positionPair.getEnd();
                //Computed reads count
                int[] readsVec;
                if(isIP) {
                    readsVec = siteRecord.getIpCountVec();
                } else {
                    readsVec = siteRecord.getInputCountVec();
                }
                for(int i=overlapedStart - siteStart; i<=overlappedEnd - siteStart; i++) {
                    readsVec[i] = readsVec[i] + 1;
                }
            }
        }
    }

    public MeRIPEncoder(File ipBAMFile, File inputBAMFile, LinkedList<SiteRecord> siteRecList, int upstream, int downstream) {
        this.siteRecList = siteRecList;
        siteIntervalTree = new SiteIntervalTree();
        for (SiteRecord siteRec : siteRecList) {
            siteIntervalTree.AddSiteRecordToTree(siteRec, upstream, downstream);
            int[] ipReadsVec = new int[upstream + 5 + downstream];
            int[] inputReadsVec = new int[upstream + 5 + downstream];
            double[] meripVec = new double[upstream + 5 + downstream];
            siteRec.setIpCountVec(ipReadsVec);
            siteRec.setInputCountVec(inputReadsVec);
            if(siteRec.getMeripFeatures() == null)
                siteRec.setMeripFeatures(meripVec);
            //Increase the replicate count to compute average value
            int curReplicateNum = siteRec.getCurReplicateCount() + 1;
            siteRec.setCurReplicateCount(curReplicateNum);
        }

        SamReader ipSAMReader = SamReaderFactory.makeDefault().open(ipBAMFile);
        SamReader inputSAMReader = SamReaderFactory.makeDefault().open(inputBAMFile);
        SAMRecordIterator samRecordIterator;
        //IP total reads
        ipTotalReads = 0;
        for (samRecordIterator = ipSAMReader.iterator(); samRecordIterator.hasNext(); ) {
            SAMRecord samRec = samRecordIterator.next();
            CountSiteReads(samRec, upstream, downstream, true);
            ipTotalReads++;
        }
        samRecordIterator.close();
        System.out.println("Calculated reads in " + ipBAMFile.getName());
        //Input total reads
        inputTotalReads = 0;
        for (samRecordIterator = inputSAMReader.iterator(); samRecordIterator.hasNext(); ) {
            SAMRecord samRec = samRecordIterator.next();
            CountSiteReads(samRec, upstream, downstream, false);
            inputTotalReads++;
        }
        samRecordIterator.close();
        System.out.println("Calculated reads in " + inputBAMFile.getName());

        for(SiteRecord siteRecord : siteRecList) {
            double[] meripVec = siteRecord.getMeripFeatures();
            int[] ipReadsVec = siteRecord.getIpCountVec();
            int[] inputReadsVec = siteRecord.getInputCountVec();
            for(int i=0; i<meripVec.length; i++) {
                //Calculate Audic's model
                int ipCount = ipReadsVec[i];
                int inputCount = inputReadsVec[i];
                double audicValue;
                if ((ipCount != 0) && (inputCount != 0))
                    audicValue = AudicModel.computeNegativeLogP(ipCount, inputCount, ipTotalReads, inputTotalReads);
                else
                    audicValue = 0;
                meripVec[i] = meripVec[i] + (audicValue - meripVec[i])/(double) (siteRecord.getCurReplicateCount());
            }
        }
        System.out.println("Calculated Audic's enrichment value.");

        try {
            ipSAMReader.close();
            inputSAMReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<SiteRecord> getSiteRecList() {
        return siteRecList;
    }
}
