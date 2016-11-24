package fame.generateDataSets;

import fame.tools.Globals;
import fame.tools.SoMInfo;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.io.*;
import java.util.*;

public class RandomMoleculeSelector {
	//parameters
	private static int foldsCv = 10;
	private static double testSetRatio = 0.2;
	private static int seed = 42;
	private static int circ_occurence_limit = 10;

	// input SDF
	private static String input_sdf =  "input_data/CYP_DBs/HLM.sdf";

	// CDK descriptors
	private static String cdk_folder =  Globals.DESCRIPTORS_OUT;
//	private static String cdk_all_file=  cdk_folder + "all.csv";
	private static Set<String> removed_circ_descs = new HashSet<>();

	// quantum descriptors
	private static String quantum_path =  Globals.DESCRIPTORS_OUT + "QC_descs/zaretzki_quantum_descriptors.csv";

	//output
	private static String output =  Globals.DATASETS_OUT + "zaretzki_data_quantum.csv";

	/**
	 * Reads in all unique and valid molecules. Valid molecules are molecules that do not contain salts.
	 *
	 * @param validMolecules
	 * @return
	 * @throws FileNotFoundException
	 * @throws CDKException
	 */
	private static Map<String, IMolecule> readInMolecules(String original_sdf) throws FileNotFoundException, CDKException {
		SmilesGenerator sg = new SmilesGenerator();

		DefaultIteratingChemObjectReader mdlReader = new IteratingMDLReader(new FileInputStream(original_sdf), DefaultChemObjectBuilder.getInstance());
		Map<String, IMolecule> iMolecules = new HashMap<String, IMolecule>();

		int saltCounter = 0;
		while (mdlReader.hasNext()) {
			IMolecule iMolecule = (IMolecule) mdlReader.next();
			try {
				//check if this molecule is a salt. If it is a salt, it cannot be considered for the calculations because it will cause the
				//EquivalentClassPartitioner, which is involved in the atom symmetry test, to crash (will run out of heap space).
				if (!ConnectivityChecker.isConnected(iMolecule) ) {
					System.out.println(iMolecule.getProperty(Globals.ID_PROP) + " is a salt: " + sg.createSMILES(iMolecule));
					saltCounter++;
				} else {
					//assign molecule title
//					iMolecule.setProperty("RIREG_ID", iMolecule.getProperty(CDKConstants.TITLE));
					iMolecule.setProperty(CDKConstants.TITLE, iMolecule.getProperty(Globals.ID_PROP));
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

	/**
	 * Reads in descriptor data.
	 * @param iMolecules
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Map<String, IMolecule> readDescriptors(Map<String, IMolecule> iMolecules, String inFile) throws FileNotFoundException {
//		System.out.println("The map has no. molecules: " + iMolecules.size());
		Scanner scanner = new Scanner(new File(inFile));
		String[] descriptorNames = scanner.nextLine().split(",");
		String thisMolecule = "";

		Map<String, IMolecule> available_mols = new HashMap<>(); // new map that will only contain molecules for which we have computed the descriptors
		while (scanner.hasNextLine()) {
			String[] splitLine = scanner.nextLine().split(",");
			thisMolecule = new String(splitLine[0]);
			int atomNumber = (Integer.parseInt(splitLine[1].split("\\.")[1])-1);
			if (iMolecules.containsKey(thisMolecule)) {
				IMolecule mol = iMolecules.get(thisMolecule);
				IAtom atm = mol.getAtom(atomNumber);
				for (int i = 1; i < descriptorNames.length; i++) {
					if (removed_circ_descs.contains(descriptorNames[i])) {
						continue;
					}
					if (!splitLine[i].equals("null")) {
						atm.setProperty(descriptorNames[i], splitLine[i]);
					} else {
						atm.setProperty(descriptorNames[i], null);
					}
				}
				available_mols.put(thisMolecule, mol);
			}
		}

//		// just for debugging
//		for (IMolecule mol : available_mols.values()) {
//			System.out.println(mol.getProperty(Globals.ID_PROP).toString());
//			Utils.printAtomProps(mol);
//			for (IAtom atom : mol.atoms()) {
//				if (atom.getProperty("Atom") == null && !atom.getSymbol().equals("H")) {
//					System.err.println("Atom without annotation detected: " + atom.getProperties().toString());
//				}
//			}
//		}

		return available_mols;
	}

	private static Map<String, IMolecule> readSoMInfo(Map<String, IMolecule> iMolecules) {
		Set<String> set = iMolecules.keySet();
		for (String key : set) {
			IMolecule iMolecule = iMolecules.get(key);

			try {
				SoMInfo.parseInfoAndUpdateMol(iMolecule);
			} catch (Exception exp) {
				System.err.println("WARNING: Failed to parse molecule (" + key + ") due to the following error: ");
				exp.printStackTrace();
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
	private static Map<String, IMolecule> generateTestTraingSet(Map<String, IMolecule> iMolecules, int folds) throws CDKException {
		//generate an ArrayList containing all RMTB IDs
		ArrayList<String> id_set = new ArrayList<String>(); //list containing all RMTB IDs

		Set<String> set = iMolecules.keySet();
		for (String key : set) {
			id_set.add(key);
		}

		Random random = new Random(seed);
		int numberOfRandomlySelectedMolecules=0;
		int pick=0;
		IMolecule iMolecule = null;

		while ((double) numberOfRandomlySelectedMolecules/(double) iMolecules.size() < testSetRatio) {
			pick = random.nextInt(id_set.size());
			iMolecule = iMolecules.get(id_set.get(pick));
			iMolecule.setProperty("set", "testSet1");
			id_set.remove(pick);
			numberOfRandomlySelectedMolecules++;
		}

		System.out.println(numberOfRandomlySelectedMolecules + "/" + iMolecules.size());

		while (id_set.size() > 0) {
			for (int i=0; i<folds; i++) {
				pick = random.nextInt(id_set.size());
				iMolecule = iMolecules.get(id_set.get(pick));
				iMolecule.setProperty("set", i + "CV");
				id_set.remove(pick);
				if (!(id_set.size() > 0)) {
					break;
				}
			}
		}
		return iMolecules;
	}

	/**
	 * Write the final CSV file.
	 *
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

		// create the output file
		File tmp = new File(output);
		tmp.getParentFile().mkdirs();
		if (tmp.exists()) {
			tmp.delete();
		}
		tmp.createNewFile();

		// write the data
		CSVWriter csvWriter = new CSVWriter(output, header);
		for (String key : set) {
			iMolecule = iMolecules.get(key);
			try {
				csvWriter.write(iMolecule, output, header);
			} catch (Exception exp) {
				System.err.println("Failed to save data for molecule: " + key);
				exp.printStackTrace();
			}
		}
	}

	public static Map<String, IMolecule> readCDKDescriptors(Map<String, IMolecule> iMolecules, String cdk_folder) throws Exception {
		List<String> to_remove = new ArrayList<>();
		int counter = 0;
		for (String mol_id : iMolecules.keySet()) {
			File f = new File(cdk_folder + mol_id + ".csv");
			if(f.exists() && !f.isDirectory()) {
				Map<String, IMolecule> dummy = new HashMap<>();
				dummy.put(mol_id, iMolecules.get(mol_id));
				readDescriptors(dummy, f.getAbsolutePath());
				counter++;
//				System.out.println(counter + "\tRead CDK descriptors for: " + mol_id);
			} else {
				to_remove.add(mol_id);
				System.err.println("WARNING: Failed to find CDK descriptors for molecule: " + mol_id);
			}
		}
		to_remove.forEach(iMolecules::remove);
		return iMolecules;
	}

	public static void populateCircDescsSet() throws Exception {
		Map<String, Integer> map;
		FileInputStream fis = new FileInputStream(Globals.DESCRIPTORS_OUT + "circular_stats.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		map = (HashMap) ois.readObject();
		ois.close();
		fis.close();

		for (String signature : map.keySet()) {
			int occurences = map.get(signature);
			if (occurences < circ_occurence_limit) {
				removed_circ_descs.add(signature);
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		System.out.println("##loading valid, unique molecules");
//		if (args.length != 2) {
//			System.err.println("Bad number of arguments");
//			System.exit(1);
//		}

		Map<String, IMolecule> iMolecules = readInMolecules(input_sdf);
		System.out.println("\t" + iMolecules.size() + "\tmolecules have been defined valid and unique");

		// get names of circular descriptors that should be extracted
		populateCircDescsSet();
		System.out.println(String.format("Removed %d circular descriptors", removed_circ_descs.size()));

		System.out.println("##reading in CDK descriptors");
		iMolecules = readCDKDescriptors(iMolecules, cdk_folder);
		System.out.println("\t" + iMolecules.size() + "\tmolecules have CDK descriptors computed for them");

		System.out.println("##reading in QC descriptors");
		iMolecules = readDescriptors(iMolecules, quantum_path);
		System.out.println("\t" + iMolecules.size() + "\tmolecules have CDK and quantum descriptors computed for them");

		System.out.println("##reading SoM information and treating symmetric atoms");
		iMolecules = readSoMInfo(iMolecules);

		System.out.println("##selecting a test set");
//		iMolecules = generateTestTraingSet(iMolecules, iMolecules.size());
		iMolecules = generateTestTraingSet(iMolecules, foldsCv);

		System.out.println("##writing the output file");
		writeOutput(iMolecules);
	}
}