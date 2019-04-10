/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic.GTF;

import java.util.LinkedList;

/**
 *
 * @author Ben
 */
public class ChromosomeRecord {
    private String chromosomeName;
    private LinkedList<GeneRecord> geneList = new LinkedList();

    public LinkedList<GeneRecord> getGeneList() {
        return geneList;
    }

    public String getChromosomeName() {
        return chromosomeName;
    }

    public void setChromosomeName(String chromosomeName) {
        this.chromosomeName = chromosomeName;
    }
    
    public void AddGene(GeneRecord geneRec)
    {
        geneList.add(geneRec);
    }
}
