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

package main.java.datasets.biotransformertestdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;

/**
 * Data loader class specifically for the CYP test dataset published in the BioTransformer paper (additional file 6). 
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class BiotransformerTestdataLoader {

	
	
	private static final String NUMBER_OF_MOLECULES_IN_FILE = "Number of molecules in SD file: {}. Number of molecules with known CYP metabolites: {}.";
	private static final String NAME_PROPERTY = "NAME";
	private static final String INFORMATION_IS_MISSING_FROM_RECORD = "Information is missing from record {}";
	private static final String KNOWN_METABOLITES_PROPERTY = "Reported_Metabolites";
	private static final String INCHI_PROPERTY = "InChI";
	private static final String ERROR_READING_SDF_FILE = "Error reading SDF file! Filename: {}";
	
	
	
	private static final Logger logger = LoggerFactory.getLogger(BiotransformerTestdataLoader.class.getName());

	
	
	public Map<BasicMolecule, Set<BasicMolecule>> loadDataFromSdf(String filename) {
		// key = parent  molecule, value = set of known metabolites

		File sdfFile = new File(filename);
		return loadDataFromSdf(sdfFile);
	}

	
	public Map<BasicMolecule, Set<BasicMolecule>> loadDataFromSdf(File sdfFile) {
		// key = parent  molecule, value = set of known metabolites
				
		String filename = sdfFile.getName();
		
		Map<BasicMolecule, Set<BasicMolecule>> metabolismData = new HashMap<>();
		
		int counter = 0;
		
		try (IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance())) {
		
			while (reader.hasNext()) {
				addStructure(metabolismData, reader);
				counter ++;
			}
		
		
		} catch (IOException e) {
			logger.error(ERROR_READING_SDF_FILE, filename, e);
			return metabolismData;
		}
		
		logger.debug(NUMBER_OF_MOLECULES_IN_FILE, counter, metabolismData.size());
		
		int numKnownMetabolites = 0;
		for (Entry<BasicMolecule, Set<BasicMolecule>> entry : metabolismData.entrySet()) {
			numKnownMetabolites += entry.getValue().size();
		}
		
		logger.debug("Number of known metabolites: {}", numKnownMetabolites);

		return metabolismData;
	}


	private void addStructure(Map<BasicMolecule, Set<BasicMolecule>> metabolismData, final IteratingSDFReader reader) {
		
		IAtomContainer parentMolecule = reader.next();
		String parentSmiles = MoleculeManipulator.generateSmiles(parentMolecule);
		String parentName = parentMolecule.getProperty(NAME_PROPERTY);
		String calculatedParentInchi = MoleculeManipulator.generateInchi(parentMolecule);
		// TODO first try without using stereochemistry for inchi
		
		if (logger.isDebugEnabled()) {
			String parentInchi = parentMolecule.getProperty(INCHI_PROPERTY);
			if (!parentInchi.equals(calculatedParentInchi)) {
				logger.warn("The InChI provided in the SD file is not the same as the InChI generated from the SD information for "
						+ "molecule {}.\nCalculated InChI: {}\nUsing the calculated InChI for standardization reasons.", parentName, calculatedParentInchi);
			}
		}
				
		BasicMolecule parent = new BasicMolecule();
		parent.setId(parentName);
		parent.setSmiles(parentSmiles);
		parent.setInchi(calculatedParentInchi);

		String reportedMetabolitesString = parentMolecule.getProperty(KNOWN_METABOLITES_PROPERTY);
		logger.debug("reported: {}", reportedMetabolitesString);

		
		Set<BasicMolecule> knownMetabolites = new HashSet<>();
		
		BasicMolecule metabolite = null;
		for (String line : reportedMetabolitesString.split("\n")) {
			logger.debug("line: {}", line);
			
			if (line.startsWith("M")) {  // metabolite ID line
				metabolite = new BasicMolecule();
				metabolite.setId(line);
				
			} else if (line.startsWith("INCHI: ")) {  // InChI line
				
				Assert.notNull(metabolite, "Metabolite should not be null here.");
				String inchi = line.substring(7);

				if (inchi.equals("NULL")) {
					if (metabolite.getId() != null) {
						logger.info("Metabolite {} has no InChI, so it can't be included in the dataset.", metabolite.getId());
					}
				} else {
//					metabolite.setInchi(inchi);
					
					// Unfortunately, I have no choice but to generate SMILES from the InChI, since the InChI is the only structural information that is provided.
					String smiles = MoleculeManipulator.generateSmilesFromInchi(inchi);
					metabolite.setSmiles(smiles);
					
					// because some InChIs in the file are not standard InChIs. This is annoying, because one 
					// really shouldn't use the InChI as amolecule representation! However, I seem to have no other choice in this case.
					metabolite.setInchi(MoleculeManipulator.generateInchiFromSmiles(smiles));
					
					Boolean newMetab = knownMetabolites.add(metabolite);
					if (newMetab) {
						logger.debug("adding metabolite to set: {} with InChI {}", metabolite.getId(), metabolite.getInchi());
					} else {
						logger.debug("this metabolite of {} is a duplicate! {}", parentName, metabolite.getId());
						// note: this seems to never happen
					}
				}
			}
		}		
		logger.debug("number of metabolites for this molecule {}: {}", parentName, knownMetabolites.size());
		
		
		if (metabolismData.containsKey(parent)) {
			
			// note: this code doesn't seem to matter because the only molecule affected has no known metabolites

			logger.info("Molecule {} is already in the dataset according to the InChI {}. Is this parent compound a duplicate?", parent.getId(), parent.getInchi());
			logger.debug("number of metabolites: {}", metabolismData.get(parent).size());
			for (BasicMolecule m : knownMetabolites) {
				metabolismData.get(parent).add(m);
			}
			logger.debug("number of metabolites after adding: {}", metabolismData.get(parent).size());
			
			
		} else {
			metabolismData.put(parent, knownMetabolites);
		}
		
//		if (!knownMetabolites.isEmpty()) {  // ignore molecules with no known  metabolites
//		}
		

	}

	
	
	
	
}
