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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.molecule.BasicMolecule;
import main.java.utils.molecule.MoleculeManipulator;


/**
 * Used to write dataset(s) to files (SMILES or SDF). 
 * The output filenames are hard-coded.
 * The program will exit once the file has been written.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class DatasetWriter {


	private static final String LINE_SEPARATOR = "line.separator";
	private static final String ERROR_WRITING_FILE_OF_DATASET_PARENT_SMILES = "Error writing file of dataset parent smiles.";
	private static final String NUMBER_OF_PARENT_SMILES_WRITTEN_TO_FILE = "Number of parent smiles written to file: {}";
	private static final String EXITING = "Exiting because only wanted to write the dataset to a file.";
	private static final String UTILITY_CLASS = "Utility class";

	private DatasetWriter() {
		throw new IllegalStateException(UTILITY_CLASS);
	}

	private static final Logger logger = LoggerFactory.getLogger(DatasetWriter.class.getName());


	public static void writeDatasetParentCompoundsToSdfAndExit(List<String> inputSmiles) {

		try (SDFWriter writer = new SDFWriter(new BufferedWriter(new FileWriter("/work/kops/metaboliteproject/dataset/reference/parents_without_test_dataset_overlap.sdf")))){

			IAtomContainerSet inputMolSet = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);
			for (String s : inputSmiles) {
				IAtomContainer a = MoleculeManipulator.generateMoleculeFromSmiles(s);
				inputMolSet.addAtomContainer(a);
			}

			tryToWriteSdf(writer, inputMolSet);

		} catch (IOException e) {
			logger.error("Error writing file of drugbank parent smiles", e);
		}
		logger.info(EXITING);
		System.exit(0);
	}


	private static void tryToWriteSdf(SDFWriter writer, IAtomContainerSet inputMolSet) {
		try {
			writer.write(inputMolSet);
		} catch (CDKException e) {
			logger.error("Error writing SD file.", e);
		}
	}


	public static void writeDatasetSmilesToFileAndExit(List<String> inputSmiles) {
		// for writing all dataset parent compounds as SMILES to file

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("/work/kops/metaboliteproject/dataset/testdataset_comprehensive/parent_smiles.txt"))){

			int numberWritten = 0; 

			for (String singleInputSmiles : inputSmiles) {
				writer.write('"' + singleInputSmiles + '"' + System.getProperty(LINE_SEPARATOR));
				numberWritten ++;
			}

			logger.debug(NUMBER_OF_PARENT_SMILES_WRITTEN_TO_FILE, numberWritten);
		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_DATASET_PARENT_SMILES, e);
		}
		logger.info(EXITING);
		System.exit(0);
	}


	public static void writeDatasetSmilesToFileAndExit(Map<BasicMolecule, Set<BasicMolecule>> dataset) {
		// for writing all dataset parent compounds as SMILES to file

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("/work/kops/metaboliteproject/dataset/testdataset_comprehensive/parent_smiles.txt"))){

			int numberWritten = 0; 

			for (Entry<BasicMolecule, Set<BasicMolecule>> entry : dataset.entrySet()) {
				String parentsmiles = entry.getKey().getSmiles();
				
				writer.write('"' + parentsmiles + '"' + System.getProperty(LINE_SEPARATOR));
				numberWritten ++;
			}

			logger.debug(NUMBER_OF_PARENT_SMILES_WRITTEN_TO_FILE, numberWritten);
		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_DATASET_PARENT_SMILES, e);
		}
		logger.info(EXITING);
		System.exit(0);
	}

	
	public static void writeDatasetSmilesAndNumMetabolitesToFile(Map<BasicMolecule, Set<BasicMolecule>> dataset, String filename, Boolean exit) {
		// for writing all dataset parent compounds as SMILES to file

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){

			writer.write("numMetabolites,parentSmiles" + System.getProperty(LINE_SEPARATOR));

			int numberWritten = 0; 

			for (Entry<BasicMolecule, Set<BasicMolecule>> entry : dataset.entrySet()) {
				String parentsmiles = entry.getKey().getSmiles();
				int numMetabolites = entry.getValue().size();
				
				writer.write(String.valueOf(numMetabolites) + "," + '"' + parentsmiles + '"' + System.getProperty(LINE_SEPARATOR));
				numberWritten ++;
				
//				String smiles1 = "CC[C@H](C)[C@H](NC(=O)[C@H](CC1=CC=CC=C1)NC(=O)[C@H](CCC(O)=O)NC(=O)[C@H](CCCCNC(=O)COCCOCCNC(=O)COCCOCCNC(=O)CC[C@@H](NC(=O)CCCCCCCCCCCCCCCCC(O)=O)C(O)=O)NC(=O)[C@H](C)NC(=O)[C@H](C)NC(=O)[C@H](CCC(N)=O)NC(=O)CNC(=O)[C@H](CCC(O)=O)NC(=O)[C@H](CC(C)C)NC(=O)[C@H](CC1=CC=C(O)C=C1)NC(=O)[C@H](CO)NC(=O)[C@H](CO)NC(=O)[C@@H](NC(=O)[C@H](CC(O)=O)NC(=O)[C@H](CO)NC(=O)[C@@H](NC(=O)[C@H](CC1=CC=CC=C1)NC(=O)[C@@H](NC(=O)CNC(=O)[C@H](CCC(O)=O)NC(=O)C(C)(C)NC(=O)[C@@H](N)CC1=CNC=N1)[C@@H](C)O)[C@@H](C)O)C(C)C)C(=O)N[C@@H](C)C(=O)N[C@@H](CC1=CNC2=C1C=CC=C2)C(=O)N[C@@H](CC(C)C)C(=O)N[C@@H](C(C)C)C(=O)N[C@@H](CCCNC(N)=N)C(=O)NCC(=O)N[C@@H](CCCNC(N)=N)C(=O)NCC(O)=O";
//				String smiles2 = "CC[C@@H]1NC(=O)[C@H]([C@H](O)[C@H](C)C\\C=C\\C)N(C)C(=O)[C@H](C(C)C)N(C)C(=O)[C@H](CC(C)C)N(C)C(=O)[C@H](CC(C)C)N(C)C(=O)[C@@H](C)NC(=O)[C@H](C)NC(=O)[C@H](CC(C)C)N(C)C(=O)[C@@H](NC(=O)[C@H](CC(C)C)N(C)C(=O)CN(C)C1=O)C(C)C";
//				String smiles3 = "CC[C@H](C)[C@H](NC(=O)[C@H](CC1=CC=C(O)C=C1)NC(=O)[C@@H](NC(=O)[C@H](CCCN=C(N)N)NC(=O)[C@@H](N)CC(O)=O)C(C)C)C(=O)N[C@@H](CC1=CN=CN1)C(=O)N1CCC[C@H]1C(=O)N[C@@H](CC1=CC=CC=C1)C(O)=O";
//				String smiles4 = "C/C(/[H])=C(\\[H])/C[C@@]([H])(C)[C@]([H])([C@@]1([H])C(=N[C@@]([H])(CC)C(=O)N(C)CC(=O)N(C)[C@@]([H])(CC(C)C)C(=N[C@@]([H])(C(C)C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=N[C@@]([H])(C)C(=N[C@]([H])(COCCO)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(C(C)C)C(=O)N1C)O)O)O)O)O";
//				String smiles5 = "C/C(/[H])=C(\\[H])/C[C@@]([H])(C)C(=O)[C@@]1([H])C(=N[C@@]([H])(C(C)C)C(=O)N(C)CC(=O)N(C)[C@@]([H])(CC(C)C)C(=N[C@@]([H])(C(C)C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=N[C@@]([H])(C)C(=N[C@]([H])(C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(C(C)C)C(=O)N1C)O)O)O)O";
//				String smiles6 = "C/C(/[H])=C(\\[H])/C[C@@]([H])(C)[C@]([H])([C@]1([H])C(=N[C@]([H])(CC)C(=O)N(C)CC(=O)N(C)[C@]([H])(CC(C)C)C(=N[C@@]([H])(C(C)C)C(=O)N(C)[C@]([H])(CC(C)C)C(=N[C@]([H])(C)C(=N[C@@]([H])(C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(CC(C)C)C(=O)N(C)[C@@]([H])(C(C)C)C(=O)N1C)O)O)O)O)O";
//				
//				Set<String> setLargeMoleculeSmiles = new HashSet<>(Arrays.asList(smiles1,smiles2,smiles3,smiles4,smiles5,smiles6));
//				for (String smiles : setLargeMoleculeSmiles) {
//					if (parentsmiles.equals(smiles)) {
//						logger.info("large molecule {} has metabolites:", smiles);
//						for (BasicMolecule m : entry.getValue()) {
//							logger.info("metabolite: {}", m.getSmiles());
//						}
//					}
//				}
	
			}

			logger.debug(NUMBER_OF_PARENT_SMILES_WRITTEN_TO_FILE, numberWritten);
		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_DATASET_PARENT_SMILES, e);
		}
		if (exit) {
			logger.info(EXITING);
			System.exit(0);
		}
	}



}
