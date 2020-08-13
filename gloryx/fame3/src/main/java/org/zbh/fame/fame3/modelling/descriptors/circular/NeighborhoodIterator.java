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

import org.openscience.cdk.interfaces.IAtomContainer;
import org.zbh.fame.fame3.globals.Globals;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbh.fame.fame3.utils.depiction.Depictor;

import java.util.*;

/**
 * Facilitates iterating over atom neighborhoods
 *
 * Created by sicho on 11/21/16.
 */
public class NeighborhoodIterator {

    private int depth;
    private IAtomContainer mol;
    private boolean depict = false;
    private boolean ignore_hydrogens = true;
    
	private static final Logger logger = LoggerFactory.getLogger(NeighborhoodIterator.class.getName());


    public NeighborhoodIterator(IAtomContainer mol, int depth) {
        this.mol = mol;
        this.depth = depth;
    }

    public NeighborhoodIterator(IAtomContainer mol, int depth, boolean depict) {
        this(mol, depth);
        this.depict = depict;
    }

    public NeighborhoodIterator(IAtomContainer mol, int depth, boolean depict, boolean ignore_hydrogens) {
        this(mol, depth, depict);
        this.ignore_hydrogens = ignore_hydrogens;
    }

    private static void depictNeighborhood(IAtomContainer mol, Set<IAtom> frag_atms, String outfile) throws Exception {
        Set<IBond> bonds_all = new HashSet<>();
        for (IBond bond : mol.bonds()) {
            if (frag_atms.contains(bond.getAtom(0)) && frag_atms.contains(bond.getAtom(1))) {
                bonds_all.add(bond);
            }
        }
        IAtomContainer mol_frag = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        IBond[] b_arr = new IBond[bonds_all.size()];
        IAtom[] a_arr = new IAtom[frag_atms.size()];
        mol_frag.setBonds(bonds_all.toArray(b_arr));
        mol_frag.setAtoms(frag_atms.toArray(a_arr));
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol_frag);

        new Depictor().generateDepiction(mol_frag, outfile);
    }

    public void iterate(NeighborhoodCollector collector) {
        for (IAtom atm : mol.atoms()) {
            if (ignore_hydrogens && atm.getSymbol().equals("H")) {
                continue;
            }
            int idx = mol.getAtomNumber(atm);
            Map<Integer, Set<IAtom>> neigborhood_map = new HashMap<>();
            Set<IAtom> atoms_current = new HashSet<>();
            Set<IAtom> atoms_next = new HashSet<>();
            atoms_current.add(atm);
            neigborhood_map.put(0, new HashSet<>(atoms_current));
            collector.collect(atm, neigborhood_map.get(0), 0);
            for (int i = 0; i != depth; i++) {
                atoms_next.clear();
                for (IAtom atm_current : atoms_current) {
                    List<IAtom> nbs = mol.getConnectedAtomsList(atm_current);
                    atoms_next.addAll(nbs);
                    if (ignore_hydrogens) {
                        for (IAtom nb : nbs) {
                            if (nb.getSymbol().equals("H")) {
                                atoms_next.remove(nb);
                            }
                        }
                    }
                }
                if (i > 0) {
                    atoms_next.removeAll(neigborhood_map.get(i-1));
                }
                neigborhood_map.put(i + 1, new HashSet<>(atoms_next));
                collector.collect(atm, neigborhood_map.get(i + 1), i + 1);
                atoms_current.clear();
                atoms_current.addAll(atoms_next);
            }

            if (depict) {
                try {
                    Set<IAtom> fragment_atoms = new HashSet<>();
                    for (Integer val : neigborhood_map.keySet()) {
                        fragment_atoms.addAll(neigborhood_map.get(val));
                    }
                    depictNeighborhood(mol, fragment_atoms, String.format("%s_%d.png", mol.getProperty(Globals.ID_PROP), idx + 1));
                } catch (Exception exp) {
                    logger.warn("WARNING: failed to generate neighborhood depiction");
                    exp.printStackTrace();
                }
            }
        }
    }
}
