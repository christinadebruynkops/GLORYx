/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
 
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



package main.java.metaboliteprediction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.Errors;
import main.java.utils.Filenames;
import main.java.utils.Phase;
import main.java.utils.TestParameters;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Launches GLORYx.
 * The usage mode is hard-coded in the main method and can be changed between evaluation mode and the user version.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class MetabPredictLauncher {

	private static final String P1P2 = "P1+P2";
	private static final String PHASE_ARG = "phase";
	private static final String PHASE_2 = "P2";
	private static final String PHASE_1 = "P1";
	private static final String NTHREADS_ARG = "nthreads";
	private static final String MODE_ARG = "mode";
	private static final String OUTPUT_DIR_ARG = "outputdirectory";
	private static final String INPUT_SMILES_ARG = "inputsmiles";
	private static final String INPUT_FILE_ARG = "inputfile";
	
	private static final String SDF_FILEENDING = ".sdf";
	private static final String MAXEFFICIENCY_MODE = "MaxEfficiency";
	private static final String MAXCOVERAGE_MODE = "MaxCoverage";

	private static final String ERROR_CONFIGURING_LOG4J_PROPERTIES_FILE_PATH_RESOURCE = "Error configuring log4j properties file path as resource.";
	private static final String SDFILE_ENDING_ERRORMESSAGE = "The provided file appears not to be an SDF file. The file ending should be '.sdf'.";
	private static final String MODE_INFOMESSAGE = "Running metabolite predictor with the following specifications: \n\tUse SoMs as cutoff: {}\n\t"
			+ "Cutoff if using SoMs as cutoff (ignore if not using): {}\n\tUse combined phase 2 FAME 3 model P2: {}";
	private static final String RUN_WITH_HELP_ERRORMESSAGE = "Run the program with the '-h' or '--help' option to see detailed usage description.";
	private static final String TOO_FEW_ARGUMENTS_ERRORMESSAGE = "too few arguments";
	private static final String NO_INPUT_SPECIFIED_ERRORMESSAGE = "No input specified.";

	private static final String METABOLITEPROJECT_DIR = "/work/kops/metaboliteproject/";
	static final String LOG_PROPERTIES_PATH = "log4j.properties";
	static final String LOG_PROPERTIES_PATH_USER = "userversion.log4j.properties"; // src/main/java/resources

	private static final Logger logger = LoggerFactory.getLogger(MetabPredictLauncher.class.getName());

	public static void main(String[] args) {

		Instant start = Instant.now();  // for measuring total run time 
		
		String timeStamp = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(Calendar.getInstance().getTime());

		TestParameters.Version usageMode = TestParameters.Version.USER; 
		TestParameters.UserVersion userVersionType = TestParameters.UserVersion.OFFLINE;
		
		// configure log4j so that it also works when running from command line. Use ERROR only configuration for user version
		if (usageMode == TestParameters.Version.USER) {
			
//			configureLog4j(LOG_PROPERTIES_PATH);
			
			// TODO use this one in real user version
			configureLog4j(LOG_PROPERTIES_PATH_USER); 
		} else {
			configureLog4j(LOG_PROPERTIES_PATH);
		}
				
		ArgumentParser parser = createArgumentParser();
		Namespace parsedArgs = checkArguments(args, parser);

		// fetch inputs
		String inputFilename = parsedArgs.getString(INPUT_FILE_ARG);
		if (inputFilename != null) {
			inputFilename = stripBracketsFromInputFilename(inputFilename);
		}
		
		List<String> inputSmiles = parsedArgs.<String>getList(INPUT_SMILES_ARG);
		if (inputSmiles == null) {
			inputSmiles = new ArrayList<>();
		}
		String outputDirectory = parsedArgs.getString(OUTPUT_DIR_ARG);
		String mode = parsedArgs.getString(MODE_ARG);
		String phase = parsedArgs.getString(PHASE_ARG);
		int numThreads = parsedArgs.getInt(NTHREADS_ARG);

		// check output dir, create if necessary
		File outdir = new File(outputDirectory);
		if (!outdir.exists()) {
			outdir.mkdir();
		}
		
		String prefix = METABOLITEPROJECT_DIR; // hard-code the prefix used for the evaluation files in -filenames-
		Filenames filenames = setUpFilenames(inputFilename, inputSmiles, outputDirectory, prefix);

		Double defaultSoMCutoff = 0.2; 
		TestParameters testParameters = setUpTestParameters(inputFilename, usageMode, userVersionType, mode, defaultSoMCutoff, numThreads, filenames, phase);

		// set up necessary FAME 3 parameters
//		String fameOutputDirectory = setUpFameOutputDir(testParameters, filenames, prefix); // not needed for GLORYx because we don't write FAME 3 output to html files anymore


		// make predictions
		MetabolitePredictor predictor = new MetabolitePredictor();
		predictor.predictMetabolites(testParameters, filenames, prefix, timeStamp);
		
		// for measuring total run time
		Instant finish = Instant.now();
		calculateAndLogTotalRuntime(start, finish);
	}


	private static String stripBracketsFromInputFilename(String inputFilename) {
		if (inputFilename.startsWith("[")) {
			inputFilename = inputFilename.substring(1);
		}
		if (inputFilename.endsWith("]")) {
			inputFilename = inputFilename.substring(0, inputFilename.length() - 1);
		}
		return inputFilename;
	}


	private static void calculateAndLogTotalRuntime(Instant start, Instant finish) {
		long timeElapsed = Duration.between(start, finish).toMillis();
		Double timeInS = timeElapsed/1000.0;
		if (timeInS < 60) {
			logger.info("total time elapsed in s: {}", timeInS); 
		} else {
			Double timeInMin = timeInS/60.0;
			logger.info("total time elapsed in min: {}", timeInMin);
		}
	}


	private static void configureLog4j(String fileName) {
		Properties p = new Properties();
		
		fileName = "main/resources/" + fileName;
				
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);
		try {
			p.load(inputStream);
		} catch (IOException e1) {
			logger.error(ERROR_CONFIGURING_LOG4J_PROPERTIES_FILE_PATH_RESOURCE);
		}

		PropertyConfigurator.configure(p);
	}


	private static String setUpFameOutputDir(TestParameters testParameters, Filenames filenames, String prefix) {
		String fameOutputDirectory;  // FAME 3 will create this directory if it doesn't yet exist
		if (testParameters.isUserVersion()) {
			fameOutputDirectory = filenames.getUserOutputDir() + filenames.getIndividualResultsDir(); 
		} else {
			fameOutputDirectory = prefix + filenames.getIndividualResultsDir(); 
		}
		filenames.createDir(fameOutputDirectory); // make sure dir will exist by the time we need it
		return fameOutputDirectory;
	}


	private static Filenames setUpFilenames(String inputFilename, List<String> inputSmiles, String outputDirectory, String prefix) {
		Filenames filenames = new Filenames(prefix);
		filenames.setUserInputFilename(inputFilename);
		filenames.setUserInputSmiles(inputSmiles);
		filenames.setUserOutputDir(outputDirectory);
		return filenames;
	}


	private static TestParameters setUpTestParameters(String inputFilename, TestParameters.Version usageMode, TestParameters.UserVersion userVersion, String mode, Double defaultSoMCutoff, int nThreads, Filenames filenames, String phase) {
		TestParameters testParameters;
		Phase p = Phase.getPhaseFromString(phase);
//		TestParameters.UseSoMsAsHardFilter useSoMPrefilter = setPrefilterOption(mode); // was used for GLORY
		TestParameters.UseSoMsAsHardFilter useSoMPrefilter = TestParameters.UseSoMsAsHardFilter.NO; // not a user parameter currently
		if (usageMode == TestParameters.Version.USER) {  // options set based on user input and/or defaults

			// set up all test parameters
			TestParameters.InputFormat inputFormat = setInputFormat(inputFilename, filenames);
			logger.debug("input format: {}", inputFormat);

			testParameters = new TestParameters(
					useSoMPrefilter,
					defaultSoMCutoff, // only used if using SoMs as hard filter
					TestParameters.Version.USER, 
					userVersion,
					TestParameters.Reference.NONE, 
					inputFormat,
					p,
					nThreads);

		} else {  // options are hard-coded in evaluation mode
			testParameters = new TestParameters(
					useSoMPrefilter, // use SoM probability cutoff as prefilter
					0.4, // SoM probability cutoff if using SoMs as hard filter
					TestParameters.Version.EVALUATION, 
					userVersion,
					TestParameters.Reference.TEST_DATASET,  //DRUGBANK_PLUS_METXBIODB, //TEST_DATASET,
					TestParameters.InputFormat.DATABASE,  
					p,
					nThreads);

			logger.info(MODE_INFOMESSAGE, 
					testParameters.useSoMsAsHardFilter(), testParameters.getSoMProbabilityCutoff(), TestParameters.useCombinedPhase2());
		}
		return testParameters;
	}

	/**
	 * Method left over from GLORY.
	 * 
	 * @param mode
	 * @return
	 */
	private static TestParameters.UseSoMsAsHardFilter setPrefilterOption(String mode) {
		TestParameters.UseSoMsAsHardFilter useSoMPrefilter;
		if (mode.equals(MAXCOVERAGE_MODE)) {
			useSoMPrefilter = TestParameters.UseSoMsAsHardFilter.NO;
		} else {
			useSoMPrefilter = TestParameters.UseSoMsAsHardFilter.YES;
		}
		return useSoMPrefilter;
	}

	private static TestParameters.InputFormat setInputFormat(String inputFilename, Filenames filenames) {
		TestParameters.InputFormat inputFormat = null;
		if (inputFilename == null) {
			inputFormat = TestParameters.InputFormat.INDIVIDUAL_SMILES;
		} else if (inputFilename.endsWith(SDF_FILEENDING)) {  // have to get rid of brackets[]
			inputFormat = TestParameters.InputFormat.SDFILE;
			logger.debug("input SD filename: {}", inputFilename);
		} else {
			logger.error(SDFILE_ENDING_ERRORMESSAGE);
			logger.debug("input filename: {}", inputFilename);
			Errors.createErrorHtmlAndExit(filenames, Errors.INPUT_FILE_FORMAT);
		}
		return inputFormat;
	}

	private static Namespace checkArguments(String[] args, ArgumentParser parser) {

		Namespace parsedArgs = null;
		try {
			parsedArgs = parser.parseArgs(args);
			
			// check inputs
			if (parsedArgs.getString(INPUT_FILE_ARG) == null && parsedArgs.<String>getList(INPUT_SMILES_ARG) == null ) {
				throw new ArgumentParserException(NO_INPUT_SPECIFIED_ERRORMESSAGE, parser);
			}

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			if (e.getMessage().equals(TOO_FEW_ARGUMENTS_ERRORMESSAGE)) {
				logger.error(RUN_WITH_HELP_ERRORMESSAGE, e);
			}
			System.exit(1);  // ok to leave this, because this cannot be caused by a user of the website
		}
		return parsedArgs;
	}


	private static ArgumentParser createArgumentParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("metabolitepredictor")
				.defaultHelp(true)
				.description("This is GLORYx. It predicts metabolites formed by phase I and phase II metabolic reactions in humans. "
						+ "GLORYx can predict metabolites formed by phase I metabolism, phase II metabolism, or both phases I and II metabolism. "
						+ "GLORYx provides a priority score (overall score) and rank (per input molecule) for each predicted metabolite. "
						+ "For more details on the GLORYx method, please see the publication (DOI: 10.1021/acs.chemrestox.0c00224).") // TODO: complete and add paper citation
//                .version(Utils.convertStreamToString(MetabPredictLauncher.class.getResourceAsStream("/src/main/resources/VERSION.txt")));
//				.version(ClassLoader.getSystemClassLoader().getResourceAsStream("src/main/resources/VERSION.txt").toString())  // TODO: this doesn't work properly
				;  // TODO add version back in when generating executable
//		parser.addArgument("-v", "--version").action(Arguments.version())
//		.help("Show program version.")
//		;
//		parser.addArgument("-m", "--mode")
//		.choices(MAXCOVERAGE_MODE, MAXEFFICIENCY_MODE).setDefault(MAXCOVERAGE_MODE)
//		.help("Mode for metabolite prediction. MaxCoverage prioritizes recall, whereas MaxEfficiency uses a "
//				+ "cutoff based on FAME 2's predicted SoM probabilities to reduce the number of predicted metabolites. "
//				+ "Both  modes score and rank the predicted metabolites.")
//		;
		parser.addArgument("-p", "--phase")
		.choices(PHASE_1, PHASE_2, P1P2).setDefault(P1P2) // , "UGT", "GST", "SULT", "NAT", "MT"
		.help("Choose 'P1' for phase I, 'P2' for phase II, or 'P1+P2' for phases I and II.") // TODO or CYP? //  Or UGT, GST, SULT, NAT, or MT.
		;
		parser.addArgument("-f", "--inputfile").nargs(1)
		.help("One SDF file containing molecules whose metabolites will be predicted."
				+ "\nEach SDF entry should be a single-component molecule. No predictions will be made for multi-component molecules. "
				+ "\nAll molecules should be neutral and already have explicit hydrogens added. "
				+ "If there are still missing hydrogens, the software will try to add them automatically. "
				+ "Pre-calculating spatial coordinates of atoms is not necessary. "
				+ "\nPlease provide either an SDF file or SMILES strings, not both. If you provide both, "
				+ "predictions will only be made for molecules in the SDF file.")
		;
		parser.addArgument("-s", "--inputsmiles").nargs("*")
		.help("One or more SMILES strings (surrounded by quotation marks) of molecules whose metabolites will be predicted."
				+ "\nEach SMILES should represent a single-component molecule. No predictions will be made for multi-component molecules. "
				+ "\nAll molecules should be neutral and already have explicit hydrogens added. "
				+ "If there are still missing hydrogens, the software will try to add them automatically."
				+ "\nPlease provide either an SDF file or SMILES strings, not both. If you provide both, " 
				+ "predictions will only be made for molecules in the SDF file.")
		;
		parser.addArgument("-o", "--outputdirectory")
		.setDefault("metabolitepredictionresults")
		.help("The path to the output directory."
				+ "\nThe metabolite predictions will be written to an SDF file in this directory, or multiple "
				+ "SDF files within a ZIP file if the input contains more than 1000 molecules. "
				+ "If the output directory doesn't yet exist, it will be created.")
		;
		parser.addArgument("--nthreads")
		.setDefault(Runtime.getRuntime().availableProcessors())
		.type(Integer.class)
		.help("Number of threads to use. "
				+ "If no number is specified, the metabolite predictor will use the same number "
				+ "of threads as the number of processors available to the JVM.")
		;

		return parser;
	}




}
