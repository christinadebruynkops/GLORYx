package modelling;

import globals.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import smartcyp.SMARTSnEnergiesTable;
import utils.MoleculeKUFAME;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Predictor {
    private Globals globals;
    
    private int moleculeID = -1;

	public Predictor(Globals globals) {
        this.globals = globals;
	}
	
	// original functionality used in FAME 3 - molecules are set to null to save memory once the files are written - this should be completely unnecessary with modern versions of java
	public void calculate() throws ClassNotFoundException, IOException, InterruptedException {
		List<IAtomContainer> molecules = calculateAndReturn();
		molecules = null;  // this is to save memory, GC should take care of the rest when a worker finishes processing a molecule -> we want molecules out of memory as soon as the descriptors are in the file
	}
	
	
	// needed for metabolite prediction
	public List<IAtomContainer> calculateAndReturnWithoutMultithreading(List<String> inputSmiles) throws IOException, ClassNotFoundException {
		
		List<IAtomContainer> molecules = calculateDescriptorsForSMILES(inputSmiles);
		for (int i = 0; i < molecules.size(); i++) {
			Runnable worker = new PredictorWorkerThread(
					molecules.get(i)
					, this.globals
			);
			worker.run();
		}
		return molecules;
	}
	
    public void setMoleculeID(int id) {
    		this.moleculeID = id;
    }

	public List<IAtomContainer> calculateAndReturn() throws IOException, InterruptedException, ClassNotFoundException {
		ArrayList<IAtomContainer> molecules = new ArrayList<>();
		if (!globals.input_sdf.isEmpty()) {
			DefaultIteratingChemObjectReader reader = (IteratingSDFReader) new IteratingSDFReader(new FileInputStream(globals.input_sdf), DefaultChemObjectBuilder.getInstance());

			int counter = 1;
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer) reader.next();
				SmilesGenerator smi_gen = new SmilesGenerator();
				if (molecule.getProperty(Globals.ID_PROP) == null) {
					molecule.setProperty(Globals.ID_PROP, molecule.getProperty("Identifier"));
					if (molecule.getProperty(Globals.ID_PROP) == null) {
						molecule.setProperty(Globals.ID_PROP, "mol_" + Integer.toString(globals.input_number) + "_" + Integer.toString(counter));
						System.err.println("WARNING: No SDF name field found for molecule:\n" + smi_gen.createSMILES(molecule) + ".\nUsing a generated name: " + molecule.getProperty(Globals.ID_PROP));
					}
				} else {
//				System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
					molecule.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP).toString().replaceAll("[^A-Za-z0-9]", "_"));
				}
				try {
					MoleculeKUFAME mol_ku = new MoleculeKUFAME(molecule, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
					mol_ku.setProperties(molecule.getProperties());
					molecules.add(mol_ku);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				System.out.println("Successfully parsed structure for: " + molecule.getProperty(Globals.ID_PROP));
				counter++;
			}
		}
		if (!globals.input_smiles.isEmpty()) {
			molecules = calculateDescriptorsForSMILES(globals.input_smiles);
		}

		System.out.flush();
		System.err.flush();

		ExecutorService executor = null;
		if (globals.cpus <= 0) {
			executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		} else {
			executor = Executors.newFixedThreadPool(globals.cpus);
		}
		for (int i = 0; i < molecules.size(); i++) {
			Runnable worker = new PredictorWorkerThread(
					molecules.get(i)
					, this.globals
			);
			executor.execute(worker);
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		
		return molecules;
	}
	
	private ArrayList<IAtomContainer> calculateDescriptorsForSMILES(List<String> inputSmiles) {
		
		ArrayList<IAtomContainer> molecules = new ArrayList<>();
		
		int counter = 1;
					
		for (String smiles : inputSmiles) {
						
			IAtomContainer mol = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class); //null;
//				smiles = smiles.replaceAll("[^\\-=#.$:()%+A-Za-z0-9\\\\/@\\]\\[]", "");
			smiles = smiles.replaceAll("[\"']", "");
			try {
				SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
				mol = sp.parseSmiles(smiles);
				
			} catch (InvalidSmilesException ise) {
//				logger.error("ERROR: SMILES parsing failed for: " + smiles + ". Skipping this input molecule.");
				continue;
			}
			
			if (this.moleculeID == -1) {
				mol.setProperty(Globals.ID_PROP, "mol_" + Integer.toString(globals.input_number) + "_" + Integer.toString(counter));
//				logger.info("Generating identifier for " + smiles + ": " + mol.getProperty(Globals.ID_PROP));
			} else {
				if (inputSmiles.size() > 1) {
//					logger.error("Error generating unique IDs for input molecules.");
				}
				mol.setProperty(Globals.ID_PROP, "mol_" + this.moleculeID);
			}
			try {
				MoleculeKUFAME mol_ku = new MoleculeKUFAME(mol, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
				mol_ku.setProperties(mol.getProperties());
				molecules.add(mol_ku);
			} catch (CloneNotSupportedException e) {
//				logger.error("Could not clone molecule.", e);
			}

			counter++;
		}
		return molecules;
	}
	
}
