package org.zbh.fame.fame3.globals;

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

import java.io.*;
import java.util.*;

/**
 * A simple class holding some useful global constants that are used
 * to configure the scripts.
 *
 * Created by sicho on 10/5/16.
 */
public class Globals {
    public final String AD_model_path;
    public final String AD_model_attrs_path;
    public String pmml_path;
    public String model_code;
    public String model_name;
    public Modeller modeller;
    public String input_sdf;
    public List<String> input_smiles = new ArrayList<>();
    public int input_number;
    public String output_dir;
    public String model_dir;
    public String encoders_json;
    public String imputation_json;
    public Map<String, String> misc_params = new HashMap<>();
    public String target_var;
    public Set<String> desc_groups;
    public boolean generate_pngs;
    public boolean generate_csvs;
    public boolean use_AD;
    public Depictor depictor;
    public Depictor som_depictor;
    public Encoder at_encoder;
    public CircImputer circ_imputer;
    public List<File> js_code_paths = new ArrayList<>();
    public Map<String, String> model_map = new HashMap<>();

    public int circ_depth;
    public int fing_depth;
    public static final String CHEMDOODLE_ROOT = "/js/";
    public static final String MODELS_ROOT = "/models/";
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule
    public int cpus;

    public Globals(Namespace args_ns) throws Exception
    {
        model_map.put("P1+P2", "global");
        model_map.put("P1", "phaseI");
        model_map.put("P2", "phaseII");
        System.out.println("Selected model: " + args_ns.getString("model"));
        System.out.println("Output Directory: " + args_ns.getString("output_directory"));
        this.output_dir = args_ns.getString("output_directory");
        this.model_name = model_map.get(args_ns.getString("model")) + "_" + args_ns.getString("depth");;
        this.generate_pngs = args_ns.getBoolean("depict_png");
        this.generate_csvs = args_ns.getBoolean("output_csv");
        this.use_AD = !args_ns.getBoolean("no_app_domain");
        this.target_var = model_map.get(args_ns.getString("model"));
        this.depictor = new Depictor();
        this.som_depictor = new Depictor(new Depictor.SoMColorer());
        this.circ_depth = Integer.parseInt(this.model_name.split("_")[1]);
        this.fing_depth = 10; // is always 10 because of the AD score

        cpus = args_ns.getInt("processors");
        model_code = this.model_name.split("_")[0] + "_cdk_fing_ccdk" + "_" + this.model_name.split("_")[1];
        desc_groups = new HashSet<>(
                Arrays.asList(
                        model_code.split("_")
                )
        );
        model_dir = MODELS_ROOT + this.model_code + "/";
        AD_model_path = MODELS_ROOT + "AD/" + "nns_" + target_var + ".ser";
        AD_model_attrs_path = MODELS_ROOT + "AD/" + "nns_attributes_" + target_var + ".ser";
        pmml_path = model_dir + "final_model.pmml";
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

        String decision_threshold = args_ns.getString("decision_threshold");
        if (!decision_threshold.equals("model")) {
            try {
                double df = Double.parseDouble(decision_threshold);
                if (df < 0 || df > 1) throw new NumberFormatException("Bad decision threshold value.");
            } catch (NumberFormatException exp) {
                System.err.println("Bad decision threshold.");
                exp.printStackTrace();
                System.exit(1);
            }
            misc_params.put("decision_threshold", decision_threshold);
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

        // init modeller
        this.modeller = new Modeller(this);
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

    public void setInputSDF(String sdf_path) throws FileNotFoundException {
        input_sdf = sdf_path;
        File sdf_file = new File(input_sdf);
        if (!sdf_file.exists()) {
            throw new FileNotFoundException("Input SDF '" + sdf_file.getAbsolutePath() + "' not found.");
        }
        input_smiles = new ArrayList<>();
    }

    public void setInputSmiles(List<String> smiles_input) {
        input_smiles = smiles_input;
        input_sdf = "";
    }
}
