package fame.generateDataSets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.openscience.cdk.exception.CDKException;

public class ExternalTestSetMarker {
	//parameters
	static String species = "rat";
	static double cutoff2 = 0.80;
	static double cutoff3 = 0.50;
	static String wDir =                     "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	
	static String csvInput = wDir +          "003trainingAndTestSets/" + species + "/001beforeSeparation/randomSelection.csv";
	static String externalMolecules = wDir + "003trainingAndTestSets/" + species + "/001beforeSeparation/distances.csv";
	static String csvOutput = wDir +         "003trainingAndTestSets/" + species + "/001beforeSeparation/randomSelectionRevised.csv";
	
	
	public static void main(String... aArgs) throws IOException, CDKException {
		
		//read in molecules that are from the external chemical domain
		Scanner scanner = new Scanner(new File(externalMolecules));
		ArrayList<String> testSet2 = new ArrayList<String>();
		ArrayList<String> testSet3 = new ArrayList<String>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.replaceAll("\"", "");
			if (Float.valueOf(line.split(",")[1]) <= cutoff2) {
				System.out.println(line);
				testSet2.add(line.split(",")[0]);
			}
			if (Float.valueOf(line.split(",")[1]) <= cutoff3) {
				System.out.println(line);
				testSet3.add(line.split(",")[0]);
			}
		}
		
		File file = new File(csvOutput);
		FileOutputStream out = new FileOutputStream(file, false);
		PrintStream p = new PrintStream(out);
		
		//read in dataset
		//read in header
		scanner = new Scanner(new File(csvInput));
		String header = scanner.nextLine();
		p.println(header);
		String[] line = header.split("\t");
		int setColumnNumber = 0;
		int moleculeNameColumnNumber = 0;
		for (int i=0; i<line.length; i++) {
			if (line[i].equals("set")) {
				setColumnNumber = i;
			}
			if (line[i].equals("MoleculeName")) {
				moleculeNameColumnNumber = i;
			}
		}
//		System.out.println(setColumnNumber);
//		System.out.println(moleculeNameColumnNumber);
		
		while (scanner.hasNextLine()) {
			line = scanner.nextLine().split("\t");
			if (testSet2.contains(line[moleculeNameColumnNumber])) {
				line[setColumnNumber] = "testSet2";
			}
			if (testSet3.contains(line[moleculeNameColumnNumber])) {
				line[setColumnNumber] = "testSet3";
			}
			for (int i=0; i<line.length-1; i++) {
//				System.out.print(line[i] + "\t");
				p.print(line[i] + "\t");
			}
			p.print(line[line.length-1]);
//			System.out.println();
			p.println();
		}
		p.close();
	}
}

