package descriptors;

import utils.Utils;

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
        String babel_out_file = original_file.replaceAll("\\.sdf$", "") + "_babel_out.sdf";

        // use open babel to ionize the structures
        String command = "babel -p 7 -isdf " + original_file + " -osdf " + babel_out_file;
        System.out.printf(Utils.executeCommand(command));

        return babel_out_file;
    }

    public static void main(String[] args) throws Exception {
        sanitize(args[0]);
    }
}
