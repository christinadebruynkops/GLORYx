# GLORYx

## Prediction of Metabolites Formed by Phase I and Phase II Xenobiotic Metabolism

This is the GLORYx program. GLORYx predicts phase I and phase II metabolites for the chemical compound(s) provided by the user. The method is based on site of metabolism (SoM) prediction combined with sets of reaction rules encoding metabolic reactions. 

For more details on the GLORYx method, see the publication [1]. The SoM prediction method that is used in GLORYx is FAME 3 [2]. 

1. GLORYx: Prediction of the Metabolites Resulting from Phase 1 and Phase 2 Biotransformations of Xenobiotics. Christina de Bruyn Kops, Martin Šícho, Angelica Mazzolari, and Johannes Kirchmair.
   Chemical Research in Toxicolory, 2020.
   DOI: 10.1021/acs.chemrestox.0c00224

2. FAME 3: Predicting the Sites of Metabolism in Synthetic Compounds and Natural Products for Phase 1 and Phase 2 Metabolic Enzymes. 
   Martin Šícho, Conrad Stork, Angelica Mazzolari, Christina de Bruyn Kops, Alessandro Pedretti, Bernard Testa, Giulio Vistoli, Daniel Svozil, and Johannes Kirchmair.
   Journal of Chemical Information and Modeling, 2017, 57 (8), 1832–1846.
   DOI: 10.1021/acs.jcim.9b00376
   
## Contents of this Repository

This repository contains the code for GLORYx, which includes a modified version of the FAME 3 code. The FAME 3 code was modified to use CDK version 2.0 and to allow efficient incorporation of FAME 3 prediction into GLORYx.

*IMPORTANT*: Note that the FAME 3 models are not bundled with the source code. The models need to be obtained from the authors of FAME 3 [2]. The models are available free of charge for non-profit use or
a license to use the models in for-profit organizations can be issued by the authors.

Separate README files are provided for the datasets and the reaction rules, in the respective directories. In addition, separate NOTICE files are provided for the source code and for the reference dataset, in the respective directories.

## Acknowledgements

The development of GLORYx would not have been possible without the prior development of FAME 3. FAME 3 was developed by Martin Šícho and Johannes Kirchmair. 

Many of the reaction rules used in GLORYx were sourced from [SyGMa](https://pypi.org/project/SyGMa/), DOI: 10.1002/cmdc.200700312. All of the reaction rules from SyGMa were re-implemented in GLORYx because they had to be translated by hand from Reaction SMARTS to SMIRKS. Many other reaction rules were sourced from GLORY (previous work by the authors of GLORYx), DOI: 10.3389/fchem.2019.00402. The reaction rules can be found in the reaction_rules folder as well as in the transformation.reactionrules package in the source code.

Thanks also belong to Conrad Stork for contributing his chemistry knowledge during the development of the cytochrome P450 reaction rules.

## Disclaimer

This software is based on a number of third-party dependencies that are listed in the attached 
`NOTICE`, which also includes their licensing information and 
links to websites where original copies of the software can be obtained. 
The source code of FAME 3 was modified to use CDK version 2.0 and to allow efficient incorporation of FAME 3 prediction into GLORYx.

The source code of the third-party libraries was not modified except in two cases, both of which occurred during the previous development of FAME 3.
These exceptions were the SMARTCyp software
and some classes from the WEKA machine learning library (version 3.8). 
The SMARTCyp code was slightly adapted 
in order to work well with the FAME 3 software.
From the WEKA library, only the `LinearNNSearch` class
was modified for thread safety.
The SMARTCyp code was
 obtained through the SMARTCyp website mentioned in the 
[original publication](https://academic.oup.com/bioinformatics/article/26/23/2988/221339).
The source files to be modified from the WEKA library were obtained from the 
[GitHub mirror](https://github.com/Waikato/weka-3.8).