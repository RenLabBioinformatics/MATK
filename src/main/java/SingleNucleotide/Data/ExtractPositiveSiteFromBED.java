package SingleNucleotide.Data;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.Genome.GenomeTools;
import org.biojava.nbio.genome.parsers.twobit.TwoBitParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ben on 2018/5/7.
 */
public class ExtractPositiveSiteFromBED {
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap = new HashMap();
    private Pattern methylationMotif;
    private TwoBitParser twoBitParser;

    private LinkedList<BEDRecord> extractedBEDList;

    public ExtractPositiveSiteFromBED(File bedFile, File genomeFile) {
        try {
            twoBitParser = new TwoBitParser(genomeFile);
            BEDReader bedReader = new BEDReader(bedFile);
            bedRecMap = bedReader.getBEDChromosomeMap();

            extractedBEDList = new LinkedList<BEDRecord>();
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

    public void ExtractSeq(int up, int down) {
        try {
            extractedBEDList.clear();

            String motif = "[A-Z]{" + up + "}[AGT][GA]AC[ACT][A-Z]{" + down + "}";//Construct DRACH motif
            methylationMotif = Pattern.compile(motif);
            LinkedList<BEDRecord> bedList;
            String methylatedNucleotide;

            String[] chromosomes = twoBitParser.getSequenceNames();

            for (String chrName : bedRecMap.keySet()) {
                bedList = bedRecMap.get(chrName);
                if (IsContainsChromosome(chrName, chromosomes)) {
                    twoBitParser.setCurrentSequence(chrName);
                    System.out.println("Extract methylation segment in chromosome " + chrName);
                    for (BEDRecord bedRec : bedList) {
                        twoBitParser.reset();
                        if (bedRec.getStrand() == GenomeTools.SenseStrand)
                            methylatedNucleotide = twoBitParser.loadFragment(bedRec.getChrStart() - 2 - up, up + 5 + down);
                        else {
                            methylatedNucleotide = twoBitParser.loadFragment(bedRec.getChrStart() - 2 - down, down + 5 + up);
                            methylatedNucleotide = GenomeTools.reverseSeq(methylatedNucleotide);
                        }
                        Matcher matcher = methylationMotif.matcher(methylatedNucleotide);
                        if (matcher.matches()) {
                            bedRec.setName(methylatedNucleotide);
                            extractedBEDList.add(bedRec);
                        }
                    }

                    twoBitParser.close();
                } else {
                    System.out.println("There are no chromosome " + chrName + " in the genome! Skipped this chromosome!");
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
            for (BEDRecord bedRec : extractedBEDList) {
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
            for (BEDRecord bedRec : extractedBEDList) {
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
            twoBitParser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ExtractPositiveSiteFromBED extractPositiveSiteFromBed = new ExtractPositiveSiteFromBED(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\CombineData\\Mouse_mm10.bed"),
                new File("E:\\Genome\\Mouse\\mm10\\Mus_musculus.GRCm38.chr.2bit"));
        extractPositiveSiteFromBed.ExtractSeq(30, 30);
//        extractPositiveSiteFromBed.SaveMethylationSiteInFASTA(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TestData\\Zebrafish_SRAMP_Positive.fa"));
        extractPositiveSiteFromBed.SaveMethylationSite(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Positive.txt"));
    }
}
