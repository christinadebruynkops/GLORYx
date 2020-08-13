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
