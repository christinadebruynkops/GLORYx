package org.zbh.fame.fame3.utils;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

public class CSVWriter {

	private static String separator = ";";

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
			head = head.replaceAll(",", separator);
			head = head.replaceAll(" ", "");
			p.println(head);		
			p.close();
 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	

	public void write(IAtomContainer iMolecule,String filename, ArrayList<String> header) throws Exception {
		FileOutputStream out = new FileOutputStream(filename, true);
		PrintStream p = new PrintStream(out);
		for (int atomNumber = 0; atomNumber < iMolecule.getAtomCount(); atomNumber++) {
			IAtom iAtom = iMolecule.getAtom(atomNumber);

			if (iAtom.getSymbol().equals("H")) {
				continue;
			}

			Iterator<String> itrHeader = header.iterator();
			String line = "";
			while (itrHeader.hasNext()) {
				String propertyName = itrHeader.next();
				if (iAtom.getProperty(propertyName) != null) {
//						System.out.println(iMolecule.getProperty(propertyName));
					line = line + iAtom.getProperty(propertyName) + separator;
				} else if (iMolecule.getProperty(propertyName) != null) {
//						System.out.println(iAtom.getProperty(propertyName));
					line = line + iMolecule.getProperty(propertyName) + separator;
				} else {
					line = line + "NA" + separator;
//					throw new Exception("Property not found: " + propertyName);
				}
			}
//			if (!iMolecule.getAtom(atomNumber).getSymbol().equals("H")) {
//				//Weka doesn't like to interpret csv files which use tab as a separator and also include "," (these come from the conversion of Arrays into Strings)
//				line = line.replaceAll(",", ";");
//				line = line.substring(0, line.length()-1);
//				p.println(line);
//			}
			p.println(line);
		}
		p.close();

	}
}
