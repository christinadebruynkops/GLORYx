package org.zbh.fame.fame3.modelling;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.utils.data.FAMEMolSupplier;
import org.zbh.fame.fame3.utils.data.Predictions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Predictor {
    private Globals globals;
    private FAMEMolSupplier supplier;
    private List<Predictions> predictions;
    private boolean save_predictions;

	public Predictor(Globals globals, FAMEMolSupplier supplier, boolean save_predictions) {
        if (globals.isValid()) {
            this.globals = globals;
        } else {
            System.err.println(globals.toString());
            throw new IllegalArgumentException("Global parameters are invalid. Aborting...");
        }
        this.supplier = supplier;
		this.predictions = new ArrayList<>();
		this.save_predictions = save_predictions;
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
            IAtomContainer next_mol = supplier.getNext();
			Runnable worker;
            if (save_predictions) {
				Predictions prediction = new Predictions(next_mol.getProperty(Globals.ID_PROP).toString());
				predictions.add(prediction);
				worker = new PredictorWorkerThread(
						next_mol
						, this.globals
						, prediction
				);
			} else {
				worker = new PredictorWorkerThread(
						next_mol
						, this.globals
				);
			}
		    executor.execute(worker);
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		System.out.println();
	}

	public List<Predictions> getPredictions() {
	    return predictions;
    }
}
