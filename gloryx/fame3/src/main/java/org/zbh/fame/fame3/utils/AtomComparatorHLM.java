/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
   Copyright (C) 2013  Johannes Kirchmair <johannes.kirchmair@univie.ac.at>
 
    This file is part of GLORYx.

    GLORYx is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    All we ask is that proper credit is given for our work, which includes 
    - but is not limited to - adding the above copyright notice to the beginning 
    of your source code files, and to any copyright notice that you may distribute 
    with programs based on this work.

    GLORYx is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GLORYx.  If not, see <https://www.gnu.org/licenses/>.
*/

package org.zbh.fame.fame3.utils;

import org.zbh.fame.fame3.modelling.Modeller;
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