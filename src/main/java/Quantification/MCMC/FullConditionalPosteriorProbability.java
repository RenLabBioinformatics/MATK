package Quantification.MCMC;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Tong on 2018/1/22.
 */
public class FullConditionalPosteriorProbability {
    private double N_IP;
    private double N_Input;
    private LinkedList<Double> XB;
    private LinkedList<Double> XP;
    private LinkedList<Double> YB;
    private LinkedList<Double> YP;
    private int a = 2;
    private int c = 2;
    private int d = 2;


    public FullConditionalPosteriorProbability(double N_IP, double N_Input, LinkedList<Double> XB, LinkedList<Double> XP, LinkedList<Double> YB, LinkedList<Double> YP){
        this.N_Input = N_Input;
        this.N_IP = N_IP;
        this.XB = XB;
        this.XP = XP;
        this.YB = YB;
        this.YP = YP;
    }

    public double P1_ProbabilityCaculate(double p1, double p3){
        double p1XB = 0;
        double p1XP = 0;
        double LnP_p1;
        double log_p1_p3 = Math.log(p1 + p3);
        double log_p1 = Math.log(p1);
        double N_IP_multiply_p1 = (-N_IP)* p1;
        for(Iterator iter = XB.iterator(); iter.hasNext();){
            double xb = (Double)iter.next();
            p1XB = p1XB + N_IP_multiply_p1 + xb * log_p1;
        }

        for (Iterator iter = XP.iterator(); iter.hasNext();){
            double xp = (Double)iter.next();
            p1XP = p1XP + N_IP_multiply_p1 + xp * log_p1_p3;
        }
        LnP_p1 = p1XB + p1XP + (a-1) * log_p1 + (a-1) * Math.log(1-p1);
        return LnP_p1;
    }

    public double P2_ProbabilityCaculate(double p2){
        double p2YB = 0;
        double p2YP = 0;
        double LnP_p2;
        double log_p2 = Math.log(p2);
        double N_Input_multiply_p2 = (-N_Input)*p2;
        for (Iterator iter = YB.iterator(); iter.hasNext();){
            double yb = (Double)iter.next();
            p2YB = p2YB + N_Input_multiply_p2 + yb * log_p2;
        }
        for(Iterator iter = YP.iterator(); iter.hasNext();){
            double yp = (Double)iter.next();
            p2YP = p2YP + N_Input_multiply_p2 + yp * log_p2;
        }
        LnP_p2 = p2YB + p2YP + (a-1) * log_p2 + (a-1) * Math.log(1-p2);
        return LnP_p2;
    }

    public double P3_ProbabilityCaculate(double p3, double p1){
        double p3XP = 0;
        double log_p1_p3 = Math.log(p1 + p3);
        double N_IP_multiply_p3 = (-N_IP)*p3;
        for(Iterator iter = XP.iterator(); iter.hasNext();){
            double xp = (Double)iter.next();
            p3XP = p3XP + N_IP_multiply_p3 + xp * log_p1_p3;
        }
        double LnP_p3 = p3XP + (c-1) * Math.log(p3) + (d-1) * Math.log(1-p3);
        return LnP_p3;
    }

    public double PM_ProbabilityCaculate(double PM, double p1){
        double pmXP = 0;
        double log1_PM = Math.log(1-PM);
        double N_IP_PM_p1 = (-N_IP * PM * p1);
        for (Iterator iter = XP.iterator(); iter.hasNext();){
            double xp = (Double)iter.next();
            pmXP = pmXP + N_IP_PM_p1/(1-PM) - xp * log1_PM;
        }
        double LnP_pm = pmXP + (c-1) * Math.log(PM) - (c-1) * log1_PM + (d-1) * Math.log(1- PM - p1* PM) - (d-1) * log1_PM;
        return LnP_pm;
    }

}
