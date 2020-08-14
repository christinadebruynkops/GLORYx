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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.zbh.fame.fame3.globals.Globals;

import main.java.depiction.CreateResultsHTML;
import main.java.datasets.newtestdata.TestDatasetLoader;
import main.java.sompredictor.SoMPredictor;
import main.java.utils.Calculations;
import main.java.utils.Errors;
import main.java.utils.Filenames;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.Phase;
import main.java.utils.Prediction;
import main.java.utils.molecule.PredictedMolecule;
import main.java.utils.analysis.DatasetWriter;
import main.java.utils.analysis.JsonFileWriter;
import main.java.metaboliteprediction.PredictionHandler;
import main.java.utils.analysis.RankingRocResult;
import main.java.utils.analysis.Result;
import main.java.utils.analysis.ResultsWriter;
import main.java.utils.TestParameters;
import main.java.utils.TestParameters.Reference;
import main.java.data.DatasetLoader;

/**
 * Sets up the metabolite prediction.
 * Sets up multithreading for the prediction of metabolites as well as for evaluation (if in evaluation mode).
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MetabolitePredictor {
	
	private static final String GLOBAL_PARAMETERS_SHOULD_NOT_BE_NULL = "Global parameters should not be null";
	private static final String ERROR_SHUTTING_DOWN_EXECUTOR = "Error shutting down executor.";
	private static final String AVERAGE_NUMBER_OF_METABOLITES_IN_DATASET = "Average number of metabolites in dataset: {}";
	private static final String ERROR_WRITING_SD_FILE_OF_PREDICTED_METABOLITES = "Error writing SD file of predicted metabolites.";
	
	private static final String FILE_ALL_RESULTS = "results.txt";
	private static final String FILE_ALL_RESULTS_CSV = "results.csv";
	private static final String FILE_ALL_ROC_RESULTS_SCORE = "ROCresults_score.txt";
	private static final String FILE_ALL_ROC_RESULTS_RANK = "ROCresults_rank.txt";

	private static final String AVERAGE_SCORE = "Average score: {}";
	private static final String LIST_OF_SCORES = "List of scores: {}";
	
	private static final String FAME3_PHASEI_MODEL = "P1";
	private static final String FAME3_PHASEII_MODEL = "P2";
	private static final String FAME3_ALL_MODEL = "P1+P2"; 
	
	private static final Logger logger = LoggerFactory.getLogger(MetabolitePredictor.class.getName());
	
	private Map<BasicMolecule, Set<BasicMolecule>> combinedDataset;
	private Map<BasicMolecule, Set<BasicMolecule>> drugbankData;
	private Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData;
	
	private Boolean writeDatasetParentsToFileOnly = false; // hard-coded evaluation option
	private Boolean writeDatasetAsSdf = false;
	private Boolean writeDatasetAsJson = true;

	private String evalResultsDir = "/predictor_results/";
	
	public MetabolitePredictor() {
		
		drugbankData = new LinkedHashMap<>(); // prep for DrugBank as test set
		combinedDataset = new LinkedHashMap<>();
	}

	
	public void predictMetabolites(TestParameters testParameters, Filenames filenames, String prefix, String timeStamp) {
		
		List<String> inputSmiles = new ArrayList<>();
		List<String> inputNames = new ArrayList<>();  // put e.g. DrugBank IDs in here
		
		// get test dataset
		TestDatasetLoader loader = new TestDatasetLoader();
		testMetabolismData = new HashMap<>();
		if (!testParameters.isUserVersion()) {
			testMetabolismData = loader.loadData(filenames);
		}
		
		
//		// --- for permutation test ---
//		TestDatasetLoader loader = new TestDatasetLoader();
//		Map<BasicMolecule, Set<BasicMolecule>> testData = new HashMap<>();
//		if (!testParameters.isUserVersion()) {
//			testData = loader.loadData(filenames);
//		}

		
//		Set<Entry<BasicMolecule, Set<BasicMolecule>>> ent = testData.entrySet();
//		for (Map.Entry<BasicMolecule, Set<BasicMolecule>> mapEntry : ent) {
//			if (Integer.decode(mapEntry.getKey().getId()) == 16) {
//				BasicMolecule parent = mapEntry.getKey();
//				logger.info("parent 16 is {}", parent.getName());
//				logger.info("parent 16 has {} known first generation metabolites.", mapEntry.getValue().size());
//			}
//		}
//		System.exit(0);
		
		
		
//		evalResultsDir = evalResultsDir + "/subset_1/";
//		for (int perm = 1; perm <= testData.size(); perm++) {
//
//				
//			// create a shallow copy of the map
//			testMetabolismData = new HashMap<>();
//			Set<Entry<BasicMolecule, Set<BasicMolecule>>> entries = testData.entrySet();
//			for (Map.Entry<BasicMolecule, Set<BasicMolecule>> mapEntry : entries) {
//				if (Integer.decode(mapEntry.getKey().getId()) != perm) {
//					logger.info("parent {} is {}", mapEntry.getKey().getId(), mapEntry.getKey().getName());
//					testMetabolismData.put(mapEntry.getKey(), mapEntry.getValue());
//				}
//			}
//			logger.info("size of test dataset: {}", testMetabolismData.size());
//			
//			evalResultsDir = evalResultsDir.replaceAll("_[0-9]+", "_" + Integer.toString(perm));
//			logger.info("eval results dir: {}", evalResultsDir);
//			File evalResultsDirDir = new File(prefix + evalResultsDir);
//			if (!evalResultsDirDir.exists()) {
//				evalResultsDirDir.mkdir();
//				logger.info("making eval results dir");
//			}
//			
//			inputSmiles = new ArrayList<>();
//			inputNames = new ArrayList<>();
//			testParameters.setPhase(Phase.PHASES_1_AND_2);
//
//			
//			
//			// -------
			
		logger.info("Making predictions for {}", testParameters.getPhase().getPhaseName());
		
		List<String> fameModels = selectRelevantFameModels(testParameters);
		
		// get input smiles and the metabolism information from whichever database is in use (see testParameters)
		DatasetLoader dl = new DatasetLoader();
		dl.getInputSmilesAndReferenceMetabolismData(testParameters, filenames, drugbankData, combinedDataset, 
				testMetabolismData, inputSmiles, inputNames);	
				
		//transform and compare predictions to known metabolites for each parent compound
		Map<Integer, Prediction> allPredictedMolecules = Collections.synchronizedMap(new HashMap<>()); // map ID (input number) to predictions, where predictions includes info on parent
		List<Long> runTimes = Collections.synchronizedList(new ArrayList<>());
		
		for (String fameModelName : fameModels) {
			
			if ( (testParameters.predictAllMetabolism() && !testParameters.useCombinedP1P2()) || 
					(!testParameters.predictAllMetabolism() && !testParameters.predictPhase1() && !TestParameters.useCombinedPhase2() ) ) {
				
				setAppropriatePhase(testParameters, fameModelName);
			}
	
			if (writeDatasetParentsToFileOnly && writeDatasetAsJson) {
				JsonFileWriter.writeDatasetToJSONFile(combinedDataset, filenames);
			}

			logger.info("Using FAME 3 model: {}", fameModelName);
						
			// Set globals and load FAME 3 model. Want to do this only once due to memory concerns.
			Globals fameParameters = SoMPredictor.createGlobals(fameModelName);
			Assert.notNull(fameParameters, GLOBAL_PARAMETERS_SHOULD_NOT_BE_NULL);
			
			// write dataset to file and exit
			if (writeDatasetParentsToFileOnly && writeDatasetAsSdf) {
				// write all dataset parent compounds to SD file
				DatasetWriter.writeDatasetParentCompoundsToSdfAndExit(inputSmiles);
			} else if (writeDatasetParentsToFileOnly) {
				// write smiles
				DatasetWriter.writeDatasetSmilesToFileAndExit(inputSmiles);
			}
			

			if (inputSmiles.isEmpty()) {
				logger.error("No input SMILES.");
				Errors.createErrorHtmlAndExit(filenames, Errors.NO_VALID_INPUT);
			}
			
			
			ExecutorService executor = Executors.newFixedThreadPool(testParameters.getNumThreads());
			int counter = 1;
			logger.info("number of input smiles to start with: {}", inputSmiles.size());
			


			// make the predictions
			for (String singleInputSmiles : inputSmiles) {
				
				String singleInputName = getSingleInputName(inputNames, counter); 

//					if (counter > 2) {
//						break;
//					}
		
				logger.info("Predicting for {}\t{}", singleInputName, singleInputSmiles);
				
				// comment this block out if just writing dataset parent compounds to file as SMILES
				makePredictions(testParameters, fameParameters, allPredictedMolecules, runTimes, executor, counter, singleInputSmiles, singleInputName, false);

				counter ++;
			}

			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				logger.error(ERROR_SHUTTING_DOWN_EXECUTOR);
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}

		} // for loop for different FAME models
		
		// rerun predictions with P2 model if any of the individual phase 2 models failed
		if ( ( testParameters.predictAllMetabolism() && !testParameters.useCombinedP1P2() 
				&& testParameters.getPhase() != Phase.PHASE_1 && !TestParameters.useCombinedPhase2() ) // predicting P1+P2 and using separate models 
				|| ( !testParameters.predictAllMetabolism() && testParameters.getPhase() != Phase.PHASE_1 && !TestParameters.useCombinedPhase2() )  // predicting P2 and using separate models
				) {
			
			logger.info("Redoing predictions for molecules for which one or more individual phase 2 models failed");
			
			ExecutorService executor = Executors.newFixedThreadPool(testParameters.getNumThreads());
			
			String fameModelName = "P2";
			testParameters.setPhase(Phase.PHASE_2); 
			
			// Set globals and load FAME 3 model. Want to do this only once due to memory concerns.
			Globals fameParameters = SoMPredictor.createGlobals(fameModelName);
			Assert.notNull(fameParameters, GLOBAL_PARAMETERS_SHOULD_NOT_BE_NULL);
			
			int failedCounter = 0;
			for (Entry<Integer, Prediction> entry : allPredictedMolecules.entrySet()) {
				Prediction p = entry.getValue();
				
				if (p.fameModelFailed()) {
					failedCounter ++;
					
					int molNumber = entry.getKey();

					if (logger.isInfoEnabled()) {
						logger.info("Rerunning molecule {} using FAME 3 model {} and phase {}", molNumber, fameModelName, testParameters.getPhase().name());
					}
					
					String singleInputName = getSingleInputName(inputNames, molNumber); 
					String singleInputSmiles = inputSmiles.get(molNumber - 1);
					String inchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(singleInputSmiles);
					
					logger.debug("smiles in set: {}", p.getParentMolecule().getSmiles());
					logger.debug("new input smiles: {}", singleInputSmiles);
					
					Assert.isTrue(inchi.equals(p.getParentMolecule().getInchi()), "Error - Did not find same input molecule when trying predictions with generalized P2 model!");
					
					makePredictions(testParameters, fameParameters, allPredictedMolecules, runTimes, executor, molNumber, singleInputSmiles, singleInputName, true);
				}
			}
			logger.info("Number of molecules for which one or more individual phase 2 models failed and that had to be rerun: {}", failedCounter); // approx 64 in reference dataset
			
		executor.shutdown();
			try {
				if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				logger.error(ERROR_SHUTTING_DOWN_EXECUTOR);
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		for (Entry<Integer, Prediction> x : allPredictedMolecules.entrySet()) {
			if (logger.isDebugEnabled()) {
				if (x.getKey() != null && x.getValue() != null && x.getValue().getPredictedMetabolites() != null) {
					logger.debug("Number of predicted metabolites for molecule {}: {}", x.getKey(), x.getValue().getPredictedMetabolites().size());
				}
			}
		}

		
		// evaluation
		evaluate(testParameters, filenames, prefix, timeStamp, inputSmiles, allPredictedMolecules, runTimes);
			
//		} // end permutation loop
	}


	private void evaluate(TestParameters testParameters, Filenames filenames, String prefix, String timeStamp,
			List<String> inputSmiles, Map<Integer, Prediction> allPredictedMolecules, List<Long> runTimes) {
		
		if (testParameters.isUserVersion()) { // process results, combining if necessary (e.g. if use multiple phase 2 individual models
			
			combineAndWritePredictionsToFiles(testParameters, filenames, timeStamp, inputSmiles, allPredictedMolecules);
		
		} else {  // do evaluation on dataset- ranking and duplicate removal happen in PredictionEvaluator

			List<Result> results = Collections.synchronizedList(new ArrayList<>());
			List<RankingRocResult> rankingRocResults = Collections.synchronizedList(new ArrayList<>());
			
			
			ExecutorService executor = Executors.newFixedThreadPool(testParameters.getNumThreads());
			
			logger.debug("size of allPredictedMolecules: {}", allPredictedMolecules.size());
			
			for (Entry<Integer, Prediction> entry : allPredictedMolecules.entrySet()) {
				
				// start executor workers
				if (testParameters.getReference() == TestParameters.Reference.DRUGBANK_PLUS_METXBIODB) {
					Runnable worker = new PredictionEvaluatorWorker(entry, testParameters, filenames, results, rankingRocResults, combinedDataset);
					executor.execute(worker);
				} else if (testParameters.getReference() == TestParameters.Reference.DRUGBANK) {
					Runnable worker = new PredictionEvaluatorWorker(entry, testParameters, filenames, results, rankingRocResults, drugbankData);
					executor.execute(worker);
				} else if (testParameters.getReference() == TestParameters.Reference.TEST_DATASET) {
					Runnable worker = new PredictionEvaluatorWorker(entry, testParameters, filenames, results, rankingRocResults, testMetabolismData);
					executor.execute(worker);
				}
			}
			
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				logger.error(ERROR_SHUTTING_DOWN_EXECUTOR);
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
			
			
			// write results from all parent molecules to a single file
			ResultsWriter.writeResultsToTxtFile(prefix + evalResultsDir, FILE_ALL_RESULTS, results);
			ResultsWriter.writeResultsToCsvFile(prefix + evalResultsDir, FILE_ALL_RESULTS_CSV, results);
			ResultsWriter.writeScoreRocResultsToFile(prefix + evalResultsDir, FILE_ALL_ROC_RESULTS_SCORE, rankingRocResults);
			ResultsWriter.writeRankRocResultsToFile(prefix + evalResultsDir, FILE_ALL_ROC_RESULTS_RANK, rankingRocResults);
			
			
			// this conversion is needed to calculate the average
			List<Double> scores = getScoresAsList(results);
			
			Double avg = Calculations.calculateAverage(scores);
			if (logger.isInfoEnabled()) {
				logger.info(LIST_OF_SCORES, scores.toString());
				logger.info(AVERAGE_SCORE, avg);
			}
			

			logger.debug("size of Results: {}", results.size());
			if (testParameters.inputIsDatabase()) {
				if (testParameters.getReference() == Reference.DRUGBANK_PLUS_METXBIODB) {
					logger.debug("size of dataset: {}", combinedDataset.size());
					Assert.state(combinedDataset.size() == results.size(), "Error: the number of entries in Results should be the same as the number of parent compounds in the dataset");
				} else if (testParameters.getReference() == Reference.TEST_DATASET) {
					logger.debug("size of dataset: {}", testMetabolismData.size());
//					Assert.state(testMetabolismData.size() == results.size(), "Error: the number of entries in Results should be the same as the number of parent compounds in the dataset");
				}
			}

			outputPrecisionRecall(results);
			
			
			// TODO this doesn't work
			if (testParameters.getReference() == TestParameters.Reference.TEST_DATASET) { 
				logger.info("Average time per parent molecule: {} s", (Double) Calculations.calculateAverage(runTimes)/1000.0 );
			}	
			
		}
	}


	private List<String> selectRelevantFameModels(TestParameters testParameters) {
		List<String> fameModels = new ArrayList<>();

		switch (testParameters.getPhase()) {
		
		case PHASE_1:
			fameModels.add(FAME3_PHASEI_MODEL);
			break;
		case PHASE_2:
			if (TestParameters.useCombinedPhase2()) {
				fameModels.add(FAME3_PHASEII_MODEL);
			} else {
				if (TestParameters.useSygmaRulesOnly()) { // for evaluation only
					//In this case, the GST model is not used.
					fameModels.add("UGT");
					fameModels.add("SULT");
					fameModels.add("MT");
					fameModels.add("NAT");
					fameModels.add("P2");
				} else {
					fameModels.add("UGT");
					fameModels.add("GST");
					fameModels.add("SULT");
					fameModels.add("MT");
					fameModels.add("NAT");
					fameModels.add("P2");
				}
			}
			break;
			
		case PHASES_1_AND_2:
			if (testParameters.useCombinedP1P2()) {
				fameModels.add(FAME3_ALL_MODEL);
			} else {
				fameModels.add("P1");
				fameModels.add("UGT");
				fameModels.add("GST");
				fameModels.add("SULT");
				fameModels.add("MT");
				fameModels.add("NAT");
				fameModels.add("P2");
			}
			break;
			
		case UGT:
			fameModels.add("UGT");
			break;
		case GST:
			fameModels.add("GST");
			break;
		case SULT:
			fameModels.add("SULT");
			break;
		case MT:
			fameModels.add("MT");
			break;
		case NAT:
			fameModels.add("NAT");
			break;
			
		default :
			logger.error("phase specified for which there is no FAME model");
			break;
		}
		return fameModels;
	}


	private String getSingleInputName(List<String> inputNames, int counter) {
		String singleInputName = "";
		if (!inputNames.isEmpty()) { 
			singleInputName = inputNames.get(counter - 1);
		}
		return singleInputName;
	}


	private void combineAndWritePredictionsToFiles(TestParameters testParameters, Filenames filenames, String timeStamp,
			List<String> inputSmiles, Map<Integer, Prediction> allPredictedMolecules) {
		
		ResultsWriter rw = new ResultsWriter(testParameters, filenames);
		
		logger.debug("combining and preparing to write");
		
		Iterator<Entry<Integer, Prediction>> it = allPredictedMolecules.entrySet().iterator();
		while (it.hasNext()) {
			
			Entry<Integer, Prediction> entry = it.next();
			Prediction predictions = entry.getValue();
			int molNum = entry.getKey();
			
			if ( predictions.getPredictedMetabolites() == null && !predictions.getErrors().isEmpty()) {
				logger.info("No predictions made for molecule {} due to error.", predictions.getParentMolecule().getSmiles());
				continue; // no need to write an empty sdf file
			}
			
			combineAndRankPredictions(testParameters, predictions);
			
//			// write predictions for individual molecule to sdf
//			String outputSdFilename = filenames.getUserOutputDir() + filenames.getIndividualResultsDir() + "mol_" + molNum + "/" + filenames.getIndividualOutputSDFilename();
//			rw.writePredictedMetabolitesToSdf(outputSdFilename, predictions);

		}

		// write all predictions to a single sdf, unless there are more than 1000 input molecules, in which write a separate output file for each batch of 1000 input molecules
		int numWithPredictions = -1;
		if (allPredictedMolecules.size() <= testParameters.getBatchSize()) { 
			numWithPredictions = rw.writePredictionsToSdf(filenames.getOutputSDFilename(), allPredictedMolecules);
		} else {
			try {
				numWithPredictions = rw.writeBatchedPredictionsToSdf(filenames.getOutputSDFilename(), allPredictedMolecules);
			} catch (IOException e) {
				logger.error(ERROR_WRITING_SD_FILE_OF_PREDICTED_METABOLITES, e);
			}
		}
		
		logger.info("Number of input molecules for which metabolite(s) could be predicted: {}", numWithPredictions);
		
		if (testParameters.isWebVersion()) {
			CreateResultsHTML htmlWriter = new CreateResultsHTML(allPredictedMolecules, numWithPredictions, inputSmiles.size(), filenames, timeStamp, testParameters);
			htmlWriter.writeHTML();
		}

	}
	

	private List<PredictedMolecule> combineAndRankPredictedMetabolites(TestParameters testParameters, Prediction predictions) {
		
		List<PredictedMolecule> rankedPredictions;
		PredictionHandler ph = new PredictionHandler();
		
		if (!testParameters.useCombinedPhase2()) {
			// combine predictions for each parent mol
			
			Set<PredictedMolecule> cleanPredictions = new HashSet<>();
			
			for (PredictedMolecule m : predictions.getPredictedMetabolites()) {
				
				ph.addPredictedMoleculeIfNotInSetOrHasHigherScore(cleanPredictions, m);	
			}
			rankedPredictions = ph.rankPredictedMetabolites(cleanPredictions); 
			
		} else {
			rankedPredictions = ph.rankPredictedMetabolites(predictions.getPredictedMetabolites()); 
		}
		
		Assert.notNull(rankedPredictions, "Ranked predictions should not be null!");
		return rankedPredictions;
	}
	
	
	private void combineAndRankPredictions(TestParameters testParameters, Prediction predictions) {
		
		List<PredictedMolecule> rankedPredictions;
		PredictionHandler ph = new PredictionHandler();
		
		if (!testParameters.useCombinedPhase2()) {
			// combine predictions for each parent mol
			
			Set<PredictedMolecule> cleanPredictions = new HashSet<>();
			
			for (PredictedMolecule m : predictions.getPredictedMetabolites()) {
				
				ph.addPredictedMoleculeIfNotInSetOrHasHigherScore(cleanPredictions, m);	
			}
			rankedPredictions = ph.rankPredictedMetabolites(cleanPredictions); 
			
		} else {
			rankedPredictions = ph.rankPredictedMetabolites(predictions.getPredictedMetabolites()); 
		}
		
		Assert.notNull(rankedPredictions, "Ranked predictions should not be null!");
		
		predictions.setRankedPredictedMetabolites(rankedPredictions); 
		
		return;
	}


	private void setAppropriatePhase(TestParameters testParameters, String fameModelName) {
		// make sure individual models are being used before calling this method
		
		switch (fameModelName) {
		
		case "UGT":
			testParameters.setPhase(Phase.UGT);
			break;
		case "GST":
			testParameters.setPhase(Phase.GST);
			break;
		case "SULT":
			testParameters.setPhase(Phase.SULT);
			break;
		case "MT":
			testParameters.setPhase(Phase.MT);
			break;
		case "NAT":
			testParameters.setPhase(Phase.NAT);
			break;
		case "P2": 
			testParameters.setPhase(Phase.OTHER_PHASE2);
			break;
		case "P1": 
			testParameters.setPhase(Phase.PHASE_1);
			break;
		default :
			logger.error("error setting phase based on FAME 3 model name");
			break;
		}
	}


	private void makePredictions(TestParameters testParameters, Globals fameParameters, Map<Integer, Prediction> allPredictedMolecules,
			List<Long> runTimes, ExecutorService executor, int counter, String singleInputSmiles, String singleInputName, Boolean rerunning) {
		
		if (testParameters.isUserVersion()) {
			
			Runnable worker = new MetabolitePredictorWorker(singleInputSmiles, allPredictedMolecules, testParameters, fameParameters, 
					counter, singleInputName, runTimes, rerunning);
			
			executor.execute(worker);

			
		} else {
			
			Runnable worker = new MetabolitePredictorWorker(singleInputSmiles, allPredictedMolecules, 
					testParameters, fameParameters, counter, singleInputName, rerunning);
			executor.execute(worker);
		}
	}


	public static void outputPrecisionRecall(List<Result> results) {
		
		List<Integer> numberMetabolites = getNumberOfMetabolitesAsList(results);
		logger.info(AVERAGE_NUMBER_OF_METABOLITES_IN_DATASET, Calculations.calculateAverage(numberMetabolites));
		
		// Output precision and recall
		
		int totalTP = 0;
		int totalPredicted = 0;
		int totalNoSoM = 0;
		int predictedCounter = 0;
		int fameFailCounter = 0;
		for (Result r : results) {
			// tally number of true positives, number of predicted metabolites and number of molecules for which no SoM was predicted
			totalTP += r.getNumberOfTruePositives();
			totalPredicted += r.getNumberOfPredictedMetabolites();
			
			if (r.wasSoMPredicted() != null && !r.wasSoMPredicted()) {
				totalNoSoM += 1;
			}
			
			if (r.didPredictionFail() != null && r.didPredictionFail()) {
				logger.debug("fame failed for molecule {}", r.getMolId());
				fameFailCounter ++;
			}
			
			if (r.getNumberOfPredictedMetabolites() > 0) {
				predictedCounter ++;
			}
		}
		
		int totalKnown = Calculations.calculateSum(numberMetabolites);
		
		logger.info("number of input molecules for which metabolite(s) could be predicted: {} of {}", predictedCounter, results.size());

		logger.info("total number of known metabolites: {}", totalKnown);
		logger.info("total number of true positives: {}", totalTP);
		logger.info("total number of predicted metabolites: {}", totalPredicted);
		
		logger.info("number of parent compounds FAME 3 could not handle or that didn't pass the filter: {}", fameFailCounter);
		logger.info("number of parent compounds for which no SoM was predicted: {}", totalNoSoM);
		
		if (totalPredicted == 0) {
			logger.error("The total number of predicted metabolites was 0! There is probably an error, unless there was only one input molecule.");
		}
		Double precision = (double) totalTP/ (double) totalPredicted;  
		Double recall = (double) totalTP/ (double) totalKnown;
		
		logger.info("Precision: {}", precision);
		logger.info("Recall: {}", recall);
	}


	public static List<Integer> getNumberOfMetabolitesAsList(List<Result> results) {
		List<Integer> numberMetabolites = new ArrayList<>(); 
		for (Result result : results) {
			int numMet = result.getNumberOfMetabolites();
			numberMetabolites.add(numMet);
		}
		return numberMetabolites;
	}


	private List<Double> getScoresAsList(List<Result> results) {
		List<Double> scores = new ArrayList<>();
		for (Result result : results) {
			double score = result.getRecoveryScore();
			scores.add(score);
		}
		return scores;
	}



}
