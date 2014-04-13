package biomodel.gui.sbmlcore;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.comp.Submodel;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.sbol.SBOLField;
import biomodel.gui.schematic.ModelEditor;
import biomodel.gui.util.PropertyField;
import biomodel.gui.util.PropertyList;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;


import main.Gui;

public class SpeciesPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * calls constructor to construct the panel
	 * @param selected
	 * @param speciesList
	 * @param componentsList
	 * @param bioModel
	 * @param paramsOnly
	 * @param refGCM
	 * @param modelEditor
	 * @param influencesList
	 * @param conditionsList
	 */
	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList componentsList, 
			BioModel bioModel, boolean paramsOnly, BioModel refGCM,
			ModelEditor modelEditor, boolean inTab){

		super(new BorderLayout());
		constructor(selected, speciesList, componentsList, bioModel, paramsOnly, refGCM, modelEditor, inTab);
	}
	
	/**
	 * constructs the species panel
	 * 
	 * @param selected
	 * @param speciesList
	 * @param influencesList
	 * @param conditionsList
	 * @param componentsList
	 * @param bioModel
	 * @param paramsOnly
	 * @param refGCM
	 * @param gcmEditor
	 */
	private void constructor(String selected, PropertyList speciesList, 
			PropertyList componentsList, BioModel bioModel, boolean paramsOnly,
			BioModel refGCM,  ModelEditor modelEditor, boolean inTab) {

		JPanel grid;
		
		//if this is in analysis mode, only show the sweepable/changeable values
		if (paramsOnly)
			grid = new JPanel(new GridLayout(10,1));
		else {
			
			if (bioModel.getSBMLDocument().getLevel() > 2) {
				grid = new JPanel(new GridLayout(20,1));
			} 
			else {
				grid = new JPanel(new GridLayout(19,1));
			}
		}
		
		this.add(grid, BorderLayout.CENTER);		

		this.selected = selected;
		this.speciesList = speciesList;
		this.components = componentsList;
		this.bioModel = bioModel;
		this.refGCM = refGCM;
		this.paramsOnly = paramsOnly;

		this.modelEditor = modelEditor;


		fields = new HashMap<String, PropertyField>();

		Model model = bioModel.getSBMLDocument().getModel();
		species = model.getSpecies(selected);

		String origString = "default";
		PropertyField field = null;		
		
		// ID field
		field = new PropertyField(GlobalConstants.ID, species.getId(), null, null, Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		
		if (!paramsOnly) grid.add(field);
			
		// Name field
		field = new PropertyField(GlobalConstants.NAME, species.getName(), null, null, Utility.NAMEstring, paramsOnly, 
				"default", false);
		fields.put(GlobalConstants.NAME, field);
		
		if (!paramsOnly) grid.add(field);		

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);

		if (!paramsOnly) grid.add(tempPanel);
		
		// Dimension field
		tempPanel = new JPanel();
		dimensionType = new JComboBox();
		dimensionType.addItem("Scalar");
		dimensionType.addItem("Vector");
		dimensionType.addItem("Matrix");
		dimensionType.addActionListener(this);
		dimensionX = new JComboBox();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
			Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
			if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
				dimensionX.addItem(param.getId());
			}
		}
		dimensionY = new JComboBox();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
			Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
			if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
				dimensionY.addItem(param.getId());
			}
		}
		dimensionX.setEnabled(false);
		dimensionY.setEnabled(false);
		dimensionTypeLabel = new JLabel("Array Dimension");
		dimensionSizeLabel = new JLabel("Array Size");
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(dimensionTypeLabel);
		tempPanel.add(dimensionType);
		if (!paramsOnly) grid.add(tempPanel);
		tempPanel = new JPanel(new GridLayout(1, 2));
		tempPanel.add(dimensionSizeLabel);
		tempPanel.add(dimensionX);
		if (!paramsOnly) grid.add(tempPanel);
		tempPanel = new JPanel(new GridLayout(1, 2));
		tempPanel.add(new JLabel());
		tempPanel.add(dimensionY);
		if (!paramsOnly) grid.add(tempPanel);
		
		// Set dimension type and size parameter
		String[] sizes = new String[2];
		if(paramsOnly){
			dimensionType.setEnabled(false);
		}
		sizes[0] = AnnotationUtility.parseVectorSizeAnnotation(species);
		if(sizes[0]==null){
			sizes = AnnotationUtility.parseMatrixSizeAnnotation(species);
			if(sizes==null){
				dimensionType.setSelectedIndex(0);
				dimensionX.setEnabled(false);
				dimensionY.setEnabled(false);
			}
			else{
				dimensionType.setSelectedIndex(2);
				dimensionX.setEnabled(true);
				dimensionY.setEnabled(true);
				dimensionX.setSelectedItem(sizes[0]);
				dimensionY.setSelectedItem(sizes[1]);
			}
		}
		else{
			dimensionType.setSelectedIndex(1);
			dimensionX.setEnabled(true);
			dimensionX.setSelectedItem(sizes[0]);
			dimensionY.setEnabled(false);
		}
		
		// compartment field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Compartment");
		compartBox = MySpecies.createCompartmentChoices(bioModel);		
		compartBox.setSelectedItem(species.getCompartment());
		compartBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(compartBox);
		
		if (!paramsOnly) grid.add(tempPanel);
		
		tempPanel = new JPanel(new GridLayout(1, 4));
		iIndex = new JTextField(10);
		jIndex = new JTextField(10);
		tempPanel.add(new JLabel("Compartment Indices"));
		tempPanel.add(new JLabel());
		tempPanel.add(iIndex);
		tempPanel.add(jIndex);
		
		String[] indecies = new String[2];
		indecies[0] = AnnotationUtility.parseRowIndexAnnotation(species);
		if(indecies[0]!=null){
			indecies[1] = AnnotationUtility.parseColIndexAnnotation(species);
			if(indecies[1]==null){
				iIndex.setText(indecies[0]);
				jIndex.setText("");
			}
			else{
				iIndex.setText(indecies[0]);
				jIndex.setText(indecies[1]);
			}
		}
		else{
			iIndex.setText("");
			jIndex.setText("");
		}
			
		if (!paramsOnly) grid.add(tempPanel);
		
		// Initial field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.INITIAL_STRING);
			if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialAmount()) {
				defaultValue = "" + refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialAmount();
			} else if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialConcentration()) {
				defaultValue = "[" + refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialConcentration() + "]";
			}
			field = new PropertyField(GlobalConstants.INITIAL_STRING, 
					bioModel.getParameter(GlobalConstants.INITIAL_STRING), origString, defaultValue,
					Utility.SWEEPstring + "|" + Utility.CONCstring, paramsOnly, origString, false);
			fields.put(GlobalConstants.INITIAL_STRING, field);
			String sweep = AnnotationUtility.parseSweepAnnotation(species);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (species.isSetInitialAmount() &&	!defaultValue.equals("" + species.getInitialAmount())) {
				field.setValue("" + species.getInitialAmount());
				field.setCustom();
			} else if (species.isSetInitialConcentration() && !defaultValue.equals("["+species.getInitialConcentration()+"]")) {
				field.setValue("[" + species.getInitialConcentration() + "]");
				field.setCustom();
			}
			grid.add(field);
		}
		else {
			tempPanel = new JPanel();
			tempLabel = new JLabel("Initial Amount/Concentration");
			initialField = new JTextField("");
			InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(species.getId());
			if (init!=null) {
				initialField.setText(bioModel.removeBooleans(init.getMath()));
			} else if (species.isSetInitialAmount()) {
				initialField.setText("" + species.getInitialAmount());
			} else if (species.isSetInitialConcentration()) {
				initialField.setText("[" + species.getInitialConcentration() + "]");
			}
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(initialField);
			grid.add(tempPanel);
		}
		
		// Units field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Units");
		unitsBox = MySpecies.createUnitsChoices(bioModel);
		
		if (species.isSetUnits()) {
			
			unitsBox.setSelectedItem(species.getUnits());
		}
		
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(unitsBox);

		if (!paramsOnly) grid.add(tempPanel);
		
		// Conversion factor field
		if (bioModel.getSBMLDocument().getLevel() > 2) {
			
			tempPanel = new JPanel();
			tempLabel = new JLabel("Conversion Factor");
			convBox = MySpecies.createConversionFactorChoices(bioModel);
			
			if (species.isSetConversionFactor()) {
				convBox.setSelectedItem(species.getConversionFactor());
			}
			
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(convBox);

			if (!paramsOnly) grid.add(tempPanel);
			
			tempPanel = new JPanel(new GridLayout(1, 4));
			conviIndex = new JTextField(10);
			convjIndex = new JTextField(10);
			tempPanel.add(new JLabel("Conversion Factor Indices"));
			tempPanel.add(new JLabel());
			tempPanel.add(conviIndex);
			tempPanel.add(convjIndex);
			
			indecies[0] = AnnotationUtility.parseRowIndexAnnotation(species);
			if(indecies[0]!=null){
				indecies[1] = AnnotationUtility.parseColIndexAnnotation(species);
				if(indecies[1]==null){
					conviIndex.setText(indecies[0]);
					convjIndex.setText("");
				}
				else{
					conviIndex.setText(indecies[0]);
					convjIndex.setText(indecies[1]);
				}
			}
			else{
				conviIndex.setText("");
				convjIndex.setText("");
			}
				
			if (!paramsOnly) grid.add(tempPanel);
		}
		
		// Boundary condition field
		tempPanel = new JPanel(new GridLayout(1,3));
		specBoundary = new JCheckBox("Boundary Condition");
		specBoundary.setSelected(species.getBoundaryCondition());
		tempPanel.add(specBoundary);

		// Constant field
		specConstant = new JCheckBox("Constant");
		specConstant.setSelected(species.getConstant());
		tempPanel.add(specConstant);

		// Has only substance units field
		specHasOnly = new JCheckBox("Has Only Substance Units");
		specHasOnly.setSelected(species.getHasOnlySubstanceUnits());
		tempPanel.add(specHasOnly);

		if (!paramsOnly) grid.add(tempPanel);
		
		//diffusible/constitutive checkboxes
		diffusion = BioModel.getDiffusionReaction(selected,bioModel.getSBMLDocument().getModel());
		constitutive = bioModel.getConstitutiveReaction(selected);
		degradation = bioModel.getDegradationReaction(selected);
		complex = bioModel.getComplexReaction(selected);
		
		tempPanel = new JPanel(new GridLayout(1,3));
		specDiffusible = new JCheckBox("Diffusible");
		specDiffusible.setSelected(diffusion != null);
		specDiffusible.addActionListener(this);
		specDiffusible.setActionCommand("constdiffChanged");
		specConstitutive = new JCheckBox("Constitutive");
		specConstitutive.setSelected(constitutive != null);
		specConstitutive.addActionListener(this);
		specConstitutive.setActionCommand("constdiffChanged");
		specDegradable = new JCheckBox("Degrades");
		specDegradable.setSelected(degradation != null);
		specDegradable.addActionListener(this);
		specDegradable.setActionCommand("constdiffChanged");
		
		tempPanel.add(specConstitutive);
		tempPanel.add(specDegradable);
		tempPanel.add(specDiffusible);
		
		if (!paramsOnly) grid.add(tempPanel);
		
		//mark as interesting field
		if (paramsOnly) {
			
			String thresholdText = "";
			boolean speciesMarked = false;
			
			ArrayList<String> interestingSpecies = modelEditor.getReb2Sac().getInterestingSpeciesAsArrayList();				
			
			//look for the selected species among the already-interesting
			//if it is interesting, populate the field with its data
			for (String speciesInfo : interestingSpecies) {
				if (speciesInfo.split(" ")[0].equals(selected)) {
					speciesMarked = true;
					thresholdText = speciesInfo.replace(selected, "").trim();
					break;
				}
			}			
			
			tempPanel = new JPanel(new GridLayout(1, 2));
			specInteresting = new JCheckBox("Mark as Interesting");
			specInteresting.addActionListener(this);
			specInteresting.setSelected(speciesMarked);
			tempPanel.add(specInteresting);
			thresholdTextField = new JTextField(thresholdText);
			
			/*if (!bioModel.isInput(selected) &&
					(modelEditor.getBioModel().getBiochemicalSpecies() != null &&
					!modelEditor.getBioModel().getBiochemicalSpecies().contains(selected))) { */
			tempPanel.add(thresholdTextField);
			specInteresting.setText("Mark as Interesting (Enter comma-separated thresholds)");
			//}
			
			grid.add(tempPanel);
		}		
		
		// kocr
		origString = "default";
		String defaultValue = bioModel.getParameter(GlobalConstants.OCR_STRING);
		String formatString = Utility.NUMstring;
		if (paramsOnly) {
			//defaultValue = refGCM.getParameter(GlobalConstants.OCR_STRING);
			if (constitutive != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(constitutive.getId());
				LocalParameter ko = refProd.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
				if (ko != null) {
					defaultValue = ko.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.OCR_STRING, bioModel.getParameter(GlobalConstants.OCR_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (constitutive != null) {
			LocalParameter ko = constitutive.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
			String sweep = AnnotationUtility.parseSweepAnnotation(ko);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (ko != null && !defaultValue.equals(ko.getValue()+"")) {
				field.setValue(ko.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.OCR_STRING, field);
		grid.add(field);
		
		// stoichiometry
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (constitutive != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(constitutive.getId());
				LocalParameter np = refProd.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
				if (np != null) {
					defaultValue = np.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, bioModel.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (constitutive != null) {
			LocalParameter np = constitutive.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
			String sweep = AnnotationUtility.parseSweepAnnotation(np);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (np != null && !defaultValue.equals(np.getValue()+"")) {
				field.setValue(np.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		grid.add(field);		
		
		// Decay field
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.KDECAY_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (degradation != null) {
				Reaction refDeg = refGCM.getSBMLDocument().getModel().getReaction(degradation.getId());
				LocalParameter kd = refDeg.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
				if (kd != null) {
					defaultValue = kd.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KDECAY_STRING, 
				bioModel.getParameter(GlobalConstants.KDECAY_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (degradation != null) {
			LocalParameter kd = degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
			String sweep = AnnotationUtility.parseSweepAnnotation(kd);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (kd != null && !defaultValue.equals(""+kd.getValue())) {
				field.setValue(""+kd.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.KDECAY_STRING, field);
		grid.add(field);
		
		//Extracellular decay field
		/*
		origString = "default";
		if (paramsOnly) {
			
			String defaultValue = refGCM.getParameter(GlobalConstants.KECDECAY_STRING);
			
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KECDECAY_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KECDECAY_STRING);
				origString = "custom";
			}
			else { 
				defaultValue = gcm.getParameter(GlobalConstants.KECDECAY_STRING);
			}
			
			field = new PropertyField(GlobalConstants.KECDECAY_STRING, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString, false);
		}
		else {
			
			field = new PropertyField(GlobalConstants.KECDECAY_STRING, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), origString, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), Utility.NUMstring, paramsOnly,
					origString, false);
		}
		
		fields.put(GlobalConstants.KECDECAY_STRING, field);
		grid.add(field);
		*/
		
		// Complex Equilibrium Constant Field
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (complex != null) {
				Reaction refComp = refGCM.getSBMLDocument().getModel().getReaction(complex.getId());
				LocalParameter kc_f = refComp.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
				LocalParameter kc_r = refComp.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
				if (kc_f != null && kc_r != null) {
					defaultValue = kc_f.getValue()+"/"+kc_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, 
				bioModel.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (complex != null) {
			LocalParameter kc_f = complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			LocalParameter kc_r = complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			String sweep = AnnotationUtility.parseSweepAnnotation(kc_f);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (kc_f != null && kc_r != null && !defaultValue.equals(kc_f.getValue()+"/"+kc_r.getValue())) {
				field.setValue(kc_f.getValue()+"/"+kc_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.KCOMPLEX_STRING, field);
		grid.add(field);

		// Membrane Diffusible Field
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_MEMDIFF_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_MEMDIFF_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (diffusion != null) {
				Reaction refDiff = refGCM.getSBMLDocument().getModel().getReaction(diffusion.getId());
				LocalParameter kmdiff_f = refDiff.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
				LocalParameter kmdiff_r = refDiff.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
				if (kmdiff_f != null && kmdiff_r != null) {
					defaultValue = kmdiff_f.getValue()+"/"+kmdiff_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		}
		field = new PropertyField(GlobalConstants.MEMDIFF_STRING, bioModel
				.getParameter(GlobalConstants.MEMDIFF_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (diffusion != null) {
			LocalParameter kmdiff_f = diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
			LocalParameter kmdiff_r = diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
			String sweep = AnnotationUtility.parseSweepAnnotation(kmdiff_f);
			if (sweep != null) {
				field.setValue(sweep);
				field.setCustom();
			} else if (kmdiff_f != null && kmdiff_r != null && !defaultValue.equals(kmdiff_f.getValue()+"/"+kmdiff_r.getValue())) {
				field.setValue(kmdiff_f.getValue()+"/"+kmdiff_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.MEMDIFF_STRING, field);
		grid.add(field);
		
		//extracellular diffusion field
		/*
		origString = "default";
		if (paramsOnly) {
			
			String defaultValue = refGCM.getParameter(GlobalConstants.KECDIFF_STRING);
			
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KECDIFF_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KECDIFF_STRING);
				origString = "custom";
			}
			else { 
				defaultValue = gcm.getParameter(GlobalConstants.KECDIFF_STRING);
			}
			
			field = new PropertyField(GlobalConstants.KECDIFF_STRING, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString, false);
		}
		else {
			
			field = new PropertyField(GlobalConstants.KECDIFF_STRING, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), origString, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), Utility.NUMstring, paramsOnly,
					origString, false);
		}
		
		fields.put(GlobalConstants.KECDIFF_STRING, field);
		grid.add(field);
		*/
		
		// Parse out GCM and SBOL annotations and add to respective fields
		if (!paramsOnly) {
			// Field for annotating species with SBOL DNA components
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			sbolField = new SBOLField(sbolURIs, sbolStrand, GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 
					3, false);

			grid.add(sbolField);

			if (bioModel.isInput(species.getId())) {
				typeBox.setSelectedItem(GlobalConstants.INPUT);
				specDiffusible.setSelected(false);
			} else if (bioModel.isOutput(species.getId())) {
				typeBox.setSelectedItem(GlobalConstants.OUTPUT);
				specDiffusible.setSelected(false);
			} else {
				typeBox.setSelectedItem(GlobalConstants.INTERNAL);
			}
		}
		

		
		setFieldEnablings();
		
		boolean display = false;
		
		if (!inTab) {
			while (!display) {
				//show the panel; handle the data
				int value = JOptionPane.showOptionDialog(Gui.frame, this, "Species Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				display = handlePanelData(value);
			}
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}
	
//	private boolean checkSbolValues() {
//		for (SBOLField sf : sbolFields.values()) {
//			if (!sf.isValidText())
//				return false;
//		}
//		return true;
//	}

	/**
	 * adds interesting species to a list in Reb2Sac.java
	 * 
	 * @return whether the values were okay for adding or not
	 */
	private boolean addInterestingSpecies() {
		
		if (specInteresting.isSelected()) {
			
			String thresholdText = thresholdTextField.getText();
			ArrayList<Integer> thresholdValues = new ArrayList<Integer>();

			//if the threshold text is empty, don't do anything to it
			if (!thresholdText.isEmpty()) {
				
				try {
					// check the threshold values for validity
					for (String threshold : thresholdText.trim().split(",")) {
						thresholdValues.add(Integer.parseInt(threshold.trim()));
					}
				}
				catch (NumberFormatException e) {
	
					Utility.createErrorMessage("Error", "Threshold values must be comma-separated integers");
					return false;
				}
	
				Integer[] threshVals = thresholdValues.toArray(new Integer[0]);
				Arrays.sort(threshVals);
				thresholdText = "";
	
				for (Integer thresholdVal : threshVals)
					thresholdText += thresholdVal.toString() + ",";
	
				// take off the last ", "
				if (threshVals.length > 0)
					thresholdText = thresholdText.substring(0, thresholdText.length() - 1);
			}

			// everything is okay, so add the interesting species to the list
			modelEditor.getReb2Sac().addInterestingSpecies(selected + " " + thresholdText);
		}
		else {
			modelEditor.getReb2Sac().removeInterestingSpecies(selected);
		}
		return true;
	}
	
	/**
	 * handles the panel data
	 * 
	 * @return
	 */
	public boolean handlePanelData(int value) {	
		
		species = bioModel.getSBMLDocument().getModel().getSpecies(selected);
		
		// the new id of the species. Will be filled in later.
		String newSpeciesID = null;
		
		// if the value is -1 (user hit escape) then set it equal to the cancel value
		if(value == -1) {
			
			for(int i=0; i<options.length; i++) {
				
				if(options[i] == options[1])
					value = i;
			}
		}
		
		// "OK"
		if (options[value].equals(options[0])) {
			
			boolean valueCheck = checkValues();
			
			if (!valueCheck) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			
			if (selected == null) {
				if (bioModel.isSIdInUse(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "ID already exists.");
					return false;
				}
			}
			else if (!selected.equals(fields.get(GlobalConstants.ID).getValue())) {
				
				if (bioModel.isSIdInUse(fields.get(GlobalConstants.ID).getValue())) {
					
					Utility.createErrorMessage("Error", "ID already exists.");
					return false;
				}
			}
			
			newSpeciesID = fields.get(GlobalConstants.ID).getValue();

			if (selected != null) {			
				
				//check and add interesting species information
				if (paramsOnly) {
					if (!addInterestingSpecies())
					return false;
				}
				
				if (!paramsOnly) {
					
					InitialAssignments.removeInitialAssignment(bioModel, selected);
					if (Utility.isValid(initialField.getText(), Utility.NUMstring)) {
						species.setInitialAmount(Double.parseDouble(initialField.getText()));
					} 
					else if (Utility.isValid(initialField.getText(), Utility.CONCstring)) {
						species.setInitialConcentration(Double.parseDouble(initialField.getText().substring(1,initialField.getText().length()-1)));
					} else {
						boolean error;
						if (dimensionType.getSelectedIndex()==0) {
							error = InitialAssignments.addInitialAssignment(bioModel, species.getId(), initialField.getText().trim(),"","");
						} else if (dimensionType.getSelectedIndex()==1) {
							error = InitialAssignments.addInitialAssignment(bioModel, species.getId(), initialField.getText().trim(),
									(String)dimensionX.getSelectedItem(),"");
						} else {
							error = InitialAssignments.addInitialAssignment(bioModel, species.getId(), initialField.getText().trim(),
									(String)dimensionX.getSelectedItem(),(String)dimensionY.getSelectedItem());
						}
						if (error) return false;
						species.setInitialAmount(Double.parseDouble("0.0"));
					}
					// Checks whether SBOL annotation on model needs to be deleted later when annotating species with SBOL
//					if (sbolField.getSBOLURIs().size() > 0 && 
//							bioModel.getElementSBOLCount() == 0 && bioModel.getModelSBOLAnnotationFlag()) {
//						Object[] options = { "OK", "Cancel" };
//						int choice = JOptionPane.showOptionDialog(null, 
//								"SBOL associated to model elements can't coexist with SBOL associated to model itself unless" +
//								" the latter was previously generated from the former.  Remove SBOL associated to model?", 
//								"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//						if (choice == JOptionPane.OK_OPTION)
//							removeModelSBOLAnnotationFlag = true;
//						else {
//							if (species.isSetInitialAmount())
//								species.unsetInitialAmount();
//							else if (species.isSetInitialConcentration())
//								species.unsetInitialConcentration();
//							return false;
//						}
//					}
					
					species.setName(fields.get(GlobalConstants.NAME).getValue());
					
					species.setBoundaryCondition(specBoundary.isSelected());
					species.setConstant(specConstant.isSelected());
					species.setCompartment((String)compartBox.getSelectedItem());
					species.setHasOnlySubstanceUnits(specHasOnly.isSelected());
					
					String unit = (String) unitsBox.getSelectedItem();
					
					if (unit.equals("( none )")) {
						species.unsetUnits();
					}
					else {
						species.setUnits(unit);
					}
					
					String convFactor = null;
					
					if (bioModel.getSBMLDocument().getLevel() > 2) {
						convFactor = (String) convBox.getSelectedItem();
						
						if (convFactor.equals("( none )")) {
							species.unsetConversionFactor();
						}
						else {
							species.setConversionFactor(convFactor);
						}
					}
					if (dimensionType.getSelectedIndex() == 1){
						AnnotationUtility.removeMatrixSizeAnnotation(species);
						AnnotationUtility.setVectorSizeAnnotation(species,(String) dimensionX.getSelectedItem());
					}
					else if (dimensionType.getSelectedIndex() == 2){
						AnnotationUtility.removeVectorSizeAnnotation(species);
						AnnotationUtility.setMatrixSizeAnnotation(species,(String) dimensionX.getSelectedItem(), 
								(String) dimensionY.getSelectedItem());
					}
					else{
						AnnotationUtility.removeVectorSizeAnnotation(species);
						AnnotationUtility.removeMatrixSizeAnnotation(species);
					}
					if (!iIndex.getText().equals("")) {
						AnnotationUtility.setRowIndexAnnotation(species,iIndex.getText());
					} else {
						AnnotationUtility.removeRowIndexAnnotation(species);
					}
					if (!jIndex.getText().equals("")) {
						AnnotationUtility.setColIndexAnnotation(species,jIndex.getText());
					} else {
						AnnotationUtility.removeColIndexAnnotation(species);
					} 
					if (!conviIndex.getText().equals("")) {
						AnnotationUtility.setConversionRowIndexAnnotation(species,conviIndex.getText());
					} else {
						AnnotationUtility.removeConversionRowIndexAnnotation(species);
					} 
					if (!convjIndex.getText().equals("")) {
						AnnotationUtility.setConversionColIndexAnnotation(species,convjIndex.getText());
					} else {						
						AnnotationUtility.removeConversionColIndexAnnotation(species);
					}
				} else {
					PropertyField f = fields.get(GlobalConstants.INITIAL_STRING);
					if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
						if (Utility.isValid(f.getValue(), Utility.NUMstring)) {
							species.setInitialAmount(Double.parseDouble(f.getValue()));
							AnnotationUtility.removeSweepAnnotation(species);
						} 
						else if (Utility.isValid(f.getValue(), Utility.CONCstring)) {
							species.setInitialConcentration(Double.parseDouble(f.getValue().substring(1,f.getValue().length()-1)));
							AnnotationUtility.removeSweepAnnotation(species);
						} else {
							AnnotationUtility.setSweepAnnotation(species, f.getValue());
						}
					} else {
						if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialAmount()) {
							species.setInitialAmount(refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialAmount());
						} else if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialConcentration()) {
							species.setInitialConcentration(refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialConcentration());
						}

					}
				}
			}
			String speciesType = typeBox.getSelectedItem().toString();
			boolean onPort = (speciesType.equals(GlobalConstants.INPUT)||speciesType.equals(GlobalConstants.OUTPUT));
			
			if (degradation != null && !specDegradable.isSelected()) {
				bioModel.removeReaction(degradation.getId());
			} else if (specDegradable.isSelected()) {
				PropertyField f = fields.get(GlobalConstants.KDECAY_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					if (f.getValue().startsWith("(")) {
						bioModel.createDegradationReaction(selected, 1.0, f.getValue(),onPort);		
					} else {
						bioModel.createDegradationReaction(selected, Double.parseDouble(f.getValue()), null,onPort);		
					}
				} else {
					bioModel.createDegradationReaction(selected, -1, null,onPort);		
				}
			} 
			if (diffusion != null && !specDiffusible.isSelected()) {
				bioModel.removeReaction(diffusion.getId());
			} else if (specDiffusible.isSelected()) {
				String kmdiffStr = null;
				PropertyField f = fields.get(GlobalConstants.MEMDIFF_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					kmdiffStr = f.getValue();
				}
				bioModel.createDiffusionReaction(selected, kmdiffStr,onPort);		
			} 
			if (constitutive != null && !specConstitutive.isSelected()) {
				bioModel.removeReaction(constitutive.getId());
			} else if (specConstitutive.isSelected()) {
				String npStr = null;
				PropertyField f = fields.get(GlobalConstants.STOICHIOMETRY_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					npStr = f.getValue();
				}
				String koStr = null;
				f = fields.get(GlobalConstants.OCR_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					koStr = f.getValue();
				}				
				bioModel.createConstitutiveReaction(selected, koStr, npStr, onPort);		
			} 
			if (complex != null) {
				String KcStr = null;
				PropertyField f = fields.get(GlobalConstants.KCOMPLEX_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					KcStr = f.getValue();
				}
				bioModel.createComplexReaction(selected, KcStr,onPort);
			}
			
			if (!paramsOnly) {
				// Add SBOL annotation to species
				if (sbolField.getSBOLURIs().size() > 0) {
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(species.getMetaId(), 
							sbolField.getSBOLURIs(), sbolField.getSBOLStrand());
					AnnotationUtility.setSBOLAnnotation(species, sbolAnnot);
				} else 
					AnnotationUtility.removeSBOLAnnotation(species);
			}
			
			if (selected != null && !selected.equals(newSpeciesID)) {
				bioModel.changeSpeciesId(selected, newSpeciesID);
				((DefaultListModel) components.getModel()).clear();

				for (int i = 0; i < bioModel.getSBMLCompModel().getListOfSubmodels().size(); i++) {
					Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(i);
					components.addItem(submodel.getId() + " " + submodel.getModelRef() + " " + bioModel.getComponentPortMap(submodel.getId()));
				}
			}
			bioModel.createDirPort(species.getId(),speciesType);

			//gcm.addSpecies(newSpeciesID, property);
			
			if (paramsOnly) {
				if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
						fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
								fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
								fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])
						|| fields.get(GlobalConstants.OCR_STRING).getState().equals(
								fields.get(GlobalConstants.OCR_STRING).getStates()[1])
						|| fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(
								fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1])
						|| fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
								fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
					newSpeciesID += " Modified";
				}
			}
			
			speciesList.removeItem(selected);
			speciesList.removeItem(selected + " Modified");
			speciesList.addItem(newSpeciesID);
			speciesList.setSelectedValue(newSpeciesID, true);
	
			modelEditor.refresh();
			modelEditor.setDirty(true);
		}

		// "Cancel"
		if(options[value].equals(options[1])) {
			return true;
		}
		return true;
	}

	public String updates() {
		
		String updates = "";
		
		if (paramsOnly) {

			if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
					fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])) {
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.INITIAL_STRING + " "
						+ fields.get(GlobalConstants.INITIAL_STRING).getValue();
			}

			if (fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
					fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KDECAY_STRING + " "
						+ fields.get(GlobalConstants.KDECAY_STRING).getValue();
			}

			if (fields.get(GlobalConstants.OCR_STRING).getState().equals(
					fields.get(GlobalConstants.OCR_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.OCR_STRING + " "
						+ fields.get(GlobalConstants.OCR_STRING).getValue();
			}

			if (fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(
					fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.STOICHIOMETRY_STRING + " "
						+ fields.get(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			
			if (fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
					fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KCOMPLEX_STRING + " "
						+ fields.get(GlobalConstants.KCOMPLEX_STRING).getValue();
			}
			
			if (fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
					fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.MEMDIFF_STRING + " "
						+ fields.get(GlobalConstants.MEMDIFF_STRING).getValue();
			}
			
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("constdiffChanged")) {
			
			//disallow constant == true if diffusible or constitutive are selected
			if (specConstitutive.isSelected() || specDiffusible.isSelected() || specDegradable.isSelected()) {
				specConstant.setEnabled(false);
				specConstant.setSelected(false);
			}
			else {
				specConstant.setEnabled(true);
			}
			setFieldEnablings();
		}

		if (paramsOnly)
			thresholdTextField.setEnabled(specInteresting.isSelected());
		

		// if the dimension type is changed
		else if (e.getSource() == dimensionType) {
			int index = dimensionType.getSelectedIndex();
			if (index == 0) {
				dimensionX.setEnabled(false);
				dimensionY.setEnabled(false);
			}
			else if (index == 1) {
				dimensionX.setEnabled(true);
				dimensionY.setEnabled(false);
			}
			else if (index == 2) {
				dimensionX.setEnabled(true);
				dimensionY.setEnabled(true);
			}
		}
	}

	/**
	 * enables/disables field based on the species type
	 * @param type
	 */
	private void setFieldEnablings() {
		fields.get(GlobalConstants.OCR_STRING).setEnabled(false);
		fields.get(GlobalConstants.STOICHIOMETRY_STRING).setEnabled(false);
		fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
		fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
		fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		typeBox.setEnabled(true);
		
		//diffusible
		if (specConstitutive.isSelected()) {
			fields.get(GlobalConstants.OCR_STRING).setEnabled(true);
			fields.get(GlobalConstants.STOICHIOMETRY_STRING).setEnabled(true);
		} 
		if (specDiffusible.isSelected()) {
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(true);
			typeBox.setEnabled(false);
		} 
		if (specDegradable.isSelected()) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
		}
		if (complex != null) {
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		}
		if (typeBox.getSelectedItem()!=GlobalConstants.INTERNAL) {
			specDiffusible.setEnabled(false);
		} else {
			specDiffusible.setEnabled(true);
		}
	}
	
	private String selected = "";

	private PropertyList speciesList = null;
	
	private PropertyList components = null;

	private String[] options = { "Ok", "Cancel" };

	private BioModel bioModel = null;
	private BioModel refGCM = null;

	private JComboBox typeBox = null;
	private JComboBox compartBox = null;
	private JComboBox convBox = null;
	private JComboBox unitsBox = null;
	private JCheckBox specBoundary, specConstant, specHasOnly = null;
	private JTextField initialField = null;
	
	private JComboBox dimensionType, dimensionX, dimensionY = null;
	private JLabel dimensionTypeLabel, dimensionSizeLabel = null;
	private JTextField iIndex, jIndex = null;
	private JTextField conviIndex, convjIndex = null;
	
	private JCheckBox specInteresting = null;
	private JCheckBox specDiffusible = null;
	private JCheckBox specConstitutive = null;
	private JCheckBox specDegradable = null;
	
	private Species species = null;
	private Reaction diffusion = null;
	private Reaction constitutive = null;
	private Reaction degradation = null;
	private Reaction complex = null;
	
	private JTextField thresholdTextField = null;
	
	private static final String[] types = new String[] { GlobalConstants.INPUT, GlobalConstants.INTERNAL, 
		GlobalConstants.OUTPUT};

	private HashMap<String, PropertyField> fields = null;
	
	private SBOLField sbolField;
	
	private boolean paramsOnly;
	
	private ModelEditor modelEditor;
}
