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

/**
 * Enum to specify the priority level (common vs uncommon).
 * Each priority level corresponds to a weighting factor to weight the reaction rule.
 * Reaction rules for "common" reaction types receive a higher weight.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum PriorityLevel {
	
	// In GLORY, the values of COMMON and UNCOMMON were 5 and 1, respectively. 
	// They have now been normalized so that COMMON = 1 for the sake of meaningfulness of the priority scores (i.e. probability)
	COMMON (1),
	UNCOMMON (0.2); 
	
	private final double factor;
	
	PriorityLevel(double factor) {
		this.factor = factor;
	}
	
	public double getFactor() {
		return this.factor;
	}
	

}
