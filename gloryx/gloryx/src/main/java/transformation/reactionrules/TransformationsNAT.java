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
 * This enum is the "NAT" subset of the phase II reaction rule set from SyGMa.
 * The reaction rules correspond to metabolic reactions mediated by N-acetyl transferases.
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsNAT implements Transformations {
	
	// WARNING!!!!!!!!!! THIS CONTENT IS ALSO CONTAINED in SyGMaTransformationsPhaseII.java. NEVER make changes to SMIRKS unless you make them in both files!!!


//	# -- N-acetylation --
//	[c:1][NH2:2]>>[c:1][N:2]C(=O)C	0.271	N-acetylation_(aniline)	#75/277
	N_ACETYLATION_ANILINE { // TODO make more specific so it doesn't match any other aromatic ring? unclear
		@Override
		public String getSMIRKS() {
			return "[c:1][NH2:2]([H])>>[c:1][N:2]C(=O)C";
		}
		@Override
		public String getName() {
			return "N-acetylation_(aniline)";
		}
		@Override
		public Double getLikelihood() {
			return 0.271;
		}
	},
	
//	[C;!$(C=[*;!#6]):1][NH2:2]>>[C:1][N:2]C(=O)C	0.149	N-acetylation_(aliphatic_NH2)	#37/248
	N_ACETYLATION_ALIPHATIC_NH2 {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C=[*;!#6;!#1]):1][NH2:2]([H])>>[C:1][N:2]C(=O)C";
		}
		@Override
		public String getName() {
			return "N-acetylation_(aliphatic_NH2)";
		}
		@Override
		public Double getLikelihood() {
			return 0.149;
		}
	},
	
//	[*;!#6:1][NH2:2]>>[*:1][N:2]C(=O)C	0.157	N-acetylation_(heteroatom_bonded_NH2)	#8/51
	N_ACETYLATION_HETEROATOM_BONDED_NH2 {
		@Override
		public String getSMIRKS() {
			return "[*;!#6;!#1:1][NH2:2]([H])>>[*:1][N:2]C(=O)C"; // modified so atom 1 doesn't match H
		}
		@Override
		public String getName() {
			return "N-acetylation_(heteroatom_bonded_NH2)";
		}
		@Override
		public Double getLikelihood() {
			return 0.157;
		}
	},
	
//	[CX4:1][NH1;R:2][CX4:3]>>[C:1][N:2]([C:3])C(=O)C	0.032	N-acetylation_(NH1)	#4/124
	N_ACETYLATION_NH1 {
		@Override
		public String getSMIRKS() {
			return "[CX4:1][NH1;R:2]([H])[CX4:3]>>[C:1][N:2]([C:3])C(=O)C";
		}
		@Override
		public String getName() {
			return "N-acetylation_(NH1)"; // in ring
		}
		@Override
		public Double getLikelihood() {
			return 0.032;
		}
	},
	
//	[CH3:1][NH1:2][#6:3]>>[CH3:1][N:2]([*:3])C(=O)C	0.020	N-acetylation_(NH1-CH3)	#3/149
	N_ACETYLATION_NH1CH3 {
		@Override
		public String getSMIRKS() {
			return "[CH3:1][NH1:2]([H])[#6:3]>>[CH3:1][N:2]([#6:3])C(=O)C";
		}
		@Override
		public String getName() {
			return "N-acetylation_(NH1-CH3)";
		}
		@Override
		public Double getLikelihood() {
			return 0.020;
		}
	},
	
//	# not predictive [nH1;$(n(c)c):1]>>[n:1]C(=O)C	0.003	N-acetylation_(aromatic_-nH-)	# 1/311
//
//	# -- O-acetylation --
//	# not predictive, only few occurences [*:1][OH1:2]>>[*:1][O:2]C(=O)C		OH_acetylation	# /
//
	
	
	
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
