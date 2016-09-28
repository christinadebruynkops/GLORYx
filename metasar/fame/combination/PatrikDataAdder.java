package fame.combination;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openscience.cdk.exception.NoSuchAtomException;

//scan Patrik's descriptors and place in Map<String,ArrayList<String>>
//replace , by \t
//scan combined list
	//header: identify MoleculeName
//add to end of list


public class PatrikDataAdder {
	static String patrik = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/002rydbergDescriptors/allSubstrates.sdf_rydbergAfterBuxFix.csv";
	static String input = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/004metaPrintPredictions/human/testSet.combined.csv";
	static String output = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/004metaPrintPredictions/human/testSet.combinedWithPatrik.csv";
	
	public static Map<String, Map<String, String>> readPatrik() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(patrik));
		String[] splitLine = scanner.nextLine().split(",");
		
		int atom=0;
		int moleculeName=0;
		for (int i=0; i<splitLine.length; i++) {
			if (splitLine[i].equals("Atom")) {
				atom=i;
			}
			if (splitLine[i].equals("Molecule")) {
				moleculeName=i;
			}
		}
		
		String line = "";
		Map<String,Map<String,String>> patrik = new HashMap<String,Map<String,String>>();
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			splitLine = line.split(",");
			Map<String,String> entry = new HashMap<String,String>();
			if (patrik.containsKey(splitLine[moleculeName])) {
				entry = patrik.get(splitLine[moleculeName]);
				entry.put(splitLine[atom], line);
			} else {
				entry.put(splitLine[atom], line);
			}
			patrik.put(splitLine[moleculeName], entry);
		}
		return patrik;
		
	}
	
	public static void readInput(Map<String, Map<String, String>> patrik) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(input));
		String line = scanner.nextLine();
		String[] splitLine = line.split("\t");
		int moleculeName=0;
		int atom=0;
		for (int i=0; i<splitLine.length; i++) {
			if (splitLine[i].equals("MoleculeName")) {
				moleculeName=i;
			} else if (splitLine[i].equals("Atom")) {
				atom=i;
			}
		}
		
		FileOutputStream out = new FileOutputStream(output, false);
		PrintStream p = new PrintStream(out);
		p.println(line + "\tMolecule\tAtom1\tMol_bonds2end\tMol_rotablebonds\tMol_AtomCount\tMol_TPSA\tMol_TPSAperAtom\tMol_Volume\tMol_HAcount\tMol_HDcount\tMol_PIsystemSize\tBranch_bonds2end\tBranch_rotablebonds\tBranch_AtomCount\tBranch_TPSA\tBranch_TPSAperAtom\tBranch_Volume\tBranch_HAcount\tBranch_HDcount\tBranch_PIsystemSize");
		
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			splitLine = line.split("\t");
			if (patrik.get(splitLine[moleculeName]) == null) {
				p.println(line + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
			} else if (patrik.get(splitLine[moleculeName]).get(splitLine[atom]) == null) {
				p.println(line + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
			} else {
				p.println(line + "\t" + patrik.get(splitLine[moleculeName]).get(splitLine[atom]).replace(",", "\t"));
			}
		}
		p.close();
	}
	
	public static void main(String... aArgs) throws IOException, NoSuchAtomException {
		Map<String, Map<String, String>> patrik = readPatrik();
		readInput(patrik);
	}
	
	

}
