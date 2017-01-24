package utils;

import modelling.Modeller;
import org.openscience.cdk.interfaces.IAtom;

import java.util.Comparator;

public class AtomComparatorHLM implements Comparator<IAtom> {

    private final int before = -1;
    private final int after = 1;

    double currentAtomScore;
    double comparisonAtomScore;
    double currentAtomAccessibility;
    double comparisonAtomAccessibility;



    // Atoms sorted by Energy and A
    // My implementation of compare, compares E and A
    public int compare(IAtom currentAtom, IAtom comparisonAtom) {
        double proba_yes_a = (Double) currentAtom.getProperty(Modeller.proba_yes_fld);
        double proba_yes_b = (Double) comparisonAtom.getProperty(Modeller.proba_yes_fld);
        if (proba_yes_a >= proba_yes_b) {
            return before;
        } else {
            return after;
        }
    }


}