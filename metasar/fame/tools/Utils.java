package fame.tools;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Looks for protonated carboxyls in a molecule and deprotonates all of them.
     *
     * @param mol
     * @throws CDKException
     */
    public  static void deprotonateCarboxyls(IAtomContainer mol) throws CDKException {
        SMARTSQueryTool querytool = new SMARTSQueryTool("[OX1]=[CX3]-[OX2][H]");
        boolean status = querytool.matches(mol);
        List<IAtom> to_remove = new ArrayList<>();
        if (status) {
            List<List<Integer>> matches = querytool.getMatchingAtoms();
            for (int match_idx = 0; match_idx < matches.size(); match_idx++) {
                for (Integer atom_idx : matches.get(match_idx)) {
                    IAtom iAtom = mol.getAtom(atom_idx);

//                    System.out.println(iAtom.getSymbol());
                    if (iAtom.getSymbol().equals("H")) {
                        to_remove.add(iAtom);
                    }
                }
            }
        }

        for (IAtom deleted : to_remove) {
            mol.removeAtomAndConnectedElectronContainers(deleted);
        }

        querytool = new SMARTSQueryTool("[OX1]=[CX3]-[OX1]");
        status = querytool.matches(mol);

        if (status) {
            List<List<Integer>> matches = querytool.getMatchingAtoms();
            for (int match_idx = 0; match_idx < matches.size(); match_idx++) {
                for (Integer atom_idx : matches.get(match_idx)) {
                    IAtom iAtom = mol.getAtom(atom_idx);

//                    System.out.println(iAtom.getSymbol());
                    if (iAtom.getSymbol().equals("O")) {
                        if (iAtom.getHybridization().toString().equals("SP3")) {
                            iAtom.setImplicitHydrogenCount(0);
                            iAtom.setFormalCharge(-1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Looks for deprotonated carboxyls in a molecule and protonates all of them.
     *
     * @param mol
     * @throws CDKException
     */
    public  static void protonateCarboxyls(IAtomContainer mol) throws CDKException {
        SMARTSQueryTool querytool = new SMARTSQueryTool("[OX1]=[CX3]-[O-]");
        boolean status = querytool.matches(mol);
        List<IAtom> to_remove = new ArrayList<>();

        if (status) {
            List<List<Integer>> matches = querytool.getMatchingAtoms();
            for (int match_idx = 0; match_idx < matches.size(); match_idx++) {
                for (Integer atom_idx : matches.get(match_idx)) {
                    IAtom iAtom = mol.getAtom(atom_idx);

//                    System.out.println(iAtom.getSymbol());
                    if (iAtom.getSymbol().equals("O")) {
                        if (iAtom.getHybridization().toString().equals("SP3")) {
                            iAtom.setFormalCharge(0);
                            iAtom.setImplicitHydrogenCount(1);
                        }
                    }
                }
            }
        }
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
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
