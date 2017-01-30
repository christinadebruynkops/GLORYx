package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

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
                        "metabolism for compounds. It includes multiple models " +
                        "which can predict the regioselectivity of cytochromes P450."
                );
        parser.addArgument("-m", "--model")
                .choices("cdk", "cdk_ccdk", "cdk_fing", "cdk_fing_ccdk").setDefault("cdk_ccdk")
                .help("The model to use according to the set of descriptors computed. Can be one of: 'cdk', 'cdk_ccdk', 'cdk_fing' and 'cdk_fing_ccdk'.");
        parser.addArgument("FILE").nargs("+")
                .help("One or more SDF files with compounds to predict.");
        parser.addArgument("-s", "--sanitize")
                .action(Arguments.storeTrue())
                .setDefault(true)
                .help("Use Open Babel to sanitize the structures before processing (recommended). Turned on by default.");
        parser.addArgument("-o", "--output-directory")
                .setDefault("fame_results")
                .help("The path to the output directory. If it doesn't exist, it will be created. Uses './fame_results' by default.");
        parser.addArgument("-p", "--depict-png")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Generates depictions of molecules with the predicted sites highlighted as PNG files as well. Turned off by default.");

        Namespace args_ns = null;
        try {
            args_ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        for (String input_file : args_ns.<String>getList("FILE")) {
            // initialize global settings
            Globals params = new Globals(
                    input_file
                    , args_ns.getString("output_directory")
                    , args_ns.getString("model")
                    , "HLM"
                    , args_ns.getBoolean("sanitize")
            );
            params.generate_pngs = args_ns.getBoolean("depict_png");

            // calculate the descriptors
            Predictor desc_calc = new Predictor(params);
            desc_calc.calculate();
        }
    }
}
