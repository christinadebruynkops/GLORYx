package fame.descriptors;

import fame.tools.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AtomicDescriptorsCalculatorMultiThread {	
	public static void main(String[] args) throws Exception{
		// Check args for correctness
		if (args.length != 1){
			System.out.println("Wrong input" + '\n' + "Usage: java -jar AtomicDescriptorsCalculator.jar <inputFile.sdf>");
			System.exit(0);
		}

		File inputFile = new File(args[0]);

		if (!inputFile.exists()) {
			System.err.println("File not found: " + args[0]);
			System.exit(1);
		}

		// sanitize the data and get the path to the modified file
		String sanitized_file = Sanitize.sanitize(args[0]);
	    
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(sanitized_file), DefaultChemObjectBuilder.getInstance());
        ArrayList<Molecule> molecules = new ArrayList<Molecule>();

		int counter = 0;
        while (reader.hasNext() && (counter < Globals.LOAD_MAX_MOL || Globals.LOAD_MAX_MOL == -1)) {
        	Molecule molecule = (Molecule)reader.next();
			System.out.println("Reading " + molecule.getProperty("MolID"));
			molecules.add(molecule);
			counter++;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < molecules.size(); i++) {
        	Runnable worker = new WorkerThread(molecules.get(i), true); // insert true to generate SOMs depictions
        	executor.execute(worker);
        }
        executor.shutdown();
//        while (!executor.isTerminated()) {
//        	System.out.println("in process");
//        }

		System.out.flush();
        System.out.println("Descriptor calculator finished. All threads completed.");
	}
}
