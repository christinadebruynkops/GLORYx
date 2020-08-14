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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import main.java.utils.Enzymes;
import main.java.utils.Phase;

/** 
 * Class to store all relevant molecule information in a more memory-efficient way than using IAtomContainers.
 * Two BasicMolecules are considered equal if they have the same InChI, so beware when adding them to sets if 
 * two instances with the same InChI have e.g. different enzyme or phase information.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class BasicMolecule {
	
	protected String id;
	protected String name;
	protected String smiles;
	protected String inchi;
	
	protected Set<Enzymes> enzymesFormedBy;
	protected Phase metabolismPhase;
	
	protected String drugbankId;
	protected String metxbiodbId;
	

	//constructors
	public BasicMolecule(String id, String smiles, String inchi) {
		this.id = id;
		this.smiles = smiles;
		this.inchi = inchi;
		this.enzymesFormedBy = new HashSet<>();
	}
	
	public BasicMolecule(String id, String name, String smiles, String inchi) {
		this.id = id;
		this.name = name;
		this.smiles = smiles;
		this.inchi = inchi;
		this.enzymesFormedBy = new HashSet<>();
	}
	
	public BasicMolecule() {
		this.id = "";
		this.smiles = "";
		this.inchi = "";
		this.enzymesFormedBy = new HashSet<>();
	}
	
	
	// BEWARE: Two BasicMolecules are considered equal if they have the same InChI.
	
    @Override
    public boolean equals(Object o) {

        if (o == this) {
        		return true;
        }
        if (!(o instanceof BasicMolecule)) {
            return false;
        }
        BasicMolecule basicMolecule = (BasicMolecule) o;
        return (basicMolecule.getInchi().equals(this.inchi)); 
    }

    @Override
    public int hashCode() {
        return Objects.hash(inchi);
    }
	

	
	// --- getters, setters, and adders ---

	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	
	public void setInchi(String inchi) {
		this.inchi = inchi;
	}
	
	public void setMetabolismPhase(Phase metabolismPhase) {
		this.metabolismPhase = metabolismPhase;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSmiles() {
		return this.smiles;
	}
	
	public String getInchi() {
		return this.inchi;
	}
	
	public Phase getMetabolismPhase() {
		return metabolismPhase;
	}
	
	public void addEnzyme(Enzymes enzyme) {
		if (this.enzymesFormedBy == null) { // should never happen because initialized in constructors
			this.enzymesFormedBy = new HashSet<>();
		}
		this.enzymesFormedBy.add(enzyme);
	}
	
	public void addEnzymes(Collection<Enzymes> enzymes) {
		if (this.enzymesFormedBy == null) { // should never happen because initialized in constructors
			this.enzymesFormedBy = new HashSet<>();
		}
		for (Enzymes enzyme : enzymes) {
			this.enzymesFormedBy.add(enzyme);
		}
	}
	
	public Set<Enzymes> getEnzymes() {
		return this.enzymesFormedBy;
	}

	public void setDrugBankID(String id) {
		this.drugbankId = id;
	}
	
	public void setMetXBioDBID(String id) {
		this.metxbiodbId = id;
	}
	
	public String getDrugBankID() {
		return this.drugbankId;
	}
	
	public String getMetXBioDBID() {
		return this.metxbiodbId;
	}

}
