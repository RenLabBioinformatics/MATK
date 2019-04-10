package PeakCalling;

import Basic.BED.BEDRecord;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/24.
 */
public class SingleSamplePeakCalling {
    private HashMap<String, LinkedList<PeakRecord>> peakListMap;

    public SingleSamplePeakCalling(File trainingData, File locationFile, double q) {
        VAEPeakCalling vaePeakCalling = new VAEPeakCalling(trainingData);
        System.out.println("Begin fitting the MeRIP-seq data...");
        vaePeakCalling.Train(500, 10);
        System.out.println("Fitting done. Begin peak calling...");
        vaePeakCalling.Predict(locationFile, 500);

        System.out.println("Determining peak calling threshold using PSO algorithm...");
        FilterPeaks filterPeaks = new FilterPeaks(vaePeakCalling.getBinResMap(), "/Param.txt", q);
        filterPeaks.Filtering();

        System.out.println("Merging bins...");
        MergeBin mergeBin = new MergeBin(filterPeaks.getPeakBinResMap());
        mergeBin.Merge();
        peakListMap = mergeBin.getPeakListMap();
    }

    public HashMap<String, LinkedList<BEDRecord>> getBEDRecMap() {
        HashMap<String, LinkedList<BEDRecord>> bedRecMap = new HashMap<>();
        int index = 1;
        for(String chrName : peakListMap.keySet()) {
            LinkedList<PeakRecord> peakRecList = peakListMap.get(chrName);
            LinkedList<BEDRecord> bedRecList = new LinkedList<>();
            for(PeakRecord peakRec : peakRecList) {
                BEDRecord bedRec = new BEDRecord(chrName, (peakRec.getStartIndex()*25 + 1), ( (peakRec.getEndIndex() + 1)*25 ));
                bedRec.setScore(peakRec.getProbability());
                bedRec.setName("Peak" + index);
                index++;
                bedRecList.add(bedRec);
            }
            bedRecMap.put(chrName, bedRecList);
        }
        return bedRecMap;
    }

    public void SavePeakBin(File saveFile, String format) {
        ResultWriter resultWriter = new ResultWriter(peakListMap);
        if(format.equalsIgnoreCase("tsv")) {
            System.out.println("Saving result in TSV format.");
            resultWriter.SaveInTSV(saveFile);
        } else if (format.equalsIgnoreCase("bed")) {
            System.out.println("Saving result in BED format.");
            resultWriter.SaveInBED(saveFile);
        } else {
            System.out.println("Specify unsupported output format: " + format.toUpperCase() + ". Set default to TSV.");
            resultWriter.SaveInTSV(saveFile);
        }
    }

    public static void main(String[] args) {
        SingleSamplePeakCalling singleSamplePeakCalling = new SingleSamplePeakCalling(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\GPD_CDF\\SRR2120887_SRR2120890.bed.csv"),
                new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\GPD_CDF\\SRR2120887_SRR2120890.bed.loc"), 0.05);
        singleSamplePeakCalling.SavePeakBin(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\GPD_CDF\\SRR2120887_SRR2120890.bed"), "bed");
    }
}
