package modelling;

import globals.Globals;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by sicho on 1/18/17.
 */
public class Encoder {

    private String descriptor;
    private HashMap<String, Integer> encoder_map = new HashMap<>();
    private static final String path = Globals.ENCODERS_PATH;

    public Encoder(String descriptor) throws IOException, JSONException {
        this.descriptor = descriptor;

        String encoders_json = "";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null)
        {
            encoders_json += line;
        }
        reader.close();

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
            String current_val = atom.getProperty(descriptor).toString();
            atom.setProperty(descriptor, encoder_map.get(current_val));
        }
    }
}
