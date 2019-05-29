# FAME 3

This is the *fame3* program. It attempts to predict sites of metabolism (SOMs)
for the supplied chemical compounds. It is based on extra trees classifier trained 
for prediction of both phase I and phase II SOMs from the MetaQSAR database [1]. It contains 
a combined phase I and phase II (P1+P2) 
model and separate phase I (P1) and phase II (P2) models. For more details 
on the FAME 3 method, see the FAME 3 publication [2].

1. MetaQSAR: An Integrated Database Engine to Manage and Analyze Metabolic Data
   Alessandro Pedretti, Angelica Mazzolari, Giulio Vistoli, and Bernard Testa
   Journal of Medicinal Chemistry 2018 61 (3), 1019-1030
   DOI: 10.1021/acs.jmedchem.7b01473

2. TODO: add reference

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

This work was funded by the Deutsche Forschungsgemeinschaft (DFG, German Research Foundation) - project number KI 2085/1-1 
and by the Ministry of Education of the Czech Republic - project numbers MSMT No 20-SVV/2017, NPU I - LO1220 and LM2015063.