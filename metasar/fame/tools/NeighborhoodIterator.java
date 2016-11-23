package fame.tools;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.*;

/**
 * Facilitates iterating over atom neighborhoods
 *
 * Created by sicho on 11/21/16.
 */
public class NeighborhoodIterator {

    private int depth;
    private IMolecule mol;
    private boolean depict = false;
    private boolean ignore_hydrogens = true;

    public NeighborhoodIterator(IMolecule mol, int depth) {
        this.mol = mol;
        this.depth = depth;
    }

    public NeighborhoodIterator(IMolecule mol, int depth, boolean depict) {
        this(mol, depth);
        this.depict = depict;
    }

    public NeighborhoodIterator(IMolecule mol, int depth, boolean depict, boolean ignore_hydrogens) {
        this(mol, depth, depict);
        this.ignore_hydrogens = ignore_hydrogens;
    }

    private static void depictNeighborhood(IMolecule mol, Set<IAtom> frag_atms, String outfile) throws Exception {
        Set<IBond> bonds_all = new HashSet<>();
        for (IBond bond : mol.bonds()) {
            if (frag_atms.contains(bond.getAtom(0)) && frag_atms.contains(bond.getAtom(1))) {
                bonds_all.add(bond);
            }
        }
        IMolecule mol_frag = new Molecule();
        IBond[] b_arr = new IBond[bonds_all.size()];
        IAtom[] a_arr = new IAtom[frag_atms.size()];
        mol_frag.setBonds(bonds_all.toArray(b_arr));
        mol_frag.setAtoms(frag_atms.toArray(a_arr));
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol_frag);

        Depiction.generateDepiction(mol_frag, outfile);
    }

    public void iterate(NeighborhoodCollector collector) {
        for (IAtom atm : mol.atoms()) {
            if (ignore_hydrogens && atm.getSymbol().equals("H")) {
                continue;
            }
            int idx = mol.getAtomNumber(atm);
            Map<Integer, Set<IAtom>> neigborhood_map = new HashMap<>();
            Set<IAtom> atoms_current = new HashSet<>();
            Set<IAtom> atoms_next = new HashSet<>();
            atoms_current.add(atm);
            neigborhood_map.put(0, new HashSet<>(atoms_current));
            collector.collect(atm, neigborhood_map.get(0), 0);
            for (int i = 0; i != depth; i++) {
                atoms_next.clear();
                for (IAtom atm_current : atoms_current) {
                    List<IAtom> nbs = mol.getConnectedAtomsList(atm_current);
                    atoms_next.addAll(nbs);
                    if (ignore_hydrogens) {
                        for (IAtom nb : nbs) {
                            if (nb.getSymbol().equals("H")) {
                                atoms_next.remove(nb);
                            }
                        }
                    }
                }
                if (i > 0) {
                    atoms_next.removeAll(neigborhood_map.get(i-1));
                }
                neigborhood_map.put(i + 1, new HashSet<>(atoms_next));
                collector.collect(atm, neigborhood_map.get(i + 1), i + 1);
                atoms_current.clear();
                atoms_current.addAll(atoms_next);
            }

            if (depict) {
                try {
                    Set<IAtom> fragment_atoms = new HashSet<>();
                    for (Integer val : neigborhood_map.keySet()) {
                        fragment_atoms.addAll(neigborhood_map.get(val));
                    }
                    depictNeighborhood(mol, fragment_atoms, String.format("%s_%d.png", mol.getProperty(Globals.ID_PROP), idx + 1));
                } catch (Exception exp) {
                    System.err.println("WARNING: failed to generate neigborhood depiction");
                    exp.printStackTrace();
                }
            }
        }
    }
}
