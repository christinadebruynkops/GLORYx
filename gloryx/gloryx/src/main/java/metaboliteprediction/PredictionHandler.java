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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import main.java.utils.TestParameters;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.PredictedMolecule;

/**
 * This class manipulates the predictions.
 *  
 * @author Christina de Bruyn Kops
 *
 */
public class PredictionHandler {

	private static final String EXISTING_PREDICTED_METABOLITES_SHOULD_NOT_BE_NULL = "Existing predicted metabolites should not be null.";
	private static final String PRODUCT_ALREADY_IN_SET_WITH_LOWER_SCORE = "Product already in set but with lower score. Replacing.";
	private static final String PREDICTED_MOLECULE_HAS_NO_INCHI = "Predicted molecule has no InChI.";
	private static final String NOT_ADDING_PRODUCT_TO_SET_BECAUSE_NO_INCHI_COULD_BE_GENERATED = "Not adding product {} from transformation {} to set because no InChI could be generated.";
	private static final String PRODUCT_ALREADY_IN_SET_WITH_HIGHER_SCORE = "Not adding product to set because it's already in the set but with a higher priority score";
	private static final String PERIOD = "\\.";
	private static final String NULL_INCHI_FOR_PREDICTED_SMILES = "Null InChI for predicted smiles {}";

	
	private static final double THRESHOLD = .000001; // used when checking whether two numbers are equal
	
	private static final Logger logger = LoggerFactory.getLogger(PredictionHandler.class.getName());

	
	/**
	 * Adds a predicted metabolite if it is not already in the set or if it has a higher priority score than the version already in the set.
	 * <p>
	 * WARNING: InChI from predictedComponent used as is. No change to whether or not stereochemistry information is contained.
	 * 
	 * @param products
	 * @param predictedComponent
	 */
	public void addPredictedMoleculeIfNotInSetOrHasHigherScore(Set<PredictedMolecule> products, final PredictedMolecule predictedComponent) {
		
		Assert.notNull(products, EXISTING_PREDICTED_METABOLITES_SHOULD_NOT_BE_NULL);
		
		if (predictedComponent.getInchi() == null) {
			logger.info(NOT_ADDING_PRODUCT_TO_SET_BECAUSE_NO_INCHI_COULD_BE_GENERATED, predictedComponent.getParentID(), predictedComponent.getTransformationName());
		}
		
		Boolean toAdd = true;
		
		Iterator<PredictedMolecule> itr = products.iterator();
		while (itr.hasNext()) {
			PredictedMolecule mol = itr.next();
			
			if (mol.getInchi() == null) {
				logger.warn(PREDICTED_MOLECULE_HAS_NO_INCHI);
			} else {
				if (mol.getInchi().equals(predictedComponent.getInchi())) {
					
					if ((Double) mol.getPriorityScore() >= (Double) predictedComponent.getPriorityScore()) {
						toAdd = false;
						logger.debug(PRODUCT_ALREADY_IN_SET_WITH_HIGHER_SCORE);
						
					} else if ((Double) mol.getPriorityScore() < (Double) predictedComponent.getPriorityScore()) {
						logger.debug(PRODUCT_ALREADY_IN_SET_WITH_LOWER_SCORE);
						itr.remove();  // remove from set since same product but lower priority score. Will add back in the one with the higher score later.
					}
					break; // because there are no duplicates in predicted set
				}
			}
		}
		if (toAdd) {  
			products.add(predictedComponent);
		} 
		return;
	}
	
	
	/**
	 * Adds a predicted metabolite if it is not already in the set.
	 * <p>
	 * WARNING: InChI from predictedComponent used as is. No change to whether or not stereochemistry information is contained.
	 * 
	 * @param products
	 * @param predictedComponent
	 */
	public void addPredictedMoleculeIfNotInSet(Set<PredictedMolecule> products, final PredictedMolecule predictedComponent) {
		
		Assert.notNull(products, EXISTING_PREDICTED_METABOLITES_SHOULD_NOT_BE_NULL);
		
		if (predictedComponent.getInchi() == null) {
			logger.info(NOT_ADDING_PRODUCT_TO_SET_BECAUSE_NO_INCHI_COULD_BE_GENERATED, predictedComponent.getParentID(), predictedComponent.getTransformationName());
		}
		
		Boolean toAdd = true;
		
		Iterator<PredictedMolecule> itr = products.iterator();
		while (itr.hasNext()) {
			PredictedMolecule mol = itr.next();
			
			if (mol.getInchi() == null) {
				logger.warn(PREDICTED_MOLECULE_HAS_NO_INCHI);
			} else {
				if (mol.getInchi().equals(predictedComponent.getInchi())) {
					
					// predicted metabolite is already in the set
					toAdd = false;
					break; // because there are no duplicates in predicted set
				}
			}
		}
		if (toAdd) {  
			logger.debug("adding molecule to set");
			products.add(predictedComponent);
		} 
		return;
	}

	
	/**
	 * Ranks the predicted metabolites based on their priority scores.
	 * Ties are allowed.
	 * 
	 * @param predictedMetabolites
	 * @return
	 */
	public List<PredictedMolecule> rankPredictedMetabolites(Set<PredictedMolecule> predictedMetabolites) {
		
		List<PredictedMolecule> predictedMetabolitesList = predictedMetabolites.stream().collect(Collectors.toCollection(ArrayList::new));
		Collections.sort(predictedMetabolitesList);
		
		int prevRank = 0;
		Double prevScore = (double) 0;
		
		for (int i = 0; i < predictedMetabolitesList.size(); i++) {
		
			logger.debug("priority score: {} for predicted metabolite {}", predictedMetabolitesList.get(i).getPriorityScore(), predictedMetabolitesList.get(i).getSmiles());
			
			
			if (i > 0) {
				prevRank = predictedMetabolitesList.get(i-1).getRank();
				prevScore = predictedMetabolitesList.get(i-1).getPriorityScore();
			}
			
			if (Math.abs(prevScore - predictedMetabolitesList.get(i).getPriorityScore()) < THRESHOLD) {  // important note: consider two doubles equal if the difference is below a certain threshold
				predictedMetabolitesList.get(i).setRank(prevRank);
				logger.debug("tied predictions");
			} else {
				predictedMetabolitesList.get(i).setRank(i + 1);
			}
			
			logger.debug("rank: {}", predictedMetabolitesList.get(i).getRank());
			
		}
		return predictedMetabolitesList;
	}
	
	
	public Set<PredictedMolecule> getComponentsOfMoleculeWithoutStereochemistry(final TestParameters testParameters, final PredictedMolecule molecule, IAtomContainer iAtomContainer) {
		// if molecule is actually made up of more than one molecule, process all component molecules

		Set<PredictedMolecule> molecules = new HashSet<>();
		
		Double priorityScore = molecule.getPriorityScore();
		String transformationName = molecule.getTransformationName();
		String parentID = molecule.getParentID();
		Boolean madeSoMCutoff = molecule.getMadeSoMCutoff();
		
		final String wholeSmiles = molecule.getSmiles();
		final String[] smilesParts = wholeSmiles.split(PERIOD);

		if (smilesParts.length > 1) {
						
			for (String componentSmiles : smilesParts) {
				processAndAddMolecule(testParameters, molecules, priorityScore, transformationName, parentID, madeSoMCutoff, componentSmiles);
			}
			
		} else {
			processAndAddMolecule(testParameters, molecules, priorityScore, transformationName, parentID, madeSoMCutoff, wholeSmiles, iAtomContainer);
		}
		return molecules;
	}
	
	 // TODO
	public Set<PredictedMolecule> getComponentsOfMoleculeWithoutStereochemistry(final TestParameters testParameters, final PredictedMolecule molecule) {
		// if molecule is actually made up of more than one molecule, process all component molecules

		Set<PredictedMolecule> molecules = new HashSet<>();
		
		Double priorityScore = molecule.getPriorityScore();
		String transformationName = molecule.getTransformationName();
		String parentID = molecule.getParentID();
		Boolean madeSoMCutoff = molecule.getMadeSoMCutoff();
		
		final String wholeSmiles = molecule.getSmiles();
		final String[] smilesParts = wholeSmiles.split(PERIOD);

		if (smilesParts.length > 1) {
						
			for (String componentSmiles : smilesParts) {
				processAndAddMolecule(testParameters, molecules, priorityScore, transformationName, parentID, madeSoMCutoff, componentSmiles);
			}
			
		} else {
			processAndAddMolecule(testParameters, molecules, priorityScore, transformationName, parentID, madeSoMCutoff, wholeSmiles);
		}
		return molecules;
	}


	private void processAndAddMolecule(final TestParameters testParameters, Set<PredictedMolecule> molecules, final Double priorityScore, final String transformationName, 
			final String parentID, final Boolean madeSoMCutoff, final String smiles) {
		
		if (!MoleculeManipulator.checkIfOnlyHydrogens(smiles) && 
				( (testParameters.getMetaboliteNumberOfHeavyAtomsCutoff() > 0 && MoleculeManipulator.moleculeIsLargeEnough(smiles, testParameters.getMetaboliteNumberOfHeavyAtomsCutoff())) 
						|| (testParameters.getMetaboliteNumberOfHeavyAtomsCutoff() == 0) ) ) { // TODO don't need the second part of the OR
				
			String inchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(smiles);
			if (inchi != null) {
				
				PredictedMolecule mol = createComponent(priorityScore, transformationName, parentID, madeSoMCutoff, smiles, inchi);
				molecules.add(mol);
				
			}  else {
				logger.info(NULL_INCHI_FOR_PREDICTED_SMILES, smiles);
			}

		}
	}
	
	private void processAndAddMolecule(final TestParameters testParameters, Set<PredictedMolecule> molecules, final Double priorityScore, final String transformationName, 
			final String parentID, final Boolean madeSoMCutoff, final String smiles, IAtomContainer molecule) {
		
		if (!MoleculeManipulator.checkIfOnlyHydrogens(smiles) && 
				( (testParameters.getMetaboliteNumberOfHeavyAtomsCutoff() > 0 && MoleculeManipulator.moleculeIsLargeEnough(smiles, testParameters.getMetaboliteNumberOfHeavyAtomsCutoff())) 
						|| (testParameters.getMetaboliteNumberOfHeavyAtomsCutoff() == 0) ) ) { // TODO don't need the second part of the OR
				
			String inchi = MoleculeManipulator.generateInchiWithoutStereo(molecule);
			if (inchi != null) {
				
				PredictedMolecule mol = createComponent(priorityScore, transformationName, parentID, madeSoMCutoff, smiles, inchi);
				molecules.add(mol);
				
			}  else {
				logger.info(NULL_INCHI_FOR_PREDICTED_SMILES, smiles);
			}

		}
	}


	private PredictedMolecule createComponent(final Double priorityScore, final String transformationName, final String parentID, 
			final Boolean madeSoMCutoff, final String smiles, final String inchi) {
		
		PredictedMolecule mol = new PredictedMolecule();
		mol.setInchi(inchi);
		mol.setSmiles(smiles);
		
		if (priorityScore != null) {
			mol.setPriorityScore(priorityScore);
		}
		if (transformationName != null) {
			mol.setTransformationName(transformationName);
		}
		if (parentID != null) {
			mol.setParentID(parentID);
		}
		if (madeSoMCutoff != null) {
			mol.setMadeSoMCutoff(madeSoMCutoff);
		}
		return mol;
	}
	
	
	
	
}
