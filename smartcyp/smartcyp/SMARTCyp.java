/* 
 * Copyright (C) 2010-2013  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
 * 
 * Contact: smartcyp@farma.ku.dk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package smartcyp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.lang.System;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class SMARTCyp {


	public static void main(String[] arguments) throws Exception{

		SMARTCyp SMARTCypMain = new SMARTCyp();
		
		long ms = System.currentTimeMillis();

		// Check that the arguments (molecule files) have been given
		if (arguments.length < 1){
			System.out.println("Wrong number of arguments!" + '\n' + "Usage: SMARTCyp <One or more moleculeFiles>");
			System.exit(0);			
		}
		
		//check for input flags and copy input files to filenames array
		boolean smilesinput = false;
		int nohtml = 0;
		int dirwanted = 0;
		int filewanted = 0;
		int nocsv = 0;
		int printall = 0;
		int png = 0; //png means use old html and image output
		int filter = 0;
		int noempcorr = 0;
		int smilesstringinput = 0;
		double filtercutoffvalue = 0;
		String filtercutoff = "";
		String outputdir = "";
		String outputfile = "";
	    for(int i=0; i < arguments.length; i++){
	    	if (arguments[i].equals("-nohtml")){
	        	nohtml = 1;
	        }
	    	if (arguments[i].equals("-nocsv")){
	        	nocsv = 1;
	        }
	    	if (arguments[i].equals("-printall")){
	        	printall = 1;
	        }
	    	if (arguments[i].equals("-outputdir")){
	        	outputdir = arguments[i+1];
	        	dirwanted = 1;
	        }
	    	if (arguments[i].equals("-outputfile")){
	        	outputfile = arguments[i+1];
	        	filewanted = 1;
	        }
	    	if (arguments[i].equals("-png")){
	        	png = 1;
	        }
	    	if (arguments[i].equals("-filter")){
	    		filtercutoff = arguments[i+1];
	        	filter = 1;
	        }
	    	if (arguments[i].equals("-noempcorr")){
	        	noempcorr = 1;
	        }
	    	if (arguments[i].equals("-smiles")){
	    		smilesstringinput = 1;
	    		//make a file out of arguments[i+1] and modify arguments[i+1] to end with .smiles
	    		String smilesfilename = "smartcypxyz.smiles";
	    		String smilesstring = arguments[i+1]; 
	    		arguments[i+1] = smilesfilename;
	    		PrintWriter smilesfile;
	    		try {
	    			smilesfile = new PrintWriter(new BufferedWriter(new FileWriter("smartcypxyz.smiles")));
	    			smilesfile.println(smilesstring);
	    			smilesfile.close();
	    		} catch (IOException e) {
	    			System.out.println("Could not create temporary smiles file");
	    			e.printStackTrace();
	    		}
	    	}
	    }
	    String[] filenames;
	    if (nohtml == 1 || dirwanted == 1 || filewanted == 1 || nocsv == 1 || printall == 1 || png == 1 || filter == 1 || smilesstringinput == 1 || noempcorr == 1 ){
	        ArrayList<String> tmplist = new ArrayList<String>();
	        Collections.addAll(tmplist, arguments);
	        if (dirwanted == 1){
	        	//a specific output directory has been requested
	        	tmplist.remove(outputdir);
	        	tmplist.remove("-outputdir");
	        	File dir = new File(outputdir);
	        	//check if the directory exists, otherwise create it
	        	if (!dir.exists()){
	        		dir.mkdir();
	        	}
	        	outputdir = outputdir + File.separator;
	        }
	        if (filewanted == 1){
	        	//a specific filename base has been requested
	        	tmplist.remove(outputfile);
	        	tmplist.remove("-outputfile");
	        }
	        if (nohtml == 1) tmplist.remove("-nohtml");
	        if (nocsv == 1) tmplist.remove("-nocsv");
	        if (printall == 1) tmplist.remove("-printall");
	        if (png == 1) tmplist.remove("-png");
	        if (smilesstringinput == 1) tmplist.remove("-smiles");
	        if (noempcorr == 1) tmplist.remove("-noempcorr");
	        if (filter == 1){
	        	//filtering with a cutoff number has been requested
	        	tmplist.remove(filtercutoff);
	        	tmplist.remove("-filter");
	        	filtercutoffvalue = Double.parseDouble(filtercutoff);
	        }
	        filenames = (String[])tmplist.toArray(new String[0]);
	    }
	    else {
	    	filenames = arguments;
	    }
	    //check if there are any smiles-files in the input, for use with filtering output
	    for (int filecount = 0; filecount < filenames.length; filecount++){
	    	if (filenames[filecount].endsWith(".smi")) smilesinput = true;
	    }
	    //end of input flags

		// Date and Time is used as part of the names of outfiles
		String dateAndTime = SMARTCypMain.getDateAndTime();


		// Produce SMARTSnEnergiesTable object
		System.out.println("\n ************** Processing SMARTS and Energies **************");
		SMARTSnEnergiesTable SMARTSnEnergiesTable = new SMARTSnEnergiesTable();

		
		// Read in structures/molecules
		System.out.println("\n ************** Reading molecule structures **************");
		MoleculeSet moleculeSet = SMARTCyp.readInStructures(filenames, SMARTSnEnergiesTable.getSMARTSnEnergiesTable());

		MoleculeKU moleculeKU;
		for(int moleculeIndex = 0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++){
			moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
			
			System.out.println("\n ************** Molecule " + (moleculeIndex + 1) + " **************");
			
			//System.out.println("\n ************** Matching SMARTS to assign Energies **************");
			moleculeKU.assignAtomEnergies(SMARTSnEnergiesTable.getSMARTSnEnergiesTable());	

			//System.out.println("\n ************** Calculating shortest distance to protonated amine **************");
			moleculeKU.calculateDist2ProtAmine();

			//System.out.println("\n ************** Calculating shortest distance to carboxylic acid **************");
			moleculeKU.calculateDist2CarboxylicAcid();
			
			//System.out.println("\n ************** Calculating Span2End**************");
			moleculeKU.calculateSpan2End();
			
			if (noempcorr == 0){
				//System.out.println("\n ************** Add Empirical Nitrogen Oxidation Corrections **************");
				moleculeKU.unlikelyNoxidationCorrection();
			}
			
			//System.out.println("\n ************** Calculating Accessabilities and Atom Scores**************");
			moleculeKU.calculateAtomAccessabilities();
			//compute 2DSASA
			double [] SASA = moleculeKU.calculateSASA();
			moleculeKU.calculateAtomScores();
			moleculeKU.calculate2D6AtomScores();
			moleculeKU.calculate2C9AtomScores();

			
			//System.out.println("\n ************** Identifying, sorting and ranking C, N, P and S atoms **************");
			moleculeKU.sortAtoms();
			moleculeKU.rankAtoms();
			moleculeKU.sortAtoms2D6();
			moleculeKU.rankAtoms2D6();
			moleculeKU.sortAtoms2C9();
			moleculeKU.rankAtoms2C9();
			
			if(filter == 1){
				moleculeKU.setFilterValue();
			}
					
		}
		
		
		//System.out.println(System.currentTimeMillis() - ms);

		
		//don't write csv file if there are no molecules in the input
		if (moleculeSet.getMoleculeCount()>0){
			if (nocsv==0){
				// Write results as CSV
				System.out.println("\n ************** Writing Results as CSV **************");
				WriteResultsAsCSV writeResultsAsCSV = new WriteResultsAsCSV(dateAndTime, arguments, outputdir, outputfile, printall);
				writeResultsAsCSV.writeCSV(moleculeSet);
			}
		}
		

		if (nohtml==0 && png==1){
			//use old html and png output
			// Write Images	
			System.out.println("\n ************** Writing Images **************");
			GenerateImages generateImages = new GenerateImages(dateAndTime, outputdir);
			generateImages.generateAndWriteImages(moleculeSet);

			// Write results as HTML
			System.out.println("\n ************** Writing Results as HTML **************");
			WriteResultsAsHTML writeResultsAsHTML = new WriteResultsAsHTML(dateAndTime, filenames, outputdir, outputfile);
			writeResultsAsHTML.writeHTML(moleculeSet);
		}
		
		if (nohtml==0 && png==0){
			//use ChemDoodle HTML output
			System.out.println("\n ************** Writing Results as ChemDoodle HTML **************");
			WriteResultsAsChemDoodleHTML writeResultsAsChemDoodle = new WriteResultsAsChemDoodleHTML(dateAndTime, filenames, outputdir, outputfile);
			writeResultsAsChemDoodle.writeHTML(moleculeSet);
		}
		
		if (filter == 1){
			WriteFilterValuesAsCSV writeFilterValuesAsCSV = new WriteFilterValuesAsCSV(dateAndTime, arguments, outputdir, outputfile);
			writeFilterValuesAsCSV.writeCSV(moleculeSet);
			double FilterValue;
			MoleculeSet filteredmoleculeSet = new MoleculeSet();;
			for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
				MoleculeKU filtermolecule = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
				//System.out.println(test.getProperty("FilterValue"));
				FilterValue = (Double) filtermolecule.getProperty("FilterValue"); 
				if(FilterValue > filtercutoffvalue){
					filteredmoleculeSet.addAtomContainer(filtermolecule);
					//System.out.println(moleculeIndex);
				}	
			}
			
			//now save an outputfile in smiles or sdf format
			WriteFilterStructures writeFilterStructures = new WriteFilterStructures(dateAndTime, arguments, outputdir, outputfile, smilesinput);
			writeFilterStructures.write(filteredmoleculeSet);
		}
		
		//we're done, if smilesinput then remove the temporary smiles file
		if(smilesstringinput == 1){
			try{
				File smilesfile = new File("smartcypxyz.smiles");
				if(smilesfile.delete()){
	    			System.out.println("The temporary file " + smilesfile.getName() + " has been deleted!");
	    		}else{
	    			System.out.println("Delete operation of temporary smiles fil failed.");
	    		}
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
		}
	}


	// The Date and Time is used as part of the output filenames
	private String getDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		return dateFormat.format(date);
	}



	// Reads the molecule infiles
	// Stores MoleculeKUs and AtomKUs
	public static MoleculeSet readInStructures(String[] inFileNames, HashMap<String, Double> SMARTSnEnergiesTable) throws CloneNotSupportedException, CDKException{

		MoleculeSet moleculeSet = new MoleculeSet();


		List<IAtomContainer> moleculeList;
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		ISimpleChemObjectReader reader;

		File inputFile;
		String infileName;
		ReaderFactory readerFactory;
		IChemFile emptyChemFile;		
		IChemFile chemFile;


		// Iterate over all molecule infiles (it can be a single file)
		int moleculeFileNr;
		int highestMoleculeID = 1;
		for (moleculeFileNr = 0; moleculeFileNr < inFileNames.length; moleculeFileNr++) {
	
			infileName = inFileNames[moleculeFileNr];	
			inputFile = new File(infileName);

			readerFactory = new ReaderFactory();
			
			boolean deducebonds = false;

			try {

				if (infileName.endsWith(".sdf")) {
					/*//commented away because the V2000 reader seem to fail on many structures in sdf files
					//check if it is V2000or V3000 sdf format
					boolean isV2000 = false;
					boolean isV3000 = false;
					FileInputStream fs= new FileInputStream(infileName);
					BufferedReader br = new BufferedReader(new InputStreamReader(fs));
					for(int i = 0; i < 3; ++i)
					  br.readLine();
					String linefour = br.readLine();
					if (linefour.contains("V2000")) {
						isV2000=true;
					}
					else if (linefour.contains("V3000")) {
						isV3000=true;
					}
					fs.close();
					//now we got the correct format
					if (isV2000){
						reader = new MDLV2000Reader(new FileReader(infileName));
					}
					else if (isV3000){
						reader = new MDLV3000Reader(new FileReader(infileName));
					}
					else {
						reader = new MDLReader(new FileReader(infileName));
					}
					*/
					reader = new MDLReader(new FileReader(infileName));
					
				}
				else if (infileName.endsWith(".smi") || infileName.endsWith(".smiles")){
					reader = new SMILESReader(new FileReader(infileName));
					deducebonds = true;
				}
				else	 reader = readerFactory.createReader(new FileReader(inputFile));


				emptyChemFile = builder.newInstance(IChemFile.class);
				chemFile = (IChemFile) reader.read(emptyChemFile);

				if (chemFile == null) continue;	

				//System.out.println(chemFile.toString());

				// Get Molecules
				moleculeList = ChemFileManipulator.getAllAtomContainers(chemFile);

				// Iterate Molecules
				MoleculeKU moleculeKU;
				IAtomContainer iAtomContainerTmp;		
				IAtomContainer iAtomContainer;	
				CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
				for(int atomContainerNr = 0; atomContainerNr < moleculeList.size() ; atomContainerNr++){
					iAtomContainerTmp = moleculeList.get(atomContainerNr);
					
					// Remove salts or solvents... Keep only the largest molecule
			        if (!ConnectivityChecker.isConnected(iAtomContainerTmp)) {
			        	//System.out.println(atomContainerNr);
			             IMoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(iAtomContainerTmp);

			             int maxID = 0 ;
			             int maxVal = -1 ;
			             for(int i = 0 ; i < fragments.getMoleculeCount() ; i++) {
			            	 if(fragments.getMolecule(i).getAtomCount() > maxVal) {
			                     maxID = i ;
			                     maxVal = fragments.getMolecule(i).getAtomCount();
			                 }
			             }
			             iAtomContainerTmp = fragments.getMolecule(maxID);
			        }
			        //end of salt removal
					
					iAtomContainer = AtomContainerManipulator.removeHydrogens(iAtomContainerTmp);
					
					//check number of atoms, if less than 2 don't add molecule
					if (iAtomContainer.getAtomCount()>1){
						//System.out.println(iAtomContainer.getProperties());
			
						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(iAtomContainer);
						
						
						if(deducebonds){
							DeduceBondSystemTool dbst = new DeduceBondSystemTool();
							iAtomContainer = dbst.fixAromaticBondOrders((IMolecule) iAtomContainer);
						}
						
						
						adder.addImplicitHydrogens(iAtomContainer);
						CDKHueckelAromaticityDetector.detectAromaticity(iAtomContainer); 	
							
						moleculeKU = new MoleculeKU(iAtomContainer, SMARTSnEnergiesTable);	
						moleculeSet.addMolecule(moleculeKU);
						moleculeKU.setID(Integer.toString(highestMoleculeID));
						//set the molecule title in the moleculeKU object
						if (iAtomContainer.getProperty("SMIdbNAME")!="" && iAtomContainer.getProperty("SMIdbNAME")!=null) {
							iAtomContainer.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty("SMIdbNAME"));
						}
						moleculeKU.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty(CDKConstants.TITLE));
						moleculeKU.setProperties(iAtomContainer.getProperties());
						highestMoleculeID++;
					}

				}
				System.out.println(moleculeList.size() + " molecules were read from the file "+ inFileNames[moleculeFileNr]);

			} 
			catch (FileNotFoundException e) {
				System.out.println("File " + inFileNames[moleculeFileNr] + " not found");
				e.printStackTrace();
			} 
			catch (IOException e) {e.printStackTrace();}
		}
		return moleculeSet;
	}

}



