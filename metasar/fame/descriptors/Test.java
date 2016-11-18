package fame.descriptors;

import fame.tools.Depiction;
import fame.tools.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Just a class to test stuff a bit. Do not take this too seriously ;)
 *
 * Created by sicho on 10/11/16.
 */
public class Test {

    private static void protonationTest() throws Exception {
        SmilesParser smiles_parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = smiles_parser.parseSmiles("[H]OC(=O)CCCCC(=O)O[H]");
        mol = AtomContainerManipulator.removeHydrogens(mol);

        IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());

        // print out the Sybyl atom types assigned to the molecule above
        for (IAtom atom : mol.atoms()) {
            IAtomType iAtomType = atm.findMatchingAtomType(mol,atom);
            System.out.print(mol.getAtomNumber(atom));
            System.out.print("\t");
            System.out.print(iAtomType.getAtomTypeName());
            System.out.println();
        }

        Utils.deprotonateCarboxyls(mol);

        // atom types should be assigned correctly now

        for (IAtom atom : mol.atoms()) {
            IAtomType iAtomType = atm.findMatchingAtomType(mol,atom);
            System.out.print(mol.getAtomNumber(atom));
            System.out.print("\t");
            System.out.print(iAtomType.getAtomTypeName());
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        SmilesParser smiles_parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//        SmilesGenerator smiles_generator = new SmilesGenerator();
        IMolecule mol = smiles_parser.parseSmiles("OC(=O)CC(=O)O");
        Depiction.generateDepiction(mol, "test.png");

        int depth = 1;
        int frag_counter = 0;
        for (IAtom atm : mol.atoms()) {
            int idx = mol.getAtomNumber(atm);
            System.out.println("Atom #" + idx + ": " + atm.getSymbol());
            Set<IAtom> atoms_all = new HashSet<>();
            Set<IAtom> atoms_current = new HashSet<>();
            Set<IAtom> atoms_next = new HashSet<>();
            atoms_all.add(atm);
            atoms_current.add(atm);
            for (int i = 0; i != depth; i++) {
                atoms_next.clear();
                for (IAtom atm_current : atoms_current) {
                    List<IAtom> nbs = mol.getConnectedAtomsList(atm_current);
                    atoms_all.addAll(nbs);
                    atoms_next.addAll(nbs);
                }
                atoms_next.removeAll(atoms_all);
                atoms_current.clear();
                atoms_current.addAll(atoms_next);
            }

            Set<IBond> bonds_all = new HashSet<>();
            for (IBond bond : mol.bonds()) {
                if (atoms_all.contains(bond.getAtom(0)) && atoms_all.contains(bond.getAtom(1))) {
                    bonds_all.add(bond);
                }
            }

            IMolecule mol_frag = new Molecule();
            IBond[] b_arr = new IBond[bonds_all.size()];
            IAtom[] a_arr = new IAtom[atoms_all.size()];
            mol_frag.setBonds(bonds_all.toArray(b_arr));
            mol_frag.setAtoms(atoms_all.toArray(a_arr));
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol_frag);

            AtomSignature atm_sig = new AtomSignature(mol_frag.getAtomNumber(atm), mol_frag);
            System.out.println(atm_sig.toCanonicalString());

            Depiction.generateDepiction(mol_frag, String.format("test_%d.png", frag_counter));
            frag_counter++;
        }

    }
}
