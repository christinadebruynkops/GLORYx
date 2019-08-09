# FAME 3

This is the *fame3* program. It attempts to predict sites of metabolism (SOMs)
for the supplied chemical compounds. It is based on extra trees classifier trained 
for prediction of both phase I and phase II SOMs from the MetaQSAR database [1]. It contains 
a combined phase I and phase II (P1+P2) 
model and separate phase I (P1) and phase II (P2) models. For more details 
on the FAME 3 method, see the FAME 3 publication [2].

*IMPORTANT*: If this file is packaged with a source code distribution of this software, 
note that the FAME 3 models are not bundled with it, but need to be obtained from the authors of the 
original work [2]. The models are available free of charge for non-profit use or
a license to use the models in for-profit organisations can be issued by the authors. 

1. MetaQSAR: An Integrated Database Engine to Manage and Analyze Metabolic Data
   Alessandro Pedretti, Angelica Mazzolari, Giulio Vistoli, and Bernard Testa
   Journal of Medicinal Chemistry 2018 61 (3), 1019-1030
   DOI: 10.1021/acs.jmedchem.7b01473

2. FAME 3: Predicting the Sites of Metabolism in Synthetic Compounds and Natural Products for Phase 1 and Phase 2 Metabolic Enzymes
   Martin Šícho, Conrad Stork, Angelica Mazzolari, Christina de Bruyn Kops, Alessandro Pedretti, Bernard Testa, Giulio Vistoli, Daniel Svozil, and Johannes Kirchmair
   Journal of Chemical Information and Modeling Just Accepted Manuscript
   DOI: 10.1021/acs.jcim.9b00376

## Installation instructions

There is no installation needed for the software. Just unpack the archive 
into your desired installation directory. For example, on a Linux system this can be done
with the `tar` command:

```bash
tar -xzf fame3-${version}-bin.tar.gz ${YOUR_INSTALL_DIR}
```

The FAME 3 binary and the required dependencies will then become available in the installation directory.

## Usage Examples

### Linux and Mac

On Linux and Mac, running the program is easy since
you can use the shell script provided in the installation directory:

```bash
cd ${YOUR_INSTALL_DIR}/fame2
./fame3
```

You can also add `${YOUR_INSTALL_DIR}` to the `$PATH` 
environment variable to have universal access:

```bash
export PATH="$PATH:$YOUR_INSTALL_DIR"
```

There is a folder with example SDF files in the installation folder
that you can use to test the program:

```bash
fame3 -o "test_predictions" "${YOUR_INSTALL_DIR}/examples/tamoxifen.sdf"
```

The program also accepts SMILES strings:

```bash
fame3 -o "test_predictions" -s "O=C3OC4OC1(OOC42C(CC1)C(C)CCC2C3C)C"
```

Multiple SMILES can also be specified:

```bash
fame3 -o "test_predictions" -s "O=C3OC4OC1(OOC42C(CC1)C(C)CCC2C3C)C" "CN(C)CCOc1ccc(cc1)/C(c2ccccc2)=C(/CC)c3ccccc3"
```

And SDF input can be combined with SMILES input as well:

```bash
fame3 -o "test_predictions" "${YOUR_INSTALL_DIR}/examples/tamoxifen.sdf" -s "O=C3OC4OC1(OOC42C(CC1)C(C)CCC2C3C)C" "CN(C)CCOc1ccc(cc1)/C(c2ccccc2)=C(/CC)c3ccccc3"
```

All the inputs above will generate the `test_predictions` 
folder in the current directory. This folder will contain
subfolders named after the input molecules (molecules specified as SMILES will 
have a name generated for them). The FAME 3
output files for each compound will then be located in the respective subdirectory.

More information on running the program is available in the help menu:

```bash
fame3 -h
```

### Windows

On Windows, there is no batch script at the moment. 
You will have to replace the `fame3` call and run the java package explicitly:

```
java -Xmx16g -jar ${YOUR_INSTALL_DIR}/fame3.jar -o 'test_predictions' "${YOUR_INSTALL_DIR}/examples/tamoxifen.sdf"
```
 
Since the unpacked FAME 3 model and the FAMEscore models can take 
quite a bit of memory the `-Xmx16g` flag is necessary.

Otherwise, *fame3* functions as outlined above for Linux and Mac.

## Contact Information

 - Martin Šícho - [martin.sicho@vscht.cz](mailto::martin.sicho@vscht.cz)
    - CZ-OPENSCREEN: National Infrastructure for Chemical Biology, Laboratory of Informatics and Chemistry, Faculty of Chemical Technology, University of Chemistry and Technology Prague, 166 28 Prague 6, Czech Republic
 - Johannes Kirchmair - [kirchmair@zbh.uni-hamburg.de](mailto::kirchmair@zbh.uni-hamburg.de)
    - Universität Hamburg, Faculty of Mathematics, Informatics and Natural Sciences, Department of Computer Science, Center for Bioinformatics, Hamburg, 20146, Germany
   
## Acknowledgement & Funding

We highly appreciate the help of Patrik
Rydberg and his collaborators who made their visualization code from the 
SMARTCyp program freely available as open source software.

This work was funded by the Deutsche Forschungsgemeinschaft (DFG, German Research Foundation) - project number KI 2085/1-1, Bergen Research Foundation (BFS) - grant no. BFS2017TMT01,
and by the Ministry of Education of the Czech Republic - project numbers MSMT No 21-SVV/2018, LM2015063 and by RVO 68378050-KAV-NPUI.

## Disclaimer

This software is based on a number of third-party dependencies that are listed in the attached 
`NOTICE`, which also includes their licensing information and 
links to websites where original copies of the software can be obtained. The source code 
of the third-party libraries was not modified with the important exception of the SMARTCyp software, 
which was integrated into the FAME 3 codebase as obtained through the SMARTCyp website mentioned in the 
[original publication](https://academic.oup.com/bioinformatics/article/26/23/2988/221339). 
The SMARTCyp code was slightly adapted 
in order to work well with the FAME 3 software and the changes are tracked in 
the [GitHub repository](TODO: add link).