package descriptors;

import globals.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DescriptorCalculator {
    private Globals globals;

	public DescriptorCalculator(Globals globals) {
        this.globals = globals;
	}

	public void calculate() throws IOException, InterruptedException, ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(globals.input_sdf), DefaultChemObjectBuilder.getInstance());
		ArrayList<IMolecule> molecules = new ArrayList<>();

//        int counter = 5;
		while (reader.hasNext()) {
			IMolecule molecule = (IMolecule) reader.next();
			System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
			molecules.add(molecule);
//			counter--;
//			if (counter == 0) {
//				break;
//			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < molecules.size(); i++) {
			Runnable worker = new WorkerThread(
					molecules.get(i)
					, this.globals
			);
			executor.execute(worker);
		}
		molecules = null; // this is to save memory, GC should take care of the rest when a worker finishes processing a molecule -> we want molecules out of memory as soon as the descriptors are in the file
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		System.out.println("Descriptor calculator finished. All threads completed.");
	}
}
