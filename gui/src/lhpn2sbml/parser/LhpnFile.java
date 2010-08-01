package lhpn2sbml.parser;

import gcm2sbml.util.Utility;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import verification.Verification;

import biomodelsim.Log;

public class LhpnFile {

	protected HashMap<String, Transition> transitions;

	protected HashMap<String, Place> places;

	protected HashMap<String, Variable> booleans;

	protected HashMap<String, Variable> continuous;

	protected HashMap<String, Variable> integers;

	protected ArrayList<Variable> variables;

	protected String property;

	protected Log log;

	public LhpnFile(Log log) {
		this.log = log;
		transitions = new HashMap<String, Transition>();
		places = new HashMap<String, Place>();
		booleans = new HashMap<String, Variable>();
		continuous = new HashMap<String, Variable>();
		integers = new HashMap<String, Variable>();
		variables = new ArrayList<Variable>();
		property = new String();
	}

	public LhpnFile() {
		transitions = new HashMap<String, Transition>();
		places = new HashMap<String, Place>();
		booleans = new HashMap<String, Variable>();
		continuous = new HashMap<String, Variable>();
		integers = new HashMap<String, Variable>();
		variables = new ArrayList<Variable>();
		property = new String();
	}

	public void save(String filename) {
		try {
			String file = filename;
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer();
			HashMap<String, Integer> boolOrder = new HashMap<String, Integer>();
			int i = 0;
			if (!booleans.isEmpty()) {
				boolean flag = false;
				for (String s : booleans.keySet()) {
					if (booleans.get(s) != null) {
						if (!booleans.get(s).isOutput()) {
							if (!flag) {
								buffer.append(".inputs ");
								flag = true;
							}
							buffer.append(s + " ");
							boolOrder.put(s, i);
							i++;
						}
					}
				}
				buffer.append("\n");
				flag = false;
				for (String s : booleans.keySet()) {
					if (booleans.get(s) != null) {
						if (!flag) {
							buffer.append(".outputs ");
							flag = true;
						}
						if (booleans.get(s).isOutput()) {
							buffer.append(s + " ");
							boolOrder.put(s, i);
							i++;
						}
					}
				}
				buffer.append("\n");
			}
			if (!transitions.isEmpty()) {
				buffer.append(".dummy ");
				for (String s : transitions.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!continuous.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.variables ");
				for (String s : continuous.keySet()) {
					buffer.append(s + " ");
				}
				for (String s : integers.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!transitions.isEmpty()) {
				boolean flag = false;
				for (Transition t : transitions.values()) {
					if (t.isFail()) {
						if (!flag) {
							buffer.append("#@.failtrans ");
						}
						buffer.append(t.getName() + " ");
						flag = true;
					}
				}
				if (flag) {
					buffer.append("\n");
				}
			}
			if (!places.isEmpty()) {
				buffer.append("#|.places ");
				for (String s : places.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!booleans.isEmpty()) {
				boolean flag = false;
				for (i = 0; i < boolOrder.size(); i++) {
					for (String s : booleans.keySet()) {
						if (boolOrder.get(s).equals(i)) {
							if (!flag) {
								buffer.append("#@.init_state [");
								flag = true;
							}
							if (booleans.get(s).getInitValue().equals("true")) {
								buffer.append("1");
							} else if (booleans.get(s).getInitValue().equals(
									"false")) {
								buffer.append("0");
							} else {
								buffer.append("X");
							}
						}
					}
				}
				if (flag) {
					buffer.append("]\n");
				}
			}
			if (!transitions.isEmpty()) {
				buffer.append(".graph\n");
				for (Transition t : transitions.values()) {
					for (Place s : t.getPreset()) {
						buffer.append(s.getName() + " " + t.getName() + "\n");
					}
					for (Place s : t.getPostset()) {
						buffer.append(t.getName() + " " + s.getName() + "\n");
					}
				}
			}
			boolean flag = false;
			if (!places.keySet().isEmpty()) {
				for (Place place : places.values()) {
					if (place.isMarked()) {
						if (!flag) {
							buffer.append(".marking {");
							flag = true;
						}
						buffer.append(place.getName() + " ");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (property != null && !property.equals("")) {
				buffer.append("#@.property " + property + "\n");
			}
			if (!continuous.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.init_vals {");
				for (Variable var : continuous.values()) {
					buffer.append("<" + var.getName() + "="
							+ var.getInitValue() + ">");
				}
				for (Variable var : integers.values()) {
					buffer.append("<" + var.getName() + "="
							+ var.getInitValue() + ">");
				}
				if (!continuous.isEmpty()) {
					buffer.append("}\n#@.init_rates {");
					for (Variable var : continuous.values()) {
						buffer.append("<" + var.getName() + "="
								+ var.getInitRate() + ">");
					}
				}
				buffer.append("}\n");
			}
			if (!transitions.isEmpty()) {
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.getEnabling() != null && !t.getEnabling().equals("")) {
						if (!flag) {
							buffer.append("#@.enablings {");
							flag = true;
						}
						buffer.append("<" + t.getName() + "=["
								+ t.getEnabling() + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					HashMap<String, String> contAssign = t.getContAssignments();
					if (!contAssign.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						for (String var : contAssign.keySet()) {
							buffer.append("<" + t.getName() + "=[" + t + ":="
									+ contAssign.get(var) + "]>");
						}
					}
					HashMap<String, String> intAssign = t.getIntAssignments();
					if (!intAssign.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						for (String var : intAssign.keySet()) {
							buffer.append("<" + t.getName() + "=[" + var + ":="
									+ intAssign.get(var) + "]>");
						}
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					HashMap<String, String> rateAssign = t.getRateAssignments();
					for (String var : rateAssign.keySet()) {
						if (!var.equals("")) {
							if (!flag) {
								buffer.append("#@.rate_assignments {");
								flag = true;
							}
							buffer.append("<" + t.getName() + "=[" + var + ":="
									+ t.getRateAssignment(var) + "]>");
						}
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.containsDelay()) {
						if (!flag) {
							buffer.append("#@.delay_assignments {");
							flag = true;
						}
						buffer.append("<" + t.getName() + "=[" + t.getDelay()
								+ "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.containsPriority()) {
						if (!flag) {
							buffer.append("#@.priority_assignments {");
							flag = true;
						}
						buffer.append("<" + t.getName() + "=["
								+ t.getPriority() + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			flag = false;
			for (Transition t : transitions.values()) {
				HashMap<String, String> boolAssign = t.getBoolAssignments();
				for (String var : boolAssign.keySet()) {
					if (!flag) {
						buffer.append("#@.boolean_assignments {");
						flag = true;
					}
					buffer.append("<" + t.getName() + "=[" + var + ":="
							+ boolAssign.get(var) + "]>");
				}
			}
			if (flag) {
				buffer.append("}\n");
			}
			buffer.append("#@.continuous ");
			for (String s : continuous.keySet()) {
				buffer.append(s + " ");
			}
			buffer.append("\n");
			if (buffer.toString().length() > 0) {
				buffer.append(".end\n");
			}
			p.print(buffer);
			p.close();
			if (log != null) {
				log.addText("Saving:\n" + file + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		// try {

		parseProperty(data);
		parseInOut(data);
		parsePlaces(data);
		parseControlFlow(data);
		parseVars(data);
		parseIntegers(data);
		parseMarking(data);
		boolean error = parseEnabling(data);
		error = parseAssign(data, error);
		error = parseRateAssign(data, error);
		error = parseDelayAssign(data, error);
		error = parsePriorityAssign(data, error);
		error = parseBooleanAssign(data, error);
		error = parseTransitionRate(data, error);
		parseFailTransitions(data);

		if (!error) {
			Utility
					.createErrorMessage("Invalid Expressions",
							"The input file contained invalid expressions.  See console for details.");
		}
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void addTransition(String name) {
		Transition trans = new Transition(name, variables, this);
		transitions.put(name, trans);
	}

	public void addTransition(String name, Properties prop) {
		Transition trans = new Transition(name, variables, this);
		for (String p : prop.getProperty("preset").split("\\s")) {
			trans.addPreset(places.get(p));
		}
		for (String p : prop.getProperty("postset").split("\\s")) {
			trans.addPostset(places.get(p));
		}
		transitions.put(name, trans);
	}

	public void addPlace(String name, Boolean ic) {
		Place place = new Place(name, ic);
		places.put(name, place);
	}

	public void addEnabling(String transition, String enabling) {
		transitions.get(transition).addEnabling(enabling);
	}

	public void addProperty(String prop) {
		property = prop;
	}

	public void addMovement(String fromName, String toName) {
		if (isTransition(fromName)) {
			transitions.get(fromName).addPostset(places.get(toName));
			places.get(toName).addPreset(transitions.get(fromName));
		} else {
			transitions.get(toName).addPreset(places.get(fromName));
			places.get(fromName).addPostset(transitions.get(toName));
		}
	}

	public void addInput(String name, String ic) {
		Variable var = new Variable(name, "boolean", ic, Variable.INPUT);
		booleans.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addOutput(String name, String ic) {
		Variable var = new Variable(name, "boolean", ic, Variable.OUTPUT);
		booleans.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addContinuous(String name) {
		Variable var = new Variable(name, "continuous");
		continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addContinuous(String name, Properties initCond) {
		Variable var = new Variable(name, "continuous", initCond);
		continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addContinuous(String name, String initVal, String initRate) {
		Properties initCond = new Properties();
		initCond.setProperty("value", initVal);
		initCond.setProperty("rate", initRate);
		Variable var = new Variable(name, "continuous", initCond);
		continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addInteger(String name, String ic) {
		Variable var = new Variable(name, "integer", ic);
		integers.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addTransitionRate(String transition, String rate) {
		transitions.get(transition).addDelay("exponential(" + rate + ")");
	}

	public void addBoolAssign(String transition, String variable,
			String assignment) {
		if (!variables.contains(variable)) {
			addOutput(variable, "unknown");
		}
		transitions.get(transition).addIntAssign(variable, assignment);
	}

	public void addRateAssign(String transition, String variable, String rate) {
		transitions.get(transition).addRateAssign(variable, rate);
	}

	public void addIntAssign(String transition, String variable, String assign) {
		transitions.get(transition).addIntAssign(variable, assign);
	}

	public void changePlaceName(String oldName, String newName) {
		places.get(oldName).setName(newName);
		places.put(newName, places.get(oldName));
		places.remove(oldName);
	}

	public void changeTransitionName(String oldName, String newName) {
		transitions.get(oldName).setName(newName);
		transitions.put(newName, transitions.get(oldName));
		transitions.remove(oldName);
	}

	public void changeDelay(String t, String delay) {
		transitions.get(t).addDelay(delay);
	}

	public void changePriority(String t, String priority) {
		transitions.get(t).addDelay(priority);
	}

	public void changeInitialMarking(String p, boolean marking) {
		places.get(p).setMarking(marking);
	}

	public void changeVariableName(String oldName, String newName) {
		if (isContinuous(oldName)) {
			continuous.put(newName, continuous.get(oldName));
			continuous.remove(oldName);
		} else if (isBoolean(oldName)) {
			booleans.put(newName, booleans.get(oldName));
			booleans.remove(oldName);
		} else if (isInteger(oldName)) {
			integers.put(newName, integers.get(oldName));
			integers.remove(oldName);
		}
	}

	public void changeContInitCond(String var, Properties initCond) {
		continuous.get(var).addInitCond(initCond);
	}

	public void changeIntegerInitCond(String var, String initCond) {
		integers.get(var).addInitValue(initCond);
	}

	public String[] getAllIDs() {
		String[] ids = new String[transitions.size() + places.size()
				+ variables.size()];
		int i = 0;
		for (String t : transitions.keySet()) {
			ids[i++] = t;
		}
		for (String p : places.keySet()) {
			ids[i++] = p;
		}
		for (Variable v : variables) {
			ids[i++] = v.getName();
		}
		return ids;
	}

	public String getProperty() {
		return property;
	}

	public String[] getTransitionList() {
		String[] transitionList = new String[transitions.size()];
		int i = 0;
		for (String t : transitions.keySet()) {
			transitionList[i++] = t;
		}
		return transitionList;
	}

	public Transition getTransition(String transition) {
		return transitions.get(transition);
	}

	public ExprTree getTransitionRateTree(String transition) {
		if (transitions.get(transition).getDelayTree() == null)
			return null;
		if (transitions.get(transition).getDelayTree().op.equals("exponential")) {
			return transitions.get(transition).getDelayTree().r1;
		}
		return null;
	}

	public ExprTree getEnablingTree(String transition) {
		return transitions.get(transition).getEnablingTree();
	}

	public String[] getPlaceList() {
		String[] placeList = new String[places.size()];
		int i = 0;
		for (String t : places.keySet()) {
			placeList[i++] = t;
		}
		return placeList;
	}

	public Place getPlace(String place) {
		return places.get(place);
	}

	public String[] getPreset(String name) {
		if (isTransition(name)) {
			String[] preset = new String[transitions.get(name).getPreset().length];
			int i = 0;
			for (Place p : transitions.get(name).getPreset()) {
				preset[i++] = p.getName();
			}
			return preset;
		} else if (places.containsKey(name)) {
			String[] preset = new String[places.get(name).getPreset().length];
			int i = 0;
			for (Transition t : places.get(name).getPreset()) {
				preset[i++] = t.getName();
			}
			return preset;
		} else {
			return null;
		}
	}

	public String[] getPostset(String name) {
		if (isTransition(name)) {
			String[] postset = new String[transitions.get(name).getPostset().length];
			int i = 0;
			for (Place p : transitions.get(name).getPostset()) {
				postset[i++] = p.getName();
			}
			return postset;
		} else if (places.containsKey(name)) {
			String[] postset = new String[places.get(name).getPostset().length];
			int i = 0;
			for (Transition t : places.get(name).getPostset()) {
				postset[i++] = t.getName();
			}
			return postset;
		} else {
			return null;
		}
	}

	public String[] getControlFlow() {
		ArrayList<String> movements = new ArrayList<String>();
		for (Transition t : transitions.values()) {
			for (Place p : t.getPostset()) {
				movements.add(t.getName() + " " + p.getName());
			}
			for (Place p : t.getPreset()) {
				movements.add(p.getName() + " " + t.getName());
			}
		}
		String[] array = new String[movements.size()];
		int i = 0;
		for (String s : movements) {
			array[i++] = s;
		}
		return array;
	}

	public boolean getInitialMarking(String place) {
		return places.get(place).isMarked();
	}

	public String[] getVariables() {
		String[] vars = new String[variables.size()];
		int i = 0;
		for (Variable v : variables) {
			vars[i++] = v.getName();
		}
		return vars;
	}

	public Variable getVariable(String name) {
		if (isBoolean(name)) {
			return booleans.get(name);
		} else if (isContinuous(name)) {
			return continuous.get(name);
		} else if (isInteger(name)) {
			return integers.get(name);
		}
		return null;
	}

	public HashMap<String, String> getInputs() {
		HashMap<String, String> inputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (!v.isOutput()) {
				inputs.put(v.getName(), v.getInitValue());
			}
		}
		return inputs;
	}

	public HashMap<String, String> getOutputs() {
		HashMap<String, String> outputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (v.isOutput()) {
				outputs.put(v.getName(), v.getInitValue());
			}
		}
		return outputs;
	}

	public HashMap<String, Properties> getContinuous() {
		HashMap<String, Properties> tempCont = new HashMap<String, Properties>();
		for (Variable v : continuous.values()) {
			Properties prop = new Properties();
			prop.setProperty("value", v.getInitValue());
			prop.setProperty("rate", v.getInitRate());
			tempCont.put(v.getName(), prop);
		}
		return tempCont;
	}

	public HashMap<String, String> getIntegers() {
		HashMap<String, String> tempInt = new HashMap<String, String>();
		for (Variable v : integers.values()) {
			tempInt.put(v.getName(), v.getInitValue());
		}
		return tempInt;
	}

	public String[] getBooleanVars() {
		String[] vars = new String[booleans.size()];
		int i = 0;
		for (String v : booleans.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getBooleanVars(String transition) {
		Set<String> set = transitions.get(transition).getBoolAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getContVars() {
		String[] vars = new String[continuous.size()];
		int i = 0;
		for (String v : continuous.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getContVars(String transition) {
		Set<String> set = transitions.get(transition).getContAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getRateVars(String transition) {
		Set<String> set = transitions.get(transition).getRateAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getIntVars() {
		String[] vars = new String[integers.size()];
		int i = 0;
		for (String v : integers.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getIntVars(String transition) {
		Set<String> set = transitions.get(transition).getIntAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String getInitialVal(String var) {
		if (isBoolean(var)) {
			return booleans.get(var).getInitValue();
		} else if (isInteger(var)) {
			return integers.get(var).getInitValue();
		} else {
			return continuous.get(var).getInitValue();
		}
	}

	public String getInitialRate(String var) {
		if (isContinuous(var)) {
			return continuous.get(var).getInitRate();
		} else
			return null;
	}

	public String getBoolAssign(String transition, String variable) {
		return transitions.get(transition).getBoolAssignment(variable);
	}

	public ExprTree getBoolAssignTree(String transition, String variable) {
		return transitions.get(transition).getBoolAssignTree(variable);
	}

	public String getContAssign(String transition, String variable) {
		return transitions.get(transition).getContAssignment(variable);
	}

	public ExprTree getContAssignTree(String transition, String variable) {
		return transitions.get(transition).getContAssignTree(variable);
	}

	public String getRateAssign(String transition, String variable) {
		return transitions.get(transition).getRateAssignment(variable);
	}

	public ExprTree getRateAssignTree(String transition, String variable) {
		return transitions.get(transition).getRateAssignTree(variable);
	}

	public String getIntAssign(String transition, String variable) {
		return transitions.get(transition).getIntAssignment(variable);
	}

	public ExprTree getIntAssignTree(String transition, String variable) {
		return transitions.get(transition).getIntAssignTree(variable);
	}

	public void removeTransition(String name) {
		if (!transitions.containsKey(name)) {
			return;
		}
		for (Place p : transitions.get(name).getPreset()) {
			removeMovement(p.getName(), name);
		}
		for (Place p : transitions.get(name).getPostset()) {
			removeMovement(name, p.getName());
		}
		transitions.remove(name);
	}

	public void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			for (Transition t : places.get(name).getPreset()) {
				removeMovement(t.getName(), name);
			}
			for (Transition t : places.get(name).getPostset()) {
				removeMovement(name, t.getName());
			}
			places.remove(name);
		}
	}

	/*
	 * public void renamePlace(String oldName, String newName, Boolean ic) {
	 * Place place = new Place(newName, ic); if (oldName != null &&
	 * places.containsKey(oldName)) { places.put(newName, place);
	 * places.remove(oldName); } for (Transition t : transitions.values()) {
	 * t.renamePlace(places.get(oldName), places.get(newName)); } }
	 */

	public void renamePlace(String oldName, String newName) {
		if (oldName != null && places.containsKey(oldName)) {
			places.put(newName, places.get(oldName));
			places.get(newName).changeName(newName);
			places.remove(oldName);
		}
	}

	public void renameTransition(String oldName, String newName) {
		if (oldName != null && transitions.containsKey(oldName)) {
			transitions.put(newName, transitions.get(oldName));
			transitions.get(newName).changeName(newName);
			transitions.remove(oldName);
		}
	}

	public void removeMovement(String from, String to) {
		if (isTransition(from)) {
			transitions.get(from).removePostset(places.get(to));
			places.get(to).removePreset(transitions.get(from));
		} else {
			transitions.get(to).removePreset(places.get(from));
			places.get(from).removePostset(transitions.get(to));
		}
	}

	public void removeInput(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeBoolean(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeOutput(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeContinuous(String name) {
		if (name != null && continuous.containsKey(name)) {
			continuous.remove(name);
			variables.remove(continuous.get(name));
		}
	}

	public void removeInteger(String name) {
		if (name != null && integers.containsKey(name)) {
			integers.remove(name);
			variables.remove(integers.get(name));
		}
	}

	public boolean removeVar(String name) {
		for (Transition t : transitions.values()) {
			if (t.containsAssignment(name)) {
				return false;
			}
		}
		if (name != null && continuous.containsKey(name)) {
			removeContinuous(name);
		} else if (name != null && booleans.containsKey(name)) {
			removeBoolean(name);
		} else if (name != null && integers.containsKey(name)) {
			removeInteger(name);
		} else {
			for (Variable v : variables) {
				if (v.getName().equals(name)) {
					variables.remove(v);
					break;
				}
			}
		}
		return true;
	}

	public void removeAllAssignVar(String name) {
		for (Transition t : transitions.values()) {
			if (t.containsAssignment(name)) {
				t.removeAssignment(name);
			}
		}
	}

	public boolean isTransition(String name) {
		return transitions.containsKey(name);
	}

	public boolean isInput(String var) {
		if (booleans.containsKey(var)) {
			return !booleans.get(var).isOutput();
		}
		return false;
	}

	public boolean isOutput(String var) {
		if (booleans.containsKey(var)) {
			return booleans.get(var).isOutput();
		}
		return false;
	}

	public boolean isBoolean(String var) {
		return booleans.containsKey(var);
	}

	public boolean isContinuous(String var) {
		return continuous.containsKey(var);
	}

	public boolean isInteger(String var) {
		return integers.containsKey(var);
	}

	public boolean isMarked(String place) {
		return places.get(place).isMarked();
	}

	public boolean containsTransition(String name) {
		return transitions.containsKey(name);
	}

	public boolean containsMovement(String name) {
		if (places.containsKey(name)) {
			return places.get(name).isConnected();
		} else {
			return transitions.get(name).isConnected();
		}
	}

	public boolean containsMovement(String from, String to) {
		if (isTransition(from)) {
			return transitions.get(from).containsPostset(to);
		} else {
			return places.get(from).containsPostset(to);
		}
	}

	public Abstraction abstractLhpn(Verification pane) {
		Abstraction abstraction = new Abstraction(this, pane);
		return abstraction;
	}

	private void parseProperty(StringBuffer data) {
		Pattern pattern = Pattern.compile(PROPERTY);
		Matcher lineMatcher = pattern.matcher(data.toString());
		if (lineMatcher.find()) {
			property = lineMatcher.group(1);
		}
	}

	private void parseInOut(StringBuffer data) {
		Properties varOrder = new Properties();
		Pattern inLinePattern = Pattern.compile(INPUT);
		Matcher inLineMatcher = inLinePattern.matcher(data.toString());
		Integer i = 0;
		Integer inLength = 0;
		if (inLineMatcher.find()) {
			Pattern inPattern = Pattern.compile(WORD);
			Matcher inMatcher = inPattern.matcher(inLineMatcher.group(1));
			while (inMatcher.find()) {
				varOrder.setProperty(i.toString(), inMatcher.group());
				i++;
				inLength++;
			}
		}
		Pattern outPattern = Pattern.compile(OUTPUT);
		Matcher outLineMatcher = outPattern.matcher(data.toString());
		if (outLineMatcher.find()) {
			Pattern output = Pattern.compile(WORD);
			Matcher outMatcher = output.matcher(outLineMatcher.group(1));
			while (outMatcher.find()) {
				varOrder.setProperty(i.toString(), outMatcher.group());
				i++;
			}
		}
		Pattern initState = Pattern.compile(INIT_STATE);
		Matcher initMatcher = initState.matcher(data.toString());
		if (initMatcher.find()) {
			Pattern initDigit = Pattern.compile("[01X]+");
			Matcher digitMatcher = initDigit.matcher(initMatcher.group());
			digitMatcher.find();
			String[] initArray = new String[digitMatcher.group().length()];
			Pattern bit = Pattern.compile("[01X]");
			Matcher bitMatcher = bit.matcher(digitMatcher.group());
			i = 0;
			while (bitMatcher.find()) {
				initArray[i] = bitMatcher.group();
				i++;
			}
			for (i = 0; i < inLength; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1")) {
					addInput(name, "true");
				} else if (initArray[i].equals("0")) {
					addInput(name, "false");
				} else {
					addInput(name, "unknown");
				}
			}
			for (i = inLength; i < initArray.length; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1") && name != null) {
					addOutput(name, "true");
				} else if (initArray[i].equals("0") && name != null) {
					addOutput(name, "false");
				} else {
					addOutput(name, "unknown");
				}
			}
		} else {
			if (varOrder.size() != 0) {
				System.out
						.println("WARNING: Boolean variables have not been initialized.");
				for (i = 0; i < varOrder.size(); i++) {
					if (i < inLength) {
						addInput(varOrder.getProperty(i.toString()), "unknown");
					} else {
						addOutput(varOrder.getProperty(i.toString()), "unknown");
					}
				}
			}
		}
	}

	private void parseControlFlow(StringBuffer data) {
		Pattern pattern = Pattern.compile(TRANSITION);
		Matcher lineMatcher = pattern.matcher(data.toString());
		if (lineMatcher.find()) {
			lineMatcher.group(1);
			String name = lineMatcher.group(1).replaceAll("\\+/", "P");
			name = name.replaceAll("-/", "M");
			Pattern transPattern = Pattern.compile(WORD);
			Matcher transMatcher = transPattern.matcher(name);
			while (transMatcher.find()) {
				addTransition(transMatcher.group());
			}
			Pattern placePattern = Pattern.compile(PLACE);
			Matcher placeMatcher = placePattern.matcher(data.toString());
			while (placeMatcher.find()) {
				String temp = placeMatcher.group(1).replaceAll("\\+", "P");
				temp = temp.replaceAll("-", "M");
				String[] tempPlace = temp.split("\\s");
				if (isTransition(tempPlace[0])) {
					if (!places.containsKey(tempPlace[1])) {
						addPlace(tempPlace[1], false);
					}
				} else {
					if (!places.containsKey(tempPlace[0])) {
						addPlace(tempPlace[0], false);
					}
				}
				addMovement(tempPlace[0], tempPlace[1]);
			}
		}
	}

	private void parseVars(StringBuffer data) {
		Properties initCond = new Properties();
		Properties initValue = new Properties();
		Properties initRate = new Properties();
		Pattern linePattern = Pattern.compile(CONTINUOUS);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			while (varMatcher.find()) {
				addContinuous(varMatcher.group());
			}
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			if (initLineMatcher.find()) {
				Pattern initPattern = Pattern.compile(INIT_COND);
				Matcher initMatcher = initPattern.matcher(initLineMatcher
						.group(1));
				while (initMatcher.find()) {
					if (continuous.containsKey(initMatcher.group(1))) {
						initValue.put(initMatcher.group(1), initMatcher
								.group(2));
					}
				}
			}
			Pattern rateLinePattern = Pattern.compile(INIT_RATE);
			Matcher rateLineMatcher = rateLinePattern.matcher(data.toString());
			if (rateLineMatcher.find()) {
				Pattern ratePattern = Pattern.compile(INIT_COND);
				Matcher rateMatcher = ratePattern.matcher(rateLineMatcher
						.group(1));
				while (rateMatcher.find()) {
					initRate.put(rateMatcher.group(1), rateMatcher.group(2));
				}
			}
			for (String s : continuous.keySet()) {
				if (initValue.containsKey(s)) {
					initCond.put("value", initValue.get(s));
				} else {
					if (continuous.get(s).getInitValue() != null) // Added this
																	// condition
																	// for
																	// mergeLPN
																	// methods
																	// sake. SB
						initCond.put("value", continuous.get(s).getInitValue());
					else
						initCond.put("value", "[-inf,inf]");
				}
				if (initRate.containsKey(s)) {
					initCond.put("rate", initRate.get(s));
				} else {
					if (continuous.get(s).getInitRate() != null) // Added this
																	// condition
																	// for
																	// mergeLPN
																	// methods
																	// sake. SB
						initCond.put("rate", continuous.get(s).getInitRate());
					else
						initCond.put("rate", "[-inf,inf]");
				}
				addContinuous(s, initCond);
			}
		}
	}

	private void parseIntegers(StringBuffer data) {
		String initCond = "0";
		Properties initValue = new Properties();
		Pattern linePattern = Pattern.compile(VARIABLES);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			while (varMatcher.find()) {
				if (!continuous.containsKey(varMatcher.group())) {
					addInteger(varMatcher.group(), initCond);
				}
			}
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			if (initLineMatcher.find()) {
				Pattern initPattern = Pattern.compile(INIT_COND);
				Matcher initMatcher = initPattern.matcher(initLineMatcher
						.group(1));
				while (initMatcher.find()) {
					if (integers.containsKey(initMatcher.group(1))) {
						initValue.put(initMatcher.group(1), initMatcher
								.group(2));
					}
				}
			}
			for (String s : integers.keySet()) {
				if (initValue.get(s) != null) {
					initCond = initValue.get(s).toString();
				} else {
					if (integers.get(s).getInitValue() != null) // Added this
																// condition for
																// mergeLPN
																// methods sake.
																// SB
						initCond = integers.get(s).getInitValue();
					else
						initCond = "[-inf,inf]";
				}
				addInteger(s, initCond);
			}
		}
	}

	private void parsePlaces(StringBuffer data) {
		Pattern linePattern = Pattern.compile(PLACES_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			while (markMatcher.find()) {
				String name = markMatcher.group();
				Place place = new Place(name);
				places.put(name, place);
			}
		}
	}

	private void parseMarking(StringBuffer data) {
		Pattern linePattern = Pattern.compile(MARKING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			while (markMatcher.find()) {
				places.get(markMatcher.group()).setMarking(true);
			}
		}
	}

	private boolean parseEnabling(StringBuffer data) {
		boolean error = true;
		Pattern linePattern = Pattern.compile(ENABLING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern enabPattern = Pattern.compile(ENABLING);
			Matcher enabMatcher = enabPattern.matcher(lineMatcher.group(1));
			while (enabMatcher.find()) {
				if (transitions.get(enabMatcher.group(1)).addEnabling(
						enabMatcher.group(2)) == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private boolean parseAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern rangePattern = Pattern.compile(RANGE);
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Matcher varMatcher;
			while (assignMatcher.find()) {
				Transition transition = transitions.get(assignMatcher.group(1));
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				if (varMatcher.find()) {
					String variable = varMatcher.group(1);
					String assignment = varMatcher.group(2);
					if (isInteger(variable)) {
						Matcher rangeMatcher = rangePattern.matcher(assignment);
						if (rangeMatcher.find()) {
							if (rangeMatcher.group(1).matches(INTEGER)
									&& rangeMatcher.group(2).matches(INTEGER)) {
								if (Integer.parseInt(rangeMatcher.group(1)) == Integer
										.parseInt(rangeMatcher.group(2))) {
									transition.addIntAssign(variable,
											rangeMatcher.group(1));
								}
							}
							if (transition.addIntAssign(variable, "uniform("
									+ rangeMatcher.group(1) + ","
									+ rangeMatcher.group(2) + ")") == false) {
								error = false;
							}
						} else {
							if (transition.addIntAssign(variable, assignment) == false) {
								error = false;
							}
						}
					} else {
						Matcher rangeMatcher = rangePattern.matcher(assignment);
						if (rangeMatcher.find()) {
							if (rangeMatcher.group(1).matches(INTEGER)
									&& rangeMatcher.group(2).matches(INTEGER)) {
								if (Integer.parseInt(rangeMatcher.group(1)) == Integer
										.parseInt(rangeMatcher.group(2))) {
									if (transition.addContAssign(variable,
											rangeMatcher.group(1)) == false) {
										error = false;
									}
								}
							}
							if (transition.addContAssign(variable, "uniform("
									+ rangeMatcher.group(1) + ","
									+ rangeMatcher.group(2) + ")") == false) {
								error = false;
							}
						} else if (transition.addContAssign(variable,
								assignment) == false) {
							error = false;
						}
					}
				}
			}
		}
		return error;
	}

	private boolean parseRateAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(RATE_ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern rangePattern = Pattern.compile(RANGE);
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Matcher varMatcher;
			while (assignMatcher.find()) {
				Transition transition = transitions.get(assignMatcher.group(1));
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				while (varMatcher.find()) {
					String variable = varMatcher.group(1);
					String assignment = varMatcher.group(2);
					Matcher rangeMatcher = rangePattern.matcher(assignment);
					if (rangeMatcher.find()) {
						if (rangeMatcher.group(1).matches(INTEGER)
								&& rangeMatcher.group(2).matches(INTEGER)) {
							if (Integer.parseInt(rangeMatcher.group(1)) == Integer
									.parseInt(rangeMatcher.group(2))) {
								if (transition.addRateAssign(variable,
										rangeMatcher.group(1)) == false) {
									error = false;
								}
							}
						}
						if (transition.addRateAssign(variable, "uniform("
								+ rangeMatcher.group(1) + ","
								+ rangeMatcher.group(2) + ")") == false) {
							error = false;
						}
					} else if (transition.addRateAssign(variable, assignment) == false) {
						error = false;
					}
				}
			}
		}
		return error;
	}

	private boolean parseDelayAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(DELAY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern delayPattern = Pattern.compile(DELAY);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			while (delayMatcher.find()) {
				Transition transition = transitions.get(delayMatcher.group(1));
				Pattern rangePattern = Pattern.compile(RANGE);
				Matcher rangeMatcher = rangePattern.matcher(delayMatcher
						.group(2));
				String delay;
				if (rangeMatcher.find()) {
					if (rangeMatcher.group(1).equals(rangeMatcher.group(2))) {
						delay = rangeMatcher.group(1);
					} else {
						delay = "uniform(" + rangeMatcher.group(1) + ","
								+ rangeMatcher.group(2) + ")";
					}
				} else {
					delay = delayMatcher.group(2);
					/*
					if (delay.startsWith("[") && delay.endsWith("]")) {
						delay = delay.substring(1, delay.length() - 1);
					}
					*/
				}
				if (transition.addDelay(delay) == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private boolean parsePriorityAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(PRIORITY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern priorityPattern = Pattern.compile(PRIORITY);
			Matcher priorityMatcher = priorityPattern.matcher(lineMatcher
					.group(1).replace("\\s", ""));
			while (priorityMatcher.find()) {
				Transition transition = transitions.get(priorityMatcher
						.group(1));
				String priority = priorityMatcher.group(2);
				if (transition.addPriority(priority) == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private boolean parseBooleanAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(BOOLEAN_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern transPattern = Pattern.compile(BOOLEAN_TRANS);
			Matcher transMatcher = transPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern assignPattern = Pattern.compile(BOOLEAN_ASSIGN);
			while (transMatcher.find()) {
				Transition transition = transitions.get(transMatcher.group(1));
				Matcher assignMatcher = assignPattern.matcher(transMatcher
						.group(2));
				for (int i = 0; i < booleans.size(); i++) {
					while (assignMatcher.find()) {
						String variable = assignMatcher.group(1);
						String assignment = assignMatcher.group(2);
						if (transition.addBoolAssign(variable, assignment) == false) {
							error = false;
						}
					}
				}
			}
		}
		return error;
	}

	private boolean parseTransitionRate(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(TRANS_RATE_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern delayPattern = Pattern.compile(ENABLING);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1));
			while (delayMatcher.find()) {
				Transition transition = transitions.get(delayMatcher.group(1));
				if (transition.addDelay("exponential(" + delayMatcher.group(2)
						+ ")") == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private void parseFailTransitions(StringBuffer data) {
		Pattern linePattern = Pattern.compile(FAIL_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			for (String s : lineMatcher.group(1).split("\\s")) {
				if (!s.equals("")) {
					transitions.get(s).setFail(true);
				}
			}
		}
	}

	private static final String PROPERTY = "#@\\.property (\\S+?)\\n";

	private static final String INPUT = "\\.inputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String OUTPUT = "\\.outputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String INIT_STATE = "#@\\.init_state \\[(\\w+)\\]";

	private static final String TRANSITION = "\\.dummy([^\\n]*?)\\n";

	private static final String WORD = "(\\S+)";

	private static final String INTEGER = "([-\\d]+)";

	private static final String PLACE = "\\n([\\w_\\+-/&&[^\\.#]]+ [\\w_\\+-/]+)";

	private static final String CONTINUOUS = "#@\\.continuous ([.[^\\n]]+)\\n";

	private static final String VARS_INIT = "#@\\.init_vals \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_RATE = "#@\\.init_rates \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_COND = "<(\\w+)=([\\S^>]+?)>";

	private static final String VARIABLES = "#@\\.variables ([.[^\\n]]+)\\n";

	private static final String PLACES_LINE = "#\\|\\.places ([.[^\\n]]+)\\n";

	private static final String MARKING = "\\w+";

	private static final String MARKING_LINE = "\\.marking \\{(.+)\\}";

	private static final String ENABLING_LINE = "#@\\.enablings \\{([.[^\\}]]+?)\\}";

	private static final String ENABLING = "<([\\S[^=]]+?)=\\[([^\\]]+?)\\]>?";

	private static final String ASSIGNMENT_LINE = "#@\\.assignments \\{([.[^\\}]]+?)\\}";

	private static final String RATE_ASSIGNMENT_LINE = "#@\\.rate_assignments \\{([.[^\\}]]+?)\\}";

	private static final String ASSIGNMENT = "<([\\S[^=]]+?)=\\[(\\S+?)\\]>";

	private static final String ASSIGN_VAR = "([^:]+?):=(.+)";

	private static final String DELAY_LINE = "#@\\.delay_assignments \\{([\\S[^\\}]]+?)\\}";

//	private static final String DELAY = "<([\\w_]+)=(\\S+?)>";

	private static final String DELAY = "<([\\w_]+)=\\[(\\S+?)\\]>";

	private static final String RANGE = "\\[([\\w-]+?),([\\w-]+?)\\]";

	private static final String PRIORITY_LINE = "#@\\.priority_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String PRIORITY = "<([\\w_]+)=\\[(\\S+?)\\]>";

	private static final String TRANS_RATE_LINE = "#@\\.transition_rates \\{([\\S[^\\}]]+?)\\}";

	private static final String FAIL_LINE = "#@\\.failtrans ([.[^\\n]]+)\\n";

	private static final String BOOLEAN_LINE = "#@\\.boolean_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String BOOLEAN_TRANS = "<([\\S[^=]]+?)=\\[(\\S+?)\\]>";

	private static final String BOOLEAN_ASSIGN = "([^:]+?):=(.+)";

//	private static final String BOOLEAN_TRANS = "<([\\w]+?)=([\\S[^>]]+?)>";

//	private static final String BOOLEAN_ASSIGN = "\\[([\\w_]+):=\\s?([\\S^\\]]+?)\\]";

}