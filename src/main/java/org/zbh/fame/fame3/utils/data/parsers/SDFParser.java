package org.zbh.fame.fame3.utils.data.parsers;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.zbh.fame.fame3.globals.Globals;
import smartcyp.SMARTSnEnergiesTable;
import org.zbh.fame.fame3.utils.MoleculeKUFAME;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SDFParser implements FAMEFileParser {

    private String sdf_path;
    private InputStream input_stream;
    private String prefix;
    private IIteratingChemObjectReader reader;
    private int counter;
    private List<FAMEFileParserException> errors;

    public SDFParser(String sdf_path) {
        this.sdf_path = sdf_path;
        this.input_stream = null;
        this.setNamePrefix("SDF_");
        this.reader = null;
        this.counter = 0;
        this.errors = new ArrayList<>();
    }

    public SDFParser(String sdf_path, String prefix) {
        this(sdf_path);
        this.setNamePrefix(prefix);
    }

    public SDFParser(String sdf_path, InputStream is) {
        this.sdf_path = sdf_path;
        this.input_stream = is;
        this.setNamePrefix("SMIFile_");
        this.reader = null;
        this.counter = 0;
        this.errors = new ArrayList<>();
    }

    public SDFParser(String sdf_path, InputStream is, String prefix) {
        this(sdf_path, is);
        this.setNamePrefix(prefix);
    }

    @Override
    public void setNamePrefix(String prefix) {
        this.prefix = prefix;
    }

    private void initReader() throws FileNotFoundException {
        try {
            if (input_stream == null) {
                input_stream = new FileInputStream(sdf_path);
            }
            reader = new IteratingSDFReader(
                    input_stream
                    , DefaultChemObjectBuilder.getInstance()
            );
            counter = 0;
        } catch (FileNotFoundException e) {
            String message = "Input SDF could not be read:" + getFilePath();
            System.err.println(message);
            this.errors.add(new FAMEFileParserException(message, e));
            e.printStackTrace();
            throw e;
        }
    }

    private void destroyReader() {
        reader = null;
        counter = 0;
    }

    @Override
    public synchronized List<IAtomContainer> getMols() {
        try {
            initReader();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }

        List<IAtomContainer> molecules = new ArrayList<>();
        IAtomContainer mol = getNext();
        while (mol != null) {
            molecules.add(mol);
            mol = getNext();
        }

        destroyReader();
        return molecules;
    }

    @Override
    public synchronized IAtomContainer getNext() {
        if (reader == null) {
            try {
                initReader();
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        if (reader.hasNext()) {
            IAtomContainer molecule = (IAtomContainer) reader.next();
            SmilesGenerator smi_gen = new SmilesGenerator();
            if (molecule.getProperty(Globals.ID_PROP) == null) {
                molecule.setProperty(Globals.ID_PROP, molecule.getProperty("Identifier"));
                if (molecule.getProperty(Globals.ID_PROP) == null) {
                    molecule.setProperty(Globals.ID_PROP, prefix + counter++);
                    System.err.println("WARNING: No SDF name field found for molecule:\n" + smi_gen.createSMILES(molecule) + ".\nUsing a generated name: " + molecule.getProperty(Globals.ID_PROP));
                }
            } else {
//				System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
                molecule.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP).toString().replaceAll("[^A-Za-z0-9]", "_"));
            }
            try {
                MoleculeKUFAME mol_ku = new MoleculeKUFAME(molecule, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
                mol_ku.setProperties(molecule.getProperties());
                System.out.println("Successfully parsed structure from SDF for: " + molecule.getProperty(Globals.ID_PROP));
                mol_ku.setProperty(Globals.FILE_PATH_PROP, getFilePath());
                return mol_ku;
            } catch (CloneNotSupportedException e) {
                this.errors.add(new FAMEFileParserException(e, molecule.getProperty(Globals.ID_PROP).toString(), getFilePath()));
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        if (reader == null) {
            try {
                initReader();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
        return reader.hasNext();
    }

    @Override
    public String getFilePath() {
        return this.sdf_path;
    }

    @Override
    public List<FAMEFileParserException> getErrors() {
        return errors;
    }
}
