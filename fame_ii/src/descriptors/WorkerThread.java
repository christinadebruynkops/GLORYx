//Span descriptor calculation taken from SmartCyp

package descriptors;

import descriptors.circular.CircularCollector;
import descriptors.circular.NeighborhoodIterator;
import globals.Globals;
import modelling.Encoder;
import modelling.Modeller;
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
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

//import utils.SoMInfo;

public class WorkerThread implements Runnable {

	private IMolecule molecule;
	private String mol_name;
	private String out_dir;
	private Globals globals;

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

	private Set<String> computeCircDescriptors(List<String> desc_names, CircularCollector.Aggregator aggregator, int circ_depth) throws Exception {
		CircularCollector circ_collector = new CircularCollector(desc_names, aggregator);
		NeighborhoodIterator circ_iterator = new NeighborhoodIterator(molecule, circ_depth);
		circ_iterator.iterate(circ_collector);
		return circ_collector.getSignatures();
	}

	public WorkerThread(IMolecule molecule, Globals globals) throws IOException, ClassNotFoundException{
		this.globals = globals;
		this.molecule = molecule;
		mol_name = molecule.getProperty(Globals.ID_PROP).toString();
		File out_dir = new File(globals.output_dir, mol_name);
		if (!out_dir.exists()) {
			out_dir.mkdir();
		}
		this.out_dir = out_dir.toPath().toString() + '/';
	}

	@Override
	public void run() {
		try {
			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				throw new Exception("Error: salt: " + mol_name);
			}

			System.out.println("************** Molecule " + mol_name + " **************");
			globals.depictor.generateDepiction(molecule, out_dir + mol_name + ".png");

			// add implicit hydrogens (this is here to test for some internal CDK errors that can affect the descriptor calculations)
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			CDKHydrogenAdder adder;
			adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			try {
				adder.addImplicitHydrogens(molecule);
			} catch (Exception exp) {
				System.err.println("CDK internal error for: " + mol_name);
				throw exp;
			}

			// check atom types and count the number of added hydrogens
			int hydrogens_total = 0;
			int implicit_hydrogens = 0;
			for (int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
				IAtom atom = molecule.getAtom(atomNr);

				String symbol = atom.getSymbol();
				if (!allowed_atoms.contains(symbol)) {
					throw new Exception("Atypical atom detected: " + symbol + ". Skipping: " + mol_name);
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
				System.err.println("WARNING: implicit hydrogens detected for molecule: " + mol_name);

				// add convert implicit hydrogens to explicit ones
				System.err.println("Making all hydrogens explicit...");
				System.err.println("Explicit Hydrogens: " + Integer.toString(hydrogens_total));
				System.err.println("Added Implicit Hydrogens: " + AtomContainerManipulator.getTotalHydrogenCount(molecule));
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

				System.err.println("Generating depiction for: " + mol_name);
				try {
					globals.depictor.generateDepiction(molecule, out_dir + mol_name + "_with_hs.png");
				} catch (Exception exp) {
					System.err.println("Failed to generate depiction for: " + mol_name);
					exp.printStackTrace();
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
//			Depictor.generateDepiction(molecule, "deprot.png");
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
//			Depictor.generateDepiction(molecule, "prot.png");

			//check if molecule too large
			if (heavyAtomCount > 100) {
				throw new Exception("Error: molecule is too large: " + mol_name);
			}

			File data_dir = new File(out_dir);
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
			for(int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
				IAtom iAtom = molecule.getAtom(atomNr);
				//determine Sybyl atom types
				if (!iAtom.getSymbol().equals("H")) {
//					System.out.println("AtomNr " + atomNr);

					iAtom.setProperty("Atom", iAtom.getSymbol() + "." + (atomNr + 1));
					iAtom.setProperty("Molecule", mol_name);

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
					for (int compAtomNr = 0; compAtomNr < molecule.getAtomCount(); compAtomNr++) {
						if (highestMaxTopDistInMatrixRow < minTopDistMatrix[atomNr][compAtomNr]) {
							highestMaxTopDistInMatrixRow = minTopDistMatrix[atomNr][compAtomNr];
						}
					}

					iAtom.setProperty(desc_names[desc_idx], Double.toString(highestMaxTopDistInMatrixRow / longestMaxTopDistInMolecule));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(longestMaxTopDistInMolecule - highestMaxTopDistInMatrixRow));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(highestMaxTopDistInMatrixRow));
					desc_idx++;
					iAtom.setProperty(desc_names[desc_idx], Double.toString(longestMaxTopDistInMolecule));
				}
			}

			// the base descriptors -> always calculated (see above)
			List<String> base_descriptors = new ArrayList<>();
			base_descriptors.addAll(Arrays.asList(desc_names));

			// calculate circular descriptors (CDK)
			Set<String> ccdk_signatures = new HashSet<>();
			if (globals.desc_groups.contains("ccdk")) {
				ccdk_signatures = computeCircDescriptors(base_descriptors, new CircularCollector.MeanAggregator(), Globals.circ_depth);
			}

			// calculate the atom type circular fingerprints
			Set<String> fing_signatures = new HashSet<>();
			if (globals.desc_groups.contains("fing")) {
				CircularCollector fg_collector = new CircularCollector(Arrays.asList("AtomType"), new CircularCollector.CountJoiner());
				NeighborhoodIterator fg_iterator = new NeighborhoodIterator(molecule, Globals.fing_depth);
				fg_iterator.iterate(fg_collector);
                fing_signatures = fg_collector.getSignatures();
			}

			// encode atom types
            Encoder at_encoder = new Encoder("AtomType", globals.encoders_json);
            at_encoder.encode(molecule);

            // impute missing values for circular descriptors
            // TODO: implement

			// do the modelling and process the results
			globals.modeller.predict(molecule);
			globals.som_depictor.generateDepiction(molecule, out_dir + mol_name + "_soms.png");

			// write the basic CDK descriptors
			List<String> basic_descs = new ArrayList<>(Arrays.asList(
					"Molecule"
					, "Atom"
					, Modeller.is_som_fld
					, Modeller.proba_yes_fld
					, Modeller.proba_no_fld
					, "AtomType"
			));
			basic_descs.addAll(Arrays.asList(desc_names));
			Utils.writeAtomData(
					molecule
					, out_dir + mol_name + "_basic" + ".csv"
					, basic_descs
					, false
			);

			// write the atom type fingerprints
			if (globals.desc_groups.contains("fing")) {
				List<String> fingerprints = new ArrayList<>();
				fingerprints.addAll(basic_descs);
				fingerprints.addAll(fing_signatures);
				Utils.writeAtomData(
						molecule
						, out_dir + mol_name + "_fing_level" + Integer.toString(Globals.fing_depth) + ".csv"
						, fingerprints
						, true
				);
			}

			// write the circular descriptors
			if (globals.desc_groups.contains("ccdk")) {
				List<String> circ_descs = new ArrayList<>();
				circ_descs.addAll(basic_descs);
				circ_descs.addAll(ccdk_signatures);
				Utils.writeAtomData(
						molecule
						, out_dir + mol_name + "_circ_level" + Integer.toString(Globals.circ_depth) + ".csv"
						, circ_descs
						, false
				);
			}
		}

		catch (ArrayIndexOutOfBoundsException e) {
			//catches some massive molecules
			System.out.println("Error: ArrayIndexOutOfBoundsException: " + mol_name);
		}
		catch (CDKException e) {
			System.out.println("Error: CDKException: " + mol_name);
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Error: Exception: " + mol_name);
			e.printStackTrace();
		}
	}
}