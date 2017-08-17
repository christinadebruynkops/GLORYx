package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.Utils;

import java.io.FileNotFoundException;
import java.util.List;

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
                        "is a model based on the atom itself and its immediate neighbors" +
                        " (atoms at most one bond away)."
                );
//        parser.addArgument("-d", "--depth")
//                .type(Integer.class)
//                .choices(1,2,3,4,5,6)
//                .setDefault(6)
//                .help("The maximum number of layers to consider in atom type fingerprints and circular descriptors.");
        parser.addArgument("FILE").nargs("*")
                .help("One or more SDF files with compounds to predict. " +
                        "One SDF can contain multiple compounds."+
                        "\nAll molecules should be neutral " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "If there are still missing hydrogens, the software will try to add them automatically." +
                        "Calculating spatial coordinates of atoms is not necessary.")
                ;
        parser.addArgument("-s", "--smiles").nargs("*")
                .help("One or more SMILES strings of compounds to predict. " +
                        "\nAll molecules should be neutral " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "If there are still missing hydrogens, the software will try to add them automatically.")
        ;
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

            // check inputs
            if (args_ns.<String>getList("FILE").isEmpty() && args_ns.<String>getList("smiles") == null) {
                throw new ArgumentParserException("No input specified.", parser);
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            if (e.getMessage().equals("too few arguments")) {
                System.err.println("Run the program with the '-h' or '--help' option to see detailed usage description.");
            }
            System.exit(1);
        }

        // fetch inputs
        List<String> sdf_inputs = args_ns.<String>getList("FILE");
        List<String> smile_inputs = args_ns.<String>getList("smiles");

        // initialize global settings
        System.out.println("Selected model: " + args_ns.getString("model"));
        System.out.println("Output Directory: " + args_ns.getString("output_directory"));
        Globals params = new Globals(
                args_ns.getString("output_directory")
                , args_ns.getString("model")
                , "HLM"
        );
        params.generate_pngs = args_ns.getBoolean("depict_png");
        params.generate_csvs = args_ns.getBoolean("output_csv");

        // process files
        int counter = 1;
        for (String input_file : sdf_inputs) {
            System.out.println("Processing: " + input_file);
            System.out.println("Note: Make sure that all molecules in the input file are neutral and have explicit hydrogens added.");

            // check if input file exists and change the path in settings
            try {
                params.setInputSDF(input_file);
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + input_file);
                System.err.println("Skipping...");
            }

            params.input_number = counter;

            // sanitize the data if requested and save the path to the modified file
//            if (args_ns.getBoolean("sanitize")) {
//                System.out.println("Sanitizing structures with babel...");
//                params.input_sdf = Sanitize.sanitize(params);
//            }

            // make predictions
            Predictor desc_calc = new Predictor(params);
            desc_calc.calculate();
            counter++;
        }

        // process smiles
        System.out.println("Processing: " + smile_inputs.toString());
        params.setInputSmiles(smile_inputs);
        params.input_number = counter;

        // make predictions
        Predictor desc_calc = new Predictor(params);
        desc_calc.calculate();
    }
}
