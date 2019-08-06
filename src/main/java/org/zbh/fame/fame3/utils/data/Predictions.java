package org.zbh.fame.fame3.utils.data;

import org.zbh.fame.fame3.modelling.Result;

import java.util.ArrayList;
import java.util.List;

public class Predictions {
    private String mol_name;
    private List<Double> probabilites;
    private List<Boolean> predictions;
    private List<String> atom_ids;
    private String prediction_HTML;
    private Exception error;

    public Predictions(
            String mol_name
            , List<Double> probabilites
            , List<Boolean> predictions
            , List<String> atom_ids
            , String prediction_HTML
    ) {
        this.mol_name = mol_name;
        this.probabilites = probabilites;
        this.predictions = predictions;
        this.atom_ids = atom_ids;
        this.prediction_HTML = prediction_HTML;
        this.error = null;
    }

    public Predictions(String mol_name) {
        this(
            mol_name
            , new ArrayList<>()
            , new ArrayList<>()
            , new ArrayList<>()
            , null
        );
    }

    public String getMolName() {
        return mol_name;
    }

    public List<Double> getProbabilites() {
        return probabilites;
    }

    public List<Boolean> getPredictions() {
        return predictions;
    }

    public List<String> getAtomIDs() {
        return atom_ids;
    }

    public String getPredictionHTML() {
        return prediction_HTML;
    }

    public Exception getError() {
        return error;
    }

    public void addResult(String atom_id, Result result) {
        atom_ids.add(atom_id);
        predictions.add(result.is_som);
        probabilites.add(result.probability_yes);
    }

    public void setPredictionHTML(String prediction_HTML) {
        this.prediction_HTML = prediction_HTML;
    }

    public void setError(Exception error) {
        this.error = error;
    }
}
