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

package main.java.transformation.reactionrules;

import main.java.transformation.Transformations;
import main.java.utils.Phase;
import main.java.transformation.PriorityLevel;

/**
 * This enum is the "SULT" subset of the phase II reaction rule set from SyGMa.
 * The reaction rules correspond to metabolic reactions mediated by sulfotransferases.
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsSULT implements Transformations{
	
	// WARNING!!!!!!!!!! THIS CONTENT IS ALSO CONTAINED in SyGMaTransformationsPhaseII.java. NEVER make changes to SMIRKS unless you make them in both files!!!

	
//	# -- sulfation --
//	[c:1][OH1:2]>>[c:1][O:2]S(=O)(=O)O	0.119	sulfation_(aromatic_hydroxyl)	#131/1097
	SULFATION_AROMATIC_HYDROXYL {
		@Override
		public String getSMIRKS() {
			return "[c:1][O:2]([H])>>[c:1][O:2]S(=O)(=O)O";
		}
		@Override
		public String getName() {
			return "sulfation_(aromatic_hydroxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.119;
		}
	},
	
//	[c:1][NH2:2]>>[c:1][N:2]S(=O)(=O)O	0.011	sulfation_(aniline)	#3/277
	SULFATION_ANILINE { // TODO make more specific so it doesn't match any other aromatic ring?
		@Override
		public String getSMIRKS() {
			return "[c:1][NH2:2]([H])>>[c:1][N:2]S(=O)(=O)O";
		}
		@Override
		public String getName() {
			return "sulfation_(aniline)";
		}
		@Override
		public Double getLikelihood() {
			return 0.011;
		}
	},
	
//	[C;!$(C=O);!$(CC[OH1]):1][OH1:2]>>[C:1][O:2]S(=O)(=O)O	0.018	sulfation_(aliphatic_hydroxyl)	#33/1796
	SULFATION_ALIPHATIC_HYDROXYL {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C=O);!$(CC[OH1]):1][O:2]([H])>>[C:1][O:2]S(=O)(=O)O";
		}
		@Override
		public String getName() {
			return "sulfation_(aliphatic_hydroxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.018;
		}
	},
	
//	# not predictive [c:1][NH1:2][#6:3]>>[c:1][N:2]([#6:3])S(=O)(=O)O		aromatic_N_sulfation2	# /
//	# not predictive [C:1][NH2:2]>>[C:1][N:2]S(=O)(=O)O		aliphatic_N_sulfation1	# /
//	# not predictive[C:1][NH1;R:2][C:3]>>[C:1][N:2]([C:3])S(=O)(=O)O		aliphatic_N_sulfation2	# /
	
	;
	
	public String getName() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public PriorityLevel getPriorityLevel() {
		return PriorityLevel.COMMON;
	}
	
	public Phase getPhase() {
		return Phase.PHASE_2;
	}

}
