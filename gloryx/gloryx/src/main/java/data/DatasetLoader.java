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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ambit2.smarts.SMIRKSManager;
import ambit2.smarts.SMIRKSReaction;
import ambit2.smarts.SmartsConst;
import main.java.datasets.drugbankdata.MetabolismDataLoader;
import main.java.datasets.metxbiodb.MetXBioDBDatasetLoader;
import main.java.utils.Errors;
import main.java.utils.Filenames;
import main.java.utils.Phase;
import main.java.utils.TestParameters;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;

/**
 * This class is used to load data from various sources.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class DatasetLoader {

	private static final Logger logger = LoggerFactory.getLogger(DatasetLoader.class.getName());

	private static final String ERROR_READING_SDF_FILE = "Error reading SD file. Filename: {}. Exiting.";
	
	private static final String NUMBER_OF_PARENT_COMPOUNDS = "Number of parent compounds used for test: {}";
	
	private static final String LABEL_COMBINEDDATASET = "combined dataset";
	private static final String LABEL_METXBIODB = "MetXBioDB";
	private static final String LABEL_DRUGBANK = "DrugBank";
	
	
	public void getInputSmilesAndReferenceMetabolismData(TestParameters testParameters, Filenames filenames,
			Map<BasicMolecule, Set<BasicMolecule>> drugbankData, Map<BasicMolecule, Set<BasicMolecule>> combinedDataset,
			Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData, 
			List<String> inputSmiles, List<String> moleculeNames) {		
		

		if (testParameters.inputIsIndividualSmiles()) {  // use SMILES strings as input
			
			for (String smiles : filenames.getUserInputSmiles()) {
				inputSmiles.add(smiles);
			}

		} else if (testParameters.inputIsSdf()){ 

			// read in sdf and add all molecules to inputSmiles
			readInFromSDF(inputSmiles, moleculeNames, filenames);					

		} else {  // using database for comparison

			if (testParameters.getReference() == TestParameters.Reference.DRUGBANK) {  // use DrugBank data

				// get the data
				MetabolismDataLoader dloader = new MetabolismDataLoader();
				dloader.loadMetabolismData(filenames, drugbankData, testParameters.getPhase());
								
				DatasetManipulator.removeOverlapAndprovideInfoOnDatasetComparedToNewTestDataset(testMetabolismData, drugbankData, LABEL_DRUGBANK);

				addFromMapToInputSmiles(drugbankData, inputSmiles, moleculeNames);
				
				// note: no kekulization necessary because SMILES are not in aromatic format
				
			} else if (testParameters.getReference() == TestParameters.Reference.DRUGBANK_PLUS_METXBIODB) {
				
				getCombinedReferenceDataset(filenames, combinedDataset, drugbankData, testMetabolismData, testParameters.getPhase());
								
				addFromMapToInputSmiles(combinedDataset, inputSmiles, moleculeNames);
				
				// note: no kekulization necessary because SMILES from MetXBioDB were created from InChI, and are not in aromatic format.
				
			} else if (testParameters.getReference() == TestParameters.Reference.TEST_DATASET) {
				
				addFromMapToInputSmiles(testMetabolismData, inputSmiles, moleculeNames);
			}
				
			logger.debug(NUMBER_OF_PARENT_COMPOUNDS, inputSmiles.size());
		}
	}



	private void addFromMapToInputSmiles(Map<BasicMolecule, Set<BasicMolecule>> dataset, List<String> inputSmiles, List<String> moleculeNames) {
		
		for (BasicMolecule parent : dataset.keySet()) {
			
			String kekulizedSmiles = MoleculeManipulator.kekulizeMoleculeSmiles(parent.getSmiles());
			inputSmiles.add(kekulizedSmiles);
			moleculeNames.add(parent.getId());
		}
	}


	public Map<BasicMolecule, Set<BasicMolecule>> getCombinedReferenceDataset(Filenames filenames, Map<BasicMolecule, Set<BasicMolecule>> combinedDataset, Map<BasicMolecule, Set<BasicMolecule>> drugbankData,
			Map<BasicMolecule, Set<BasicMolecule>> testMetabolismData, Phase phase) {
		
		logger.info("\nGetting DrugBank data...");
		
		// get the DrugBank data
		MetabolismDataLoader dloader = new MetabolismDataLoader();
		dloader.loadMetabolismData(filenames, drugbankData, phase);
		
		// write file
//		DatasetWriter.writeDatasetSmilesAndNumMetabolitesToFile(drugbankData, "/work/kops/metaboliteproject/dataset/reference_comprehensive/drugbank_info.txt", false);
		
		DatasetManipulator.removeOverlapAndprovideInfoOnDatasetComparedToNewTestDataset(testMetabolismData, drugbankData, LABEL_DRUGBANK);
		DatasetManipulator.logAmountOfDataForPhaseAndEnzyme(drugbankData);

		logger.info("\nGetting MetXBioDB data...");
		
		// get the MetXBioDB data
		MetXBioDBDatasetLoader dbloader = new MetXBioDBDatasetLoader(true);  // remove stereochemistry
		Map<BasicMolecule, Set<BasicMolecule>> metxbiodbData = dbloader.loadData(filenames, phase);
		
		// write file
//		DatasetWriter.writeDatasetSmilesAndNumMetabolitesToFile(metxbiodbData, "/work/kops/metaboliteproject/dataset/reference_comprehensive/metxbiodb_info.txt",true);
				
		DatasetManipulator.removeOverlapAndprovideInfoOnDatasetComparedToNewTestDataset(testMetabolismData, metxbiodbData, LABEL_METXBIODB);
		DatasetManipulator.logAmountOfDataForPhaseAndEnzyme(metxbiodbData);

		
		logger.info("\nCombining datasets...");
		DatasetManipulator.combineDatasets(metxbiodbData, drugbankData, combinedDataset);
		
		if (logger.isInfoEnabled()) {
			DatasetManipulator.getBasicMetabolismInfo(combinedDataset);
		}
		
		logger.info("\nRemoving overlap of combined dataset with new test dataset...");
		
		DatasetManipulator.removeOverlapAndprovideInfoOnDatasetComparedToNewTestDataset(testMetabolismData, combinedDataset, LABEL_COMBINEDDATASET);
		DatasetManipulator.logAmountOfDataForPhaseAndEnzyme(combinedDataset);

		return combinedDataset;
	}


	/**
	 * This method can be used to check if the dataset contains parent-metabolite pairs that correspond to oxidation 
	 * to an aldehyde (as opposed to oxidation directly to a carboxylic acid). 
	 * Used during development of GLORY.
	 * Returns a boolean indicating whether the product is an aldehyde formed by oxidation of the parent molecule.
	 * 
	 * @param aldehydeSMIRKS
	 * @param foundAnAldehyde
	 * @param parentMolecule
	 * @param molecule
	 * @return
	 * @throws Exception
	 * @throws CloneNotSupportedException
	 */
	private Boolean checkIfExpectedTransformationOccurred(String aldehydeSMIRKS, Boolean foundAnAldehyde,
			IAtomContainer parentMolecule, IAtomContainer molecule) throws Exception, CloneNotSupportedException {
		
		SMIRKSManager smirksManager = new SMIRKSManager(SilentChemObjectBuilder.getInstance());
		SMIRKSReaction transformation = smirksManager.parse(aldehydeSMIRKS);
		if (smirksManager.hasErrors()) {
			logger.error("Error parsing SMIRKS {}", aldehydeSMIRKS);
		}

		IAtomContainerSet products = smirksManager.applyTransformationWithSingleCopyForEachPos(parentMolecule.clone(), null, transformation, SmartsConst.SSM_MODE.SSM_ALL);
		
		// now check if one of the products of the transformation is the molecule
		String knownInchi = MoleculeManipulator.generateInchiWithoutStereo(molecule);
		if (products != null) {
			for (IAtomContainer p : products.atomContainers()) {
				String productInchi = MoleculeManipulator.generateInchiWithoutStereo(p);
				if (knownInchi.equals(productInchi)) {
					logger.debug("this aldehyde was created with alcohol oxidation transformation");
					foundAnAldehyde = true;
					break;
				}
			}
		}
		return foundAnAldehyde;
	}


	/**
	 * Reads in molecules from user-specified SDF file.
	 * Does not continue reading the sdf file if a null or broken SDF entry occurs, because that would mess up the numbering of the input molecules.
	 * See comments within this method's source code for more details.
	 * 
	 * @param inputSmiles
	 * @param moleculeNames
	 * @param filenames
	 */
	private void readInFromSDF(List<String> inputSmiles, List<String> moleculeNames, Filenames filenames) {
		

		try (IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(new File(
				filenames.getUserInputFilename())), DefaultChemObjectBuilder.getInstance(), 
				false)) { // this boolean indicates whether or not to continue reading the file if a 
						  // null or broken SD entry occurs - however, if set to true, any broken molecules 
						  // just disappear (hasNext not true for them) so the numbering of the input 
						  // molecules would be all messed up. Therefore this value is set to false for now. //TODO

			
			while (reader.hasNext()) {
				IAtomContainer molecule = reader.next();
				inputSmiles.add(MoleculeManipulator.generateSmiles(molecule));

				String moleculeTitle = molecule.getProperty(CDKConstants.TITLE);
				moleculeNames.add(moleculeTitle);
				
			}
		} catch (IOException e) { // includes FileNotFoundException
			logger.error(ERROR_READING_SDF_FILE, filenames.getUserInputFilename());
			
			Errors.createErrorHtmlAndExit(filenames, Errors.INPUT_FILE_COULD_NOT_BE_READ);
		} 
		// If the SD file is corrupted or broken in some way, no exception is thrown. Therefore, check whether any input molecules were found.
		// Note that as long as the first entry in the SD file can be read properly, there will be no indication of whether or not the whole file could be read.
		if (inputSmiles.isEmpty()) {
			logger.error("Input SD file is broken. No input molecules could be read. Exiting.");
			
			Errors.createErrorHtmlAndExit(filenames, Errors.INPUT_SD_FILE_BROKEN);
		}
	}

	
	private static List<String> getInputSmiles(final Set<BasicMolecule> allParents) {
		List<String> inputSmiles = new ArrayList<>();
		String smiles;
		for (BasicMolecule parent : allParents) {
			smiles = parent.getSmiles();
			inputSmiles.add(smiles);
		}
		return inputSmiles;
	}

}
