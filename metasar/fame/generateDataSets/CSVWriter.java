package fame.generateDataSets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

public class CSVWriter {

	public CSVWriter(String filename, ArrayList<String> header) {
	   	try{
    		File file = new File(filename);
    		if(file.delete()){
//    			System.out.println(file.getName() + " is deleted!");  			
    		}else{
//    			System.out.println("Delete operation is failed.");
    		}
			FileOutputStream out = new FileOutputStream(filename, true);
			PrintStream p = new PrintStream(out);
			String head = header.toString();
			head = head.replaceAll("\\[", "");
			head = head.replaceAll("\\]", "");
			head = head.replaceAll(",", "\t");
			head = head.replaceAll(" ", "");
			p.println(head);		
			p.close();
 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	

	public void write(IMolecule iMolecule,String filename, ArrayList<String> header) throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(filename, true);
		PrintStream p = new PrintStream(out);
		for (int atomNumber = 0; atomNumber < iMolecule.getAtomCount(); atomNumber++) {
			IAtom iAtom = iMolecule.getAtom(atomNumber);
			Iterator<String> itrHeader = header.iterator();
			String line = "";
			while (itrHeader.hasNext()) {
				String propertyName = itrHeader.next();
				if (iMolecule.getProperty(propertyName) != null) {
//						System.out.println(iMolecule.getProperty(propertyName));
					line = line + iMolecule.getProperty(propertyName) + "\t";
				} else if (iAtom.getProperty(propertyName) != null) {
//						System.out.println(iAtom.getProperty(propertyName));
					line = line + iAtom.getProperty(propertyName) + "\t";
				} else {
					line = line + "\t";
				}
			}
			if (!iMolecule.getAtom(atomNumber).getSymbol().equals("H")) {
				//Weka doesn't like to interpret csv files which use tab as a separator and also include "," (these come from the conversion of Arrays into Strings)
				line = line.replaceAll(",", ";");
				line = line.substring(0, line.length()-1);
				p.println(line);
			}
		}
		p.close();

	}
}
