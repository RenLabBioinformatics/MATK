package BAMProcess;

import htsjdk.samtools.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben on 2016/9/6.
 */
public class BAMFileReader
{
    private SamReader samReader;
    private HashMap<String, Integer> sequenceDicMap;
    private double totalReads;


    public BAMFileReader(File bamFile)
    {
        samReader = SamReaderFactory.makeDefault().open(bamFile);
        sequenceDicMap = new HashMap<String, Integer>();
        //Read sequence dictionary
        SAMFileHeader header = samReader.getFileHeader();
        SAMSequenceDictionary sequenceDictionary = header.getSequenceDictionary();
        List<SAMSequenceRecord> seqRecList = sequenceDictionary.getSequences();//get chromosome contig
        for(SAMSequenceRecord samSeqRec : seqRecList)
        {
            String chrName = samSeqRec.getSequenceName();
            int chrLen = samSeqRec.getSequenceLength();
            sequenceDicMap.put(chrName, chrLen);
        }
    }

    private LinkedList<PositionPair> SplitSAMRecord(SAMRecord samRecord)
    {
        LinkedList<PositionPair> retList = new LinkedList<PositionPair>();
        int alignmentStart = samRecord.getAlignmentStart();
        int alignmentEnd = samRecord.getAlignmentEnd();
        boolean isFirst = true;

        int curStart = alignmentStart;
        int curEnd = curStart;
        Cigar cigar = samRecord.getCigar();
        int numCigarElement = cigar.numCigarElements();
        for(int i=0; i<numCigarElement; i++)
        {
            CigarElement cigarElement = cigar.getCigarElement(i);
            if(cigarElement.getOperator() == CigarOperator.N)
            {
                if(!isFirst)
                {
                    PositionPair posPair = new PositionPair(curStart, curEnd);
                    retList.add(posPair);
                    curStart = curEnd + cigarElement.getLength() + 1;
                    curEnd = curStart;
                }
                else
                {
                    curStart = curStart + cigarElement.getLength();
                    curEnd = curStart;
                }
            }
            else
            {
                curEnd = curEnd + cigarElement.getLength() - 1;
            }
            isFirst = false;
        }
        if(curEnd <= alignmentEnd)
        {
            PositionPair posPair = new PositionPair(curStart, curEnd);
            retList.add(posPair);
        }

        return retList;
    }

    public HashMap<String, ChromosomeBin> Binning(int binSize)
    {
        totalReads = 0;
        HashMap<String, ChromosomeBin> binMap = new HashMap<String, ChromosomeBin>();
        for(String chrName : sequenceDicMap.keySet())
        {
            ChromosomeBin chrBin = new ChromosomeBin();
            binMap.put(chrName, chrBin);
        }
        //get bin count
        SAMRecordIterator samRecItr;
        for(samRecItr = samReader.iterator(); samRecItr.hasNext();)
        {
            SAMRecord samRecord = samRecItr.next();
            String refName = samRecord.getContig();
            //split reads, removed regions with Cigar string 'N'
            LinkedList<PositionPair> splitList = SplitSAMRecord(samRecord);
            if(binMap.containsKey(refName))
            {
                ChromosomeBin chrBin = binMap.get(refName);
                for(PositionPair posPair : splitList)
                {
                    int alignmentStart = posPair.getStart();
                    int alignmentEnd = posPair.getEnd();
                    if( (alignmentStart > 0) && (alignmentEnd > 0))
                    {
                        int startBinIndex = (int)Math.floor( ((double)alignmentStart / (double)binSize) );
                        int endBinIndex = (int)Math.floor( ((double)alignmentEnd / (double)binSize) );
                        for(int i=startBinIndex; i<=endBinIndex; i++)
                        {
                            chrBin.AddBAMBin(i, 1);
                        }
                        totalReads++;
                    }
                    else
                        System.out.println("Reads " + samRecord.getReadName() + " not mapped, skipped!");
                }

            }
            else
                System.out.println("No reference name " + refName + " in genome.");

        }
        samRecItr.close();

        NormalizedBin(binMap);
        return binMap;
    }


    private void NormalizedBin(HashMap<String, ChromosomeBin> binMap)
    {
        for(String chrName : binMap.keySet())
        {
            ChromosomeBin chrBin = binMap.get(chrName);
            LinkedList<Integer> binIndexList = chrBin.getAllBinIndex();
            for(int index : binIndexList)
            {
                BAMBin bin = chrBin.getBAMBin(index);
                double curRate = bin.getReadCount();
                curRate = (curRate/totalReads)*1000000;
                bin.setReadCount(curRate);
            }
        }
    }

    public HashMap<String, Integer> getSequenceDicMap() {
        return sequenceDicMap;
    }

    public double getTotalReads()
    {
        return totalReads;
    }

    public void SaveBinMap(HashMap<String, ChromosomeBin> binMap, String saveFile)
    {
        try
        {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : binMap.keySet())
            {
                ChromosomeBin chrBin = binMap.get(chrName);
                LinkedList<Integer> binIndexList = chrBin.getAllBinIndex();
                for(int index : binIndexList)
                {
                    BAMBin bin = chrBin.getBAMBin(index);
                    fw.write(chrName + "\t" + bin + "\n");
                }
            }
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void CloseBAMFile()
    {
        try
        {
            samReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        BAMFileReader bamFileReader = new BAMFileReader(new File("G:\\云同步文件夹\\工作文档\\RNA-methylation\\PeakCalling\\DeepRIP\\Application_data\\bam\\SRR847360_sort_uniq_IP.bam"));
        HashMap<String, ChromosomeBin> chrBin = bamFileReader.Binning(25);
//        bamFileReader.SaveBinMap(chrBin, "G:\\云同步文件夹\\工作文档\\RNA-methylation\\PeakCalling\\DeepRIP\\Application_data\\bam\\IPTest.bin");
        bamFileReader.CloseBAMFile();

//        BAMFileReader.CreateBAMIndex(new File("E:\\DeepRIPTest\\KeBam\\SRR2120887_sort_uniq_ip.bam"));
    }
}
