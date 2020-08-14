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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.Errors;
import main.java.utils.Filenames;
import main.java.utils.Prediction;
import main.java.utils.TestParameters;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.ParentMolecule;
import main.java.utils.molecule.PredictedMolecule;


/**
 * Used to write the predicted metabolites to a SMILES or SDF file.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class ResultsWriter {


	private static final String ERROR_WRITING_PREDICTED_MOLECULE_TO_OUTPUT_SD_FILE = "Error writing predicted molecule {} to output SD file.";
	private static final String ERROR_WRITING_SMILES_FILE_OF_PREDICTED_METABOLITES = "Error writing smiles file of predicted metabolites.";
	private static final String NO_PREDICTIONS_WERE_MADE = "No predictions were made and therefore no output smi file was written for this input molecule.";
	private static final String NO_PREDICTIONS_WERE_MADE_AND_THEREFORE_NO_OUTPUT_SD_FILE_WAS_WRITTEN_FOR_THIS_INPUT_MOLECULE = "No predictions were made and therefore no output SD file was written for this input molecule.";
	private static final String ERROR_WRITING_SD_FILE_OF_PREDICTED_METABOLITES = "Error writing SD file of predicted metabolites.";
	
	private static final String RESULT_FILE_HEADER_WITH_RANKING = "mol ID\tscore\tnumber of metabolites\tnumber of false positives\tbestrankofknownmetabolite\n";
	private static final String RESULT_FILE_HEADER_WITH_RANKING_CSV = "molID,numHeavyAtoms,MW,maxSomProb,avgTopThreeSomProb,medianSomProb,portionMetabolitesPredicted,numKnownMetabolites,numPredictedMetabolites,TP,FP,bestRankOfKnownMetabolite\n";
	private static final String ERROR_WRITING_FILE_OF_LIST = "Error writing file of list.";	
	private static final String COMMA = ",";
	private static final String TAB = "\t";
	private static final String ENDLINE = "\n";
	private static final String NA = "N/A";
	private static final String ERROR_READING_SDF_FILE = "Error reading SD file. Filename: {}. Exiting.";

	
	private static final Logger logger = LoggerFactory.getLogger(ResultsWriter.class.getName());
	
	private TestParameters testParameters;
	private Filenames filenames;
	
	public ResultsWriter(TestParameters testParameters, Filenames filenames) {
		this.testParameters = testParameters;
		this.filenames = filenames;
	}
	
	
	/**
	 * Converts PredictedMolecule to IAtomContainer, generate coordinates, and writes to SDF.
	 * 
	 * @param sdfWriter
	 * @param p Prediction object containing information about the parent molecule
	 * @param predictions the predicted metabolites
	 */
	private void writeAllToSdf(SDFWriter sdfWriter, Prediction p, List<PredictedMolecule> predictions) {
		
		// first write the parent molecule
		writeParent(sdfWriter, p);
		
		// now write the predicted metabolites
		int counter = 1;
		for (PredictedMolecule pred : predictions) {
			
			IAtomContainer predictedMetabolite = createIAtomContainerForSdfFromPrediction(pred, p, counter);
			writeMoleculeToSDF(sdfWriter, pred.getSmiles(), predictedMetabolite);
			counter ++;
		}
	}

	private void writeParent(SDFWriter sdfWriter, Prediction p) {
		
		if (testParameters.inputIsSdf()) {
			
			int molnum = Integer.parseInt(p.getParentMolecule().getId());
			
			// this is a time-consuming way to do this...
			try (IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(new File(
					filenames.getUserInputFilename())), DefaultChemObjectBuilder.getInstance(), 
					false)) { // this boolean indicates whether or not to continue reading the file if a 
							  // null or broken SD entry occurs - however, if set to true, any broken molecules 
							  // just disappear (hasNext not true for them) so the numbering of the input 
							  // molecules would be all messed up. Therefore this value is set to false for now. //TODO same as in DatasetLoader

				int counter = 1;
				while (reader.hasNext()) {
					
					if (counter == molnum) {
												
						IAtomContainer parentMolecule = reader.next();
						parentMolecule.setProperty("ID_GLORYx", molnum);
						
//						if (parentMolecule.getProperty(CDKConstants.TITLE) == null || parentMolecule.getProperty(CDKConstants.TITLE).equals("")) {
//							parentMolecule.setProperty(CDKConstants.TITLE, "Molecule " + molnum);
//						}
						
						writeMoleculeToSDF(sdfWriter, p.getParentMolecule().getSmiles(), parentMolecule);

						break;
					} else {
						reader.next();
					}
					counter ++;
				}
			} catch (IOException e) { 
				logger.error(ERROR_READING_SDF_FILE, filenames.getUserInputFilename());
			} 
			
		} else { // input is SMILES or else it's the evaluation version
			IAtomContainer parentMolecule = createIAtomContainerForSdfFromInputSmiles(p);
			writeMoleculeToSDF(sdfWriter, p.getParentMolecule().getSmiles(), parentMolecule);
		}

	}
	
	public void writePredictedMetabolitesToSdf(String outputFilename, Prediction pred) {
		
		logger.debug("writing to individual sdf {}", outputFilename);
		
		List<PredictedMolecule> predictions = pred.getRankedPredictedMetabolites();
	
		if (!predictions.isEmpty()) {
			Boolean validPrediction = checkPredictionsForInchi(predictions);
			if (validPrediction) {
				try (SDFWriter sdfWriter = new SDFWriter(new FileWriter(outputFilename))){

					writeAllToSdf(sdfWriter, pred, predictions);
					
				} catch (IOException e) {
					logger.error(ERROR_WRITING_SD_FILE_OF_PREDICTED_METABOLITES, e);
				}
			} else {
				logger.error(NO_PREDICTIONS_WERE_MADE_AND_THEREFORE_NO_OUTPUT_SD_FILE_WAS_WRITTEN_FOR_THIS_INPUT_MOLECULE);
			}
			
		} else {
			logger.error(NO_PREDICTIONS_WERE_MADE_AND_THEREFORE_NO_OUTPUT_SD_FILE_WAS_WRITTEN_FOR_THIS_INPUT_MOLECULE);
		}
	}
	
	public void writePredictedMetabolitesToSmi(String outputFilename, List<PredictedMolecule> predictions) {
		
		if (!predictions.isEmpty()) {
			Boolean validPrediction = checkPredictionsForInchi(predictions);
			if (validPrediction) {
				try (FileWriter fw = new FileWriter(outputFilename)){

					for (PredictedMolecule pred : predictions) {
						
						fw.write('"' + pred.getSmiles() + '"' + "\n"); //  + "\t" + pred.getTransformationName()
					}
					
				} catch (IOException e) {
					logger.error(ERROR_WRITING_SMILES_FILE_OF_PREDICTED_METABOLITES, e);
				}
			} else {
				logger.error(NO_PREDICTIONS_WERE_MADE);
			}
			
		} else {
			logger.error(NO_PREDICTIONS_WERE_MADE);
		}
	}

	private Boolean checkPredictionsForInchi(List<PredictedMolecule> predictions) {
		Boolean validPrediction = false;
		for (PredictedMolecule pred : predictions) {
			if (!pred.getInchi().isEmpty()) {
				validPrediction = true;
				break;
			}
		}
		return validPrediction;
	}
	
	public int writePredictionsToSdf(String outputFilename, Map<Integer, Prediction> allPredictedMolecules) {
		
		logger.debug("writing to sdf {}", outputFilename);
		
		int numWithPredictions = 0;
		try (SDFWriter sdfWriter = new SDFWriter(new FileWriter(outputFilename))){
			
			for (Prediction p : allPredictedMolecules.values()) {
				
				numWithPredictions = getAndWriteRankedPredictions(numWithPredictions, sdfWriter, p);
			}
			
		} catch (IOException e) {
			logger.error(ERROR_WRITING_SD_FILE_OF_PREDICTED_METABOLITES, e);
		}
		return numWithPredictions;
	}
	
	
	public int writeBatchedPredictionsToSdf(String outputFilename, Map<Integer, Prediction> allPredictedMolecules) throws IOException {
		// numbering of map starts at 1
		
		int batchSize = TestParameters.getBatchSize();

		int numWithPredictions = 0;
				
		int numBatches = (int) Math.ceil((double) allPredictedMolecules.size() / (double) batchSize); 
//		logger.debug("num batches: {}", numBatches);
		
		List<String> batchOutputFiles = new ArrayList<>();
		
		for (int batchNumber = 0; batchNumber < numBatches; batchNumber++) {
			
			String batchOutputFilename = outputFilename.substring(0, outputFilename.length()-4).concat("_").concat(Integer.toString(batchNumber)).concat(".sdf");
			batchOutputFiles.add(batchOutputFilename);
//			logger.debug("writing to sdf {}", batchOutputFilename); 
			
			SDFWriter sdfWriter = new SDFWriter(new FileWriter(batchOutputFilename));

			for (int i = 1; i <= batchSize; i++) { 
				
				int molnum = batchNumber * batchSize + i;
				if (molnum > allPredictedMolecules.size()) {
					logger.debug("ran out of molecules at {}", molnum);
					break;
				}
//				logger.debug("input molecule {} to batch {}", molnum, batchNumber);
				
				if (allPredictedMolecules.get(molnum) != null) {
					// this handles the case in which we're on the last batch and it's not full
					
					Prediction p = allPredictedMolecules.get(molnum);
					numWithPredictions = getAndWriteRankedPredictions(numWithPredictions, sdfWriter, p); 
					
				} else {
					logger.error("Null prediction but haven't yet run out of molecules. This should never happen!");
				}	
			}
			sdfWriter.close();
		}
		
		if (testParameters.isWebVersion()) {
			//  zip all files if possible, to original outputFilename but ending with .zip
			zipBatchedOutputSdfFiles(outputFilename, batchOutputFiles);
			
	        // delete individual files if entire zip was made successfully (rather than within the zipBatchedOutputSdfFiles method)
	        deleteBatchedOutputSdfFiles(batchOutputFiles);
		}

		
		return numWithPredictions;
	}

	private int getAndWriteRankedPredictions(int numWithPredictions, SDFWriter sdfWriter, Prediction p) {
		
		List<PredictedMolecule> predictions = p.getRankedPredictedMetabolites();
		
		if (predictions != null && !predictions.isEmpty() && checkPredictionsForInchi(predictions)) {
			numWithPredictions ++;
			
			writeAllToSdf(sdfWriter, p, predictions);
		} else {
			// TODO is this a sufficient validity check?
			if (p.getErrors().contains(Errors.COULD_NOT_PROCESS_INPUT_MOL)) {
				logger.error("Input molecule could not be processed and so cannot be written to output SDF.");
			} else {
				writeParent(sdfWriter, p);
			}
		}
		return numWithPredictions;
	}

	private void deleteBatchedOutputSdfFiles(List<String> batchOutputFiles) {
		for (String batchOutputFilename : batchOutputFiles) {
		    File f = new File(batchOutputFilename);
		    f.delete();
		}
	}

	private void zipBatchedOutputSdfFiles(String outputFilename, List<String> batchOutputFiles) throws FileNotFoundException, IOException {
		
		String outputZip = outputFilename.substring(0, outputFilename.length()-4).concat(".zip");
		
		FileOutputStream fos = new FileOutputStream(outputZip);
		try (ZipOutputStream zipOut = new ZipOutputStream(fos) ) {
			
		    for (String batchOutputFilename : batchOutputFiles) {
		        File fileToZip = new File(batchOutputFilename);
		        
		        try (FileInputStream fis = new FileInputStream(batchOutputFilename)) {
		            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		            zipOut.putNextEntry(zipEntry);
		            
		            byte[] bytes = new byte[1024];
		            int length;
		            while((length = fis.read(bytes)) >= 0) {
		                zipOut.write(bytes, 0, length);
		            }
		        }
		    }
		}
	}
	
	
	private void writeMoleculeToSDF(SDFWriter sdfWriter, String smiles, IAtomContainer molecule) {
		try {
			sdfWriter.write(molecule);
		} catch (CDKException e) {
			logger.error(ERROR_WRITING_PREDICTED_MOLECULE_TO_OUTPUT_SD_FILE, smiles);
		}
	}
	
	/**
	 * Generates 2D coordinates for predicted metabolite and adds all relevant properties to IAtomContainer 
	 * so that these properties can be written to the SD file.
	 * 
	 * @param metabolite
	 * @param prediction
	 * @return
	 */
	private IAtomContainer createIAtomContainerForSdfFromPrediction(PredictedMolecule metabolite, Prediction prediction, int metaboliteNumber) {
		
		IAtomContainer predictedMetabolite = MoleculeManipulator.generateMoleculeFromSmiles(metabolite.getSmiles());
		MoleculeManipulator.generate2dCoordinates(predictedMetabolite); // very important!
		predictedMetabolite.setProperty("Rank", metabolite.getRank());
		predictedMetabolite.setProperty("Priority score", metabolite.getPriorityScore());
		predictedMetabolite.setProperty("Reaction type", metabolite.getTransformationName());
		
		ParentMolecule parent = prediction.getParentMolecule();
		predictedMetabolite.setProperty("Parent ID", parent.getId());
		predictedMetabolite.setProperty(CDKConstants.TITLE, parent.getName() + "_metabolite " + Integer.toString(metaboliteNumber));
		predictedMetabolite.setProperty("Parent InChI", parent.getInchi());
		
		// use original input SMILES if the input was in SMILES format
		if (parent.getOriginalInputSmiles() != null) {
			predictedMetabolite.setProperty("Parent SMILES",  parent.getOriginalInputSmiles());
		} else {
			IAtomContainer parentMolecule = MoleculeManipulator.generateMoleculeFromSmiles(parent.getSmiles());
			String smiles = MoleculeManipulator.generateSmilesWithoutExplicitHydrogens(parentMolecule); // important for readability
			predictedMetabolite.setProperty("Parent SMILES",  smiles);
		}
		
		return predictedMetabolite;
	}
	
	/**
	 * Generates 2D coordinates for parent molecule and adds input SMILES as property to IAtomContainer
	 * so that the parent molecule can be written to the SD file.
	 * 
	 * @param prediction
	 * @return
	 */
	private IAtomContainer createIAtomContainerForSdfFromInputSmiles(Prediction prediction) {
		
		IAtomContainer parentMolecule = MoleculeManipulator.generateMoleculeFromSmiles(prediction.getParentMolecule().getSmiles());
		MoleculeManipulator.generate2dCoordinates(parentMolecule); // very important!
		
		// use original input SMILES if the input was in SMILES format // TODO redundant now
		if (prediction.getParentMolecule().getOriginalInputSmiles() != null) {
			parentMolecule.setProperty("SMILES", prediction.getParentMolecule().getOriginalInputSmiles());
		} else {
			String smiles = MoleculeManipulator.generateSmilesWithoutExplicitHydrogens(parentMolecule); // important for readability
			parentMolecule.setProperty("SMILES", smiles);
		}
		
		parentMolecule.setProperty(CDKConstants.TITLE, prediction.getParentMolecule().getName());

		return parentMolecule; 
	}
	
	
	
	// --- write evaluation results ---
	
	
	public static void writeResultsToTxtFile(String prefix, String filename, List<Result> results) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(prefix + filename))){
			
			writer.write(RESULT_FILE_HEADER_WITH_RANKING);
			for (Result result : results) {
				if (result.getBestRankOfKnownMetabolite() == 0) {
					writer.append(result.getMolId() + TAB + result.getRecoveryScore() + TAB + result.getNumberOfMetabolites() + TAB + result.getNumberOfFalsePositives() + TAB + NA + ENDLINE);
				} else {
					writer.append(result.getMolId() + TAB + result.getRecoveryScore() + TAB + result.getNumberOfMetabolites() + TAB + result.getNumberOfFalsePositives() + TAB + result.getBestRankOfKnownMetabolite() + ENDLINE);
				}
			}

		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_LIST);
		}
	}
	
	public static void writeResultsToCsvFile(String prefix, String filename, List<Result> results) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(prefix + filename))){
			
			writer.write(RESULT_FILE_HEADER_WITH_RANKING_CSV);
			// "molID,numHeavyAtoms,MW,maxSomProb,avgTopThreeSomProb,medianSomProb,portionMetabolitesPredicted,numKnownMetabolites,numPredictedMetabolites,TP,FP,bestRankOfKnownMetabolite\n"
			for (Result result : results) {

				writer.append(result.getMolId() + COMMA + 
						result.getHeavyAtomCount() + COMMA + 
						String.format("%.1f", result.getMolecularWeight()) + COMMA + 
						
						String.format("%.4f", result.getMaxSomProbability()) + COMMA + 
						String.format("%.4f", result.getAvgOfTop3SomProbabilities()) + COMMA + 
						String.format("%.4f", result.getMedianSomProbability()) + COMMA + 
						
						result.getRecoveryScore() + COMMA + 
						result.getNumberOfMetabolites() + COMMA + 
						result.getNumberOfPredictedMetabolites() + COMMA + 
						result.getNumberOfTruePositives() + COMMA + 
						result.getNumberOfFalsePositives() + COMMA + 
						result.getBestRankOfKnownMetabolite() + ENDLINE);
			}

		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_LIST);
		}
	}
	
	
	public static void writeScoreRocResultsToFile(String prefix, String filename, List<RankingRocResult> results) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(prefix + filename))){
			
			for (RankingRocResult result : results) {

				int correctlyPredicted = result.getCorrectlyPredicted() ? 1 : 0;
				writer.append(result.getScore().toString() + TAB + Integer.toString(correctlyPredicted) + ENDLINE);

			}			

		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_LIST);
		}
	}
	
	public static void writeRankRocResultsToFile(String prefix, String filename, List<RankingRocResult> results) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(prefix + filename))){
			
			for (RankingRocResult result : results) {

				int correctlyPredicted = result.getCorrectlyPredicted() ? 1 : 0;
				
				Double convertedRank = 1.00 / result.getRank();

				writer.append(convertedRank.toString() + TAB + Integer.toString(correctlyPredicted) + ENDLINE);

			}			

		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_LIST);
		}
	}
	
	
	public void writePredictionSmilesToFile(Filenames filenames, List<ToWriteSmilesFile> toWrite) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filenames.getOutputSmilesPredictionFilename()))){
			
			writer.write("parent name\tmetabolite smiles" + ENDLINE);
			
			for (ToWriteSmilesFile item : toWrite) {
				writer.append(item.getParentName() + "\t" + '"' + item.getMetaboliteSmiles() + '"' +  ENDLINE);
			}
		} catch (IOException e) {
			logger.error(ERROR_WRITING_FILE_OF_LIST);
		}
	}
	
	
}
