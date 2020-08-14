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

package main.java.utils.analysis;


/** 
 * Stores the results of the analysis of the prediction for a given input molecule.
 * Used for evaluation.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Result {
	
	private double recoveryScore;
	private int numberOfKnownMetabolites;
	private int numberOfTruePositives;
	private int numberOfPredictedMetabolites;
	private int numberOfFalsePositives;
	private int bestRankOfKnownMetabolite;
	private String molId;
	private Boolean somPredicted;
	private Boolean predictionFailed;
	private Double molecularWeight;
	private int heavyAtomCount;
	private Double maxSomProbability;
	private Double medianSomProbability;
	private Double avgOfTop3SomProbabilities;
	
	
	// --- constructors ---
	
	public Result(String molId, int numberOfKnownMetabolites, Boolean predictionFailed) {
		
		this.molId = molId;
		this.numberOfKnownMetabolites = numberOfKnownMetabolites;
		this.predictionFailed = predictionFailed;
		this.recoveryScore = 0;
		this.numberOfTruePositives = 0;
		this.numberOfPredictedMetabolites = 0;
		this.numberOfFalsePositives = 0;
		
	}
	
	public Result(String molId, double recoveryScore, int numberOfKnownMetabolites, int numberOfTruePositives, int numberOfPredictedMetabolites,  
			int numberOfFalsePositives, Boolean somPredicted, int bestRankOfKnownMetabolite) {
		this.molId = molId;
		this.recoveryScore = recoveryScore;
		this.numberOfKnownMetabolites = numberOfKnownMetabolites;
		this.numberOfTruePositives = numberOfTruePositives;
		this.numberOfPredictedMetabolites = numberOfPredictedMetabolites;
		this.numberOfFalsePositives = numberOfFalsePositives;
		this.somPredicted = somPredicted;
		this.bestRankOfKnownMetabolite = bestRankOfKnownMetabolite;
	}
	
	public Result(String molId, double recoveryScore, int numberOfKnownMetabolites, int numberOfTruePositives, int numberOfPredictedMetabolites,  
			int numberOfFalsePositives, Boolean somPredicted, int bestRankOfKnownMetabolite, Double molecularWeight, int heavyAtomCount) {
		this.molId = molId;
		this.recoveryScore = recoveryScore;
		this.numberOfKnownMetabolites = numberOfKnownMetabolites;
		this.numberOfTruePositives = numberOfTruePositives;
		this.numberOfPredictedMetabolites = numberOfPredictedMetabolites;
		this.numberOfFalsePositives = numberOfFalsePositives;
		this.somPredicted = somPredicted;
		this.bestRankOfKnownMetabolite = bestRankOfKnownMetabolite;
		
		this.molecularWeight = molecularWeight;
		this.heavyAtomCount = heavyAtomCount;
	}
	
	public Result(String molId, double recoveryScore, int numberOfKnownMetabolites, int numberOfTruePositives, int numberOfPredictedMetabolites,  
			int numberOfFalsePositives, Boolean somPredicted, int bestRankOfKnownMetabolite, 
			Double molecularWeight, int heavyAtomCount, Double maxSomProbability, Double medianSomProbability, Double avgOfTop3SomProbabilities) {
		this.molId = molId;
		this.recoveryScore = recoveryScore;
		this.numberOfKnownMetabolites = numberOfKnownMetabolites;
		this.numberOfTruePositives = numberOfTruePositives;
		this.numberOfPredictedMetabolites = numberOfPredictedMetabolites;
		this.numberOfFalsePositives = numberOfFalsePositives;
		this.somPredicted = somPredicted;
		this.bestRankOfKnownMetabolite = bestRankOfKnownMetabolite;
		
		this.molecularWeight = molecularWeight;
		this.heavyAtomCount = heavyAtomCount;
		this.maxSomProbability = maxSomProbability;
		this.medianSomProbability = medianSomProbability;
		this.avgOfTop3SomProbabilities = avgOfTop3SomProbabilities;
	}
	
	
	// --- getters ---
	
	public String getMolId() {
		return molId;
	}
	
	public double getRecoveryScore() {
		return recoveryScore;
	}
	
	public int getNumberOfMetabolites() {
		return numberOfKnownMetabolites;
	}
	
	public int getNumberOfFalsePositives() {
		return numberOfFalsePositives;
	}
	
	public int getBestRankOfKnownMetabolite() {
		return bestRankOfKnownMetabolite;
	}

	public int getNumberOfPredictedMetabolites() {
		return numberOfPredictedMetabolites;
	}

	public int getNumberOfTruePositives() {
		return numberOfTruePositives;
	}

	public Boolean wasSoMPredicted() {
		return somPredicted;
	}
	
	public Boolean didPredictionFail() {
		return predictionFailed;
	}
	
	public Double getMolecularWeight() {
		return molecularWeight;
	}
	
	public int getHeavyAtomCount() {
		return heavyAtomCount;
	}
	
	public Double getMaxSomProbability() {
		return maxSomProbability;
	}
	
	public Double getMedianSomProbability() {
		return medianSomProbability;
	}
	
	public Double getAvgOfTop3SomProbabilities() {
		return avgOfTop3SomProbabilities;
	}
	
	
}
