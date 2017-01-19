package globals;

import modelling.Modeller;
import utils.Depictor;
import utils.Sanitize;
import utils.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public Depictor depictor = new Depictor();
    public Depictor som_depictor = new Depictor(new Depictor.SoMColorer());

    public static final int circ_depth = 6;
    public static final int fing_depth = 6;
    public static final String MODELS_ROOT = "/modelling/models/"; // FIXME: the contents of this directory should be inside the jar file
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule

    public Globals(
            String input_sdf
            , String output_dir
            , String model_code
            , String target_var
            , boolean sanitize
            ) throws Exception
    {
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
        imputation_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "imputation.json"));

        // check files and create directories
        if (!new File(this.input_sdf).exists()) {
            throw new Exception("File not found: " + this.input_sdf);
        }
        File outdir = new File(this.output_dir);
        if (!outdir.exists()) {
            outdir.mkdir();
        }

        // sanitize the data if requested and save the path to the modified file
        if (this.sanitize) {
            this.input_sdf = Sanitize.sanitize(this);
        }
    }


}
