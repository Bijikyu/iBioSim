package gcm2sbml.scripts;

import gcm2sbml.util.ExperimentResult;
import gcm2sbml.util.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GCMScript {

	public void generateThresholds(String directory, String species,
			double bestTime, String type, int num) {
		double[] highs = new double[3 * num];
		double[] lows = new double[3 * num];

		for (int i = 1; i <= num; i++) {
			for (int j = 0; j < 3; j++) {
				double[] results = generateThreshold(directory + i, gate[j],
						species, bestTime);
				highs[3 * (i - 1) + j] = results[1];
				lows[3 * (i - 1) + j] = results[0];

			}
		}
		System.out.println("\nHigh:");
		for (int i = 0; i < highs.length; i++) {
			System.out.print(highs[i] + " ");
		}
		System.out.println("\nLow:");
		for (int i = 0; i < lows.length; i++) {
			System.out.print(lows[i] + " ");
		}
	}

	public double[] generateThreshold(String directory, String type,
			String species, double bestTime) {
		HashMap<String, double[]> results = null;
		double[] timeValues = null;
		double high = 0;
		double low = 0;
		int index = -1;
		results = Utility.calculateAverage(directory + File.separator + type
				+ experiment[0]);

		timeValues = results.get("time");
		for (int i = 0; i < timeValues.length; i++) {
			if (timeValues[i] > bestTime) {
				index = i - 1;
				break;
			}
		}

		low = results.get(species)[index];
		results = Utility.calculateAverage(directory + File.separator + type
				+ experiment[1]);
		high = results.get(species)[index];

		double range = (high - low) / 3.;
		return new double[] { low + range, high - range };
	}
	
	public double[][] generateThreshold(String directory, ArrayList<String> species, String type, double bestTime) {
		ExperimentResult highResults, lowResults = null;
		double[] timeValues = null;
		double[] high = new double[species.size()];
		double[] low = new double[species.size()];
		int index = -1;
		highResults = new ExperimentResult(Utility.calculateAverage(directory + File.separator + type
				+ experiment[0]));

		
		lowResults = new ExperimentResult(Utility.calculateAverage(directory + File.separator + type
				+ experiment[1]));
				
		for (int i = 0; i < species.size(); i++) {
			double range = (highResults.getValue(species.get(i), bestTime)-lowResults.getValue(species.get(i), bestTime)) / 3.;
			high[i] = highResults.getValue(species.get(i), bestTime)-range;
			low[i] = lowResults.getValue(species.get(i), bestTime)+range;
		}		
		return new double[][] {low, high};
	}
	
	public double[][] generateStatistics(String directory, TesterInterface tester) {
		double[][] passed = null;
		String[] files = Utility.getTSDFiles(directory);
		for (int i = 0; i < files.length; i++) {
			boolean[] results = tester.passedTest(new ExperimentResult(directory+File.separator+files[i]));
			if (passed == null) {
				passed = new double[3][results.length];				
			}
			for (int j = 0; j < results.length; j++) {
				if (results[j]) {
					passed[1][j] = passed[1][j] + 1;
				}
			}
		}
		double[] times = tester.getTimes();
		for (int i = 0; i < passed[0].length; i++) {
			passed[0][i] = times[i];
			passed[1][i] = 1-passed[1][i]/files.length;
			passed[2][i] = 1.96*Math.sqrt(passed[1][i]*(1-passed[1][i])/files.length);
		}
		
		return passed;
	}

	private String[] kind = { "coop", "rep", "promoter" };
	private String[] gate = { "maj", "tog", "si" };
	private String[] experiment = { "-h-high", "-h-low", "-l-high", "-l-low" };
	private static final String directory = "/home/shang/namphuon/muller";
}
