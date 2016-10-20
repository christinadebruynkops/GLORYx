package fame.tools;

import org.openscience.cdk.exception.NoSuchAtomException;
import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
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

    private static String addEntry(String line, String entry, String delim, boolean is_confirmed) {
        if (is_confirmed) {
            line += entry + delim;
        } else {
            line += entry + "?" + delim;
        }
        return line;
    }

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

    private static void setConfirmationStatus(IAtom atom, String property, boolean confirmed, boolean possible) {
        if (confirmed) {
            atom.setProperty(property, Globals.IS_SOM_CONFIRMED_VAL);
        } else if (possible) {
            atom.setProperty(property, Globals.IS_SOM_POSSIBLE_VAL);
        } else {
            atom.setProperty(property, Globals.UNKNOWN_VALUE);
        }
    }

    /**
     * Parse the SoM information saved in the SD file. Throws an exception if no SoM information
     * is present or it is in unknown format.
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
        // TODO: assert that these have the same length

        // parse the SoM information into suitable data structures
        int list_idx = 0;
        Map<Integer, List<SoMInfo>> som_info_map = new HashMap<>();
        for (String som_entry : som_list) {
            String reasubcls = reasubcls_list.get(list_idx);
            String reacls = reacls_list.get(list_idx);
            String reamain = reamain_list.get(list_idx);
            String reagen = reagen_list.get(list_idx);

            // skip entries without annotated SoMs
            if (som_entry.matches("None") || som_entry.isEmpty()) {
                System.err.println("WARNING: Skipping empty SoM entry ('" + som_entry + "') for molecule " + iMolecule.getProperty(Globals.ID_PROP));
                list_idx++;
                continue;
            }

            String[] soms = som_entry.split(",");
            for (String som : soms) {
                int som_atom_number;
                boolean is_confirmed;

                if (som.matches("\\d+")) {
                    som_atom_number = Integer.parseInt(som);
                    is_confirmed = true;
                } else if (som.matches("\\d+\\?")) {
                    som_atom_number = Integer.parseInt(som.replaceAll("\\?", ""));
                    is_confirmed = false;
                } else {
                    throw new InvalidSoMAnnotationException(som);
                }

                SoMInfo som_info = new SoMInfo(
                        iMolecule
                        , som_atom_number
                        , is_confirmed
                        , Integer.parseInt(reasubcls)
                        , Integer.parseInt(reacls)
                        , Integer.parseInt(reamain)
                        , Integer.parseInt(reagen)
                );
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

        // throw an exception if no SoM annotation was found for this molecule
        if (som_info_map.isEmpty()) {
            throw new NoSoMAnnotationException(iMolecule);
        }

        // add symmetry numbers to the molecule
        int[] symmetryNumbersArray;
        try {
            EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner(iMolecule);
            symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu(iMolecule);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            throw new Exception("memory error");
        }

        // generate a mapping of symmetry numbers to atom numbers
        Map<Integer,Set<Integer>> som_map = new HashMap<>();
        for (int i=0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);
            int symmetry_number = symmetryNumbersArray[i+1];
            iAtom.setProperty("SymmetryAtomNumber", symmetry_number);
            iMolecule.setAtom(i, iAtom);

            int atom_number = iMolecule.getAtomNumber(iAtom) + 1;
            if (som_map.containsKey(symmetry_number)) {
                Set<Integer> atom_ids = som_map.get(symmetry_number);
                atom_ids.add(atom_number);
                som_map.put(symmetry_number, atom_ids);
            } else {
                Set<Integer> atom_ids = new HashSet<>();
                atom_ids.add(atom_number);
                som_map.put(symmetry_number, atom_ids);
            }
        }

        //add combined SOM information to each unique atom of a molecule
        for (int i=0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);

            // search for SoMs in the equivalence class of this atom
            int symmetry_number = (Integer) iAtom.getProperty("SymmetryAtomNumber");
            Set<Integer> equiv_atoms = som_map.get(symmetry_number);
            boolean equiv_confirmed_som = false;
            boolean equiv_maybe_som = false;
            boolean equiv_possible_phase_I = false;
            boolean equiv_confirmed_phase_I = false;
            boolean equiv_possible_phase_II = false;
            boolean equiv_confirmed_phase_II = false;
            boolean equiv_possible_metapie = false;
            boolean equiv_confirmed_metapie = false;
            List<SoMInfo> equiv_som_infos = new ArrayList<>();
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
