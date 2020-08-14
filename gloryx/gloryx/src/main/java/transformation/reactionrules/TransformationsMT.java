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
 * This enum is the "MT" subset of the phase II reaction rule set from SyGMa. 
 * The reaction rules correspond to metabolic reactions mediated by methyltransferases.
 * The SMIRKS were converted by hand from the reaction SMARTS provided by SyGMa.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public enum TransformationsMT implements Transformations {
	
	// WARNING!!!!!!!!!! THIS CONTENT IS ALSO CONTAINED in SyGMaTransformationsPhaseII.java. NEVER make changes to SMIRKS unless you make them in both files!!!

	
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
