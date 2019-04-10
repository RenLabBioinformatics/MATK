package PeakCalling.Threshold.GPD;

import PeakCalling.FilterPeaks;
import PeakCalling.PeakDistribution.DataPoint;
import PeakCalling.VAEPeakCalling;
import org.apache.commons.math3.random.EmpiricalDistribution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/*
*Calculate the empirical and estimated cumulative probability for the MeRIP-seq data
* */
public class CumulativeProbability {
    private double[] X;
    private double gamma;
    private double sigma;
    private LinkedList<DataPoint> empiricalCDF;
    private LinkedList<DataPoint> estimatedCDF;

    public CumulativeProbability(double[] X, double gamma, double sigma) {
        this.X = X;
        this.gamma = gamma;
        this.sigma = sigma;
    }

    private double CalEmpiricalCumulativeP(double x) {
        double count = 0;
        for(int i=0; i<X.length; i++) {
            if(X[i] <= x)
                count++;
            else
                break;
        }
        return count/((double) X.length);
    }

    public void CDF(int pointSize) {
        int lowerBound = (int)(X.length * 0.01);
        int upperBound = (int)(X.length * 0.99);
        double minVal = X[lowerBound];
        double maxVal = X[upperBound];
        double step = (maxVal - minVal)/((double)pointSize);
        //Calculate  cdf
        empiricalCDF = new LinkedList<>();
        estimatedCDF = new LinkedList<>();
        GPDFunction gpdFunction = new GPDFunction(gamma, sigma);
        for(double val = minVal; val <= maxVal; val = val + step) {
            DataPoint empiricalDataPoint = new DataPoint();
            empiricalDataPoint.setX(val);
            empiricalDataPoint.setY(CalEmpiricalCumulativeP(val));
            empiricalCDF.add(empiricalDataPoint);

            DataPoint estimatedDataPoint = new DataPoint();
            estimatedDataPoint.setX(val);
            estimatedDataPoint.setY(gpdFunction.cdf(val));
            estimatedCDF.add(estimatedDataPoint);
        }
    }

    public void SaveCDF(File empiricalFile, File estimatedFile) {
        try {
            FileWriter fwEmp = new FileWriter(empiricalFile);
            FileWriter fwEst = new FileWriter(estimatedFile);
            //Write empirical CDF
            fwEmp.write("X\tY\n");
            for(DataPoint dataPoint: empiricalCDF) {
                fwEmp.write(dataPoint.getX() + "\t" + dataPoint.getY() + "\n");
            }
            //Write estimated CDF
            fwEst.write("X\tY\n");
            for(DataPoint dataPoint: estimatedCDF) {
                fwEst.write(dataPoint.getX() + "\t" + dataPoint.getY() + "\n");
            }
            //Close file
            fwEmp.close();
            fwEst.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        VAEPeakCalling vaePeakCalling = new VAEPeakCalling(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\SRR2120870_SRR2120874.csv"));
        vaePeakCalling.Train(500,10);
        vaePeakCalling.Predict(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\SRR2120870_SRR2120874.loc"), 500);
        FilterPeaks filterPeaks = new FilterPeaks(vaePeakCalling.getBinResMap(), "/Param.txt", 0.05);
        filterPeaks.Filtering();
        filterPeaks.SaveCDF(new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\GPD_CDF\\SRR2120870_SRR2120874_Empirical.txt"),
                new File("E:\\工作文档\\MATK\\PeakCalling\\Liver\\GPD_CDF\\SRR2120870_SRR2120874_GPD.txt"));
    }
}
