package org.zbh.fame.fame3.utils.data;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.zbh.fame.fame3.utils.data.parsers.FAMEFileParser;

import java.util.ArrayList;
import java.util.List;

public class FAMEMolSupplier {

    private List<FAMEFileParser> parsers;
    private FAMEFileParser current_parser;

    public FAMEMolSupplier(FAMEFileParser parser) {
        parsers = new ArrayList<>();
        current_parser = parser;
    }

    public FAMEMolSupplier(List<FAMEFileParser> parsers) {
        this.parsers = parsers;
        this.current_parser = this.parsers.remove(parsers.size() - 1);
    }

    public synchronized IAtomContainer getNext() {
        if (current_parser.hasNext()) {
            return current_parser.getNext();
        } else if (this.hasNext()) {
            return current_parser.getNext();
        } else {
            current_parser = null;
            parsers = null;
            return null;
        }
    }

    public boolean hasNext() {
        if (current_parser.hasNext()) {
            return true;
        } else if (!parsers.isEmpty()){
            while (!parsers.isEmpty() && !current_parser.hasNext()) {
                current_parser = parsers.remove(parsers.size() - 1);
            }
            return current_parser.hasNext();
        } else {
            return false;
        }
    }
}
