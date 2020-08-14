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

package main.java.utils.analysis;

/**
 * Helper class to store the information needed to write a SMILES file of the predicted metabolites.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class ToWriteSmilesFile {

	private String parentName;
	private String metaboliteSmiles;
	
	public ToWriteSmilesFile(String parentName, String metaboliteSmiles) {
		this.parentName = parentName;
		this.metaboliteSmiles = metaboliteSmiles;
	}
	
	public String getParentName() {
		return this.parentName;
	}
	
	public String getMetaboliteSmiles() {
		return this.metaboliteSmiles;
	}
	
}
