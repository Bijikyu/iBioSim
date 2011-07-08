package stategraph;

import javax.swing.JProgressBar;

public class PerfromTransientMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private JProgressBar progress;

	private double timeLimit, timeStep, printInterval, error;

	private String[] condition;

	private boolean globallyTrue;

	public PerfromTransientMarkovAnalysisThread(StateGraph sg, JProgressBar progress) {
		super(sg);
		this.sg = sg;
		this.progress = progress;
	}

	public void start(double timeLimit, double timeStep, double printInterval, double error, String[] condition, boolean globallyTrue) {
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.printInterval = printInterval;
		this.error = error;
		this.condition = condition;
		this.globallyTrue = globallyTrue;
		progress.setIndeterminate(false);
		super.start();
	}

	@Override
	public void run() {
		sg.performTransientMarkovianAnalysis(timeLimit, timeStep, printInterval, error, condition, progress, globallyTrue);
	}
}
