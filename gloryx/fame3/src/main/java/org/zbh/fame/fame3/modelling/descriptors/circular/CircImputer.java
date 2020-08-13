/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
 
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
