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
 * This enum is the subset of the phase II reaction rule set from SyGMa that covers reaction types not corresponding to any of the five main phase II enzyme families.
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsOtherPhaseII implements Transformations {
	// These reactions are not covered by either GST, MT, NAT, SULT or UGT

//
//	# -- glycination --
//	[c:1][C:2](=O)[OH1]>>[c:1][C:2](=O)NCC(=O)O	0.087	glycination_(aromatic_carboxyl)	#16/183
	GLYCINATION_AROMATIC_CARBOXYL {
		@Override
		public String getSMIRKS() {
			return "[c:1][C:2](=O)[O]([H])>>[c:1][C:2](=O)NCC(=O)O";
		}
		@Override
		public String getName() {
			return "glycination_(aromatic_carboxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.087;
		}
	},
	
//	[C!$(CN):1][C:2](=O)[OH1]>>[C:1][C:2](=O)NCC(=O)O	0.036	glycination_(aliphatic_carboxyl)	#19/535
	GLYCINATION_ALIPHATIC_CARBOXYL {
		@Override
		public String getSMIRKS() {
			return "[C!$(CN):1][C:2](=O)[O]([H])>>[C:1][C:2](=O)NCC(=O)O";
		}
		@Override
		public String getName() {
			return "glycination_(aliphatic_carboxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.036;
		}
	},
	
//
//	# -- phosphorylation --
//	[OH1;$(O[CH2]C1AACO1),$(OP([OH1])(=O)OCC1AACO1),$(OP([OH1])(=O)OP(O)(=O)OCC1AACO1):1]>>[O:1]P(O)(O)=O	0.403	phosphorylation	#27/67
	PHOSPHORYLATION {
		@Override
		public String getSMIRKS() {
			return "[OH1;$(O[CH2]C1AACO1),$(OP([OH1])(=O)OCC1AACO1),$(OP([OH1])(=O)OP(O)(=O)OCC1AACO1):1]([H])>>[O:1]P(O)(O)=O";
		}
		@Override
		public Double getLikelihood() {
			return 0.403;
		}
	},
	
//	[#6,P:1][O:2][P:3]([O:4])([O:5])=[O:6]>>([*;#6,P:1][O:2].O[P:3]([O:4])([O:5])=[O:6])	0.203	dephosphorylation	#37/182
	DEPHOSPHORYLATION {
		@Override
		public String getSMIRKS() {
			return "[#6,P:1][O:2][P:3]([O:4])([O:5])=[O:6]>>([*;#6,P:1][O:2].O[P:3]([O:4])([O:5])=[O:6])"; // not modified
		}
		@Override
		public Double getLikelihood() {
			return 0.203;
		}
	},
	
	
	
	
	;
		
	public String getName() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public PriorityLevel getPriorityLevel() {
		return PriorityLevel.UNCOMMON;
	}
	
	public Phase getPhase() {
		return Phase.PHASE_2;
	}
	
}
