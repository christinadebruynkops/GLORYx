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

package org.zbh.fame.fame3.utils.data;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.zbh.fame.fame3.utils.data.parsers.FAMEFileParser;
import org.zbh.fame.fame3.utils.data.parsers.FAMEFileParserException;

import java.util.ArrayList;
import java.util.List;

public class FAMEMolSupplier {

    private List<FAMEFileParser> parsers;
    private FAMEFileParser current_parser;
    private List<FAMEFileParserException> errors;

    public FAMEMolSupplier(FAMEFileParser parser) {
        parsers = new ArrayList<>();
        errors = new ArrayList<>();
        current_parser = parser;
    }

    public FAMEMolSupplier(List<FAMEFileParser> parsers) {
        this.parsers = parsers;
        if (this.parsers.isEmpty()) {
            throw new IllegalArgumentException("The supplied list of file parsers cannot be empty.");
        }
        this.errors = new ArrayList<>();
        this.current_parser = this.parsers.remove(parsers.size() - 1);
    }

    public synchronized IAtomContainer getNext() {
        if (current_parser.hasNext()) {
            return current_parser.getNext();
        } else if (this.hasNext()) {
            return current_parser.getNext();
        } else {
            return null;
        }
    }

    public boolean hasNext() {
        if (current_parser.hasNext()) {
            return true;
        } else if (!parsers.isEmpty()){
            while (!parsers.isEmpty() && !current_parser.hasNext()) {
                errors.addAll(current_parser.getErrors());
                current_parser = parsers.remove(parsers.size() - 1);
            }
            if (!current_parser.hasNext()) {
                errors.addAll(current_parser.getErrors());
                return false;
            } else {
                return true;
            }
        } else {
            errors.addAll(current_parser.getErrors());
            return false;
        }
    }

    public List<FAMEFileParserException> getErrors() {
        return errors;
    }
}
