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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;

/**
 * This class is used to calculate the overlaps between different datasets.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class GenericOverlapCalculator {
	
	private static final String TOTAL_NUMBER_OF_PARENT_MOLECULES = "Total number of parent molecules: {}";
	private static final String NUMBER_OF_OVERLAPPING_PARENT_MOLECULES = "Number of overlapping parent molecules: {}";
	private static final String NUMBER_OF_EXTRA_METABOLITES = "Number of extra metabolites in the other dataset for these overlapping parent compounds: {}";
	private static final String NUMBER_OF_OVERLAPPING_REFERENCE_DATASET_METABOLITES = "Number of overlapping reference dataset metabolites for the overlapping parent compounds: {} ({} metabolites total)";
	private static final String DRUG_BANK_PARENT_MOLECULE_THAT_IS_ALSO_IN_THE_OTHER_DATASET = "Reference dataset parent molecule that is also in the other dataset: {} with SMILES {} and InChI {}";
	private static final String UTILITY_CLASS = "Utility class";
	
	private static final Logger logger = LoggerFactory.getLogger(GenericOverlapCalculator.class.getName());
	
	
	private GenericOverlapCalculator() {
	    throw new IllegalStateException(UTILITY_CLASS);
	  }
	

	/**
	 * Used e.g. to calculate overlap between DrugBank and MetXBioDB.
	 * 
	 * @param referenceDataset
	 * @param otherMetabolismData
	 * @return set of InChIs
	 */
	public static Set<String> getOverlapBetweenOtherDatasetAndReferenceDatasetWithoutChangingInchi(Map<BasicMolecule, Set<BasicMolecule>> referenceDataset, 
			Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData) {
		
		// compare molecules from both datasets
		return compareParentsWithoutChangingInchi(referenceDataset, otherMetabolismData, true);	
	
	}
	

	/**
	 * Used e.g. to calculate overlap between DrugBank and the manually curated new test dataset.
	 * <p>
	 * BEWARE: referenceDataset must not have stereochemistry information encoded in its InChIs, because stereochemistry is removed from the InChIs in the other dataset. 
	 * Otherwise, the result of this comparison will be inaccurate.
	 * 
	 * @param referenceDataset reference dataset; InChIs must not include stereochemistry information
	 * @param otherMetabolismData other dataset; stereochemistry information will be removed in this method
	 * @return
	 */
	public static Set<String> getOverlapAndGenerateInchiWithoutStereochemistryForOtherDataset(Map<BasicMolecule, Set<BasicMolecule>> referenceDataset, 
			Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData) {
			
		// first create a list of all InChIs without stereochemistry from the new dataset
		Set<String> testDbInchisNoStereo = getInchisWithoutStereochemistry(otherMetabolismData);
		
		// compare molecules from both datasets
		return compareParentsWithoutChangingInchi(referenceDataset, testDbInchisNoStereo);
	}

	
	/**
	 * BEWARE: If one dataset has stereochemistry in the InChIs and the other does not, the result of this comparison will be inaccurate!
	 * 
	 * @param mainDataset
	 * @param otherMetabolismDataInchis
	 * @return
	 */
	private static Set<String> compareParentsWithoutChangingInchi(Map<BasicMolecule, Set<BasicMolecule>> mainDataset, Set<String> otherMetabolismDataInchis) {
		
		Set<String> overlapDrugbankIds = new HashSet<>();
		
		for (BasicMolecule parent : mainDataset.keySet()) {
			
			for (String testInchiNoStereo : otherMetabolismDataInchis) {
				if (parent.getInchi().equals(testInchiNoStereo)) {
					
					overlapDrugbankIds.add(parent.getId());
					
					logger.info(DRUG_BANK_PARENT_MOLECULE_THAT_IS_ALSO_IN_THE_OTHER_DATASET, parent.getId(), parent.getSmiles(), parent.getInchi());
				}
			}
		}
		return overlapDrugbankIds;
	}
	
	
	/**
	 * If returnInchi is set to true, this method will return a set of Inchis of the overlapping parent molecules. 
	 * Otherwise, this method will return the set of IDs (from referenceDataset) of the overlapping parent molecules.
	 * <p>
	 * BEWARE: No stereochemistry information is checked or changed. 
	 * If one dataset contains stereochemistry information in the InChIs and the other does not, the result of this comparison will be inaccurate!
	 * 
	 * @param referenceDataset
	 * @param otherMetabolismData
	 * @param returnInchi whether or not a set of InChIs of the overlapping parent molecules is returned
	 * @return set of InChIs or IDs of overlapping parent molecules, depending on value of returnInchi
	 */
	private static Set<String> compareParentsWithoutChangingInchi(Map<BasicMolecule, Set<BasicMolecule>> referenceDataset, Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData, 
			Boolean returnInchi) {
		
		Set<String> overlap = new HashSet<>();
		
		int totalNumberOfReferenceDatasetMetabolitesFromOverlapParents = 0;
		int totalNumberOfOtherDatasetMetabolitesFromOverlapParents = 0;
		int numberOfOverlappingReferenceDatasetMetabolites = 0;
		int numberOfOverlappingOtherDatasetMetabolites = 0;
		
		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : referenceDataset.entrySet()) {
			
			BasicMolecule parent = entry.getKey();
			
			String referenceInchi = parent.getInchi();
			for (Entry<BasicMolecule, Set<BasicMolecule>> otherEntry : otherMetabolismData.entrySet()) {
				
				BasicMolecule otherParent = otherEntry.getKey();
				
				if (referenceInchi.equals(otherParent.getInchi())) {
					
					if (returnInchi) {
						overlap.add(parent.getInchi());
					} else {
						overlap.add(parent.getId());
					}
					
					Set<BasicMolecule> metabolites = referenceDataset.get(parent);
					totalNumberOfReferenceDatasetMetabolitesFromOverlapParents += metabolites.size();
					numberOfOverlappingReferenceDatasetMetabolites = countNumberOfOverlappingReferenceDatasetMetabolites(
							otherMetabolismData, numberOfOverlappingReferenceDatasetMetabolites, otherParent, metabolites);
					
					logger.info(DRUG_BANK_PARENT_MOLECULE_THAT_IS_ALSO_IN_THE_OTHER_DATASET, parent.getId(), parent.getSmiles(), parent.getInchi());
					
					totalNumberOfOtherDatasetMetabolitesFromOverlapParents += otherMetabolismData.get(otherParent).size();
					numberOfOverlappingOtherDatasetMetabolites = countNumberOfOverlappingOtherDatasetMetabolites(
							referenceDataset, otherMetabolismData, numberOfOverlappingOtherDatasetMetabolites, parent, otherParent);
				}
			}
		}

		logger.info(NUMBER_OF_OVERLAPPING_PARENT_MOLECULES, overlap.size());
		logger.info(NUMBER_OF_OVERLAPPING_REFERENCE_DATASET_METABOLITES, numberOfOverlappingReferenceDatasetMetabolites, totalNumberOfReferenceDatasetMetabolitesFromOverlapParents);
		logger.info(NUMBER_OF_EXTRA_METABOLITES, totalNumberOfOtherDatasetMetabolitesFromOverlapParents - numberOfOverlappingOtherDatasetMetabolites);
		logger.info(TOTAL_NUMBER_OF_PARENT_MOLECULES, referenceDataset.size() + otherMetabolismData.size() - overlap.size());
		return overlap;
	}

	
	private static int countNumberOfOverlappingOtherDatasetMetabolites(Map<BasicMolecule, Set<BasicMolecule>> referenceDataset,
			Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData, int numberOfOverlappingOtherDatasetMetabolites, BasicMolecule parent, BasicMolecule otherParent) {
		
		for (BasicMolecule otherMetabolite : otherMetabolismData.get(otherParent)) {
			for (BasicMolecule drugbankMetabolite: referenceDataset.get(parent)) {
				if (otherMetabolite.equals(drugbankMetabolite)) {
					numberOfOverlappingOtherDatasetMetabolites ++;
				}
			}
		}
		return numberOfOverlappingOtherDatasetMetabolites;
	}

	
	private static int countNumberOfOverlappingReferenceDatasetMetabolites(Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData,
			int numberOfOverlappingReferenceDatasetMetabolites, BasicMolecule otherParent, Set<BasicMolecule> metabolites) {
		
		for (BasicMolecule metabolite : metabolites) {
			for (BasicMolecule otherMetabolite : otherMetabolismData.get(otherParent)) {
				if (otherMetabolite.equals(metabolite)) {
					numberOfOverlappingReferenceDatasetMetabolites ++;
				}
			}
		}
		return numberOfOverlappingReferenceDatasetMetabolites;
	}
	
	
	private static Set<String> getInchisWithoutStereochemistry(Map<BasicMolecule, Set<BasicMolecule>> otherMetabolismData) {
		
		Set<String> testDbInchisNoStereo = new HashSet<>();
		for (BasicMolecule parent : otherMetabolismData.keySet()) {
			String testInchiNoStereo = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parent.getSmiles());
			testDbInchisNoStereo.add(testInchiNoStereo);
		}
		return testDbInchisNoStereo;
	}
	
}
