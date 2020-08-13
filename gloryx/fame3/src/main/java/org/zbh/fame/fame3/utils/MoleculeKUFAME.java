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
