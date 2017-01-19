package utils;

import globals.Globals;

import java.io.File;

/**
 * Runs open babel and ionizes the molecules to pH = 7.
 *
 * Created by sicho on 11/14/16.
 */
public class Sanitize {

    /**
     * Calls Open Babel to charge them and add hydrogens to them.
     *
     * @param params
     * @return
     * @throws Exception
     */
    public static String sanitize(Globals params) throws Exception {
        File origin = new File(params.input_sdf);
        if (!origin.exists()) {
            System.err.println("Input file not found: " + params.input_sdf);
            System.exit(1);
        }

        String origin_name = origin.getName();
        String babel_out_file = new File(params.output_dir, origin_name.replaceAll("\\.sdf$", "") + "_babel_sanitized.sdf").getPath();


        // use open babel to ionize the structures
        String command = "babel -p 7 -isdf " + params.input_sdf + " -osdf " + babel_out_file;
        System.out.println("Running: " + command);
        System.out.printf(Utils.executeCommand(command));

        return babel_out_file;
    }
}
