package fame.weka;

import fame.weka.core.Instances;
import fame.weka.core.converters.ArffSaver;
import fame.weka.core.converters.ConverterUtils.DataSource;
import fame.weka.filters.unsupervised.instance.Resample;
 
import java.io.File;

/**
 * Resamples the data set. I am not using this script at this moment.
 * @author jkirchmair
 *
 */
public class Resampler {
	static String wDir = "/Users/jkirchmair/UNI/CAM/metabolismPrediction/workflow/";

	public static void main(String[] args) throws Exception {
		File folder = new File(wDir + "004metaPrintPredictions/human/test");
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isFile() && file.getName().endsWith(".arff")) {
		    	System.out.println("Loading file " + file);
		    	DataSource source = new DataSource(file.toString());
		    	Instances data = source.getDataSet();
		    	Resample resample = new Resample();
		    	resample.setSampleSizePercent(50);
		    	resample.setInputFormat(data);
		    	data = Resample.useFilter(data, resample);
			    ArffSaver saver = new ArffSaver();
			    saver.setInstances(data);
			    saver.setFile(new File(file.toString().replace(".arff", ".50.arff")));
			    saver.setDestination(new File(file.toString().replace(".arff", ".50.arff")));
			    saver.writeBatch();
		    }
		}
	}
}