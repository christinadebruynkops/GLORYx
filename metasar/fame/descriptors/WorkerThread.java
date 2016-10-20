//Span descriptor calculation taken from SmartCyp

package fame.descriptors;

import fame.tools.Depiction;
import fame.tools.Globals;
import fame.tools.SoMInfo;
import fame.tools.Utils;
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
import org.openscience.cdk.qsar.descriptors.atomic.*;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.*;

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
//			synchronized (System.out) {
			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				throw new Exception("Error: salt: " + molecule.getProperty(id_prop));
			}

			// check if the molecule has SOMs annotated
			String som_info = (String) molecule.getProperty(Globals.SOM_PROP);
			if (som_info.equals("None")) {
				throw new Exception("Error: no SoMs annotated: " + molecule.getProperty(id_prop));
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

			// parse the SoM information and save the essential data to the molecule (throws exception for molecules without SoMs annotated)
			SoMInfo.parseInfoAndUpdateMol(molecule);

			// if requested, generate picture of the molecule with atom numbers and SOMs highlighted
			if (depict) {
				File depictions_dir = new File(Globals.DEPICTIONS_OUT);
				if (!depictions_dir.exists()) {
					depictions_dir.mkdir();
				}
				Depiction.generateDepiction(molecule, Globals.DEPICTIONS_OUT + ((String) molecule.getProperty(id_prop)) + ".png");
			}

			// add implicit hydrogens
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			adder.addImplicitHydrogens(molecule);

			// count all implicit hydrogens
			int hydrogens_total = 0;
			int implicit_hydrogens = 0;
			for (int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
				IAtom atom = molecule.getAtom(atomNr);

//				System.out.println("----- " + atom.getAtomTypeName() + " (#" + (molecule.getAtomNumber(atom) + 1) + ")");
//				System.out.println("Iteration Number: " + atomNr);
//				System.out.println("Implicit Hydrogens: " + atom.getImplicitHydrogenCount());
				if (atom.getImplicitHydrogenCount() != null) {
					implicit_hydrogens += atom.getImplicitHydrogenCount();
				}
				if (atom.getSymbol().equals("H")) {
					hydrogens_total++;
				}
			}

			// check for some badly represented groups
			if (Utils.matchesSMARTS(molecule, "[NX3]([OH])[OH]")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a badly represented nitro group");
			} else if (Utils.matchesSMARTS(molecule, "[N+]O")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a nitrogen with +1 charge and oxygen with a missing negative charge");
			} else if (Utils.matchesSMARTS(molecule, "[N,N+]-[OH]")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a badly represented nitroxide");
			} else if (Utils.matchesSMARTS(molecule, "[CX3](O[H])O[H]")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a badly represented carboxyl group");
			} else if (Utils.matchesSMARTS(molecule, "[NH2]-[N]~N")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a badly represented azide group (missing triple bond)");
			} else if (Utils.matchesSMARTS(molecule, "N=[N+][N-]")) {
				throw new Exception("SMARTS match: molecule " + molecule.getProperty(id_prop) + " contains a badly represented azide group (missing double bond)");
			}

			// if implicit hydrogens were added, show a warning and make them explicit
			if (implicit_hydrogens > 0) {
				System.err.println("WARNING: implicit hydrogens detected for molecule: " + molecule.getProperty(id_prop));

				// add convert implicit hydrogens to explicit ones
				System.err.println("Making all hydrogens explicit...");
				System.err.println("Explicit Hydrogens: " + Integer.toString(hydrogens_total));
				System.err.println("Added Implicit Hydrogens: " + AtomContainerManipulator.getTotalHydrogenCount(molecule));
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

				if (depict) {
					System.err.println("Generating depiction for: " + molecule.getProperty(id_prop));
					try {
						Depiction.generateDepiction(molecule, Globals.DEPICTIONS_OUT + ((String) molecule.getProperty(id_prop)) + "_with_hs.png");
					} catch (Exception exp) {
						System.err.println("Failed to generate depiction for: " + molecule.getProperty(id_prop));
						exp.printStackTrace();
					}

				}
			}

			// needed for the SPAN descriptors
			int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(molecule);
			// calculate the maximum topology distance
			// takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
			int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
//				Utils.printMatrix(adjacencyMatrix, 0);
//				Utils.printMatrix(minTopDistMatrix, 2);
			// find the longest Path of all, "longestMaxTopDistInMolecule"
			double longestMaxTopDistInMolecule = 0;
			double currentMaxTopDist = 0;
			for(int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
				for(int i = 0; i < molecule.getAtomCount(); i++){
					currentMaxTopDist =  minTopDistMatrix[atomNr][i];
//					System.out.println(longestMaxTopDistInMolecule + "\t" + currentMaxTopDist);

					if(currentMaxTopDist > longestMaxTopDistInMolecule) {
						longestMaxTopDistInMolecule = currentMaxTopDist;
					}
				}
			}

			// deprotonate the carboxyl groups (this needs to be done or the Sybyl atom type checker won't recognize it as a carboxyl)
			// aromatize; required for Sybyl atom type determination
			SMSDNormalizer.aromatizeMolecule(molecule);
			IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());
//			Utils.deprotonateCarboxyls(molecule);
//			Depiction.generateDepiction(molecule, "deprot.png");
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
			}
			Utils.fixSybylCarboxyl(molecule);
//			Utils.protonateCarboxyls(molecule); // protonate the molecule back
//			Depiction.generateDepiction(molecule, "prot.png");

			//check if molecule too large
			if (heavyAtomCount > 100) {
				throw new Exception("Error: molecule is too large: " + molecule.getProperty(id_prop));
			}

			File data_dir = new File(Globals.DESCRIPTORS_OUT);
			if (!data_dir.exists()) {
				data_dir.mkdir();
			}
			PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(Globals.DESCRIPTORS_OUT + molecule.getProperty(id_prop).toString() + ".csv")));

			outfile.print("Molecule,Atom,AtomType,atomDegree,atomHybridization,atomHybridizationVSEPR,atomValence,effectiveAtomPolarizability," +
					"iPAtomicHOSE,partialSigmaCharge,partialTChargeMMFF94,piElectronegativity,protonAffinityHOSE,sigmaElectronegativity," +
					"stabilizationPlusCharge,relSPAN,diffSPAN,highestMaxTopDistInMatrixRow,longestMaxTopDistInMolecule");
			outfile.println();

			// original CDK descriptors used in FAME
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

			// computationally intensive descriptors
//				IPAtomicLearningDescriptor iPAtomicLearningDescriptor = new IPAtomicLearningDescriptor();
//				PartialPiChargeDescriptor partialPiChargeDescriptor = new PartialPiChargeDescriptor();
//				PartialTChargePEOEDescriptor partialTChargePEOEDescriptor = new PartialTChargePEOEDescriptor();

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

					// computationally intensive descriptors
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
//			}
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