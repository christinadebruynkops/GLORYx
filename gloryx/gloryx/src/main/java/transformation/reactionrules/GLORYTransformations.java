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

import main.java.transformation.PriorityLevel;
import main.java.transformation.Transformations;
import main.java.utils.Phase;

/**
 * This enum is the reaction rule set from GLORY. 
 * All rules represent CYP-mediated metabolic reactions.
 * The reaction rules were created by Christina de Bruyn Kops based on the scientific literature.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum GLORYTransformations implements Transformations {	
	
		
	// hydroxylation
	
	ALIPHATIC_HYDROXYLATION {
		@Override
		public String getSMIRKS() {
			return "[C;X4:1][H:2]>>[C:1][O][H:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	ALIPHATIC_HYDROXYLATION_WITH_ALLYLIC_REARRANGEMENT_1 {
		@Override
		public String getSMIRKS() {
			return "[C;!$(C(=C)CC=C);X3:1]=[C;X3:2][C;!$(C(C=C)C=C);X4:3]([H])>>[C:1](O)-[C:2]=[C:3]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	ALIPHATIC_HYDROXYLATION_WITH_ALLYLIC_REARRANGEMENT_2 {
		@Override
		public String getSMIRKS() {
			return "[C;X3:1]=[C;X3:2][C;$(C(C=C)C=C)X4:3]([H])([H])>>[C:1](O)-[C:2]=[C:3]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	AROMATIC_HYDROXYLATION {
		@Override
		public String getSMIRKS() {
			return "[c:1][H:2]>>[c:1][O][H:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	AROMATIC_HYDROXYLATION_WITH_NIH_SHIFT {
		@Override
		public String getSMIRKS() {
			return "[c:1]([H:5])[c;$(c1c([H])c([H])[c;H0]c([H])c([H])1):2][CH3,Br,Cl:3]>>[c:1]([*:3])[c:2][O][H:5]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	AROMATIC_HYDROXYLATION_OF_PYRAZOLONE {
		@Override
		public String getSMIRKS() {
			return "[#6;$([#6]1[#6](=O)[#7][#7][#6]:,=1),$([#6]1:,=[#6][#6](=O)[#7][#7]1):1][H:2]>>[#6:1][O][H:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	
	HYDROXYLATION_OF_CYCLOPROPANE {
		@Override
		public String getSMIRKS() {
			return "[C:1]1[C:2][C:3]1[C:4]([H])>>[C:1](O)[C:2][C:3]=[C:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	AMINE_HYDROXYLATION {
		@Override
		public String getSMIRKS() {
			return "[N:1]([H:3])[#6:2]>>[N:1]([O][H:3])[#6:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	
	// dealkylation
	
	N_DEALKYLATION {
		@Override
		public String getSMIRKS() {
			return "[#7:1][C:2]([H])>>[#7:1][H].[C:2]=[O]";
		}
		@Override
		public String getName() {
			return "N-dealkylation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	N_DEALKYLATION_PIPERAZINE {
		@Override
		public String getSMIRKS() {
			return "[*;!#1:1][N;X3:2]1[C:3][C:4][N;X3:5][CH2][CH2]1>>[*:1][N:2][C:3][C:4][N:5]";
		}
		@Override
		public String getName() {
			return "N-dealkylation of piperazine";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	N_DEALKYLATION_MORPHOLINE {
		@Override
		public String getSMIRKS() {
			return "[N;X3;$(N1CCOCC1):1][CH2;$(C1NCCOC1)][CH2;$(C1OCCNC1)][O;$(O1CCNCC1):4]>>[N:1].[O:4]";
		}
		@Override
		public String getName() {
			return "N-dealkylation of morpholine";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	S_DEALKYLATION {
		// aka thioether cleavage
		@Override
		public String getSMIRKS() {
			return "[C:3][#16:1][C:2]([H])>>[C:3][#16:1]([H]).[C:2]=[O]";
		}
		@Override
		public String getName() {
			return "S-dealkylation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	O_DEALKYLATION_METHYLENEDIOXYPHENYL {
		@Override
		public String getSMIRKS() {
			return "[O$(O1c2ccccc2OC1):1][C:2]([H])([H])[O$(O1c2ccccc2OC1):3]>>[O:1]([H]).[C:2](=O)[O-].[O:3]([H])";
		}
		@Override
		public String getName() {
			return "O-dealkylation of methylenedioxyphenyl";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	
	// oxygenation
	
	S_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#16:1] >> [#16:1](=[O])";
		}
		@Override
		public String getName() {
			return "S-oxidation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	N_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#7;X3,X2;H0:1][#6:2]>>[#7+:1]([O-])[#6:2]";
		}
		@Override
		public String getName() {
			return "N-oxidation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	P_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[#15;X3:1]>>[#15;X4:1]=[O]";
		}
		@Override
		public String getName() {
			return "P-oxidation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	ALDEHYDE_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])=[O:2]>>[C:1](O)=[O:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	ALDEHYDE_OXIDATION_TO_OLEFIN {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])[C:2]([H])[C:3]([H])=[O:4]>>[C:1]=[C:2].[C:3](O)=[O:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	ALCOHOL_OXIDATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])[O:2][H]>>[C:1]=[O:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},

	
	OLEFIN_OXIDATION_1 {  
		@Override
		public String getSMIRKS() {
			return "[C:1]([C:3])([C:4])=[C:2]([C:5])([C:6])>>[C:1](=O)([C:3])[C:2]([C:4])([C:5])([C:6])";
		}
		@Override
		public String getName() {
			return "olefin oxidation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	ACETYLENE_OXIDATION_1 {  
		@Override
		public String getSMIRKS() {
			return "[#6:3][C:1]#[C:2][#6:4]>>[#6:3][C:1]([#6:4])=[C:2](=O)";
		}
		@Override
		public String getName() {
			return "acetylene oxidation, first step";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	ACETYLENE_OXIDATION_2 {  
		@Override
		public String getSMIRKS() {
			return "[#6:3][C:1]#[C:2][#6:4]>>[#6:3][C:1]([#6:4])[C:2](=O)[O]";
		}
		@Override
		public String getName() {
			return "acetylene oxidation, complete oxidation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	OXIDATION_OF_PHENOL_TO_QUINONE {  
		@Override
		public String getSMIRKS() {
			return "[c:1]1([O:7][H])[c:2][c:3][c;X3:4]([!C:8])[c:5][c:6]1>>[C:1]1(=[O:7])-[C:2]=[C:3]-[C;X3:4](=O)-[C:5]=[C:6]-1.[!C:8]";
		}
		@Override
		public String getName() {
			return "oxidation of 4-substituted phenol to quinone";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_ANISOLE_TO_QUINONE {  
		@Override
		public String getSMIRKS() {
			return "[c:1]1([O:7][C:9])[c:2][c:3][c;X3:4]([!C:8])[c:5][c:6]1>>[C:1]1(=[O:7])-[C:2]=[C:3]-[C;X3:4](=O)-[C:5]=[C:6]-1.[!C:8].[C:9]";
		}
		@Override
		public String getName() {
			return "oxidation of 4-substituted anisole to quinone";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_PHENOL_TO_QUINONE_2 {  
		@Override
		public String getSMIRKS() {
			return "[c:1]1([O:7][H])[c:2][c:3][c;X3:4]([C:8])[c:5][c:6]1>>[C:1]1(=[O:7])-[C:2]=[C:3]-[C:4]([C:8])(O)-[C:5]=[C:6]-1";
		}
		@Override
		public String getName() {
			return "oxidation of 4-substituted phenol to quinone (substituent not a leaving group)";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_PHENOL_TO_QUINONE_IMINE {  
		@Override
		public String getSMIRKS() {
			return "[c:1]1([O:7][H])[c:2][c:3][c;X3:4]([N:8][H])[c:5][c:6]1>>[C:1]1(=[O:7])-[C:2]=[C:3]-[C;X3:4](=[N:8])-[C:5]=[C:6]-1";
		}
		@Override
		public String getName() {
			return "oxidation of 4-substituted phenol to quinone imine";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_ANISOLE_TO_QUINONE_2 {  
		@Override
		public String getSMIRKS() {
			return "[c:1]1([O:7][C:9])[c:2][c:3][c;X3:4]([C:8])[c:5][c:6]1>>[C:1]1(=[O:7])-[C:2]=[C:3]-[C:4]([C:8])(O)-[C:5]=[C:6]-1.[C:9]";
		}
		@Override
		public String getName() {
			return "oxidation of 4-substituted anisole to quinone (substituent not a leaving group)";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_INDOLE {  
		@Override
		public String getSMIRKS() {
			return "[c;$(c1cc2ccccc2n1),$(c1c2ccccc2nc1):1]([H])=,:[c:2]-,:[n:3]>>[C:1](=[O])-[C:2]-[N:3]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	

	
	// epoxidation
	
	EPOXIDATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]=[C:2]>>[C:1]1[C:2][O]1";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	
	
	// aromatization
	
	OXIDATION_OF_DIHYDROPYRIDINES {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1]1([H])[#6:2]=[#6:3][#6;X4:4]([H])[#6:5]=[#6:6]1>>[n;H0:1]1=[#6:2][#6:3]=[#6:4][#6:5]=[#6:6]1";
		}
		@Override
		public String getName() {
			return "oxidation of 1,4-dihydropyridines";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	
	// dearylation
	
	N_DEARYLATION {
		@Override
		public String getSMIRKS() {
			return "[c;R1:1]1[c;R1:2][c;R1:3][c;R1:4][c;R1:5][c;R1:6]1[N:7][c:8]>>[C:1]1=[C:2]-[C:3]-[C:4]=[C:5]-[C:6]1=[O].[c:8][NH2:7]";
		}
		@Override
		public String getName() {
			return "N-dearylation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	O_DEARYLATION_1 {
		@Override
		public String getSMIRKS() {
			return "[c;R1:1]1[c;R1:2][c;R1:3][c;R1:4][c;R1:5][c;R1:6]1[O:7][c:8]>>[C:1]1=[C:2]-[C:3]-[C:4]=[C:5]-[C:6]1=[O].[c:8][O:7]";
		}
		@Override
		public String getName() {
			return "O-dearylation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	O_DEARYLATION_2 {
		@Override
		public String getSMIRKS() {
			return "[c;R1:1]1[c;R1:2][c;R1:3][c;R1:4][c;R1:5][c;R1:6]1[O:7][c;R1:8]2[c;R1:9][c;R1:10][c;R1:11][c;R1:12][c;R1:13]2>>[C:1]1=[C:2]-[C:3]-[C:4]=[C:5]-[C:6]1=[O:7].[C:8]2(=[O])-[C:9]=[C:10]-[C:11]-[C:12]=[C:13]-2";
		}
		@Override
		public String getName() {
			return "O-dearylation";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	O_DEARYLATION_3 {
		@Override
		public String getSMIRKS() {
			return "[c;R1:1]1[c;R1:2][c;R1:3]([O:20][H])[c;R1:4][c;R1:5][c;R1:6]1[O:7][c:8]>>[C:1]1=[C:2]-[C:3](=[O:20])-[C:4]=[C:5]-[C:6]1=[O].[c:8][O:7]";
		}
		@Override
		public String getName() {
			return "O-dearylation with para hydroxyl";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	O_DEARYLATION_4 {
		@Override
		public String getSMIRKS() {
			return "[c;R1:1]1[c;R1:2][c;R1:3]([O:20][H])[c;R1:4][c;R1:5][c;R1:6]1[O:7][c;R1:8]2[c;R1:9][c;R1:10][c;R1:11][c;R1:12][c;R1:13]2>>[C:1]1=[C:2]-[C:3](=[O:20])-[C:4]=[C:5]-[C:6]1=[O:7].[C:8]2(=[O])-[C:9]=[C:10]-[C:11]-[C:12]=[C:13]-2";
		}
		@Override
		public String getName() {
			return "O-dearylation with para hydroxyl";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	
	// deformylation
	
	DEFORMYLATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])-[C:2]([H])-[C:3]=[O:4]>>[C:1]=[C:2].[C:3]=[O:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	

	// desulfuration
	
	DESULFURATION_OF_PHOSPHOR {
		@Override
		public String getSMIRKS() {
			return "[*:1][P:2](=S)([*:3])[*:4]>>[*:1][P:2](=O)([*:3])[*:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},

	DESULFURATION_OF_CARBON {
		@Override
		public String getSMIRKS() {
			return "[*:1][C:2](=S)[*:3]>>[*:1][C:2](=O)[*:3]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},


	// reduction of nitrogen compounds
	
	REDUCTION_OF_N_OXIDE {
		@Override
		public String getSMIRKS() {
			return "[#7+;X4:1]([O-])>>[#7;X3:1]";
		}
		@Override
		public String getName() {
			return "reduction of N-oxide";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_RNOR_1 {
		@Override
		public String getSMIRKS() {
			return "[#8;$([#8][#6]):1][#7:2]:,=[#6:3]>>[#8:1]([H]).[#7:2]([H])([H])-[#6:3]([H])";
		}
		@Override
		public String getName() {
			return "reduction of RNOR";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_RNOR_2 {
		@Override
		public String getSMIRKS() {
			return "[#8;$([#8][#6]):1][#7:2][#6;!X4:3]>>[#8:1]([H]).[#7:2].[#6:3]=[O]";
		}
		@Override
		public String getName() {
			return "reduction of RNOR";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_RNOR_3 {
		@Override
		public String getSMIRKS() {
			return "[#8;$([#8][#6]):1][#7;$([#7][#6]):2]>>[#8:1]([H]).[#7:2]([H])";
		}
		@Override
		public String getName() {
			return "reduction of RNOR";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_NITRO_GROUP_1 {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1](=O)=[O]>>[NH2:1]";
		}
		@Override
		public String getName() {
			return "reduction of nitro group";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_NITRO_GROUP_2 {
		@Override
		public String getSMIRKS() {
			return "[N+;X3:1](=O)[O-]>>[NH2:1]";
		}
		@Override
		public String getName() {
			return "reduction of nitro group";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTION_OF_NITROSO_COMPOUND {
		@Override
		public String getSMIRKS() {
			// C- or N-nitroso compound allowed
			return "[C,N:1][N;X2:2](=O)>>[C,N:1][N;H2:2]";
		}
		@Override
		public String getName() {
			return "reduction of C- or N-nitroso compound";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	AZO_REDUCTION {
		@Override
		public String getSMIRKS() {
			return "[#6:1][N:2]=[N:3][#6:4]>>[#6:1][NH2:2].[NH2:3][#6:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	HYDRAZINE_REDUCTION {
		@Override
		public String getSMIRKS() {
			return "[NX3:1]-[NX3:2]>>[N:1]([H]).[N:2]([H])";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	

	// dehalogenation
	
	OXIDATIVE_DEHALOGENATION_ALKYL {  
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])[F,Cl,Br:2]>>[C:1]=[O].[F,Cl,Br:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	OXIDATIVE_DEHALOGENATION_BENZYL {  
		@Override
		public String getSMIRKS() {
			return "[c;$([c;!H]1ccccc1),$(c1[c;!H]cccc1),$(c1c[c;!H]ccc1),$(c1cc[c;!H]cc1),$(c1ccc[c;!H]c1),$(c1cccc[c;!H]1):1][F,Cl,Br,I:2]>>[c:1][O].[F,Cl,Br,I:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	REDUCTIVE_DEHALOGENATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([F,Cl,Br:3])[C:2]([F,Cl,Br:4])>>[C:1]=[C:2].[*:3].[*:4]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	
	// dehydrogenation / desaturation
	
	ALKYL_DEHYDROGENATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])-[C:2]([H])>>[C:1]=[C:2]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	DEHYDROGENATION_OF_N_C_BOND_1 {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1]([H])[C;!H3:2][H]>>[N:1]=[C:2]";
		}
		@Override
		public String getName() {
			return "dehydration of N-C bond";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	DEHYDROGENATION_OF_N_C_BOND_2 {
		@Override
		public String getSMIRKS() {
			return "[#7;X3:1]([H]):,-[#6;!H3:2]([H]):,=[#6:3]-[C:4]([H])>>[#7:1]=[#6:2]-[#6:3]=[C:4]";
		}
		@Override
		public String getName() {
			return "dehydration of N-C bond";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	DEHYDROGENATION_OF_N_C_BOND_3 {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1][C:2][H]>>[N+:1]=[C:2]";
		}
		@Override
		public String getName() {
			return "dehydration of N-C bond";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	
	// oxidative ether and ester cleavage
	
	OXIDATIVE_ETHER_CLEAVAGE {
		// to one alcohol and one aldehyde/ketone, if only one of the ether carbons has a hydrogen attached
		@Override
		public String getSMIRKS() {
			return "[#6:1][O:2][C:3]([H])>>[#6:1][O;H1:2].[C;X3:3](=O)";
		}
		@Override
		public String getName() {
			return "oxidative ether cleavage to one alcohol and one aldehyde/ketone";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.COMMON;
		}
	},
	
	OXIDATIVE_ESTER_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[C$(C(O)([#6])=O):2][O:3][C:4][H]>>[C:2][O:3].[C:4]=[O]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	MONOTHIOPHOSPHATE_ESTER_CLEAVAGE_1 {
		@Override
		public String getSMIRKS() {
			return "[S:1]=[P$(P(O)(O)=S):2][O:3][#6:4]>>[S:1]=[P:2][O:3].[#6:4][O]";
		}
		@Override
		public String getName() {
			return "monothiophosphate ester cleavage";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	MONOTHIOPHOSPHATE_ESTER_CLEAVAGE_2 {
		@Override
		public String getSMIRKS() {
			return "[S:1]=[P$(P(O)(O)=S):2][O:3][#6:4]>>[S:1].[O]=[P:2][O:3].[#6:4][O]";
		}
		@Override
		public String getName() {
			return "monothiophosphate ester cleavage";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	PHOSPHOESTER_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[O:1]=[P$(P(O)(O)=O):2][O:3][#6:4]>>[O:1]=[P:2][O:3].[#6:4][O]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CARBAMATE_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[#7:1][C;$([C](O)=O):2][O:3][C:4]>>[#7:1][H].[C:2]=[O:3].[C:4][O]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CARBAMIDE_CLEAVAGE {
		@Override
		public String getSMIRKS() {
			return "[N:1][C;$([C](N)(N)=O):2][N:3]>>[N:1][C:2].[N:3]";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	OXIDATION_OF_N_NITROSAMINE {
		@Override
		public String getSMIRKS() {
			return "[N$(N(C)C):1]([C:3][H])[N$(N(N)=O):2]>>[N:1]([H])([H]).[N:2]([O-]).[C:3](=O)";
		}
		@Override
		public String getName() {
			return "oxidation of N-nitrosamine";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	SCISSION_OF_UNSATURATED_FATTY_ACID_PEROXIDES_1 {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])=[C:2]-[C:3]=[C:4]-[C:5]-[O:6]([O])>>[C:1]=[C:2][C:3](O)[C:4]1-[C:5]-[O:6]1";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	SCISSION_OF_UNSATURATED_FATTY_ACID_PEROXIDES_2 {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])=[C:2]-[C:3]=[C:4]-[C:5]-[O:6]([O])>>[C:1]([O])[C:2]=[C:3][C:4]1-[C:5]-[O:6]1";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	
	
	// dehydration
	
	ALDOXIME_DEHYDRATION {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])=[N:2][O]([H])>>[C:1]#[N:2]";
		}
		@Override
		public String getName() {
			return "dehydration of an aldoxime to a nitrile";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	// ring formation
	
	CYCLIZATION_TO_6_MEMBERED_LACTONE {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])([OH])[#6:2][#6:3][#6:4][C;$(C=O):5][O:6][*:7]>>[C:1]1[#6:2][#6:3][#6:4][C;$(C=O):5][O:6]1.[*:7]";
		}
		@Override
		public String getName() {
			return "cyclization to 6-membered lactone";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CYCLIZATION_TO_5_MEMBERED_LACTONE {
		@Override
		public String getSMIRKS() {
			return "[C:1]([H])[c:2][c:3][C;$(C=O):4][O:5][C,#1:6]>>[C:1]1[c:2][c:3][C:4][O:5]1.[*:6]";
		}
		@Override
		public String getName() {
			return "cyclization to 5-membered lactone";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CYCLIZATION_TO_6_MEMBERED_NCN_RING {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1]([H])~[*:2]~[*:3]~[*:4]~[N:5]-[C:6]([H])>>[N:1]1~[*:2]~[*:3]~[*:4]~[N:5]-[C:6]1";
		}
		@Override
		public String getName() {
			return "cyclization to 6-membered NCN ring";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CYCLIZATION_TO_5_MEMBERED_NCN_RING {
		@Override
		public String getSMIRKS() {
			return "[N;X3:1]([H])~[*:2]~[*:3]~[N:5]-[C:6]([H])>>[N:1]1~[*:2]~[*:3]~[N:5]-[C:6]1";
		}
		@Override
		public String getName() {
			return "cyclization to 5-membered NCN ring";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	CYCLIZATION_TO_FURAN {
		@Override
		public String getSMIRKS() {
			return "[O:1]=[C;R1:2][C;R1:3]=[C:4][C:5]([H])[H]>>[O:1]1[C:2]=[C:3][C:4]=[C:5]1";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	
	// ring expansion
	
	CYCLOBUTAMINE_EXPANSION {
		@Override
		public String getSMIRKS() {
			return "[C:1]1-[C:2]-[C:3]-[C$(C1(C)CCC1):4]1[N:5]([H])>>[C:1]1-[C:2]-[C:3]-[C:4]=[N+:5]1";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},
	
	D_HOMOANNULATION_STEROIDS {
		@Override
		public String getSMIRKS() {
			return "[C$([#6R1]~1~[#6R1]~[#6R1]~[#6R2]~2~[#6R2]~1~[#6R1]~[#6R1]~[#6R2]~3~[#6R2]~2~[#6R1]~[#6R1]~[#6R2]~4~[#6R1]~[#6R1]~[#6R1]~[#6R1]~[#6R2]~3~4):1]1([O:6][H])([C:7]#[C:8])[C:2][C:3][C:4][C:5]1>>[C:1]1(=[O:6])[C:7](=[C:8](O))[C:2][C:3][C:4][C:5]1";
		}
		@Override
		public String getName() {
			return "D-homoannulation of 17 alpha-ethinyl steroids";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},

	OXIDATION_OF_SPIROOXANE {
		@Override
		public String getSMIRKS() {
			return "[C:1]1[C:2]2([C:3][C:4]2)[C:5]([H])[C:6][C:7][C:8]1>>[C:1]1[C:2]2(O)[C:3][C:4][C:5]2[C:6][C:7][C:8]1";
		}
		@Override
		public String getName() {
			return "oxidation of spiro[2,5]oxane";
		}
		@Override
		public PriorityLevel getPriorityLevel() {
			return PriorityLevel.UNCOMMON;
		}
	},

	
	
	
	;

	public abstract String getSMIRKS();
	
	public abstract PriorityLevel getPriorityLevel();
	
	public String getName() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public Double getLikelihood() {
		return null;
	}
	
	public Phase getPhase() {
		return Phase.PHASE_1;
	}
	
}

