package Quantification.MCMC;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Tong on 2018/1/22.
 */
public class MCMCIteration {
    private FullConditionalPosteriorProbability probability;

    public void SetCondition(double N_IP, double N_Input, LinkedList<Double> XB, LinkedList<Double> XP, LinkedList<Double> YB, LinkedList<Double> YP){
        this.probability = new FullConditionalPosteriorProbability(N_IP,N_Input, XB, XP, YB, YP);
    }

    public void SetCondition(double N_IP, double N_Input, LinkedList<Double> XB, LinkedList<Double> XP, LinkedList<Double> YB, LinkedList<Double> YP, FileWriter vectorfw){
        this.probability = new FullConditionalPosteriorProbability(N_IP,N_Input, XB, XP, YB, YP);
        try {
            vectorfw.write("in condition\n");
            vectorfw.write("XP:\t");
            for (Iterator<Double> iterator = XP.iterator(); iterator.hasNext(); ) {
                vectorfw.write(iterator.next() + "\t");
            }
            vectorfw.write("\nYP:\t");
            for (Iterator<Double> iterator = YP.iterator(); iterator.hasNext(); ) {
                vectorfw.write(iterator.next() + "\t");
            }
            vectorfw.write("\nXB:\t");
            for (Iterator<Double> iterator = XB.iterator(); iterator.hasNext(); ) {
                vectorfw.write(iterator.next() + "\t");
            }
            vectorfw.write("\nYB:\t");
            for (Iterator<Double> iterator = YB.iterator(); iterator.hasNext(); ) {
                vectorfw.write(iterator.next() + "\t");
            }
            vectorfw.write("\n" + N_IP + "\t" + N_Input + "\n");
        }catch (IOException ex){

        }
    }

    public static double ExpectedDistributionTest(Double ln_thetacand, Double ln_thetat){
        double theta = (ln_thetacand-ln_thetat);
        double alpha;
        if(Math.exp(theta) > 1){
            alpha = 1;
        }else{
            alpha = (Math.exp(theta));
        }
        return alpha;
    }

    public LinkedList SamplingIteration(double p1, double p2, double p3, double PM, int time, int burn_in){
        LinkedList<Double> result = new LinkedList<>();
        NormalDistribution nordi = new NormalDistribution(0, 0.001);
        UniformRealDistribution unifromdi = new UniformRealDistribution(0,1);
        LinkedList<Double> PMList = new LinkedList<Double>();
        LinkedList<Double> P1List = new LinkedList<>();
        LinkedList<Double> P3List = new LinkedList<>();
        double PM_mean = 0;
        double P1_mean = 0;
        double P3_mean = 0;
        for(int i=0; i<time; i++){
            double p1_candidate = Math.abs(p1 + nordi.sample());
            double p2_candidate = Math.abs(p2 + nordi.sample());
            double p3_candidate = Math.abs(p3 + nordi.sample());
            double PM_candidate = Math.abs(PM + nordi.sample());
            double alpha_p1 = MCMCIteration.ExpectedDistributionTest(this.probability.P1_ProbabilityCaculate(p1_candidate,p3),this.probability.P1_ProbabilityCaculate(p1,p3));
            double alpha_p2 = MCMCIteration.ExpectedDistributionTest(this.probability.P2_ProbabilityCaculate(p2_candidate),this.probability.P2_ProbabilityCaculate(p2));
            double alpha_p3 = MCMCIteration.ExpectedDistributionTest(this.probability.P3_ProbabilityCaculate(p3_candidate,p1),this.probability.P3_ProbabilityCaculate(p3,p1));
            double alpha_PM = MCMCIteration.ExpectedDistributionTest(this.probability.PM_ProbabilityCaculate(PM_candidate,p1),this.probability.PM_ProbabilityCaculate(PM,p1));
            double u = unifromdi.sample();
            if(alpha_p1 >= u){
                p1 = p1_candidate;
            }
            if(alpha_p2 >= u){
                p2 = p2_candidate;
            }
            if(alpha_p3 >= u){
                p3 = p3_candidate;
            }
            if(alpha_PM >= u){
                PM = PM_candidate;
            }
            if(i>burn_in){
                PMList.add(PM);
                P1List.add(p1);
                P3List.add(p3);
                PM_mean = PM_mean + PM;
                P1_mean = P1_mean + p1;
                P3_mean = P3_mean + p3;
            }

        }
        result.add(PM_mean/PMList.size());
        result.add(P1_mean/P1List.size());
        result.add(P3_mean/P3List.size());
        P1List.clear();
        P3List.clear();
        return PMList;
    }

    public double SamplingIteration(double p1, double p2, double p3, double PM, int time, int burn_in, double variance, LinkedList<Double> PMList){
        LinkedList<Double> result = new LinkedList<>();
        NormalDistribution nordi = new NormalDistribution(0, variance);
        UniformRealDistribution unifromdi = new UniformRealDistribution(0,1);
//        LinkedList<Double> PMList = new LinkedList<Double>();
        LinkedList<Double> P1List = new LinkedList<>();
        LinkedList<Double> P3List = new LinkedList<>();
        double PM_mean = 0;
        double P1_mean = 0;
        double P3_mean = 0;
        double accept_p1 = 0;
        double accept_p2 = 0;
        double accept_p3 = 0;
        double accept_pm = 0;
        for(int i=0; i<time; i++){
            double p1_candidate = Math.abs(p1 + nordi.sample());
            double p2_candidate = Math.abs(p2 + nordi.sample());
            double p3_candidate = Math.abs(p3 + nordi.sample());
            double PM_candidate = Math.abs(PM + nordi.sample());
            double alpha_p1 = MCMCIteration.ExpectedDistributionTest(this.probability.P1_ProbabilityCaculate(p1_candidate,p3),this.probability.P1_ProbabilityCaculate(p1,p3));
            double alpha_p2 = MCMCIteration.ExpectedDistributionTest(this.probability.P2_ProbabilityCaculate(p2_candidate),this.probability.P2_ProbabilityCaculate(p2));
            double alpha_p3 = MCMCIteration.ExpectedDistributionTest(this.probability.P3_ProbabilityCaculate(p3_candidate,p1),this.probability.P3_ProbabilityCaculate(p3,p1));
            double alpha_PM = MCMCIteration.ExpectedDistributionTest(this.probability.PM_ProbabilityCaculate(PM_candidate,p1),this.probability.PM_ProbabilityCaculate(PM,p1));
            double u = unifromdi.sample();
            if(alpha_p1 >= u){
                p1 = p1_candidate;
                accept_p1++;
            }
            if(alpha_p2 >= u){
                p2 = p2_candidate;
                accept_p2++;
            }
            if(alpha_p3 >= u){
                p3 = p3_candidate;
                accept_p3++;
            }
            if(alpha_PM >= u){
                PM = PM_candidate;
                accept_pm++;
            }
            if(i>burn_in){
                PMList.add(PM);
                P1List.add(p1);
                P3List.add(p3);
                PM_mean = PM_mean + PM;
                P1_mean = P1_mean + p1;
                P3_mean = P3_mean + p3;
            }
        }
        result.add(PM_mean/PMList.size());
        result.add(P1_mean/P1List.size());
        result.add(P3_mean/P3List.size());
//        double random_coefficient = (1.1+Math.random()*10);
//        System.out.println((accept_p1/time) + "\t" + (accept_p2/time) + "\t" + (accept_p3/time) + "\t" + (accept_pm/time));
        return (accept_p1/time);
    }

//    public LinkedList DuplicationSamplingIteration(double p1, double p2, double p3, double PM, int time, int burn_in){
//        LinkedList<Double> result = new LinkedList<>();
//        NormalDistribution nordi = new NormalDistribution(0, 0.05);
//        UniformRealDistribution unifromdi = new UniformRealDistribution(0,1);
//        LinkedList<Double> PMList = new LinkedList<Double>();
//        LinkedList<Double> P1List = new LinkedList<>();
//        LinkedList<Double> P3List = new LinkedList<>();
//        double PM_mean = 0;
//        double P1_mean = 0;
//        double P3_mean = 0;
//        for(int i=0; i<time; i++){
//            double p1_candidate = Math.abs(p1 + nordi.sample());
//            double p2_candidate = Math.abs(p2 + nordi.sample());
//            double p3_candidate = Math.abs(p3 + nordi.sample());
//            double PM_candidate = Math.abs(PM + nordi.sample());
//            double alpha_p1 = MCMCIteration.ExpectedDistributionTest(this.probability.P1_ProbabilityCaculate(p1_candidate,p3),this.probability.P1_ProbabilityCaculate(p1,p3));
//            double alpha_p2 = MCMCIteration.ExpectedDistributionTest(this.probability.P2_ProbabilityCaculate(p2_candidate),this.probability.P2_ProbabilityCaculate(p2));
//            double alpha_p3 = MCMCIteration.ExpectedDistributionTest(this.probability.P3_ProbabilityCaculate(p3_candidate,p1),this.probability.P3_ProbabilityCaculate(p3,p1));
//            double alpha_PM = MCMCIteration.ExpectedDistributionTest(this.probability.PM_ProbabilityCaculate(PM_candidate,p1),this.probability.PM_ProbabilityCaculate(PM,p1));
//            if(alpha_p1 > unifromdi.sample()){
//                p1 = p1_candidate;
//            }
//            if(alpha_p2 > unifromdi.sample()){
//                p2 = p2_candidate;
//            }
//            if(alpha_p3 > unifromdi.sample()){
//                p3 = p3_candidate;
//            }
//            if(alpha_PM > unifromdi.sample()){
//                PM = PM_candidate;
//            }
//            if(i>burn_in){
//                PMList.add(PM);
//                P1List.add(p1);
//                P3List.add(p3);
//                PM_mean = PM_mean + PM;
//                P1_mean = P1_mean + p1;
//                P3_mean = P3_mean + p3;
////                System.out.println(PM);
//            }
//
//        }
//        System.out.println("mean: " + PM_mean/PMList.size());
//        return PMList;
//    }

    public static LinkedList<Double> ReadsGenerate(double ExpectionProbabilty, int readscount){
        PoissonDistribution poissondi = new PoissonDistribution(ExpectionProbabilty);
        LinkedList<Double> ReadsList = new LinkedList<Double>();
        for(int i=0; i<readscount; i++){
            double reads = poissondi.sample();
            ReadsList.add(reads);
        }
        return ReadsList;
    }

    public LinkedList ModelTest(double supposed_N_IP, double supposed_N_Input, double supposed_p1, double supposed_p2, double supposed_p3){
        LinkedList<LinkedList<Double>> IPList = new LinkedList<>();
        LinkedList<Double> XB = MCMCIteration.ReadsGenerate(supposed_N_IP*supposed_p1,100);

        LinkedList<Double> XP = MCMCIteration.ReadsGenerate((supposed_N_IP * supposed_p1 + supposed_N_IP * supposed_p3),100);

        LinkedList<Double> YB = MCMCIteration.ReadsGenerate(supposed_N_Input * supposed_p2, 100);

        LinkedList<Double> YP = MCMCIteration.ReadsGenerate(supposed_N_Input * supposed_p2, 100);
        IPList.add(XP);
        IPList.add(XB);
        this.SetCondition(supposed_N_IP,supposed_N_Input,XB,XP,YB,YP);
        LinkedList<Double> resultList = this.SamplingIteration(0.5,0.5,0.5,0.3,2000,1000);

        return resultList;
    }

}
