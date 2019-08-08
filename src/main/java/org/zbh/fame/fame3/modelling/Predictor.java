package org.zbh.fame.fame3.modelling;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.utils.data.FAMEMolSupplier;
import org.zbh.fame.fame3.utils.data.Predictions;

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
    private boolean use_FAMEScore;
    private int max_threads;
    private String decision_threshold;

	public Predictor(Globals globals, FAMEMolSupplier supplier) {
        if (globals.isValid()) {
            this.globals = globals;
        } else {
            System.err.println(globals.toString());
            throw new IllegalArgumentException("Global parameters are invalid. Aborting...");
        }
        this.supplier = supplier;
		this.predictions = new ArrayList<>();
		this.save_predictions = false;
		this.use_FAMEScore = globals.use_AD;
		this.max_threads = globals.cpus;
		this.decision_threshold = globals.decision_threshold;
	}

	public Predictor(
			Globals globals
			, FAMEMolSupplier supplier
			, boolean save_predictions
			, boolean use_FAMEScore
			, int max_threads
			, String decision_threshold
	) {
		this(globals, supplier);
		this.save_predictions = save_predictions;
		this.use_FAMEScore = use_FAMEScore;
		this.max_threads = max_threads;
		this.decision_threshold = decision_threshold;
	}

	public void calculate() throws InterruptedException{
		System.out.flush();
		System.err.flush();
		ExecutorService executor;
		if (max_threads <= 0) {
			executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		} else {
			executor = Executors.newFixedThreadPool(max_threads);
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
						, use_FAMEScore
						, decision_threshold
				);
			} else {
				worker = new PredictorWorkerThread(
						next_mol
						, this.globals
						, null
						, use_FAMEScore
						, decision_threshold
				);
			}
		    executor.execute(worker);
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
	}

	public List<Predictions> getPredictions() {
	    return predictions;
    }
}
