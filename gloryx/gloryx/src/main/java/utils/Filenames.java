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

package main.java.utils;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to keep track of all filenames involved in development and evaluation of GLORYx.
 * Paths are hard-coded in the private class variables, except for the prefix for the filenames, which is hard-coded in MetabPredictLauncher.
 * The prefix has no effect on the user version.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Filenames {
	
	private static final Logger logger = LoggerFactory.getLogger(Filenames.class.getName());

	
	private static final String DRUGBANK_STRUCTURES = "/dataset/drugbank/version5.1.4/all-structures.sdf";
	private static final String METABOLITE_STRUCTURES = "/dataset/drugbank/version5.1.4/metabolite-structures.sdf";
	private static final String DRUGBANK_CSV = "/dataset/drugbank/version5.1.4/drugbank_metabolism_version5.1.4.csv";
	private static final String OUTPUT_SMILES = "/metaboliteprediction.smi";
	private static final String MANUAL_TEST_DB =  "/dataset/testdataset_comprehensive/comprehensivetestdataset.json"; //"/dataset/testdataset_comprehensive/old_test_set/comprehensivetestdataset_withbortezomib.json";
	private static final String METXBIODB = "/dataset/MetXBioDB/MetXBioDB-1-0.json"; 
	private static final String REFERENCEDATASET_SMILES = "dataset/reference/parent_smiles_without_test_dataset_overlap.txt";
	private static final String BIOTRANSFORMER_TESTSET = "dataset/biotransformertestset/dataset_publicationadditionalfile6.sdf";
	private static final String BIOTRANSFORMER_TESTSET_SMILES = "dataset/biotransformertestset/parent_smiles_all_biotransformertestset.txt"; // "dataset/biotransformertestset/parent_smiles_only46withcypmetabolites.txt";
	private static final String MANUAL_TEST_DB_SMILES = "dataset/newtestdataset/parent_smiles_testdataset.txt";
	private static final String JSON_DATASET_FILENAME = "referencedataset_tmp.json";
	
	private static final String OUTPUT_SD_FILE = "metabolite_predictions.sdf";
	private static final String OUTPUT_HTML_FILE = "metabolite_prediction_results";
	private static final String INDIVIDUAL_RESULTS_DIR = "individual_results/";

	
	private String prefix;
	
	private String drugbankStructuresFilename;
	private String metaboliteStructuresFilename;
	private String drugbankCsvFilename;
	private String outputSmilesPredictionFilename;
	private String manualTestDbFilename;
	private String metxbiodbFilename;
	private String refDatasetSmilesFilename;
	private String biotransformerTestsetFilename;
	private String biotransformerTestsetSmilesFilename;
	private String manualTestDbSmilesFilename;
	
	private String userInputFilename;
	private List<String> userInputSmiles;
	private String userOutputDir;
	private String outputSDFileUserVersion;
	private String outputHTMLFileUserVersion;
	private String individualResultsDir = INDIVIDUAL_RESULTS_DIR; 
	
	private String outputJsonDataset;


	//constructor
	public Filenames(String prefix) {
		
		this.prefix = prefix;
		
		this.drugbankStructuresFilename = prefix + DRUGBANK_STRUCTURES;
		this.metaboliteStructuresFilename = prefix + METABOLITE_STRUCTURES;
		this.drugbankCsvFilename = prefix + DRUGBANK_CSV;
		this.outputSmilesPredictionFilename = prefix + OUTPUT_SMILES;
		this.manualTestDbFilename = prefix + MANUAL_TEST_DB;
		this.metxbiodbFilename = prefix + METXBIODB;
		this.refDatasetSmilesFilename = prefix + REFERENCEDATASET_SMILES;
		this.biotransformerTestsetFilename = prefix + BIOTRANSFORMER_TESTSET;
		this.biotransformerTestsetSmilesFilename = prefix + BIOTRANSFORMER_TESTSET_SMILES;
		this.manualTestDbSmilesFilename = prefix + MANUAL_TEST_DB_SMILES;
		this.outputJsonDataset = prefix + JSON_DATASET_FILENAME;
	}
	
	
	public String getUserInputFilename() {
		return userInputFilename;
	}

	public void setUserInputFilename(String userInputFilename) {
		this.userInputFilename = userInputFilename;
	}
	
	public List<String> getUserInputSmiles() {
		return userInputSmiles;
	}

	public void setUserInputSmiles(List<String> userInputSmiles) {
		this.userInputSmiles = userInputSmiles;
	}

	public String getUserOutputDir() {
		return userOutputDir;
	}

	public void setUserOutputDir(String userOutputDir) {
		this.userOutputDir = userOutputDir;
		if (!this.userOutputDir.endsWith("/")) {
			this.userOutputDir = this.userOutputDir + "/";
		}
		createDir(this.userOutputDir);
		this.outputSDFileUserVersion = this.userOutputDir + OUTPUT_SD_FILE;
		this.outputHTMLFileUserVersion = this.userOutputDir + OUTPUT_HTML_FILE;
	}
	
	public String getIndividualResultsDir() {
		return this.individualResultsDir;
	}
	
	public String getIndividualOutputSDFilename() {
		return OUTPUT_SD_FILE;
	}
	
	public String getOutputSDFilename() {
		if (this.outputSDFileUserVersion == null) {
			logger.error("Output SD file has not been defined. Please define the output directory first.");
			System.exit(1);
		}
		return this.outputSDFileUserVersion;
	}
	
	public String getOutputSDFilenameOnly() {
		return Filenames.OUTPUT_SD_FILE;
	}
	
	public String getOutputHTMLFilenameOnly() {
		return Filenames.OUTPUT_HTML_FILE;
	}
	
	public String getOutputHTMLFilename() {
		if (this.outputHTMLFileUserVersion == null) {
			logger.error("Output HTML file has not been defined. Please define the output directory first.");
			System.exit(1);
		}
		return this.outputHTMLFileUserVersion;
	}
	
	
	
	// getters for evaluation
	
	public String getDrugbankStructuresFilename() {
		return this.drugbankStructuresFilename;
	}
	
	public String getMetaboliteStructuresFilename() {
		return this.metaboliteStructuresFilename;
	}
	
	public String getDrugbankCsvFilename() {
		return this.drugbankCsvFilename;
	}
	
	public String getOutputSmilesPredictionFilename() {
		return this.outputSmilesPredictionFilename;
	}
	
	public String getManualTestDbFilename() {
		return this.manualTestDbFilename;
	}
	
	public String getMetxbiodbFilename() {
		return metxbiodbFilename;
	}
	
	public String getReferenceDatasetSmilesFilename() {
		return refDatasetSmilesFilename;
	}
	
	public String getBiotransformerTestsetFilename() {
		return biotransformerTestsetFilename;
	}
	
	public String getBiotransformerTestsetSmilesFilename() {
		return biotransformerTestsetSmilesFilename;
	}
	
	public String getManualTestDbSmilesFilename() {
		return manualTestDbSmilesFilename;
	}
	
	public String getOutputJsonDatasetFilename() {
		return outputJsonDataset;
	}
	
	public String getPrefix() {
		return prefix;
	}


	
	public void createDir(String dirName) {
		File dir = new File(dirName);
	    if (!dir.exists()) {
	    		dir.mkdirs();
	    }
	}

}
