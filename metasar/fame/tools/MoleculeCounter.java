package fame.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MoleculeCounter {
	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String input = wDir + "003trainingAndTestSets/dog/001beforeSeparation/randomSelection.csv";
	
	
	public static void main(String... aArgs) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(input));
		int i=0;
		int moleculeName=0;
		int set=0;

		String[] splitLine = scanner.nextLine().split("\t");
		for (i=0; i<splitLine.length; i++) {
			if (splitLine[i].equals("MoleculeName")) {
				moleculeName=i;
			}
			if (splitLine[i].equals("set")) {
				set=i;
			}
		}
		
		ArrayList<String> molecules = new ArrayList<String>();
		int moleculeCount=0;
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (!molecules.contains(line[moleculeName])) {
				if (line[set].contains("CV")) {
					System.out.println(line[moleculeName]);
					molecules.add(line[moleculeName]);
					moleculeCount++;
				}
			}
		}
		System.out.println("There are " + moleculeCount + " molecule in this file.");
	}
}
