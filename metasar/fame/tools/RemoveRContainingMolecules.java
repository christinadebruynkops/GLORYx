package fame.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.openscience.cdk.exception.CDKException;

public class RemoveRContainingMolecules {
	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String sdOriginalInput = wDir + "001metaboliteDatabase/003convertedToSdf/allSubstrates.sdf"; 
	static String sdOutput = wDir + "001metaboliteDatabase/003convertedToSdf/allSubstrates_RcontainingMoleculesRemoved.sdf"; 

	public static void main(String... aArgs) throws IOException, CDKException {
		Scanner scanner = new Scanner(new File(sdOriginalInput));
		FileOutputStream out = new FileOutputStream(sdOutput, false);
		PrintStream p = new PrintStream(out);
		
		while (scanner.hasNextLine()) {
			boolean valid = true;
			String line = scanner.nextLine();
			ArrayList<String> entry = new ArrayList<String>();
			while (!line.equals("$$$$")) {
				if (line.contains(" R   ")) {
					valid = false;
				}
				entry.add(line);
				line = scanner.nextLine();
			}
			entry.add(line);
			if (valid) {
				Iterator<String> itr = entry.iterator();
				while (itr.hasNext()) {
					p.println(itr.next());
				}
			}
		}
		p.close();
	}
}