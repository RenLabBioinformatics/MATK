/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Basic.GTF;

/**
 *
 * @author Ben
 */
public class FeatureRecord implements Cloneable {
    private String feature;
    private int start;
    private int end;
    private int strand;
    private String transcriptId;
    private String transcriptName;

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getTranscriptName() {
        return transcriptName;
    }

    public void setTranscriptName(String transcriptName) {
        this.transcriptName = transcriptName;
    }

    public Object clone() {
        FeatureRecord featureRecord = new FeatureRecord();
        featureRecord.setStart(this.start);
        featureRecord.setEnd(this.end);
        featureRecord.setStrand(this.strand);
        featureRecord.setFeature(this.feature);
        featureRecord.setTranscriptId(this.transcriptId);
        featureRecord.setTranscriptName(this.transcriptName);
        return featureRecord;
    }
}
