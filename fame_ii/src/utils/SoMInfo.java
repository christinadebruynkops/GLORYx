package utils;

import globals.Globals;
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
    private int reagen;
    private String target_id;

    private static final List<String> targets = new ArrayList<>(
            Arrays.asList(
                    "1A2"
                    , "2A6"
                    , "2B6"
                    , "2C8"
                    , "2C9"
                    , "2C19"
                    , "2D6"
                    , "2E1"
                    , "3A4"
            ));

    private static final List<Integer> reagens = new ArrayList<>(
            Arrays.asList(
                    1
                    , 2
                    , 3
            ));

    private SoMInfo(
            IAtomContainer mol
            , int atom_id
            , int reagen
            , String target_id
    ) {
        this.mol = mol;
        this.atom_id = atom_id;
        this.reagen = reagen;
        this.target_id = target_id;
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

    public String getTarget() {
        return target_id;
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

        // parse the SoM information into the SoMInfo data structures
        Map<Integer, List<SoMInfo>> som_info_map = new HashMap<>(); // maps atom position to all the SoM information available for that atom
        for (Map.Entry<Object, Object> som_entry : iMolecule.getProperties().entrySet()) {
            String key = (String) som_entry.getKey();

            if (key.startsWith(Globals.PRIM_SOM_PROP_PREFIX)
                    || key.startsWith(Globals.SEC_SOM_PROP_PREFIX)
                    || key.startsWith(Globals.TER_SOM_PROP_PREFIX)
                    ) {

                String value = (String) som_entry.getValue();

                int reagen;
                String prop_prefix;
                if (key.startsWith(Globals.PRIM_SOM_PROP_PREFIX)) {
                    reagen = 1;
                    prop_prefix = Globals.PRIM_SOM_PROP_PREFIX;
                } else if (key.startsWith(Globals.SEC_SOM_PROP_PREFIX)) {
                    reagen = 2;
                    prop_prefix = Globals.SEC_SOM_PROP_PREFIX;
                } else if (key.startsWith(Globals.TER_SOM_PROP_PREFIX)) {
                    reagen = 3;
                    prop_prefix = Globals.TER_SOM_PROP_PREFIX;
                } else {
                    throw new Exception("This shouldn't have happened :(");
                }
                String target_id = key.replaceAll(prop_prefix, "");

                String[] atom_ids = value.split("\\s");
                for (String atom_id_str : atom_ids) {
                    int atom_id = Integer.parseInt(atom_id_str);
                    SoMInfo som_info = new SoMInfo(
                            iMolecule
                            , atom_id
                            , reagen
                            , target_id
                    );

                    if (som_info_map.containsKey(atom_id)) {
                        List<SoMInfo> atom_infos = som_info_map.get(atom_id);
                        atom_infos.add(som_info);
                        som_info_map.put(atom_id, atom_infos);
                    } else {
                        List<SoMInfo> atom_infos = new ArrayList<>();
                        atom_infos.add(som_info);
                        som_info_map.put(atom_id, atom_infos);
                    }
                }
            }
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
            List<SoMInfo> equiv_som_infos = new ArrayList<>(); // collect all SoM information we have about the atom into this list
            for (int equiv_atom : equiv_atoms) { // iterate over all atoms in the equivalance class for the current atom
                if (som_info_map.containsKey(equiv_atom)) { // if there is SoM info available for any atom in the class, do this:
                    equiv_confirmed_som = true;
                    equiv_som_infos.addAll(som_info_map.get(equiv_atom)); // get all the information associated with the SoM identified
                }
            }

            // initialize map of output data
            Map<String, String> written_data = new TreeMap<>();
            written_data.put(Globals.IS_SOM_PROP, Globals.UNKNOWN_VALUE);
            for (String target_id : targets) {
                written_data.put(target_id, Globals.UNKNOWN_VALUE);
                for (Integer reagen_id : reagens) {
                    written_data.put(target_id + "_" + Integer.toString(reagen_id), Globals.UNKNOWN_VALUE);
                }
            }

            // write som information into the 'table'
            if (equiv_confirmed_som) {
                written_data.put(Globals.IS_SOM_PROP, Globals.IS_SOM_CONFIRMED_VAL);

                for (SoMInfo info : equiv_som_infos) {
                    written_data.put(info.target_id, Globals.IS_SOM_CONFIRMED_VAL);
                    written_data.put(info.target_id + "_" + Integer.toString(info.reagen), Globals.IS_SOM_CONFIRMED_VAL);
                }
            }

            // write atom properties
            for (Map.Entry<String, String> entry : written_data.entrySet()) {
                iAtom.setProperty(entry.getKey(), entry.getValue());
            }
        }

        return som_info_map;
    }
}

