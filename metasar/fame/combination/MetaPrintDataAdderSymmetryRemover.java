package fame.combination;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openscience.cdk.exception.NoSuchAtomException;

public class MetaPrintDataAdderSymmetryRemover {
	/**
	 * Reads in a MetaPrint results file which is of the type:
	    atomNumber, atomtype, normalisedRatio, reactionCentreCount, substrateCount
		RMTB00000003
		0,C.3,0.159,0,0
		1,C.2,0.159,0,0
		2,C.3,0.159,0,0
		3,C.3,0.159,0,0
		4,Br,0.159,0,0
		RMTB00000004
		0,C.3,0.159,0,0
		1,C.2,0.159,0,0
		...
	 * @param metaPrintResults 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Map<String, ArrayList<String>> readMetaPrintResults(String metaPrintResults) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(metaPrintResults));
		Map<String, ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
		ArrayList<String> entry = new ArrayList<String>();
		
		entry.add(scanner.nextLine());
		map.put("header", entry);
		String line = scanner.nextLine();

		while (scanner.hasNext()) {
			String name = line;
			line = scanner.nextLine();
			entry = new ArrayList<String>();
			while (!line.startsWith("RMTB")) {
				if (!line.isEmpty() && !line.contains(",H,")) {
					line = line.replaceAll(",", "\t");

					entry.add(line);
				}
				if (scanner.hasNextLine()) {
					line = scanner.nextLine();					
				} else break;
			}
			map.put(name, entry);
		}
		return map;
	}
	
	private static void writeOutputTable(Map<String, ArrayList<String>> metaPrint, String inputTable, String outputTable) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(inputTable));
		FileOutputStream out = new FileOutputStream(outputTable, false);
		PrintStream p = new PrintStream(out);			
	
		//preparing and printing the header line. identify column containing the MoleculeName
		String header = scanner.nextLine();
		header = header.replaceAll(" ",  "");
		header = header + "\t";
		String[] splitLine = header.split("\t");
		int moleculeName = 0;
		int symmetryAtomNumber = 0;
		for (int i = 0; i < splitLine.length; i++) {
//			System.out.println(splitLine[i]);
			if (splitLine[i].equals("MoleculeName")) {
				moleculeName = i;
			}
			if (splitLine[i].equals("SymmetryAtomNumber")) {
				symmetryAtomNumber = i;
			}
		}
		
		for(int i=0; i < metaPrint.get("header").size()-1; i++) {
	        header = header + metaPrint.get("header").get(i) + "\t";
		}
		header = header + metaPrint.get("header").get(metaPrint.get("header").size()-1);
		header = header.replaceAll(",", "\t");
		header = header.replaceAll(" ",  "");

		System.out.println("header " + header);
		p.println(header);

		String line = "";
		int i=0;
		String old = "";
		ArrayList<String> visitedSymmetryAtoms = null;
		while (scanner.hasNextLine()) {
			line= scanner.nextLine();
			splitLine = line.split("\t");
			System.out.println(splitLine[moleculeName]);
			
			if (splitLine[moleculeName].equals(old)) {
				line = line + "\t" + metaPrint.get(splitLine[moleculeName]).get(i+1);
				line = line.replaceAll("'", "\t");
				if (!visitedSymmetryAtoms.contains(splitLine[symmetryAtomNumber])) {
					visitedSymmetryAtoms.add(splitLine[symmetryAtomNumber]);
					p.println(line);
				}

//				System.out.println("This line B" + line + "," + metaPrint.get(splitLine[moleculeName]).get(i+1));
				old = new String(splitLine[moleculeName]);
				i++;
			} else {
				i = 0;
//				System.out.println("This line A " + line + "," + metaPrint.get(splitLine[moleculeName]).get(i));

				line = line + "\t" + metaPrint.get(splitLine[moleculeName]).get(i);
				old = new String(splitLine[moleculeName]);
				visitedSymmetryAtoms = new ArrayList<String>();
				visitedSymmetryAtoms.add(splitLine[symmetryAtomNumber]);
				p.println(line);
			}
		}
		p.close();
	}
	
	public static void main(String... aArgs) throws IOException, NoSuchAtomException {
//		String[] species = {"all","human","rat","dog"};
		String[] species = {"human"};
//		String[] dataSet = {"cv0_trainingSet","cv1_trainingSet","cv2_trainingSet","cv3_trainingSet","cv4_trainingSet",
//				"cv0_validationSet","cv1_validationSet","cv2_validationSet","cv3_validationSet","cv4_validationSet","trainingSet",
//				"testSet1","testSet2","testSet3"};
		String[] dataSet = {"testSet1"};
		
		for (int i=0; i<species.length; i++) {
			for (int ii=0; ii<dataSet.length;ii++) {
				String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
				String inputTable = wDir + "003trainingAndTestSets/" + species[i] + "/002afterSeparation/" + dataSet[ii] + ".csv";
				String metaPrintResults = wDir + "004metaPrintPredictions/" + species[i] + "/" + dataSet[ii] + ".metaPrint2D.predictions";
				String outputTable = wDir + "004metaPrintPredictions/" + species[i] + "/" + dataSet[ii] + ".combined.csv";
				Map<String, ArrayList<String>> metaPrint = readMetaPrintResults(metaPrintResults);
				writeOutputTable(metaPrint, inputTable, outputTable);				
			}
		}
	}
}
