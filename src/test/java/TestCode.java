/**
 * Created by Ben on 2018/5/7.
 */
public class TestCode {
    public static void main(String[] args)
    {
        int up = 30;
        int down = 30;
        String motif = "[A-Z]{" + up + "}[AGT][GA]AC[ACT][A-Z]{" + down + "}";
        System.out.println(motif);
    }
}
