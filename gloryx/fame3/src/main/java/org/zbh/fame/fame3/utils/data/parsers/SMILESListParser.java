/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
   Copyright (C) 2013  Johannes Kirchmair <johannes.kirchmair@univie.ac.at>
 
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

package org.zbh.fame.fame3.utils.data.parsers;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbh.fame.fame3.globals.Globals;

import smartcyp.SMARTSnEnergiesTable;
import org.zbh.fame.fame3.utils.MoleculeKUFAME;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SMILESListParser implements FAMEFileParser {
    private List<String> smiles;
    private List<String> names;
    private String prefix;
    private List<IAtomContainer> molecules;
    private List<FAMEFileParserException> errors;
    
	private static final Logger logger = LoggerFactory.getLogger(SMILESListParser.class.getName());


    public SMILESListParser(List<String> smiles) {
        this.smiles = new LinkedList<>(smiles);
        this.names = null;
        this.errors = new LinkedList<>();
        this.prefix = "SMIList_";
        this.molecules = null;
    }

    public SMILESListParser(List<String> smiles, List<String> names) throws InputMismatchException {
        this.prefix = "SMIList_";
        this.names = new LinkedList<>(names);
        this.smiles = new LinkedList<>(smiles);
        if (this.names.size() != this.smiles.size()) {
            throw new InputMismatchException("The list of SMILES does not have the same size as the list of names.");
        }
        this.errors = new LinkedList<>();
        this.molecules = null;
    }

    public SMILESListParser(List<String> smiles, String prefix) {
        this(smiles);
        this.setNamePrefix(prefix);
    }

    public SMILESListParser(List<String> smiles, List<String> names, String prefix) {
        this(smiles, names);
        this.setNamePrefix(prefix);
    }

    @Override
    public void setNamePrefix(String prefix) {
        this.prefix = prefix;
        if (this.molecules != null) {
            this.molecules = getMols();
        }
    }

    @Override
    public synchronized List<IAtomContainer> getMols() {
        ArrayList<IAtomContainer> molecules = new ArrayList<>();
        int counter = 1;
        for (String smiles : smiles) {
            IAtomContainer mol = null;
//				smiles = smiles.replaceAll("[^\\-=#.$:()%+A-Za-z0-9\\\\/@\\]\\[]", "");
            smiles = smiles.replaceAll("[\"']", "");
            try {
                SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
                mol = sp.parseSmiles(smiles);
            } catch (InvalidSmilesException ise) {
                String message = "WARNING: SMILES parsing failed for: " + smiles;
                logger.error(message);
                this.errors.add(new FAMEFileParserException(message, ise, smiles, smiles));
                ise.printStackTrace();
                continue;
            }

            if (this.names == null) {
                mol.setProperty(Globals.ID_PROP, prefix + counter);
            } else {
                mol.setProperty(Globals.ID_PROP, prefix + names.get(counter - 1));
            }
            counter++;

            logger.debug("Generating identifier for " + smiles + ": " + mol.getProperty(Globals.ID_PROP));
            try {
                MoleculeKUFAME mol_ku = new MoleculeKUFAME(mol, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
                mol_ku.setProperties(mol.getProperties());
                mol_ku.setProperty(Globals.FILE_PATH_PROP, getFilePath());
                molecules.add(mol_ku);
            } catch (CloneNotSupportedException e) {
                this.errors.add(new FAMEFileParserException(e, smiles, smiles));
                logger.error("Error parsing molecule.", e);
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
            if (names != null) {
                names.remove(molecules.size() - 1);
            }
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
