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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.java.utils.molecule.BasicMolecule;

/**
 * Helper class to keep track of some information for each parent molecule.
 * Used for evaluation.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Count {

	private static final String NOTE_SCORE_NAN = "Note: Score is NaN for {}";
	private static final String PERCENTAGE_OF_KNOWN_METABOLITES_PREDICTED = "Percentage of known metabolites predicted: {} \n";
	private static final String NUMBER_OF_KNOWN_METABOLITES_PREDICTED = "Number of known metabolites that were predicted: {}";
	private static final String NUMBER_OF_KNOWN_METABOLITES = "number of known metabolites: {}";
	
	private static final Logger logger = LoggerFactory.getLogger(Count.class.getName());

	private int knownFound;
	private int numberOfFalsePositives;
	private int totalPredictions;
	private int bestRank;
	
	public Count(int knownFound, int numberOfFalsePositives, int totalPredictions, int bestRank) {
		this.knownFound = knownFound;
		this.numberOfFalsePositives = numberOfFalsePositives;
		this.totalPredictions = totalPredictions;
		this.bestRank = bestRank;
	}
		
	public int getKnownFound() {
		return knownFound;
	}
	
	public int getNumberOfFalsePositives() {
		return numberOfFalsePositives;
	}
	
	public int getBestRank() {
		return bestRank;
	}
	
	public int getTotalPredictions() {
		return totalPredictions;
	}

	
	/**
	 * Calculates a score for each parent molecule based on how many metabolites were predicted correctly. 
	 * This is NOT the priority score.
	 * 
	 * @param knownMetabolites
	 * @param id
	 * @return
	 */
	public double calculateScore(final Set<BasicMolecule> knownMetabolites, final String id) {
		
		Double score = ((double) knownFound/knownMetabolites.size());
		if (!score.isNaN()) {  // check for NaN because require metabolites to have a certain number of atoms
			logger.debug(NUMBER_OF_KNOWN_METABOLITES, knownMetabolites.size());
			logger.info(NUMBER_OF_KNOWN_METABOLITES_PREDICTED, knownFound);
			logger.info(PERCENTAGE_OF_KNOWN_METABOLITES_PREDICTED, score);
		} else {
			logger.info(NOTE_SCORE_NAN, id);
		}
		return score;
	}

	
}
