package fame.evaluators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class AUC {
	static String wDir   = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String input  = wDir + "005weka/human/044_cv0_validationSet.predictions";
	static String output = wDir +  "005weka/human/044_cv0_validationSet.auc";

	public static void main(String... aArgs) throws FileNotFoundException {
		//go to beginning of Weka predictions in the Weka file
		Scanner predictionScanner = new Scanner(new File(input));
		while (predictionScanner.hasNext()) {
			if (predictionScanner.nextLine().contains("inst#")) {
				break;
			}
		}
		
		FileOutputStream out = new FileOutputStream(output, false);
		PrintStream p = new PrintStream(out);
		p.println("data\tlabel");
		
		while (predictionScanner.hasNext()) {
			String prediction = predictionScanner.nextLine();
			prediction = prediction.replaceAll("\\+", "");
			prediction = prediction.replaceAll("\\*", "");
			prediction = prediction.replaceAll(" +", "\t");
			prediction = prediction.substring(1);
			String[] splitLine = prediction.split("\t");
			splitLine[2] = splitLine[2].replace("1:", "");
			splitLine[2] = splitLine[2].replace("2:", "");
			splitLine[1] = splitLine[1].replace("1:", "");
			splitLine[1] = splitLine[1].replace("2:", "");
			if (splitLine[2].equals("false")) {
				Float tmp = (float) (1.00 - Float.parseFloat(splitLine[3]));
				splitLine[3] = tmp.toString();
			}
			if (splitLine[1].equals("true")) {
				splitLine[1] = "1";
			} else {
				splitLine[1] = "0";
			}
			p.println(splitLine[3] + "\t" + splitLine[1]);
		}
		
		p.close();

	}
}
