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

import java.time.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.modelling.Modeller;

import main.java.sompredictor.SoMPredictor;
import main.java.transformation.Transformer;
import main.java.utils.Calculations;
import main.java.utils.Errors;
import main.java.utils.Prediction;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.ParentMolecule;
import main.java.utils.molecule.PredictedMolecule;
import main.java.metaboliteprediction.PredictionHandler;
import main.java.utils.TestParameters;


/**
 * Performs the metabolite prediction.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MetabolitePredictorWorker implements Runnable {
	
	private static final String INPUT_MOLECULE_COULD_NOT_BE_PROCESSED = "Input molecule could not be processed.";
	private static final String RUNNING_FAME3 = "Running FAME 3. Input smiles: {}";
	private static final String ERROR_ONLY_ONE_MOLECULE_SHOULD_BE_CREATED = "Error! Only one molecule should be created!";
	private static final String NUMBER_OF_PREDICTED_PRODUCTS_UNEDITED = "Number of predicted products for molecule {}, unedited: {}";
	private static final String PERFORMING_TRANSFORMATION_FOR_MOLECULE = "Performing transformation for molecule {} {}";
	private static final String TOTAL_NUMBER_OF_PRODUCTS = "Total number of products: {}";
	private static final String PARENT_SMILES = "Parent SMILES: {}";
	private static final String ERROR_IN_FAME3_PREDICTION = "Error in FAME 3 prediction for molecule {} with SMILES {}. No metabolite predictions can be made for this molecule.";

	
	private static final Logger logger = LoggerFactory.getLogger(MetabolitePredictorWorker.class.getName());
	
	
	// set in constructor:
	private IAtomContainer molecule;
	private String inputSmiles;
	private final String originalInputSmiles;
	private TestParameters testParameters;
	private Globals fameParameters;
	private int moleculeCounter;
	private final String inputName;
	private final Boolean rerunning;

	// determine
	private Boolean somPredicted; 
	private double maxSomProbability;
	private double medianSomProbability;
	private double avgOfTop3SomProbabilities;

	// fill
	private Map<Integer, Prediction> predictions;
	private List<Long> runTimes;
		
	
	
	// constructor for using DrugBank dataset or combined reference dataset or new manually curated test dataset
	public MetabolitePredictorWorker(String singleInputSmiles, Map<Integer, Prediction> predictions,
			TestParameters testParameters, Globals fameParameters, final int moleculeCounter, String inputName, Boolean rerunning) {
		
		this.inputSmiles = singleInputSmiles;
		this.originalInputSmiles = singleInputSmiles;
		this.testParameters = testParameters;
		this.fameParameters = fameParameters; 
		this.moleculeCounter = moleculeCounter;
		this.inputName = inputName;		
		this.predictions = predictions;
		
		this.rerunning = rerunning;
		
		this.somPredicted = false;
	}
	
	// constructor for user version
	public MetabolitePredictorWorker(String singleInputSmiles, Map<Integer, Prediction> predictions, 
			TestParameters testParameters, Globals fameParameters, final int moleculeCounter, String inputName, 
			List<Long> runTimes, Boolean rerunning) {
		
		this.inputSmiles = singleInputSmiles;
		this.originalInputSmiles = singleInputSmiles;
		this.testParameters = testParameters;
		this.fameParameters = fameParameters;
		this.moleculeCounter = moleculeCounter;
		this.inputName = inputName;
		this.predictions = predictions;
		this.runTimes = runTimes;
		
		this.rerunning = rerunning;
		
		this.somPredicted = false;
	}
	
	
	
	// key method
	@Override
	public void run() {
		
		Instant start = Instant.now();  // for measuring run time per compound
		
		logger.debug("input smiles: {}", inputSmiles);
		
		// check that input molecule contains at least TestParameters.METABOLITE_HEAVY_ATOM_CUTOFF (i.e. 3) heavy atoms and that it can be processed by FAME 3
		Boolean cantMakePrediction = checkInputMol();
		if (cantMakePrediction) {
			return; // no need to bother trying to make predictions 
		}
		
		inputSmiles = MoleculeManipulator.kekulizeMoleculeSmiles(inputSmiles);
			
		logger.debug("input smiles after kekulization: {}", inputSmiles);

		
		// first, calculate SoMs
		
		List<String> inputSmilesList = new ArrayList<>();
		inputSmilesList.add(inputSmiles);

		// Prediction with FAME 3
		SoMPredictor predictor = new SoMPredictor(moleculeCounter);
		logger.debug(RUNNING_FAME3, inputSmiles);
		IAtomContainerSet molecules = predictor.predict(inputSmilesList, fameParameters);
		
		if ( molecules == null || molecules.getAtomContainerCount() > 1) {
			logger.error(ERROR_ONLY_ONE_MOLECULE_SHOULD_BE_CREATED);
			return;
		} 
		
		IAtomContainerSet moleculesFameCantHandle = removeMoleculesFameCanNotHandle(molecules);  // for every molecule, check that the prediction worked for all atoms
		
		if (molecules.isEmpty()) {
						
			if (!moleculesFameCantHandle.isEmpty()) {
				
				Assert.isTrue(moleculesFameCantHandle.getAtomContainerCount() == 1, ERROR_ONLY_ONE_MOLECULE_SHOULD_BE_CREATED);
					
				// already checked for connectivity and unpermitted atoms, see above
				addDummyPredictionToMap(Errors.OTHER_FAME_ERROR);

				// Only one molecule was created, and it could not be handled by FAME 3. Add to results anyway in PredictionEvaluator (no predictions for this molecule).

			}  else {
				logger.error(INPUT_MOLECULE_COULD_NOT_BE_PROCESSED);
				addDummyPredictionToMap(Errors.COULD_NOT_PROCESS_INPUT_MOL);
			}
			return;
			
		} else {
			Assert.isTrue(moleculesFameCantHandle.isEmpty(), "The list of molecules FAME 3 can't handle should be empty.");
		}	
		
		molecule = molecules.getAtomContainer(0);
		
		// check whether any SoM was predicted for this molecule and save some info about predictions for analysis purposes
		somAnalysis();


		if (logger.isInfoEnabled()) {
			logger.info(PERFORMING_TRANSFORMATION_FOR_MOLECULE, (String) molecule.getProperty(Globals.ID_PROP), MoleculeManipulator.generateSmiles(molecule));
		}
		
		Transformer transformer = new Transformer(testParameters, fameParameters.model_name, rerunning);
		Set<PredictedMolecule> predictedMetabolites = transformer.transform(molecule);
		
		logger.info(NUMBER_OF_PREDICTED_PRODUCTS_UNEDITED, (String) molecule.getProperty(Globals.ID_PROP), predictedMetabolites.size());

		if (logger.isDebugEnabled()) {
			logger.debug(PARENT_SMILES, MoleculeManipulator.generateSmiles(molecule));
			logger.debug(TOTAL_NUMBER_OF_PRODUCTS, predictedMetabolites.size());
		}
		
		setParentForAllPredictedMetabolites(predictedMetabolites); // only the ID is set since the other information is contained in the ParentMolecule 
		ParentMolecule parent = new ParentMolecule();
		createParentMolForMap(parent);
		
		
		if (!predictions.containsKey(moleculeCounter)) {
			Prediction prediction = new Prediction(parent, predictedMetabolites);
			if (somPredicted) {
				prediction.setSomPredicted(somPredicted);
			}
			predictions.put(moleculeCounter, prediction);

		} else {
			
			Prediction p = predictions.get(moleculeCounter);
			
			if (somPredicted) {
				p.setSomPredicted(somPredicted);
			}
			
			Set<PredictedMolecule> alreadyPredicted = p.getPredictedMetabolites(); 
			
			if (alreadyPredicted == null) { // happens if prediction failed for previous model
				p.setPredictedMetabolites(predictedMetabolites);
				
			} else {
				for (PredictedMolecule predictedMol : predictedMetabolites) {
					
					PredictionHandler ph = new PredictionHandler();
					ph.addPredictedMoleculeIfNotInSetOrHasHigherScore(alreadyPredicted, predictedMol);
				}
			}
		}
		
		// for measuring run time per compound
		if (testParameters.getReference() == TestParameters.Reference.TEST_DATASET && testParameters.isUserVersion()) {
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			logger.info("time elapsed in ms: {}", timeElapsed);
			runTimes.add(timeElapsed);
		}
	}

	private void somAnalysis() {
		if (testParameters.isUserVersion()) {
			for (IAtom atom : molecule.atoms()) {
				if (!atom.getSymbol().equals("H") && 
						( (testParameters.useSoMsAsHardFilter() && (Double) atom.getProperty(Modeller.proba_yes_fld) >= testParameters.getSoMProbabilityCutoff())
							|| (!testParameters.useSoMsAsHardFilter() )  // && (Double) atom.getProperty(Modeller.proba_yes_fld) > 0.0 // TODO 
						) ) {
						somPredicted = true;
						break;
				}
			}
		} 
		else {
			calculateSomStats(); // TODO some of the things calculated are not currently saved in any way (they would have to be added to the Prediction)
		}
	}

	
	private void calculateSomStats() { // for evaluation purposes only
		double maxSomProb = 0;
		double[] somValues = new double[MoleculeManipulator.getHeavyAtomCount(molecule)];
		
		int counter = 0;
		for (IAtom atom : molecule.atoms()) {
			if (!atom.getSymbol().equals("H")) {
				
				if ( (testParameters.useSoMsAsHardFilter() && (Double) atom.getProperty(Modeller.proba_yes_fld) >= testParameters.getSoMProbabilityCutoff())
						|| (!testParameters.useSoMsAsHardFilter() )  // && (Double) atom.getProperty(Modeller.proba_yes_fld) > 0.0 // TODO 
					) {
					somPredicted = true;
				}
				
				Double somProb = atom.getProperty(Modeller.proba_yes_fld);
				somValues[counter] = somProb;
				if (somProb > maxSomProb) {
					maxSomProb = somProb;
				}
				counter ++;
			}
		}
		Median median = new Median();
		medianSomProbability = median.evaluate(somValues);
		maxSomProbability = maxSomProb;
		Arrays.sort(somValues);
		double[] topThree = null;
		if (somValues.length >= 3) {
			topThree = Arrays.copyOfRange(somValues, somValues.length-3, somValues.length);
		} else {
			topThree = Arrays.copyOfRange(somValues, 0, somValues.length);
		}
		
		List<Double> topThreeList = new ArrayList<>();
		for (double x : topThree) {
			topThreeList.add((Double) x);
		}
		if (topThreeList.size() < 3) {
			topThreeList.add(Double.NaN);
		}
		if (topThreeList.size() < 3) { // it's pretty silly to make predictions in this case, but that's another issue
			topThreeList.add(Double.NaN);
		}
		avgOfTop3SomProbabilities = Calculations.calculateAverage(topThreeList);
	}
	
	
	private void setParentForAllPredictedMetabolites(Set<PredictedMolecule> predictedMetabolites) { // not needed anymore because parent information is saved in a separate object
		
		for (PredictedMolecule m : predictedMetabolites) {
			m.setParentID(Integer.toString(moleculeCounter));
		}
	}

	
	private Boolean checkInputMol() {
		
		Boolean cantMakePrediction = false;
		IAtomContainer inputMolecule = MoleculeManipulator.generateMoleculeFromSmiles(inputSmiles);
		
		if (inputMolecule == null) {
			logger.error(INPUT_MOLECULE_COULD_NOT_BE_PROCESSED);
			addDummyPredictionToMap(Errors.COULD_NOT_PROCESS_INPUT_MOL);
			cantMakePrediction = true;
			return cantMakePrediction;
		}
		
		if (testParameters.isUserVersion() && !MoleculeManipulator.moleculeIsLargeEnough(inputMolecule, testParameters.getMetaboliteNumberOfHeavyAtomsCutoff())) {
			addDummyPredictionToMap(Errors.INPUT_MOLECULE_TOO_SMALL);
			cantMakePrediction = true;
			return cantMakePrediction;
		}
		
		Boolean containsUnpermittedAtom = checkIfContainsUnpermittedAtomTypeFromSmiles(inputMolecule);
		
		if (containsUnpermittedAtom || !ConnectivityChecker.isConnected(inputMolecule)) {
			if (containsUnpermittedAtom) {
				addDummyPredictionToMap(Errors.CONTAINS_UNPERMITTED_ATOM_TYPE);
			} 
			if (!ConnectivityChecker.isConnected(inputMolecule)) {
				addDummyPredictionToMap(Errors.MULTICOMPONENT_INPUT);
			}
			cantMakePrediction = true; // no need to bother trying to make predictions with FAME
		}
		return cantMakePrediction;
	}

	
	private Boolean checkIfContainsUnpermittedAtomTypeFromSmiles(IAtomContainer inputMolecule) {
		Boolean containsUnpermittedAtom = false;
		for (int atomNr = 0; atomNr < inputMolecule.getAtomCount()  ; atomNr++ ) {
			IAtom atom = inputMolecule.getAtom(atomNr);
			String symbol = atom.getSymbol();
			if (!SoMPredictor.allowed_atoms.contains(symbol)) {
				containsUnpermittedAtom = true;
				break;
			}
		}
		return containsUnpermittedAtom;
	}

	
	private void addDummyPredictionToMap(Errors error) {
		
		if (!predictions.containsKey(moleculeCounter)) {
			// create parent, set error, set whether prediction failed
			
			ParentMolecule parent = new ParentMolecule();
			
			if (error == Errors.COULD_NOT_PROCESS_INPUT_MOL) {
				parent.setSmiles(inputSmiles); 
			} else {
				createParentMolForMap(parent);
			}

			Prediction p = new Prediction(parent, null);
			p.addError(error);
			if (error == Errors.OTHER_FAME_ERROR) {
				p.setFameModelFailed(true);
			}
			predictions.put(moleculeCounter, p);
			
		} else {
			// parent already created so just set error and whether the prediction failed
			Prediction p = predictions.get(moleculeCounter);
			p.addError(error);
			if (error == Errors.OTHER_FAME_ERROR) {
				p.setFameModelFailed(true);
			}		
		}
	}

	
	private void createParentMolForMap(ParentMolecule parent) {
		
		parent.setId(Integer.toString(moleculeCounter));
		
		if (inputName == null || inputName.isEmpty()) {
			parent.setName("Molecule " + Integer.toString(moleculeCounter));
		} else {
			parent.setName(inputName);
		}
		
		parent.setSmiles(inputSmiles);
		parent.setInchi(MoleculeManipulator.generateInchiWithoutStereoFromSmiles(this.inputSmiles));
		
		if (testParameters.inputIsIndividualSmiles()) {
			parent.setOriginalInputSmiles(originalInputSmiles);
		}
		

		SoMPredictor sp = new SoMPredictor(moleculeCounter);
		IAtomContainer parentMol = sp.prepareMolecule(inputSmiles);
		
		parent.setMolecularWeight(AtomContainerManipulator.getNaturalExactMass(parentMol));
		parent.setHeavyAtomCount(MoleculeManipulator.getHeavyAtomCount(parentMol));
		
		return;
	}


	private IAtomContainerSet removeMoleculesFameCanNotHandle(IAtomContainerSet molecules) {
		
		IAtomContainerSet moleculesFameCantHandle = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);
		
		Iterator<IAtomContainer> moleculeIterator = molecules.atomContainers().iterator();
		while (moleculeIterator.hasNext()) {
			IAtomContainer mol = moleculeIterator.next();
			for (IAtom atom : mol.atoms()) {
				if (atom.getProperty(Modeller.is_som_fld) == null && atom.getAtomicNumber() > 1) {
					
					if (logger.isErrorEnabled()) {
						logger.error(ERROR_IN_FAME3_PREDICTION, (String) mol.getProperty(Globals.ID_PROP), MoleculeManipulator.generateSmiles(mol));
					}
					moleculeIterator.remove();
					moleculesFameCantHandle.addAtomContainer(mol);
					break;
				}
			}
		}
		return moleculesFameCantHandle;
	}


}
