package fame.descriptors;

import fame.tools.NeighborhoodCollector;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import java.io.InputStream;
import java.util.*;

public class CircularCollector implements NeighborhoodCollector {

    public class SumCombinator implements NeighborCombinator {

        @Override
        public Object combine(Object current, Object added) {
            double current_ = Double.parseDouble(current.toString());
            double added_ = Double.parseDouble(added.toString());
            return current_ + added_;
        }
    }

    private static final InputStream stream = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance()).getClass().getClassLoader().getResourceAsStream("org/openscience/cdk/dict/data/sybyl-atom-types.owl");
    private static final AtomTypeFactory factory = AtomTypeFactory.getInstance(stream, "owl", SilentChemObjectBuilder.getInstance());
    private static final IAtomType[] types = factory.getAllAtomTypes();

    private Map<IAtom, Set<NeighborData>> molecule_map;
    private List<String> descriptors;
    private Map<String, NeighborCombinator> combinators;
    private int depth_reached = -1;
    private boolean ignore_zero_depth = true;

    CircularCollector(List<String> descriptors) {
        this.descriptors = new ArrayList<>(descriptors);
        this.molecule_map = new HashMap<>();
        this.combinators = new TreeMap<>();
    }

    CircularCollector(List<String> descriptors, boolean ignore_zero_depth) {
        this(descriptors);
        this.ignore_zero_depth = ignore_zero_depth;
    }

    CircularCollector(List<String> descriptors, Map<String, NeighborCombinator> combinators, boolean ignore_zero_depth) {
        this(descriptors);
        this.combinators = combinators;
        this.ignore_zero_depth = ignore_zero_depth;
    }

    @Override
    public void collect(IAtom atm, Set<IAtom> neighbors, int current_depth) {
        if (ignore_zero_depth && current_depth == 0) {
            return;
        }

        if (current_depth > depth_reached) {
            depth_reached = current_depth;
        }

        if (!molecule_map.containsKey(atm)) {
            molecule_map.put(atm, new HashSet<>());
        }
        for (IAtom nb : neighbors) {
            for (String desc : descriptors) {
                molecule_map.get(atm).add(new NeighborData(
                        nb
                        , nb.getProperty("AtomType").toString()
                        , desc
                        , current_depth
                        , nb.getProperty(desc)
                ));
            }
        }
    }

    public Set<String> getSignatures() {
        return combinators.keySet();
    }

    public void writeData(IMolecule mol) throws Exception {
        int starting_depth = 0;
        if (ignore_zero_depth) {
            starting_depth = 1;
        }

        if (combinators.isEmpty()) {
            for (String desc : this.descriptors) {
                for (IAtomType tp : types) {
                    for (int depth = starting_depth; depth <= depth_reached; depth++) {
                        combinators.put(String.format("%s_%s_%d", desc, tp.getAtomTypeName(), depth), new SumCombinator());
                    }
                }
            }
        }

        for (IAtom atm : mol.atoms()) {
            if (ignore_zero_depth && atm.getSymbol().equals("H")) {
                continue;
            }
            for (String sig : combinators.keySet()) {
                atm.setProperty(sig, null);
            }
            for (NeighborData data : molecule_map.get(atm)) {
                String signature = data.getSignature();
                if (combinators.containsKey(signature)) {
                    if (atm.getProperty(signature) != null) {
                        atm.setProperty(
                                signature
                                , combinators.get(signature).combine(
                                        atm.getProperty(signature)
                                        , data.getValue()
                                )
                        );
                    } else {
                        atm.setProperty(signature, data.getValue());
                    }
                } else {
                    throw new Exception("invalid signature");
                }
            }
        }
    }
}
