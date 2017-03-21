package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("FAME II")
                .defaultHelp(true)
                .description("This is FAME II. It can predict sites of " +
                        "metabolism for compounds. It includes models " +
                        "for regioselectivity prediction of cytochromes P450.")
                .version(Utils.convertStreamToString(Main.class.getResourceAsStream("/main/VERSION.txt")));
        parser.addArgument("--version").action(Arguments.version()).help("Show program version.");
        List<String> options = Arrays.asList(
                "cdk", "cdk_ccdk", "cdk_fing", "cdk_fing_ccdk"
        );

        parser.addArgument("-m", "--model")
                .choices("cdk", "cdk_ccdk", "cdk_fing", "cdk_fing_ccdk").setDefault("cdk_fing_ccdk")
                .help("Model specification in terms of used descriptors.");
        parser.addArgument("-d", "--depth")
                .type(Integer.class)
                .choices(1,2,3,4,5,6)
                .setDefault(4)
                .help("The maximum number of layers to consider in atom type fingerprints and circular descriptors.");
        parser.addArgument("FILE").nargs("+")
                .help("One or more SDF files with compounds to predict.");
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
                .help("Generates depictions of molecules with the predicted sites highlighted as PNG files as well.");

        Namespace args_ns = null;
        try {
            args_ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        // initialize global settings
        List<String> inputs = args_ns.<String>getList("FILE");
        Globals params = new Globals(
                inputs.get(0)
                , args_ns.getString("output_directory")
                , args_ns.getString("model")
                , args_ns.getInt("depth")
                , "HLM"
        );
        params.generate_pngs = args_ns.getBoolean("depict_png");

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
