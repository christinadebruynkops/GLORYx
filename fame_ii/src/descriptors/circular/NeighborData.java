package descriptors.circular;

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
