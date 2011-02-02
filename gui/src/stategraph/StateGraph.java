package stategraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import parser.Parser;


import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import main.Gui;

public class StateGraph implements Runnable {
	// private HashMap<String, LinkedList<State>> stateGraph;

	private ArrayList<State> stateGraph;

	private ArrayList<String> variables;

	private LhpnFile lhpn;

	private boolean stop;

	private String markovResults;

	private Parser probData;

	private int waitingThreads, threadCount;

	private boolean phase1, phase2;

	public StateGraph(LhpnFile lhpn) {
		this.lhpn = lhpn;
		stop = false;
		markovResults = null;
	}

	public void buildStateGraph() {
		stateGraph = new ArrayList<State>();// HashMap<String,
		// LinkedList<State>>();
		HashMap<String, LinkedList<Integer>> stateLocations = new HashMap<String, LinkedList<Integer>>();
		variables = new ArrayList<String>();
		for (String var : lhpn.getBooleanVars()) {
			variables.add(var);
		}
		for (String var : lhpn.getIntVars()) {
			variables.add(var);
		}
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		ArrayList<String> markedPlaces = new ArrayList<String>();
		for (String place : lhpn.getPlaceList()) {
			if (lhpn.getPlace(place).isMarked()) {
				markedPlaces.add(place);
			}
		}
		// LinkedList<State> markings = new LinkedList<State>();
		LinkedList<Integer> markings = new LinkedList<Integer>();
		int counter = 0;
		State state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
				"S" + counter, createStateVector(variables, allVariables),
				copyAllVariables(allVariables));
		// markings.add(state);
		counter++;
		stateGraph.add(state);// .put(createStateVector(variables,
		// allVariables), markings);
		markings.add(stateGraph.size() - 1);
		stateLocations.put(createStateVector(variables, allVariables), markings);
		Stack<Transition> transitionsToFire = new Stack<Transition>();
		for (String transition : lhpn.getTransitionList()) {
			boolean addToStack = true;
			if (lhpn.getEnablingTree(transition) != null
					&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) == 0.0) {
				addToStack = false;
			}
			if (lhpn.getTransitionRateTree(transition) != null
					&& lhpn.getTransitionRateTree(transition).evaluateExpr(allVariables) == 0.0) {
				addToStack = false;
			}
			if (lhpn.getPreset(transition).length != 0) {
				for (String place : lhpn.getPreset(transition)) {
					if (!markedPlaces.contains(place)) {
						addToStack = false;
					}
				}
			}
			else {
				addToStack = false;
			}
			if (addToStack) {
				transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces),
						state));
			}
		}
		while (transitionsToFire.size() != 0 && !stop) {
			Transition fire = transitionsToFire.pop();
			markedPlaces = fire.getMarkedPlaces();
			allVariables = copyAllVariables(fire.getParent().getVariables());
			for (String place : lhpn.getPreset(fire.getTransition())) {
				markedPlaces.remove(place);
			}
			for (String place : lhpn.getPostset(fire.getTransition())) {
				markedPlaces.add(place);
			}
			for (String key : allVariables.keySet()) {
				if (lhpn.getBoolAssignTree(fire.getTransition(), key) != null) {
					double eval = lhpn.getBoolAssignTree(fire.getTransition(), key).evaluateExpr(
							allVariables);
					if (eval == 0.0) {
						allVariables.put(key, "false");
					}
					else {
						allVariables.put(key, "true");
					}
				}
				if (lhpn.getContAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ lhpn.getContAssignTree(fire.getTransition(), key).evaluateExpr(
									allVariables));
				}
				if (lhpn.getIntAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ ((int) lhpn.getIntAssignTree(fire.getTransition(), key).evaluateExpr(
									allVariables)));
				}
			}
			// if (!stateGraph.containsKey(createStateVector(variables,
			// allVariables))) {
			if (!stateLocations.containsKey(createStateVector(variables, allVariables))) {
				// markings = new LinkedList<State>();
				markings = new LinkedList<Integer>();
				state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
						"S" + counter, createStateVector(variables, allVariables),
						copyAllVariables(allVariables));
				// markings.add(state);
				fire.getParent().addNextState(
						state,
						lhpn.getTransitionRateTree(fire.getTransition()).evaluateExpr(
								fire.getParent().getVariables()), fire.getTransition());
				counter++;
				stateGraph.add(state);// .put(createStateVector(variables,
				// allVariables), markings);
				markings.add(stateGraph.size() - 1);
				stateLocations.put(createStateVector(variables, allVariables), markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					if (lhpn.getEnablingTree(transition) != null
							&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) == 0.0) {
						addToStack = false;
					}
					if (lhpn.getTransitionRateTree(transition) != null
							&& lhpn.getTransitionRateTree(transition).evaluateExpr(allVariables) == 0.0) {
						addToStack = false;
					}
					if (lhpn.getPreset(transition).length != 0) {
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
							}
						}
					}
					else {
						addToStack = false;
					}
					if (addToStack) {
						transitionsToFire.push(new Transition(transition,
								copyArrayList(markedPlaces), state));
					}
				}
			}
			else {
				// markings = stateGraph.get(createStateVector(variables,
				// allVariables));
				markings = stateLocations.get(createStateVector(variables, allVariables));
				boolean add = true;
				boolean same = true;
				for (Integer index : markings) {// State mark : stateGraph) {
					State mark = stateGraph.get(index);
					for (String place : mark.getMarkings()) {
						if (!markedPlaces.contains(place)) {
							same = false;
						}
					}
					for (String place : markedPlaces) {
						boolean contains = false;
						for (String place2 : mark.getMarkings()) {
							if (place2.equals(place)) {
								contains = true;
							}
						}
						if (!contains) {
							same = false;
						}
					}
					if (same) {
						add = false;
						fire.getParent().addNextState(
								mark,
								lhpn.getTransitionRateTree(fire.getTransition()).evaluateExpr(
										fire.getParent().getVariables()), fire.getTransition());
					}
					same = true;
				}
				if (add) {
					state = new State(markedPlaces.toArray(new String[0]),
							new StateTransitionPair[0], "S" + counter, createStateVector(variables,
									allVariables), copyAllVariables(allVariables));
					// markings.add(state);
					fire.getParent().addNextState(
							state,
							lhpn.getTransitionRateTree(fire.getTransition()).evaluateExpr(
									fire.getParent().getVariables()), fire.getTransition());
					counter++;
					stateGraph.add(state);// .put(createStateVector(variables,
					// allVariables), markings);
					markings.add(stateGraph.size() - 1);
					stateLocations.put(createStateVector(variables, allVariables), markings);
					for (String transition : lhpn.getTransitionList()) {
						boolean addToStack = true;
						if (lhpn.getEnablingTree(transition) != null
								&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) == 0.0) {
							addToStack = false;
						}
						if (lhpn.getTransitionRateTree(transition) != null
								&& lhpn.getTransitionRateTree(transition)
										.evaluateExpr(allVariables) == 0.0) {
							addToStack = false;
						}
						if (lhpn.getPreset(transition).length != 0) {
							for (String place : lhpn.getPreset(transition)) {
								if (!markedPlaces.contains(place)) {
									addToStack = false;
								}
							}
						}
						else {
							addToStack = false;
						}
						if (addToStack) {
							transitionsToFire.push(new Transition(transition,
									copyArrayList(markedPlaces), state));
						}
					}
				}
			}
		}
	}

	public boolean canPerformMarkovianAnalysis() {
		for (String trans : lhpn.getTransitionList()) {
			if (!lhpn.isExpTransitionRateTree(trans)) {
				JOptionPane.showMessageDialog(Gui.frame,
						"LPN has transitions without exponential delay.",
						"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (String var : lhpn.getVariables()) {
				if (lhpn.isRandomBoolAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame,
							"LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lhpn.isRandomContAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame,
							"LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lhpn.isRandomIntAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame,
							"LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		if (lhpn.getContVars().length > 0) {
			JOptionPane.showMessageDialog(Gui.frame, "LPN contains continuous variables.",
					"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public boolean performTransientMarkovianAnalysis(double timeLimit, double timeStep,
			double printInterval, double error, String[] condition, JProgressBar progress) {
		if (!canPerformMarkovianAnalysis()) {
			stop = true;
			return false;
		}
		else if (condition != null) {
			double nextPrint = printInterval;
			progress.setMaximum((int) timeLimit);
			double Gamma = 0;
			ArrayList<String> dataLabels = new ArrayList<String>();
			dataLabels.add("time");
			dataLabels.add("~(" + condition[0] + ")&~(" + condition[1] + ")");
			dataLabels.add(condition[1]);
			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
			ArrayList<Double> temp = new ArrayList<Double>();
			temp.add(0.0);
			data.add(temp);
			temp = new ArrayList<Double>();
			temp.add(0.0);
			data.add(temp);
			temp = new ArrayList<Double>();
			temp.add(0.0);
			data.add(temp);
			probData = new Parser(dataLabels, data);
			State initial = getInitialState();
			if (initial != null) {
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);
				double lowerbound = 0;
				if (!condition[2].equals("")) {
					ExprTree expr = new ExprTree(lhpn);
					expr.token = expr.intexpr_gettok(condition[2]);
					expr.intexpr_L(condition[2]);
					lowerbound = Math.min(expr.evaluateExpr(null), timeLimit);
					pruneStateGraph("~(" + condition[0] + ")");
					// Compute Gamma
					for (State m : stateGraph) {
						Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
					}
					// Compute K
					int K = 0;
					double xi = 1;
					double delta = 1;
					double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
					while (delta < eta) {
						K = K + 1;
						xi = xi * ((Gamma * timeStep) / K);
						delta = delta + xi;
					}
					double step = Math.min(Math.min(timeStep, lowerbound), nextPrint);
					for (double i = 0; i < lowerbound; i += step) {
						step = Math.min(Math.min(timeStep, lowerbound - i), nextPrint - i);
						if (step != timeStep) {
							// Compute K
							K = 0;
							xi = 1;
							delta = 1;
							eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
							while (delta < eta) {
								K = K + 1;
								xi = xi * ((Gamma * step) / K);
								delta = delta + xi;
							}
						}
						if (!performTransientMarkovianAnalysis(step, error, Gamma, K, progress)) {
							return false;
						}
						double prob = 0;
						// for (String state : stateGraph.keySet()) {
						for (State m : stateGraph) {
							// for (State m : stateGraph.get(state)) {
							expr = new ExprTree(lhpn);
							expr.token = expr.intexpr_gettok("~(" + condition[0] + ")");
							expr.intexpr_L("~(" + condition[0] + ")");
							if (expr.evaluateExpr(m.getVariables()) == 1.0) {
								prob += m.getCurrentProb();
							}
							// }
						}
						if (i + step == nextPrint) {
							probData.getData().get(0).add(nextPrint);
							probData.getData().get(1).add(prob * 100);
							probData.getData().get(2).add(0.0);
							nextPrint += printInterval;
						}
					}
				}
				else {
					pruneStateGraph("~(" + condition[0] + ")");
				}
				ExprTree expr = new ExprTree(lhpn);
				expr.token = expr.intexpr_gettok(condition[3]);
				expr.intexpr_L(condition[3]);
				double upperbound = Math.min(expr.evaluateExpr(null) - lowerbound, timeLimit
						- lowerbound);
				pruneStateGraph(condition[1]);
				// Compute Gamma
				for (State m : stateGraph) {
					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
				}
				// Compute K
				int K = 0;
				double xi = 1;
				double delta = 1;
				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
				while (delta < eta) {
					K = K + 1;
					xi = xi * ((Gamma * timeStep) / K);
					delta = delta + xi;
				}
				double step = Math.min(Math.min(timeStep, upperbound - lowerbound), nextPrint
						- lowerbound);
				for (double i = 0; i < upperbound; i += step) {
					step = Math.min(Math.min(timeStep, upperbound - i), nextPrint - lowerbound - i);
					if (step != timeStep) {
						// Compute K
						K = 0;
						xi = 1;
						delta = 1;
						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
						while (delta < eta) {
							K = K + 1;
							xi = xi * ((Gamma * step) / K);
							delta = delta + xi;
						}
					}
					if (!performTransientMarkovianAnalysis(step, error, Gamma, K, progress)) {
						return false;
					}
					double failureProb = 0;
					double successProb = 0;
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					for (State m : stateGraph) {
						ExprTree failureExpr = new ExprTree(lhpn);
						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~("
								+ condition[1] + ")");
						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
						ExprTree successExpr = new ExprTree(lhpn);
						successExpr.token = successExpr.intexpr_gettok(condition[1]);
						successExpr.intexpr_L(condition[1]);
						if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
							failureProb += m.getCurrentProb();
						}
						else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
							successProb += m.getCurrentProb();
						}
						// }
					}
					if (lowerbound + i + step == nextPrint) {
						probData.getData().get(0).add(nextPrint);
						probData.getData().get(1).add(failureProb * 100);
						probData.getData().get(2).add(successProb * 100);
						nextPrint += printInterval;
					}
				}
				HashMap<String, Double> output = new HashMap<String, Double>();
				double failureProb = 0;
				double successProb = 0;
				double timelimitProb = 0;
				for (State m : stateGraph) {
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					ExprTree failureExpr = new ExprTree(lhpn);
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~("
							+ condition[1] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					ExprTree successExpr = new ExprTree(lhpn);
					successExpr.token = successExpr.intexpr_gettok(condition[1]);
					successExpr.intexpr_L(condition[1]);
					if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
						failureProb += m.getCurrentProb();
					}
					else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
						successProb += m.getCurrentProb();
					}
					else {
						timelimitProb += m.getCurrentProb();
					}
					// }
				}
				output.put("~(" + condition[0].trim() + ")&~(" + condition[1].trim() + ")",
						failureProb);
				output.put(condition[1].trim(), successProb);
				output.put("timelimit", timelimitProb);
				String result1 = "#total";
				String result2 = "1.0";
				for (String s : output.keySet()) {
					result1 += " " + s;
					result2 += " " + output.get(s);
				}
				markovResults = result1 + "\n" + result2 + "\n";
				return true;
			}
			else {
				return false;
			}
		}
		else {
			progress.setMaximum((int) timeLimit);
			State initial = getInitialState();
			if (initial != null) {
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);
				boolean stop = false;
				// Compute Gamma
				double Gamma = 0;
				for (State m : stateGraph) {
					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
				}
				// Compute K
				int K = 0;
				double xi = 1;
				double delta = 1;
				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
				while (delta < eta) {
					K = K + 1;
					xi = xi * ((Gamma * timeStep) / K);
					delta = delta + xi;
				}
				for (double i = 0; i < timeLimit; i += timeStep) {
					double step = Math.min(timeStep, timeLimit - i);
					if (step == timeLimit - i) {
						// Compute K
						K = 0;
						xi = 1;
						delta = 1;
						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
						while (delta < eta) {
							K = K + 1;
							xi = xi * ((Gamma * step) / K);
							delta = delta + xi;
						}
					}
					stop = !performTransientMarkovianAnalysis(step, error, Gamma, K, progress);
					progress.setValue((int) i);
					if (stop) {
						return false;
					}
				}
				return !stop;
			}
			else {
				return false;
			}
		}
	}

	private synchronized boolean performTransientMarkovianAnalysis(double timeLimit, double error,
			double Gamma, int K, JProgressBar progress) {
		if (timeLimit == 0.0) {
			return true;
		}
		int progressValue = progress.getValue();
		// Approximate pi(t)
		threadCount = 4;
		waitingThreads = threadCount;
		phase1 = true;
		phase2 = false;
		ArrayList<TransientMarkovMatrixMultiplyThread> threads = new ArrayList<TransientMarkovMatrixMultiplyThread>();
		for (int i = 0; i < threadCount; i++) {
			TransientMarkovMatrixMultiplyThread thread = new TransientMarkovMatrixMultiplyThread(
					this);
			thread.start((i * stateGraph.size()) / threadCount, ((i + 1) * stateGraph.size())
					/ threadCount, Gamma, timeLimit, K);
			threads.add(thread);
		}
		for (int k = 1; k <= K && !stop; k++) {
			while (waitingThreads != 0) {
				try {
					notifyAll();
					wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (stop) {
				for (TransientMarkovMatrixMultiplyThread thread : threads) {
					try {
						thread.join();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
			phase1 = false;
			phase2 = true;
			while (waitingThreads != threadCount) {
				try {
					notifyAll();
					wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (stop) {
				for (TransientMarkovMatrixMultiplyThread thread : threads) {
					try {
						thread.join();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
			phase1 = true;
			phase2 = false;
			progress.setValue(progressValue + ((int) ((timeLimit * k) / K)));
			/*
			 * for (State m : stateGraph) { //for (String state :
			 * stateGraph.keySet()) { // for (State m : stateGraph.get(state)) {
			 * double nextProb = m.getCurrentProb() (1 -
			 * (m.getTransitionSum(0.0, null) / Gamma)); for
			 * (StateTransitionPair prev : m.getPrevStatesWithTrans()) { // if
			 * (lhpn.getTransitionRateTree(prev.getTransition()) // != null) {
			 * // prob = //
			 * lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr
			 * (prev.getState().getVariables()); // } nextProb +=
			 * (prev.getState().getCurrentProb() * (prev.getTransition() /
			 * Gamma)); } m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
			 * m.setPiProb(m.getPiProb() + m.getNextProb()); //} } if (stop) {
			 * return false; } for (State m : stateGraph) { //for (String state
			 * : stateGraph.keySet()) { //for (State m : stateGraph.get(state))
			 * { m.setCurrentProbToNext(); //} } progress.setValue(progressValue
			 * + ((int) ((timeLimit * k) / K)));
			 */
		}
		try {
			while (waitingThreads != 0) {
				try {
					notifyAll();
					wait(10);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (TransientMarkovMatrixMultiplyThread thread : threads) {
				thread.join();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!stop) {
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				m.setPiProb(m.getPiProb() * (Math.pow(Math.E, ((0 - Gamma) * timeLimit))));
				m.setCurrentProbToPi();
				// }
			}
		}
		if (stop) {
			return false;
		}
		return true;
	}

	public synchronized void transientMarkovMatrixMultiplication(int startIndex, int endIndex,
			double Gamma, double timeLimit, int K) {
		for (int k = 1; k <= K && !stop; k++) {
			for (int i = startIndex; i < endIndex; i++) {
				State m = stateGraph.get(i);
				double nextProb = m.getCurrentProb()
						* (1 - (m.getTransitionSum(0.0, null) / Gamma));
				for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
					nextProb += (prev.getState().getCurrentProb() * (prev.getTransition() / Gamma));
				}
				m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
				m.setPiProb(m.getPiProb() + m.getNextProb());
				if (stop) {
					waitingThreads--;
					notifyAll();
					return;
				}
			}
			waitingThreads--;
			if (waitingThreads == 0) {
				notifyAll();
			}
			try {
				while (!phase2 && !stop) {
					wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = startIndex; i < endIndex; i++) {
				State m = stateGraph.get(i);
				m.setCurrentProbToNext();
				if (stop) {
					waitingThreads++;
					notifyAll();
					return;
				}
			}
			waitingThreads++;
			if (waitingThreads == threadCount) {
				notifyAll();
			}
			try {
				while (!phase1 && !stop) {
					wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (stop) {
				return;
			}
		}
		waitingThreads--;
	}

	public void pruneStateGraph(String condition) {
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			ExprTree expr = new ExprTree(lhpn);
			expr.token = expr.intexpr_gettok(condition);
			expr.intexpr_L(condition);
			if (expr.evaluateExpr(m.getVariables()) == 1.0) {
				for (State s : m.getNextStates()) {
					ArrayList<StateTransitionPair> newTrans = new ArrayList<StateTransitionPair>();
					for (StateTransitionPair trans : s.getPrevStatesWithTrans()) {
						if (trans.getState() != m) {
							newTrans.add(trans);
						}
					}
					s.setPrevStatesWithTrans(newTrans.toArray(new StateTransitionPair[0]));
				}
				m.setNextStatesWithTrans(new StateTransitionPair[0]);
			}
			m.setTransitionSum(-1);
			// }
		}
	}

	public boolean performSteadyStateMarkovianAnalysis(double tolerance,
			ArrayList<String> conditions) {
		if (!canPerformMarkovianAnalysis()) {
			stop = true;
			return false;
		}
		else {
			State initial = getInitialState();
			if (initial != null && !stop) {
				resetColorsForMarkovianAnalysis();
				int period = findPeriod(initial);
				if (period == 0) {
					period = 1;
				}
				int step = 0;
				initial.setCurrentProb(1.0);
				boolean done = false;
				if (!stop) {
					do {
						step++;
						step = step % period;
						for (State m : stateGraph) {
							// for (String state : stateGraph.keySet()) {
							// for (State m : stateGraph.get(state)) {
							if (m.getColor() % period == step) {
								double nextProb = 0.0;
								for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
									double transProb = 0.0;
									transProb += prev.getTransition();
									// if
									// (lhpn.getTransitionRateTree(prev.getTransition())
									// !=
									// null) {
									// if
									// (lhpn.getTransitionRateTree(prev.getTransition())
									// !=
									// null) {
									// if
									// (!lhpn.isExpTransitionRateTree(prev
									// .getTransition())
									// &&
									// lhpn.getDelayTree(prev.getTransition())
									// .evaluateExpr(null) == 0) {
									// transProb = 1.0;
									// }
									// else {
									// transProb =
									// lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr(
									// prev.getState().getVariables());
									// }
									// }
									// }
									// else {
									// transProb = 1.0;
									// }
									double transitionSum = prev.getState().getTransitionSum(1.0, m);
									if (transitionSum != 0) {
										transProb = (transProb / transitionSum);
									}
									else {
										transProb = 0.0;
									}
									nextProb += (prev.getState().getCurrentProb() * transProb);
									if (stop) {
										return false;
									}
								}
								if (nextProb != 0.0) {
									m.setNextProb(nextProb);
								}
							}
							if (stop) {
								return false;
							}
						}
						if (stop) {
							return false;
						}
						// }
						boolean change = false;
						for (State m : stateGraph) {
							// for (String state : stateGraph.keySet()) {
							// for (State m : stateGraph.get(state)) {
							if (m.getColor() % period == step) {
								if ((m.getCurrentProb() != 0)
										&& (Math.abs(((m.getCurrentProb() - m.getNextProb()))
												/ m.getCurrentProb()) > tolerance)) {
									change = true;
								}
								else if (m.getCurrentProb() == 0 && m.getNextProb() > tolerance) {
									change = true;
								}
								m.setCurrentProbToNext();
							}
							if (stop) {
								return false;
							}
						}
						if (stop) {
							return false;
						}
						// }
						if (!change) {
							done = true;
						}
					}
					while (!done && !stop);
				}
				if (!stop) {
					double totalProb = 0.0;
					for (State m : stateGraph) {
						// for (String state : stateGraph.keySet()) {
						// for (State m : stateGraph.get(state)) {
						double transitionSum = m.getTransitionSum(1.0, null);
						if (transitionSum != 0.0) {
							m.setCurrentProb((m.getCurrentProb() / period) / transitionSum);
						}
						totalProb += m.getCurrentProb();
						if (stop) {
							return false;
						}
					}
					if (stop) {
						return false;
					}
					// }
					for (State m : stateGraph) {
						// for (String state : stateGraph.keySet()) {
						// for (State m : stateGraph.get(state)) {
						if (totalProb != 0.0) {
							m.setCurrentProb(m.getCurrentProb() / totalProb);
						}
						if (stop) {
							return false;
						}
					}
					if (stop) {
						return false;
					}
					// }
					resetColors();
					HashMap<String, Double> output = new HashMap<String, Double>();
					if (conditions != null && !stop) {
						for (String cond : conditions) {
							double prob = 0;
							// for (String ss : s.split("&&")) {
							// if (ss.split("->").length == 2) {
							// String[] states = ss.split("->");
							// for (String state : stateGraph.keySet()) {
							// for (State m : stateGraph.get(state)) {
							// ExprTree expr = new ExprTree(lhpn);
							// expr.token = expr.intexpr_gettok(states[0]);
							// expr.intexpr_L(states[0]);
							// if (expr.evaluateExpr(m.getVariables()) == 1.0) {
							// for (StateTransitionPair nextState : m
							// .getNextStatesWithTrans()) {
							// ExprTree nextExpr = new ExprTree(lhpn);
							// nextExpr.token = nextExpr
							// .intexpr_gettok(states[1]);
							// nextExpr.intexpr_L(states[1]);
							// if (nextExpr.evaluateExpr(nextState.getState()
							// .getVariables()) == 1.0) {
							// prob += (m.getCurrentProb() * (lhpn
							// .getTransitionRateTree(
							// nextState.getTransition())
							// .evaluateExpr(m.getVariables()) / m
							// .getTransitionSum(1.0, null)));
							// }
							// }
							// if (stop) {
							// return false;
							// }
							// }
							// if (stop) {
							// return false;
							// }
							// }
							// if (stop) {
							// return false;
							// }
							// }
							// }
							// else {
							for (State m : stateGraph) {
								// for (String state : stateGraph.keySet()) {
								// for (State m : stateGraph.get(state)) {
								ExprTree expr = new ExprTree(lhpn);
								expr.token = expr.intexpr_gettok(cond);
								expr.intexpr_L(cond);
								if (expr.evaluateExpr(m.getVariables()) == 1.0) {
									prob += m.getCurrentProb();
								}
								// }
							}
							output.put(cond.trim(), prob);
						}
						String result1 = "#total";
						String result2 = "1.0";
						for (String s : output.keySet()) {
							result1 += " " + s;
							result2 += " " + output.get(s);
						}
						markovResults = result1 + "\n" + result2 + "\n";
					}
				}
			}
			return true;
		}
	}

	public String getMarkovResults() {
		return markovResults;
	}

	public boolean outputTSD(String filename) {
		if (probData != null) {
			probData.outputTSD(filename);
			return true;
		}
		return false;
	}

	private int findPeriod(State state) {
		if (stop) {
			return 0;
		}
		int period = 0;
		int color = 0;
		state.setColor(color);
		color = state.getColor() + 1;
		Queue<State> unVisitedStates = new LinkedList<State>();
		for (State s : state.getNextStates()) {
			if (s.getColor() == -1) {
				s.setColor(color);
				unVisitedStates.add(s);
			}
			else {
				if (period == 0) {
					period = (state.getColor() - s.getColor() + 1);
				}
				else {
					period = gcd(state.getColor() - s.getColor() + 1, period);
				}
			}
			if (stop) {
				return 0;
			}
		}
		while (!unVisitedStates.isEmpty() && !stop) {
			state = unVisitedStates.poll();
			color = state.getColor() + 1;
			for (State s : state.getNextStates()) {
				if (s.getColor() == -1) {
					s.setColor(color);
					unVisitedStates.add(s);
				}
				else {
					if (period == 0) {
						period = (state.getColor() - s.getColor() + 1);
					}
					else {
						period = gcd(state.getColor() - s.getColor() + 1, period);
					}
				}
				if (stop) {
					return 0;
				}
			}
		}
		return period;
	}

	private int gcd(int a, int b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}

	private void resetColorsForMarkovianAnalysis() {
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			m.setColor(-1);
			if (stop) {
				return;
			}
		}
		// if (stop) {
		// return;
		// }
		// }
	}

	public void resetColors() {
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			m.setColor(0);
			if (stop) {
				return;
			}
		}
		// if (stop) {
		// return;
		// }
		// }
	}

	public State getInitialState() {
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (State s : stateGraph) {// .get(createStateVector(variables,
			// allVariables))) {
			if (s.getID().equals("S0")) {
				return s;
			}
		}
		return null;
	}

	public ArrayList<State> getStateGraph() {// HashMap<String,
		// LinkedList<State>>
		// getStateGraph() {
		return stateGraph;
	}

	public int getNumberOfStates() {
		int count = 0;
		// for (String s : stateGraph.keySet()) {
		for (int i = 0; i < stateGraph.size(); i++) {// .get(s).size(); i++) {
			count++;
		}
		// }
		return count;
	}

	private ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private HashMap<String, String> copyAllVariables(HashMap<String, String> original) {
		HashMap<String, String> copy = new HashMap<String, String>();
		for (String s : original.keySet()) {
			copy.put(s, original.get(s));
		}
		return copy;
	}

	private String createStateVector(ArrayList<String> variables,
			HashMap<String, String> allVariables) {
		String vector = "";
		for (String s : variables) {
			if (allVariables.get(s).toLowerCase().equals("true")) {
				vector += "1,";
			}
			else if (allVariables.get(s).toLowerCase().equals("false")) {
				vector += "0,";
			}
			else {
				vector += allVariables.get(s) + ",";
			}
		}
		if (vector.length() > 0) {
			vector = vector.substring(0, vector.length() - 1);
		}
		return vector;
	}

	public void outputStateGraph(String file, boolean withProbs) {
		try {
			NumberFormat num = NumberFormat.getInstance();
			num.setMaximumFractionDigits(6);
			num.setGroupingUsed(false);
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("digraph G {\n");
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				if (withProbs) {
					out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<"
							+ m.stateVector + ">\\nProb = " + num.format(m.getCurrentProb())
							+ "\"]\n");
				}
				else {
					out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<"
							+ m.stateVector + ">\"]\n");
				}
				for (StateTransitionPair next : m.getNextStatesWithTrans()) {
					/*
					 * System.out.println(m.getID() + " -> " +
					 * next.getState().getID() + " [label=\"" +
					 * next.getTransition() + "\\n");
					 * System.out.println(m.getTransitionSum());
					 * System.out.println(lhpn.getTransitionRateTree(
					 * next.getTransition ()).evaluateExpr(m.getVariables()));
					 */
					out.write(m.getID() + " -> " + next.getState().getID() + " [label=\""
							+ next.getTransitionName() + "\\n" + num.format(next.getTransition())
							+ "\"]\n");
					// if (lhpn.getTransitionRateTree(next.getTransition())
					// != null) {
					// out.write(m.getID()
					// + " -> "
					// + next.getState().getID()
					// + " [label=\""
					// + next.getTransition()
					// + "\\n"
					// +
					// num.format((lhpn.getTransitionRateTree(next.getTransition()).evaluateExpr(m
					// .getVariables()) /*
					// * / m . getTransitionSum ( )
					// */)) + "\"]\n");
					// }
					// else {
					// out.write(m.getID() + " -> " +
					// next.getState().getID() +
					// " [label=\"" + next.getTransition() + "\"]\n");
					// }
				}
			}
			// }
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error outputting state graph as dot file.");
		}
	}

	public void stop() {
		stop = true;
	}

	public boolean getStop() {
		return stop;
	}

	private class Transition {
		private String transition;

		private ArrayList<String> markedPlaces;

		private State parent;

		private Transition(String transition, ArrayList<String> markedPlaces, State parent) {
			this.transition = transition;
			this.markedPlaces = markedPlaces;
			this.parent = parent;
		}

		private String getTransition() {
			return transition;
		}

		private ArrayList<String> getMarkedPlaces() {
			return markedPlaces;
		}

		private State getParent() {
			return parent;
		}
	}

	private class StateTransitionPair {
		private double transition;

		private String transitionName;

		private State state;

		private StateTransitionPair(State state, double transition, String transitionName) {
			this.state = state;
			this.transition = transition;
			this.transitionName = transitionName;
		}

		private State getState() {
			return state;
		}

		private double getTransition() {
			return transition;
		}

		private String getTransitionName() {
			return transitionName;
		}
	}

	public class State {
		private String[] markings;

		private StateTransitionPair[] nextStates;

		private StateTransitionPair[] prevStates;

		private String stateVector;

		private String id;

		private int color;

		private double currentProb;

		private double nextProb;

		private double piProb;

		private String variables;

		private double transitionSum;

		public State(String[] markings, StateTransitionPair[] nextStates, String id,
				String stateVector, HashMap<String, String> variables) {
			this.markings = markings;
			this.nextStates = nextStates;
			prevStates = new StateTransitionPair[0];
			this.id = id;
			this.stateVector = stateVector;
			color = 0;
			currentProb = 0.0;
			nextProb = 0.0;
			this.variables = "";
			for (String key : variables.keySet()) {
				if (this.variables.equals("")) {
					this.variables += key + "=" + variables.get(key);
				}
				else {
					this.variables += "," + key + "=" + variables.get(key);
				}
			}
			transitionSum = -1;
		}

		private String getID() {
			return id;
		}

		private HashMap<String, String> getVariables() {
			HashMap<String, String> vars = new HashMap<String, String>();
			if (!variables.equals("")) {
				String[] assignments = variables.split(",");
				for (String assignment : assignments) {
					String[] split = assignment.split("=");
					vars.put(split[0], split[1]);
				}
			}
			return vars;
		}

		private String[] getMarkings() {
			return markings;
		}

		private void setCurrentProb(double probability) {
			currentProb = probability;
		}

		private double getCurrentProb() {
			return currentProb;
		}

		private void setNextProb(double probability) {
			nextProb = probability;
		}

		private double getNextProb() {
			return nextProb;
		}

		private void setPiProb(double probability) {
			piProb = probability;
		}

		private double getPiProb() {
			return piProb;
		}

		private void setCurrentProbToNext() {
			currentProb = nextProb;
		}

		private void setCurrentProbToPi() {
			currentProb = piProb;
		}

		private void setTransitionSum(double transitionSum) {
			this.transitionSum = transitionSum;
		}

		private double getTransitionSum(double noRate, State n) {
			if (transitionSum == -1) {
				transitionSum = 0;
				for (StateTransitionPair next : nextStates) {
					transitionSum += next.getTransition();
					// if (lhpn.getTransitionRateTree(next.getTransition()) !=
					// null) {
					// if
					// (!lhpn.isExpTransitionRateTree(next.getTransition())
					// &&
					// lhpn.getDelayTree(next.getTransition()).evaluateExpr(null)
					// == 0) {
					// if (n == null || next.equals(n)) {
					// return 1.0;
					// }
					// else {
					// return 0.0;
					// }
					// }
					// else {
					// transitionSum +=
					// lhpn.getTransitionRateTree(next.getTransition()).evaluateExpr(variables);
					// }
					// }
					// else {
					// transitionSum += noRate;
					// }
				}
			}
			return transitionSum;
		}

		private StateTransitionPair[] getNextStatesWithTrans() {
			return nextStates;
		}

		private StateTransitionPair[] getPrevStatesWithTrans() {
			return prevStates;
		}

		public State[] getNextStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : nextStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public State[] getPrevStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : prevStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public String getStateVector() {
			return stateVector;
		}

		private void addNextState(State nextState, double transition, String transitionName) {
			StateTransitionPair[] newNextStates = new StateTransitionPair[nextStates.length + 1];
			for (int i = 0; i < nextStates.length; i++) {
				newNextStates[i] = nextStates[i];
			}
			newNextStates[newNextStates.length - 1] = new StateTransitionPair(nextState,
					transition, transitionName);
			nextStates = newNextStates;
			nextState.addPreviousState(this, transition, transitionName);
		}

		private void addPreviousState(State prevState, double transition, String transitionName) {
			StateTransitionPair[] newPrevStates = new StateTransitionPair[prevStates.length + 1];
			for (int i = 0; i < prevStates.length; i++) {
				newPrevStates[i] = prevStates[i];
			}
			newPrevStates[newPrevStates.length - 1] = new StateTransitionPair(prevState,
					transition, transitionName);
			prevStates = newPrevStates;
		}

		private void setNextStatesWithTrans(StateTransitionPair[] trans) {
			nextStates = trans;
		}

		private void setPrevStatesWithTrans(StateTransitionPair[] trans) {
			prevStates = trans;
		}
	}

	public void run() {
	}
}
