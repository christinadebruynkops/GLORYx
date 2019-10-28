package org.zbh.fame.fame3.modelling.descriptors.circular;

import org.json.JSONException;
import org.json.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sicho on 1/19/17.
 */
public class CircImputer {

    private Map<String, Double> desc_map = new HashMap<>();

    public CircImputer(String imputation_file) throws JSONException {
        JSONObject json = new JSONObject(imputation_file);
        Iterator iterator = json.keys();
        while (iterator.hasNext()) {
            String desc_name = iterator.next().toString();
            double desc_val = json.getDouble(desc_name);
            desc_map.put(desc_name, desc_val);
        }
    }

    public void impute(IAtomContainer mol, Set<String> sigs) {
        for (IAtom atm : mol.atoms()) {
            for (String desc_name : desc_map.keySet()) {
                if (!atm.getSymbol().equals("H") && atm.getProperty(desc_name) == null) {
                    atm.setProperty(desc_name, desc_map.get(desc_name));
                }
            }
        }
    }
}
