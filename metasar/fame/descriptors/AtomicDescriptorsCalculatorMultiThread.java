package fame.descriptors;

import fame.tools.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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
		String input_file = args[0];
		input_file = Sanitize.sanitize(input_file);
	    
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(input_file), DefaultChemObjectBuilder.getInstance());
        ArrayList<Molecule> molecules = new ArrayList<Molecule>();

//        int counter = 5;
		while (reader.hasNext()) {
        	Molecule molecule = (Molecule)reader.next();
			System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
			molecules.add(molecule);
//			counter--;
//			if (counter == 0) {
//				break;
//			}
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < molecules.size(); i++) {
        	Runnable worker = new WorkerThread(molecules.get(i), true); // insert true to generate SOMs depictions
        	executor.execute(worker);
        }
        molecules = null; // this is to save memory, GC should take care of the rest when workers finish
        executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		System.out.println("Descriptor calculator finished. All threads completed.");

		// serialize the circular descriptors statistics
		FileOutputStream fos = new FileOutputStream(Globals.DESCRIPTORS_OUT + "circular_stats.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(WorkerThread.getStats());
		oos.close();
		fos.close();
		System.out.println("Serialized circular descriptors statistics to: " + Globals.DESCRIPTORS_OUT + "circular_stats.ser");

//		System.out.println("Generating data set...");
//		String[] dummy = new String[0];
//		RandomMoleculeSelector.main(dummy);
//		System.out.println("Done.");
	}
}
