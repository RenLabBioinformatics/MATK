package Quantification.ExtendedGTF;

import Basic.GTF.ChromosomeRecord;

import java.util.LinkedList;

/**
 * Created by Tong on 2018/10/19.
 */
public class QuantifyChromosomeRecord extends ChromosomeRecord {
    private LinkedList<QuantifyGeneRecord> geneList = new LinkedList();

    public void AddQuantifyGene(QuantifyGeneRecord geneRec)
    {
        geneList.add(geneRec);
    }
}
