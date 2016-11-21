package fame.descriptors;

import fame.tools.NeighborhoodCollector;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import java.util.*;

public class CircularCollector implements NeighborhoodCollector {

    private Map<IAtom, Set<NeighborData>> molecule_map;
    private List<String> descriptors;

    CircularCollector(List<String> descriptors) {
        this.descriptors = new ArrayList<>(descriptors);
    }

    @Override
    public void collect(IAtom atm, Set<IAtom> neighbors, int current_depth) {
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

    public void writeData(IMolecule mol, Map<String, NeighborCombinator> combinators) throws Exception {
        for (IAtom atm : mol.atoms()) {
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
