package org.zbh.fame.fame3.utils.data.parsers;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.zbh.fame.fame3.globals.Globals;
import smartcyp.SMARTSnEnergiesTable;
import org.zbh.fame.fame3.utils.MoleculeKUFAME;

import java.util.ArrayList;
import java.util.List;

public class SMILESListParser implements FAMEFileParser {
    private List<String> smiles;
    private String prefix;
    private List<IAtomContainer> molecules;
    private List<FAMEFileParserException> errors;

    public SMILESListParser(List<String> smiles) {
        this.smiles = smiles;
        this.errors = new ArrayList<>();
        this.prefix = "SMIList_";
        this.molecules = null;
    }

    public SMILESListParser(List<String> smiles, String prefix) {
        this(smiles);
        this.setNamePrefix(prefix);
    }

    @Override
    public void setNamePrefix(String prefix) {
        this.prefix = prefix;
        this.molecules = getMols();
    }

    @Override
    public synchronized List<IAtomContainer> getMols() {
        ArrayList<IAtomContainer> molecules = new ArrayList<>();
        int counter = 1;
        for (String smiles : smiles) {
            IMolecule mol = null;
//				smiles = smiles.replaceAll("[^\\-=#.$:()%+A-Za-z0-9\\\\/@\\]\\[]", "");
            smiles = smiles.replaceAll("[\"']", "");
            try {
                SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
                mol = sp.parseSmiles(smiles);
            } catch (InvalidSmilesException ise) {
                String message = "WARNING: SMILES parsing failed for: " + smiles;
                System.err.println(message);
                this.errors.add(new FAMEFileParserException(message, ise, smiles, smiles));
                ise.printStackTrace();
                continue;
            }

            mol.setProperty(Globals.ID_PROP, prefix + counter++);
            System.out.println("Generating identifier for " + smiles + ": " + mol.getProperty(Globals.ID_PROP));
            try {
                MoleculeKUFAME mol_ku = new MoleculeKUFAME(mol, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
                mol_ku.setProperties(mol.getProperties());
                mol_ku.setProperty(Globals.FILE_PATH_PROP, getFilePath());
                molecules.add(mol_ku);
            } catch (CloneNotSupportedException e) {
                this.errors.add(new FAMEFileParserException(e, smiles, smiles));
                e.printStackTrace();
            }
        }

        this.molecules = molecules;
        return molecules;
    }

    @Override
    public synchronized IAtomContainer getNext() {
        if (molecules == null) {
            molecules = getMols();
        }

        if (!molecules.isEmpty()) {
            smiles.remove(molecules.size() - 1);
            return molecules.remove(molecules.size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        if (molecules == null) {
            molecules = this.getMols();
        }
        return !molecules.isEmpty();
    }

    @Override
    public String getFilePath() {
        return "";
    }

    @Override
    public List<FAMEFileParserException> getErrors() {
        return errors;
    }
}
