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

package main.java.utils.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.transformation.Transformations;
import main.java.transformation.reactionrules.GLORYTransformations;
import main.java.transformation.reactionrules.SyGMaTransformationsPhaseI;
import main.java.transformation.reactionrules.TransformationsGST;
import main.java.transformation.reactionrules.TransformationsMT;
import main.java.transformation.reactionrules.TransformationsNAT;
import main.java.transformation.reactionrules.TransformationsOtherPhaseII;
import main.java.transformation.reactionrules.TransformationsSULT;
import main.java.transformation.reactionrules.TransformationsUGT;

/**
 * This class was created specifically to write the CSV file of reaction rules for the supporting information of the GLORYx paper.
 * Note that the relevant rule set information is hard-coded specifically for this purpose.
 * The output filename is also hard-coded.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class TransformationWriter {
	
	private static final String CURRENT_WORK = "Current work";
	private static final String GLORY = "GLORY";
	private static final String SYGMA = "SyGMa";
	private static final Logger logger = LoggerFactory.getLogger(TransformationWriter.class.getName());


	public static void main(String[] args) {
		
		String outfilename = "/Users/kops/zbhmount/metaboliteproject/gloryx_comprehensive/rulesets/gloryx_reactionrules.csv"; // filename should end in csv
		
		if (!outfilename.endsWith(".csv")) {
			logger.warn("WARNING: A CSV file will be written, but the filename you've provided does not end with .csv.");
		}
		
		List<Transformations[]> allRules = new ArrayList<>();
		List<String> rulesetNames = new ArrayList<>();
		List<String> rulesetSources = new ArrayList<>();
		assembleAllRules(allRules, rulesetNames, rulesetSources);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfilename))){
			
			writer.write("Reaction name,SMIRKS,Priority level,Name of rule subset,Rule source\n");
			
			int counter = 0;
			for (Transformations[] ruleSet : allRules) { 
				
				String rulesetName = rulesetNames.get(counter);
				String rulesetSource = rulesetSources.get(counter);
				
				counter ++;
				
				for (Transformations rule : ruleSet) {
					
					writer.write('"' + rule.getName() + '"' + "," + '"' + rule.getSMIRKS() + '"' + "," + rule.getPriorityLevel().name().toLowerCase() + "," + rulesetName + "," + rulesetSource + "\n");
				}
			}
		} catch (IOException e) {
			logger.error("Error writing file", e);
		}
	}

	private static void assembleAllRules(List<Transformations[]> allRules, List<String> rulesetNames, List<String> rulesetSources) {
		Transformations[] p1_sygma = SyGMaTransformationsPhaseI.values();
		Transformations[] p1_glory = GLORYTransformations.values();
		Transformations[] p2_gst = TransformationsGST.values();
		Transformations[] p2_mt = TransformationsMT.values();
		Transformations[] p2_nat = TransformationsNAT.values();
		Transformations[] p2_sult = TransformationsSULT.values();
		Transformations[] p2_ugt = TransformationsUGT.values();
		Transformations[] p2_other = TransformationsOtherPhaseII.values();
		
		// This is the order the rule subsets will appear in
		allRules.add(p1_sygma);
		allRules.add(p1_glory);
		allRules.add(p2_ugt);
		allRules.add(p2_sult);
		allRules.add(p2_gst);
		allRules.add(p2_nat);
		allRules.add(p2_mt);
		allRules.add(p2_other);
		
		rulesetNames.add("Phase 1 SyGMa rules");
		rulesetNames.add("CYP rules from GLORY (phase 1)");
		rulesetNames.add("UGT rules (phase 2)");
		rulesetNames.add("SULT rules (phase 2)");
		rulesetNames.add("GST rules (phase 2)");
		rulesetNames.add("NAT rules (phase 2)");
		rulesetNames.add("MT rules (phase 2)");
		rulesetNames.add("Other phase 2 rules");
		
		rulesetSources.add(SYGMA);
		rulesetSources.add(GLORY);
		rulesetSources.add(SYGMA);
		rulesetSources.add(SYGMA);
		rulesetSources.add(CURRENT_WORK);
		rulesetSources.add(SYGMA);
		rulesetSources.add(SYGMA);
		rulesetSources.add(SYGMA);
	}

}
