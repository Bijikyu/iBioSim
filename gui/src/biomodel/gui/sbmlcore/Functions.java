package biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.Utility;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.JSBML;

import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


/**
 * This is a class for creating SBML functions
 * 
 * @author Chris Myers
 * 
 */
public class Functions extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addFunction, removeFunction, editFunction;

	private JList functions; // JList of functions

	private BioModel bioModel;

	private ModelEditor modelEditor;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private JCheckBox onPort;

	/* Create initial assignment panel */
	public Functions(BioModel bioModel, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.modelEditor = modelEditor;
		Model model = bioModel.getSBMLDocument().getModel();
		addFunction = new JButton("Add Function");
		removeFunction = new JButton("Remove Function");
		editFunction = new JButton("Edit Function");
		functions = new JList();
		ListOf<FunctionDefinition> listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (!SBMLutilities.isSpecialFunction(function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (SBMLutilities.isSpecialFunction(function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (int j = 0; j < function.getArgumentCount(); j++) {
				if (j != 0) {
					funcs[count] += ", ";
				}
				funcs[count] += SBMLutilities.myFormulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[count] += " ) = " + SBMLutilities.myFormulaToString(function.getBody());
			}
			count++;
		}
		String[] oldFuncs = funcs;
		try {
			funcs = sortFunctions(funcs);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in function definitions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			funcs = oldFuncs;
		}
		JPanel addRem = new JPanel();
		addRem.add(addFunction);
		addRem.add(removeFunction);
		addRem.add(editFunction);
		addFunction.addActionListener(this);
		removeFunction.addActionListener(this);
		editFunction.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Functions:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(functions);
		Utility.sort(funcs);
		functions.setListData(funcs);
		functions.setSelectedIndex(0);
		functions.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Sort functions in order to be evaluated
	 */
	private static String[] sortFunctions(String[] funcs) {
		String[] result = new String[funcs.length];
		String temp;
		String temp2;
		int j = 0;
		int start = 0;
		int end = 0;

		for (int i = 0; i < funcs.length; i++) {
			String[] func = funcs[i].split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
			start = -1;
			end = -1;
			for (int k = 0; k < j; k++) {
				String[] f = result[k].split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
				for (int l = 1; l < f.length; l++) {
					if (f[l].equals(func[0])) {
						end = k;
					}
				}
				for (int l = 1; l < func.length; l++) {
					if (func[l].equals(f[0])) {
						start = k;
					}
				}
			}
			if (end == -1) {
				result[j] = funcs[i];
			}
			else if (start < end) {
				temp = result[end];
				result[end] = funcs[i];
				for (int k = end + 1; k < j; k++) {
					temp2 = result[k];
					result[k] = temp;
					temp = temp2;
				}
				result[j] = temp;
			}
			else {
				result[j] = funcs[i];
				throw new RuntimeException();
			}
			j++;
		}
		return result;
	}

	/**
	 * Creates a frame used to edit functions or create new ones.
	 */
	private void functionEditor(String option) {
		if (option.equals("OK") && functions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No function selected.", "Must Select a Function", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel functionPanel = new JPanel();
		JPanel funcPanel = new JPanel(new GridLayout(5, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel argLabel = new JLabel("Arguments:");
		JLabel eqnLabel = new JLabel("Definition:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JTextField funcID = new JTextField(12);
		JTextField funcName = new JTextField(12);
		JTextField args = new JTextField(12);
		JTextField eqn = new JTextField(12);
		onPort = new JCheckBox();
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				FunctionDefinition function = bioModel.getSBMLDocument().getModel().getFunctionDefinition((((String) functions.getSelectedValue()).split(" ")[0]));
				funcID.setText(function.getId());
				selectedID = function.getId();
				funcName.setText(function.getName());
				if (bioModel.getPortByIdRef(function.getId())!=null) {
					onPort.setSelected(true);
				} else {
					onPort.setSelected(false);
				}
				String argStr = "";
				for (int j = 0; j < function.getArgumentCount(); j++) {
					if (j != 0) {
						argStr += ", ";
					}
					argStr += SBMLutilities.myFormulaToString(function.getArgument(j));
				}
				args.setText(argStr);
				if (function.isSetMath()) {
					eqn.setText("" + SBMLutilities.myFormulaToString(function.getBody()));
				}
				else {
					eqn.setText("");
				}
			}
			catch (Exception e) {
			}
		}
		funcPanel.add(idLabel);
		funcPanel.add(funcID);
		funcPanel.add(nameLabel);
		funcPanel.add(funcName);
		funcPanel.add(argLabel);
		funcPanel.add(args);
		funcPanel.add(eqnLabel);
		funcPanel.add(eqn);
		funcPanel.add(onPortLabel);
		funcPanel.add(onPort);
		functionPanel.add(funcPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, functionPanel, "Function Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), funcID.getText().trim(), selectedID, false);
			if (!error) {
				String[] vars = eqn.getText().trim().split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
				for (int i = 0; i < vars.length; i++) {
					if (vars[i].equals(funcID.getText().trim())) {
						JOptionPane.showMessageDialog(Gui.frame, "Recursive functions are not allowed.", "Recursion Illegal",
								JOptionPane.ERROR_MESSAGE);
						error = true;
						break;
					}
				}
			}
			if (!error) {
				if (eqn.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				} else
					try {
						IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
						if (args.getText().trim().equals("") && ASTNode.parseFormula("lambda(" + eqn.getText().trim() + ")", parser) == null) {
							JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (!args.getText().trim().equals("")
								&& JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")") == null) {
							JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else {
							ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), eqn.getText().trim(), args.getText().trim(), true);
							if (invalidVars.size() > 0) {
								String invalid = "";
								for (int i = 0; i < invalidVars.size(); i++) {
									if (i == invalidVars.size() - 1) {
										invalid += invalidVars.get(i);
									}
									else {
										invalid += invalidVars.get(i) + "\n";
									}
								}
								String message;
								message = "Function can only contain the arguments or other function calls.\n\n" + "Illegal variables:\n" + invalid;
								JTextArea messageArea = new JTextArea(message);
								messageArea.setLineWrap(true);
								messageArea.setWrapStyleWord(true);
								messageArea.setEditable(false);
								JScrollPane scrolls = new JScrollPane();
								scrolls.setMinimumSize(new Dimension(300, 300));
								scrolls.setPreferredSize(new Dimension(300, 300));
								scrolls.setViewportView(messageArea);
								JOptionPane.showMessageDialog(Gui.frame, scrolls, "Illegal Variables", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					} catch (HeadlessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			if (!error) {
				error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(eqn.getText().trim()));
			}
			if (!error) {
				if (option.equals("OK")) {
					String[] funcs = new String[functions.getModel().getSize()];
					for (int i = 0; i < functions.getModel().getSize(); i++) {
						funcs[i] = functions.getModel().getElementAt(i).toString();
					}
					int index = functions.getSelectedIndex();
					String val = ((String) functions.getSelectedValue()).split(" ")[0];
					functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					funcs = Utility.getList(funcs, functions);
					functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					FunctionDefinition f = bioModel.getSBMLDocument().getModel().getFunctionDefinition(val);
					f.setId(funcID.getText().trim());
					f.setName(funcName.getText().trim());
					if (args.getText().trim().equals("")) {
						try {
							f.setMath(JSBML.parseFormula("lambda(" + eqn.getText().trim() + ")"));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						try {
							f.setMath(JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					String oldVal = funcs[index];
					funcs[index] = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = " + eqn.getText().trim();
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in functions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs[index] = oldVal;
					}
					Port port = bioModel.getPortByIdRef(val);
					if (port!=null) {
						if (onPort.isSelected()) {
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						} else {
							SBMLutilities.removeFromParentAndDelete(port);
						}
					} else {
						if (onPort.isSelected()) {
							port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						}
					}
					functions.setListData(funcs);
					functions.setSelectedIndex(index);
					SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, val, funcID.getText().trim());
				}
				else {
					String[] funcs = new String[functions.getModel().getSize()];
					for (int i = 0; i < functions.getModel().getSize(); i++) {
						funcs[i] = functions.getModel().getElementAt(i).toString();
					}
					int index = functions.getSelectedIndex();
					JList add = new JList();
					String addStr;
					addStr = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = " + eqn.getText().trim();
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(funcs, functions, add);
					String[] oldVal = funcs;
					funcs = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						funcs[i] = (String) adding[i];
					}
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in functions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs = oldVal;
					}
					if (!error) {
						FunctionDefinition f = bioModel.getSBMLDocument().getModel().createFunctionDefinition();
						f.setId(funcID.getText().trim());
						f.setName(funcName.getText().trim());
						if (args.getText().trim().equals("")) {
							try {
								f.setMath(JSBML.parseFormula("lambda(" + eqn.getText().trim() + ")"));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							try {
								f.setMath(JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						}
					}
					functions.setListData(funcs);
					functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (bioModel.getSBMLDocument().getModel().getFunctionDefinitionCount() == 1) {
						functions.setSelectedIndex(0);
					}
					else {
						functions.setSelectedIndex(index);
					}
				}
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, functionPanel, "Function Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a function if not in use
	 */
	private void removeFunction() {
		int index = functions.getSelectedIndex();
		if (index != -1) {
			if (!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), ((String) functions.getSelectedValue()).split(" ")[0], false, true, true)) {
				FunctionDefinition tempFunc = bioModel.getSBMLDocument().getModel().getFunctionDefinition(((String) functions.getSelectedValue()).split(" ")[0]);
				ListOf<FunctionDefinition> f = bioModel.getSBMLDocument().getModel().getListOfFunctionDefinitions();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getFunctionDefinitionCount(); i++) {
					if (f.get(i).getId().equals(tempFunc.getId())) {
						f.remove(i);
					}
				}
				for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
					Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
					if (port.isSetIdRef() && port.getIdRef().equals(tempFunc.getId())) {
						bioModel.getSBMLCompModel().getListOfPorts().remove(i);
						break;
					}
				}
				functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(functions);
				functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < functions.getModel().getSize()) {
					functions.setSelectedIndex(index);
				}
				else {
					functions.setSelectedIndex(index - 1);
				}
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			}
		}
	}

	/**
	 * Refresh functions panel
	 */
	public void refreshFunctionsPanel() {
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<FunctionDefinition> listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (!SBMLutilities.isSpecialFunction(function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (SBMLutilities.isSpecialFunction(function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (int j = 0; j < function.getArgumentCount(); j++) {
				if (j != 0) {
					funcs[count] += ", ";
				}
				funcs[count] += SBMLutilities.myFormulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[count] += " ) = " + SBMLutilities.myFormulaToString(function.getBody());
			}
			count++;
		}
		Utility.sort(funcs);
		functions.setListData(funcs);
		functions.setSelectedIndex(0);
	}
	
	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addFunction) {
			functionEditor("Add");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editFunction) {
			functionEditor("OK");
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeFunction) {
			removeFunction();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == functions) {
				functionEditor("OK");
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