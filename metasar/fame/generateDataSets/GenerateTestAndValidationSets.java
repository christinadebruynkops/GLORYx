package fame.generateDataSets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class GenerateTestAndValidationSets {
	//parameters
	static String species = "rat";
	
	//input
	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String csvInput = wDir + "003trainingAndTestSets/" + species + "/001beforeSeparation/randomSelectionRevised.csv";
	static String sdfInput = wDir + "001metaboliteDatabase/003convertedToSdf/allSubstrates.sdf";
	static String mp2dInput = wDir + "001metaboliteDatabase/002metaPrint2DCalcn/" + species + "/metab2011_2_mp2d_merged.txt";
	
	//output
	static String csvHeader = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/header.csv";
	static String csv0CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv0_validationSet.csv";
	static String csv1CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv1_validationSet.csv";
	static String csv2CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv2_validationSet.csv";
	static String csv3CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv3_validationSet.csv";
	static String csv4CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv4_validationSet.csv";
	static String csvTestSet1 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet1.csv";
	static String csvTestSet2 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet2.csv";
	static String csvTestSet3 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet3.csv";
	
	static String sdf0CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv0_validationSet.sdf";
	static String sdf1CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv1_validationSet.sdf";
	static String sdf2CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv2_validationSet.sdf";
	static String sdf3CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv3_validationSet.sdf";
	static String sdf4CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv4_validationSet.sdf";
	static String sdfTestSet1 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet1.sdf";
	static String sdfTestSet2 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet2.sdf";
	static String sdfTestSet3 = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/testSet3.sdf";

	static String metaPrint2D0CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv0_validationSet.metaPrint2D";
	static String metaPrint2D1CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv1_validationSet.metaPrint2D";
	static String metaPrint2D2CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv2_validationSet.metaPrint2D";
	static String metaPrint2D3CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv3_validationSet.metaPrint2D";
	static String metaPrint2D4CV = wDir + "003trainingAndTestSets/" + species + "/002afterSeparation/cv4_validationSet.metaPrint2D";
	
	private static Map<String, ArrayList<String>> mpFileReader() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(mp2dInput));
		//iterate over all molecules of the MetaPrint2D file
		String line = scanner.nextLine();
		Map<String, ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
		
		while (scanner.hasNextLine()) {
			ArrayList<String> entry = new ArrayList<String>();
			entry.add(line);
//			System.out.println(line);

			line = line.substring(1);
			String[] rmtbs = line.split("\\+");
			line = scanner.nextLine();

			while (!line.startsWith("@RMTB")) {
				entry.add(line);
//				System.out.println(line);
				if (scanner.hasNextLine()) {
					line = scanner.nextLine();
				} else break;
			}

			map.put(rmtbs[0], entry);
//			System.out.println(rmtbs[0] + "\t" + entry);
		}
		return map;
	}
	
	static Map<String, ArrayList<String>> sdFileReader() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(sdfInput));
		Map<String, ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
		while (scanner.hasNextLine()) {
			ArrayList<String> entry = new ArrayList<String>();
			String line = scanner.nextLine();
			String rmtbId = "";
			while (!line.equals("$$$$")) {
				entry.add(line);
				if (line.startsWith("RMTB")) {
					rmtbId = line;
				}
				line = scanner.nextLine();
			}
			entry.add("$$$$");
			entry.set(0, rmtbId);
			map.put(rmtbId, entry);
//			System.out.println(entry);
		}
		return map;
	}
	
	static void process(Map<String, ArrayList<String>> sdFileMap, Map<String, ArrayList<String>> metaPrint2DFileMap) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(csvInput));
		//read in header
		String header = scanner.nextLine();
		System.out.println(header);
		String[] splitLine = header.split("\t");
		int setColumnNumber = 0;
		int moleculeNameColumnNumber = 0;
		
		//identify relevant columns
		for (int i=0; i<splitLine.length; i++) {
			if (splitLine[i].equals("set")) {
				setColumnNumber = i;
			}
			if (splitLine[i].equals("MoleculeName")) {
				moleculeNameColumnNumber = i;
			}
		}
		
		
		File fileHeader = new File(csvHeader);
		FileOutputStream outHeader = new FileOutputStream(fileHeader, false);
		PrintStream pHeader = new PrintStream(outHeader);
		
		File fileCsv0CV = new File(csv0CV);
		FileOutputStream outCsv0CV = new FileOutputStream(fileCsv0CV, false);
		PrintStream pCsv0CV = new PrintStream(outCsv0CV);
		
		File fileCsv1CV = new File(csv1CV);
		FileOutputStream outCsv1CV = new FileOutputStream(fileCsv1CV, false);
		PrintStream pCsv1CV = new PrintStream(outCsv1CV);
		
		File fileCsv2CV = new File(csv2CV);
		FileOutputStream outCsv2CV = new FileOutputStream(fileCsv2CV, false);
		PrintStream pCsv2CV = new PrintStream(outCsv2CV);
		
		File fileCsv3CV = new File(csv3CV);
		FileOutputStream outCsv3CV = new FileOutputStream(fileCsv3CV, false);
		PrintStream pCsv3CV = new PrintStream(outCsv3CV);
		
		File fileCsv4CV = new File(csv4CV);
		FileOutputStream outCsv4CV = new FileOutputStream(fileCsv4CV, false);
		PrintStream pCsv4CV = new PrintStream(outCsv4CV);
		
		File fileCsvTestSet1 = new File(csvTestSet1);
		FileOutputStream outCsvTestSet1 = new FileOutputStream(fileCsvTestSet1, false);
		PrintStream pCsvTestSet1 = new PrintStream(outCsvTestSet1);
		
		File fileCsvTestSet2 = new File(csvTestSet2);
		FileOutputStream outfileCsvTestSet2 = new FileOutputStream(fileCsvTestSet2, false);
		PrintStream pCsvTestSet2 = new PrintStream(outfileCsvTestSet2);
		
		File fileCsvTestSet3 = new File(csvTestSet3);
		FileOutputStream outfileCsvTestSet3 = new FileOutputStream(fileCsvTestSet3, false);
		PrintStream pCsvTestSet3 = new PrintStream(outfileCsvTestSet3);
		
		File fileSdf0CV = new File(sdf0CV);
		FileOutputStream outSdf0CV = new FileOutputStream(fileSdf0CV, false);
		PrintStream pSdf0CV = new PrintStream(outSdf0CV);
		
		File fileSdf1CV = new File(sdf1CV);
		FileOutputStream outSdf1CV = new FileOutputStream(fileSdf1CV, false);
		PrintStream pSdf1CV = new PrintStream(outSdf1CV);
		
		File fileSdf2CV = new File(sdf2CV);
		FileOutputStream outSdf2CV = new FileOutputStream(fileSdf2CV, false);
		PrintStream pSdf2CV = new PrintStream(outSdf2CV);
		
		File fileSdf3CV = new File(sdf3CV);
		FileOutputStream outSdf3CV = new FileOutputStream(fileSdf3CV, false);
		PrintStream pSdf3CV = new PrintStream(outSdf3CV);
		
		File fileSdf4CV = new File(sdf4CV);
		FileOutputStream outSdf4CV = new FileOutputStream(fileSdf4CV, false);
		PrintStream pSdf4CV = new PrintStream(outSdf4CV);
		
		File fileSdfTestSet1 = new File(sdfTestSet1);
		FileOutputStream outSdfTestSet1 = new FileOutputStream(fileSdfTestSet1, false);
		PrintStream pSdfTestSet1 = new PrintStream(outSdfTestSet1);
		
		File fileSdfTestSet2 = new File(sdfTestSet2);
		FileOutputStream outfileSdfTestSet2 = new FileOutputStream(fileSdfTestSet2, false);
		PrintStream pSdfTestSet2 = new PrintStream(outfileSdfTestSet2);

		File fileSdfTestSet3 = new File(sdfTestSet3);
		FileOutputStream outfileSdfTestSet3 = new FileOutputStream(fileSdfTestSet3, false);
		PrintStream pSdfTestSet3 = new PrintStream(outfileSdfTestSet3);
		
		File fileMetaPrint2D0CV = new File(metaPrint2D0CV);
		FileOutputStream outMetaPrint2D0CV = new FileOutputStream(fileMetaPrint2D0CV, false);
		PrintStream pMetaPrint2D0CV = new PrintStream(outMetaPrint2D0CV);
		
		File fileMetaPrint2D1CV = new File(metaPrint2D1CV);
		FileOutputStream outMetaPrint2D1CV = new FileOutputStream(fileMetaPrint2D1CV, false);
		PrintStream pMetaPrint2D1CV = new PrintStream(outMetaPrint2D1CV);
		
		File fileMetaPrint2D2CV = new File(metaPrint2D2CV);
		FileOutputStream outMetaPrint2D2CV = new FileOutputStream(fileMetaPrint2D2CV, false);
		PrintStream pMetaPrint2D2CV = new PrintStream(outMetaPrint2D2CV);
		
		File fileMetaPrint2D3CV = new File(metaPrint2D3CV);
		FileOutputStream outMetaPrint2D3CV = new FileOutputStream(fileMetaPrint2D3CV, false);
		PrintStream pMetaPrint2D3CV = new PrintStream(outMetaPrint2D3CV);
		
		File fileMetaPrint2D4CV = new File(metaPrint2D4CV);
		FileOutputStream outMetaPrint2D4CV = new FileOutputStream(fileMetaPrint2D4CV, false);
		PrintStream pMetaPrint2D4CV = new PrintStream(outMetaPrint2D4CV);
		
		pHeader.println(header);
		pHeader.close();
		
		
		pCsv0CV.println(header);
		pCsv1CV.println(header);
		pCsv2CV.println(header);
		pCsv3CV.println(header);
		pCsv4CV.println(header);
		pCsvTestSet1.println(header);
		pCsvTestSet2.println(header);
		pCsvTestSet3.println(header);
		
		//scan all lines
		String line = scanner.nextLine();
		splitLine = line.split("\t");
		while (scanner.hasNextLine()) {
			ArrayList<String> entry = new ArrayList<String>();
			String moleculeName = splitLine[moleculeNameColumnNumber];
			String currentName = splitLine[moleculeNameColumnNumber];
			String currentSet = splitLine[setColumnNumber];
			System.out.println("Next molecule: " + splitLine[moleculeNameColumnNumber]);
			while (currentName.equals(moleculeName)) {
//				System.out.println("this molecule " + splitLine[moleculeNameColumnNumber]);
				//replaces commas which can cause interpretation errors with Weka
				line = line.replace(",", ";");
				if (line.endsWith("\t")) {
					//removes terminal tabs which can cause interpretation errors with Weka
					line = line.substring(0, line.length()-1);
				}
				entry.add(line);
				if (scanner.hasNextLine()) {
					line = scanner.nextLine();
					splitLine = line.split("\t");
					currentName = splitLine[moleculeNameColumnNumber];
				} else break;
			}
			Iterator<String> csvItr = entry.iterator();
//			System.out.println("number of lines " + entry.size());
			Iterator<String> sdfItr = sdFileMap.get(moleculeName).iterator();
			Iterator<String> metaPrint2DItr = metaPrint2DFileMap.get(moleculeName).iterator();
			
			if (currentSet.equals("0CV")) {
				while (csvItr.hasNext()) {
					pCsv0CV.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdf0CV.println(sdfItr.next());
				}
				while (metaPrint2DItr.hasNext()) {
					pMetaPrint2D0CV.println(metaPrint2DItr.next());
				}
			} else if (currentSet.equals("1CV")) {
				while (csvItr.hasNext()) {
					pCsv1CV.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdf1CV.println(sdfItr.next());
				}
				while (metaPrint2DItr.hasNext()) {
					pMetaPrint2D1CV.println(metaPrint2DItr.next());
				}
			} else if (currentSet.equals("2CV")) {
				while (csvItr.hasNext()) {
					pCsv2CV.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdf2CV.println(sdfItr.next());
				}
				while (metaPrint2DItr.hasNext()) {
					pMetaPrint2D2CV.println(metaPrint2DItr.next());
				}
			} else if (currentSet.equals("3CV")) {
				while (csvItr.hasNext()) {
					pCsv3CV.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdf3CV.println(sdfItr.next());
				}
				while (metaPrint2DItr.hasNext()) {
					pMetaPrint2D3CV.println(metaPrint2DItr.next());
				}
			} else if (currentSet.equals("4CV")) {
				while (csvItr.hasNext()) {
					pCsv4CV.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdf4CV.println(sdfItr.next());
				}
				while (metaPrint2DItr.hasNext()) {
					pMetaPrint2D4CV.println(metaPrint2DItr.next());
				}
			} else if (currentSet.equals("testSet3")) {
				while (csvItr.hasNext()) {
					String tmp = csvItr.next();
					pCsvTestSet3.println(tmp);
					pCsvTestSet2.println(tmp);
					pCsvTestSet1.println(tmp);
				}
				while (sdfItr.hasNext()) {
					String tmp = sdfItr.next();
					pSdfTestSet3.println(tmp);
					pSdfTestSet2.println(tmp);
					pSdfTestSet1.println(tmp);
				}
			}
			else if (currentSet.equals("testSet2")) {
				while (csvItr.hasNext()) {
					String tmp = csvItr.next();
					pCsvTestSet2.println(tmp);
					pCsvTestSet1.println(tmp);
				}
				while (sdfItr.hasNext()) {
					String tmp = sdfItr.next();
					pSdfTestSet2.println(tmp);
					pSdfTestSet1.println(tmp);
				}
			} else if (currentSet.equals("testSet1")) {
				while (csvItr.hasNext()) {
					pCsvTestSet1.println(csvItr.next());
				}
				while (sdfItr.hasNext()) {
					pSdfTestSet1.println(sdfItr.next());
				}
			} 
		}
		
		pCsv0CV.close();
		pCsv1CV.close();
		pCsv2CV.close();
		pCsv3CV.close();
		pCsv4CV.close();
		pCsvTestSet1.close();
		pCsvTestSet2.close();
		pCsvTestSet3.close();
		pSdf0CV.close();
		pSdf1CV.close();
		pSdf2CV.close();
		pSdf3CV.close();
		pSdf4CV.close();
		pSdfTestSet1.close();
		pSdfTestSet2.close();
		pSdfTestSet3.close();
		pMetaPrint2D0CV.close();
		pMetaPrint2D1CV.close();
		pMetaPrint2D2CV.close();
		pMetaPrint2D3CV.close();
		pMetaPrint2D4CV.close();
	}
	
	
	public static void main(String... aArgs) throws FileNotFoundException {
		Map<String, ArrayList<String>> sdFileMap = sdFileReader();
		Map<String, ArrayList<String>> metaPrint2DFileMap = mpFileReader();
		process(sdFileMap, metaPrint2DFileMap);
	}
}
