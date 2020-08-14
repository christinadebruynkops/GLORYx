/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
 
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

package main.java.depiction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.Errors;
import main.java.utils.Filenames;

/**
 * This class is used to write the HTML page to be displayed if an error occurred that prevented GLORYx from running properly.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class CreateErrorHTML {

	private Errors error;
	private String outputDir;
	private String outputFilename;
	PrintWriter outfile;
	PrintWriter cssfile;

	
	private static final String END_DIV = "</div>\n";

	private static final Logger logger = LoggerFactory.getLogger(CreateErrorHTML.class.getName());

	
	public CreateErrorHTML(Errors error, Filenames filenames) {
		this.error = error;
		outputDir = filenames.getUserOutputDir();
		outputFilename = filenames.getOutputHTMLFilename();
		
	}
	
	
	public void writeHTML() {

		if (outputFilename=="") {  // this should never occur
			outputFilename = "metabolite_prediction_results";
		}

		try {
			outfile = new PrintWriter(new BufferedWriter(new FileWriter(outputFilename + ".html")));
		} catch (IOException e) {
			logger.error("Could not create HTML outfile", e);
		}

		writeHead();
		
		writeBody();
		
		writeEndOfFile(); // closes body and html

		outfile.close();

	}
	
	
	private void writeHead() {

		outfile.println(
				//  --- for incorporation into webserver ---
				"{% load static %}" +
						
				" {% block header %}\n" + 
				"{% include \"default_header.html\" %}\n" + 
				"{% endblock %}\n"
				);
		
//		outfile.println("<!DOCTYPE html>\n" + 
//				"<html>\n" + 
//				"<head>\n" + 
//				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css\">\n"
//				);
//		
//		writeStyle();
//		
//		outfile.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/general.css\">");
//		outfile.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/accordion.css\">");
//		
//		outfile.println(
//				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" 
//				// TODO: may need to change this or can remove this part once this page is incorporated into web server
//				);
//		
//		outfile.println("</head>\n");
	}
	
	private void writeStyle() {
		
		File dir = new File(outputDir + "css/"); 
		dir.mkdir();
		
		try {
			cssfile = new PrintWriter(new BufferedWriter(new FileWriter(outputDir + "css/general.css")));
		} catch (IOException e) {
			logger.error("Could not create CSS file general.css", e);
		}

		cssfile.println("\n" +

				// define a custom button
				
//				".btn-outline-secondary.download-button {\n" + 
//				"	border-radius: 6px;\n" + 
//				"	padding-left: 8px;\n" + 
//				"	padding-right: 8px;\n" + 
//				"	border: 2px solid " + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_INACTIVE + ";\n" + 
//				"	background-color:" + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_INACTIVE + ";\n" + 
//				"	color:#3d3d3d;\n" + 
//				"}\n\n" +
//				
//				".btn-outline-secondary.download-button:hover {\n" +
//				"	border: 2px solid " + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
//				"	background-color:" + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
//				"	color:#3d3d3d;\n" + 
//				"}\n\n" +
//				
//				".btn-outline-secondary.download-button:active {\n" +
//				"	border: 2px solid " + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
//				"	background-color:" + CreateResultsHTML.ACCORDION_BACKGROUND_COLOR_ACTIVE_HOVER + ";\n" + 
//				"	color:#3d3d3d;\n" + 
//				"}\n\n" +
//				
//				".btn-outline-secondary.download-button:focus {\n" +
//				"	outline:none;\nbox-shadow: none;\n" + 
//				"}\n\n" +

				// define classes to use with <p> or <h> that add space afterwards
				".bottom-extra-space {\n" + 
				"   margin-bottom: 2em;\n" + 
				"}\n\n" +

				".bottom-slight-extra-space {\n" + 
				"   margin-bottom: 1em;\n" + 
				"}\n\n" +
				
				".top-extra-space {\n" + 
				"   margin-top: 2em;\n" + 
				"}\n\n" +
				
				".top-slight-extra-space {\n" + 
				"   margin-top: 1em;\n" + 
				"}\n\n" 
				);
		
		cssfile.close();

	}
	
	
	private void writeBody() {
		
		outfile.println(
				"<div class=\"container-fluid\" style = \"padding: 4em 2em; margin = 14em 2em; background-color: #FFFFFF; \" >\n" +
						"    	     <h5 class=\"bottom-slight-extra-space\" style=\"color:red;\";>\n" + 
						"				ERROR!\n" +
						"    	     </h5>\n"
						);
		
		outfile.println(
				" <p class=\"bottom-slight-extra-space top-slight-extra-space\" style = \"display:block\">\n" + 
				error.errorMessage() + " Please fix the problem and try again.\n" +
				" </p>\n"
				);
		
		
		outfile.println(
				

				" <p class=\"bottom-slight-extra-space\">\n" + 
				"  <a class=\"btn btn-outline-secondary btn-sm download-button\" role=\"button\" rel=\"nofollow\" onclick=\"alertFunction();\">\n" + 
				"   Delete input\n" + 
				"  </a>\n" + 
				" </p>\n" + 

				
				
				""
				);
		
	}
	
	
	private void writeEndOfFile() {

		outfile.println(
				END_DIV + // end large container containing header ERROR!
				
				// standard footer TODO:
				"{% block footer %}\n" + 
				"{% include \"gloryx/footer.html\" %}\n" + 
				"{% endblock %}" +
				
						"</body>\n" + 
						"</html>"
				);

	}
}
