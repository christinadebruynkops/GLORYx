package utils.depiction;

import globals.Globals;
import modelling.Modeller;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.listener.PropertiesListener;
import smartcyp.MoleculeKU;
import smartcyp.WriteResultsAsChemDoodleHTML;
import utils.MoleculeKUFAME;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Created by sicho on 1/24/17.
 */
public class DepictorSMARTCyp extends WriteResultsAsChemDoodleHTML {

    private Globals globals;

    public DepictorSMARTCyp(String dateTime, String[] infileNames, String outputdir, String outputfile, Globals globals) {
        super(dateTime, infileNames, outputdir, outputfile);
        this.globals = globals;
    }

    public void writeHTML(MoleculeSet moleculeSet) {

        if (OutputFile=="") OutputFile = "FAME_2_Results_" + this.dateAndTime;

        try {
            outfile = new PrintWriter(new BufferedWriter(new FileWriter(this.OutputFile)));
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
        outfile.println("<title>FAME II Output</title>");
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
        outfile.println("ul#navlist li#cypHLM {");
        outfile.println("border-bottom: 1px solid rgb(255,235,235);");
        outfile.println("background-color: rgb(255,235,235); }");
        outfile.println("li#cypHLM a { color:  rgb(80,80,100); }");
//        outfile.println("ul#navlist li#cyp3A4 {");
//        outfile.println("border-bottom: 1px solid rgb(242,238,234);");
//        outfile.println("background-color: rgb(242,238,234); }");
//        outfile.println("li#cyp3A4 a { color:  rgb(80,80,100); }");
//        outfile.println("ul#navlist li#cyp2D6 {");
//        outfile.println("border-bottom: 1px solid rgb(228,235,240);");
//        outfile.println("background-color: rgb(228,235,240); }");
//        outfile.println("li#cyp2D6 a { color: rgb(80,80,100); }");
//        outfile.println("ul#navlist li#cyp2C9 {");
//        outfile.println("border-bottom: 1px solid rgb(255,235,235);");
//        outfile.println("background-color: rgb(255,235,235); }");
//        outfile.println("li#cyp2C9 a { color: rgb(80,80,100); }");
        outfile.println("#navlist a	{ ");
        outfile.println("float: left;");
        outfile.println("display: block;");
        outfile.println("text-decoration: none;");
        outfile.println("padding: 4px;");
        outfile.println("font-style: italic; }");
        outfile.println(".tableHLM {background-color:rgb(255,235,235); border-radius: 5px; padding: 5px}");
//        outfile.println(".table2c9 {background-color:rgb(255,235,235); border-radius: 5px; padding: 5px}");
//        outfile.println(".table2d6 {background-color:rgb(228,235,240); border-radius: 5px; padding: 5px}");
//        outfile.println(".table3a4 {background-color:rgb(242,238,234); border-radius: 5px; padding: 5px}");
        outfile.println("canvas.ChemDoodleWebComponent {border: none;}");
        outfile.println("-->");
        outfile.println("</style>");
        outfile.println("<script type=\"text/javascript\" src=\"../ui/ChemDoodleWeb-libs.js\"></script>");
        outfile.println("<script type=\"text/javascript\" src=\"../ui/ChemDoodleWeb.js\"></script>");
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
        outfile.println("	var cypHLMlist = [];");
        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
            int iplusone = moleculeIndex + 1;
            outfile.println("	cypHLMlist[" + moleculeIndex + "] = \"molecule" + iplusone + "CYPHLMdiv\";");
        }
//        outfile.println("	var cyp2c9list = [];");
//        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
//            int iplusone = moleculeIndex + 1;
//            outfile.println("	cyp2c9list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2C9\";");
//        }
//        outfile.println("	var cyp2d6list = [];");
//        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
//            int iplusone = moleculeIndex + 1;
//            outfile.println("	cyp2d6list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2D6div\";");
//        }
//        outfile.println("	var cyp3a4list = [];");
//        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
//            int iplusone = moleculeIndex + 1;
//            outfile.println("	cyp3a4list[" + moleculeIndex + "] = \"molecule" + iplusone + "standarddiv\";");
//        }
        outfile.println("	var maxmol = " + moleculeSet.getMoleculeCount() + ";");
        outfile.println("	if(cyp2show=='HLM'){");
        outfile.println("		for(var i=0; i<maxmol; i++) {");
        outfile.println("			HideContent(cypHLMlist[i]);");
        outfile.println("			HideContent(cypHLMlist[i]);");
        outfile.println("			ShowContent(cypHLMlist[i]);");
        outfile.println("		}");
        outfile.println("	}");
//        outfile.println("	if(cyp2show=='2C9'){");
//        outfile.println("		for(var i=0; i<maxmol; i++) {");
//        outfile.println("			HideContent(cyp3a4list[i]);");
//        outfile.println("			HideContent(cyp2d6list[i]);");
//        outfile.println("			ShowContent(cyp2c9list[i]);");
//        outfile.println("		}");
//        outfile.println("	}");
//        outfile.println("	if(cyp2show=='2D6'){");
//        outfile.println("		for(var i=0; i<maxmol; i++) {");
//        outfile.println("			HideContent(cyp2c9list[i]);");
//        outfile.println("			HideContent(cyp3a4list[i]);");
//        outfile.println("			ShowContent(cyp2d6list[i]);");
//        outfile.println("		}");
//        outfile.println("	}");
//        outfile.println("	if(cyp2show=='3A4'){");
//        outfile.println("		for(var i=0; i<maxmol; i++) {");
//        outfile.println("			HideContent(cyp2c9list[i]);");
//        outfile.println("			HideContent(cyp2d6list[i]);");
//        outfile.println("			ShowContent(cyp3a4list[i]);");
//        outfile.println("		}");
//        outfile.println("	}");
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
            outfile.println("<h1>FAME II Output</h1>");
            outfile.println("\n <b>Produced:</b> " + this.dateAndTime + ".<br />");
            outfile.println("\n <b>Input file:</b> " + Arrays.toString(namesOfInfiles) + ".<br />");
            outfile.println("\n <br /><br /><b>Visualization:</b><br />");
            outfile.println("\n <i>To alternate between atoms and atom numbers, move the mouse cursor over the figure.</i>");


            // Iterate MoleculKUs
            for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {

                MoleculeKU moleculeKU = (MoleculeKU) moleculeSet.getMolecule(moleculeIndex);

                this.writeMoleculeKUTable(moleculeKU);
            }

        }
        outfile.println("</body>");
        outfile.println("</html>");

    }

    public void writeMoleculeKUTable(MoleculeKU moleculeKU) {

        moleculeID = moleculeKU.getProperty(Globals.ID_PROP).toString();

        outfile.println("");
        outfile.println("<!-- Molecule " + moleculeID + " -->");	// Invisible code marker for molecule

        String title = "Molecule " + moleculeID;

        //preconstruct coordinates and stuff for each molecule
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MDLV2000Writer writer = new MDLV2000Writer(baos);
        //generate 2D coordinates
        moleculeKU = this.generate2Dcoordinates(moleculeKU);

        //force 2D coordinates even if 3D exists
        Properties customSettings = new Properties();
        customSettings.setProperty(
                "ForceWriteAs2DCoordinates", "true"
        );
        customSettings.setProperty(
                "WriteAromaticBondTypes", "true"
        );

        PropertiesListener listener = new PropertiesListener(customSettings);
        writer.addChemObjectIOListener(listener);
        //end force 2D coordinates
        try {
            writer.write(moleculeKU);
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String MoleculeString = baos.toString();
        //now split molecule into multiple lines to enable explicit printout of \n
        String Moleculelines[] = MoleculeString.split("\\r?\\n");

        //define a suitable canvas size in the y-axis
        int ycanvassize = 300;
        double [] molMinMax = GeometryTools.getMinMax(moleculeKU);
        double ylength = molMinMax[3]-molMinMax[1];
        int minLenghtForIncrease = 8;
        if(ylength > minLenghtForIncrease){ycanvassize = (int) (300+(ylength-minLenghtForIncrease)*20);}

        // Write HLM output
        String MolNameHLM = "molecule" + moleculeKU.getProperty(Globals.ID_PROP).toString() + "HLM";
        outfile.println("<div id='molecule" + moleculeID + "CYPHLMdiv'>");
//        outfile.println("<div id='molecule" + moleculeID + "CYPHLMdiv' style='display:none;'>");
        outfile.println("<table>");
        outfile.println("<tr>");
        outfile.println("<td style='vertical-align:top;'>");

        // Table row, contains 1 molecule canvas through ChemDoodle
        //output chemdoodle HLM molecule here

        outfile.println("<script>");
        outfile.println("var " + MolNameHLM + " = new ChemDoodle.ViewerCanvas('" + MolNameHLM + "', 400, " + ycanvassize + ");");
        outfile.println(MolNameHLM + ".specs.atoms_useJMOLColors = true;");
        outfile.print("var " + MolNameHLM + "MolFile = '");
        for(int i=0; i<Moleculelines.length; i++){
            outfile.print(Moleculelines[i]);
            outfile.print("\\n");
        }
        outfile.print("'; \n");
        outfile.println("var " + MolNameHLM + "Mol = ChemDoodle.readMOL(" + MolNameHLM + "MolFile); ");
        outfile.println("new ChemDoodle.informatics.HydrogenDeducer().removeHydrogens(" + MolNameHLM + "Mol, false);");
        outfile.println("// get the dimension of the molecule");
        outfile.println("var size = " + MolNameHLM + "Mol.getDimension();");
        outfile.println("// find the scale by taking the minimum of the canvas/size ratios");
        outfile.println("var scale = Math.min(" + MolNameHLM + ".width/size.x, " + MolNameHLM + ".height/size.y);");
        outfile.println("// load the molecule first (this function automatically sets scale, so we need to change specs after");
        outfile.println(MolNameHLM + ".loadMolecule(" + MolNameHLM + "Mol);");
        outfile.println("// change the specs.scale value to the scale calculated, shrinking it slightly so that text is not cut off");
        outfile.println(MolNameHLM + ".specs.scale = scale*.8;");
        outfile.println(MolNameHLM + ".mouseover = function(){");
        outfile.println("	for (var i = 0, ii=" + MolNameHLM + "Mol.atoms.length; i<ii; i++) {");
        outfile.println("		" + MolNameHLM + "Mol.atoms[i].altLabel = i+1;");
        outfile.println("	}");
        outfile.println("this.repaint();");
        outfile.println("}");
        outfile.println(MolNameHLM + ".mouseout = function(){");
        outfile.println("	for (var i = 0, ii=" + MolNameHLM + "Mol.atoms.length; i<ii; i++) {");
        outfile.println("		delete " + MolNameHLM + "Mol.atoms[i].altLabel;");
        outfile.println("	}");
        outfile.println("	this.repaint();");
        outfile.println("}");
        outfile.println(MolNameHLM + ".touchstart = function(){");
        outfile.println("	for (var i = 0, ii=" + MolNameHLM + "Mol.atoms.length; i<ii; i++) {");
        outfile.println("		" + MolNameHLM + "Mol.atoms[i].altLabel = i+1;");
        outfile.println("	}");
        outfile.println("this.repaint();");
        outfile.println("}");
        outfile.println(MolNameHLM + ".touchend = function(){");
        outfile.println("	for (var i = 0, ii=" + MolNameHLM + "Mol.atoms.length; i<ii; i++) {");
        outfile.println("		delete " + MolNameHLM + "Mol.atoms[i].altLabel;");
        outfile.println("	}");
        outfile.println("	this.repaint();");
        outfile.println("}");
        outfile.println(MolNameHLM + ".drawChildExtras = function(ctx){");
        outfile.println("	ctx.save();");
        outfile.println("	ctx.translate(this.width/2, this.height/2);");
        outfile.println("	ctx.rotate(this.specs.rotateAngle);");
        outfile.println("	ctx.scale(this.specs.scale, this.specs.scale);");
        outfile.println("	ctx.translate(-this.width/2, -this.height/2);");
        outfile.println("	//draw atom numbers and draw circles on ranked atoms");
        outfile.println("	ctx.lineWidth = 2 + 4/this.specs.scale;");
        outfile.println("	radius = 6 + 6/this.specs.scale;");
        outfile.println("	for (var i = 0, ii=" + MolNameHLM + "Mol.atoms.length; i<ii; i++) {");
        outfile.println("		var atom = " + MolNameHLM + "Mol.atoms[i];");
        //iterate through the atoms and add circles to top 3 rank
        // Iterate over the Atoms in this molecule
        IAtom rankAtom;
        for (int atomIndex=0; atomIndex < moleculeKU.getAtomCount(); atomIndex++) {
            rankAtom = moleculeKU.getAtom(atomIndex);
            if (rankAtom.getSymbol().equals("H")) {
                continue;
            }
            String AtomNr = rankAtom.getID();
            boolean is_som = (Boolean) rankAtom.getProperty(Modeller.is_som_fld);
            if(is_som){
                outfile.println("		if((i + 1) == " + AtomNr + "){");
                outfile.println("			ctx.strokeStyle = 'rgb(255,204,102)';");
                outfile.println("			ctx.beginPath();");
                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
                outfile.println("			ctx.stroke();");
                outfile.println("		}");
            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom).intValue() == 2){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(223,189,174)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom).intValue() == 3){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(214,227,181)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
        }
        outfile.println("	}");
        outfile.println("	ctx.restore();");
        outfile.println("}");
        outfile.println(MolNameHLM + ".repaint();");
        outfile.println("</script>");
        //end of chemdoodle HLM molecule
        outfile.println("</td>");
        outfile.println("<td style='vertical-align:top;'>");
        outfile.println("<ul id='navlist'>");
        outfile.println("<li id='cypHLM' style=\"padding: 2px;\">Model: " + globals.model_code + "</li>");
//        outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\" title=\"Click to show standard predictions\">Standard</a></li>");
//        outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" title=\"Click to show CYP2C predictions\">CYP2C</a></li>");
//        outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" >CYP2D6</a></li>");
        outfile.println("</ul>");

        outfile.println("<div class='tableHLM'>");

        // Visible header for Molecule
        outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");

        // Table of Atom data
        outfile.println("<table class=\"molecule\">");
        outfile.println("<tr><th>Atom</th><th>Probability</th></tr>");

        // Iterate over the Atoms in this sortedAtomsTreeSet
        TreeSet<IAtom> sortedAtomsTreeSet = (TreeSet<IAtom>) ((MoleculeKUFAME) moleculeKU).getAtomsSortedByHLMProbability();

        Iterator<IAtom> sortedAtomsTreeSetIteratorHLM = sortedAtomsTreeSet.iterator();
        IAtom currentAtomHLM;

        while(sortedAtomsTreeSetIteratorHLM.hasNext()){
            currentAtomHLM = sortedAtomsTreeSetIteratorHLM.next();
            this.writeAtomRowinMoleculeKUTableHLM(currentAtomHLM);
        }
        outfile.println("</table>");
        outfile.println("</div>");
        outfile.println("</td>");
        outfile.println("</tr>");
        outfile.println("</table>");
        outfile.println("</div>");
        //end of HLM output

        //Write 2D6 output
//        String MolName2D6 = "molecule" + moleculeID + "2D6";
//        outfile.println("<div id='molecule" + moleculeID + "CYP2D6div' style='display:none;'>");
//        outfile.println("<table>");
//        outfile.println("<tr>");
//        outfile.println("<td style='vertical-align:top;'>");

//        // Table row, contains 1 molecule canvas through ChemDoodle
//        //output chemdoodle 2D6 molecule here
//
//        outfile.println("<script>");
//        outfile.println("var " + MolName2D6 + " = new ChemDoodle.ViewerCanvas('" + MolName2D6 + "', 400, " + ycanvassize + ");");
//        outfile.println(MolName2D6 + ".specs.atoms_useJMOLColors = true;");
//        outfile.print("var " + MolName2D6 + "MolFile = '");
//        for(int i=0; i<Moleculelines.length; i++){
//            outfile.print(Moleculelines[i]);
//            outfile.print("\\n");
//        }
//        outfile.print("'; \n");
//        outfile.println("var " + MolName2D6 + "Mol = ChemDoodle.readMOL(" + MolName2D6 + "MolFile); ");
//        outfile.println("// get the dimension of the molecule");
//        outfile.println("var size = " + MolName2D6 + "Mol.getDimension();");
//        outfile.println("// find the scale by taking the minimum of the canvas/size ratios");
//        outfile.println("var scale = Math.min(" + MolName2D6 + ".width/size.x, " + MolName2D6 + ".height/size.y);");
//        outfile.println("// load the molecule first (this function automatically sets scale, so we need to change specs after");
//        outfile.println(MolName2D6 + ".loadMolecule(" + MolName2D6 + "Mol);");
//        outfile.println("// change the specs.scale value to the scale calculated, shrinking it slightly so that text is not cut off");
//        outfile.println(MolName2D6 + ".specs.scale = scale*.8;");
//        outfile.println(MolName2D6 + ".mouseover = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2D6 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName2D6 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2D6 + ".mouseout = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2D6 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName2D6 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2D6 + ".touchstart = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2D6 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName2D6 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2D6 + ".touchend = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2D6 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName2D6 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2D6 + ".drawChildExtras = function(ctx){");
//        outfile.println("	ctx.save();");
//        outfile.println("	ctx.translate(this.width/2, this.height/2);");
//        outfile.println("	ctx.rotate(this.specs.rotateAngle);");
//        outfile.println("	ctx.scale(this.specs.scale, this.specs.scale);");
//        outfile.println("	ctx.translate(-this.width/2, -this.height/2);");
//        outfile.println("	//draw atom numbers and draw circles on ranked atoms");
//        outfile.println("	ctx.lineWidth = 2 + 4/this.specs.scale;");
//        outfile.println("	radius = 6 + 6/this.specs.scale;");
//        outfile.println("	for (var i = 0, ii=" + MolName2D6 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		var atom = " + MolName2D6 + "Mol.atoms[i];");
//        //iterate through the atoms and add circles to top 3 rank
//        // Iterate over the Atoms in this molecule
//        IAtom rankAtom;
//        for (int atomIndex=0; atomIndex < moleculeKU.getAtomCount(); atomIndex++) {
//            rankAtom = moleculeKU.getAtom(atomIndex);
//            String AtomNr = rankAtom.getID();
//            if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom).intValue() == 1){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(255,204,102)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom).intValue() == 2){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(223,189,174)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(rankAtom).intValue() == 3){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(214,227,181)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//        }
//        outfile.println("	}");
//        outfile.println("	ctx.restore();");
//        outfile.println("}");
//        outfile.println(MolName2D6 + ".repaint();");
//        outfile.println("</script>");
//        //end of chemdoodle 2D6 molecule
//        outfile.println("</td>");
//        outfile.println("<td style='vertical-align:top;'>");
//        outfile.println("<ul id='navlist'>");
//        outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\" title=\"Click to show standard predictions\">Standard</a></li>");
//        outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" title=\"Click to show CYP2C predictions\">CYP2C</a></li>");
//        outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" >CYP2D6</a></li>");
//        outfile.println("</ul>");
//
//        outfile.println("<div class='table2d6'>");
//
//        // Visible header for Molecule
//        outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");
//
//        // Table of Atom data
//        outfile.println("<table class=\"molecule\">");
//        outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>S2End</th><th>N+Dist</th><th>2DSASA</th></tr>");
//
//        // Iterate over the Atoms in this sortedAtomsTreeSet
//        sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA2D6();
//        Iterator<Atom> sortedAtomsTreeSetIterator2D6 = sortedAtomsTreeSet.iterator();
//        Atom currentAtom2D6;
//
//        while(sortedAtomsTreeSetIterator2D6.hasNext()){
//            currentAtom2D6 = sortedAtomsTreeSetIterator2D6.next();
//            this.writeAtomRowinMoleculeKUTable2D6(currentAtom2D6);
//        }
//        outfile.println("</table>");
//        outfile.println("</div>");
//        outfile.println("</td>");
//        outfile.println("</tr>");
//        outfile.println("</table>");
//        outfile.println("</div>");
//        //end of 2D6 output
//
//        //Write 2C9 output
//        String MolName2C9 = "molecule" + moleculeID + "2C9";
//        outfile.println("<div id='molecule" + moleculeID + "CYP2C9' style='display:none;'>");
//        outfile.println("<table>");
//        outfile.println("<tr>");
//        outfile.println("<td style='vertical-align:top;'>");
//
//        // Table row, contains 1 molecule canvas through ChemDoodle
//        //output chemdoodle 2C9 molecule here
//
//        outfile.println("<script>");
//        outfile.println("var " + MolName2C9 + " = new ChemDoodle.ViewerCanvas('" + MolName2C9 + "', 400, " + ycanvassize + ");");
//        outfile.println(MolName2C9 + ".specs.atoms_useJMOLColors = true;");
//        outfile.print("var " + MolName2C9 + "MolFile = '");
//        for(int i=0; i<Moleculelines.length; i++){
//            outfile.print(Moleculelines[i]);
//            outfile.print("\\n");
//        }
//        outfile.print("'; \n");
//        outfile.println("var " + MolName2C9 + "Mol = ChemDoodle.readMOL(" + MolName2C9 + "MolFile); ");
//        outfile.println("// get the dimension of the molecule");
//        outfile.println("var size = " + MolName2C9 + "Mol.getDimension();");
//        outfile.println("// find the scale by taking the minimum of the canvas/size ratios");
//        outfile.println("var scale = Math.min(" + MolName2C9 + ".width/size.x, " + MolName2C9 + ".height/size.y);");
//        outfile.println("// load the molecule first (this function automatically sets scale, so we need to change specs after");
//        outfile.println(MolName2C9 + ".loadMolecule(" + MolName2C9 + "Mol);");
//        outfile.println("// change the specs.scale value to the scale calculated, shrinking it slightly so that text is not cut off");
//        outfile.println(MolName2C9 + ".specs.scale = scale*.8;");
//        outfile.println(MolName2C9 + ".mouseover = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2C9 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName2C9 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2C9 + ".mouseout = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2C9 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName2C9 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2C9 + ".touchstart = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2C9 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName2C9 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2C9 + ".touchend = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName2C9 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName2C9 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName2C9 + ".drawChildExtras = function(ctx){");
//        outfile.println("	ctx.save();");
//        outfile.println("	ctx.translate(this.width/2, this.height/2);");
//        outfile.println("	ctx.rotate(this.specs.rotateAngle);");
//        outfile.println("	ctx.scale(this.specs.scale, this.specs.scale);");
//        outfile.println("	ctx.translate(-this.width/2, -this.height/2);");
//        outfile.println("	//draw atom numbers and draw circles on ranked atoms");
//        outfile.println("	ctx.lineWidth = 2 + 4/this.specs.scale;");
//        outfile.println("	radius = 6 + 6/this.specs.scale;");
//        outfile.println("	for (var i = 0, ii=" + MolName2C9 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		var atom = " + MolName2C9 + "Mol.atoms[i];");
//        //iterate through the atoms and add circles to top 3 rank
//        // Iterate over the Atoms in this molecule
//        for (int atomIndex=0; atomIndex < moleculeKU.getAtomCount(); atomIndex++) {
//            rankAtom = moleculeKU.getAtom(atomIndex);
//            String AtomNr = rankAtom.getID();
//            if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom).intValue() == 1){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(255,204,102)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom).intValue() == 2){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(223,189,174)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(rankAtom).intValue() == 3){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(214,227,181)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//        }
//        outfile.println("	}");
//        outfile.println("	ctx.restore();");
//        outfile.println("}");
//        outfile.println(MolName2C9 + ".repaint();");
//        outfile.println("</script>");
//        //end of chemdoodle 2C9 molecule


//        outfile.println("</td>");
//        outfile.println("<td style='vertical-align:top;'>");
//        outfile.println("<ul id='navlist'>");
//        outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\" title=\"Click to show standard predictions\">Standard</a></li>");
//        outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" >CYP2C</a></li>");
//        outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" title=\"Click to show CYP2D6 predictions\">CYP2D6</a></li>");
//        outfile.println("</ul>");
//
//        outfile.println("<div class='table2c9'>");
//
//        // Visible header for Molecule
//        outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");
//
//        // Table of Atom data
//        outfile.println("<table class=\"molecule\">");
//        outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>S2End</th><th>COODist</th><th>2DSASA</th></tr>");
//
//        // Iterate over the Atoms in this sortedAtomsTreeSet
//        sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA2C9();
//        Iterator<Atom> sortedAtomsTreeSetIterator2C9 = sortedAtomsTreeSet.iterator();
//        Atom currentAtom2C9;
//
//        while(sortedAtomsTreeSetIterator2C9.hasNext()){
//            currentAtom2C9 = sortedAtomsTreeSetIterator2C9.next();
//            this.writeAtomRowinMoleculeKUTable2C9(currentAtom2C9);
//        }
//        outfile.println("</table>");
//        outfile.println("</div>");
//        outfile.println("</td>");
//        outfile.println("</tr>");
//        outfile.println("</table>");
//        outfile.println("</div>");
//        //end of 2C9 output


//        //Write 3A4 output
//        String MolName3A4 = "molecule" + moleculeID + "standard";
//        outfile.println("<div id='" + MolName3A4 + "div' style='display:;'>");
//        outfile.println("<table>");
//        outfile.println("<tr>");
//        outfile.println("<td style='vertical-align:top;'>");
//
//        // Table row, contains 1 molecule images and mouseover to a second image with atom numbers
//        //output chemdoodle 3A4 molecule here
//        outfile.println("<script>");
//        outfile.println("var " + MolName3A4 + " = new ChemDoodle.ViewerCanvas('" + MolName3A4 + "', 400, " + ycanvassize + ");");
//        outfile.println(MolName3A4 + ".specs.atoms_useJMOLColors = true;");
//        outfile.print("var " + MolName3A4 + "MolFile = '");
//        //now use the multiple lines of the structure defined above in 2D6 output to enable explicit printout of \n
//        for(int i=0; i<Moleculelines.length; i++){
//            outfile.print(Moleculelines[i]);
//            outfile.print("\\n");
//        }
//        outfile.print("'; \n");
//        outfile.println("var " + MolName3A4 + "Mol = ChemDoodle.readMOL(" + MolName3A4 + "MolFile); ");
//        outfile.println("// get the dimension of the molecule");
//        outfile.println("var size = " + MolName3A4 + "Mol.getDimension();");
//        outfile.println("// find the scale by taking the minimum of the canvas/size ratios");
//        outfile.println("var scale = Math.min(" + MolName3A4 + ".width/size.x, " + MolName3A4 + ".height/size.y);");
//        outfile.println("// load the molecule first (this function automatically sets scale, so we need to change specs after");
//        outfile.println(MolName3A4 + ".loadMolecule(" + MolName3A4 + "Mol);");
//        outfile.println("// change the specs.scale value to the scale calculated, shrinking it slightly so that text is not cut off");
//        outfile.println(MolName3A4 + ".specs.scale = scale*.8;");
//        outfile.println(MolName3A4 + ".mouseover = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName3A4 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName3A4 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName3A4 + ".mouseout = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName3A4 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName3A4 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName3A4 + ".touchstart = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName3A4 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		" + MolName3A4 + "Mol.atoms[i].altLabel = i+1;");
//        outfile.println("	}");
//        outfile.println("this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName3A4 + ".touchend = function(){");
//        outfile.println("	for (var i = 0, ii=" + MolName3A4 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		delete " + MolName3A4 + "Mol.atoms[i].altLabel;");
//        outfile.println("	}");
//        outfile.println("	this.repaint();");
//        outfile.println("}");
//        outfile.println(MolName3A4 + ".drawChildExtras = function(ctx){");
//        outfile.println("	ctx.save();");
//        outfile.println("	ctx.translate(this.width/2, this.height/2);");
//        outfile.println("	ctx.rotate(this.specs.rotateAngle);");
//        outfile.println("	ctx.scale(this.specs.scale, this.specs.scale);");
//        outfile.println("	ctx.translate(-this.width/2, -this.height/2);");
//        outfile.println("	//draw atom numbers and draw circles on ranked atoms");
//        outfile.println("	ctx.lineWidth = 2 + 4/this.specs.scale;");
//        outfile.println("	radius = 6 + 6/this.specs.scale;");
//        outfile.println("	for (var i = 0, ii=" + MolName3A4 + "Mol.atoms.length; i<ii; i++) {");
//        outfile.println("		var atom = " + MolName3A4 + "Mol.atoms[i];");
//        //iterate through the atoms and add circles to top 3 rank
//        // Iterate over the Atoms in this molecule
//        for (int atomIndex=0; atomIndex < moleculeKU.getAtomCount(); atomIndex++) {
//            rankAtom = moleculeKU.getAtom(atomIndex);
//            String AtomNr = rankAtom.getID();
//            if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom).intValue() == 1){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(255,204,102)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom).intValue() == 2){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(223,189,174)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//            else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom) != null && MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(rankAtom).intValue() == 3){
//                outfile.println("		if((i + 1) == " + AtomNr + "){");
//                outfile.println("			ctx.strokeStyle = 'rgb(214,227,181)';");
//                outfile.println("			ctx.beginPath();");
//                outfile.println("			ctx.arc(atom.x, atom.y, radius, 0, Math.PI * 2, false);");
//                outfile.println("			ctx.stroke();");
//                outfile.println("		}");
//            }
//        }
//        outfile.println("	}");
//        outfile.println("	ctx.restore();");
//        outfile.println("}");
//        outfile.println(MolName3A4 + ".repaint();");
//        //end of chemdoodle 3A4 molecule
//        outfile.println("</script>");
//        outfile.println("</td>");
//        outfile.println("<td style='vertical-align:top;'>");
//        outfile.println("<ul id='navlist'>");
//        outfile.println("<li id='cyp3A4'><a href=\"javascript:Switch2D6and3A4('3A4')\">Standard</a></li>");
//        outfile.println("<li id='cyp2C9'><a href=\"javascript:Switch2D6and3A4('2C9')\" title=\"Click to show CYP2C predictions\">CYP2C</a></li>");
//        outfile.println("<li id='cyp2D6'><a href=\"javascript:Switch2D6and3A4('2D6')\" title=\"Click to show CYP2D6 predictions\">CYP2D6</a></li>");
//        outfile.println("</ul>");
//
//        outfile.println("<div class='table3a4'>");
//
//        // Visible header for Molecule
//        outfile.println("<span class=\"boldlarge\">" + title + "</span><br />");
//
//        // Table of Atom data
//        outfile.println("<table class=\"molecule\">");
//        outfile.println("<tr><th>Rank</th><th>Atom</th><th>Score</th><th>Energy</th><th>Accessibility</th><th>2DSASA</th></tr>");
//
//        // Iterate over the Atoms in this sortedAtomsTreeSet
//        sortedAtomsTreeSet = (TreeSet<Atom>) moleculeKU.getAtomsSortedByEnA();
//        Iterator<Atom> sortedAtomsTreeSetIterator = sortedAtomsTreeSet.iterator();
//        Atom currentAtom;
//
//        while(sortedAtomsTreeSetIterator.hasNext()){
//            currentAtom = sortedAtomsTreeSetIterator.next();
//            this.writeAtomRowinMoleculeKUTable(currentAtom);
//        }
//        outfile.println("</table>");
//        outfile.println("</div>");
//        outfile.println("</td>");
//        outfile.println("</tr>");
//        outfile.println("</table>");
//        outfile.println("</div>");
//        //end of 3A4 output
//        outfile.println("<hr />");
    }

    public void writeAtomRowinMoleculeKUTableHLM(IAtom atom){

        if((Boolean) atom.getProperty(Modeller.is_som_fld)) outfile.println("<tr class=\"highlight1\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
        else outfile.println("<tr>");

        outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>"); // For example C.22 or N.9
        double proba_yes = (Double) atom.getProperty(Modeller.proba_yes_fld);
        outfile.println("<td>" + Double.toString(proba_yes) + "</td>");

//        if(MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(atom)) + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
//        outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Span2End.get(atom).intValue() + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom).intValue() + "</td>");
//        outfile.println("<td>" +  twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
        outfile.println("</tr>");
    }

//    public void writeAtomRowinMoleculeKUTable(Atom atom){
//
//        if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
//        else outfile.println("<tr>");
//
//        outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(atom).intValue() + "</td>");
//        outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
//        if(MoleculeKU.SMARTCYP_PROPERTY.Score.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Score.get(atom)) + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
//        outfile.println("<td>" +  twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Accessibility.get(atom)) + "</td>");
//        outfile.println("<td>" +  twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
//        outfile.println("</tr>");
//    }

//    public void writeAtomRowinMoleculeKUTable2D6(Atom atom){
//
//        if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
//        else outfile.println("<tr>");
//
//        outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(atom).intValue() + "</td>");
//        outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
//        if(MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(atom)) + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
//        outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Span2End.get(atom).intValue() + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(atom).intValue() + "</td>");
//        outfile.println("<td>" +  twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
//        outfile.println("</tr>");
//    }

//    public void writeAtomRowinMoleculeKUTable2C9(Atom atom){
//
//        if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 1) outfile.println("<tr class=\"highlight1\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 2) outfile.println("<tr class=\"highlight2\">");
//        else if(MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 3) outfile.println("<tr class=\"highlight3\">");
//        else outfile.println("<tr>");
//
//        outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() + "</td>");
//        outfile.println("<td>" + atom.getSymbol() + "."+ atom.getID() + "</td>");			// For example C.22 or N.9
//        if(MoleculeKU.SMARTCYP_PROPERTY.Score2C9.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Score2C9.get(atom)) + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" + MoleculeKU.SMARTCYP_PROPERTY.Energy.get(atom) + "</td>");
//        outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Span2End.get(atom).intValue() + "</td>");
//        if(MoleculeKU.SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(atom) == null) outfile.println("<td>-</td>");
//        else outfile.println("<td>" +  MoleculeKU.SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(atom).intValue() + "</td>");
//        outfile.println("<td>" +  twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(atom)) + "</td>");
//        outfile.println("</tr>");
//    }


//    // Generates 2D coordinates of molecules
//    public MoleculeKU generate2Dcoordinates(MoleculeKU iAtomContainer){
//
//        //		boolean isConnected = ConnectivityChecker.isConnected(iAtomContainer);
//        //		System.out.println("isConnected " + isConnected);
//
//        final StructureDiagramGenerator structureDiagramGenerator = new StructureDiagramGenerator();
//
//        // Generate 2D coordinates?
//        if (GeometryTools.has2DCoordinates(iAtomContainer))
//        {
//            // System.out.println(iAtomContainer.toString() + " already had 2D coordinates");
//            return iAtomContainer; // already has 2D coordinates.
//        }
//        else
//        {
//            // Generate 2D structure diagram (for each connected component).
//            final AtomContainer iAtomContainer2d = new AtomContainer();
//
//            synchronized (structureDiagramGenerator)
//            {
//                structureDiagramGenerator.setMolecule(iAtomContainer, true);
//                structureDiagramGenerator.setUseTemplates(true);
//                try
//                {
//                    // Generate 2D coords for this molecule.
//                    structureDiagramGenerator.generateCoordinates();
//                    iAtomContainer = (MoleculeKU) structureDiagramGenerator.getMolecule();
//                }
//                catch (final Exception e)
//                {
//                    // Use projection instead.
//                    Projector.project2D(iAtomContainer);
//                    System.out.println("Exception in generating 2D coordinates");
//                    e.printStackTrace();
//                }
//            }
//
//            if(GeometryTools.has2DCoordinates(iAtomContainer)) return  iAtomContainer;
//            else {
//                System.out.println("Generating 2D coordinates for " + iAtomContainer2d + " failed.");
//                return null;
//            }
//        }
//    }
}
