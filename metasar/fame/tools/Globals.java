package fame.tools;

/**
 * A simple class holding some useful global variables that can be used
 * to configure the scripts.
 *
 * Created by sicho on 10/5/16.
 */
public class Globals {
    public static final String ID_PROP = "MolID"; // SDF file property variable holding the ID of the molecule
    public static final String SOM_PROP = "SoMs"; // SDF file property variable in which the sites of metabolism are saved
    public static final String IS_SOM_PROP = "isSom";
    public static final String IS_SOM_CONFIRMED_PROP = "isSomConfirmed";
    public static final String REASUBCLS_PROP = "reasubclasses";
    public static final String REACLS_PROP = "reaclasses";
    public static final String REAMAIN_PROP = "reamain";
    public static final String REAGEN_PROP = "reagens";
    public static final int LOAD_MAX_MOL = -1; // load this many molecules at max (-1 for unlimited)
}
