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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import main.java.utils.molecule.MoleculeManipulator;

/**
 * Loads molecule structures from DrugBank.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class DrugbankStructureLoader {

	private static final String ERROR_READING_INCHI_FROM_SDF = "Error reading InChI from sdf";
	private static final String INCHI = "InChI";
	private static final String INFORMATION_IS_MISSING_FROM_RECORD = "Information is missing from record {}";
	private static final String SMILES = "SMILES";
	private static final String DATABASE_ID = "DATABASE_ID";
	private static final String ERROR_CLOSING_FILE_READER = "Error closing file reader for {}";
	private static final String NUMBER_OF_FAILED_ENTRIES = "Number of failed entries in sdf: {}";
	private static final String NUMBER_OF_MOLECULES_ADDED = "Number of molecules added to HashMap: {}";
	private static final String ERROR_READING_SDF_FILE = "Error reading SDF file! Filename: {}";
	
	private static final Logger logger = LoggerFactory.getLogger(DrugbankStructureLoader.class.getName());


	public Map<String, String> loadSmilesFromSdf(String filename) {
		// returns a map, key = database ID, value = smiles

		File sdfFile = new File(filename);
		return loadSmilesFromSdf(sdfFile);
	}

	
	public Map<String, String> loadSmilesFromSdf(File sdfFile) {
		
		// returns a map, key = database ID, value = smiles
		
		String filename = sdfFile.getName();
		
		Map<String, String> molecules = new HashMap<>();
		int nFailedEntries = 0;
		
		IteratingSDFReader reader;
		try {
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
		} catch (FileNotFoundException e) {
			logger.error(ERROR_READING_SDF_FILE, filename);
			return molecules;
		}
		
		while (reader.hasNext()) {
			nFailedEntries = addStructure(molecules, nFailedEntries, reader);
		}
		tryClosingReader(filename, reader);
		
		logger.debug(NUMBER_OF_MOLECULES_ADDED, molecules.size());
		logger.debug(NUMBER_OF_FAILED_ENTRIES, nFailedEntries);

		return molecules;
	}
	
	
	public Set<String> loadInchisFromSdf(String filename) {
		File sdfFile = new File(filename);
		return loadInchisDirectlyFromSdf(sdfFile);
	}
	
	public Set<String> loadInchisDirectlyFromSdf(File sdfFile) {

		HashSet<String> inchis = new HashSet<>();
		int nFailedEntries = 0;

		IteratingSDFReader reader;
		try {
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
		} catch (FileNotFoundException e) {
			logger.error(ERROR_READING_SDF_FILE, sdfFile.getName());
			return inchis;
		}
		
		while (reader.hasNext()) {
			IAtomContainer molecule = reader.next();
			String inchi = molecule.getProperty(INCHI);
			if (StringUtils.isEmpty(inchi)) {
				logger.error(ERROR_READING_INCHI_FROM_SDF);
			} else {
				inchis.add(inchi);
			}
		}
		tryClosingReader(sdfFile.getName(), reader);
		
		logger.debug(NUMBER_OF_MOLECULES_ADDED, inchis.size());
		logger.debug(NUMBER_OF_FAILED_ENTRIES, nFailedEntries);

		return inchis;
	}


	private void tryClosingReader(final String filename, IteratingSDFReader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			logger.error(ERROR_CLOSING_FILE_READER, filename);
		}
	}


	private int addStructure(Map<String, String> molecules, int nFailedEntries, final IteratingSDFReader reader) {
		
		IAtomContainer molecule = reader.next();
		
//		if (MoleculeManipulator.checkIfOnlyHydrogens(molecule) || MoleculeManipulator.checkIfWater(molecule)) {
//			if (MoleculeManipulator.checkIfOnlyHydrogens(molecule)) {
//				logger.debug("Excluding DrugBank structure because it is a hydrogen molecule");
//			} else {
//				logger.debug("Excluding DrugBank structure because it is a water molecule");
//			}
//			// not including this as a failed entry because it didn't really fail; it's my own filter
////			nFailedEntries += 1;
//			return nFailedEntries;
//		}
		
		String dbid = molecule.getProperty(DATABASE_ID);
		String smiles = molecule.getProperty(SMILES);
		
		// If there is no SMILES field in the entry, convert the molecule into SMILES format.
		if (StringUtils.isEmpty(smiles)) {
			smiles = MoleculeManipulator.generateSmiles(molecule);
		}
		
		// If there is no database ID field in the entry, use InChI instead.
		if (StringUtils.isEmpty(dbid)) {
			dbid = MoleculeManipulator.generateInchiWithoutStereo(molecule);
			logger.warn("No database ID for sdf entry found. Using InChI instead.");
		}
		
		nFailedEntries = checkAndAdd(molecules, nFailedEntries, dbid, smiles);
		return nFailedEntries;
	}


	private int checkAndAdd(Map<String, String> molecules, int nFailedEntries, String dbid, String smiles) {
		if (StringUtils.isEmpty(dbid) || StringUtils.isEmpty(smiles)) {
			logger.debug(INFORMATION_IS_MISSING_FROM_RECORD, dbid);
			nFailedEntries += 1;
			
		} else {
			molecules.put(dbid, smiles);
		}
		return nFailedEntries;
	}






}
