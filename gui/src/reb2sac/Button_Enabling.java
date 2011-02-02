package reb2sac;

import java.awt.Component;
import java.util.*;

import gcm.gui.GCM2SBMLEditor;

import javax.swing.*;

/**
 * This is the Button_Enabling class. It contains methods that enable and
 * disable radio buttons and textfields.
 * 
 * @author Curtis Madsen
 */
public class Button_Enabling {

	/**
	 * This static method enables and disables the required fields for none and
	 * abstraction.
	 * 
	 * @param lhpn
	 * @param gcmEditor
	 */
	public static void enableNoneOrAbs(JRadioButton ODE, JRadioButton monteCarlo,
			JRadioButton markov, JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel minStepLabel, JTextField minStep, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JRadioButton none,
			JLabel spLabel, JLabel speciesLabel, JButton addIntSpecies, JButton editIntSpecies,
			JButton removeIntSpecies, JTextField rapid1, JTextField rapid2, JTextField qssa,
			JTextField maxCon, JLabel rapidLabel1, JLabel rapidLabel2, JLabel qssaLabel,
			JLabel maxConLabel, JCheckBox usingSSA, JButton clearIntSpecies, JTextField fileStem,
			JLabel fileStemLabel, JList preAbs, JList loopAbs, JList postAbs, JLabel preAbsLabel,
			JLabel loopAbsLabel, JLabel postAbsLabel, JButton addPreAbs, JButton rmPreAbs,
			JButton editPreAbs, JButton addLoopAbs, JButton rmLoopAbs, JButton editLoopAbs,
			JButton addPostAbs, JButton rmPostAbs, JButton editPostAbs, JRadioButton lhpn,
			ArrayList<ArrayList<Component>> speciesInt) {
		if (!usingSSA.isSelected()) {
			ODE.setEnabled(true);
		}
		monteCarlo.setEnabled(true);
		markov.setEnabled(false);
		lhpn.setEnabled(false);
		if (none.isSelected()) {
			preAbs.setEnabled(false);
			loopAbs.setEnabled(false);
			postAbs.setEnabled(false);
			preAbsLabel.setEnabled(false);
			loopAbsLabel.setEnabled(false);
			postAbsLabel.setEnabled(false);
			addPreAbs.setEnabled(false);
			rmPreAbs.setEnabled(false);
			editPreAbs.setEnabled(false);
			addLoopAbs.setEnabled(false);
			rmLoopAbs.setEnabled(false);
			editLoopAbs.setEnabled(false);
			addPostAbs.setEnabled(false);
			rmPostAbs.setEnabled(false);
			editPostAbs.setEnabled(false);
			for (ArrayList<Component> comp : speciesInt) {
				for (Component c : comp) {
					c.setEnabled(false);
				}
			}
			spLabel.setEnabled(false);
			speciesLabel.setEnabled(false);
			addIntSpecies.setEnabled(false);
			editIntSpecies.setEnabled(false);
			removeIntSpecies.setEnabled(false);
			clearIntSpecies.setEnabled(false);
			maxConLabel.setEnabled(false);
			maxCon.setEnabled(false);
			qssaLabel.setEnabled(false);
			qssa.setEnabled(false);
			rapidLabel1.setEnabled(false);
			rapid1.setEnabled(false);
			rapidLabel2.setEnabled(false);
			rapid2.setEnabled(false);
			ArrayList<String> getLists = new ArrayList<String>();
			Object[] objects = getLists.toArray();
			preAbs.setListData(objects);
			loopAbs.setListData(objects);
			getLists = new ArrayList<String>();
			if (monteCarlo.isSelected()) {
				getLists.add("distribute-transformer");
				getLists.add("reversible-to-irreversible-transformer");
			}
			getLists.add("kinetic-law-constants-simplifier");
			objects = getLists.toArray();
			postAbs.setListData(objects);
		}
		else {
			preAbs.setEnabled(true);
			loopAbs.setEnabled(true);
			postAbs.setEnabled(true);
			preAbsLabel.setEnabled(true);
			loopAbsLabel.setEnabled(true);
			postAbsLabel.setEnabled(true);
			addPreAbs.setEnabled(true);
			rmPreAbs.setEnabled(true);
			editPreAbs.setEnabled(true);
			addLoopAbs.setEnabled(true);
			rmLoopAbs.setEnabled(true);
			editLoopAbs.setEnabled(true);
			addPostAbs.setEnabled(true);
			rmPostAbs.setEnabled(true);
			editPostAbs.setEnabled(true);
			for (ArrayList<Component> comp : speciesInt) {
				comp.get(0).setEnabled(true);
				comp.get(1).setEnabled(true);
				if (((JCheckBox) comp.get(0)).isSelected()) {
					for (Component c : comp) {
						c.setEnabled(true);
					}
				}
			}
			spLabel.setEnabled(true);
			speciesLabel.setEnabled(true);
			addIntSpecies.setEnabled(true);
			editIntSpecies.setEnabled(true);
			removeIntSpecies.setEnabled(true);
			clearIntSpecies.setEnabled(true);
			maxConLabel.setEnabled(true);
			maxCon.setEnabled(true);
			qssaLabel.setEnabled(true);
			qssa.setEnabled(true);
			rapidLabel1.setEnabled(true);
			rapid1.setEnabled(true);
			rapidLabel2.setEnabled(true);
			rapid2.setEnabled(true);
			ArrayList<String> getLists = new ArrayList<String>();
			getLists.add("modifier-structure-transformer");
			getLists.add("modifier-constant-propagation");
			Object[] objects = getLists.toArray();
			preAbs.setListData(objects);
			getLists = new ArrayList<String>();
			getLists.add("operator-site-forward-binding-remover");
			getLists.add("dimerization-reduction");
			getLists.add("enzyme-kinetic-rapid-equilibrium-1");
			getLists.add("irrelevant-species-remover");
			getLists.add("inducer-structure-transformer");
			getLists.add("modifier-constant-propagation");
			getLists.add("similar-reaction-combiner");
			getLists.add("modifier-constant-propagation");
			objects = getLists.toArray();
			loopAbs.setListData(objects);
			getLists = new ArrayList<String>();
			if (monteCarlo.isSelected()) {
				getLists.add("distribute-transformer");
				getLists.add("reversible-to-irreversible-transformer");
			}
			getLists.add("kinetic-law-constants-simplifier");
			objects = getLists.toArray();
			postAbs.setListData(objects);
		}
		if (markov.isSelected() || lhpn.isSelected()) {
			if (!usingSSA.isSelected()) {
				ODE.setSelected(true);
			}
			else {
				monteCarlo.setSelected(true);
			}
			monteCarlo.setSelected(false);
			markov.setSelected(false);
			lhpn.setEnabled(false);
			seed.setEnabled(true);
			seedLabel.setEnabled(true);
			runs.setEnabled(true);
			runsLabel.setEnabled(true);
			fileStem.setEnabled(true);
			fileStemLabel.setEnabled(true);
			minStepLabel.setEnabled(true);
			minStep.setEnabled(true);
			stepLabel.setEnabled(true);
			step.setEnabled(true);
			errorLabel.setEnabled(false);
			absErr.setEnabled(false);
			limitLabel.setEnabled(true);
			limit.setEnabled(true);
			intervalLabel.setEnabled(true);
			interval.setEnabled(true);
			if (!usingSSA.isSelected()) {
				simulators.setEnabled(true);
				simulatorsLabel.setEnabled(true);
				explanation.setEnabled(true);
				description.setEnabled(true);
			}
			simulators.removeAllItems();
			simulators.addItem("euler");
			simulators.addItem("gear1");
			simulators.addItem("gear2");
			simulators.addItem("rk4imp");
			simulators.addItem("rk8pd");
			simulators.addItem("rkf45");
			simulators.setSelectedItem("rkf45");
		}
	}

	/**
	 * This static method enables and disables the required fields for nary.
	 * 
	 * @param gcmEditor
	 * @param lhpn
	 */
	public static void enableNary(JRadioButton ODE, JRadioButton monteCarlo, JRadioButton markov,
			JTextField seed, JLabel seedLabel, JTextField runs, JLabel runsLabel,
			JLabel minStepLabel, JTextField minStep, JLabel stepLabel, JTextField step,
			JLabel errorLabel, JTextField absErr, JLabel limitLabel, JTextField limit,
			JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JLabel spLabel,
			JLabel speciesLabel, JButton addIntSpecies, JButton editIntSpecies,
			JButton removeIntSpecies, JTextField rapid1, JTextField rapid2, JTextField qssa,
			JTextField maxCon, JLabel rapidLabel1, JLabel rapidLabel2, JLabel qssaLabel,
			JLabel maxConLabel, JCheckBox usingSSA, JButton clearIntSpecies, JTextField fileStem,
			JLabel fileStemLabel, JList preAbs, JList loopAbs, JList postAbs, JLabel preAbsLabel,
			JLabel loopAbsLabel, JLabel postAbsLabel, JButton addPreAbs, JButton rmPreAbs,
			JButton editPreAbs, JButton addLoopAbs, JButton rmLoopAbs, JButton editLoopAbs,
			JButton addPostAbs, JButton rmPostAbs, JButton editPostAbs, JRadioButton lhpn,
			GCM2SBMLEditor gcmEditor, ArrayList<ArrayList<Component>> speciesInt) {
		ODE.setEnabled(false);
		monteCarlo.setEnabled(true);
		if (!usingSSA.isSelected()) {
			markov.setEnabled(true);
		}
		if (gcmEditor != null) {
			lhpn.setEnabled(true);
		}
		preAbs.setEnabled(true);
		loopAbs.setEnabled(true);
		postAbs.setEnabled(true);
		preAbsLabel.setEnabled(true);
		loopAbsLabel.setEnabled(true);
		postAbsLabel.setEnabled(true);
		addPreAbs.setEnabled(true);
		rmPreAbs.setEnabled(true);
		editPreAbs.setEnabled(true);
		addLoopAbs.setEnabled(true);
		rmLoopAbs.setEnabled(true);
		editLoopAbs.setEnabled(true);
		addPostAbs.setEnabled(true);
		rmPostAbs.setEnabled(true);
		editPostAbs.setEnabled(true);
		for (ArrayList<Component> comp : speciesInt) {
			comp.get(0).setEnabled(true);
			comp.get(1).setEnabled(true);
			if (((JCheckBox) comp.get(0)).isSelected()) {
				for (Component c : comp) {
					c.setEnabled(true);
				}
			}
		}
		spLabel.setEnabled(true);
		speciesLabel.setEnabled(true);
		addIntSpecies.setEnabled(true);
		editIntSpecies.setEnabled(true);
		removeIntSpecies.setEnabled(true);
		clearIntSpecies.setEnabled(true);
		maxConLabel.setEnabled(true);
		maxCon.setEnabled(true);
		qssaLabel.setEnabled(true);
		qssa.setEnabled(true);
		rapidLabel1.setEnabled(true);
		rapid1.setEnabled(true);
		rapidLabel2.setEnabled(true);
		rapid2.setEnabled(true);
		if (ODE.isSelected()) {
			ODE.setSelected(false);
			monteCarlo.setSelected(true);
			markov.setSelected(false);
			seed.setEnabled(true);
			seedLabel.setEnabled(true);
			runs.setEnabled(true);
			runsLabel.setEnabled(true);
			fileStem.setEnabled(true);
			fileStemLabel.setEnabled(true);
			minStepLabel.setEnabled(false);
			minStep.setEnabled(false);
			stepLabel.setEnabled(false);
			step.setEnabled(false);
			errorLabel.setEnabled(false);
			absErr.setEnabled(false);
			limitLabel.setEnabled(true);
			limit.setEnabled(true);
			intervalLabel.setEnabled(true);
			interval.setEnabled(true);
			if (!usingSSA.isSelected()) {
				simulators.setEnabled(true);
				simulatorsLabel.setEnabled(true);
				explanation.setEnabled(true);
				description.setEnabled(true);
			}
			simulators.removeAllItems();
			simulators.addItem("gillespie");
			simulators.addItem("gillespieJava");
			simulators.addItem("mpde");
			simulators.addItem("mp");
			simulators.addItem("mp-adaptive");
			simulators.addItem("mp-event");
			simulators.addItem("emc-sim");
			simulators.addItem("bunker");
			simulators.addItem("nmc");
		}
		ArrayList<String> getLists = new ArrayList<String>();
		getLists.add("modifier-structure-transformer");
		getLists.add("modifier-constant-propagation");
		Object[] objects = getLists.toArray();
		preAbs.setListData(objects);
		getLists = new ArrayList<String>();
		getLists.add("operator-site-forward-binding-remover");
		getLists.add("dimerization-reduction-level-assignment");
		getLists.add("enzyme-kinetic-rapid-equilibrium-1");
		getLists.add("irrelevant-species-remover");
		getLists.add("inducer-structure-transformer");
		getLists.add("modifier-constant-propagation");
		getLists.add("similar-reaction-combiner");
		getLists.add("modifier-constant-propagation");
		objects = getLists.toArray();
		loopAbs.setListData(objects);
		getLists = new ArrayList<String>();
		if (monteCarlo.isSelected()) {
			getLists.add("distribute-transformer");
			getLists.add("reversible-to-irreversible-transformer");
		}
		getLists.add("kinetic-law-constants-simplifier");
		objects = getLists.toArray();
		postAbs.setListData(objects);
	}

	/**
	 * This static method enables and disables the required fields for ODE.
	 */
	public static void enableODE(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel minStepLabel, JTextField minStep, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA,
			JTextField fileStem, JLabel fileStemLabel, JList postAbs) {
		seed.setEnabled(true);
		seedLabel.setEnabled(true);
		runs.setEnabled(true);
		runsLabel.setEnabled(true);
		fileStem.setEnabled(true);
		fileStemLabel.setEnabled(true);
		minStepLabel.setEnabled(true);
		minStep.setEnabled(true);
		stepLabel.setEnabled(true);
		step.setEnabled(true);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(true);
		limit.setEnabled(true);
		intervalLabel.setEnabled(true);
		interval.setEnabled(true);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		simulators.addItem("euler");
		simulators.addItem("gear1");
		simulators.addItem("gear2");
		simulators.addItem("rk4imp");
		simulators.addItem("rk8pd");
		simulators.addItem("rkf45");
		simulators.setSelectedItem("rkf45");
		ArrayList<String> getLists = new ArrayList<String>();
		getLists.add("kinetic-law-constants-simplifier");
		Object[] objects = getLists.toArray();
		postAbs.setListData(objects);
	}

	/**
	 * This static method enables and disables the required fields for Monte
	 * Carlo.
	 * 
	 * @param fileStemLabel
	 * @param fileStem
	 */
	public static void enableMonteCarlo(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel minStepLabel, JTextField minStep, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA,
			JTextField fileStem, JLabel fileStemLabel, JList postAbs) {
		seed.setEnabled(true);
		seedLabel.setEnabled(true);
		runs.setEnabled(true);
		runsLabel.setEnabled(true);
		fileStem.setEnabled(true);
		fileStemLabel.setEnabled(true);
		minStepLabel.setEnabled(true);
		minStep.setEnabled(true);
		stepLabel.setEnabled(true);
		step.setEnabled(true);
		errorLabel.setEnabled(false);
		limitLabel.setEnabled(true);
		limit.setEnabled(true);
		intervalLabel.setEnabled(true);
		interval.setEnabled(true);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		simulators.addItem("gillespie");
		simulators.addItem("gillespieJava");
		simulators.addItem("mpde");
		simulators.addItem("mp");
		simulators.addItem("mp-adaptive");
		simulators.addItem("mp-event");
		simulators.addItem("emc-sim");
		simulators.addItem("bunker");
		simulators.addItem("nmc");
		absErr.setEnabled(false);
		ArrayList<String> getLists = new ArrayList<String>();
		getLists.add("distribute-transformer");
		getLists.add("reversible-to-irreversible-transformer");
		getLists.add("kinetic-law-constants-simplifier");
		Object[] objects = getLists.toArray();
		postAbs.setListData(objects);
	}

	/**
	 * This static method enables and disables the required fields for Markov.
	 * 
	 * @param modelFile
	 */
	public static void enableMarkov(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel minStepLabel, JTextField minStep, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA,
			JTextField fileStem, JLabel fileStemLabel, GCM2SBMLEditor gcmEditor, JList postAbs,
			String modelFile) {
		seed.setEnabled(false);
		seedLabel.setEnabled(false);
		runs.setEnabled(false);
		runsLabel.setEnabled(false);
		fileStem.setEnabled(true);
		fileStemLabel.setEnabled(true);
		minStepLabel.setEnabled(false);
		minStep.setEnabled(false);
		stepLabel.setEnabled(false);
		step.setEnabled(false);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(false);
		limit.setEnabled(false);
		intervalLabel.setEnabled(false);
		interval.setEnabled(false);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		if (gcmEditor != null || modelFile.contains(".lpn")) {
			simulators.addItem("steady-state-markov-chain-analysis");
			simulators.addItem("transient-markov-chain-analysis");
		}
		simulators.addItem("atacs");
		simulators.addItem("ctmc-transient");
		ArrayList<String> getLists = new ArrayList<String>();
		getLists.add("kinetic-law-constants-simplifier");
		Object[] objects = getLists.toArray();
		postAbs.setListData(objects);
	}

	/**
	 * This static method enables and disables the required fields for sbml,
	 * dot, and xhtml.
	 */
	public static void enableSbmlDotAndXhtml(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel minStepLabel, JTextField minStep, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JComboBox intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JTextField fileStem,
			JLabel fileStemLabel, JList postAbs) {
		seed.setEnabled(false);
		seedLabel.setEnabled(false);
		runs.setEnabled(false);
		runsLabel.setEnabled(false);
		fileStem.setEnabled(false);
		fileStemLabel.setEnabled(false);
		minStepLabel.setEnabled(false);
		minStep.setEnabled(false);
		stepLabel.setEnabled(false);
		step.setEnabled(false);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(false);
		limit.setEnabled(false);
		intervalLabel.setEnabled(false);
		interval.setEnabled(false);
		simulators.setEnabled(false);
		simulatorsLabel.setEnabled(false);
		explanation.setEnabled(false);
		description.setEnabled(false);
		fileStem.setText("");
		ArrayList<String> getLists = new ArrayList<String>();
		getLists.add("kinetic-law-constants-simplifier");
		Object[] objects = getLists.toArray();
		postAbs.setListData(objects);
	}
}
