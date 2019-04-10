import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.BED.BEDWriter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class SplitPeaks {
    private HashMap<String, LinkedList<BEDRecord>> peakRecMap;
    private HashMap<String, LinkedList<BEDRecord>> siteRecMap;
    private LinkedList<BEDRecord> peaksWithSiteList, peaksWithoutSiteList;

    public SplitPeaks(File bedFile, File siteBEDFile) {
        BEDReader bedReader = new BEDReader(bedFile);
        peakRecMap = bedReader.getBEDChromosomeMap();

        BEDReader siteBEDReader = new BEDReader(siteBEDFile);
        siteRecMap = siteBEDReader.getBEDChromosomeMap();
    }

    private boolean isContainSite(BEDRecord peakBEDRec) {
        boolean retVal = false;
        String chrName = peakBEDRec.getChrName();
        if(siteRecMap.containsKey(chrName)) {
            LinkedList<BEDRecord> siteRecList = siteRecMap.get(chrName);
            for(BEDRecord siteRec : siteRecList) {
                if( (peakBEDRec.getChrStart() <= siteRec.getChrStart()) && (peakBEDRec.getChrEnd() >= siteRec.getChrStart()) ) {
                    retVal = true;
                    break;
                }
            }
        } else
            retVal = false;
        return retVal;
    }

    //divide peaks into two groups: peaks with sites and peaks without sites
    public void Split() {
        peaksWithSiteList = new LinkedList<>();
        peaksWithoutSiteList = new LinkedList<>();
        for(String chrName : peakRecMap.keySet()) {
            LinkedList<BEDRecord> peakRecList = peakRecMap.get(chrName);
            for(BEDRecord peakRec : peakRecList) {
                if(isContainSite(peakRec))
                    peaksWithSiteList.add(peakRec);
                else
                    peaksWithoutSiteList.add(peakRec);
            }
        }
    }

    public void SaveSplitPeaks(File peaksWithSitesFile, File peaksWithoutSitesFile) {
        BEDWriter bedWriterWithSites = new BEDWriter(peaksWithSiteList);
        bedWriterWithSites.SaveInSimpleBED(peaksWithSitesFile);

        BEDWriter bedWriterWithoutSites = new BEDWriter(peaksWithoutSiteList);
        bedWriterWithoutSites.SaveInSimpleBED(peaksWithoutSitesFile);
    }

    public static void main(String[] args) {
        SplitPeaks splitPeaks = new SplitPeaks(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\IP-Input_BLOCK.bed"), new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\sites.bed"));
        splitPeaks.Split();
        splitPeaks.SaveSplitPeaks(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\peaksWithSite_BLOCK.bed"), new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\peaksWithoutSite_BLOCK.bed"));
    }
}
