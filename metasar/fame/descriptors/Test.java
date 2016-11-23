package fame.descriptors;

import fame.tools.Globals;
import fame.tools.NeighborhoodCollector;
import fame.tools.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.Set;


/**
 * Just a class to test stuff a bit. Do not take this too seriously ;)
 *
 * Created by sicho on 10/11/16.
 */
public class Test {

    static private class MyCollector implements NeighborhoodCollector {

        IMolecule mol;

        MyCollector(IMolecule mol) {
            this.mol = mol;
        }

        @Override
        public void collect(IAtom atm, Set<IAtom> neighbors, int current_depth) {
            int idx = mol.getAtomNumber(atm);
            System.out.println(String.format("Atom #%d", idx + 1));
            System.out.println(String.format("\t depth: %d", current_depth));
            System.out.println("\t neighbors:");
            for (IAtom ng : neighbors) {
                System.out.println(String.format("\t\t %s", ng.getAtomTypeName()));
            }
        }
    }

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
        IMolecule mol = smiles_parser.parseSmiles("[O-]C(=O)CC(=O)[O-]");
        mol.setProperty(Globals.ID_PROP, "_test_mol");
//        Depiction.generateDepiction(mol, "_test_mol.png");

//        NeighborhoodCollector collector = new MyCollector(mol);
//        NeighborhoodIterator iterator = new NeighborhoodIterator(mol, 1, true);
//        iterator.iterate(collector);

        WorkerThread my_thread = new WorkerThread(mol, true);
        my_thread.run();
    }
}
