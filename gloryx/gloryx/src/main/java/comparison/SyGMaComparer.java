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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import main.java.datasets.drugbankdata.MetabolismDataLoader;
import main.java.metaboliteprediction.MetabolitePredictor;
import main.java.metaboliteprediction.PredictionEvaluator;
import main.java.datasets.newtestdata.TestDatasetLoader;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.Count;
import main.java.data.DatasetLoader;
import main.java.data.DatasetManipulator;
import main.java.utils.Filenames;
import main.java.utils.Phase;
import main.java.utils.Prediction;
import main.java.utils.analysis.GenericOverlapCalculator;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.ParentMolecule;
import main.java.utils.molecule.PredictedMolecule;
import main.java.utils.analysis.RankingRocResult;
import main.java.utils.analysis.Result;
import main.java.utils.analysis.ResultsWriter;
import main.java.utils.TestParameters;

/**
 * Evaluation of SyGMa's predictions, including optionally a direct metabolite by metabolite comparison to GLORYx's predictions.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class SyGMaComparer {
	

	private static final String TOTAL_NUMBER_OF_METABOLITES_PREDICTED = "Total number of metabolites predicted: {}";
	private static final String ERROR_CLOSING_CSV_FILE = "Error closing CSV file {}";
	private static final String GETTING_ALL_KNOWN_METABOLITES_FROM_DRUGBANK_MESSAGE = "getting all known metabolites from DrugBank...";
	private static final String GETTING_ALL_KNOWN_METABOLITES_FROM_REFDATA_MESSAGE = "getting all known metabolites from reference dataset...";
	private static final String GETTING_ALL_KNOWN_METABOLITES_FROM_TESTDATASET_MESSAGE = "getting all known metabolites from manually curated test dataset...";
	private static final String FINISHED_REMOVING_COMPOUNDS_MESSAGE = "...finished removing compounds from DrugBank dataset. New size of DrugBank data: {}";
	private static final String GETTING_NEW_TEST_DATASET_MESSAGE = "getting new test dataset...";
	private static final String GOT_DRUGBANK_METABOLITES_MESSAGE = "got all known metabolites from DrugBank (for {} parents)";
	private static final String CHECKING_OVERLAP_MESSAGE = "checking overlap between new test dataset and DrugBank data...";
	private static final String NUMBER_OF_PREDICTIONS_FOUND_MESSAGE = "Found SyGMa predictions for {} parent molecules.";
	private static final String ERROR_READING_FILE_MESSAGE = "Error reading in SyGMa prediction data from file. Terminating program.";
	private static final String INPUT_FILE_MESSAGE = "Input file of SyGMa predictions: {}";
	
	private static final String HEADER_METABOLITE_SCORE = "metabolite score";
	private static final String HEADER_METABOLITE_SMILES = "metabolite SMILES";
	private static final String HEADER_PARENT_SMILES = "parent SMILES";
	private static final String ERROR_READING_CSV_FILE = "Error reading CSV file {}";
	private static final String DRUGBANK_PARAMETER = "drugbank";
	private static final String TESTDATASET_PARAMETER = "testdataset";
	private static final String REFERENCEDATASET_PARAMETER = "referencedataset";

	private static final String PREDICTIONS_FILENAME = "sygma_predictions_"; // or referencedataset
	private static final String PREDICTIONS_FILENAME_ENDING = "_20200409.txt"; // 
	private static final String ROCRESULTS_SCORE_FILENAME = "ROCresults_score.txt";
	private static final String ROCRESULTS_RANK_FILENAME = "ROCresults_rank.txt";
	private static final String RESULTS_FILENAME = "results.txt";

	private static final Logger logger = LoggerFactory.getLogger(SyGMaComparer.class.getName());

	
	public static void main(String[] args) {

		// CHANGE AS NEEDED
		Phase phase = Phase.PHASES_1_AND_2; // use either phase 1 or phase 2 or PHASES_1_AND_2
		Boolean compareDirectlyToGloryxOnly = false; // TODO directories are hard-coded below
		
		
		
		String sygmaSpecificPrefix = args[args.length-1];
		if (!sygmaSpecificPrefix.endsWith("/")) {
			sygmaSpecificPrefix = sygmaSpecificPrefix + "/";
		}
		String phaseName = null;
		if (phase == Phase.PHASES_1_AND_2) {
			phaseName = "p1p2";
		} else if (phase == Phase.PHASE_1) {
			phaseName = "p1";
		} else {
			phaseName = "p2";
		}
		
		String sygmaPhasePrefix = sygmaSpecificPrefix +  phaseName + "/";
		File dir = new File(sygmaPhasePrefix);
	    if (!dir.exists()) dir.mkdirs();
		
		
		String prefix = args[args.length-2];

		String dataset = args[args.length-3];
		if (!dataset.equals(DRUGBANK_PARAMETER) && !dataset.equals(TESTDATASET_PARAMETER) && !dataset.equals(REFERENCEDATASET_PARAMETER)) {
			logger.error("Error. Wrong dataset specification. Please use either \"drugbank\" or \"testdataset\" or \"referencedataset\".");
			System.exit(1);
		}
		TestParameters.Reference reference = null;
		String datasetname = null;
		if (dataset.equals(TESTDATASET_PARAMETER)) {
			reference = TestParameters.Reference.TEST_DATASET;
			datasetname = "testdataset_";
		} else if (dataset.equals(REFERENCEDATASET_PARAMETER)) {
			reference = TestParameters.Reference.DRUGBANK_PLUS_METXBIODB;
			datasetname = "referencedataset_";
		}
		

		String sygmaPredictionsFile = sygmaSpecificPrefix + PREDICTIONS_FILENAME + datasetname + phaseName + PREDICTIONS_FILENAME_ENDING;
		logger.debug(INPUT_FILE_MESSAGE, sygmaPredictionsFile);

		
		Boolean useStereo = false; // TODO change this back if test dataset analysis should consider stereochemistry
//		if (dataset.equals(TESTDATASET_PARAMETER)) {
//			useStereo = true;
//		} else {
//			useStereo = false;
//		}
		
		TestParameters testParameters = new TestParameters(TestParameters.UseSoMsAsHardFilter.NO, TestParameters.Version.EVALUATION, 
				TestParameters.UserVersion.OFFLINE, reference, TestParameters.InputFormat.DATABASE, phase, Runtime.getRuntime().availableProcessors()); 
		
		Map<String, Prediction> allSyGMaPredictions = readInSyGMaPredictionsFromFile(sygmaPredictionsFile, useStereo, testParameters);
		String evalResultsDir = "";
		
		
//		// --- permutation test - comment out previous two lines for this
//		Map<String, Prediction> tmpPredictions = readInSyGMaPredictionsFromFile(sygmaPredictionsFile, useStereo, testParameters);
//		if (tmpPredictions == null) {
//			logger.error(ERROR_READING_FILE_MESSAGE);
//			return;
//		}
//		
//		String evalResultsDir = "permutation_testdataset/subset_1/";
//		int perm = 1;
//		for (String parentSmiles : tmpPredictions.keySet()) {
//			
//			Map<String, Prediction> allSyGMaPredictions = new HashMap<>();
//			for (Entry<String, Prediction> entry : tmpPredictions.entrySet()) {
//				if (!entry.getKey().equals(parentSmiles)) {
//					allSyGMaPredictions.put(entry.getKey(), entry.getValue());
//				}
//			}
//			
//			evalResultsDir = evalResultsDir.replaceAll("_[0-9]+", "_" + Integer.toString(perm));
//			File evalResultsDirDir = new File(sygmaSpecificPrefix + evalResultsDir);
//			if (!evalResultsDirDir.exists()) {
//				evalResultsDirDir.mkdir();
//			}
//
//			logger.info("molecule {} is {}", perm, parentSmiles);
//			perm ++;
		
			// -------
		
			if (allSyGMaPredictions == null) {
				logger.error(ERROR_READING_FILE_MESSAGE);
				return;
			}
	
			if (logger.isDebugEnabled() ) {
				
				logger.debug(NUMBER_OF_PREDICTIONS_FOUND_MESSAGE, allSyGMaPredictions.size());
				
				int totalNumberOfMetabolitesPredicted = 0;
				for (Entry<String, Prediction> entry : allSyGMaPredictions.entrySet()) {
					totalNumberOfMetabolitesPredicted += entry.getValue().getPredictedMetabolites().size();
				}
				logger.debug(TOTAL_NUMBER_OF_METABOLITES_PREDICTED, totalNumberOfMetabolitesPredicted);
	
			}
			
	//		// compare to GLORYx only :
			if (compareDirectlyToGloryxOnly) {
				String gloryResultsDir = "";
				// TODO this is very messy. always make sure to use the correct dir
				if (phase == Phase.PHASE_2) {
					gloryResultsDir = "/Users/kops/zbhmount/metaboliteproject/gloryx_comprehensive/results_refdata_20200406/p2_sygmarulesonly/"; 
				} else if (phase == Phase.PHASE_1) {
					gloryResultsDir = "/Users/kops/zbhmount/metaboliteproject/gloryx_comprehensive/results_refdata_20200406/p1_sygmarulesonly/"; 
				} else {
					logger.error("Direct comparison can only be made for phase 1 or phase 2.");
				}
				compareDirectlyToGloryResultsAndExit(allSyGMaPredictions, gloryResultsDir);
			}
	
			
			List<Result> results = null;
			List<RankingRocResult> rankingRocResults = new ArrayList<>();
			
			Filenames filenames = new Filenames(prefix);
			
			if (dataset.equals(DRUGBANK_PARAMETER)) {
				
				logger.debug(GETTING_ALL_KNOWN_METABOLITES_FROM_DRUGBANK_MESSAGE);
				Map<BasicMolecule, Set<BasicMolecule>> drugbankData = getDrugbankData(filenames, phase);
				
				results = comparePredictionsToKnownMetabolites(allSyGMaPredictions, drugbankData, rankingRocResults, testParameters);
			
			} else if (dataset.equals(REFERENCEDATASET_PARAMETER)) {
				
				logger.debug(GETTING_ALL_KNOWN_METABOLITES_FROM_REFDATA_MESSAGE);
				
				TestDatasetLoader loader = new TestDatasetLoader();
				Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData = loader.loadData(filenames);
				
				Map<BasicMolecule, Set<BasicMolecule>> combinedDataset = new LinkedHashMap<>();
				Map<BasicMolecule, Set<BasicMolecule>> drugbankData = new LinkedHashMap<>();
				
				DatasetLoader dl = new DatasetLoader();
				Map<BasicMolecule, Set<BasicMolecule>> referenceDataset = dl.getCombinedReferenceDataset(filenames, combinedDataset, drugbankData, testMetabolismData, phase);
	
				results = comparePredictionsToKnownMetabolites(allSyGMaPredictions, referenceDataset, rankingRocResults, testParameters);
				
				
			} else {  // use test dataset
				
				logger.debug(GETTING_ALL_KNOWN_METABOLITES_FROM_TESTDATASET_MESSAGE);
				
				TestDatasetLoader loader = new TestDatasetLoader();
				Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData = loader.loadData(filenames);
				
				int totalNumMetabolites = 0;
				for (Entry<BasicMolecule, Set<BasicMolecule>> entry : testMetabolismData.entrySet()) {
					totalNumMetabolites += entry.getValue().size();
				}			
				logger.debug("total number of metabolites found: {}", totalNumMetabolites);
	
				
				results = comparePredictionsToKnownMetabolites(allSyGMaPredictions, testMetabolismData, rankingRocResults, testParameters);
				
			}
	
			Assert.notNull(results, "Error! Result of comparison is null.");
	
			logger.debug("number of entries in results: {}", results.size());
//			ResultsWriter.writeResultsToTxtFile(sygmaPhasePrefix, RESULTS_FILENAME, results); 
//			ResultsWriter.writeScoreRocResultsToFile(sygmaPhasePrefix, ROCRESULTS_SCORE_FILENAME, rankingRocResults);
//			ResultsWriter.writeRankRocResultsToFile(sygmaPhasePrefix, ROCRESULTS_RANK_FILENAME, rankingRocResults);
			// TODO
			ResultsWriter.writeResultsToTxtFile(sygmaSpecificPrefix + evalResultsDir, RESULTS_FILENAME, results); 
			ResultsWriter.writeScoreRocResultsToFile(sygmaSpecificPrefix + evalResultsDir, ROCRESULTS_SCORE_FILENAME, rankingRocResults);
			ResultsWriter.writeRankRocResultsToFile(sygmaSpecificPrefix + evalResultsDir, ROCRESULTS_RANK_FILENAME, rankingRocResults);
			
			int maxPredMet = calculateMaxNumPredictedMetabolites(allSyGMaPredictions);
			logger.info("max number of predicted metabolites for a single parent: {}", maxPredMet);
			
			// Output precision and recall
			MetabolitePredictor.outputPrecisionRecall(results);
		
//		} // end permutation for loop
	}



	private static void compareDirectlyToGloryResultsAndExit(Map<String, Prediction> allSyGMaPredictions, String gloryResultsDir) {
		
		File gloryResultsDirFile = new File(gloryResultsDir);
		Map<String, ComparisonInfo> gloryResults = new HashMap<>();  // save parent inchi and num predicted metabolites and mol number
		
		Collection<File> files = FileUtils.listFiles(gloryResultsDirFile, null, false);  // false for no subdirectories 
		for(File file : files){
			String filename = file.toPath().getFileName().toString();
			
			if (filename.startsWith("results_mol_")) {
				List<String> lines = new ArrayList<>();
				
				try (BufferedReader objReader = new BufferedReader(new FileReader(file))) {
					
					extractParentAndPredictedLines(lines, objReader);
					addComparisonInfoToMap(gloryResults, filename, lines);

				} catch (FileNotFoundException e) {
					logger.error("Error creating FileReader for file {}", file.toPath());
				} catch (IOException e) {
					logger.error("Error reading file {}", file.toPath());

				} 
			}
		} 
		comparePredictedMetabolitesAndLogIfNotMatch(allSyGMaPredictions, gloryResults);
		
		System.exit(0);
	}


	private static void comparePredictedMetabolitesAndLogIfNotMatch(Map<String, Prediction> allSyGMaPredictions,
			Map<String, ComparisonInfo> gloryResults) {
		
		int discrepancyCounter = 0;
		Boolean discrepancy = false;
				
		for (Prediction pred : allSyGMaPredictions.values()) {
			String parentInchi = pred.getParentMolecule().getInchi();
			Set<String> sygmaPredictedInchis = getInchiSet(pred.getPredictedMetabolites());
			
			ComparisonInfo gloryResult = gloryResults.get(parentInchi);
			if (gloryResult == null) {
				// no predictions for this parent molecule
				continue;
			}
			Set<String> gloryPredictedInchis = getInchiSet(gloryResult.getPredictedMetabolites());
			
			discrepancy = false;
			
			for (PredictedMolecule m : gloryResult.getPredictedMetabolites()) {
				if (!sygmaPredictedInchis.contains(m.getInchi())) {
					logger.info("GLORY predicted metabolite {} that SyGMa did not predict! Look at this result more closely. "
							+ "\n\tFile {} . \n\tSyGMa parent smiles {} \n\t reaction {}", m.getSmiles(), gloryResult.getFilename(), 
							pred.getParentMolecule().getSmiles(), m.getTransformationName());
					discrepancy = true;
				}
			}
			for (PredictedMolecule m : pred.getPredictedMetabolites()) {
				if (!gloryPredictedInchis.contains(m.getInchi())) {
					logger.info("SyGMa predicted metabolite {} that GLORY did not predict! Look at this result more closely. "
							+ "\n\tFile {} . \n\tSyGMa parent smiles {}", m.getSmiles(), gloryResult.getFilename(), pred.getParentMolecule().getSmiles());
					discrepancy = true;
				}
			}
			if (discrepancy) {
				discrepancyCounter ++;
			}
		}
		logger.info("Number of parent molecules with a discrepancy in the predicted metabolites: {}", discrepancyCounter);
	}



	private static Set<String> getInchiSet(Set<PredictedMolecule> predictions) {
		
		Set<String> predictedInchis = new HashSet<>();
		for (PredictedMolecule m : predictions) {
			predictedInchis.add(m.getInchi());
		}
		return predictedInchis;
	}


	private static void addComparisonInfoToMap(Map<String, ComparisonInfo> gloryResults, String filename, List<String> lines) {
		
		String parentSmiles = lines.get(0).split("\\s")[1];						
		String thisParentInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parentSmiles);
		int numMetsPred = lines.size() - 1;
		
		// now let's actually enable checking of whether the metabolites are actually the same
		Set<PredictedMolecule> metabolites = collectGloryMetabolites(lines);

		ComparisonInfo ci = new ComparisonInfo(numMetsPred, filename, metabolites);
		gloryResults.put(thisParentInchi, ci);
	}



	private static Set<PredictedMolecule> collectGloryMetabolites(List<String> lines) {
		
		Set<PredictedMolecule> metabolites = new HashSet<>();
		for(int i=1; i<lines.size(); i++) {
			
			String[] splitstring  = lines.get(i).split("\\s");
			if (splitstring[0].equals("predicted")) { // double-check just in case
				
				String metaboliteSmiles = splitstring[splitstring.length-1];
				String metaboliteInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(metaboliteSmiles);
				String transformationName = splitstring[1];
				
				PredictedMolecule m = new PredictedMolecule();
				m.setInchi(metaboliteInchi);
				m.setSmiles(metaboliteSmiles);
				m.setTransformationName(transformationName);
				metabolites.add(m);
			}
		}
		return metabolites;
	}



	private static void extractParentAndPredictedLines(List<String> lines, BufferedReader objReader) throws IOException {
		
		String strCurrentLine;
		while ((strCurrentLine = objReader.readLine()) != null) {
			if (strCurrentLine.startsWith("parent") || strCurrentLine.startsWith("predicted")) {
				lines.add(strCurrentLine);
			}
		}
	}



	private static List<Result> comparePredictionsToKnownMetabolites(Map<String, Prediction> allSyGMaPredictions,
			Map<BasicMolecule, Set<BasicMolecule>> refData, List<RankingRocResult> rankingRocResults, TestParameters testParameters) {
		
		List<Result> results = new ArrayList<>();
		
		for (Prediction prediction : allSyGMaPredictions.values()) {
			
			PredictionEvaluator evaluator = new PredictionEvaluator(testParameters, refData, prediction, 
					results, rankingRocResults, "outfile.txt");			
			
			evaluator.evaluatePredictions();
		}
		return results;
	}



	private static void calculateAndAddResultToList(List<Result> results, Prediction value, Set<BasicMolecule> knownMetabolites, Count result) {
		
		int numMetabolites = knownMetabolites.size();
		int bestRank = result.getBestRank();  // should be 0 if no metabolites were predicted
		int knownFound = result.getKnownFound();
		int numPredicted = value.getPredictedMetabolites().size();
		int numFP = result.getNumberOfFalsePositives();
		double recoveryRate = result.calculateScore(knownMetabolites, value.getParentMolecule().getSmiles());  
		
		if (logger.isDebugEnabled()) {
			logger.debug("number of known metabolites: {}", numMetabolites);
			logger.debug("number of predicted metabolites: {}", numPredicted);
			logger.debug("number known found aka number of true positives: {}", knownFound);
			logger.debug("number of false positives: {}", numFP);
			logger.debug("best rank: {}", bestRank);  
			logger.debug("recovery rate: {}", recoveryRate);
		}
		
		results.add(new Result(value.getParentMolecule().getSmiles(), recoveryRate, numMetabolites, knownFound, numPredicted, numFP, false, bestRank));  // set somPredicted to false because irrelevant
	}



	private static Map<BasicMolecule, Set<BasicMolecule>> getDrugbankData(Filenames filenames, Phase phase) {
		
		Map<BasicMolecule, Set<BasicMolecule>> drugbankData = new HashMap<>();
		MetabolismDataLoader dloader = new MetabolismDataLoader();
		dloader.loadMetabolismData(filenames, drugbankData, phase);
		logger.debug(GOT_DRUGBANK_METABOLITES_MESSAGE, drugbankData.size());
		
		logger.debug(GETTING_NEW_TEST_DATASET_MESSAGE);
		// get new test dataset
		TestDatasetLoader loader = new TestDatasetLoader();
		Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData = loader.loadData(filenames);
		
		logger.debug(CHECKING_OVERLAP_MESSAGE);
		// check overlap between new test dataset and DrugBank data
		Set<String> overlapDrugbankIds = GenericOverlapCalculator.getOverlapAndGenerateInchiWithoutStereochemistryForOtherDataset(drugbankData, testMetabolismData);		
		DatasetManipulator.removeOverlapFromDataset(drugbankData, overlapDrugbankIds);
		logger.debug(FINISHED_REMOVING_COMPOUNDS_MESSAGE, drugbankData.size());	
		
		return drugbankData;
	}


	private static Map<String, Prediction> readInSyGMaPredictionsFromFile(String sygmaPredictionsFile, Boolean useStereo, TestParameters testParameters) {
		
		File csv = new File(sygmaPredictionsFile);
		CSVParser parser;
		try {
			parser = CSVParser.parse(csv, Charset.defaultCharset(), CSVFormat.TDF.withHeader().withIgnoreEmptyLines());  // .withQuote(null)
		} catch (IOException e) {
			logger.error(ERROR_READING_CSV_FILE, sygmaPredictionsFile);
			return null;
		}
		
		Map<String, Prediction> allSyGMaPredictions = new HashMap<>();
		
		for (CSVRecord record : parser) {
			
			String parentSMILES = record.get(HEADER_PARENT_SMILES);
			PredictedMolecule metabolite = processMetabolitePredictionInformation(record);
			if (!metabolite.getInchi().equals(metabolite.getParentInchi()) && MoleculeManipulator.moleculeIsLargeEnough(metabolite.getSmiles(), testParameters.getMetaboliteNumberOfHeavyAtomsCutoff())) { // use my own heavy atom count cutoff, same as for GLORY
				addMetabolitePredictionToMap(allSyGMaPredictions, parentSMILES, metabolite, useStereo);
			}
		}
		
		// close the parser
		try {
			parser.close();
		} catch (IOException e) {
			logger.error(ERROR_CLOSING_CSV_FILE, sygmaPredictionsFile);
			return null;
		}
		
		return allSyGMaPredictions;
	}


	private static void addMetabolitePredictionToMap(Map<String, Prediction> allSyGMaPredictions, String parentSMILES, PredictedMolecule metabolite, Boolean useStereo) {
		
		if (allSyGMaPredictions.containsKey(parentSMILES)) {
			allSyGMaPredictions.get(parentSMILES).addPredictedMetabolite(metabolite);
			
		} else {
			String parentInchi = null;
			if (useStereo) {
				parentInchi = MoleculeManipulator.generateInchiFromSmiles(parentSMILES);
			} else {
				parentInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parentSMILES);
			}
			ParentMolecule parent = new ParentMolecule();
			parent.setInchi(parentInchi);
			parent.setSmiles(parentSMILES);
			Prediction sygmaPrediction = new Prediction(parent);
			sygmaPrediction.addPredictedMetabolite(metabolite);
			allSyGMaPredictions.put(parentSMILES, sygmaPrediction);		
		}
	}


	private static PredictedMolecule processMetabolitePredictionInformation(CSVRecord record) {
		String metaboliteSMILES = record.get(HEADER_METABOLITE_SMILES);
		
		// Since some of the SMILES of SyGMa's predictions are wrong in a way that causes atoms to be interpreted as radicals instead of having a hydrogen
		IAtomContainer metaboliteAtomContainer = MoleculeManipulator.generateMoleculeFromSmiles(metaboliteSMILES);
		MoleculeManipulator.addImplicitHydrogens(metaboliteAtomContainer);
		
		String metaboliteNewSmiles = MoleculeManipulator.generateSmiles(metaboliteAtomContainer);
		
		String metaboliteInChI = MoleculeManipulator.generateInchiWithoutStereo(metaboliteAtomContainer);
		Double metaboliteScore = Double.valueOf(record.get(HEADER_METABOLITE_SCORE));
		
		String parentSMILES = record.get(HEADER_PARENT_SMILES);
		String parentInChI = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parentSMILES);
		
		PredictedMolecule metabolite = new PredictedMolecule();
		metabolite.setSmiles(metaboliteNewSmiles);
		metabolite.setInchi(metaboliteInChI);
		metabolite.setPriorityScore(metaboliteScore);
		metabolite.setMadeSoMCutoff(true);
		
		metabolite.setParentSmiles(parentSMILES);
		metabolite.setParentInchi(parentInChI);
		return metabolite;
	}
	
	
	private static int calculateMaxNumPredictedMetabolites(Map<String, Prediction> allSyGMaPredictions) {
		int maxPredMet = 0;
		for (Prediction x : allSyGMaPredictions.values()) {
			if (x.getPredictedMetabolites().size() > maxPredMet) {
				maxPredMet = x.getPredictedMetabolites().size();
			}
		}
		return maxPredMet;
	}
	
	

}
