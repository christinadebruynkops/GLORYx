package org.zbh.fame.fame3.utils.data.parsers;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.List;

public interface FAMEFileParser {

    void setNamePrefix(String prefix);
    List<IAtomContainer> getMols();
    IAtomContainer getNext();
    boolean hasNext();
    String getFilePath();
}
