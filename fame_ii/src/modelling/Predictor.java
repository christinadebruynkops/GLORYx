package modelling;

import globals.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import smartcyp.SMARTSnEnergiesTable;
import utils.MoleculeKUFAME;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Predictor {
    private Globals globals;

	public Predictor(Globals globals) {
        this.globals = globals;
	}

	public void calculate() throws IOException, InterruptedException, ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(globals.input_sdf), DefaultChemObjectBuilder.getInstance());
		ArrayList<IMolecule> molecules = new ArrayList<>();

//        int counter = 5;
		while (reader.hasNext()) {
			IMolecule molecule = (IMolecule) reader.next();
			if (molecule.getProperty(Globals.ID_PROP) == null) {
				System.err.println("WARNING: Failed to find SDF name field. Skipping...");
				continue;
			}
//			System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
			molecule.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP).toString().replaceAll("[^A-Za-z0-9]", "_"));
			try {
				MoleculeKUFAME mol_ku = new MoleculeKUFAME(molecule, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
				mol_ku.setProperties(molecule.getProperties());
				molecules.add(mol_ku);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
//			counter--;
//			if (counter == 0) {
//				break;
//			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
