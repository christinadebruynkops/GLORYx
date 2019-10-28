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
