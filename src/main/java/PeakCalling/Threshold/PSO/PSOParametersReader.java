package PeakCalling.Threshold.PSO;


import java.io.*;

/**
 * Created by Ben on 2016/8/25.
 */
public class PSOParametersReader
{
    private PSOParameters psoParam;

    public PSOParametersReader()
    {
        psoParam = new PSOParameters();
        psoParam.setSwarmSize(100);
        psoParam.setNeighborSize(2);
        psoParam.setInertiaWeight(0.8);
        psoParam.setRandomCoefficients1(2.1);
        psoParam.setRandomCoefficients2(2.1);
        psoParam.setVmax(1);
        psoParam.setInitialLowerLimit(-1);
        psoParam.setInitialUpperLimit(1);
    }

    public void ReadFromFile(String filePath)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(PSOParametersReader.class.getResourceAsStream(filePath)));
            String strLine;
            String[] strArr;
            while(br.ready())
            {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                if(strArr[0].equals("SwarmSize"))
                    psoParam.setSwarmSize(Integer.parseInt(strArr[1]));
                else if(strArr[0].equals("NeighborSize"))
                    psoParam.setNeighborSize(Integer.parseInt(strArr[1]));
                else if(strArr[0].equals("InertiaWeight"))
                    psoParam.setInertiaWeight(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("pBestCoff"))
                    psoParam.setRandomCoefficients1(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("LeaderCoff"))
                    psoParam.setRandomCoefficients2(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("MaxVelocity"))
                    psoParam.setVmax(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("LowerInit"))
                    psoParam.setInitialLowerLimit(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("UpperInit"))
                    psoParam.setInitialUpperLimit(Double.parseDouble(strArr[1]));
            }
            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void ReadFromFile(File filePath)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String strLine;
            String[] strArr;
            while(br.ready())
            {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                if(strArr[0].equals("SwarmSize"))
                    psoParam.setSwarmSize(Integer.parseInt(strArr[1]));
                else if(strArr[0].equals("NeighborSize"))
                    psoParam.setNeighborSize(Integer.parseInt(strArr[1]));
                else if(strArr[0].equals("InertiaWeight"))
                    psoParam.setInertiaWeight(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("pBestCoff"))
                    psoParam.setRandomCoefficients1(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("LeaderCoff"))
                    psoParam.setRandomCoefficients2(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("MaxVelocity"))
                    psoParam.setVmax(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("LowerInit"))
                    psoParam.setInitialLowerLimit(Double.parseDouble(strArr[1]));
                else if(strArr[0].equals("UpperInit"))
                    psoParam.setInitialUpperLimit(Double.parseDouble(strArr[1]));
            }
            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public PSOParameters getPsoParam()
    {
        return psoParam;
    }
}
