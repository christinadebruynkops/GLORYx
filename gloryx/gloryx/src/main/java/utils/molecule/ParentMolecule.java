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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Class to store all relevant parent molecule information in a more memory-efficient way than using IAtomContainers.
 * The extra information compared to BasicMolecule is used for evaluation purposes.
 * Two BasicMolecules are considered equal if they have the same InChI and the same ID, so beware when adding them to sets if 
 * two instances with the same InChI have e.g. different enzyme or phase information.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class ParentMolecule extends BasicMolecule {

	private Double molecularWeight;
	private int heavyAtomCount;
	private String originalInputSmiles;
	
	private static final Logger logger = LoggerFactory.getLogger(ParentMolecule.class.getName());

	
	// ParentMolecules are considered equal based solely on their InChIs and IDs.
	
	@Override
	public boolean equals(Object obj) {  // just require InChI and ID to be equal, and require InChI to be defined (i.e. neither null nor "")
    	
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ParentMolecule other = (ParentMolecule) obj;
		
		if (inchi == null) {  // should never be null, but if it is, it will be added to the set/map no matter what
			return false;
		} else if (inchi.equals("")) {  // if no inchi is specified, the PredictedMolecule will be added to set/map no matter what
			return false;	
	    } else if (!inchi.equals(other.inchi)) {
			return false;
			
			
	    } else if (id == null) {  // should never be null, but if it is, it will be added to the set/map no matter what
			return false;
		} else if (id.equals("")) {  // if no ID is specified, the PredictedMolecule will be added to set/map no matter what
			logger.warn("ParentMolecule with empty ID is being checked for equality.");
			return false;	
	    } else if (!id.equals(other.id)) {
	    		logger.warn("Two ParentMolecule objects have the same InChI but different IDs. They may be stereoisomers....");
			return false;
	    }
		
		return true;
	}
 
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inchi == null) ? 0 : inchi.hashCode());
		return result;
	}
	
	
	// --- getters and setters ---
	
	public Double getMolecularWeight() {
		return molecularWeight;
	}
	public void setMolecularWeight(Double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}
	
	public int getHeavyAtomCount() {
		return heavyAtomCount;
	}
	public void setHeavyAtomCount(int heavyAtomCount) {
		this.heavyAtomCount = heavyAtomCount;
	}

	public String getOriginalInputSmiles() {
		return originalInputSmiles;
	}

	public void setOriginalInputSmiles(String originalInputSmiles) {
		this.originalInputSmiles = originalInputSmiles;
	}
	
}
