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

package main.java.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Converts DrugBank enzyme annotation to the relevant information (enzyme family).
 * The metabolism phase that each enyzme family corresponds to is set here.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum Enzymes implements Enzyme {
	
	// template:
	//			allNames.add("");
	
	CYP {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Cytochrome P450 2C8");
			allNames.add("Cytochrome P450 2C9");
			allNames.add("Cytochrome P450 2C18");
			allNames.add("Cytochrome P450 2C19");
			
			allNames.add("Cytochrome P450 3A7"); // unusual
			allNames.add("Cytochrome P450 3A4");
			allNames.add("Cytochrome P450 3A5");
			allNames.add("Cytochrome P450 3A43"); // unusual - is this an error?
			allNames.add("Cytochrome p450 3A subfamily");
			allNames.add("CYP3A"); // this was not included in the dataset for GLORY

			allNames.add("Cytochrome P450 2D6");
			allNames.add("Cytochrome P450 2E1");
			
			allNames.add("Cytochrome P450 1A1");
			allNames.add("Cytochrome P450 1A2");
			
			//unusual
			allNames.add("Cytochrome P450 2B6");
			allNames.add("Cytochrome P450 2B7 isoform");
			allNames.add("Cytochrome P450 2A6");
			allNames.add("Cytochrome P450 2J2");
			allNames.add("Cytochrome P450 1B1");
			allNames.add("Cytochrome P450 4A11");
			allNames.add("Cytochrome P450 19A1");
			allNames.add("Leukotriene-B(4) omega-hydroxylase 1"); //CYP4F2
			allNames.add("25-hydroxyvitamin D-1 alpha hydroxylase mitochondrial"); 
			allNames.add("125-dihydroxyvitamin D(3) 24-hydroxylase mitochondrial");  // CYP24A1
			allNames.add("Steroid 21-hydroxylase"); 
			allNames.add("Steroid 17-alpha-hydroxylase/1720 lyase"); 
			allNames.add("Vitamin D 25-hydroxylase"); 
			allNames.add("Cholesterol side-chain cleavage enzyme mitochondrial"); // CYP11A1
			allNames.add("CYP2B protein");

			
			// Note: The dataset for GLORY only took into account enzyme names beginning with "Cytochrome P450"

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	UGT {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Glucuronosyltransferase");
			allNames.add("UDP-glucuronosyltransferase 1-1");
			allNames.add("UDP-glucuronosyltransferase 1-3");
			allNames.add("UDP-glucuronosyltransferase 1-4");
			allNames.add("UDP-glucuronosyltransferase 1-6");
			allNames.add("UDP-glucuronosyltransferase 1-7");
			allNames.add("UDP-glucuronosyltransferase 1-8");
			allNames.add("UDP-glucuronosyltransferase 1-9");
			allNames.add("UDP-glucuronosyltransferase 1-10");

			allNames.add("UDP-glucuronosyltransferase 1A1");
			allNames.add("UDP-glucuronosyltransferase 1A9");
			
			allNames.add("UDP-glucuronosyltransferase 2B4");
			allNames.add("UDP-glucuronosyltransferase 2B7");
			allNames.add("UDP-glucuronosyltransferase 2B10");
			allNames.add("UDP-glucuronosyltransferase 2B15");
			allNames.add("UDP-glucuronosyltransferase 2B17");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}
		
		@Override
		public String getEnzymeFamily() {
			return name();
		}

	},
	
	
	GST {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Glutathione S-transferase Mu 1");
			allNames.add("Glutathione S-transferase theta-1");
			allNames.add("Glutathione S-transferase P");
			allNames.add("Glutathione S-transferase A1");
			allNames.add("Glutathione S-transferase A2");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}
		
		@Override
		public String getEnzymeFamily() {
			return name();
		}

	},

	
	SULT {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Sulfotransferase 1A1");
			allNames.add("Sulfotransferase 2A1");
			allNames.add("Sulfotransferase 1A2");
			allNames.add("Sulfotransferase");
			allNames.add("Sulfotransferase 1A3/1A4");
			allNames.add("Bile salt sulfotransferase");  // TODO double-check
			allNames.add("Sulfotransferase family cytosolic 1B member 1");
			allNames.add("Estrogen sulfotransferase"); // TODO double-check
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}
		
		@Override
		public String getEnzymeFamily() {
			return name();
		}

	},
	
	
	CARBOXYLESTERASE { // see Laizure 2013 review

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Carboxylesterase");
			allNames.add("Liver carboxylesterase 1");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	ADH {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Alcohol dehydrogenase 1C");
			allNames.add("Alcohol dehydrogenase 1A");
			allNames.add("Alcohol dehydrogenase 1B");
			allNames.add("Alcohol dehydrogenase [NADP(+)]");

			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},

	
	ALDH { // aldehyde dehydrogenase

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Aldehyde dehydrogenase");
			allNames.add("Aldehyde dehydrogenase dimeric NADP-preferring");
			allNames.add("Aldehyde dehydrogenase mitochondrial");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	MT {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Thiopurine S-methyltransferase");
			allNames.add("Catechol O-methyltransferase");
			allNames.add("Methionine synthase");
			allNames.add("Phenylethanolamine N-methyltransferase");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}
		
		@Override
		public String getEnzymeFamily() {
			return name();
		}

	},
	
	
	NAT {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Arylamine N-acetyltransferase 1");
			allNames.add("Arylamine N-acetyltransferase 2");
			allNames.add("Diamine acetyltransferase 1"); //TODO double check
			allNames.add("Bile acid-CoA:amino acid N-acyltransferase");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}
		
		@Override
		public String getEnzymeFamily() {
			return name();
		}

	},
	
	ACYL_COA_LIGASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Medium-chain fatty-acid--CoA ligase");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}

	},
	
	
	AO {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Amine oxidase [flavin-containing] A");
			allNames.add("Amine oxidase [flavin-containing] B");
			allNames.add("Monoamine oxidase");
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	AOX {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Aldehyde oxidase");

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	FMO {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Dimethylaniline monooxygenase [N-oxide-forming] 3");
			allNames.add("Dimethylaniline monooxygenase [N-oxide-forming] 1");


			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	
	
	HYDROLASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Cholinesterase");
			allNames.add("Epoxide hydrolase 1");
			allNames.add("Valacyclovir hydrolase");
			allNames.add("Serum paraoxonase/arylesterase 1");
			allNames.add("Liver esterases");
			allNames.add("Beta-ureidopropionase");
			allNames.add("Adenosine deaminase");
			allNames.add("Deoxycytidylate deaminase");
			allNames.add("5-oxoprolinase");
			allNames.add("Cytidine deaminase");
			allNames.add("3-ketoacyl-CoA thiolase peroxisomal");
			allNames.add("Phospholipase A2 membrane associated");
			allNames.add("Dipeptidase 1");
			allNames.add("Dipeptidyl peptidase 4");
			allNames.add("Glutamyl aminopeptidase");
			allNames.add("Beta-glucosidase");
			allNames.add("Phospholipase D1");
			allNames.add("Intestinal-type alkaline phosphatase");
			allNames.add("Ectonucleotide pyrophosphatase/phosphodiesterase family member 1");
			allNames.add("Angiotensin-converting enzyme 2");
			allNames.add("Neprilysin");
			allNames.add("Histidine triad nucleotide-binding protein 1");
			
			
			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},

	
	PEROXIDASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Prostaglandin G/H synthase 1");
			allNames.add("Prostaglandin G/H synthase 2");
			allNames.add("Glutathione peroxidase 1");
			allNames.add("Myeloperoxidase");

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	REDUCTASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Thioredoxin reductase 1 cytoplasmic");
			allNames.add("Carbonyl reductase [NADPH] 1");
			allNames.add("Carbonyl reductase [NADPH] 3");
			allNames.add("Aldo-keto reductase family 1 member C3");
			allNames.add("Pentaerythritol tetranitrate reductase");
			allNames.add("Dihydrofolate reductase");
			allNames.add("Glutathione reductase mitochondrial");
			allNames.add("NADPH azoreductase");
			allNames.add("Methylenetetrahydrofolate reductase");

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	OTHER_DEHYDROGENASES { 

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Xanthine dehydrogenase/oxidase");
			allNames.add("Succinate-semialdehyde dehydrogenase mitochondrial");
			allNames.add("Retinal dehydrogenase 1");
			allNames.add("3-oxo-5-alpha-steroid 4-dehydrogenase 2");
			allNames.add("Dihydropyrimidine dehydrogenase [NADP(+)]");
			allNames.add("Bifunctional methylenetetrahydrofolate dehydrogenase/cyclohydrolase mitochondrial");
			allNames.add("Medium-chain specific acyl-CoA dehydrogenase mitochondrial");
			allNames.add("Hydroxyacyl-coenzyme A dehydrogenase mitochondrial");
			allNames.add("3 beta-hydroxysteroid dehydrogenase/Delta 5-->4-isomerase type 1");
			allNames.add("NAD(P)H dehydrogenase [quinone] 1");
			allNames.add("NADH dehydrogenase [ubiquinone] iron-sulfur protein 7 mitochondrial");
			allNames.add("NADH dehydrogenase [ubiquinone] iron-sulfur protein 3 mitochondrial");
			allNames.add("NADH dehydrogenase [ubiquinone] iron-sulfur protein 2 mitochondrial");


			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	OTHER_OXIDOREDUCTASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Quinone oxidoreductase");
			allNames.add("Betabeta-carotene 1515'-dioxygenase");
			allNames.add("Arachidonate 15-lipoxygenase");
			allNames.add("Catalase");
			allNames.add("Fatty acid desaturase 2");
			allNames.add("Nitric oxide synthase endothelial");
			allNames.add("Nitric oxide synthase brain");
			allNames.add("Nitric oxide synthase inducible");
			allNames.add("Aldo-keto reductase family 1 member C4");
			allNames.add("Dopamine beta-hydroxylase");
			allNames.add("Aldo-keto reductase family 1 member C1");
			allNames.add("Thyroxine 5-deiodinase");
			allNames.add("Corticosteroid 11-beta-dehydrogenase isozyme 1");

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_1;
		}

	},
	
	KINASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Creatine kinase");
			allNames.add("Deoxycytidine kinase");
			allNames.add("Deoxyguanosine kinase mitochondrial");
			allNames.add("UMP-CMP kinase");
			allNames.add("Hexokinase-1");
			allNames.add("Adenylate kinase 2 mitochondrial");
			allNames.add("Adenylate kinase isoenzyme 1");
			allNames.add("Galactokinase");
			allNames.add("Adenosine kinase");
			allNames.add("Nucleoside diphosphate kinase A");
			allNames.add("Adenylate kinase 4 mitochondrial");
			allNames.add("Nucleoside diphosphate kinase A");
			allNames.add("Nucleoside diphosphate kinase B");
			allNames.add("Nucleoside diphosphokinase");


			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}

	},
	
	
	OTHER_TRANSFERASES {

		@Override
		public List<String> getAllNameAndIsozymeVariations() {
			List<String> allNames = new ArrayList<>();
			
			allNames.add("Protein-glutamine gamma-glutamyltransferase 6");
			allNames.add("Hypoxanthine-guanine phosphoribosyltransferase");
			allNames.add("Choline-phosphate cytidylyltransferase A");
			allNames.add("Galactose-1-phosphate uridylyltransferase");
			allNames.add("GMP synthase [glutamine-hydrolyzing]");

			return allNames;
		}

		@Override
		public Phase getPhase() {
			return Phase.PHASE_2;
		}

	},
	

	
	;
	

	public String getEnzymeFamily() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public static Boolean checkIfEnzymeMatches(final Phase phase, Set<Enzymes> enzymes) {
		Boolean enzymeMatchesArtificialPhase = false;
		for (Enzymes enzyme : enzymes) {
			if (phase.getPhaseName().equalsIgnoreCase(enzyme.getEnzymeFamily())) {
				enzymeMatchesArtificialPhase = true;
				break;
			}
		}
		return enzymeMatchesArtificialPhase;
	}
	
}
