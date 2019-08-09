/* 
 * Copyright (C) 2010-2011  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
 * 
 * Contact: smartcyp@farma.ku.dk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package smartcyp;


import java.util.Comparator;

import org.openscience.cdk.Atom;

import smartcyp.MoleculeKU.SMARTCYP_PROPERTY;



public class AtomComparator implements Comparator<Atom> {

	private final int before = -1;
	private final int equal = 0;		// Only used for symmetric atoms, not atoms with same Score
	private final int after = 1;

	double currentAtomScore;
	double comparisonAtomScore;
	double currentAtomAccessibility;
	double comparisonAtomAccessibility;



	// Atoms sorted by Energy and A
	// My implementation of compare, compares E and A
	public int compare(Atom currentAtom, Atom comparisonAtom) {

		return this.compareScore(currentAtom, comparisonAtom);

	}



	private int compareScore(Atom currentAtom, Atom comparisonAtom){
		
		// Set Score values
		if(SMARTCYP_PROPERTY.Score.get(currentAtom) != null)  currentAtomScore = SMARTCYP_PROPERTY.Score.get(currentAtom).doubleValue();
		if(SMARTCYP_PROPERTY.Score.get(comparisonAtom) != null)  comparisonAtomScore = SMARTCYP_PROPERTY.Score.get(comparisonAtom).doubleValue();
		
		// Dual null Scores
		if (SMARTCYP_PROPERTY.Score.get(currentAtom) == null && SMARTCYP_PROPERTY.Score.get(comparisonAtom) == null){					
			//If scores are null the Energies are too, then compare the Accessibility
			return this.compareAccessibility(currentAtom, comparisonAtom);
		}

		// Single null scores
		else if(SMARTCYP_PROPERTY.Score.get(currentAtom) == null) return after;
		else if(SMARTCYP_PROPERTY.Score.get(comparisonAtom) == null) return before;

		// Compare 2 numeric scores
		else if(currentAtomScore < comparisonAtomScore) return before;
		else if(currentAtomScore > comparisonAtomScore) return after;

		// Distinguish symmetric atoms
		else return this.checksymmetry(currentAtom, comparisonAtom);

	}



	private int compareAccessibility(Atom currentAtom, Atom comparisonAtom){

		// Compare 2 numeric Accessibility values
		currentAtomAccessibility = SMARTCYP_PROPERTY.Accessibility.get(currentAtom).doubleValue();
		comparisonAtomAccessibility = SMARTCYP_PROPERTY.Accessibility.get(comparisonAtom).doubleValue();
		
		if(currentAtomAccessibility < comparisonAtomAccessibility) return after;
		else if(currentAtomAccessibility > comparisonAtomAccessibility) return before;

		// Distinguish symmetric atoms
		else return this.checksymmetry(currentAtom, comparisonAtom);

	}

	private int checksymmetry(Atom currentAtom, Atom comparisonAtom){

		// Symmetric
		if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(comparisonAtom).intValue()) return equal;
		
		// Non-symmetric
		else return after;
	}


}

