package org.zbh.fame.fame3.utils.data;

import org.zbh.fame.fame3.modelling.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Predictions {
    private String mol_name;
    private List<Result> results;
    private String mol_block;
    private String prediction_HTML;
    private Exception error;

    public Predictions(
            String mol_name
            , List<Result> results
            , String mol_block
            , String prediction_HTML
    ) {
        this.results = results;
        this.mol_name = mol_name;
        this.mol_block = mol_block;
        this.prediction_HTML = prediction_HTML;
        this.error = null;
    }

    public Predictions(String mol_name) {
        this(
            mol_name
            , new ArrayList<>()
            , null
            , null
        );
    }

    public String getMolName() {
        return mol_name;
    }

    public List<Double> getProbabilites() {
        return results.stream().map(item -> item.probability_yes).collect(Collectors.toList());
    }

    public List<Boolean> getPredictions() {
        return results.stream().map(item -> item.is_som).collect(Collectors.toList());
    }

    public List<String> getAtomIDs() {
        return results.stream().map(item -> item.atom_id).collect(Collectors.toList());
    }

    public List<Double> getFAMEScores() {
        return results.stream().map(item -> item.AD_score).collect(Collectors.toList());
    }

    public List<Result> getResults() {
        return results;
    }

    public String getMolBlock() {
        return mol_block;
    }

    public String getPredictionHTML() {
        return prediction_HTML;
    }

    public Exception getError() {
        return error;
    }

    public void addResult(Result result) {
        results.add(result);
    }

    public void setMolBlock(String mol_block) {
        this.mol_block = mol_block;
    }

    public void setPredictionHTML(String prediction_HTML) {
        this.prediction_HTML = prediction_HTML;
    }

    public void setError(Exception error) {
        this.error = error;
    }
}
