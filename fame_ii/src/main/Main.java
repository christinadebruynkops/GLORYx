package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.Sanitize;
import utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        List<String> circular_options = new ArrayList<>();
        for (String option : options) {
            if (option.contains("_")) {
                for (int i = 1; i <= 6; i++) {
                    String thing = option + "_" + Integer.toString(i);
                    circular_options.add(thing);
                }
            }
        }
        circular_options.add(options.get(0));
        Collections.reverse(circular_options);
        parser.addArgument("-m", "--model")
                .choices(circular_options.toArray()).setDefault("cdk_fing_ccdk_4")
                .help("Model specification in terms of used descriptors." +
                        "It is possible to specify only models that use circular descriptors of up to certain depth by appending a number to the name (such as 'cdk_fing_ccdk_4'). " +
                        "Models of up to depth 6 are available in the package. If the depth is not specified, 4 is used as default.");
        parser.addArgument("FILE").nargs("+")
                .help("One or more SDF files with compounds to predict.");
        parser.addArgument("-s", "--sanitize")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Use Open Babel (executable needs to be available) to sanitize the structures before processing.");
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
            if (args_ns.getBoolean("sanitize")) {
                System.out.println("Sanitizing structures with babel...");
                params.input_sdf = Sanitize.sanitize(params);
            }

            // calculate the descriptors
            Predictor desc_calc = new Predictor(params);
            desc_calc.calculate();
        }
    }
}
