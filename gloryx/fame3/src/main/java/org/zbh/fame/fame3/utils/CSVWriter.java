/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
   Copyright (C) 2013  Johannes Kirchmair <johannes.kirchmair@univie.ac.at>
 
    This file is part of GLORYx.

    GLORYx is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    All we ask is that proper credit is given for our work, which includes 
    - but is not limited to - adding the above copyright notice to the beginning 
    of your source code files, and to any copyright notice that you may distribute 
    with programs based on this work.

    GLORYx is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GLORYx.  If not, see <https://www.gnu.org/licenses/>.
*/

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
