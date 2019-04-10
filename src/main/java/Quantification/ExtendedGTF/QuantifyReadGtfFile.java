package Quantification.ExtendedGTF;

import Basic.GTF.FeatureRecord;
import Basic.GTF.GeneAttribute;
import Basic.GTF.TranscriptRecord;
import Basic.Genome.GenomeTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Tong on 2018/10/19.
 */
public class QuantifyReadGtfFile{
    HashMap<String, QuantifyChromosomeRecord> chromosomeMap = new HashMap();

    private GeneAttribute ParseAttributes(String attributes)
    {
        String[] strArr = attributes.split("\";");
        String geneID = "Unknown";
        String geneName = "Unknown";
        String transcriptID = "Unknown";
        String transcriptName = "Unknown";
        String geneBioType = "Unknown";
        for(int i=0; i<strArr.length; i++)
        {
            strArr[i] = strArr[i].trim();
            if(strArr[i].startsWith("gene_id"))
            {
                geneID = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
            if(strArr[i].startsWith("gene_name"))
            {
//                System.out.println(strArr[i]);
                geneName = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
            if(strArr[i].startsWith("transcript_id"))
            {
                transcriptID = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
            if(strArr[i].startsWith("transcript_name"))
            {
                transcriptName = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
            if(strArr[i].startsWith("gene_biotype"))
            {
                geneBioType = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
            if(strArr[i].startsWith("gene_type"))
            {
                geneBioType = strArr[i].substring(strArr[i].indexOf("\"") + 1, strArr[i].length());
            }
        }
        GeneAttribute geneAttr = new GeneAttribute();
        geneAttr.setGeneID(geneID);
        if(geneName.equals("Unknown") && (!geneID.equals("Unknown")) )
            geneAttr.setGeneName(geneID);
        else
            geneAttr.setGeneName(geneName);
        geneAttr.setTranscriptID(transcriptID);
        if(transcriptName.equals("Unknown") && (!transcriptID.equals("Unknown")) )
            geneAttr.setTranscriptName(transcriptID);
        else
            geneAttr.setTranscriptName(transcriptName);
        geneAttr.setBioType(geneBioType);

        return geneAttr;
    }

    public void ReadFromFile(File gtfFile)
    {
        try
        {
            chromosomeMap.clear();
            BufferedReader br = new BufferedReader(new FileReader(gtfFile));
            String strLine;
            String[] strArr;
            GeneAttribute geneAttr = null;
            QuantifyGeneRecord geneRec = null;
            QuantifyChromosomeRecord chrRec;
            TranscriptRecord transcriptRec = null;

            while(br.ready())
            {
                strLine = br.readLine();
                if(!strLine.startsWith("#"))
                {
                    strArr = strLine.split("\t");
//                    if(strArr[0].equals("MT"))
//                        strArr[0] = "M";
//                    if(!strArr[0].startsWith("chr"))
//                        strArr[0] = "chr" + strArr[0];
                    //Parse attributes
                    geneAttr = ParseAttributes(strArr[8]);
                    //
                    if(chromosomeMap.containsKey(strArr[0]))
                        chrRec = chromosomeMap.get(strArr[0]);
                    else
                    {
                        chrRec = new QuantifyChromosomeRecord();
                        chrRec.setChromosomeName(strArr[0]);
                        chromosomeMap.put(strArr[0], chrRec);
                    }
                    //Add gene
                    if(strArr[2].equals("gene"))
                    {
                        geneRec = new QuantifyGeneRecord();
                        geneRec.setStart(Integer.parseInt(strArr[3]));
                        geneRec.setEnd(Integer.parseInt(strArr[4]));
                        geneRec.setGeneId(geneAttr.getGeneID());
                        geneRec.setGeneName(geneAttr.getGeneName());
                        geneRec.setBioType(geneAttr.getBioType());
                        if(strArr[6].equals("+"))
                            geneRec.setStrand(GenomeTools.SenseStrand);
                        else
                            geneRec.setStrand(GenomeTools.AntiSenseStrand);
                        chrRec.AddGene(geneRec);
                    }
                    else if(strArr[2].equals("transcript"))
                    {
                        transcriptRec = new TranscriptRecord();
                        transcriptRec.setStart(Integer.parseInt(strArr[3]));
                        transcriptRec.setEnd(Integer.parseInt(strArr[4]));
                        transcriptRec.setTranscriptId(geneAttr.getTranscriptID());
                        transcriptRec.setTranscriptName(geneAttr.getTranscriptName());
                        if(strArr[6].equals("+"))
                            transcriptRec.setStrand(GenomeTools.SenseStrand);
                        else
                            transcriptRec.setStrand(GenomeTools.AntiSenseStrand);
                        geneRec.AddTranscript(transcriptRec);
                    }
                    else
                    {
                        FeatureRecord featureRec = new FeatureRecord();
                        featureRec.setEnd(Integer.parseInt(strArr[4]));
                        featureRec.setFeature(strArr[2]);
                        featureRec.setStart(Integer.parseInt(strArr[3]));
                        if (strArr[6].equals("+")) {
                            featureRec.setStrand(GenomeTools.SenseStrand);
                        } else {
                            featureRec.setStrand(GenomeTools.AntiSenseStrand);
                        }
                        featureRec.setTranscriptId(geneAttr.getTranscriptID());
                        featureRec.setTranscriptName(geneAttr.getTranscriptName());
                        transcriptRec.AddFeature(featureRec);
                    }
                }
            }

            br.close();
        }
        catch(IOException e)
        {

        }
    }

    public HashMap<String, QuantifyChromosomeRecord> getChromosomeMap()
    {
        return chromosomeMap;
    }
}
