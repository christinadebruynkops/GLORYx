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
 * This enum is the "UGT" subset of the phase II reaction rule set from SyGMa.
 * The reaction rules correspond to metabolic reactions mediated by UDP-glucuronosyltransferases.
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsUGT implements Transformations {
	
	// WARNING!!!!!!!!!! THIS CONTENT IS ALSO CONTAINED in SyGMaTransformationsPhaseII.java. NEVER make changes to SMIRKS unless you make them in both files!!!
	
	//NOTE the FAME 3 model is for glucuronidations & glycosylations
	
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
