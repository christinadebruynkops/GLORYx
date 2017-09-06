//Span descriptor calculation taken from SmartCyp

package modelling;

import globals.Globals;
import modelling.descriptors.circular.CircularCollector;
import modelling.descriptors.circular.NeighborhoodIterator;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.*;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.FixBondOrdersTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.DeAromatizationTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import utils.Utils;
import utils.depiction.DepictorSMARTCyp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//import utils.SoMInfo;

public class PredictorWorkerThread implements Runnable {

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
		circ_collector.writeData(molecule);
		return circ_collector.getSignatures();
	}

	public PredictorWorkerThread(IMolecule molecule, Globals globals) throws IOException, ClassNotFoundException{
		this.globals = globals;
		this.molecule = molecule;
		mol_name = molecule.getProperty(Globals.ID_PROP).toString();
	}

	@Override
	public void run() {
		try {
			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				throw new Exception("Error: salt: " + mol_name);
			}

			// make output directory
			File out_dir_file = new File(globals.output_dir, mol_name);
			if (!out_dir_file.exists()) {
				out_dir_file.mkdir();
			}
			this.out_dir = out_dir_file.toPath().toString() + '/';

			System.out.println("************** Processing molecule: " + mol_name + " **************");
			if (globals.generate_pngs) {
				globals.depictor.generateDepiction(molecule, out_dir + mol_name + ".png");
			}

			// start stop watch
			long startTime = System.nanoTime();

			// prepare the structure
			AllRingsFinder finder = new AllRingsFinder();
			IRingSet rings = finder.findAllRings(molecule);
			for (IAtomContainer ring: rings.atomContainers()) {
				DeAromatizationTool.deAromatize((IRing) ring);
			}
			AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
			CDKHydrogenAdder adder;
			adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			try {
				adder.addImplicitHydrogens(molecule);
			} catch (Exception exp) {
				System.err.println("CDK internal error for: " + mol_name);
				throw exp;
			}
			FixBondOrdersTool botool = new FixBondOrdersTool();
			botool.kekuliseAromaticRings(molecule);
			CDKHueckelAromaticityDetector.detectAromaticity(molecule);

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
				System.err.println("Explicit hydrogens in the original structure: " + Integer.toString(hydrogens_total));
				System.err.println("Added hydrogens: " + AtomContainerManipulator.getTotalHydrogenCount(molecule));
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

				if (globals.generate_pngs) {
					System.err.println("Generating depiction for: " + mol_name);
					globals.depictor.generateDepiction(molecule, out_dir + mol_name + "_with_hs.png");
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
//			SMSDNormalizer.aromatizeMolecule(molecule);
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

			// create the output directory
			File data_dir = new File(out_dir);
			if (!data_dir.exists()) {
				data_dir.mkdir();
			}

//			System.out.println("Calculating basic CDK descriptors...");

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
//				System.out.printf("Calculating circular descriptors (depth %1$s)...\n", Integer.toString(globals.circ_depth));
				ccdk_signatures = computeCircDescriptors(base_descriptors, new CircularCollector.MeanAggregator(), globals.circ_depth);

				// impute missing values for circular descriptors
				globals.circ_imputer.impute(molecule, ccdk_signatures);
			}

			// calculate the atom type circular fingerprints
			CircularCollector fg_collector = new CircularCollector(Arrays.asList("AtomType"), new CircularCollector.CountJoiner());
			if (globals.desc_groups.contains("fing")) {
//				System.out.printf("Calculating circular fingerprints (depth %1$s)...\n", Integer.toString(globals.fing_depth));
				NeighborhoodIterator fg_iterator = new NeighborhoodIterator(molecule, globals.fing_depth);
				fg_iterator.iterate(fg_collector);
				fg_collector.writeData(molecule);
			}

			// encode atom types
            globals.at_encoder.encode(molecule);

			// do the modelling and process the results
//			System.out.println("Predicting...");
			globals.modeller.predict(molecule, Double.parseDouble(globals.misc_params.get("decision_threshold")));

			// stop the stop watch and print result
			long stopTime = System.nanoTime();
			double elapsedTimeMillis = ((double) (stopTime - startTime)) / 10e6;
			System.out.println("Prediction and descriptor calculation finished (" + mol_name + "). Elapsed time: " + Double.toString(elapsedTimeMillis) + " ms.");

			// generate PNG depictions if requested
			if (globals.generate_pngs) {
				globals.som_depictor.generateDepiction(molecule, out_dir + mol_name + "_soms.png");
			}

			// write the HTML output
			String[] filenames = new String[1];
			filenames[0] = globals.input_sdf;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			Date date = new Date();
			DepictorSMARTCyp depictor_sc = new DepictorSMARTCyp(dateFormat.format(date), filenames, out_dir, out_dir + mol_name + "_soms.html", globals);
			MoleculeSet moleculeSet = new MoleculeSet();
			moleculeSet.addAtomContainer(molecule);
			depictor_sc.writeHTML(moleculeSet);

			// write CSV files if requested
			if (globals.generate_csvs) {
				System.out.println("Writing CSV files...");
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
					fingerprints.addAll(fg_collector.getSignatures());
					Utils.writeAtomData(
							molecule
							, out_dir + mol_name + "_fing_level" + Integer.toString(globals.fing_depth) + ".csv"
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
							, out_dir + mol_name + "_circ_level" + Integer.toString(globals.circ_depth) + ".csv"
							, circ_descs
							, false
					);
				}
			}

			System.out.println("************** Done (" + mol_name + ") **************");
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