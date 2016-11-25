//Span descriptor calculation taken from SmartCyp

package fame.descriptors;

import fame.tools.*;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.normalize.SMSDNormalizer;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.*;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorkerThread implements Runnable {

	private IMolecule molecule;
	private boolean depict;
	private static final String id_prop = Globals.ID_PROP;
	private static final Set<String> allowed_atoms = new HashSet<>(Arrays.asList(
			"C"
			, "N"
			, "S"
			, "O"
			, "H"
			, "F"
			, "Cl"
			, "Br"
			, "I"
			, "P"
	));
	private static final Map<String, Integer> circ_descs_stats = new HashMap<>();

	public static Map<String, Integer> getStats() {
		return circ_descs_stats;
	}

	public WorkerThread(IMolecule molecule, boolean depict) throws IOException, ClassNotFoundException{
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

			System.out.println("************** Molecule " + (molecule.getProperty(id_prop) + " **************"));

			// remove molecules that cause trouble in the machine learning phase
			if (molecule.getProperty(id_prop).equals("M17055")) {
				throw new Exception("Removed M17055");
			}

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
			CDKHydrogenAdder adder;
			adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			try {
				adder.addImplicitHydrogens(molecule);
			} catch (Exception exp) {
				System.err.println("Error while adding implicit hydrogens: " + molecule.getProperty(id_prop));
				throw exp;
			}

			// count all implicit hydrogens
			int hydrogens_total = 0;
			int implicit_hydrogens = 0;
			for (int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
				IAtom atom = molecule.getAtom(atomNr);

				String symbol = atom.getSymbol();
				if (!allowed_atoms.contains(symbol)) {
					throw new Exception("Atypical atom detected: " + symbol + ". Skipping: " + molecule.getProperty(id_prop));
				}

				if (atom.getImplicitHydrogenCount() != null) {
					implicit_hydrogens += atom.getImplicitHydrogenCount();
				}
				if (atom.getSymbol().equals("H")) {
					hydrogens_total++;
				}
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
//					System.err.println("Generating depiction for: " + molecule.getProperty(id_prop));
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
					iAtom.setProperty("AtomType", iAtomType.getAtomTypeName());
//		            	System.out.println(iAtom.getProperty("SybylAtomType"));
					if (!iAtom.getSymbol().equals("H")) {
						heavyAtomCount++;
					}
				}
			}
//			Utils.fixSybylCarboxyl(molecule);
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

			// original CDK descriptors used in FAME
			List<IAtomicDescriptor> calculators = new ArrayList<>();
			calculators.add(new AtomDegreeDescriptor());
			calculators.add(new AtomHybridizationDescriptor());
			calculators.add(new AtomHybridizationVSEPRDescriptor());
			calculators.add(new AtomValenceDescriptor());
			calculators.add(new EffectiveAtomPolarizabilityDescriptor());
			calculators.add(new IPAtomicHOSEDescriptor());
			calculators.add(new PartialSigmaChargeDescriptor());
			calculators.add(new PartialTChargeMMFF94Descriptor());
			calculators.add(new PiElectronegativityDescriptor());
			calculators.add(new ProtonAffinityHOSEDescriptor());
			calculators.add(new SigmaElectronegativityDescriptor());
			calculators.add(new StabilizationPlusChargeDescriptor());

			// computationally intensive descriptors
//				IPAtomicLearningDescriptor iPAtomicLearningDescriptor = new IPAtomicLearningDescriptor();
//				PartialPiChargeDescriptor partialPiChargeDescriptor = new PartialPiChargeDescriptor();
//				PartialTChargePEOEDescriptor partialTChargePEOEDescriptor = new PartialTChargePEOEDescriptor();

			String[] desc_names = ("atomDegree,atomHybridization,atomHybridizationVSEPR,atomValence,effectiveAtomPolarizability," +
					"iPAtomicHOSE,partialSigmaCharge,partialTChargeMMFF94,piElectronegativity,protonAffinityHOSE,sigmaElectronegativity," +
					"stabilizationPlusCharge,relSPAN,diffSPAN,highestMaxTopDistInMatrixRow,longestMaxTopDistInMolecule").split(",");
			for(int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ){
				IAtom iAtom = molecule.getAtom(atomNr);
				//determine Sybyl atom types
				if (!iAtom.getSymbol().equals("H")) {
//					System.out.println("AtomNr " + atomNr);

					iAtom.setProperty("Atom", iAtom.getSymbol() + "."+ (atomNr+1));
					iAtom.setProperty("Molecule", molecule.getProperty(id_prop));

					int desc_idx = 0;
					for (IAtomicDescriptor calc : calculators) {
						iAtom.setProperty(desc_names[desc_idx], calc.calculate(molecule.getAtom(atomNr), molecule).getValue().toString());
						desc_idx++;
					}

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

					iAtom.setProperty(desc_names[desc_idx], Double.toString(highestMaxTopDistInMatrixRow/longestMaxTopDistInMolecule));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(longestMaxTopDistInMolecule - highestMaxTopDistInMatrixRow));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(highestMaxTopDistInMatrixRow));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(longestMaxTopDistInMolecule));
				}
			}

			// calculate the circular descriptors
			int circ_depth = 3;
			CircularCollector circ_collector = new CircularCollector(Arrays.asList(desc_names), new CircularCollector.MeanAggregator());
        	NeighborhoodIterator iterator = new NeighborhoodIterator(molecule, circ_depth);
			iterator.iterate(circ_collector);
			synchronized (circ_descs_stats) {
				circ_collector.writeData(molecule, circ_descs_stats);
			}

			// calculate the circular fingerprints
			int fg_circ_depth = 6;
			CircularCollector fg_collector = new CircularCollector(Arrays.asList("AtomType"), new CircularCollector.CountJoiner());
			NeighborhoodIterator fg_iterator = new NeighborhoodIterator(molecule, fg_circ_depth);
			fg_iterator.iterate(fg_collector);
			synchronized (circ_descs_stats) {
				fg_collector.writeData(molecule, circ_descs_stats);
			}


			// write the file
			List<String> basic_descs = new ArrayList<>(Arrays.asList(
					"Molecule"
					, "Atom"
					, "AtomType"
			));
			basic_descs.addAll(Arrays.asList(desc_names));
			Utils.writeAtomData(molecule, basic_descs, "_basic", false);

			List<String> fingerprints = new ArrayList<>();
			fingerprints.addAll(basic_descs);
			fingerprints.addAll(fg_collector.getSignatures());
			Utils.writeAtomData(molecule, fingerprints, "_fing", true);

			List<String> circ_descs = new ArrayList<>();
			circ_descs.addAll(basic_descs);
			circ_descs.addAll(circ_collector.getSignatures());
			Utils.writeAtomData(molecule, circ_descs, "_circ", false);
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