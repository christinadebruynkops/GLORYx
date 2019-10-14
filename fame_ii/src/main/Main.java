package main;

import globals.Globals;
import modelling.Predictor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.Utils;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.neighboursearch.PerformanceStats;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    static class TanimotoDistance implements DistanceFunction, Serializable {

        Instances instances = null;
        String indices = "";
        boolean invert_selection = false;

        @Override
        public void setInstances(Instances instances) {
            this.instances = instances;
        }

        @Override
        public Instances getInstances() {
            return instances;
        }

        @Override
        public void setAttributeIndices(String s) {
            indices = s;
        }

        @Override
        public String getAttributeIndices() {
            return indices;
        }

        @Override
        public void setInvertSelection(boolean b) {
            invert_selection = b;
        }

        @Override
        public boolean getInvertSelection() {
            return invert_selection;
        }

        public BitSet convert(Instance ins) {
            BitSet ret = new BitSet(ins.numValues());
            for (int i = 0; i < ins.numValues(); i++) {
                double val = ins.value(i);
                if (val == 1) {
                    ret.set(i);
                }
            }

            return ret;
        }

        @Override
        public double distance(Instance instance, Instance instance1) {
            BitSet a = this.convert(instance);
            BitSet b = this.convert(instance1);
//            try {
//                return 1 - Tanimoto.calculate(a, b);
//            } catch (CDKException e) {
//                e.printStackTrace();
//                return -1;
//            }
            final int size = Math.max(a.length(), b.length());
            final BitSet and = new BitSet(size);
            and.or(a);
            and.and(b);
            final BitSet or = new BitSet(size);
            or.or(a);
            or.or(b);
            final double union = or.cardinality();
            final double intersection = and.cardinality();
            return (union - intersection) / union;
        }

        @Override
        public double distance(Instance instance, Instance instance1, PerformanceStats performanceStats) throws Exception {
            return distance(instance, instance1);
        }

        @Override
        public double distance(Instance instance, Instance instance1, double v) {
            return distance(instance, instance1);
        }

        @Override
        public double distance(Instance instance, Instance instance1, double v, PerformanceStats performanceStats) {
            return distance(instance, instance1);
        }

        @Override
        public void postProcessDistances(double[] doubles) {

        }

        @Override
        public void update(Instance instance) {

        }

        @Override
        public void clean() {

        }

        @Override
        public Enumeration<Option> listOptions() {
            return null;
        }

        @Override
        public void setOptions(String[] strings) throws Exception {

        }

        @Override
        public String[] getOptions() {
            return new String[0];
        }
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = getArgumentParser();

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
        if (smile_inputs == null) {
            smile_inputs = new ArrayList<>();
        }

        // initialize global settings
        Globals params = new Globals(
                args_ns
        );

        // process files
        int counter = 1;
        for (String input_file : sdf_inputs) {
            System.out.println("Processing SDF file: " + input_file);
//            System.out.println("Note: Make sure that all molecules in the input file are neutral and have explicit hydrogens added.");

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
        if (!smile_inputs.isEmpty()) {
            System.out.println("Processing SMILES: " + smile_inputs.toString());
            params.setInputSmiles(smile_inputs);
            params.input_number = counter;

            // make predictions
            Predictor desc_calc = new Predictor(params);
            desc_calc.calculate();
        }
    }

	public static ArgumentParser getArgumentParser() {
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
                .version(Utils.convertStreamToString(Main.class.getResourceAsStream("/main/VERSION.txt")));
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
                .help("One or more SDF files with the compounds to predict. " +
                        "One SDF can contain multiple compounds. "+
                        "\nAll molecules should be neutral (with the exception of tertiary ammonium) " +
                        "and have explicit hydrogens added prior to modelling. " +
                        "However, if there are missing hydrogens, " +
                        "the software will try to add them automatically. " +
                        "Calculating spatial coordinates of atoms is not necessary.")
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
        parser.addArgument("-l", "--output-html") // for implementation in GLORY
        			.action(Arguments.storeTrue())
        			.setDefault(false)
        			.help("Creates HTML file visualizing predictions.");
        parser.addArgument("-t", "--decision-threshold")
                .setDefault("model")
                .help("Define the decision threshold for the model (0 to 1). Use \"model\" for the default model threshold.");
        parser.addArgument("-a", "--no-app-domain")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Do not use the applicability domain model. FAMEscore values" +
                        " will not be calculated, but the predictions will be faster.");
		return parser;
	}
}
