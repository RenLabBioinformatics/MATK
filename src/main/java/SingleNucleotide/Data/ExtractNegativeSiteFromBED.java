package SingleNucleotide.Data;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.GTF.*;
import Basic.Genome.GenomeTools;
import org.biojava.nbio.genome.parsers.twobit.TwoBitParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ben on 2018/5/7.
 */
public class ExtractNegativeSiteFromBED {
    private HashMap<String, LinkedList<FeatureRecord>> exonRecMap = new HashMap();
    private HashSet<String> methyPosition = new HashSet();
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;
    private Pattern methylationMotif;
    private TwoBitParser twoBitParser;

    private LinkedList<BEDRecord> negativeSiteList;

    private boolean IsExonHasMethyPosition(String chrName, int startPosition, int endPosition, String strand) {
        int methyPos;
        String methyChrName;
        String methyStrand;
        boolean retVal = false;

        LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
        if (bedRecList == null)
            return false;
        for (BEDRecord bedRec : bedRecList) {
            methyChrName = bedRec.getChrName();
            methyPos = bedRec.getChrStart();
            if (bedRec.getStrand() == GenomeTools.SenseStrand)
                methyStrand = "+";
            else
                methyStrand = "-";
            if (chrName.equals(methyChrName) && strand.equals(methyStrand)) {
                if ((startPosition <= methyPos) && (methyPos <= endPosition)) {
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;
    }

    private String getExonId(String featureStr) {
        String retVal = "Unknown";

        if (featureStr.contains("exon_id")) {
            String leftStr = featureStr.substring(featureStr.indexOf("exon_id \"") + 9);
            retVal = leftStr.substring(0, leftStr.indexOf("\""));
        }

        return retVal;
    }

    public ExtractNegativeSiteFromBED(File genomeFile, File gtfFile, File bedFile) {
        try {
            twoBitParser = new TwoBitParser(genomeFile);

            BEDReader bedReader = new BEDReader(bedFile);
            bedRecMap = bedReader.getBEDChromosomeMap();
            for (String chrName : bedRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
                for (BEDRecord bedRec : bedRecList) {
                    String methylSiteStr = bedRec.getChrName() + "|" + bedRec.getChrStart() + "|";
                    if (bedRec.getStrand() == GenomeTools.SenseStrand)
                        methylSiteStr = methylSiteStr + "+";
                    else
                        methylSiteStr = methylSiteStr + "-";
                    methyPosition.add(methylSiteStr);
                }
            }
            System.out.println("Read methylation sites from bed file.");

            ReadGtfFile readGtfFile = new ReadGtfFile();
            readGtfFile.ReadFromFile(gtfFile);
            HashMap<String, ChromosomeRecord> gtfChrRecMap = readGtfFile.getChromosomeMap();
            for (String chrName : gtfChrRecMap.keySet()) {
                ChromosomeRecord chromosomeRecord = gtfChrRecMap.get(chrName);
                if (bedRecMap.containsKey(chrName)) {
                    LinkedList<GeneRecord> geneRecList = chromosomeRecord.getGeneList();
                    for (GeneRecord geneRecord : geneRecList) {
                        LinkedList<TranscriptRecord> transcriptRecList = geneRecord.getTranscriptList();
                        for (TranscriptRecord transcriptRec : transcriptRecList) {
                            LinkedList<FeatureRecord> featureRecList = transcriptRec.getFeatureList();
                            for (FeatureRecord featureRec : featureRecList) {
                                String strandStr;
                                if (featureRec.getStrand() == GenomeTools.SenseStrand)
                                    strandStr = "+";
                                else
                                    strandStr = "-";
                                if (IsExonHasMethyPosition(chrName, featureRec.getStart(), featureRec.getEnd(), strandStr) && featureRec.getFeature().equals("exon")) {
                                    LinkedList<FeatureRecord> tmpFeatureRecList;
                                    if (exonRecMap.containsKey(chrName))
                                        tmpFeatureRecList = exonRecMap.get(chrName);
                                    else {
                                        tmpFeatureRecList = new LinkedList<FeatureRecord>();
                                        exonRecMap.put(chrName, tmpFeatureRecList);
                                    }
                                    tmpFeatureRecList.add(featureRec);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("BED file dose not contain chromosome " + chrName + "! Skip this chromosome!");
                }
            }
            System.out.println("Read methylated exon from genome.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNearestDistance(String chrName, int position) {
        int retVal = Integer.MAX_VALUE;
        if (bedRecMap.containsKey(chrName)) {
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for (BEDRecord bedRec : bedRecList) {
                int distance = Math.abs(bedRec.getChrStart() - position);
                if (distance < retVal)
                    retVal = distance;
            }
        }
        return retVal;
    }

    private void FindNegativeSeqInExon(String chrName, FeatureRecord exonRec, int up, int down, int nearDistance) {
        try {
            String motif = "[A-Z]{" + up + "}[AGT][GA]AC[ACT][A-Z]{" + down + "}";//Construct DRACH motif
            methylationMotif = Pattern.compile(motif);
            twoBitParser.reset();
            String exonSeq = twoBitParser.loadFragment(exonRec.getStart() - 1, exonRec.getEnd() - exonRec.getStart() + 1);
            if (exonRec.getStrand() == GenomeTools.AntiSenseStrand)
                exonSeq = GenomeTools.reverseSeq(exonSeq);

            for (int i = 0; i < exonSeq.length(); i++) {
                char code = exonSeq.charAt(i);
                if (code == 'A') {
                    String methylStr = chrName + "|" + (exonRec.getStart() - 1 + i) + "|";
                    if (exonRec.getStrand() == GenomeTools.SenseStrand)
                        methylStr = methylStr + "+";
                    else
                        methylStr = methylStr + "-";

                    if (!methyPosition.contains(methylStr)) {
                        int negativeSitePosition = exonRec.getStart() - 1 + i;
                        String negativeSiteSeq;
                        twoBitParser.reset();
                        if (exonRec.getStrand() == GenomeTools.SenseStrand) {
                            negativeSiteSeq = twoBitParser.loadFragment(negativeSitePosition - 2 - up, up + 5 + down);
                        } else {
                            negativeSiteSeq = twoBitParser.loadFragment(negativeSitePosition - 2 - down, down + 5 + up);
                            negativeSiteSeq = GenomeTools.reverseSeq(negativeSiteSeq);
                        }
                        Matcher matcher = methylationMotif.matcher(negativeSiteSeq);
                        int nearestDistance = getNearestDistance(chrName, negativeSitePosition);
                        if (matcher.matches()) {
                            if (nearestDistance >= nearDistance) {
                                BEDRecord bedRecord = new BEDRecord(chrName, negativeSitePosition, negativeSitePosition + 1, exonRec.getStrand());
                                bedRecord.setName(negativeSiteSeq);
                                negativeSiteList.add(bedRecord);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean IsContainsChromosome(String chrName, String[] chromosomes) {
        boolean retVal = false;
        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i].equals(chrName)) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }

    public void ExtractNegativeSeq(int up, int down, int nearDistance) {
        try {
            String[] chromosomes = twoBitParser.getSequenceNames();
            negativeSiteList = new LinkedList<BEDRecord>();
            for (String chrName : exonRecMap.keySet()) {
                System.out.println("Extracting negative sites from chromosome " + chrName);
                if (IsContainsChromosome(chrName, chromosomes)) {
                    twoBitParser.setCurrentSequence(chrName);
                    LinkedList<FeatureRecord> featureRecList = exonRecMap.get(chrName);
                    for (FeatureRecord featureRec : featureRecList) {
                        FindNegativeSeqInExon(chrName, featureRec, up, down, nearDistance);
                    }
                    twoBitParser.close();
                } else {
                    System.out.println("Genome file does not contain chromosome " + chrName + "! Skip this chromosome!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SaveMethylationSite(File savePath) {
        try {
            FileWriter fw = new FileWriter(savePath);
            fw.write("Chromosome Name\tStart Position\tEnd Position\tStrand\tSite Sequence\n");
            for (BEDRecord bedRec : negativeSiteList) {
                fw.write(bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t");
                if (bedRec.getStrand() == GenomeTools.SenseStrand)
                    fw.write("+\t");
                else
                    fw.write("-\t");
                fw.write(bedRec.getName() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveMethylationSiteInFASTA(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for (BEDRecord bedRec : negativeSiteList) {
                fw.write(">" + bedRec.getChrName() + "|" + bedRec.getChrStart() + "-" + bedRec.getChrEnd() + "|" + GenomeTools.FormatStrand(bedRec.getStrand()) + "\n");
                fw.write(bedRec.getName() + "\n");//write sequence
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CloseGenome() {
        try {
            twoBitParser.closeParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ExtractNegativeSiteFromBED extractNegativeSiteFromBED = new ExtractNegativeSiteFromBED(new File("E:\\Genome\\Mouse\\mm10\\Mus_musculus.GRCm38.chr.2bit"),
                new File("E:\\Genome\\Mouse\\mm10\\Mus_musculus.GRCm38.94.chr.gtf"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\CombineData\\Mouse_mm10.bed"));
        extractNegativeSiteFromBED.ExtractNegativeSeq(30, 30, 200);
//        extractNegativeSiteFromBED.SaveMethylationSiteInFASTA(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TestData\\Zebrafish_SRAMP_Negative.fa"));
        extractNegativeSiteFromBED.SaveMethylationSite(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Negative.txt"));
        extractNegativeSiteFromBED.CloseGenome();
    }
}
