package fame.evaluators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.math.util.MathUtils;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.NoSuchAtomException;
import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

public class ZaretzkiSmartCyp {
	private static String inputMolecules = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/merged_predictions_washed.sdf";
	private static String inputPredictions = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/merged_predictions_washed.smartCyp.csv";
//	private static String inputMolecules = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/test.sdf";
//	private static String inputPredictions = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/test.csv";	
	private static int k = 3;
	
	public static void main(String[] args) throws Exception{
		Map<Integer, Map<Integer, Double>> scoreList = readPredictions();
		readMolecules(scoreList);
	}

	private static Map<Integer, Map<Integer, Double>> readPredictions() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(inputPredictions));
		scanner.nextLine();
		String line = scanner.nextLine();
		String[] splitLine = null ;
		Map<Integer,Map<Integer,Double>> scoreList = new HashMap<Integer,Map<Integer,Double>>();
		Integer moleculeNumber=0;
		while (scanner.hasNextLine()) {
//			System.out.println("next molecule");
			splitLine = line.split(",");
			String thisMolecule = splitLine[0];
			Map<Integer,Double> scoreMap = new HashMap<Integer,Double>();
			Integer atomNumber = 0;
			while (splitLine[0].equals(thisMolecule) && scanner.hasNextLine()) {
				atomNumber = Integer.parseInt(splitLine[1].split("\\.")[1])-1;
				scoreMap.put(atomNumber, Double.parseDouble(splitLine[3]));
				System.out.println(atomNumber + "\t" + Double.parseDouble(splitLine[3]));
				line = scanner.nextLine();
				splitLine = line.split(",");
			}
			scoreMap.put(atomNumber+1, Double.parseDouble(splitLine[3]));
			System.out.println(atomNumber + "\t" + Double.parseDouble(splitLine[3]));

			scoreList.put(moleculeNumber, scoreMap);
			moleculeNumber++;
		}
		return scoreList;
	}

	private static void readMolecules(Map<Integer, Map<Integer, Double>> scoreList) throws NoSuchAtomException, FileNotFoundException {
		DefaultIteratingChemObjectReader<?> reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(inputMolecules), DefaultChemObjectBuilder.getInstance());	    
		Integer moleculeNumber = 0;
		Double sumOfTheLiftNumerator = 0.0;
		Double sumOfTheLiftDenominator = 0.0;
		Double sumOfPk = 0.0;
		Double sumOfRk = 0.0;
		
		while (reader.hasNext()) {
	    	ArrayList<Integer> soms = new ArrayList<Integer>();
	    	Molecule molecule = (Molecule)reader.next();
//	    	System.out.println(molecule.getProperty(CDKConstants.TITLE));
	    	
	    	//get annotated SOMs
	    	String[] propertyNames = {"PRIMARY_SOM","SECONDARY_SOM","TERTIARY_SOM"};
	    	for (int i=0; i<propertyNames.length; i++) {
	    		if (!(molecule.getProperty(propertyNames[i]) == null)) {
		    		if (molecule.getProperty(propertyNames[i]).toString().contains(" ")) {
				    	String splitSom[] = molecule.getProperty(propertyNames[i]).toString().split(" ");
				    	for (int ii=0; ii<splitSom.length; ii++) {
				    		if (!soms.contains(Integer.parseInt(splitSom[ii]))) {
					    		soms.add(Integer.parseInt(splitSom[ii]));
				    		}
				    	}
		    		} else {
			    		soms.add(Integer.parseInt(molecule.getProperty(propertyNames[i]).toString()));
		    		}
	    		}
	    	}
//	    	System.out.println("SOMS " + soms);

	    	//define atom symmetry
	    	EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner((AtomContainer) molecule);
			int[] symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu((AtomContainer) molecule);
	    	

	    	//build a map of scores
			Map<String[], Double> uniqueMap = new HashMap<String[], Double>();			
			
			ArrayList<Integer> visitedSymmetryAtoms = new ArrayList<Integer>();
			
	    	//add symmetry numbers and scores
	    	System.out.println("moleculeName\tatomNumber\tsymmetryAtomNumber\tsom\tprobability");
	    	for (Integer i=0; i<molecule.getAtomCount(); i++) {
	    		if (!molecule.getAtom(i).getSymbol().equals("H")) {
		    		if (scoreList.get(moleculeNumber).get(i) == null) {
		    			scoreList.get(moleculeNumber).put(i, 999.00);
		    		}
	    			System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + (i+1) + "\t" + symmetryNumbersArray[i+1] + "\t" + soms.contains(i+1) + "\t" + scoreList.get(moleculeNumber).get(i));

		    		if (!visitedSymmetryAtoms.contains(symmetryNumbersArray[i+1])) {
//			    			System.out.println("adding atom " + symmetryNumbersArray[i+1]);
		    			visitedSymmetryAtoms.add(symmetryNumbersArray[i+1]);


		    			if (soms.contains(i+1)) {
		    				Integer tmpInt = symmetryNumbersArray[i+1];
		    				uniqueMap.put(new String[] {i.toString(),tmpInt.toString(),"true"}, scoreList.get(moleculeNumber).get(i));

		    			} else {
		    				Integer tmpInt = symmetryNumbersArray[i+1];
		    				uniqueMap.put(new String[] {i.toString(),tmpInt.toString(),"false"}, scoreList.get(moleculeNumber).get(i));
		    			}
		    		}
	    		}
	    	}

	    	
	    	//sort map by increasing values
    		SortedSet<Entry<String[],Double>> uniqueSorted;
    		uniqueSorted = entriesSortedByIncreasingValues(uniqueMap);

    		
	    	/*
	    	 * Evaluate results
	    	 */
    		System.out.println();
	    	System.out.println("Atoms sorted by decreasing probability");
	    	System.out.println("moleculeName\tatomNumber\tsymmetryAtomNumber\tsom\tprobability");
			Iterator<Entry<String[], Double>> itr = uniqueSorted.iterator();
	    	while (itr.hasNext()) {
	    		Entry<String[], Double> tmp = itr.next();
	    		System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + (Integer.parseInt(tmp.getKey()[0])+1) + "\t" + tmp.getKey()[1] + "\t" + tmp.getKey()[2] + "\t" + tmp.getValue());
	    	}
	    	//int Mp: number of atoms among the top-k ranked positions.
	    	itr = uniqueSorted.iterator();
	    	int Mp = 0;
	        
	        //int OMp: number of SOM atoms among the top-k ranked positions
	        int OMp = 0;
			
	        Entry<String[], Double> uniqueSortedEntry = null;
	        int i = 0;
	        Double lastValid = null;

	    	System.out.println();
	    	System.out.println("Top-k ranked atom positions");
	    	System.out.println("moleculeName\tatomNumber\tsymmetryAtomNumber\tsom\tprobability");
	    	
			//success among the top-k ranked positions
			while (itr.hasNext() && i < k) {
				uniqueSortedEntry = itr.next();
				if (uniqueSortedEntry.getValue() == 0) {
					lastValid = 0.0;
					Mp=k;
					break;
				}
				System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + uniqueSortedEntry.getKey()[0] + "\t" + (Integer.parseInt(uniqueSortedEntry.getKey()[1])+1) + "\t" + uniqueSortedEntry.getKey()[2] + "\t" + uniqueSortedEntry.getValue());
				if (uniqueSortedEntry.getKey()[2].equals("true")) {
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
						if (uniqueSortedEntry.getKey()[2].equals("true")) {
							OMp++;
						}
						System.out.println("#" + molecule.getProperty(CDKConstants.TITLE) + "\t" + uniqueSortedEntry.getKey()[0] + "\t" + (Integer.parseInt(uniqueSortedEntry.getKey()[1])+1) + "\t" + uniqueSortedEntry.getKey()[2] + "\t" + uniqueSortedEntry.getValue());
		
						if (itr.hasNext()) {
							uniqueSortedEntry = itr.next();
						} else break;
					}
				}					
			}
			
	        //int Mr: number of unique atoms of this molecule        
	        int Mr = uniqueMap.size();
	        
	        //int OMr: number of SOM atoms of this molecule
	        int OMr = 0;
	        //iterate over Map
	        for (Entry<String[], Double> entry : uniqueMap.entrySet()) {
	        	if (!entry.getKey()[2].equals("false")) {
	            	OMr++;
	        	}
	        }
			
	        //int NOM: number of non-SOM atoms (NOMr = Mr - OMr)
	        int NOMr = Mr - OMr;
			
	        //int NOMp: number of non-SOM atoms
	        int NOMp = Mp - OMp;
			
	    	System.out.println();
	    	
	        /*
	         * calculate prediction accuracy
	         */
	        Double Pk = null;
	        if (NOMp >= k) {
	            Pk = ( 1- ((MathUtils.binomialCoefficientDouble(NOMp, k)) / (MathUtils.binomialCoefficient(Mp, k))));
	            System.out.printf("BENCHMARKS\tk=" + k + "\tPk\t" + "%.2f" + " = 1 - ( " + NOMp + " choose " + k + " ) / ( " + Mp + " choose " + k + " )\t", Pk);
	            System.out.println();
	        } else {
	        	Pk = 1.0;
	        	System.out.printf("BENCHMARKS\tk=" + k + "\tPk\t" + "%.2f" + " = 1 - ( " + NOMp + " choose " + k + " ) / ( " + Mp + " choose " + k + " )\t", Pk);
	        	System.out.println();
	        }
	        
	        /*
	         * calculate statistical difficulty of randomly predicting an observed SOM on that substrate within k rank-positions
	         */
	        Double Rk = null;
	        if (NOMr >= k) {
	            Rk = (double) ( 1- ((MathUtils.binomialCoefficientDouble(NOMr, k)) / (MathUtils.binomialCoefficient(Mr, k))));
	            System.out.printf("BENCHMARKS\tk=" + k + "\tRk\t" + "%.2f" + " = 1 - ( " + NOMr + " choose " + k + " ) / ( " + Mr + " choose " + k + " )\t", Rk);            
	            System.out.println();
	        } else {
	            Rk = 1.0;
	            System.out.printf("BENCHMARKS\tk=" + k + "\tRk\t" + "%.2f" + " = 1 - ( " + NOMr + " choose " + k + " ) / ( " + Mr + " choose " + k + " )\t", Rk);            
	            System.out.println();
	        }
	        
	        sumOfRk = sumOfRk + Rk;
	    	sumOfPk = sumOfPk + Pk;
	    	sumOfTheLiftNumerator   = sumOfTheLiftNumerator + (Pk * (1.00/ Rk));
	    	sumOfTheLiftDenominator = sumOfTheLiftDenominator + (1.00 / Rk);
	    	moleculeNumber++;

//		    	System.out.println(pKCounter + "\t" + Pk + "\t" + Rk + "\t" + (Pk * (1.00 / Rk)));
	    	System.out.println();
	    }
		
		System.out.println("##############RESULTS for Zaretzki dataset k=" + k+"##############");
		Double[] results = new Double[4];
		System.out.println("RESULT for number of molecules " + moleculeNumber);
		results[0] = (double) moleculeNumber;
		System.out.println("RESULT: standard accuracy " + sumOfPk/(double) moleculeNumber);
		results[1] = (sumOfPk/(double) moleculeNumber);
		System.out.println("RESULT: lift accuracy " + (sumOfTheLiftNumerator/sumOfTheLiftDenominator));
		results[2] = sumOfTheLiftNumerator/sumOfTheLiftDenominator;
		System.out.println("RESULT: random model " + sumOfRk/moleculeNumber);
		results[3] = sumOfRk/moleculeNumber;
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
}


