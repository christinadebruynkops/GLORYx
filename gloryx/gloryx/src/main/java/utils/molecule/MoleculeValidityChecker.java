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

package main.java.utils.molecule;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to check whether a predicted metabolite is a valid molecule.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MoleculeValidityChecker {

	private static final String EXCEPTION_WARNING = "Error matching atom type for product {} ; excluding.";
	private static final String BROKEN_WARNING = "No atom type could be assigned for a carbon; hence ignoring this product because it's broken. SMILES: {} . Transformation name {}";


	private MoleculeValidityChecker() {
		throw new IllegalStateException("Utility class");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MoleculeValidityChecker.class.getName());	
	
	
	/**
	 * Checks whether a molecule has an invalid valence for any carbon atom and is therefore an invalid molecule.
	 * <p>
	 * Note: It doesn't make sense to check valences of other atoms to exclude products, as nitrogens are valid in most 
	 * forms even when the reaction did something it shouldn't have.
	 * 
	 * @param molecule
	 * @param transformationName
	 * @return returns true if the molecule is invalid and should be EXCLUDED
	 */
	public static Boolean excludeDueToCarbonValence(IAtomContainer molecule, String transformationName) {
		
		Boolean skip = false;
		
		for (IAtom a : molecule.atoms()) {
			if (a.getAtomicNumber() == 6) {
				
				try {
					IAtomType atomType = CDKAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance()).findMatchingAtomType(molecule, a);					
					if (atomType.getAtomTypeName().equals("X")) {
						// no atom type could be assigned and we know it's a carbon because of the atomic number, so this must be a carbon with an 
						//invalid valence (e.g. 5 bonds total because of too many double bonds
						if (logger.isWarnEnabled()) {
							logger.warn(BROKEN_WARNING,
									MoleculeManipulator.generateSmiles(molecule), transformationName);
						}
						skip = true;
					}
					
				} catch (CDKException e) {
					logger.error(EXCEPTION_WARNING, MoleculeManipulator.generateSmiles(molecule));
					skip = true;
				}
				
				if (skip) {
					break;
				}
			} 
			// use this to check - if doing this, remove the break statement above to check ALL atoms in all molecules
//			else if (a.getAtomicNumber() != 1) { 
//				try {
//					IAtomType atomType = CDKAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance()).findMatchingAtomType(molecule, a);
//					logger.debug("atom type is {} with valence {}", atomType.getAtomTypeName(), atomType.getValency());
//					
//					if (atomType.getAtomTypeName().equals("X")) {
//						logger.warn("No atom type could be assigned for an atom with atomic number {} in product {}. Transformation name {}", 
//								a.getAtomicNumber(), MoleculeManipulator.generateSmiles(molecule), transformationName);
//						// skip = true;
//						// continue;
//					}
//					
//				} catch (CDKException e) {
//					logger.error("Error matching atom type of atom with atomic number {} for product {}.", a.getAtomicNumber(), MoleculeManipulator.generateSmiles(molecule));
//					// skip = true;
//					// continue;
//				}
//			}
		}
		return skip;
	}
	
	
}
