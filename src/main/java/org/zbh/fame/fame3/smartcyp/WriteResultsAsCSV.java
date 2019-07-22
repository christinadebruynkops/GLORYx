/* 
 * Copyright (C) 2010-2013  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
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
import org.openscience.cdk.MoleculeSet;

import org.zbh.fame.fame3.smartcyp.MoleculeKU.SMARTCYP_PROPERTY;



public class WriteResultsAsCSV {

	PrintWriter outfile;
	String moleculeID;
	private String dateAndTime;
	String[] namesOfInfiles;
	String OutputDir;
	String OutputFile;
	int printall;
	

	DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	//DecimalFormat twoDecimalFormat = new DecimalFormat();


	public WriteResultsAsCSV(String dateTime, String[] infileNames, String outputdir, String outputfile, int PrintAll){
		dateAndTime = dateTime;
		namesOfInfiles = infileNames;
		OutputDir = outputdir;
		OutputFile = outputfile;
		printall = PrintAll;

		// DecimalFormat for A
		twoDecimalFormat.setDecimalSeparatorAlwaysShown(false);
		DecimalFormatSymbols decformat = new DecimalFormatSymbols();
		decformat.setDecimalSeparator('.');
		decformat.setGroupingSeparator(',');
		twoDecimalFormat.setMaximumFractionDigits(2);
		twoDecimalFormat.setDecimalFormatSymbols(decformat);
	}



	public void writeCSV(MoleculeSet moleculeSet) {
		
		if (OutputFile=="") OutputFile = "SMARTCyp_Results_" + this.dateAndTime;

		try {
			outfile = new PrintWriter(new BufferedWriter(new FileWriter(this.OutputDir + OutputFile + ".csv")));
		} catch (IOException e) {
			System.out.println("Could not create CSV outfile");
			e.printStackTrace();
		}

		outfile.println("Molecule,Atom,Ranking,Score,Energy,Relative Span,2D6ranking,2D6score,Span2End,N+Dist,2Cranking,2Cscore,COODist,2DSASA");
		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S


		// Iterate MoleculKUs
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {

			// Set variables
			MoleculeKU moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
			moleculeID = moleculeKU.getID();

			// Iterate Atoms
			for(int atomIndex = 0; atomIndex < moleculeKU.getAtomCount()  ; atomIndex++ ){
				
				currentAtom = (Atom) moleculeKU.getAtom(atomIndex);

				// Match atom symbol
				currentAtomType = currentAtom.getSymbol();
				if(printall == 1 || currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
					
					outfile.print((moleculeIndex + 1) + "," + currentAtom.getSymbol() + "."+ currentAtom.getID() + "," + SMARTCYP_PROPERTY.Ranking.get(currentAtom) + ",");				
					if(SMARTCYP_PROPERTY.Score.get(currentAtom) != null) 
						outfile.print(twoDecimalFormat.format(SMARTCYP_PROPERTY.Score.get(currentAtom)) + "," + SMARTCYP_PROPERTY.Energy.get(currentAtom));
					else outfile.print("999,999");
					outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Accessibility.get(currentAtom)));
					if(SMARTCYP_PROPERTY.Score2D6.get(currentAtom) != null) {
						outfile.print("," + SMARTCYP_PROPERTY.Ranking2D6.get(currentAtom));
						outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Score2D6.get(currentAtom)));
					}
					else outfile.print("999,999");
					outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Span2End.get(currentAtom)));
					if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(currentAtom) != null)
						outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Dist2ProtAmine.get(currentAtom)));
					else outfile.print(",0");
					if(SMARTCYP_PROPERTY.Score2C9.get(currentAtom) != null) {
						outfile.print("," + SMARTCYP_PROPERTY.Ranking2C9.get(currentAtom));
						outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Score2C9.get(currentAtom)));
					}
					else outfile.print("999,999");
					if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(currentAtom) != null)
						outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(currentAtom)));
					else outfile.print(",0");
					if(SMARTCYP_PROPERTY.SASA2D.get(currentAtom) != null) {
						outfile.print("," + twoDecimalFormat.format(SMARTCYP_PROPERTY.SASA2D.get(currentAtom)));
					}
					else outfile.print(",0");
					outfile.print("\n");
				}
			}

		}


		outfile.close();
	}
}




