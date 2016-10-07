package fame.generateDataSets;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.NoSuchAtomException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class RandomMoleculeSelector {
	//parameters
//	static String species = "dog";
	static int foldsCv = 10;
	static double testSetRatio = 0.2;
//	static String wDir = "/netscratch/jk528/2013-07-31_metabolismPrediction/";
//	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String wDir = "./";

	//input
//	static String mpInput =         wDir + "001metaboliteDatabase/002metaPrint2DCalcn/" + species + "/metab2011_2_mp2d_merged.txt";
//	static String mpRxnInput =      wDir + "001metaboliteDatabase/002metaPrint2DCalcn/" + species + "/metab2011_2_mp2dReact_merged.txt";
//	static String sdOriginalInput = wDir + "001metaboliteDatabase/003convertedToSdf/test.sdf";
	static String sdOriginalInput = wDir + "MetaSAR_all_annotated.sdf";
//	static String smartCypInput =   wDir + "002descriptors/allSubstrates_smartCyp.csv";
	static String cdkInput = wDir + "data/all_data.csv";
//	static String patrikInput =     wDir + "002descriptors/allSubstrates.sdf_newatomdescriptors.csv";
	
	//output
//	static String output =  wDir + "003trainingAndTestSets/" + species + "/001beforeSeparation/randomSelection.csv";
	static String output =  wDir + "training_and_test_sets/before_separation/random_selection.csv";

	/**
	 * Reads in all unique and valid molecules. Valid molecules are molecules that do not contain salts. 
	 * Unique molecules are identified based on the merged MetaPrint2D model.
	 * @param validMolecules 
	 * @return
	 * @throws FileNotFoundException
	 * @throws CDKException 
	 */
	private static Map<String, IMolecule> readInMolecules() throws FileNotFoundException, CDKException {
		// commented this out because there should be no duplicates in my dataset TODO: check this with Johannes
//		Scanner scanner = new Scanner(new File(mpInput));
//		SmilesGenerator sg = new SmilesGenerator();
//
//		//a specific molecule can occur more than once in the metabolite database. Since we have merged all SOMs onto a single representation for
//		//each molecule, we here identify the RMTB numbers of these unique representations. We only want to read in these unique representations.
//		ArrayList<String> uniqueRmtbs = new ArrayList<String>();
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			if (line.startsWith("@RMTB")) {
//				line = line.substring(1);
//				uniqueRmtbs.add(line.split("\\+")[0]);
//			}
//		}
		
		IteratingMDLReader mdlReader = new IteratingMDLReader(new FileInputStream(sdOriginalInput), DefaultChemObjectBuilder.getInstance());
		Map<String, IMolecule> iMolecules = new HashMap<String, IMolecule>();

		int saltCounter = 0;
		while (mdlReader.hasNext()) {
			IMolecule iMolecule = (IMolecule) mdlReader.next();
			try {	
				//check if this molecule is a salt. If it is a salt, it cannot be considered for the calculations because it will cause the 
				//EquivalentClassPartitioner, which is involved in the atom symmetry test, to crash (will run out of heap space).
				if (!ConnectivityChecker.isConnected(iMolecule) ) {
					System.out.println(iMolecule.getProperty("RXN:VARIATION(1):MDLNUMBER") + " is a salt: " + sg.createSMILES(iMolecule));
					saltCounter++;
				}
				if (ConnectivityChecker.isConnected(iMolecule) && uniqueRmtbs.contains(iMolecule.getProperty("RXN:VARIATION(1):MDLNUMBER"))) {
					//assign molecule title
					iMolecule.setProperty("RIREG_ID", iMolecule.getProperty(CDKConstants.TITLE));
					iMolecule.setProperty(CDKConstants.TITLE, iMolecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
					//generate smiles and add it is a property
					iMolecule.setProperty("smiles", sg.createSMILES(iMolecule));				
					iMolecules.put((String) iMolecule.getProperty(CDKConstants.TITLE), iMolecule);
				}
			} catch (NullPointerException e) {
				System.out.println(e);
			} 
		}
		System.out.println("\t" + saltCounter + "\tSalts have been identified and disregarded");
		return iMolecules;
	}
	
	
//	/**
//	 * Reads in a complete, merged MetaPrint2D model in txt file format and stores it with the individual IAtoms and IMolecules.
//	 * @throws FileNotFoundException
//	 */
//	private static Map<String, IMolecule> mpFileReader(Map<String, IMolecule> iMolecules) throws FileNotFoundException {
//		Scanner scanner = new Scanner(new File(mpInput));
//		//iterate over all molecules of the MetaPrint2D file
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			line = line.substring(1);
//
//			//adds all RMTB numbers to the iMolecule. These numbers are used to identify molecules that are unique.
//			List<String> rmtbs = Arrays.asList(line.split("\\+"));
//
//			//iMolecules does not contain all rmtbs.get(0) because some molecules are invalid and sorted out in the earlier procedure (i.e. salts)
//			if (iMolecules.containsKey(rmtbs.get(0))) {
//				IMolecule iMolecule = iMolecules.get(rmtbs.get(0));
//				//iterate until beginning of the fingerprints section
//				while (!line.startsWith("$")) {
//					line = scanner.nextLine();
//				}
//				line = scanner.nextLine();
//
//				//iterate over all fingerprints
//				int i=0;
//				while (!line.isEmpty()) {
//					IAtom iAtom = iMolecule.getAtom(i);
//					String[] splitLine = line.split("\\t");
//					if (splitLine.length > 1) {
//						iAtom.setProperty("SOM", splitLine[1]);
//						iMolecule.setAtom(i, iAtom);
//					}
//					i++;
//					line = scanner.nextLine();
//				}
//			} else {
//				while (!line.isEmpty()) {
//					if (scanner.hasNextLine()) {
//						line = scanner.nextLine();
//					} else break;
//				}
//			}
//		}
//		return iMolecules;
//	}
	
//	/**
//	 * Reads in a complete MetaPrint2D-react model in txt file format and stores it with the individual IAtoms and IMolecules.
//	 * @param iMolecules
//	 * @return
//	 * @throws FileNotFoundException
//	 */
//	private static Map<String, IMolecule> mpRxnFileReader(Map<String, IMolecule> iMolecules) throws FileNotFoundException {
//		Scanner scanner = new Scanner(new File(mpRxnInput));
//
//		//iterate reaction type declaration at the beginning of the file
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			if (line.isEmpty()) {
//				break;
//			}
//		}
//
//		//iterate over all molecules
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			line = line.substring(1);
//			//Make an arrayList of all RMTB IDs
//			List<String> rmtbs = Arrays.asList(line.split("\\+"));
//
//			//go to beginning of the fingerprints section
//			while (!line.startsWith("$")) {
//				line = scanner.nextLine();
//			}
//
//			//iterate over all fingerprints of a molecule and add any reaction type annotation to a map
//			Map<Integer, String> reactionTypeMap = new HashMap<Integer, String>();
//			int i = 0;
//			while (!line.isEmpty()) {
//				if (scanner.hasNextLine()) {
//					line = scanner.nextLine();
//				} else break;
//				if (!line.isEmpty()) {
//					String[] splitLine = line.split("\\t");
//					if (splitLine.length > 1) {
//						reactionTypeMap.put(i, splitLine[1]);
//					}
//				}
//				i++;
//			}
//
//			Iterator<String> rmtbItr = rmtbs.iterator();
//			while (rmtbItr.hasNext()) {
//				String rmtb = rmtbItr.next();
//				i=0;
//				if (iMolecules.containsKey(rmtb)) {
//					IMolecule iMolecule = iMolecules.get(rmtb);
//					for (int ii= 0; ii < iMolecule.getAtomCount(); ii++) {
//						if (reactionTypeMap.containsKey(ii)) {
//							IAtom iAtom = iMolecule.getAtom(ii);
//							iAtom.setProperty("ReactionTypes", reactionTypeMap.get(ii));
//							iMolecule.setAtom(ii, iAtom);
//						}
//					}
//		            i++;
//				}
//			}
//		}
//		return iMolecules;
//	}
//
//
//	/**
//	 * Reads in SmartCyp results file.
//	 * @param iMolecules
//	 * @return
//	 * @throws FileNotFoundException
//	 */
//	private static Map<String, IMolecule> readSmartCypData(Map<String, IMolecule> iMolecules) throws FileNotFoundException {
//		ArrayList<Integer> energyRelatedDescriptors = new ArrayList<Integer>();
//		int energy = 0;
//		Scanner scanner = new Scanner(new File(smartCypInput));
//		//identifies columns which contain values that are related to the calculated energy for the reason that energy is assigned an arbitrary, high
//		//level if the atoms cannot be a SOM of CYPs
//		String[] descriptorNames = scanner.nextLine().split(",");
//		for (int i=0; i < descriptorNames.length; i++) {
//			if (descriptorNames[i].equals("Score") ||
//					descriptorNames[i].equals("Energy") ||
//					descriptorNames[i].equals("2D6score") ||
//					descriptorNames[i].equals("2Cscore")) {
//				energyRelatedDescriptors.add(i);
//			}
//			if (descriptorNames[i].equals("Energy")) {
//				energy = i;
//			}
//		}
//
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			if (!line.contains("null")) {	//lines can contain "null" if they are a salt or if they have too many rings, for example (see RMTB00089493)
//				String[] splitLine = line.split(",");
//				String thisMolecule = new String(splitLine[0]);
//				int atomNumber = (Integer.parseInt(splitLine[1].split("\\.")[1])-1);
//				if (iMolecules.containsKey(thisMolecule)) {
//					iMolecules.get(thisMolecule).setProperty("smartCyp", "true");
//					if (splitLine[energy].equals("999")) {
//						Iterator<Integer> itr = energyRelatedDescriptors.iterator();
//						while (itr.hasNext()) {
//							splitLine[(Integer) itr.next()] = "";
//						}
//					}
////					System.out.println(iMolecules.get(thisMolecule).getProperty(CDKConstants.TITLE));
//					for (int i = 1; i < descriptorNames.length; i++) {
//						iMolecules.get(thisMolecule).getAtom(atomNumber).setProperty(descriptorNames[i], splitLine[i]);
//					}
//				}
//			}
//		}
//		return iMolecules;
//	}
	
	/**
	 * Reads in descriptor data.
	 * @param iMolecules
	 * @return
	 * @throws FileNotFoundException 
	 */
	private static Map<String, IMolecule> readDescriptorData(Map<String, IMolecule> iMolecules, String inFile, String data) throws FileNotFoundException {
//		System.out.println("The map has no. molecules: " + iMolecules.size());
		Scanner scanner = new Scanner(new File(inFile));
		String[] descriptorNames = scanner.nextLine().split(",");
		String thisMolecule = "";

		while (scanner.hasNextLine()) {
			String[] splitLine = scanner.nextLine().split(",");
			thisMolecule = new String(splitLine[0]);
			int atomNumber = (Integer.parseInt(splitLine[1].split("\\.")[1])-1);
			if (iMolecules.containsKey(thisMolecule)) {
				iMolecules.get(thisMolecule).setProperty(data, "true");
				for (int i = 1; i < descriptorNames.length; i++) {
					iMolecules.get(thisMolecule).getAtom(atomNumber).setProperty(descriptorNames[i], splitLine[i]);
				}
			}
		}
		return iMolecules;
	}
	
	private static Map<String, IMolecule> treatAtomSymmetry(Map<String, IMolecule> iMolecules) throws NoSuchAtomException {
        Set<String> set = iMolecules.keySet();
        for (String key : set) {
			IMolecule iMolecule = iMolecules.get(key);
			//charges need to be set for the EquivalentClassPartitioner to run properly.
			for (int i = 0; i < iMolecule.getAtomCount(); i++) {
				IAtom iAtom = iMolecule.getAtom(i);
				iAtom.setProperty("MoleculeName", iMolecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
				iAtom.setCharge((double) iAtom.getFormalCharge());
			}

			/*
			 * add symmetry numbers to the molecule. Also generate a map containing all SOM information for each unique atom of a molecule
			 */
			int[] symmetryNumbersArray = null;
//	        System.out.println("#the current molecule is " + iMolecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
			EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner((AtomContainer) iMolecule);
			symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu((AtomContainer) iMolecule);

			Map<Integer,ArrayList<String>> somMap = new HashMap<Integer,ArrayList<String>>();
			Map<Integer,ArrayList<String>> reactivityTypesMap = new HashMap<Integer,ArrayList<String>>();
			for (int i=0; i < iMolecule.getAtomCount(); i++) {
				IAtom iAtom = iMolecule.getAtom(i);
				iAtom.setProperty("SymmetryAtomNumber", symmetryNumbersArray[i+1]);
				iMolecule.setAtom(i, iAtom);
				if (iAtom.getProperty("SOM") != null) {					
					List<String> somList = Arrays.asList(iAtom.getProperty("SOM").toString().split(","));
					Iterator<String> somItr = somList.iterator();
					while (somItr.hasNext()) {
						String som = somItr.next();
						if (somMap.containsKey((Integer) iAtom.getProperty("SymmetryAtomNumber"))) {
							ArrayList<String> combinedList = somMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber"));
							if (!combinedList.contains(som)) {
								combinedList.add(som);
							}
							somMap.put((Integer) iAtom.getProperty("SymmetryAtomNumber"), combinedList);
						} else {
							ArrayList<String> combinedList = new ArrayList<String>();
							combinedList.add(som);
							somMap.put((Integer) iAtom.getProperty("SymmetryAtomNumber"), combinedList);
						}
					}					
				}
				if (iAtom.getProperty("ReactionTypes") != null) {
					List<String> somList = Arrays.asList(iAtom.getProperty("ReactionTypes").toString().split(","));
					Iterator<String> somItr = somList.iterator();
					while (somItr.hasNext()) {
						String som = somItr.next();
						if (reactivityTypesMap.containsKey((Integer) iAtom.getProperty("SymmetryAtomNumber"))) {
							ArrayList<String> combinedList = reactivityTypesMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber"));
							if (!combinedList.contains(som)) {
								combinedList.add(som);
							}
							reactivityTypesMap.put((Integer) iAtom.getProperty("SymmetryAtomNumber"), combinedList);
						} else {
							ArrayList<String> combinedList = new ArrayList<String>();
							combinedList.add(som);
							reactivityTypesMap.put((Integer) iAtom.getProperty("SymmetryAtomNumber"), combinedList);
						}
					}					
				}
			}
			//add combined SOM information to each unique atom of a molecule
			for (int i=0; i < iMolecule.getAtomCount(); i++) {
				IAtom iAtom = iMolecule.getAtom(i);
//				System.out.println("AtomSymmetryNumber      " + iAtom.getProperty("SymmetryAtomNumber"));
				if (somMap.containsKey((Integer) iAtom.getProperty("SymmetryAtomNumber"))) {
					iAtom.setProperty("SOMcombined", somMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber")));
					iAtom.setProperty("isSom", "true");
					
					//add information about the metabolic phase
					if (somMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber")).contains("A2")) {
						iAtom.setProperty("phaseII", "true");
						if (somMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber")).size()>1) {
							iAtom.setProperty("phaseI", "true");
						}
					} else {
						iAtom.setProperty("phaseI", "true");
					}
				} else {
					iAtom.setProperty("isSom", "false");
				}
				if (reactivityTypesMap.containsKey((Integer) iAtom.getProperty("SymmetryAtomNumber"))) {
					iAtom.setProperty("ReactionTypesCombined", reactivityTypesMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber")));
				}
				if (iAtom.getProperty("SOMcombined") == null) {
					iAtom.setProperty("SOMcombined", "");
				}
				if (iAtom.getProperty("phaseI") == null) {
					iAtom.setProperty("phaseI", "false");
				}
				if (iAtom.getProperty("phaseII") == null) {
					iAtom.setProperty("phaseII", "false");
				}
//				System.out.println(somMap.get((Integer) iAtom.getProperty("SymmetryAtomNumber")) + "\t" + iAtom.getProperty("phaseI") + "\t" + iAtom.getProperty("phaseII"));
			}
			
		}
		return iMolecules;
	}
	
	/**
	 * Selects a defined number of molecules randomly (this is to generate the test set)
	 * @param iMolecules
	 * @return
	 * @throws CDKException 
	 */
	private static Map<String, IMolecule> generateTestTraingSet(Map<String, IMolecule> iMolecules) throws CDKException {
		//generate an ArrayList containing all RMTB IDs
		ArrayList<String> unusedRmtbs = new ArrayList<String>(); //list containing all RMTB IDs

		Set<String> set = iMolecules.keySet();
        for (String key : set) {
        	unusedRmtbs.add(key);
        }
		
		Random random = new Random();
		int numberOfRandomlySelectedMolecules=0;
		int pick=0;
        IMolecule iMolecule = null;
        
        while ((double) numberOfRandomlySelectedMolecules/(double) iMolecules.size() < testSetRatio) {
        	pick = random.nextInt(unusedRmtbs.size());
			iMolecule = iMolecules.get(unusedRmtbs.get(pick));
			iMolecule.setProperty("set", "testSet1");
			unusedRmtbs.remove(pick);
			numberOfRandomlySelectedMolecules++;
		}
        
        System.out.println(numberOfRandomlySelectedMolecules + "\t" + iMolecules.size());

        while (unusedRmtbs.size() > 0) {
        	for (int i=0; i<foldsCv; i++) {
        		pick = random.nextInt(unusedRmtbs.size());
        		iMolecule = iMolecules.get(unusedRmtbs.get(pick));
        		iMolecule.setProperty("set", i + "CV");
        		unusedRmtbs.remove(pick);
        		if (!(unusedRmtbs.size() > 0)) {
        			break;
        		}
        	}
        }               
		return iMolecules;
	}
	
	/**
	 * Write sdf and MetaPrint output for training and test set
	 * @param iMolecules 
	 * @param testSet
	 * @param iMolecules
	 * @param atomTypeNames 
	 * @return 
	 * @return 
	 * @throws IOException 
	 */
	private static void writeOutput(Map<String, IMolecule> iMolecules) throws IOException {
        //generate header of the CSV files. Cave: Not all atoms have all properties annotated. Hence, I am iterating over the atoms of the
        //first molecule to make sure that I have all atom properties considered.
        ArrayList<String> header = new ArrayList<String>();
		Set<String> set = iMolecules.keySet();

		//go over first molecule to collect all properties
    	IMolecule iMolecule = iMolecules.get(set.iterator().next());
		Map<Object, Object> properties = iMolecule.getProperties();
        Set<Object> set1 = properties.keySet();
                    
        //collect all atom properties
        for (int i=0; i < iMolecule.getAtomCount(); i++) {
    		IAtom iAtom = iMolecule.getAtom(i);
    		properties = iAtom.getProperties();
            set1 = properties.keySet();
            for (Object key1 : set1) {
            	if (!header.contains(key1.toString())) {
                    header.add(key1.toString());
            	}
            }
        }
        
        //set and smiles are molecular and not atomic properties.
        header.add("set");
        header.add("smiles");
        
        CSVWriter csvWriter = new CSVWriter(output, header);
        for (String key : set) {
        	iMolecule = iMolecules.get(key);
        	//must have smartcyp, patrik and cdk calculated
        	if (iMolecule.getProperty("smartCyp") != null && iMolecule.getProperty("cdk") != null && iMolecule.getProperty("patrik") != null) {
            	csvWriter.write(iMolecule, output, header);        		
        	}
        }
	}
	
	public static void main(String... aArgs) throws IOException, CDKException {
		System.out.println("##loading valid, unique molecules");
		Map<String, IMolecule> iMolecules = readInMolecules();
		System.out.println("\t" + iMolecules.size() + "\tMolecules have been defined valid and unique");
//		System.out.println("##reading in MetaPrint model");
//		iMolecules = mpFileReader(iMolecules);
//		System.out.println("\t" + iMolecules.size() + "\tMolecules");
//		System.out.println("##reading in MetaPrint-React model");
//		iMolecules = mpRxnFileReader(iMolecules);
//		System.out.println("\t" + iMolecules.size() + "\tMolecules");
//		System.out.println("##reading in SmartCyp data");
//		iMolecules = readSmartCypData(iMolecules);
//		System.out.println("\t" + iMolecules.size() + "\tMolecules");
		System.out.println("##reading in CDK descriptors");
		iMolecules = readDescriptorData(iMolecules, cdkInput, "cdk");
		System.out.println("\t" + iMolecules.size() + "\tMolecules");
//		System.out.println("##reading in Patrik's descriptors");
//		iMolecules = readDescriptorData(iMolecules, patrikInput, "patrik");
//		System.out.println("\t" + iMolecules.size() + "\tMolecules");
		System.out.println("##treating symmetric atoms");
		iMolecules = treatAtomSymmetry(iMolecules);
		System.out.println("##selecting a test set");
		iMolecules = generateTestTraingSet(iMolecules);
		System.out.println("##writing the output files");
		writeOutput(iMolecules);
	}
}