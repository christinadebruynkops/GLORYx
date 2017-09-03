# Fame2

This is *fame2*, a program which attempts to predict sites of metabolism for supplied chemical compounds. 
It includes extra trees models for regioselectivity prediction of some cytochrome P450 isoforms.

## Usage

On the Linux platform and Mac, running the program is easy since
you can use the shell script provided in the installation directory:

```bash
cd ${YOUR_INSTALL_DIR}/fame2
./fame2
```

You can also add `${YOUR_INSTALL_DIR}` to the `$PATH` 
environment variable to have universal access:

```bash
export PATH="$PATH:$YOUR_INSTALL_DIR"
```

There is an example SDF file in the installation folder
that you can use to test the program:

```bash
fame2 -o "test_predictions" "${YOUR_INSTALL_DIR}/example_compounds/tamoxifen.sdf"
```

The program also accept SMILES strings:

```bash
fame2 -o "test_predictions" -s CCO c1ccccc1C
```

This creates the `test_predictions` folder in the current directory
which contains the predictions and other output files (if required)
for each analyzed compound.

On other platforms, you will have to run the java package explicitly. 
For example:

```
java -Xms1024m -jar ${YOUR_INSTALL_DIR}/fame2.jar -o 'test_predictions' "${YOUR_INSTALL_DIR}/example_compounds/tamoxifen.sdf"
```

Since the unpacked model takes quite a bit of memory the `-Xms1024m`
flags are often necessary to override some default java options.

You can find more instructions and tips on how to use the package by running: 

```bash
fame2 -h
```

## Contact Information

 - Martin Šícho - [martin.sicho@vscht.cz](mailto::martin.sicho@vscht.cz)
    - CZ-OPENSCREEN: National Infrastructure for Chemical Biology, Laboratory of Informatics and Chemistry, Faculty of Chemical Technology, University of Chemistry and Technology Prague, 166 28 Prague 6, Czech Republic
 - Johannes Kirchmair - [kirchmair@zbh.uni-hamburg.de](mailto::kirchmair@zbh.uni-hamburg.de)
    - Universität Hamburg, Faculty of Mathematics, Informatics and Natural Sciences, Department of Computer Science, Center for Bioinformatics, Hamburg, 20146, Germany
   
## Acknowledgement & Funding

We would like to express our thank you to Jed Zaretzki and his collaborators for compiling the freely available
dataset which was used to develop the models in this software. We also highly appreciate the help of Patrik
Rydberg and his collaborators who made their visualization code from the 
[SMARTCyp program](http://www.farma.ku.dk/smartcyp/background.php) freely available
as open source software.

This work was funded by the Deutsche Forschungsgemeinschaft (DFG, German Research Foundation) - project number KI 2085/1-1 and by the Ministry of Education of the Czech Republic - project numbers MSMT No 20-SVV/2017, NPU I - LO1220 and LM2015063. MS was supported by the Erasmus+ Programme of the European Commission.