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
 * This enum is the "GST" reaction rule set consisting of only glutathione conjugation reactions. 
 * The reaction rules correspond to metabolic reactions mediated by glutathione S-transferases.
 * The reaction rules were created by Christina de Bruyn Kops based on the scientific literature.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsGST implements Transformations {
	
	// WARNING - these are not from SyGMa
	// GST reaction rules, developed myself (not covered by SyGMa's reaction rules)

	
	// glutathione: SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N
	
	GLUTATHIONE_CONJUGATION_EPOXIDE {
		@Override
		public String getSMIRKS() { // example substrate C1CC2OC2O1
			// trans-stilbene oxide C1=CC=C(C=C1)C2C(O2)C3=CC=CC=C3
			return "[C:1]([H])1-[O:2]-[C:3]([H])1>>[C:1]([O:2]([H]))[C:3]SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(epoxide)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},

	
	GLUTATHIONE_CONJUGATION_ALPHA_BETA_UNSATURATED_CARBONYL {
		@Override
		public String getSMIRKS() { // example substrate 
			// 4-HNE CCCCCC(O)C=CC=O
			// acrolein C=CC=O
			// should NOT match C1=CC(=O)C=CC(=O)1 or C1=CC(=O)C=CC(=N)1
			return "[C;!$(C1=CC(=O)C=CC(=[N,O])1);!$(C1=CC(=O)C(=O)C=C1):1]=[C;$(CC=O):2]>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)-[C;$(CC=O):2]([H])";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(alpha,beta-unsaturated_carbonyl)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_NUCLEOPHILIC_SUBSTITUTION {
		@Override
		public String getSMIRKS() { 
			// example substrates
			// c1ccccc1CCl
			// c1ccccc1COS(=O)(=O)O
			// ethylene dibromide BrCCBr
			return "[C;H2;$(Cc),$(CCc),$(CC=C),$(C[CH3]),$(C[CH2]):1]([H])[*;Br,Cl,I,$(OS(=O)(=O)[O-]),$(OS(=O)(=O)O*),$(OP(=O)(O*)O*)]>>[C;H2:1]([H])SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N"; 
			// the slides also allow an alkyl R group (atom 1) but I can't figure out a good way to specify that exactly
			// TODO is there a better way to specify alkyl groups?
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(aliphatic_nucleophilic_substitution)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_CHLOROALKENE {
		@Override
		public String getSMIRKS() { 
			// example substrate: hexachlorobutadiene ClC(Cl)=C(Cl)C(Cl)=C(Cl)Cl
			return "[C;$(C=C):1](Cl)>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)"; 
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(chloroalkene)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_AROMATIC_NUCLEOPHILIC_SUBSTITUTION {
		@Override
		public String getSMIRKS() { // example substrate 
			return "[c:1][*;Br,Cl,I,$(OS(=O)(=O)[O-]),$(OS(=O)(=O)O*),$(OP(=O)(O*)O*)]>>[c:1]SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N"; // TODO is this correct and specific enough?
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(aromatic_nucleophilic_substitution)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_QUINONE {
		@Override
		public String getSMIRKS() { // example substrate:
			// amodiaquine quinoneimine  CCN(CC)CC1=CC(=NC2=C3C=CC(=CC3=NC=C2)Cl)C=CC1=O
			// NAPQI CC(=O)N=c1ccc(=O)cc1
			return "[C:1]([H])1-[C:2](=[O:3])-[C:4]=[C:5]-[C:6](=[O,N:7])-[C:8]=1>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)1-[C:2](-[O:3])=[C:4]-[C:5]=[C:6](-[*:7])-[C:8]=1";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(quinone)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_ORTHOQUINONE {
		@Override
		public String getSMIRKS() { // example substrate:
			// C(=O)1C(=O)C=CC=C1
			return "[C:1]([H])1[C:2](=O)[C:3](=O)[C:4]=[C:5][C:6]=1>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)1[C:2](-O)=[C:3](-O)[C:4]=[C:5][C:6]=1";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(orthoquinone)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_ISOCYANATE {
		@Override
		public String getSMIRKS() { // example substrate CS(=O)CCCCN=C=S
			return "[C;$(C(=N[#6])=[S,O]):1]=[N:2]>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)[N:2]";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(isocyanate_isothiocyanate)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
	GLUTATHIONE_CONJUGATION_NITRILE {
		@Override
		public String getSMIRKS() { // example substrate CCCC#N
			return "[C:1]#[N:2]>>[C:1](SCC(C(=O)NCC(=O)O)NC(=O)CCC(C(=O)O)N)=[N:2]";
		}
		@Override
		public String getName() {
			return "glutathione_conjugation_(nitrile)";
		}
		@Override
		public Double getLikelihood() {
			return null;
		}
	},
	
//	GLUTATHIONE_CONJUGATION_X {
//		@Override
//		public String getSMIRKS() { // example substrate 
//			return "";
//		}
//		@Override
//		public String getName() {
//			return "glutathione_conjugation_()";
//		}
//		@Override
//		public Double getLikelihood() {
//			return null;
//		}
//	},
	
	
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

