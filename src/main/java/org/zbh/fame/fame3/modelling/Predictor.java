package org.zbh.fame.fame3.modelling;

import org.zbh.fame.fame3.globals.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.zbh.fame.fame3.smartcyp.SMARTSnEnergiesTable;
import org.zbh.fame.fame3.utils.MoleculeKUFAME;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Predictor {
    private Globals globals;

	public Predictor(Globals globals) {
        if (globals.isValid()) {
            this.globals = globals;
        } else {
            System.err.println(globals.toString());
            throw new IllegalArgumentException("Global parameters are invalid. Aborting...");
        }
	}

	public void calculate() throws IOException, InterruptedException, ClassNotFoundException {
		ArrayList<IMolecule> molecules = new ArrayList<>();
		if (!globals.input_sdf.isEmpty()) {
			DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(globals.input_sdf), DefaultChemObjectBuilder.getInstance());

			int counter = 1;
			while (reader.hasNext()) {
				IMolecule molecule = (IMolecule) reader.next();
				SmilesGenerator smi_gen = new SmilesGenerator();
				if (molecule.getProperty(Globals.ID_PROP) == null) {
					molecule.setProperty(Globals.ID_PROP, molecule.getProperty("Identifier"));
					if (molecule.getProperty(Globals.ID_PROP) == null) {
						molecule.setProperty(Globals.ID_PROP, "mol_" + globals.input_number + "_" + counter);
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
			int counter = 1;
			for (String smiles : globals.input_smiles) {
				IMolecule mol = null;
//				smiles = smiles.replaceAll("[^\\-=#.$:()%+A-Za-z0-9\\\\/@\\]\\[]", "");
				smiles = smiles.replaceAll("[\"']", "");
				try {
					SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
					mol = sp.parseSmiles(smiles);
				} catch (InvalidSmilesException ise) {
					System.err.println("WARNING: SMILES parsing failed for: " + smiles);
					System.exit(1);
				}

				mol.setProperty(Globals.ID_PROP, "mol_" + globals.input_number + "_" + counter);
				System.out.println("Generating identifier for " + smiles + ": " + mol.getProperty(Globals.ID_PROP));
				try {
					MoleculeKUFAME mol_ku = new MoleculeKUFAME(mol, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
					mol_ku.setProperties(mol.getProperties());
					molecules.add(mol_ku);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}

				counter++;
			}
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
		molecules = null; // this is to save memory, GC should take care of the rest when a worker finishes processing a molecule -> we want molecules out of memory as soon as the descriptors are in the file
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
	}
}
