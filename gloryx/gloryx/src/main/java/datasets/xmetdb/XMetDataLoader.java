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

package main.java.datasets.xmetdb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.datasets.drugbankdata.DrugbankStructureLoader;
import main.java.utils.analysis.GenericOverlapCalculator;


/**
 * This is an old class that loads data from XMetDB and checks the overlap with the DrugBank dataset, 
 * which was CYP data only at the time this code was written (during the development of GLORY).
 * This code is old and has not been tested recently.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class XMetDataLoader {

	private static final String ERROR_CLOSING_CSV_FILE = "Error closing CSV file {}";

	private static final String NUMBER_OF_VALID_REACTIONS = "Total number of valid reactions (reactant and product both have InChI): {}";
	private static final String NUMBER_OF_PARENT_COMPOUNDS = "Number of parent compounds in XMetDB (CYP reactions only): {}";
	private static final String ENDLINE = "\n";
	private static final String EMPTY_STRING = "";
	private static final String ERROR_READING_CSV_FILE = "Error reading CSV file {}";
	private static final String ERROR_WRITING_OUTPUT_FILE = "Error writing output file.";

	
	private static final Logger logger = LoggerFactory.getLogger(XMetDataLoader.class.getName());

	
	public static void main(String[] args) {

		String inputFilename = args[args.length-2];
		String outputFilename = args[args.length-1];
		String drugbankSdFile = args[args.length-3];
		
		HashMap<String, HashSet<String>> metabolismInfo = parseDatabaseCSV(inputFilename);
		if (metabolismInfo != null) {
			
			writeNumberOfMetabolitesToFile(outputFilename, metabolismInfo);
			logger.info(NUMBER_OF_PARENT_COMPOUNDS, metabolismInfo.keySet().size());
		}
		

		// calculate overlap b/t XMetDB and DrugBank
		
		// read in DrugBank approved drugs sdf, get SMILES and convert to set of InChIs
		DrugbankStructureLoader drugbankLoader = new DrugbankStructureLoader();
		Map<String, String> drugbankIdsSmiles = drugbankLoader.loadSmilesFromSdf(drugbankSdFile);
		Set<String> drugbankInchis = new HashSet<>(); // = GenericOverlapCalculator.getInchisForApprovedDrugs(drugbankIdsSmiles); // TODO fix this
		
		int newCounter = 0;
		for (Entry<String, HashSet<String>> tree : metabolismInfo.entrySet()) {
			
			String parent = tree.getKey();
			if (!drugbankInchis.contains(parent)) {
				newCounter ++;
			}
		}
		logger.info("Number of parent compounds that are not contained in DrugBank: {}", newCounter);

	}

	private static HashMap<String, HashSet<String>> parseDatabaseCSV(String inputFilename) {
		File csv = new File(inputFilename);
		CSVParser parser;
		try {
			parser = CSVParser.parse(csv, Charset.defaultCharset(), CSVFormat.DEFAULT);
		} catch (IOException e) {
			logger.error(ERROR_READING_CSV_FILE, inputFilename);
			return null;
		}
		
		HashMap<String, HashSet<String>> metabolismInfo = new HashMap<>();
		String parentInchi = EMPTY_STRING;
		String metaboliteInchi = EMPTY_STRING;
		int validReactionCounter = 0;
		
		for (CSVRecord record : parser) {
			// extract parent and metabolite information and add to metabolism data map
			
			parentInchi = record.get(3);
			metaboliteInchi = record.get(5);
			
			if (!parentInchi.isEmpty() && !metaboliteInchi.isEmpty()) {
				
				validReactionCounter ++;
				
				if (metabolismInfo.containsKey(parentInchi)) {
					metabolismInfo.get(parentInchi).add(metaboliteInchi);

				} else {
					metabolismInfo.put(parentInchi, new HashSet<String>());
					metabolismInfo.get(parentInchi).add(metaboliteInchi);
				}
			}

			parentInchi = EMPTY_STRING;
			metaboliteInchi = EMPTY_STRING;
			
		}
		
		// close the parser
		try {
			parser.close();
		} catch (IOException e) {
			logger.error(ERROR_CLOSING_CSV_FILE, inputFilename);
			return null;
		}
		
		logger.info(NUMBER_OF_VALID_REACTIONS, validReactionCounter);
		return metabolismInfo;
	}

	private static void writeNumberOfMetabolitesToFile(String outputFilename,
			HashMap<String, HashSet<String>> metabolismInfo) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))){

			for (Entry<String, HashSet<String>> tree : metabolismInfo.entrySet()) {
				HashSet<String> metabolites = tree.getValue();
				writer.append(metabolites.size() + ENDLINE);
			}
			
		} catch (IOException e) {
			logger.error(ERROR_WRITING_OUTPUT_FILE);
		}
	}

}
