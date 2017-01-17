package utils;

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
     * @param original_file
     * @return
     * @throws Exception
     */
    public static String sanitize(String original_file) throws Exception {
        if (!new File(original_file).exists()) {
            System.err.println("File not found: " + original_file);
            System.exit(1);
        }

        String babel_out_file = original_file.replaceAll("\\.sdf$", "") + "_babel_sanitized.sdf";

        // use open babel to ionize the structures
        String command = "babel -p 7 -isdf " + original_file + " -osdf " + babel_out_file;
        System.out.println("Running: " + command);
        System.out.printf(Utils.executeCommand(command));

        return babel_out_file;
    }

    public static void main(String[] args) throws Exception {
        sanitize(args[0]);
    }
}
