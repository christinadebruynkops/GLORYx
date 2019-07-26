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
