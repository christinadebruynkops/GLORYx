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

package main.java.utils;

import main.java.depiction.CreateErrorHTML;


/** 
 * Keeps track of errors along with the error message that is then written to the output html file.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum Errors {

	
	COULD_NOT_PROCESS_INPUT_MOL  ("The input molecule could not be processed. Please check that your input SMILES or structure represents a valid molecule."),
 
	CONTAINS_UNPERMITTED_ATOM_TYPE ("FAME 3 was not able to predict any sites of metabolism for this molecule because it contains at least one unpermitted "
			+ "atom. Only the following atoms are allowed: C, N, S, O, H, F, Cl, Br, I, P, B, and Si."),
    
	INPUT_MOLECULE_TOO_SMALL ("The input molecule must contain at least three heavy atoms."),
    
    MULTICOMPONENT_INPUT ("The input molecule has multiple components. Each input molecule should only have one component; salts are not allowed."),
    
    OTHER_FAME_ERROR ("FAME 3 was not able to predict any sites of metabolism for this molecule."),

    
    
    INPUT_FILE_FORMAT ("The input file must be an SD file with the ending \".sdf\"."),

    INPUT_SD_FILE_BROKEN ("The input SD file could not be read properly and may be corrupted."),

    INPUT_FILE_COULD_NOT_BE_READ ("The input file could not be read."),
    
    NO_VALID_INPUT ("No valid input could be found.") // used as catch-all in case inputSmiles for some reason is empty (should not occur though)
    
    ;
	
    private final String message;  

    Errors(String message) {
        this.message = message;
    }

    public String errorMessage() {
    		return this.message;
    }
    
	public static void createErrorHtmlAndExit(Filenames filenames, Errors error) {
		CreateErrorHTML errorHtmlWriter = new CreateErrorHTML(error, filenames);
		errorHtmlWriter.writeHTML();
		System.exit(2);
	}
	
}
