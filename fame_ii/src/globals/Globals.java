package globals;

/**
 * A simple class holding some useful global constants that are used
 * to configure the scripts.
 *
 * Created by sicho on 10/5/16.
 */
public class Globals {
    public static final String INPUT_SDF = "input.sdf";
    public static final String ID_PROP = "ID"; // SDF file property variable holding the ID of the molecule
    public static final String PRIM_SOM_PROP_PREFIX = "PRIMARY_SOM_";
    public static final String SEC_SOM_PROP_PREFIX = "SECONDARY_SOM_";
    public static final String TER_SOM_PROP_PREFIX = "TERTIARY_SOM_";
    public static final String SOM_PROP = "SoMs"; // SDF file property variable in which the sites of metabolism are saved
    public static final String NAME_OTHER_PROP = "Name_other";
    public static final String NAME_PROP = "Name";
    public static final String IS_SOM_PROP = "HLM";
    public static final String IS_PI_PROP = "Phase_I";
    public static final String IS_PII_PROP = "Phase_II";
    public static final String IS_METAPIE_PROP = "MetaPie";
    public static final String IS_SOM_CONFIRMED_VAL = "true";
    public static final String IS_SOM_POSSIBLE_VAL = "possible";
    public static final String UNKNOWN_VALUE = "unknown";
    public static final String REASUBCLS_PROP = "reasubclasses";
    public static final String REACLS_PROP = "reaclasses";
    public static final String REAMAIN_PROP = "reamain";
    public static final String REAGEN_PROP = "reagens";
    public static final int LOAD_MAX_MOL = -1; // load this many molecules at max (-1 for unlimited)
    public static final String DESCRIPTORS_OUT = "./descriptors_zaretzki/";
    public static final String DEPICTIONS_OUT = "./depictions_zaretzki/";
    public static final String DATASETS_OUT = "./datasets_zaretzki/";
}
