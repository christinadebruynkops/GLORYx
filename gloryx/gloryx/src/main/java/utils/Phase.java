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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.utils.molecule.BasicMolecule;

/**
 * This enum is used to keep track of the metabolism phase information.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum Phase {
	
	PHASE_1 {
		
		@Override
		public String getFame3ModelName() {
			return "P1";
		}
		
		@Override
		public int getPhaseNumber() {
			return 1;
		}
		
		@Override
		public String toString() {
			return "phase I";
		}
	},
	
	PHASE_2 {
		
		@Override
		public String getFame3ModelName() {
			return "P2";
		}
		
		@Override
		public int getPhaseNumber() {
			return 2;
		}
		
		@Override
		public String toString() {
			return "phase II";
		}
	},
	
	PHASES_1_AND_2 {
		
		@Override
		public String getFame3ModelName() {
			return "P1+P2";
		}
		
		@Override
		public String toString() {
			return "phase I and phase II";
		}
	},
	
	
	
	// special cases for individual reaction type prediction for phase 2 metabolism
	
	UGT {
		
		@Override
		public String getFame3ModelName() {
			return "glucuronidation";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	GST {
		
		@Override
		public String getFame3ModelName() {
			return "gshconjugation";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	MT {
		
		@Override
		public String getFame3ModelName() {
			return "methylation";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	SULT {
		
		@Override
		public String getFame3ModelName() {
			return "sulfonation";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	NAT {
		
		@Override
		public String getFame3ModelName() {
			return "acetylation";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	OTHER_PHASE2 {
		
		@Override
		public String getFame3ModelName() {
			return "phaseII";
		}
		
		@Override
		public String getPhaseName() {
			return name();
		}
		
		@Override
		public Boolean isSpecificEnzymeFamily() {
			return true; 
		}
	},
	
	
	
	
	
	;	
	
	private static final Logger logger = LoggerFactory.getLogger(Phase.class.getName());

	
	public String getPhaseName() {
		return name().toLowerCase().replace("_", " ");
	}

	public String getFame3ModelName() {
		return "P1+P2"; // since this is the default
	}
	
	public Boolean isSpecificEnzymeFamily() {
		return false; // careful!
	}
	
	

	public static Boolean isPhase1(Phase phase) {
		return (phase == PHASE_1 || phase == Phase.PHASES_1_AND_2);
	}
	
	public static Boolean isOnlyPhase1(Phase phase) {
		return (phase == PHASE_1);
	}
	
	public static Boolean isPhase2(Phase phase) {
		return (phase == PHASE_2 || phase == Phase.PHASES_1_AND_2);
	}
	
	public static Boolean isOnlyPhase2(Phase phase) {
		return (phase == PHASE_2);
	}
	
	public static Boolean phasesMatch(Phase p1, Phase p2) {
		return ( (isPhase1(p1) && isPhase1(p2)) || (isPhase2(p1) && isPhase2(p2)) );
	}
	
	public static Boolean phasesStrictlyMatch(Phase p1, Phase p2) {
		return ( (isOnlyPhase1(p1) && isOnlyPhase1(p2)) || (isOnlyPhase2(p1) && isOnlyPhase2(p2)) );
	}
	
	
	public static Phase combinePhases(BasicMolecule m1, BasicMolecule m2) {
				
		if (phasesStrictlyMatch(m1.getMetabolismPhase(), m2.getMetabolismPhase())) {
			return m1.getMetabolismPhase();
			
		} else { // options are p1+p2, p1+combined, or p2+combined
			logger.warn("Duplicate metabolites have different phase annotations!!");
			return PHASES_1_AND_2;
		} 
	}
	
	public static Phase getPhaseFromString(String phaseString) {
		Phase phase;
		switch (phaseString) {
			case "P1": phase = PHASE_1;
				break;
			case "P2": phase = PHASE_2;
				break;
			case "P1+P2": phase = PHASES_1_AND_2;
				break;
				
			// reaction type-specific models for phase 2 metabolism
			case "UGT": phase = UGT;
				break;
			case "GST" : phase = GST;
				break;
			case "MT" : phase = MT;
				break;
			case "NAT" : phase = NAT;
				break;
			case "SULT" : phase = SULT;
				break;
				
			default: phase = PHASES_1_AND_2; // this is fine as the default because the argument parser takes care of invalid user input
            		break;
		}
		return phase;
	}

	public int getPhaseNumber() {

		return 0;
	}
	
}
