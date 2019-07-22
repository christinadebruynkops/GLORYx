/* 
 * Copyright (C) 2013  Patrik Rydberg <patrik.rydberg@gmail.com>
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

package org.zbh.fame.fame3.smartcyp;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.smiles.SmilesGenerator;



public class WriteFilterValuesAsCSV {

	PrintWriter outfile;
	String moleculeID;
	private String dateAndTime;
	String[] namesOfInfiles;
	String OutputDir;
	String OutputFile;
	double FilterValue;

	DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	//DecimalFormat twoDecimalFormat = new DecimalFormat();


	public WriteFilterValuesAsCSV(String dateTime, String[] infileNames, String outputdir, String outputfile){
		dateAndTime = dateTime;
		namesOfInfiles = infileNames;
		OutputDir = outputdir;
		OutputFile = outputfile;

		// DecimalFormat for value
		twoDecimalFormat.setDecimalSeparatorAlwaysShown(false);
		DecimalFormatSymbols decformat = new DecimalFormatSymbols();
		decformat.setDecimalSeparator('.');
		decformat.setGroupingSeparator(',');
		twoDecimalFormat.setMaximumFractionDigits(2);
		twoDecimalFormat.setDecimalFormatSymbols(decformat);
	}



	public void writeCSV(MoleculeSet moleculeSet) {
		
		if (OutputFile=="") OutputFile = "SMARTCyp_Results_Filtervalues_" + this.dateAndTime;
		else OutputFile = OutputFile + "_Filtervalues";

		try {
			outfile = new PrintWriter(new BufferedWriter(new FileWriter(this.OutputDir + OutputFile + ".csv")));
		} catch (IOException e) {
			System.out.println("Could not create CSV outfile");
			e.printStackTrace();
		}

		outfile.println("Molecule,SMILES,Name,FilterValue");
		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S


		// Iterate MoleculKUs
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {

			// Set variables
			MoleculeKU moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
			moleculeID = moleculeKU.getID();
			FilterValue = (Double) moleculeKU.getProperty("FilterValue");
			
			 SmilesGenerator sg = new SmilesGenerator();
			 sg.setUseAromaticityFlag(true);
			 String smilesString = sg.createSMILES(moleculeKU);

			outfile.print((moleculeIndex + 1) + "," + smilesString + "," + moleculeKU.getProperty(CDKConstants.TITLE) + "," + twoDecimalFormat.format(FilterValue) + "\n");
		}


		outfile.close();
	}
}




