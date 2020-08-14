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

package main.java.metaboliteprediction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import main.java.utils.Filenames;
import main.java.utils.Prediction;
import main.java.utils.TestParameters;
import main.java.utils.analysis.RankingRocResult;
import main.java.utils.analysis.Result;
import main.java.utils.molecule.BasicMolecule;


/**
 * Runnable class for the evaluation to use with multithreading.
 * Calls methods in PredictionEvaluator to do the actual evaluation.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class PredictionEvaluatorWorker implements Runnable  {

	private static final String FILE_RESULTS = "/predictor_results/results_";
	private static final String FILE_ENDING = ".txt";

	
	private Prediction prediction;
	private int molId;
	private TestParameters testParameters;
	private Filenames filenames;
	private List<Result> results;
	private List<RankingRocResult> rankingRocResults;
	private Map<BasicMolecule, Set<BasicMolecule>> dataset;
	private PredictionEvaluator evaluator;
	
	
	// constructor
	public PredictionEvaluatorWorker(Entry<Integer, Prediction> entry, TestParameters testParameters, Filenames filenames, 
			List<Result> results, List<RankingRocResult> rankingRocResults, Map<BasicMolecule, Set<BasicMolecule>> dataset) {
		
		this.molId = entry.getKey();
		this.prediction = entry.getValue();
		this.testParameters = testParameters;
		this.filenames = filenames;
		this.results = results;
		this.rankingRocResults = rankingRocResults;
		this.dataset = dataset;
		
		String molFilename = filenames.getPrefix() + FILE_RESULTS + "mol_" + molId + FILE_ENDING;

		
		this.evaluator = new PredictionEvaluator(this.testParameters, this.dataset, this.prediction, this.results, this.rankingRocResults, molFilename);
	}
	
	
	
	@Override
	public void run() {	// do evaluation
				
		evaluator.evaluatePredictions();
	}


}
