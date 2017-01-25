package main;

import globals.Globals;
import modelling.Predictor;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // TODO: this stuff will be the parameters of the program
        String input_sdf = "test_files/compounds/tamoxifen.sdf";
        String out_dir = "test_files/results";
        String model_code = "cdk_ccdk"; // can be 'cdk', 'cdk_ccdk', 'cdk_fing' or 'cdk_fing_ccdk'
        String target_var = "HLM";
        boolean santize = true;

        Globals params = new Globals(
                input_sdf
                , out_dir
                , model_code
                , target_var
                , santize
        );

        // calculate the descriptors
        Predictor desc_calc = new Predictor(params);
        desc_calc.calculate();
    }
}
