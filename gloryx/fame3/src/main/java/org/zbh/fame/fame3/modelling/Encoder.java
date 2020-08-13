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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by sicho on 1/18/17.
 */
public class Encoder {

    private String descriptor;
    private HashMap<String, Integer> encoder_map = new HashMap<>();

    public Encoder(String descriptor, String encoders_json) throws IOException, JSONException {
        this.descriptor = descriptor;

        JSONObject obj = new JSONObject(encoders_json);
        JSONArray arr = obj.getJSONArray(this.descriptor);
        for (int i = 0; i < arr.length(); i++)
        {
            JSONArray arr2 = arr.getJSONArray(i);
            encoder_map.put(arr2.get(0).toString(), Integer.parseInt(arr2.get(1).toString()));
        }
    }

    public void encode(IAtomContainer molecule) throws Exception {
        for (IAtom atom : molecule.atoms()) {
            if (atom.getSymbol().equalsIgnoreCase("H")) {
                continue;
            }
            String current_val = atom.getProperty(descriptor).toString();
            
            if (current_val.equals("O.co2")) {
            	// FIXME: look into this and fix properly
            		current_val = "O.2";
            }

            	Integer encoded_val = encoder_map.get(current_val);
            	if (encoded_val == null) throw new Exception("Unknown atom type code:" + current_val);
            	atom.setProperty(descriptor, encoded_val);
        }
    }
}
