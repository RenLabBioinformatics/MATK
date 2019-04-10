package SingleNucleotide.Data.PredictionEncode;

import Basic.BED.BEDRecord;
import SingleNucleotide.Data.SiteEncoder;
import SingleNucleotide.Data.SiteRecord;

import java.util.LinkedList;

public class PeakEncodeRecord {
    private BEDRecord peakBEDRec;
    private LinkedList<SiteRecord> potentialSiteList;

    public PeakEncodeRecord(BEDRecord peakBEDRec) {
        this.peakBEDRec = peakBEDRec;
        potentialSiteList = new LinkedList<>();
    }

    public BEDRecord getPeakBEDRec() {
        return peakBEDRec;
    }

    public LinkedList<SiteRecord> getPotentialSiteList() {
        return potentialSiteList;
    }

    public void AddPotentialSite(SiteRecord siteRecord) {
        potentialSiteList.add(siteRecord);
    }
}
