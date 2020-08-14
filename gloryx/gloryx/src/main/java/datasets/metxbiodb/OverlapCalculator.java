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

package main.java.datasets.metxbiodb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.datasets.drugbankdata.MetabolismDataLoader;
import main.java.utils.molecule.BasicMolecule;
import main.java.utils.Filenames;
import main.java.utils.analysis.GenericOverlapCalculator;
import main.java.utils.Phase;

/**
 * Calculated and logs overlap between MetXBioDB and DrugBank datasets.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class OverlapCalculator {
	// calculates the overlap between the MetXBioDB and DrugBank datasets.

	private static final String SIZE_OF_OVERLAP = "Size of overlap of MetXBioDB with DrugBank: {}";
	private static final String OVERLAP = "Overlap of MetXBioDB with DrugBank: {}";
	
	
	private static final Logger logger = LoggerFactory.getLogger(OverlapCalculator.class.getName());

	
	public static void main(String[] args) {
		
		String prefix = args[args.length-2];
		String phaseString = args[args.length-1];
		Phase phase = Phase.getPhaseFromString(phaseString);
		
		logger.info("Computing overlap for {}", phase.getPhaseName());
		
		Filenames filenames = new Filenames(prefix);
		MetXBioDBDatasetLoader metxbiodbLoader = new MetXBioDBDatasetLoader(true);  // remove stereochemistry
		Map<BasicMolecule, Set<BasicMolecule>> metxbiodbData = metxbiodbLoader.loadData(filenames, phase);
	
		// read in DrugBank approved drugs sdf, get SMILES and convert to set of InChIs
		Map<BasicMolecule, Set<BasicMolecule>> drugbankData = new HashMap<>();
		MetabolismDataLoader dloader = new MetabolismDataLoader();
		dloader.loadMetabolismData(filenames, drugbankData, phase);
		
		// Get overlap between MetXBioDB and DrugBank datasets - change order when calling to change direction of comparison. The first one listed will be used as the reference.
		// Note neither the MetXBioDB nor the DrugBank data structure has stereochem info in the InChIs currently.
		Set<String> overlapDrugbankIds = GenericOverlapCalculator.getOverlapBetweenOtherDatasetAndReferenceDatasetWithoutChangingInchi(drugbankData, metxbiodbData);	
		logger.debug(OVERLAP, overlapDrugbankIds);
		logger.debug(SIZE_OF_OVERLAP, overlapDrugbankIds.size());
	}

	
	
	
}
