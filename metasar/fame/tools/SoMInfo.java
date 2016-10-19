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
    ) throws InvalidSoMAnnotationException, NoSoMAnnotationException, NoSuchAtomException {
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
        EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner(iMolecule);
        symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu(iMolecule);

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
            boolean equiv_is_som = false; // indicates if a SoM was found within this equivalence class
            SoMInfo main_info = null; // contains information about the representative SoM in the class (if it exists)
            for (int equiv_atom : equiv_atoms) { // iterate over all atoms in the equivalance class for the current atom
                if (som_info_map.containsKey(equiv_atom)) { // if there is SoM info available for any atom in the class, do this:
                    equiv_is_som = true;
                    List<SoMInfo> infos = som_info_map.get(equiv_atom); // get all the information associated with the SoM identified
                    main_info = infos.get(0); // init the representative SoM info with the first available entry
                    for (SoMInfo som_info : infos) {
                        if (som_info.is_confirmed) {
                            main_info = som_info; // if there is an entry with where this atom is a confirmed SoM, prefer it
                        }
                    }
                    break; // TODO: thanks to this we only consider first annotated SoM in the class, but maybe we should check if there is more entries per equivalence class and decide what to do
                }
            }

            // if any of the atoms in the equivalence class is a reported SoM, mark this as a SoM too and save all the info
            if (equiv_is_som) {
                iAtom.setProperty(Globals.IS_SOM_PROP, "true");
            } else {
                iAtom.setProperty(Globals.IS_SOM_PROP, "false");
            }
            if (main_info != null) {
                iAtom.setProperty(Globals.IS_SOM_CONFIRMED_PROP, main_info.is_confirmed);
                iAtom.setProperty(Globals.REASUBCLS_PROP, main_info.reasubclass_id);
                iAtom.setProperty(Globals.REACLS_PROP, main_info.reaclass_id);
                iAtom.setProperty(Globals.REAMAIN_PROP, main_info.reamain_id);
                iAtom.setProperty(Globals.REAGEN_PROP, main_info.reagen_id);
            } else {
                iAtom.setProperty(Globals.IS_SOM_CONFIRMED_PROP, "NA");
                iAtom.setProperty(Globals.REASUBCLS_PROP, "NA");
                iAtom.setProperty(Globals.REACLS_PROP, "NA");
                iAtom.setProperty(Globals.REAMAIN_PROP, "NA");
                iAtom.setProperty(Globals.REAGEN_PROP, "NA");
            }
        }

        return som_info_map;
    }
}
