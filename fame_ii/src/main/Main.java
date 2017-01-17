package main;

import descriptors.DescriptorCalculator;
import globals.Globals;
import utils.Sanitize;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The main method of FAME II.
 *
 * Created by sicho on 1/16/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // TODO: this stuff will be the parameters of the program
        String input_sdf = Globals.INPUT_SDF;
        String out_dir = Globals.DESCRIPTORS_OUT;
        Set<String> desc_groups = new HashSet<>(
                Arrays.asList(
                "cdk"
                , "fing"
                , "ccdk"
                )
        );

        // sanitize the data and get the path to the modified file
        input_sdf = Sanitize.sanitize(input_sdf);

        // calculate the descriptors
        DescriptorCalculator desc_calc = new DescriptorCalculator(input_sdf, out_dir, desc_groups);
        desc_calc.calculate();
    }
}
