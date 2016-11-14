package fame.descriptors;

import fame.tools.Globals;
import fame.tools.SoMInfo;
import fame.tools.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

/**
 * Can read an SD file and 'sanitize' the structures.
 *
 * The sanitization involves multiple steps:
 *
 *  1. Read the input SD file.
 *  2. Parse the SoM information in the file and attach it to the respective molecule and its atoms.
 *  3. Remove all explicit hydrogens from the molecules and adjust the SoM information accordingly.
 *  4. Write the sanitized molecules into a new file and use Open Babel to add hydrogens (at the end of the SD file) and charge the molecules for pH = 7.
 *  5. Pass the path to the Open Babel output file back to the caller.
 *
 *  See the sanitize() method for details
 *
 * Created by sicho on 10/19/16.
 */
public class Sanitize {

    private static String addEntry(String line, String entry) {
        line += "'" + entry + "', ";
        return line;
    }

    /**
     * Reads in the SoM information from the input molecule, removes explicit hydrogens
     * and updates the associated SoM information and molecule instance accordingly.
     *
     * @param molecule
     * @param sd_writer
     * @throws Exception
     */
    private static void sanitizeMolecule(IMolecule molecule) throws Exception {
        Map<Integer, List<SoMInfo>> som_map = SoMInfo.parseInfoAndUpdateMol(molecule);

        // find hydrogens and mark them for removal
        List<IAtom> to_remove = new ArrayList<>();
        for (IAtom atm : molecule.atoms()) {
            if (atm.getSymbol().equals("H")) {
                to_remove.add(atm);
            }
        }

        // update the SoM information
        Set<Integer> som_ids = som_map.keySet(); // get the ids of atoms that are marked as SoM
        for (IAtom removed : to_remove) { // iterate over the hydrogens marked for removal
            Map<Integer, List<SoMInfo>> som_map_new = new HashMap<>(); // the updated map with new indices
            int removed_id = molecule.getAtomNumber(removed) + 1; // id of the hydrogen currently being removed
            for (Integer som_id : som_ids) { // iterate over SoM information for all atoms
                if (removed_id < som_id) { // if SoM information exists for an atom that lies before the currently removed hydrogen in the SD file, ...
                    for (SoMInfo som_info : som_map.get(som_id)) {
                        som_info.setAtomID(som_id - 1); // ...decrease the atom index for all such SoM infos
                    }
                    som_map_new.put(som_id - 1, som_map.get(som_id)); // update the new map with the modified information
                } else {
                    som_map_new.put(som_id, som_map.get(som_id)); // if the information is for an atom after this hydrogen, just copy the old information into the new map
                }
            }
            som_map.clear(); // clear the old map
            som_map.putAll(som_map_new); // put the modified data in the old map
            som_ids = som_map.keySet(); // get the new ids for atoms with SoM information attached
            molecule.removeAtomAndConnectedElectronContainers(removed); // actually remove the currently processed hydrogen (this updates the CDK representation and makes the new indices valid within the molecule instance)
        }

        // update the information in the molecule properties (this information is written into the SD file)
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
    }

    /**
     * Reads every molecule from the supplied SD file and creates
     * a CDK representation which is used in the sanitization (see sanitizeMolecule()).
     *
     * When all molecules are sanitized and written into an output SDF,
     * it calls Open Babel to charge them and add hydrogens to them.
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
//                sanitizeMolecule(molecule, sd_writer);
//                break;
//            }
			System.out.println("Sanitizing " + molecule.getProperty(Globals.ID_PROP));
            try {
                // do the actual sanitization
                sanitizeMolecule(molecule);

                // write the changed molecule to a new SD file
                sd_writer.write(molecule);
            } catch (Exception exp) {
                System.err.println("Error: sanitization failed for molecule: " + molecule.getProperty(Globals.ID_PROP));
                System.err.println(exp.getMessage());
                exp.printStackTrace();
            }
            counter++;
        }
        sd_writer.close();

        // use open babel to ionize the structures
        String command = "babel -p 7 -isdf " + babel_in_file + " -osdf " + babel_out_file;
        System.out.printf(Utils.executeCommand(command));

        return babel_out_file;
    }

    public static void main(String[] args) throws Exception {
        sanitize(args[0]);
    }
}
