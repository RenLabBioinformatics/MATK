package SingleNucleotide.Data.PSSM;

import java.io.*;
import java.util.LinkedList;

public class PSSMReader {
    private String alphabet;
    private double[][] pssmMatrix;

    public PSSMReader(String matrixResource) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(PSSMReader.class.getResourceAsStream(matrixResource)));
            String strLine;
            LinkedList<String> lines = new LinkedList<>();
            while(br.ready()) {
                strLine = br.readLine();
                lines.add(strLine);
            }
            br.close();

            int lineNumber = 0;
            String[] strArr;
            alphabet = "";
            for(String line : lines) {
                strArr = line.split("\t");
                if(lineNumber == 0) {
                    pssmMatrix = new double[lines.size() - 1][strArr.length];
                } else {
                    alphabet = alphabet + strArr[0];
                    for(int i=1; i<strArr.length; i++) {
                        pssmMatrix[lineNumber - 1][i - 1] = Double.parseDouble(strArr[i]);
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PSSMReader(File matrixFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(matrixFile));
            String strLine;
            LinkedList<String> lines = new LinkedList<>();
            while(br.ready()) {
                strLine = br.readLine();
                lines.add(strLine);
            }
            br.close();

            int lineNumber = 0;
            String[] strArr;
            alphabet = "";
            for(String line : lines) {
                strArr = line.split("\t");
                if(lineNumber == 0) {
                    pssmMatrix = new double[lines.size() - 1][strArr.length];
                } else {
                    alphabet = alphabet + strArr[0];
                    for(int i=1; i<strArr.length; i++) {
                        pssmMatrix[lineNumber - 1][i - 1] = Double.parseDouble(strArr[i]);
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAlphabet() {
        return alphabet;
    }

    public double[][] getPssmMatrix() {
        return pssmMatrix;
    }

    /**
     * Position: 0-base coordinate
     */
    public double getPSSMValue(char code, int position) {
        int codeIndex = alphabet.indexOf(code);

        if(codeIndex == -1)
            codeIndex = 0;
        if(position < 0)
            position = 0;
        if(position >= pssmMatrix[0].length)
            position = pssmMatrix[0].length - 1;

        return pssmMatrix[codeIndex][position];
    }

    public static void main(String[] args) {
        PSSMReader pssmReader = new PSSMReader(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Human_PSSM.txt"));
        System.out.println(pssmReader.getPSSMValue('A', 0));
        System.out.println(pssmReader.getPSSMValue('T',38));
    }
}
