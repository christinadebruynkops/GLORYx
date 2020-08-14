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
 * This enum is the phase II reaction rule set from SyGMa. 
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa. 
 * This class is marked as deprecated because all of the reaction rules have been copied into a separate 
 * enum corresponding to their enzyme family. Therefore all rules are duplicated and extreme care should be taken when changing a rule. 
 * 
 * @author Christina de Bruyn Kops
 *
 * @deprecated
 */
@Deprecated
public enum SyGMaTransformationsPhaseII implements Transformations {
	
	// WARNING!!!!!!!!!! THIS CONTENT IS ALSO CONTAINED in individual enzyme family enums! NEVER make changes to SMIRKS unless you make them in both files!!!
	
	// there are 27 rules, though there should only be 26 according to the 2008 SyGMa paper (paper says 144 rules total, 118 of which are phase 1)
	// maybe dephosphorylation shouldn't be a rule - it's not a conjugation reaction anyway

	
//	# *** PHASE 2 ***
//
//	# -- O-glucuronidation --
//	[C;!$(C1CCOCC1);!$(C1COCCC1);!$(C(O)=O):1][OH1:2]>>[C:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O	0.101	O-glucuronidation_(aliphatic_hydroxyl)	#208/2064
	O_GLUCURONIDATION_ALIPHATIC_HYDROXYL {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C1CCOCC1);!$(C1COCCC1);!$(C(O)=O):1][O:2]([H])>>[C:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "O-glucuronidation_(aliphatic_hydroxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.101;
		}
	},
	
//	[c:1][OH1:2]>>[c:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O	0.250	O-glucuronidation_(aromatic_hydroxyl)	#274/1097
	O_GLUCURONIDATION_AROMATIC_HYDROXYL {
		@Override
		public String getSMIRKS() {
			return "[c:1][O:2]([H])>>[c:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "O-glucuronidation_(aromatic_hydroxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.250;
		}
	},
	
//	[#7:1][OH1:2]>>[*:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O	0.213	O-glucuronidation_(N-hydroxyl)	#10/47
	O_GLUCURONIDATION_N_HYDROXYL {
		@Override
		public String getSMIRKS() {
			return "[#7:1][O:2]([H])>>[#7:1][O:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "O-glucuronidation_(N-hydroxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.213;
		}
	},
	
//	[C:1][C;!$(C(O)(=O)C1OCCCC1):2](=O)[OH1]>>[C:1][C:2](=O)OC1OC(C(O)=O)C(O)C(O)C1O	0.150	O-glucuronidation_(aliphatic_carboxyl)	#113/751
	O_GLUCURONIDATION_ALIPHATIC_CARBOXYL {
		@Override
		public String getSMIRKS() {
			return "[C:1][C;!$(C(O)(=O)C1OCCCC1):2](=O)[O]([H])>>[C:1][C:2](=O)OC1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "O-glucuronidation_(aliphatic_carboxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.150;
		}
	},
	
//	[c:1][C:2](=O)[OH1]>>[c:1][C:2](=O)OC1OC(C(O)=O)C(O)C(O)C1O	0.311	O-glucuronidation_(aromatic_carboxyl)	#57/183
	O_GLUCURONIDATION_AROMATIC_CARBOXYL {
		@Override
		public String getSMIRKS() {
			return "[c:1][C:2](=O)[O]([H])>>[c:1][C:2](=O)OC1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "O-glucuronidation_(aromatic_carboxyl)";
		}
		@Override
		public Double getLikelihood() {
			return 0.311;
		}
	},
	
//
//	# -- N-glucuronidation --
//	# not predictive [#6:1][#7:2]>>[#6:1][#7:2]C1OC(C(O)=O)C(O)C(O)C1O		N-gluc_general	# /
	
//	[c:1][NH2;X3:2]>>[c:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O	0.036	N-glucuronidation_(aniline)	#10/277
	N_GLUCURONIDATION_ANILINE { // TODO should this be specific to aniline? currently matches other aromatic rings too
		@Override
		public String getSMIRKS() {
			return "[c:1][NH2;X3:2]([H])>>[c:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(aniline)";
		}
		@Override
		public Double getLikelihood() {
			return 0.036;
		}
	},
	
//	[C:1][NH2;X3:2]>>[C:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O	0.013	N-glucuronidation_(aliphatic_NH2)	#6/476
	N_GLUCURONIDATION_ALIPHATIC_NH2 {
		@Override
		public String getSMIRKS() {
			return "[C:1][NH2;X3:2]([H])>>[C:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(aliphatic_NH2)";
		}
		@Override
		public Double getLikelihood() {
			return 0.013;
		}
	},
	
//	[c:1][NH1;X3:2]>>[c:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O	0.028	N-glucuronidation_(aniline_NH1-R)	#16/565
	N_GLUCURONIDATION_ANILINE_NH1R { // TODO should this be specific to aniline? currently matches other aromatic rings too
		@Override
		public String getSMIRKS() {
			return "[c:1][NH1;X3:2]([H])>>[c:1][N:2]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(aniline_NH1-R)";
		}
		@Override
		public Double getLikelihood() {
			return 0.028;
		}
	},
	
//	[N;X3;$(N([CH3])([CH3])[CH2]C):1]>>[N+:1]C1OC(C(O)=O)C(O)C(O)C1O	0.095	N-glucuronidation_(N(CH3)2)	#9/95
	N_GLUCURONIDATION_NCH32 {
		@Override
		public String getSMIRKS() {
			return "[N;X3;$(N([CH3])([CH3])[CH2]C):1]>>[N+:1]C1OC(C(O)=O)C(O)C(O)C1O"; // not modified
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(N(CH3)2)";
		}
		@Override
		public Double getLikelihood() {
			return 0.095;
		}
	},
	
//	[N;X3;R;$(N(C)(C)[CH3]):1]>>[N+:1]C1OC(C(O)=O)C(O)C(O)C1O	0.027	N-glucuronidation_(NCH3_in_a_ring)	#6/222
	N_GLUCURONIDATION_NCH3_RING {
		@Override
		public String getSMIRKS() {
			return "[N;X3;R;$(N(C)(C)[CH3]):1]>>[N+:1]C1OC(C(O)=O)C(O)C(O)C1O"; // not modified
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(NCH3_in_a_ring)";
		}
		@Override
		public Double getLikelihood() {
			return 0.027;
		}
	},
	
//	[NH1;X3;R;$(N(C)C):1]>>[N:1]C1OC(C(O)=O)C(O)C(O)C1O	0.019	N-glucuronidation_(NH_in_a_ring)	#7/359
	N_GLUCURONIDATION_NH_RING {
		@Override
		public String getSMIRKS() {
			return "[NH1;X3;R;$(N(C)C):1]([H])>>[N:1]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(NH_in_a_ring)";
		}
		@Override
		public Double getLikelihood() {
			return 0.019;
		}
	},
	
//	[n;X2:1]>>[n+:1]C1OC(C(O)=O)C(O)C(O)C1O	0.014	N-glucuronidation_(aromatic_=n-)	#22/1588
	N_GLUCURONIDATION_AROMATIC1 {
		@Override
		public String getSMIRKS() {
			return "[n;X2:1]>>[n+:1]C1OC(C(O)=O)C(O)C(O)C1O"; // not modified
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(aromatic_=n-)";
		}
		@Override
		public Double getLikelihood() {
			return 0.014;
		}
	},
	
//	[nH1;X3:1]>>[n:1]C1OC(C(O)=O)C(O)C(O)C1O	0.020	N-glucuronidation_(aromatic_-nH-)	#7/344
	N_GLUCURONIDATION_AROMATIC2 { // note: c1cNcc1 for testing
		@Override
		public String getSMIRKS() {
			return "[nH1;X3:1]([H])>>[n:1]C1OC(C(O)=O)C(O)C(O)C1O";
		}
		@Override
		public String getName() {
			return "N-glucuronidation_(aromatic_-nH-)";
		}
		@Override
		public Double getLikelihood() {
			return 0.020;
		}
	},
	
//
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
//
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
//	# -- methylation --
//	[c:1][OH1:2]>>[c:1][O:2]C	0.054	methylation_(aromatic_OH)	#59/1097
	METHYLATION_AROMATIC_OH {
		@Override
		public String getSMIRKS() {
			return "[c:1][O:2]([H])>>[c:1][O:2]C";
		}
		@Override
		public String getName() {
			return "methylation_(aromatic_OH)";
		}
		@Override
		public Double getLikelihood() {
			return 0.054;
		}
	},
	
	
//	[#6:1][SH1:2]>>[#6:1][S:2]C	0.375	methylation_(thiol)	#9/24
	METHYLATION_THIOL {
		@Override
		public String getSMIRKS() {
			return "[#6:1][SH1:2]([H])>>[#6:1][S:2]C";
		}
		@Override
		public String getName() {
			return "methylation_(thiol)";
		}
		@Override
		public Double getLikelihood() {
			return 0.375;
		}
	},
	
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
	
	public Phase getPhase() {
		return Phase.PHASE_2;
	}
	
	public PriorityLevel getPriorityLevel() {
		return PriorityLevel.COMMON;
	}
	
}
