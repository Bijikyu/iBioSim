package lhpn2sbml.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biomodelsim.Log;

import verification.Verification;
import verification.AbstPane;

public class Abstraction extends LhpnFile {

	private HashMap<Transition, Integer> process_trans = new HashMap<Transition, Integer>();

	private HashMap<String, Integer> process_write = new HashMap<String, Integer>();

	private HashMap<String, Integer> process_read = new HashMap<String, Integer>();

	private ArrayList<Transition> read = new ArrayList<Transition>();
	
	private ArrayList<String> intVars, newestIntVars;

	private AbstPane abstPane;

	public Abstraction(Log log, Verification pane) {
		super(log);
		this.abstPane = pane.getAbstPane();
	}

	public Abstraction(LhpnFile lhpn, Verification pane) {
		super(lhpn.log);
		this.abstPane = pane.getAbstPane();
		transitions = lhpn.transitions;
		places = lhpn.places;
		booleans = lhpn.booleans;
		continuous = lhpn.continuous;
		integers = lhpn.integers;
		variables = lhpn.variables;
		properties = lhpn.properties;
	}

	public Abstraction(LhpnFile lhpn, AbstPane abst) {
		super(lhpn.log);
		this.abstPane = abst;
		transitions = lhpn.transitions;
		places = lhpn.places;
		booleans = lhpn.booleans;
		continuous = lhpn.continuous;
		integers = lhpn.integers;
		variables = lhpn.variables;
		properties = lhpn.properties;
	}

	public void abstractSTG(boolean print) {
		long start = System.nanoTime();
		boolean change = true;
		if (abstPane.absListModel.contains(abstPane.xform12)) {
			abstractAssign();
		}
		for (Transition t : transitions.values()) {
			if (t.getEnabling() == null) {
				continue;
			}
			if (t.getEnabling().equals("")
					|| t.getEnabling().trim().equals("~shutdown")
					|| t.getEnabling().equals("~fail")) {
				t.addEnabling(null);
			}
		}
		Integer numTrans = transitions.size();
		Integer numPlaces = places.size();
		Integer numVars = variables.size();
		if (print) {
			System.out.println("Transitions before abstraction: " + numTrans);
			System.out.println("Places before abstraction: " + numPlaces);
			System.out.println("Variables before abstraction: " + numVars);
		}
		Integer i = 0;
		while (change && i < abstPane.maxIterations()) {
			change = false;
			divideProcesses();
			intVars = new ArrayList<String>();
			for (String v : abstPane.getIntVars()) {
				intVars.add(v);
			}
			// Transform 0 - Merge Parallel Places
			if (abstPane.absListModel.contains(abstPane.xform0)
					&& abstPane.isSimplify()) {
				change = checkTrans0(change);
			}
			// Transform 1 - Remove a Place in a Self Loop
			if (abstPane.absListModel.contains(abstPane.xform1)
					&& abstPane.isSimplify()) {
				change = checkTrans1(change);
			}
			// Transform 11 - Remove Unused Variables
			// if (abstPane.absListModel.contains(abstPane.xform11) &&
			// abstPane.isSimplify()) {
			// change = removeVars(change);
			// }
			// Transforms 5a, 6, 7 - Combine Transitions with the Same Preset
			// and/or Postset
			if ((abstPane.absListModel.contains(abstPane.xform5)
					|| abstPane.absListModel.contains(abstPane.xform6) || abstPane.absListModel
					.contains(abstPane.xform7))
					&& abstPane.isAbstract()) {
				change = checkTrans5(change);
			}
			// Transform 5b
			if (abstPane.absListModel.contains(abstPane.xform5)
					&& abstPane.isAbstract()) {
				change = checkTrans5b(change);
			}
			// Transform 4 - Remove a Transition with a Single Place in the
			// Preset
			if (abstPane.absListModel.contains(abstPane.xform4)
					&& abstPane.isSimplify()) {
				change = checkTrans4(change);
			}
			// Transform 3 - Remove a Transition with a Single Place in the
			// Postset
			if (abstPane.absListModel.contains(abstPane.xform3)
					&& abstPane.isSimplify()) {
				change = checkTrans3(change);
			}
			// Transform 22 - Remove Vacuous Transitions (simplification)
			if (abstPane.absListModel.contains(abstPane.xform22)
					&& abstPane.isSimplify()) {
				change = checkTrans22(change);
			}
			// Transform 23 - Remove Vacuous Transitions (abstraction)
			if (abstPane.absListModel.contains(abstPane.xform22)
					&& abstPane.isAbstract()) {
				change = checkTrans23(change);
			}
			// Transform 14 - Remove Dead Places
			if (abstPane.absListModel.contains(abstPane.xform14)
					&& abstPane.isSimplify()) {
				change = removeDeadPlaces(change);
			}
			// Transform 8 - Propagate local assignments
			if (abstPane.absListModel.contains(abstPane.xform8)
					&& abstPane.isSimplify()) {
				change = checkTrans8(change);
			}
			// Transform 9 - Remove Write Before Write
			if (abstPane.absListModel.contains(abstPane.xform9)
					&& abstPane.isSimplify()) {
				change = checkTrans9(change);
			}
			// Transform 10 - Simplify Expressions
			if (abstPane.absListModel.contains(abstPane.xform10)
					&& abstPane.isSimplify()) {
				simplifyExpr();
			}
			// Transform 15 - Remove Dead Transitions
			if (abstPane.absListModel.contains(abstPane.xform15)
					&& abstPane.isSimplify()) {
				change = removeDeadTransitions(change);
			}
			// Transform 17 - Remove Dominated Transitions
			if (abstPane.absListModel.contains(abstPane.xform17)
					&& abstPane.isSimplify()) {
				change = removeDominatedTransitions(change);
				change = removeRedundantTransitions(change);
			}
			// Transform 18 - Remove Unread Variables
			if (abstPane.absListModel.contains(abstPane.xform18)
					&& abstPane.isSimplify()) {
				change = removeUnreadVars(change);
			}
			// Transform 20 - Remove Arc after Fail Transition
			if (abstPane.absListModel.contains(abstPane.xform20)
					&& abstPane.isSimplify()) {
				change = removePostFailPlaces(change);
			}
			// Transform 24 - Pairwise Write Before Write
			if (abstPane.absListModel.contains(abstPane.xform24)
					&& abstPane.isSimplify()) {
				change = weakWriteBeforeWrite(change);
			}
			// Transform 25 - Propagate Constant Variable Values
			if (abstPane.absListModel.contains(abstPane.xform25)
					&& abstPane.isSimplify()) {
				change = propagateConst(change);
			}
			// Transform 19 - Merge Coordinated Variables
			if (abstPane.absListModel.contains(abstPane.xform19)
					&& abstPane.isSimplify()) {
				change = mergeCoordinatedVars(change);
				simplifyExpr();
			}
			// Transform 26 - Remove Dangling Transitions
			if (abstPane.absListModel.contains(abstPane.xform26)
					&& abstPane.isSimplify()) {
				change = removeDanglingTransitions(change);
			}
			// Transform 28 - Combing Parallel Transitions (Abstraction)
			if (abstPane.absListModel.contains(abstPane.xform28)
					&& abstPane.isAbstract()) {
				change = mergeTransitionsAbs(change);
			}
			// Transform 27 - Combine Parallel Transitions (Simplification)
			else if (abstPane.absListModel.contains(abstPane.xform27)
					&& abstPane.isSimplify()) {
				change = mergeTransitionsSimp(change);
			}
			// Transform 29 - Remove Uninteresting Variables (Simplification)
			if (abstPane.absListModel.contains(abstPane.xform29) && abstPane.isSimplify()) {
				change = removeUninterestingVariables(change);
			}
			i++;
		}
		// Transform 21 - Normalize Delays
		if (abstPane.absListModel.contains(abstPane.xform21)
				&& abstPane.isAbstract()) {
			normalizeDelays();
		}
		numTrans = transitions.size();
		numPlaces = places.size();
		numVars = variables.size();
		if (print) {
			System.out.println("Transitions after abstraction: " + numTrans);
			System.out.println("Places after abstraction: " + numPlaces);
			System.out.println("Variables after abstraction: " + numVars);
			Double stop = (System.nanoTime() - start) * 1.0e-9;
			System.out.println("Total Abstraction Time: " + stop.toString()
					+ " s");
			System.out.println("Number of Abstraction Loop Iterations: "
					+ i.toString());
		}
	}

	public void abstractVars(String[] intVars) {
		// Remove uninteresting variables
		ArrayList<String> interestingVars = getIntVars(intVars);
		ArrayList<String> vars = new ArrayList<String>(); // The list of
		// uninteresting variables
		for (Variable s : variables) {
			if (!interestingVars.contains(s.getName())) {
				vars.add(s.getName());
			}
		}
		if (vars != null) {
			for (String s : vars) {
				for (Transition t : transitions.values()) {
					if (t.getAssignments().containsKey(s)) {
						// Remove assignments to removed variables
						t.removeAssignment(s);
					}
				}
				// Set initial condition of removed variables to "unknown"
				if (booleans.containsKey(s)) {
					booleans.get(s).addInitValue("unknown");
				} else if (continuous.containsKey(s)) {
					Properties prop = new Properties();
					prop.setProperty("value", "[-INF,INF]");
					prop.setProperty("rate", "[-INF,INF]");
					continuous.get(s).addInitCond(prop);
				} else if (integers.containsKey(s)) {
					integers.get(s).addInitValue("[-INF,INF]");
				}
			}
		}
	}

	public void abstractAssign() {
		for (Transition t : transitions.values()) {
			if (t.getPostset().length > 0) {
				Place[] postset = t.getPostset();
				for (Place p : postset) {
					if (p.getPostset().length > 0) {
						Transition[] postTrans = p.getPostset();
						for (Transition tP : postTrans) {
							boolean flag = true;
							HashMap<String, String> contAssignments = tP
									.getContAssignments();
							if (contAssignments.containsKey(tP.getName())) {
								for (String var : contAssignments.keySet()) {
									if (!t.getContAssignments()
											.containsKey(var)
											|| (tP.getContAssignTree(var).isit != 'c')) {
										flag = false;
									}
								}
							} else {
								flag = false;
							}
							HashMap<String, String> intAssignments = tP
									.getIntAssignments();
							if (intAssignments.containsKey(tP.getName())) {
								for (String var : intAssignments.keySet()) {
									if (!t.getIntAssignments().containsKey(var)
											|| (tP.getIntAssignTree(var).isit != 'c')) {
										flag = false;
									}
								}
							} else {
								flag = false;
							}
							HashMap<String, String> boolAssignments = tP
									.getBoolAssignments();
							if (boolAssignments.containsKey(tP.getName())) {
								for (String var : boolAssignments.keySet()) {
									if (!t.getBoolAssignments()
											.containsKey(var)
											|| (tP.getBoolAssignTree(var).isit != 'c')) {
										flag = false;
									}
								}
							} else {
								flag = false;
							}
							if (flag) {
								for (String var : contAssignments.keySet()) {
									String[] assign = {
											contAssignments.get(var).toString(),
											contAssignments.get(var).toString() };
									String[][] assignRange = new String[2][2];
									Pattern pattern = Pattern
											.compile("\\[(\\S+?),(\\S+?)\\]");
									for (int i = 0; i < assign.length; i++) {
										Matcher matcher = pattern
												.matcher(assign[i]);
										if (matcher.find()) {
											assignRange[i][0] = matcher
													.group(1);
											assignRange[i][1] = matcher
													.group(2);
										} else {
											assignRange[i][0] = assign[i];
											assignRange[i][1] = assign[i];
										}
									}
									if (assignRange[0][0].equals("inf")) {
										assign[0] = assignRange[1][0];
									} else if (assignRange[1][0].equals("inf")) {
										assign[0] = assignRange[0][0];
									} else if (Float
											.parseFloat(assignRange[0][0]) < Float
											.parseFloat(assignRange[1][0])) {
										assign[0] = assignRange[0][0];
									} else {
										assign[0] = assignRange[1][0];
									}
									if (assignRange[0][1].equals("inf")
											|| assignRange[1][1].equals("inf")) {
										assign[1] = "inf";
									} else if (Float
											.parseFloat(assignRange[0][1]) > Float
											.parseFloat(assignRange[1][1])) {
										assign[1] = assignRange[0][1];
									} else {
										assign[1] = assignRange[1][1];
									}
									if (assign[0].equals(assign[1])) {
										tP.addContAssign(var, assign[0]);
									} else {
										tP.addContAssign(var, "[" + assign[0]
												+ "," + assign[1] + "]");
									}
								}
								for (String var : intAssignments.keySet()) {
									String[] assign = {
											intAssignments.get(var).toString(),
											intAssignments.get(var).toString() };
									String[][] assignRange = new String[2][2];
									Pattern pattern = Pattern
											.compile("\\[(\\S+?),(\\S+?)\\]");
									for (int i = 0; i < assign.length; i++) {
										Matcher matcher = pattern
												.matcher(assign[i]);
										if (matcher.find()) {
											assignRange[i][0] = matcher
													.group(1);
											assignRange[i][1] = matcher
													.group(2);
										} else {
											assignRange[i][0] = assign[i];
											assignRange[i][1] = assign[i];
										}
									}
									if (assignRange[0][0].equals("inf")) {
										assign[0] = assignRange[1][0];
									} else if (assignRange[1][0].equals("inf")) {
										assign[0] = assignRange[0][0];
									} else if (Integer
											.parseInt(assignRange[0][0]) < Integer
											.parseInt(assignRange[1][0])) {
										assign[0] = assignRange[0][0];
									} else {
										assign[0] = assignRange[1][0];
									}
									if (assignRange[0][1].equals("inf")
											|| assignRange[1][1].equals("inf")) {
										assign[1] = "inf";
									} else if (Integer
											.parseInt(assignRange[0][1]) > Integer
											.parseInt(assignRange[1][1])) {
										assign[1] = assignRange[0][1];
									} else {
										assign[1] = assignRange[1][1];
									}
									if (assign[0].equals(assign[1])) {
										tP.addIntAssign(var, assign[0]);
									} else {
										tP.addIntAssign(var, "[" + assign[0]
												+ "," + assign[1] + "]");
									}
								}
								for (String var : boolAssignments.keySet()) {
									String[] assign = {
											boolAssignments.get(var).toString(),
											boolAssignments.get(var).toString() };
									String[][] assignRange = new String[2][2];
									Pattern pattern = Pattern
											.compile("\\[(\\S+?),(\\S+?)\\]");
									for (int i = 0; i < assign.length; i++) {
										Matcher matcher = pattern
												.matcher(assign[i]);
										if (matcher.find()) {
											assignRange[i][0] = matcher
													.group(1);
											assignRange[i][1] = matcher
													.group(2);
										} else {
											assignRange[i][0] = assign[i];
											assignRange[i][1] = assign[i];
										}
									}
									if (assignRange[0][0].equals("false")
											|| assignRange[1][0]
													.equals("false")) {
										assign[0] = "false";
									} else {
										assign[0] = "true";
									}
									if (assignRange[0][1].equals("true")
											|| assignRange[1][1].equals("true")) {
										assign[1] = "true";
									} else {
										assign[1] = "false";
									}
									if (assign[0].equals(assign[1])) {
										tP.addBoolAssign(var, assign[0]);
									} else {
										tP.addBoolAssign(var, "[" + assign[0]
												+ "," + assign[1] + "]");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean removeDeadPlaces(boolean change) {
		ArrayList<String> removePlace = new ArrayList<String>();
		for (Place p : places.values()) {
			if (p.getPreset().length == 0 && !p.isMarked()) {// If the place is
				// initially unmarked and has no preset
				for (Transition t : p.getPostset()) { // Remove each transition
					// in the post set
					removeMovement(p.getName(), t.getName());
					for (Place pP : t.getPostset()) {
						removeMovement(t.getName(), pP.getName());
					}
					removeTransition(t.getName());
				}
				removePlace.add(p.getName());
				change = true;
				continue;
			}
			if (p.getPreset().length == 0 && p.getPostset().length == 0) {
				removePlace.add(p.getName()); // Remove unconnected places
			}
		}
		for (Place p : places.values()) {
			if (!change && abstPane.absListModel.contains(abstPane.xform15)) {
				if (p.isMarked())
					continue;
				ArrayList<Place> list = new ArrayList<Place>();
				if (hasMarkedPreset(p, list)) // If the place is not
					// recursively dead
					continue;
				for (Transition t : p.getPostset()) {
					removeMovement(p.getName(), t.getName()); // Remove all
					// transitions in its post set
					for (Place pP : t.getPostset()) {
						removeMovement(t.getName(), pP.getName());
					}
					removeTransition(t.getName());
				}
				for (Transition t : p.getPreset()) {
					removeMovement(t.getName(), p.getName()); // Remove all
					// transitions in its preset
				}
				removePlace.add(p.getName());
				change = true;
			}
		}
		for (String s : removePlace) {
			removePlace(s);
		}
		return change;
	}

	private boolean removeDeadTransitions(boolean change) {
		HashMap<String, String> initVars = new HashMap<String, String>();
		for (Variable v : variables) {
			initVars.put(v.getName(), v.getInitValue());
		}
		ArrayList<String> removeTrans = new ArrayList<String>();
		ArrayList<String> removeEnab = new ArrayList<String>();
		for (Transition t : transitions.values()) {
			ExprTree expr = t.getEnablingTree();
			if (expr == null) {
				continue;
			}
			if (expr.isit == 't') {
				if (expr.uvalue == 0
						&& abstPane.absListModel.contains(abstPane.xform16)
						&& abstPane.isSimplify()) {
					// If the enabling condition is constant false
					removeTrans.add(t.getName());
				} else if (expr.lvalue == 1
						&& abstPane.absListModel.contains(abstPane.xform15)
						&& abstPane.isSimplify()) {
					// If the enabling condition is constant true
					removeEnab.add(t.getName());
				}
			}
			// If the enabling condition is initially true
			if (abstPane.absListModel.contains(abstPane.xform16)
					&& (expr.evaluateExp(initVars) == 1)
					&& abstPane.isSimplify()) {
				boolean enabled = true;
				for (Transition tP : transitions.values()) {
					if (!tP.equals(t)
							&& expr.getChange(tP.getAssignments()) == 'F'
							|| expr.getChange(tP.getAssignments()) == 'f'
							|| expr.getChange(tP.getAssignments()) == 'X') {
						enabled = false;
						break;
					}
				}
				if (enabled) {
					removeEnab.add(t.getName());
				}
			}
			// If the enabling condition is initially false
			else if (abstPane.absListModel.contains(abstPane.xform11)
					&& (expr.evaluateExp(initVars) == 0)
					&& abstPane.isSimplify()) {
				boolean disabled = true;
				for (Transition tP : transitions.values()) {
					if (!tP.getName().equals(t)
							&& expr.getChange(tP.getAssignments()) == 'T'
							|| expr.getChange(tP.getAssignments()) == 't'
							|| expr.getChange(tP.getAssignments()) == 'X') {
						disabled = false;
						break;
					}
				}
				if (disabled) {
					removeTrans.add(t.getName());
				}
			}
		}
		for (String t : removeEnab) {
			transitions.get(t).removeEnabling();
		}
		if (abstPane.absListModel.contains(abstPane.xform15)) {
			for (String t : removeTrans) {
				Transition trans = transitions.get(t);
				for (Place p : trans.getPreset()) {
					removeMovement(p.getName(), t);
				}
				for (Place p : trans.getPostset()) {
					// for (Transition tP : p.getPostset()) {
					// removeMovement(p.getName(), tP.getName());
					// if (tP.getPreset().length == 0) {
					// removeTransition(tP.getName());
					// }
					// }
					removeMovement(t, p.getName());
				}
				removeTransition(t);
			}
		}
		return change;
	}

	private boolean removeDominatedTransitions(boolean change) {
		for (Place p : places.values()) {
			for (Transition t : p.getPostset()) {
				for (Transition tP : p.getPostset()) {
					boolean flag = false;
					if (!t.equals(tP)) {
						if (t.getEnablingTree() == null) {
							continue;
						} else if (tP.getEnablingTree() == null) {
							flag = true;
						} else if (tP.getEnablingTree().implies(
								t.getEnablingTree())) {
							flag = true;
						}
						if (!t.getDelay().contains("uniform")
								&& !t.getDelay().matches("[\\d-]+")) {
							continue;
						}
					}
					if (flag) {
						String delayT = t.getDelay();
						String delayTP = t.getDelay();
						if (delayT != null && delayTP != null) {
							Pattern rangePattern = Pattern
									.compile("uniform\\(([\\d]+),([\\d]+)\\)");
							Matcher delayTMatcher = rangePattern
									.matcher(delayT);
							Matcher delayTpMatcher = rangePattern
									.matcher(delayTP);
							if (delayTMatcher.find() && delayTpMatcher.find()) {
								String lower = delayTpMatcher.group(1);
								String upper = delayTMatcher.group(2);
								if (Integer.parseInt(lower) > Integer
										.parseInt(upper)) {
									for (Place s : tP.getPreset()) {
										removeMovement(s.getName(), tP
												.getName());
									}
									for (Place s : tP.getPreset()) {
										removeMovement(tP.getName(), s
												.getName());
									}
									removeTransition(tP.getName());
									change = true;
								}
							}
						}
					}
				}
			}
		}
		return change;
	}

	private boolean removeRedundantTransitions(boolean change) {
		for (Place p : places.values()) {
			for (Transition t : p.getPostset()) {
				for (Transition tP : p.getPostset()) {
					if (t.getEnabling() != null || tP.getEnabling() != null) {
						if (t.getEnabling() != null && tP.getEnabling() != null) {
							if (!t.getEnabling().equals(tP.getEnabling())) {
								continue;
							}
						} else {
							continue;
						}
					}
					if (!t.getBoolAssignments().equals(tP.getBoolAssignments())) {
						continue;
					}
					if (!t.getIntAssignments().equals(tP.getIntAssignments())) {
						continue;
					}
					if (!t.getContAssignments().equals(tP.getContAssignments())) {
						continue;
					}
					if (!t.getRateAssignments().equals(t.getRateAssignments())) {
						continue;
					}
					if (!t.equals(tP) && t.getDelay() != null
							&& tP.getDelay() != null) {
						String delayT = t.getDelay();
						String delayTP = tP.getDelay();
						if (!delayT.contains("uniform")
								&& !delayT.matches("[\\d-]+")) {
							continue;
						}
						Pattern rangePattern = Pattern
								.compile("uniform\\(([\\d]+),([\\d]+)\\)");
						Matcher delayTMatcher = rangePattern.matcher(delayT);
						Matcher delayTpMatcher = rangePattern.matcher(delayTP);
						if (delayTMatcher.find() && delayTpMatcher.find()) {
							Integer lower, upper;
							Integer lower1 = Integer.parseInt(delayTpMatcher
									.group(1));
							Integer upper1 = Integer.parseInt(delayTpMatcher
									.group(2));
							Integer lower2 = Integer.parseInt(delayTMatcher
									.group(1));
							Integer upper2 = Integer.parseInt(delayTMatcher
									.group(2));
							if (lower1 < lower2) {
								lower = lower1;
							} else {
								lower = lower2;
							}
							if (upper1 > upper2) {
								upper = upper1;
							} else {
								upper = upper2;
							}
							for (Place s : tP.getPreset()) {
								removeMovement(s.getName(), tP.getName());
							}
							for (Place s : tP.getPostset()) {
								removeMovement(tP.getName(), s.getName());
							}
							String delay = "uniform(" + lower.toString() + ","
									+ upper.toString() + ")";
							t.addDelay(delay);
							removeTransition(tP.getName());
							change = true;
						}
					}
				}
			}
		}
		return change;
	}

	private boolean removePostFailPlaces(boolean change) {
		for (Transition t : transitions.values()) {
			if (t.isFail()) {
				for (Place p : t.getPostset()) {
					removeMovement(t.getName(), p.getName());
				}
			}
		}
		return change;
	}

	private boolean removeUnreadVars(boolean change) {
		ArrayList<String> remove = new ArrayList<String>();
		for (Variable v : variables) {
			String s = v.getName();
			boolean isRead = false;
			for (Transition t : transitions.values()) {
				ExprTree enab = t.getEnablingTree();
				if (enab != null) {
					if (enab.containsVar(s)) {
						isRead = true;
						break;
					}
				}
				HashMap<String, ExprTree> boolAssignTrees = t
						.getBoolAssignTrees();
				for (ExprTree e : boolAssignTrees.values()) {
					if (e != null) {
						if (e.containsVar(s)) {
							isRead = true;
							break;
						}
					}
				}
				HashMap<String, ExprTree> intAssignTrees = t
						.getIntAssignTrees();
				for (ExprTree e : intAssignTrees.values()) {
					if (e != null) {
						if (e.containsVar(s)) {
							isRead = true;
							break;
						}
					}
				}
				HashMap<String, ExprTree> contAssignTrees = t
						.getContAssignTrees();
				for (ExprTree e : contAssignTrees.values()) {
					if (e != null) {
						if (e.containsVar(s)) {
							isRead = true;
							break;
						}
					}
				}
				HashMap<String, ExprTree> rateAssignTrees = t
						.getRateAssignTrees();
				for (ExprTree e : rateAssignTrees.values()) {
					if (e != null) {
						if (e.containsVar(s)) {
							isRead = true;
							break;
						}
					}
				}
			}
			if (!isRead) {
				remove.add(s);
				change = true;
			}
		}
		for (String s : remove) {
			removeAllAssignVar(s);
			removeVar(s);
		}
		return change;
	}

	private boolean mergeCoordinatedVars(boolean change) {
		ArrayList<String> remove = new ArrayList<String>();
		ArrayList<String[]> merge = new ArrayList<String[]>();
		HashMap<String, ExprTree> mergeInverse = new HashMap<String, ExprTree>();
		for (Variable var1 : booleans.values()) {
			for (Variable var2 : booleans.values()) {
				if (var1.equals(var2))
					continue;
				if (intVars.contains(var2))
					continue;
				boolean same = areCorrelatedBooleans(var1.getName(), var2
						.getName());
				boolean invert = areInverted(var1.getName(), var2.getName());
				if (same) {
					if (variables.contains(var1)
							&& variables.contains(var2)
							&& (process_read.get(var2.getName()) != -1 || process_read
									.get(var1.getName()) == -1)) {
						String[] temp = { var1.getName(), var2.getName() };
						merge.add(temp);
						// mergeVariables(var1, var2);
						remove.add(var2.getName());
						change = true;
					}
				} else if (invert) {
					ExprTree expr = new ExprTree(this);
					expr.token = expr.intexpr_gettok("~" + var1.getName());
					expr.intexpr_L("~" + var1.getName());
					if (process_read.get(var2.getName()) != -1
							|| process_read.get(var1.getName()) == -1) {
						mergeInverse.put(var2.getName(), expr);
						remove.add(var2.toString());
					}
					// mergeVariables(expr, var2.getName());
				}
			}
		}
		for (Variable var1 : continuous.values()) {
			for (Variable var2 : continuous.values()) {
				if (var1.equals(var2))
					continue;
				boolean same = areCorrelatedContinuous(var1.getName(), var2
						.getName());
				if (same) {
					if (variables.contains(var1)
							&& variables.contains(var2)
							&& (process_read.get(var2.getName()) != -1 || process_read
									.get(var1.getName()) == -1)) {
						String[] temp = { var1.getName(), var2.getName() };
						merge.add(temp);
						// mergeVariables(var1, var2);
						remove.add(var2.getName());
						change = true;
					}
				}
			}
		}
		for (Variable var1 : integers.values()) {
			for (Variable var2 : integers.values()) {
				if (var1.equals(var2))
					continue;
				boolean same = areCorrelatedIntegers(var1.getName(), var2
						.getName());
				if (same) {
					if (variables.contains(var1)
							&& variables.contains(var2)
							&& (process_read.get(var2.getName()) != -1 || process_read
									.get(var1.getName()) == -1)) {
						String[] temp = { var1.getName(), var2.getName() };
						merge.add(temp);
						// mergeVariables(var1, var2);
						remove.add(var2.getName());
						change = true;
					}
				}
			}
		}
		for (String[] s : merge) {
			mergeVariables(s[0], s[1]);
		}
		for (String s : mergeInverse.keySet()) {
			mergeVariables(mergeInverse.get(s), s);
		}
		for (String s : remove) {
			removeVar(s);
		}
		return change;
	}

	private boolean hasMarkedPreset(Place place, ArrayList<Place> list) {
		if (list.contains(place)) {
			return false;
		}
		list.add(place);
		for (Transition t : place.getPreset()) {
			for (Place p : t.getPreset()) {
				if (p.isMarked())
					return true;
				else if (hasMarkedPreset(p, list))
					return true;
			}
		}
		return false;
	}

	private ArrayList<String> getIntVars(String[] oldIntVars) {
		ArrayList<String> intVars = new ArrayList<String>();
		if (booleans.containsKey("fail"))
			intVars.add("fail");
		else if (booleans.containsKey("shutdown"))
			intVars.add("shutdown");
		for (String s : oldIntVars) {
			if (!intVars.contains(s))
				intVars.add(s);
		}
		ArrayList<String> tempIntVars = new ArrayList<String>();
		tempIntVars = intVars;
		do {
			intVars = new ArrayList<String>();
			for (String s : tempIntVars) {
				intVars.add(s);
			}
			for (Transition t : transitions.values()) {
				for (ExprTree e : t.getContAssignTrees().values()) {
					tempIntVars.addAll(e.getVars());
				}
				for (ExprTree e : t.getRateAssignTrees().values()) {
					tempIntVars.addAll(e.getVars());
				}
				for (ExprTree e : t.getIntAssignTrees().values()) {
					tempIntVars.addAll(e.getVars());
				}
				for (ExprTree e : t.getBoolAssignTrees().values()) {
					tempIntVars.addAll(e.getVars());
				}
			}
		} while (!intVars.equals(tempIntVars));
		tempIntVars = intVars;
		do {
			intVars = new ArrayList<String>();
			for (String s : tempIntVars) {
				intVars.add(s);
			}
			for (String var : intVars) {
				ArrayList<Transition> process = new ArrayList<Transition>();
				for (Transition t : transitions.values()) {
					if (t.getAssignments().containsKey(var)) {
						process.add(t);
					}
				}
				ArrayList<Transition> tempProcess = new ArrayList<Transition>();
				for (Transition t : process) {
					tempProcess.add(t);
				}
				do {
					process = new ArrayList<Transition>();
					process.addAll(tempProcess);
					for (Transition t : process) {
						for (Place p : t.getPostset()) {
							for (Transition tP : p.getPostset()) {
								if (!tempProcess.contains(tP)) {
									tempProcess.add(tP);
								}
							}
						}
						for (Place p : t.getPreset()) {
							for (Transition tP : p.getPreset()) {
								if (!tempProcess.contains(tP)) {
									tempProcess.add(tP);
								}
							}
						}
					}
				} while (!tempProcess.equals(process));
				for (Transition trans : process) {
					for (String s : trans.getEnablingTree().getVars()) {
						if (!tempIntVars.contains(s))
							tempIntVars.add(s);
					}
				}
			}
		} while (!intVars.equals(tempIntVars));
		return intVars;
	}

	private boolean comparePreset(Place p1, Place p2) {
		Transition[] pre1 = p1.getPreset();
		Transition[] pre2 = p2.getPreset();
		if (pre1.length != pre2.length || pre1.length == 0) {
			return false;
		}
		for (Transition t1 : pre1) {
			boolean contains = false;
			for (Transition t2 : pre2) {
				if (t1 == t2) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePreset(Place p1, Place p2, Transition trans1,
			Transition trans2) {
		Transition[] set1 = p1.getPreset();
		Transition[] set2 = p2.getPreset();
		if (set1.length != set2.length || set1.length == 0) {
			return false;
		}
		for (Transition t1 : set1) {
			boolean contains = false;
			for (Transition t2 : set2) {
				if (t1.equals(t2) || t1.equals(trans1) || t1.equals(trans2)) {
					contains = true;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePreset(Transition t1, Transition t2) {
		Place[] pre1 = t1.getPreset();
		Place[] pre2 = t2.getPreset();
		if (pre1.length != pre2.length || pre1.length == 0) {
			return false;
		}
		for (Place p1 : pre1) {
			boolean contains = false;
			for (Place p2 : pre2) {
				if (p1.equals(p2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePostset(Place p1, Place p2) {
		Transition[] pre1 = p1.getPostset();
		Transition[] pre2 = p2.getPostset();
		if (pre1.length != pre2.length || pre1.length == 0) {
			return false;
		}
		for (Transition t1 : pre1) {
			boolean contains = false;
			for (Transition t2 : pre2) {
				if (t1.equals(t2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePostset(Place p1, Place p2, Transition trans1,
			Transition trans2) {
		Transition[] set1 = p1.getPostset();
		Transition[] set2 = p2.getPostset();
		if (set1.length != set2.length || set1.length == 0) {
			return false;
		}
		for (Transition t1 : set1) {
			boolean contains = false;
			for (Transition t2 : set2) {
				if (t1.equals(t2) || t1.equals(trans1) || t1.equals(trans2)) {
					contains = true;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePostset(Transition t1, Transition t2) {
		Place[] post1 = t1.getPostset();
		Place[] post2 = t2.getPostset();
		if (post1.length != post1.length || post1.length == 0) {
			return false;
		}
		for (Place p1 : post1) {
			boolean contains = false;
			for (Place p2 : post2) {
				if (p1.equals(p2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private void combinePlaces(Place place1, Place place2) {
		for (Transition t : place1.getPreset()) {
			addMovement(t.getName(), place1.getName());
			removeMovement(t.getName(), place2.getName());
		}
		for (Transition t : place1.getPostset()) {
			addMovement(place1.getName(), t.getName());
			removeMovement(place2.getName(), t.getName());
		}
		removePlace(place2.getName());
	}

	private boolean checkTrans0(boolean change) {
		ArrayList<Place[]> merge = new ArrayList<Place[]>();
		for (Place p1 : places.values()) {
			for (Place p2 : places.values()) {
				if (!p1.equals(p2)) {
					boolean assign = false;
					if (comparePreset(p1, p2) && comparePostset(p1, p2)
							&& (p1.isMarked() == p2.isMarked()) && !assign) {
						Place[] temp = { p1, p2 };
						merge.add(temp);
					}
				}
			}
		}
		for (Place[] a : merge) {
			change = true;
			combinePlaces(a[0], a[1]);
		}
		return change;
	}

	private boolean checkTrans1(boolean change) {
		ArrayList<Place> remove = new ArrayList<Place>();
		for (Place p : places.values()) {
			Transition[] preset = p.getPreset();
			Transition[] postset = p.getPostset();
			if (preset.length == 1 && postset.length == 1) {
				if (preset[0].equals(postset[0]) && !p.isMarked()) {
					remove.add(p);
				} else {
					continue;
				}
			}
		}
		for (Place p : remove) {
			change = true;
			removePlace(p.getName());
		}
		return change;
	}

	private boolean checkTrans5(boolean change) {
		ArrayList<Transition[]> combine = new ArrayList<Transition[]>();
		HashMap<Transition, boolean[]> samesets = new HashMap<Transition, boolean[]>();
		for (Transition t1 : transitions.values()) {
			if (!(t1.getDelayTree().isit == 'n' || (t1.getDelayTree().isit == 'a' && t1
					.getDelayTree().op.equals("uniform")))) {
				continue;
			}
			for (Transition t2 : transitions.values()) {
				if (!(t2.getDelayTree().isit == 'n' || (t2.getDelayTree().isit == 'a' && t2
						.getDelayTree().op.equals("uniform")))) {
					continue;
				}
				if (!t1.equals(t2)) {
					boolean samePreset = comparePreset(t1, t2);
					boolean samePostset = comparePostset(t1, t2);
					boolean assign = hasAssignments(t1) || hasAssignments(t2);
					if ((samePreset && samePostset) && !assign) {
						Transition[] array = { t1, t2 };
						boolean[] same = { samePreset, samePostset };
						combine.add(array);
						samesets.put(t1, same);
					} else if (samePreset && !assign
							&& abstPane.absListModel.contains(abstPane.xform6)) {
						Place[] postset1 = t1.getPostset();
						Place[] postset2 = t2.getPostset();
						if (postset1.length == 1 && postset2.length == 1) {
							if (comparePreset(
									places.get(postset1[0].getName()), places
											.get(postset2[0].getName()), t1, t2)) {
								Transition[] array = { t1, t2 };
								boolean[] same = { samePreset, samePostset };
								combine.add(array);
								samesets.put(t1, same);
							}
						}
					} else if (samePostset && !assign
							&& abstPane.absListModel.contains(abstPane.xform7)) {
						Place[] preset1 = t1.getPreset();
						Place[] preset2 = t1.getPreset();
						if (preset1.length == 1 && preset2.length == 1) {
							if (comparePostset(
									places.get(preset1[0].getName()), places
											.get(preset2[0].getName()), t1, t2)
									&& !t2.isFail()) {
								Transition[] array = { t1, t2 };
								boolean[] same = { samePreset, samePostset };
								combine.add(array);
								samesets.put(t1, same);
							}
						}
					}
				}
			}
		}
		for (Transition[] s : combine) {
			change = true;
			combineTransitions(s[0], s[1], samesets.get(s[0])[0], samesets
					.get(s[0])[1]);
		}
		return change;
	}

	private boolean checkTrans5b(boolean change) {
		ArrayList<Transition[]> combine = new ArrayList<Transition[]>();
		for (Transition t1 : transitions.values()) {
			if (!(t1.getDelayTree().isit == 'n' || (t1.getDelayTree().isit == 'a' && t1
					.getDelayTree().op.equals("uniform")))) {
				continue;
			}
			for (Transition t2 : transitions.values()) {
				if (!(t2.getDelayTree().isit == 'n' || (t2.getDelayTree().isit == 'a' && t2
						.getDelayTree().op.equals("uniform")))) {
					continue;
				}
				boolean transform = true;
				if (!t1.equals(t2)) {
					boolean assign = hasAssignments(t1) || hasAssignments(t2);
					if (!assign) {
						for (Place p1 : t1.getPreset()) {
							if (transform) {
								for (Place p2 : t2.getPreset()) {
									if (!p1.equals(p2)) {
										if (!comparePreset(p1, p2)) {
											transform = false;
											break;
										}
									}
								}
							}
						}
					}
					if (transform) {
						for (Place p1 : t1.getPostset()) {
							for (Place p2 : t2.getPostset()) {
								if (!p1.equals(p2)) {
									if (!comparePostset(p1, p2)) {
										transform = false;
										break;
									}
								}
							}
						}
					}
					if (transform && !assign) {
						Transition[] array = { t1, t2 };
						combine.add(array);
					}
				}
			}
		}
		for (Transition[] s : combine) {
			if (!s[1].isFail()) {
				change = true;
				combineTransitions(s[0], s[1], true, true);
			}
		}
		return change;
	}

	private boolean checkTrans3(boolean change) {
		// Remove a transition with a single place in the postset
		ArrayList<Transition> remove = new ArrayList<Transition>();
		for (Transition t : transitions.values()) {
			Place[] postset = t.getPostset();
			if (postset.length == 1 && !postset[0].equals("")) {
				boolean assign = hasAssignments(t);
				boolean post = true;
				for (Place p : t.getPreset()) {
					if (p.getPostset().length != 1) {
						post = false;
					}
				}
				if ((postset[0].getPreset().length == 1 || post) && !assign) {
					remove.add(t);
				}
			}
		}
		for (Transition t : remove) {
			Place[] postset = t.getPostset();
			if (postset.length == 1 && !t.isFail()) {
				if (removeTrans3(t))
					change = true;
			}
		}
		return change;
	}

	private boolean checkTrans4(boolean change) {
		// Remove a Transition with a Single Place in the Preset
		ArrayList<Transition> remove = new ArrayList<Transition>();
		for (Transition t : transitions.values()) {
			Place[] preset = t.getPreset();
			if (preset.length == 1) {
				boolean assign = hasAssignments(t);
				boolean pre = true;
				for (Place p : t.getPostset()) {
					if (p.getPreset().length != 1) {
						pre = false;
					}
				}
				Transition[] postset = preset[0].getPostset();
				if ((postset.length == 1 || pre)
						&& places.containsKey(preset[0])) {
					if (!assign) {
						remove.add(t);
					}
				}
			}
		}
		for (Transition t : remove) {
			if (!t.isFail()) {
				if (removeTrans4(t))
					change = true;
			}
		}
		return change;
	}

	private boolean checkTrans22(boolean change) {
		// Remove Vacuous Transitions - Abstraction
		ArrayList<Transition> remove = new ArrayList<Transition>();
		for (Transition t : transitions.values()) {
			if (!(t.getDelayTree().isit == 'n' || (t.getDelayTree().isit == 'a' && t
					.getDelayTree().op.equals("uniform")))) {
				continue;
			}
			Place[] postset = t.getPostset();
			Place[] preset = t.getPreset();
			if (postset.length == 1 && preset.length == 1) {
				boolean assign = false;
				if (isGloballyDisabled(t)) {
					assign = true;
					break;
				}
				if (!assign) {
					if (t.getAssignments().size() > 0) {
						assign = true;
					}
				}
				boolean post = true;
				Transition[] tempPreset;
				for (Place p : t.getPreset()) {
					if (p.getPostset().length != 1) {
						post = false;
					}
				}
				tempPreset = postset[0].getPreset();
				if (!((tempPreset.length == 1 || post) && !assign)) {
					assign = true;
				}
				boolean pre = true;
				for (Place p : t.getPostset()) {
					if (p.getPreset().length != 1) {
						pre = false;
					}
				}
				Transition[] tempPostset = preset[0].getPostset();
				if ((tempPostset.length == 1 || pre)
						&& places.containsKey(preset[0])) {
					if (!assign) {
						remove.add(t);
					}
				}
			}
		}
		for (Transition t : remove) {
			Place[] postset = t.getPostset();
			if (postset.length == 1 && !postset[0].equals("") && !t.isFail()) {
				if (removeVacTransAbs(t))
					change = true;
			}
		}
		return change;
	}

	private boolean checkTrans23(boolean change) {
		// Remove Vacuous Transitions
		ArrayList<Transition> remove = new ArrayList<Transition>();
		for (Transition t : transitions.values()) {
			if (!(t.getDelayTree().isit == 'n' || (t.getDelayTree().isit == 'a' && t
					.getDelayTree().op.equals("uniform")))) {
				continue;
			}
			Place[] postset = t.getPostset();
			Place[] preset = t.getPreset();
			if (postset.length == 1 && preset.length == 1) {
				boolean assign = false;
				if (t.getEnablingTree() != null) {
					for (String var : t.getEnablingTree().getVars()) {
						if (!process_write.get(var)
								.equals(process_trans.get(t))) {
							assign = true;
							break;
						}
					}
				}
				if (t.getDelayTree().isit == 'a') {
					if (!t.getDelayTree().op.equals("uniform") || t.getDelayTree().r1.isit != 'n' || t.getDelayTree().r2.isit != 'n') {
						assign = true;
						break;
					}
				}
				else if (t.getDelayTree().isit != 'n') {
					assign = true;
					break;
				}
				if (!assign) {
					if (t.getAssignments().size() > 0) {
						assign = true;
					}
				}
				boolean post = true;
				Transition[] tempPreset;
				for (Place p : t.getPreset()) {
					if (p.getPostset().length != 1) {
						post = false;
					}
				}
				tempPreset = postset[0].getPreset();
				if (!((tempPreset.length == 1 || post) && !assign)) {
					assign = true;
				}
				boolean pre = true;
				for (Place p : t.getPostset()) {
					if (p.getPreset().length != 1) {
						pre = false;
					}
				}
				Transition[] tempPostset = preset[0].getPostset();
				if ((tempPostset.length == 1 || pre)
						&& places.containsKey(preset[0].getName())) {
					if (!assign) {
						remove.add(t);
					}
				}
			}
		}
		for (Transition t : remove) {
			Place[] postset = t.getPostset();
			if (postset.length == 1 && !postset[0].equals("") && !t.isFail()) {
				if (removeVacTrans(t))
					change = true;
			}
		}
		return change;
	}

	private boolean checkTrans8(boolean change) {
		// Propagate expressions of local variables to transition post sets
		ArrayList<Place> initMarking = new ArrayList<Place>();
		ArrayList<Transition> unvisited = new ArrayList<Transition>();
		unvisited.addAll(transitions.values());
		for (Place p : places.values()) {
			if (p.isMarked()) {
				initMarking.add(p);
			}
		}
		for (int i = 0; i < 2; i++) {
			for (Place p : initMarking) {
				for (Transition t : p.getPostset()) {
					change = trans8Iteration(t, unvisited, change);
				}
			}
		}
		return change;
	}

	private boolean checkTrans9(boolean change) {
		// Remove Write Before Write
		ArrayList<String[]> remove = new ArrayList<String[]>();
		for (Transition t : transitions.values()) {
			for (String var : t.getAssignments().keySet()) {
				read = new ArrayList<Transition>();
				if ((process_read.get(var).equals(process_trans.get(t)) && process_write
						.get(var).equals(process_trans.get(t)))
						&& !readBeforeWrite(t, var)) {
					String[] temp = { t.getName(), var };
					remove.add(temp);
				}
			}
		}
		for (String[] temp : remove) {
			transitions.get(temp[0]).removeAssignment(temp[1]);
			change = true;
		}
		return change;
	}

	private boolean weakWriteBeforeWrite(boolean change) {
		ArrayList<String[]> remove = new ArrayList<String[]>();
		for (Transition t : transitions.values()) {
			for (String var : t.getAssignments().keySet()) {
				read = new ArrayList<Transition>();
				// Check read variables for global writes
				if ((process_read.get(var).equals(process_trans.get(t)) && process_write
						.get(var).equals(process_trans.get(t)))
						&& !weakReadBeforeWrite(t, var)) {
					String[] temp = { t.getName(), var };
					remove.add(temp);
				}
			}
		}
		for (String[] temp : remove) {
			transitions.get(temp[0]).removeAssignment(temp[1]);
			change = true;
		}
		return change;
	}

	private boolean removeUninterestingVariables(boolean change) {
		ArrayList <String> allIntVars = new ArrayList<String>(); // Set V
		ArrayList <String> newIntVars = new ArrayList<String>(); // Set V''
		ArrayList <String> newestIntVars = new ArrayList<String>(); // Set V'
		ArrayList<Integer> intProc = new ArrayList<Integer>(); // Processes with failure transitions or transitions that have interesting variables in their enabling conditions
		for (String v : abstPane.getIntVars()) {
			allIntVars.add(v);
			newIntVars.add(v);
		}
		for (Transition t : transitions.values()) {
			if (t.isFail()) {
				intProc.add(process_trans.get(t));
			}
		}
		for (Transition t : transitions.values()) {
			if (intProc.contains(process_trans.get(t)) && t.getEnablingTree() != null) {
				for (String u : t.getEnablingTree().getVars()) {
					if (!allIntVars.contains(u)) {
						allIntVars.add(u);
						newIntVars.add(u);
					}
				}
			}
		}
		do {
			for (Transition t : transitions.values()) { // Determine which processes are interesting
				for (String v : newIntVars) {
					if (t.getEnablingTree() != null) {
					if (t.getEnablingTree().getVars().contains(v)) {
						intProc.add(process_trans.get(t));
					if (t.getEnablingTree() != null) {
						if (t.getEnablingTree().getVars().contains(v)) {
							intProc.add(process_trans.get(t));
						}
					}
					}
				}
			}
			}
			for (Transition t : transitions.values()) {
				for (String key : t.getAssignTrees().keySet()) {
					if (allIntVars.contains(key)) {
						for (String v : t.getAssignTree(key).getVars()) {
							if (!allIntVars.contains(v)) {
								addInterestingVariable(v);
							}
						}
					}
				}
				if (intProc.contains(process_trans.get(t)) && t.getEnablingTree() != null) {
					for (String v : t.getEnablingTree().getVars()) {
						if (!allIntVars.contains(v)) {
							addInterestingVariable(v);
						}
					}
				}
			}
			for (String v : newestIntVars) {
				if (!allIntVars.contains(v)) {
					newIntVars.add(v);
					allIntVars.add(v);
				}
			}
		} while (newestIntVars.size() > 0);
		ArrayList<Variable> removeVars = new ArrayList<Variable>();
		for (Variable v : variables) {
			if (!allIntVars.contains(v.getName())) {
				removeVars.add(v);
				change = true;
			}
		}
		for (Variable v : removeVars) {
			removeAllAssignVar(v.getName());
			removeVar(v.getName());
		}
		return change;
	}

	private void simplifyExpr() {
		for (Transition t : transitions.values()) {
			t.simplifyExpr();
		}
	}

	// private void normalizeDelays() {
	// int N = abstPane.getNormFactor();
	// for (Transition t : transitions.values()) {
	// String delay = t.getDelay();
	// if (delay != null) {
	// Pattern pattern = Pattern.compile("uniform\\(([\\w-]+?),([\\w-]+?)\\)");
	// Matcher matcher = pattern.matcher(delay);
	// Pattern normPattern =
	// Pattern.compile("uniform\\(([\\w-]+?),([\\w-]+?)\\)");
	// Matcher normMatcher = normPattern.matcher(delay);
	// Pattern numPattern = Pattern.compile("[-\\d]+");
	// Matcher numMatcher = numPattern.matcher(delay);
	// if (matcher.find()) {
	// String lVal = matcher.group(1);
	// String uVal = matcher.group(2);
	// if (!lVal.contains("inf")) {
	// Integer lInt = Integer.parseInt(lVal);
	// lInt = (lInt / N) * N;
	// lVal = lInt.toString();
	// }
	// if (!uVal.contains("inf")) {
	// Integer uInt = Integer.parseInt(uVal);
	// if (uInt % N != 0) {
	// uInt = (uInt / N + 1) * N;
	// uVal = uInt.toString();
	// }
	// }
	// delay = "uniform(" + lVal + "," + uVal + ")";
	// t.addDelay(delay);
	// }
	// else if (normMatcher.find()) {
	// String lVal = normMatcher.group(1);
	// String uVal = normMatcher.group(2);
	// if (!lVal.contains("inf")) {
	// Integer lInt = Integer.parseInt(lVal);
	// lInt = (lInt / N) * N;
	// lVal = lInt.toString();
	// }
	// if (!uVal.contains("inf")) {
	// Integer uInt = Integer.parseInt(uVal);
	// if (uInt % N != 0) {
	// uInt = (uInt / N + 1) * N;
	// uVal = uInt.toString();
	// }
	// }
	// delay = "normal(" + lVal + "," + uVal + ")";
	// t.addDelay(delay);
	// }
	// else if (numMatcher.find()) {
	// Integer val = Integer.parseInt(delay);
	// Integer lInt = (val / N) * N;
	// String lVal = lInt.toString();
	// Integer uInt = lInt + N;
	// String uVal = uInt.toString();
	// delay = "uniform(" + lVal + "," + uVal + ")";
	// t.addDelay(delay);
	// }
	// }
	// }
	// }

	private void normalizeDelays() {
		int N = abstPane.getNormFactor();
		for (Transition t : transitions.values()) {
			if (!t.containsDelay()) {
				continue;
			}
			ExprTree delay = t.getDelayTree();
			switch (delay.isit) {
			case 'n':
				double val = delay.lvalue;
				if (val % N == 0) {
					continue;
				}
				int lower = (int) (val / N) * N;
				Integer lInt = (Integer) lower;
				String lVal = lInt.toString();
				Integer uInt = lInt + N;
				String uVal = uInt.toString();
				t.addDelay("uniform(" + lVal + "," + uVal + ")");
				break;
			case 'a':
				if (delay.op.equals("uniform")) {
					lower = (int) delay.r1.lvalue;
					lInt = (Integer) lower;
					int upper = (int) delay.r2.uvalue;
					uInt = (Integer) upper;
					if (lower == upper) {
						continue;
					}
					if (lower != INFIN) {
						lInt = (lInt / N) * N;
						lVal = lInt.toString();
					} else {
						lVal = "inf";
					}
					if (upper != INFIN) {
						if (uInt % N != 0) {
							uInt = (uInt / N + 1) * N;
							uVal = uInt.toString();
						} else {
							uVal = lVal;
						}
					} else {
						uVal = "inf";
					}
					t.addDelay("uniform(" + lVal + "," + uVal + ")");
				}
				break;
			default:
				break;
			}
		}
	}

	private boolean removeTrans3(Transition transition) {
		// Remove a transition with a single place in the postset
		// if (!transition.getDelay().contains("uniform")
		// && !transition.getDelay().matches("[\\d-]+")) {
		// return false;
		// }
		Place place = transition.getPostset()[0];
		Place[] preset = transition.getPreset();
		Transition[] postset = place.getPostset();
		Transition[] placePreset = place.getPreset();
		boolean marked = place.isMarked();
		// Check to make sure that the place is not a self-loop
		if (preset.length == 1) {
			Transition[] tempPreset = preset[0].getPreset();
			if (tempPreset.length == 1 && tempPreset[0].equals(transition)) {
				return false;
			}
		}
		if (transition.isFail()) {
			return false;
		}
		// Update control flow
		removeMovement(transition.getName(), place.getName());
		for (Place p : preset) {
			if (marked)
				p.setMarking(true);
			removeMovement(p.getName(), transition.getName());
			for (Transition t : postset) {
				addMovement(p.getName(), t.getName());
			}
		}
		for (Transition t : placePreset) {
			removeMovement(t.getName(), place.getName());
			for (Place p : preset) {
				if (!p.equals(place) && !t.equals(transition)) {
					addMovement(t.getName(), p.getName());
				}
			}
		}
		for (Transition t : postset) {
			if (t.getEnablingTree() != null) {
				ExprTree expr = t.getEnablingTree();
				if (transition.getEnablingTree() != null) {
					expr.setNodeValues(expr, transition.getEnablingTree(),
							"&&", 'l');
					t.addEnabling(expr.toString("LHPN"));
				}
			} else if (transition.getEnablingTree() != null) {
				t.addEnabling(transition.getEnablingTree().toString());
			}
			removeMovement(place.getName(), t.getName());
		}
		removePlace(place.getName());
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		if (transition.getDelay() != null) {
			Matcher rangeMatcher = rangePattern.matcher(transition.getDelay());
			if (rangeMatcher.find()) {
				oldDelay[0] = rangeMatcher.group(1);
				oldDelay[1] = rangeMatcher.group(2);
			}
		} else {
			oldDelay[0] = "0";
			oldDelay[1] = "inf";
		}
		for (Transition t : postset) {
			if (t.getDelay() != null) {
				Matcher newMatcher = rangePattern.matcher(t.getDelay());
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1),
							newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf")
								&& !newDelay[i].equals("inf")) {
							if (i != 0 || !marked) {
								newDelay[i] = String.valueOf(Integer
										.parseInt(newDelay[i])
										+ Integer.parseInt(oldDelay[i]));
							}
						} else {
							newDelay[i] = "inf";
						}
					}
					t.addDelay("uniform(" + newDelay[0] + "," + newDelay[1]
							+ ")");
				}
			}
		}
		removeTransition(transition.getName());
		return true;
	}

	private boolean removeTrans4(Transition transition) {
		// Remove a transition with a single place in the preset
		// if (!transition.getDelay().contains("uniform")
		// && !transition.getDelay().matches("[\\d-]+")) {
		// return false;
		// }
		Place place = transition.getPreset()[0];
		Transition[] preset = place.getPreset();
		Place[] postset = transition.getPostset();
		Transition[] placePostset = place.getPostset();
		// Check to make sure that the place is not a self-loop
		if (postset.length == 1) {
			Transition[] tempPostset = postset[0].getPostset();
			if (tempPostset.length == 1 && tempPostset[0].equals(transition)) {
				return false;
			}
		}
		if (transition.isFail()) {
			return false;
		}
		boolean marked = place.isMarked();
		// Update the control Flow
		removeMovement(place.getName(), transition.getName());
		for (Transition t : preset) {
			removeMovement(t.getName(), place.getName());
			for (Place p : postset) {
				addMovement(t.getName(), p.getName());
			}
		}
		for (Transition t : placePostset) {
			removeMovement(place.getName(), t.getName());
		}
		for (Place p : postset) {
			removeMovement(transition.getName(), p.getName());
			for (Transition t : placePostset) {
				if (!t.equals(transition)) {
					addMovement(p.getName(), t.getName());
				}
			}
			if (marked) {
				p.setMarking(true);
			}
		}
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		Matcher rangeMatcher = rangePattern.matcher(transition.getDelay());
		if (rangeMatcher.find()) {
			oldDelay[0] = rangeMatcher.group(1);
			oldDelay[1] = rangeMatcher.group(2);
		}
		HashMap<Transition, Boolean> postTrans = new HashMap<Transition, Boolean>();
		for (Place p : postset) {
			for (Transition t : p.getPostset()) {
				postTrans.put(t, p.isMarked());
			}
		}
		for (Transition t : postTrans.keySet()) {
			if (t.getDelay() != null) {
				Matcher newMatcher = rangePattern.matcher(t.getDelay());
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1),
							newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf")
								&& !newDelay[i].equals("inf")) {
							newDelay[i] = String.valueOf(Integer
									.parseInt(newDelay[i])
									+ Integer.parseInt(oldDelay[i]));
						} else {
							newDelay[i] = "inf";
						}
					}
					t.addDelay("uniform(" + newDelay[0] + "," + newDelay[1]
							+ ")");
				}
			}
		}
		removePlace(place.getName());
		removeTransition(transition.getName());
		return true;
	}

	private boolean removeVacTrans(Transition transition) {
		// if (!transition.getDelay().contains("uniform")
		// && !transition.getDelay().matches("[\\d-]+")) {
		// return false;
		// }
		Place place = transition.getPostset()[0];
		Place[] preset = transition.getPreset();
		Transition[] postset = place.getPostset();
		Transition[] placePreset = place.getPreset();
		boolean marked = place.isMarked();
		// Check to make sure that the place is not a self-loop
		if (preset.length == 1) {
			Transition[] tempPreset = preset[0].getPreset();
			if (tempPreset.length == 1 && tempPreset[0].equals(transition)) {
				return false;
			}
		}
		if (transition.isFail()) {
			return false;
		}
		// Update control flow
		removeMovement(transition.getName(), place.getName());
		for (Place p : preset) {
			places.put(p.getName(), p);
			if (marked)
				p.setMarking(true);
			removeMovement(p.getName(), transition.getName());
			for (Transition t : postset) {
				addMovement(p.getName(), t.getName());
			}
		}
		for (Transition t : placePreset) {
			removeMovement(t.getName(), place.getName());
			for (Place p : preset) {
				if (!p.equals(place) && !t.equals(transition)) {
					addMovement(t.getName(), p.getName());
				}
			}
		}
		for (Transition t : postset) {
			if (t.getEnablingTree() != null) {
				ExprTree expr = t.getEnablingTree();
				if (transition.getEnablingTree() != null) {
					expr.setNodeValues(expr, transition.getEnablingTree(),
							"&&", 'l');
					t.addEnabling(expr.toString("LHPN"));
				}
			} else if (transition.getEnablingTree() != null) {
				t.addEnabling(transition.getEnablingTree().toString("LHPN"));
			}
			removeMovement(place.getName(), t.getName());
		}
		removePlace(place.getName());
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		if (transition.getDelay() != null) {
			Matcher rangeMatcher = rangePattern.matcher(transition.getDelay());
			if (rangeMatcher.find()) {
				oldDelay[0] = rangeMatcher.group(1);
				oldDelay[1] = rangeMatcher.group(2);
			}
		} else {
			oldDelay[0] = "0";
			oldDelay[1] = "inf";
		}
		for (Transition t : postset) {
			if (t.getDelay() != null) {
				Matcher newMatcher = rangePattern.matcher(t.getDelay());
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1),
							newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf")
								&& !newDelay[i].equals("inf")) {
							if (i != 0 || !marked) {
								newDelay[i] = String.valueOf(Integer
										.parseInt(newDelay[i])
										+ Integer.parseInt(oldDelay[i]));
							}
						} else {
							newDelay[i] = "inf";
						}
					}
					t.addDelay("uniform(" + newDelay[0] + "," + newDelay[1]
							+ ")");
				}
			}
		}
		removeTransition(transition.getName());
		return true;
	}

	private boolean removeVacTransAbs(Transition transition) {
		// if (!transition.getDelay().contains("uniform")
		// && !transition.getDelay().matches("[\\d-]+")) {
		// return false;
		// }
		Place place = transition.getPostset()[0];
		Place[] preset = transition.getPreset();
		Transition[] postset = place.getPostset();
		Transition[] placePreset = place.getPreset();
		boolean marked = place.isMarked();
		// Check to make sure that the place is not a self-loop
		if (preset.length == 1) {
			Transition[] tempPreset = preset[0].getPreset();
			if (tempPreset.length == 1 && tempPreset[0].equals(transition)) {
				return false;
			}
		}
		if (transition.isFail()) {
			return false;
		}
		// Update control flow
		removeMovement(transition.getName(), place.getName());
		for (Place p : preset) {
			if (marked)
				addPlace(p.getName(), true);
			removeMovement(p.getName(), transition.getName());
			for (Transition t : postset) {
				addMovement(p.getName(), t.getName());
			}
		}
		for (Transition t : placePreset) {
			removeMovement(t.getName(), place.getName());
			for (Place p : preset) {
				if (!p.equals(place) && !t.equals(transition)) {
					addMovement(t.getName(), p.getName());
				}
			}
		}
		for (Transition t : postset) {
			if (t.getEnablingTree() != null) {
				ExprTree expr = t.getEnablingTree();
				if (transition.getEnablingTree() != null) {
					expr.setNodeValues(expr, transition.getEnablingTree(),
							"&&", 'l');
					t.addEnabling(expr.toString("LHPN"));
				}
			} else if (transition.getEnablingTree() != null) {
				t.addEnabling(transition.getEnabling());
			}
			removeMovement(place.getName(), t.getName());
		}
		removePlace(place.getName());
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		if (transition.getDelay() != null) {
			Matcher rangeMatcher = rangePattern.matcher(transition.getDelay());
			if (rangeMatcher.find()) {
				oldDelay[0] = rangeMatcher.group(1);
				oldDelay[1] = rangeMatcher.group(2);
			}
		} else {
			oldDelay[0] = "0";
			oldDelay[1] = "inf";
		}
		for (Transition t : postset) {
			if (t.getDelay() != null) {
				Matcher newMatcher = rangePattern.matcher(t.getDelay());
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1),
							newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf")
								&& !newDelay[i].equals("inf")) {
							if (i != 0) {
								newDelay[i] = String.valueOf(Integer
										.parseInt(newDelay[i])
										+ Integer.parseInt(oldDelay[i]));
							}
						} else {
							newDelay[i] = "inf";
						}
					}
					t.addDelay("uniform(" + newDelay[0] + "," + newDelay[1]
							+ ")");
				}
			}
		}
		removeTransition(transition.getName());
		return true;
	}

	private void combineTransitions(Transition trans1, Transition trans2,
			boolean samePreset, boolean samePostset) {
		if (trans2.isFail() || !transitions.containsValue(trans1)) {
			return;
		}
		if (trans1.containsDelay() && trans2.containsDelay()) {
			// if (!trans1.getDelay().contains("uniform")
			// && !trans1.getDelay().matches("[\\d-]+")) {
			// return;
			// }
			// if (!trans2.getDelay().contains("uniform")
			// && !trans2.getDelay().matches("[\\d-]+")) {
			// return;
			// }
			String[] delay = { trans1.getDelay(), trans2.getDelay() };
			String[][] delayRange = new String[2][2];
			Pattern pattern = Pattern.compile("uniform\\((\\S+?),(\\S+?)\\)");
			for (int i = 0; i < delay.length; i++) {
				Matcher matcher = pattern.matcher(delay[i]);
				if (matcher.find()) {
					delayRange[i][0] = matcher.group(1);
					delayRange[i][1] = matcher.group(2);
				} else {
					delayRange[i][0] = delay[i];
					delayRange[i][1] = delay[i];
				}
			}
			if (delayRange[0][0].equals("inf")) {
				delay[0] = delayRange[1][0];
			} else if (delayRange[1][0].equals("inf")) {
				delay[0] = delayRange[0][0];
			} else if (Integer.parseInt(delayRange[0][0]) < Integer
					.parseInt(delayRange[1][0])) {
				delay[0] = delayRange[0][0];
			} else {
				delay[0] = delayRange[1][0];
			}
			if (delayRange[0][1].equals("inf")
					|| delayRange[1][1].equals("inf")) {
				delay[1] = "inf";
			} else if (Integer.parseInt(delayRange[0][1]) > Integer
					.parseInt(delayRange[1][1])) {
				delay[1] = delayRange[0][1];
			} else {
				delay[1] = delayRange[1][1];
			}
			if (delay[0].equals(delay[1])) {
				trans1.addDelay(delay[0]);
			} else {
				trans1.addDelay("uniform(" + delay[0] + "," + delay[1] + ")");
			}
		} else {
			trans1.addDelay("uniform(0,inf)");
		}
		// Combine Control Flow
		for (Place p : trans2.getPreset()) {
			addMovement(p.getName(), trans1.getName());
			removeMovement(p.getName(), trans2.getName());
		}
		for (Place p : trans2.getPostset()) {
			addMovement(trans1.getName(), p.getName());
			removeMovement(trans2.getName(), p.getName());
		}
		removeTransition(trans2.getName());
		if (!samePostset) {
			for (Place p : trans2.getPostset()) {
				if (p.isMarked() == trans1.getPostset()[0].isMarked()) {
					combinePlaces(p, trans1.getPostset()[0]);
				}
			}
		} else if (!samePreset) {
			for (Place p : trans2.getPreset()) {
				if (p.isMarked() == trans1.getPreset()[0].isMarked()) {
					combinePlaces(p, trans1.getPreset()[0]);
				}
			}
		}
	}

	private boolean propagateConst(boolean change) {
		for (String v : booleans.keySet()) {
			if (!booleans.get(v).getInitValue().equals("unknown")) {
				boolean unassigned = true;
				for (Transition t : transitions.values()) {
					if (t.getBoolAssignments().containsKey(v)) {
						unassigned = false;
					}
				}
				if (unassigned) {
					change = true;
					ExprTree init = new ExprTree(this);
					init.token = init.intexpr_gettok(booleans.get(v)
							.getInitValue());
					init.intexpr_L(booleans.get(v).getInitValue());
					for (Transition t : transitions.values()) {
						if (t.getEnablingTree() != null) {
							t.getEnablingTree().replace(v, "boolean", init);
						}
						for (ExprTree e : t.getAssignTrees().values()) {
							if (e != null) {
								e.replace(v, "boolean", init);
							}
						}
					}
				}
			}
		}
		for (String v : continuous.keySet()) {
			if (!continuous.get(v).getInitValue().equals("[-inf,inf]")) {
				Pattern pattern = Pattern
						.compile("uniform\\(([\\d\\.-]+?),([\\d\\.-]+?)\\)");
				Matcher valMatch = pattern.matcher(continuous.get(v)
						.getInitValue());
				Matcher rateMatch = pattern.matcher(continuous.get(v)
						.getInitRate());
				Double value = 0.0;
				if (valMatch.find()) {
					Double lval = Double.parseDouble(valMatch.group(1));
					Double uval = Double.parseDouble(valMatch.group(2));
					if (!lval.equals(uval)) {
						continue;
					}
					value = lval;
				} else {
					value = Double
							.parseDouble(continuous.get(v).getInitValue());
				}
				if (rateMatch.find()) {
					Double lval = Double.parseDouble(rateMatch.group(1));
					Double uval = Double.parseDouble(rateMatch.group(2));
					if (!lval.equals(0.0) || !uval.equals(0.0)) {
						continue;
					}
				}
				boolean unassigned = true;
				for (Transition t : transitions.values()) {
					if (t.getAssignments().containsKey(v)) {
						unassigned = false;
					}
				}
				if (unassigned) {
					change = true;
					ExprTree init = new ExprTree(this);
					init.token = init.intexpr_gettok(value.toString());
					init.intexpr_L(value.toString());
					for (Transition t : transitions.values()) {
						if (t.getEnablingTree() != null) {
							t.getEnablingTree().replace(v, "continuous", init);
						}
						for (ExprTree e : t.getAssignTrees().values()) {
							if (e != null) {
								e.replace(v, "continuous", init);
							}
						}
					}
				}
			}
		}
		for (String v : integers.keySet()) {
			if (!integers.get(v).getInitValue().equals("[-inf,inf]")) {
				Pattern pattern = Pattern
						.compile("\\[([\\d\\.-]+?),([\\d\\.-]+?)\\]");
				Matcher valMatch = pattern.matcher(integers.get(v)
						.getInitValue());
				Double value = 0.0;
				if (valMatch.find()) {
					Double lval = Double.parseDouble(valMatch.group(1));
					Double uval = Double.parseDouble(valMatch.group(2));
					if (!lval.equals(uval)) {
						continue;
					}
					value = lval;
				} else {
					value = Double.parseDouble(integers.get(v).getInitValue());
				}
				boolean unassigned = true;
				for (Transition t : transitions.values()) {
					if (t.getIntAssignments().containsKey(v)) {
						unassigned = false;
					}
				}
				if (unassigned) {
					change = true;
					ExprTree init = new ExprTree(this);
					init.token = init.intexpr_gettok(value.toString());
					init.intexpr_L(value.toString());
					for (Transition t : transitions.values()) {
						if (t.getEnablingTree() != null) {
							t.getEnablingTree().replace(v, "integer", init);
						}
						for (ExprTree e : t.getAssignTrees().values()) {
							if (e != null) {
								e.replace(v, "integer", init);
							}
						}
					}
				}
			}
		}
		return change;
	}

	private boolean trans8Iteration(Transition trans,
			ArrayList<Transition> unvisited, boolean change) {
		ArrayList<String[]> toChange = new ArrayList<String[]>();
		for (String var : trans.getIntAssignments().keySet()) {
			String[] add = { trans.getName(), var };
			toChange.add(add);
		}
		for (String var : trans.getBoolAssignments().keySet()) {
			String[] add = { trans.getName(), var };
			toChange.add(add);
		}
		for (String[] array : toChange) {
			change = trans8(array[0], array[1], change);
		}
		unvisited.remove(trans);
		for (Place p : trans.getPostset()) {
			for (Transition t : p.getPostset()) {
				if (unvisited.contains(t)) {
					change = trans8Iteration(t, unvisited, change);
					unvisited.remove(t);
				}
			}
		}
		return change;
	}

	private boolean trans8(String transName, String var, boolean change) {
		// Propagate expressions of local variables to transition post sets
		Transition trans = transitions.get(transName);
		if (!((process_read.get(var).equals(process_trans.get(trans)) || process_read
				.get(var) == 0) && process_write.get(var).equals(
				process_trans.get(trans)))) {
			return change; // Return if the variable is not local
		}
		HashMap<String, ExprTree> typeAssign;
		// The assignments that will contain var
		if (isInteger(var)) {
			typeAssign = trans.getIntAssignTrees();
		} else if (isBoolean(var)) {
			typeAssign = trans.getBoolAssignTrees();
		} else {
			return change;
		}
		ExprTree e = typeAssign.get(var);
		if (e == null) {
			return change;
		}
		for (String v : e.getVars()) {
			if (!process_write.get(v).equals(process_trans.get(trans))) {
				return change; // Return if the variables in support(e) are
				// not locally written
			}
		}
		if (e.toString().equals("")) {
			return change;
		}
		for (Place p : trans.getPostset()) {
			for (Transition tP : p.getPostset()) {
				if (transName.equals(tP.getName())) {
					return change;
				}
				for (Place pP : tP.getPreset()) {
					for (Transition tPP : pP.getPreset()) {
						// if (typeAssign.containsKey(tPP)) {
						if (tPP.getAssignTrees().containsKey(var)) {
							ExprTree ePP = tPP.getAssignTree(var);
							if (!ePP.isEqual(e)) {
								return change; // All assignments
								// to var in ..(t..) must be equal
							}
							for (String v : ePP.getVars()) {
								if (!v.equals(var)) {
									if (isBoolean(v)) { // All
										// variables in
										// support(e) cannot be
										// assigned
										if (tPP.getBoolAssignments()
												.containsKey(v)) {
											return change;
										}
									} else if (isInteger(v)) {
										if (tPP.getIntAssignments()
												.containsKey(v)) {
											return change;
										}
									} else {
										if (tPP.getContAssignments()
												.containsKey(v)) {
											return change;
										}
									}
								}
							}
						} else {
							return change;
						}
						// }
					}
				}
			}
		}
		// Perform transform
		for (Place p : trans.getPostset()) {
			for (Transition tP : p.getPostset()) {
				replace(tP, var, e);
				for (Place pP : tP.getPreset()) {
					for (Transition tPP : pP.getPreset()) {
						if (isBoolean(var)) {
							tPP.removeBoolAssign(var);
						} else if (isInteger(var)) {
							tPP.removeIntAssign(var);
						} else {
							tPP.removeContAssign(var);
						}
					}
				}
				change = true;
			}
		}
		return change;
	}

	private boolean readBeforeWrite(Transition trans, String var) {
		for (Place p : trans.getPostset()) {
			for (Transition t : p.getPostset()) {
				boolean written = false;
				for (String s : t.getAssignTrees().keySet()) {
					ExprTree e1 = t.getAssignTree(s);
					if (e1.getVars().contains(var)) {
						return true;
					}
					if (s.equals(var)) {
						written = true;
					}
				}
				if (t.getEnablingTree() != null) {
					if (t.getEnablingTree().getVars().contains(var)) {
						return true;
					}
				}
				if (written)
					return false;
				if (!read.contains(t)) {
					read.add(t);
					if (readBeforeWrite(t, var)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean weakReadBeforeWrite(Transition trans, String var) {
		for (Place p : trans.getPostset()) {
			for (Transition t : p.getPostset()) {
				boolean written = false;
				for (String s : t.getAssignTrees().keySet()) {
					ExprTree e1 = t.getAssignTree(s);
					if (e1.getVars().contains(var)) {
						return true;
					}
					if (s.equals(var)) {
						written = true;
					}
				}
				if (t.getEnablingTree() != null) {
					if (t.getEnablingTree().getVars().contains(var)) {
						return true;
					}
				}
				if (written)
					return false;
			}
		}
		return true;
	}

	private Boolean removeDanglingTransitions(Boolean change) {
		ArrayList<Transition> remove = new ArrayList<Transition>();
		for (Transition t : transitions.values()) {
			if (t.getPostset().length == 0) {
				if (t.getAssignments().size() == 0) {
					if (!t.isFail()) {
						for (Place p : t.getPreset()) {
							if (p.getPostset().length > 1) {
								return change;
							}
						}
						remove.add(t);
					}
				}
			}
		}
		for (Transition t : remove) {
			for (Place p : t.getPreset()) {
				for (Transition tP : p.getPreset()) {
					removeMovement(tP.getName(), p.getName());
				}
				removeMovement(p.getName(), t.getName());
				removePlace(p.getName());
			}
			removeTransition(t.getName());
			change = true;
		}
		return change;
	}

	private boolean divideProcesses() {
		for (Transition t : transitions.values()) { // Add all transitions to
			// process
			// structure
			process_trans.put(t, 0);
		}
		for (String v : booleans.keySet()) { // / Add All variables to process
			// structure
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		for (String v : continuous.keySet()) {
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		for (String v : integers.keySet()) {
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		Integer i = 1; // The total number of processes
		Integer process = 1; // The active process number
		while (process_trans.containsValue(0)) {
			Transition new_proc = new Transition(); // Find a transition that is
			// not part of a
			// process
			for (Transition t : process_trans.keySet()) {
				if (process_trans.get(t) == 0) {
					new_proc = t;
					break;
				}
			}
			boolean flag = false; // Make sure that it is not part of a process
			for (Place p : new_proc.getPreset()) {
				if (!flag) // Check the preset to see if it is part of a process
					for (Transition t : p.getPreset()) {
						if (!flag)
							if (process_trans.get(t) != 0) {
								flag = true;
								process = process_trans.get(t);
								break;
							}
					}
			}
			if (!flag) // Check the postset to see if it is part of a process
				for (Place p : new_proc.getPostset()) {
					if (!flag)
						for (Transition t : p.getPostset()) {
							if (!flag)
								if (process_trans.get(t) != 0) {
									flag = true;
									process = process_trans.get(t);
									break;
								}
						}
				}
			if (!flag) {
				i++; // Increment the process counter if it is not part of a
				// process
				process = i;
			}
			if (!addTransProcess(new_proc, process))
				return false;
		}
		assignVariableProcess();
		return true;
	}

	private void assignVariableProcess() {
		for (Transition t : transitions.values()) { // For each
			// transition with assignments
			HashMap<String, String> assignments = t.getAssignments();
			HashMap<String, ExprTree> assignTrees = t.getAssignTrees();
			for (String v : assignments.keySet()) { // The variables assigned on
				// each transition
				if ((process_write.get(v) == 0)
						|| (process_write.get(v) == process_trans.get(t))) {
					process_write.put(v, process_trans.get(t)); // Mark a
					// variable as locally written to a process
				} else {
					process_write.put(v, -1); // Mark a variable as globally
					// written
				}
			}
			for (ExprTree e : assignTrees.values()) {
				for (String v : e.getVars()) {
					if ((process_read.get(v) == 0)
							|| (process_read.get(v) == process_trans.get(t))) {
						process_read.put(v, process_trans.get(t)); // Mark
						// a variable as locally read
					} else {
						process_read.put(v, -1); // Mark a variable as
						// globally read
					}
				}
			}
			ExprTree e = t.getEnablingTree();
			if (e != null) {
				for (String v : e.getVars()) {
					if ((process_read.get(v) == 0)
							|| (process_read.get(v) == process_trans.get(t))) {
						process_read.put(v, process_trans.get(t));
					} else {
						process_read.put(v, -1);
					}
				}
			}
		}
	}

	private boolean addTransProcess(Transition trans, Integer proc) {
		process_trans.put(trans, proc); // Add the current transition to the
		// process
		for (Place p : trans.getPostset()) {
			for (Transition t : p.getPostset()) {
				if (process_trans.get(t) == 0)
					addTransProcess(t, proc); // Add the postset of the
				// transition to the same process recursively
				else if (process_trans.get(t) != proc) {
					System.out
							.println("Error: Multiple Process Labels Added to the Same Transition");
					return false;
				}
			}
		}
		for (Place p : trans.getPreset()) {
			for (Transition t : p.getPreset()) {
				if (process_trans.get(t) == 0)
					addTransProcess(t, proc); // Add the preset of the
				// transition to the same process recursively
				else if (process_trans.get(t) != proc) {
					System.out
							.println("Error: Multiple Process Labels Added to the Same Transition");
					return false;
				}
			}
		}
		return true;
	}

	private boolean replace(Transition trans, String var, ExprTree expr) {
		boolean flag = false;
		String type;
		if (isInteger(var)) {
			type = "integer";
		} else if (isContinuous(var)) {
			type = "continuous";
		} else {
			type = "boolean";
		}
		if (trans.getEnablingTree() != null) {
			trans.getEnablingTree().replace(var, type, expr);
			trans.addEnabling(trans.getEnablingTree().toString("LHPN"));
			flag = true;
		}
		for (String v : trans.getAssignTrees().keySet()) {
			ExprTree e1 = trans.getAssignTree(v);
			if (e1 != null) {
				e1.replace(var, type, expr);
			}
			if (isBoolean(v)) {
				trans.addBoolAssign(v, e1.toString("boolean", "LHPN"));
			} else if (isInteger(v)) {
				trans.addIntAssign(v, e1.toString("integer", "LHPN"));
			} else if (isContinuous(v)
					&& trans.getContAssignments().containsKey(var)) {
				trans.addContAssign(v, e1.toString("continuous", "LHPN"));
			} else if (trans.getRateAssignments().containsKey(var)) {
				trans.addRateAssign(v, e1.toString("continuous", "LHPN"));
			} else {
				trans.addRateAssign(v.split("\\s")[0], e1.toString(
						"continuous", "LHPN"));
			}
		}
		if (isInteger(var)) {
			if (!trans.getIntAssignments().containsKey(var)) {
				trans.addIntAssign(var, expr.toString());
			}
		} else if (isBoolean(var)) {
			if (!trans.getBoolAssignments().containsKey(var)) {
				trans.addBoolAssign(var, expr.toString());
			}
		}
		return flag;
	}

	private boolean areCorrelatedBooleans(String var1, String var2) {
		String init1;
		String init2;
		init1 = booleans.get(var1).getInitValue();
		init2 = booleans.get(var2).getInitValue();
		if (init1.equals(init2)) {
			for (Transition t : transitions.values()) {
				if (t.getBoolAssignments().containsKey(var1)) {
					if (t.getBoolAssignments().containsKey(var2)) {
						if (!t.getBoolAssignments().get(var1).equals(
								t.getBoolAssignments().get(var2))) {
							return false;
						}
					} else {
						return false;
					}
				} else if (t.containsAssignment(var2)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean areInverted(String var1, String var2) {
		String init1;
		String init2;
		init1 = booleans.get(var1).getInitValue();
		init2 = booleans.get(var2).getInitValue();
		if (!(init1.equals("~(" + init2 + ")") || init1.equals("unknown")
				&& init2.equals("unknown"))) {
			return false;
		}
		for (Transition t : transitions.values()) {
			if (t.getBoolAssignments().containsKey(var1)) {
				if (t.getBoolAssignments().containsKey(var2)) {
					ExprTree expr = new ExprTree(t.getBoolAssignTree(var2));
					expr.setNodeValues(expr, null, "!", 'l');
					if (t.getBoolAssignTree(var2).equals(expr)) {
						continue;
					} else if (t.getBoolAssignment(var1).toLowerCase().equals(
							"true")
							&& t.getBoolAssignment(var2).toLowerCase().equals(
									"false")
							|| t.getBoolAssignment(var1).toLowerCase().equals(
									"false")
							&& t.getBoolAssignment(var2).toLowerCase().equals(
									"true")) {
						continue;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else if (t.containsAssignment(var2)) {
				return false;
			}
		}
		return true;
	}

	private boolean areCorrelatedContinuous(String var1, String var2) {
		if (continuous.get(var1).equals(continuous.get(var2))) {
			for (Transition t : transitions.values()) {
				if (t.getContAssignTrees().containsKey(var1)) {
					if (t.getContAssignTrees().containsKey(var2)) {
						if (!t.getContAssignTree(var1).equals(
								t.getContAssignTree(var2))) {
							return false;
						}
					} else {
						return false;
					}
				} else if (t.getContAssignments().containsKey(var2)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean areCorrelatedIntegers(String var1, String var2) {
		if (integers.get(var1).equals(integers.get(var2))) {
			for (Transition t : transitions.values()) {
				if (t.getIntAssignTrees().containsKey(var1)) {
					if (t.getIntAssignTrees().containsKey(var2)) {
						if (!t.getIntAssignment(var1).equals(
								t.getIntAssignment(var2))) {
							return false;
						}
					} else {
						return false;
					}
				} else if (t.getIntAssignTrees().containsKey(var2)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private void mergeVariables(String var1, String var2) {
		Variable v1 = getVariable(var1);
		Variable v2 = getVariable(var2);
		if (!variables.contains(v2) || !variables.contains(v1)) {
			return;
		}
		for (Transition t : transitions.values()) {
			if (t.getEnablingTree() != null) {
				t.getEnablingTree().replaceVar(var2, var1);
			}
			HashMap<String, ExprTree> m = t.getBoolAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replaceVar(var2, var1);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeBoolAssign(var2);
			}
			m = t.getContAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replaceVar(var2, var1);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeContAssign(var2);
			}
			m = t.getBoolAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replaceVar(var2, var1);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeIntAssign(var2);
			}
			m = t.getRateAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replaceVar(var2, var1);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeRateAssign(var2);
			}
			t.simplifyExpr();
		}
		if (process_read.get(var1) != process_read.get(var2)) {
			process_read.put(var1, -1);
		}
		removeVar(var2);
	}

	private void mergeVariables(ExprTree expr, String var2) {
		Variable v1 = getVariable(expr.r1.variable);
		Variable v2 = getVariable(var2);
		if (!variables.contains(v2) || !variables.contains(v1)) {
			return;
		}
		for (Transition t : transitions.values()) {
			if (t.getEnablingTree() != null) {
				t.getEnablingTree().replace(var2, "", expr);
			}
			HashMap<String, ExprTree> m = t.getBoolAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replace(var2, "", expr);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeBoolAssign(var2);
			}
			m = t.getContAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replace(var2, "", expr);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeContAssign(var2);
			}
			m = t.getIntAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replace(var2, "", expr);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeIntAssign(var2);
			}
			m = t.getRateAssignTrees();
			for (ExprTree e : m.values()) {
				if (e != null) {
					e.replace(var2, "", expr);
				}
			}
			if (m.containsKey(var2)) {
				m.remove(var2);
				t.removeRateAssign(var2);
			}
		}
		if (process_read.get(expr.r1.variable) != process_read.get(var2)) {
			process_read.put(expr.r1.variable, -1);
		}
		removeVar(var2);
	}

	public boolean mergeTransitionsSimp(boolean change) {
		HashMap<Transition, ArrayList<Transition>> toMerge = new HashMap<Transition, ArrayList<Transition>>();
		for (Transition t1 : transitions.values()) {
			for (Transition t2 : transitions.values()) {
				if (t1.equals(t2)) {
					continue;
				}
				if (t1.isFail() != t2.isFail()) {
					continue;
				}
				if ((comparePreset(t1, t2) || (t1.getPreset().length == 0 && t2
						.getPreset().length == 0))
						&& (comparePostset(t1, t2) || (t1.getPostset().length == 0 && t2
								.getPostset().length == 0))) {
					boolean combine = true;
					ExprTree tree = new ExprTree(this);
					tree.setNodeValues(t1.getEnablingTree(), t2.getEnablingTree(), "&&", 'l');
					for (String v : tree.getVars()) {
						if (process_write.get(v) != process_trans.get(t1)) {
							combine = false;
							break;
						}
					}
					for (Transition t : transitions.values()) {
						if (tree.becomesTrue(t.getAssignments())) {
							combine = false;
							break;
						}
					}
					for (String var : t1.getAssignments().keySet()) {
						if (!t2.containsAssignment(var)
								|| !t1.getAssignTree(var).isEqual(
										t2.getAssignTree(var))) {
							combine = false;
							break;
						}
					}
					for (String var : t2.getAssignments().keySet()) {
						if (!t1.containsAssignment(var)) {
							combine = false;
							break;
						}
					}
					if (combine) {
						if (toMerge.containsKey(t1)) {
							toMerge.get(t1).add(t2);
						} else {
							toMerge.put(t1, new ArrayList<Transition>());
							toMerge.get(t1).add(t2);
						}
					}
				}
			}
		}
		for (Transition t : toMerge.keySet()) {
			if (transitions.containsValue(t)) {
				mergeTransitions(t, toMerge.get(t), false);
				change = true;
			}
		}
		return change;
	}

	private boolean mergeTransitionsAbs(boolean change) {
		HashMap<Transition, ArrayList<Transition>> toMerge = new HashMap<Transition, ArrayList<Transition>>();
		for (Transition t1 : transitions.values()) {
			for (Transition t2 : transitions.values()) {
				if (t1.equals(t2)) {
					continue;
				}
				if (t1.isFail() != t2.isFail()) {
					continue;
				}
				if ((comparePreset(t1, t2) || (t1.getPreset().length == 0 && t2
						.getPreset().length == 0))
						&& (comparePostset(t1, t2) || (t1.getPostset().length == 0 && t2
								.getPostset().length == 0))) {
					boolean combine = true;
					ExprTree tree = new ExprTree(this);
					tree.setNodeValues(t1.getEnablingTree(), t2.getEnablingTree(), "&&", 'l');
					for (String v : tree.getVars()) {
						if (process_write.get(v) != process_trans.get(t1)) {
							combine = false;
							break;
						}
					}
					for (Transition t : transitions.values()) {
						if (tree.becomesTrue(t.getAssignments())) {
							combine = false;
							break;
						}
					}
					if (t1.getEnablingTree() != null && t2.getEnablingTree() != null) {
						if (t1.getEnablingTree().isit == 'a') {
							if (!t1.getEnablingTree().op.equals("uniform") || t1.getEnablingTree().r1.isit != 'n' || t1.getEnablingTree().r2.isit != 'n') {
								combine = false;
								break;
							}
						}
						else if (t1.getEnablingTree().isit != 'n') {
							combine = false;
							break;
						}
						if (t2.getEnablingTree().isit == 'a') {
							if (!t2.getEnablingTree().op.equals("uniform") || t2.getEnablingTree().r1.isit != 'n' || t2.getEnablingTree().r2.isit != 'n') {
								combine = false;
								break;
							}
						}
						else if (t2.getEnablingTree().isit != 'n') {
							combine = false;
							break;
						}
					}
					for (String var : t1.getAssignments().keySet()) {
						if (!t2.containsAssignment(var)
								|| !t1.getAssignTree(var).isEqual(
										t2.getAssignTree(var))) {
							combine = false;
							break;
						}
					}
					for (String var : t2.getAssignments().keySet()) {
						if (!t1.containsAssignment(var)) {
							combine = false;
							break;
						}
					}
					if (combine) {
						if (toMerge.containsKey(t1)) {
							toMerge.get(t1).add(t2);
						} else {
							toMerge.put(t1, new ArrayList<Transition>());
							toMerge.get(t1).add(t2);
						}
					}
				}
			}
		}
		for (Transition t : toMerge.keySet()) {
			if (transitions.containsValue(t)) {
				mergeTransitions(t, toMerge.get(t), true);
				change = true;
			}
		}
		return change;
	}

	private void mergeTransitions(Transition t, ArrayList<Transition> list,
			boolean abstraction) {
		if (!t.getDelay().contains("uniform")
				&& !t.getDelay().matches("[\\d-]+")) {
			return;
		}
		for (Transition tP : list) {
			if (!tP.getDelay().contains("uniform")
					&& !tP.getDelay().matches("[\\d-]+")) {
				return;
			}
		}
		if (abstraction) {
			for (Transition tP : list) {
				if (!transitions.containsKey(tP.getName())) {
					return;
				}
			}
			String enabling = "(" + t.getEnabling() + ")";
			for (Transition tP : list) {
				enabling = enabling + "|(" + tP.getEnabling() + ")";
			}
			t.addEnabling(enabling);
			try {
				Pattern pattern = Pattern.compile(RANGE);
				Matcher matcher = pattern.matcher(t.getDelay());
				ExprTree priority1 = t.getPriorityTree();
				Integer dl1, dl2, du1, du2;
				if (matcher.find()) {
					dl1 = Integer.parseInt(matcher.group(1));
					du1 = Integer.parseInt(matcher.group(2));
				} else {
					dl1 = Integer.parseInt(t.getDelay());
					du1 = Integer.parseInt(t.getDelay());
				}
				for (Transition tP : list) {
					matcher = pattern.matcher(tP.getDelay());
					if (matcher.find()) {
						dl2 = Integer.parseInt(matcher.group(1));
						du2 = Integer.parseInt(matcher.group(2));
					} else {
						dl2 = Integer.parseInt(tP.getDelay());
						du2 = Integer.parseInt(tP.getDelay());
					}
					if (dl1.compareTo(dl2) > 0) {
						dl1 = dl2;
					}
					if (du1.compareTo(dl2) <= 0) {
						du1 = du2;
					}
					ExprTree priority2 = tP.getPriorityTree();
					priority2.setNodeValues(tP.getEnablingTree(), priority2, "*", 'a');
					priority1.setNodeValues(priority1, priority2, "+", 'a');
				}
				if (dl1.toString().equals(du1.toString())) {
					t.addDelay(dl1.toString());
				} else {
					t.addDelay("uniform(" + dl1.toString() + ","
							+ du1.toString() + ")");
				}
				t.addPriority(priority1.toString());
			} catch (Exception e) {
				t.addDelay("uniform(0,inf)");
			}
			for (Transition tP : list) {
				removeTransition(tP.getName());
			}
		} else {
			for (Transition tP : list) {
				if (!transitions.containsKey(tP.getName())) {
					return;
				}
			}
			String enabling = "(" + t.getEnabling() + ")";
			for (Transition tP : list) {
				enabling = enabling + "|(" + tP.getEnabling() + ")";
			}
			ExprTree dl = new ExprTree(this);
			ExprTree du = new ExprTree(this);
			ExprTree delay = new ExprTree(this);
			ExprTree delay1 = t.getDelayTree();
			ExprTree e1 = t.getEnablingTree();
			ExprTree priority1 = t.getPriorityTree();
			ExprTree dl1, dl2, du1, du2;
			if (delay1.isit == 'a' & delay1.op.equals("uniform")) {
				dl1 = delay1.r1;
				du1 = delay1.r2;
			} else {
				dl1 = new ExprTree(delay1);
				du1 = new ExprTree(delay1);
			}
			dl1.setNodeValues(e1, dl1, "*", 'a');
			du1.setNodeValues(e1, du1, "*", 'a');
			dl = dl1;
			du = du1;
			for (Transition tP : list) {
				ExprTree delay2 = tP.getDelayTree();
				if (delay2.isit == 'a' & delay2.op.equals("uniform")) {
					dl2 = delay2.r1;
					du2 = delay2.r2;
				} else {
					dl2 = new ExprTree(delay2);
					du2 = new ExprTree(delay2);
				}
				ExprTree e2 = tP.getEnablingTree();
				dl2.setNodeValues(e2, dl2, "*", 'a');
				dl.setNodeValues(dl, dl2, "+", 'a');
				du2.setNodeValues(e2, du2, "*", 'a');
				du.setNodeValues(du, du2, "+", 'a');
				ExprTree priority2 = tP.getPriorityTree();
				priority2.setNodeValues(e2, priority2, "*", 'a');
				priority1.setNodeValues(priority1, priority2, "+", 'a');
			}
			if (!dl.isEqual(du)) {
				delay.setNodeValues(dl, du, "uniform", 'a');
			} else {
				delay = dl;
			}
			t.addEnabling(enabling);
			t.addDelay(delay.toString());
			t.addPriority(priority1.toString());
		}
		for (Transition tP : list) {
			removeTransition(tP.getName());
		}
	}

	private boolean hasAssignments(Transition trans) {
		if (trans.getEnabling() != null) {
			return true;
		}
		if (trans.getAssignments().size() > 0) {
			return true;
		}
		return false;
	}

	private boolean isGloballyDisabled(Transition trans) {
		ExprTree enabling = trans.getEnablingTree();
		if (enabling == null) {
			return false;
		}
		for (Transition t : transitions.values()) {
			if (process_trans.get(t).equals(process_trans.get(trans))) {
				continue;
			}
			if (enabling.becomesTrue(t.getAssignments())) {
				return false;
			}
		}
		return true;
	}
	
	private void addInterestingVariable(String var) {
		newestIntVars.add(var);
		if (continuous.containsKey(var)) {
			newestIntVars.add(var + "_rate");
		}
	}

	private static final String RANGE = "uniform\\((\\w+?),(\\w+?)\\)";

	private static final int INFIN = 2147483647;
}