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

package main;

import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.neighboursearch.PerformanceStats;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Enumeration;

public class Main {
    static class TanimotoDistance implements DistanceFunction, Serializable {

        Instances instances = null;
        String indices = "";
        boolean invert_selection = false;

        @Override
        public void setInstances(Instances instances) {
            this.instances = instances;
        }

        @Override
        public Instances getInstances() {
            return instances;
        }

        @Override
        public void setAttributeIndices(String s) {
            indices = s;
        }

        @Override
        public String getAttributeIndices() {
            return indices;
        }

        @Override
        public void setInvertSelection(boolean b) {
            invert_selection = b;
        }

        @Override
        public boolean getInvertSelection() {
            return invert_selection;
        }

        public BitSet convert(Instance ins) {
            BitSet ret = new BitSet(ins.numValues());
            for (int i = 0; i < ins.numValues(); i++) {
                double val = ins.value(i);
                if (val == 1) {
                    ret.set(i);
                }
            }

            return ret;
        }

        @Override
        public double distance(Instance instance, Instance instance1) {
            BitSet a = this.convert(instance);
            BitSet b = this.convert(instance1);
//            try {
//                return 1 - Tanimoto.calculate(a, b);
//            } catch (CDKException e) {
//                e.printStackTrace();
//                return -1;
//            }
            final int size = Math.max(a.length(), b.length());
            final BitSet and = new BitSet(size);
            and.or(a);
            and.and(b);
            final BitSet or = new BitSet(size);
            or.or(a);
            or.or(b);
            final double union = or.cardinality();
            final double intersection = and.cardinality();
            return (union - intersection) / union;
        }

        @Override
        public double distance(Instance instance, Instance instance1, PerformanceStats performanceStats) throws Exception {
            return distance(instance, instance1);
        }

        @Override
        public double distance(Instance instance, Instance instance1, double v) {
            return distance(instance, instance1);
        }

        @Override
        public double distance(Instance instance, Instance instance1, double v, PerformanceStats performanceStats) {
            return distance(instance, instance1);
        }

        @Override
        public void postProcessDistances(double[] doubles) {

        }

        @Override
        public void update(Instance instance) {

        }

        @Override
        public void clean() {

        }

        @Override
        public Enumeration<Option> listOptions() {
            return null;
        }

        @Override
        public void setOptions(String[] strings) throws Exception {

        }

        @Override
        public String[] getOptions() {
            return new String[0];
        }
    }

}
