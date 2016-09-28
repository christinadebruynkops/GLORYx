package fame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.util.Logger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.normalize.SMSDNormalizer;
import org.openscience.cdk.qsar.descriptors.atomic.EffectiveAtomPolarizabilityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialSigmaChargeDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialTChargeMMFF94Descriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PiElectronegativityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.SigmaElectronegativityDescriptor;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.jmol.app.jmolpanel.AppConsole;

import fame.weka.classifiers.Classifier;
import fame.weka.core.Instance;
import fame.weka.core.Instances;
import fame.weka.core.converters.ConverterUtils.DataSource;


public class Fame {
	private String input;
	private String model;
	private Boolean visualize = false;
	private static Boolean minimize = false;

	
	
	public static void main(String[] args) throws Exception{
        System.out.println("FAst MEtabolizer (FAME): A Rapid and Accurate Predictor of Xenobiotic Metabolism");
        System.out.println("Johannes Kirchmair, jk528@cam.ac.uk");
        System.out.println();
        System.out.println("Make sure to use 3D minimized molecular structures with explicit hydrogens as input");
        
        args = new String[] {"-i", "/Users/jkirchmair/Desktop/test.sdf", 
        		"-m", "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/005weka/human/019.model", "-vm"};

//        args = new String[] {"-i", "/Users/jkirchmair/UNI/CAM/metabolismPrediction/manuscript/figures/diazepam.sdf", 
//        		"-m", "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/006results/release/models/019.model", 
//        		"-vm"};

        
        Fame fame = new Fame();
        fame.parseArgs(args);
        fame.process();
	}
	

	private static void printUsage() {
        System.out.println("Make sure to use 3D minimized molecular structures with explicit hydrogens as input");
        
		System.out.println("Usage:");
		System.out.println("-i <input file>    path to input file (sd file)");
		System.out.println("-m <input file>    path to model file");
		System.out.println("-v                 visualize results using Jmol (optional)");
		System.out.println("-vm                visualize results after minimizing conformation (optional)");
		System.out.println("Note that the visualizer, Jmol, will show the first molecule on startup.");
		System.out.println("You can select other molecules (\"models\") from the context menu, which is called with");
		System.out.println("right click from within the 3D viewer.");
		System.out.println();
		System.out.println("Example:");
		System.out.println("java -Xmx2g -jar fame.jar -i input.sdf -m global_both.model -v");
	}
	

	/**
	 * Reads input args
	 * @param args
	 * @throws Exception
	 */
	private void parseArgs(String[] args) throws Exception {
		if (args.length == 0) {
	        printUsage();
	        System.exit(1);
	    }
	    
	    Iterator<String> itr = Arrays.asList(args).iterator();
	
	    while (itr.hasNext()) {
	    	String arg = itr.next();
	    	if (arg.equals("-i")) {
	            if (!itr.hasNext()) {
	                throw new IllegalArgumentException("Missing required value for argument \"-i\"");
	            }
	            input = itr.next();
	    	} else if (arg.equals("-m")) {
	            if (!itr.hasNext()) {
	                throw new IllegalArgumentException("Missing required value for argument \"-m\"");
	            }
	            model = itr.next();
	    	} else if (arg.equals("-v")) {
	    		visualize = true;
	    	} else if (arg.equals("-vm")) {
	    		visualize = true;
	    		minimize = true;
	    	}
	    }
	    
	    if (input==null) {
	        throw new IllegalArgumentException("Mandatory argument \"-i\" not defined");
	    }
	    
	    if (model==null) {
	        throw new IllegalArgumentException("Mandatory argument \"-m\" not defined");
	    }
	}

	
	private void process() throws Exception {
		File infile = new File(input);
		
	    if (!infile.exists()) {
		    System.err.println("File not found: " + input);
		    System.exit(1);
		}
	    
		//define relevant Sybyl atom types
		ArrayList<String> sybylAtomTypes = new ArrayList<String>();
		sybylAtomTypes.add("C.ar");
		sybylAtomTypes.add("C.2");
		sybylAtomTypes.add("C.3");
		sybylAtomTypes.add("O.3");
		sybylAtomTypes.add("O.2");
		sybylAtomTypes.add("N.ar");
		sybylAtomTypes.add("N.3");
		sybylAtomTypes.add("Cl");
		sybylAtomTypes.add("P.3");
		sybylAtomTypes.add("N.am");
		sybylAtomTypes.add("C.1");
		sybylAtomTypes.add("N.1");
		sybylAtomTypes.add("F");
		sybylAtomTypes.add("Any");
		sybylAtomTypes.add("S.O2");
		sybylAtomTypes.add("S.3");
		sybylAtomTypes.add("N.2");
		sybylAtomTypes.add("Br");
		sybylAtomTypes.add("S.O");
		sybylAtomTypes.add("N.pl3");
		sybylAtomTypes.add("C.cat");
		sybylAtomTypes.add("S.2");
		sybylAtomTypes.add("Sn");
		sybylAtomTypes.add("N.4");
		sybylAtomTypes.add("I");
		sybylAtomTypes.add("Se");
		sybylAtomTypes.add("O.co2");
		sybylAtomTypes.add("Si");
		sybylAtomTypes.add("null");
	
		//read in all molecules and calculate descriptors
		DefaultIteratingChemObjectReader<?> reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(input), DefaultChemObjectBuilder.getInstance());
		Map<String, ArrayList<String>> descriptors = new LinkedHashMap<String,ArrayList<String>>();	//to store all descriptor values for all molecules
		Integer moleculeCounter = 0;
	    while (reader.hasNext()) {
	    	Molecule molecule = (Molecule)reader.next();
         	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);         	
         	CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
        	adder.addImplicitHydrogens(molecule);
        	AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
        	SMSDNormalizer.aromatizeMolecule(molecule);	//prepare molecules for correct atom type assignment
        	String title = CDKConstants.TITLE.toString() + "_" + moleculeCounter.toString();
        	descriptors.put(title, calculateDescriptors(molecule));
        	moleculeCounter++;
	    }
		writeArff(input, sybylAtomTypes, descriptors);
		predictMolecule(input, model, visualize, descriptors);
    }


	/**
	 * Calculates and returns all descriptor values
	 * @param sybylAtomTypes
	 * @param molecule
	 * @return
	 */
	static ArrayList<String> calculateDescriptors(Molecule molecule) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());
			EffectiveAtomPolarizabilityDescriptor effectiveAtomPolarizabilityDescriptor = new EffectiveAtomPolarizabilityDescriptor();
			PartialSigmaChargeDescriptor partialSigmaChargeDescriptor = new PartialSigmaChargeDescriptor();
			PartialTChargeMMFF94Descriptor partialTChargeMMFF94Descriptor = new PartialTChargeMMFF94Descriptor();
			PiElectronegativityDescriptor piElectronegativityDescriptor = new PiElectronegativityDescriptor();
			SigmaElectronegativityDescriptor sigmaElectronegativityDescriptor = new SigmaElectronegativityDescriptor();

			System.out.println();
	        System.out.println("************** Calculating descriptors for molecule " + (molecule.getProperty(CDKConstants.TITLE) + " **************"));
			
			//calculate SPAN descriptor and derive Sybyl atom types
			int[][] adjMatrix = AdjacencyMatrix.getMatrix(molecule);	// calculate the maximum topology distance
			int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjMatrix);
			double longestMaxTopDistInMolecule = 0;
			double currentMaxTopDist = 0;
			for(int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
				IAtom iAtom = molecule.getAtom(atomNr);
	            IAtomType iAtomType = atm.findMatchingAtomType(molecule,molecule.getAtom(atomNr)); //derive Sybyl atom types
	            if (iAtomType != null) {
	            	iAtom.setProperty("SybylAtomType", iAtomType.getAtomTypeName());
	            }
				for(int y = 0; y < molecule.getAtomCount(); y++){
					currentMaxTopDist =  minTopDistMatrix[atomNr][y];
					if(currentMaxTopDist > longestMaxTopDistInMolecule) {
						longestMaxTopDistInMolecule = currentMaxTopDist;
					}
				}
			}			
			
			for(int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ){		
				String data = "";
				IAtom iAtom = molecule.getAtom(atomNr);
				if (!iAtom.getSymbol().equals("H")) {
					System.out.println("Calculating atomNr " + atomNr);
					
					//calculate chemical descriptors
					data = (molecule.getProperty(CDKConstants.TITLE)) + "," + 
						iAtom.getSymbol() + "."+ 
						(atomNr+1) + ","+
						iAtom.getProperty("SybylAtomType") + "," +
						(effectiveAtomPolarizabilityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",") +
						(partialSigmaChargeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",") + 
						(partialTChargeMMFF94Descriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",") +
						(piElectronegativityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",") + 
						(sigmaElectronegativityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",") + 
						longestMaxTopDistInMolecule  + "," +
						"?"; //class label
									
		            results.add(data);
				}		
			}
		}
		
		catch (OutOfMemoryError e) {
			//can be thrown by salts
			System.out.println("Error: OutOfMemoryError: " + molecule.getProperty(CDKConstants.TITLE));
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//can be thrown by very large molecules
			System.out.println("Error: ArrayIndexOutOfBoundsException: " + molecule.getProperty(CDKConstants.TITLE));
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Sort set by decreasing values
	 * @param map
	 * @return
	 */
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByDecreasingValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1; //Preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
	
	/**
	 * Generates a Weka .arff file
	 * @param filename
	 * @param sybylAtomTypes
	 * @param results
	 * @throws IOException
	 */
	private static void writeArff(String filename, ArrayList<String> sybylAtomTypes, Map<String, ArrayList<String>> descriptors) throws IOException {
        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(filename.replaceAll("\\.sd.*",".arff"))));
        outfile.println("@relation prediction");
        outfile.println("");
        outfile.print("@attribute atomType {");
        Iterator<String> itr = sybylAtomTypes.iterator();
        while (itr.hasNext()) {
        	outfile.print(itr.next());
        	if (itr.hasNext()) {
            	outfile.print(",");
        	}
        }

        outfile.println("}");

        outfile.println("@attribute effectiveAtomPolarizability numeric");
        outfile.println("@attribute partialSigmaCharge numeric");
        outfile.println("@attribute partialTChargeMMFF94 numeric");
        outfile.println("@attribute piElectronegativity numeric");
        outfile.println("@attribute sigmaElectronegativity numeric");
        outfile.println("@attribute longestMaxTopDistInMolecule numeric");
        outfile.println("@attribute isSom {true,false}");
        outfile.println("");
        outfile.println("@data");

        Set<String> set = descriptors.keySet();
        for (String key : set) {
    		itr = descriptors.get(key).iterator();
    		while (itr.hasNext()) {
    			String[] splitLine = itr.next().split(",");
    			for (int i=2; i<splitLine.length-1; i++) {
        			outfile.print(splitLine[i] + ",");
    			}
    			outfile.println(splitLine[splitLine.length-1]);
    		}
        }
        outfile.close();
	}
	
	/**
	 * Runs Weka models and writes out results
	 * @param input
	 * @param visualize
	 * @param descriptors
	 * @throws Exception
	 */
	private static void predictMolecule(String input, String model, Boolean visualize, Map<String, ArrayList<String>> descriptors) throws Exception {
    	System.out.println();
        System.out.println("************** Initiating the prediction **************");
        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(input.replaceAll("\\.sd.*",".csv").toString())));
        outfile.println("molecule,atom,atomType,effectiveAtomPolarizability," +
        		"partialSigmaCharge,partialTChargeMMFF94," +
        		"piElectronegativity,sigmaElectronegativityDescriptor," +
        		"longestMaxTopDistInMatrixRow,classLabel,FAME");
        
    	DataSource source = new DataSource(input.replaceAll("\\.sd.*",".arff").toString());
    	Instances data = source.getDataSet();
    	data.setClassIndex(data.numAttributes() - 1);
    	
    	Classifier classifier = (Classifier) fame.weka.core.SerializationHelper.read(model);
        
        DecimalFormat df = new DecimalFormat("0.000");
        
        Set<String> set = descriptors.keySet();
        ArrayList<SortedSet<Entry<String, Double>>> sortedList = new ArrayList<SortedSet<Entry<String, Double>>>();
        int i=0;
        for (String key : set) {
            Map<String,Double> unsorted = new HashMap<String,Double>();
    		Iterator<String> itr = descriptors.get(key).iterator();
    		while (itr.hasNext()) {
                Instance instance = data.instance(i);
                double[] pred = classifier.distributionForInstance(instance);
                String[] splitString = itr.next().split(",");
                String line = splitString[0] + "," +
                	splitString[1] + "," +
                	splitString[2] + "," +
                	df.format(Double.parseDouble(splitString[3])) + "," +
                	df.format(Double.parseDouble(splitString[4])) + "," +
                	df.format(Double.parseDouble(splitString[5])) + "," +
                	df.format(Double.parseDouble(splitString[6])) + "," +
                	df.format(Double.parseDouble(splitString[7])) + "," +
                	splitString[8] + "," +
                	splitString[9] + "," +
                	df.format(pred[0]);
                unsorted.put(splitString[1], pred[0]);
                outfile.println(line);
            	i++;
    		}
        	SortedSet<Entry<String, Double>> sorted = entriesSortedByDecreasingValues(unsorted);
        	sortedList.add(sorted);
        }
        System.out.println("************** Prediction finished **************");
        if (visualize) {
            System.out.println("************** Starting Jmol **************");
            visualizeResult(input, sortedList);
        }
        outfile.close();
	}


	/**
	 * Runs Jmol for visualizing results
	 * @param input
	 * @param sortedList
	 * @throws InterruptedException 
	 */
	private static void visualizeResult(String input, ArrayList<SortedSet<Entry<String, Double>>> sortedList) throws InterruptedException {
		JFrame frame = new JFrame("FAME Visualizer");
		frame.addWindowListener(new ApplicationCloser());
		frame.setSize(410, 700);
		Container contentPane = frame.getContentPane();
		JmolPanel jmolPanel = new JmolPanel();
		jmolPanel.setPreferredSize(new Dimension(400, 400));

		// main panel: Jmol panel on top
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jmolPanel);

		// main panel: console panel on bottom
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		panel2.setPreferredSize(new Dimension(400, 300));
		AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
				"History State Clear");

		jmolPanel.viewer.setJmolCallbackListener(console);

		panel.add("South", panel2);

		contentPane.add(panel);
		frame.setVisible(true);

		String strError = jmolPanel.viewer.openFile(input);

		Iterator<SortedSet<Entry<String, Double>>> moleculesItr = sortedList.iterator();
		jmolPanel.viewer.evalString("wireframe 0.1; spacefill 0; font label 30 bold");
		jmolPanel.viewer.evalString("set useMinimizationThread false; color background white");

        DecimalFormat df = new DecimalFormat("0.00");
				
		if (strError == null) {
	        int modelNumber = 1;
			while (moleculesItr.hasNext()) {
				Iterator<Entry<String, Double>> atomsItr = moleculesItr.next().iterator();
				int atomNumber=0;
				Double lastScore = null;
				while (atomsItr.hasNext()) {
					Entry<String, Double> entry = atomsItr.next();
					if (!entry.getValue().equals(lastScore) && !(lastScore == null)) {
						atomNumber++;						
					}
					
					if (atomNumber==0) {
						jmolPanel.viewer.evalString("select " + entry.getKey().replace(".", "") + "/" + modelNumber + ";" + "label " + df.format(Math.abs(entry.getValue())) + ";" + "wireframe 0.1; spacefill 230;");
					} else if (atomNumber==1) {
						jmolPanel.viewer.evalString("select " + entry.getKey().replace(".", "") + "/" + modelNumber + ";" + "label " + df.format(Math.abs(entry.getValue())) + ";" + "wireframe 0.1; spacefill 180;");
					} else if (atomNumber==2) {
						jmolPanel.viewer.evalString("select " + entry.getKey().replace(".", "") + "/" + modelNumber + ";" + "label " + df.format(Math.abs(entry.getValue())) + ";" + "wireframe 0.1; spacefill 120;");				
					} else {
						jmolPanel.viewer.evalString("select " + entry.getKey().replace(".", "") + "/" + modelNumber + ";" + "label " + df.format(Math.abs(entry.getValue())) + "; ");
					}

					lastScore =  entry.getValue();
				}
				if (minimize) {
					jmolPanel.viewer.evalString("model " + modelNumber + "; minimize");
				}
				modelNumber++;
			}
			jmolPanel.viewer.evalString("model 1");
			jmolPanel.viewer.evalString("center */1");
			jmolPanel.viewer.evalString("select *; color label black;");
		}
		else
			Logger.error(strError);
	}

	static class ApplicationCloser extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

	static class JmolPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JmolViewer viewer;
		private final Dimension currentSize = new Dimension();
		JmolPanel() {
			viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(),
					null, null, null, null, null);
		}

		@Override
		public void paint(Graphics g) {
			getSize(currentSize);
			viewer.renderScreenImage(g, currentSize.width, currentSize.height);
		}
	}
}