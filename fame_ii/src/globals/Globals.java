package globals;

import modelling.Encoder;
import modelling.Modeller;
import modelling.descriptors.circular.CircImputer;
import utils.Sanitize;
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
    public Modeller modeller;
    public String input_sdf;
    public String output_dir;
    public String model_dir;
    public String encoders_json;
    public String imputation_json;
    public String target_var;
    public Set<String> desc_groups;
    public boolean sanitize;
    public Depictor depictor;
    public Depictor som_depictor;
    public Encoder at_encoder;
    public CircImputer circ_imputer;
    public List<File> js_code_paths = new ArrayList<>();

    public static final int circ_depth = 6;
    public static final int fing_depth = 6;
    public static final String CHEMDOODLE_ROOT = "/utils/depiction/js/";
    public static final String MODELS_ROOT = "/modelling/models/";
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule

    public Globals(
            String input_sdf
            , String output_dir
            , String model_code
            , String target_var
            , boolean sanitize
            ) throws Exception
    {
        this.depictor = new Depictor();
        this.som_depictor = new Depictor(new Depictor.SoMColorer());

        this.input_sdf = input_sdf;
        this.output_dir = output_dir;
        desc_groups = new HashSet<>(
                Arrays.asList(
                        model_code.split("_")
                )
        );
        this.target_var = target_var;
        this.sanitize = sanitize;
        model_dir = MODELS_ROOT + this.target_var + "_" + model_code + "/";
        pmml_path = model_dir + "final_model.pmml";
        modeller = new Modeller(pmml_path, this.target_var);
        encoders_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "encoders.json"));
        at_encoder = new Encoder("AtomType", encoders_json);
        imputation_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "imputation.json"));
        circ_imputer = new CircImputer(imputation_json);

        // check files and create directories
        if (!new File(this.input_sdf).exists()) {
            throw new Exception("File not found: " + this.input_sdf);
        }
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

        // sanitize the data if requested and save the path to the modified file
        if (this.sanitize) {
            System.out.println("Sanitizing structures with babel...");
            this.input_sdf = Sanitize.sanitize(this);
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
