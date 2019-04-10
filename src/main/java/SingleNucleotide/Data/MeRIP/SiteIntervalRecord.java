package SingleNucleotide.Data.MeRIP;

import SingleNucleotide.Data.SiteRecord;
import htsjdk.tribble.index.interval.Interval;

public class SiteIntervalRecord extends Interval {
    private SiteRecord siteRecord;

    public SiteIntervalRecord(SiteRecord siteRecord, int upstream, int downstream) {
        super(siteRecord.getChrEnd() - upstream - 2, siteRecord.getChrEnd() + downstream + 2); // store 1-base coordinate
        this.siteRecord = siteRecord;
    }

    public SiteRecord getSiteRecord() {
        return siteRecord;
    }
}
