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

/**
 * Keeps track of all test parameters.
 * Many parameters are hard-coded, because they were varied during evaluation but are fixed in the user version.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class TestParameters {

	private final Double somProbabilityCutoff;  // default = 0.2; see constructors
	
	private static final int METABOLITE_HEAVY_ATOM_CUTOFF = 3; // default = 3. metabolites with fewer heavy atoms than this are ignored.
	
	private static final Boolean USE_COMBINED_PHASE2 = false; // if true, use P2 fame model. if false, this will use the individual reaction class FAME 3 models for phase 2
	private static final Boolean USE_COMBINED_P1P2 = false; // if true, use P1+P2 fame model. if false, predict for phase 1 and phase 2 separately, then combine afterwards
	
	private static final Boolean USE_SYGMA_RULES_ONLY = false;
	
	private static final int BATCH_SIZE = 1000; // TODO should be 1000 for distribution
	
	
	private Boolean predictAllMetabolism = false;  

	private int numThreads;

	public enum UseSoMsAsHardFilter { 
		YES,  // use the SoMs predicted by FAME 2 as a hard filter for whether a transformation is allowed at a particular place in the molecule
		NO  // the SoMS predicted by FAME 2 will be used to score the predictions but not as a hard filter
	}
	private final UseSoMsAsHardFilter useSoMsAsHardFilter;
	
	public enum Version {
		USER,  // user version. doesn't compare to database
		EVALUATION  // private evaluation version
	}
	private final Version version;
	
	public enum UserVersion {
		WEB, // for webserver
		OFFLINE // for direct use by user
	}
	private final UserVersion userVersion;
	
	public enum Reference {
		DRUGBANK,  // DrugBank dataset (minus test dataset)
		DRUGBANK_PLUS_METXBIODB,  // Reference dataset = DrugBank combined with MetXBioDB (minus test dataset)
		TEST_DATASET,  // the manually curated test dataset created to validate this method
		NONE
	}
	private final Reference reference;
	
	// use enum instead of Boolean so that the constructor is easier to use (no chance of mixing up the order)
	public enum InputFormat {
		DATABASE,  // use all compounds from Metabolite or DrugBank (as specified by Reference) as parent compounds for the prediction
		INDIVIDUAL_SMILES,  // use individual SMILES specified by user or hard-coded
		SDFILE
	}
	private final InputFormat inputFormat;
	
	private Phase phase; 
	
	private Phase desiredPhase;
		
	
	private static final Logger logger = LoggerFactory.getLogger(TestParameters.class.getName());

	
	// constructors
	public TestParameters(UseSoMsAsHardFilter soms, Double somProbabilityCutoff, Version version, UserVersion userVersion, Reference reference, InputFormat inputFormat, Phase phase, int numThreads) {
		this.useSoMsAsHardFilter = soms;
		this.somProbabilityCutoff = somProbabilityCutoff;
		this.version = version;
		this.userVersion = userVersion;
		this.reference = reference;
		this.inputFormat = inputFormat;
		this.phase = phase;
		this.desiredPhase = phase;
		this.numThreads = numThreads;
		
		furtherConstruction(phase);

	}
	public TestParameters(UseSoMsAsHardFilter soms, Version version, UserVersion userVersion, Reference reference, InputFormat inputFormat, Phase phase, int numThreads) {
		this.useSoMsAsHardFilter = soms;
		this.somProbabilityCutoff = 0.2;
		this.version = version;
		this.userVersion = userVersion;
		this.reference = reference;
		this.inputFormat = inputFormat;
		this.phase = phase;
		this.desiredPhase = phase;
		this.numThreads = numThreads;

		furtherConstruction(phase);
	}
	
	
	private void furtherConstruction(Phase phase) {
		
		if (phase == Phase.PHASES_1_AND_2) {
			predictAllMetabolism = true;
		}
		
		if (useSygmaRulesOnly() && !useCombinedPhase2()) {
			logger.error("Using only SyGMa rules but not the combined phase 2 option will result in an undesired effect! "
					+ "The separate phase 2 option has precedence, so the GSH conjugation rules will be used as well.");
		}
	}
	
	
	// getters:
	
	public Boolean inputIsDatabase() {
		return (inputFormat == InputFormat.DATABASE);
	}
	
	public Boolean inputIsSdf() {
		return (inputFormat == InputFormat.SDFILE);
	}
	
	public Boolean inputIsIndividualSmiles() {
		return (inputFormat == InputFormat.INDIVIDUAL_SMILES);
	}
	
	public Boolean useSoMsAsHardFilter() {
		return (useSoMsAsHardFilter == UseSoMsAsHardFilter.YES);
	}
	
	public Double getSoMProbabilityCutoff() {
		return somProbabilityCutoff;
	}
	
	public int getMetaboliteNumberOfHeavyAtomsCutoff() {
		return METABOLITE_HEAVY_ATOM_CUTOFF;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public Boolean isUserVersion() {
		return (version == Version.USER);
	}
	
	public Boolean isWebVersion() {
		return (userVersion == UserVersion.WEB);
	}
	
	public Phase getPhase() {
		return phase;
	}
	
	public Phase getInputPhase() {
		return desiredPhase;
	}
	
	public Boolean predictPhase1() {
		return (phase == Phase.PHASE_1);
	}
	
	public Boolean predictPhase2() {
		return (phase == Phase.PHASE_2);
	}
	
	public Boolean predictAllMetabolism() {
		return this.predictAllMetabolism;
	}
	
	public int getNumThreads() {
		return this.numThreads;
	}
	
	public static Boolean useCombinedPhase2() { // this is an evaluation option, so it's hard-coded
		return USE_COMBINED_PHASE2;
	}
	
	public Boolean useCombinedP1P2() { // this is a user option, so not the getter is not static
		return USE_COMBINED_P1P2;
	}
	
	public static Boolean useSygmaRulesOnly() { // this is an evaluation option, so it's hard-coded
		return USE_SYGMA_RULES_ONLY;
	}
	
	public static int getBatchSize() {
		return BATCH_SIZE;
	}
	
	
	// setters
	
	public void setPhase(Phase phase) {
		this.phase = phase;
	}



}
