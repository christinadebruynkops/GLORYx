/* Copyright (C) 2017, 2019  Martin Šícho <martin.sicho@vscht.cz>
 
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

package org.zbh.fame.fame3.modelling.descriptors.circular;

import org.openscience.cdk.interfaces.IAtom;

public class NeighborData {

    IAtom atom;
    String atom_type;
    String descriptor_name;
    int depth;
    Object value;

    public NeighborData(IAtom atom, String atom_type, String descriptor_name, int depth, Object value) {
        this.atom = atom;
        this.atom_type = atom_type;
        this.descriptor_name = descriptor_name;
        this.depth = depth;
        this.value = value;
    }

    public String getSignature() {
        return String.format("%s_%s_%d", descriptor_name, atom_type, depth);
    }

    public IAtom getAtom() {
        return atom;
    }

    public String getAtomType() {
        return atom_type;
    }

    public String getDescriptorName() {
        return descriptor_name;
    }

    public int getDepth() {
        return depth;
    }

    public Object getValue() {
        return value;
    }
}
