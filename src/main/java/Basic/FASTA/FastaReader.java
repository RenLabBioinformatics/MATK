package Basic.FASTA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class FastaReader {
    LinkedList<FastaRecord> fastaRecList;
    public static final String nucleotideCode ="AGCTURYNWSMKBHDV";
    public static final String aminoAcidCode = "ARNDCQEGHILKMFPSTWYVBZJUOX*";
    public static final int Nucleotide = 1;
    public static final int AminoAcid = 2;
    private int type;

    private String FormatSequence(String inputSeq, int type) {
        String retSeq = "";
        for(int i=0; i<inputSeq.length(); i++) {
            char curCode = inputSeq.charAt(i);
            switch (type) {
                case Nucleotide:
                    if(nucleotideCode.indexOf(curCode) != -1)
                        retSeq = retSeq + curCode;
                    else
                        retSeq = retSeq + "N";
                    break;
                case AminoAcid:
                    if(aminoAcidCode.indexOf(curCode) != -1)
                        retSeq = retSeq + curCode;
                    else
                        retSeq = retSeq + "X";
                    break;
                default:
                    if(nucleotideCode.indexOf(curCode) != -1)
                        retSeq = retSeq + curCode;
                    else
                        retSeq = retSeq + "N";
            }
        }
        return retSeq;
    }

    public FastaReader(File fastaFile, int type) {
        try {
            this.type = type;
            fastaRecList = new LinkedList<>();
            BufferedReader br = new BufferedReader(new FileReader(fastaFile));
            String strLine;
            String name = null;
            StringBuilder seq = new StringBuilder("");
            while(br.ready()) {
                strLine = br.readLine();
                if(strLine.startsWith(">")) {
                    if(!seq.toString().isEmpty()) {
                        if(name == null)
                            name = "Unkown";
                        FastaRecord fasRec = new FastaRecord(name, seq.toString());
                        fastaRecList.add(fasRec);
                    }
                    name = strLine.substring(1).trim();
                    seq = new StringBuilder("");
                } else {
                    strLine = strLine.toUpperCase();
                    seq.append(FormatSequence(strLine, type));
                }
            }
            if(!seq.toString().isEmpty()) {
                if(name == null)
                    name = "Unkown";
                FastaRecord fasRec = new FastaRecord(name, seq.toString());
                fastaRecList.add(fasRec);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<FastaRecord> getFastaRecList() {
        return fastaRecList;
    }

    public static void main(String[] args) {
        FastaReader fastaReader = new FastaReader(new File("E:\\工作文档\\m6A\\实验相关\\HIV.fasta"), FastaReader.Nucleotide);
        fastaReader.getFastaRecList();
    }
}
