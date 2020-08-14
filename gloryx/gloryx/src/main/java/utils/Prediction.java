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

package main.java.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.ParentMolecule;
import main.java.utils.molecule.PredictedMolecule;

/** 
 * This class stores the prediction for each input/parent molecule, including the predicted metabolites, the parent molecule itself, and any errors.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Prediction {
	
	private static final Logger logger = LoggerFactory.getLogger(Prediction.class.getName());

	private ParentMolecule parentMolecule;
	private Set<PredictedMolecule> predictedMetabolites;
	private List<PredictedMolecule> rankedPredictedMetabolites;

	private Boolean somPredicted = false;
	private Boolean fameModelFailed = false;
	
	private Set<Errors> errors = new HashSet<>();
	
	
	public Prediction(ParentMolecule parentMolecule, Set<PredictedMolecule> predictedMetabolites) {
		this.parentMolecule = parentMolecule;
		this.predictedMetabolites = predictedMetabolites;
	}
	
	// constructor for SyGMaComparer
	public Prediction(ParentMolecule parent) {
		this.parentMolecule = parent;
		this.predictedMetabolites = new HashSet<>();
	}
	
	
	// keep track of SoM prediction things
	public Boolean getSomPredicted() {
		return somPredicted;
	}
	public void setSomPredicted(Boolean somPredicted) {
		this.somPredicted = somPredicted;
	}
	

	// keep track of errors
	public Boolean fameModelFailed() {
		return fameModelFailed;
	}
	public void setFameModelFailed(Boolean predictionFailed) {
		this.fameModelFailed = predictionFailed;
	}
	public void addError(Errors error) {
		this.errors.add(error);
	}
	public Set<Errors> getErrors() {
		return this.errors;
	}
	
	
	public ParentMolecule getParentMolecule() {
		return parentMolecule;
	}
	public void setParentMolecule(ParentMolecule parentMolecule) {
		this.parentMolecule = parentMolecule;
	}
	public Set<PredictedMolecule> getPredictedMetabolites() {
		return predictedMetabolites;
	}
	public void setPredictedMetabolites(Set<PredictedMolecule> predictedMetabolites) {
		this.predictedMetabolites = predictedMetabolites;
	}
	public void setRankedPredictedMetabolites(List<PredictedMolecule> predictedMetabolites) {
		this.rankedPredictedMetabolites = predictedMetabolites;
	}
	public List<PredictedMolecule> getRankedPredictedMetabolites() {
		return rankedPredictedMetabolites;
	}
	
	// adders
	
	/**
	 * Used to evaluate SyGMa's predictions.
	 * 
	 * @param sygmaPredictedMetabolite
	 */
	public void addPredictedMetabolite(PredictedMolecule sygmaPredictedMetabolite) {
		
		// double-check by hand to avoid duplicates, just in case SyGMa did not already do this properly
		Boolean found = false;
		for (PredictedMolecule m : this.predictedMetabolites) {
			if (m.getInchi() == sygmaPredictedMetabolite.getInchi()) {
				found = true;
				break;
			}
		}
		Boolean added = this.predictedMetabolites.add(sygmaPredictedMetabolite);
		
		if (added && found) {
			logger.error("added a duplicate predicted metabolite!!!! fix this! exiting");
			logger.error("parent: {}", sygmaPredictedMetabolite.getParentSmiles());
			System.exit(1);
		}
	}
	
}
