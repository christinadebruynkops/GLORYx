package org.zbh.fame.fame3.globals;

import org.json.JSONException;
import org.xml.sax.SAXException;
import org.zbh.fame.fame3.modelling.Encoder;
import org.zbh.fame.fame3.modelling.Modeller;
import org.zbh.fame.fame3.modelling.descriptors.circular.CircImputer;
import net.sourceforge.argparse4j.inf.Namespace;
import org.json.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.qsar.AtomValenceTool;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.zbh.fame.fame3.utils.Utils;
import org.zbh.fame.fame3.utils.depiction.Depictor;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A simple class holding some useful global constants that are used
 * to configure the scripts.
 *
 * Created by sicho on 10/5/16.
 */
public class Globals {
    public String AD_model_path;
    public String AD_model_attrs_path;
    public String pmml_path;
    public String model_code;
    public String model_name;
    public Modeller modeller;
    public String output_dir;
    public String model_dir;
    public String encoders_json;
    public String imputation_json;
    public Map<String, String> model_hyperparams;
    public String target_var;
    public Set<String> desc_groups;
    public boolean generate_pngs;
    public boolean generate_csvs;
    public boolean use_AD;
    public Depictor depictor;
    public Depictor som_depictor;
    public Encoder at_encoder;
    public CircImputer circ_imputer;
    public Map<String, String> model_map;

    public Integer circ_depth;
    public Integer fing_depth;
    public static final String CHEMDOODLE_ROOT = "/js/";
    public static final String MODELS_ROOT = "/models/";
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule
    public static final String FILE_PATH_PROP = "FAME:File";
    public int cpus;
    public String decision_threshold;

    public Globals() throws JSONException, IOException {
        this.model_map = new HashMap<>();
        model_map.put("P1+P2", "global");
        model_map.put("P1", "phaseI");
        model_map.put("P2", "phaseII");

        this.model_name = null;
        this.model_code = null;
        this.target_var = null;
        this.circ_depth = 10;
        this.fing_depth = 10;
        this.decision_threshold = "model";

        this.output_dir = null;
        this.generate_pngs = false;
        this.generate_csvs = false;
        this.use_AD = true;
        this.cpus = 0;

        initPNGDepictor();

        initModels();

        // hack to prevent NullPointerErrors when AtomValenceTool.getValence is called simultaneously from the workers in order to do AtomValenceDescriptor.calculate()
        getValenceForDummyAtom();
    }

    public Globals(String model_definition, int bond_depth, boolean init_models, boolean init_directories) throws JSONException, IOException {
        this();

        this.model_name = model_map.get(model_definition) + "_" + bond_depth;
        this.model_code = this.model_name.split("_")[0] + "_cdk_fing_ccdk" + "_" + this.model_name.split("_")[1];
        this.target_var = model_map.get(model_definition);
        this.circ_depth = Integer.parseInt(this.model_name.split("_")[1]);
        this.fing_depth = 10; // is always 10 because of the AD score
        this.decision_threshold = "model";

        this.desc_groups = new HashSet<>();
        this.model_dir = null;
        this.pmml_path = null;
        this.encoders_json = null;
        this.at_encoder = null;
        this.imputation_json = null;
        this.circ_imputer = null;
        this.model_hyperparams = new HashMap<>();
        this.modeller = null;

        if (init_models) {
            initModels();
        }

        if (init_directories) {
            initDirectories();
        }
    }

    public Globals(Namespace args_ns) throws JSONException, IOException
    {
        this(args_ns.getString("model"), Integer.parseInt(args_ns.getString("depth")), false, false);
        System.out.println("Initializing and validating settings...");
        this.output_dir = args_ns.getString("output_directory");
        this.generate_pngs = args_ns.getBoolean("depict_png");
        this.generate_csvs = args_ns.getBoolean("output_csv");
        this.use_AD = !args_ns.getBoolean("no_app_domain");
        this.cpus = args_ns.getInt("processors");
        this.decision_threshold = args_ns.getString("decision_threshold");
        System.out.println("Selected model: " + args_ns.getString("model"));
        System.out.println("Model bond depth: " + this.circ_depth);
        System.out.println("Decision threshold: " + this.decision_threshold);
        System.out.println("Output Directory: " + this.output_dir);

        // init PNG depictor
        initPNGDepictor();

        //init models
        initModels();

        // init directories
        initDirectories();

        System.out.println("Global settings initialized.");
    }

    public void initModels() throws JSONException, IOException {
        if (model_code != null) {
            this.desc_groups = new HashSet<>(
                    Arrays.asList(
                            model_code.split("_")
                    )
            );
            this.model_dir = MODELS_ROOT + this.model_code + "/";
            this.pmml_path = model_dir + "final_model.pmml";
            this.encoders_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "encoders.json"));
            this.at_encoder = new Encoder("AtomType", encoders_json);
            this.imputation_json = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "imputation.json"));
            this.circ_imputer = new CircImputer(imputation_json);

            if (use_AD) {
                this.AD_model_path = MODELS_ROOT + "AD/" + "nns_" + target_var + ".ser";
                this.AD_model_attrs_path = MODELS_ROOT + "AD/" + "nns_attributes_" + target_var + ".ser";
            } else {
                this.AD_model_path = null;
                this.AD_model_attrs_path = null;
            }

            String model_hyperparams = Utils.convertStreamToString(this.getClass().getResourceAsStream(model_dir + "misc_params.json"));
            JSONObject json = new JSONObject(model_hyperparams);
            Iterator iterator = json.keys();
            while (iterator.hasNext()) {
                String param_name = iterator.next().toString();
                this.model_hyperparams.put(param_name, json.getString(param_name));
            }

            // init modeller
            try {
                this.modeller = new Modeller(this);
            } catch (JAXBException | SAXException | ClassNotFoundException e) {
                System.err.println("Failed to initialize and parse model: " + model_code);
                System.err.println("Settings might be invalid.");
                e.printStackTrace();
            }
        } else {
            this.model_dir = null;
            this.pmml_path = null;
            this.encoders_json = null;
            this.at_encoder = null;
            this.imputation_json = null;
            this.circ_imputer = null;
            this.modeller = null;
        }
    }

    public void initDirectories() throws IOException {
        if (output_dir != null) {
            // check files and create directories
            File outdir = new File(output_dir);
            if (!outdir.exists()) {
                outdir.mkdir();
            }
            File outdir_js = new File(output_dir, "ui");
            if (!outdir_js.exists()) {
                outdir_js.mkdir();
            }

            // write the necessary JavaScript code
            List<File> js_code_paths = new ArrayList<>();
            js_code_paths.add(new File(outdir_js, "ChemDoodleWeb.js"));
            js_code_paths.add(new File(outdir_js, "ChemDoodleWeb-libs.js"));
            for (File item : js_code_paths) {
                InputStream js_istream = this.getClass().getResourceAsStream(CHEMDOODLE_ROOT + item.getName());
                OutputStream js_ostram = new FileOutputStream(item);
                copyStreams(js_istream, js_ostram);
            }
        }
    }

    private void initPNGDepictor() {
        if (this.generate_pngs) {
            this.depictor = new Depictor();
            this.som_depictor = new Depictor(new Depictor.SoMColorer());
        } else {
            this.depictor = null;
            this.som_depictor = null;
        }
    }

    public boolean isValid() {
        if (
                (this.model_name != null)
                && (this.model_code != null)
                && (this.target_var != null)
                && (this.circ_depth != null)
                && (this.fing_depth != null)
                && (this.decision_threshold != null)
                && (this.desc_groups != null)
                && (this.model_dir != null)
                && (this.pmml_path != null)
                && (this.encoders_json != null)
                && (this.at_encoder != null)
                && (this.imputation_json != null)
                && (this.circ_imputer != null)
                && (this.model_hyperparams != null)
                && (this.modeller != null)
        ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder field_vals = new StringBuilder();
        for (Field f : getClass().getDeclaredFields()) {
            try {
                field_vals.append(f.getName());
                field_vals.append(": ");
                if (f.get(this) != null) {
                    field_vals.append(f.get(this).toString());
                } else {
                    field_vals.append("null");
                }
                field_vals.append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return field_vals.toString();
    }

    private void getValenceForDummyAtom() {
        // define a dummy carbon atom
        IAtom a = SilentChemObjectBuilder.getInstance().newInstance(IAtom.class);
        a.setSymbol("C");

        // call getValence to fill the valence table (so that it will not be null for future calls to getValence)
        AtomValenceTool.getValence(a);
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
