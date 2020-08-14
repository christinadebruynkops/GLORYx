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


/** 
 * Class to store all relevant molecule information for predicted metabolites in a more memory-efficient way than using IAtomContainers.
 * Two PredictedMolecules are considered equal if they have the same InChI and the same priority score.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class PredictedMolecule extends BasicMolecule implements Comparable<PredictedMolecule> {

	private Double priorityScore = (double) 0;
	private String parentID = "";
	private String parentName = "";
	private String parentInchi = "";
	private String parentSmiles = "";
	private int rank = 0;
	private String transformationName = "";
	private Boolean madeSoMCutoff = true;
	
	
	public static final String PRIORITY_SCORE_PROPERTY = "PriorityScore";
	public static final String TRANSFORMATION_NAME_PROPERTY = "Transformation";
	public static final String PHASE_PROPERTY = "Phase";
	public static final String MADE_SOM_CUTOFF_PROPERTY = "made_som_cutoff";
	
	
	@Override
	public int compareTo(PredictedMolecule otherMolecule) { 		
		// Enables Collections.sort() to sort a list of PredictedMolecules in order of descending priority score.
		
		return this.priorityScore > otherMolecule.priorityScore ? -1 : this.priorityScore < otherMolecule.priorityScore ? 1 : 0;
	} 
	
    @Override
	public boolean equals(Object obj) {  // require InChi and priority score to be equal, and require InChI to be defined (i.e. neither null nor "")
    	
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		PredictedMolecule other = (PredictedMolecule) obj;
		
		if (priorityScore == null) {
			if (other.priorityScore != null)
				return false;
		} else if (!priorityScore.equals(other.priorityScore))
			return false;
		
		if (inchi == null) {  // should never be null, but if it is, it will be added to the set/map no matter what
			return false;
		} else if (inchi.equals("")) {  // if no inchi is specified, the PredictedMolecule will be added to set/map no matter what
			return false;	
	    } else if (!inchi.equals(other.inchi))
			return false;
		return true;
	}
 
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((priorityScore == null) ? 0 : priorityScore.hashCode());
		result = prime * result + ((inchi == null) ? 0 : inchi.hashCode());
		return result;
	}
	
    
    // --- getters and setters ---
	
	public void setPriorityScore(Double priorityScore) {
		this.priorityScore = priorityScore;
	}
	
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}
	
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public void setTransformationName(String name) {
		this.transformationName = name;
	}
	
	public void setMadeSoMCutoff(Boolean madeSoMCutoff) {
		this.madeSoMCutoff = madeSoMCutoff;
	}

	public void setParentInchi(String parentInchi) {
		this.parentInchi = parentInchi;
	}
	
	public void setParentSmiles(String parentSmiles) {
		this.parentSmiles = parentSmiles;
	}
	
	
	public Double getPriorityScore() {
		return this.priorityScore;
	}
	
	public String getParentID() {
		return this.parentID;
	}
	
	public String getParentName() {
		return this.parentName;
	}

	public int getRank() {
		return this.rank;
	}
	
	public String getTransformationName() {
		return this.transformationName;
	}

	public Boolean getMadeSoMCutoff() {
		return madeSoMCutoff;
	}

	public String getParentInchi() {
		return parentInchi;
	}

	public String getParentSmiles() {
		return parentSmiles;
	}

	
}
