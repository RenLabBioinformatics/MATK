package Pipeline;

import java.util.LinkedList;

public class PeakResultRecord {
    private String chrName;
    private int peakStart;
    private int peakEnd;
    private double peakScore;
    private String peakName;
    private int strand;
    private String geneName;
    private double m6aLevel;
    private LinkedList<SiteResultRecord> siteResRecList;

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getPeakStart() {
        return peakStart;
    }

    public void setPeakStart(int peakStart) {
        this.peakStart = peakStart;
    }

    public int getPeakEnd() {
        return peakEnd;
    }

    public void setPeakEnd(int peakEnd) {
        this.peakEnd = peakEnd;
    }

    public double getPeakScore() {
        return peakScore;
    }

    public void setPeakScore(double peakScore) {
        this.peakScore = peakScore;
    }

    public String getPeakName() {
        return peakName;
    }

    public void setPeakName(String peakName) {
        this.peakName = peakName;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public double getM6aLevel() {
        return m6aLevel;
    }

    public void setM6aLevel(double m6aLevel) {
        this.m6aLevel = m6aLevel;
    }

    public void AddSiteRecord(SiteResultRecord siteResultRecord) {
        if(siteResRecList == null)
            siteResRecList = new LinkedList<>();
        siteResRecList.add(siteResultRecord);
    }

    public LinkedList<SiteResultRecord> getSiteResRecList() {
        return siteResRecList;
    }
}
