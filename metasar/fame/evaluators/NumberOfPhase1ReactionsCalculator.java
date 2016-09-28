package fame.evaluators;

import java.io.File;
import java.util.Scanner;

public class NumberOfPhase1ReactionsCalculator {
	public static void main(String[] args) throws Exception{
    	Scanner scanner = new Scanner(new File("/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/001metaboliteDatabase/002metaPrint2DCalcn/all/metab2011_2_mp2d_merged.txt"));
    	while (scanner.hasNextLine()) {
    		String line = scanner.nextLine();
    		if (!line.matches("^[^\\d].*")) {
    			if (!line.equals("")) {
    				String[] splitLine = line.split("\t");
    				if (splitLine.length > 1) {
        				if (!splitLine[1].equals("A2")) {
        					System.out.println(line);
        				}
    				}
    			}
    		}
    	}
	}

}
