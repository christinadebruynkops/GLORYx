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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import main.java.transformation.Transformer;
import main.java.transformation.reactionrules.GLORYTransformations;
import main.java.utils.Calculations;
import main.java.utils.Count;
import main.java.utils.Errors;
import main.java.utils.Phase;
import main.java.utils.Prediction;
import main.java.utils.TestParameters;
import main.java.utils.analysis.RankingRocResult;
import main.java.utils.analysis.Result;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.ParentMolecule;
import main.java.utils.molecule.PredictedMolecule;


/**
 * Performs the evaluation, i.e. comparing the predicted metabolites to known metabolites from whichever dataset is being used and calculating the relevant metrics.
 * <p>
 * Note: Some of the check... methods are probably not usable.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class PredictionEvaluator {

	private static final String PREDICTED = "predicted\t";
	private static final String TAB = "\t";
	private static final String ENDLINE = "\n";
	private static final String ERROR_WRITING_OUTPUT_FILE = "Error writing output file to {}.";
	private static final String PERIOD = "\\.";
	private static final String FROM_REACTION_TYPE = "\tFrom transformation (may be more than one, only one listed): {}";
	private static final String NUMBER_OF_PREDICTED_METABOLITES_WITHOUT_DUPLICATES = "Number of predicted metabolites, without duplicates: {}";
	private static final String PRODUCT_SMILES = "Product SMILES: {}";
	private static final String KNOWN_NOT_FOUND = "known not found\t";
	private static final String KNOWN_FOUND = "known found\t";
	private static final String NO_KNOWN_METABOLITES = "No known metabolites";
	private static final String NUMBER_OF_KNOWN_METABOLITES = "Number of known metabolites (with at least {} heavy atoms): {}";
	private static final String NUMBER_PRODUCTS_AFTER_DUPLICATE_REMOVAL = "Number of predicted products after duplicate removal, including stereoisomers for molecule {}: {}";
	private static final String PARENT = "parent\t";

	private static final Logger logger = LoggerFactory.getLogger(PredictionEvaluator.class.getName());


	
	// things to calculate
	private Count resultCounts;
	private int numMetabolites;
		
	private TestParameters testParameters;
	private Map<BasicMolecule, Set<BasicMolecule>> dataset;
	
	private ParentMolecule parentMol;
	private Set<PredictedMolecule> predictedMetabolites;
	private Set<Errors> errors;
	private Boolean somPredicted;
	private Boolean predictionFailed;
	private String molFilename;
	
	// things to fill in via methods in this class
	private List<Result> results;
	private List<RankingRocResult> rankingRocResults;
	private Set<BasicMolecule> knownMetabolitesNoStereo;
	private List<PredictedMolecule> rankedUniquePredictions;
	private Set<String> knownFound; 
	private Set<Integer> ranksOfTruePositivePredictions;


	
	// constructor
	public PredictionEvaluator(TestParameters testParameters) {
		this.testParameters = testParameters;
	}
	
	
	public PredictionEvaluator(TestParameters testParameters, Map<BasicMolecule, Set<BasicMolecule>> dataset,
			Prediction prediction, List<Result> results, List<RankingRocResult> rankingRocResults, String molFilename) {

		this.testParameters = testParameters;
		this.dataset = dataset;
		
		this.parentMol = prediction.getParentMolecule();
		this.predictedMetabolites = prediction.getPredictedMetabolites();
		this.somPredicted = prediction.getSomPredicted();
		this.predictionFailed = prediction.fameModelFailed();
		this.errors = prediction.getErrors();
		
		this.results = results;
		this.rankingRocResults = rankingRocResults;
		
		this.knownFound = new HashSet<>();
		this.ranksOfTruePositivePredictions = new HashSet<>();
		this.knownMetabolitesNoStereo = new HashSet<>();
		
		this.molFilename = molFilename;
	}
	
	
	// used to evaluate results in evaluation mode
	public void evaluatePredictions() {

		knownMetabolitesNoStereo = getKnownMetabolites();
		
		// check if prediction failed and if so add Result
		if (!errors.isEmpty() && predictedMetabolites == null) {
			logger.info("Processing a parent molecule that FAME 3 could not handle. Adding result for that.");
			addResultForMoleculeFAME3CouldNotHandle();
			return;
		}
		
		if (predictedMetabolites.isEmpty()) {
			results.add(new Result(this.parentMol.getId(), knownMetabolitesNoStereo.size(), false)); 
			return;
		} 
		
		rankedUniquePredictions = getUniquePredictionsWithoutStereo();
		
		compareToKnownMetabolites(); // here is where something is added to rankingROCResults

		Double recoveryRate = Calculations.calculateRecoveryRate(numMetabolites, resultCounts.getKnownFound());
		results.add(new Result(parentMol.getId(), recoveryRate, numMetabolites, resultCounts.getKnownFound(), resultCounts.getTotalPredictions(), 
				resultCounts.getNumberOfFalsePositives(), this.somPredicted, resultCounts.getBestRank(), parentMol.getMolecularWeight(), parentMol.getHeavyAtomCount()));
		
		writeResults(molFilename);

	}
	


	private Set<BasicMolecule> getMetabolitesForParent(Map<BasicMolecule, Set<BasicMolecule>> referenceData, String parentInchi) {
		
		Set<BasicMolecule> knownMetabolites = new HashSet<>();
		Set<String> knownMetaboliteInchis = new HashSet<>();
		
		for (Map.Entry<BasicMolecule,Set<BasicMolecule>> entry : referenceData.entrySet()) {
			
			BasicMolecule p = entry.getKey();
			
			if (p.getInchi().equals(parentInchi)) {
				// found this parent compound in the map
				
				logger.debug("found parent {} in map with {} metabolites", parentMol.getId(), entry.getValue().size());
				
				for (BasicMolecule m : entry.getValue()) {
					BasicMolecule metabolite = new BasicMolecule();
					metabolite.setSmiles(m.getSmiles());
					metabolite.setInchi(m.getInchi());
					
					logger.debug("known metabolite inchi: {}", m.getInchi());
									
					if (knownMetaboliteInchis.add(metabolite.getInchi())) {
						knownMetabolites.add(metabolite);
					}
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			for (BasicMolecule known : knownMetabolites) {
				logger.debug("known metabolite: {}", known.getSmiles());
			}
		}
			
		return knownMetabolites;
	}
	
	
	private List<PredictedMolecule> getUniquePredictionsWithoutStereo() {
		// this method is for filling in this.rankedUniquePredictions
		
		Set<PredictedMolecule> uniquePredictions = new HashSet<>();
		Set<String> inchis = new HashSet<>();
		PredictionHandler ph = new PredictionHandler();
		
		int uniqueCounter = 0; // number of unique predicted metabolites without stereochemistry enumeration
		for (PredictedMolecule predicted: predictedMetabolites) {

			// get inchi and smiles (for all components, if applicable)
			
			Set<PredictedMolecule> mols = ph.getComponentsOfMoleculeWithoutStereochemistry(testParameters, predicted);
			for (PredictedMolecule mol : mols) {

				// just add the InChI of the original molecule				
				Boolean added = addOriginalInchi(inchis, mol.getInchi(), mol.getSmiles(), predicted);
				if (added) {
					uniqueCounter += 1;
					mol.setTransformationName(predicted.getTransformationName());
					uniquePredictions.add(mol);

				} else if (mol.getPriorityScore() != (double) 0 && mol.getPriorityScore() != null) {
					ensureHighestPriorityScoreInUniquePredictions(uniquePredictions, mol, predicted.getTransformationName());
				}
			}
		}
				
		logger.info(NUMBER_OF_PREDICTED_METABOLITES_WITHOUT_DUPLICATES, uniqueCounter);		

		return ph.rankPredictedMetabolites(uniquePredictions);  // allow/handle ties
	}


	private void rankAndWritePredictedMetabolites(final String molFilename, Set<PredictedMolecule> uniquePredictions) {
		
		PredictionHandler ph = new PredictionHandler();
		rankedUniquePredictions = ph.rankPredictedMetabolites(uniquePredictions);  // allow/handle ties
		
		writePredictedMetabolitesToFile(molFilename);
	}
	
	private void writePredictedMetabolitesToFile(final String molFilename) {
		
		if (rankedUniquePredictions == null) {
			logger.error("No predictions for molecule {}. Therefore not writing preditions to file.", parentMol.getId());
			return;
		}
		
		for (PredictedMolecule mol : rankedUniquePredictions) {
			if (molFilename != null) {
				logger.debug("metabolite: {}", mol.getSmiles());
				writePredictedToFile(molFilename, mol);
			}
		}
	}
	
	private void compareToKnownMetabolites() {
				
		logger.info(NUMBER_PRODUCTS_AFTER_DUPLICATE_REMOVAL, parentMol.getId(), predictedMetabolites.size());
		
		resultCounts = checkPredictionsAgainstKnownMetabolitesNoStereo();
	}
	
	
	/**
	 * This is the method used in GLORYx to check the predicted metabolites against the known metabolites.
	 * Checks predicted InChIs, calculates all InChIs, both predicted and known, again from the SMILES, using 
	 * no stereochemistry information (INCHI_OPTION SNon).
	 * @return
	 */
	private Count checkPredictionsAgainstKnownMetabolitesNoStereo() {
		// this is the most usable of these methods, perhaps the only usable one
		
		int numberOfFalsePositives = 0;
		int numberOfTruePositives = 0;
		Boolean isTruePositive;
		int totalNumberOfPredictedMetabolites = rankedUniquePredictions.size();
		
		for (PredictedMolecule mol : rankedUniquePredictions) {
			
			String predictedInchiNoStereo = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(mol.getSmiles());
			
			if (!mol.getMadeSoMCutoff()) { // madeSoMCutoff is true by default
				
				totalNumberOfPredictedMetabolites --;
				
				logger.debug("Molecule didn't make SoM cutoff. Reducing count of number of predicted metabolites.");
				
				Assert.isTrue(mol.getPriorityScore() == 0, "Priority score must be 0 because this predicted molecule didn't make the SoM cutoff.");

				isTruePositive = checkAgainstAllKnownMetabolitesNoStereo(mol, predictedInchiNoStereo, false);

				rankingRocResults.add(new RankingRocResult(mol.getPriorityScore(), mol.getRank(), isTruePositive)); 
				continue;
			}

			isTruePositive = checkAgainstAllKnownMetabolitesNoStereo(mol, predictedInchiNoStereo, true);
			
			if (!isTruePositive) {
				numberOfFalsePositives++;
			} else {
				numberOfTruePositives++;
			}
			
			rankingRocResults.add(new RankingRocResult(mol.getPriorityScore(), mol.getRank(), isTruePositive));  // if not true positive, then it's a false positive, aka not a known metabolite
		}
				
		if (knownFound.size() != numberOfTruePositives) {
			logger.debug("WARNING! For molecule {}, the number of known found ({}) is not the same as the number of "
					+ "TP predictions ({}). Make sure this is due to aldehyde/carboxylic acid matching.", parentMol.getId(), knownFound.size(), numberOfTruePositives);
		}
		
		
		// TODO add this back in?
		// Include false negatives in the ROC curve:
//		int numberOfKnownMetabolites = knownMetabolitesNoStereo.size();
//		int numberOfFalseNegativePredictions = numberOfKnownMetabolites - knownFound.size();
//		if (!testParameters.isUserVersion() && testParameters.getReference() == TestParameters.Reference.TEST_DATASET) {
//			logger.info("Number of false negatives: {}. Adding to results for ROC curve.", numberOfFalseNegativePredictions);
//			for (int i = 0; i < numberOfFalseNegativePredictions; i++) {
//				rankingRocResults.add(new RankingRocResult((double) 0, 1000, true));  // add data point: score is 0, and it's a known metabolite
//			}
//		}
		
		logger.info("known found: {}", knownFound.size());
		
		int bestRank = 0;
		if (!ranksOfTruePositivePredictions.isEmpty()) {
			bestRank = Collections.min(ranksOfTruePositivePredictions);
		}  else if (!knownFound.isEmpty()){ // else leave it 0 because no known metabolites were found
			logger.warn("Something may be wrong. At least one known metabolite was predicted but no ranking was found.");
		}
		
		logger.debug("best rank: {}", bestRank);
		
		return new Count(knownFound.size(), numberOfFalsePositives, totalNumberOfPredictedMetabolites, bestRank);
	}


	private Boolean checkAgainstAllKnownMetabolitesNoStereo(PredictedMolecule mol, String predictedInchiNoStereo, Boolean addToKnownFound) {
		
		Boolean isTruePositive = false;
		
		for (BasicMolecule knownMetabolite : knownMetabolitesNoStereo) {

			isTruePositive = checkAgainstKnownMetaboliteNoStereo(mol, predictedInchiNoStereo, knownMetabolite, addToKnownFound);
			
			if (isTruePositive) {
				return isTruePositive;
			}
		}
		return isTruePositive;
	}


	private Boolean checkAgainstKnownMetaboliteNoStereo(PredictedMolecule mol, String predictedInchiNoStereo, BasicMolecule knownMetabolite, Boolean addToKnownFound) {
		
		Boolean isTruePositive = false;
		String knownInchiNoStereo = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(knownMetabolite.getSmiles());

		if (predictedInchiNoStereo.equals(knownInchiNoStereo)) {

			if (!addToKnownFound) {
				isTruePositive = true;
				
			} else {
				// found a match
				isTruePositive = addFoundMatch(mol, knownMetabolite);
			}

		} else if (Phase.isPhase1(mol.getMetabolismPhase())) { 
			// this should only be done for CYP reactions, but since we don't know which of SyGMa's phase 1 reaction rules correspond to CYPs, just use phase 1 here
				
			// consider aldehyde predicted product to be correct if the known metabolite is the carboxylic acid
			isTruePositive = checkSpontaneousOxidationFromAldehydeToCarboxylicAcid(mol, knownMetabolite, knownInchiNoStereo, addToKnownFound);
			
		}
		return isTruePositive;
	}
	

	
	private Set<Integer> getRanksOfTruePositivePredictions() {
		return this.ranksOfTruePositivePredictions;
	}


	private Set<BasicMolecule> removeStereoInformation(final Set<BasicMolecule> knownMetabolites) {
		
		Set<String> knownMetabolitesNoStereoInchis = new HashSet<>();
		String inchiWithoutStereo = "";
		String smilesWithoutStereo = "";
				
		for (BasicMolecule knownMetabolite : knownMetabolites) {
			inchiWithoutStereo = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(knownMetabolite.getSmiles());
			smilesWithoutStereo = MoleculeManipulator.convertSmilesToWithoutStereo(knownMetabolite.getSmiles());

			BasicMolecule knownMetaboliteNoStereo = new BasicMolecule();
			knownMetaboliteNoStereo.setId(knownMetabolite.getId());
			knownMetaboliteNoStereo.setInchi(inchiWithoutStereo);
			knownMetaboliteNoStereo.setSmiles(smilesWithoutStereo);
			
			// check again for duplicate based on inchi before adding to set
			Boolean added = knownMetabolitesNoStereoInchis.add(inchiWithoutStereo);
			if (added) {
				knownMetabolitesNoStereo.add(knownMetaboliteNoStereo);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("\n\n");
			for (BasicMolecule known : knownMetabolitesNoStereo) {
				logger.debug("known metabolite: {}", known.getSmiles());
			}
		}
		return knownMetabolitesNoStereo;
	} 
	
	
	private static BasicMolecule containsInchi(Set<BasicMolecule> moleculeSet, String inchi) {
	    for(BasicMolecule molecule : moleculeSet) {
	        if(molecule != null && molecule.getInchi().equals(inchi)) {
	            return molecule;
	        }
	    }
	    return null;
	}
	
	
	private Set<BasicMolecule> getKnownMetabolites() {			
		
		logger.debug("parent smiles {}", this.parentMol.getSmiles());
		Set<BasicMolecule> knownMetabolites = getMetabolitesForParent(dataset, this.parentMol.getInchi()); // reference dataset uses Inchis without stereochemistry
		logger.debug("found {} known metabolites for molecule {}", knownMetabolites.size(), parentMol.getId());
		
		Set<BasicMolecule> knownMetabolitesFinal = removeStereoInformation(knownMetabolites);
		
		if (testParameters.getReference() == TestParameters.Reference.TEST_DATASET && knownMetabolites.size() != knownMetabolitesFinal.size()) {
			logger.error("The number of known metabolites before and after removal of stereochemistry is not the same for molecule {}!", this.parentMol.getId());
		} else {
			logger.debug("Number of known metabolites before stereochem removal for molecule {}: {}", this.parentMol.getId(), knownMetabolites.size());
		}
		
		numMetabolites = knownMetabolitesFinal.size();
		logger.info(NUMBER_OF_KNOWN_METABOLITES, testParameters.getMetaboliteNumberOfHeavyAtomsCutoff(), knownMetabolitesFinal.size());

		Assert.notEmpty(knownMetabolitesFinal, NO_KNOWN_METABOLITES);  // should never happen because previously checked
		
		return knownMetabolitesFinal;
	}

	
	private void ensureHighestPriorityScoreInUniquePredictions(Set<PredictedMolecule> uniquePredictions, final PredictedMolecule mol, final String transformationName) {
		
		Iterator<PredictedMolecule> uniquePredictionsIterator = uniquePredictions.iterator();
		Boolean needToReplace = false;
		while (uniquePredictionsIterator.hasNext()) {
			PredictedMolecule nextMolecule = uniquePredictionsIterator.next();
			
			// if InChI is the same, check if the priority score is lower and, if so, replace with the current mol
			if (nextMolecule.getInchi().equals(mol.getInchi()) && nextMolecule.getPriorityScore() < mol.getPriorityScore()) {
				uniquePredictionsIterator.remove();
				needToReplace = true;
				break;
			}
		}
		if (needToReplace) {
			mol.setTransformationName(transformationName);
			uniquePredictions.add(mol);
		}
	}

	
	private static Boolean addOriginalInchi(Set<String> inchis, final String inchi, final String smiles, final PredictedMolecule molecule) {
		Boolean added = inchis.add(inchi);  
		printProductMessage(added, smiles, molecule);
		return added;
	}

	
	private static void printProductMessage(final Boolean added, final String smiles, final PredictedMolecule molecule) {
		if (added) {
			logger.debug(PRODUCT_SMILES, smiles);  // only the largest component, not with stereoisomers enumerated
			if (molecule.getTransformationName() != null) {
				logger.debug(FROM_REACTION_TYPE, (String) molecule.getTransformationName());
			}
		}
	}

	
	private static Set<BasicMolecule> calculateInchi(final PredictedMolecule molecule) {
		// if molecule is actually made up of more than one molecule, process all component molecules

		Set<BasicMolecule> molecules = new HashSet<>();
		final String wholeSmiles = molecule.getSmiles();
		final String[] smilesParts = wholeSmiles.split(PERIOD);

		if (smilesParts.length > 1) {
			
			for (String componentSmiles : smilesParts) {
				final String inchi = MoleculeManipulator.generateInchiFromSmiles(componentSmiles);		
				
				if (inchi == null) { // some products may be invalid due to e.g. errors in SMIRKS
					continue;
				}
				
				BasicMolecule mol = new BasicMolecule();
				mol.setInchi(inchi);
				mol.setSmiles(componentSmiles);
				molecules.add(mol);
			}
			
		} else {
			final String inchi = molecule.getInchi();
			if (inchi != null) {
				
				final String smiles = molecule.getSmiles();
				
				BasicMolecule mol = new BasicMolecule();
				mol.setInchi(inchi);
				mol.setSmiles(smiles);
				molecules.add(mol);			}

		}
		return molecules;
	}
	
	
	private Boolean checkSpontaneousOxidationFromAldehydeToCarboxylicAcid(PredictedMolecule mol, BasicMolecule knownMetabolite, String knownMetaboliteInchiToCompare, Boolean add) {
		
		Transformer transformer = new Transformer(testParameters, ""); // don't need to define a FAME 3 model in this case

		IAtomContainerSet products = transformer.performTransformationFromSmiles(mol.getSmiles(), 
				 GLORYTransformations.ALDEHYDE_OXIDATION.getSMIRKS());
		
		Boolean isTruePositive = false;

		if (products != null && !products.isEmpty()) {

			for (IAtomContainer predictedProduct : products.atomContainers()) {
				if (MoleculeManipulator.generateInchiWithoutStereo(predictedProduct).equals(knownMetaboliteInchiToCompare)) {
					
					if (logger.isDebugEnabled() ) {
						logger.debug("Got a match when considering aldehyde predicted product to be correct if the known metabolite "
								+ "is the carboxylic acid. {} to {} and parent is {}", mol.getSmiles(), 
								knownMetabolite.getSmiles(), parentMol.getSmiles());
					}

					isTruePositive = true;
					if (add) {
						addFoundMatch(mol, knownMetabolite);
					}
				}
			}
		}
		return isTruePositive;
	}
	
	
	private Boolean addFoundMatch(PredictedMolecule mol, BasicMolecule knownMetabolite) {
		
		Boolean isTruePositive;
		knownFound.add(knownMetabolite.getInchi());  // Note: Stereochemistry not changed again here so number of known metabolites will match original loaded dataset
		logger.debug("found! {}", knownMetabolite.getSmiles());
		logger.debug("size of known found: {}", knownFound.size());
		isTruePositive = true;
		
		logger.debug("rank of found metabolite: {}", mol.getRank());
		ranksOfTruePositivePredictions.add(mol.getRank());
		return isTruePositive;
	}
	
	
	// ----- write eval output file -----


	private void writeResults(String molFilename) {

		writeParentToFile(molFilename);  // write parent smiles to file
		
		writePredictedMetabolitesToFile(molFilename); // use ranked unique predictions instead

		writeKnownMetabolitesToFile(molFilename);
	}

	
	private void writeParentToFile(String molFilename) {
		
		String smiles = parentMol.getSmiles();
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(molFilename))){
			writer.append(PARENT + smiles + ENDLINE);
		} catch (IOException e) {
			logger.error(ERROR_WRITING_OUTPUT_FILE, molFilename);
		}
	}
	
	
	private void writePredictedToFile(final String molFilename, PredictedMolecule mol) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(molFilename, true))){
			writer.append(PREDICTED + mol.getTransformationName() + TAB + mol.getRank() + TAB + mol.getSmiles() + ENDLINE);
		} catch (IOException e) {
			logger.error(ERROR_WRITING_OUTPUT_FILE, molFilename);
		}
	}

	
	private void writeKnownMetabolitesToFile(String molFilename) {
		
		if (molFilename != null) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(molFilename, true))){
				
				for (BasicMolecule knownMetabolite : knownMetabolitesNoStereo) {
					
					if (knownFound.contains(knownMetabolite.getInchi())) {
						writer.append(KNOWN_FOUND + knownMetabolite.getSmiles() + ENDLINE);
						
						logger.debug("found {}", knownMetabolite.getSmiles());
						
					} else {
						writer.append(KNOWN_NOT_FOUND + knownMetabolite.getSmiles() + ENDLINE);
					}
				}
			} catch (IOException e) {
				logger.error(ERROR_WRITING_OUTPUT_FILE);
			}
		}
	}

	
	private void addResultForMoleculeFAME3CouldNotHandle() {
		
		results.add(new Result(this.parentMol.getId(), knownMetabolitesNoStereo.size(), true)); 
	}

	
}
