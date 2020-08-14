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

package main.java.depiction;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.Errors;
import main.java.utils.Filenames;
import main.java.utils.Phase;
import main.java.utils.Prediction;
import main.java.utils.TestParameters;
import main.java.utils.molecule.MoleculeManipulator;
import main.java.utils.molecule.PredictedMolecule;

import smartcyp.MoleculeKU;
import smartcyp.WriteResultsAsChemDoodleHTML;



/**
 * This class is used to write the HTML file for the results page of the web version of GLORYx.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class CreateResultsHTML extends WriteResultsAsChemDoodleHTML {

	private static final String END_DIV = "</div>\n";
	public static final String BLOCK_BACKGROUND_COLOR = "#F7FAF9";
	public static final String ACCORDION_BACKGROUND_COLOR_INACTIVE = "#E3E6E5"; 
	public static final String ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER = "#ccc";
	private static final String NEW_BLOCK_ROUNDED_EDGES_LIGHT_GRAY = "<div class=\"container-fluid mt-4\" style = \"padding: 1em 2em 1em 2em; margin = 14em; "
			+ "background-color: " + BLOCK_BACKGROUND_COLOR + "; border-radius: 20px; "
					+ "box-shadow: 0 2px 4px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12);\" >\n"; // #FEFCFF // TODO create separate css for this type of shadow
	PrintWriter outfile;
	PrintWriter cssfile;
	PrintWriter accordioncssfile;
	Map<Integer, Prediction> results;
	int numberOfInputMoleculesWithPredictions;
	int numberOfInputMoleculesTotal;
	String outputDir;
	String outputFilename;
	String sdfFilename;
	String timeStamp;
	Boolean predictionsCouldBeMade;
	String phase;
	Boolean useZip;

	private static final Logger logger = LoggerFactory.getLogger(CreateResultsHTML.class.getName());


	public CreateResultsHTML(Map<Integer, Prediction> allPredictedMolecules, int numInputMoleculesWithPrediction, int numInputMols, Filenames filenames, String timeStamp, TestParameters testParameters) { 

		super(timeStamp, new String[1], filenames.getUserOutputDir(), filenames.getOutputHTMLFilename());

		outputDir = filenames.getUserOutputDir();
		outputFilename = filenames.getOutputHTMLFilename();
		
		this.results = allPredictedMolecules;
		numberOfInputMoleculesWithPredictions = numInputMoleculesWithPrediction;
		numberOfInputMoleculesTotal = numInputMols;
		this.timeStamp = timeStamp;
		
		this.phase = testParameters.getInputPhase().toString(); // very important to use getInputPhase, not getPhase
		
		if (numInputMoleculesWithPrediction > 0) {
			predictionsCouldBeMade = true;
		} else {
			predictionsCouldBeMade = false;
		}
		
		if (allPredictedMolecules.size() > TestParameters.getBatchSize()) {
			this.useZip = true;
		} else {
			this.useZip = false;
		}

	}

	public void writeHTML() {

		if (outputFilename=="") {  // this should never occur
			outputFilename = "metabolite_prediction_results";
		}

		try {
			outfile = new PrintWriter(new BufferedWriter(new FileWriter(outputFilename + ".html")));
		} catch (IOException e) {
			logger.error("Could not create HTML outfile", e);
		}

		this.writeHead();

		this.writeDownloadAllResultsBlock();

		// TODO: if more than 25 or so input molecules, ask user if they want to see the individual results, and warn them that the page may be slower to load
		
		if (predictionsCouldBeMade && numberOfInputMoleculesTotal <= 25) {
			this.writeBlockForIndividualInputMolecules();
		} // TODO else if too many input molecules, write something saying there are too many to view the individual results? Currently just written on About page	
		
		
		writeJavascript();

		this.writeEndOfFile(); // closes body and html

		outfile.close();

	}


	private void writeHead() {

		outfile.println(
//				"<!DOCTYPE html>\n" + 
//				"<html>\n" + 
//				"<head>\n" + 
				
				
				//  --- for incorporation into webserver ---
				"{% load static %}" + 

				" {% block header %}\n" + 
				"{% include \"default_header.html\" %}\n" + 
				"{% endblock %}\n" +
				
				// for displaying molecules
				"<script type=\"text/javascript\" src=\"{% static \"metabol/ui/ChemDoodleWeb-libs.js\" %}\"></script>\n" + 
				"<script type=\"text/javascript\" src=\"{% static \"metabol/ui/ChemDoodleWeb.js\" %}\"></script>\n" + 
				"<link rel=\"stylesheet\" href=\"{% static \"metabol/ui/ChemDoodleWeb.css\" %}\" type=\"text/css\">"

				// --- ---
				
				
//				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css\">\n"
				);
		
//		writeStyle();
//		
//		outfile.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/general.css\">");
//		outfile.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/accordion.css\">");
		

		
//		outfile.println(
//				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" 
//				// TODO: may need to change this or can remove this part once this page is incorporated into web server
//				);
//		
//		outfile.println("</head>\n");
	}

	private void writeStyle() {
		
		File dir = new File(outputDir + "css/"); 
		dir.mkdir();
		
		try {
			cssfile = new PrintWriter(new BufferedWriter(new FileWriter(outputDir + "css/general.css")));
		} catch (IOException e) {
			logger.error("Could not create CSS file general.css", e);
		}

		cssfile.println("\n" +

				// define a custom button
				
				".btn-outline-secondary.download-button {\n" + 
				"	border-radius: 6px;\n" + 
				"	padding-left: 8px;\n" + 
				"	padding-right: 8px;\n" + 
				"	border: 2px solid " + ACCORDION_BACKGROUND_COLOR_INACTIVE + ";\n" + 
				"	background-color:" + ACCORDION_BACKGROUND_COLOR_INACTIVE + ";\n" + 
				"	color:#3d3d3d;\n" + 
				"}\n\n" +
				
				".btn-outline-secondary.download-button:hover {\n" +
				"	border: 2px solid " + ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
				"	background-color:" + ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
				"	color:#3d3d3d;\n" + 
				"}\n\n" +
				
				".btn-outline-secondary.download-button:active {\n" +
				"	border: 2px solid " + ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
				"	background-color:" + ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
				"	color:#3d3d3d;\n" + 
				"}\n\n" +
				
				".btn-outline-secondary.download-button:focus {\n" +
				"	outline:none;\nbox-shadow: none;\n" + 
				"}\n\n" +

				// define classes to use with <p> or <h> that add space afterwards
				".bottom-extra-space {\n" + 
				"   margin-bottom: 2em;\n" + 
				"}\n\n" +

				".bottom-slight-extra-space {\n" + 
				"   margin-bottom: 1em;\n" + 
				"}\n\n" +
				
				".top-extra-space {\n" + 
				"   margin-top: 2em;\n" + 
				"}\n\n" +
				
				".top-slight-extra-space {\n" + 
				"   margin-top: 1em;\n" + 
				"}\n\n" +
				
				
				// for back to top link
				".totop {\n" + 
//				"    float: right;\n" + 
//				"    float-offset: 10px;\n" +
				"    position: relative;\n" +
//				"    bottom: 2px;\n" + 
				"    right: -92%;\n" + 
				"    font-size: 14px;\n" + 
				"}\n\n" + 
				
//				".totop a {\n" + 
//				"    display: none;\n" + 
//				"}\n\n" + 
//				
				"a, a:visited {\n" + 
				"    color: #33739E;\n" + 
				"    text-decoration: none;\n" + 
				"    display: block;\n" + 
				"    margin: 10px 0;\n" + 
				"}\n\n" + 
				
				"a:hover {\n" + 
				"    text-decoration: none;\n" + 
				"}\n\n"
				
				// for load more button
//				".div.multiload {\n" +
//				"   display:none;\n" +
//				"}\n\n" +
//				
//				"#loadMore {\n" + 
//				"    padding: 10px;\n" + 
//				"    text-align: center;\n" + 
//				"    background-color: #33739E;\n" + 
//				"    color: #fff;\n" + 
//				"    border-width: 0 1px 1px 0;\n" + 
//				"    border-style: solid;\n" + 
//				"    border-color: #fff;\n" + 
//				"    box-shadow: 0 1px 1px #ccc;\n" + 
//				"    transition: all 600ms ease-in-out;\n" + 
//				"    -webkit-transition: all 600ms ease-in-out;\n" + 
//				"    -moz-transition: all 600ms ease-in-out;\n" + 
//				"    -o-transition: all 600ms ease-in-out;\n" + 
//				"}\n\n" + 
//				
//				"#loadMore:hover {\n" + 
//				"    background-color: #fff;\n" + 
//				"    color: #33739E;\n" + 
//				"}\n\n"
				
				
				
				);
		
		cssfile.close();

		
		try {
			accordioncssfile = new PrintWriter(new BufferedWriter(new FileWriter(outputDir + "css/accordion.css")));
		} catch (IOException e) {
			logger.error("Could not create CSS file accordion.css", e);
		}
		
		accordioncssfile.println(

				// for accordion
				"/*accordion */\n\n" +

				".accordion {\n" + 
				"  background-color: " + ACCORDION_BACKGROUND_COLOR_INACTIVE + ";\n" + // was #eee
				"  color: #3d3d3d;\n" + // was #444 - also tried #666666
//				"  font-weight:bold;\n" +
				"  cursor: pointer;\n" + 
				"  padding: 18px;\n" + 
				"  width: 100%;\n" + 
				"  border: none;\n" + // 1px solid #6c757d;\n" + // was none
				"  text-align: left;\n" + 
				"  outline: none;\n" + 
				"  font-size: 16px;\n" + 
				"  transition: 0.4s;\n" + 
				"}\n" + 
				"\n" + 

				".active, .accordion:hover {\n" + 
				"  background-color: " + ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
//				"  border: 1px solid #ccc;\n" + // was none
				"}\n" + 
				"\n" + 
				
				".accordion:focus {\n" +
				"  outline:none;\\nbox-shadow: none;\n" +
				"}\n" + 
				"\n" + 
				
				".accordion:after {\n" + 
				"  content: '\\002B';\n" + 
				"  color: #777;\n" + 
				"  font-weight: bold;\n" + 
				"  float: right;\n" + 
				"  margin-left: 5px;\n" + 
				"}\n" + 
				"\n" + 

				".active:after {\n" + 
				"  content: \"\\2212\";\n" + 
				"}\n" + 
				"\n" + 

				".panel {\n" + 
				"  padding: 0 18px;\n" + 
				"  background-color: white;\n" + 
				"  max-height: 0;\n" + 
				"  scroll-height: 800;\n" + 
				"  overflow: auto;\n" + // was auto
				"  transition: max-height 0.2s ease-out;\n" + 
				"}\n" 

				);
		accordioncssfile.close();
	}


	private void writeDownloadAllResultsBlock() {

		// button to download all results as sdf
		// if error message, print error why no predictions could be made

		// else
		outfile.println(
				"<body>\n" + //style=\600px;\" onscroll=\"scrollFunction()\"
		
				"    	  <div class=\"container-fluid\" style = \"padding: 2em 2em 0em; margin = 14em 2em 0em; background-color: #FFFFFF; \" >\n" + 
				// #FFCCCC is a nice light pink
				"    	     <h3 class=\"bottom-slight-extra-space\";>\n" + 
				"				Results\n" +
				"    	     </h3>\n" +
				
				"            <h5 class=\"bottom-slight-extra-space\";>\n" + 
				"				Results of metabolite structure prediction for " + phase + " metabolism\n" +
				"    	     </h5>\n" + 
				
				
				"    	  <div class=\"container-fluid\">\n" + 
				"    	   <div class=\"row row-no-gutters\">\n" +

				"    	     <p>\n" + 
				"				Prediction launched " + timeStamp + ".\n" +
				"				<br>These results can be accessed at a later point in time by visiting the current web address, unless you delete them.\n" +
				"            </p>\n" + 
				END_DIV + 
				END_DIV + 
				
				NEW_BLOCK_ROUNDED_EDGES_LIGHT_GRAY + 
				
				"    	  <div class=\"container-fluid\">\n" + 
				"    	   <div class=\"row row-no-gutters\">\n"
				);

		if (predictionsCouldBeMade) {
			
			outfile.println(
					"    	     <h5>\n" + 
					"				Metabolites could be predicted for " + numberOfInputMoleculesWithPredictions + " of " + numberOfInputMoleculesTotal + " input molecules\n" +
					"    	     </h5>\n" + 
					END_DIV + 
					END_DIV + 

					"    	   <div class=\"row row-no-gutters mt-2\">\n" +
					"    	    <div class=\"col-md-9\">\n" + // lg-8 col-md-7 col-sm-6 col-xs-4
					"    	     <p>\n"
					);
//			"    	      <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" href=\"" + sdfFilename + "\" rel=\"nofollow\">\n" +

			if (this.useZip) {
				outfile.println(
						"    	      <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" href=\"{{ model.uuid }}/download_zip\" rel=\"nofollow\">\n" +
						"                 Download predicted metabolites (.zip)\n"
						);
			} else {
				outfile.println(
						"    	      <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" href=\"{{ model.uuid }}/download_sdf\" rel=\"nofollow\">\n" +
						"                 Download predicted metabolites (.sdf)\n"
						);
			}

			outfile.println(
					"    	      </a>\n" + 
					"    	     </p>\n" + 
					END_DIV + 

					// delete button
					"<div class=\"col-md-3\">\n" +  // col-lg-2 col-md-3 col-sm-4 col-xs-6
					"   <div class=\"text-center\">" +
					"      <p>\n" + 
					"      <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" rel=\"nofollow\" onclick=\"alertFunction();\">\n" + 
					"       Delete results\n" + 
					"      </a>\n" + 
					"      </p>\n" + 
					END_DIV +
					END_DIV + 
					END_DIV


					);
		} else {  // No predictions could be made
			
			outfile.println(
				"    	     <h5>\n" + 
				"				No metabolites could be predicted for the input molecules.\n" +
				"    	     </h5>\n" +
				END_DIV + 
				
				//delete button even if no predictions could be made - will delete any input data
				" <div class=\"row row-no-gutters\">\n" + 
				"      <p>\n" + 
				"      <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" rel=\"nofollow\" onclick=\"alertFunction();\">\n" + 
				"       Delete results\n" + 
				"      </a>\n" + 
				"      </p>\n" + 
				END_DIV + 
				END_DIV + 

				"    	  <div class=\"container-fluid top-slight-extra-space\">\n" + 
				"    	   <div class=\"row row-no-gutters\">\n" +

				"    	     <h5>\n" + 
				"				View errors:\n" +
				"    	     </h5>\n" +
				END_DIV + 
				END_DIV
				);
				
			// Write accordion with errors for each input molecule
			for (Entry<Integer, Prediction> entry : results.entrySet()) {
				
				int molID = entry.getKey();
				String moleculeName = "Molecule " + molID;
				outfile.println(
					"    	   <div class=\"row row-no-gutters mt-2 multiload\">\n" +
					"    	    <div class=\"col-md-8\" style = \"background-color: " + BLOCK_BACKGROUND_COLOR + "; display:table-cell;\">\n" + 

					"<button class=\"accordion\">" + moleculeName + "</button>\n" + 
							"<div class=\"panel\" style=\"overflow: scroll;\">\n"
					);
				
				// write errors
				Set<Errors> errors = entry.getValue().getErrors();
				for (Errors error : errors) {
					
					outfile.println(
							" <p class=\"bottom-slight-extra-space top-slight-extra-space\" style = \"display:block\">\n" + 
							"<b>Error:</b> " + error.errorMessage() +
							" </p>\n"
							
							);
				}
				writeSmilesForInputMolecule(entry.getValue());
				
				// double check that no predictions were made
				List<PredictedMolecule> predictedMetabolites = entry.getValue().getRankedPredictedMetabolites();
				if (predictedMetabolites != null) {
					logger.error("Should not have predicted metabolites if there was a fatal error.");
				}

					
				outfile.println(
					END_DIV +
					END_DIV +
					END_DIV
					);
				
			}
		}
		
		outfile.println(END_DIV);

	}


	private void writeBlockForIndividualInputMolecules() { // block where input molecules are listed with buttons and info for each

		// needed if there are any predicted metabolites to show
		setUpChemDoodle();
		
		outfile.println(

				NEW_BLOCK_ROUNDED_EDGES_LIGHT_GRAY +

				"    	     <h5 class=\"bottom-slight-extra-space\";>\n" + 
				"				Results for Individual Input Molecules\n" +
				"    	     </h5>\n"

				);

		for (Entry<Integer, Prediction> entry : results.entrySet()) {
			
			int molID = entry.getKey();
			String moleculeName = "Molecule " + molID;
			
			String realIndividualSdfFilename = "individual_results/mol_" + molID + "/metabolite_predictions.sdf";
			String webIndividualSdfFilename = "/gloryx/result/{{ model.uuid }}/" + molID + "/download";

			String realFameOutputFilename = "individual_results/mol_" + molID + "/mol_" + molID + "_soms.html";
			String webFameOutputFilename = "/gloryx/result/{{ model.uuid }}/" + molID;
			String webSomPredictionFilename = "/gloryx/result/{{ model.uuid }}/" + molID + "/fame3";
			
//					"{{ model.uuid }}/metabolitepredictionresults/individual_results/mol_molID + "/mol_" + molID + "_soms.html";
			
			
			// check for errors / whether any prediction for this input molecule exists
			Boolean isError = false;
			if (!entry.getValue().getErrors().isEmpty()) {
				isError = true;
			}
			
//			for (PredictedMolecule mol : predictedMetabolites) {
//				Set<Errors> errors = entry.getValue().getErrors();
//				if (mol.getInchi().isEmpty() || !errors.isEmpty()) {
//					isError = true;
//					break;
//				}
//			}

			
			outfile.println(
					
					"    	   <div class=\"row row-no-gutters mt-2\">\n" +
					"    	    <div class=\"col-md-9\" style = \"background-color: " + BLOCK_BACKGROUND_COLOR + "; display:table-cell;\">\n" + 

					"<button class=\"accordion\">" + moleculeName + "</button>\n" + 
							"<div class=\"panel\" style=\"overflow: scroll;\">\n\n" // ui-if=\"article.isOpen\" 
					);
			
			
			if (isError && entry.getValue().getRankedPredictedMetabolites() == null) { 
				
				Set<Errors> errors = entry.getValue().getErrors();
				for (Errors error : errors) {
					
					outfile.println(
							"<p class=\"bottom-slight-extra-space top-slight-extra-space\" style = \"display:block\">\n" + 
							"<b>Error:</b> " + error.errorMessage() +
							"</p>\n"
							
							);
				}
				writeSmilesForInputMolecule(entry.getValue());

			} else if (entry.getValue().getRankedPredictedMetabolites() != null) { 

			// check if FAME 3 results file exists before trying to load it in the iframe - UPDATE - not doing this anymore
//			File f = new File(this.outputDir + realFameOutputFilename);
//			if (f.isFile() && f.canRead()) {
			

				outfile.println(
////						"<iframe src=\"about:blank\" data-src='" + fameOutputFilename + "' seamless height=100% width = 100% id='fame2_" + molID + "'></iframe>" // not good for actual website
//						"<iframe src=\"" + webFameOutputFilename + "\" seamless height=100% width = 100% style=\"min-height:350px;\" id='fame2_" + molID + "'></iframe>" 

				" <div class=\"row justify-content-md-center p-3\">\n" + 
				" <div class=\"card-deck-wrapper\">\n" + 
				"  <div class=\"card-deck justify-content-md-left\">\n\n" 
				);
				
				
				
				int n = 0;
				String parentSmiles = MoleculeManipulator.kekulizeMoleculeSmiles(entry.getValue().getParentMolecule().getSmiles());
				generateMoleculeDepictionOnCard(molID, n, parentSmiles, -1, (double) -1, true); // display parent molecule
				
				List<PredictedMolecule> predictedMetabolites = entry.getValue().getRankedPredictedMetabolites();
				
				// display all predicted metabolites
				for (PredictedMolecule mol : predictedMetabolites) { 
					n ++;
					String smiles = mol.getSmiles();
					int rank = mol.getRank();
					Double priorityScore = mol.getPriorityScore();
					generateMoleculeDepictionOnCard(molID, n, smiles, rank, priorityScore, false);
				}
				
				outfile.println(
						END_DIV + 
						END_DIV +
						END_DIV
						);
				
				outfile.println(
						"\n<button class=\"btn btn-outline-secondary btn-sm btn-block download-button bottom-slight-extra-space moreBtn\">" +
						"Show additional, less likely metabolites" +
						"</button>\n"
						);
				
			}
//			} else if (!isError) { // no longer creating fame 3 output file
//				logger.error("FAME 3 output file {} does not exist but no error was found! Writing dummy error to HTML results page.", realFameOutputFilename);
//				
//				outfile.println(
//						"<p class=\"bottom-slight-extra-space top-slight-extra-space\" style = \"display:block\">\n" + 
//						"An error occurred." +
//						"</p>\n"
//						);
//				writeSmilesForInputMolecule(entry.getValue());
//			}
			


			outfile.println(
				END_DIV +
				END_DIV
				);
			
			if (!isError) {
				
				File sdf = new File(this.outputDir + realIndividualSdfFilename);
//				if (sdf.isFile() && f.canRead()) { // need if showign FAME 3 results
				if (sdf.isFile()) {

					outfile.println(
							
							"<div class=\"col-md-3\" style = \"background-color: " + BLOCK_BACKGROUND_COLOR + "; display: flex; align-items: center; justify-content: center;\">\n" + 

//							"<a class=\"btn btn-outline-secondary btn-sm btn-block download-button \" style =\"display:block\" role=\"button\" href=\"" + webIndividualSdfFilename + "\" rel=\"nofollow\">Predicted metabolites (.sdf)\n" +  //class=\"btn btn-primary custom-btn\" 
//							"</a>\n" +  

// removed this button because the visualization doesn't make sense. Only the FAME 3 results from the last model used are shown.
//							"<a class=\"btn btn-outline-secondary btn-sm btn-block download-button \" style =\"display:block\" role=\"button\" href=\"" + webSomPredictionFilename + "\" rel=\"nofollow\" target=\"_blank\">View FAME 3 predictions\n" + 
//							"</a>" +
							
							END_DIV
								);
				}
			}
			
			outfile.println(
					END_DIV
					);


		}
		// end loop



		outfile.println(
				END_DIV +

//				"<a href=\"#\" id=\"loadMore\">Load More</a>" +
				
				// back to top button
				"<div class=\"text-right top-slight-extra-space\" style=\"font-size: 14px; margin-bottom:100px; padding-right: 1em;\">\n" + 
				" <a id=\"back-to-top\" href=\"#top\">Back to top</a>\n" + 
				"</div>" +

				END_DIV);


	}


	private void setUpChemDoodle() {
		outfile.println(
				" <script>\n" + 
						
				"  ChemDoodle.default_bondLength_2D = 50;\n" + 
				"  ChemDoodle.default_bonds_width_2D = 1.4;\n" + 
				"  ChemDoodle.default_bonds_saturationWidthAbs_2D = 2.6;\n" + 
				"  ChemDoodle.default_bonds_hashSpacing_2D = 2.5;\n" + 
				"  ChemDoodle.default_atoms_font_size_2D = 16;\n" + 
				"  ChemDoodle.default_atoms_font_families_2D = ['Helvetica', 'Arial', 'sans-serif'];\n" + 
//				"  ChemDoodle.default_atoms_displayTerminalCarbonLabels_2D = true;\n" + 
				"  ChemDoodle.default_atoms_useJMOLColors = true;\n" +
				
				
				" ExpandingCanvas = function(id, width, height){\n" + 
				"  this.mouseover = function(){\n" + 
				"   this.resize(400,400);\n" + 
				"   this.repaint();\n" + 
				"  }\n" + 
				"  this.mouseout = function(){\n" + 
				"   this.resize(200,200);\n" + 
				"  }\n" + 
				"  this.create(id, width, height);\n" + 
				" }\n" + 
				" ExpandingCanvas.prototype = new ChemDoodle._Canvas();\n" + 
				" </script>\n\n" 
				);
	}

	private void generateMoleculeDepictionOnCard(int molID, int n, String smiles, int rank, Double priorityScore, Boolean isInputMol) {
		String varName = "view_met_" + molID + "_" + n;
		
		IAtomContainer mol = MoleculeManipulator.generateMoleculeFromSmiles(smiles);
		
//		MoleculeKU moleculeKU = null;
//		try {
//			moleculeKU = new MoleculeKU(mol, null);
//		} catch (CloneNotSupportedException e) {
//			logger.error("Cannot convert molecule to MoleculeKU");
//			createCard(isInputMol);
//			outfile.print("Molecule could not be rendered." + END_DIV + "\n\n");
//			return;
//		}
		String[] moleculeLines = generateMoleculeCoordinatesForChemDoodle(mol);

//		"<!-- box-shadow: 0 2px 4px -4px rgba(0,0,0,0.16),0 2px 10px -4px rgba(0,0,0,0.12); -->\n" + 

		createCard(n, rank);
		
		outfile.println(
			" <script>\n" + 
			"  var " + varName +" = new ExpandingCanvas('" + varName + "', 200, 200);\n" 
		    );
		outfile.print("var " + varName + "_molfile = '");
		    for(int i=0; i<moleculeLines.length; i++){
		        outfile.print(moleculeLines[i]);
		        outfile.print("\\n");
		    }
		outfile.print("'; \n");
		outfile.print(
			"  var " + varName + "_mol = ChemDoodle.readMOL(" + varName + "_molfile);\n" + 
			"  new ChemDoodle.informatics.HydrogenDeducer().removeHydrogens(" + varName + "_mol, false);" + // otherwise all hydrogens appear to be carbons
			"  " + varName + ".loadMolecule(" + varName + "_mol);\n"
			);
		
		if (isInputMol) { // print "Input molecule" on image
			outfile.print(
					"  " + varName + ".drawChildExtras = function(ctx){\n" + 
					"    ctx.font = \"14px Arial\";\n" + 
					"    ctx.fillStyle = \"black\";\n" + 
					"    ctx.textAlign = \"center\";\n" + 
					"    ctx.fillText(\"Input molecule\", " + varName + ".width/2, 20);\n" + 
					"  }\n" + 
					"  " + varName + ".repaint();\n" +
					"  " + varName + ".resize(200,200);\n"
					);
			
		} else { // print rank on image
			outfile.print(
					"  " + varName + ".drawChildExtras = function(ctx){\n" + 
					"    ctx.font = \"14px Arial\";\n" + 
					"    ctx.fillStyle = \"black\";\n" + 
					"    ctx.textAlign = \"left\";\n" + 
					"    ctx.fillText(\"Rank " + rank + "\", 10, " + varName + ".height-10);\n" + 
					"    ctx.textAlign = \"right\";\n" + 
					"    ctx.fillText(\"(Score: " + String.format("%.2f", priorityScore) + ")\", " + varName + ".width-10, " + varName + ".height-10);\n" + 
					"  }\n" + 
					"  " + varName + ".repaint();\n" +
					"  " + varName + ".resize(200,200);\n"
					);
		}
		
		outfile.print(
			"  </script>\n" + 
			END_DIV + "\n\n" 
				);
	}

	private void createCard(int n, int rank) { //Boolean isInputMol, 
		
		if (n <=5 || rank <= 3) { // create card that will always be shown
			outfile.println(
					"<div class=\"card bg-white text-dark text-center m-1 expanding-image rounded-0\" "
					+ "style=\"min-width: 200px; max-width: 200px; height: 200px;  border:none;\">\n" 
					);
		} else {  // create card that can be hidden
			outfile.println(
					"<div class=\"card bg-white text-dark text-center m-1 expanding-image rounded-0 more more-hidden\" "
					+ "style=\"min-width: 200px; max-width: 200px; height: 200px;  border:none;\">\n" 
					);
		}

		

//		if (isInputMol) {
//			outfile.println(
//				"<div class=\"card bg-white text-dark text-center m-1 expanding-image rounded-0\" style=\"min-width: 200px; max-width: 200px; height: 200px; border:none;\">\n" 
//				);  // outline: thin solid #007bff; 
//		} else {
//			outfile.println(
//				"<div class=\"card bg-white text-dark text-center m-1 expanding-image rounded-0\" style=\"min-width: 200px; max-width: 200px; height: 200px;  border:none;\">\n" 
//				);
//		}
	}

	private String[] generateMoleculeCoordinatesForChemDoodle(IAtomContainer mol) {

		// this code is from DepictorSMARTCyp in FAME 2

		//preconstruct coordinates and stuff for each molecule
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MDLV2000Writer writer = new MDLV2000Writer(baos);
		//generate 2D coordinates
		mol = MoleculeManipulator.generate2dCoordinates(mol);

		//force 2D coordinates even if 3D exists
		Properties customSettings = new Properties();
		customSettings.setProperty(
		        "ForceWriteAs2DCoordinates", "true"
		);
		customSettings.setProperty(
		        "WriteAromaticBondTypes", "true"
		);

		PropertiesListener listener = new PropertiesListener(customSettings);
		writer.addChemObjectIOListener(listener);
		//end force 2D coordinates
		try {
		    writer.write(mol);
		    writer.close();
		} catch (CDKException e) {
				logger.error("Error writing molecule to SD format for depiction", e);
		} catch (IOException e) {
				logger.error("Error closing SD format for depiction",e);
		}
		String moleculeString = baos.toString();
		return moleculeString.split("\\r?\\n");   //now split molecule into multiple lines to enable explicit printout of \n
	}

	private void writeSmilesForInputMolecule(Prediction p) {
		outfile.println(									
				"<p class=\"bottom-slight-extra-space top-slight-extra-space\" style = \"display:block\">\n" + 
				"<b>SMILES for this input molecule:</b> " + p.getParentMolecule().getSmiles() +
				"</p>\n"
				);
	}

	private void writeEndOfFile() {

		outfile.println(
				END_DIV + // end large container containing heading "Results"
//				
//				"<div class=\"container\" style=\"background-color: whitesmoke; margin-top: 4em;\">\n" +			
//				"test - this is the background color of the header and footer for ACM web server" + 
//				
//				END_DIV +
				
				// standard footer TODO:
				"{% block footer %}\n" + 
				"{% include \"gloryx/footer.html\" %}\n" + 
				"{% endblock %}" +
				
						"</body>\n" + 
						"</html>"
				);

	}

	private void writeJavascript() {
		outfile.println(

				
//				"<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>\n\n" + 

				"<script>\n" + 
				
				// javascript for accordion panel
				"var acc = document.getElementsByClassName(\"accordion\");\n" + 
				"var i;\n" + 
				"for (i = 0; i < acc.length; i++) {\n" + 
				" acc[i].addEventListener(\"click\", function() {\n" + 
				"  var panel = this.nextElementSibling;\n" + 
				"  if (this.classList.contains('active')) {\n" + 
				"   this.classList.remove('active');\n" + 
				"   panel.style.maxHeight = null;\n" + 
				"   return;\n" + 
				"  }\n" +
				"  this.classList.add('active');\n" +
				"  panel.style.maxHeight = panel.scrollHeight + 15 + \"px\";\n" + 
				"  $('html,body').animate({\n" + // scroll so newly opened accordion is at the top of page (to best display FAME 2 predictions in iframe)
				"    scrollTop: $(this).offset().top\n" + 
				"  }, 600);\n" + 

				// load iframe only when panel is open - this was slowing things down and also not working properly, don't know why
//				"  var $iframe=this.target.children('iframe');\n" + //this.find('iframe');\n" + 
//				"  if ($iframe.data('src')){ // only do it once per iframe\n" + 
//				"    setIframeHeight(document.getElementById($iframe.id));\n" + // ?
//				"    $iframe.attr('src', $iframe.data('src'));" +
//				"    $iframe.data('src', false);\n" + 
//				"  }\n" + 
//				"}\n" +
				
				" });\n" + 
				"}\n" + 
				

//				//TODO could resize based on number of predicted metabolites instead - but 
//				// this would be hard b/c depends on size of window how many are displayed on one line
//				
//				// javascript for automatically sizing iframe
//				"function setIframeHeight(iframe) {\n" + 
//				"	if (iframe) {\n" + 
//				"		var iframeWin = iframe.contentWindow || iframe.contentDocument.parentWindow;\n" + 
//				"		if (iframeWin.document.body) {\n" + 
//				"			iframe.height = (iframeWin.document.documentElement.scrollHeight || iframeWin.document.body.scrollHeight) + 5;\n" + 
//				"		}\n" + 
//				"	}\n" + 
//				"};\n" +
//				
//				"window.onload = function () {\n" + 
//				" var frames = document.getElementsByTagName(\"iframe\");" +
//				" for (i = 0; i < frames.length; ++i) {\n" + 
// 				"  setIframeHeight(document.getElementById(frames[i].id));\n" + 
//				" };\n" +
//				"};\n" +
				
				
				// for loading iframe content
//				"$(function(){ " +
//				"$(\"#includedContent\").load(\"b.html\"); " +
//				"});" +

//				
//				// javascript for load more button
//				"$(function () {\n" + 
//				"    $(\"div\").slice(0, 2).show();\n" +  // change slice for how many to show at once
//				"    $(\"#loadMore\").on('click', function (e) {\n" + 
//				"        e.preventDefault();\n" + 
//				"        $(\"div:hidden\").slice(0, 1).slideDown();\n" + // change slice
//				"        if ($(\"div:hidden\").length == 0) {\n" + 
//				"            $(\"#load\").fadeOut('slow');\n" + 
//				"        }\n" + 
//				"        $('html,body').animate({\n" + 
//				"            scrollTop: $(this).offset().top\n" + 
//				"        }, 1500);\n" + 
//				"    });\n" + 
//				"});\n\n" +
				
				
				// javascript for scrolling to top
				
				// When the user scrolls down 20px from the top of the document, show the button
//				"window.onscroll = function() {scrollFunction()};\n\n" + 
//
//				"function scrollFunction() {\n" + 
//				"  if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {\n" + 
//				"    document.getElementById(\"back-to-top\").style.display = \"block\";\n" + 
//				"  } else {\n" + 
//				"    document.getElementById(\"back-to-top\").style.display = \"none\";\n" + 
//				"  }\n" + 
//				"}" +
//				
				
				"$('a[href=\"#top\"]').click(function () {\n" + 
				" $('body,html').animate({\n" + 
				"  scrollTop: 0\n" + 
				" }, 600);\n" + 
				" return false;\n" + 
				"});\n" + 
				
//				// for some reason, this isn't working! I tried setting a fixed height to <body> but that didn't fix the 
//				// problem. So instead I've made the "back to top" link only available at the very bottom of the page.
//				"$(window).scroll(function () {\n" +  // was window
//				"    if ($(this).scrollTop() > 50) {\n" + 
//				"        $('.totop a').fadeIn();\n" + 
//				"    } else {\n" + 
//				"        $('.totop a').fadeOut();\n" + 
//				"    }\n" + 
//				"});" +
			

				"var mores = document.getElementsByClassName(\"moreBtn\");\n" + 
				"var j;\n" + 
				"for (j = 0; j < mores.length; j++) {\n" + 
				" mores[j].addEventListener(\"click\", function() {\n" + 
				"  var elements = this.previousElementSibling.firstElementChild.firstElementChild.getElementsByClassName(\"more\");\n" + 
				//                         console.log(elements);\n" + 
				"  if (this.innerHTML != \"Show additional, less likely metabolites\") {\n" + 
				"   this.innerHTML = \"Show additional, less likely metabolites\";\n" + 
				"   for(var i=0; i<elements.length; i++) {\n" + 
				"    var moreMetabolites = elements[i];\n" + 
				"    moreMetabolites.classList.toggle(\"more-hidden\");\n" + 
				"   }\n" + 
				"  } else {\n" + 
				"   this.innerHTML = \"Show fewer metabolites\";\n" + 
				"   \n" + 
				"   for(var i=0; i<elements.length; i++) {\n" + 
				"    var moreMetabolites = elements[i];\n" + 
				"    moreMetabolites.classList.toggle(\"more-hidden\");\n" + 
				"   }\n" + 
				"  }\n" + 
				" });\n" + 
				"}\n" +
				
				
				"</script>\n" 
				);
	}
	
	private void writeCssForPrettyButtons() {
		
		cssfile.println(

		".button-pretty-greyish {\n" + 
		"	-moz-box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	-webkit-box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #f2f8fc), color-stop(1, #d9e3eb));\n" + 
		"	background:-moz-linear-gradient(top, #f2f8fc 5%, #d9e3eb 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #f2f8fc 5%, #d9e3eb 100%);\n" + 
		"	background:-o-linear-gradient(top, #f2f8fc 5%, #d9e3eb 100%);\n" + 
		"	background:-ms-linear-gradient(top, #f2f8fc 5%, #d9e3eb 100%);\n" + 
		"	background:linear-gradient(to bottom, #f2f8fc 5%, #d9e3eb 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#f2f8fc', endColorstr='#d9e3eb',GradientType=0);\n" + 
		"	background-color:#f2f8fc;\n" + 
		"	-moz-border-radius:6px;\n" + 
		"	-webkit-border-radius:6px;\n" + 
		"	border-radius:6px;\n" + 
		"	border:1px solid #ebebeb;\n" + 
		
		"	display:inline-block;\n" + 
		"	cursor:pointer;\n" + 
		"	color:" + "#515151" + ";\n" + // was #666666
		"	font-family:Arial;\n" + 
		"	font-size:14px;\n" + 
		"	font-weight:bold;\n" + 
		"	padding:8px 20px;\n" + 
		"	text-decoration:none;\n" + 
		"   text-align:center; " +
		"   vertical-align:middle;\n" + 
		"}\n" + 
		".button-pretty-greyish:hover {\n" + 
		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #d9e3eb), color-stop(1, #f2f8fc));\n" + 
		"	background:-moz-linear-gradient(top, #d9e3eb 5%, #f2f8fc 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #d9e3eb 5%, #f2f8fc 100%);\n" + 
		"	background:-o-linear-gradient(top, #d9e3eb 5%, #f2f8fc 100%);\n" + 
		"	background:-ms-linear-gradient(top, #d9e3eb 5%, #f2f8fc 100%);\n" + 
		"	background:linear-gradient(to bottom, #d9e3eb 5%, #f2f8fc 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#d9e3eb', endColorstr='#f2f8fc',GradientType=0);\n" + 
		"	background-color:#d9e3eb;\n" + 
		
		"	text-decoration:none;\n" + 
		"   color:#0069d9;\n" +
		"}\n" + 
		".button-pretty-greyish:active {\n" + 
		"	position:relative;\n" + 
		"	top:1px;\n" + 
		"}\n" + 
		"" +
		
		".button-pretty-blue {\n" + 
//		"	-moz-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	-webkit-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #007bff), color-stop(1, #0069d9));\n" + 
//		"	background:-moz-linear-gradient(top, #007bff 5%, #0069d9 100%);\n" + 
//		"	background:-webkit-linear-gradient(top, #007bff 5%, #0069d9 100%);\n" + 
//		"	background:-o-linear-gradient(top, #007bff 5%, #0069d9 100%);\n" + 
//		"	background:-ms-linear-gradient(top, #007bff 5%, #0069d9 100%);\n" + 
//		"	background:linear-gradient(to bottom, #007bff 5%, #0069d9 100%);\n" + 
//		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#007bff', endColorstr='#0069d9',GradientType=0);\n" + 
//		"	background-color:#007bff;\n" + 
//		"	-moz-border-radius:6px;\n" + 
//		"	-webkit-border-radius:6px;\n" + 
//		"	border-radius:6px;\n" + 
//		"	border:1px solid #0063cc;\n" + 

//		"  -moz-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	-webkit-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
//		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #7cb2eb), color-stop(1, #296aab));\n" + 
//		"	background:-moz-linear-gradient(top, #7cb2eb 5%, #296aab 100%);\n" + 
//		"	background:-webkit-linear-gradient(top, #7cb2eb 5%, #296aab 100%);\n" + 
//		"	background:-o-linear-gradient(top, #7cb2eb 5%, #296aab 100%);\n" + 
//		"	background:-ms-linear-gradient(top, #7cb2eb 5%, #296aab 100%);\n" + 
//		"	background:linear-gradient(to bottom, #7cb2eb 5%, #296aab 100%);\n" + 
//		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#7cb2eb', endColorstr='#296aab',GradientType=0);\n" + 
//		"	background-color:#7cb2eb;\n" + 
//		"	-moz-border-radius:6px;\n" + 
//		"	-webkit-border-radius:6px;\n" + 
//		"	border-radius:6px;\n" + 
//		"	border:1px solid #5388bd;" +
		"-moz-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
		"	-webkit-box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
		"	box-shadow:inset 0px 0px 0px -1px #bbdaf7;\n" + 
		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #daeafa), color-stop(1, #7293b3));\n" + 
		"	background:-moz-linear-gradient(top, #daeafa 5%, #7293b3 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #daeafa 5%, #7293b3 100%);\n" + 
		"	background:-o-linear-gradient(top, #daeafa 5%, #7293b3 100%);\n" + 
		"	background:-ms-linear-gradient(top, #daeafa 5%, #7293b3 100%);\n" + 
		"	background:linear-gradient(to bottom, #daeafa 5%, #7293b3 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#daeafa', endColorstr='#7293b3',GradientType=0);\n" + 
		"	background-color:#daeafa;\n" + 
		"	-moz-border-radius:6px;\n" + 
		"	-webkit-border-radius:6px;\n" + 
		"	border-radius:6px;\n" + 
		"	border:1px solid #bdd2e6;" +
		"	display:inline-block;\n" + 
		"	cursor:pointer;\n" + 
		"	color:#ffffff;\n" + 
//		"	color:#666666;\n" + 
		"	font-family:Arial;\n" + 
		"	font-size:14px;\n" + 
		"	font-weight:400;\n" + // took 400 from acm page
		"	padding:6px 24px;\n" + 
		"	text-decoration:none;\n" + 
		"}\n" + 
		".button-pretty-blue:hover {\n" + 
//		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #0069d9), color-stop(1, #007bff));\n" + 
//		"	background:-moz-linear-gradient(top, #0069d9 5%, #007bff 100%);\n" + 
//		"	background:-webkit-linear-gradient(top, #0069d9 5%, #007bff 100%);\n" + 
//		"	background:-o-linear-gradient(top, #0069d9 5%, #007bff 100%);\n" + 
//		"	background:-ms-linear-gradient(top, #0069d9 5%, #007bff 100%);\n" + 
//		"	background:linear-gradient(to bottom, #0069d9 5%, #007bff 100%);\n" + 
//		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#0069d9', endColorstr='#007bff',GradientType=0);\n" + 
//		"	background-color:#0069d9;\n" +

//		"   background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #296aab), color-stop(1, #7cb2eb));\n" + 
//		"	background:-moz-linear-gradient(top, #296aab 5%, #7cb2eb 100%);\n" + 
//		"	background:-webkit-linear-gradient(top, #296aab 5%, #7cb2eb 100%);\n" + 
//		"	background:-o-linear-gradient(top, #296aab 5%, #7cb2eb 100%);\n" + 
//		"	background:-ms-linear-gradient(top, #296aab 5%, #7cb2eb 100%);\n" + 
//		"	background:linear-gradient(to bottom, #296aab 5%, #7cb2eb 100%);\n" + 
//		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#296aab', endColorstr='#7cb2eb',GradientType=0);\n" + 
//		"	background-color:#296aab;" +
		
		"background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #7293b3), color-stop(1, #daeafa));\n" + 
		"	background:-moz-linear-gradient(top, #7293b3 5%, #daeafa 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #7293b3 5%, #daeafa 100%);\n" + 
		"	background:-o-linear-gradient(top, #7293b3 5%, #daeafa 100%);\n" + 
		"	background:-ms-linear-gradient(top, #7293b3 5%, #daeafa 100%);\n" + 
		"	background:linear-gradient(to bottom, #7293b3 5%, #daeafa 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#7293b3', endColorstr='#daeafa',GradientType=0);\n" + 
		"	background-color:#7293b3;" +
		"	color:#ffffff;\n" + 
//		"	color:#666666;\n" + 
		"	text-decoration:none;\n" + 
		"}\n" + 
		".button-pretty-blue:active {\n" + 
		"	position:relative;\n" + 
		"	top:1px;\n" + 
		"}\n" + 
		"\n" +
		
		// grey button
		".pretty-button {\n" + 
		"	-moz-box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	-webkit-box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	box-shadow:inset 0px 0px 0px 0px #ffffff;\n" + 
		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #ebebeb), color-stop(1, #d1cdd1));\n" + 
		"	background:-moz-linear-gradient(top, #ebebeb 5%, #d1cdd1 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #ebebeb 5%, #d1cdd1 100%);\n" + 
		"	background:-o-linear-gradient(top, #ebebeb 5%, #d1cdd1 100%);\n" + 
		"	background:-ms-linear-gradient(top, #ebebeb 5%, #d1cdd1 100%);\n" + 
		"	background:linear-gradient(to bottom, #ebebeb 5%, #d1cdd1 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ebebeb', endColorstr='#d1cdd1',GradientType=0);\n" + 
		"	background-color:#ebebeb;\n" + 
		"	-moz-border-radius:6px;\n" + 
		"	-webkit-border-radius:6px;\n" + 
		"	border-radius:6px;\n" + 
		"	border:1px solid #dcdcdc;\n" + 
		"	display:inline-block;" + 
		"	cursor:pointer;\n" + 
		"	color:#666666;\n" + 
//		"	font-family:Arial;\n" + 
		"	font-size:14px;\n" + 
		"	font-weight:bold;\n" + 
		"	padding:8px 20px;\n" + 
		"	text-decoration:none; " +
		"   text-align:center; " +
		"   vertical-align:middle;\n" + 
//		"	text-shadow:0px 1px 0px #ffffff;\n" + 
		"}\n" + 
		".pretty-button:hover {\n" + 
		"	background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #d1cdd1), color-stop(1, #ebebeb));\n" + 
		"	background:-moz-linear-gradient(top, #d1cdd1 5%, #ebebeb 100%);\n" + 
		"	background:-webkit-linear-gradient(top, #d1cdd1 5%, #ebebeb 100%);\n" + 
		"	background:-o-linear-gradient(top, #d1cdd1 5%, #ebebeb 100%);\n" + 
		"	background:-ms-linear-gradient(top, #d1cdd1 5%, #ebebeb 100%);\n" + 
		"	background:linear-gradient(to bottom, #d1cdd1 5%, #ebebeb 100%);\n" + 
		"	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#d1cdd1', endColorstr='#ebebeb',GradientType=0);\n" + 
		"	background-color:#d1cdd1;\n" +
		"	color:#777777;\n" + 
		"	text-decoration:none; " +
		"}\n" + 
		".pretty-button:active {\n" + 
		"	position:relative;\n" + 
		"	top:1px;\n" + 
		"}\n" + 
		"" + 
		"\n"
		);
	}



}
