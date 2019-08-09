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

package smartcyp;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Iterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.MoleculeSet;

import smartcyp.MoleculeKU.SMARTCYP_PROPERTY;



public class WriteResultsAsHTML {


	PrintWriter outfile;
	TreeSet<Atom> sortedAtomsTreeSet;
	String moleculeID;
	private String dateAndTime;
	String[] namesOfInfiles;
	DecimalFormat twoDecimalFormat = new DecimalFormat();
	String OutputDir;
	String OutputFile;

	
	public WriteResultsAsHTML(String dateTime, String[] infileNames, String outputdir, String outputfile){
		dateAndTime = dateTime;
		namesOfInfiles = infileNames;
		OutputDir = outputdir;
		OutputFile = outputfile;
		
		// DecimalFormat for A
		twoDecimalFormat.setDecimalSeparatorAlwaysShown(false);
		DecimalFormatSymbols decformat = new DecimalFormatSymbols();
		decformat.setDecimalSeparator('.');
		decformat.setGroupingSeparator(',');
		twoDecimalFormat.setMaximumFractionDigits(2);
		twoDecimalFormat.setDecimalFormatSymbols(decformat);
	}

	
	public void writeHTML(MoleculeSet moleculeSet) {
		
		if (OutputFile=="") OutputFile = "SMARTCyp_Results_" + this.dateAndTime;

		try {
			outfile = new PrintWriter(new BufferedWriter(new FileWriter(this.OutputDir + OutputFile + ".html")));
		} catch (IOException e) {
			System.out.println("Could not create HTML outfile");
			e.printStackTrace();
		}

		this.writeHead(moleculeSet);

		this.writeBody(moleculeSet);

		outfile.close();

	}


	public void writeHead(MoleculeSet moleculeSet){

		outfile.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		outfile.println("");
		outfile.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		outfile.println("");
		outfile.println("<head>");
		outfile.println("");
		outfile.println("<title>Results from SMARTCyp</title>");
		outfile.println("<style type=\"text/css\">");
		outfile.println("<!--");
		outfile.println("body {");
		outfile.println("\tbackground: #FFFFFF;");
		outfile.println("\tcolor: #000000;");
		outfile.println("\tmargin: 5px;");
		outfile.println("\tfont-family: Verdana, Arial, sans-serif;");
		outfile.println("\tfont-size: 16px;");
		outfile.println("\t}");
		outfile.println(".boldlarge { margin-left: 20px; font-size: 20px; font-weight : bold; }");
		outfile.println(".molecule { margin-left: 20px }");
		outfile.println("table { }");
		outfile.println("th { text-align:center; font-weight : bold; }");
		outfile.println(".highlight1 {  background: rgb(255,204,102); font-weight : bold; } ");
		outfile.println(".highlight2 {  background: rgb(223,189,174); font-weight : bold; } ");
		outfile.println(".highlight3 {  background: rgb(214,227,181); font-weight : bold; } ");
		outfile.println(".hiddenPic {display:none;} ");
		outfile.println("ul#navlist	{");
		outfile.println("font: bold 14px verdana, arial, sans-serif;");
		outfile.println("list-style-type: none;");
		outfile.println("padding-bottom: 26px;");
		outfile.println("border-bottom: 0px;");
		outfile.println("margin: 0; }");
		outfile.println("ul#navlist li {");
		outfile.println("float: left;");
		outfile.println("height: 24px;");
		outfile.println("margin: 2px 4px 0px 4px;");
		outfile.println("border: 0px;");
		outfile.println("border-top-left-radius: 3px;");
		outfile.println("border-top-right-radius: 3px; }");
		outfile.println("ul#navlist li#cyp3A4 {");
		outfile.println("border-bottom: 1px solid rgb(242,238,234);");
		outfile.println("background-color: rgb(242,238,234); }");
		outfile.println("li#cyp3A4 a { color:  rgb(80,80,100); }");
		outfile.println("ul#navlist li#cyp2D6 {");
		outfile.println("border-bottom: 1px solid rgb(228,235,240);");
		outfile.println("background-color: rgb(228,235,240); }");
		outfile.println("li#cyp2D6 a { color: rgb(80,80,100); }");
		outfile.println("ul#navlist li#cyp2C9 {");
		outfile.println("border-bottom: 1px solid rgb(255,235,235);");
		outfile.println("background-color: rgb(255,235,235); }");
		outfile.println("li#cyp2C9 a { color: rgb(80,80,100); }");
		outfile.println("#navlist a	{ ");
		outfile.println("float: left;");
		outfile.println("display: block;");
		outfile.println("text-decoration: none;");
		outfile.println("padding: 4px;");
		outfile.println("font-style: italic; }");
		outfile.println(".table2d6 {background-color:rgb(228,235,240); border-radius: 5px; padding: 5px}");
		outfile.println(".table2c9 {background-color:rgb(255,235,235); border-radius: 5px; padding: 5px}");
		outfile.println(".table3a4 {background-color:rgb(242,238,234); border-radius: 5px; padding: 5px}");
		outfile.println("-->");
		outfile.println("</style>");
		outfile.println("<script type=\"text/javascript\">");
		outfile.println("function roll_over(img_name, img_src)");
		outfile.println("   {");
		outfile.println("   document[img_name].src = img_src;");
		outfile.println("   }");
		outfile.println("function HideContent(d) {");
		outfile.println("	document.getElementById(d).style.display = 'none';");
		outfile.println("	}");
		outfile.println("function ShowContent(d) {");
		outfile.println("	document.getElementById(d).style.display = 'block';");
		outfile.println("	}");
		outfile.println("function Switch2D6and3A4(cyp2show) {");
		outfile.println("	var cyp2d6list = [];");
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
			int iplusone = moleculeIndex + 1;
			outfile.println("	cyp2d6list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2D6\";");
		}
		outfile.println("	var cyp3a4list = [];");
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
			int iplusone = moleculeIndex + 1;
			outfile.println("	cyp3a4list[" + moleculeIndex + "] = \"molecule" + iplusone + "standard\";");
		}
		outfile.println("	var cyp2c9list = [];");
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
			int iplusone = moleculeIndex + 1;
			outfile.println("	cyp2c9list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2C9\";");
		}
		outfile.println("	var maxmol = " + moleculeSet.getMoleculeCount() + ";");
		outfile.println("	if(cyp2show=='2D6'){");
		outfile.println("		for(var i=0; i<maxmol; i++) {"); 
		outfile.println("			HideContent(cyp3a4list[i]);");
		outfile.println("			HideContent(cyp2c9list[i]);");
		outfile.println("			ShowContent(cyp2d6list[i]);");
		outfile.println("		}");
		outfile.println("	}");
		outfile.println("	if(cyp2show=='2C9'){");
		outfile.println("		for(var i=0; i<maxmol; i++) {"); 
		outfile.println("			HideContent(cyp3a4list[i]);");
		outfile.println("			HideContent(cyp2d6list[i]);");
		outfile.println("			ShowContent(cyp2c9list[i]);");
		outfile.println("		}");
		outfile.println("	}");
		outfile.println("	if(cyp2show=='3A4'){");
		outfile.println("		for(var i=0; i<maxmol; i++) {"); 
		outfile.println("			HideContent(cyp2d6list[i]);");
		outfile.println("			HideContent(cyp2c9list[i]);");
		outfile.println("			ShowContent(cyp3a4list[i]);");
		outfile.println("		}");
		outfile.println("	}");
		outfile.println("}");		
		outfile.println("</script>");
		outfile.println("</head>");
		outfile.println("");

	}

	public void writeBody(MoleculeSet moleculeSet){

		outfile.println("<body>");
		//error message if problems
		if (moleculeSet.getMoleculeCount()==0){
			outfile.println("<h1>There were no molecules in the input</h1>");
		}
		else {
		//no error message, print normal output
		outfile.println("<h1>Results from SMARTCyp version 2.4.2</h1>");
		outfile.println("\n These results were produced: " + this.dateAndTime + ".");
		outfile.println("\n The infiles were: " + Arrays.toString(namesOfInfiles) + ".");
		outfile.println("\n <br /><br /><i>To alternate between heteroatoms and atom numbers, move the mouse cursor over the figure.</i>");
		

		// Iterate MoleculKUs
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {

			MoleculeKU moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
			this.writeMoleculeKUTable(moleculeKU);
		}
		
		
		// Iterate again to preload images with atom numbers
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {

			MoleculeKU moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);
			this.writePreLoadImage(moleculeKU);
		}
		}
		outfile.println("</body>");
		outfile.println("</html>");

	}

	public void writeMoleculeKUTable(MoleculeKU moleculeKU) {

		moleculeID = moleculeKU.getID();

		outfile.println("");
		outfile.println("<!-- Molecule " + moleculeID + " -->");	// Invisible code marker for molecule
		
		String title = moleculeID + ": " + (String) moleculeKU.getProperty(CDKConstants.TITLE);
		if (title==null || title==""){
			title = "Molecule " + moleculeID;
		}
		
		//Write 2D6 output
		outfile.println("<div id='molecule" + moleculeID + "CYP2D6' style='display:none;'>");
		outfile.println("<table>");
		outfile.println("<tr>");
		outfile.println("<td style='vertical-align:top;'>");

		// Table row, contains 1 molecule images and mouseover to a second image with atom numbers
		String image1_2d6 = "smartcyp_images_"+ this.dateAndTime +"/" +"molecule_" + moleculeID + "_heteroAtoms2D6.png";
		String image2_2d6 = "smartcyp_images_"+ this.dateAndTime + "/" + "molecule_" + moleculeID + "_atomNumbers.png";
		outfile.println("<img src=\"" + image1_2d6 + "\" name=\"img2D6" + moleculeID + "\" onmouseover=\"roll_over('img2D6" + moleculeID + "', '" + image2_2d6 + "')\" onmouseout=\"roll_over('img2D6" + moleculeID + "', '" + image1_2d6 + "')\" /><br />");

		outfile.println("</td>");
		outfile.println("<td style='vertical-align:top;'>");
		outfile.println("<ul id='navlist'>");
		outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\" title=\"Click to show standard predictions\">Standard</a></li>");
		outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" title=\"Click to show CYP2C predictions\">CYP2C</a></li>");
		outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" >CYP2D6</a></li>");
		outfile.println("</ul>");
		
		outfile.println("<div class='table2d6'>");

		// Visible header for Molecule
		outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");

		// Table of Atom data
		outfile.println("<table class=\"molecule\">");
		outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>S2End</th><th>N+Dist</th><th>2DSASA</th></tr>");

		// Iterate over the Atoms in this sortedAtomsTreeSet
		sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA2D6();
		Iterator<Atom> sortedAtomsTreeSetIterator2D6 = sortedAtomsTreeSet.iterator();
		Atom currentAtom2D6;
		
		while(sortedAtomsTreeSetIterator2D6.hasNext()){
			currentAtom2D6 = sortedAtomsTreeSetIterator2D6.next();
			this.writeAtomRowinMoleculeKUTable2D6(currentAtom2D6);
		}
		outfile.println("</table>");
		outfile.println("</div>");
		outfile.println("</td>");
		outfile.println("</tr>");
		outfile.println("</table>");
		outfile.println("</div>");
		//end of 2D6 output
		
		//Write 2C9 output
		outfile.println("<div id='molecule" + moleculeID + "CYP2C9' style='display:none;'>");
		outfile.println("<table>");
		outfile.println("<tr>");
		outfile.println("<td style='vertical-align:top;'>");

		// Table row, contains 1 molecule images and mouseover to a second image with atom numbers
		String image1_2c9 = "smartcyp_images_"+ this.dateAndTime +"/" +"molecule_" + moleculeID + "_heteroAtoms2C9.png";
		String image2_2c9 = "smartcyp_images_"+ this.dateAndTime + "/" + "molecule_" + moleculeID + "_atomNumbers.png";
		outfile.println("<img src=\"" + image1_2c9 + "\" name=\"img2C9" + moleculeID + "\" onmouseover=\"roll_over('img2C9" + moleculeID + "', '" + image2_2c9 + "')\" onmouseout=\"roll_over('img2C9" + moleculeID + "', '" + image1_2c9 + "')\" /><br />");

		outfile.println("</td>");
		outfile.println("<td style='vertical-align:top;'>");
		outfile.println("<ul id='navlist'>");
		outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\" title=\"Click to show standard predictions\">Standard</a></li>");
		outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" >CYP2C</a></li>");
		outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" title=\"Click to show CYP2D6 predictions\">CYP2D6</a></li>");
		outfile.println("</ul>");
		
		outfile.println("<div class='table2c9'>");

		// Visible header for Molecule
		outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");

		// Table of Atom data
		outfile.println("<table class=\"molecule\">");
		outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>S2End</th><th>COODist</th><th>2DSASA</th></tr>");

		// Iterate over the Atoms in this sortedAtomsTreeSet
		sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA2C9();
		Iterator<Atom> sortedAtomsTreeSetIterator2C9 = sortedAtomsTreeSet.iterator();
		Atom currentAtom2C9;
		
		while(sortedAtomsTreeSetIterator2C9.hasNext()){
			currentAtom2C9 = sortedAtomsTreeSetIterator2C9.next();
			this.writeAtomRowinMoleculeKUTable2C9(currentAtom2C9);
		}
		outfile.println("</table>");
		outfile.println("</div>");
		outfile.println("</td>");
		outfile.println("</tr>");
		outfile.println("</table>");
		outfile.println("</div>");
		//end of 2C9 output
		
		//Write 3A4 output
		outfile.println("<div id='molecule" + moleculeID + "standard' style='display:;'>");
		outfile.println("<table>");
		outfile.println("<tr>");
		outfile.println("<td style='vertical-align:top;'>");

		// Table row, contains 1 molecule images and mouseover to a second image with atom numbers
		String image1 = "smartcyp_images_"+ this.dateAndTime +"/" +"molecule_" + moleculeID + "_heteroAtoms.png";
		String image2 = "smartcyp_images_"+ this.dateAndTime + "/" + "molecule_" + moleculeID + "_atomNumbers.png";
		outfile.println("<img src=\"" + image1 + "\" name=\"img" + moleculeID + "\" onmouseover=\"roll_over('img" + moleculeID + "', '" + image2 + "')\" onmouseout=\"roll_over('img" + moleculeID + "', '" + image1 + "')\" /><br />");
	
		outfile.println("</td>");
		outfile.println("<td style='vertical-align:top;'>");
		outfile.println("<ul id='navlist'>");
		outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\">Standard</a></li>");
		outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" title=\"Click to show CYP2C predictions\">CYP2C</a></li>");
        outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" title=\"Click to show CYP2D6 predictions\">CYP2D6</a></li>");
        outfile.println("</ul>");
		
		outfile.println("<div class='table3a4'>");

		// Visible header for Molecule
		outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");

		// Table of Atom data
		outfile.println("<table class=\"molecule\">");
		outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>Accessibility</th><th>2DSASA</th></tr>");

		// Iterate over the Atoms in this sortedAtomsTreeSet
		sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA();
		Iterator<Atom> sortedAtomsTreeSetIterator = sortedAtomsTreeSet.iterator();
		Atom currentAtom;
		
		while(sortedAtomsTreeSetIterator.hasNext()){
			currentAtom = sortedAtomsTreeSetIterator.next();
			this.writeAtomRowinMoleculeKUTable(currentAtom);
		}
		outfile.println("</table>");
		outfile.println("</div>");
		outfile.println("</td>");
		outfile.println("</tr>");
		outfile.println("</table>");
		outfile.println("</div>");
		//end of 3A4 output
		outfile.println("<hr />");
	}

	public void writePreLoadImage(MoleculeKU moleculeKU) {

		moleculeID = moleculeKU.getID();

		outfile.println("");
		String image2 = "smartcyp_images_"+ this.dateAndTime + "/" + "molecule_" + moleculeID + "_atomNumbers.png";
		outfile.println("<img src=\"" + image2 + "\" class=\"hiddenPic\"  />");
	}

	public void writeAtomRowinMoleculeKUTable(Atom atom){

		if(SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
		else if(SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
		else if(SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
		else outfile.println("<tr>");

		outfile.println("<td>" + SMARTCYP_PROPERTY.Ranking.get(atom).intValue() + "</td>");
		outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
		if(SMARTCYP_PROPERTY.Score.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + twoDecimalFormat.format(SMARTCYP_PROPERTY.Score.get(atom)) + "</td>");
		if(SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
		outfile.println("<td>" +  twoDecimalFormat.format(SMARTCYP_PROPERTY.Accessibility.get(atom)) + "</td>");
		outfile.println("<td>" +  twoDecimalFormat.format(SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
		outfile.println("</tr>");
	}

	public void writeAtomRowinMoleculeKUTable2D6(Atom atom){

		if(SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
		else if(SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
		else if(SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
		else outfile.println("<tr>");

		outfile.println("<td>" + SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() + "</td>");
		outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
		if(SMARTCYP_PROPERTY.Score2D6.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + twoDecimalFormat.format(SMARTCYP_PROPERTY.Score2D6.get(atom)) + "</td>");
		if(SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
		outfile.println("<td>" +  SMARTCYP_PROPERTY.Span2End.get(atom).intValue() + "</td>");
		if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" +  SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom).intValue() + "</td>");
		outfile.println("<td>" +  twoDecimalFormat.format(SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
		outfile.println("</tr>");
	}

	public void writeAtomRowinMoleculeKUTable2C9(Atom atom){

		if(SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
		else if(SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
		else if(SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
		else outfile.println("<tr>");

		outfile.println("<td>" + SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() + "</td>");
		outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
		if(SMARTCYP_PROPERTY.Score2C9.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + twoDecimalFormat.format(SMARTCYP_PROPERTY.Score2C9.get(atom)) + "</td>");
		if(SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" + SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
		outfile.println("<td>" +  SMARTCYP_PROPERTY.Span2End.get(atom).intValue() + "</td>");
		if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(atom) == null) outfile.println("<td>-</td>");
		else outfile.println("<td>" +  SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(atom).intValue() + "</td>");
		outfile.println("<td>" +  twoDecimalFormat.format(SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
		outfile.println("</tr>");
	}
}


