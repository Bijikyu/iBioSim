package biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.Utility;

import org.sbml.jsbml.Compartment;
//CompartmentType not supported in Level 3
//import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;

import biomodel.annotation.AnnotationUtility;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


/**
 * This is a class for creating SBML comparments
 * 
 * @author Chris Myers
 * 
 */
public class Compartments extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addCompart, removeCompart, editCompart;

	private JList compartments; // JList of compartments

	private JTextField compID, compSize, compName; // compartment fields;

	private JComboBox compUnits, CompConstants; // compartment units

//	private JComboBox compTypeBox; // compartment type combo box

	private JTextField dimText;

	private boolean editComp = false;

	private BioModel bioModel;
	
	private ModelEditor modelEditor;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private JCheckBox onPort;

	public Compartments(BioModel gcm, ModelEditor modelEditor, Boolean paramsOnly, ArrayList<String> getParams, 
			String file, ArrayList<String> parameterChanges, Boolean editOnly) {
		super(new BorderLayout());
		this.bioModel = gcm;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.modelEditor = modelEditor;
		Model model = gcm.getSBMLDocument().getModel();
		addCompart = new JButton("Add Compartment");
		removeCompart = new JButton("Remove Compartment");
		editCompart = new JButton("Edit Compartment");
		if (paramsOnly | editOnly) {
			addCompart.setEnabled(false);
			removeCompart.setEnabled(false);
		}
		compartments = new JList();
		ListOf<Compartment> listOfCompartments = model.getListOfCompartments();
		String[] comps = new String[model.getCompartmentCount()];
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = listOfCompartments.get(i);
			comps[i] = compartment.getId();
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(compartment.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							compartment.setSize(Double.parseDouble(value));
							comps[i] += " Modified " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							compartment.setSize(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							comps[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
			}
		}
		JPanel addRem = new JPanel();
		addRem.add(addCompart);
		addRem.add(removeCompart);
		addRem.add(editCompart);
		addCompart.addActionListener(this);
		removeCompart.addActionListener(this);
		editCompart.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Compartments:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(compartments);
		Utility.sort(comps);
		compartments.setListData(comps);
		compartments.setSelectedIndex(0);
		compartments.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit compartments or create new ones.
	 */
	public String compartEditor(String option,String selected) {
		JPanel compartPanel = new JPanel();
		JPanel compPanel;
		if (paramsOnly) {
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compPanel = new JPanel(new GridLayout(11, 2));
			}
			else {
				compPanel = new JPanel(new GridLayout(9, 2));
			}
		}
		else {
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compPanel = new JPanel(new GridLayout(9, 2));
			}
			else {
				compPanel = new JPanel(new GridLayout(7, 2));
			}
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
//		JLabel compTypeLabel = new JLabel("Type:");
		JLabel dimLabel = new JLabel("Spatial Dimensions:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel sizeLabel = new JLabel("Spatial Size:");
		JLabel compUnitsLabel = new JLabel("Units:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		compID = new JTextField(12);
		compName = new JTextField(12);
		
//		CompartmentType not supported in Level 3
//		ListOf listOfCompTypes = bioModel.getSBMLDocument().getModel().getListOfCompartmentTypes();
//		String[] compTypeList = new String[(int) bioModel.getSBMLDocument().getModel().getNumCompartmentTypes() + 1];
//		compTypeList[0] = "( none )";
//		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumCompartmentTypes(); i++) {
//			compTypeList[i + 1] = ((CompartmentType) listOfCompTypes.get(i)).getId();
//		}
//		Utility.sort(compTypeList);
//		Object[] choices = compTypeList;
//		compTypeBox = new JComboBox(choices);
		dimText = new JTextField(12);
		dimText.setText("3.0");
		compSize = new JTextField(12);
		compSize.setText("1.0");
		compUnits = new JComboBox();
		String[] optionsTF = { "true", "false" };
		CompConstants = new JComboBox(optionsTF);
		CompConstants.setSelectedItem("true");
		onPort = new JCheckBox();
		editComp = false;
		String selectedID = "";
		if (option.equals("OK")) {
			editComp = true;
		}
		setCompartOptions("3");
		dimText.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (editComp) {
					setCompartOptions(dimText.getText());
				}
				else {
					setCompartOptions(dimText.getText());
				}
			}
		});
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
			@Override
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
					compSize.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			compID.setEnabled(false);
			compName.setEnabled(false);
//			compTypeBox.setEnabled(false);
			dimText.setEnabled(false);
			compUnits.setEnabled(false);
			CompConstants.setEnabled(false);
			compSize.setEnabled(false);
			onPort.setEnabled(false);
			sweep.setEnabled(false);
		}
		if (option.equals("OK")) {
			try {
				Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment(selected);
				selectedID = compartment.getId();
				compName.setText(compartment.getName());
//				CompartmentType not supported in Level 3
//				if (compartment.isSetCompartmentType()) {
//					compTypeBox.setSelectedItem(compartment.getCompartmentType());
//				}
				dimText.setText(String.valueOf(compartment.getSpatialDimensions()));
				setCompartOptions(String.valueOf(compartment.getSpatialDimensions()));
				InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedID);
				if (init!=null) {
					compSize.setText(bioModel.removeBooleans(init.getMath()));
				} else if (compartment.isSetSize()) {
					compSize.setText("" + compartment.getSize());
				}
				if (compartment.isSetUnits()) {
					compUnits.setSelectedItem(compartment.getUnits());
				}
				else {
					compUnits.setSelectedItem("( none )");
				}
				if (compartment.getConstant()) {
					CompConstants.setSelectedItem("true");
				}
				else {
					CompConstants.setSelectedItem("false");
				}
				if (bioModel.getPortByIdRef(compartment.getId())!=null) {
					onPort.setSelected(true);
				} else {
					onPort.setSelected(false);
				}
				if (paramsOnly
						&& (((String) compartments.getSelectedValue()).contains("Modified")
								|| ((String) compartments.getSelectedValue()).contains("Custom") || ((String) compartments.getSelectedValue())
								.contains("Sweep"))) {
					type.setSelectedItem("Modified");
					sweep.setEnabled(true);
					compSize.setText(((String) compartments.getSelectedValue()).split(" ")[((String) compartments.getSelectedValue()).split(" ").length - 1]);
					compSize.setEnabled(true);
					if (compSize.getText().trim().startsWith("(")) {
						try {
							start.setText((compSize.getText().trim()).split(",")[0].substring(1).trim());
							stop.setText((compSize.getText().trim()).split(",")[1].trim());
							step.setText((compSize.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
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
				// TODO: Scott - change for Plugin reading
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(compartment);
				String dimInID = "";
				for(int i = 0; i<sBasePlugin.getDimensionCount(); i++){
					org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.getDimensionByArrayDimension(i);
					dimInID += "[" + dimX.getSize() + "]";
				}
				compID.setText(compartment.getId() + dimInID);
			}
			catch (Exception e) {
			}
		}
		compPanel.add(idLabel);
		compPanel.add(compID);
		compPanel.add(nameLabel);
		compPanel.add(compName);
		compPanel.add(onPortLabel);
		compPanel.add(onPort);
//		if (bioModel.getSBMLDocument().getLevel() < 3) {
//			compPanel.add(compTypeLabel);
//			compPanel.add(compTypeBox);
//		}
		compPanel.add(dimLabel);
		compPanel.add(dimText);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						compSize.setEnabled(true);
					}
					else {
						sweep.setEnabled(false);
						compSize.setEnabled(false);
						SBMLDocument d = SBMLutilities.readSBML(file);
						if (d.getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]).isSetSize()) {
							compSize.setText(d.getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]).getSize() + "");
						}
					}
				}
			});
			compPanel.add(typeLabel);
			compPanel.add(type);
		}
		compPanel.add(sizeLabel);
		compPanel.add(compSize);
		if (paramsOnly) {
			compPanel.add(new JLabel());
			compPanel.add(sweep);
		}
		compPanel.add(compUnitsLabel);
		compPanel.add(compUnits);
		compPanel.add(constLabel);
		compPanel.add(CompConstants);
		compartPanel.add(compPanel);

		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, compartPanel, "Compartment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		//TODO don't forget the whiles
		String[] dimID = new String[]{""};
		String[] dimensionIds = new String[]{""};
		while (error && value == JOptionPane.YES_OPTION) {
			dimID = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), compID.getText(), false);
			error = (dimID == null);
			if(!error){
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = SBMLutilities.checkID(bioModel.getSBMLDocument(), dimID[0].trim(), selectedID, false);
			}
			if (!error && option.equals("OK") && CompConstants.getSelectedItem().equals("true")) {
				String val = selected;
				error = SBMLutilities.checkConstant(bioModel.getSBMLDocument(), "Compartment", val);
			}
			Double addCompSize = 1.0;
			if ((!error)) {
				if (compSize.getText().trim().startsWith("(") && compSize.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((compSize.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((compSize.getText().trim()).split(",")[1].trim());
						Double.parseDouble((compSize.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
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
					InitialAssignments.removeInitialAssignment(bioModel, selectedID);
					try {
						addCompSize = Double.parseDouble(compSize.getText().trim());
					}
					catch (Exception e1) {
						// TODO: do not forget to send dimID as last parameter to this function
						error = InitialAssignments.addInitialAssignment(bioModel, dimID[0].trim(), 
								compSize.getText().trim(),dimID);
						addCompSize = 1.0;
					}
				}
			}
			double dim = 3;
			if (!error) {
				try {
					dim = Double.parseDouble(dimText.getText());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Compartment spatial dimensions must be a real number.", "Invalid Spatial Dimensions",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				String addComp = "";
//				String selCompType = (String) compTypeBox.getSelectedItem();
				// String unit = (String) compUnits.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					String[] comps = new String[compartments.getModel().getSize()];
					for (int i = 0; i < compartments.getModel().getSize(); i++) {
						comps[i] = compartments.getModel().getElementAt(i).toString();
					}
					int index = compartments.getSelectedIndex();
					String[] splits = comps[index].split(" ");
					if (splits.length == 1) {
						addComp += splits[0] + " ";
					}
					else {
						for (int i = 0; i < splits.length - 2; i++) {
							addComp += splits[i] + " ";
						}
					}
					if (splits.length > 1 && !splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
							&& !splits[splits.length - 2].equals("Sweep")) {
						addComp += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
					}
					if (compSize.getText().trim().startsWith("(") && compSize.getText().trim().endsWith(")")) {
						double startVal = Double.parseDouble((compSize.getText().trim()).split(",")[0].substring(1).trim());
						double stopVal = Double.parseDouble((compSize.getText().trim()).split(",")[1].trim());
						double stepVal = Double.parseDouble((compSize.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
						addComp += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
					}
					else {
						addComp += "Modified " + addCompSize;
					}
				}
				else {
					addComp = dimID[0].trim();
					/*
					 * if (!selCompType.equals("( none )")) { addComp += " " +
					 * selCompType; } if (!unit.equals("( none )")) { addComp +=
					 * " " + addCompSize + " " + unit; } else {
					 */
					//addComp += " " + addCompSize;
					// }
				}
				if (!error) {
					if (option.equals("OK")) {
						String[] comps = new String[compartments.getModel().getSize()];
						for (int i = 0; i < compartments.getModel().getSize(); i++) {
							comps[i] = compartments.getModel().getElementAt(i).toString();
						}
						int index = compartments.getSelectedIndex();
						String val = selected;
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						comps = Utility.getList(comps, compartments);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Compartment c = bioModel.getSBMLDocument().getModel().getCompartment(val);
						c.setId(dimID[0].trim());
						c.setName(compName.getText().trim());
//						CompartmentType not supported in Level 3
//						if (!selCompType.equals("( none )")) {
//							c.setCompartmentType(selCompType);
//						}
//						else {
//							c.unsetCompartmentType();
//						}
						// TODO: Scott - edit
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(c);
						sBasePlugin.unsetListOfDimensions();
						for(int i = 0; i<dimID.length-1; i++){
							org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
							dimX.setSize(dimID[i+1]);
							dimX.setArrayDimension(i);
						}
						if (bioModel.getSBMLDocument().getLevel() < 3) {
							c.setSpatialDimensions(dim);
						}
						else {
							c.setSpatialDimensions(dim);
						}
						if (compSize.getText().trim().equals("") || compSize.getText().trim().startsWith("(")) {
							c.unsetSize();
						}
						else {
							c.setSize(addCompSize);
						}
						if (compUnits.getSelectedItem().equals("( none )")) {
							c.unsetUnits();
						}
						else {
							c.setUnits((String) compUnits.getSelectedItem());
						}
						if (CompConstants.getSelectedItem().equals("true")) {
							c.setConstant(true);
						}
						else {
							c.setConstant(false);
						}
						Port port = bioModel.getPortByIdRef(val);
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.COMPARTMENT+"__"+c.getId());
								port.setIdRef(c.getId());
							} else {
								bioModel.getSBMLCompModel().removePort(port);
							}
						} else {
							if (onPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.COMPARTMENT+"__"+c.getId());
								port.setIdRef(c.getId());
							}
						}
						comps[index] = addComp;
						Utility.sort(comps);
						compartments.setListData(comps);
						compartments.setSelectedIndex(index);
						for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
							Species species = bioModel.getSBMLDocument().getModel().getSpecies(i);
							if (species.getCompartment().equals(val)) {
								species.setCompartment(dimID[0].trim());
							}
						}
						for (int i = 0; i < bioModel.getSBMLDocument().getModel().getReactionCount(); i++) {
							Reaction reaction = bioModel.getSBMLDocument().getModel().getReaction(i);
							if (reaction.getCompartment().equals(val)) {
								reaction.setCompartment(dimID[0].trim());
							}
						}
						if (paramsOnly) {
							int remove = -1;
							for (int i = 0; i < parameterChanges.size(); i++) {
								if (parameterChanges.get(i).split(" ")[0].equals(dimID[0].trim())) {
									remove = i;
								}
							}
							if (remove != -1) {
								parameterChanges.remove(remove);
							}
							if (!((String) type.getSelectedItem()).equals("Original")) {
								parameterChanges.add(comps[index]);
							}
						}
						else {
							SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, val, dimID[0].trim());
						}
					}
					else {
						String[] comps = new String[compartments.getModel().getSize()];
						for (int i = 0; i < compartments.getModel().getSize(); i++) {
							comps[i] = compartments.getModel().getElementAt(i).toString();
						}
						int index = compartments.getSelectedIndex();
						Compartment c = bioModel.getSBMLDocument().getModel().createCompartment();
						c.setId(dimID[0].trim());
						c.setName(compName.getText().trim());
//						CompartmentType not supported in Level 3
//						if (!selCompType.equals("( none )")) {
//							c.setCompartmentType(selCompType);
//						}
						// TODO: Scott - add
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(c);
						for(int i = 0; i<dimID.length-1; i++){
							org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
							dimX.setSize(dimID[i+1]);
							dimX.setArrayDimension(i);
						}
						if (bioModel.getSBMLDocument().getLevel() < 3) {
							c.setSpatialDimensions(dim);
						}
						else {
							c.setSpatialDimensions(dim);
						}
						if (!compSize.getText().trim().equals("")) {
							c.setSize(Double.parseDouble(compSize.getText().trim()));
						}
						if (!compUnits.getSelectedItem().equals("( none )")) {
							c.setUnits((String) compUnits.getSelectedItem());
						}
						if (CompConstants.getSelectedItem().equals("true")) {
							c.setConstant(true);
						}
						else {
							c.setConstant(false);
						}
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.COMPARTMENT+"__"+c.getId());
							port.setIdRef(c.getId());
						}
						JList add = new JList();
						String addStr;
						/*
						 * if (!selCompType.equals("( none )")) { addStr =
						 * dimID[0].trim() + " " + selCompType + " " +
						 * compSize.getText().trim(); } else {
						 */
						addStr = dimID[0].trim(); // + " " + compSize.getText().trim();
						/*
						 * } if
						 * (!compUnits.getSelectedItem().equals("( none )")) {
						 * addStr += " " + compUnits.getSelectedItem(); }
						 */
						Object[] adding = { addStr };
						add.setListData(adding);
						add.setSelectedIndex(0);
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(comps, compartments, add);
						comps = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							comps[i] = (String) adding[i];
						}
						Utility.sort(comps);
						compartments.setListData(comps);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (bioModel.getSBMLDocument().getModel().getCompartmentCount() == 1) {
							compartments.setSelectedIndex(0);
						}
						else {
							compartments.setSelectedIndex(index);
						}
					}
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, compartPanel, "Compartment Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return selected;
		}
		return dimID[0].trim();
	}

//	/**
//	 * Variable that is updated by a rule or event cannot be constant
//	 */
//	private boolean checkOutsideCycle(String compID, String outside, int depth) {
//		depth++;
//		if (depth > bioModel.getSBMLDocument().getModel().getCompartmentCount())
//			return true;
//		Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment(outside);
//		if (compartment.isSetOutside()) {
//			if (compartment.getOutside().equals(compID)) {
//				return true;
//			}
//			return checkOutsideCycle(compID, compartment.getOutside(), depth);
//		}
//		return false;
//	}

	/**
	 * Set compartment options based on number of dimensions
	 */
	private void setCompartOptions(String dimStr) {
		double dim = 3;
		try {
			dim = Double.parseDouble(dimStr);
		}
		catch (Exception e1) {
			return;
		}
		if (dim == 3) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf<UnitDefinition> listOfUnits = bioModel.getSBMLDocument().getModel().getListOfUnitDefinitions();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = listOfUnits.get(i);
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponent() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 3)) {
					if (!(bioModel.getSBMLDocument().getLevel() < 3 && unit.getId().equals("volume"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compUnits.addItem("volume");
			}
			compUnits.addItem("litre");
			compUnits.addItem("dimensionless");
			if (!paramsOnly) {
				CompConstants.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim == 2) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf<UnitDefinition> listOfUnits = bioModel.getSBMLDocument().getModel().getListOfUnitDefinitions();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = listOfUnits.get(i);
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 2)) {
					if (!(bioModel.getSBMLDocument().getLevel() < 3 && unit.getId().equals("area"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compUnits.addItem("area");
			}
			compUnits.addItem("dimensionless");
			if (!paramsOnly) {
				CompConstants.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim == 1) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf<UnitDefinition> listOfUnits = bioModel.getSBMLDocument().getModel().getListOfUnitDefinitions();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = listOfUnits.get(i);
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 1)) {
					if (!(bioModel.getSBMLDocument().getLevel() < 3 && unit.getId().equals("length"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compUnits.addItem("length");
			}
			compUnits.addItem("metre");
			compUnits.addItem("dimensionless");
			if (!paramsOnly) {
				CompConstants.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim == 0) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			if (bioModel.getSBMLDocument().getLevel() < 3) {
				compSize.setText("");
				CompConstants.setEnabled(false);
				compSize.setEnabled(false);
			}
		}
		else {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf<UnitDefinition> listOfUnits = bioModel.getSBMLDocument().getModel().getListOfUnitDefinitions();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = listOfUnits.get(i);
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == dim)) {
					compUnits.addItem(unit.getId());
				}
			}
			compUnits.addItem("dimensionless");
			if (!paramsOnly) {
				CompConstants.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
	}
	
	/**
	 * Refresh compartment panel
	 */
	public void refreshCompartmentPanel(BioModel gcm) {
		String selectedCompartment = "";
		if (!compartments.isSelectionEmpty()) {
			selectedCompartment = ((String) compartments.getSelectedValue()).split(" ")[0];
		}
		this.bioModel = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		ListOf<Compartment> listOfCompartments = model.getListOfCompartments();
		String[] comparts = new String[model.getCompartmentCount()];
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = listOfCompartments.get(i);
			comparts[i] = compartment.getId();
			comparts[i] += " " + compartment.getSize();
			if (paramsOnly) {
				for (int j = 0; j < parameterChanges.size(); j++) {
					String[] splits = parameterChanges.get(j).split(" ");
					if (splits[0].equals(comparts[i].split(" ")[0])) {
						parameterChanges.set(j,	comparts[i] + " " + splits[splits.length-2] + " " + 
								splits[splits.length-1]);
						comparts[i] = parameterChanges.get(j);
					}
				}
			}
		}
		Utility.sort(comparts);
		int selected = 0;
		for (int i = 0; i < comparts.length; i++) {
			if (comparts[i].split(" ")[0].equals(selectedCompartment)) {
				selected = i;
			}
		}
		compartments.setListData(comparts);
		compartments.setSelectedIndex(selected);
	}

	/**
	 * Remove a compartment
	 */
	private void removeCompartment() {
		int index = compartments.getSelectedIndex();
		if (index != -1) {
			if (bioModel.getSBMLDocument().getModel().getCompartmentCount() != 1) {
				if (!SBMLutilities.compartmentInUse(bioModel.getSBMLDocument(),
						((String) compartments.getSelectedValue()).split(" ")[0])) {
					if (!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), ((String) compartments.getSelectedValue()).split(" ")[0], false, true, true)) {
						Compartment tempComp = bioModel.getSBMLDocument().getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]);
						ListOf<Compartment> c = bioModel.getSBMLDocument().getModel().getListOfCompartments();
						for (int i = 0; i < bioModel.getSBMLDocument().getModel().getCompartmentCount(); i++) {
							if (c.get(i).getId().equals(tempComp.getId())) {
								c.remove(i);
							}
						}
						for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
							Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
							if (port.isSetIdRef() && port.getIdRef().equals(tempComp.getId())) {
								bioModel.getSBMLCompModel().getListOfPorts().remove(i);
								break;
							}
						}
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						Utility.remove(compartments);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (index < compartments.getModel().getSize()) {
							compartments.setSelectedIndex(index);
						}
						else {
							compartments.setSelectedIndex(index - 1);
						}
						modelEditor.setDirty(true);
						bioModel.makeUndoPoint();
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "Each model must contain at least one compartment.", "Unable To Remove Compartment",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		if (e.getSource() == addCompart) {
			compartEditor("Add","");
		}
		// if the edit compartment button is clicked
		else if (e.getSource() == editCompart) {
			if (compartments.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No compartment selected.", "Must Select a Compartment", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) compartments.getSelectedValue()).split(" ")[0];
			compartEditor("OK",selected);
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove compartment button is clicked
		else if (e.getSource() == removeCompart) {
			removeCompartment();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == compartments) {
				if (compartments.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No compartment selected.", "Must Select a Compartment", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) compartments.getSelectedValue()).split(" ")[0];
				compartEditor("OK",selected);
				initialsPanel.refreshInitialAssignmentPanel(bioModel);
				rulesPanel.refreshRulesPanel();
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}