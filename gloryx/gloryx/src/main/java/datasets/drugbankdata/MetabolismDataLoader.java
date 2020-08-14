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

package main.java.datasets.drugbankdata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ambit2.smarts.SmartsManager;
import main.java.utils.molecule.BasicMolecule;
import main.java.data.DatasetManipulator;
import main.java.utils.Enzymes;
import main.java.utils.Filenames;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.Phase;

/**
 * Loads DrugBank metabolism data for the specified phase from the SDF files and a CSV file that was created by parsing the DrugBank XML file with a separate python script.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MetabolismDataLoader {


	private static final String REMOVING_INCHI_NULL = "InChI of parent or metabolite is null for {} and {}";
	private static final String REMOVING_METABOLITE_WRONG_PHASE = "Removing metabolite because it is not a {} metabolite.";
	private static final String ERROR_CLOSING_CSV_FILE = "Error closing CSV file {}";
	private static final String PRODUCT_ONLY_HYDROGENS = "Removing product {} because it consists only of hydrogens.";
	private static final String PARENT_HAS_MULTIPLE_COMPONENTS = "Removing parent compound {} with SMILES {} because it has multiple components.";
	private static final String PERIOD = ".";
	private static final String REMOVING_METABOLITE_SAME_AS_PARENT = "Removing metabolite that is the same as parent molecule {}";
	private static final String NUMBER_OF_PARENT_COMPOUNDS = "Number of parent compounds with at least one metabolite with SMILES: {}";
	private static final String NO_SMILES_FOUND_FOR_PARENT = "No SMILES found for parent {}";
	private static final String FOUND_PARENT_IN_MAP = "Found parent {} as {} (identical InChIs)";
	private static final String ERROR_READING_CSV_FILE = "Error reading CSV file {}";
	private static final String NUMBER_OF_METABOLITES_WITHOUT_SMILES = "Number of metabolites without SMILES: {}.\nThese metabolites have been removed from the data set.";

	private static final String SPACE_SURROUNDED_BY_QUOTES = "\"\\s+\"";
	private static final String DBMET = "DBMET";

	private static final Logger logger = LoggerFactory.getLogger(MetabolismDataLoader.class.getName());
	
	
	public Map<BasicMolecule, Set<BasicMolecule>> loadMetabolismData(Filenames filenames, Phase phase) {

		HashMap<BasicMolecule, Set<BasicMolecule>> metabolismData = new HashMap<>();
		loadMetabolismData(filenames, metabolismData, phase);
		return metabolismData;
	}


	public void loadMetabolismData(Filenames filenames, Map<BasicMolecule, Set<BasicMolecule>> metabolismData, Phase phase) {

		// Note: In the data structure for the metabolism data returned by this method, there may be duplicate metabolites for each parent 
		//       compound. This kind of duplicate removal must be done at a later point using the InChIs.
		//		 There are no duplicate parent compounds (according to InChI).

		
		// expand filenames
		String drugbankStructuresFilename = filenames.getDrugbankStructuresFilename();
		String metaboliteStructuresFilename = filenames.getMetaboliteStructuresFilename();
		String drugbankCsvFilename = filenames.getDrugbankCsvFilename();
		

		// initialize
		List<String> allMetabolitesWithoutSmiles = new ArrayList<>();
		List<String> allMetaboliteSmiles = new ArrayList<>();



		// get structures
		Map<String, String> metaboliteStructures = getStructuresFromFile(metaboliteStructuresFilename);
		Map<String, String> drugbankStructures = getStructuresFromFile(drugbankStructuresFilename);

		// process csv with metabolism data
		extractMetabolismDataFromCsv(drugbankCsvFilename, metaboliteStructures, drugbankStructures, metabolismData, allMetabolitesWithoutSmiles, allMetaboliteSmiles, phase);

		logger.info(NUMBER_OF_METABOLITES_WITHOUT_SMILES, allMetabolitesWithoutSmiles.size());
		
		// print all IDs of metabolites with missing smiles
		for (String missing : allMetabolitesWithoutSmiles) {
			logger.debug("Metabolite {} with missing SMILES is excluded.", missing);
		}
				
		DatasetManipulator.removeEntriesWithNoMetabolites(metabolismData);
				
		logger.info(NUMBER_OF_PARENT_COMPOUNDS, metabolismData.size());
		
		DatasetManipulator.removeEntriesWithNoEnzymeInfo(metabolismData);
		
		DatasetManipulator.logAmountOfDataForPhaseAndEnzyme(metabolismData);


	}


	private Map<String, String> getStructuresFromFile(final String filename) {

		DrugbankStructureLoader mloader = new DrugbankStructureLoader();
		return mloader.loadSmilesFromSdf(filename);
	}


	private void extractMetabolismDataFromCsv(final String drugbankCsvFilename, final Map<String, String> metaboliteStructures, final Map<String, String> drugbankStructures, 
			Map<BasicMolecule, Set<BasicMolecule>> metabolismData, List<String> allMetabolitesWithoutSmiles, 
			List<String> allMetaboliteSmiles, Phase phase) {

		File csv = new File(drugbankCsvFilename);
		CSVParser parser;
		try {
			parser = CSVParser.parse(csv, Charset.defaultCharset(), CSVFormat.DEFAULT.withQuote(null));
		} catch (IOException e) {
			logger.error(ERROR_READING_CSV_FILE, drugbankCsvFilename);
			return;
		}
		for (CSVRecord record : parser) {
			// extract parent and metabolite information and add to metabolism data map
			processCsvRecord(metaboliteStructures, drugbankStructures, metabolismData, allMetabolitesWithoutSmiles, allMetaboliteSmiles, record, phase);
		}
		
		// close the parser
		try {
			parser.close();
		} catch (IOException e) {
			logger.error(ERROR_CLOSING_CSV_FILE, drugbankCsvFilename);
			return;
		}
		return;
	}


	private void processCsvRecord(final Map<String, String> metaboliteStructures, final Map<String, String> drugbankStructures, Map<BasicMolecule, 
			Set<BasicMolecule>> metabolismData, List<String> allMetabolitesWithoutSmiles, List<String> allMetaboliteSmiles, final CSVRecord record, final Phase phase) {

		BasicMolecule parent = createParentMolecule(metaboliteStructures, drugbankStructures, record);
		if (parent == null) {
			return;
		}
		if (parent.getSmiles().contains(PERIOD)) {
			
			MoleculeManipulator.getMainPartOfSalt(parent);
			
			// if this didn't fix the problem, discard this parent molecule
			if (parent.getSmiles().contains(PERIOD)) { 
				logger.info(PARENT_HAS_MULTIPLE_COMPONENTS, parent.getId(), parent.getSmiles());
				return;
			}
			
		}
		
		// create metabolites. In one case there is a metabolite with multiple components, so these must be separated
		Set<BasicMolecule> metabolites = createMetabolite(metaboliteStructures, drugbankStructures, allMetabolitesWithoutSmiles, allMetaboliteSmiles, record);
		
		for (BasicMolecule metabolite : metabolites) { // there may be multiple components
			
			if (metabolite == null) {
				return;
			}
					
			// check whether any metabolite is the same as its parent, whether it corresponds to the desired phase, and 
			// whether the parent is already in the map. Finally, add to map if possible.
			
			if (metabolite.getInchi() == null || parent.getInchi() == null) {
				logger.error(REMOVING_INCHI_NULL, parent.getDrugBankID(), metabolite.getDrugBankID());
				return;
			}
			if (metabolite.equals(parent)) { 
				logger.debug(REMOVING_METABOLITE_SAME_AS_PARENT, parent.getId());	
				return;
			}
			
			// make sure the metabolite is not a water molecule or a hydrogen molecule
			if (MoleculeManipulator.getHeavyAtomCount(metabolite.getSmiles()) == 1) {
				logger.warn("DrugBank metabolite has only one heavy atom! {}", metabolite.getSmiles());
				
				// check if water
				IAtomContainer metaboliteMolecule = MoleculeManipulator.generateMoleculeFromSmiles(metabolite.getSmiles());
				if (MoleculeManipulator.checkIfWater(metaboliteMolecule) ) {
					logger.warn("Excluding DrugBank metabolite because it's a water molecule.");
					return;
				}
				return;
			}
			if (MoleculeManipulator.getHeavyAtomCount(metabolite.getSmiles()) == 2) {
				logger.info("DrugBank metabolite has only two heavy atoms! {}", metabolite.getSmiles());
			}
			if (MoleculeManipulator.checkIfOnlyHydrogens(metabolite.getSmiles())) {
				logger.info("DrugBank metabolite is a hydrogen molecule.Excluding"); // currently never happens
				return;
			}

			Boolean relevantMetabolite = matchPhase(phase, metabolite);	

			if (relevantMetabolite) {
				addToMap(metabolismData, parent, metabolite);
			}
		}
	}


	private Boolean matchPhase(final Phase phase, BasicMolecule metabolite) {
		
		Boolean relevantMetabolite = true;
		
		if (phase.isSpecificEnzymeFamily()) {
			
			Set<Enzymes> enzymes = metabolite.getEnzymes();
			Boolean enzymeMatchesArtificialPhase = Enzymes.checkIfEnzymeMatches(phase, enzymes);
			if (!enzymeMatchesArtificialPhase) {
				logger.debug(REMOVING_METABOLITE_WRONG_PHASE, phase.getPhaseName());
				relevantMetabolite = false;
			}
			
			// This was used to test how many products of GST-mediated reactions could be found with the newly 
			// developed reaction rules and has no effect unless phase "GST" is explicitly input when running the program.
			if (phase == Phase.GST) { // check whether metabolite contains GS moiety
				
				String gshSmarts = "SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N";
				SmartsManager smartsManager = new SmartsManager(DefaultChemObjectBuilder.getInstance());
				smartsManager.setQuery(gshSmarts);
				
				IAtomContainer molecule = MoleculeManipulator.generateMoleculeFromSmiles(metabolite.getSmiles());
				
				try {
					if (!smartsManager.searchIn(molecule)) { 
						relevantMetabolite = false;
					}
				} catch (Exception e) {
					logger.error("Error searching molecule {} with GS SMARTS", metabolite.getSmiles());
				}
				
			}
			
			
		} else if (!Phase.phasesMatch(metabolite.getMetabolismPhase(), phase)) {
			logger.debug(REMOVING_METABOLITE_WRONG_PHASE, phase.getPhaseName());
			relevantMetabolite = false;
		}
		return relevantMetabolite;
	}


	private void addToMap(Map<BasicMolecule, Set<BasicMolecule>> metabolismData, final BasicMolecule parent, final BasicMolecule metabolite) {

		Boolean duplicate = false;
		
		// check if this parent compound is already in metabolismData
		for (Map.Entry<BasicMolecule, Set<BasicMolecule>> entry : metabolismData.entrySet()) {
			
			BasicMolecule mol = entry.getKey();
			if (mol.getInchi().equals(parent.getInchi())) {  // found this parent molecule already in map

				if (!mol.getId().equals(parent.getId())) {
					logger.debug(FOUND_PARENT_IN_MAP, parent.getId(), mol.getId());
				}

				Boolean added = entry.getValue().add(metabolite);
				if (!added) {
					DatasetManipulator.addWithoutLosingPhaseOrEnzymeData(entry.getValue(), metabolite);
				}

				duplicate = true;
				break;
			}
		}
		if (!duplicate) {
			Set<BasicMolecule> metabolites = new HashSet<>();
			metabolites.add(metabolite);
			metabolismData.put(parent, metabolites);
		}
	}


	private Set<BasicMolecule> createMetabolite(Map<String, String> metaboliteStructures, Map<String, String> drugbankStructures, 
			List<String> allMetabolitesWithoutSmiles, List<String> allMetaboliteSmiles,  final CSVRecord record) {

		Set<BasicMolecule> metabolites = new HashSet<>();
		
		String metaboliteId = record.get(3);
		String metaboliteSmiles = getSmilesForMetabolite(metaboliteStructures, drugbankStructures, metaboliteId, allMetabolitesWithoutSmiles);
		Set<Enzymes> enzymes = getEnzymesFromRecord(record);
		
		
		if (metaboliteSmiles != null && metaboliteSmiles.contains(PERIOD)) {
			logger.info("Multicomponent metabolite in DrugBank: {}", metaboliteSmiles);
			
			String[] components = metaboliteSmiles.split("\\.");
			for (String componentSmiles : components) {
				metabolites.add(createMetaboliteMolecule(metaboliteId, componentSmiles, enzymes, allMetaboliteSmiles));
			}
		} else {
			metabolites.add(createMetaboliteMolecule(metaboliteId, metaboliteSmiles, enzymes, allMetaboliteSmiles));
		}

		return metabolites;
	}


	private Set<Enzymes> getEnzymesFromRecord(final CSVRecord record) {
		
		String enzymesString = record.get(2);
		Set<String> enzymeNames = new HashSet<>(Arrays.asList(enzymesString.split(SPACE_SURROUNDED_BY_QUOTES)));
		Set<Enzymes> enzymes = new HashSet<>();
		for (String enzymeName : enzymeNames) {
			if (enzymeName.isEmpty()) {
				continue;
			}
			enzymeName = cleanUpEnzymeName(enzymeName); 
			Boolean found = false;
			for (Enzymes e : Enzymes.values()) {
				if (e.getAllNameAndIsozymeVariations().contains(enzymeName)) {
					enzymes.add(e);
					found = true;
					break;
				}
			}
			if (!found) {
				logger.debug("this enzyme has not been classified into a metabolism phase: {}", enzymeName);	
			}
		}

		return enzymes;
	}


	private String cleanUpEnzymeName(String enzymeName) {
		if (enzymeName.startsWith("\"")) {
			enzymeName = enzymeName.substring(1);
		} 
		if (enzymeName.endsWith("\"")) {
			enzymeName = enzymeName.substring(0, enzymeName.length()-1);
		} 
		if (enzymeName.endsWith("\" ")) {
			enzymeName = enzymeName.substring(0, enzymeName.length()-2);
		}
		return enzymeName;
	}


	private BasicMolecule createParentMolecule(Map<String, String> metaboliteStructures, Map<String, String> drugbankStructures, final CSVRecord record) {

		String parentId = record.get(0);
		String parentSmiles = getSmilesFromMap(metaboliteStructures, drugbankStructures, parentId);
		
		if (parentSmiles == null) {
			logger.debug(NO_SMILES_FOUND_FOR_PARENT, parentId);
		} 

		if (parentSmiles != null) {
			String parentInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parentSmiles);
			BasicMolecule parent = new BasicMolecule(parentId, parentSmiles, parentInchi);
			parent.setDrugBankID(parentId);
			return parent;
		}

		return null;
	}


	private String getSmilesForMetabolite(final Map<String, String> metaboliteStructures, final Map<String, String> drugbankStructures, 
			final String metaboliteId, List<String> allMetabolitesWithoutSmiles) {

		String metaboliteSmiles = getSmilesFromMap(metaboliteStructures, drugbankStructures, metaboliteId);
		if (metaboliteSmiles != null) {
			
			if (MoleculeManipulator.checkIfOnlyHydrogens(metaboliteSmiles)) {
				logger.info(PRODUCT_ONLY_HYDROGENS, metaboliteSmiles);  // note: I checked and this never happens for the CYP data (Jan 2019)
				metaboliteSmiles = null;  // so that it will get ignored
			}

		} else {
			allMetabolitesWithoutSmiles.add(metaboliteId);
		}
		return metaboliteSmiles;
	}


	private String getSmilesFromMap(final Map<String, String> metaboliteStructures, final Map<String, String> drugbankStructures, String id) {

		if (id.startsWith(DBMET)) {
			return metaboliteStructures.get(id);
		} else { 
			return drugbankStructures.get(id);
		}
	}


	private BasicMolecule createMetaboliteMolecule(final String metaboliteId, final String metaboliteSmiles, Set<Enzymes> enzymes, List<String> allMetaboliteSmiles) {

		BasicMolecule metabolite = null;
		if (metaboliteSmiles != null) {
			
			allMetaboliteSmiles.add(metaboliteSmiles);
			
			String metaboliteInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(metaboliteSmiles);
			metabolite = new BasicMolecule(metaboliteId, metaboliteSmiles, metaboliteInchi);
			metabolite.addEnzymes(enzymes);
			metabolite.setDrugBankID(metaboliteId);
			
			for (Enzymes e : enzymes) { 
				setPhaseForMetabolite(metabolite, e);
			}
		}
		return metabolite;
	}


	private void setPhaseForMetabolite(BasicMolecule metabolite, Enzymes e) {
		// allow for possibility that metabolite is formed by multiple enzymes and that 
		// not all of those enzymes correspond to the same metabolism phase
		
		if (e.getPhase() == Phase.PHASE_1) {
			if (metabolite.getMetabolismPhase() == Phase.PHASE_2) {
				metabolite.setMetabolismPhase(Phase.PHASES_1_AND_2);
			} else {
				metabolite.setMetabolismPhase(Phase.PHASE_1);
			}
		} else {
			if (metabolite.getMetabolismPhase() == Phase.PHASE_1) {
				metabolite.setMetabolismPhase(Phase.PHASES_1_AND_2);
			} else {
				metabolite.setMetabolismPhase(Phase.PHASE_2);
			}
		}
	}





}
