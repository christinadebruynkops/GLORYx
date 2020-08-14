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

package main.java.transformation;

import main.java.transformation.PriorityLevel;
import main.java.utils.Phase;

/**
 * Template for all of the reaction rule enums.
 * <p>
 * The getLikelihood() method is used for evaluation purposes to get the reaction likelihood from SyGMa. 
 * This information has only been included for the phase II reaction rules from SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public interface Transformations {

	public abstract String getSMIRKS();
	
	public String getName();
	
	public abstract PriorityLevel getPriorityLevel();
	
	public abstract Double getLikelihood();
	
	public abstract Phase getPhase();
	
}
