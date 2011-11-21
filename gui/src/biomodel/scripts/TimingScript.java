package biomodel.scripts;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import biomodel.util.ExperimentResult;
import biomodel.util.Utility;


import junit.framework.TestCase;

public class TimingScript extends TestCase {

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		keySpeciesList = new ArrayList<String>();
		keySpeciesList.add(keySpecies);
		script = new GCMScript();
	}

	public void testTiming() {
		try {
			generateTiming("promoter", 5);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on promoter");
		}
		try {
			generateTiming("coop", 5);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on coop");
		}
		try {
			generateTiming("rep", 7);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on rep");
		}
		try {
			generateTiming("decay", 8);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on decay");
		}
		try {
			generateTiming("ratio", 6);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on ratio");
		}
	}

	public double[] findTime(ExperimentResult results) {
		// First find the midpoint between high and low
		double low = results.getValue(keySpecies, lowTime);
		double high = results.getValue(keySpecies, highTime);
		double mid = (high - low) / 2.;

		// Now calculate switching time for low to high, high to low
		double timeToHigh = results.getTime(keySpecies, mid, switchHighTime);		
		double timeToLow = results.getTime(keySpecies, mid, switchLowTime);

		return new double[] { timeToHigh, timeToLow };
	}

	public void generateTiming(String files, int cases) {
		double[][] timingResults = new double[6][cases];

		for (int i = 1; i <= cases; i++) {
			String currDir = Utility.directory + File.separator + files + i;
			for (int j = 0; j < 3; j++) {
				HashMap<String, double[]> results = Utility
						.calculateAverage(currDir + File.separator
								+ dataGate[j]);
				ExperimentResult expResults = new ExperimentResult(results);
				double[] times = findTime(expResults);
				timingResults[j*2][i-1] = times[0] - switchHighTime;
				if (timingResults[j*2][i-1] < 0) {
					timingResults[j*2][i-1] = 0;
				}
				timingResults[j*2+1][i-1] = times[1] - switchLowTime;
				if (timingResults[j*2+1][i-1] < 0) {
					timingResults[j*2+1][i-1] = 0;
				}
			}
			System.out.println("Done " + i);
		}
		printResults(Utility.directory + File.separator + files + ".dat", timingResults);
	}

	private void printResults(String file, double[][] results) {
		try {
			// Create file
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("maj-high\tmaj-low\ttog-high\ttog-low\tsi-high\tsi-low\n");
			for (int i = 0; i < results[0].length; i++) {
				for (int j = 0; j < results.length; j++) {
					out.write(results[j][i] + "\t");
				}
				out.write("\n");
			}
			out.flush();
			// Close the output stream
			fstream.flush();
			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private GCMScript script = null;

	private String[] gate = { "maj", "tog", "si" };
	private String[] dataGate = { "majority", "toggle", "si" };
	ArrayList<String> keySpeciesList = null;
	String keySpecies = "C";

	// Switching time
	private final static double lowTime = 2400;
	private final static double highTime = 7400;

	private final static double switchHighTime = 5000;
	private final static double switchLowTime = 10000;
}
