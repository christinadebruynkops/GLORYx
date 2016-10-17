package fame.tools;

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

    public static List<SoMInfo> parseInfoAndUpdateMol (
            IAtomContainer iMolecule
    ) throws Exception {
        //charges need to be set for the EquivalentClassPartitioner to run properly.
        for (int i = 0; i < iMolecule.getAtomCount(); i++) {
            IAtom iAtom = iMolecule.getAtom(i);
            iAtom.setProperty("MoleculeName", iMolecule.getProperty(Globals.ID_PROP));
            iAtom.setCharge((double) iAtom.getFormalCharge());
        }

        // parse SoMs from the file
        List<String> som_list = Utils.parseValueList((String) iMolecule.getProperty(Globals.SOM_PROP));
        List<String> reasubcls_list = Utils.parseValueList((String) iMolecule.getProperty(Globals.REASUBCLS_PROP));
        List<String> reacls_list = Utils.parseValueList((String) iMolecule.getProperty(Globals.REACLS_PROP));
        List<String> reamain_list = Utils.parseValueList((String) iMolecule.getProperty(Globals.REAMAIN_PROP));
        List<String> reagen_list = Utils.parseValueList((String) iMolecule.getProperty(Globals.REAGEN_PROP));
        // TODO: assert that these have the same length

        // parse the SoM information into suitable data structures
        int list_idx = 0;
        List<SoMInfo> som_infos = new ArrayList<>();
        Map<Integer, List<SoMInfo>> som_info_map = new HashMap<>();
        for (String som_entry : som_list) {
            String reasubcls = reasubcls_list.get(list_idx);
            String reacls = reacls_list.get(list_idx);
            String reamain = reamain_list.get(list_idx);
            String reagen = reagen_list.get(list_idx);

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
                    throw new Exception("Unrecognized value: " + som);
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
                som_infos.add(som_info);
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
            boolean equiv_is_som = false;
            SoMInfo info;
            for (int equiv_atom : equiv_atoms) {
                if (som_info_map.containsKey(equiv_atom)) {
                    equiv_is_som = true;
                    info = som_info_map.get(equiv_atom).get(0); // TODO: decide what happens if there are more entries for given atom
                    break; // TODO: check if there is more entries per equivalence class
                }
            }

            // if any of the atoms in the equivalence class is a reported SoM, mark this as a SoM too and save all the info
            // TODO: use the info instance from above to save additional data about SoMs
            if (equiv_is_som) {
                iAtom.setProperty("isSom", "true");
            } else {
                iAtom.setProperty("isSom", "false");
            }
        }

        return som_infos;
    }
}
