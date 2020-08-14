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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.utils.Filenames;
import main.java.utils.molecule.BasicMolecule;


/** 
 * This class is used to write the dataset to a JSON file. 
 * The output filename is hard-coded in the Filenames class.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class JsonFileWriter {

	private static final String METABOLITES = "Metabolites";
	private static final String METXBIODB_BIOTRANSFORMATION_ID = "MetXBioDB biotransformation ID";
	private static final String PHASE = "Phase";
	private static final String METXBIODB_ID = "MetXBioDB ID";
	private static final String DRUG_BANK_ID = "DrugBank ID";
	private static final String PARENT_MOLECULE = "Parent molecule";
	private static final String NA = "N/A";
	private static final String INCHI = "InChI";
	private static final String SMILES = "SMILES";
	private static final String ERROR_READING_JSON_FILE = "Error reading json file";
	private static final String ERROR_WRITING_JSON_FILE = "Error writing JSON file";
	
	
	private static final Logger logger = LoggerFactory.getLogger(JsonFileWriter.class.getName());


	private JsonFileWriter() {
		throw new IllegalStateException("Utility class");
	}

	
	@SuppressWarnings("unchecked")
	public static void writeDatasetToJSONFile(Map<BasicMolecule, Set<BasicMolecule>> dataset, Filenames filenames) {		
		

		// create list of all 1-generation metabolite trees:
		JSONArray metabolismTrees = new JSONArray();

		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : dataset.entrySet()) {

			BasicMolecule parent = entry.getKey();

			Map<Object, Object> parentObject = new LinkedHashMap<>(); 
			parentObject.put(SMILES, parent.getSmiles());
			parentObject.put(INCHI, parent.getInchi());
			
			String idParentDrugbank;
			if (parent.getDrugBankID() == null) { // || !parent.getDrugBankID().startsWith("DB")) {
				idParentDrugbank = NA;
			} else {
				idParentDrugbank = parent.getDrugBankID();
			}
			
			String idParentMetX;
			if (parent.getMetXBioDBID() == null) {
				idParentMetX = NA;
			} else {
				idParentMetX = parent.getMetXBioDBID();
			}
			
			parentObject.put(DRUG_BANK_ID, idParentDrugbank); 
			parentObject.put(METXBIODB_ID, idParentMetX);

			Map<Object, Object> metabolismTree = new LinkedHashMap<>(); 
			metabolismTree.put(PARENT_MOLECULE, parentObject);

			JSONArray metabolitesList = new JSONArray();


			Set<BasicMolecule> metabolites = entry.getValue();

			for (BasicMolecule metabolite : metabolites) {

				Map<String, Object> singleMetaboliteObject = new LinkedHashMap<>(); 

				singleMetaboliteObject.put(SMILES, metabolite.getSmiles());
				singleMetaboliteObject.put(INCHI, metabolite.getInchi());
				
				singleMetaboliteObject.put(PHASE, metabolite.getMetabolismPhase());
				
				String idDrugbank;
				if (metabolite.getDrugBankID() == null) {
					idDrugbank = NA;
				} else {
					idDrugbank = metabolite.getDrugBankID();
				}
				
				singleMetaboliteObject.put(DRUG_BANK_ID, idDrugbank);  
				String idMetXBioDB;
				if (metabolite.getMetXBioDBID() == null) {
					idMetXBioDB = NA;
				} else {
					idMetXBioDB = metabolite.getMetXBioDBID();
				}
				singleMetaboliteObject.put(METXBIODB_BIOTRANSFORMATION_ID, idMetXBioDB);

				metabolitesList.add(singleMetaboliteObject);
			}
			metabolismTree.put(METABOLITES, metabolitesList);
			metabolismTrees.add(metabolismTree);
		}

		//Write JSON file
		try (FileWriter file = new FileWriter(filenames.getOutputJsonDatasetFilename())) {

			// make the resulting file human-readable
			ObjectMapper mapper = new ObjectMapper();
			file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metabolismTrees)); 
			file.flush();

		} catch (IOException e) {
			logger.error(ERROR_WRITING_JSON_FILE);
		}
		
		// test unescaping of \ in SMILES when reading in
//		testWrittenJSONFileForBackslashesInSmiles();
	}

	
	/** 
	 * Used to test whether escaping/unescaping of backslashes in SMILES is working properly.
	 * 
	 * @param filenames
	 */
	@SuppressWarnings("unused")
	private static void testWrittenJSONFileForBackslashesInSmiles(Filenames filenames) {
		
		JSONParser parser = new JSONParser();			
		try (FileReader reader = new FileReader(filenames.getOutputJsonDatasetFilename())) {

			Object obj = parser.parse(reader);
			JSONArray array = (JSONArray) obj;
			
			for (Object entry : array) {
				JSONObject entryobj = (JSONObject) entry;
				
				JSONObject parentobj = (JSONObject) entryobj.get(PARENT_MOLECULE);

				String smiles = (String) parentobj.get(SMILES);
				if (smiles.contains("\\\\")) {
					logger.error("Unescaping didn't happen: {}", smiles);
				}
			}

		} catch (IOException e1) {
			logger.error(ERROR_READING_JSON_FILE, e1);
		} catch (ParseException e2) {
			logger.error(ERROR_READING_JSON_FILE, e2);
		}
	}



}
