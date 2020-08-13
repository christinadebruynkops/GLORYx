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

package org.zbh.fame.fame3.modelling;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private boolean save_molecules;
    private IAtomContainerSet predictedMolecules;
    
	public Predictor(Globals globals, FAMEMolSupplier supplier) {
        if (globals.isValid()) {
            this.globals = globals;
        } else {
            logger.error(globals.toString());
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
	
	public Predictor( // option save_molecules added for GLORY - be careful about causing a memory leak this way
			Globals globals
			, FAMEMolSupplier supplier
			, boolean save_predictions
			, boolean save_molecules
			, boolean use_FAMEScore
			, int max_threads
	) {
		this(globals, supplier);
		this.save_predictions = save_predictions;
		this.use_FAMEScore = use_FAMEScore;
		this.max_threads = max_threads;
		this.decision_threshold = globals.decision_threshold;
		
		this.save_molecules = save_molecules;
		predictedMolecules = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Predictor.class.getName());


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
			} else if (save_molecules) { // be careful - this assumes the use case for GLORY is true, i.e. save_predictions=FALSE and save_molecules=TRUE
				worker = new PredictorWorkerThread(
						next_mol
						, this.globals
						, true
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
			if (save_molecules && predictedMolecules != null) {
				if (next_mol != null) {
					predictedMolecules.addAtomContainer(next_mol);
				} else {
					logger.error("Error saving molecule. It is null after prediction and shouldn't be.");
				}
				
			}
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
	}

	public List<Predictions> getPredictions() {
	    return predictions;
    }
	
	public IAtomContainerSet getMoleculesWithSoMAnnotations() {
		return predictedMolecules;
	}
}
