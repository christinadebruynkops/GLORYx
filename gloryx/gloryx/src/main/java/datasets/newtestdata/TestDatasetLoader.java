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

package main.java.datasets.newtestdata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

import main.java.utils.molecule.BasicMolecule;
import main.java.utils.Filenames;
import main.java.utils.molecule.MoleculeManipulator;

/** 
 * Loads the manually curated test dataset, either the test dataset for GLORY, which is in CSV format, 
 * or the test dataset for GLORYx, which is in JSON format.
 * Which test dataset will be used is determined by the file type of the filename specified in Filenames.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class TestDatasetLoader {

	
	private static final String SINGLE_SPACE = " ";
	private static final String EMPTY_STRING = "";
	private static final String ERROR_READING_CSV_FILE = "Error reading CSV file {}";
	private static final String NO_SMILES_FOUND_FOR_PARENT = "No SMILES found for parent {}";
	
	private static final String ERROR_PARSING_JSON_FILE = "Error parsing JSON file.";
	private static final String ERROR_READING_JSON_FILE = "Error reading JSON file.";

	
	private static final Logger logger = LoggerFactory.getLogger(TestDatasetLoader.class.getName());

	
	
	public Map<BasicMolecule, Set<BasicMolecule>> loadData(Filenames filenames) {		
		
		HashMap<BasicMolecule, Set<BasicMolecule>> metabolismData = new HashMap<>();
		
		
		if (filenames.getManualTestDbFilename().endsWith(".json")) {
			readInJson(filenames.getManualTestDbFilename(), metabolismData);
		} else if (filenames.getManualTestDbFilename().endsWith(".csv")) {
			readInCsv(filenames.getManualTestDbFilename(), metabolismData);
		} else {
			logger.error("Attempting to read a test dataset file that is neither a json nor a csv file. The test dataset cannot be read in.");
		}

		return metabolismData;
	}

	private void readInJson(String jsonFilename, HashMap<BasicMolecule, Set<BasicMolecule>> metabolismData) {
		
		JSONParser parser = new JSONParser();			
		try (FileReader reader = new FileReader(jsonFilename)) {
			
			JSONArray ja = (JSONArray) parser.parse(reader);
			logger.debug("size of json array: {}", ja.size());
			
	        @SuppressWarnings("unchecked")
	        int molnum = 1;
			Iterator<JSONObject> it = ja.iterator(); 
	        while (it.hasNext()) { 
	        		JSONObject dbEntry = it.next();
	        		processJsonRecord(metabolismData, dbEntry, molnum);
	        		molnum ++;
	        }

		} catch (IOException e1) {
			logger.error(ERROR_READING_JSON_FILE, e1);
		} catch (ParseException e2) {
			logger.error(ERROR_PARSING_JSON_FILE, e2);
		}
		
		
		if (logger.isInfoEnabled()) {
			logger.info("Number of parent compounds in test dataset: {}", metabolismData.size());
			
			int metCounter = 0;
			for (Set<BasicMolecule> metabolites : metabolismData.values()) {
				metCounter += metabolites.size();
			}
			logger.info("Total number of metabolites in test dataset: {}", metCounter);
		}
	}

	private void processJsonRecord(HashMap<BasicMolecule, Set<BasicMolecule>> metabolismData, JSONObject dbEntry, int molnum) {
		
		BasicMolecule parent = createParentMolecule(dbEntry, molnum);
		
		if (parent == null) {
			return;
		}
		
		Set<BasicMolecule> metabolites = createMetabolites(dbEntry);
		
		logger.debug("Found {} metabolites in dataset for parent {}", metabolites.size(), parent.getId());

		metabolismData.put(parent,  metabolites);

		return;
		
	}
	
	private BasicMolecule createParentMolecule(final JSONObject dbEntry, final int molnum) {
		
		String parentName = (String) dbEntry.get("drugName");  // this is the name of the parent molecule
		if (parentName.isEmpty() ) {
			logger.error("This entry is missing a drug name. Skipping.");
			return null;
		}
		String parentSmiles = (String) dbEntry.get("smiles");
		
		logger.debug("parent {}: {}", parentName, parentSmiles);
		
		if (parentSmiles == null) {
			logger.debug(NO_SMILES_FOUND_FOR_PARENT, parentName);
		} 

		if (parentSmiles != null) {
			String parentInchi = MoleculeManipulator.generateInchiWithoutStereoFromSmiles(parentSmiles); 
			return new BasicMolecule(Integer.toString(molnum), parentName, parentSmiles, parentInchi);
		}
		
		return null;
	}

	private Set<BasicMolecule> createMetabolites(final JSONObject dbEntry) {
		
		Set<BasicMolecule> metabolites = new HashSet<>();
		
		JSONArray metabolitesArray = (JSONArray) dbEntry.get("metabolites");
		
        @SuppressWarnings("unchecked")
		Iterator<JSONObject> it = metabolitesArray.iterator(); 
        while (it.hasNext()) { 
        		JSONObject metabolite = it.next();
        		if ( (long) metabolite.get("generation") == 1) {
        			
        			String metaboliteName = (String) metabolite.get("metaboliteName");
        			
        			String smilesString = (String) metabolite.get("smiles");
        			if (!smilesString.equals(EMPTY_STRING) && !smilesString.equals(SINGLE_SPACE)) {
        				
        				createMetaboliteBasicMolecule(metabolites, smilesString, metaboliteName);
        			}
        		}
        }
		
		return metabolites;
	}

	private void readInCsv(String csvFilename, HashMap<BasicMolecule, Set<BasicMolecule>> metabolismData) {
		File csv = new File(csvFilename);
		try (CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader().withQuote(null).withIgnoreEmptyLines())) {

			for (CSVRecord record : parser) {
				// extract parent and metabolite information and add to metabolism data map
				processCsvRecord(metabolismData, record);				
			}
		
		} catch (IOException e) {
			logger.error(ERROR_READING_CSV_FILE, csvFilename);
		}
		
		return;
	}

	private void processCsvRecord(Map<BasicMolecule, Set<BasicMolecule>> metabolismData, final CSVRecord record) {

		BasicMolecule parent = createParentMolecule(record);
		
		if (parent == null) {
			return;
		}
		
		Set<BasicMolecule> metabolites = createMetabolites(record);
		
		logger.debug("Found {} metabolites in dataset for parent {}", metabolites.size(), parent.getId());

		metabolismData.put(parent,  metabolites);

		return;
	}

	private BasicMolecule createParentMolecule(final CSVRecord record) {
		
		String parentId = record.get(0);  // this is the name of the parent molecule
		if (parentId.isEmpty() ) {
			return null;
		}
		String parentSmiles = record.get(2).replace("\"", EMPTY_STRING);
		
		logger.debug("parent smiles: {}", parentSmiles);
		
		if (parentSmiles == null) {
			logger.debug(NO_SMILES_FOUND_FOR_PARENT, parentId);
		} 

		if (parentSmiles != null) {
			String parentInchi = MoleculeManipulator.generateInchiFromSmiles(parentSmiles);
			return new BasicMolecule(parentId, parentSmiles, parentInchi);
		}
		
		return null;
	}
	
	private Set<BasicMolecule> createMetabolites(final CSVRecord record) {
		
		Set<BasicMolecule> metabolites = new HashSet<>();
		
		String metaboliteSmilesEntry = record.get(3);
		logger.debug("met string: {}", metaboliteSmilesEntry);
		String[] metaboliteSmilesArray = metaboliteSmilesEntry.split(SINGLE_SPACE); 
		
		Assert.notNull(metaboliteSmilesArray, "Error splitting the metabolites field into individual metabolite SMILES");
		
		for (String part : metaboliteSmilesArray) {
			
			if (part != null && !part.equals(EMPTY_STRING) && !part.equals(SINGLE_SPACE)) {  // in case there are accidentally too many spaces between metabolite SMILES
				createMetaboliteBasicMoleculeCsv(metabolites, part);
			}
		}
		
		return metabolites;
	}

	private void createMetaboliteBasicMoleculeCsv(Set<BasicMolecule> metabolites, String string) {
		String metaboliteSmiles = string.replace("\"", EMPTY_STRING);
		logger.debug("metabolite smiles: {}", metaboliteSmiles); 
		
		metabolites.add(new BasicMolecule(EMPTY_STRING, metaboliteSmiles, MoleculeManipulator.generateInchiFromSmiles(metaboliteSmiles)));
	}
	
	private void createMetaboliteBasicMolecule(Set<BasicMolecule> metabolites, String metaboliteSmiles, String metaboliteName) {
		
		logger.debug("metabolite {}: {}", metaboliteName, metaboliteSmiles); 
		
		metabolites.add(new BasicMolecule(metaboliteName, metaboliteSmiles, MoleculeManipulator.generateInchiFromSmiles(metaboliteSmiles)));
	}
	
	
}
