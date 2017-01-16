package utils;

import globals.Globals;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

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

    public static boolean matchesSMARTS(IAtomContainer mol, String smarts) throws Exception {
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
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
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

    /**
     * Finds a protonated carboxyl group and marks the oxygens using Sybyl notation.
     *
     * @param mol
     * @throws CDKException
     */
    public  static void fixSybylCarboxyl(IAtomContainer mol) throws CDKException {
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
        SMARTSQueryTool querytool = new SMARTSQueryTool("[OX1]=[CX3]-[OX2][H]");
        boolean status = querytool.matches(mol);

        if (status) {
            List<List<Integer>> matches = querytool.getMatchingAtoms();
            for (int match_idx = 0; match_idx < matches.size(); match_idx++) {
                for (Integer atom_idx : matches.get(match_idx)) {
                    IAtom iAtom = mol.getAtom(atom_idx);

                    if (iAtom.getSymbol().equals("O") && iAtom.getHybridization().toString().equals("SP3")) {
                        iAtom.setProperty("SybylAtomType", "O.co3");
                    }
                    if (iAtom.getSymbol().equals("O") && iAtom.getHybridization().toString().equals("SP2")) {
                        iAtom.setProperty("SybylAtomType", "O.co2");
                    }
                }
            }
        }
    }

    public static Map<Integer,Set<Integer>> generateSymmetryMap(IAtomContainer iMolecule) throws Exception {
        // compute symmetry numbers for the molecule
        int[] symmetryNumbersArray;
        try {
            EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner(iMolecule);
            symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu(iMolecule);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            throw new Exception("memory error");
        }

        // generate a mapping of symmetry numbers to atom numbers
        Map<Integer,Set<Integer>> symmetry_map = new HashMap<>();
        for (int i=0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);
            int symmetry_number = symmetryNumbersArray[i+1];
            iAtom.setProperty("SymmetryAtomNumber", symmetry_number);
            iMolecule.setAtom(i, iAtom);

            int atom_number = iMolecule.getAtomNumber(iAtom) + 1;
            if (symmetry_map.containsKey(symmetry_number)) {
                Set<Integer> atom_ids = symmetry_map.get(symmetry_number);
                atom_ids.add(atom_number);
                symmetry_map.put(symmetry_number, atom_ids);
            } else {
                Set<Integer> atom_ids = new HashSet<>();
                atom_ids.add(atom_number);
                symmetry_map.put(symmetry_number, atom_ids);
            }
        }

        return symmetry_map;
    }

    /**
     * Executes a given command in the shell.
     *
     * @param command the command to be executed
     * @return output of the command FIXME: this part does not really work, I think...
     */
    public static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    public static void writeAtomData(IMolecule mol, List<String> properties, String suffix, boolean null_as_zero) throws Exception {
        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(Globals.DESCRIPTORS_OUT + mol.getProperty(Globals.ID_PROP).toString() + suffix + ".csv")));
        outfile.println(String.join(",", properties));
        for (IAtom atom : mol.atoms()) {
            if (atom.getSymbol().equals("H")) {
                continue;
            }
            String result = "";
            for (String prop_name : properties) {
                Object prop = atom.getProperty(prop_name);
                if (prop == null && null_as_zero) {
                    prop = 0;
                }
                result += prop + ",";
            }
            rstrip(result, ",");
            outfile.println(result);
        }
        outfile.close();
    }

    public static String strip(String string, String pattern) {
        string = lstrip(string, pattern);
        string = rstrip(string, pattern);
        return string;
    }

    public static String lstrip(String string, String pattern) {
        string = string.replaceAll("^" + pattern, "");
        return string;
    }

    public static String rstrip(String string, String pattern) {
        string = string.replaceAll(pattern + "$", "");
        return string;
    }

    public static void printAtomProps(IAtomContainer mol) {
        for (IAtom atm : mol.atoms()) {
            System.out.println(atm.getProperties().toString());
        }
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
