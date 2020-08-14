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

package main.java.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.analysis.GenericOverlapCalculator;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.Phase;
import main.java.utils.Enzymes;

/**
 * This class is used to manipulate data on metabolites once it has been loaded.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class DatasetManipulator {

	
	private static final String EMPTY_STRING = "";
	private static final String NO_OVERLAP = "No overlap of {} with new test dataset.";
	private static final String CHECKING_OVERLAP = "Checking overlap between {} and new test dataset...";
	private static final String TOTAL_NUMBER_OF_METABOLITES_LEFT_IN_DATASET = "Total number of metabolites left in dataset {}: {}";
	private static final String NUMBER_OF_PARENT_MOLECULES_LEFT_IN_DATASET = "Number of parent molecules left in dataset: {}";
	private static final String REMOVING_PARENT_MESSAGE = "parent with id {} is in test dataset. removing this one.";
	private static final String TOTAL_NUMBER_OF_X_METABOLITES = "Total number of {} metabolites: {}";
	private static final String TOTAL_NUMBER_OF_PHASE_METABOLITES = "Total number of phase {} metabolites: {}";
	private static final String NUMBER_OF_PARENT_COMPOUNDS_ACCORDING_TO_INCHI = "Total number of parent compounds, according to InChI, in dataset: {}";
	private static final String NUMBER_OF_PARENT_COMPOUNDS = "Number of parent compounds with at least one metabolite with SMILES: {}";

	private static final String UTILITY_CLASS = "Utility class";

	
	private DatasetManipulator() {
	    throw new IllegalStateException(UTILITY_CLASS);
	  }

	private static final Logger logger = LoggerFactory.getLogger(DatasetManipulator.class.getName());
	
	
	public static void getBasicMetabolismInfo(Map<BasicMolecule, Set<BasicMolecule>> dataset) {
		
		int numPhase1Metabolites = 0;
		int numPhase2Metabolites = 0;
		int numCYPMetabolites = 0;
		
		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : dataset.entrySet()) {
			
			for (BasicMolecule metabolite : entry.getValue()) {
				if (metabolite.getMetabolismPhase() == Phase.PHASE_1) {
					numPhase1Metabolites ++;
				}
				if (metabolite.getMetabolismPhase() == Phase.PHASE_2) {
					numPhase2Metabolites ++;
				}
				
				numCYPMetabolites = countCypMetabolites(numCYPMetabolites, metabolite);	
			}	
		}
		logger.info("Number of Phase I metabolites in combined dataset: {}", numPhase1Metabolites);
		logger.info("Number of CYP metabolites in combined dataset: {}", numCYPMetabolites);
		logger.info("Number of Phase II metabolites in combined dataset: {}", numPhase2Metabolites);
		logger.info("Number of parent compounds in combined dataset: {}", dataset.size());

	}
	
	

	public static void removeEntriesWithNoMetabolites(Map<BasicMolecule, Set<BasicMolecule>> metabolismData) { 
		
		Iterator<Map.Entry<BasicMolecule, Set<BasicMolecule>>> iter = metabolismData.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<BasicMolecule, Set<BasicMolecule>> entry = iter.next();
			if (entry.getValue().isEmpty()) {
				iter.remove();
			} 
		}
	}
	
	
	public static void removeEntriesWithNoEnzymeInfo(Map<BasicMolecule, Set<BasicMolecule>> metabolismData) { 
		
		Iterator<Map.Entry<BasicMolecule, Set<BasicMolecule>>> iter = metabolismData.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<BasicMolecule, Set<BasicMolecule>> entry = iter.next();
			
			Iterator<BasicMolecule> metabIter = entry.getValue().iterator();
			while (metabIter.hasNext()) {
				BasicMolecule metabolite = metabIter.next();
				
				if (metabolite.getEnzymes().isEmpty()) { // remove metabolite if it has no enzyme info
					metabIter.remove();
					
				} else { // set phase
					for (Enzymes e : metabolite.getEnzymes()) {
						
						if (e.getPhase() == Phase.PHASE_1) {
							if (metabolite.getMetabolismPhase() == Phase.PHASE_2) {
								logger.error("This metabolite has already been assigned to phase 2! Note this is not necessarily bad, but requires reworking the code.");
							}
							metabolite.setMetabolismPhase(Phase.PHASE_1);
							
						} else if (e.getPhase() == Phase.PHASE_2) {
							if (metabolite.getMetabolismPhase() == Phase.PHASE_1) {
								logger.error("This metabolite has already been assigned to phase 1! Note this is not necessarily bad, but requires reworking the code.");
							}
							metabolite.setMetabolismPhase(Phase.PHASE_2);
						}
					}
				}
			}

			// remove parent if it no longer has any metabolites
			if (entry.getValue().isEmpty()) {
				iter.remove();
			} 
		}
	}
	
	
	public static void logAmountOfDataForPhaseAndEnzyme(Map<BasicMolecule, Set<BasicMolecule>> metabolismData) {
		
		if (!logger.isInfoEnabled()) {
			return;
		}
		
		int p1Counter = 0;
		int p2Counter = 0;
		int cypCounter = 0;
		int ugtCounter = 0;
		int gstCounter = 0;
		int sultCounter = 0;
		int natCounter = 0;
		int mtCounter = 0;
		List<String> parentSmiles = new ArrayList<>();
		
		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : metabolismData.entrySet()) // iterate through all metabolites

			for (BasicMolecule metabolite : entry.getValue()) {
				
				if (metabolite.getMetabolismPhase() == Phase.PHASE_1) {
					p1Counter ++;
				}
				if (metabolite.getMetabolismPhase() == Phase.PHASE_2) {
					p2Counter ++;
				}
				
				for (Enzymes e : metabolite.getEnzymes()) {
					if (e == Enzymes.CYP) {
						cypCounter ++;
					} else if (e == Enzymes.UGT) {
						ugtCounter ++;
					} else if (e == Enzymes.GST) {
						gstCounter ++;
					} else if (e == Enzymes.SULT) {
						sultCounter ++;
					} else if (e == Enzymes.NAT) {
						natCounter ++;
					} else if (e == Enzymes.MT) {
						mtCounter ++;
					}
					
				}
				
				parentSmiles.add(entry.getKey().getSmiles());
			}
		
		logger.info(TOTAL_NUMBER_OF_PHASE_METABOLITES, 1, p1Counter);
		logger.info(TOTAL_NUMBER_OF_PHASE_METABOLITES, 2,  p2Counter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "CYP", cypCounter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "UGT", ugtCounter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "GST", gstCounter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "SULT", sultCounter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "NAT", natCounter);
		logger.info(TOTAL_NUMBER_OF_X_METABOLITES, "MT", mtCounter);
		logger.info(NUMBER_OF_PARENT_COMPOUNDS, metabolismData.size());
		logger.info(NUMBER_OF_PARENT_COMPOUNDS_ACCORDING_TO_INCHI, calculateNumberInSetBasedOnInchi(parentSmiles));
		
		
	}
	

	public static int calculateNumberInSetBasedOnInchi(final List<String> allSmiles) {
		HashSet<String> inchis = new HashSet<>();
		for (String m : allSmiles) {
			String inchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(m);
			inchis.add(inchi);
		}
		return inchis.size();
	}
	
	
	public static void addWithoutLosingPhaseOrEnzymeData(Set<BasicMolecule> alreadyThereMetabolites, BasicMolecule metabolite) {
		// if the return value of this method is true, add metabolite to the set using the .add method
		
		if (alreadyThereMetabolites.contains(metabolite)) {
						
			Iterator<BasicMolecule> iter = alreadyThereMetabolites.iterator();
			while (iter.hasNext()) {
				BasicMolecule met = iter.next();
				if (met.equals(metabolite)) {
					combinePhaseAndEnzymeInfo(metabolite, met);
					break;
				}
			}
		} else {
			logger.error("Something went wrong. Metabolite wasn't added to set so it should already be in the set!");
		}
		return;
	}
	
	
	public static Map<BasicMolecule, Set<BasicMolecule>> combineDatasets(Map<BasicMolecule, Set<BasicMolecule>> dataset1, Map<BasicMolecule, Set<BasicMolecule>> dataset2) {
		
		Map<BasicMolecule, Set<BasicMolecule>> combinedDataset = new HashMap<>();
		
		combineDatasets(dataset1, dataset2, combinedDataset);
		return combinedDataset;
	}


	public static void combineDatasets(Map<BasicMolecule, Set<BasicMolecule>> dataset1,
			Map<BasicMolecule, Set<BasicMolecule>> dataset2, Map<BasicMolecule, Set<BasicMolecule>> combinedDataset) {
		
		// check which of the input datasets is larger, then copy this one to the combined dataset
		if (dataset1.size() > dataset2.size()) {
			combinedDataset.putAll(dataset1);
			addOtherDataset(combinedDataset, dataset2);
		} else {
			combinedDataset.putAll(dataset2);
			addOtherDataset(combinedDataset, dataset1);
		}
	}
	
	
	private static void addOtherDataset(Map<BasicMolecule, Set<BasicMolecule>> combinedDataset, Map<BasicMolecule, Set<BasicMolecule>> smallerDataset) {
				
		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : smallerDataset.entrySet()) {
						
			assertFalse(entry.getKey().getInchi().equals(EMPTY_STRING));
			assertNotNull(entry.getKey().getInchi());
			checkAllMetabolitesHaveInchi(entry);
						
			if (combinedDataset.containsKey(entry.getKey())) {
				
				Set<BasicMolecule> combinedMetabolites = combinedDataset.get(entry.getKey());
				
				for (BasicMolecule metabolite : entry.getValue()) {
					Boolean added = combinedMetabolites.add(metabolite);
					if (!added) {
						combineDuplicateMetabolites(combinedMetabolites, metabolite);
					}
				}
			} else {
				combinedDataset.put(entry.getKey(), entry.getValue());
			}
		}	
	}


	private static void combineDuplicateMetabolites(Set<BasicMolecule> combinedMetabolites, BasicMolecule metabolite) {
		
		for (BasicMolecule m : combinedMetabolites) {
			if (m == metabolite) {
				combineIDs(metabolite, m);
				combinePhaseAndEnzymeInfo(metabolite, m);
				break;
			}
		}
	}
	
	
	private static void combinePhaseAndEnzymeInfo(BasicMolecule metabolite, BasicMolecule met) {
		// be very careful of the order of the parameters when calling this method!
		
		met.setMetabolismPhase(Phase.combinePhases(met, metabolite));
		
		if (!metabolite.getEnzymes().isEmpty()) {
			met.addEnzymes(metabolite.getEnzymes());
		}
	}


	private static void combineIDs(BasicMolecule metabolite, BasicMolecule m) {
		// be very careful of the order of the parameters when calling this method!
		
		if (m.getMetXBioDBID() == null && metabolite.getMetXBioDBID() != null) {
			m.setMetXBioDBID(metabolite.getMetXBioDBID());
			logger.debug("set m id");
			
		} else if (m.getDrugBankID() == null && metabolite.getDrugBankID() != null) {
			m.setDrugBankID(metabolite.getDrugBankID());
		}
	}


	private static void checkAllMetabolitesHaveInchi(Entry<BasicMolecule, Set<BasicMolecule>> entry) {
		
		for (BasicMolecule metabolite : entry.getValue()) {
			assertFalse(metabolite.getInchi().equals(EMPTY_STRING));
			assertNotNull(metabolite.getInchi());
		}
	}
	
	
	public static void reportNumberLeftInDataset(Map<BasicMolecule, Set<BasicMolecule>> dataset, String datasetName) {
		
		if (logger.isInfoEnabled()) {
			
			logger.info(NUMBER_OF_PARENT_MOLECULES_LEFT_IN_DATASET, dataset.size());

			int counter = 0;
			for (Entry<BasicMolecule, Set<BasicMolecule>> entry : dataset.entrySet()) {
				counter += entry.getValue().size();
			}
			logger.info(TOTAL_NUMBER_OF_METABOLITES_LEFT_IN_DATASET, datasetName, counter);
		}
	}
	
	
	public static void removeOverlapFromDataset(Map<BasicMolecule, Set<BasicMolecule>> dataset, final Set<String> overlapDrugbankIds) {
		
		Iterator<Map.Entry<BasicMolecule, Set<BasicMolecule>>> it = dataset.entrySet().iterator();
		while (it.hasNext()) {
			String currentId = it.next().getKey().getId();
			if (overlapDrugbankIds.contains(currentId)) {
				logger.debug(REMOVING_PARENT_MESSAGE, currentId);
				it.remove();
			}
		}
	}
	
	
	public static void removeOverlapAndprovideInfoOnDatasetComparedToNewTestDataset(Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData, 
			Map<BasicMolecule, Set<BasicMolecule>> referenceDataset, String datasetName) {
		
		reportNumberLeftInDataset(referenceDataset, datasetName);
		logger.info(CHECKING_OVERLAP, datasetName);

		// check overlap between new test dataset and reference dataset (see datasetName for which one)
		Set<String> overlapIds = GenericOverlapCalculator.getOverlapAndGenerateInchiWithoutStereochemistryForOtherDataset(referenceDataset, testMetabolismData);	
		if (overlapIds.isEmpty()) {
			logger.info(NO_OVERLAP, datasetName);
		} 
		removeOverlapFromDataset(referenceDataset, overlapIds);  // only used to get information to report
		reportNumberLeftInDataset(referenceDataset, datasetName);
	}
	
	private static int countCypMetabolites(int numCYPMetabolites, BasicMolecule metabolite) {
		// note: counting CYP metabolites is tricky because all that came from MetXBioDB and are phase I are also CYP, but this is not necessarily the case for the ones from DrugBank
		
		if (metabolite.getDrugBankID() != null) {
			// so it came from DrugBank
			if (metabolite.getEnzymes().contains(Enzymes.CYP)) {
				numCYPMetabolites ++;
			}
		} else {
			// so it came from only MetXBioDB
			if (metabolite.getMetabolismPhase() == Phase.PHASE_1) {
				numCYPMetabolites ++;
			}	
		}

		return numCYPMetabolites;
	}
	
}
