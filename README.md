# Fame2

This is *fame2*. It attempts to predict sites of metabolism for supplied chemical compounds. 
It includes extra trees models for regioselectivity prediction of some cytochrome P450 isoforms.

## Usage

On the Linux platform, running the program is easy since
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

This creates the `test_predictions` folder in the current directory
which contains the predictions 
and other output files for each analyzed compound.

On other platforms, you will have to run the java package explicitly. 
For example:

```
java -Xms1024m -Xmx2048m -jar ${YOUR_INSTALL_DIR}/fame2.jar -o 'test_predictions' "${YOUR_INSTALL_DIR}/example_compounds/tamoxifen.sdf"
```

Since the unpacked model takes quite a bit of memory the `-Xms1024m -Xmx2048m`
flags are often necessary to override some default java options.

## Contact

 - Martin Šícho -- martin.sicho@vscht.cz
   - CZ-OPENSCREEN: National Infrastructure for Chemical Biology, Laboratory of Informatics and Chemistry, Faculty of Chemical Technology, University of Chemistry and Technology Prague, 166 28 Prague 6, Czech Republic
 - Johannes Kirchmair -- kirchmair@zbh.uni-hamburg.de
   - Universität Hamburg, Faculty of Mathematics, Informatics and Natural Sciences, Department of Computer Science, Center for Bioinformatics, Hamburg, 20146, Germany