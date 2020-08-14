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
 * This enum is the phase I reaction rule set from SyGMa. 
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum SyGMaTransformationsPhaseI implements Transformations {

	
	
//	# *** PHASE 1 ***
//
//	# -- N-dealkylation --
//	#
//	[*;!c:1][NH1;X3:2][CH3]>>[*:1][N:2]	0.546	N-demethylation_(R-NHCH3)	#77/141
	N_DEMETHYLATION_RNHCH3 {
		@Override
		public String getSMIRKS() {
			return "[*;!c:1][NH1;X3:2][CH3]>>[*:1][N:2]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(R-NHCH3)";
		}
	},
	
//	[c:1][NH1;X3:2][CH3]>>[c:1][N:2]	0.857	N-demethylation_(c-NHCH3)	#12/14
	N_DEMETHYLATION_CNHCH3 {
		@Override
		public String getSMIRKS() {
			return "[c:1][NH1;X3:2][CH3]>>[c:1][N:2]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(c-NHCH3)";
		}
	},
	
//	[*;!c:1][NH0;X3:2]([CH3])[CH3:3]>>[*:1][N:2][CH3:3]	0.587	N-demethylation_(R-N(CH3)2)	#101/172
	N_DEMETHYLATION_RNCH32 {
		@Override
		public String getSMIRKS() {
			return "[*;!c:1][NH0;X3:2]([CH3])[CH3:3]>>[*:1][N:2][CH3:3]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(R-N(CH3)2)";
		}
	},			
	
//	[c:1][NH0;X3:2]([CH3])[CH3:3]>>[c:1][N:2][CH3:3]	0.684	N-demethylation_(c-N(CH3)2)	#13/19
	N_DEMETHYLATION_CNCH32 {
		@Override
		public String getSMIRKS() {
			return "[c:1][NH0;X3:2]([CH3])[CH3:3]>>[c:1][N:2][CH3:3]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(c-N(CH3)2)";
		}
	},	
	
//	[*;!$([CH3]):1][NH0;X3:2]([CH3])[#6;!$([CH3]):3]>>[*:1][N:2][*:3]	0.418	N-demethylation_(R-N(CR)CH3)	#142/340
	N_DEMETHYLATION_CNCRCH3 {
		@Override
		public String getSMIRKS() {
			return "[*;!$([CH3]):1][NH0;X3:2]([CH3])[#6;!$([CH3]):3]>>[*:1][N:2][#6:3]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(R-N(CR)CH3)";
		}
	},	
	
//	[n:1][CH3]>>[nH1:1]	0.253	N-demethylation_(nCH3)	#39/154
	N_DEMETHYLATION_NCH3 {
		@Override
		public String getSMIRKS() {
			return "[n:1][CH3]>>[nH1:1]";
		}
		@Override
		public String getName() {
			return "N-demethylation_(nCH3)";
		}
	},	
	
//	#
//	[N;X3:2][CH1]([CH3])[CH3]>>[N:2]	0.371	N-depropylation	#23/62
	N_DEPROPYLATION {
		@Override
		public String getSMIRKS() {
			return "[N;X3:2][CH1]([CH3])[CH3]>>[N:2]";
		}
		@Override
		public String getName() {
			return "N-depropylation";
		}
	},	
	
//	# split ? [NH1;X3:2][CH1]([CH3])[CH3]>>[N:2]	0.114504	secondary_N-depropylation	# 15/131
//	# split ? [NH0;X3:2][CH1]([CH3])[CH3]>>[N:2]	0.521739	tertiary_N-depropylation	# 12/23
//	#
//	# no examples: [NX3:2][C:3]1[O:4][C:5][C:6][C:7]1>>([N:2].O[C:3]1[O:4][C:5][C:6][C:7]1)	0.171875	N-deglycosidation	# 11/64
//	[n:2][C:3]1[O:4][C:5][C:6][C:7]1>>([nH1:2].O[C:3]1[O:4][C:5][C:6][C:7]1)	0.113	n-deglycosidation	#17/150
	N_DEGLYCOSIDATION { // exmaple smiles for testing: c1N(C2OCCC2)ccc1
		@Override
		public String getSMIRKS() {
			return "[n:2][C:3]1[O:4][C:5][C:6][C:7]1>>([nH1:2].O[C:3]1[O:4][C:5][C:6][C:7]1)";
		}
		@Override
		public String getName() {
			return "n-deglycosidation";
		}
	},	
	
//	#
//	[NX3:2][CX3;H1]=O>>[N:2]	0.444	N-deformylation	#12/27
	N_DEFORMYLATION {
		@Override
		public String getSMIRKS() {
			return "[NX3:2][CX3;H1]=O>>[N:2]";
		}
		@Override
		public String getName() {
			return "N-deformylation";
		}
	},	
	
//	#
//	[*;!C,!X4:1][N;X3:2]1[C:3][C:4][N;X3:5][CH2][CH2]1>>[*:1][N:2][C:3][C:4][N:5]	0.033	N-dealkylation_(piperazine)	#5/151
	N_DEALKYLATION_PIPERAZINE {
		@Override
		public String getSMIRKS() {
			return "[*;!C,!X4:1][N;X3:2]1[C:3][C:4][N;X3:5][CH2][CH2]1>>[*:1][N:2][C:3][C:4][N:5]";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(piperazine)";
		}
	},	
	
//	[N;X3:2]1[C:3][C:4][O:5][CH2][CH2]1>>[N:2][C:3][C:4][O:5]	0.100	N-dealkylation_(morpholine)	#5/50
	N_DEALKYLATION_MORPHOLINE {
		@Override
		public String getSMIRKS() {
			return "[N;X3:2]1[C:3][C:4][O:5][CH2][CH2]1>>[N:2][C:3][C:4][O:5]";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(morpholine)";
		}
	},	
	
//	#
//	[*;!c:1][NH1;X3:2]!@[CH2:3][#6:4]>>([*:1][N:2].O[C:3][*:4])	0.079	N-dealkylation_(R-NHCH2-alkyl)	#90/1138
	N_DEALKYLATION_RNHCH2_ALKYL {
		@Override
		public String getSMIRKS() {
			return "[*;!c:1][NH1;X3:2]!@[CH2:3][#6:4]>>([*:1][N:2].O[C:3][#6:4])";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(R-NHCH2-alkyl)";
		}
	},	
	
//	[c:1][NH1;X3:2]!@[CH2:3][#6:4]>>([c:1][N:2].O[C:3][*:4])	0.134	N-dealkylation_(c-NHCH2-alkyl)	#15/112
	N_DEALKYLATION_CNHCH2_ALKYL {
		@Override
		public String getSMIRKS() {
			return "[c:1][NH1;X3:2]!@[CH2:3][#6:4]>>([c:1][N:2].O[C:3][#6:4])";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(c-NHCH2-alkyl)";
		}
	},	
	
//	[NH0;X3:2]!@[C;X4;H2:4]>>([N:2].O[C:4])	0.119	N-dealkylation_(tertiaryN-CH2-alkyl)	#246/2074
	N_DEALKYLATION_TERTIARY_CH2_ALKYL {
		@Override
		public String getSMIRKS() {
			return "[NH0;X3:2]!@[C;X4;H2:4]>>([N:2].O[C:4])";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(tertiaryN-CH2-alkyl)";
		}
	},	
	
//	[#6:1][N+;X4:2]([#6:3])([CH3:4])!@[#6;H1,H2:5]>>([*:1][N:2]([*:3])[C:4].O[*:5])	0.106	N-dealkylation_(quarternary_N)	#5/47
	N_DEALKYLATION_QUARTERNARY_N {
		@Override
		public String getSMIRKS() {
			return "[#6:1][N+;X4:2]([#6:3])([CH3:4])!@[#6;H1,H2:5]>>([#6:1][N+:2]([H])([#6:3])[C:4].O[#6:5])"; // changed to keep + charge and add H
		}
		@Override
		public String getName() {
			return "N-dealkylation_(quarternary_N)";
		}
	},	
	
//	[n:1][CH2:2]>>([nH:1].O[C:2])	0.042	N-dealkylation_(nCH2)	#19/452
	N_DEALKYLATION_NCH2 {
		@Override
		public String getSMIRKS() {
			return "[n:1][CH2:2]>>([nH:1].O[C:2])";
		}
		@Override
		public String getName() {
			return "N-dealkylation_(nCH2)";
		}
	},	
	
//	#
//	# low occurence [NH0;X3:2]!@[C;X4;H1:4][c:5]>>[N:2].O[C:4][c:5]		tertiary_N-dealkylation2
//	# not predictive [#6:1][N:2]([#6:3])[c:4]>>[#6:1][N:2][#6:3].O[c:4]		tertiary_N-dealkylation
//	# not predictive [#6:1]!@[N;R;X3:2]([CH2:3])[CH2:4]>>[#6:1][N:2].O[C:3].O[C:4]
//
//	# -- O-dealkylation --
//	#
//	[#6;!$(C=O):1][O:2][CH3]>>[*:1][O:2]	0.277	O-demethylation	#224/808
	O_DEMETHYLATION {
		@Override
		public String getSMIRKS() {
			return "[#6;!$(C=O):1][O:2][CH3]>>[#6:1][O:2]"; 
		}
		@Override
		public String getName() {
			return "O-demethylation";
		}
	},	
	
//	[*;!#6;!$(*=O):1][O:2][CH3]>>[*:1][O:2]	0.200	het-O-demethylation	#6/30
	HET_O_DEMETHYLATION {
		@Override
		public String getSMIRKS() {
			return "[*;!#6;!#1;!$(*=O):1][O:2][CH3]>>[*:1][O:2]";
		}
		@Override
		public String getName() {
			return "het-O-demethylation";
		}
	},	
	
//	#
//	[C;!$(C(O)~[!#6]);!$([CH3]):1][O;!$(O1CC1):2][C;X4;!$(C(O)~[!#6]);H1,H2:3]>>([C:1][O:2].O[C:3])	0.087	O-dealkylation_(aliphatic)	#28/320
	O_DEALKYLATION_ALIPHATIC {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C(O)~[!#6;!#1]);!$([CH3]):1][O;!$(O1CC1):2][C;X4;!$(C(O)~[!#6;!#1]);H1,H2:3]>>([C:1][O:2].O[C:3])";
		}
		@Override
		public String getName() {
			return "O-dealkylation_(aliphatic)";
		}
	},	
	
//	[c:1][O:2][C;X4;!$(C(O)~[!#6]);H1,H2:3]>>([c:1][O:2].O[C:3])	0.087	O-dealkylation_(aromatic)	#67/767
	O_DEALKYLATION_AROMATIC {
		@Override
		public String getSMIRKS() {
			return "[c:1][O:2][C;X4;!$(C(O)~[!#6;!#1]);H1,H2:3]>>([c:1][O:2].O[C:3])";
		}
		@Override
		public String getName() {
			return "O-dealkylation_(aromatic)";
		}
	},	
	
//	[#6;!$([CH3]);!$(C=O):1][O:2][C:3]1[O:4][C:5][C:6][C:7][C:8]1>>([*:1][O:2].O[C:3]1[O:4][C:5][C:6][C:7][C:8]1)	0.170	O-deglycosidation	#82/482
	O_DEGLYCOSIDATION {
		@Override
		public String getSMIRKS() {
			return "[#6;!$([CH3]);!$(C=O):1][O:2][C:3]1[O:4][C:5][C:6][C:7][C:8]1>>([#6:1][O:2].O[C:3]1[O:4][C:5][C:6][C:7][C:8]1)"; 
		}
		@Override
		public String getName() {
			return "O-deglycosidation";
		}
	},	
	
//	[O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8][CH2]1>>[O:1][c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8]	0.359	O-dealkylation_(methylenedioxyphenyl)a	#14/39
	O_DEALKYLATION_METHYLENEDIOXYPHENYL_1 {
		@Override
		public String getSMIRKS() {
			return "[O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8][CH2]1>>[O:1][c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8]";
		}
		@Override
		public String getName() {
			return "O-dealkylation_(methylenedioxyphenyl)a";
		}
	},	
	
//	[O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8][CH2:9]1>>([O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8].[CH2:9]1)	0.103	O-dealkylation_(methylenedioxyphenyl)b	#8/78
	O_DEALKYLATION_METHYLENEDIOXYPHENYL_2 {
		@Override
		public String getSMIRKS() {
			return "[O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8][CH2:9]1>>([O:1]1[c:2]2[c:3][c:4][c:5][c:6][c:7]2[O:8].[CH2:9]1)";
		}
		@Override
		public String getName() {
			return "O-dealkylation_(methylenedioxyphenyl)b";
		}
	},	
	
//
//	# -- S-dealkylation --
//	[c:1][S:2][CH2:3]>>([c:1][S:2].O[C:3])	0.050	S-dealkylation_c-SCH2-R	#6/119
	S_DEALKYLATION_CSCH2R {
		@Override
		public String getSMIRKS() {
			return "[c:1][S:2][CH2:3]>>([c:1][S:2]([H]).O[C:3])";
		}
		@Override
		public String getName() {
			return "S-dealkylation_c-SCH2-R";
		}
	},	
	
//
//	# -- aromatic hydroxylation --
//	# [cH1:1]>>[c:1]O	1	aromatic_hydroxylation_(general)
//	[#6:1]~[a:2]1[a:3][a:4][cH1:5][a:6][a:7]1>>[*:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1	0.061	aromatic_hydroxylation_(para_to_carbon)	#187/3041
	AROMATIC_HYDROXYLATION_PARA_TO_CARBON {
		@Override
		public String getSMIRKS() {
			return "[#6:1]~[a:2]1[a:3][a:4][cH1:5]([H])[a:6][a:7]1>>[#6:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(para_to_carbon)";
		}
	},	
	
//	[#7:1]~[a:2]1[a:3][a:4][cH1:5][a:6][a:7]1>>[*:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1	0.145	aromatic_hydroxylation_(para_to_nitrogen)	#152/1045
	AROMATIC_HYDROXYLATION_PARA_TO_NITROGEN {
		@Override
		public String getSMIRKS() {
			return "[#7:1]~[a:2]1[a:3][a:4][cH1:5]([H])[a:6][a:7]1>>[#7:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(para_to_nitrogen)";
		}
	},	
	
//	[#8:1]~[a:2]1[a:3][a:4][cH1:5][a:6][a:7]1>>[*:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1	0.056	aromatic_hydroxylation_(para_to_oxygen)	#56/1005
	AROMATIC_HYDROXYLATION_PARA_TO_OXYGEN {
		@Override
		public String getSMIRKS() {
			return "[#8:1]~[a:2]1[a:3][a:4][cH1:5]([H])[a:6][a:7]1>>[#8:1]~[a:2]1[a:3][a:4][c:5](O)[a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(para_to_oxygen)";
		}
	},	
	
//	[#6:1]~[a:2]1[a;!$(a(a)(a)[#6,#7,#8]):3][cH1:4][a;!$(a(a)(a)[#6,#7,#8]):5][a:6][a;!$(a(a)(a)[#6,#7,#8]):7]1>>[*:1]~[a:2]1[a:3][c:4](O)[a:5][a:6][a:7]1	0.016	aromatic_hydroxylation_(meta_to_carbon)	#24/1522
	AROMATIC_HYDROXYLATION_META_TO_CARBON {
		@Override
		public String getSMIRKS() {
			return "[#6:1]~[a:2]1[a;!$(a(a)(a)[#6,#7,#8]):3][cH1:4]([H])[a;!$(a(a)(a)[#6,#7,#8]):5][a:6][a;!$(a(a)(a)[#6,#7,#8]):7]1>>[#6:1]~[a:2]1[a:3][c:4](O)[a:5][a:6][a:7]1"; // modified
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(meta_to_carbon)";
		}
	},	
	
//	[#7:1]~[a:2]1[cH1:3][a;!$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[*:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1	0.030	aromatic_hydroxylation_(ortho_to_nitrogen)	#37/1244
	AROMATIC_HYDROXYLATION_ORTHO_TO_NITROGEN {
		@Override
		public String getSMIRKS() {
			return "[#7:1]~[a:2]1[cH1:3]([H])[a;!$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[#7:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(ortho_to_nitrogen)";
		}
	},	
	
//	[#8:1]~[a:2]1[cH1:3][a;!$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[*:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1	0.032	aromatic_hydroxylation_(ortho_to_oxygen)	#32/987
	AROMATIC_HYDROXYLATION_ORTHO_TO_OXYGEN {
		@Override
		public String getSMIRKS() {
			return "[#8:1]~[a:2]1[cH1:3]([H])[a;!$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[#8:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(ortho_to_oxygen)";
		}
	},	
	
//	[#6,#7,#8:1]~[a:2]1[cH1:3][a;$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[*:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1	0.013	aromatic_hydroxylation_(ortho_to_2_substituents)	#15/1158
	AROMATIC_HYDROXYLATION_ORTHO_TO_2_SUBSTITUENTS {
		@Override
		public String getSMIRKS() {
			return "[#6,#7,#8:1]~[a:2]1[cH1:3]([H])[a;$(a(a)(a)[#6,#7,#8]):4][a:5][a;!$(a(a)(a)[#6,#7,#8]):6][a:7]1>>[*:1]~[a:2]1[c:3](O)[a:4][a:5][a:6][a:7]1";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(ortho_to_2_substituents)";
		}
	},	
	
//	[cH1;$(c1saaa1):2]>>[c:2]O	0.059	aromatic_hydroxylation_(sulfur_containing_5ring)	#3/51
	AROMATIC_HYDROXYLATION_SULFUR_CONTAINING_5RING {
		@Override
		public String getSMIRKS() {
			return "[cH1;$(c1saaa1):2]([H])>>[c:2]O";
		}
		@Override
		public String getName() {
			return "aromatic_hydroxylation_(sulfur_containing_5ring)";
		}
	},	
	
//	[nH0:1][cH1;$(c1naan1):2]>>[nH1:1]-[cH0:2]=O	0.109	aromatic_oxidation_(nitrogen_containing_5ring)	#21/193
	AROMATIC_OXIDATION_NITROGEN_CONTAINING_5RING {
		@Override
		public String getSMIRKS() {
			return "[nH0:1][cH1;$(c1naan1):2]([H])>>[nH1:1]-[cH0:2]=O";
		}
		@Override
		public String getName() {
			return "aromatic_oxidation_(nitrogen_containing_5ring)";
		}
	},	
	
//	[c;$(cc[OH1]):1][OH1]>>[c:1]	0.038	aromatic_dehydroxylation	#8/213
	AROMATIC_DEHYDROXYLATION { // when two neighboring aromatic carbons are hydroxylated
		@Override
		public String getSMIRKS() {
			return "[c;$(cc[OH1]):1][OH1]>>[c:1]";
		}
		@Override
		public String getName() {
			return "aromatic_dehydroxylation";
		}
	},	
	
//
//	# -- carboxylation --
//	# carboxylation_is_combination_of_"aliphatic_primary_carbon_hydroxylation"_and_"alcohol_to_acid_oxidation"_->_queries_identical_to_primary_carbon_hydroxylation
//	[C;X4;H0;$(C[!C]):1][CH3:2]>>[C:1][C:2](=O)O	0.013	carboxylation_(primary_carbon_next_to_quart_carbon)	#5/395
	CARBOXYLATION_1 {
		@Override
		public String getSMIRKS() {
			return "[C;X4;H0;$(C[!C]):1][CH3:2]([H])([H])([H])>>[C:1][C:2](=O)O";
		}
		@Override
		public String getName() {
			return "carboxylation_(primary_carbon_next_to_quart_carbon)";
		}
	},
	
//	[CH1;$(C(-[#6])(-[#6])-[CH3]):1][CH3:2]>>[C:1][C:2](=O)O	0.014	carboxylation_(primary_carbon_next_to_tert_carbon)	#9/624
	CARBOXYLATION_2 {
		@Override
		public String getSMIRKS() {
			return "[CH1;$(C(-[#6])(-[#6])-[CH3]):1][CH3:2]([H])([H])([H])>>[C:1][C:2](=O)O"; 
		}
		@Override
		public String getName() {
			return "carboxylation_(primary_carbon_next_to_tert_carbon)";
		}
	},
	
//	[#6:1][CH2:2][CH3:3]>>[*:1][C:2][C:3](=O)O	0.030	carboxylation_(primary_carbon_next_to_sec_carbon)	#18/601
	CARBOXYLATION_3 {
		@Override
		public String getSMIRKS() {
			return "[#6:1][CH2:2][CH3:3]([H])([H])([H])>>[#6:1][C:2][C:3](=O)O";
		}
		@Override
		public String getName() {
			return "carboxylation_(primary_carbon_next_to_sec_carbon)";
		}
	},
	
//	[C;$(C=*),$(C#*):1][CH3:2]>>[C:1][C:2](=O)O	0.016	carboxylation_(primary_carbon_next_to_SP2)	#9/550
	CARBOXYLATION_4 {
		@Override
		public String getSMIRKS() {
			return "[C;$(C=*),$(C#*):1][CH3:2]([H])([H])([H])>>[C:1][C:2](=O)O";
		}
		@Override
		public String getName() {
			return "carboxylation_(primary_carbon_next_to_SP2)";
		}
	},
	
//	[c:1][CH3:2]>>[c:1][C:2](=O)O	0.051	carboxylation_(benzylic_CH3)	#28/544
	CARBOXYLATION_5 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH3:2]([H])([H])([H])>>[c:1][C:2](=O)O";
		}
		@Override
		public String getName() {
			return "carboxylation_(benzylic_CH3)";
		}
	},
	
//
//	# -- aliphatic hydroxylation --
//	# general [C;X4;!H0;!$(Cc):1]>>[C:1]O		all_aliph_hydr	# /
//	[C;X4;H0;$(C[!C]):1][CH3:2]>>[C:1][C:2]O	0.061	aliphatic_hydroxylation_(primary_carbon_next_to_quart_carbon)	#24/395
	ALIPHATIC_HYDROXYLATION_1 {
		@Override
		public String getSMIRKS() {
			return "[C;X4;H0;$(C[!C]):1][CH3:2]([H])>>[C:1][C:2]O";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(primary_carbon_next_to_quart_carbon)";
		}
	},
	
//	[CH1;$(C(-[#6])(-[#6])-[CH3]):1][CH3:2]>>[C:1][C:2]O	0.030	aliphatic_hydroxylation_(primary_carbon_next_to_tert_carbon)	#19/624
	ALIPHATIC_HYDROXYLATION_2 {
		@Override
		public String getSMIRKS() {
			return "[CH1;$(C(-[#6])(-[#6])-[CH3]):1][CH3:2]([H])>>[C:1][C:2]O";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(primary_carbon_next_to_tert_carbon)";
		}
	},
	
//	[#6:1][CH2:2][CH3:3]>>[*:1][C:2][C:3]O	0.063	aliphatic_hydroxylation_(primary_carbon_next_to_sec_carbon)	#38/601
	ALIPHATIC_HYDROXYLATION_3 {
		@Override
		public String getSMIRKS() {
			return "[#6:1][CH2:2][CH3:3]([H])>>[#6:1][C:2][C:3]O";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(primary_carbon_next_to_sec_carbon)";
		}
	},
	
//	[C;$(C=*),$(C#*):1][CH3:2]>>[C:1][C:2]O	0.049	aliphatic_hydroxylation_(primary_carbon_next_to_SP2_or_SP1)	#27/550
	ALIPHATIC_HYDROXYLATION_4 {
		@Override
		public String getSMIRKS() {
			return "[C;$(C=*),$(C#*):1][CH3:2]([H])>>[C:1][C:2]O";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(primary_carbon_next_to_SP2_or_SP1)";
		}
	},
	
//	# fairly unspecific [CX4:1][CH2:2][CX4:3]>>[C:1][C:2](O)[C:3]		secondary_aliphatic_carbon_hydroxylation	# /
//	[CX4:1][CH2:2][CH3]>>[C:1][C:2](O)C	0.106	aliphatic_hydroxylation_(sec_carbon,next_to_CH3)	#53/500
	ALIPHATIC_HYDROXYLATION_5 {
		@Override
		public String getSMIRKS() {
			return "[CX4:1][CH2:2]([H])[CH3]>>[C:1][C:2](O)C";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon,next_to_CH3)";
		}
	},
	
//	[CX4;H2:1][CH2;R:2][CX4;H2:3]>>[C:1][C:2](O)[C:3]	0.106	aliphatic_hydroxylation_(sec_carbon_in_a_ringA)	#68/642
	ALIPHATIC_HYDROXYLATION_6 {
		@Override
		public String getSMIRKS() {
			return "[CX4;H2:1][CH2;R:2]([H])[CX4;H2:3]>>[C:1][C:2](O)[C:3]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon_in_a_ringA)";
		}
	},
	
//	[CX4;H2:1][CH2;R:2][CX4;!H2:3][*;$([CH3]),!#6:4]>>[C:1][C:2](O)[C:3][*:4]	0.027	aliphatic_hydroxylation_(sec_carbon_in_a_ringB)	#36/1314
	ALIPHATIC_HYDROXYLATION_7 {
		@Override
		public String getSMIRKS() {
			return "[CX4;H2:1][CH2;R:2]([H])[CX4;!H2:3][*;!#1;$([CH3]),!#6:4]>>[C:1][C:2](O)[C:3][*:4]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon_in_a_ringB)";
		}
	},
	
//	[CX4:1][CH2;!R:2][*;!c;$(*=*):3]>>[C:1][C:2](O)[*:3]	0.012	aliphatic_hydroxylation_(sec_carbon_next_to_SP2,not_in_a_ring)	#8/668
	ALIPHATIC_HYDROXYLATION_8 {
		@Override
		public String getSMIRKS() {
			return "[CX4:1][CH2;!R:2]([H])[*;!c;$(*=*):3]>>[C:1][C:2](O)[*:3]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon_next_to_SP2,not_in_a_ring)";
		}
	},
	
//	[CX4:1][CH2;R:2][*;!c;$(*=*),$([#7]):3]>>[C:1][C:2](O)[*:3]	0.046	aliphatic_hydroxylation_(sec_carbon_next_to_SP2,in_a_ring)	#97/2110
	ALIPHATIC_HYDROXYLATION_9 {
		@Override
		public String getSMIRKS() {
			return "[CX4:1][CH2;R:2]([H])[*;!c;$(*=*),$([#7]):3]>>[C:1][C:2](O)[*:3]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon_next_to_SP2,in_a_ring)";
		}
	},
	
//	[*;!c;$(*=*):1][CH2;R:2][*;!c;$(*=*):3]>>[*:1][C:2](O)[*:3]	0.421	aliphatic_hydroxylation_(sec_carbon_both_sides_next_to_SP2,in_a_ring)	#16/38
	ALIPHATIC_HYDROXYLATION_10 {
		@Override
		public String getSMIRKS() {
			return "[*;!c;$(*=*):1][CH2;R:2]([H])[*;!c;$(*=*):3]>>[*:1][C:2](O)[*:3]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(sec_carbon_both_sides_next_to_SP2,in_a_ring)";
		}
	},
	
//	[C:1][CH1;X4:2]([C;!$([CH3]):3])[N,C&$([C]=*):4]>>[C:1][C:2](O)([C:3])[*:4]	0.013	aliphatic_hydroxylation_(tert_carbon_next_to_SP2)	#22/1707
	ALIPHATIC_HYDROXYLATION_11 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH1;X4:2]([C;!$([CH3]):3])([H])[N,C&$([C]=*):4]>>[C:1][C:2](O)([C:3])[*:4]";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(tert_carbon_next_to_SP2)";
		}
	},
	
//	[CH3][CH1;X4;!$(Cc):1][CH3]>>C[C:1](O)C	0.097	aliphatic_hydroxylation_(tert_carbon_linked_to_two_CH3_groups)	#23/236
	ALIPHATIC_HYDROXYLATION_12 {
		@Override
		public String getSMIRKS() {
			return "[CH3][CH1;X4;!$(Cc):1]([H])[CH3]>>C[C:1](O)C";
		}
		@Override
		public String getName() {
			return "aliphatic_hydroxylation_(tert_carbon_linked_to_two_CH3_groups)";
		}
	},
	
//
//	# -- benzylic hydroxylation --
//	[c:1][CH3:2]>>[c:1][C:2]O	0.153	benzylic_hydroxylation_(c-CH3)	#83/544
	BENZYLIC_HYDROXYLATION_1 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH3:2]([H])>>[c:1][C:2]O";
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH3)";
		}
	},
	
//	[c:1][CH2:2][CH3:3]>>[c:1][C:2](O)[C:3]	0.222	benzylic_hydroxylation_(c-CH2-CH3)	#10/45
	BENZYLIC_HYDROXYLATION_2 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH2:2]([H])[CH3:3]>>[c:1][C:2](O)[C:3]";
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH2-CH3)";
		}
	},
	
//	[c:1][CH2:2][#6;!$([CH3]):3]>>[c:1][C:2](O)[*:3]	0.073	benzylic_hydroxylation_(c-CH2-CR)	#78/1069
	BENZYLIC_HYDROXYLATION_3 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH2:2]([H])[#6;!$([CH3]):3]>>[c:1][C:2](O)[#6:3]";
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH2-CR)";
		}
	},
	
//	[c:1][CH2:2][NH0:3]>>[c:1][C:2](O)[N:3]	0.049	benzylic_hydroxylation_(c-CH2-N)	#9/185
	BENZYLIC_HYDROXYLATION_4 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH2:2]([H])[NH0:3]>>[c:1][C:2](O)[N:3]";
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH2-N)";
		}
	},
	
//	[c:1][CH1;X4;!$(C[O,N]):2][CH3:3]>>[c:1][C:2](O)[C:3]	0.106	benzylic_hydroxylation_(c-CH1-CH3)	#9/85
	BENZYLIC_HYDROXYLATION_5 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH1;X4;!$(C[O,N]):2]([H])[CH3:3]>>[c:1][C:2](O)[C:3]"; 
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH1-CH3)";
		}
	},
	
//	[c:1][CH1;X4;!$(C[O,N]):2][#6;c,$(C=*):3]>>[c:1][C:2](O)[*:3]	0.024	benzylic_hydroxylation_(c-CH1-CR)	#5/211
	BENZYLIC_HYDROXYLATION_6 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH1;X4;!$(C[O,N]):2]([H])[#6;c,$(C=*):3]>>[c:1][C:2](O)[#6:3]";
		}
		@Override
		public String getName() {
			return "benzylic_hydroxylation_(c-CH1-CR)";
		}
	},
	
//
//	# -- reduction --
//	[C;X4:1][C:2](=[O:3])[C;X4:4]>>[C:1][C:2](-[O:3])[C:4]	0.349	carbonyl_reduction_(aliphatic)	#114/327
	REDUCTION_1 {
		@Override
		public String getSMIRKS() {
			return "[C;X4:1][C:2](=[O:3])[C;X4:4]>>[C:1][C:2](-[O:3])[C:4]";  // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "carbonyl_reduction_(aliphatic)";
		}
	},
	
//	[C;X3:1][C:2](=[O:3])[C;X4:4]>>[C:1][C:2](-[O:3])[C:4]	0.129	carbonyl_reduction_(next_to_SP2_carbon)	#19/147
	REDUCTION_2 {
		@Override
		public String getSMIRKS() {
			return "[C;X3:1][C:2](=[O:3])[C;X4:4]>>[C:1][C:2](-[O:3])[C:4]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "carbonyl_reduction_(next_to_SP2_carbon)";
		}
	},
	
//	[c:1][C:2](=[O:3])[C;X4:4]>>[c:1][C:2](-[O:3])[C:4]	0.269	carbonyl_reduction_(next_to_aromatic_carbon)	#29/108
	REDUCTION_3 {
		@Override
		public String getSMIRKS() {
			return "[c:1][C:2](=[O:3])[C;X4:4]>>[c:1][C:2](-[O:3])[C:4]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "carbonyl_reduction_(next_to_aromatic_carbon)";
		}
	},
	
//	[c:1][C:2](=[O:3])[c:4]>>[c:1][C:2](-[O:3])[c:4]	0.041	carbonyl_reduction_(both_sides_next_to_aromatic_carbon)	#5/123
	REDUCTION_4 {
		@Override
		public String getSMIRKS() {
			return "[c:1][C:2](=[O:3])[c:4]>>[c:1][C:2](-[O:3])[c:4]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "carbonyl_reduction_(both_sides_next_to_aromatic_carbon)";
		}
	},
	
//	[C:1][CH1:2]=[O:3]>>[C:1][C:2]-[O:3]	0.088	aldehyde_reduction_(aliphatic)	#3/34
	REDUCTION_5 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH1:2]=[O:3]>>[C:1][C:2]-[O:3]";  // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "aldehyde_reduction_(aliphatic)";
		}
	},
	
//	[c:1][CH1:2]=[O:3]>>[c:1][C:2]-[O:3]	0.200	aldehyde_reduction_(aromatic)	#2/10
	REDUCTION_6 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH1:2]=[O:3]>>[c:1][C:2]-[O:3]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "aldehyde_reduction_(aromatic)";
		}
	},
	
//	[C;$(C[OH1]),$(C=O):1][C:2]=[C;!$(Cc):3]>>[C:1][C:2]-[C:3]	0.074	double_bond_reduction	#42/568
	REDUCTION_7 {
		@Override
		public String getSMIRKS() {
			return "[C;$(C[OH1]),$(C=O):1][C:2]=[C;!$(Cc):3]>>[C:1][C:2]-[C:3]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "double_bond_reduction";
		}
	},
	
//	[c;$(c=O):1][c:2][cH1;$(co),$(cn):3]>>[C:1]-[C:2]-[CH2:3]	0.126	double_bond_reduction_(aromatic)	#11/87
	REDUCTION_8 {
		@Override
		public String getSMIRKS() {
			return "[c;$(c=O);r:1]:,-[c;r:2]:,=[c;H1;$(c[#8]),$(c[#7]);r:3]>>[C:1]-[C:2]([H])-[C:3]([H])"; 
			// This can't be tested with the Ambit SMIRKS GUI since it doesn't recognize aromaticity for this ring type.
			// These SMIRKS should apply to compounds such as CN1C=C(C(=O)C2=CC=C(C=C21)F)S(=O)
			// this one works but it not specifically aromatic: [#6;$([#6]=O);r:1]:,-[#6;r:2]:,=[#6;H1;$([#6][#8]),$([#6][#7]);r:3]>>[#6:1]-[#6:2]([H])-[#6:3]([H])
		}
		@Override
		public String getName() {
			return "double_bond_reduction_(aromatic)";
		}
	},
	
//	[C;$(C[OH1]),$(C=O):1][C:2]=[C;$(Cc):3]>>[C:1][C:2]-[C:3]	0.161	double_bond_reduction_(benzylic)	#14/87
	REDUCTION_9 {
		@Override
		public String getSMIRKS() {
			return "[C;$(C[OH1]),$(C=O):1][C:2]=[C;$(Cc):3]>>[C:1][C:2]-[C:3]"; // didn't modify this because the FlagAddImplicitHAtomsOnResultProcess is set to true (see Transformer.java)
		}
		@Override
		public String getName() {
			return "double_bond_reduction_(benzylic)";
		}
	},
	
//
//	# -- aldehyde oxidation --
//	[C:1][CH1:2]=[O:3]>>[C:1][C:2](O)=[O:3]	0.500	aldehyde_oxidation_(aliphatic)	#17/34
	ALDEHYDE_OXIDATION_1 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH1:2]([H])=[O:3]>>[C:1][C:2](O)=[O:3]";
		}
		@Override
		public String getName() {
			return "aldehyde_oxidation_(aliphatic)";
		}
	},
	
//	[c:1][CH1:2]=[O:3]>>[c:1][C:2](O)=[O:3]	0.600	aldehyde_oxidation_(aromatic)	#6/10
	ALDEHYDE_OXIDATION_2 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH1:2]([H])=[O:3]>>[c:1][C:2](O)=[O:3]";
		}
		@Override
		public String getName() {
			return "aldehyde_oxidation_(aromatic)";
		}
	},
	
//
//	# -- O-deacetylation --
//	[#6:1][O:2]C(=O)[CH3]>>[*:1][O:2]	0.529	O-deacetylation	#72/136
	O_DEACETYLATION {
		@Override
		public String getSMIRKS() {
			return "[#6:1][O:2]C(=O)[CH3]>>[#6:1][O:2]";  
		}
		@Override
		public String getName() {
			return "O-deacetylation";
		}
	},
	
//
//	# -- N-deacetylation --
//	[N:2]C(=O)[CH3]>>[N:2]	0.175	N-deacetylation	#14/80
	N_DEACETYLATION {
		@Override
		public String getSMIRKS() {
			return "[N:2]C(=O)[CH3]>>[N:2]";
		}
		@Override
		public String getName() {
			return "N-deacetylation";
		}
	},
	
//
//	# -- decarboxylation --
//	[*;!C:1]~[#6:2]C(=O)[OH1]>>[*:1]~[*:2]	0.023	decarboxylation	#16/691
	DECARBOXYLATION_1 {
		@Override
		public String getSMIRKS() {
			return "[*;!C;!#1:1]~[#6:2]C(=O)[OH1]>>[*:1]~[#6:2]"; // had to specify that atom 1 also can't be a H atom
		}
		@Override
		public String getName() {
			return "decarboxylation";
		}
	},
	
//	[O:1]=[C:2][C:3](=O)[OH1]>>[O:1]=[C:2][O:3]	0.222	oxidative_decarboxylation	#2/9
	DECARBOXYLATION_2 {
		@Override
		public String getSMIRKS() {
			return "[O:1]=[C:2][C](=O)[O]>>[O:1]=[C:2][O]"; // modified a lot - SMIRKS was broken
		}
		@Override
		public String getName() {
			return "oxidative_decarboxylation";
		}
	},
	
//	[CH2:1][CH2]C(=O)[OH1]>>[C:1](=O)O	0.172	beta-oxidation	#28/163
	DECARBOXYLATION_3 {
		@Override
		public String getSMIRKS() {
			return "[CH2:1]([H])([H])[CH2]C(=O)[OH1]>>[C:1](=O)O";
		}
		@Override
		public String getName() {
			return "beta-oxidation";
		}
	},
	
//
//	# -- dehydrogenation --
//	# all_dehydrogenations [C:1][C:2]>>[C:1]=[C:2]		all_dehydro	# /
//	[*;$([#6&X3]),$([#7]~[#6X3]):1][CX4;H1&!$(C-[!#6]),H2:2][CX4;H2:3][*;$([#6&X3]),$([#7]~[#6X3]):4]>>[*:1][CH0:2]=[CH0:3][*:4]	0.041	dehydrogenation_(alpha,beta_to_SP2_both_sides)	#14/342
	DEHYDROGENATION_1 {
		@Override
		public String getSMIRKS() {
			return "[*;$([#6&X3]),$([#7]~[#6X3]):1][CX4;H1&!$(C-[!#6;!#1]),H2:2]([H])[CX4;H2:3]([H])[*;$([#6&X3]),$([#7]~[#6X3]):4]>>[*:1][CH0:2]=[CH0:3][*:4]";
		}
		@Override
		public String getName() {
			return "dehydrogenation_(alpha,beta_to_SP2_both_sides)";
		}
	},
	
//	# not predictive enough ? [*;$([#6&X3]),$([#7]~[#6X3]):1][CX4;H1&!$(C-[!#6]),H2:2][CX4;H2:3][C;H2,H3:4]>>[*:1][C:2]=[C:3][C:4]	0.00562193	dehydrogenation_(alpha,beta_to_SP2)	# 8/1423
	
//	[#6X3:1][CH1&!$(C-[!#6]):2][CH3:3]>>[*:1][CH0:2]=[CH2:3]	0.011	dehydrogenation_(CH1-CH3->C=CH2)	#3/261
	DEHYDROGENATION_2 {
		@Override
		public String getSMIRKS() { // example: CC(=O)C(C)C
			return "[#6X3:1][CH1&!$(C-[!#6;!#1]):2]([H])[CH3:3]([H])>>[#6:1][CH0:2]=[CH2:3]";  // modified b/c SMIRKS broken
		}
		@Override
		public String getName() {
			return "dehydrogenation_(CH1-CH3->C=CH2)";
		}
	},
	
//	[#6X3:1][CH2:2][CH3:3]>>[*:1][CH1:2]=[CH2:3]	0.020	dehydrogenation_(CH2-CH3->C=CH2)	#2/100
	DEHYDROGENATION_3 {
		@Override
		public String getSMIRKS() {
			return "[#6X3:1][CH2:2]([H])[CH3:3]([H])>>[#6:1][CH1:2]=[CH2:3]";
		}
		@Override
		public String getName() {
			return "dehydrogenation_(CH2-CH3->C=CH2)";
		}
	},
	
//	[N,c:1][C;X4;H1:2]-[N;X3;H1:3]>>[*:1][CH0:2]=[NH0:3]	0.102	dehydrogenation_(amine)	#5/49
	DEHYDROGENATION_4 {
		@Override
		public String getSMIRKS() {
			return "[N,c:1][C;X4;H1:2]([H])-[N;X3;H1:3]([H])>>[*:1][CH0:2]=[NH0:3]";
		}
		@Override
		public String getName() {
			return "dehydrogenation_(amine)";
		}
	},
	
//	[c:1][#6:2]1[#6:3]=[#6:4][NH1:5][#6:6]=[#6:7]1>>[c:1][*H0:2]1=[*:3][*:4]=[NH0:5][*:6]=[*:7]1	0.808	dehydrogenation_(aromatization_of_1,4-dihydropyridine)	#21/26
	DEHYDROGENATION_5 {
		@Override
		public String getSMIRKS() {
			return "[c:1][#6:2]([H])1[#6:3]=[#6:4][NH1:5]([H])[#6:6]=[#6:7]1>>[c:1][#6H0:2]1=[#6:3][#6:4]=[NH0:5][#6:6]=[#6:7]1";
		}
		@Override
		public String getName() {
			return "dehydrogenation_(aromatization_of_1,4-dihydropyridine)";
		}
	},
	
//
//	# -- dehydration --
//	[CX4@!H0;$(C[*;#6&X3,$([#7]~[#6X3])]):1]-[CX4@;$(C[*;#6&X3,$([#7]~[#6X3])]):2]([OH1])>>[CH0:1]=[C:2]	0.177	dehydration_next_to_SP2_both_sides	#25/141
	DEHYDRATION_1 {
		@Override
		public String getSMIRKS() { 
			return "[CX4!H0;$(C[#6X3]),$(C[#7]~[#6X3]):1]([H])-[CX4;$(C[#6X3]),$(C[#7]~[#6X3]):2]([OH1])>>[CH0:1]=[C:2]";  // modified. removed stereochemistry info because it caused an error and fixed the recursive SMARTS logic so it had the desired effect
		}
		// should work for C(C=C)C(C=C)O 
		// should not work for C1CCN(CC1)CC2=CC=CC(=C2)OCCCO
		@Override
		public String getName() {
			return "dehydration_next_to_SP2_both_sides";
		}
	},
	
//	[CX4@!H0;!$(C[*;#6&X3,$([#7]~[#6X3])]):1]-[CX4@;$(C[*;#6&X3,$([#7]~[#6X3])]):2]([OH1])>>[CH0:1]=[C:2]	0.017	dehydration_next_to_SP2_a	#7/406
	DEHYDRATION_2 {
		@Override
		public String getSMIRKS() {
			return "[CX4!H0;!$(C[*;#6&X3]);!$(C[#7]~[#6X3]):1]([H])-[CX4;$(C[#6X3]),$(C[#7]~[#6X3]):2]([OH1])>>[CH0:1]=[C:2]"; //modified significantly. removed stereochemistry info because it was preventing the SMIRKS from being applied. Also fixed the logic of the !$ statements for atom 1
		}
		@Override
		public String getName() {
			return "dehydration_next_to_SP2_a";
		}
	},
	
//	[CX4@!H0;$(C[*;#6&X3,$([#7]~[#6X3])]):1]-[CX4@;!$(C[*;#6&X3,$([#7]~[#6X3])]):2]([OH1])>>[CH0:1]=[C:2]	0.019	dehydration_next_to_SP2_b	#8/415
	DEHYDRATION_3 {
		@Override
		public String getSMIRKS() {
			return "[CX4!H0;$(C[#6X3]),$(C[#7]~[#6X3]):1]([H])-[CX4;!$(C[*;#6&X3]);!$(C[#7]~[#6X3]):2]([OH1])>>[CH0:1]=[C:2]"; //modified significantly. removed stereochemistry info because it was preventing the SMIRKS from being applied. Also fixed the logic of the !$ statements for atom 2
		}
		@Override
		public String getName() {
			return "dehydration_next_to_SP2_b";
		}
	},
	
//	# -- primary alcohol oxidation to carboxyl --
//	[c:1][CH2:2][OH1]>>[c:1][C:2](=O)O	0.527	primary_alcohol_oxidation_(benzylic)	#39/74
	PRIMARY_ALCOHOL_OXIDATION_1 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH2:2]([H])([H])[OH1]>>[c:1][C:2](=O)O";
		}
		@Override
		public String getName() {
			return "primary_alcohol_oxidation_(benzylic)";
		}
	},
	
//	[C:1][CH2:2][OH1]>>[C:1][C:2](=O)O	0.199	primary_alcohol_oxidation_(aliphatic)	#84/423
	PRIMARY_ALCOHOL_OXIDATION_2 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH2:2]([H])([H])[OH1]>>[C:1][C:2](=O)O";
		}
		@Override
		public String getName() {
			return "primary_alcohol_oxidation_(aliphatic)";
		}
	},
	
//
//	# -- secondary alcohol oxidation to carbonyl --
//	[C;!$(C[OH1]):1][CH1:2]([C;!$(C[OH1]):3])-[OH1:4]>>[C:1][CH0:2]([C:3])=[OH0:4]	0.101	secondary_alcohol_oxidation_(aliphatic)	#79/786
	SECONDARY_ALCOHOL_OXIDATION_1 {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C[OH1]):1][CH1:2]([H])([C;!$(C[OH1]):3])-[OH1:4]([H])>>[C:1][CH0:2]([C:3])=[OH0:4]";
		}
		@Override
		public String getName() {
			return "secondary_alcohol_oxidation_(aliphatic)";
		}
	},
	
//	[c:1][CH1:2]([C:3])-[OH1:4]>>[c:1][CH0:2]([C:3])=[OH0:4]	0.115	secondary_alcohol_oxidation_(benzylic)	#17/148
	SECONDARY_ALCOHOL_OXIDATION_2 {
		@Override
		public String getSMIRKS() {
			return "[c:1][CH1:2]([H])([C:3])-[OH1:4]([H])>>[c:1][CH0:2]([C:3])=[O:4]";
		}
		@Override
		public String getName() {
			return "secondary_alcohol_oxidation_(benzylic)";
		}
	},
	
//
//	# -- S oxidation --
//	[c:1][S;X3:2](=[O:3])[C:4]>>[c:1][S:2](=[O:3])(=O)[C:4]	0.741	sulfoxide_oxidation_(c-S-C)	#20/27
	S_OXIDATION_1 {
		@Override
		public String getSMIRKS() {
			return "[c:1][S;X3:2](=[O:3])[C:4]>>[c:1][S:2](=[O:3])(=O)[C:4]";
		}
		@Override
		public String getName() {
			return "sulfoxide_oxidation_(c-S-C)";
		}
	},
	
//	[C:1][S;X3:2](=[O:3])[C:4]>>[C:1][S:2](=[O:3])(=O)[C:4]	0.353	sulfoxide_oxidation_(C-S-C)	#12/34
	S_OXIDATION_2 {
		@Override
		public String getSMIRKS() {
			return "[C:1][S;X3:2](=[O:3])[C:4]>>[C:1][S:2](=[O:3])(=O)[C:4]";
		}
		@Override
		public String getName() {
			return "sulfoxide_oxidation_(C-S-C)";
		}
	},
	
//	[c:1][S;X3:2](=[O:3])[c:4]>>[c:1][S:2](=[O:3])(=O)[c:4]	0.333	sulfoxide_oxidation_(c-S-c)	#7/21
	S_OXIDATION_3 {
		@Override
		public String getSMIRKS() {
			return "[c:1][S;X3:2](=[O:3])[c:4]>>[c:1][S:2](=[O:3])(=O)[c:4]";
		}
		@Override
		public String getName() {
			return "sulfoxide_oxidation_(c-S-c)";
		}
	},
	
//	[c:1][S;X2:2][C:4]>>[c:1][S:2](=O)[C:4]	0.186	sulfide_oxidation_(c-S-C)	#13/70
	S_OXIDATION_4 {
		@Override
		public String getSMIRKS() {
			return "[c:1][S;X2:2][C:4]>>[c:1][S:2](=O)[C:4]";
		}
		@Override
		public String getName() {
			return "sulfide_oxidation_(c-S-C)";
		}
	},
	
//	[C:1][S;X2:2][C:4]>>[C:1][S:2](=O)[C:4]	0.237	sulfide_oxidation_(C-S-C)	#50/211
	S_OXIDATION_5 {
		@Override
		public String getSMIRKS() {
			return "[C:1][S;X2:2][C:4]>>[C:1][S:2](=O)[C:4]";
		}
		@Override
		public String getName() {
			return "sulfide_oxidation_(C-S-C)";
		}
	},
	
//	#[c:1][#16;X2:2][c:4]>>[c:1][#16:2](=O)[c:4]	0.25	sulfide_oxidation_(c-S-c)	# 37/148
	S_OXIDATION_6 {
		@Override
		public String getSMIRKS() {
			return "[c:1][#16;X2:2][c:4]>>[c:1][#16:2](=O)[c:4]";
		}
		@Override
		public String getName() {
			return "sulfide_oxidation_(c-S-c)"; // this SMIRKS overlaps with thiophene_oxidation and sulfide_oxidation_(c-S-c)
		}
	},
	
//	[c:1][S;X2:2][c:4]>>[c:1][S:2](=O)[c:4]	0.571	sulfide_oxidation_(c-S-c)	#40/70
	S_OXIDATION_7 {
		@Override
		public String getSMIRKS() {
			return "[c:1][S;X2:2][c:4]>>[c:1][S:2](=O)[c:4]";
		}
		@Override
		public String getName() {
			return "sulfide_oxidation_(c-S-c)";
		}
	},
	
//	[sr5:1]>>[sr5:1]=O	0.025	thiophene_oxidation	#3/121
	S_OXIDATION_8 {
		@Override
		public String getSMIRKS() {
			return "[sr5:1]>>[sr5:1]=O";
		}
		@Override
		public String getName() {
			return "thiophene_oxidation";
		}
	},
	
//	[S;X3;$(S([#6])[#6]):1]=O>>[S:1]	0.195	sulfoxide_reduction	#16/82
	S_REDUCTION {
		@Override
		public String getSMIRKS() {
			return "[S;X3;$(S([#6])[#6]):1]=O>>[S:1]";
		}
		@Override
		public String getName() {
			return "sulfoxide_reduction";
		}
	},
	
//
//	# -- epoxide_hydrolysis --
//	[C:1]1O[C:2]1>>[C:1](O)[C:2]O	0.333	epoxide_hydrolysis	#10/30
	EPOXIDE_HYDROLYSIS {
		@Override
		public String getSMIRKS() {
			return "[C:1]1O[C:2]1>>[C:1](O)[C:2]O";
		}
		@Override
		public String getName() {
			return "epoxide_hydrolysis";
		}
	},
	
//	# not predictive [C:1]=[C:2]>>[C:1]1-[C:2]O1		epoxidation	# /
//
//	# -- oxidative_deamination --
//	[#6:1][N:2]=;@[C:3]([#6:4])[N:5]>>([*:1][N:2]-[C:3]([*:4])=O.[N:5])	0.057	oxidative_deamination_(amidine)	#3/53
	OXIDATIVE_DEAMINATION_1 {
		@Override
		public String getSMIRKS() {
			return "[#6:1][N:2]=;@[C:3]([#6:4])[N:5]>>([#6:1][N:2]-[C:3]([#6:4])=O.[N:5])"; // modified
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(amidine)";
		}
	},
	
//	[nX2:1][c:2][N:3]>>([nH1:1][c:2]=O.[N:3])	0.029	oxidative_deamination_(aromatic)	#23/795
	OXIDATIVE_DEAMINATION_2 {
		@Override
		public String getSMIRKS() {
			return "[nX2:1][c:2]([N:3])>>[nH1:1]([H])-[c:2](=O.[N:3])"; // this won't work for all molecules (any that require the double bonds to 
			// shift in the aromatic ring in the product) but if it doesn't, 
			// the product should be thrown out because of too many bonds on the C and the other cases should be caught by the two reactions below that I added to cover the shifting bonds case.
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(aromatic)";
		}
	},
	
	OXIDATIVE_DEAMINATION_AROMATIC_6RING {
		@Override
		public String getSMIRKS() {  // example of 2-ring system: NC1=NC(N)=C2N=C(C(N)=NC2=N1)C1=CC=CC=C1
			return "[nX2:1]1[c:2]([N:3])[a:4][a:5][a:6][a:7]1>>[nX2:1]([H])1-[c:2](=O.[N:3])-[a:4]=[a:5]-[a:6]=[a:7]-1"; // the shifting of bonds in the aromatic ring doesn't 
			// happen automatically and I can't figure out how to get it to work without explicitly including the whole ring in the reaction
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(aromatic)";
		}
	},
	
	OXIDATIVE_DEAMINATION_AROMATIC_5RING_1 {
		@Override
		public String getSMIRKS() {  
			return "[nX2:1]1[c:2]([N:3])[a:4][n:5]([H])[a:6]1>>[nH1:1]([H])1-[c:2](=O.[N:3])-[a:4]-[n:5]=[a:6]-1"; // the shifting of bonds in the aromatic ring doesn't 
			// happen automatically and I can't figure out how to get it to work without explicitly including the whole ring in the reaction
			// this covers the case of imidazole c1c(N)nc[nH]1
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(aromatic)";
		}
	},
	
	// not necessary because double bond could just stay put in the case of pyrazole
	
//	// for pyrazole: c1c(N)n[nH]c1
//	OXIDATIVE_DEAMINATION_AROMATIC_5RING_2 { 
//		@Override
//		public String getSMIRKS() {  // example of 2-ring system: NC1=NC(N)=C2N=C(C(N)=NC2=N1)C1=CC=CC=C1
//			return "[nX2:1]1[c:2]([N:3])[a:4][a:5][n:6]([H])1>>[nH1:1]([H])1-[c:2](=O.[N:3])-[a:4]-[a:5]=[n:6]-1"; // the shifting of bonds in the aromatic ring doesn't 
//			// happen automatically and I can't figure out how to get it to work without explicitly including the whole ring in the reaction
//			// this covers the case of pyrazole but TODO is the double bond in the right place??
//		}
//		@Override
//		public String getName() {
//			return "oxidative_deamination_(aromatic)";
//		}
//		@Override
//		public PriorityLevel getPriorityLevel() {
//			return null;
//		}
//	},
//	
	
	
//	[C:1][CH2:2][NH2]>>[C:1][CH1:2]=O	0.030	oxidative_deamination_(on_primary_carbon)	#2/67
	OXIDATIVE_DEAMINATION_3 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH2:2]([H])[NH2]>>[C:1][CH1:2]=O";
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(on_primary_carbon)";
		}
	},
	
//	[C:1][CH1:2]([C:3])[NH2]>>[C:1][CH0:2]([C:3])=O	0.106	oxidative_deamination_(on_secondary_carbon)	#15/142
	OXIDATIVE_DEAMINATION_4 {
		@Override
		public String getSMIRKS() {
			return "[C:1][CH1:2]([H])([C:3])[NH2]>>[C:1][CH0:2]([C:3])=O";
		}
		@Override
		public String getName() {
			return "oxidative_deamination_(on_secondary_carbon)";
		}
	},
	
//	# -- nitro --
//	[c:1][N+](=O)[O-]>>[c:1][NH2]	0.122	nitro_to_aniline	#16/131
	NITRO_1 {
		@Override
		public String getSMIRKS() {
			return "[c:1][N+](=O)[O-]>>[c:1][NH2]"; // TODO: double-check whether CDK represents nitro in charged form so that it's recognized
		}
		@Override
		public String getName() {
			return "nitro_to_aniline";
		}
	},
	
//	[c;$(c1[cH1][cH1][c]([*;!#1])[cH1][cH1]1):1][NH2]>>[c:1][N+](=O)[O-]	0.045	aniline_to_nitro	#3/66
	NITRO_2 {
		@Override
		public String getSMIRKS() {
			return "[c;$(c1[cH1][cH1][c]([*;!#1])[cH1][cH1]1):1][NH2]>>[c:1][N+](=O)[O-]";
		}
		@Override
		public String getName() {
			return "aniline_to_nitro";
		}
	},
	
//	# not predictive [c:1][N+:2](=[O])[O-]>>[c:1][N:2]=[O]		nitro_to_nitroso	# /
//
//	# -- dehalogenation --
//	# no occurence [#6:1][C:2](=[O:3])[*;F,Cl,Br,I]>>[#6:1][C:2](=[O:3])O		haloacid_hydrolysis	# /
//	# no occurence [C:1]([OH1:2])[*;Cl,Br,I]>>[C:1]=[O:2]		oxidative_dehalogenation	# /
//	[CX4;H1,H2:1][Cl,Br,I]>>[C:1]O	0.120	aliphatic_dehalogenation	#11/92
	DEHALOGENATION_1 {
		@Override
		public String getSMIRKS() {
			return "[CX4;H1,H2:1][Cl,Br,I]>>[C:1]O";
		}
		@Override
		public String getName() {
			return "aliphatic_dehalogenation";
		}
	},
	
//	[c;$(c1ccc([#7])cc1):1][Cl]>>[c:1]O	0.045	aromatic_dechlorination	#4/88
	DEHALOGENATION_2 {
		@Override
		public String getSMIRKS() {
			return "[c;$(c1ccc([#7])cc1):1][Cl]>>[c:1]O";
		}
		@Override
		public String getName() {
			return "aromatic_dechlorination";
		}
	},
	
//
//	# -- condensation --
//	# Consider the next 3 as one rule
//	[OH1][C:2]!@[*:3]~!@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O	0.133	ring_closure_(hydroxyl-5bonds-carboxyl)	#2/15
	CONDENSATION_1 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]!@[*:3]~!@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O"; // works fine with C(=O)(O)CCCO
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-5bonds-carboxyl)";
		}
	},
	
//	[OH1][C:2]@[*:3]~!@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O	0.133	ring_closure_(hydroxyl-5bonds-carboxyl)	#2/15
	CONDENSATION_2 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]@[*:3]~!@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O"; // works fine with C(=O)(O)CC1C(O)CCCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-5bonds-carboxyl)";
		}
	},
	
//	[OH1][C:2]!@[*:3]~@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O	0.133	ring_closure_(hydroxyl-5bonds-carboxyl)	#2/15
	CONDENSATION_3 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]!@[*:3]~@[*:4][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]C1=O"; // works fine with C(=O)(O)C1C(CO)CCCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-5bonds-carboxyl)";
		}
	},
	
//	# Consider the next 3 as one rule
//	[NH1;!$(NC=O):1][#6:2]~!@[*:3]~!@[*:4]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]C1=O	0.438	ring_closure_(NH1-5bonds-carboxyl)2	#7/16
	CONDENSATION_4 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~!@[*:3]~!@[*:4]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]C1=O"; // now works with C(=O)(O)CCCNCC
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-5bonds-carboxyl)2";
		}
	},
	
//	[NH1;!$(NC=O):1][#6:2]~[*:3]~!@[*:4]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]C1=O	0.438	ring_closure_(NH1-5bonds-carboxyl)2	#7/16
	CONDENSATION_5 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~[*:3]~!@[*:4]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]C1=O"; // now works with same mol as above
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-5bonds-carboxyl)2";
		}
	},
	
//	[NH1;!$(NC=O):1][#6:2]~!@[*:3]~[*:4]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]C1=O	0.438	ring_closure_(NH1-5bonds-carboxyl)2	#7/16
	CONDENSATION_6 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~!@[*:3]~[*:4]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]C1=O"; // now works with same mol as above
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-5bonds-carboxyl)2";
		}
	},
	
//	# Consider the next 4 as one rule
//	[OH1][C:2]!@[*:3]~!@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O	0.302	ring_closure_(hydroxyl-6bonds-carboxyl)	#16/53
	RING_CLOSURE_HYDROXYL_6BONDS_1 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]!@[*:3]~!@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O"; // works fine with C(=O)(O)CCCCO
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-6bonds-carboxyl)";
		}
	},
	
//	[OH1][C:2]@[*:3]~!@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O	0.302	ring_closure_(hydroxyl-6bonds-carboxyl)	#16/53
	RING_CLOSURE_HYDROXYL_6BONDS_2 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]@[*:3]~!@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O"; // works fine with C(=O)(O)CCC1C(O)CCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-6bonds-carboxyl)";
		}
	},
	
//	[OH1][C:2]!@[*:3]~@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O	0.302	ring_closure_(hydroxyl-6bonds-carboxyl)	#16/53
	RING_CLOSURE_HYDROXYL_6BONDS_3 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]!@[*:3]~@[*:4]~!@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O"; // works fine with C(=O)(O)CC1C(CO)CCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-6bonds-carboxyl)";
		}
	},
	
//	[OH1][C:2]!@[*:3]~!@[*:4]~@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O	0.302	ring_closure_(hydroxyl-6bonds-carboxyl)	#16/53
	RING_CLOSURE_HYDROXYL_6BONDS_4 {
		@Override
		public String getSMIRKS() {
			return "[OH1][C:2]!@[*:3]~!@[*:4]~@[*:5][C;!$(CC1OCC(O)C(O)C1O)](=O)-[OH1]>>O1[C:2][*:3]~[*:4]~[*:5]C1=O"; // works fine with C(=O)(O)C1C(CCO)CCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(hydroxyl-6bonds-carboxyl)";
		}
	},
	
//	# Consider the next 4 as one rule
//	[NH1;!$(NC=O):1][#6:2]~!@[*:3]~!@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]~[*:5]C1=O	0.424	ring_closure_(NH1-6bonds-carboxyl)	#14/33
	RING_CLOSURE_NH1_6BONDS_1 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~!@[*:3]~!@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]~[*:5]C1=O"; // now works for C(=O)(O)CCCCNCC
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-6bonds-carboxyl)";
		}
	},
	
//	[NH1;!$(NC=O):1][#6:2]~@[*:3]~!@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]~[*:5]C1=O	0.424	ring_closure_(NH1-6bonds-carboxyl)	#14/33
	RING_CLOSURE_NH1_6BONDS_2 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~@[*:3]~!@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]~[*:5]C1=O"; // now works for C(=O)(O)CCC1C(NCC)CCCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-6bonds-carboxyl)";
		}
	},
	
//	[NH1;!$(NC=O):1][#6:2]~!@[*:3]~@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]~[*:5]C1=O	0.424	ring_closure_(NH1-6bonds-carboxyl)	#14/33
	RING_CLOSURE_NH1_6BONDS_3 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~!@[*:3]~@[*:4]~!@[*:5]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]~[*:5]C1=O"; // now works for C(=O)(O)CC1C(CNCC)CCCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-6bonds-carboxyl)";
		}
	},
	
//	[NH1;!$(NC=O):1][#6:2]~!@[*:3]~!@[*:4]~@[*:5]C(=O)-[OH1]>>[N:1]1[*:2]~[*:3]~[*:4]~[*:5]C1=O	0.424	ring_closure_(NH1-6bonds-carboxyl)	#14/33
	RING_CLOSURE_NH1_6BONDS_4 {
		@Override
		public String getSMIRKS() {
			return "[NH1;!$(NC=O):1]([H])[#6:2]~!@[*:3]~!@[*:4]~@[*:5]C(=O)-[OH1]>>[N:1]1[#6:2]~[*:3]~[*:4]~[*:5]C1=O"; // now works for C(=O)(O)C1C(CCNCC)CCCC1
		}
		@Override
		public String getName() {
			return "ring_closure_(NH1-6bonds-carboxyl)";
		}
	},
	
//	# presumed [OH1:1][C:2][A:3][A:4][C:5](=[O:6])[N:7]>>[O:1]1[C:2][A:3][A:4][C:5]1=[O:6].[N:7]		hydroxyl-amide_5ring_closure	# /
//	# presumed [OH1:1][C:2][A:3][A:4][A:5][C:6](=[O:7])-[N:8]>>[O:1]1[C:2][A:3][A:4][A:5][C:6]1=[O:7].[N:8]hydroxyl-amide_6ring_closure	# /
//	# presumed [OH1:1][C:2][A:3][N:4][C:5](=[O:6])>>[O:1]1[C:2][A:3][N:4].[C:5]1=[O:6]		hydroxyl-amide_5ring_rearr	# /
//	# presumed [OH1:1][C:2][A:3][A:4][N:5][C:6](=[O:7])>>[O:1]1[C:2][A:3][A:4][N:5].[C:6]1=[O:7]		hydroxyl-amide_6ring_rearr	# /
//
//	# -- hydrolysis --
//	[C;$(C=O):1][O:2][CH3]>>[C:1][O:2]	0.357	hydrolysis_(methoxyester)	#35/98
	HYDROLYSIS_METHOXYESTER {
		@Override
		public String getSMIRKS() {
			return "[C;$(C=O):1][O:2][CH3]>>[C:1][O:2]";
		}
		@Override
		public String getName() {
			return "hydrolysis_(methoxyester)";
		}
	},
	
//	[C$(C[#6!H3]):2](=[O:3])O[#6!H3:4]>>([C:2](=[O:3])O.O[*:4])	0.272	hydrolysis_(ester)	#268/984
	HYDROLYSIS_ESTER {
		@Override
		public String getSMIRKS() {
			return "[C$(C[#6!H3]):2](=[O:3])O[#6!H3:4]>>([C:2](=[O:3])O.O[#6:4])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(ester)";
		}
	},
	
//	[C$(C[#6!H3]):2](=[O:3])[NH2]>>[C:2](=[O:3])O	0.294	hydrolysis_(primary_amide)	#30/102
	HYDROLYSIS_PRIMARY_AMIDE {
		@Override
		public String getSMIRKS() {
			return "[C$(C[#6!H3]):2](=[O:3])[NH2]>>[C:2](=[O:3])O";
		}
		@Override
		public String getName() {
			return "hydrolysis_(primary_amide)";
		}
	},
	
//	[C$(C[#6!H3]):2](=[O:3])[NH1:4][#6:5]>>([C:2](=[O:3])O.[N:4][*:5])	0.092	hydrolysis_(secondary_amide)	#109/1191
	HYDROLYSIS_SECONDARY_AMIDE {
		@Override
		public String getSMIRKS() {
			return "[C$(C[#6!H3]):2](=[O:3])[NH1:4][#6:5]>>([C:2](=[O:3])O.[N:4][#6:5])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(secondary_amide)";
		}
	},
	
//	[C$(C[#6!H3]):2](=[O:3])[#7:4]([#6:5])[#6:6]>>([C:2](=[O:3])O.[*:4]([*:5])[*:6])	0.096	hydrolysis_(tertiary_amide)	#61/637
	HYDROLYSIS_TERTIARY_AMIDE {
		@Override
		public String getSMIRKS() {
			return "[C$(C[#6!H3]):2](=[O:3])[#7:4]([#6:5])[#6:6]>>([C:2](=[O:3])O.[#7:4]([#6:5])[#6:6])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(tertiary_amide)";
		}
	},
	
//	[C$(C[#6!H3]):2](=[O:3])[N:4][*;!#6:5]>>([C:2](=[O:3])O.[N:4][*:5])	0.189	hydrolysis_(heteroatom_bonded_amide)	#18/95
	HYDROLYSIS_HETEROATOM_BONDED_AMIDE {
		@Override
		public String getSMIRKS() {
			return "[C$(C[#6!H3]):2](=[O:3])[N:4][*;!#6;!#1:5]>>([C:2](=[O:3])O.[N:4][*:5])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(heteroatom_bonded_amide)";
		}
	},
	
//	[#7,#8:1][C:2](=[O:3])[#7,#8:4][*:5]>>([*:1][C:2](=[O:3])O.[*H1:4][*:5])	0.053	hydrolysis_(urea_or_carbonate)	#57/1066
	HYDROLYSIS_UREA_CARBONATE {
		@Override
		public String getSMIRKS() {
			return "[#7,#8:1][C:2](=[O:3])[#7,#8:4][*;!#1:5]>>([*:1][C:2](=[O:3])O.[*H1:4][*:5])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(urea_or_carbonate)";
		}
	},
	
//	[*:5][*;!#6;!$(S(=O)(=O)N);!$(P(O)(O)(O)=O):1](=[*;!#6:2])[N,O:3][*:4]>>([*:5][*:1](=[*:2])O.[*:3][*:4])	0.174	hydrolysis_(X=X-X_exclude_phosphate)	#55/317
	HYDROLYSIS_XXX {
		@Override
		public String getSMIRKS() {
			return "[*;!#1:5][*;!#6;!#1;!$(S(=O)(=O)N);!$(P(O)(O)(O)=O):1](=[*;!#6;!#1:2])[N,O:3][*;!#1:4]>>([*:5][*:1](=[*:2])O.[*:3][*:4])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(X=X-X_exclude_phosphate)";
		}
	},
	
//	[#6:1][N:2][CH1]([OH1])[*:3]>>([*:1][N:2].C(=O)[*:3])	0.102	hydrolysis_(CNC(OH)R)	#5/49
	HYDROLYSIS_CNCOHR {
		@Override
		public String getSMIRKS() {
			return "[#6:1][N:2][CH1]([OH1])[*;!#1:3]>>([#6:1][N:2].C(=O)[*:3])";
		}
		@Override
		public String getName() {
			return "hydrolysis_(CNC(OH)R)";
		}
	},
	
//	# now covered by oxidative deamination (aromatic): [n:2][c:3]!@[N;$(N(C)(C)c),$(NS(=O)=O):5]>>([n:2][c:3]O.[N:5])	0.0215385	hydrolysis_(N-substituted-pyridine)	# 7/325
//
//	# -- N-oxidation --
//	[C;X4;!H3;!$(C(N)[!#6;!#1]):1][N;X3:2]([C;X4;!H3;!$(C(N)[!#6;!#1]):3])[C;X4;!H3;!$(C(N)[!#6;!#1]):4]>>[C:1][N+:2]([C:3])([C:4])[O-]	0.060	N-oxidation_(tertiary_N)	#30/503
	N_OXIDATION_TERTIARY_N {
		@Override
		public String getSMIRKS() {
			return "[C;X4;!H3;!$(C(N)[!#6;!#1]):1][N;X3:2]([C;X4;!H3;!$(C(N)[!#6;!#1]):3])[C;X4;!H3;!$(C(N)[!#6;!#1]):4]>>[C:1][N+:2]([C:3])([C:4])[O-]";
		}
		@Override
		public String getName() {
			return "N-oxidation_(tertiary_N)";
		}
	},
	
//	[C;X4;!H3;!$(C(N)[!#6;!#1]):1][N;X3:2]([CH3:3])[C;X4;!H3;!$(C(N)[!#6;!#1]):4]>>[C:1][N+:2]([C:3])([C:4])[O-]	0.190	N-oxidation_(tertiary_NCH3)	#38/200
	N_OXIDATION_TERTIARY_NCH3 {
		@Override
		public String getSMIRKS() {
			return "[C;X4;!H3;!$(C(N)[!#6;!#1]):1][N;X3:2]([CH3:3])[C;X4;!H3;!$(C(N)[!#6;!#1]):4]>>[C:1][N+:2]([C:3])([C:4])[O-]";
		}
		@Override
		public String getName() {
			return "N-oxidation_(tertiary_NCH3)";
		}
	},
	
//	[C;X4;!$(C(N)[!#6;!#1]):1][N;X3:2]([CH3:3])[CH3:4]>>[C:1][N+:2]([C:3])([C:4])[O-]	0.195	N-oxidation_(RN(CH3)2)	#30/154
	N_OXIDATION_TERTIARY_RNCH32 {
		@Override
		public String getSMIRKS() {
			return "[C;X4;!$(C(N)[!#6;!#1]):1][N;X3:2]([CH3:3])[CH3:4]>>[C:1][N+:2]([C:3])([C:4])[O-]";
		}
		@Override
		public String getName() {
			return "N-oxidation_(RN(CH3)2)";
		}
	},
	
//	[#6:1]~[#7;X2;R:2]~[#6:3]>>[*:1]~[*+:2](~[*:3])[O-]	0.036	N-oxidation_(-N=)	#47/1313
	N_OXIDATION_N {
		@Override
		public String getSMIRKS() { // example: CCC1CN=CC1
			return "[#6:1]~[#7;X2;R:2]~[#6:3]>>[#6:1]~[#7+:2](~[#6:3])[O-]";
		}
		@Override
		public String getName() {
			return "N-oxidation_(-N=)";
		}
	},
	
//	[c:1][NH2:2]>>[c:1][N:2]O	0.014	N-oxidation_(aniline)	#4/277
	N_OXIDATION_ANILINE {
		@Override
		public String getSMIRKS() {
			return "[c:1][NH2:2]([H])>>[c:1][N:2]O"; // had to remove one hydrogen
		}
		@Override
		public String getName() {
			return "N-oxidation_(aniline)";
		}
	},
	
//
//	# -- acetyl_shift --
//	[#6:1][C:2](=O)O[C:5][C:6][OH1]>>[*:1][C:2](=O)O[C:6][C:5]O	0.071	acetyl_shift	#8/113
	ACETYL_SHIFT {
		@Override
		public String getSMIRKS() {
			return "[#6:1][C:2](=O)O[C:5][C:6][OH1]>>[#6:1][C:2](=O)O[C:6][C:5]O";
		}
		@Override
		public String getName() {
			return "acetyl_shift";
		}
	},
	
//
//	# -- tautomerisation --
//	[c:1][C:2](=[O:3])[CH2:4][#6:5]>>[c:1][C:2](-[O:3])=[C:4][*:5]	0.036	tautomerisation_(keto->enol)	#2/55
	TAUTOMERISATION_KETO_ENOL {
		@Override
		public String getSMIRKS() {
			return "[c:1][C:2](=[O:3])[CH2:4]([H])[#6:5]>>[c:1][C:2](-[O:3])=[C:4][#6:5]";
		}
		@Override
		public String getName() {
			return "tautomerisation_(keto->enol)";
		}
	},
	
//
//	# -- special rules --
//	[#6:3][CH1:1]=[CH2:2]>>[*:3][C:1](O)-[C:2]O	0.200	vinyl_oxidation	#12/60
	VINYL_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#6:3][CH1:1]=[CH2:2]>>[#6:3][C:1](O)-[C:2]O";
		}
		@Override
		public String getName() {
			return "vinyl_oxidation";
		}
	},
	
//	[#6:3][C:1]([CH3:4])=[CH2:2]>>[*:3][C:1]([CH3:4])(O)-[C:2]O	0.300	isopropenyl_oxidation	#3/10
	ISOPROPENYL_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#6:3][C:1]([CH3:4])=[CH2:2]>>[#6:3][C:1]([CH3:4])(O)-[C:2]O";
		}
		@Override
		public String getName() {
			return "isopropenyl_oxidation";
		}
	},
	
//	[CH2:1][CH2;R:2][N:3]>>[C:1][C:2](=O)[N:3]	0.048	oxidation_(amine_in_a_ring)	#53/1108
	OXIDATION_AMINE_RING {
		@Override
		public String getSMIRKS() {
			return "[CH2:1][CH2;R:2]([H])([H])[N:3]>>[C:1][C:2](=O)[N:3]";
		}
		@Override
		public String getName() {
			return "oxidation_(amine_in_a_ring)";
		}
	},
	
//	[#6:1][C:2]([#6:3])=[N;!$(N-N):4]>>([*:1][C:2]([*:3])=O.[N:4])	0.027	imine_hydrolysis	#3/113
	IMINE_HYDROLYSIS {
		@Override
		public String getSMIRKS() {
			return "[#6:1][C:2]([#6:3])=[N;!$(N-N):4]>>([#6:1][C:2]([#6:3])=O.[N:4])";
		}
		@Override
		public String getName() {
			return "imine_hydrolysis";
		}
	},
	
//	[#6:2]=[N:4]-[N:5]>>([*:2]=O.[N:4]-[N:5])	0.204	hydrazone_hydrolysis	#11/54
	HYDRAZONE_HYDROLYSIS {
		@Override
		public String getSMIRKS() {
			return "[#6:2]=[N:4]-[N:5]>>([#6:2]=O.[N:4]-[N:5])";
		}
		@Override
		public String getName() {
			return "hydrazone_hydrolysis";
		}
	},
	
//	[c:1][N:2]=[N:3][c:4]>>([c:1][N:2].[N:3][c:4])	0.778	diazene_cleavage	#7/9
	DIAZENE_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[c:1][N:2]=[N:3][c:4]>>([c:1][N:2].[N:3][c:4])";
		}
		@Override
		public String getName() {
			return "diazene_cleavage";
		}
	},
	
//	[*:1][N:2]=[N+]=[N-]>>[*:1][N:2]	0.500	azide_cleavage	#4/8
	AZIDE_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[*:1][N:2]=[N+]=[N-]>>[*:1][N:2]"; // TODO: make sure this works with CDK's charge handling
		}
		@Override
		public String getName() {
			return "azide_cleavage";
		}
	},
	
//	[#6:1][c:2]1[cH1:3][cH1:4][cH1:5][cH1:6][cH1:7]1>>[*:1][c:2]1[c:3][c:4](OC)[c:5](O)[c:6][c:7]1		0.021	aromatic_oxidation	#12/573
	AROMATIC_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#6:1][c:2]1[cH1:3][cH1:4]([H])[cH1:5]([H])[cH1:6][cH1:7]1>>[#6:1][c:2]1[c:3][c:4](OC)[c:5](O)[c:6][c:7]1"; // TODO: this reaction seems a bit sketchy
		}
		@Override
		public String getName() {
			return "aromatic_oxidation";
		}
	},
	
//	# discarded [*;!#1:7][#6:1](:1):[#6:2]:[#6H1:3]:[#6H1:4]:[#6:5]:[#6:6]:1>>[*:7][#6:1](-1)=[#6:2]-[#6:3](O)-[#6:4](O)-[#6:5]=[#6:6]-1		try	# /
	
//	[P:1]=[S]>>[P:1]=[O]	0.294	phosphine_sulphide_hydrolysis	#10/34
	PHOSPHINE_SULPHIDE_HYDROLYSIS {
		@Override
		public String getSMIRKS() {
			return "[P:1]=[S]>>[P:1]=[O]";
		}
		@Override
		public String getName() {
			return "phosphine_sulphide_hydrolysis";
		}
	},
	
//	# low occurence [N:1][CH1:2]=[N:3]>>[N:1][CH1:2](O)=[N:3]		xanthine_oxidation	# /
	
//	[#7,O;H1:1][#6:2]:1:[#6:3]:[#6:4]:[#6:5](:[#6:6]:[#6:7]:1)[#7,O;H1:8]>>[*:1]=[*:2]-1-[*:3]=[*:4]-[*:5](-[*:6]=[*:7]-1)=[*:8]	0.045	oxidation_to_quinone	#4/89
	OXIDATION_TO_QUINONE {
		@Override
		public String getSMIRKS() {
			return "[#7,O;H1:1]([H])[#6:2]:1:[#6:3]:[#6:4]:[#6:5](:[#6:6]:[#6:7]:1)[#7,O;H1:8]([H])>>[*:1]=[#6:2]-1-[#6:3]=[#6:4]-[#6:5](-[#6:6]=[#6:7]-1)=[*:8]";
		}
		@Override
		public String getName() {
			return "oxidation_to_quinone";
		}
	},
	
//	[#6:1][O:2]@[CH1:3]([OH1:4])[*:5]>>([*:1][O:2].[C:3]([O:4])[*:5])	0.300	cyclic_hemiacetal_ring_opening	#3/10
	CYCLIC_HEMIACETAL_RING {
		@Override
		public String getSMIRKS() {
			return "[#6:1][O:2]@[CH1:3]([OH1:4])[*:5]>>([#6:1][O:2].[C:3]([O:4])[*:5])";
		}
		@Override
		public String getName() {
			return "cyclic_hemiacetal_ring_opening";
		}
	},
	
//	[NX2:1]=[CH1:2]>>[N:1]-[C:2]=O	0.028	oxidation_(C=N)	#1/36
	CN_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[NX2:1]=[CH1:2]([H])>>[N:1]-[C:2]=O";
		}
		@Override
		public String getName() {
			return "oxidation_(C=N)";
		}
	},
	
//	[#6X3:1][I]>>[*:1]	0.125	deiodonidation	#3/24
	DEIODONIDATION {
		@Override
		public String getSMIRKS() {
			return "[#6X3:1][I]>>[#6:1]";
		}
		@Override
		public String getName() {
			return "deiodonidation";
		}
	},
	
//	[C:1]#N>>[C:1](=O)-N	0.013	nitrile_to_amide	#1/75
	NITRILE_TO_AMIDE {
		@Override
		public String getSMIRKS() {
			return "[C:1]#N>>[C:1](=O)-N";
		}
		@Override
		public String getName() {
			return "nitrile_to_amide";
		}
	},
	
//
//	# -- steroids --
//	[C;$(C~1~C~C~C~C~2~C~C~C~3~C~4~C~C~C~C~4~C~C~C~3~C~2~1):1]1[C:2][C:3](=[O:30])[C:4][C:5]=[C:6]1>>[C:1]1[C:2][C:3](=[O:30])[C:4]=[C:5]-[C:6]1	0.667	steroid_d5d4	#2/3
	STEROID_D5D4 {
		@Override
		public String getSMIRKS() { // example O=C4/CC3/CC[C@@H]2[C@H](CC[C@@]1(C(=O)CC[C@H]12)C)[C@@]=3CC4
			return "[C;$(C~1~C~C~C~C~2~C~C~C~3~C~4~C~C~C~C~4~C~C~C~3~C~2~1):1]1[C:2][C:3](=[O:30])[C:4]([H])[C:5]=[C:6]1>>[C:1]1[C:2][C:3](=[O:30])[C:4]=[C:5]-[C:6]1";
		}
		@Override
		public String getName() {
			return "steroid_d5d4";
		}
	},
	
//	[C;$(C~1~C~2~C~C~C~3~C~4~C~C~C~C~C~4~C~C~C~3~C~2~C~C~1):17]([OH1:30])!@[C:31]>>([C:17]=[OH0:30].[C:31])	0.056	steroid_17hydroxy_to_keto	#9/162
	STEROID_17HYDROXY {
		@Override
		public String getSMIRKS() {
			return "[C;$(C~1~C~2~C~C~C~3~C~4~C~C~C~C~C~4~C~C~C~3~C~2~C~C~1):17]([OH1:30]([H]))!@[C:31]>>([C:17]=[OH0:30].[C:31])";
		}
		@Override
		public String getName() {
			return "steroid_17hydroxy_to_keto";
		}
	},
	
	
	;
		
	public String getName() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public PriorityLevel getPriorityLevel() {
		return PriorityLevel.COMMON;
	}
	
	public Double getLikelihood() {
		return null;
	}
	
	public Phase getPhase() {
		return Phase.PHASE_1;
	}
	
}
