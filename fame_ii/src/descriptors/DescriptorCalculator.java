package descriptors;

import globals.Globals;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DescriptorCalculator {
	private String input_file;
	private String output_dir;
	private String id_prop = Globals.ID_PROP; // TODO: this will be the cdk property assigned by default to a molecule
	private Set<String> desc_groups = new HashSet<>();

	public DescriptorCalculator(String input_file, String output_dir, Set<String> desc_groups) {
		if (!new File(input_file).exists()) {
			System.err.println("File not found: " + input_file);
			System.exit(1);
		}
		this.input_file = input_file;
		this.output_dir = output_dir;
		this.desc_groups.addAll(desc_groups);
	}

	public void calculate() throws IOException, InterruptedException, ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		DefaultIteratingChemObjectReader reader = (IteratingMDLReader) new IteratingMDLReader(new FileInputStream(input_file), DefaultChemObjectBuilder.getInstance());
		ArrayList<IMolecule> molecules = new ArrayList<>();

//        int counter = 5;
		while (reader.hasNext()) {
			IMolecule molecule = (IMolecule) reader.next();
			System.out.println("Reading " + molecule.getProperty(id_prop));
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
		molecules = null; // this is to save memory, GC should take care of the rest when a worker finishes processing a molecule
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		System.out.println("Descriptor calculator finished. All threads completed.");

		// serialize the circular descriptors statistics
		FileOutputStream fos = new FileOutputStream(output_dir + "circular_stats.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(WorkerThread.getStats());
		oos.close();
		fos.close();
		System.out.println("Serialized circular descriptors statistics to: " + output_dir + "circular_stats.ser");
	}
}
