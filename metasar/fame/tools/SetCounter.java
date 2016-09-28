package fame.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class SetCounter {
	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";
	static String input = wDir + "003trainingAndTestSets/dog/002afterSeparation/testSet1.csv";
	
	
	public static void main(String... aArgs) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(input));
		int moleculeNameColumn=0;
		int setColumn=0;
		
		String[] splitLine = scanner.nextLine().split("\t");
		for (int i=0; i<splitLine.length; i++) {
			if (splitLine[i].equals("set")) {
				setColumn=i;
			}
			if (splitLine[i].equals("MoleculeName")) {
				moleculeNameColumn=i;
			}
		}
		
		String moleculeName ="";
		String setName ="";

		Map<String,Integer> map = new HashMap<String,Integer>();
		ArrayList<String> molecules = new ArrayList<String>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			moleculeName = line.split("\t")[moleculeNameColumn];
			setName = line.split("\t")[setColumn];
			if (!molecules.contains(moleculeName)) {
				if (map.containsKey(setName)) {
					map.put(setName, (map.get(setName)+1));
				} else {
					map.put(setName, 1);
				}
				molecules.add(moleculeName);
			}
		}
		
        Set<String> set = map.keySet();
        for (String key : set) {
    		System.out.println(key + "\t" + map.get(key));
        }
		
	}
}
