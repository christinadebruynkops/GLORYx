/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
   Copyright (C) 2013  Johannes Kirchmair <johannes.kirchmair@univie.ac.at>
 
    This file is part of GLORYx.

    GLORYx is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    All we ask is that proper credit is given for our work, which includes 
    - but is not limited to - adding the above copyright notice to the beginning 
    of your source code files, and to any copyright notice that you may distribute 
    with programs based on this work.

    GLORYx is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GLORYx.  If not, see <https://www.gnu.org/licenses/>.
*/

//Span descriptor calculation taken from SmartCyp

package org.zbh.fame.fame3.modelling;

import ambit2.smarts.SMIRKSManager;
import ambit2.smarts.SMIRKSReaction;

import org.openscience.cdk.io.MDLV2000Writer;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.modelling.descriptors.PartialSigmaChargeDescriptorPatched;
import org.zbh.fame.fame3.modelling.descriptors.circular.CircularCollector;
import org.zbh.fame.fame3.modelling.descriptors.circular.NeighborhoodIterator;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.normalize.SMSDNormalizer;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.*;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.DeAromatizationTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbh.fame.fame3.utils.Utils;
import org.zbh.fame.fame3.utils.data.Predictions;
import org.zbh.fame.fame3.utils.depiction.DepictorSMARTCyp;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PredictorWorkerThread implements Runnable {

	private IAtomContainer molecule;
	private String mol_name;
	private String out_dir;
	private Globals globals;
	private Predictions predictions;
	private boolean returnMol;
	private boolean useAD;
	private String decision_threshold;
	
	private static final Logger logger = LoggerFactory.getLogger(PredictorWorkerThread.class.getName());


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
            , "B"
            , "Si"
	));

	private Set<String> computeCircDescriptors(List<String> desc_names, CircularCollector.Aggregator aggregator, int circ_depth) throws Exception {
		CircularCollector circ_collector = new CircularCollector(desc_names, aggregator);
		NeighborhoodIterator circ_iterator = new NeighborhoodIterator(molecule, circ_depth);
		circ_iterator.iterate(circ_collector);
		circ_collector.writeData(molecule);
		return circ_collector.getSignatures();
	}


	public PredictorWorkerThread(IAtomContainer molecule, Globals globals) {
		this.globals = globals;
		this.molecule = molecule;
		this.predictions = null;
		this.useAD = false;
		this.decision_threshold = globals.decision_threshold;
		this.mol_name = molecule.getProperty(Globals.ID_PROP).toString();

		// create output directory
		if (globals.output_dir != null) {
			if (globals.generate_csvs || globals.generate_html || globals.generate_pngs) {
				File out_dir_file = new File(globals.output_dir, mol_name);
				if (!out_dir_file.exists()) {
					out_dir_file.mkdir();
				}
				this.out_dir = out_dir_file.toPath().toString() + '/';
			}
		}
	}

	public PredictorWorkerThread(IAtomContainer molecule, Globals globals, Predictions predictions, boolean useAD, String decision_threshold) {
		this(molecule, globals);
		this.predictions = predictions;
		this.useAD = useAD;
		this.decision_threshold = decision_threshold;
	}
	
	public PredictorWorkerThread(IAtomContainer molecule, Globals globals, boolean returnMol, boolean useAD, String decision_threshold) {
		this(molecule, globals);
		this.predictions = null;
		this.returnMol = returnMol;
		this.useAD = useAD;
		this.decision_threshold = decision_threshold;
	}
	

	@Override
	public void run() {
		try {
			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				throw new Exception("Error: salt: " + mol_name);
			}

			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			// Standardize structure:
			// This is necessary because input SMILES may have used expanded valence representation of
			// nitro groups or other nitrogen-containing functional groups, and this representation is not compatible with calculating descriptors.
			String nitroGroupStandardizationSMIRKS = "[*:1][N:2](=[O:3])=[O:4]>>[*:1][N+:2](=[O:3])[O-:4]";  // can be found on Daylight SMIRKS website
			String nitrogenStandardizationSMIRKS = "[*:1][N;v5:2](=[O:3])>>[*:1][N+:2][O-:3]";  // this SMIRKS covers the case of nitro groups as well (valence 5), but would result in two products

			SMIRKSManager smirksManager = new SMIRKSManager(SilentChemObjectBuilder.getInstance());
			SMIRKSReaction transformationNitroGroup = smirksManager.parse(nitroGroupStandardizationSMIRKS);
			SMIRKSReaction transformationExpandedValenceNitrogen = smirksManager.parse(nitrogenStandardizationSMIRKS);
			if (smirksManager.hasErrors()) {
				logger.error("Error parsing SMIRKS " + nitroGroupStandardizationSMIRKS);
			}
			smirksManager.applyTransformation(molecule, null, transformationNitroGroup);  // apply this first because otherwise the other SMIRKS may result in two (same) products. Only want one.
			smirksManager.applyTransformation(molecule, null, transformationExpandedValenceNitrogen);
			// Note: There is still a chance that there will be multiple molecules inside the atom container. Hence there should be a salt checker after this code

			//check if salt
			if (!ConnectivityChecker.isConnected(molecule)) {
				// FIXME: check the components after transformation and only select one (they should be the same)
				throw new Exception("Error: Two components after standardization: " + mol_name);
			}


			logger.debug("Processing molecule: " + mol_name);
			if (globals.generate_pngs && globals.output_dir != null) {
				globals.depictor.generateDepiction((IAtomContainer) molecule, out_dir + mol_name + ".png");
			}

			// start stop watch
			long startTime = System.nanoTime();


//			// prepare the structure
			AllRingsFinder finder = new AllRingsFinder();
			IRingSet rings = finder.findAllRings(molecule);
			for (IAtomContainer ring: rings.atomContainers()) {
				DeAromatizationTool.deAromatize((IRing) ring);
			}
			
			AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
			// aromatize; required for correct Sybyl atom type determination
			SMSDNormalizer.aromatizeMolecule(molecule);
			CDKHydrogenAdder adder;
			adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			try {
				adder.addImplicitHydrogens(molecule);
			} catch (Exception exp) {
				logger.error("CDK internal error for: " + mol_name);
				throw exp;
			}
//			FixBondOrdersTool botool = new FixBondOrdersTool();
//			botool.kekuliseAromaticRings(molecule);
//			CDKHueckelAromaticityDetector.detectAromaticity(molecule);


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
				logger.warn("WARNING: implicit hydrogens detected for molecule: " + mol_name);

				// add convert implicit hydrogens to explicit ones
				logger.warn("Making all hydrogens explicit...");
				logger.debug("Explicit hydrogens in the original structure: " + Integer.toString(hydrogens_total));
				logger.debug("Added hydrogens: " + AtomContainerManipulator.getTotalHydrogenCount(molecule));
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

				if (globals.generate_pngs && globals.output_dir != null) {
					logger.debug("Generating depiction for: " + mol_name);
					globals.depictor.generateDepiction((IAtomContainer) molecule, out_dir + mol_name + "_with_hs.png");
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
//			Utils.deprotonateCarboxyls(molecule);
//			Depictor.generateDepiction(molecule, "deprot.png");

			IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());
			for(int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
				IAtom iAtom = molecule.getAtom(atomNr);

//				String symbol = iAtom.getSymbol();
//				if (!allowed_atoms.contains(symbol)) {
//					throw new Exception("Atypical atom detected: " + symbol);
//				}

				if (iAtom.getSymbol().equals("H")) continue;
				//determine Sybyl atom types
				IAtomType iAtomType = atm.findMatchingAtomType(molecule,molecule.getAtom(atomNr));
				if (iAtomType != null) {
					String atype_name = iAtomType.getAtomTypeName();
					iAtom.setProperty("AtomType", atype_name);
				} else {
					String id = iAtom.getSymbol() + "." + Integer.toString(atomNr + 1);
					throw new Exception("Failed to determine Sybyl atom type for atom: " + id);
				}
			}
//			Utils.fixSybylCarboxyl(molecule);
//			Utils.protonateCarboxyls(molecule); // protonate the molecule back
//			Depictor.generateDepiction(molecule, "prot.png");

			logger.debug("Calculating descriptors for: " + mol_name);

			// original CDK descriptors used in FAME
			List<IAtomicDescriptor> calculators = new ArrayList<>();
			calculators.add(new AtomDegreeDescriptor());
			calculators.add(new AtomHybridizationDescriptor());
			calculators.add(new AtomHybridizationVSEPRDescriptor());
			calculators.add(new AtomValenceDescriptor());
			calculators.add(new EffectiveAtomPolarizabilityDescriptor());
			calculators.add(new IPAtomicHOSEDescriptor());
			calculators.add(new PartialSigmaChargeDescriptorPatched());
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
//						
//						if (molecule.getAtom(atomNr) == null) {
//							System.out.println("atom is null");
//						} else {
//							System.out.println("atom is " + molecule.getAtom(atomNr).getSymbol());
//						}
						
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
			logger.debug("Descriptor calculation finished for: " + mol_name);

			// encode atom types
            globals.at_encoder.encode(molecule);

			// do the modelling and process the results
			logger.debug("Predicting: " + mol_name);
			double threshold = Double.parseDouble(globals.model_hyperparams.get("decision_threshold"));
			if (!decision_threshold.equals("model")) {
				threshold = Double.parseDouble(decision_threshold);
			}
			globals.modeller.predict(
					molecule
					, threshold
					, useAD
					, predictions
			);

			// stop the stop watch and print result
			long stopTime = System.nanoTime();
			double elapsedTimeMillis = ((double) (stopTime - startTime)) / 10e6;
			logger.info("Prediction finished for " + mol_name + ". Elapsed time: " + Double.toString(elapsedTimeMillis) + " ms.");

			// save the MDL block of the molecule to predictions if avaialable
			if (predictions != null) {
				StringWriter string_writer = new StringWriter();
				MDLV2000Writer mdl_writer = new MDLV2000Writer(string_writer);
				mdl_writer.writeMolecule(molecule);
				predictions.setMolBlock(string_writer.toString());
			}

			// generate PNG depictions if requested
			if (globals.generate_pngs && globals.output_dir != null) {
				globals.som_depictor.generateDepiction((IAtomContainer) molecule, out_dir + mol_name + "_soms.png");
			}

			// write the HTML output if requested (no if clause in regular fame 3)
			if (globals.generate_html) {
	            String[] filenames = new String[1];
	            filenames[0] = (String) molecule.getProperty(Globals.FILE_PATH_PROP);
	            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	            Date date = new Date();
	            DepictorSMARTCyp depictor_sc = new DepictorSMARTCyp(dateFormat.format(date), filenames, out_dir, out_dir + mol_name + "_soms.html", globals);
	            IAtomContainerSet moleculeSet = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);
	            moleculeSet.addAtomContainer(molecule);
				if (globals.output_dir != null) {
					depictor_sc.writeHTML(moleculeSet);
				}
				if (predictions != null) {
				    depictor_sc.usePlacehoders(true);
					predictions.setPredictionHTML(depictor_sc.getPageAsString(moleculeSet));
	            }
			}

			// write CSV files if requested
			if (globals.generate_csvs && globals.output_dir != null) {
				logger.debug("Writing CSV files for: " + mol_name);
				// write the basic CDK descriptors
				List<String> basic_descs = new ArrayList<>(Arrays.asList(
						"Molecule"
						, "Atom"
						, Modeller.is_som_fld
						, Modeller.proba_yes_fld
						, Modeller.proba_no_fld
						, "AD_score"
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

			logger.info("************** Done (" + mol_name + ") **************");
			if (!returnMol) {
				molecule = null; // clears memory since we are done with this one
			}
			System.gc();
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//catches some massive molecules
			logger.error("Error: ArrayIndexOutOfBoundsException: " + mol_name);
			saveErrorToPrediction(e);
		}
		catch (CDKException e) {
			logger.error("Error: CDKException: {}", mol_name, e);
            saveErrorToPrediction(e);
		}
		catch (IOException e) {
            logger.error("Error: IOException: {}", mol_name, e);
            saveErrorToPrediction(e);
		}
		catch (Exception e) {
			logger.error("Error: Unknown Exception: {}", mol_name, e);
            saveErrorToPrediction(e);
		}
	}

	private void saveErrorToPrediction(Exception exp) {
	    if (this.predictions != null) {
	        this.predictions.setError(exp);
        }
    }
}