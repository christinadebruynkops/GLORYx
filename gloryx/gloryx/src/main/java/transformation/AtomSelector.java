/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
 
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

package main.java.transformation;

import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.zbh.fame.fame3.modelling.Modeller;

import ambit2.smarts.IAcceptable;

/** 
 * Class to specify that transformations can be applied only when at least one heavy atom in the mapping is predicted to be a SoM. 
 * This class is therefore only used if SoMs are being used as a hard filter (i.e. in GLORY's MaxEfficiency mode).
 * This class is based on the IAcceptable class from Ambit SMIRKS. 
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class AtomSelector implements IAcceptable{

	private static final String ATOM_ACCEPTED = "Atom {} is a SoM and is accepted for transformation";
	private static final String ERROR_PROB_IS_NULL = "Error: Modeller.proba_yes_fld is null for a heavy atom. This should never happen.";

	private static final Logger logger = LoggerFactory.getLogger(AtomSelector.class.getName());

	private Double somCutoff = 0.4;
	
	// constructors
	public AtomSelector() {
	}
	public AtomSelector(Double somCutoff) {
		this.somCutoff = somCutoff;
	}
	
	
	@Override
	public boolean accept(List<IAtom> atoms) {
		// this method is called by SMIRKSManager's ApplyTransformation... methods
		// determines whether an atom can be used for a SMIRKS transformation
		
		// as long as one of the atoms in the list is predicted to be a SoM, the transformation will be applied

		boolean acceptable = false;

		for (IAtom atom : atoms) {
			if (!atom.getSymbol().equals("H")) {  // only consider heavy atoms

				Double prob = atom.getProperty(Modeller.proba_yes_fld); 
				Boolean isSom = (prob >= somCutoff);
				
				// debugging ---
//				Boolean isSomOriginal = atom.getProperty(Modeller.is_som_fld);  
//				if (isSom != isSomOriginal) {
//					logger.error("ERROR: Manual calculation of SoM doesn't match!");
//				}
				// ----
				
				Assert.notNull(prob, ERROR_PROB_IS_NULL);
				
				if (isSom) {
					acceptable = true; 
					String atomDescription = atom.getProperty("Atom");
					logger.debug(ATOM_ACCEPTED, atomDescription);
					break;
				}
			}
		}
		return acceptable;
	}

}
