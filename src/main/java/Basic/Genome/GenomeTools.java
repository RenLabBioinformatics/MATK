package Basic.Genome;

/**
 * Created by Ben on 2018/5/7.
 */
public class GenomeTools {
    public static int SenseStrand = 0;
    public static int AntiSenseStrand = 1;

    public static String reverseSeq(String seq)
    {
        StringBuilder retSeq = new StringBuilder("");
        for(int i=0; i<seq.length(); i++)
        {
            switch(seq.charAt(i))
            {
                case 'A':
                    retSeq.append("T");
                    break;
                case 'T':
                    retSeq.append("A");
                    break;
                case 'C':
                    retSeq.append("G");
                    break;
                case 'G':
                    retSeq.append("C");
                    break;
                default:
                    retSeq.append("N");
            }
        }
        retSeq = retSeq.reverse();
        return retSeq.toString();
    }

    public static char reverseNucleotide(char nucleotide)
    {
        char retVal;
        switch (nucleotide)
        {
            case 'A':
                retVal = 'T';
                break;
            case 'T':
                retVal = 'A';
                break;
            case 'C':
                retVal = 'G';
                break;
            case 'G':
                retVal = 'C';
                break;
            default:
                retVal = 'N';
        }
        return retVal;
    }

    public static String FormatStrand(int strand) {
        if(strand == GenomeTools.SenseStrand)
            return "+";
        else if(strand == GenomeTools.AntiSenseStrand)
            return "-";
        else
            return "+";
    }
}
