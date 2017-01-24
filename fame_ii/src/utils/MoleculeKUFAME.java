package utils;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import smartcyp.MoleculeKU;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by sicho on 1/24/17.
 */
public class MoleculeKUFAME extends MoleculeKU {

    AtomComparatorHLM atomComparatorHLM = new AtomComparatorHLM();
    private TreeSet<IAtom> atomsSortedByProbability = new TreeSet<IAtom>(atomComparatorHLM);

    // Constructor
    // This constructor also calls the methods that calculate MaxTopDist, Energies and sorts C, N, P and S atoms
    // This constructor is the only way to create MoleculeKU and Atom objects, -there is no add() method
    public MoleculeKUFAME(IAtomContainer iAtomContainer, HashMap<String, Double> SMARTSnEnergiesTable) throws CloneNotSupportedException
    {
        super(iAtomContainer, SMARTSnEnergiesTable);
    }

    public TreeSet<IAtom> getAtomsSortedByHLMProbability() {

        if (atomsSortedByProbability.isEmpty()) {
            for (IAtom atm: this.atoms()){
                if(!atm.getSymbol().equals("H")) {
                    atomsSortedByProbability.add(atm);
                }
            }
        }

        return atomsSortedByProbability;
    }
}
