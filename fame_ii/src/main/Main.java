package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.math3.util.Pair;
import utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("fame2")
                .defaultHelp(true)
                .description("This is fame2. It attempts to predict sites of " +
                        "metabolism for supplied chemical compounds. It includes extra trees models " +
                        "for regioselectivity prediction of some cytochrome P450 isoforms.") // TODO: add paper citation
                .version(Utils.convertStreamToString(Main.class.getResourceAsStream("/main/VERSION.txt")));
        parser.addArgument("--version").action(Arguments.version()).help("Show program version.");

        parser.addArgument("-m", "--model")
                .choices("circCDK_ATF_1", "circCDK_4", "circCDK_ATF_6").setDefault("circCDK_ATF_1")
                .help("Model to use to generate predictions. \n Either the model with the best " +
                        "average performance ('circCDK_ATF_6') " +
                        "during the independent test set validation " +
                        "as performed in the original paper " +
                        "or one of the simpler models that were found to have" +
                        " comparable performance (" +
                        "'circCDK_ATF_1' and 'circCDK_4'). The 'circCDK_ATF_1' model is selected by default " +
                        "as it is expected to offer the best trade-off between generalization and accuracy." +
                        "\n The number after the model code indicates how wide the encoded" +
                        "environment of an atom is. For example, the default 'circCDK_ATF_1' " +
                        "is a model based on the atom itself and his immediate neighbors" +
                        " (atoms at most one bond away)."
                );
//        parser.addArgument("-d", "--depth")
//                .type(Integer.class)
//                .choices(1,2,3,4,5,6)
//                .setDefault(6)
//                .help("The maximum number of layers to consider in atom type fingerprints and circular descriptors.");
        parser.addArgument("FILE").nargs("+")
                .help("One or more SDF files with compounds to predict. " +
                        "One SDF can contain multiple compounds."+
                        "\nAll molecules should be neutral " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "Calculating spatial coordinates of atoms is not necessary.")
                ;
//        parser.addArgument("-s", "--sanitize")
//                .action(Arguments.storeTrue())
//                .setDefault(false)
//                .help("Use Open Babel (executable needs to be available) to sanitize the structures before processing.");
        parser.addArgument("-o", "--output-directory")
                .setDefault("fame_results")
                .help("The path to the output directory. If it doesn't exist, it will be created.");
        parser.addArgument("-p", "--depict-png")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Generates depictions of molecules with the predicted sites highlighted as PNG files in addition to the HTML output.");
        parser.addArgument("-c", "--output-csv")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Saves calculated descriptors and predictions to CSV files.");

        Namespace args_ns = null;
        try {
            args_ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            if (e.getMessage().equals("too few arguments")) {
                System.err.println("Run the program with the '-h' or '--help' option to see detailed usage description.");
            }
            System.exit(1);
        }

        Map<String, Pair<String, Integer>> model_to_specs = new HashMap<>();
        model_to_specs.put("circCDK_ATF_1", new Pair("cdk_fing_ccdk", 1));
        model_to_specs.put("circCDK_ATF_6", new Pair("cdk_fing_ccdk", 6));
        model_to_specs.put("circCDK_4", new Pair("cdk_ccdk", 4));

        // initialize global settings
        System.out.println("Selected model: " + args_ns.getString("model"));
        List<String> inputs = args_ns.<String>getList("FILE");
        Globals params = new Globals(
                inputs.get(0)
                , args_ns.getString("output_directory")
                , model_to_specs.get(args_ns.getString("model")).getKey()
                , model_to_specs.get(args_ns.getString("model")).getValue()
                , "HLM"
        );
        params.generate_pngs = args_ns.getBoolean("depict_png");
        params.generate_csvs = args_ns.getBoolean("output_csv");

        // process files
        for (String input_file : inputs) {
            System.out.println("Processing: " + input_file);

            // check if input file exists and change the path in settings
            if (!new File(input_file).exists()) {
                System.err.println("File not found: " + input_file);
                System.err.println("Skipping...");
            }
            params.input_sdf = input_file;

            // sanitize the data if requested and save the path to the modified file
//            if (args_ns.getBoolean("sanitize")) {
//                System.out.println("Sanitizing structures with babel...");
//                params.input_sdf = Sanitize.sanitize(params);
//            }

            // calculate the descriptors
            Predictor desc_calc = new Predictor(params);
            desc_calc.calculate();
        }
    }
}
