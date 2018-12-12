package modelling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

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

    public void encode(IMolecule molecule) {
        for (IAtom atom : molecule.atoms()) {
            if (atom.getSymbol().equalsIgnoreCase("H")) {
                continue;
            }
            String current_val = atom.getProperty(descriptor).toString();
            atom.setProperty(descriptor, encoder_map.get(current_val));
        }
    }
}
