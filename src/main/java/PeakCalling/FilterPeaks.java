package PeakCalling;

import PeakCalling.Threshold.GPD.CumulativeProbability;
import PeakCalling.Threshold.GPD.EstimationParameters;
import PeakCalling.Threshold.GPD.GPDFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Filter peaks from raw prediction result using GPD method
 * Created by Ben on 2018/4/23.
 */
public class FilterPeaks {
    private HashMap<String, LinkedList<BinResult>> binResMap, peakBinResMap;
    private EstimationParameters gpdEstimate;
    private final double accuracy = 10E-06;//Acc for PSO optimization
    private final int iteration = 350;//Maximal iteration step for PSO optimization

    private int N, Nt;
    private double t;
    private double q;

    private void RefineInitialThreshold(LinkedList<Double> callScoreList) {
        boolean isPass = false;
        for(double percentage=1 - q; percentage<=0.98; percentage=percentage+0.01) {
            t = callScoreList.get((int)(callScoreList.size()*percentage));
            if(t >= 0) {
                q = (1 - percentage) - 0.01;
                isPass = true;
                break;
            }
        }
        if(!isPass) {
            t = callScoreList.get((int)(callScoreList.size()*0.99));
            q = 0.005;
        }
    }

    public FilterPeaks(HashMap<String, LinkedList<BinResult>> binResMap, String psoParamFile, double q) {
        this.binResMap = binResMap;
        LinkedList<Double> callScoreList = new LinkedList<Double>();
        for(String chrName : binResMap.keySet()) {
            LinkedList<BinResult> binResList = binResMap.get(chrName);
            for(BinResult binRes : binResList) {
                callScoreList.add(binRes.getScore());
            }
        }
        N = callScoreList.size();
        Collections.sort(callScoreList);//Sorted the score. This is required by the GPD hybrid estimation method
        t = callScoreList.get((int)(callScoreList.size()*(1 - q - 0.01)));//Compute the initial threshold t
        this.q = q;
        if(t < 0)
            RefineInitialThreshold(callScoreList);
        //prepare data for GPD parameter estimation
        LinkedList<Double> gpdX = new LinkedList<Double>();//store the value used for GPD parameter estimation
        for(Double score : callScoreList) {
            double x = score - t;
            if(x > 0) {
                //only the score value that larger than the initial threshold (the extreme value in the right tail) are preserved for estimation
                gpdX.add(x);
            }
        }
        Nt = gpdX.size();
        //Construct GPD estimation method
        double[] X = new double[gpdX.size()];
        int i = 0;
        for(Double x : gpdX) {
            X[i] = x;
            i++;
        }
        gpdX.clear();callScoreList.clear();//Clean the memory
        gpdEstimate = new EstimationParameters(psoParamFile, X);
        //Do estimation
        System.out.println("Estimating GPD parameters, N = " + N + "; Nt = " + Nt + "; t = " + t);
        gpdEstimate.Optimize(accuracy, iteration);
        System.out.println("Parameters for GPD is gamma = " + gpdEstimate.getGamma() + "; sigma = " + gpdEstimate.getSigma());
    }

    public void Filtering() {
        //Calculate cutoff
        GPDFunction gpdFunction = new GPDFunction(gpdEstimate.getGamma(), gpdEstimate.getSigma());
        double cutoff = gpdFunction.QuantileCutoff(q, t, N, Nt);
        System.out.println("Calculate cutoff = " + cutoff + "; q = " + q);
        //Filter peaks
        peakBinResMap = new HashMap<String, LinkedList<BinResult>>();
        LinkedList<BinResult> peakBinResList;
        for(String chrName : binResMap.keySet()) {
            LinkedList<BinResult> binResList = binResMap.get(chrName);
            for(BinResult binRes : binResList) {
                if(binRes.getScore() > cutoff) {
                    double probability = ((double) Nt/ (double) N)*( 1 - gpdFunction.cdf(binRes.getScore()) );
                    BinResult peakBinRes = new BinResult(binRes.getChrName(), binRes.getIndex(), binRes.getScore(), probability);

                    if(peakBinResMap.containsKey(peakBinRes.getChrName())) {
                        peakBinResList = peakBinResMap.get(peakBinRes.getChrName());
                        peakBinResList.add(peakBinRes);
                    } else {
                        peakBinResList = new LinkedList<BinResult>();
                        peakBinResList.add(peakBinRes);
                        peakBinResMap.put(peakBinRes.getChrName(), peakBinResList);
                    }

                }
            }
        }
    }

    public HashMap<String, LinkedList<BinResult>> getPeakBinResMap() {
        return peakBinResMap;
    }

    public void SavePeakBin(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : peakBinResMap.keySet()) {
                LinkedList<BinResult> peakBinResList = peakBinResMap.get(chrName);
                for(BinResult peakBinRes : peakBinResList) {
                    fw.write(peakBinRes.getChrName() + "\t" + peakBinRes.getIndex()  + "\t" + peakBinRes.getScore() + "\t" + peakBinRes.getProbability() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveCDF(File empiricalCDFFile, File estimatedCDFFile) {
        CumulativeProbability cumulativeProbability = new CumulativeProbability(gpdEstimate.getX(), gpdEstimate.getGamma(), gpdEstimate.getSigma());
        cumulativeProbability.CDF(500);
        cumulativeProbability.SaveCDF(empiricalCDFFile, estimatedCDFFile);
    }
}
