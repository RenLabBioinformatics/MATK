package PeakCalling.PeakDistribution;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.BED.BLOCKRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/*
* AnnotateSite the transcript location of a given BED file
* */
public class AnnotateBEDDistrubution {
    private LinkedList<BEDRecord> BEDRecList;
    private AnnotateDistribution annotateDistribution;
    private double[] locationVariable;
    private LinkedList<DataPoint> densitiyDataList;

    public AnnotateBEDDistrubution(File bedFile, File gtfFile) {
        BEDReader bedReader = new BEDReader(bedFile);
        BEDRecList = bedReader.getBedRecList();

        annotateDistribution = new AnnotateDistribution(gtfFile);
    }

    private int CalculateCenterPosition(BEDRecord bedRecord) {
        int retPos = -1;

        LinkedList<BLOCKRecord> blockList = bedRecord.getBlockRecList();
        if(blockList == null)
            retPos = bedRecord.getChrStart() + ((bedRecord.getChrEnd() - bedRecord.getChrStart())/2) + 1;//convert 0-base coordinate to 1-base coordinate
        else {
            //Calculate total length
            int totalLen = 0;
            for(BLOCKRecord blockRec : blockList) {
                totalLen = totalLen + (blockRec.getChrEnd() - blockRec.getChrStart() + 1);
            }
            int halfLen = totalLen/2;
            //Find center position
            int curLen = 0;
            for(BLOCKRecord blockRec : blockList) {
                double tmpLen = curLen + (blockRec.getChrEnd() - blockRec.getChrStart() + 1);
                if(tmpLen >= halfLen) {
                    retPos = blockRec.getChrStart() + (halfLen - curLen);
                    break;
                } else {
                    curLen = curLen + (blockRec.getChrEnd() - blockRec.getChrStart() + 1);
                }
            }
        }

        return retPos;
    }

    public void Annotate(boolean isConsiderStrand) {
        LinkedList<Double> locationList = new LinkedList<>();
        for(BEDRecord bedRec : BEDRecList) {
            int centerPos = CalculateCenterPosition(bedRec);
            double location = annotateDistribution.AnnotateTranscriptLocation(bedRec.getChrName(), centerPos, bedRec.getStrand(), isConsiderStrand);
            if(location != -1)
                locationList.add(location);
        }
        //Convert to array
        locationVariable = new double[locationList.size()];
        int index = 0;
        for(double location : locationList) {
            locationVariable[index] = location;
            index++;
        }
    }

    public void EstimateDensity(int numOfPoint) {
        densitiyDataList = new LinkedList<>();
        KernelEstimation kernelEstimation = new KernelEstimation(locationVariable);
        double step = 300D/((double) numOfPoint);
        for(double x=0; x<=300; x=x+step) {
            DataPoint dataPoint = new DataPoint();
            dataPoint.setX(x);
            dataPoint.setY(kernelEstimation.Density(x));
            densitiyDataList.add(dataPoint);
        }
    }

    public double[] getLocationVariable() {
        return locationVariable;
    }

    public LinkedList<DataPoint> getDensitiyDataList() {
        return densitiyDataList;
    }

    public void SaveVariable(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(int i=0; i<locationVariable.length; i++) {
                fw.write(locationVariable[i] + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveDensity(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("X\tY\n");
            for(DataPoint dataPoint : densitiyDataList) {
                fw.write(dataPoint.getX() + "\t" + dataPoint.getY() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AnnotateBEDDistrubution annotateBEDDistrubution = new AnnotateBEDDistrubution(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\GPD_CDF\\SRR2120870_SRR2120874.bed"),
                new File("E:\\Genome\\Mouse\\mm10\\Mus_musculus.GRCm38.94.gtf"));
        annotateBEDDistrubution.Annotate(false);
//        annotateBEDDistrubution.SaveVariable(new File("E:\\工作文档\\MATK\\PeakCalling\\MACS_nanpore.txt"));
        annotateBEDDistrubution.EstimateDensity(300);
        annotateBEDDistrubution.SaveDensity(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\GPD_CDF\\SRR2120870_SRR2120874_Distribution.txt"));
    }
}
