package utils;

import org.openscience.cdk.MoleculeSet;
import smartcyp.WriteResultsAsChemDoodleHTML;

/**
 * Created by sicho on 1/24/17.
 */
public class DepictorSMARTCyp extends WriteResultsAsChemDoodleHTML {

    public DepictorSMARTCyp(String dateTime, String[] infileNames, String outputdir, String outputfile) {
        super(dateTime, infileNames, outputdir, outputfile);
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
        outfile.println(".table2c9 {background-color:rgb(255,235,235); border-radius: 5px; padding: 5px}");
        outfile.println(".table2d6 {background-color:rgb(228,235,240); border-radius: 5px; padding: 5px}");
        outfile.println(".table3a4 {background-color:rgb(242,238,234); border-radius: 5px; padding: 5px}");
        outfile.println("canvas.ChemDoodleWebComponent {border: none;}");
        outfile.println("-->");
        outfile.println("</style>");
        outfile.println("<script type=\"text/javascript\" src=\"http://www.farma.ku.dk/smartcyp/chemdoodle/ChemDoodleWeb-libs.js\"></script>");
        outfile.println("<script type=\"text/javascript\" src=\"http://www.farma.ku.dk/smartcyp/chemdoodle/ChemDoodleWeb.js\"></script>");
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
        outfile.println("	var cyp2c9list = [];");
        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
            int iplusone = moleculeIndex + 1;
            outfile.println("	cyp2c9list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2C9\";");
        }
        outfile.println("	var cyp2d6list = [];");
        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
            int iplusone = moleculeIndex + 1;
            outfile.println("	cyp2d6list[" + moleculeIndex + "] = \"molecule" + iplusone + "CYP2D6div\";");
        }
        outfile.println("	var cyp3a4list = [];");
        for (int moleculeIndex=0; moleculeIndex < moleculeSet.getMoleculeCount(); moleculeIndex++) {
            int iplusone = moleculeIndex + 1;
            outfile.println("	cyp3a4list[" + moleculeIndex + "] = \"molecule" + iplusone + "standarddiv\";");
        }
        outfile.println("	var maxmol = " + moleculeSet.getMoleculeCount() + ";");
        outfile.println("	if(cyp2show=='2C9'){");
        outfile.println("		for(var i=0; i<maxmol; i++) {");
        outfile.println("			HideContent(cyp3a4list[i]);");
        outfile.println("			HideContent(cyp2d6list[i]);");
        outfile.println("			ShowContent(cyp2c9list[i]);");
        outfile.println("		}");
        outfile.println("	}");
        outfile.println("	if(cyp2show=='2D6'){");
        outfile.println("		for(var i=0; i<maxmol; i++) {");
        outfile.println("			HideContent(cyp2c9list[i]);");
        outfile.println("			HideContent(cyp3a4list[i]);");
        outfile.println("			ShowContent(cyp2d6list[i]);");
        outfile.println("		}");
        outfile.println("	}");
        outfile.println("	if(cyp2show=='3A4'){");
        outfile.println("		for(var i=0; i<maxmol; i++) {");
        outfile.println("			HideContent(cyp2c9list[i]);");
        outfile.println("			HideContent(cyp2d6list[i]);");
        outfile.println("			ShowContent(cyp3a4list[i]);");
        outfile.println("		}");
        outfile.println("	}");
        outfile.println("}");
        outfile.println("</script>");
        outfile.println("</head>");
        outfile.println("");
    }
}
