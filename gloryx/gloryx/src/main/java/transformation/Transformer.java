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

package main.java.transformation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.modelling.Modeller;

import ambit2.smarts.EquivalenceTester;
import ambit2.smarts.IsomorphismTester;
import ambit2.smarts.SMIRKSManager;
import ambit2.smarts.SMIRKSReaction;
import ambit2.smarts.SmartsConst;
import ambit2.smarts.StereoChemUtils;
import main.java.metaboliteprediction.PredictionHandler;
import main.java.sompredictor.SoMPredictor;
import main.java.transformation.reactionrules.SyGMaTransformationsPhaseI;
import main.java.transformation.reactionrules.SyGMaTransformationsPhaseII;
import main.java.transformation.reactionrules.TransformationsGST;
import main.java.transformation.reactionrules.TransformationsMT;
import main.java.transformation.reactionrules.TransformationsNAT;
import main.java.transformation.reactionrules.TransformationsOtherPhaseII;
import main.java.transformation.reactionrules.TransformationsSULT;
import main.java.transformation.reactionrules.TransformationsUGT;
import main.java.transformation.reactionrules.GLORYTransformations;
import main.java.transformation.PriorityLevel;
import main.java.utils.Phase;
import main.java.utils.TestParameters;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.MoleculeValidityChecker;
import main.java.utils.molecule.PredictedMolecule;

/**
 * This class performs the transformations. 
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Transformer {


	private static final String ERROR_CONFIGURING_ATOMS_AND_AROMATICITY = "Error configuring atoms and aromaticity.";
	private static final String DEBUG_ATOM_SOM_PROBABILITY = "atom {}, SoM probability: {}";
	private static final String PREDICTION_WOULD_NOT_HAVE_BEEN_MADE_IF_HARD_CUTOFF_FOR_SOM_PROBABILITY = "This prediction would not have been made if a hard cutoff for SoM probability was used. Setting priority score to 0.";
	private static final String ERROR_PREPARING_PREDICTED_MOLECULE_FOR_ALDEHYDE_TO_CARBOXYLIC_ACID_TRANSFORM = "Error preparing predicted molecule for aldehyde to carboxylic acid transform {}";
	private static final String PERFORMING_TRANSFORMATION_FROM_SMILES = "Performing transformation from SMILES {}";
	private static final String NO_TRANSFORMATION_BECAUSE_MOLECULE_COULD_NOT_BE_CLONED = "No transformation could be performed on molecule {} because the molecule could not be cloned.";
	private static final String THERE_IS_NO_MAPPING_MESSAGE = "There is no mapping for SMIRKS {}";
	private static final String ERROR_PARSING_SMIRKS = "Error parsing SMIRKS {} for reaction type {}";
	private static final String ERROR_APPLYING_TRANSFORMATION_AT_LOCATION = "Error applying transformation {} at specific location (with cloning)";
	private static final String ERROR_PROCESSING_PRODUCT = "Error processing product of applyTransformationsAtLocationsWithCloning for transformation {}";
	private static final String NUMBER_OF_TOTAL_PRODUCTS_SO_FAR = "Number of total products so far: {}";
	private static final String PRODUCTS_FOUND_WITH_SMIRKS = "Product(s) found with SMIRKS: {}";
	private static final String ATOM_TYPE = "AtomType";
	private static final String MOLECULE_NAME = "Molecule";  // molecule ID
	private static final String PROPERTY_INFO = "Property {}: {}";
	private static final String PROPERTY_IS_NULL = "Property {} is null";
	private static final String ERROR_CLONING_MOLECULE = "Error cloning molecule {}";
	private static final String PROCESSING_TRANSFORMATIONS_FOR_NEW_MOLECULE = "Processing transformations for new molecule: {}";
	private static final String ERROR_APPLYING_SMIRKS_TRANSFORMATION = "Error applying SMIRKS transformation {} for molecule {}";
	private static final String PARENT = "Parent: {}";
	private static final String PARENT2 = "parent";
	
    public static final String IS_SOM_FIELD = "isSoM";
	private static final String ATOM_PROPERTY = "Atom"; // atom name/type
	private static final String HYDROGEN_SYMBOL = "H";

	private static final Logger logger = LoggerFactory.getLogger(Transformer.class.getName());
	

	private TestParameters testParameters;
	private String fame3model;
	private Boolean duplicateOverride = false; // slight misnomer. It actually means that priority scores from the previous run will override the one from this run, for a given predicted metabolite
	
	public Transformer(TestParameters testParameters, String fame3model) { 
		this.testParameters = testParameters;
		this.fame3model = fame3model;
	}
	
	public Transformer(TestParameters testParameters, String fame3model, Boolean duplicateOverride) { 
		this.testParameters = testParameters;
		this.fame3model = fame3model;
		this.duplicateOverride = duplicateOverride;
	}

	// Note on use of Ambit SMIRKS for transformations: If I didn't have to generate all mappings in a separate step in order to calculate the priority
	// scores, I would use this method: smrkMan.applyTransformationWithSingleCopyForEachPos with SmartsConst.SSM_MODE.SSM_ALL

	
	public Set<PredictedMolecule> transform(final IAtomContainer molecule) {

		logger.info(PROCESSING_TRANSFORMATIONS_FOR_NEW_MOLECULE, (String) molecule.getProperty(Globals.ID_PROP));

		// The parent is kekulized properly before FAME 3 prediction, because all input SMILES are kekulized before running FAME 3. 
		// Therefore it is not necessary to kekulize the parent molecule again here.
		// It IS necessary to detect aromaticity again, because FAME 3 uses the CDK Hueckel aromaticity model, which does not detect 
		//  aromaticity in rings with exocyclic heteroatoms involved in the aromaticity.
		redetectAromaticity(molecule);


		SMIRKSReaction transformation;
		final SMIRKSManager smrkMan = new SMIRKSManager(SilentChemObjectBuilder.getInstance()); 

		Set<PredictedMolecule> allProducts = new HashSet<>();

		logger.debug("model: {}", fame3model);
		
		// select reaction rules based on current phase
		Transformations[] reactionRules = selectAppropriateReactionRules();

		
		for (Transformations smirks : reactionRules) { 
			
//			if (smirks == null) { // it's a GST rule and has been left out for now // TODO remove once done with eval
//				continue;
//			}
			
			transformation = smrkMan.parse(smirks.getSMIRKS());

			if (smrkMan.hasErrors()) {
				logger.error(ERROR_PARSING_SMIRKS, smirks.getSMIRKS(), smirks.getName());
			}

			IAtomContainer product = initializeProduct(molecule);  // This involves cloning molecule. 
			String parentID = molecule.getProperty(Globals.ID_PROP);
			String parentInchi = MoleculeManipulator.generateInchiWithoutStereo(molecule);
				
			if (product == null) {
				logger.error(NO_TRANSFORMATION_BECAUSE_MOLECULE_COULD_NOT_BE_CLONED, (String) molecule.getProperty(Globals.ID_PROP));
				break;
			}
			
			// In order to calculate a priority score for each product, I need to know the SoM probability of each atom in the mapping. 
			// Therefore, I now generate the mappings and perform the transformations (as well as quite a bit of pre- and post-processing) 
			// individually instead of using the single method from AMBIT SMIRKS. 
			// During this process, I realized that using AllMappings (instead of NonIdenticalMappings) saves me from having to define multiple 
			// SMIRKS for the same reaction type in cases where the mapping is topologically symmetrical. I therefore changed to using AllMappings 
			// even in the case of not ranking the predicted metabolites.

			transformWithRankingPrep(transformation, smrkMan, allProducts, smirks, product, parentID, parentInchi);

		}

		return allProducts; 
	}

	private void redetectAromaticity(final IAtomContainer molecule) {
		
		MoleculeManipulator.addConvertToExplicitHydrogens(molecule);  // make all hydrogens explicit (first add any implicit hydrogens if necessary)		
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule); // this has already been done in FAME 3 code
			MoleculeManipulator.detectAromaticity(molecule); // this is VERY IMPORTANT
		} catch (CDKException e) {
			logger.error(ERROR_CONFIGURING_ATOMS_AND_AROMATICITY);
		}
	}

	private Transformations[] selectAppropriateReactionRules() {
		
		Transformations[] reactionRules = null;
		
		switch (testParameters.getPhase()) {
        case PHASE_1:
        		if (TestParameters.useSygmaRulesOnly()) {
                reactionRules = SyGMaTransformationsPhaseI.values();
        		} else {
        			reactionRules = combinePhase1Rules(); 
        		}
            break;
        case PHASE_2:  	
        		if (TestParameters.useSygmaRulesOnly()) {
            		reactionRules = SyGMaTransformationsPhaseII.values(); 
        		} else {
        			reactionRules = combinePhase2Rules();
        		}
	        break;
        case PHASES_1_AND_2:
        		reactionRules = combineAllRules();
        		break;
        case UGT:
        		reactionRules = TransformationsUGT.values();
        		break;
        case GST:
    			reactionRules = TransformationsGST.values();
    			break;
        case SULT:
    			reactionRules = TransformationsSULT.values();
    			break;
        case NAT:
    			reactionRules = TransformationsNAT.values();
    			break;
        case MT:
    			reactionRules = TransformationsMT.values();
    			break;
        case OTHER_PHASE2:
    			reactionRules = TransformationsOtherPhaseII.values();
    			break;
	    default:
	    		logger.error("Invalid phase specification. Correct reaction rule set cannot be determined.", testParameters.getPhase());
	    		System.exit(1);
	    		break;
		}
		return reactionRules;
	}

	private Transformations[] combinePhase1Rules() {
		
		int lenA = SyGMaTransformationsPhaseI.values().length;
		int lenB = GLORYTransformations.values().length;
     	
		Transformations[] reactionRules = (Transformations[]) Array.newInstance(Transformations.class, lenA + lenB);
         System.arraycopy(SyGMaTransformationsPhaseI.values(), 0, reactionRules, 0, lenA);
         System.arraycopy(GLORYTransformations.values(), 0, reactionRules, lenA, lenB);
		return reactionRules;
	}

	private Transformations[] combinePhase2Rules() {
		
		int len1 = TransformationsGST.values().length;
		int len2 = TransformationsMT.values().length;
		int len3 = TransformationsNAT.values().length;
		int len4 = TransformationsOtherPhaseII.values().length;
		int len5 = TransformationsSULT.values().length;
		int len6 = TransformationsUGT.values().length;

     	
		Transformations[] reactionRules = (Transformations[]) Array.newInstance(Transformations.class, len1 + len2 + len3 + len4 + len5 + len6);
         System.arraycopy(TransformationsGST.values(), 0, reactionRules, 0, len1);
         System.arraycopy(TransformationsMT.values(), 0, reactionRules, len1, len2);
         System.arraycopy(TransformationsNAT.values(), 0, reactionRules, len1 + len2, len3);
         System.arraycopy(TransformationsOtherPhaseII.values(), 0, reactionRules, len1 + len2 + len3, len4);
         System.arraycopy(TransformationsSULT.values(), 0, reactionRules, len1 + len2 + len3 + len4, len5);
         System.arraycopy(TransformationsUGT.values(), 0, reactionRules, len1 + len2 + len3 + len4 + len5, len6);
		return reactionRules;
	}
	
	
	private Transformations[] combineAllRules() {
		
		// phase 2
		int len1 = TransformationsGST.values().length;
		int len2 = TransformationsMT.values().length;
		int len3 = TransformationsNAT.values().length;
		int len4 = TransformationsOtherPhaseII.values().length;
		int len5 = TransformationsSULT.values().length;
		int len6 = TransformationsUGT.values().length;

		// phase 1
		int lenA = SyGMaTransformationsPhaseI.values().length;
		int lenB = GLORYTransformations.values().length;

		Transformations[] reactionRules = (Transformations[]) Array.newInstance(Transformations.class, len1 + len2 + len3 + len4 + len5 + len6 + lenA + lenB);
		System.arraycopy(TransformationsGST.values(), 0, reactionRules, 0, len1);
		System.arraycopy(TransformationsMT.values(), 0, reactionRules, len1, len2);
		System.arraycopy(TransformationsNAT.values(), 0, reactionRules, len1 + len2, len3);
		System.arraycopy(TransformationsOtherPhaseII.values(), 0, reactionRules, len1 + len2 + len3, len4);
		System.arraycopy(TransformationsSULT.values(), 0, reactionRules, len1 + len2 + len3 + len4, len5);
		System.arraycopy(TransformationsUGT.values(), 0, reactionRules, len1 + len2 + len3 + len4 + len5, len6);
		System.arraycopy(SyGMaTransformationsPhaseI.values(), 0, reactionRules, len1 + len2 + len3 + len4 + len5 + len6, lenA);
		System.arraycopy(GLORYTransformations.values(), 0, reactionRules, len1 + len2 + len3 + len4 + len5 + len6 + lenA, lenB);

		return reactionRules;
	}
	
	
	public IAtomContainerSet performTransformationFromSmiles(String smiles, String smirks) { 
		// SoMS are not predicted
		// used to check case of spontaneous oxidation from aldehyde to carboxylic acid in eval
		
		logger.debug(PERFORMING_TRANSFORMATION_FROM_SMILES, smiles);
		
		IAtomContainer predictedMolecule = MoleculeManipulator.generateMoleculeFromSmiles(smiles);
		predictedMolecule.setProperty(Globals.ID_PROP, "");
		SoMPredictor predictor = new SoMPredictor(0);
		try {
			predictor.prepareMolecule(predictedMolecule);
		} catch (CDKException e1) {
			logger.error(ERROR_PREPARING_PREDICTED_MOLECULE_FOR_ALDEHYDE_TO_CARBOXYLIC_ACID_TRANSFORM, smiles);
		}
		MoleculeManipulator.addConvertToExplicitHydrogens(predictedMolecule);
		
		
		final SMIRKSManager smrkMan = new SMIRKSManager(SilentChemObjectBuilder.getInstance()); 
		SMIRKSReaction transformation= smrkMan.parse(smirks);
		IAtomContainer product = initializeProduct(predictedMolecule);

		return performTransformationsNoSoms(transformation, smrkMan, null, product, smirks);
	}


	private void transformWithRankingPrep(final SMIRKSReaction transformation, final SMIRKSManager smrkMan, Set<PredictedMolecule> allProducts, 
			final Transformations smirks, IAtomContainer product, final String parentID, final String parentInchi) {
		
		EquivalenceTester eqTester = prepareToApplyTransformation(transformation, smrkMan, product);

		List<List<IAtom>> allMappings = smrkMan.getAllMappings(product);  
		if (allMappings != null && !allMappings.isEmpty()) {

			allMappings = filterEquivalentMappingsIfSpecified(smrkMan, product, eqTester, allMappings);
			transformAndScoreForAllMappings(transformation, smrkMan, allProducts, smirks, product, allMappings, parentID, parentInchi);

		} else {
			logger.debug(THERE_IS_NO_MAPPING_MESSAGE, smirks.getName());
		}
		
		return;
	}


	private void transformAndScoreForAllMappings(SMIRKSReaction transformation, final SMIRKSManager smrkMan, Set<PredictedMolecule> allProducts, 
			Transformations smirks, IAtomContainer product, List<List<IAtom>> allMappings, final String parentID, final String parentInchi) { 

		for (List<IAtom> mapping : allMappings) {

			Double maxSoMProbability = getMaxSoMProbabilityInMapping(mapping);  // get the maximum SoM probability in the mapping

			IAtomContainer newProduct = doTransformationAndProcessProduct(transformation, smirks.getName(), smrkMan, product, mapping);
			
			if (newProduct == null) {					
				continue;
			}
//			
//			if (MoleculeManipulator.generateInchiWithoutStereo(newProduct).equals(MoleculeManipulator.generateInchiWithoutStereo(product))) {
//				logger.info("Prediction has same InChI as parent. Skipping.");
//				continue;
//			} // TODO do this for all components of newProduct!!
			
			// VALIDITY CHECK
			if (MoleculeValidityChecker.excludeDueToCarbonValence(newProduct, smirks.getName())) {
				continue;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("got {} from {}", MoleculeManipulator.generateSmilesWithoutStereo(newProduct), smirks.getName());
				logger.debug(PARENT, MoleculeManipulator.generateSmiles(product, PARENT2));
				logger.debug(NUMBER_OF_TOTAL_PRODUCTS_SO_FAR, allProducts.size());
			}

			newProduct.setProperty(Globals.ID_PROP, parentID);
			
			calculateAndSetPriorityScore(maxSoMProbability, newProduct, smirks);  // calculate prediction score and add to product as property

			// add product to set if not already there or if this prediction has a higher prediction score than the same product that is already in the set
			addProductToSetIfNotAlreadyIn(allProducts, newProduct, parentInchi);
		}
	}
	


	private void calculateAndSetPriorityScore(final Double maxSoMProbability, IAtomContainer newProduct, final Transformations smirks) {
		
		// This was used for GLORY when where were different reaction type priority levels.
//		if (smirks.getPriorityLevel() == null) {
//			logger.info("No priority level set for transformation (): {}. Priority score will not be calculated.", smirks.getName());
//
//			return;
//		}
		
		if (!testParameters.isUserVersion() && newProduct.getProperty(PredictedMolecule.MADE_SOM_CUTOFF_PROPERTY) != null 
				&& (! (Boolean) newProduct.getProperty(PredictedMolecule.MADE_SOM_CUTOFF_PROPERTY))) {
			
				logger.info(PREDICTION_WOULD_NOT_HAVE_BEEN_MADE_IF_HARD_CUTOFF_FOR_SOM_PROBABILITY);
				
				newProduct.setProperty(PredictedMolecule.PRIORITY_SCORE_PROPERTY, (double) 0);
			
		} else {
			
			Double priorityScore = (double) 0;
			
//			// define the priority score differently depending on phase - TODO this could change later
//			if (testParameters.getPhase() == Phase.PHASE_2) {
//				if (smirks.getLikelihood() != null) {
//					priorityScore = maxSoMProbability;
////					priorityScore = maxSoMProbability * smirks.getLikelihood();
//				} else {
//					logger.error("The likelihood for reaction {} is null! The priority score will be 0.", smirks.getName());
//				}
//				
//			} else 
			
				
//			if (testParameters.getPhase() == Phase.PHASE_1) { 
//				priorityScore = maxSoMProbability; 
				
			PriorityLevel reactionTypePriority = smirks.getPriorityLevel();
			Double reactionTypePriorityFactor = reactionTypePriority.getFactor(); // (double) 1; //
			priorityScore = reactionTypePriorityFactor * maxSoMProbability;  // define/calculate priority score
				
//			} else { //if (testParameters.getPhase().isSpecificEnzymeFamily()) {
//				
//				
//				PriorityLevel reactionTypePriority = smirks.getPriorityLevel();
//				Double reactionTypePriorityFactor = reactionTypePriority.getFactor();
//				priorityScore = reactionTypePriorityFactor * maxSoMProbability;  // define/calculate priority score
//				
//				
////				priorityScore = maxSoMProbability;
//				
//				priorityScore = maxSoMProbability * smirks.getLikelihood(); // TODO
//				priorityScore = (maxSoMProbability * 2) + ( smirks.getLikelihood() ) / 3; // TODO

////				priorityScore = smirks.getLikelihood(); // TODO careful! this will fail for GST
//
//			} 
//			else {
//				logger.error("Cannot calculate a priority score for combined Phase1+2 prediction");
//			}
						
//			logger.debug("priority score {}", priorityScore);
			
			newProduct.setProperty(PredictedMolecule.PRIORITY_SCORE_PROPERTY, priorityScore);
			
		}

		newProduct.setProperty(PredictedMolecule.TRANSFORMATION_NAME_PROPERTY, smirks.getName());
		newProduct.setProperty(PredictedMolecule.PHASE_PROPERTY, smirks.getPhase());
		
		return;
	}
	

	private IAtomContainer doTransformationAndProcessProduct(SMIRKSReaction transformation, String transformationName, 
			final SMIRKSManager smrkMan, IAtomContainer product, List<IAtom> mapping) {

		IAtomContainer newProduct = null;
		List<List<IAtom>> dummyListOfMappings = new ArrayList<>();
		dummyListOfMappings.add(mapping);
		
		Boolean acceptable = true;
		Boolean madeCutoff = true;
		
		if (testParameters.useSoMsAsHardFilter()) {
							
			// Have to manually use implementation of the IAcceptable functionality, basically, because 
			// there is no existing SMIRKSManager method that both uses IAcceptable and allows specification of a specific mapping. 
			
			AtomSelector atomSelector = new AtomSelector(testParameters.getSoMProbabilityCutoff());
			acceptable = atomSelector.accept(mapping);

			// if user version, leave as is. otherwise, predict all products but set the score to 0 if it doesn't make the SoM probability cutoff.
			if (!testParameters.isUserVersion()) {
				madeCutoff = acceptable;
				acceptable = true;
			}

		}
		
		if (acceptable) { // always true if not using hard filter
			try {
				newProduct = smrkMan.applyTransformationsAtLocationsWithCloning(product, dummyListOfMappings, transformation);
			} catch (Exception e) {
				logger.info(ERROR_APPLYING_TRANSFORMATION_AT_LOCATION, transformationName, e);
				return null;
			}
		}

		if (newProduct != null) {
			checkAndSetForNewProduct(transformationName, smrkMan, newProduct, madeCutoff);
		}
		
		return newProduct;
	}

	
	private void checkAndSetForNewProduct(String transformationName, final SMIRKSManager smrkMan, IAtomContainer newProduct, Boolean madeCutoff) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("product before kekulizing: {}", MoleculeManipulator.generateSmiles(newProduct));
		}
		
		// there is an error and aromaticity is getting lost, so first kekulize, then process including detecting aromaticity (not sure if that makes a difference, but kekulizing is key)
		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(SilentChemObjectBuilder.getInstance());
		try {
			adder.addImplicitHydrogens(newProduct);
		} catch (CDKException e1) {
			logger.error("Error adding implicit hydrogens to product {}", MoleculeManipulator.generateSmiles(newProduct));
		}
		try {
    			Kekulization.kekulize(newProduct);
    			
		} catch (CDKException e) {
			logger.error("Error kekulizing product {}", MoleculeManipulator.generateSmiles(newProduct));
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("product after kekulizing: {}", MoleculeManipulator.generateSmiles(newProduct));
		}
		
		if (smrkMan.isFlagProcessResultStructures()) {
			try {
				smrkMan.processProduct(newProduct);
			} catch (Exception e) {
				logger.error(ERROR_PROCESSING_PRODUCT, transformationName, e);
			}
		}
		if (smrkMan.isFlagCheckResultStereo()) {
			StereoChemUtils.checkStereoElements(newProduct);
		}
		
		newProduct.setProperty(PredictedMolecule.TRANSFORMATION_NAME_PROPERTY, transformationName);
		
		if (logger.isDebugEnabled()) {
			logger.debug("product after processing: {}", MoleculeManipulator.generateSmiles(newProduct));
		}
		
		if (!testParameters.isUserVersion() && !madeCutoff) {
				
			// set property so that it's clear that the SoM probability cutoff was not reached
			newProduct.setProperty(PredictedMolecule.MADE_SOM_CUTOFF_PROPERTY, false);
		}
	}


	private List<List<IAtom>> filterEquivalentMappingsIfSpecified(final SMIRKSManager smrkMan, IAtomContainer product, EquivalenceTester eqTester, List<List<IAtom>> allMappings) {
		
		if (smrkMan.isFlagFilterEquivalentMappings()) {
			eqTester.setTarget(product);
			eqTester.quickFindEquivalentTerminalHAtoms();
			allMappings = eqTester.filterEquivalentMappings(allMappings);
		}
		return allMappings;
	}


	@SuppressWarnings("static-access")
	private EquivalenceTester prepareToApplyTransformation(SMIRKSReaction transformation, final SMIRKSManager smrkMan, IAtomContainer product) {

		setFlagsForSMIRKSManager(smrkMan);

		// VERY IMPORTANT TO IGNORE/SUPPRESS THE JAVA WARNING FOR THE FOLLOWING LINE! Must get the specific SmartsParser for this instance of the SMIRKSManager!
		smrkMan.getSmartsParser().prepareTargetForSMARTSSearch(transformation.reactantFlags, product);  
		
		if (transformation.reactantFlags.hasRecursiveSmarts) {
			smrkMan.mapRecursiveAtomsAgainstTarget(transformation.reactantRecursiveAtoms, product);
		}
		IsomorphismTester isoTester = smrkMan.getIsomorphismTester(); 
		isoTester.setQuery(transformation.reactant);

		return new EquivalenceTester();
	}


	private void setFlagsForSMIRKSManager(final SMIRKSManager smrkMan) {
		smrkMan.setFlagFilterEquivalentMappings(false); // appears based on simple testing to have no effect
		smrkMan.setFlagClearImplicitHAtomsBeforeResultProcess(true); // recommended (see source code)
		smrkMan.setFlagClearAromaticityBeforeResultProcess(true); // recommended (see source code)
		smrkMan.setFlagClearHybridizationBeforeResultProcess(true); // recommended (see source code)
		smrkMan.setFlagAddImplicitHAtomsOnResultProcess(true);
		smrkMan.setFlagCheckAromaticityOnResultProcess(true); 
		smrkMan.setFlagConvertAddedImplicitHToExplicitOnResultProcess(true);
		smrkMan.setFlagCheckResultStereo(true);
		smrkMan.setFlagProcessResultStructures(true);
		smrkMan.setFlagApplyStereoTransformation(true); // this way stereochemistry may be changed by SMIRKS
	}


	private Double getMaxSoMProbabilityInMapping(List<IAtom> mapping) {
		Double maxSoMProbability = (double) 0;
		for (IAtom atom : mapping) {

			if (!atom.getSymbol().equals(HYDROGEN_SYMBOL)) {  // note: no H atoms should be in the mapping anyway, but this is to double-check

				if (logger.isDebugEnabled()) {
					logger.debug(DEBUG_ATOM_SOM_PROBABILITY, atom.getProperty(ATOM_PROPERTY), (Double) atom.getProperty(Modeller.proba_yes_fld));
				}

				if ((Double) atom.getProperty(Modeller.proba_yes_fld) > maxSoMProbability) {
					maxSoMProbability = atom.getProperty(Modeller.proba_yes_fld);
				}
			}
		}
		return maxSoMProbability;
	}


	private void addProductToSetIfNotAlreadyIn(Set<PredictedMolecule> products, final IAtomContainer product, final String parentInchi) {
		// If the product is already in the set, it is checked whether the priority score is higher or lower 
		// than the one recorded in the set. If the priority score of the current product is higher, then 
		// the product in the set is replaced with the current product.
		// Since the products predicted from transformations may be multicomponent 'molecules', a check for 
		// multicomponent products is imlemented using SMILES ("." separates components) and all components are considered
		// individually. 
		
		if (MoleculeManipulator.generateInchiWithoutStereo(product).equals(parentInchi)) {
			logger.warn("The parent molecule was predicted as a metabolite. Not including!");
			return;
		}
		
		PredictedMolecule predicted = MoleculeManipulator.convertIAtomContainerToPredictedMoleculeWithoutStereochemistry(product);
		
		PredictionHandler ph = new PredictionHandler();
		Set<PredictedMolecule> predictedComponents = ph.getComponentsOfMoleculeWithoutStereochemistry(testParameters, predicted, product);  // check if multi-component (i.e. "." in SMILES string)
			
		for (PredictedMolecule predictedComponent : predictedComponents) {

			// make sure not to include any component that is the same as the parent molecule
			if (MoleculeManipulator.generateInchiWithoutStereoFromSmiles(predictedComponent.getSmiles()).equals(parentInchi)) {
				logger.warn("The parent molecule was predicted as one component of a metabolite. Not including!");
				continue;
			} 

			// could be the case that two components are the same, and in that case they should not both be added
			
			if (duplicateOverride) { //special case of redoing phase 2 predictions using the general P2 model
				
				logger.debug("only adding prediction if not in set");
				ph.addPredictedMoleculeIfNotInSet(products, predictedComponent);
				
			} else { //normal usage
				ph.addPredictedMoleculeIfNotInSetOrHasHigherScore(products, predictedComponent);
			}
		}
	}


	public IAtomContainerSet performTransformationsNoSoms(final SMIRKSReaction transformation, final SMIRKSManager smrkMan, IAtomContainerSet products, IAtomContainer product, String transformationName) {
		// perform transformations, without taking SoMs into account, and get a separate IAtomContainer for each product
		try {
			products = smrkMan.applyTransformationWithSingleCopyForEachPos(product, null, transformation, SmartsConst.SSM_MODE.SSM_ALL);

		} catch (Exception e) {
			logger.error(ERROR_APPLYING_SMIRKS_TRANSFORMATION, transformationName, product.getProperty(Globals.ID_PROP), e);
		}
		return products;
	}


	public IAtomContainer initializeProduct(final IAtomContainer molecule) {

		IAtomContainer product = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
		try {
			product = molecule.clone();
		} catch (CloneNotSupportedException e) {
			logger.error(ERROR_CLONING_MOLECULE, molecule.getProperty(Globals.ID_PROP), e);
			return null;
		} catch (OutOfMemoryError error) {
			logger.error(ERROR_CLONING_MOLECULE, molecule.getProperty(Globals.ID_PROP), error);
			return null;
		}
		product.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP));
		return product;
	}


	public void logAllAtomProperties(IAtomContainer molecule) {
		// logger has to be set to debug in order to see this output

		List<String> properties = new ArrayList<>(Arrays.asList(
				MOLECULE_NAME
				, ATOM_PROPERTY
				, Modeller.is_som_fld  // whether the atom is predicted to be a SoM or not
				, Modeller.proba_yes_fld  // may later be interested in this, whether it correlates to correct metabolite prediction
				, Modeller.proba_no_fld
				, ATOM_TYPE
				));

		for (IAtom atom : molecule.atoms()) {
			if (atom.getSymbol().equals(HYDROGEN_SYMBOL)) {
				continue;
			}
			for (String prop_name : properties) {
				Object prop = atom.getProperty(prop_name);
				if (prop == null) { 
					logger.debug(PROPERTY_IS_NULL, prop_name);
				} else {
					logger.debug(PROPERTY_INFO, prop_name, prop);
				}
			}
		}
	}


}
