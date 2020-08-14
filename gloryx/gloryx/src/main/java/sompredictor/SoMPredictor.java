/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
 
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

package main.java.sompredictor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.normalize.SMSDNormalizer;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.FixBondOrdersTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.DeAromatizationTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.main.Main;
import org.zbh.fame.fame3.modelling.Predictor;
import org.zbh.fame.fame3.utils.data.FAMEMolSupplier;
import org.zbh.fame.fame3.utils.data.parsers.SMILESListParser;

import main.java.utils.molecule.MoleculeManipulator;


/**
 * This class is responsible for running FAME 3 as well as preparing molecules in the same way that they are prepared by FAME 3, 
 * in case FAME 3 will not be run for those molecules.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class SoMPredictor {
	
	private static final String MOLECULE_IS_NULL_AFTER_KEKULIZING = "Molecule is null after kekulizing.";
	private static final String MOL_PREFIX = "mol_";
	private static final String FAME_3_THREAD_INTERRUPTED_FOR_MOLECULE = "FAME 3 thread interrupted for molecule {}.";
	private static final String MORE_THAN_ONE_SMILES_AS_INPUT = "More than one SMILES as input. This will lead to errors so exiting now.";
	private static final String ERROR_MAKING_PREDICTIONS_WITH_FAME_3_MOL_NULL = "Error making predictions with FAME 3. Molecule is null afterwards.";
	private static final String ERROR_GENERATING_GLOBAL_PARAMETERS = "Error generating global parameters for FAME 3 prediction. Exiting.";
	private static final String NUMBER_OF_MOLECULES_IN_ARRAYLIST = "Number of molecules in arraylist {}";
	private static final String RUNNING_FAME3 = "Running FAME 3...";
	private static final String ERROR_PREPARING_MOLECULE = "Error preparing molecule {}";
	private static final String IS_SOM_FIELD = "isSoM";


	private static final Logger logger = LoggerFactory.getLogger(SoMPredictor.class.getName());
	
	private int moleculeCounter;

	// This set of allowed atoms is copied from PredictorWorkerThread in the FAME 3 code, because 
	// accessing it directly there seemed too complicated.
	public static final Set<String> allowed_atoms = new HashSet<>(Arrays.asList(
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

	
	// constructors
	
	public SoMPredictor (int moleculeCounter) {
		this.moleculeCounter = moleculeCounter;
	}
	
	
	// methods
	
	public static Globals createGlobals(String fameModelName) {
		
		// to initialize Globals, need a Namespace from ArgumentParser
		ArgumentParser parser = Main.getArgumentParser();
		Namespace args = null;
		try { 
			args = parser.parseArgs(new String[] { "-r", "1", "-m", fameModelName, "-s", "dummy", "-a"}); // , "-l" // no longer writing html for webserver
			// -a says not to use applicability domain model - this is to save time, since I don't use the FAMEscore in any way. 
			// Specifying it here may seem redundant, because it is specified in the Predictor constructed below, but it is actually 
			// important for creating the jar file since specifying it here allows me to not need to include the applicability domain models
			// in the resources folder and in the jar file. It also saves time to not load the applicability domain model(s) in the first place.
			// Can  use -t option to set decision threshold.
			// -l says to create results html file (I had added this option so it's turned off automatically, but for the webserver we actually do want to create the html)
		} catch (ArgumentParserException e1) {
			logger.error("Error parsing arguments for FAME 3", e1);
			System.exit(1);  // leave this because it can't be caused by a user of the web server
		} 
		logger.info("arguments: {}", args);
		
		// initialize Globals
		Globals params = null;
		try {
			params = new Globals(args);
		} catch (Exception e) {
			logger.error(ERROR_GENERATING_GLOBAL_PARAMETERS, e);
			System.exit(1);  // leave this because it can't be caused by a user of the web server
		}
		return params;
	}
	
	public IAtomContainerSet predict(List<String> smilesInput, Globals params) {
		logger.info(RUNNING_FAME3);
		// IMPORTANT NOTE: input smiles are not set in the Globals params because of multithreading. The input smiles is set locally in each instance of Params.

		// make predictions
		IAtomContainerSet moleculesWithSoMs = predictSoMs(params, smilesInput);
		if (moleculesWithSoMs == null) {
			logger.error(ERROR_MAKING_PREDICTIONS_WITH_FAME_3_MOL_NULL);
		} else {
			logger.debug(NUMBER_OF_MOLECULES_IN_ARRAYLIST, moleculesWithSoMs.getAtomContainerCount());
		}

		return moleculesWithSoMs;
	}

	/**
	 * Predict SoMs with FAME 3 for only one molecule at a time. The list smilesInput must contain exactly one SMILES string.
	 * 
	 * @param params
	 * @param smilesInput
	 * @return IAtomContainerSet containing a single molecule (the input molecule) with the predicted SoMs annotated
	 */
	private IAtomContainerSet predictSoMs(Globals params, List<String> smilesInput) {
		
		if (smilesInput.size() != 1) {
			logger.error(MORE_THAN_ONE_SMILES_AS_INPUT);
			System.exit(1);
		}
		
		// set up to construct FAME 3 Predictor so input molecule is labeled correctly
		List<String> names = new ArrayList<>();
		names.add(Integer.toString(moleculeCounter));
		SMILESListParser smiParser = new SMILESListParser(smilesInput, names, MOL_PREFIX);
		FAMEMolSupplier fameSupplier = new FAMEMolSupplier(smiParser);

		// construct the Predictor
		Predictor somPredictor = new Predictor(params, fameSupplier, false, true, 
				false, // don't use AD score
				1); // only one thread should be used

		IAtomContainerSet moleculesWithSoMs = null;
		try {
			somPredictor.calculate();
		} catch (InterruptedException e) {
			logger.warn(FAME_3_THREAD_INTERRUPTED_FOR_MOLECULE, smilesInput.get(0));
			Thread.currentThread().interrupt();
		}
		moleculesWithSoMs = somPredictor.getMoleculesWithSoMAnnotations();

		return moleculesWithSoMs;
	}

	/**
	 * For preparing a molecule the same as is done in FAME 3, e.g. for consistency if not actually using FAME 3.
	 * 
	 * @param smiles
	 * @return
	 */
	public IAtomContainer prepareMolecule(String smiles) {
		
		IAtomContainer molecule = MoleculeManipulator.generateMoleculeFromSmiles(smiles);
		try {
			prepareMolecule(molecule);
		} catch (CDKException e) {
			logger.error(ERROR_PREPARING_MOLECULE, moleculeCounter);
		}
		return molecule;
	}

	/**
	 * For preparing a molecule the same as is done in FAME 3, e.g. for consistency if not actually using FAME 3.
	 * 
	 * @param smiles
	 * @return
	 */
	public void prepareMolecule(IAtomContainer molecule) throws CDKException {
		// this code was taken from src.modelling.PredictorWorkerThread.run() and refactored (including use of logger)

		dearomatizationOfRings(molecule);
		
		AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
		
		// aromatize; required for correct Sybyl atom type determination
		SMSDNormalizer.aromatizeMolecule(molecule);
		
		addImplicitHydrogens(molecule);
		
		Assert.notNull(molecule, MOLECULE_IS_NULL_AFTER_KEKULIZING);

		applyAromaticity(molecule);

		makeImplicitHydrogensExplicit(molecule);

	}

	private void makeImplicitHydrogensExplicit(IAtomContainer molecule) {
		// check atom types and count the number of added hydrogens
		int hydrogensTotal = 0;
		int implicitHydrogens = 0;
		for (int atomNr = 0; atomNr < molecule.getAtomCount()  ; atomNr++ ) {
			IAtom atom = molecule.getAtom(atomNr);

			String symbol = atom.getSymbol();
			if (!allowed_atoms.contains(symbol)) {
				logger.error("Atypical atom detected: {}. Skipping: {}", symbol, moleculeCounter);
			}

			if (atom.getImplicitHydrogenCount() != null) {
				implicitHydrogens += atom.getImplicitHydrogenCount();
			}
			if (atom.getSymbol().equals("H")) {
				hydrogensTotal++;
			}
		}
		
		// if implicit hydrogens were added, show a warning and make them explicit
		if (implicitHydrogens > 0) {
			logger.debug("WARNING: implicit hydrogens detected for molecule: {}", moleculeCounter);

			// add convert implicit hydrogens to explicit ones
			logger.debug("Making all hydrogens explicit...");
			logger.debug("Explicit hydrogens in the original structure: {}", hydrogensTotal);
			logger.debug("Added hydrogens: {}", AtomContainerManipulator.getTotalHydrogenCount(molecule));
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
		}

		Assert.notNull(molecule, "Molecule is null after making implicit hydrogens explicit");
	}

	@SuppressWarnings("deprecation")
	public void applyAromaticity(IAtomContainer molecule) throws CDKException {
		// Mimics the old CDKHuckelAromaticityDetector which uses the CDK atom types.
		// It is necessary to use the deprecated CDK methods to achieve the desired outcomes 
		// in FAME 3, so the same methods are used here for consistency.
		
		FixBondOrdersTool botool = new FixBondOrdersTool();
		botool.kekuliseAromaticRings(molecule);
		CDKHueckelAromaticityDetector.detectAromaticity(molecule);

		Assert.notNull(molecule, "Molecule is null after applying aromaticity");
	}

	private void addImplicitHydrogens(IAtomContainer molecule) throws CDKException {
		CDKHydrogenAdder adder;
		adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
		try {
			adder.addImplicitHydrogens(molecule);
		} catch (Exception exp) {
			logger.error("CDK internal error for: {}", moleculeCounter);
			throw exp;
		}
		
		Assert.notNull(molecule, "Molecule is null after adding implicit hydrogens");
	}

	/**
	 * This method was used to determine that the "new" CDK approach to kekulization, Kekulization.kekulize, has a 
	 * different outcome than the deprecated DeAromatizationTool.deAromatize. 
	 * Hence the decision to continue to use the deprecated version, in FAME 2/FAME 3 and here.
	 * 
	 * @param molecule
	 * @throws CDKException
	 */
	@SuppressWarnings({"unused", "deprecation"})
	private void kekulizeRings(IAtomContainer molecule) throws CDKException {  
		// Note: Using this method clearly shows that the DeAromatizationTool can do what Kekulization.kekulize cannot.
		
		AllRingsFinder finder = new AllRingsFinder();
		IRingSet rings = finder.findAllRings(molecule);
		try {
			for (IAtomContainer ring: rings.atomContainers()) {
				Kekulization.kekulize(ring);
			}
		} catch (Exception e1) {
			logger.warn("Error kekulizing. Using deprecated CDK dearomatization tool instead.", e1);
			
			try {
				for (IAtomContainer ring: rings.atomContainers()) {
					DeAromatizationTool.deAromatize((IRing) ring);
				}
			} catch (Exception e2) {
				logger.error("Error using CDK dearomatization tool", e2);
			}
			logger.info("SMILES after dearomatization: {}", MoleculeManipulator.generateSmiles(molecule));
		}
	}
	
	@SuppressWarnings("deprecation")
	private void dearomatizationOfRings(IAtomContainer molecule) throws CDKException {
		// code copied from FAME 3
		
		AllRingsFinder finder = new AllRingsFinder();
		IRingSet rings = finder.findAllRings(molecule);
		try {
			for (IAtomContainer ring: rings.atomContainers()) {
				DeAromatizationTool.deAromatize((IRing) ring);
			}
		} catch (Exception e) {
			logger.error("Error using dearomatization tool");
		}
	}
	
	public void prepareMoleculesSameAsFame(List<IAtomContainer> molecules) {
		
		for (IAtomContainer mol : molecules) {

			try {
				prepareMolecule(mol);
			} catch (CDKException e) {
				logger.error(ERROR_PREPARING_MOLECULE, moleculeCounter);
			}

			// set is_som to true for all heavy atoms
			designateAllHeavyAtomsSoMs(mol);
		}
	}
	
	private void designateAllHeavyAtomsSoMs(IAtomContainer molecule) {
		for (IAtom atom : molecule.atoms()) {
			if (atom.getAtomicNumber() > 1) {
				atom.setProperty(IS_SOM_FIELD, true);
			}
		}
	}

}
