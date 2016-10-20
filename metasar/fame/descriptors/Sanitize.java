package fame.descriptors;

import fame.tools.Globals;
import fame.tools.SoMInfo;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A script that reads in the whole database and sanitizes the entries.
 *
 * Created by sicho on 10/19/16.
 */
public class Sanitize {

    private static String addEntry(String line, String entry) {
        line += "'" + entry + "', ";
        return line;
    }

    /**
     * Removes explicit hydrogens from a molecule and updates the associated SoM
     * information accordingly.
     *
     * @param molecule
     * @param sd_writer
     * @throws Exception
     */
    private static void writeSanitizedData(IMolecule molecule, SDFWriter sd_writer) throws Exception {
        Map<Integer, List<SoMInfo>> som_map;
        try {
            som_map = SoMInfo.parseInfoAndUpdateMol(molecule);
        } catch (SoMInfo.NoSoMAnnotationException exp) {
            exp.printStackTrace();
            return;
        }

        // find spurious hydrogens that need to be removed
        List<IAtom> to_remove = new ArrayList<>();
        for (IAtom atm : molecule.atoms()) {
            if (atm.getSymbol().equals("H")) {
                to_remove.add(atm);
            }
        }

        // update the SoM information accordingly
        Set<Integer> som_ids = som_map.keySet();
        for (IAtom removed : to_remove) {
            Map<Integer, List<SoMInfo>> som_map_new = new HashMap<>();
            int removed_id = molecule.getAtomNumber(removed) + 1;
            for (Integer som_id : som_ids) {
                if (removed_id < som_id) {
                    for (SoMInfo som_info : som_map.get(som_id)) {
                        som_info.setAtomID(som_id - 1);
                    }
                    som_map_new.put(som_id - 1, som_map.get(som_id));
                } else {
                    som_map_new.put(som_id, som_map.get(som_id));
                }
            }
            som_map.clear();
            som_map.putAll(som_map_new);
            som_ids = som_map.keySet();
            molecule.removeAtomAndConnectedElectronContainers(removed);
        }

        // update the annotations
        String atom_ids = "";
        String reasubclass_ids = "";
        String reaclass_ids = "";
        String reamain_ids = "";
        String reagen_ids = "";
        for (List<SoMInfo> infos : som_map.values()) {
            for (SoMInfo info : infos) {
                String atom_id = Integer.toString(info.getAtomID());
                if (!info.isConfirmed()) {
                    atom_id += "?";
                }
                String reasubclass_id = Integer.toString(info.getReasubclass());
                String reaclass_id = Integer.toString(info.getReaclass());
                String reamain_id = Integer.toString(info.getReamain());
                String reagen_id = Integer.toString(info.getReagen());

                atom_ids = addEntry(atom_ids, atom_id);
                reasubclass_ids = addEntry(reasubclass_ids, reasubclass_id);
                reaclass_ids = addEntry(reaclass_ids, reaclass_id);
                reamain_ids = addEntry(reamain_ids, reamain_id);
                reagen_ids = addEntry(reagen_ids, reagen_id);
            }
        }
        String end_comma = ",\\s*$";
        molecule.setProperty(Globals.SOM_PROP, atom_ids.replaceAll(end_comma, ""));
        molecule.setProperty(Globals.REASUBCLS_PROP, reasubclass_ids.replaceAll(end_comma, ""));
        molecule.setProperty(Globals.REACLS_PROP, reaclass_ids.replaceAll(end_comma, ""));
        molecule.setProperty(Globals.REAMAIN_PROP, reamain_ids.replaceAll(end_comma, ""));
        molecule.setProperty(Globals.REAGEN_PROP, reagen_ids.replaceAll(end_comma, ""));
        molecule.removeProperty("cdk:Title");
        molecule.setProperty(Globals.NAME_OTHER_PROP, molecule.getProperty(Globals.NAME_PROP));
        molecule.setProperty(Globals.NAME_PROP, molecule.getProperty(Globals.ID_PROP));

        // write the changed molecule to a new SD file
        sd_writer.write(molecule);
    }

    private static String executeCommand(String command) {

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

    /**
     * Reads every molecule from the supplied SD file and attempts to create
     * a sensible representation by removing explicit hydrogens and
     * ionizing the structures using open babel.
     *
     * @param original_file
     * @return
     * @throws Exception
     */
    public static String sanitize(String original_file) throws Exception {
        DefaultIteratingChemObjectReader reader = new IteratingMDLReader(new FileInputStream(original_file), DefaultChemObjectBuilder.getInstance());

        String babel_in_file = original_file.replaceAll("\\.sdf$", "") + "_babel_in.sdf";
        String babel_out_file = original_file.replaceAll("\\.sdf$", "") + "_babel_out.sdf";

        FileWriter writer = new FileWriter(babel_in_file);
        SDFWriter sd_writer = new SDFWriter(writer);

        // read the molecules from the original file and sanitize them
        int counter = 0;
        while (reader.hasNext() && (counter < Globals.LOAD_MAX_MOL || Globals.LOAD_MAX_MOL == -1)) {
            Molecule molecule = (Molecule)reader.next();
//			if (Utils.matchesSMARTS(molecule, "[NX3]([O])[O]")) {
//            if (molecule.getProperty(Globals.ID_PROP).equals("676") || molecule.getProperty(Globals.ID_PROP).equals("1270")) {
//                System.out.println("Sanitizing " + molecule.getProperty(Globals.ID_PROP));
//                writeSanitizedData(molecule, sd_writer);
//                break;
//            }
			System.out.println("Sanitizing " + molecule.getProperty(Globals.ID_PROP));
            try {
                writeSanitizedData(molecule, sd_writer);
            } catch (Exception exp) {
                System.err.println("Error: sanitization failed for molecule: " + molecule.getProperty(Globals.ID_PROP));
                exp.printStackTrace();
            }
            counter++;
        }
        sd_writer.close();

        // use open babel to ionize the structures
        String command = "babel -p 7 -isdf " + babel_in_file + " -osdf " + babel_out_file;
        System.out.printf(executeCommand(command));

        return babel_out_file;
    }

    public static void main(String[] args) throws Exception {
        sanitize(args[0]);
    }
}
