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
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
	private static final Logger logger = LoggerFactory.getLogger(SDFParser.class.getName());


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
            logger.error(message);
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
                    logger.warn("WARNING: No SDF name field found for molecule:\n" + smi_gen.createSMILES(molecule) + ".\nUsing a generated name: " + molecule.getProperty(Globals.ID_PROP));
                }
            } else {
//				System.out.println("Reading " + molecule.getProperty(Globals.ID_PROP));
                molecule.setProperty(Globals.ID_PROP, molecule.getProperty(Globals.ID_PROP).toString().replaceAll("[^A-Za-z0-9]", "_"));
            }
            try {
                MoleculeKUFAME mol_ku = new MoleculeKUFAME(molecule, new SMARTSnEnergiesTable().getSMARTSnEnergiesTable());
                mol_ku.setProperties(molecule.getProperties());
                logger.debug("Successfully parsed structure from SDF for: " + molecule.getProperty(Globals.ID_PROP));
                mol_ku.setProperty(Globals.FILE_PATH_PROP, getFilePath());
                return mol_ku;
            } catch (CloneNotSupportedException e) {
                this.errors.add(new FAMEFileParserException(e, molecule.getProperty(Globals.ID_PROP).toString(), getFilePath()));
                logger.error("Error parsing molecule.", e);
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
            		logger.error("File not found.", e);
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
