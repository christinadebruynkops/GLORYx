package org.zbh.fame.fame3.modelling;

import com.google.common.collect.RangeSet;
import org.xml.sax.SAXException;
import org.zbh.fame.fame3.globals.Globals;
import org.apache.commons.math3.stat.StatUtils;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Value;
import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.NearestNeighbourSearch;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Created by sicho on 1/18/17.
 */
public class Modeller {

    private Evaluator evaluator;
    private NearestNeighbourSearch nns;
    private List<String> nns_attributes;
    int bits_per_layer = 32;
    private String target_var;
    public static final int yes_val = 0;
    public static final int no_val = 1;
    public static final String proba_yes_fld = "probability(" + yes_val + ")";
    public static final String proba_no_fld = "probability(" + no_val + ")";
    public static final String is_som_fld = "isSoM";

    public Modeller(Globals globals) throws JAXBException, SAXException, IOException, ClassNotFoundException {
        this.target_var = globals.target_var;
        System.out.println("Loading model...");
        PMML pmml = loadModel(globals.pmml_path);
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        evaluator = modelEvaluator;

        if (globals.use_AD) {
            System.out.println("Loading applicability domain model...");
            InputStream file_in = Modeller.class.getResourceAsStream(globals.AD_model_path);
            InputStream file2_in = Modeller.class.getResourceAsStream(globals.AD_model_attrs_path);
            ObjectInputStream in = new ObjectInputStream(file_in);
            ObjectInputStream in2 = new ObjectInputStream(file2_in);
            nns = (NearestNeighbourSearch) in.readObject();
            nns_attributes = (List<String>) in2.readObject();
            in.close();
            in2.close();
            file_in.close();
            file2_in.close();
        }
    }

    private static PMML loadModel(String pmml_path) throws JAXBException, SAXException {
        InputStream res = Modeller.class.getResourceAsStream(pmml_path);
        PMML pmml = PMMLUtil.unmarshal(res);
        return pmml;
    }

    private Instances encodeMol(Instances at_fingeprints) {
        ArrayList<Attribute> new_atts = new ArrayList<>();
        int num_bits = bits_per_layer * at_fingeprints.numAttributes();
        for (int i = 1; i <= num_bits ; i++) {
            new_atts.add(new Attribute(Integer.toString(i)));
        }
        Instances insts_bits = new Instances("insts_bits", new_atts, at_fingeprints.size());
        Enumeration<Instance> insts_enum = at_fingeprints.enumerateInstances();
        while (insts_enum.hasMoreElements()) {
            Instance old_inst = insts_enum.nextElement();
            Instance new_inst = new DenseInstance(num_bits);
            for (int i = 0; i < old_inst.numValues(); i++) {
                double val = old_inst.value(i);
                for (int j = i * bits_per_layer; j < (i+1) * bits_per_layer; j++) {
                    if (val > 0) {
                        new_inst.setValue(j, 1);
                        val--;
                    } else {
                        new_inst.setValue(j, 0);
                    }
                }
            }

            insts_bits.add(new_inst);
        }

        return insts_bits;
    }

    private double calculateADScoreValue(double[] dists, int k) {
        double[] dists_k = Arrays.copyOfRange(dists, 0, k);
        return 1 - (StatUtils.mean(dists_k));
    }

    synchronized public void getADScore(IMolecule molecule) {
        ArrayList<Attribute> mol_attrs = new ArrayList<>();
        for (String nns_attribute : nns_attributes) {
            mol_attrs.add(new Attribute(nns_attribute));
        }

        for (IAtom atm : molecule.atoms()) {
            if (atm.getSymbol().equalsIgnoreCase("H")) {
                continue;
            }
            Instances insts_numeric = new Instances("insts_numeric", mol_attrs, 1);
            Instance inst = new DenseInstance(mol_attrs.size());
            for (String att_name : nns_attributes) {
                if (atm.getProperty(att_name) != null) {
                    inst.setValue(insts_numeric.attribute(att_name), ((Integer) atm.getProperty(att_name)).doubleValue());
                } else {
                    inst.setValue(insts_numeric.attribute(att_name), 0.0);
                    if (att_name.contains("_" + atm.getProperty("AtomType").toString() + "_0")) {
                        inst.setValue(insts_numeric.attribute(att_name), 1.0);
                    }
                }
            }

            insts_numeric.add(inst);
            Instances insts_fings = encodeMol(insts_numeric);
            try {
                int k = 3;
                Instances nbrs = nns.kNearestNeighbours(insts_fings.instance(0), k);
                double[] dists = nns.getDistances();

                atm.setProperty("AD_score", calculateADScoreValue(dists, k));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<IAtom, Result> predict(IMolecule molecule, double decision_threshold) {
        Map<IAtom, Result> results = new HashMap<>();
        for (IAtom atom : molecule.atoms()) {
            if (atom.getSymbol().equals("H")) {
                continue;
            }

            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
            for(InputField inputField : evaluator.getInputFields()){
                FieldName inputFieldName = inputField.getName();
//                System.out.println(inputFieldName.getValue());

                // The raw (ie. user-supplied) value could be any Java primitive value
                Object rawValue = atom.getProperty(inputFieldName.toString());
                if (rawValue == null && inputFieldName.toString().startsWith("AtomType_")) {
                    rawValue = 0;
                }

                // The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
                FieldValue inputFieldValue = inputField.prepare(rawValue);

//                System.out.println(inputFieldValue.asString());
                arguments.put(inputFieldName, inputFieldValue);
            }

            Map<FieldName, ?> result = evaluator.evaluate(arguments);
            List<TargetField> targetFields = evaluator.getTargetFields();
            Map<String, ProbabilityDistribution> targetfields = new HashMap<>();
            for(TargetField targetField : targetFields){
                FieldName targetFieldName = targetField.getName();
                targetfields.put(targetFieldName.getValue(), (ProbabilityDistribution) result.get(targetFieldName));
            }
            List<OutputField> outputFields = evaluator.getOutputFields();
            Map<String, Double> outfields = new HashMap<>();
            for(OutputField outputField : outputFields){
                FieldName outputFieldName = outputField.getName();

                outfields.put(outputFieldName.getValue(), (Double) result.get(outputFieldName));
            }

//            System.out.println(outfields.get("probability_1"));
//            System.out.println(outfields.get("probability_0"));
//            System.out.println(targetfields.get(Globals.target_var).getResult());
            Result res = new Result();
            res.probability_yes = outfields.get(proba_yes_fld);
            res.probability_no = outfields.get(proba_no_fld);
            if (res.probability_yes >= decision_threshold) {
                res.is_som = true;
            } else {
                res.is_som = false;
            }
            atom.setProperty(is_som_fld, res.is_som);
            atom.setProperty(proba_yes_fld, res.probability_yes);
            atom.setProperty(proba_no_fld, res.probability_no);
            results.put(atom, res);
        }
        return results;
    }

    public void inspect() {
        // input
        List<InputField> inputFields = evaluator.getInputFields();
        for(InputField inputField : inputFields){
            org.dmg.pmml.DataField pmmlDataField = (org.dmg.pmml.DataField)inputField.getField();
            org.dmg.pmml.MiningField pmmlMiningField = inputField.getMiningField();

            org.dmg.pmml.DataType dataType = inputField.getDataType();
            org.dmg.pmml.OpType opType = inputField.getOpType();

            switch(opType){
                case CONTINUOUS:
                    RangeSet<Double> validArgumentRanges = FieldValueUtil.getValidRanges(pmmlDataField);
                    break;
                case CATEGORICAL:
                case ORDINAL:
                    List<Value> validArgumentValues = FieldValueUtil.getValidValues(pmmlDataField);
                    break;
                default:
                    break;
            }
        }

        // target
        List<TargetField> targetFields = evaluator.getTargetFields();
        for(TargetField targetField : targetFields){
            org.dmg.pmml.DataField pmmlDataField = targetField.getDataField();
            org.dmg.pmml.MiningField pmmlMiningField = targetField.getMiningField(); // Could be null
            org.dmg.pmml.Target pmmlTarget = targetField.getTarget(); // Could be null

            org.dmg.pmml.DataType dataType = targetField.getDataType();
            org.dmg.pmml.OpType opType = targetField.getOpType();

            switch(opType){
                case CONTINUOUS:
                    break;
                case CATEGORICAL:
                case ORDINAL:
                    List<Value> validResultValues = FieldValueUtil.getValidValues(pmmlDataField);
                    break;
                default:
                    break;
            }
        }

        // output
        List<OutputField> outputFields = evaluator.getOutputFields();
        for(OutputField outputField : outputFields){
            org.dmg.pmml.OutputField pmmlOutputField = outputField.getOutputField();

            org.dmg.pmml.DataType dataType = outputField.getDataType(); // Could be null
            org.dmg.pmml.OpType opType = outputField.getOpType(); // Could be null

            boolean finalResult = outputField.isFinalResult();
            if(!finalResult){
                continue;
            }
        }
    }

}
