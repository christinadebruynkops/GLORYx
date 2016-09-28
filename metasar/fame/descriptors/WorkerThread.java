//Span descriptor calculation taken from SmartCyp

package fame.descriptors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.normalize.SMSDNormalizer;
import org.openscience.cdk.qsar.descriptors.atomic.AtomDegreeDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.AtomHybridizationDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.AtomHybridizationVSEPRDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.AtomValenceDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.EffectiveAtomPolarizabilityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.IPAtomicHOSEDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialSigmaChargeDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialTChargeMMFF94Descriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PiElectronegativityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.ProtonAffinityHOSEDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.SigmaElectronegativityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.StabilizationPlusChargeDescriptor;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class WorkerThread implements Runnable {
    private Molecule molecule;

    public WorkerThread(Molecule molecule) throws IOException, ClassNotFoundException{
        this.molecule=molecule;
    }
    
    @Override
    public void run() {
		try {
			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				throw new Exception("Error: salt: " + molecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
			}

        	//this code is not used. it was initially written to add charges
//	            Constructor<AtomTypeCharges> constructor = AtomTypeCharges.class.getDeclaredConstructor(new Class[0]);
//	         	constructor.setAccessible(true);
//	         	AtomTypeCharges atomTypeCharges = constructor.newInstance(new Object[0]);
//	         	atomTypeCharges.calculateCharges(molecule);
//				for(int x = 0; x < molecule.getAtomCount(); x++){
//					System.out.println(molecule.getAtom(x).getCharge());
//				}

        	SMSDNormalizer.aromatizeMolecule(molecule); //aromatize molecule; required for Sybyl atom type determination
         	IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());  //determine Sybyl atom type
        	
         	//this is not used because I am starting from molecules with explicit hydrogens (as annotated by MOE)
        	//add implicit hydrogens and convert to explicit hydrogen
//	         	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);         	
//	         	CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
//	        	adder.addImplicitHydrogens(molecule);
//	        	AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
         	
	        System.out.println("************** Molecule " + (molecule.getProperty("RXN:VARIATION(1):MDLNUMBER") + " **************"));

			int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(molecule);
			// calculate the maximum topology distance
			// takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
			int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);

			// find the longest Path of all, "longestMaxTopDistInMolecule"
			double longestMaxTopDistInMolecule = 0;
			double currentMaxTopDist = 0;
			int heavyAtomCount = 0;
			for(int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
				IAtom iAtom = molecule.getAtom(atomNr);
				//determine Sybyl atom types
	            IAtomType iAtomType = atm.findMatchingAtomType(molecule,molecule.getAtom(atomNr));
	            if (iAtomType != null) {
	            	iAtom.setProperty("SybylAtomType", iAtomType.getAtomTypeName());
//		            	System.out.println(iAtom.getProperty("SybylAtomType"));
		            if (!iAtom.getSymbol().equals("H")) {
		            	heavyAtomCount++;
		            }
	            }
				for(int i = 0; i < molecule.getAtomCount(); i++){
					currentMaxTopDist =  minTopDistMatrix[atomNr][i];
//					System.out.println(longestMaxTopDistInMolecule + "\t" + currentMaxTopDist);

					if(currentMaxTopDist > longestMaxTopDistInMolecule) {
						longestMaxTopDistInMolecule = currentMaxTopDist;
					}
				}
			}
			
			//check if molecule too large
        	if (heavyAtomCount > 100) {
				throw new Exception("Error: too large: " + molecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
    	    } 

	        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(molecule.getProperty("RXN:VARIATION(1):MDLNUMBER").toString() + ".csv")));
			
			outfile.print("Molecule,Atom,AtomType,atomDegree,atomHybridization,atomHybridizationVSEPR,atomValence,effectiveAtomPolarizability," +
					"iPAtomicHOSE,partialSigmaCharge,partialTChargeMMFF94,piElectronegativity,protonAffinityHOSE,sigmaElectronegativity," +
					"stabilizationPlusCharge,relSPAN,diffSPAN,highestMaxTopDistInMatrixRow,longestMaxTopDistInMolecule");
			outfile.println();

			AtomDegreeDescriptor atomDegreeDescriptor = new AtomDegreeDescriptor();
			AtomHybridizationDescriptor atomHybridizationDescriptor = new AtomHybridizationDescriptor();
			AtomHybridizationVSEPRDescriptor atomHybridizationVSEPRDescriptor = new AtomHybridizationVSEPRDescriptor();
			AtomValenceDescriptor atomValenceDescriptor = new AtomValenceDescriptor();
			EffectiveAtomPolarizabilityDescriptor effectiveAtomPolarizabilityDescriptor = new EffectiveAtomPolarizabilityDescriptor();
			IPAtomicHOSEDescriptor iPAtomicHOSEDescriptor = new IPAtomicHOSEDescriptor();
			PartialSigmaChargeDescriptor partialSigmaChargeDescriptor = new PartialSigmaChargeDescriptor();
			PartialTChargeMMFF94Descriptor partialTChargeMMFF94Descriptor = new PartialTChargeMMFF94Descriptor();
			PiElectronegativityDescriptor piElectronegativityDescriptor = new PiElectronegativityDescriptor();
			ProtonAffinityHOSEDescriptor protonAffinityHOSEDescriptor = new ProtonAffinityHOSEDescriptor();
			SigmaElectronegativityDescriptor sigmaElectronegativityDescriptor = new SigmaElectronegativityDescriptor();
			StabilizationPlusChargeDescriptor stabilizationPlusChargeDescriptor = new StabilizationPlusChargeDescriptor();

			
			for(int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ){		
				String result = "";
				IAtom iAtom = molecule.getAtom(atomNr);
				//determine Sybyl atom types
				if (!iAtom.getSymbol().equals("H")) {
//					System.out.println("AtomNr " + atomNr);

					result = result + ((molecule.getProperty("RXN:VARIATION(1):MDLNUMBER")) + "," + iAtom.getSymbol() + "."+ (atomNr+1) + ",");
					result = result + (iAtom.getProperty("SybylAtomType") + ",");
					result = result + (atomDegreeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (atomHybridizationDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (atomHybridizationVSEPRDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (atomValenceDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (effectiveAtomPolarizabilityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (iPAtomicHOSEDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (partialSigmaChargeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (partialTChargeMMFF94Descriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (piElectronegativityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (protonAffinityHOSEDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
//							result = result + (protonTotalPartialChargeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");	//produces several results (NAN)
					result = result + (sigmaElectronegativityDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
					result = result + (stabilizationPlusChargeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() +",");
											
					//take too long to calculate
//						result = iPAtomicLearningDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString();
//						result = partialPiChargeDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString();
//						result = partialTChargePEOEDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString();

					//3D
//						result = result + (inductiveAtomicHardnessDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");
//						result = result + (inductiveAtomicSoftnessDescriptor.calculate(molecule.getAtom(atomNr), molecule).getValue().toString() + ",");

					//calculate SPAN descriptor
					double highestMaxTopDistInMatrixRow = 0;
					for (int compAtomNr = 0; compAtomNr < molecule.getAtomCount(); compAtomNr++){
						if (highestMaxTopDistInMatrixRow < minTopDistMatrix[atomNr][compAtomNr]) {
							highestMaxTopDistInMatrixRow = minTopDistMatrix[atomNr][compAtomNr];
						}
					}
//					System.out.println(longestMaxTopDistInMolecule + "\t" + currentMaxTopDist);

					result = result + (highestMaxTopDistInMatrixRow/longestMaxTopDistInMolecule) + ",";
					result = result + (longestMaxTopDistInMolecule - highestMaxTopDistInMatrixRow) + ",";
					result = result + (highestMaxTopDistInMatrixRow) + ",";
					result = result + (longestMaxTopDistInMolecule);					
		            outfile.println(result);
				}		
			}
			outfile.close();
		}
		
		catch (ArrayIndexOutOfBoundsException e) {
			//catches some massive molecules
			System.out.println("Error: ArrayIndexOutOfBoundsException: " + molecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
		}
		catch (CDKException e) {
			System.out.println("Error: CDKException: " + molecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Error: Exception: " + molecule.getProperty("RXN:VARIATION(1):MDLNUMBER"));
			e.printStackTrace();
		}
	}
}