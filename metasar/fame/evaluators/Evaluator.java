package fame.evaluators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.math.util.MathUtils;

public class Evaluator {
	//parameters
	static String species = "human";
	static String experimentNr = "019";
	static String scoreToEvaluate = "mp2d"; //MetaPrint2D = mp2d; my model = my; smartCyp = smartCyp; all smartCyp models combined = smartCypCombined
	static String phaseColumnName = "isSom"; //isSom (which is both phases) or phaseI or phaseII
	
	
	static String wDir        = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
//	static String[] dataSets  = {"test"};
	static String[] dataSets  = {"cv0_validationSet","cv1_validationSet","cv2_validationSet","cv3_validationSet","cv4_validationSet","testSet1","testSet2","testSet3"};
	static String reactionTypes = wDir + "001metaboliteDatabase/002metaPrint2DCalcn/human/metab2011_2_mp2dReact_merged.txt";
	static String output      = wDir + "005weka/" + species + "/" + experimentNr + "_" + scoreToEvaluate + ".eval";
	
    public static Double[] process(String dataSet, int k, PrintStream p) throws FileNotFoundException {   	
    	k++;
		p.println("##############New run for " + dataSet + " and k=" + k + "##############\n");
//		System.out.println("##############New run for " + dataSet + " and k=" + k + "##############\n");

		String prediction = wDir + "005weka/" + species + "/" + experimentNr + "_" + dataSet + ".predictions";
		String reference = wDir + "004metaPrintPredictions/" + species + "/" + dataSet + ".combined.csv";
    	Scanner referenceScanner = new Scanner(new File(reference));
		Scanner predictionScanner = new Scanner(new File(prediction));
		
		//read in table header
		String[] combinedLine = referenceScanner.nextLine().split("\t");
		int normalisedRatio = 0;
		int som = 0;
		int symmetryAtomNr = 0;
		int atomNr = 0;
		int rmtbId = 0;
		int reactionTypesCombined = 0;
		int smartCyp1 =0;
		int smartCyp2 =0;
		int smartCyp3 =0;

		
		//identify the column numbers of columns storing specific information
		for (int i = 0; i < combinedLine.length; i++) {
			if (combinedLine[i].equals(phaseColumnName)) {
				som = i;
			}
			if (combinedLine[i].equals("normalisedRatio")) {
				normalisedRatio = i;
			}
			if (combinedLine[i].equals("SymmetryAtomNumber")) {
				symmetryAtomNr = i;
			}
			if (combinedLine[i].equals("Atom")) {
				atomNr = i;
			}
			if (combinedLine[i].equals("MoleculeName")) {
				rmtbId = i;
			}
			if (combinedLine[i].equals("ReactionTypesCombined")) {
				reactionTypesCombined = i;
			}
			if (combinedLine[i].equals("Score")) {
				smartCyp1 = i;
			}
			if (combinedLine[i].equals("2D6score")) {
				smartCyp2 = i;
			}
			if (combinedLine[i].equals("2Cscore")) {
				smartCyp3 = i;
			}
		}
					
		//go to beginning of Weka predictions in the Weka file
		while (predictionScanner.hasNext()) {
			if (predictionScanner.nextLine().contains("inst#")) {
				break;
			}
		}
		
		Map<Integer, Integer> reactionTypesInDatabase = new HashMap<Integer, Integer>();
		Map<Integer, Integer> reactionTypesCorrectlyPredicted = new HashMap<Integer, Integer>();

		
		Integer numberOfMolecules = 0;
		Integer numberOfCorrectlyPredictedMolecules = 0;

		//array counts the number of molecules with a specific number of heavy atoms. The length of the vector is arbitrarily chosen.
		//In this case, molecules with number of atoms > 40 are all collected in pos. 41 of the array
		int[] numberOfMoleculesWithSpecificNumberofHeavyAtoms = new int[42];
		Double[] pkCounterHeavyAtoms = new Double[42];
		for (int i = 0; i < pkCounterHeavyAtoms.length; i++) {
			pkCounterHeavyAtoms[i] = 0.0;
		}
		Double sumOfTheLiftNumerator = 0.0;
		Double sumOfTheLiftDenominator = 0.0;
		Double sumOfRk = 0.0;
		Double sumOfPk = 0.0;

		combinedLine = combineLine(referenceScanner.nextLine(), predictionScanner.nextLine());
		
		//define which column contains the score:
		int score = 0;
		if (scoreToEvaluate.equals("mp2d")) {
			score = normalisedRatio; //MetaPrint2D
		} else if (scoreToEvaluate.equals("my")) {
			score = combinedLine.length-1; //CDK
		} else if (scoreToEvaluate.equals("smartCyp")) {
			score = smartCyp1;
		}

		while (referenceScanner.hasNextLine()) {
	    	p.println("---Next Molecule---");
	    	boolean hasSom = false;
	    	
			String rmtb = combinedLine[rmtbId];
			String thisRmtbId = combinedLine[rmtbId];

			Map<String[], Double> uniqueMap = new HashMap<String[], Double>();
			ArrayList<String> visitedSymmetryAtoms = new ArrayList<String>();
			p.println("moleculeName\tatomNumber\tsymmetryAtomNumber\tsom\tscore\t");
	    	while (thisRmtbId.equals(rmtb)) {
//	    		System.out.println(combinedLine[som]);
	    		if (combinedLine[som].equals("true")) {
	    			hasSom = true;
	    		}
//		    		System.out.println("symmetry: " + combinedLine[symmetryAtomNr]);
	    		if (!visitedSymmetryAtoms.contains(combinedLine[symmetryAtomNr])) {
	    			visitedSymmetryAtoms.add(combinedLine[symmetryAtomNr]);
	    			p.println(combinedLine[rmtbId] + "\t" + combinedLine[atomNr] + "\t" + combinedLine[symmetryAtomNr] + "\t" + combinedLine[som] + "\t" + combinedLine[score]);
//	    			System.out.println(combinedLine[rmtbId] + "\t" + combinedLine[symmetryAtomNr] + "\t" + combinedLine[som] + "\t" + combinedLine[score]);
	    			Double scoreValue;
	    			if (scoreToEvaluate.contains("smartCyp") && combinedLine[smartCyp1].isEmpty()) {
	    				combinedLine[smartCyp1]="999";
	    				combinedLine[smartCyp2]="999";
	    				combinedLine[smartCyp3]="999";
	    			}
	    			if (scoreToEvaluate.equals("smartCypCombined")) {
	    				Double tmp = (Double.parseDouble(combinedLine[smartCyp1]) + Double.parseDouble(combinedLine[smartCyp2]) + Double.parseDouble(combinedLine[smartCyp3]) )/3;
	    				combinedLine[score]= tmp.toString();
	    			}
	    			scoreValue = Double.parseDouble(combinedLine[score]);
//		    			Double scoreValue = Double.parseDouble(combinedLine[combinedLine.length-1]) + (Double.parseDouble(combinedLine[normalisedRatio])/2);
					uniqueMap.put(combinedLine, scoreValue);
			    	if (!combinedLine[reactionTypesCombined].isEmpty()) {
						reactionTypesInDatabase = analyseReactionType(combinedLine, reactionTypesInDatabase, reactionTypesCombined);
						p.println("reactionTypesInDatabase\t" + reactionTypesInDatabase);
					}
	    		}
				if (referenceScanner.hasNextLine()) {
					combinedLine = combineLine(referenceScanner.nextLine(), predictionScanner.nextLine());
					thisRmtbId = new String(combinedLine[rmtbId]);
				} else {
					break;
				}
			}
	    	
	    	if (!hasSom) {
//	    		System.out.println("Warning: molecule " + rmtb + " has no valid SOM annotated!");
	    	} else {
//	    		System.out.println("Molecule " + combinedLine[rmtbId] + " has at least one valid SOM annotated");
	    		SortedSet<Entry<String[],Double>> uniqueSorted;
	    		if (scoreToEvaluate.equals("mp2d") || scoreToEvaluate.equals("my")) {
	    			uniqueSorted = entriesSortedByDecreasingValues(uniqueMap);
	    		} else {
	    			uniqueSorted = entriesSortedByIncreasingValues(uniqueMap);
	    		}
		    	
		    	/*
		    	 * Evaluate results
		    	 */
		    	p.println();
		    	p.println("Atoms sorted by decreasing probability");
		    	p.println("moleculeName\tatomNumber\tsymmetryAtomNumber\tsom\tprobability");
				Iterator<Entry<String[], Double>> itr = uniqueSorted.iterator();
		    	while (itr.hasNext()) {
		    		Entry<String[], Double> tmp = itr.next();
		    		p.println(tmp.getKey()[rmtbId] + "\t" + tmp.getKey()[atomNr]+ "\t" + tmp.getKey()[symmetryAtomNr] + "\t" + tmp.getKey()[som] + "\t" + tmp.getValue());
		    	}
		    	//int Mp: number of atoms among the top-k ranked positions.
		    	itr = uniqueSorted.iterator();
		    	int Mp = 0;
		        
		        //int OMp: number of SOM atoms among the top-k ranked positions
		        int OMp = 0;
				
		        Entry<String[], Double> uniqueSortedEntry = null;
		        int i = 0;
		        Double lastValid = null;

		    	p.println();
		    	p.println("Top-k ranked atom positions");
		    	p.println("moleculeName\tsymmetryAtomNumber\tsom\tprobability\treactionType");
		    	
				//success among the top-k ranked positions
		    	boolean predicted = false;
		    	while (itr.hasNext() && i < k) {
					uniqueSortedEntry = itr.next();
					if (uniqueSortedEntry.getValue() == 0) {
						lastValid = 0.0;
						Mp=k;
						break;
					}
					p.println(uniqueSortedEntry.getKey()[rmtbId] + "\t" + uniqueSortedEntry.getKey()[symmetryAtomNr] + "\t" + uniqueSortedEntry.getKey()[som] + "\t" + uniqueSortedEntry.getValue() + "\t" + uniqueSortedEntry.getKey()[reactionTypesCombined]);
					if (uniqueSortedEntry.getKey()[som].equals("true")) {
						predicted = true;
						reactionTypesCorrectlyPredicted = analyseReactionType(uniqueSortedEntry.getKey(), reactionTypesCorrectlyPredicted, reactionTypesCombined);
						OMp++;
					}
					lastValid = new Double(uniqueSortedEntry.getValue());
					Mp++;
					i++;
				}
				
				if (lastValid > 0) {
					//success among the top-k ranked positions (extended by identical scores)
					if (itr.hasNext()) {
						uniqueSortedEntry = itr.next();
						while (uniqueSortedEntry.getValue().equals(lastValid)) {
							Mp++;
//									System.out.println(tmp.getKey()[reactionTypesCombined] + "\t" + tmp.getKey()[isSom]);
							if (uniqueSortedEntry.getKey()[som].equals("true")) {
								predicted = true;
								reactionTypesCorrectlyPredicted = analyseReactionType(uniqueSortedEntry.getKey(), reactionTypesCorrectlyPredicted, reactionTypesCombined);
								OMp++;
							}
							p.println("#" + uniqueSortedEntry.getKey()[rmtbId] + "\t" + uniqueSortedEntry.getKey()[symmetryAtomNr] + "\t" + uniqueSortedEntry.getKey()[som] + "\t" + uniqueSortedEntry.getValue() + "\t" + uniqueSortedEntry.getKey()[reactionTypesCombined]);
			
							if (itr.hasNext()) {
								uniqueSortedEntry = itr.next();
							} else break;
						}
					}					
				}
				
				if (predicted) {
					numberOfCorrectlyPredictedMolecules++;
				}
				
		        //int Mr: number of unique atoms of this molecule        
		        int Mr = uniqueMap.size();
		        
		        //int OMr: number of SOM atoms of this molecule
		        int OMr = 0;
		        //iterate over Map
		        for (Entry<String[], Double> entry : uniqueMap.entrySet()) {
		        	if (!entry.getKey()[som].equals("false")) {
		            	OMr++;
		        	}
		        }
				
		        //int NOM: number of non-SOM atoms (NOMr = Mr - OMr)
		        int NOMr = Mr - OMr;
				
		        //int NOMp: number of non-SOM atoms
		        int NOMp = Mp - OMp;
				
		    	p.println();
		    	
		        /*
		         * calculate prediction accuracy
		         */
		        Double Pk = null;
		        if (NOMp >= k) {
		            Pk = ( 1- ((MathUtils.binomialCoefficientDouble(NOMp, k)) / (MathUtils.binomialCoefficient(Mp, k))));
		            p.printf("BENCHMARKS\tk=" + k + "\tPk\t" + "%.2f" + " = 1 - ( " + NOMp + " choose " + k + " ) / ( " + Mp + " choose " + k + " )\t", Pk);
		            p.println();
		        } else {
		        	Pk = 1.0;
		            p.printf("BENCHMARKS\tk=" + k + "\tPk\t" + "%.2f" + " = 1 - ( " + NOMp + " choose " + k + " ) / ( " + Mp + " choose " + k + " )\t", Pk);
		            p.println();
		        }
		        
		        
		        /*
		         * calculate statistical difficulty of randomly predicting an observed SOM on that substrate within k rank-positions
		         */
		        Double Rk = null;
		        if (NOMr >= k) {
		            Rk = (double) ( 1- ((MathUtils.binomialCoefficientDouble(NOMr, k)) / (MathUtils.binomialCoefficient(Mr, k))));
		            p.printf("BENCHMARKS\tk=" + k + "\tRk\t" + "%.2f" + " = 1 - ( " + NOMr + " choose " + k + " ) / ( " + Mr + " choose " + k + " )\t", Rk);            
		            p.println();
		        } else {
		            Rk = 1.0;
		            p.printf("BENCHMARKS\tk=" + k + "\tRk\t" + "%.2f" + " = 1 - ( " + NOMr + " choose " + k + " ) / ( " + Mr + " choose " + k + " )\t", Rk);            
		            p.println();
		        }
		        
		        sumOfRk = sumOfRk + Rk;
		    	sumOfPk = sumOfPk + Pk;
		    	sumOfTheLiftNumerator   = sumOfTheLiftNumerator + (Pk * (1.00/ Rk));
		    	sumOfTheLiftDenominator = sumOfTheLiftDenominator + (1.00 / Rk);
		    	numberOfMolecules++;
		    	if (Mr < pkCounterHeavyAtoms.length-1) {
			    	pkCounterHeavyAtoms[Mr] = pkCounterHeavyAtoms[Mr] + Pk;
			    	numberOfMoleculesWithSpecificNumberofHeavyAtoms[Mr]++;
		    	} else {
			    	pkCounterHeavyAtoms[pkCounterHeavyAtoms.length-1] = pkCounterHeavyAtoms[pkCounterHeavyAtoms.length-1] + Pk;
			    	numberOfMoleculesWithSpecificNumberofHeavyAtoms[pkCounterHeavyAtoms.length-1]++;		    				    		
		    	}
//			    	System.out.println(pKCounter + "\t" + Pk + "\t" + Rk + "\t" + (Pk * (1.00 / Rk)));
		    	p.println();
			}
    	}


		
		//print out standard accuracy for molecules with a specific number of heavy atoms
		p.println("heavy atoms\tPk\tmolecules\tstandard accuracy");
		for (int i = 1; i < pkCounterHeavyAtoms.length; i++) {
			p.println(i +"\t" + pkCounterHeavyAtoms[i] + "\t" + numberOfMoleculesWithSpecificNumberofHeavyAtoms[i] + "\t" + pkCounterHeavyAtoms[i]/(double) numberOfMoleculesWithSpecificNumberofHeavyAtoms[i]);
		}
		
		p.println();
		
		Scanner reactionTypeScanner = new Scanner(new File(reactionTypes));
		Map<String,String> reactionTypes = new HashMap<String,String>();
		while (reactionTypeScanner.hasNextLine()) {
			String line = reactionTypeScanner.nextLine();
			if (!line.startsWith("RXN")) {
				break;
			}
			line = line.replaceAll("RXN\\[", "");
			line = line.replaceAll("\\]", "");
			reactionTypes.put(line.split(" ")[0],line.split(" ")[1].split("\t")[0]);
//				System.out.println(line.split(" ")[0] + "\t" + line.split(" ")[1]);
		}
		
		Set<Integer> set = reactionTypesInDatabase.keySet();
		p.print("reactionType");
		for (Integer key : set) {
			p.print("\t" + key);
		}
		p.println();
		p.print("reactionType");
		for (Integer key : set) {
			p.print("\t" + reactionTypes.get(key.toString()));
		}
		p.println();
		p.print("inDatabase");
		for (Integer key : set) {
			p.print("\t" + reactionTypesInDatabase.get(key));
		}		
		p.println();
		p.print("correctPrediction");
		for (Integer key : set) {
			p.print("\t" + (reactionTypesCorrectlyPredicted.get(key) == null ? "0" : reactionTypesCorrectlyPredicted.get(key)));
		}
		p.println();
		p.print("%correct");
		for (Integer key : set) {
			if (reactionTypesCorrectlyPredicted.get(key) == null) {
				reactionTypesCorrectlyPredicted.put(key, 0);
			}
			p.print("\t" + ((float) reactionTypesCorrectlyPredicted.get(key)/(float) reactionTypesInDatabase.get(key)));
		}
		
		
		p.println();
		p.println();
		p.println("##############RESULTS for " + dataSet + " k=" + k+"##############");
		System.out.println("##############RESULTS for " + dataSet + " k=" + k+"##############");
		System.out.println("number of molecules: " + numberOfMolecules + "\t" + numberOfCorrectlyPredictedMolecules);
		Double[] results = new Double[4];
		p.println("RESULT for number of molecules " + numberOfMolecules);
		results[0] = (double) numberOfMolecules;
		p.println("RESULT: standard accuracy " + sumOfPk/(double) numberOfMolecules);
		results[1] = (sumOfPk/(double) numberOfMolecules);
		p.println("RESULT: lift accuracy " + (sumOfTheLiftNumerator/sumOfTheLiftDenominator));
		results[2] = sumOfTheLiftNumerator/sumOfTheLiftDenominator;
		p.println("RESULT: random model " + sumOfRk/numberOfMolecules);
		results[3] = sumOfRk/numberOfMolecules;
		return results;
	}

	/**
     * Combines all data from the test set csv file and the Weka prediction file
     * @param reference
     * @param prediction
     * @return
     * @throws FileNotFoundException
     */
	public static String[] combineLine(String reference, String prediction) throws FileNotFoundException {
//			System.out.println("wekaLine " + prediction);
//			System.out.println(tableLine);
		prediction = prediction.replaceAll("\\+", "");
		prediction = prediction.replaceAll("\\*", "");
		prediction = prediction.replaceAll(" +", "\t");
		prediction = prediction.substring(1);
		String[] tmpSplitWekaLine1 = prediction.split("\t");
		tmpSplitWekaLine1[2] = tmpSplitWekaLine1[2].replace("1:", "");
		tmpSplitWekaLine1[2] = tmpSplitWekaLine1[2].replace("2:", "");
		if (tmpSplitWekaLine1[2].equals("false")) {
			Float tmp = (float) (1.00 - Float.parseFloat(tmpSplitWekaLine1[3]));
			tmpSplitWekaLine1[3] = tmp.toString();
		}
		
		String[] splitWekaLine1 = new String[4];
		splitWekaLine1[3] = tmpSplitWekaLine1[3];

		//fill combinedLine array with all data from the test set csv file
		String[] splitTableLine = reference.split("\t");
		String[] combinedLine = new String[splitTableLine.length + 1];
		int ii = 0;
		for (int i=0; i < splitTableLine.length; i++) {
			combinedLine[ii] = splitTableLine[i];
			ii++;
		}

		//add Weka result to combinedLine
		combinedLine[ii] = splitWekaLine1[3];
		
		return combinedLine;
		
	}

	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByIncreasingValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByDecreasingValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
    public static Map<Integer, Integer> analyseReactionType(String[] combinedLine, Map<Integer, Integer> map, int reactionTypesCombined) {
		combinedLine[reactionTypesCombined] = combinedLine[reactionTypesCombined].replace("[", "");
		combinedLine[reactionTypesCombined] = combinedLine[reactionTypesCombined].replace("]", "");
		combinedLine[reactionTypesCombined] = combinedLine[reactionTypesCombined].replaceAll(" ", "");
//		combinedLine[reactionTypesCombined] = combinedLine[reactionTypesCombined].replaceAll(":[0-9]", "");
		String reactionTypes[] = combinedLine[reactionTypesCombined].split(";");
		for (int i = 0; i < reactionTypes.length; i++) {
			//even for some known SOMs the reactionType may be not annotated for the simple reason that MetaPrint is not always able to determine
			//the specific reaction type
			if (!reactionTypes[i].isEmpty() && reactionTypes[i].contains(":0")) {
				reactionTypes[i] = reactionTypes[i].replaceAll(":[0-9]", "");
				if (!map.containsKey(Integer.parseInt(reactionTypes[i]))) {
					map.put(Integer.parseInt(reactionTypes[i]), 1);
				} else {
					map.put(Integer.parseInt(reactionTypes[i]), (map.get(Integer.parseInt(reactionTypes[i]))+1));
				}				
			}
//			System.out.println(reactionTypes[i]);
		}
		return map;
	}

	public static void main(String... aArgs) throws FileNotFoundException {
		//check which files are available: CV, testSet, externalSet, then run process
		FileOutputStream out = new FileOutputStream(output, false);
		PrintStream p = new PrintStream(out);
		for (int k=0; k<3; k++) {
			ArrayList<Double[]> resultList = new ArrayList<Double[]>();
			for (String dataSet : dataSets) {
				String prediction = wDir + "005weka/" + species + "/" + experimentNr + "_" + dataSet + ".predictions";
				File f = new File(prediction);
				if (f.exists()) {
					Double[] results = process(dataSet, k, p);
					resultList.add(results);
				}
			}
//			System.out.println();
//			System.out.println("RESULT\t" + "validationSet\ttestSet1\ttestSet2\ttestSet3\trandomTestSet1\trandomTestSet2\trandomTestSet3");
			System.out.println(((
					resultList.get(0)[1]+
					resultList.get(1)[1]+
					resultList.get(2)[1]+
					resultList.get(3)[1]+
					resultList.get(4)[1])/5)
					+ "\t" + 
					resultList.get(5)[1] + "\t" + 
					resultList.get(6)[1] + "\t" + 
					resultList.get(7)[1] + "\t" + 
					resultList.get(5)[3] + "\t" +
					resultList.get(6)[3] + "\t" +
					resultList.get(7)[3]
							);
		}
		p.close();
	}
}
