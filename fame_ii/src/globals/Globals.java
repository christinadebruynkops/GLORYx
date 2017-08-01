package globals;

import modelling.Encoder;
import modelling.Modeller;
import modelling.descriptors.circular.CircImputer;
import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;
import utils.Utils;
import utils.depiction.Depictor;

import java.io.*;
import java.util.*;

/**
 * A simple class holding some useful global constants that are used
 * to configure the scripts.
 *
 * Created by sicho on 10/5/16.
 */
public class Globals {
    public String pmml_path;
    public String model_code;
    public String model_name;
    public Modeller modeller;
    public String input_sdf;
    public int input_sdf_number;
    public String output_dir;
    public String model_dir;
    public String encoders_json;
    public String imputation_json;
    public Map<String, String> misc_params = new HashMap<>();
    public String target_var;
    public Set<String> desc_groups;
    public boolean generate_pngs = false;
    public boolean generate_csvs = false;
    public Depictor depictor;
    public Depictor som_depictor;
    public Encoder at_encoder;
    public CircImputer circ_imputer;
    public List<File> js_code_paths = new ArrayList<>();

    public int circ_depth;
    public int fing_depth;
    public static final String CHEMDOODLE_ROOT = "/utils/depiction/js/";
    public static final String MODELS_ROOT = "/modelling/models/";
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule

    public Globals(
            String input_sdf
            , String output_dir
            , String model_name
            , String target_var
            ) throws Exception
    {
        Map<String, Pair<String, Integer>> model_to_specs = new HashMap<>();
        model_to_specs.put("circCDK_ATF_1", new Pair("cdk_fing_ccdk", 1));
        model_to_specs.put("circCDK_ATF_6", new Pair("cdk_fing_ccdk", 6));
        model_to_specs.put("circCDK_4", new Pair("cdk_ccdk", 4));

        this.depictor = new Depictor();
        this.som_depictor = new Depictor(new Depictor.SoMColorer());
        this.model_name = model_name;
        this.circ_depth = model_to_specs.get(this.model_name).getValue();
        this.fing_depth = model_to_specs.get(this.model_name).getValue();

        this.input_sdf = input_sdf;
        File sdf_file = new File(input_sdf);
        if (!sdf_file.exists()) {
            throw new FileNotFoundException("Input SDF '" + sdf_file.getAbsolutePath() + "' not found.");
        }
        this.output_dir = output_dir;
        model_code = model_to_specs.get(this.model_name).getKey() + "_" + Integer.toString(this.circ_depth);
        desc_groups = new HashSet<>(
                Arrays.asList(
                        model_code.split("_")
                )
        );
        this.target_var = target_var;
        model_dir = MODELS_ROOT + this.target_var + "_" + this.model_code + "/";
        pmml_path = model_dir + "final_model.pmml";
        modeller = new Modeller(pmml_path, this.target_var);
        encoders_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "encoders.json"));
        at_encoder = new Encoder("AtomType", encoders_json);
        imputation_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "imputation.json"));
        circ_imputer = new CircImputer(imputation_json);

        String misc_params_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "misc_params.json"));
        JSONObject json = new JSONObject(misc_params_json);
        Iterator iterator = json.keys();
        while (iterator.hasNext()) {
            String param_name = iterator.next().toString();
            misc_params.put(param_name, json.getString(param_name));
        }

        // check files and create directories
        File outdir = new File(this.output_dir);
        if (!outdir.exists()) {
            outdir.mkdir();
        }
        File outdir_js = new File(this.output_dir, "ui");
        if (!outdir_js.exists()) {
            outdir_js.mkdir();
        }

        // write the necessary JavaScript code
        js_code_paths.add(new File(outdir_js, "ChemDoodleWeb.js"));
        js_code_paths.add(new File(outdir_js, "ChemDoodleWeb-libs.js"));
        for (File item : js_code_paths) {
            InputStream js_istream = this.getClass().getResourceAsStream(CHEMDOODLE_ROOT + item.getName());
            OutputStream js_ostram = new FileOutputStream(item);
            copyStreams(js_istream, js_ostram);
        }
    }

    private static void copyStreams(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        //read from is to buffer
        while((bytesRead = is.read(buffer)) !=-1){
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        //flush OutputStream to write any buffered data to file
        os.flush();
        os.close();
    }
}
