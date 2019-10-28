/* 
 * Copyright (C) 2012  Patrik Rydberg <patrik.rydberg@gmail.com>
 * 
 * Contact: smartcyp@farma.ku.dk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package smartcyp;



import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.listener.PropertiesListener;



public class WriteFilterStructures {

	String moleculeID;
	private String dateAndTime;
	String[] namesOfInfiles;
	String OutputDir;
	String OutputFile;
	boolean SmilesInput;
	double FilterValue;

	DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	//DecimalFormat twoDecimalFormat = new DecimalFormat();


	public WriteFilterStructures(String dateTime, String[] infileNames, String outputdir, String outputfile, boolean smilesinput){
		dateAndTime = dateTime;
		namesOfInfiles = infileNames;
		OutputDir = outputdir;
		OutputFile = outputfile;
		SmilesInput = smilesinput;

	}


	public void write(MoleculeSet moleculeSet) throws CDKException {
	
		if (OutputFile=="") OutputFile = "SMARTCyp_Results_Filtervalues_" + this.dateAndTime;
		else OutputFile = OutputFile + "_structures";
		
		if (SmilesInput == true){
			try {
				SMILESWriter outfile = new SMILESWriter(new FileWriter(this.OutputDir + OutputFile + ".smi"));
				Properties customSettings = new Properties();
				customSettings.setProperty("UseAromaticity", "true");
				PropertiesListener listener = new PropertiesListener(customSettings);
				outfile.addChemObjectIOListener(listener);
				outfile.customizeJob();
				outfile.writeMoleculeSet(moleculeSet);
				outfile.close();
			}
			catch (IOException e) {
				System.out.println("Could not create SMILES outfile");
				e.printStackTrace();
			}
		}
		else {
			try {
				SDFWriter outfile = new SDFWriter(new FileWriter(this.OutputDir + OutputFile + ".sdf"));
				outfile.write(moleculeSet);
				outfile.close();
			}
			catch (IOException e) {
				System.out.println("Could not create SDF outfile");
				e.printStackTrace();
			}
		}
	}
}




