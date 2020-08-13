/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
   Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
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

package org.zbh.fame.fame3.globals;

import main.NearestNeighbourSearch;
import org.apache.commons.math3.util.Pair;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbh.fame.fame3.utils.Utils;
import org.zbh.fame.fame3.utils.depiction.Depictor;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A simple class holding some useful global constants that are used
 * to configure the prediction facilities. It is a wrong spaghetti way
 * to handle something like this, but oh well...
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
    public boolean generate_html; // new for GLORY
    public boolean use_AD;
    public Depictor depictor;
    public Depictor som_depictor;
    public Encoder at_encoder;
    public CircImputer circ_imputer;
    public Map<String, String> model_map;

    public Integer circ_depth;
    public Integer fing_depth;
    public static final String CHEMDOODLE_ROOT = "/js/";
    public static final String MODELS_ROOT = "models/"; //"/work/kops/metaboliteproject/fame3/models/"; // "resources/models/"; // "src/main/resources/models/" // currently just "models/" if exporting as jar
    public static final String ID_PROP = "cdk:Title"; // SDF file property variable holding the ID of the molecule
    public static final String FILE_PATH_PROP = "FAME:File";
    public int cpus;
    public String decision_threshold;
    
	private static final Logger logger = LoggerFactory.getLogger(Globals.class.getName());


    public Globals() throws JSONException, IOException {
        this.model_map = new HashMap<>();
        model_map.put("P1+P2", "global");
        model_map.put("P1", "phaseI");
        model_map.put("P2", "phaseII");
        model_map.put("UGT", "glucuronidation"); // added individual phase 2 reaction type model names
        model_map.put("NAT", "acetylation"); 
        model_map.put("GST", "gshconjugation");
        	model_map.put("MT", "methylation");
        	model_map.put("SULT", "sulfonation");

        this.model_name = null;
        this.model_code = null;
        this.target_var = null;
        this.circ_depth = 10;
        this.fing_depth = 10;
        this.decision_threshold = "model";

        this.desc_groups = new HashSet<>();
        this.model_dir = null;
        this.pmml_path = null;
        this.encoders_json = null;
        this.at_encoder = null;
        this.imputation_json = null;
        this.circ_imputer = null;
        this.model_hyperparams = new HashMap<>();

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

    public Globals(String model_definition, int bond_depth, boolean init_models, boolean init_directories, boolean use_AD) throws JSONException, IOException {
        this();
        this.use_AD = use_AD;

        this.model_name = model_map.get(model_definition) + "_" + bond_depth;
        this.model_code = this.model_name.split("_")[0] + "_cdk_fing_ccdk" + "_" + this.model_name.split("_")[1];
        this.target_var = model_map.get(model_definition);
        this.circ_depth = Integer.parseInt(this.model_name.split("_")[1]);
        this.fing_depth = 10; // is always 10 because of the AD score
        this.decision_threshold = "model";

        this.desc_groups = new HashSet<>(
                Arrays.asList(
                        model_code.split("_")
                )
        );
        this.model_dir = MODELS_ROOT + this.model_code + "/";
        this.pmml_path = model_dir + "final_model.pmml";
        
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        
        try {
        		encoders_json = Utils.convertStreamToString(classLoader.getResourceAsStream(model_dir + "encoders.json")); // was this.getClass()
        } catch (Exception e) {
        		logger.error("Error reading json file for model. Exiting.", e);
        		System.exit(1);
        }
//      this.encoders_json = Utils.convertStreamToString(new FileInputStream(model_dir + "encoders.json"));
        
        this.at_encoder = new Encoder("AtomType", encoders_json);
        
        try {
            imputation_json = Utils.convertStreamToString(classLoader.getResourceAsStream(model_dir + "imputation.json"));
        } catch (Exception e) {
	    		logger.error("Error reading json file for model. Exiting.", e);
	    		System.exit(1);
        }
//      this.imputation_json = Utils.convertStreamToString(new FileInputStream(model_dir + "imputation.json"));
        this.circ_imputer = new CircImputer(imputation_json);

        this.AD_model_path = MODELS_ROOT + "AD/" + "nns_" + target_var + ".ser";
        this.AD_model_attrs_path = MODELS_ROOT + "AD/" + "nns_attributes_" + target_var + ".ser";

        String model_hyperparams = "";
	    	try {
	    		model_hyperparams = Utils.convertStreamToString(classLoader.getResourceAsStream(model_dir + "misc_params.json"));
	    	} catch (Exception e) {
	    		logger.error("Error reading json file for model. Exiting.", e);
	    		System.exit(1);
	    	}
//      String model_hyperparams = Utils.convertStreamToString(new FileInputStream(model_dir + "misc_params.json"));
	    	
        JSONObject json = new JSONObject(model_hyperparams);
        Iterator iterator = json.keys();
        while (iterator.hasNext()) {
            String param_name = iterator.next().toString();
            this.model_hyperparams.put(param_name, json.getString(param_name));
        }
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
        this(
                args_ns.getString("model")
                , Integer.parseInt(args_ns.getString("depth"))
                , false
                , false
                , !args_ns.getBoolean("no_app_domain")
        );
        logger.info("Initializing and validating settings...");
        this.output_dir = args_ns.getString("output_directory");
        this.generate_pngs = args_ns.getBoolean("depict_png");
        this.generate_csvs = args_ns.getBoolean("output_csv");
        this.generate_html = args_ns.getBoolean("output_html");
        this.cpus = args_ns.getInt("processors");
        this.decision_threshold = args_ns.getString("decision_threshold");
        logger.info("Selected model: " + args_ns.getString("model"));
        logger.info("Model bond depth: " + this.circ_depth);
        logger.info("Decision threshold: " + this.decision_threshold);
        logger.info("Output Directory: " + this.output_dir);

        // init PNG depictor
        initPNGDepictor();

        //init models
        initModels();

        // init directories
        initDirectories();

        logger.info("Global settings initialized.");
    }

    public void initModels() throws IOException {
        if (model_code != null) {
            // init modeller
            try {
                this.modeller = new Modeller(this);
            } catch (JAXBException | SAXException | ClassNotFoundException e) {
                logger.error("Failed to initialize and parse model: {}", model_code);
                logger.error("Settings might be invalid. {}", e);
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
        	
        		if (generate_csvs || generate_html || generate_pngs) { // added for GLORY specifically
                // check files and create directories
                File outdir = new File(output_dir);
                if (!outdir.exists()) {
                    outdir.mkdir();
                }
                File outdir_js = new File(output_dir, "ui");
                if (!outdir_js.exists()) {
                    outdir_js.mkdir();
                }

                // write the necessary JavaScript code - don't want this for web version
//                List<File> js_code_paths = new ArrayList<>();
//                js_code_paths.add(new File(outdir_js, "ChemDoodleWeb.js"));
//                js_code_paths.add(new File(outdir_js, "ChemDoodleWeb-libs.js"));
//                for (File item : js_code_paths) {
//                    InputStream js_istream = this.getClass().getResourceAsStream(CHEMDOODLE_ROOT + item.getName());
//                    OutputStream js_ostram = new FileOutputStream(item);
//                    copyStreams(js_istream, js_ostram);
//                }
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

    public void setADModel(String AD_model_path, String AD_model_attrs_path) throws IOException, ClassNotFoundException {
        this.use_AD = true;
        this.AD_model_path = AD_model_path;
        this.AD_model_attrs_path = AD_model_attrs_path;
        this.modeller.setADModel(AD_model_path, AD_model_attrs_path);
    }

    public void setADModel(Pair<NearestNeighbourSearch, List<String>> model) throws IOException, ClassNotFoundException {
        this.modeller.setADModel(model);
    }

    public Pair<String, String> getADModelInfo() {
        return new Pair<>(this.AD_model_path, this.AD_model_attrs_path);
    }

    public Pair<NearestNeighbourSearch, List<String>> getADModel() {
        return this.modeller.getADModel();
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
            		logger.error("Error. {}", e);
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
