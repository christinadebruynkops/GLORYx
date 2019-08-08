package org.zbh.fame.fame3.main;

import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.zbh.fame.fame3.utils.Utils;
import org.zbh.fame.fame3.utils.data.FAMEMolSupplier;
import org.zbh.fame.fame3.utils.data.parsers.FAMEFileParser;
import org.zbh.fame.fame3.utils.data.parsers.SDFParser;
import org.zbh.fame.fame3.utils.data.parsers.SMILESFileParser;
import org.zbh.fame.fame3.utils.data.parsers.SMILESListParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("fame3")
                .defaultHelp(true)
                .description("This is FAME 3 [1]. It is a collection of machine learning models to predict sites of " +
                        "metabolism (SOMs) for supplied chemical compounds (supplied as SMILES or in an SDF file).\n " +
                        "FAME 3 includes a combined model (\"P1+P2\") " +
                        "for phase I and phase II SOMs" +
                        " and also separate phase I and phase II models (\"P1\" and \"P2\"). It is based on extra trees classifiers " +
                        "trained for regioselectivity prediction on data from the MetaQSAR database [2]." +
                        "Feel free to take a look at the README.html file for usage examples." +
                        "\n\n1. TODO: FAME 3 paper reference" + // TODO: add FAME3 paper reference
                        "\n2. MetaQSAR: An Integrated Database Engine to Manage and Analyze Metabolic Data\n" +
                        "Alessandro Pedretti, Angelica Mazzolari, Giulio Vistoli, and Bernard Testa\n" +
                        "Journal of Medicinal Chemistry 2018 61 (3), 1019-1030\n" +
                        "DOI: 10.1021/acs.jmedchem.7b01473")
                .version(Utils.convertStreamToString(Main.class.getResourceAsStream("/VERSION.txt")));
        parser.addArgument("--version").action(Arguments.version()).help("Show program version.");

        parser.addArgument("-m", "--model")
                .choices("P1+P2", "P1", "P2").setDefault("P1+P2")
                .help("Model to use to generate predictions. " +
                        "Select P1+P2 to predict both phase I and phase II SOMs. " +
                        "Select P1 to predict phase I only. " +
                        "Select P2 to predict phase II only."
                );
        parser.addArgument("-r", "--processors")
                .type(Integer.class)
                .setDefault(0)
                .help("Maximum number of CPUs the program should use. Set to 0 to use all available CPUs.");
        parser.addArgument("-d", "--depth")
                .type(Integer.class)
                .choices(2,5)
                .setDefault(5)
                .help("The circular descriptor bond depth. " +
                        "It is the maximum number of layers to consider in atom type " +
                        "fingerprints and circular descriptors. Optimal results should be achieved " +
                        "with the default bond depth of 5. However, in some cases the lower " +
                        "complexity model could be more successful, especially if FAMEscores are low.");
        parser.addArgument("FILE").nargs("*")
                .help("One or more files with the compounds to predict. " +
                        "FAME 3 currently supports SDF files and SMILES files." +
                        "In order for a file to be parsed as a SMILES file, it needs to have the \".smi\"" +
                        "file extension. Files with a different extension will be parsed as an SDF." +
                        "The file can contain multiple compounds. "+
                        "\nAll molecules should be neutral (with the exception of tertiary ammonium) " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "However, if there are missing hydrogens, " +
                        "the software will try to add them automatically. " +
                        "Calculating spatial coordinates of atoms is not necessary." +
                        "The compounds will be assigned a generic name if the name cannot be determined from the file.")
                ;
        parser.addArgument("-s", "--smiles").nargs("*")
                .help ("One or more SMILES strings of the compounds to predict. " +
                        "\nAll molecules should be neutral (with the exception of tertiary ammonium) " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "However, if there are missing hydrogens, " +
                        "the software will try to add them automatically. " +
                        "Calculating spatial coordinates of atoms is not necessary.")
        ;
        parser.addArgument("-o", "--output-directory")
                .setDefault("fame3_results")
                .help("Path to the output directory. If it doesn't exist, it will be created.");
        parser.addArgument("-p", "--depict-png")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Generates depictions of molecules with the predicted sites highlighted as PNG files in addition to the HTML output.");
        parser.addArgument("-c", "--output-csv")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Saves calculated descriptors and predictions to CSV files.");
        parser.addArgument("-t", "--decision-threshold")
                .setDefault("model")
                .help("Define the decision threshold for the model (0 to 1). Use \"model\" for the default model threshold.");
        parser.addArgument("-a", "--no-app-domain")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Do not use the applicability domain model. FAMEscore values" +
                        " will not be calculated, but the predictions will be faster.");

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
        List<String> file_inputs = args_ns.<String>getList("FILE");
        List<String> smile_inputs = args_ns.<String>getList("smiles");
        if (smile_inputs == null) {
            smile_inputs = new ArrayList<>();
        }

        // process files
        List<FAMEFileParser> parsers = new ArrayList<>();
        int counter = 1;
        for (String input_file : file_inputs) {
            if (input_file.endsWith(".smi")) {
                System.out.println("Processing SMILES file: " + input_file);
                SMILESFileParser smi_parser = new SMILESFileParser(input_file, "SMIFile_" + counter++ + "_");
                parsers.add(smi_parser);
            } else {
                // all unrecognized files are processed as SDF files
                System.out.println("Processing SDF file: " + input_file);
                SDFParser sdf_parser = new SDFParser(input_file, "SDF_" + counter++ + "_");
                parsers.add(sdf_parser);
            }
        }

        // process smiles input list
        if (!smile_inputs.isEmpty()) {
            System.out.println("Processing SMILES: " + smile_inputs.toString());
            parsers.add(new SMILESListParser(smile_inputs, "SMIList_" + counter++ + "_"));
        }

        // initialize global settings
        Globals params = new Globals(
                args_ns
        );

        // begin the calculation
        Predictor predictor = new Predictor(
                params
                , new FAMEMolSupplier(parsers)
        );
        predictor.calculate();
    }
}
