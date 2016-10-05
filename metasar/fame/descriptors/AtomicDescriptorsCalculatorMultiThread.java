package fame.descriptors;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;


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
	    
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(args[0]), DefaultChemObjectBuilder.getInstance());
        ArrayList<Molecule> molecules = new ArrayList<Molecule>();

		int counter = 0;
        while (reader.hasNext() && counter < 2 ) { // FIXME: atm only read the first two mols -> remove this in the final version
        	Molecule molecule = (Molecule)reader.next();
        	System.out.println("Reading " + molecule.getProperty("MolID"));
        	molecules.add(molecule);
			counter++;
        }
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < molecules.size(); i++) {
        	Runnable worker = new WorkerThread(molecules.get(i), false); // insert true to generate SOMs depictions
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
