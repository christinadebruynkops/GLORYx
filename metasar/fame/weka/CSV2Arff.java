package fame.weka;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
 
public class CSV2Arff {
	static void convertCsvToArff(String wDir, String species, String phase) throws IOException {
		File folder = new File(wDir + "004metaPrintPredictions/" + species);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isFile() && file.getName().endsWith(".csv")) {
		    	System.out.println("Loading file " + file);
			    // load CSV
			    CSVLoader loader = new CSVLoader();
			    loader.setSource(new File(file.toString()));
			    Instances data = loader.getDataSet();
			    data.numAttributes();
			    for (String tmp : (new String[] {
			    		"cdk:AtomAtomMapping","Atom","Ranking","Score","Energy","2D6ranking","2D6score",
			    		"N+Dist","2Cranking","2Cscore","COODist","MoleculeName","SymmetryAtomNumber","SOMcombined",
			    		"SOM","ReactionTypes","ReactionTypesCombined","set","smiles","atomNumber","atomtype","normalisedRatio",
			    		"reactionCentreCount","substrateCount","non-normalisedRatio",
			    		
			    		"RelativeSpan","Span2End","2DSASA", //SmartCyp
			    		
//			    		"partialTChargeMMFF94", //my descriptors
//			    		"partialSigmaCharge",
//			    		"piElectronegativity",
//			    		"sigmaElectronegativity",
//			    		"AtomType",
//			    		"effectiveAtomPolarizability",
//			    		"highestMaxTopDistInMatrixRow",

//			    		"atomDegree",
//			    		"atomHybridization",
//			    		"atomHybridizationVSEPR",
//			    		"atomValence",
			    		"iPAtomicHOSE",
			    		"protonAffinityHOSE",
//			    		"stabilizationPlusCharge",
//			    		"relSPAN",
//			    		"diffSPAN",
//			    		"longestMaxTopDistInMolecule",
			    		
			    		"Mol_bonds2end",  //Patrik's descriptors
			    		"Mol_rotablebonds",
			    		"Mol_AtomCount",
			    		"Mol_TPSA",
			    		"Mol_TPSAperAtom",
			    		"Mol_Volume",
			    		"Mol_HAcount",
			    		"Mol_HDcount",
			    		"Mol_PIsystemSize",
			    		"Branch_bonds2end",
			    		"Branch_rotablebonds",
			    		"Branch_AtomCount",
			    		"Branch_TPSA",
			    		"Branch_TPSAperAtom",
			    		"Branch_Volume",
			    		"Branch_HAcount",
			    		"Branch_HDcount",
			    		"Branch_PIsystemSize"
			    		})) {
			    	data.deleteAttributeAt(data.attribute(tmp).index());
			    }
			    
			    if (phase.equals("Both")) {
			    	data.deleteAttributeAt(data.attribute("phaseI").index());
			    	data.deleteAttributeAt(data.attribute("phaseII").index());
			    }
			    if (phase.equals("PhaseI")) {
			    	data.deleteAttributeAt(data.attribute("isSom").index());
			    	data.deleteAttributeAt(data.attribute("phaseII").index());
			    }
			    
			    if (phase.equals("PhaseII")) {
			    	data.deleteAttributeAt(data.attribute("isSom").index());
			    	data.deleteAttributeAt(data.attribute("phaseI").index());
			    }
			    
			    // save ARFF
			    ArffSaver saver = new ArffSaver();
			    saver.setInstances(data);
			    saver.setFile(new File(file.toString().replace(".csv", ".xxx")));
			    saver.setDestination(new File(file.toString().replace(".csv", ".xxx")));
			    saver.writeBatch();
			    
				Scanner scanner = new Scanner(new File(file.toString().replace(".csv", ".xxx")));
//				File output = new File(file.toString().replace(".csv", (phase + "FpRemoved.arff")));
				File output = new File(file.toString().replace(".csv", (phase + ".arff")));
				FileOutputStream out = new FileOutputStream(output, false);
				PrintStream p = new PrintStream(out);
		    	while (scanner.hasNext()) {
		    		String line = scanner.nextLine();
		    		if (line.startsWith("@attribute AtomType")) {
		    			line = "@attribute AtomType {C.ar,C.2,C.3,O.3,O.2,N.ar,N.3,Cl,P.3,N.am,C.1,N.1,F,Any,S.O2,S.3,N.2,Br,S.O,N.pl3,C.cat,S.2,Sn,N.4,I,Se,O.co2,Si,null}";
		    		} else if (line.startsWith("@attribute isSom")) {
		    			line = "@attribute isSom {true,false}";
		    		} else if (line.startsWith("@attribute phaseI ")) {
		    			line = "@attribute phaseI {true,false}";
		    		} else if (line.startsWith("@attribute phaseII ")) {
		    			line = "@attribute phaseII {true,false}";		    			
		    		}
		    		p.println(line);
		    	}
		    	p.close();
		    }
		}

	}
	
	public static void main(String[] args) throws Exception {
//		String[] species = {"all","human","rat","dog"};
		String[] species = {"human"};
//		String[] phase = {"Both","PhaseI","PhaseII"};
		String[] phase = {"Both"};
		String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
		for (int i=0; i<species.length; i++) {
			for (int ii=0; ii<phase.length; ii++) {
				convertCsvToArff(wDir, species[i], phase[ii]);				
			}
		}
	}

}