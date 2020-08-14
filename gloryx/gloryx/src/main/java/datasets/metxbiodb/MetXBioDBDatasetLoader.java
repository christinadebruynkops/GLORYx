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

package main.java.datasets.metxbiodb;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.json.simple.parser.JSONParser;

import main.java.utils.molecule.BasicMolecule;
import main.java.data.DatasetManipulator;
import main.java.utils.Enzymes;
import main.java.utils.Filenames;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.Phase;

/**
 * Loads MetXBioDB data for the specified metabolism phase.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MetXBioDBDatasetLoader {
	
	private static final String TOTAL_NUMBER_OF_CYP_METABOLITES = "Total number of CYP metabolites in MetXBioDB: {}";
	private static final String TOTAL_NUMBER_OF_METABOLITES = "Total number of metabolites in MetXBioDB dataset: {}";
	private static final String NUMBER_OF_PHASE_METABOLITES = "Number of phase {} metabolites in MetXBioDB dataset: {}";
	private static final String JSON_METXBIODB_ID = "METXBIODB_ID";
	private static final String PRODUCT_ONLY_HYDROGENS = "Removing product {} from reaction {} because it consists only of hydrogens.";
	private static final String SUBSTRATE_CONSISTS_OF_MULTIPLE_MOLECULES = "Substrate of {} consists of multiple molecules. Not using in dataset. SMILES: {}";
	private static final String PRODUCT_CONSISTS_OF_MULTIPLE_MOLECULES = "Product of {} consists of multiple molecules. Will be separated into multiple metabolites. SMILES: {}";
	private static final String PATTERN_PERIOD = ".";
	private static final String NO_REACTION_FOUND_WITH_IDENTIFIER = "No reaction found with identifier {}";
	private static final String INCHI_HAS_STEREOCHEMISTRY_INFORMATION = "InChI has stereochemistry information: \n{}\n{}";
	private static final String SUBSTRATE_HAD_NO_INCHI = "Substrate of reaction {} had no InChI";
	private static final String NUMBER_OF_MOLECULES_WITH_STEREOCHEMISTRY = "Number of compounds with stereochemistry: {}. (Possible duplicates contained in this number.)"
			+ "Removed stereochemistry for all (note that double bond stereochemistry is removed as well.";
	private static final String NUMBER_OF_UNIQUE_PARENT_COMPOUNDS = "Number of unique parent compounds MetXBioDB: {}";
	private static final String ERROR_PARSING_JSON_FILE = "Error parsing JSON file.";
	private static final String ERROR_READING_JSON_FILE = "Error reading JSON file.";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "0";

	private static final String JSON_PRODUCTS = "Products";
	private static final String JSON_INCHI = "InChI";
	private static final String JSON_SUBSTRATE = "Substrate";
	private static final String JSON_ENZYMES = "Enzyme(s)";
	private static final String JSON_BIOSYSTEM = "Biosystem";
	private static final String JSON_BIOTRANSFORMATION_TYPE = "Biotransformation type";
	private static final String JSON_BIOTRANSFORMATIONS = "biotransformations";
	private static final String JSON_BIOTID = "BIOTID";

	private static final String ENTRY_CYP = "CYP";
	private static final String ENTRY_HUMAN = "Human";

	private static final Logger logger = LoggerFactory.getLogger(MetXBioDBDatasetLoader.class.getName());

	private Map<BasicMolecule, Set<BasicMolecule>> metabolismData;
	private int stereoInchiCounter = 0;
	private Boolean removeStereochemistry;
	
	private int numRxnsWithNoSubstrateInchi = 0;
	private int numMetabolitesWithoutInchi = 0;
	
	// constructors
	public MetXBioDBDatasetLoader(Map<BasicMolecule, Set<BasicMolecule>> metabolismData, Boolean removeStereochemistry) {
		this.metabolismData = metabolismData;
		this.removeStereochemistry = removeStereochemistry;
	}
	public MetXBioDBDatasetLoader(Map<BasicMolecule, Set<BasicMolecule>> metabolismData) {
		this.metabolismData = metabolismData;
		this.removeStereochemistry = false;
	}
	public MetXBioDBDatasetLoader(Boolean removeStereochemistry) {
		metabolismData = new HashMap<>();
		this.removeStereochemistry = removeStereochemistry;
	}
	public MetXBioDBDatasetLoader() {
		metabolismData = new HashMap<>();
		this.removeStereochemistry = false;
	}
	


	public Map<BasicMolecule, Set<BasicMolecule>> loadData(Filenames filenames, Phase phase) {		

		readInJson(filenames.getMetxbiodbFilename(), phase);
		
		logger.debug("Number of parent compounds before removing ones with no metabolites: {}", metabolismData.size());
		DatasetManipulator.removeEntriesWithNoMetabolites(metabolismData);
		logger.debug("Number of parent compounds after removing ones with no metabolites: {}", metabolismData.size());
		
		DatasetManipulator.logAmountOfDataForPhaseAndEnzyme(metabolismData);
		
		return metabolismData;
	}


	private void readInJson(String jsonFilename, Phase phase) {

		JSONParser parser = new JSONParser();			
		try (FileReader reader = new FileReader(jsonFilename)) {

			Object obj = parser.parse(reader);
			JSONObject dbObject = (JSONObject) obj;
			JSONObject biotransformationList = (JSONObject) dbObject.get(JSON_BIOTRANSFORMATIONS);

			// hack because of JSON file setup - double check max number (make sure not off by 1) when the MetXBioDB JSON file is updated!
			Iterator<Integer> i = IntStream.range(1, 2179).iterator();
			while (i.hasNext()) {
				String identifier = createIdentifier(i.next());
				parseBiotransformation(biotransformationList, identifier, phase);
			}

		} catch (IOException e1) {
			logger.error(ERROR_READING_JSON_FILE, e1);
		} catch (ParseException e2) {
			logger.error(ERROR_PARSING_JSON_FILE, e2);
		}
		
		if (logger.isInfoEnabled()) {
			countAndPrintMetaboliteNumbers();
		}
	}
	
	
	private void countAndPrintMetaboliteNumbers() {

		int totalMetaboliteCounter = 0;
		int phase1metaboliteCounter = 0;
		int phase2metaboliteCounter = 0;
		int cypMetaboliteCounter = 0;
		for (Entry<BasicMolecule, Set<BasicMolecule>> x : metabolismData.entrySet()) {
			totalMetaboliteCounter += x.getValue().size();
			for (BasicMolecule metabolite: x.getValue()) {
				if (Phase.isPhase1(metabolite.getMetabolismPhase())) {
					phase1metaboliteCounter ++;
					if (metabolite.getEnzymes().contains(Enzymes.CYP)) {
						cypMetaboliteCounter ++;
					}
				}
				if (Phase.isPhase2(metabolite.getMetabolismPhase())) {
					phase2metaboliteCounter ++;
				}
			}
		}
		
		logger.info(TOTAL_NUMBER_OF_METABOLITES, totalMetaboliteCounter);
		logger.info(NUMBER_OF_PHASE_METABOLITES, 1, phase1metaboliteCounter);
		logger.info(TOTAL_NUMBER_OF_CYP_METABOLITES, cypMetaboliteCounter); 
		logger.info(NUMBER_OF_PHASE_METABOLITES, 2, phase2metaboliteCounter);
		
		logger.info(NUMBER_OF_UNIQUE_PARENT_COMPOUNDS, metabolismData.size());
		logger.info(NUMBER_OF_MOLECULES_WITH_STEREOCHEMISTRY, stereoInchiCounter);
		

		logger.info("Number of reactions in MetXBioDB for which the substrate "
				+ "has no InChI (therefore not used): {}", numRxnsWithNoSubstrateInchi);
		logger.info("Number of metabolites without Inchi: {}", numMetabolitesWithoutInchi);
	}

	
	private void parseBiotransformation(JSONObject biotransformationList, String identifier, Phase desiredPhase) {

		JSONObject reaction = (JSONObject) biotransformationList.get(identifier);
		
		if (reaction != null ) {
			
//			String biosystem = (String) reaction.get(JSON_BIOSYSTEM); // previously used this to check whether human, but now the phase check takes care of this.
			String enzymesString = (String) reaction.get(JSON_ENZYMES);
			String phaseString = (String) reaction.get(JSON_BIOTRANSFORMATION_TYPE);
			
			// extract enzyme info. needed to check phase in case of specific enzyme family
			Set<Enzymes> enzymes = extractEnzymeInfo(enzymesString);
			
			// make sure it's the desired phase
			Phase phase = getPhaseFromString(phaseString);
			if (desiredPhase.isSpecificEnzymeFamily()) {
				
				// TODO: speed up by only extracting specific enzyme info from enzyme family we care about, using map or switch statement for contains(). no need to save all enzyme info for each metabolite
				
				Boolean enzymeMatchesArtificialPhase = Enzymes.checkIfEnzymeMatches(desiredPhase, enzymes);
				if (!enzymeMatchesArtificialPhase) {
					logger.debug("Removing metabolite because it is not a {} metabolite.", desiredPhase.getPhaseName());
					return;
				}
				
			} else if (!Phase.phasesMatch(phase, desiredPhase)) {
				logger.debug("Removing metabolite because it is not a {} metabolite.", desiredPhase.getPhaseName());
				return;
			}

			JSONObject substrate = (JSONObject) reaction.get(JSON_SUBSTRATE);
			String substrateInchi = (String) substrate.get(JSON_INCHI);   
			String substrateId = (String) substrate.get(JSON_METXBIODB_ID);
			
			if (substrateInchi.equals(EMPTY_STRING)) {
				logger.debug(SUBSTRATE_HAD_NO_INCHI, identifier);
				numRxnsWithNoSubstrateInchi ++;
				return;
			}
			
			// Generate SMILES. Needed for the input for the metabolite predictor and FAME 2. Keep stereochem if present. 
			String substrateSmiles = MoleculeManipulator.generateSmilesFromInchi(substrateInchi); 
			if (substrateSmiles.contains(PATTERN_PERIOD)) {
				logger.info(SUBSTRATE_CONSISTS_OF_MULTIPLE_MOLECULES, identifier, substrateSmiles);
				return;
			}
			
			// some molecules have stereochemistry information. In order to be compared with DrugBank and to be 
			// later combined with DrugBank to create the reference dataset, stereochemistry information must be removed from the InChIs. 
			substrateInchi = removeStereochemistryIfSpecified(substrateInchi);
			
			BasicMolecule reactant = new BasicMolecule(identifier, substrateSmiles, substrateInchi);
			reactant.setMetXBioDBID(substrateId);
			
			Set<BasicMolecule> metabolites = assembleMetabolites(identifier, reaction, enzymes, phase, substrateInchi);
			addMetabolitesToDataset(reactant, metabolites);
			
		} else {
			logger.debug(NO_REACTION_FOUND_WITH_IDENTIFIER, identifier);
		}
	}
	
	
	private Set<Enzymes> extractEnzymeInfo(String enzymesString) {
		Set<Enzymes> enzymes = new HashSet<>();
					
		int exceptionCounter = 0;
		
		if (enzymesString.contains(ENTRY_CYP)) {
			enzymes.add(Enzymes.CYP);
		} else {
			exceptionCounter ++;
		}
		if (enzymesString.contains("UGT") || enzymesString.contains("UDP-glucuronosyltransferase")) {
			enzymes.add(Enzymes.UGT);
		} else {
			exceptionCounter ++;
		}
		if (enzymesString.contains("GST")) {
			enzymes.add(Enzymes.GST);
		} else {
			exceptionCounter ++;
		}
		if (enzymesString.contains("COMT")) {
			enzymes.add(Enzymes.MT);
		} else {
			exceptionCounter ++;
		}
		if (enzymesString.contains("SULT")) {
			enzymes.add(Enzymes.SULT);
		} else {
			exceptionCounter ++;
		}
		if (enzymesString.contains("NAT")) {
			enzymes.add(Enzymes.NAT);
		} else {
			exceptionCounter ++;
		}
		
		// other enzymes not covered: TAU, bacterial, and EC-identified enzymes. this is fine.
		if (exceptionCounter == 6) {
			logger.debug("An enzyme string does not contain any of the known enzyme families! Enzyme string: {}" , enzymesString);
		}
		return enzymes;
	}
	
	
	private void addMetabolitesToDataset(BasicMolecule reactant, Set<BasicMolecule> metabolites) {
		if (metabolismData.containsKey(reactant) ) {
			for (BasicMolecule m : metabolites) {
				
				// handle case of same metabolite but different annotated phase or enzyme(s)
				Boolean added = metabolismData.get(reactant).add(m);
				if (!added) {
					DatasetManipulator.addWithoutLosingPhaseOrEnzymeData(metabolismData.get(reactant), m);
				}
			}
		} else {
			metabolismData.put(reactant, metabolites);		
		}
	}
	
	
	private Set<BasicMolecule> assembleMetabolites(String identifier, JSONObject reaction, Set<Enzymes> enzymes, Phase phase, String parentInchi) {
		
		JSONArray products = (JSONArray) reaction.get(JSON_PRODUCTS);
		Set<BasicMolecule> metabolites = new HashSet<>();
		for (Object p : products) {
			JSONObject product = (JSONObject) p;
			String productInchi = (String) product.get(JSON_INCHI);
			if (!productInchi.equals(EMPTY_STRING)) {
				
				// check whether stereochemistry information and if necessary remove
				productInchi = removeStereochemistryIfSpecified(productInchi);
				
				if (productInchi.equals(parentInchi)) {
					continue;
				}
				
				String productSmiles = MoleculeManipulator.generateSmilesFromInchi(productInchi); // obviously not ideal, but only the InChI is provided :(
				if (productSmiles.contains(PATTERN_PERIOD)) {
					logger.warn(PRODUCT_CONSISTS_OF_MULTIPLE_MOLECULES, identifier, productSmiles);
					// this never happens in original version of db I used - I checked
				}
				if (MoleculeManipulator.checkIfOnlyHydrogens(productSmiles)) {
					logger.info(PRODUCT_ONLY_HYDROGENS, productSmiles, identifier);  // I checked and this currently never happens
				}
				// make sure the metabolite is not a water molecule or only one heavy atom
				if (MoleculeManipulator.getHeavyAtomCount(productSmiles) == 1) {
					logger.warn("MetXBioDB metabolite has only one heavy atom! {}", productSmiles);
					
					// check if water
					IAtomContainer metaboliteMolecule = MoleculeManipulator.generateMoleculeFromSmiles(productSmiles);
					if (MoleculeManipulator.checkIfWater(metaboliteMolecule) ) {
						logger.warn("Excluding MetXBioDB metabolite because it's a water molecule.");
						continue;
					}
					continue;
				}
				if (MoleculeManipulator.getHeavyAtomCount(productSmiles) == 2) {
					logger.info("MetXBioDB metabolite has only two heavy atoms! {}", productSmiles);
				}

				BasicMolecule metabolite = new BasicMolecule(EMPTY_STRING, productSmiles, productInchi);
				metabolite.setMetXBioDBID(identifier);
				metabolite.setMetabolismPhase(phase);
				
				metabolite.addEnzymes(enzymes);
				
				Boolean added = metabolites.add(metabolite);
				if (!added) {
					DatasetManipulator.addWithoutLosingPhaseOrEnzymeData(metabolites, metabolite);
				}
			} else {
				numMetabolitesWithoutInchi ++;
			}
		}
		return metabolites;
	}
	

	
	
	private Phase getPhaseFromString(String phaseString) {
		Phase phase = null;
		if (phaseString.equals("Human Phase II")) {
			phase = Phase.PHASE_2;
		} else if (phaseString.equals("Human Phase I")) {
			phase = Phase.PHASE_1;
		} else {
			logger.debug("Excluding reaction because it's not human phase 1 or human phase 2.");
		}
		return phase;
	}
	
	
	private String removeStereochemistryIfSpecified(String inchi) {
		
		if (removeStereochemistry) {
			String inchiWithoutStereochemistry = MoleculeManipulator.generateInchiWithoutStereo(MoleculeManipulator.generateMoleculeFromInchi(inchi));
			
			if (!inchi.equals(inchiWithoutStereochemistry)) {
				logger.debug(INCHI_HAS_STEREOCHEMISTRY_INFORMATION, inchi, inchiWithoutStereochemistry);
				stereoInchiCounter ++;
				inchi = inchiWithoutStereochemistry;	
			}
		}
		return inchi;
	}
	
	
	private String createIdentifier(int num) {
		int lenNum = Integer.toString(num).length();
		String zero = ZERO;
		String zeros = new String(new char[5-lenNum]).replace("\0", zero);
		return JSON_BIOTID + zeros + Integer.toString(num);
	}


}
