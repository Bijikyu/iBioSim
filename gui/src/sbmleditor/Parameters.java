package sbmleditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.UnitDefinition;

import util.MutableBoolean;
import util.Utility;

/**
 * This is a class for creating SBML parameters
 * 
 * @author Chris Myers
 * 
 */
public class Parameters extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JList parameters; // JList of parameters

	private JButton addParam, removeParam, editParam; // parameters buttons

	private JTextField paramID, paramName, paramValue;

	private JComboBox paramUnits;

	private JComboBox paramConst;

	private SBMLDocument document;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	public Parameters(SBMLDocument document, ArrayList<String> usedIDs, MutableBoolean dirty, Boolean paramsOnly, ArrayList<String> getParams,
			String file, ArrayList<String> parameterChanges) {
		super(new BorderLayout());
		this.document = document;
		this.usedIDs = usedIDs;
		this.dirty = dirty;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		Model model = document.getModel();
		JPanel addParams = new JPanel();
		addParam = new JButton("Add Parameter");
		removeParam = new JButton("Remove Parameter");
		editParam = new JButton("Edit Parameter");
		addParams.add(addParam);
		addParams.add(removeParam);
		addParams.add(editParam);
		addParam.addActionListener(this);
		removeParam.addActionListener(this);
		editParam.addActionListener(this);
		if (paramsOnly) {
			addParam.setEnabled(false);
			removeParam.setEnabled(false);
		}
		JLabel parametersLabel = new JLabel("List of Global Parameters:");
		parameters = new JList();
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setViewportView(parameters);
		ListOf listOfParameters = model.getListOfParameters();
		String[] params = new String[(int) model.getNumParameters()];
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			/*
			 * if (parameter.isSetUnits()) { params[i] = parameter.getId() + " "
			 * + parameter.getValue() + " " + parameter.getUnits(); } else {
			 */
			params[i] = parameter.getId() + " " + parameter.getValue();
			// }
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(parameter.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							parameter.setValue(Double.parseDouble(value));
							params[i] += " Modified " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							parameter.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							params[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
			}
		}
		Utility.sort(params);
		parameters.setListData(params);
		parameters.setSelectedIndex(0);
		parameters.addMouseListener(this);
		this.add(parametersLabel, "North");
		this.add(scroll3, "Center");
		this.add(addParams, "South");
	}
	
	/**
	 * Refresh parameter panel
	 */
	public void refreshParameterPanel(SBMLDocument document) {
		String selectedParameter = "";
		if (!parameters.isSelectionEmpty()) {
			selectedParameter = ((String) parameters.getSelectedValue()).split(" ")[0];
		}
		this.document = document;
		Model model = document.getModel();
		ListOf listOfParameters = model.getListOfParameters();
		String[] params = new String[(int) model.getNumParameters()];
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			params[i] = parameter.getId();
			params[i] += " " + parameter.getValue();
			for (int j = 0; j < parameterChanges.size(); j++) {
				if (parameterChanges.get(j).split(" ")[0].equals(params[i].split(" ")[0])) {
					parameterChanges.set(j,
							params[i] + " " + parameterChanges.get(j).split(" ")[2] + " " + parameterChanges.get(j).split(" ")[3]);
					params[i] = parameterChanges.get(j);
				}
			}
		}
		Utility.sort(params);
		int selected = 0;
		for (int i = 0; i < params.length; i++) {
			if (params[i].split(" ")[0].equals(selectedParameter)) {
				selected = i;
			}
		}
		parameters.setListData(params);
		parameters.setSelectedIndex(selected);
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void parametersEditor(String option) {
		if (option.equals("OK") && parameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel parametersPanel;
		if (paramsOnly) {
			parametersPanel = new JPanel(new GridLayout(7, 2));
		}
		else {
			parametersPanel = new JPanel(new GridLayout(5, 2));
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel valueLabel = new JLabel("Value:");
		JLabel unitLabel = new JLabel("Units:");
		JLabel constLabel = new JLabel("Constant:");
		paramID = new JTextField();
		paramName = new JTextField();
		paramValue = new JTextField();
		paramUnits = new JComboBox();
		paramUnits.addItem("( none )");
		Model model = document.getModel();
		ListOf listOfUnits = model.getListOfUnitDefinitions();
		String[] units = new String[(int) model.getNumUnitDefinitions()];
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			units[i] = unit.getId();
		}
		for (int i = 0; i < units.length; i++) {
			if (document.getLevel() > 2
					|| (!units[i].equals("substance") && !units[i].equals("volume") && !units[i].equals("area") && !units[i].equals("length") && !units[i]
							.equals("time"))) {
				paramUnits.addItem(units[i]);
			}
		}
		String[] unitIdsL2V4 = { "substance", "volume", "area", "length", "time", "ampere", "becquerel", "candela", "celsius", "coulomb",
				"dimensionless", "farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux",
				"metre", "mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIdsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray",
				"henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm",
				"pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIds;
		if (document.getLevel() < 3) {
			unitIds = unitIdsL2V4;
		}
		else {
			unitIds = unitIdsL3V1;
		}
		for (int i = 0; i < unitIds.length; i++) {
			paramUnits.addItem(unitIds[i]);
		}
		paramConst = new JComboBox();
		paramConst.addItem("true");
		paramConst.addItem("false");
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Ok", "Close" };
				JPanel p = new JPanel(new GridLayout(4, 2));
				JLabel startLabel = new JLabel("Start:");
				JLabel stopLabel = new JLabel("Stop:");
				JLabel stepLabel = new JLabel("Step:");
				JLabel levelLabel = new JLabel("Level:");
				p.add(startLabel);
				p.add(start);
				p.add(stopLabel);
				p.add(stop);
				p.add(stepLabel);
				p.add(step);
				p.add(levelLabel);
				p.add(level);
				int i = JOptionPane.showOptionDialog(Gui.frame, p, "Sweep", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (i == JOptionPane.YES_OPTION) {
					double startVal = 0.0;
					double stopVal = 0.0;
					double stepVal = 0.0;
					try {
						startVal = Double.parseDouble(start.getText().trim());
						stopVal = Double.parseDouble(stop.getText().trim());
						stepVal = Double.parseDouble(step.getText().trim());
					}
					catch (Exception e1) {
					}
					paramValue.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			paramID.setEditable(false);
			paramName.setEditable(false);
			paramValue.setEnabled(false);
			paramUnits.setEnabled(false);
			paramConst.setEnabled(false);
			sweep.setEnabled(false);
		}
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				Parameter paramet = document.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]);
				paramID.setText(paramet.getId());
				selectedID = paramet.getId();
				paramName.setText(paramet.getName());
				if (paramet.getConstant()) {
					paramConst.setSelectedItem("true");
				}
				else {
					paramConst.setSelectedItem("false");
				}
				if (paramet.isSetValue()) {
					paramValue.setText("" + paramet.getValue());
				}
				if (paramet.isSetUnits()) {
					paramUnits.setSelectedItem(paramet.getUnits());
				}
				if (paramsOnly && (((String) parameters.getSelectedValue()).contains("Modified"))
						|| (((String) parameters.getSelectedValue()).contains("Custom"))
						|| (((String) parameters.getSelectedValue()).contains("Sweep"))) {
					type.setSelectedItem("Modified");
					sweep.setEnabled(true);
					paramValue
							.setText(((String) parameters.getSelectedValue()).split(" ")[((String) parameters.getSelectedValue()).split(" ").length - 1]);
					paramValue.setEnabled(true);
					paramUnits.setEnabled(false);
					if (paramValue.getText().trim().startsWith("(")) {
						try {
							start.setText((paramValue.getText().trim()).split(",")[0].substring(1).trim());
							stop.setText((paramValue.getText().trim()).split(",")[1].trim());
							step.setText((paramValue.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
							if (lev == 1) {
								level.setSelectedIndex(0);
							}
							else {
								level.setSelectedIndex(1);
							}
						}
						catch (Exception e1) {
						}
					}
				}
			}
			catch (Exception e) {
			}
		}
		parametersPanel.add(idLabel);
		parametersPanel.add(paramID);
		parametersPanel.add(nameLabel);
		parametersPanel.add(paramName);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						paramValue.setEnabled(true);
						paramUnits.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						paramValue.setEnabled(false);
						paramUnits.setEnabled(false);
						SBMLDocument d = Gui.readSBML(file);
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetValue()) {
							paramValue.setText(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getValue() + "");
						}
						else {
							paramValue.setText("");
						}
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetUnits()) {
							paramUnits.setSelectedItem(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getUnits()
									+ "");
						}
					}
				}
			});
			parametersPanel.add(typeLabel);
			parametersPanel.add(type);
		}
		parametersPanel.add(valueLabel);
		parametersPanel.add(paramValue);
		if (paramsOnly) {
			parametersPanel.add(new JLabel());
			parametersPanel.add(sweep);
		}
		parametersPanel.add(unitLabel);
		parametersPanel.add(paramUnits);
		parametersPanel.add(constLabel);
		parametersPanel.add(paramConst);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(document, usedIDs, paramID.getText().trim(), selectedID, false);
			if (!error) {
				double val = 0.0;
				if (paramValue.getText().trim().startsWith("(") && paramValue.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((paramValue.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((paramValue.getText().trim()).split(",")[1].trim());
						Double.parseDouble((paramValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev != 1 && lev != 2) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "The level can only be 1 or 2.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e1) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Invalid sweeping parameters.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					try {
						val = Double.parseDouble(paramValue.getText().trim());
					}
					catch (Exception e1) {
						JOptionPane
								.showMessageDialog(Gui.frame, "The value must be a real number.", "Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					String unit = (String) paramUnits.getSelectedItem();
					String param = "";
					if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
						String[] params = new String[parameters.getModel().getSize()];
						for (int i = 0; i < parameters.getModel().getSize(); i++) {
							params[i] = parameters.getModel().getElementAt(i).toString();
						}
						int index = parameters.getSelectedIndex();
						String[] splits = params[index].split(" ");
						for (int i = 0; i < splits.length - 2; i++) {
							param += splits[i] + " ";
						}
						if (!splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (paramValue.getText().trim().startsWith("(") && paramValue.getText().trim().endsWith(")")) {
							double startVal = Double.parseDouble((paramValue.getText().trim()).split(",")[0].substring(1).trim());
							double stopVal = Double.parseDouble((paramValue.getText().trim()).split(",")[1].trim());
							double stepVal = Double.parseDouble((paramValue.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
						}
						else {
							param += "Modified " + val;
						}
					}
					else {
						param = paramID.getText().trim() + " " + val;
						/*
						 * if (!unit.equals("( none )")) { param =
						 * paramID.getText().trim() + " " + val + " " + unit; }
						 */
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("true")) {
						String v = ((String) parameters.getSelectedValue()).split(" ")[0];
						error = SBMLutilities.checkConstant(document, "Parameters", v);
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("false")) {
						String v = ((String) parameters.getSelectedValue()).split(" ")[0];
						error = checkNotConstant(v);
					}
					if (!error) {
						if (option.equals("OK")) {
							String[] params = new String[parameters.getModel().getSize()];
							for (int i = 0; i < parameters.getModel().getSize(); i++) {
								params[i] = parameters.getModel().getElementAt(i).toString();
							}
							int index = parameters.getSelectedIndex();
							String v = ((String) parameters.getSelectedValue()).split(" ")[0];
							Parameter paramet = document.getModel().getParameter(v);
							parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							params = Utility.getList(params, parameters);
							parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							paramet.setId(paramID.getText().trim());
							paramet.setName(paramName.getText().trim());
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							for (int i = 0; i < usedIDs.size(); i++) {
								if (usedIDs.get(i).equals(v)) {
									usedIDs.set(i, paramID.getText().trim());
								}
							}
							paramet.setValue(val);
							if (unit.equals("( none )")) {
								paramet.unsetUnits();
							}
							else {
								paramet.setUnits(unit);
							}
							params[index] = param;
							Utility.sort(params);
							parameters.setListData(params);
							parameters.setSelectedIndex(index);
							if (paramsOnly) {
								int remove = -1;
								for (int i = 0; i < parameterChanges.size(); i++) {
									if (parameterChanges.get(i).split(" ")[0].equals(paramID.getText().trim())) {
										remove = i;
									}
								}
								if (remove != -1) {
									parameterChanges.remove(remove);
								}
								if (!((String) type.getSelectedItem()).equals("Original")) {
									parameterChanges.add(param);
								}
							}
							else {
								SBMLutilities.updateVarId(document, false, v, paramID.getText().trim());
							}
						}
						else {
							String[] params = new String[parameters.getModel().getSize()];
							for (int i = 0; i < parameters.getModel().getSize(); i++) {
								params[i] = parameters.getModel().getElementAt(i).toString();
							}
							int index = parameters.getSelectedIndex();
							Parameter paramet = document.getModel().createParameter();
							paramet.setId(paramID.getText().trim());
							paramet.setName(paramName.getText().trim());
							usedIDs.add(paramID.getText().trim());
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							paramet.setValue(val);
							if (!unit.equals("( none )")) {
								paramet.setUnits(unit);
							}
							JList add = new JList();
							Object[] adding = { param };
							add.setListData(adding);
							add.setSelectedIndex(0);
							parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Utility.add(params, parameters, add, false, null, null, null, null, null, null, Gui.frame);
							params = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								params[i] = (String) adding[i];
							}
							Utility.sort(params);
							parameters.setListData(params);
							parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (document.getModel().getNumParameters() == 1) {
								parameters.setSelectedIndex(0);
							}
							else {
								parameters.setSelectedIndex(index);
							}
						}
						dirty.setValue(true);
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Parameter that is used in a conversion factor must be constant.
	 */
	private boolean checkNotConstant(String val) {
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			Species species = document.getModel().getSpecies(i);
			if (species.getConversionFactor().equals(val)) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Parameter must be constant because it is used as a conversion factor for " + species.getId() + ".",
						" Parameter Must Be Constant", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove a global parameter
	 */
	private void removeParameter() {
		int index = parameters.getSelectedIndex();
		if (index != -1) {
			if (!SBMLutilities.variableInUse(document, ((String) parameters.getSelectedValue()).split(" ")[0], false)) {
				Parameter tempParameter = document.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]);
				ListOf p = document.getModel().getListOfParameters();
				for (int i = 0; i < document.getModel().getNumParameters(); i++) {
					if (((Parameter) p.get(i)).getId().equals(tempParameter.getId())) {
						p.remove(i);
					}
				}
				usedIDs.remove(tempParameter.getId());
				parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(parameters);
				parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < parameters.getModel().getSize()) {
					parameters.setSelectedIndex(index);
				}
				else {
					parameters.setSelectedIndex(index - 1);
				}
				dirty.setValue(true);
			}
		}
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		// if the add parameters button is clicked
		if (e.getSource() == addParam) {
			parametersEditor("Add");
		}
		// if the edit parameters button is clicked
		else if (e.getSource() == editParam) {
			parametersEditor("OK");
			initialsPanel.refreshInitialAssignmentPanel(document);
			rulesPanel.refreshRulesPanel(document);
		}
		// if the remove parameters button is clicked
		else if (e.getSource() == removeParam) {
			removeParameter();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == parameters) {
				parametersEditor("OK");
				initialsPanel.refreshInitialAssignmentPanel(document);
				rulesPanel.refreshRulesPanel(document);
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}

}
