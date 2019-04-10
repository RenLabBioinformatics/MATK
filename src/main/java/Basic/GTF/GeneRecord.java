/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic.GTF;

import java.util.LinkedList;

/**
 *
 * @author Ben
 */
public class GeneRecord {
    private int start;
    private int end;
    private int strand;
    private String geneId;
    private String geneName;
    private String bioType;
    private LinkedList<TranscriptRecord> transcriptList = new LinkedList();
    private LinkedList<FeatureRecord> featureList = new LinkedList<>();

    public String getBioType() {
        return bioType;
    }

    public void setBioType(String bioType) {
        this.bioType = bioType;
    }

    public void AddTranscript(TranscriptRecord transcript)
    {
        transcriptList.add(transcript);
    }
    
    public  LinkedList<TranscriptRecord> getTranscriptList()
    {
        return transcriptList;
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

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public void AddFeature(FeatureRecord featureRec) {
        featureList.add(featureRec);
    }

    public LinkedList<FeatureRecord> getFeatureList() {
        return featureList;
    }
}
