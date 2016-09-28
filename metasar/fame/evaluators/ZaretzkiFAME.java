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
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

public class ZaretzkiFAME {
	private static String inputMolecules = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/merged_predictions_washed.sdf";
	private static String inputPredictions = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/merged_predictions_washed.fame.csv";
//	private static String inputMolecules = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/test.sdf";
//	private static String inputPredictions = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/somDatasets/003zaretzki2012rma/002moe/test.csv";	
	private static int k = 2;
	
	public static void main(String[] args) throws Exception{
		ArrayList<Double> scores = readPredictions();
		readMolecules(scores);

	}

	private static ArrayList<Double> readPredictions() throws FileNotFoundException {
		ArrayList<Double> scores = new ArrayList<Double>();
		Scanner scanner = new Scanner(new File(inputPredictions));
		scanner.nextLine();
		String line = scanner.nextLine();
		String[] splitLine = null ;
		while (scanner.hasNextLine()) {
//			System.out.println("next molecule");
			splitLine = line.split(",");
			String thisMolecule = splitLine[0];
			while (splitLine[0].equals(thisMolecule) && scanner.hasNextLine()) {
				scores.add(Double.parseDouble(splitLine[splitLine.length-1]));
//				System.out.println(splitLine[0] + "\t" + splitLine[splitLine.length-1]);
				line = scanner.nextLine();
				splitLine = line.split(",");
			}
		}
		scores.add(Double.parseDouble(splitLine[splitLine.length-1]));
		return scores;
	}

	private static void readMolecules(ArrayList<Double> scores) throws NoSuchAtomException, FileNotFoundException {
		DefaultIteratingChemObjectReader<?> reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(inputMolecules), DefaultChemObjectBuilder.getInstance());	    
		Iterator<Double> score = scores.iterator();
		
		Integer numberOfMolecules = 0;
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
	    	for (int i=0; i<molecule.getAtomCount(); i++) {
	    		if (!molecule.getAtom(i).getSymbol().equals("H")) {
		    		Double thisScore = score.next();
	    			System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + symmetryNumbersArray[i+1] + "\t" + thisScore);

		    		if (!visitedSymmetryAtoms.contains(symmetryNumbersArray[i+1])) {
//		    			System.out.println("adding atom " + symmetryNumbersArray[i+1]);
		    			visitedSymmetryAtoms.add(symmetryNumbersArray[i+1]);


		    			if (soms.contains(i+1)) {
		    				Integer tmpInt = symmetryNumbersArray[i+1];
		    				uniqueMap.put(new String[] {tmpInt.toString(),"true"}, thisScore);

		    			} else {
		    				Integer tmpInt = symmetryNumbersArray[i+1];
		    				uniqueMap.put(new String[] {tmpInt.toString(),"false"}, thisScore);
		    			}
		    		}
	    		}
	    	}

	    	
	    	//sort map by decreasing values
    		SortedSet<Entry<String[],Double>> uniqueSorted;
    		uniqueSorted = entriesSortedByDecreasingValues(uniqueMap);

    		
	    	/*
	    	 * Evaluate results
	    	 */
    		System.out.println();
	    	System.out.println("Atoms sorted by decreasing probability");
	    	System.out.println("moleculeName\tsymmetryAtomNumber\tsom\tprobability");
			Iterator<Entry<String[], Double>> itr = uniqueSorted.iterator();
	    	while (itr.hasNext()) {
	    		Entry<String[], Double> tmp = itr.next();
	    		System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + tmp.getKey()[0] + "\t" + tmp.getKey()[1] + "\t" + tmp.getValue());
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
	    	System.out.println("moleculeName\tsymmetryAtomNumber\tsom\tprobability\treactionType");
	    	
			//success among the top-k ranked positions
			while (itr.hasNext() && i < k) {
				uniqueSortedEntry = itr.next();
				if (uniqueSortedEntry.getValue() == 0) {
					lastValid = 0.0;
					Mp=k;
					break;
				}
				System.out.println(molecule.getProperty(CDKConstants.TITLE) + "\t" + uniqueSortedEntry.getKey()[0] + "\t" + uniqueSortedEntry.getKey()[1] + "\t" + uniqueSortedEntry.getValue());
				if (uniqueSortedEntry.getKey()[1].equals("true")) {
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
//								System.out.println(tmp.getKey()[reactionTypesCombined] + "\t" + tmp.getKey()[isSom]);
						if (uniqueSortedEntry.getKey()[1].equals("true")) {
							OMp++;
						}
						System.out.println("#" + molecule.getProperty(CDKConstants.TITLE) + "\t" + uniqueSortedEntry.getKey()[0] + "\t" + uniqueSortedEntry.getKey()[1] + "\t" + uniqueSortedEntry.getValue());
		
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
	        	if (!entry.getKey()[1].equals("false")) {
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
	    	numberOfMolecules++;

//		    	System.out.println(pKCounter + "\t" + Pk + "\t" + Rk + "\t" + (Pk * (1.00 / Rk)));
	    	System.out.println();
	    }
		
		System.out.println("##############RESULTS for Zaretzki dataset k=" + k+"##############");
		Double[] results = new Double[4];
		System.out.println("RESULT for number of molecules " + numberOfMolecules);
		results[0] = (double) numberOfMolecules;
		System.out.println("RESULT: standard accuracy " + sumOfPk/(double) numberOfMolecules);
		results[1] = (sumOfPk/(double) numberOfMolecules);
		System.out.println("RESULT: lift accuracy " + (sumOfTheLiftNumerator/sumOfTheLiftDenominator));
		results[2] = sumOfTheLiftNumerator/sumOfTheLiftDenominator;
		System.out.println("RESULT: random model " + sumOfRk/numberOfMolecules);
		results[3] = sumOfRk/numberOfMolecules;
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
}


