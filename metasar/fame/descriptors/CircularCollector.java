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

    public interface NeighborJoiner {

        Object combine(List<Object> to_combine);
    }

    public static class SumJoiner implements NeighborJoiner {

        @Override
        public Object combine(List<Object> to_combine) {
            double result = 0;
            for (Object val : to_combine) {
                result += Double.parseDouble(val.toString());
            }
            return result;
        }
    }

    public static class MeanJoiner implements NeighborJoiner {

        @Override
        public Object combine(List<Object> to_combine) {
            double result = 0;
            for (Object val : to_combine) {
                result += Double.parseDouble(val.toString());
            }
            return result / to_combine.size();
        }
    }

    public static class CountJoiner implements NeighborJoiner {

        @Override
        public Object combine(List<Object> to_combine) {
            return to_combine.size();
        }
    }

    private static final InputStream stream = SybylAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance()).getClass().getClassLoader().getResourceAsStream("org/openscience/cdk/dict/data/sybyl-atom-types.owl");
    private static final AtomTypeFactory factory = AtomTypeFactory.getInstance(stream, "owl", SilentChemObjectBuilder.getInstance());
    private static final IAtomType[] types = factory.getAllAtomTypes();

    private Map<IAtom, Set<NeighborData>> molecule_map;
    private List<String> descriptors;
    private NeighborJoiner default_joiner = new SumJoiner();
    private Map<String, NeighborJoiner> joiners;
    private int depth_reached = -1;
    private boolean ignore_zero_depth = true;

    CircularCollector(List<String> descriptors) {
        this.descriptors = new ArrayList<>(descriptors);
        this.molecule_map = new HashMap<>();
        this.joiners = new TreeMap<>();
    }

    CircularCollector(List<String> descriptors, NeighborJoiner default_joiner) {
        this(descriptors);
        this.default_joiner = default_joiner;
    }

    CircularCollector(List<String> descriptors, NeighborJoiner default_joiner, boolean ignore_zero_depth) {
        this(descriptors);
        this.ignore_zero_depth = ignore_zero_depth;
        this.default_joiner = default_joiner;
    }

    CircularCollector(List<String> descriptors, Map<String, NeighborJoiner> joiners, boolean ignore_zero_depth) {
        this(descriptors);
        this.joiners = joiners;
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
        return joiners.keySet();
    }

    public void writeData(IMolecule mol, Map<String, Integer> stats) throws Exception {
        int starting_depth = 0;
        if (ignore_zero_depth) {
            starting_depth = 1;
        }

        if (joiners.isEmpty()) {
            for (String desc : this.descriptors) {
                for (IAtomType tp : types) {
                    for (int depth = starting_depth; depth <= depth_reached; depth++) {
                        joiners.put(String.format("%s_%s_%d", desc, tp.getAtomTypeName(), depth), default_joiner.getClass().newInstance());
                    }
                }
            }
        }

        for (IAtom atm : mol.atoms()) {
            if (ignore_zero_depth && atm.getSymbol().equals("H")) {
                continue;
            }
            for (String signature : joiners.keySet()) {
                atm.setProperty(signature, null);
                if (!stats.containsKey(signature)) {
                    stats.put(signature, 0);
                }
            }
            Map<String, List<Object>> sig_val_map = new HashMap<>();
            for (NeighborData data : molecule_map.get(atm)) {
                String signature = data.getSignature();
                if (joiners.containsKey(signature)) {
                    if (!sig_val_map.containsKey(signature)) {
                        sig_val_map.put(signature, new ArrayList<>());
                    }
                    sig_val_map.get(signature).add(data.getValue());
                } else {
                    throw new Exception("invalid signature");
                }
            }

            for (String signature : sig_val_map.keySet()) {
                stats.put(signature, stats.get(signature) + 1);
                atm.setProperty(signature, joiners.get(signature).combine(sig_val_map.get(signature)));
            }
        }
    }
}
