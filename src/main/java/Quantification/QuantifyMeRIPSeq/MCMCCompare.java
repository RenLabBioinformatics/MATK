package Quantification.QuantifyMeRIPSeq;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Tong on 2018/6/19.
 */
public class MCMCCompare {
    private LinkedList<Double> Control_MCMCList;
    private LinkedList<Double> Treat_MCMCList;

    public void setControl_MCMCList(LinkedList<Double> control_mcmcList){
        this.Control_MCMCList = control_mcmcList;
    }

    public void setTreat_MCMCList(LinkedList<Double> treat_MCMCList){
        this.Treat_MCMCList = treat_MCMCList;
    }

    public double ComparePValue(){
        double count_left = 0;
        double count_rigtht = 0;
        double size = Control_MCMCList.size()*Treat_MCMCList.size();
//        System.out.println("error");
        for(Iterator<Double> iterator_c = Control_MCMCList.iterator(); iterator_c.hasNext();){
            double control_value = iterator_c.next();
            for(Iterator<Double> iterator_t = Treat_MCMCList.iterator(); iterator_t.hasNext();){
                double treat_value = iterator_t.next();
                double distance = control_value /treat_value;
//                System.out.println(distance);
                if(distance >1){
                    count_rigtht ++;
                }else if(distance <1){
                    count_left ++;
                }
            }
        }
        double proportion_left = count_left/size;
        double proportion_right = count_rigtht/size;
//        System.out.println(proportion_left + "\t" + proportion_right);
        return (Math.min(proportion_left,proportion_right))*2;
    }
}
