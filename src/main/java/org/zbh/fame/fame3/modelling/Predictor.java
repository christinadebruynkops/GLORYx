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
import org.zbh.fame.fame3.utils.data.FAMEMolSupplier;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Predictor {
    private Globals globals;
    private FAMEMolSupplier supplier;

	public Predictor(Globals globals, FAMEMolSupplier supplier) {
        if (globals.isValid()) {
            this.globals = globals;
        } else {
            System.err.println(globals.toString());
            throw new IllegalArgumentException("Global parameters are invalid. Aborting...");
        }
        this.supplier = supplier;
	}

	public void calculate() throws IOException, InterruptedException, ClassNotFoundException {
		System.out.flush();
		System.err.flush();
		ExecutorService executor = null;
		if (globals.cpus <= 0) {
			executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		} else {
			executor = Executors.newFixedThreadPool(globals.cpus);
		}
		while (supplier.hasNext()) {
			Runnable worker = new PredictorWorkerThread(
					supplier.getNext()
					, this.globals
			);
			executor.execute(worker);
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
	}
}
