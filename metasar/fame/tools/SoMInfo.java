package fame.tools;

import org.openscience.cdk.exception.NoSuchAtomException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.*;

/**
 * Holds information about SoMs and the reaction classes for a molecule.
 *
 * Created by sicho on 10/17/16.
 */
public class SoMInfo {

    public static class InvalidSoMAnnotationException extends Exception {
        public InvalidSoMAnnotationException(String bad_string) {
            super("Invalid SoM annotation detected: " + bad_string);
        }
    }

    public static class NoSoMAnnotationException extends Exception {
        public NoSoMAnnotationException(IAtomContainer mol) {
            super("No SoM annotation detected for mol: " + mol.getProperty(Globals.ID_PROP));
        }
    }

    private IAtomContainer mol;
    private int atom_id;
    private boolean is_confirmed;
    private int reasubclass_id;
    private int reaclass_id;
    private int reamain_id;
    private int reagen_id;

    private SoMInfo(
            IAtomContainer mol
            , int atom_id
            , boolean is_confirmed
            , int reasubclass_id
            , int reaclass_id
            , int reamain_id
            , int reagen_id
    ) {
        this.mol = mol;
        this.atom_id = atom_id;
        this.is_confirmed = is_confirmed;
        this.reasubclass_id = reasubclass_id;
        this.reaclass_id = reaclass_id;
        this.reamain_id = reamain_id;
        this.reagen_id = reagen_id;
    }

    public int getAtomID() {
        return atom_id;
    }

    public void setAtomID(int val) {
        atom_id = val;
    }

    public IAtomContainer getMol() {
        return mol;
    }

    public boolean isConfirmed() {
        return is_confirmed;
    }

    public int getReasubclass() {
        return reasubclass_id;
    }

    public int getReaclass() {
        return reaclass_id;
    }

    public int getReamain() {
        return reamain_id;
    }

    public int getReagen() {
        return reagen_id;
    }

    /**
     * Helper method that reads a line which encodes SoM information in a given format.
     * Returns a list of values. Each item in the list represents information about one
     * reaction.
     *
     * @param line
     * @return
     * @throws InvalidSoMAnnotationException
     */
    public static List<String> parseValueList(String line) throws InvalidSoMAnnotationException {
        line = line.replaceAll("^'", "").replaceAll("'$", "");
        List<String> values = new ArrayList<>();
        for (String som : line.split("', '")) {
            if (!som.isEmpty()) {
                values.add(som);
            }
        }
        for (String val : values) {
            boolean empty_val = val.matches("None") || val.isEmpty();
            if (!(
                    val.matches("^(\\d+\\??,\\s*)*\\d+\\??$")
                    || empty_val
            )) {
                throw new InvalidSoMAnnotationException(val);
            }
        }
        return values;
    }

    /**
     * Add an entry to a line using a given delimiter. If the entry represents
     * unconfirmed information a question mark is also attached to the entry.
     *
     * @param line line to append to
     * @param entry entry to append
     * @param delim delimiter to use
     * @param is_confirmed confirmation status
     * @return
     */
    private static String addEntry(String line, String entry, String delim, boolean is_confirmed) {
        if (is_confirmed) {
            line += entry + delim;
        } else {
            line += entry + "?" + delim;
        }
        return line;
    }

    /**
     * Generate a string representation of a list of SoM information instances.
     * Use the given delimiter to separate each value.
     *
     * @param infos
     * @param delimiter
     * @return
     */
    public static Map<String, String> concatenateSoMInfos(List<SoMInfo> infos, String delimiter) {
        String atom_ids = "";
        String reasubclass_ids = "";
        String reaclass_ids = "";
        String reamain_ids = "";
        String reagen_ids = "";

        for (SoMInfo info : infos) {
            atom_ids = addEntry(atom_ids, Integer.toString(info.atom_id), delimiter, info.is_confirmed);
            reasubclass_ids = addEntry(reasubclass_ids, Integer.toString(info.reasubclass_id), delimiter, info.is_confirmed);
            reaclass_ids = addEntry(reaclass_ids, Integer.toString(info.reaclass_id), delimiter, info.is_confirmed);
            reamain_ids = addEntry(reamain_ids, Integer.toString(info.reamain_id), delimiter, info.is_confirmed);
            reagen_ids = addEntry(reagen_ids, Integer.toString(info.reagen_id), delimiter, info.is_confirmed);
        }

        Map<String, String> ret = new HashMap<>();
        ret.put("atoms", atom_ids);
        ret.put(Globals.REASUBCLS_PROP, Utils.rstrip(reasubclass_ids, delimiter));
        ret.put(Globals.REACLS_PROP, Utils.rstrip(reaclass_ids, delimiter));
        ret.put(Globals.REAMAIN_PROP, Utils.rstrip(reamain_ids, delimiter));
        ret.put(Globals.REAGEN_PROP, Utils.rstrip(reagen_ids, delimiter));

        return ret;
    }

    /**
     * A helper method that sets an atom property to the values given in Globals.IS_SOM_CONFIRMED_VAL,
     * Globals.IS_SOM_POSSIBLE_VAL or Globals.UNKNOWN_VALUE based on the information passed by the caller
     * (name of the property and if it represents a confirmed or just possibly correct information).
     *
     * @param atom atom to set the property on
     * @param property name of the property (it encodes the information -- e.g. this is a SoM)
     * @param confirmed this information was confirmed and should be correct
     * @param possible this information was not confirmed and cannot be verified, but there are clues that show it might be true
     */
    private static void setConfirmationStatus(IAtom atom, String property, boolean confirmed, boolean possible) {
        if (confirmed) {
            atom.setProperty(property, Globals.IS_SOM_CONFIRMED_VAL);
        } else if (possible) {
            atom.setProperty(property, Globals.IS_SOM_POSSIBLE_VAL);
        } else {
            atom.setProperty(property, Globals.UNKNOWN_VALUE);
        }
    }

    private static boolean checkNone(String val) {
        return val.equals("") || val.equals("None");
    }

    /**
     * Parse the SoM information saved in the SD file. Throws an exception if no SoM information
     * is present or it is in unknown format.
     *
     * This method also treats the symmetry in the molecule and associates the same
     * information with equivalent atoms.
     *
     * @param iMolecule
     * @return
     * @throws InvalidSoMAnnotationException
     * @throws NoSoMAnnotationException
     * @throws NoSuchAtomException
     */
    public static Map<Integer, List<SoMInfo>> parseInfoAndUpdateMol (
            IAtomContainer iMolecule
    ) throws Exception {
        //charges need to be set for the EquivalentClassPartitioner to run properly.
        for (int i = 0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);
            iAtom.setProperty("MoleculeName", iMolecule.getProperty(Globals.ID_PROP));
            iAtom.setCharge((double) iAtom.getFormalCharge());
        }

        // parse SoMs from the file
        List<String> som_list = parseValueList((String) iMolecule.getProperty(Globals.SOM_PROP));
        List<String> reasubcls_list = parseValueList((String) iMolecule.getProperty(Globals.REASUBCLS_PROP));
        List<String> reacls_list = parseValueList((String) iMolecule.getProperty(Globals.REACLS_PROP));
        List<String> reamain_list = parseValueList((String) iMolecule.getProperty(Globals.REAMAIN_PROP));
        List<String> reagen_list = parseValueList((String) iMolecule.getProperty(Globals.REAGEN_PROP));
        if ( (som_list.size() != reasubcls_list.size())
                || (som_list.size() != reacls_list.size())
                || (som_list.size() != reamain_list.size())
                || (som_list.size() != reagen_list.size())
                ) {
            throw new Exception("The sizes of parsed SoM information lists do not match.");
        }

        // parse the SoM information into the SoMInfo data structures
        int list_idx = 0; // position in the list of parsed strings
        Map<Integer, List<SoMInfo>> som_info_map = new HashMap<>(); // maps atom position to all the SoM information available for that atom
        for (String som_entry : som_list) {
            String reasubcls = reasubcls_list.get(list_idx);
            String reacls = reacls_list.get(list_idx);
            String reamain = reamain_list.get(list_idx);
            String reagen = reagen_list.get(list_idx);

            // skip entries without annotated SoMs (entries with no atom positions)
            if (checkNone(som_entry)) {
                System.err.println("WARNING: Skipping empty SoM entry ('" + som_entry + "') for molecule " + iMolecule.getProperty(Globals.ID_PROP));
                list_idx++;
                continue;
            }

            String[] soms = som_entry.split(","); // more than one atom positions can be annotated per entry
            for (String som : soms) {
                int som_atom_number; // index of the atom in the molecule
                boolean is_confirmed; // is it a confirmed SoM or a possible SoM?

                if (som.matches("\\d+")) {
                    som_atom_number = Integer.parseInt(som);
                    is_confirmed = true;
                } else if (som.matches("\\d+\\?")) {
                    som_atom_number = Integer.parseInt(som.replaceAll("\\?", ""));
                    is_confirmed = false;
                } else {
                    throw new InvalidSoMAnnotationException(som);
                }

                SoMInfo som_info;
                if (
                        !checkNone(reasubcls)
                        && !checkNone(reacls)
                        && !checkNone(reamain)
                        && !checkNone(reagen)
                        ) {
                    som_info = new SoMInfo(
                            iMolecule
                            , som_atom_number
                            , is_confirmed
                            , Integer.parseInt(reasubcls)
                            , Integer.parseInt(reacls)
                            , Integer.parseInt(reamain)
                            , Integer.parseInt(reagen)
                    );
                } else {
                    System.err.println("WARNING: Incomplete SoM information detected. Skipping: " + som_atom_number + " " + reasubcls + " " + reacls + " " + reamain + " " + reagen);
                    continue;
                }

                if (som_info_map.containsKey(som_atom_number)) {
                    List<SoMInfo> atom_infos = som_info_map.get(som_atom_number);
                    atom_infos.add(som_info);
                    som_info_map.put(som_atom_number, atom_infos);
                } else {
                    List<SoMInfo> atom_infos = new ArrayList<>();
                    atom_infos.add(som_info);
                    som_info_map.put(som_atom_number, atom_infos);
                }
            }

            list_idx++;
        }

        // throw an exception if no SoM annotation was found for any atom in this molecule
        if (som_info_map.isEmpty()) {
            throw new NoSoMAnnotationException(iMolecule);
        }

        Map<Integer,Set<Integer>> symmetry_map = Utils.generateSymmetryMap(iMolecule);

        // add combined SoM information to each atom of the molecule
        for (int i=0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);

            // search for SoMs in the equivalence class of this atom and parse what is needed to encode some more SoM information
            int symmetry_number = (Integer) iAtom.getProperty("SymmetryAtomNumber");
            Set<Integer> equiv_atoms = symmetry_map.get(symmetry_number);
            boolean equiv_confirmed_som = false;
            boolean equiv_maybe_som = false;
            boolean equiv_possible_phase_I = false;
            boolean equiv_confirmed_phase_I = false;
            boolean equiv_possible_phase_II = false;
            boolean equiv_confirmed_phase_II = false;
            boolean equiv_possible_metapie = false;
            boolean equiv_confirmed_metapie = false;
            List<SoMInfo> equiv_som_infos = new ArrayList<>(); // collect all SoM information we have about the atom into this list
            for (int equiv_atom : equiv_atoms) { // iterate over all atoms in the equivalance class for the current atom
                if (som_info_map.containsKey(equiv_atom)) { // if there is SoM info available for any atom in the class, do this:
                    List<SoMInfo> infos = som_info_map.get(equiv_atom);
                    for (SoMInfo info : infos) {
                        if (info.is_confirmed) {
                            equiv_confirmed_som = true;
                        }
                        if (!equiv_confirmed_som) {
                            equiv_maybe_som = true;
                        }
                        if (info.is_confirmed && (info.reamain_id == 1 || info.reamain_id == 2)) {
                            equiv_confirmed_phase_I = true;
                        }
                        if (!info.is_confirmed && (info.reamain_id == 1 || info.reamain_id == 2)) {
                            equiv_possible_phase_I = true;
                        }
                        if (info.is_confirmed && info.reamain_id == 3) {
                            equiv_confirmed_phase_II = true;
                        }
                        if (!info.is_confirmed && info.reamain_id == 3) {
                            equiv_possible_phase_II = true;
                        }
                        if (info.is_confirmed && info.reamain_id == 4) {
                            equiv_confirmed_metapie = true;
                        }
                        if (!info.is_confirmed && info.reamain_id == 4) {
                            equiv_possible_metapie = true;
                        }
                    }
                    equiv_som_infos.addAll(infos); // get all the information associated with the SoM identified
                }
            }

            // if any of the atoms in the equivalence class is a possible or confirmed SoM, mark this atom the same way and save all the info
            setConfirmationStatus(iAtom, Globals.IS_SOM_PROP, equiv_confirmed_som, equiv_maybe_som);
            setConfirmationStatus(iAtom, Globals.IS_PI_PROP, equiv_confirmed_phase_I, equiv_possible_phase_I);
            setConfirmationStatus(iAtom, Globals.IS_PII_PROP, equiv_confirmed_phase_II, equiv_possible_phase_II);
            setConfirmationStatus(iAtom, Globals.IS_METAPIE_PROP, equiv_confirmed_metapie, equiv_possible_metapie);

            Map<String, String> concated_vals = concatenateSoMInfos(equiv_som_infos, "/");

            if (equiv_confirmed_som || equiv_maybe_som) {
                iAtom.setProperty(Globals.REASUBCLS_PROP, concated_vals.get(Globals.REASUBCLS_PROP));
                iAtom.setProperty(Globals.REACLS_PROP, concated_vals.get(Globals.REACLS_PROP));
                iAtom.setProperty(Globals.REAMAIN_PROP, concated_vals.get(Globals.REAMAIN_PROP));
                iAtom.setProperty(Globals.REAGEN_PROP, concated_vals.get(Globals.REAGEN_PROP));
            } else {
                iAtom.setProperty(Globals.REASUBCLS_PROP, Globals.UNKNOWN_VALUE);
                iAtom.setProperty(Globals.REACLS_PROP, Globals.UNKNOWN_VALUE);
                iAtom.setProperty(Globals.REAMAIN_PROP, Globals.UNKNOWN_VALUE);
                iAtom.setProperty(Globals.REAGEN_PROP, Globals.UNKNOWN_VALUE);
            }
        }

        return som_info_map;
    }
}
