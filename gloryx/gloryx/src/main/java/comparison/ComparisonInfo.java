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

package main.java.comparison;

import java.util.Set;

import main.java.utils.molecule.PredictedMolecule;

/**
 * Comparison helper class to hold the relevant information.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class ComparisonInfo {

	private Set<PredictedMolecule> predictedMetabolites;
	private int numPredictedMetabolites;
	private String filename;

	public ComparisonInfo(int numPredictedMetabolites, String filename, Set<PredictedMolecule> predictedMetabolites) {
		this.numPredictedMetabolites = numPredictedMetabolites;
		this.filename = filename;
		this.predictedMetabolites = predictedMetabolites;
	}

	public int getNumPredictedMetabolites() { 
		return this.numPredictedMetabolites; 
	}
	public String getFilename() { 
		return this.filename; 
	}
	
	public Set<PredictedMolecule> getPredictedMetabolites() {
		return this.predictedMetabolites;
	}


}
