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

package main.java.utils.molecule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.zbh.fame.fame3.globals.Globals;

import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;


/** 
 * Key helper class to perform various manipulations of molecules, including converting between formats, generating 2D coordinates, 
 * aromaticity handling, duplicate handling, and checking the size of the molecule.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MoleculeManipulator {
	
	private static final String ERROR_GENERATING_INCHI_MOLECULE_IS_NULL = "Error generating InChI (without stereochemistry) for molecule {}. Molecule is null.";
	private static final String ERROR_MOLECULE_IS_NULL_NO_INCHI = "Molecule is null, cannot generate InChI";
	private static final String ERROR_INCHI_TO_STRUCTURE = "Error, InChIToStructure object is null!";
	private static final String ERROR_MOLECULE_IS_NULL = "Error: Molecule is null when getting the heavy atom count for smiles ";
	private static final String ERROR_GENERATING_INCHI_TO_STRUCTURE = "Error generating InChiToStructure";
	private static final String ERROR_GENERATING_MOLECULE_FROM_SMILES = "Error generating molecule from SMILES. Please make sure the SMILES is valid.";
	private static final String ERROR_GENERATING_SMILES = "Error generating SMILES for compound {}";	
	private static final String ERROR_INCHI_GENERATOR = "Could not initialize InChIGeneratorFactory or get InChIGenerator for {}";
	private static final String CDK_INTERNAL_ERROR_WHEN_ADDING_IMPLICIT_HYDROGENS = "CDK internal error when adding implicit hydrogens: {}";
	private static final String ERROR_STRUCTURE_GENERATION_FAILED = "Structure generation failed: {} : {}";
	private static final String PARENT_HAS_MULTIPLE_COMPONENTS = "Removing parent compound {} with SMILES {} because it has multiple components.";
	private static final String DEBUG_COPY_AND_SUPPRESSED_HYDROGENS = "Doing copy and suppressed hydrogens for molecule {}";
	private static final String INCHI_WARNING = "InChi warning: {}";
	private static final String UTILITY_CLASS = "Utility class";
	private static final String EMPTY_STRING = "";
	
	private MoleculeManipulator() {
	    throw new IllegalStateException(UTILITY_CLASS);
	  }

	private static final Logger logger = LoggerFactory.getLogger(MoleculeManipulator.class.getName());
	
	
	// --- molecule size ---
	
	public static int getHeavyAtomCount(final IAtomContainer molecule) {
				
        int heavyAtomCounter = 0;
        for (IAtom a : molecule.atoms()) {
        		if (a.getAtomicNumber() > 1) { 
        			heavyAtomCounter++;
        		}
        }
        return heavyAtomCounter;
	}
	
	public static int getHeavyAtomCount(final String smiles) {
		
		IAtomContainer molecule = MoleculeManipulator.generateMoleculeFromSmiles(smiles);
		Assert.notNull(molecule, ERROR_MOLECULE_IS_NULL + smiles);
		
        return getHeavyAtomCount(molecule);
	}
	
	
	public static Boolean moleculeIsLargeEnough(String smiles, int heavyAtomCountCutoff) {
		
		int numHeavyAtoms = getHeavyAtomCount(smiles);
		
		return (numHeavyAtoms >= heavyAtomCountCutoff);
	}
	
	public static Boolean moleculeIsLargeEnough(IAtomContainer molecule, int heavyAtomCountCutoff) {
		
		int numHeavyAtoms = getHeavyAtomCount(molecule);
		
		return (numHeavyAtoms >= heavyAtomCountCutoff);
	}

	
	// --- check whether a molecule is interesting (i.e. not only hydrogens, not water) ---
	
	public static Boolean checkIfOnlyHydrogens(String smiles) {
		
		if (smiles.matches("^[H\\]\\[]*$")) {  // also works: "^[H\\Q]\\E\\Q[\\E]*$"
			logger.info("Product consists only of hydrogens! Removing this one.");
			return true;
		}
		return false;
	}

	public static Boolean checkIfOnlyHydrogens(IAtomContainer molecule) {
		return (getHeavyAtomCount(molecule) == 0);
	}
	
	public static Boolean checkIfWater(IAtomContainer molecule) {
		
		if (getHeavyAtomCount(molecule) == 1) {
	        for (IAtom a : molecule.atoms()) {
	        		if (a.getAtomicNumber() == 8) { 
	        			return true;
	        		}
	        }
	        return false;
		} else {
			return false;
		}
	}
	
	
	// --- salts ---
	
	public static void getMainPartOfSalt(BasicMolecule parent) {
		
		String[] components = parent.getSmiles().split("\\.");
		if (components.length > 2) {
			logger.info(PARENT_HAS_MULTIPLE_COMPONENTS, parent.getId(), parent.getSmiles());
			return;
		}
				
		String newSmiles = null;
		
		for (String component : components) {
			logger.debug("component: {}", component);
			if ( !(component.equals("[K+]") || component.equals("[Na+]") || component.equals("[Ca++]")) ) {
				// keep this component, but make sure only one component is kept
				if (newSmiles != null) {
					logger.info(PARENT_HAS_MULTIPLE_COMPONENTS, parent.getId(), parent.getSmiles());
					return;
				}
				newSmiles = component;
			}
		}
		
		if (newSmiles != null) {
			parent.setSmiles(newSmiles);
			parent.setInchi(MoleculeManipulator.generateInchiWithoutStereoFromSmiles(newSmiles));
		}
	}

	
	// --- generating coordinates ---
		
	// needed to write SD files and for depiction in results HTML file
	public static IAtomContainer generate2dCoordinates(IAtomContainer mol){ // modified from WriteResultsAsChemDoodleHTML from smartcyp package because sdf much less complicated with new CDK version

		StructureDiagramGenerator sdg = new StructureDiagramGenerator();

		// first check whether the molecule already has 2D coordinates, then generate them if necessary
		if (GeometryTools.has2DCoordinates(mol)) {
			logger.info("predicted metabolite already had 2D coordinates");
			return mol; 
			
		} else { // try to generate 2D coordinates
			
			try {
				sdg.generateCoordinates(mol);
			} catch (CDKException e) {
				logger.error("Error generating 2D coordinates for {}.", MoleculeManipulator.generateSmiles(mol));
			}	

			if (GeometryTools.has2DCoordinates(mol)) {
				return  mol;
			} else {
				logger.error("Generating 2D coordinates for {} failed.", MoleculeManipulator.generateSmiles(mol));
				return null;
			}
		}	
	}

	
	// -----------------conversion to PredictedMolecule---------------
	
	
	public static PredictedMolecule convertIAtomContainerToPredictedMoleculeWithoutStereochemistry(IAtomContainer atomContainer) {
		
		return convertIAtomContainerToPredictedMolecule(atomContainer, false);
	}
	
	
	public static PredictedMolecule convertIAtomContainerToPredictedMoleculeWithStereochemistry(IAtomContainer atomContainer) {
		
		return convertIAtomContainerToPredictedMolecule(atomContainer, true);
	}
	
	
	public static PredictedMolecule convertIAtomContainerToPredictedMolecule(IAtomContainer atomContainer, Boolean useStereochemistry) {
		
		PredictedMolecule molecule = new PredictedMolecule();
		
		if (useStereochemistry) {
			molecule.setInchi(generateInchi(atomContainer));
		} else {
			molecule.setInchi(generateInchiWithoutStereo(atomContainer));
		}
		
		molecule.setSmiles(generateSmiles(atomContainer, atomContainer.getProperty(Globals.ID_PROP)));
		if (molecule.getSmiles().equals(EMPTY_STRING)) {
			logger.info("Couldn't generate SMILES. Trying to generate SMILES without stereochemistry information.");
			molecule.setSmiles(generateSmilesWithoutStereo(atomContainer, atomContainer.getProperty(Globals.ID_PROP)));
			
			if (logger.isInfoEnabled()) {
				logEmptySmilesInchi(atomContainer, molecule);
			}
		}

		setPropertiesForPredictedMolecule(atomContainer, molecule);
		
		return molecule;
	}


	private static void logEmptySmilesInchi(IAtomContainer atomContainer, PredictedMolecule molecule) {
		
		if (molecule.getSmiles().equals(EMPTY_STRING)) {
			if (molecule.getInchi().equals(EMPTY_STRING) || molecule.getInchi() == null) {
				logger.info("Couldn't generate SMILES or InChI for molecule {}", (String) atomContainer.getProperty(Globals.ID_PROP));
			} else {
				logger.info("Couldn't generate SMILES for molecule {} with InChI {}.", atomContainer.getProperty(Globals.ID_PROP), molecule.getInchi());
			}
		} else {
			logger.info("Successfully generated SMILES (without stereochemistry information) for molecule {}: {}. Transformation was {}.", 
					(String) atomContainer.getProperty(Globals.ID_PROP), molecule.getSmiles(), atomContainer.getProperty(PredictedMolecule.TRANSFORMATION_NAME_PROPERTY));
		}
	}


	private static void setPropertiesForPredictedMolecule(IAtomContainer atomContainer, PredictedMolecule molecule) {
		
		molecule.setParentID(atomContainer.getProperty(Globals.ID_PROP));  // this should be the ID of the parent molecule, if set
		if (atomContainer.getProperty(PredictedMolecule.PRIORITY_SCORE_PROPERTY) != null) {
			molecule.setPriorityScore(atomContainer.getProperty(PredictedMolecule.PRIORITY_SCORE_PROPERTY));
		}
		if (atomContainer.getProperty(PredictedMolecule.TRANSFORMATION_NAME_PROPERTY) != null) {
			molecule.setTransformationName(atomContainer.getProperty(PredictedMolecule.TRANSFORMATION_NAME_PROPERTY));
		}
		if (atomContainer.getProperty(PredictedMolecule.MADE_SOM_CUTOFF_PROPERTY) != null) {
			molecule.setMadeSoMCutoff(atomContainer.getProperty(PredictedMolecule.MADE_SOM_CUTOFF_PROPERTY));
		}
		if (atomContainer.getProperty(PredictedMolecule.PHASE_PROPERTY) != null) {
			molecule.setMetabolismPhase(atomContainer.getProperty(PredictedMolecule.PHASE_PROPERTY));
		} else {
			logger.error("Prediction's phase is null");
		}
	}
	

	
	//-----------------SMILES generator-------------------

	public static String generateSmiles(final IAtomContainer molecule, final String molId) {
		
		// BEWARE: returned SMILES can be null

		String smiles = EMPTY_STRING;
		final SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Absolute);  // absolute is like isomeric but also canonical
		// stopped using SmiFlavor.UseAromaticSymbols. It's not recommended for portability, according to CDK. CDK is unable to kekulize molecules created from SMILES generated this way.
		try {
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);

		} catch (NullPointerException n ) {
			addConvertToExplicitHydrogens(molecule);
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);
			return smiles;
		}
		return smiles;
	}
	
	public static String generateSmilesWithoutStereo(final IAtomContainer molecule, final String molId) {
		
		// BEWARE: returned SMILES can be null

		String smiles = EMPTY_STRING;
		final SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Unique);  // use unique (canonical) instead of absolute to not include stereochem info
		// stopped using SmiFlavor.UseAromaticSymbols. It's not recommended for portability, according to CDK. CDK is unable to kekulize molecules created from SMILES generated this way.
		try {
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);

		} catch (NullPointerException n ) {
			addConvertToExplicitHydrogens(molecule);
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);
			return smiles;
		}
		return smiles;
	}

	public static String convertSmilesToWithoutStereo(final String oldSmiles) {

		String smiles = EMPTY_STRING;
		IAtomContainer molecule = generateMoleculeFromSmiles(oldSmiles);
		String molId = EMPTY_STRING;
		
		final SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Unique);  // use unique (canonical) instead of absolute to not include stereochem info
		try {
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);

		} catch (NullPointerException n ) {
			addConvertToExplicitHydrogens(molecule);
			smiles = tryToCreateSmiles(molecule, molId, smiles, sg);
			return smiles;
		}
		return smiles;
	}

	private static String tryToCreateSmiles(final IAtomContainer molecule, final String molId, String smiles, final SmilesGenerator sg) {
		try {
			smiles = sg.create(molecule);

		} catch (CDKException e) {
			logger.error(ERROR_GENERATING_SMILES, molId, e);
			return smiles;
		}
		return smiles;
	}

	public static String generateSmiles(final IAtomContainer molecule) {
		final String molId = EMPTY_STRING;
		return generateSmiles(molecule, molId);
	}
	
	public static String generateSmilesWithoutStereo(final IAtomContainer molecule) {
		final String molId = EMPTY_STRING;
		return generateSmilesWithoutStereo(molecule, molId);
	}
	
	public static String generateSmilesWithoutExplicitHydrogens(final IAtomContainer molecule) {
		// for readability of ouptut files

		final String molId = EMPTY_STRING;
//		addConvertToExplicitHydrogens(molecule);  // trying to add implicit hydrogens very often results in an IAtom is not typed error, which does not seem to 
		// affect whether or not the suppression of hydrogens works
		// instead, just percieve and configure:
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
		} catch (CDKException e) {
			logger.error("Error percieving atom types.");
		}
		
		logger.debug(DEBUG_COPY_AND_SUPPRESSED_HYDROGENS, (String) molecule.getProperty(Globals.ID_PROP));
	
		IAtomContainer copy = AtomContainerManipulator.copyAndSuppressedHydrogens(molecule);
		return generateSmiles(copy, molId);
	}
	
	public static String generateSmilesFromInchi(final String inchi) {
		final IAtomContainer molecule = generateMoleculeFromInchi(inchi);
		return generateSmiles(molecule);
	}

	
	
	// --------------Generate IAtomContainer----------------

	public static IAtomContainer generateMoleculeFromSmiles(final String smiles) {
		final SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
	    IAtomContainer molecule = null;
		try {
			molecule = sp.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			logger.error(ERROR_GENERATING_MOLECULE_FROM_SMILES, e); 
			// could set < sp.kekulise(false); > (kekulization occurs automatically by default) and then try again, but this is not recommended (see John Mayfield's 2013 blog post)
		}
		return molecule;
	}
	
	public static IAtomContainer generateMoleculeFromInchi(final String inchi) {
		logger.warn("Generating a molecule from InChI! This should only be done as a last resort.");
		final InChIToStructure intostruct = getInChIToStructureObject(inchi);
		checkStructureFromInchi(intostruct); //check return status of structure generation
		return intostruct.getAtomContainer();
	}

	private static InChIToStructure getInChIToStructureObject(final String inchi) {
		InChIToStructure intostruct = null;
		try {
			final InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
			intostruct = factory.getInChIToStructure(inchi, SilentChemObjectBuilder.getInstance());
		} catch (CDKException e) {
			logger.error(ERROR_GENERATING_INCHI_TO_STRUCTURE, e);
		}
		return intostruct;
	}

	private static void checkStructureFromInchi(InChIToStructure intostruct) {
		Assert.notNull(intostruct, ERROR_INCHI_TO_STRUCTURE);
		INCHI_RET ret = intostruct.getReturnStatus();
		if (ret == INCHI_RET.WARNING) { // Structure generated, but with warning message
			logger.warn(INCHI_WARNING, intostruct.getMessage());
		} else if (ret != INCHI_RET.OKAY && logger.isErrorEnabled()) { // Structure generation failed
			logger.error(ERROR_STRUCTURE_GENERATION_FAILED, ret.toString(), intostruct.getMessage());
		}
	}
	
	
	// -----------Manipulating hydrogens----------------

	public static void addImplicitHydrogens(IAtomContainer molecule) {
		
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
		} catch (CDKException e1) {
			logger.error("Error perceiving atom types");
		}

		CDKHydrogenAdder adder;
		adder = CDKHydrogenAdder.getInstance(SilentChemObjectBuilder.getInstance());
		try {
			adder.addImplicitHydrogens(molecule);
		} catch (Exception e) {
			logger.error(CDK_INTERNAL_ERROR_WHEN_ADDING_IMPLICIT_HYDROGENS, e.toString());  // e.g. IAtom is not typed error
		} 
	}
	
	public static void addConvertToExplicitHydrogens(IAtomContainer molecule) {
		CDKHydrogenAdder adder;
		adder = CDKHydrogenAdder.getInstance(SilentChemObjectBuilder.getInstance());
		try {
			adder.addImplicitHydrogens(molecule);
		} catch (Exception e) {
			logger.error(CDK_INTERNAL_ERROR_WHEN_ADDING_IMPLICIT_HYDROGENS, e.toString());  // e.g. IAtom is not typed error
			
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				adder.addImplicitHydrogens(molecule);
			} catch (CDKException e1) {
				logger.error("Tried perceiving atom types and then adding implicit hydrogens but still got CDK error");
			}
			
			
		} finally {
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
		}
	}
	
	
	// ----------Aromaticity------------------
	
	public static boolean detectAromaticity(IAtomContainer molecule) throws CDKException { // exception means there was a problem detecting the cycles
		// Note that this is not the same way that aromaticity is detected for FAME 3.
		// This aromaticity model was implemented for performing transformations with Ambit SMIRKS. The standard CDK aromaticity model used in Ambit SMIRKS (and also in FAME 3) 
		//    does not properly recognize aromaticity of aromatic rings with heterocyclic atoms outside the ring.

		Aromaticity aromaticity = new Aromaticity(ElectronDonation.daylight(), Cycles.or(Cycles.all(), Cycles.relevant())); 
		// tried Cycles.cdkAromaticSet() vs Cycles.all() but for ring systems with 3 or fewer rings, it should be the same according to the doc

		return aromaticity.apply(molecule);
	}
	
	
	// ---------------Kekulization ------------------
	
	public static String kekulizeMoleculeSmiles(String smiles) {
		
		IAtomContainer molecule = generateMoleculeFromSmiles(smiles);
		kekulizeMolecule(molecule);
		return generateSmiles(molecule);
	}
	
	
	public static void kekulizeMolecule(IAtomContainer molecule) {
		try {
			Kekulization.kekulize(molecule);
		} catch (CDKException e) {
			logger.error("Error kekulizing molecule {}", generateSmiles(molecule));
		}
	}

	
	//----------- InChI generator ---------------
	
	public static String generateInchi(final IAtomContainer molecule, final String id) {
		
		// BEWARE: returned InChI can be null
		
		if (molecule == null) {
			logger.error(ERROR_MOLECULE_IS_NULL_NO_INCHI);
			return null;
		}
		
		InChIGeneratorFactory factory;
		InChIGenerator generator;
		try {
			factory = InChIGeneratorFactory.getInstance();
			generator = factory.getInChIGenerator(molecule);  // default: include absolute stereo
			
		} catch (CDKException e) {
			logger.error(ERROR_INCHI_GENERATOR, id, e);
			return EMPTY_STRING;
		}
		return generator.getInchi();
	}
	
	public static String generateInchiWithoutStereo(final IAtomContainer molecule, final String id) {
		
		// BEWARE: returned InChI can be null
		
		if (molecule == null) {
			logger.error(ERROR_MOLECULE_IS_NULL_NO_INCHI);
			return null;
		}
		
		InChIGeneratorFactory factory;
		InChIGenerator generator;
		try {
			factory = InChIGeneratorFactory.getInstance();
			ArrayList<net.sf.jniinchi.INCHI_OPTION> options = new ArrayList<>();
			options.add(INCHI_OPTION.SNon);   // option to not use stereochemistry information
			generator = factory.getInChIGenerator(molecule, options); 
			
		} catch (CDKException e) {
			logger.error(ERROR_INCHI_GENERATOR, id, e);
			return EMPTY_STRING;
		}
		return generator.getInchi();
	}
	
	public static String generateInchi(final IAtomContainer molecule) {
		
		String id = EMPTY_STRING;
		if (molecule.getProperty(Globals.ID_PROP) != null) {
			id = molecule.getProperty(Globals.ID_PROP);
		}
		return generateInchi(molecule, id);
	}
	
	public static String generateInchiWithoutStereo(final IAtomContainer molecule) {
		
		String id = EMPTY_STRING;
		if (molecule.getProperty(Globals.ID_PROP) != null) {
			id = molecule.getProperty(Globals.ID_PROP);
		}
		
		return generateInchiWithoutStereo(molecule, id);
	}
	
	public static String generateInchiFromSmiles(final String smiles) {
		final IAtomContainer molecule = generateMoleculeFromSmiles(smiles);
		if (molecule == null) {
			logger.error(ERROR_GENERATING_INCHI_MOLECULE_IS_NULL, smiles);
			return null;
		}
		return generateInchi(molecule);
	}
	
	public static String generateInchiWithoutStereoFromSmiles(final String smiles) {
		final IAtomContainer molecule = generateMoleculeFromSmiles(smiles);
		if (molecule == null) {
			logger.error(ERROR_GENERATING_INCHI_MOLECULE_IS_NULL, smiles);
			return null;
		}
		return generateInchiWithoutStereo(molecule);
	}
	
	
	//-------------Duplicate molecule removal--------------
	
	public static IAtomContainerSet removeDuplicates(final IAtomContainerSet molecules) {
		Set<String> inchis = new HashSet<>();
		IAtomContainerSet setWithoutDuplicates = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);
		for (IAtomContainer molecule : molecules.atomContainers()) {
			addIfNewInchi(inchis, setWithoutDuplicates, molecule);
		}
		return setWithoutDuplicates;
	}

	private static void addIfNewInchi(Set<String> inchis, IAtomContainerSet setWithoutDuplicates, final IAtomContainer molecule) {
		if (inchis.add(MoleculeManipulator.generateInchi(molecule))) {
			setWithoutDuplicates.addAtomContainer(molecule);
		}
	}

}
