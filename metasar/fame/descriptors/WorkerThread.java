//Span descriptor calculation taken from SmartCyp

package fame.descriptors;

import java.io.*;

import fame.tools.Depiction;
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
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import fame.tools.Globals;

public class WorkerThread implements Runnable {
	private Molecule molecule;
	private boolean depict;
	private static final String id_prop = Globals.ID_PROP;

	public WorkerThread(Molecule molecule, boolean depict) throws IOException, ClassNotFoundException{
		this.molecule = molecule;
		this.depict = depict;
	}

	@Override
	public void run() {
		try {
			synchronized (System.out) {
				//check if salt
				if (!ConnectivityChecker.isConnected(molecule)) {
					throw new Exception("Error: salt: " + molecule.getProperty(id_prop));
				}

				System.out.println("************** Molecule " + (molecule.getProperty(id_prop) + " **************"));

				//this code is not used. it was initially written to add charges
//	            Constructor<AtomTypeCharges> constructor = AtomTypeCharges.class.getDeclaredConstructor(new Class[0]);
//	         	constructor.setAccessible(true);
//	         	AtomTypeCharges atomTypeCharges = constructor.newInstance(new Object[0]);
//	         	atomTypeCharges.calculateCharges(molecule);
//				for(int x = 0; x < molecule.getAtomCount(); x++){
//					System.out.println(molecule.getAtom(x).getCharge());
//				}

				// add implicit hydrogens
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
				adder.addImplicitHydrogens(molecule);

				// count all implicit hydrogens
				int hydrogens_total = 0;
				int implicit_hydrogens = 0;
				for (int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
					IAtom atom = molecule.getAtom(atomNr);

					System.out.println("----- " + atom.getAtomTypeName() + " (#" + molecule.getAtomNumber(atom) + ")");
//				System.out.println("Iteration Number: " + atomNr);
					System.out.println("Implicit Hydrogens: " + atom.getImplicitHydrogenCount());
					if (atom.getImplicitHydrogenCount() != null) {
						implicit_hydrogens += atom.getImplicitHydrogenCount();
					}
					if (atom.getAtomTypeName().equals("H")) {
						hydrogens_total++;
					}
				}

				// if implicit hydrogens were added, show a warning and make them explicit
				if (implicit_hydrogens > 0) {
					System.err.println("WARNING: implicit hydrogens detected: " + molecule.getProperty(id_prop));

					// add convert implicit hydrogens to explicit ones
					System.out.println("Making all hydrogens explicit...");
					System.out.println("Explicit Hydrogens: " + Integer.toString(hydrogens_total - implicit_hydrogens));
					System.out.println("Added Implicit Hydrogens: " + AtomContainerManipulator.getTotalHydrogenCount(molecule));
					AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
				}

				// generate picture of the molecule with atom numbers (starting from 1!) and SOMs highlighted
				if (depict) {
					File depictions_dir = new File("depictions");
					if (!depictions_dir.exists()) {
						depictions_dir.mkdir();
					}
					Depiction.generateDepiction(molecule, "depictions/" + ((String) molecule.getProperty(id_prop)) + ".png");
				}

				// determine Sybyl atom type
				SMSDNormalizer.aromatizeMolecule(molecule); //aromatize molecule; required for Sybyl atom type determination
				IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());

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
					throw new Exception("Error: too large: " + molecule.getProperty(id_prop));
				}

				File data_dir = new File("data");
				if (!data_dir.exists()) {
					data_dir.mkdir();
				}
				PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter("data/" + molecule.getProperty(id_prop).toString() + ".csv")));

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

						result = result + ((molecule.getProperty(id_prop)) + "," + iAtom.getSymbol() + "."+ (atomNr+1) + ",");
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
		}

		catch (ArrayIndexOutOfBoundsException e) {
			//catches some massive molecules
			System.out.println("Error: ArrayIndexOutOfBoundsException: " + molecule.getProperty(id_prop));
		}
		catch (CDKException e) {
			System.out.println("Error: CDKException: " + molecule.getProperty(id_prop));
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Error: Exception: " + molecule.getProperty(id_prop));
			e.printStackTrace();
		}
	}
}