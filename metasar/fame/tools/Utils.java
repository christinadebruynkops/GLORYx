package fame.tools;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import java.lang.reflect.Array;

/**
 * Simple utility class with some helper functions.
 *
 * Created by sicho on 10/6/16.
 */
public class Utils {

    public static <Ar> int[] matrixShape(Ar ar_x, Ar ar_y) {
        int x = Array.getLength(ar_x);
        int y = Array.getLength(ar_y);
        int[] ret = new int[2];
        ret[0] = x;
        ret[1] = y;
        return ret;
    }

    public static void printMatrix(int[][] ar, int zfill) {
        int x_dim = Array.getLength(ar);
        int y_dim = Array.getLength(ar[0]);
        for (int x = 0; x < x_dim;x++) {
            for (int y = 0; y < y_dim;y++) {
                String number = Integer.toString(ar[x][y]);
                if (number.length() < zfill) {
                    while (number.length() < zfill) {
                        number = "0" + number;
                    }
                }
                System.out.print(number);
                System.out.print(",");
            }
            System.out.println();
        }
    }

    public static boolean metchesSMARTS(IAtomContainer mol, String smarts) throws Exception {
        SMARTSQueryTool querytool = new SMARTSQueryTool(smarts);
        return querytool.matches(mol);
    }

    public static void syncOut(String out) {
        synchronized (System.out) {
            System.out.println(out);
        }
    }

    public static void  syncErr(String out) {
        synchronized (System.out) {
            System.err.println(out);
        }
    }
}
