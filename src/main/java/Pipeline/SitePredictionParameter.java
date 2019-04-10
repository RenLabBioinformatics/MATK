package Pipeline;

import java.io.*;
import java.util.HashMap;

public class SitePredictionParameter {
    private int upStream, downStream;
    private double highCutoff, mediumCutoff, lowCutoff;

    public SitePredictionParameter(String parameterResource) {
        try {
            HashMap<String, String> paramMap = new HashMap<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(SitePredictionParameter.class.getResourceAsStream(parameterResource)));
            String strLine;
            String[] strArr;
            while (br.ready()) {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                paramMap.put(strArr[0], strArr[1]);
            }
            br.close();

            upStream = Integer.parseInt(paramMap.get("Up"));
            downStream = Integer.parseInt(paramMap.get("Down"));
            highCutoff = Double.parseDouble(paramMap.get("High"));
            mediumCutoff = Double.parseDouble(paramMap.get("Medium"));
            lowCutoff = Double.parseDouble(paramMap.get("Low"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SitePredictionParameter(File parameterFile) {
        try {
            HashMap<String, String> paramMap = new HashMap<>();

            BufferedReader br = new BufferedReader(new FileReader(parameterFile));
            String strLine;
            String[] strArr;
            while (br.ready()) {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                paramMap.put(strArr[0], strArr[1]);
            }
            br.close();

            upStream = Integer.parseInt(paramMap.get("Up"));
            downStream = Integer.parseInt(paramMap.get("Down"));
            highCutoff = Double.parseDouble(paramMap.get("High"));
            mediumCutoff = Double.parseDouble(paramMap.get("Medium"));
            lowCutoff = Double.parseDouble(paramMap.get("Low"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUpStream() {
        return upStream;
    }

    public int getDownStream() {
        return downStream;
    }

    public double getHighCutoff() {
        return highCutoff;
    }

    public double getMediumCutoff() {
        return mediumCutoff;
    }

    public double getLowCutoff() {
        return lowCutoff;
    }
}
