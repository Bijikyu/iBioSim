package synthesis.genetic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;

import sbol.util.SBOLFileManager;

import main.Log;
import main.util.Utility;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

public class SynthesisView extends JTabbedPane implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;
	private String synthID;
	private String separator;
	private String rootFilePath;
	private Log log;
	private JFrame frame;
	private Properties synthProps;
	private JTextField specText;
	private List<String> libFilePaths;
	private JList libList;
	private JScrollPane libScroll;
	private JButton addLibButton;
	private JButton removeLibButton;
//	private JButton mergeLibButton;
	private JComboBox methodBox;
	private JLabel numSolnsLabel;
	private JTextField numSolnsText;
	
	public SynthesisView(String synthID, String separator, String rootFilePath, Log log) {
		this.synthID = synthID;
		this.separator = separator;
		this.rootFilePath = rootFilePath;
		this.log = log;
		new File(rootFilePath + separator + synthID).mkdir();
		JPanel optionsPanel = constructOptionsPanel();
		addTab("Synthesis Options", optionsPanel);
		getComponentAt(getComponents().length - 1).setName("Synthesis Options");
	}
	
	private JPanel constructOptionsPanel() {
		JPanel topPanel = new JPanel();
		JPanel optionsPanel = new JPanel(new BorderLayout());
		JPanel specLibPanel = constructSpecLibPanel();
		JPanel methodPanel = constructMethodPanel();
		topPanel.add(optionsPanel);
		optionsPanel.add(specLibPanel, BorderLayout.NORTH);
		optionsPanel.add(methodPanel, BorderLayout.CENTER);
		return topPanel;
	}
	
	private JPanel constructSpecLibPanel() {
		JPanel specLibPanel = new JPanel();
		JLabel libLabel = new JLabel("Library Files: ");
		JPanel inputPanel = constructSpecLibInputPanel();
		specLibPanel.add(libLabel);
		specLibPanel.add(inputPanel);
		return specLibPanel;
	}
	
	private JPanel constructSpecLibInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout());
		JPanel specPanel = constructSpecPanel();
		libScroll = new JScrollPane();
		libScroll.setPreferredSize(new Dimension(276, 55));
		JPanel buttonPanel = constructLibButtonPanel();
		inputPanel.add(specPanel, BorderLayout.NORTH);
		inputPanel.add(libScroll, BorderLayout.WEST);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		return inputPanel;
	}
	
	private JPanel constructSpecPanel() {
		JPanel specPanel = new JPanel();
		JLabel specLabel = new JLabel("Specification File:");
		specText = new JTextField();
		specText.setEditable(false);
		specPanel.add(specLabel);
		specPanel.add(specText);
		return specPanel;
	}
	
	private JPanel constructLibButtonPanel() {
		JPanel buttonPanel = new JPanel();
		addLibButton = new JButton("Add");
		addLibButton.addActionListener(this);
		removeLibButton = new JButton("Remove");
		removeLibButton.addActionListener(this);
//		mergeLibButton = new JButton("Merge");
//		mergeLibButton.addActionListener(this);
		buttonPanel.add(addLibButton);
		buttonPanel.add(removeLibButton);
//		buttonPanel.add(mergeLibButton);
		return buttonPanel;
	}
	
	private JPanel constructMethodPanel() {
		JPanel topPanel = new JPanel();
		JPanel methodPanel = new JPanel(new BorderLayout());
		JPanel labelPanel = constructMethodLabelPanel();
		JPanel inputPanel = constructMethodInputPanel();
		topPanel.add(methodPanel);
		methodPanel.add(labelPanel, BorderLayout.WEST);
		methodPanel.add(inputPanel, BorderLayout.CENTER);
		return topPanel;
	}
	
	private JPanel constructMethodLabelPanel() {
		JPanel labelPanel = new JPanel(new GridLayout(2, 1));
		labelPanel.add(new JLabel("Synthesis Method:  "));
		numSolnsLabel = new JLabel("Number of Solutions:  ");
		labelPanel.add(numSolnsLabel);
		return labelPanel;
	}
	
	private JPanel constructMethodInputPanel() {
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		methodBox = new JComboBox(GlobalConstants.SBOL_SYNTH_STRUCTURAL_METHODS.split(","));
		methodBox.addActionListener(this);
		numSolnsText = new JTextField(39);
		numSolnsText.addActionListener(this);
		inputPanel.add(methodBox);
		inputPanel.add(numSolnsText);
		return inputPanel;
	}
	
	public void loadDefaultSynthesisProperties(String specFileID) {
		synthProps = createDefaultSynthesisProperties(specFileID);
		saveSynthesisProperties();
		loadSynthesisOptions();
	}
	
	private static Properties createDefaultSynthesisProperties(String specFileID) {
		Properties synthProps = new Properties();
		Preferences prefs = Preferences.userRoot();
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_LIBS_PREFERENCE, ""));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY,
				prefs.get(GlobalConstants.SBOL_SYNTH_METHOD_PREFERENCE, 
						GlobalConstants.SBOL_SYNTH_EXHAUST_BB));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PREFERENCE, "1"));
		return synthProps;
	}
	
	private void saveSynthesisProperties() {
		String propFilePath = rootFilePath + separator + synthID + separator + synthID 
				+ GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION;
		log.addText("Creating properties file:\n" + propFilePath + "\n");
		try {
			FileOutputStream propStreamOut = new FileOutputStream(new File(propFilePath));
			synthProps.store(propStreamOut, synthID + " SBOL Synthesis Properties");
			propStreamOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadSynthesisProperties(Properties synthProps) {
		this.synthProps = synthProps;
		loadSynthesisOptions();
	}
	
	private void loadSynthesisOptions() {
		specText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		libFilePaths = new LinkedList<String>();
		for (String libFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
			if (libFilePath.length() > 0)
				libFilePaths.add(libFilePath);
		libList = new JList();
		updateLibraryFiles();
		libScroll.setViewportView(libList);
		methodBox.setSelectedItem(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY));
		numSolnsText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addLibButton)
			addLibraryFile(libList.getSelectedIndex());
		else if (e.getSource() == removeLibButton)
			removeLibraryFiles(libList.getSelectedIndices());
//		else if (e.getSource() == mergeLibButton)
//			mergeLibraryFiles(libList.getSelectedIndices());
		else if (e.getSource() == methodBox)
			toggleMethodSettings();
	}
	
	private void toggleMethodSettings() {
		if (methodBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_EXHAUST_BB)) 
			numSolnsText.setText("1");
	}
	
	private void addLibraryFile(int addIndex) {
		File startDirectory = new File(Preferences.userRoot().get("biosim.general.project_dir", ""));
		String libFilePath = Utility.browse(frame, startDirectory, null, 
				JFileChooser.DIRECTORIES_ONLY, "Open", -1);
		if (libFilePath.length() > 0 && !libFilePaths.contains(libFilePath)) {
			if (addIndex >= 0)
				libFilePaths.add(addIndex, libFilePath);
			else
				libFilePaths.add(libFilePath);
			updateLibraryFiles();
		}
	}
	
	private void removeLibraryFiles(int[] removeIndices) {
		if (removeIndices.length > 0)
			for (int i = removeIndices.length - 1; i >=0; i--)
				libFilePaths.remove(removeIndices[i]);
		else
			libFilePaths.remove(libFilePaths.size() - 1);
		updateLibraryFiles();
	}
	
//	private void mergeLibraryFiles(int[] mergeIndices) {
//		for (int mergeIndex : mergeIndices) 
//			for (String fileID : new File(libFilePaths.get(mergeIndex)).list()) {
//				if (fileID.endsWith(GlobalConstants.SBOL_FILE_EXTENSION)) {
//					SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(libFilePaths.get(mergeIndex) + separator + fileID);
//					SBOLDocument flatSBOLDoc = SBOLUtility.flattenSBOLDocument(sbolDoc);
//					
//				}
//			}
//	}
	
	private void updateLibraryFiles() {
		String[] libListData = new String[libFilePaths.size()];
		for (int i = 0; i < libListData.length; i++) {
			String[] splitPath = libFilePaths.get(i).split(separator);
			libListData[i] = splitPath[splitPath.length - 1];
		}
		libList.setListData(libListData);
	}
	
	@Override
	public void run() { 
		
	}
	
	public List<String> run(String synthFilePath) {
		Set<String> sbolFilePaths = new HashSet<String>();
		for (String libFilePath : libFilePaths) 
			for (String fileID : new File(libFilePath).list())
				if (fileID.endsWith(".sbol"))
					sbolFilePaths.add(libFilePath + separator + fileID);
		SBOLFileManager fileManager = new SBOLFileManager(sbolFilePaths);
		
		Set<SynthesisGraph> graphlibrary = new HashSet<SynthesisGraph>();
//		boolean flatImport = libFilePaths.size() > 1;
		for (String libFilePath : libFilePaths) 
			for (String gateFileID : new File(libFilePath).list()) 
				if (gateFileID.endsWith(".xml")) {
					BioModel gateModel = new BioModel(libFilePath);
					gateModel.load(gateFileID);
					graphlibrary.add(new SynthesisGraph(gateModel, fileManager));
				}
		
		BioModel specModel = new BioModel(rootFilePath);
		specModel.load(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		SynthesisGraph spec = new SynthesisGraph(specModel, fileManager);
		
		Synthesizer synthesizer = new Synthesizer(graphlibrary, synthProps);
		List<List<SynthesisGraph>> solutions = synthesizer.mapSpecification(spec);
		List<String> solutionFileIDs = importSolutions(solutions, spec, fileManager, synthFilePath);
		return solutionFileIDs;
	}
	
	private List<String> importSolutions(List<List<SynthesisGraph>> solutions, SynthesisGraph spec, 
			SBOLFileManager fileManager, String synthFilePath) {
		List<BioModel> solutionModels = new LinkedList<BioModel>();
		Set<String> solutionFileIDs = new HashSet<String>();
		for (List<SynthesisGraph> solutionGraphs : solutions) {
			solutionFileIDs.addAll(importSolutionSubModels(solutionGraphs, synthFilePath));
			solutionFileIDs.add(importSolutionDNAComponents(solutionGraphs, fileManager, synthFilePath));
		}
		int idIndex = 0;
		for (List<SynthesisGraph> solutionGraphs : solutions) {
			BioModel solutionModel = new BioModel(synthFilePath);
			solutionModel.createSBMLDocument("tempID_" + idIndex, false, false);	
			idIndex++;
			Synthesizer.composeSolutionModel(solutionGraphs, spec, solutionModel);
			solutionModels.add(solutionModel);
		}
		List<String> orderedSolnFileIDs = new LinkedList<String>();
		idIndex = 0;
		for (BioModel solutionModel : solutionModels) {
			String solutionID = spec.getModelFileID().replace(".xml", "");
			do {
				idIndex++;
			} while (solutionFileIDs.contains(solutionID + "_" + idIndex + ".xml"));
			solutionModel.setSBMLFile(solutionID + "_" + idIndex + ".xml");
			solutionModel.getSBMLDocument().getModel().setId(solutionID + "_" + idIndex);
			solutionModel.save(synthFilePath + separator + solutionID + "_" + idIndex + ".xml");
			orderedSolnFileIDs.add(solutionID + "_" + idIndex + ".xml");
		}
		orderedSolnFileIDs.addAll(solutionFileIDs);
		return orderedSolnFileIDs;
	}
	
	private Set<String> importSolutionSubModels(List<SynthesisGraph> solutionGraphs, 
			String synthFilePath) {
		HashMap<String, SynthesisGraph> solutionFileToGraph = new HashMap<String, SynthesisGraph>();
		Set<String> clashingFileIDs = new HashSet<String>();
		for (SynthesisGraph solutionGraph : solutionGraphs) {
			BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());
			solutionSubModel.load(solutionGraph.getModelFileID());
			if (solutionFileToGraph.containsKey(solutionGraph.getModelFileID())) {
				SynthesisGraph clashingGraph = solutionFileToGraph.get(solutionGraph.getModelFileID());
				BioModel clashingSubModel = new BioModel(clashingGraph.getProjectPath());
				clashingSubModel.load(clashingGraph.getModelFileID());
				if (!compareModels(solutionSubModel, clashingSubModel)) {
					clashingFileIDs.add(solutionGraph.getModelFileID());
					solutionFileToGraph.remove(solutionGraph.getModelFileID());
					solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
							solutionGraph);
					solutionFileToGraph.put(flattenProjectIntoModelFileID(clashingSubModel, solutionFileToGraph.keySet()), 
							clashingGraph);
				}
			} else if (clashingFileIDs.contains(solutionGraph.getModelFileID())) 
				solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
						solutionGraph);
			else
				solutionFileToGraph.put(solutionGraph.getModelFileID(), solutionGraph);
		}
		for (String subModelFileID : solutionFileToGraph.keySet()) {
			SynthesisGraph solutionGraph = solutionFileToGraph.get(subModelFileID);
			BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());
			solutionSubModel.load(solutionGraph.getModelFileID());
			solutionSubModel.getSBMLDocument().getModel().setId(subModelFileID.replace(".xml", ""));
			solutionSubModel.save(synthFilePath + separator + subModelFileID);
			solutionGraph.setModelFileID(subModelFileID);
		}
		return solutionFileToGraph.keySet();
	}
	
	private String flattenProjectIntoModelFileID(BioModel biomodel, Set<String> modelFileIDs) {
		String[] splitPath = biomodel.getPath().split(separator);
		String flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
		int fileIndex = 0;
		while (modelFileIDs.contains(flatModelFileID)) {
			fileIndex++;
			flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile().replace(".xml", "") 
					+ "_" + fileIndex + ".xml";
		}
		return splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
	}
	
	private static boolean compareModels(BioModel subModel1, BioModel subModel2) {
		SBMLWriter writer = new SBMLWriter();
		String hash1 = null;
		String hash2 = null;
		try {
			String xml1 = writer.writeSBMLToString(subModel1.getSBMLDocument());
			hash1 = biomodel.util.Utility.MD5(xml1);
			String xml2 = writer.writeSBMLToString(subModel2.getSBMLDocument());
			hash2 = biomodel.util.Utility.MD5(xml2);
		}
		catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash1 == hash2;
	}
	
	private String importSolutionDNAComponents(List<SynthesisGraph> solutionGraphs, SBOLFileManager fileManager, 
			String synthFilePath) {
		Set<URI> compURIs = new HashSet<URI>();
		for (SynthesisGraph solutionGraph : solutionGraphs) {
			for (URI compURI : solutionGraph.getCompURIs())
				if (!compURIs.contains(compURI))
					compURIs.add(compURI);
		}
		String sbolFileID = getSpecFileID().replace(".xml", GlobalConstants.SBOL_FILE_EXTENSION);
		SBOLFileManager.saveDNAComponents(fileManager.resolveURIs(new LinkedList<URI>(compURIs)), 
				synthFilePath + separator + sbolFileID);
		return sbolFileID;
	}
	
	public void save() {
		Set<Integer> saveIndices = new HashSet<Integer>();
		for (int i = 0; i < getTabCount(); i++)
			saveIndices.add(i);
		saveTabs(saveIndices);
	}
	
	public void saveTabs(Set<Integer> tabIndices) {
		if (tabIndices.size() > 0) {
			updateSynthesisProperties(tabIndices);
			saveSynthesisProperties();
		}
	}
	
	public void changeSpecFile(String specFileID) {
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		saveSynthesisProperties();
	}
	
	private void updateSynthesisProperties(Set<Integer> tabIndices) {
		for (int tabIndex : tabIndices)
			if (tabIndex == 0) {
				String libFileProp = "";
				for (String libFilePath : libFilePaths)
					libFileProp = libFileProp + libFilePath + ",";
				if (libFileProp.length() > 0)
					libFileProp = libFileProp.substring(0, libFileProp.length() - 1);
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
						libFileProp);
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY, 
						methodBox.getSelectedItem().toString());
				if (Integer.parseInt(numSolnsText.getText()) <= 0)
					numSolnsText.setText("0");
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, numSolnsText.getText());
			}
	}
	
	public boolean tabChanged(int tabIndex) {
		if (tabIndex == 0) {
			return (libsChanged() || methodChanged() || numSolnsChanged());
		}
		return false;
	}
	
	private boolean libsChanged() {
		List<String> prevLibFilePaths = new LinkedList<String>();
		for (String prevLibFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
				if (prevLibFilePath.length() > 0)
					prevLibFilePaths.add(prevLibFilePath);
		if (prevLibFilePaths.size() != libFilePaths.size())
			return true;
		for (String prevLibFilePath : prevLibFilePaths)
			if (!libFilePaths.contains(prevLibFilePath))
				return true;
		return false;
	}
	
	private boolean methodChanged() {
		String currentMethod = methodBox.getSelectedItem().toString();
		String previousMethod = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY);
		return !currentMethod.equals(previousMethod);
	}
	
	private boolean numSolnsChanged() {
		int currentNumSolns = Integer.parseInt(numSolnsText.getText());
		int previousNumSolns = Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
		return currentNumSolns != previousNumSolns;
	}
	
	public String getSpecFileID() {
		return specText.getText();
	}
	
	public String getRootDirectory() {
		return rootFilePath;
	}
	
	public void renameView(String synthID) {
		this.synthID = synthID;
	}

}
