package org.zbh.fame.fame3.utils.data.parsers;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.zbh.fame.fame3.globals.Globals;
import org.zbh.fame.fame3.smartcyp.SMARTSnEnergiesTable;
import org.zbh.fame.fame3.utils.MoleculeKUFAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SMILESFileParser implements FAMEFileParser {

    private File input_smi;
    private String prefix;
    private IIteratingChemObjectReader reader;
    private int counter;

    public SMILESFileParser(String input_smi) throws FileNotFoundException {
        this.input_smi = new File(input_smi);
        if (!this.input_smi.exists()) {
            throw new FileNotFoundException("Specified input SMILES file does not exist: " + getFilePath());
        }
        this.setNamePrefix("SMIFile_");
        this.reader = null;
        this.counter = 0;
    }

    public SMILESFileParser(String input_smi, String prefix) throws FileNotFoundException {
        this(input_smi);
        this.setNamePrefix(prefix);
    }

    @Override
    public void setNamePrefix(String prefix) {
        this.prefix = prefix;
    }

    private void initReader() {
        try {
            reader = new IteratingSMILESReader(
                    new FileInputStream(input_smi)
                    , DefaultChemObjectBuilder.getInstance()
            );
            this.counter = 0;
        } catch (FileNotFoundException e) {
            System.err.println("Input SMILES file could not be read and it will be skipped: " + getFilePath());
            e.printStackTrace();
        }
    }

    private void destroyReader() {
        reader = null;
        this.counter = 0;
    }

    @Override
    public synchronized List<IAtomContainer> getMols() {
        initReader();

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
            initReader();
        }

        if (reader.hasNext()) {
            IMolecule molecule = (IMolecule) reader.next();
            SmilesGenerator smi_gen = new SmilesGenerator();
            if (molecule.getProperty(Globals.ID_PROP) == null) {
                molecule.setProperty(Globals.ID_PROP, molecule.getProperty("Identifier"));
                if (molecule.getProperty(Globals.ID_PROP) == null) {
                    molecule.setProperty(Globals.ID_PROP, prefix + counter++);
                    System.err.println("WARNING: No name found in the SMILES file for molecule with SMILES:\n" + smi_gen.createSMILES(molecule) + ".\nUsing a generated name: " + molecule.getProperty(Globals.ID_PROP));
                }
            } else {
//				System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
                molecule.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP).toString().replaceAll("[^A-Za-z0-9]", "_"));
            }
            try {
                MoleculeKUFAME mol_ku = new MoleculeKUFAME(molecule, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
                mol_ku.setProperties(molecule.getProperties());
                System.out.println("Successfully parsed structure from SMILES for: " + molecule.getProperty(Globals.ID_PROP));
                mol_ku.setProperty(Globals.FILE_PATH_PROP, getFilePath());
                return mol_ku;
            } catch (CloneNotSupportedException e) {
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
            initReader();
        }
        return reader.hasNext();
    }

    @Override
    public String getFilePath() {
        return this.input_smi.getAbsolutePath();
    }
}
