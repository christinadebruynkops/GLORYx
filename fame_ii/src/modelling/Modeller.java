package modelling;

import com.google.common.collect.RangeSet;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Value;
import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sicho on 1/18/17.
 */
public class Modeller {

    private Evaluator evaluator;
    private String target_var;
    public static final int yes_val = 0;
    public static final int no_val = 1;
    public static final String proba_yes_fld = "probability_" + Integer.toString(yes_val);
    public static final String proba_no_fld = "probability_" + Integer.toString(no_val);
    public static final String is_som_fld = "isSoM";

    public Modeller(String pmml_path, String target_var) throws Exception {
        this.target_var = target_var;
        System.out.println("Loading model...");
        PMML pmml = loadModel(pmml_path);
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        evaluator = modelEvaluator;
    }

    private static PMML loadModel(String pmml_path) throws Exception {
        InputStream res = Modeller.class.getResourceAsStream(pmml_path);
        PMML pmml = PMMLUtil.unmarshal(res);
        return pmml;
    }

    public Map<IAtom, Result> predict(IMolecule molecule) {
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
            int prediction = (Integer) targetfields.get(target_var).getResult();
            if (prediction == yes_val) {
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
