package graph;

import gcm2sbml.parser.GCMFile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import lhpn2sbml.parser.LhpnFile;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jibble.epsgraphics.EpsGraphics2D;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.w3c.dom.DOMImplementation;

import parser.TSDParser;
import reb2sac.Reb2Sac;
import biomodelsim.BioSim;
import biomodelsim.Log;
import buttons.Buttons;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This is the Graph class. It takes in data and draws a graph of that data. The
 * Graph class implements the ActionListener class, the ChartProgressListener
 * class, and the MouseListener class. This allows the Graph class to perform
 * actions when buttons are pressed, when the chart is drawn, or when the chart
 * is clicked.
 * 
 * @author Curtis Madsen
 */
public class Graph extends JPanel implements ActionListener, MouseListener, ChartProgressListener {

	private static final long serialVersionUID = 4350596002373546900L;

	private JFreeChart chart; // Graph of the output data

	private XYSeriesCollection curData; // Data in the current graph

	private String outDir; // output directory

	private String printer_id; // printer id

	/*
	 * Text fields used to change the graph window
	 */
	private JTextField XMin, XMax, XScale, YMin, YMax, YScale;

	private ArrayList<String> graphSpecies; // names of species in the graph

	private BioSim biomodelsim; // tstubd gui

	private JButton save, run, saveAs;

	private JButton export, refresh; // buttons

	// private JButton exportJPeg, exportPng, exportPdf, exportEps, exportSvg,
	// exportCsv; // buttons

	private HashMap<String, Paint> colors;

	private HashMap<String, Shape> shapes;

	private String selected, lastSelected;

	private LinkedList<GraphSpecies> graphed;

	private LinkedList<GraphProbs> probGraphed;

	private JCheckBox resize;

	private JComboBox XVariable;

	private JCheckBox LogX, LogY;

	private Log log;

	private ArrayList<JCheckBox> boxes;

	private ArrayList<JTextField> series;

	private ArrayList<JComboBox> colorsCombo;

	private ArrayList<JComboBox> shapesCombo;

	private ArrayList<JCheckBox> connected;

	private ArrayList<JCheckBox> visible;

	private ArrayList<JCheckBox> filled;

	private JCheckBox use;

	private JCheckBox connectedLabel;

	private JCheckBox visibleLabel;

	private JCheckBox filledLabel;

	private String graphName;

	private String separator;

	private boolean change;

	private boolean timeSeries;

	private boolean topLevel;

	private ArrayList<String> graphProbs;

	private JTree tree;

	private IconNode node, simDir;

	private Reb2Sac reb2sac; // reb2sac options

	private ArrayList<String> learnSpecs;

	private boolean warn;

	private ArrayList<String> averageOrder;

	private JPopupMenu popup; // popup menu

	private ArrayList<String> directories;

	private JPanel specPanel;

	private JScrollPane scrollpane;

	private JPanel all;

	private JPanel titlePanel;

	private JScrollPane scroll;

	private boolean updateXNumber;

	/**
	 * Creates a Graph Object from the data given and calls the private graph
	 * helper method.
	 */
	public Graph(Reb2Sac reb2sac, String printer_track_quantity, String label, String printer_id,
			String outDir, String time, BioSim biomodelsim, String open, Log log, String graphName,
			boolean timeSeries, boolean learnGraph) {
		this.reb2sac = reb2sac;
		averageOrder = null;
		popup = new JPopupMenu();
		warn = false;
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// initializes member variables
		this.log = log;
		this.timeSeries = timeSeries;
		if (graphName != null) {
			this.graphName = graphName;
			topLevel = true;
		}
		else {
			if (timeSeries) {
				this.graphName = outDir.split(separator)[outDir.split(separator).length - 1]
						+ ".grf";
			}
			else {
				this.graphName = outDir.split(separator)[outDir.split(separator).length - 1]
						+ ".prb";
			}
			topLevel = false;
		}
		this.outDir = outDir;
		this.printer_id = printer_id;
		this.biomodelsim = biomodelsim;
		XYSeriesCollection data = new XYSeriesCollection();
		if (learnGraph) {
			updateSpecies();
		}
		else {
			learnSpecs = null;
		}

		// graph the output data
		if (timeSeries) {
			setUpShapesAndColors();
			graphed = new LinkedList<GraphSpecies>();
			selected = "";
			lastSelected = "";
			graph(printer_track_quantity, label, data, time);
			if (open != null) {
				open(open);
			}
		}
		else {
			setUpShapesAndColors();
			probGraphed = new LinkedList<GraphProbs>();
			selected = "";
			lastSelected = "";
			probGraph(label);
			if (open != null) {
				open(open);
			}
		}
	}

	/**
	 * This private helper method calls the private readData method, sets up a
	 * graph frame, and graphs the data.
	 * 
	 * @param dataset
	 * @param time
	 */
	private void graph(String printer_track_quantity, String label, XYSeriesCollection dataset,
			String time) {
		chart = ChartFactory.createXYLineChart(label, time, printer_track_quantity, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(java.awt.Color.WHITE);
		chart.addProgressListener(this);
		ChartPanel graph = new ChartPanel(chart);
		graph.setLayout(new GridLayout(1, 1));
		JLabel edit = new JLabel("Double click here to create graph");
		edit.addMouseListener(this);
		Font font = edit.getFont();
		font = font.deriveFont(Font.BOLD, 42.0f);
		edit.setFont(font);
		edit.setHorizontalAlignment(SwingConstants.CENTER);
		graph.add(edit);
		graph.addMouseListener(this);
		change = false;

		// creates text fields for changing the graph's dimensions
		resize = new JCheckBox("Auto Resize");
		resize.setSelected(true);
		XVariable = new JComboBox();
		updateXNumber = false;
		XVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (updateXNumber && node != null) {
					String curDir = "";
					if (node.getParent().getParent() != null
							&& directories.contains(((IconNode) node.getParent().getParent())
									.getName()
									+ separator + ((IconNode) node.getParent()).getName())) {
						curDir = ((IconNode) node.getParent().getParent()).getName() + separator
								+ ((IconNode) node.getParent()).getName();
					}
					else if (directories.contains(((IconNode) node.getParent()).getName())) {
						curDir = ((IconNode) node.getParent()).getName();
					}
					else {
						curDir = "";
					}
					for (int i = 0; i < graphed.size(); i++) {
						if (graphed.get(i).getDirectory().equals(curDir)) {
							graphed.get(i).setXNumber(XVariable.getSelectedIndex());
						}
					}
				}
			}
		});
		LogX = new JCheckBox("LogX");
		LogX.setSelected(false);
		LogY = new JCheckBox("LogY");
		LogY.setSelected(false);
		XMin = new JTextField();
		XMax = new JTextField();
		XScale = new JTextField();
		YMin = new JTextField();
		YMax = new JTextField();
		YScale = new JTextField();

		// creates the buttons for the graph frame
		JPanel ButtonHolder = new JPanel();
		run = new JButton("Save and Run");
		save = new JButton("Save Graph");
		saveAs = new JButton("Save As");
		saveAs.addActionListener(this);
		export = new JButton("Export");
		refresh = new JButton("Refresh");
		// exportJPeg = new JButton("Export As JPEG");
		// exportPng = new JButton("Export As PNG");
		// exportPdf = new JButton("Export As PDF");
		// exportEps = new JButton("Export As EPS");
		// exportSvg = new JButton("Export As SVG");
		// exportCsv = new JButton("Export As CSV");
		run.addActionListener(this);
		save.addActionListener(this);
		export.addActionListener(this);
		refresh.addActionListener(this);
		// exportJPeg.addActionListener(this);
		// exportPng.addActionListener(this);
		// exportPdf.addActionListener(this);
		// exportEps.addActionListener(this);
		// exportSvg.addActionListener(this);
		// exportCsv.addActionListener(this);
		if (reb2sac != null) {
			ButtonHolder.add(run);
		}
		ButtonHolder.add(save);
		ButtonHolder.add(saveAs);
		ButtonHolder.add(export);
		ButtonHolder.add(refresh);
		// ButtonHolder.add(exportJPeg);
		// ButtonHolder.add(exportPng);
		// ButtonHolder.add(exportPdf);
		// ButtonHolder.add(exportEps);
		// ButtonHolder.add(exportSvg);
		// ButtonHolder.add(exportCsv);

		// puts all the components of the graph gui into a display panel
		// JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// ButtonHolder, null);
		// splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		// this.add(splitPane, "South");

		// determines maximum and minimum values and resizes
		resize(dataset);
		this.revalidate();
	}

	private void readGraphSpecies(String file) {
		biomodelsim.frame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		graphSpecies = new TSDParser(file, biomodelsim, true).getSpecies();
		biomodelsim.frame().setCursor(null);
		if (learnSpecs != null) {
			for (String spec : learnSpecs) {
				if (!graphSpecies.contains(spec)) {
					graphSpecies.add(spec);
				}
			}
			for (int i = 1; i < graphSpecies.size(); i++) {
				if (!learnSpecs.contains(graphSpecies.get(i))) {
					graphSpecies.remove(i);
					i--;
				}
			}
		}
	}

	/**
	 * This public helper method parses the output file of ODE, monte carlo, and
	 * markov abstractions.
	 */
	public ArrayList<ArrayList<Double>> readData(String file, String label, String directory,
			boolean warning) {
		warn = warning;
		String[] s = file.split(separator);
		String getLast = s[s.length - 1];
		String stem = "";
		int t = 0;
		try {
			while (!Character.isDigit(getLast.charAt(t))) {
				stem += getLast.charAt(t);
				t++;
			}
		}
		catch (Exception e) {
		}
		if ((label.contains("average") && file.contains("mean"))
				|| (label.contains("variance") && file.contains("variance"))
				|| (label.contains("deviation") && file.contains("standard_deviation"))) {
			biomodelsim.frame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			TSDParser p = new TSDParser(file, biomodelsim, warn);
			biomodelsim.frame().setCursor(null);
			warn = p.getWarning();
			graphSpecies = p.getSpecies();
			ArrayList<ArrayList<Double>> data = p.getData();
			if (learnSpecs != null) {
				for (String spec : learnSpecs) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!learnSpecs.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			else if (averageOrder != null) {
				for (String spec : averageOrder) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!averageOrder.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			return data;
		}
		if (label.contains("average") || label.contains("variance") || label.contains("deviation")) {
			int counter = 1;
			while (!(new File(file).exists())) {
				file = file.substring(0, file.length() - getLast.length());
				file += stem;
				file += counter + "";
				file += "." + printer_id.substring(0, printer_id.length() - 8);
				counter++;
			}
		}
		if (label.contains("average")) {
			return calculateAverageVarianceDeviation(file, stem, 0, directory, warn);
		}
		else if (label.contains("variance")) {
			return calculateAverageVarianceDeviation(file, stem, 1, directory, warn);
		}
		else if (label.contains("deviation")) {
			return calculateAverageVarianceDeviation(file, stem, 2, directory, warn);
		}
		else {
			biomodelsim.frame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			TSDParser p = new TSDParser(file, biomodelsim, warn);
			biomodelsim.frame().setCursor(null);
			warn = p.getWarning();
			graphSpecies = p.getSpecies();
			ArrayList<ArrayList<Double>> data = p.getData();
			if (learnSpecs != null) {
				for (String spec : learnSpecs) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!learnSpecs.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			else if (averageOrder != null) {
				for (String spec : averageOrder) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!averageOrder.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			return data;
		}
	}

	/**
	 * This method adds and removes plots from the graph depending on what check
	 * boxes are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		// if the save button is clicked
		if (e.getSource() == run) {
			reb2sac.getRunButton().doClick();
		}
		if (e.getSource() == save) {
			save();
		}
		// if the save as button is clicked
		if (e.getSource() == saveAs) {
			saveAs();
		}
		// if the export button is clicked
		else if (e.getSource() == export) {
			export();
		}
		else if (e.getSource() == refresh) {
			refresh();
		}
		else if (e.getActionCommand().equals("rename")) {
			String rename = JOptionPane.showInputDialog(biomodelsim.frame(),
					"Enter A New Filename:", "Rename", JOptionPane.PLAIN_MESSAGE);
			if (rename != null) {
				rename = rename.trim();
			}
			else {
				return;
			}
			if (!rename.equals("")) {
				boolean write = true;
				if (rename.equals(node.getName())) {
					write = false;
				}
				else if (new File(outDir + separator + rename).exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + "\nDo you want to overwrite?", "Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						File dir = new File(outDir + separator + rename);
						if (dir.isDirectory()) {
							biomodelsim.deleteDir(dir);
						}
						else {
							dir.delete();
						}
						for (int i = 0; i < simDir.getChildCount(); i++) {
							if (((IconNode) simDir.getChildAt(i)).getChildCount() > 0
									&& ((IconNode) simDir.getChildAt(i)).getName().equals(rename)) {
								simDir.remove(i);
							}
						}
						boolean checked = false;
						for (int i = 0; i < simDir.getChildCount(); i++) {
							if (((IconNode) simDir.getChildAt(i)).getIconName().equals(
									"" + (char) 10003)) {
								checked = true;
							}
						}
						if (!checked) {
							simDir.setIcon(MetalIconFactory.getTreeFolderIcon());
							simDir.setIconName("");
						}
						for (int i = 0; i < graphed.size(); i++) {
							if (graphed.get(i).getDirectory().equals(rename)) {
								graphed.remove(i);
								i--;
							}
						}
					}
					else {
						write = false;
					}
				}
				if (write) {
					String getFile = node.getName();
					IconNode s = node;
					while (s.getParent().getParent() != null) {
						getFile = s.getName() + separator + getFile;
						s = (IconNode) s.getParent();
					}
					getFile = outDir + separator + getFile;
					new File(getFile).renameTo(new File(outDir + separator + rename));
					for (GraphSpecies spec : graphed) {
						if (spec.getDirectory().equals(node.getName())) {
							spec.setDirectory(rename);
						}
					}
					directories.remove(node.getName());
					directories.add(rename);
					node.setUserObject(rename);
					node.setName(rename);
					simDir.remove(node);
					int i;
					for (i = 0; i < simDir.getChildCount(); i++) {
						if (((IconNode) simDir.getChildAt(i)).getChildCount() != 0) {
							if (((IconNode) simDir.getChildAt(i)).getName().compareToIgnoreCase(
									rename) > 0) {
								simDir.insert(node, i);
								break;
							}
						}
						else {
							break;
						}
					}
					simDir.insert(node, i);
					ArrayList<String> rows = new ArrayList<String>();
					for (i = 0; i < tree.getRowCount(); i++) {
						if (tree.isExpanded(i)) {
							tree.setSelectionRow(i);
							rows.add(node.getName());
						}
					}
					scrollpane = new JScrollPane();
					refreshTree();
					scrollpane.getViewport().add(tree);
					scrollpane.setPreferredSize(new Dimension(175, 100));
					all.removeAll();
					all.add(titlePanel, "North");
					all.add(scroll, "Center");
					all.add(scrollpane, "West");
					all.revalidate();
					all.repaint();
					TreeSelectionListener t = new TreeSelectionListener() {
						public void valueChanged(TreeSelectionEvent e) {
							node = (IconNode) e.getPath().getLastPathComponent();
						}
					};
					tree.addTreeSelectionListener(t);
					int select = 0;
					for (i = 0; i < tree.getRowCount(); i++) {
						tree.setSelectionRow(i);
						if (rows.contains(node.getName())) {
							tree.expandRow(i);
						}
						if (rename.equals(node.getName())) {
							select = i;
						}
					}
					tree.removeTreeSelectionListener(t);
					addTreeListener();
					tree.setSelectionRow(select);
				}
			}
		}
		else if (e.getActionCommand().equals("delete")) {
			TreePath[] selected = tree.getSelectionPaths();
			TreeSelectionListener t = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					node = (IconNode) e.getPath().getLastPathComponent();
				}
			};
			for (TreeSelectionListener listen : tree.getTreeSelectionListeners()) {
				tree.removeTreeSelectionListener(listen);
			}
			tree.addTreeSelectionListener(t);
			for (TreePath select : selected) {
				tree.setSelectionPath(select);
				if (!((IconNode) select.getLastPathComponent()).getName().equals("Average")
						&& !((IconNode) select.getLastPathComponent()).getName().equals("Variance")
						&& !((IconNode) select.getLastPathComponent()).getName().equals(
								"Standard Deviation")
						&& ((IconNode) select.getLastPathComponent()).getParent() != null) {
					for (int i = 0; i < simDir.getChildCount(); i++) {
						if (((IconNode) simDir.getChildAt(i)) == ((IconNode) select
								.getLastPathComponent())) {
							if (((IconNode) simDir.getChildAt(i)).getChildCount() != 0) {
								simDir.remove(i);
								File dir = new File(outDir + separator
										+ ((IconNode) select.getLastPathComponent()).getName());
								if (dir.isDirectory()) {
									biomodelsim.deleteDir(dir);
								}
								else {
									dir.delete();
								}
								directories.remove(((IconNode) select.getLastPathComponent())
										.getName());
								for (int j = 0; j < graphed.size(); j++) {
									if (graphed.get(j).getDirectory().equals(
											((IconNode) select.getLastPathComponent()).getName())) {
										graphed.remove(j);
										j--;
									}
								}
							}
							else {
								simDir.remove(i);
								File dir = new File(outDir + separator
										+ ((IconNode) select.getLastPathComponent()).getName()
										+ "." + printer_id.substring(0, printer_id.length() - 8));
								if (dir.isDirectory()) {
									biomodelsim.deleteDir(dir);
								}
								else {
									dir.delete();
								}
								int count = 0;
								for (int j = 0; j < simDir.getChildCount(); j++) {
									if (((IconNode) simDir.getChildAt(j)).getChildCount() == 0) {
										count++;
									}
								}
								if (count == 3) {
									for (int j = 0; j < simDir.getChildCount(); j++) {
										if (((IconNode) simDir.getChildAt(j)).getChildCount() == 0) {
											simDir.remove(j);
											j--;
										}
									}
								}
							}
						}
						else if (((IconNode) simDir.getChildAt(i)).getChildCount() != 0) {
							for (int j = 0; j < simDir.getChildAt(i).getChildCount(); j++) {
								if (((IconNode) ((IconNode) simDir.getChildAt(i)).getChildAt(j)) == ((IconNode) select
										.getLastPathComponent())) {
									((IconNode) simDir.getChildAt(i)).remove(j);
									File dir = new File(outDir + separator
											+ ((IconNode) simDir.getChildAt(i)).getName()
											+ separator
											+ ((IconNode) select.getLastPathComponent()).getName()
											+ "."
											+ printer_id.substring(0, printer_id.length() - 8));
									if (dir.isDirectory()) {
										biomodelsim.deleteDir(dir);
									}
									else {
										dir.delete();
									}
									boolean checked = false;
									for (int k = 0; k < simDir.getChildAt(i).getChildCount(); k++) {
										if (((IconNode) simDir.getChildAt(i).getChildAt(k))
												.getIconName().equals("" + (char) 10003)) {
											checked = true;
										}
									}
									if (!checked) {
										((IconNode) simDir.getChildAt(i)).setIcon(MetalIconFactory
												.getTreeFolderIcon());
										((IconNode) simDir.getChildAt(i)).setIconName("");
									}
									int count = 0;
									for (int k = 0; k < simDir.getChildAt(i).getChildCount(); k++) {
										if (((IconNode) simDir.getChildAt(i).getChildAt(k))
												.getChildCount() == 0) {
											count++;
										}
									}
									if (count == 3) {
										File dir2 = new File(outDir + separator
												+ ((IconNode) simDir.getChildAt(i)).getName());
										if (dir2.isDirectory()) {
											biomodelsim.deleteDir(dir2);
										}
										else {
											dir2.delete();
										}
										directories.remove(((IconNode) simDir.getChildAt(i))
												.getName());
										for (int k = 0; k < graphed.size(); k++) {
											if (graphed.get(k).getDirectory().equals(
													((IconNode) simDir.getChildAt(i)).getName())) {
												graphed.remove(k);
												k--;
											}
										}
										simDir.remove(i);
									}
								}
							}
						}
					}
				}
			}
			boolean checked = false;
			for (int i = 0; i < simDir.getChildCount(); i++) {
				if (((IconNode) simDir.getChildAt(i)).getIconName().equals("" + (char) 10003)) {
					checked = true;
				}
			}
			if (!checked) {
				simDir.setIcon(MetalIconFactory.getTreeFolderIcon());
				simDir.setIconName("");
			}
			ArrayList<String> rows = new ArrayList<String>();
			for (int i = 0; i < tree.getRowCount(); i++) {
				if (tree.isExpanded(i)) {
					tree.setSelectionRow(i);
					rows.add(node.getName());
				}
			}
			scrollpane = new JScrollPane();
			refreshTree();
			scrollpane.getViewport().add(tree);
			scrollpane.setPreferredSize(new Dimension(175, 100));
			all.removeAll();
			all.add(titlePanel, "North");
			all.add(scroll, "Center");
			all.add(scrollpane, "West");
			all.revalidate();
			all.repaint();
			t = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					node = (IconNode) e.getPath().getLastPathComponent();
				}
			};
			tree.addTreeSelectionListener(t);
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.setSelectionRow(i);
				if (rows.contains(node.getName())) {
					tree.expandRow(i);
				}
			}
			tree.removeTreeSelectionListener(t);
			addTreeListener();
		}
		else if (e.getActionCommand().equals("delete runs")) {
			for (int i = simDir.getChildCount() - 1; i >= 0; i--) {
				if (((IconNode) simDir.getChildAt(i)).getChildCount() == 0) {
					if (!((IconNode) simDir.getChildAt(i)).getName().equals("Average")
							&& !((IconNode) simDir.getChildAt(i)).getName().equals("Variance")
							&& !((IconNode) simDir.getChildAt(i)).getName().equals(
									"Standard Deviation")) {
						File dir = new File(outDir + separator
								+ ((IconNode) simDir.getChildAt(i)).getName() + "."
								+ printer_id.substring(0, printer_id.length() - 8));
						if (dir.isDirectory()) {
							biomodelsim.deleteDir(dir);
						}
						else {
							dir.delete();
						}
					}
					simDir.remove(i);
				}
			}
			boolean checked = false;
			for (int i = 0; i < simDir.getChildCount(); i++) {
				if (((IconNode) simDir.getChildAt(i)).getIconName().equals("" + (char) 10003)) {
					checked = true;
				}
			}
			if (!checked) {
				simDir.setIcon(MetalIconFactory.getTreeFolderIcon());
				simDir.setIconName("");
			}
			ArrayList<String> rows = new ArrayList<String>();
			for (int i = 0; i < tree.getRowCount(); i++) {
				if (tree.isExpanded(i)) {
					tree.setSelectionRow(i);
					rows.add(node.getName());
				}
			}
			scrollpane = new JScrollPane();
			refreshTree();
			scrollpane.getViewport().add(tree);
			scrollpane.setPreferredSize(new Dimension(175, 100));
			all.removeAll();
			all.add(titlePanel, "North");
			all.add(scroll, "Center");
			all.add(scrollpane, "West");
			all.revalidate();
			all.repaint();
			TreeSelectionListener t = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					node = (IconNode) e.getPath().getLastPathComponent();
				}
			};
			tree.addTreeSelectionListener(t);
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.setSelectionRow(i);
				if (rows.contains(node.getName())) {
					tree.expandRow(i);
				}
			}
			tree.removeTreeSelectionListener(t);
			addTreeListener();
		}
		else if (e.getActionCommand().equals("delete all")) {
			for (int i = simDir.getChildCount() - 1; i >= 0; i--) {
				if (((IconNode) simDir.getChildAt(i)).getChildCount() == 0) {
					if (!((IconNode) simDir.getChildAt(i)).getName().equals("Average")
							&& !((IconNode) simDir.getChildAt(i)).getName().equals("Variance")
							&& !((IconNode) simDir.getChildAt(i)).getName().equals(
									"Standard Deviation")) {
						File dir = new File(outDir + separator
								+ ((IconNode) simDir.getChildAt(i)).getName() + "."
								+ printer_id.substring(0, printer_id.length() - 8));
						if (dir.isDirectory()) {
							biomodelsim.deleteDir(dir);
						}
						else {
							dir.delete();
						}
					}
					simDir.remove(i);
				}
				else {
					File dir = new File(outDir + separator
							+ ((IconNode) simDir.getChildAt(i)).getName());
					if (dir.isDirectory()) {
						biomodelsim.deleteDir(dir);
					}
					else {
						dir.delete();
					}
					simDir.remove(i);
				}
			}
			boolean checked = false;
			for (int i = 0; i < simDir.getChildCount(); i++) {
				if (((IconNode) simDir.getChildAt(i)).getIconName().equals("" + (char) 10003)) {
					checked = true;
				}
			}
			if (!checked) {
				simDir.setIcon(MetalIconFactory.getTreeFolderIcon());
				simDir.setIconName("");
			}
			ArrayList<String> rows = new ArrayList<String>();
			for (int i = 0; i < tree.getRowCount(); i++) {
				if (tree.isExpanded(i)) {
					tree.setSelectionRow(i);
					rows.add(node.getName());
				}
			}
			scrollpane = new JScrollPane();
			refreshTree();
			scrollpane.getViewport().add(tree);
			scrollpane.setPreferredSize(new Dimension(175, 100));
			all.removeAll();
			all.add(titlePanel, "North");
			all.add(scroll, "Center");
			all.add(scrollpane, "West");
			all.revalidate();
			all.repaint();
			TreeSelectionListener t = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					node = (IconNode) e.getPath().getLastPathComponent();
				}
			};
			tree.addTreeSelectionListener(t);
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.setSelectionRow(i);
				if (rows.contains(node.getName())) {
					tree.expandRow(i);
				}
			}
			tree.removeTreeSelectionListener(t);
			addTreeListener();
		}
		// // if the export as jpeg button is clicked
		// else if (e.getSource() == exportJPeg) {
		// export(0);
		// }
		// // if the export as png button is clicked
		// else if (e.getSource() == exportPng) {
		// export(1);
		// }
		// // if the export as pdf button is clicked
		// else if (e.getSource() == exportPdf) {
		// export(2);
		// }
		// // if the export as eps button is clicked
		// else if (e.getSource() == exportEps) {
		// export(3);
		// }
		// // if the export as svg button is clicked
		// else if (e.getSource() == exportSvg) {
		// export(4);
		// } else if (e.getSource() == exportCsv) {
		// export(5);
		// }
	}

	/**
	 * Private method used to auto resize the graph.
	 */
	private void resize(XYSeriesCollection dataset) {
		NumberFormat num = NumberFormat.getInstance();
		num.setMaximumFractionDigits(4);
		num.setGroupingUsed(false);
		XYPlot plot = chart.getXYPlot();
		XYItemRenderer rend = plot.getRenderer();
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		for (int j = 0; j < dataset.getSeriesCount(); j++) {
			XYSeries series = dataset.getSeries(j);
			double[][] seriesArray = series.toArray();
			Boolean visible = rend.getSeriesVisible(j);
			if (visible == null || visible.equals(true)) {
				for (int k = 0; k < series.getItemCount(); k++) {
					maxY = Math.max(seriesArray[1][k], maxY);
					minY = Math.min(seriesArray[1][k], minY);
					maxX = Math.max(seriesArray[0][k], maxX);
					minX = Math.min(seriesArray[0][k], minX);
				}
			}
		}
		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		if (minY == Double.MAX_VALUE || maxY == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		}
		// else if ((maxY - minY) < .001) {
		// axis.setRange(minY - 1, maxY + 1);
		// }
		else {
			/*
			 * axis.setRange(Double.parseDouble(num.format(minY -
			 * (Math.abs(minY) .1))), Double .parseDouble(num.format(maxY +
			 * (Math.abs(maxY) .1))));
			 */
			if ((maxY - minY) < .001) {
				axis.setStandardTickUnits(new StandardTickUnitSource());
			}
			axis.setRange(minY - (Math.abs(minY) * .1), maxY + (Math.abs(maxY) * .1));
		}
		axis.setAutoTickUnitSelection(true);
		if (LogY.isSelected()) {
			try {
				LogarithmicAxis rangeAxis = new LogarithmicAxis(chart.getXYPlot().getRangeAxis()
						.getLabel());
				rangeAxis.setStrictValuesFlag(false);
				plot.setRangeAxis(rangeAxis);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(biomodelsim.frame(),
						"Log plots are not allowed with data\nvalues less than or equal to zero.",
						"Error", JOptionPane.ERROR_MESSAGE);
				NumberAxis rangeAxis = new NumberAxis(chart.getXYPlot().getRangeAxis().getLabel());
				plot.setRangeAxis(rangeAxis);
				LogY.setSelected(false);
			}
		}
		axis = (NumberAxis) plot.getDomainAxis();
		if (minX == Double.MAX_VALUE || maxX == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		}
		// else if ((maxX - minX) < .001) {
		// axis.setRange(minX - 1, maxX + 1);
		// }
		else {
			if ((maxX - minX) < .001) {
				axis.setStandardTickUnits(new StandardTickUnitSource());
			}
			axis.setRange(minX, maxX);
		}
		axis.setAutoTickUnitSelection(true);
		if (LogX.isSelected()) {
			try {
				LogarithmicAxis domainAxis = new LogarithmicAxis(chart.getXYPlot().getDomainAxis()
						.getLabel());
				domainAxis.setStrictValuesFlag(false);
				plot.setDomainAxis(domainAxis);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(biomodelsim.frame(),
						"Log plots are not allowed with data\nvalues less than or equal to zero.",
						"Error", JOptionPane.ERROR_MESSAGE);
				NumberAxis domainAxis = new NumberAxis(chart.getXYPlot().getDomainAxis().getLabel());
				plot.setRangeAxis(domainAxis);
				LogX.setSelected(false);
			}
		}
	}

	/**
	 * After the chart is redrawn, this method calculates the x and y scale and
	 * updates those text fields.
	 */
	public void chartProgress(ChartProgressEvent e) {
		// if the chart drawing is started
		if (e.getType() == ChartProgressEvent.DRAWING_STARTED) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		// if the chart drawing is finished
		else if (e.getType() == ChartProgressEvent.DRAWING_FINISHED) {
			this.setCursor(null);
			JFreeChart chart = e.getChart();
			XYPlot plot = (XYPlot) chart.getXYPlot();
			NumberAxis axis = (NumberAxis) plot.getRangeAxis();
			YMin.setText("" + axis.getLowerBound());
			YMax.setText("" + axis.getUpperBound());
			YScale.setText("" + axis.getTickUnit().getSize());
			axis = (NumberAxis) plot.getDomainAxis();
			XMin.setText("" + axis.getLowerBound());
			XMax.setText("" + axis.getUpperBound());
			XScale.setText("" + axis.getTickUnit().getSize());
		}
	}

	/**
	 * Invoked when the mouse is clicked on the chart. Allows the user to edit
	 * the title and labels of the chart.
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() != tree) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				if (timeSeries) {
					editGraph();
				}
				else {
					editProbGraph();
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() == tree) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			if (selRow < 0)
				return;
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			boolean set = true;
			for (TreePath p : tree.getSelectionPaths()) {
				if (p.equals(selPath)) {
					tree.addSelectionPath(selPath);
					set = false;
				}
			}
			if (set) {
				tree.setSelectionPath(selPath);
			}
			if (e.isPopupTrigger()) {
				popup.removeAll();
				if (node.getChildCount() != 0 && node.getParent() != null) {
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(rename);
				}
				if (node.getParent() != null) {
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					popup.add(delete);
				}
				else {
					JMenuItem delete = new JMenuItem("Delete All Runs");
					delete.addActionListener(this);
					delete.setActionCommand("delete runs");
					popup.add(delete);
					JMenuItem deleteAll = new JMenuItem("Delete Recursive");
					deleteAll.addActionListener(this);
					deleteAll.setActionCommand("delete all");
					popup.add(deleteAll);
				}
				if (popup.getComponentCount() != 0) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == tree) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			if (selRow < 0)
				return;
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			boolean set = true;
			for (TreePath p : tree.getSelectionPaths()) {
				if (p.equals(selPath)) {
					tree.addSelectionPath(selPath);
					set = false;
				}
			}
			if (set) {
				tree.setSelectionPath(selPath);
			}
			if (e.isPopupTrigger()) {
				popup.removeAll();
				if (node.getChildCount() != 0 && node.getParent() != null) {
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(rename);
				}
				if (node.getParent() != null) {
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					popup.add(delete);
				}
				else {
					JMenuItem delete = new JMenuItem("Delete All Runs");
					delete.addActionListener(this);
					delete.setActionCommand("delete runs");
					popup.add(delete);
					JMenuItem deleteAll = new JMenuItem("Delete Recursive");
					deleteAll.addActionListener(this);
					deleteAll.setActionCommand("delete all");
					popup.add(deleteAll);
				}
				if (popup.getComponentCount() != 0) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
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

	private void setUpShapesAndColors() {
		DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
		colors = new HashMap<String, Paint>();
		shapes = new HashMap<String, Shape>();
		colors.put("Red", draw.getNextPaint());
		colors.put("Blue", draw.getNextPaint());
		colors.put("Green", draw.getNextPaint());
		colors.put("Yellow", draw.getNextPaint());
		colors.put("Magenta", draw.getNextPaint());
		colors.put("Cyan", draw.getNextPaint());
		colors.put("Tan", draw.getNextPaint());
		colors.put("Gray (Dark)", draw.getNextPaint());
		colors.put("Red (Dark)", draw.getNextPaint());
		colors.put("Blue (Dark)", draw.getNextPaint());
		colors.put("Green (Dark)", draw.getNextPaint());
		colors.put("Yellow (Dark)", draw.getNextPaint());
		colors.put("Magenta (Dark)", draw.getNextPaint());
		colors.put("Cyan (Dark)", draw.getNextPaint());
		colors.put("Black", draw.getNextPaint());
		colors.put("Red ", draw.getNextPaint());
		colors.put("Blue ", draw.getNextPaint());
		colors.put("Green ", draw.getNextPaint());
		colors.put("Yellow ", draw.getNextPaint());
		colors.put("Magenta ", draw.getNextPaint());
		colors.put("Cyan ", draw.getNextPaint());
		colors.put("Gray (Light)", draw.getNextPaint());
		colors.put("Red (Extra Dark)", draw.getNextPaint());
		colors.put("Blue (Extra Dark)", draw.getNextPaint());
		colors.put("Green (Extra Dark)", draw.getNextPaint());
		colors.put("Yellow (Extra Dark)", draw.getNextPaint());
		colors.put("Magenta (Extra Dark)", draw.getNextPaint());
		colors.put("Cyan (Extra Dark)", draw.getNextPaint());
		colors.put("Red (Light)", draw.getNextPaint());
		colors.put("Blue (Light)", draw.getNextPaint());
		colors.put("Green (Light)", draw.getNextPaint());
		colors.put("Yellow (Light)", draw.getNextPaint());
		colors.put("Magenta (Light)", draw.getNextPaint());
		colors.put("Cyan (Light)", draw.getNextPaint());
		shapes.put("Square", draw.getNextShape());
		shapes.put("Circle", draw.getNextShape());
		shapes.put("Triangle", draw.getNextShape());
		shapes.put("Diamond", draw.getNextShape());
		shapes.put("Rectangle (Horizontal)", draw.getNextShape());
		shapes.put("Triangle (Upside Down)", draw.getNextShape());
		shapes.put("Circle (Half)", draw.getNextShape());
		shapes.put("Arrow", draw.getNextShape());
		shapes.put("Rectangle (Vertical)", draw.getNextShape());
		shapes.put("Arrow (Backwards)", draw.getNextShape());
	}

	private void editGraph() {
		final ArrayList<GraphSpecies> old = new ArrayList<GraphSpecies>();
		for (GraphSpecies g : graphed) {
			old.add(g);
		}
		titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("Title:");
		JLabel xLabel = new JLabel("X-Axis Label:");
		JLabel yLabel = new JLabel("Y-Axis Label:");
		final JTextField title = new JTextField(chart.getTitle().getText(), 5);
		final JTextField x = new JTextField(chart.getXYPlot().getDomainAxis().getLabel(), 5);
		final JTextField y = new JTextField(chart.getXYPlot().getRangeAxis().getLabel(), 5);
		final JLabel xMin = new JLabel("X-Min:");
		final JLabel xMax = new JLabel("X-Max:");
		final JLabel xScale = new JLabel("X-Step:");
		final JLabel yMin = new JLabel("Y-Min:");
		final JLabel yMax = new JLabel("Y-Max:");
		final JLabel yScale = new JLabel("Y-Step:");
		LogX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					XYPlot plot = (XYPlot) chart.getXYPlot();
					try {
						LogarithmicAxis domainAxis = new LogarithmicAxis(chart.getXYPlot()
								.getDomainAxis().getLabel());
						domainAxis.setStrictValuesFlag(false);
						plot.setRangeAxis(domainAxis);
					}
					catch (Exception e1) {
						JOptionPane
								.showMessageDialog(
										biomodelsim.frame(),
										"Log plots are not allowed with data\nvalues less than or equal to zero.",
										"Error", JOptionPane.ERROR_MESSAGE);
						NumberAxis domainAxis = new NumberAxis(chart.getXYPlot().getDomainAxis()
								.getLabel());
						plot.setRangeAxis(domainAxis);
						LogX.setSelected(false);
					}
				}
			}
		});
		LogY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					XYPlot plot = (XYPlot) chart.getXYPlot();
					try {
						LogarithmicAxis rangeAxis = new LogarithmicAxis(chart.getXYPlot()
								.getRangeAxis().getLabel());
						rangeAxis.setStrictValuesFlag(false);
						plot.setRangeAxis(rangeAxis);
					}
					catch (Exception e1) {
						JOptionPane
								.showMessageDialog(
										biomodelsim.frame(),
										"Semilog plots are not allowed with data\nvalues less than or equal to zero.",
										"Error", JOptionPane.ERROR_MESSAGE);
						NumberAxis rangeAxis = new NumberAxis(chart.getXYPlot().getRangeAxis()
								.getLabel());
						plot.setRangeAxis(rangeAxis);
						LogY.setSelected(false);
					}
				}
			}
		});
		resize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					xMin.setEnabled(false);
					XMin.setEnabled(false);
					xMax.setEnabled(false);
					XMax.setEnabled(false);
					xScale.setEnabled(false);
					XScale.setEnabled(false);
					yMin.setEnabled(false);
					YMin.setEnabled(false);
					yMax.setEnabled(false);
					YMax.setEnabled(false);
					yScale.setEnabled(false);
					YScale.setEnabled(false);
				}
				else {
					xMin.setEnabled(true);
					XMin.setEnabled(true);
					xMax.setEnabled(true);
					XMax.setEnabled(true);
					xScale.setEnabled(true);
					XScale.setEnabled(true);
					yMin.setEnabled(true);
					YMin.setEnabled(true);
					yMax.setEnabled(true);
					YMax.setEnabled(true);
					yScale.setEnabled(true);
					YScale.setEnabled(true);
				}
			}
		});
		if (resize.isSelected()) {
			xMin.setEnabled(false);
			XMin.setEnabled(false);
			xMax.setEnabled(false);
			XMax.setEnabled(false);
			xScale.setEnabled(false);
			XScale.setEnabled(false);
			yMin.setEnabled(false);
			YMin.setEnabled(false);
			yMax.setEnabled(false);
			YMax.setEnabled(false);
			yScale.setEnabled(false);
			YScale.setEnabled(false);
		}
		else {
			xMin.setEnabled(true);
			XMin.setEnabled(true);
			xMax.setEnabled(true);
			XMax.setEnabled(true);
			xScale.setEnabled(true);
			XScale.setEnabled(true);
			yMin.setEnabled(true);
			YMin.setEnabled(true);
			yMax.setEnabled(true);
			YMax.setEnabled(true);
			yScale.setEnabled(true);
			YScale.setEnabled(true);
		}
		Properties p = null;
		if (learnSpecs != null) {
			try {
				String[] split = outDir.split(separator);
				p = new Properties();
				FileInputStream load = new FileInputStream(new File(outDir + separator
						+ split[split.length - 1] + ".lrn"));
				p.load(load);
				load.close();
			}
			catch (Exception e) {
			}
		}
		String simDirString = outDir.split(separator)[outDir.split(separator).length - 1];
		simDir = new IconNode(simDirString, simDirString);
		simDir.setIconName("");
		String[] files = new File(outDir).list();
		for (int i = 1; i < files.length; i++) {
			String index = files[i];
			int j = i;
			while ((j > 0) && files[j - 1].compareToIgnoreCase(index) > 0) {
				files[j] = files[j - 1];
				j = j - 1;
			}
			files[j] = index;
		}
		boolean add = false;
		directories = new ArrayList<String>();
		for (String file : files) {
			if (file.length() > 3
					&& file.substring(file.length() - 4).equals(
							"." + printer_id.substring(0, printer_id.length() - 8))) {
				if (file.contains("run-") || file.contains("mean") || file.contains("variance")
						|| file.contains("standard_deviation")) {
					add = true;
				}
				else {
					IconNode n = new IconNode(file.substring(0, file.length() - 4), file.substring(
							0, file.length() - 4));
					simDir.add(n);
					n.setIconName("");
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(file.substring(0, file.length() - 4))
								&& g.getDirectory().equals("")) {
							n.setIcon(TextIcons.getIcon("g"));
							n.setIconName("" + (char) 10003);
							simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
							simDir.setIconName("" + (char) 10003);
						}
					}
				}
			}
			else if (new File(outDir + separator + file).isDirectory()) {
				boolean addIt = false;
				String[] files3 = new File(outDir + separator + file).list();
				for (int i = 1; i < files3.length; i++) {
					String index = files3[i];
					int j = i;
					while ((j > 0) && files3[j - 1].compareToIgnoreCase(index) > 0) {
						files3[j] = files3[j - 1];
						j = j - 1;
					}
					files3[j] = index;
				}
				for (String getFile : files3) {
					if (getFile.length() > 3
							&& getFile.substring(getFile.length() - 4).equals(
									"." + printer_id.substring(0, printer_id.length() - 8))) {
						addIt = true;
					}
					else if (new File(outDir + separator + file + separator + getFile)
							.isDirectory()) {
						for (String getFile2 : new File(outDir + separator + file + separator
								+ getFile).list()) {
							if (getFile2.length() > 3
									&& getFile2.substring(getFile2.length() - 4).equals(
											"." + printer_id.substring(0, printer_id.length() - 8))) {
								addIt = true;
							}
						}
					}
				}
				if (addIt) {
					directories.add(file);
					IconNode d = new IconNode(file, file);
					d.setIconName("");
					boolean add2 = false;
					for (String f : files3) {
						if (f.contains(printer_id.substring(0, printer_id.length() - 8))) {
							if (f.contains("run-") || f.contains("mean") || f.contains("variance")
									|| f.contains("standard_deviation")) {
								add2 = true;
							}
							else {
								IconNode n = new IconNode(f.substring(0, f.length() - 4), f
										.substring(0, f.length() - 4));
								d.add(n);
								n.setIconName("");
								for (GraphSpecies g : graphed) {
									if (g.getRunNumber().equals(f.substring(0, f.length() - 4))
											&& g.getDirectory().equals(d.getName())) {
										n.setIcon(TextIcons.getIcon("g"));
										n.setIconName("" + (char) 10003);
										d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
										d.setIconName("" + (char) 10003);
										simDir.setIcon(MetalIconFactory
												.getFileChooserUpFolderIcon());
										simDir.setIconName("" + (char) 10003);
									}
								}
							}
						}
						else if (new File(outDir + separator + file + separator + f).isDirectory()) {
							boolean addIt2 = false;
							String[] files2 = new File(outDir + separator + file + separator + f)
									.list();
							for (int i = 1; i < files2.length; i++) {
								String index = files2[i];
								int j = i;
								while ((j > 0) && files2[j - 1].compareToIgnoreCase(index) > 0) {
									files2[j] = files2[j - 1];
									j = j - 1;
								}
								files2[j] = index;
							}
							for (String getFile2 : files2) {
								if (getFile2.length() > 3
										&& getFile2.substring(getFile2.length() - 4).equals(
												"."
														+ printer_id.substring(0, printer_id
																.length() - 8))) {
									addIt2 = true;
								}
							}
							if (addIt2) {
								directories.add(file + separator + f);
								IconNode d2 = new IconNode(f, f);
								d2.setIconName("");
								boolean add3 = false;
								for (String f2 : files2) {
									if (f2.contains(printer_id
											.substring(0, printer_id.length() - 8))) {
										if (f2.contains("run-") || f2.contains("mean")
												|| f2.contains("variance")
												|| f2.contains("standard_deviation")) {
											add3 = true;
										}
										else {
											IconNode n = new IconNode(f2.substring(0,
													f2.length() - 4), f2.substring(0,
													f2.length() - 4));
											d2.add(n);
											n.setIconName("");
											for (GraphSpecies g : graphed) {
												if (g.getRunNumber().equals(
														f2.substring(0, f2.length() - 4))
														&& g.getDirectory().equals(
																d.getName() + separator
																		+ d2.getName())) {
													n.setIcon(TextIcons.getIcon("g"));
													n.setIconName("" + (char) 10003);
													d2.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													d2.setIconName("" + (char) 10003);
													d.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													d.setIconName("" + (char) 10003);
													simDir.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													simDir.setIconName("" + (char) 10003);
												}
											}
										}
									}
								}
								if (add3) {
									IconNode n = new IconNode("Average", "Average");
									d2.add(n);
									n.setIconName("");
									for (GraphSpecies g : graphed) {
										if (g.getRunNumber().equals("Average")
												&& g.getDirectory().equals(
														d.getName() + separator + d2.getName())) {
											n.setIcon(TextIcons.getIcon("g"));
											n.setIconName("" + (char) 10003);
											d2.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											d2.setIconName("" + (char) 10003);
											d
													.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
											d.setIconName("" + (char) 10003);
											simDir.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											simDir.setIconName("" + (char) 10003);
										}
									}
									n = new IconNode("Variance", "Variance");
									d2.add(n);
									n.setIconName("");
									for (GraphSpecies g : graphed) {
										if (g.getRunNumber().equals("Variance")
												&& g.getDirectory().equals(
														d.getName() + separator + d2.getName())) {
											n.setIcon(TextIcons.getIcon("g"));
											n.setIconName("" + (char) 10003);
											d2.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											d2.setIconName("" + (char) 10003);
											d
													.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
											d.setIconName("" + (char) 10003);
											simDir.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											simDir.setIconName("" + (char) 10003);
										}
									}
									n = new IconNode("Standard Deviation", "Standard Deviation");
									d2.add(n);
									n.setIconName("");
									for (GraphSpecies g : graphed) {
										if (g.getRunNumber().equals("Standard Deviation")
												&& g.getDirectory().equals(
														d.getName() + separator + d2.getName())) {
											n.setIcon(TextIcons.getIcon("g"));
											n.setIconName("" + (char) 10003);
											d2.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											d2.setIconName("" + (char) 10003);
											d
													.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
											d.setIconName("" + (char) 10003);
											simDir.setIcon(MetalIconFactory
													.getFileChooserUpFolderIcon());
											simDir.setIconName("" + (char) 10003);
										}
									}
								}
								int run = 1;
								String r2 = null;
								for (String s : files2) {
									if (s.contains("run-")) {
										r2 = s;
									}
								}
								if (r2 != null) {
									for (String s : files2) {
										if (s.length() > 4) {
											String end = "";
											for (int j = 1; j < 5; j++) {
												end = s.charAt(s.length() - j) + end;
											}
											if (end.equals(".tsd") || end.equals(".dat")
													|| end.equals(".csv")) {
												if (s.contains("run-")) {
													run = Math.max(run, Integer
															.parseInt(s.substring(4, s.length()
																	- end.length())));
												}
											}
										}
									}
									for (int i = 0; i < run; i++) {
										if (new File(outDir + separator + file + separator + f
												+ separator + "run-" + (i + 1) + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
											IconNode n;
											if (learnSpecs != null) {
												n = new IconNode(p.get("run-"
														+ (i + 1)
														+ "."
														+ printer_id.substring(0, printer_id
																.length() - 8)), "run-" + (i + 1));
												if (d2.getChildCount() > 3) {
													boolean added = false;
													for (int j = 3; j < d2.getChildCount(); j++) {
														if (d2
																.getChildAt(j)
																.toString()
																.compareToIgnoreCase(
																		(String) p
																				.get("run-"
																						+ (i + 1)
																						+ "."
																						+ printer_id
																								.substring(
																										0,
																										printer_id
																												.length() - 8))) > 0) {
															d2.insert(n, j);
															added = true;
															break;
														}
													}
													if (!added) {
														d2.add(n);
													}
												}
												else {
													d2.add(n);
												}
											}
											else {
												n = new IconNode("run-" + (i + 1), "run-" + (i + 1));
												d2.add(n);
											}
											n.setIconName("");
											for (GraphSpecies g : graphed) {
												if (g.getRunNumber().equals("run-" + (i + 1))
														&& g.getDirectory().equals(
																d.getName() + separator
																		+ d2.getName())) {
													n.setIcon(TextIcons.getIcon("g"));
													n.setIconName("" + (char) 10003);
													d2.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													d2.setIconName("" + (char) 10003);
													d.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													d.setIconName("" + (char) 10003);
													simDir.setIcon(MetalIconFactory
															.getFileChooserUpFolderIcon());
													simDir.setIconName("" + (char) 10003);
												}
											}
										}
									}
								}
								d.add(d2);
							}
						}
					}
					if (add2) {
						IconNode n = new IconNode("Average", "Average");
						d.add(n);
						n.setIconName("");
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals("Average")
									&& g.getDirectory().equals(d.getName())) {
								n.setIcon(TextIcons.getIcon("g"));
								n.setIconName("" + (char) 10003);
								d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								d.setIconName("" + (char) 10003);
								simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								simDir.setIconName("" + (char) 10003);
							}
						}
						n = new IconNode("Variance", "Variance");
						d.add(n);
						n.setIconName("");
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals("Variance")
									&& g.getDirectory().equals(d.getName())) {
								n.setIcon(TextIcons.getIcon("g"));
								n.setIconName("" + (char) 10003);
								d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								d.setIconName("" + (char) 10003);
								simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								simDir.setIconName("" + (char) 10003);
							}
						}
						n = new IconNode("Standard Deviation", "Standard Deviation");
						d.add(n);
						n.setIconName("");
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals("Standard Deviation")
									&& g.getDirectory().equals(d.getName())) {
								n.setIcon(TextIcons.getIcon("g"));
								n.setIconName("" + (char) 10003);
								d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								d.setIconName("" + (char) 10003);
								simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								simDir.setIconName("" + (char) 10003);
							}
						}
					}
					int run = 1;
					String r = null;
					for (String s : files3) {
						if (s.contains("run-")) {
							r = s;
						}
					}
					if (r != null) {
						for (String s : files3) {
							if (s.length() > 4) {
								String end = "";
								for (int j = 1; j < 5; j++) {
									end = s.charAt(s.length() - j) + end;
								}
								if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
									if (s.contains("run-")) {
										run = Math.max(run, Integer.parseInt(s.substring(4, s
												.length()
												- end.length())));
									}
								}
							}
						}
						for (int i = 0; i < run; i++) {
							if (new File(outDir + separator + file + separator + "run-" + (i + 1)
									+ "." + printer_id.substring(0, printer_id.length() - 8))
									.exists()) {
								IconNode n;
								if (learnSpecs != null) {
									n = new IconNode(p.get("run-" + (i + 1) + "."
											+ printer_id.substring(0, printer_id.length() - 8)),
											"run-" + (i + 1));
									if (d.getChildCount() > 3) {
										boolean added = false;
										for (int j = 3; j < d.getChildCount(); j++) {
											if (d.getChildAt(j).toString().compareToIgnoreCase(
													(String) p.get("run-"
															+ (i + 1)
															+ "."
															+ printer_id.substring(0, printer_id
																	.length() - 8))) > 0) {
												d.insert(n, j);
												added = true;
												break;
											}
										}
										if (!added) {
											d.add(n);
										}
									}
									else {
										d.add(n);
									}
								}
								else {
									n = new IconNode("run-" + (i + 1), "run-" + (i + 1));
									d.add(n);
								}
								n.setIconName("");
								for (GraphSpecies g : graphed) {
									if (g.getRunNumber().equals("run-" + (i + 1))
											&& g.getDirectory().equals(d.getName())) {
										n.setIcon(TextIcons.getIcon("g"));
										n.setIconName("" + (char) 10003);
										d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
										d.setIconName("" + (char) 10003);
										simDir.setIcon(MetalIconFactory
												.getFileChooserUpFolderIcon());
										simDir.setIconName("" + (char) 10003);
									}
								}
							}
						}
					}
					simDir.add(d);
				}
			}
		}
		if (add) {
			IconNode n = new IconNode("Average", "Average");
			simDir.add(n);
			n.setIconName("");
			for (GraphSpecies g : graphed) {
				if (g.getRunNumber().equals("Average") && g.getDirectory().equals("")) {
					n.setIcon(TextIcons.getIcon("g"));
					n.setIconName("" + (char) 10003);
					simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
					simDir.setIconName("" + (char) 10003);
				}
			}
			n = new IconNode("Variance", "Variance");
			simDir.add(n);
			n.setIconName("");
			for (GraphSpecies g : graphed) {
				if (g.getRunNumber().equals("Variance") && g.getDirectory().equals("")) {
					n.setIcon(TextIcons.getIcon("g"));
					n.setIconName("" + (char) 10003);
					simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
					simDir.setIconName("" + (char) 10003);
				}
			}
			n = new IconNode("Standard Deviation", "Standard Deviation");
			simDir.add(n);
			n.setIconName("");
			for (GraphSpecies g : graphed) {
				if (g.getRunNumber().equals("Standard Deviation") && g.getDirectory().equals("")) {
					n.setIcon(TextIcons.getIcon("g"));
					n.setIconName("" + (char) 10003);
					simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
					simDir.setIconName("" + (char) 10003);
				}
			}
		}
		int run = 1;
		String runs = null;
		for (String s : new File(outDir).list()) {
			if (s.contains("run-")) {
				runs = s;
			}
		}
		if (runs != null) {
			for (String s : new File(outDir).list()) {
				if (s.length() > 4) {
					String end = "";
					for (int j = 1; j < 5; j++) {
						end = s.charAt(s.length() - j) + end;
					}
					if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
						if (s.contains("run-")) {
							run = Math.max(run, Integer.parseInt(s.substring(4, s.length()
									- end.length())));
						}
					}
				}
			}
			for (int i = 0; i < run; i++) {
				if (new File(outDir + separator + "run-" + (i + 1) + "."
						+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					IconNode n;
					if (learnSpecs != null) {
						n = new IconNode(p.get("run-" + (i + 1) + "."
								+ printer_id.substring(0, printer_id.length() - 8)), "run-"
								+ (i + 1));
						if (simDir.getChildCount() > 3) {
							boolean added = false;
							for (int j = 3; j < simDir.getChildCount(); j++) {
								if (simDir.getChildAt(j).toString()
										.compareToIgnoreCase(
												(String) p.get("run-"
														+ (i + 1)
														+ "."
														+ printer_id.substring(0, printer_id
																.length() - 8))) > 0) {
									simDir.insert(n, j);
									added = true;
									break;
								}
							}
							if (!added) {
								simDir.add(n);
							}
						}
						else {
							simDir.add(n);
						}
					}
					else {
						n = new IconNode("run-" + (i + 1), "run-" + (i + 1));
						simDir.add(n);
					}
					n.setIconName("");
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals("run-" + (i + 1))
								&& g.getDirectory().equals("")) {
							n.setIcon(TextIcons.getIcon("g"));
							n.setIconName("" + (char) 10003);
							simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
							simDir.setIconName("" + (char) 10003);
						}
					}
				}
			}
		}
		if (simDir.getChildCount() == 0) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "No data to graph."
					+ "\nPerform some simulations to create some data first.", "No Data",
					JOptionPane.PLAIN_MESSAGE);
		}
		else {
			all = new JPanel(new BorderLayout());
			specPanel = new JPanel();
			scrollpane = new JScrollPane();
			refreshTree();
			addTreeListener();
			scrollpane.getViewport().add(tree);
			scrollpane.setPreferredSize(new Dimension(175, 100));
			scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(1050, 500));
			JPanel editPanel = new JPanel(new BorderLayout());
			editPanel.add(specPanel, "Center");
			scroll.setViewportView(editPanel);
			// JButton ok = new JButton("Ok");
			/*
			 * ok.addActionListener(new ActionListener() { public void
			 * actionPerformed(ActionEvent e) { double minY; double maxY; double
			 * scaleY; double minX; double maxX; double scaleX; change = true;
			 * try { minY = Double.parseDouble(YMin.getText().trim()); maxY =
			 * Double.parseDouble(YMax.getText().trim()); scaleY =
			 * Double.parseDouble(YScale.getText().trim()); minX =
			 * Double.parseDouble(XMin.getText().trim()); maxX =
			 * Double.parseDouble(XMax.getText().trim()); scaleX =
			 * Double.parseDouble(XScale.getText().trim()); NumberFormat num =
			 * NumberFormat.getInstance(); num.setMaximumFractionDigits(4);
			 * num.setGroupingUsed(false); minY =
			 * Double.parseDouble(num.format(minY)); maxY =
			 * Double.parseDouble(num.format(maxY)); scaleY =
			 * Double.parseDouble(num.format(scaleY)); minX =
			 * Double.parseDouble(num.format(minX)); maxX =
			 * Double.parseDouble(num.format(maxX)); scaleX =
			 * Double.parseDouble(num.format(scaleX)); } catch (Exception e1) {
			 * JOptionPane.showMessageDialog(biomodelsim.frame(), "Must enter
			 * doubles into the inputs " + "to change the graph's
			 * dimensions!", "Error", JOptionPane.ERROR_MESSAGE); return; }
			 * lastSelected = selected; selected = ""; ArrayList<XYSeries>
			 * graphData = new ArrayList<XYSeries>(); XYLineAndShapeRenderer
			 * rend = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
			 * int thisOne = -1; for (int i = 1; i < graphed.size(); i++) {
			 * GraphSpecies index = graphed.get(i); int j = i; while ((j > 0) &&
			 * (graphed.get(j -
			 * 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
			 * graphed.set(j, graphed.get(j - 1)); j = j - 1; } graphed.set(j,
			 * index); } ArrayList<GraphSpecies> unableToGraph = new
			 * ArrayList<GraphSpecies>(); HashMap<String,
			 * ArrayList<ArrayList<Double>>> allData = new HashMap<String,
			 * ArrayList<ArrayList<Double>>>(); for (GraphSpecies g : graphed) {
			 * if (g.getDirectory().equals("")) { thisOne++;
			 * rend.setSeriesVisible(thisOne, true);
			 * rend.setSeriesLinesVisible(thisOne, g.getConnected());
			 * rend.setSeriesShapesFilled(thisOne, g.getFilled());
			 * rend.setSeriesShapesVisible(thisOne, g.getVisible());
			 * rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
			 * rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape()); if
			 * (!g.getRunNumber().equals("Average") &&
			 * !g.getRunNumber().equals("Variance") &&
			 * !g.getRunNumber().equals("Standard Deviation")) { if (new
			 * File(outDir + separator + g.getRunNumber() + "." +
			 * printer_id.substring(0, printer_id.length() - 8)).exists()) {
			 * readGraphSpecies(outDir + separator + g.getRunNumber() + "." +
			 * printer_id.substring(0, printer_id.length() - 8),
			 * biomodelsim.frame()); ArrayList<ArrayList<Double>> data; if
			 * (allData.containsKey(g.getRunNumber() + " " + g.getDirectory()))
			 * { data = allData.get(g.getRunNumber() + " " + g.getDirectory());
			 * } else { data = readData(outDir + separator + g.getRunNumber() +
			 * "." + printer_id.substring(0, printer_id.length() - 8),
			 * biomodelsim.frame(), y.getText().trim(), g.getRunNumber(), null);
			 * for (int i = 2; i < graphSpecies.size(); i++) { String index =
			 * graphSpecies.get(i); ArrayList<Double> index2 = data.get(i); int
			 * j = i; while ((j > 1) && graphSpecies.get(j -
			 * 1).compareToIgnoreCase(index) > 0) { graphSpecies.set(j,
			 * graphSpecies.get(j - 1)); data.set(j, data.get(j - 1)); j = j -
			 * 1; } graphSpecies.set(j, index); data.set(j, index2); }
			 * allData.put(g.getRunNumber() + " " + g.getDirectory(), data); }
			 * graphData.add(new XYSeries(g.getSpecies())); if (data.size() !=
			 * 0) { for (int i = 0; i < (data.get(0)).size(); i++) {
			 * graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
			 * (data.get(g.getNumber() + 1)).get(i)); } } } else {
			 * unableToGraph.add(g); thisOne--; } } else { boolean ableToGraph =
			 * false; try { for (String s : new File(outDir).list()) { if
			 * (s.length() > 3 && s.substring(0, 4).equals("run-")) {
			 * ableToGraph = true; } } } catch (Exception e1) { ableToGraph =
			 * false; } if (ableToGraph) { int next = 1; while (!new File(outDir
			 * + separator + "run-" + next + "." + printer_id.substring(0,
			 * printer_id.length() - 8)).exists()) { next++; }
			 * readGraphSpecies(outDir + separator + "run-" + next + "." +
			 * printer_id.substring(0, printer_id.length() - 8),
			 * biomodelsim.frame()); ArrayList<ArrayList<Double>> data; if
			 * (allData.containsKey(g.getRunNumber() + " " + g.getDirectory()))
			 * { data = allData.get(g.getRunNumber() + " " + g.getDirectory());
			 * } else { data = readData(outDir + separator + "run-1." +
			 * printer_id.substring(0, printer_id.length() - 8),
			 * biomodelsim.frame(), y.getText().trim(),
			 * g.getRunNumber().toLowerCase(), null); for (int i = 2; i <
			 * graphSpecies.size(); i++) { String index = graphSpecies.get(i);
			 * ArrayList<Double> index2 = data.get(i); int j = i; while ((j > 1)
			 * && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
			 * graphSpecies.set(j, graphSpecies.get(j - 1)); data.set(j,
			 * data.get(j - 1)); j = j - 1; } graphSpecies.set(j, index);
			 * data.set(j, index2); } allData.put(g.getRunNumber() + " " +
			 * g.getDirectory(), data); } graphData.add(new
			 * XYSeries(g.getSpecies())); if (data.size() != 0) { for (int i =
			 * 0; i < (data.get(0)).size(); i++) {
			 * graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
			 * (data.get(g.getNumber() + 1)).get(i)); } } } else {
			 * unableToGraph.add(g); thisOne--; } } } else { thisOne++;
			 * rend.setSeriesVisible(thisOne, true);
			 * rend.setSeriesLinesVisible(thisOne, g.getConnected());
			 * rend.setSeriesShapesFilled(thisOne, g.getFilled());
			 * rend.setSeriesShapesVisible(thisOne, g.getVisible());
			 * rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
			 * rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape()); if
			 * (!g.getRunNumber().equals("Average") &&
			 * !g.getRunNumber().equals("Variance") &&
			 * !g.getRunNumber().equals("Standard Deviation")) { if (new
			 * File(outDir + separator + g.getDirectory() + separator +
			 * g.getRunNumber() + "." + printer_id.substring(0,
			 * printer_id.length() - 8)).exists()) { readGraphSpecies( outDir +
			 * separator + g.getDirectory() + separator + g.getRunNumber() + "."
			 * + printer_id.substring(0, printer_id.length() - 8),
			 * biomodelsim.frame()); ArrayList<ArrayList<Double>> data; if
			 * (allData.containsKey(g.getRunNumber() + " " + g.getDirectory()))
			 * { data = allData.get(g.getRunNumber() + " " + g.getDirectory());
			 * } else { data = readData(outDir + separator + g.getDirectory() +
			 * separator + g.getRunNumber() + "." + printer_id.substring(0,
			 * printer_id.length() - 8), biomodelsim.frame(),
			 * y.getText().trim(), g.getRunNumber(), g.getDirectory()); for (int
			 * i = 2; i < graphSpecies.size(); i++) { String index =
			 * graphSpecies.get(i); ArrayList<Double> index2 = data.get(i); int
			 * j = i; while ((j > 1) && graphSpecies.get(j -
			 * 1).compareToIgnoreCase(index) > 0) { graphSpecies.set(j,
			 * graphSpecies.get(j - 1)); data.set(j, data.get(j - 1)); j = j -
			 * 1; } graphSpecies.set(j, index); data.set(j, index2); }
			 * allData.put(g.getRunNumber() + " " + g.getDirectory(), data); }
			 * graphData.add(new XYSeries(g.getSpecies())); if (data.size() !=
			 * 0) { for (int i = 0; i < (data.get(0)).size(); i++) {
			 * graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
			 * (data.get(g.getNumber() + 1)).get(i)); } } } else {
			 * unableToGraph.add(g); thisOne--; } } else { boolean ableToGraph =
			 * false; try { for (String s : new File(outDir + separator +
			 * g.getDirectory()).list()) { if (s.length() > 3 && s.substring(0,
			 * 4).equals("run-")) { ableToGraph = true; } } } catch (Exception
			 * e1) { ableToGraph = false; } if (ableToGraph) { int next = 1;
			 * while (!new File(outDir + separator + g.getDirectory() +
			 * separator + "run-" + next + "." + printer_id.substring(0,
			 * printer_id.length() - 8)).exists()) { next++; }
			 * readGraphSpecies(outDir + separator + g.getDirectory() +
			 * separator + "run-" + next + "." + printer_id.substring(0,
			 * printer_id.length() - 8), biomodelsim.frame());
			 * ArrayList<ArrayList<Double>> data; if
			 * (allData.containsKey(g.getRunNumber() + " " + g.getDirectory()))
			 * { data = allData.get(g.getRunNumber() + " " + g.getDirectory());
			 * } else { data = readData(outDir + separator + g.getDirectory() +
			 * separator + "run-1." + printer_id.substring(0,
			 * printer_id.length() - 8), biomodelsim.frame(),
			 * y.getText().trim(), g.getRunNumber().toLowerCase(),
			 * g.getDirectory()); for (int i = 2; i < graphSpecies.size(); i++)
			 * { String index = graphSpecies.get(i); ArrayList<Double> index2 =
			 * data.get(i); int j = i; while ((j > 1) && graphSpecies.get(j -
			 * 1).compareToIgnoreCase(index) > 0) { graphSpecies.set(j,
			 * graphSpecies.get(j - 1)); data.set(j, data.get(j - 1)); j = j -
			 * 1; } graphSpecies.set(j, index); data.set(j, index2); }
			 * allData.put(g.getRunNumber() + " " + g.getDirectory(), data); }
			 * graphData.add(new XYSeries(g.getSpecies())); if (data.size() !=
			 * 0) { for (int i = 0; i < (data.get(0)).size(); i++) {
			 * graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
			 * (data.get(g.getNumber() + 1)).get(i)); } } } else {
			 * unableToGraph.add(g); thisOne--; } } } } for (GraphSpecies g :
			 * unableToGraph) { graphed.remove(g); } XYSeriesCollection dataset
			 * = new XYSeriesCollection(); for (int i = 0; i < graphData.size();
			 * i++) { dataset.addSeries(graphData.get(i)); }
			 * fixGraph(title.getText().trim(), x.getText().trim(),
			 * y.getText().trim(), dataset);
			 * chart.getXYPlot().setRenderer(rend); XYPlot plot =
			 * chart.getXYPlot(); if (resize.isSelected()) { resize(dataset); }
			 * else { NumberAxis axis = (NumberAxis) plot.getRangeAxis();
			 * axis.setAutoTickUnitSelection(false); axis.setRange(minY, maxY);
			 * axis.setTickUnit(new NumberTickUnit(scaleY)); axis = (NumberAxis)
			 * plot.getDomainAxis(); axis.setAutoTickUnitSelection(false);
			 * axis.setRange(minX, maxX); axis.setTickUnit(new
			 * NumberTickUnit(scaleX)); } //f.dispose(); } });
			 */
			// final JButton cancel = new JButton("Cancel");
			// cancel.addActionListener(new ActionListener() {
			// public void actionPerformed(ActionEvent e) {
			// selected = "";
			// int size = graphed.size();
			// for (int i = 0; i < size; i++) {
			// graphed.remove();
			// }
			// for (GraphSpecies g : old) {
			// graphed.add(g);
			// }
			// f.dispose();
			// }
			// });
			final JButton deselect = new JButton("Deselect All");
			deselect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// selected = "";
					int size = graphed.size();
					for (int i = 0; i < size; i++) {
						graphed.remove();
					}
					IconNode n = simDir;
					while (n != null) {
						if (n.isLeaf()) {
							n.setIcon(MetalIconFactory.getTreeLeafIcon());
							n.setIconName("");
							IconNode check = (IconNode) ((DefaultMutableTreeNode) n.getParent())
									.getChildAfter(n);
							if (check == null) {
								n = (IconNode) n.getParent();
								if (n.getParent() == null) {
									n = null;
								}
								else {
									IconNode check2 = (IconNode) ((DefaultMutableTreeNode) n
											.getParent()).getChildAfter(n);
									if (check2 == null) {
										n = (IconNode) n.getParent();
										if (n.getParent() == null) {
											n = null;
										}
										else {
											n = (IconNode) ((DefaultMutableTreeNode) n.getParent())
													.getChildAfter(n);
										}
									}
									else {
										n = check2;
									}
								}
							}
							else {
								n = check;
							}
						}
						else {
							n.setIcon(MetalIconFactory.getTreeFolderIcon());
							n.setIconName("");
							n = (IconNode) n.getChildAt(0);
						}
					}
					tree.revalidate();
					tree.repaint();
					if (tree.getSelectionCount() > 0) {
						int selectedRow = tree.getSelectionRows()[0];
						tree.setSelectionRow(0);
						tree.setSelectionRow(selectedRow);
					}
				}
			});
			JPanel titlePanel1 = new JPanel(new GridLayout(3, 6));
			JPanel titlePanel2 = new JPanel(new GridLayout(1, 6));
			titlePanel1.add(titleLabel);
			titlePanel1.add(title);
			titlePanel1.add(xMin);
			titlePanel1.add(XMin);
			titlePanel1.add(yMin);
			titlePanel1.add(YMin);
			titlePanel1.add(xLabel);
			titlePanel1.add(x);
			titlePanel1.add(xMax);
			titlePanel1.add(XMax);
			titlePanel1.add(yMax);
			titlePanel1.add(YMax);
			titlePanel1.add(yLabel);
			titlePanel1.add(y);
			titlePanel1.add(xScale);
			titlePanel1.add(XScale);
			titlePanel1.add(yScale);
			titlePanel1.add(YScale);
			JPanel deselectPanel = new JPanel();
			deselectPanel.add(deselect);
			titlePanel2.add(deselectPanel);
			titlePanel2.add(resize);
			titlePanel2.add(XVariable);
			titlePanel2.add(LogX);
			titlePanel2.add(new JPanel());
			titlePanel2.add(LogY);
			titlePanel.add(titlePanel1, "Center");
			titlePanel.add(titlePanel2, "South");
			// JPanel buttonPanel = new JPanel();
			// buttonPanel.add(ok);
			// buttonPanel.add(deselect);
			// buttonPanel.add(cancel);
			all.add(titlePanel, "North");
			all.add(scroll, "Center");
			all.add(scrollpane, "West");
			// all.add(buttonPanel, "South");
			Object[] options = { "Ok", "Cancel" };
			int value = JOptionPane
					.showOptionDialog(biomodelsim.frame(), all, "Edit Graph",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
			if (value == JOptionPane.YES_OPTION) {
				double minY;
				double maxY;
				double scaleY;
				double minX;
				double maxX;
				double scaleX;
				change = true;
				try {
					minY = Double.parseDouble(YMin.getText().trim());
					maxY = Double.parseDouble(YMax.getText().trim());
					scaleY = Double.parseDouble(YScale.getText().trim());
					minX = Double.parseDouble(XMin.getText().trim());
					maxX = Double.parseDouble(XMax.getText().trim());
					scaleX = Double.parseDouble(XScale.getText().trim());
					/*
					 * NumberFormat num = NumberFormat.getInstance();
					 * num.setMaximumFractionDigits(4);
					 * num.setGroupingUsed(false); minY =
					 * Double.parseDouble(num.format(minY)); maxY =
					 * Double.parseDouble(num.format(maxY)); scaleY =
					 * Double.parseDouble(num.format(scaleY)); minX =
					 * Double.parseDouble(num.format(minX)); maxX =
					 * Double.parseDouble(num.format(maxX)); scaleX =
					 * Double.parseDouble(num.format(scaleX));
					 */
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biomodelsim.frame(),
							"Must enter doubles into the inputs "
									+ "to change the graph's dimensions!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				lastSelected = selected;
				selected = "";
				ArrayList<XYSeries> graphData = new ArrayList<XYSeries>();
				XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot()
						.getRenderer();
				int thisOne = -1;
				for (int i = 1; i < graphed.size(); i++) {
					GraphSpecies index = graphed.get(i);
					int j = i;
					while ((j > 0)
							&& (graphed.get(j - 1).getSpecies().compareToIgnoreCase(
									index.getSpecies()) > 0)) {
						graphed.set(j, graphed.get(j - 1));
						j = j - 1;
					}
					graphed.set(j, index);
				}
				ArrayList<GraphSpecies> unableToGraph = new ArrayList<GraphSpecies>();
				HashMap<String, ArrayList<ArrayList<Double>>> allData = new HashMap<String, ArrayList<ArrayList<Double>>>();
				for (GraphSpecies g : graphed) {
					if (g.getDirectory().equals("")) {
						thisOne++;
						rend.setSeriesVisible(thisOne, true);
						rend.setSeriesLinesVisible(thisOne, g.getConnected());
						rend.setSeriesShapesFilled(thisOne, g.getFilled());
						rend.setSeriesShapesVisible(thisOne, g.getVisible());
						rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
						rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
						if (!g.getRunNumber().equals("Average")
								&& !g.getRunNumber().equals("Variance")
								&& !g.getRunNumber().equals("Standard Deviation")) {
							if (new File(outDir + separator + g.getRunNumber() + "."
									+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + g.getRunNumber() + "."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + "mean."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + "variance."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + "standard_deviation."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								boolean ableToGraph = false;
								try {
									for (String s : new File(outDir).list()) {
										if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
											ableToGraph = true;
										}
									}
								}
								catch (Exception e1) {
									ableToGraph = false;
								}
								if (ableToGraph) {
									int next = 1;
									while (!new File(outDir + separator + "run-" + next + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										next++;
									}
									ArrayList<ArrayList<Double>> data;
									if (allData.containsKey(g.getRunNumber() + " "
											+ g.getDirectory())) {
										data = allData.get(g.getRunNumber() + " "
												+ g.getDirectory());
									}
									else {
										data = readData(outDir + separator + "run-1."
												+ printer_id.substring(0, printer_id.length() - 8),
												g.getRunNumber().toLowerCase(), null, false);
										for (int i = 2; i < graphSpecies.size(); i++) {
											String index = graphSpecies.get(i);
											ArrayList<Double> index2 = data.get(i);
											int j = i;
											while ((j > 1)
													&& graphSpecies.get(j - 1).compareToIgnoreCase(
															index) > 0) {
												graphSpecies.set(j, graphSpecies.get(j - 1));
												data.set(j, data.get(j - 1));
												j = j - 1;
											}
											graphSpecies.set(j, index);
											data.set(j, index2);
										}
										allData
												.put(g.getRunNumber() + " " + g.getDirectory(),
														data);
									}
									graphData.add(new XYSeries(g.getSpecies()));
									if (data.size() != 0) {
										for (int i = 0; i < (data.get(0)).size(); i++) {
											graphData.get(graphData.size() - 1).add(
													(data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
								else {
									unableToGraph.add(g);
									thisOne--;
								}
							}
						}
					}
					else {
						thisOne++;
						rend.setSeriesVisible(thisOne, true);
						rend.setSeriesLinesVisible(thisOne, g.getConnected());
						rend.setSeriesShapesFilled(thisOne, g.getFilled());
						rend.setSeriesShapesVisible(thisOne, g.getVisible());
						rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
						rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
						if (!g.getRunNumber().equals("Average")
								&& !g.getRunNumber().equals("Variance")
								&& !g.getRunNumber().equals("Standard Deviation")) {
							if (new File(outDir + separator + g.getDirectory() + separator
									+ g.getRunNumber() + "."
									+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + g.getRunNumber() + "."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber(), g.getDirectory(), false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "mean."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "variance."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ArrayList<ArrayList<Double>> data;
								if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
									data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								}
								else {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "standard_deviation."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
									for (int i = 2; i < graphSpecies.size(); i++) {
										String index = graphSpecies.get(i);
										ArrayList<Double> index2 = data.get(i);
										int j = i;
										while ((j > 1)
												&& graphSpecies.get(j - 1).compareToIgnoreCase(
														index) > 0) {
											graphSpecies.set(j, graphSpecies.get(j - 1));
											data.set(j, data.get(j - 1));
											j = j - 1;
										}
										graphSpecies.set(j, index);
										data.set(j, index2);
									}
									allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
								}
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								boolean ableToGraph = false;
								try {
									for (String s : new File(outDir + separator + g.getDirectory())
											.list()) {
										if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
											ableToGraph = true;
										}
									}
								}
								catch (Exception e1) {
									ableToGraph = false;
								}
								if (ableToGraph) {
									int next = 1;
									while (!new File(outDir + separator + g.getDirectory()
											+ separator + "run-" + next + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										next++;
									}
									ArrayList<ArrayList<Double>> data;
									if (allData.containsKey(g.getRunNumber() + " "
											+ g.getDirectory())) {
										data = allData.get(g.getRunNumber() + " "
												+ g.getDirectory());
									}
									else {
										data = readData(outDir + separator + g.getDirectory()
												+ separator + "run-1."
												+ printer_id.substring(0, printer_id.length() - 8),
												g.getRunNumber().toLowerCase(), g.getDirectory(),
												false);
										for (int i = 2; i < graphSpecies.size(); i++) {
											String index = graphSpecies.get(i);
											ArrayList<Double> index2 = data.get(i);
											int j = i;
											while ((j > 1)
													&& graphSpecies.get(j - 1).compareToIgnoreCase(
															index) > 0) {
												graphSpecies.set(j, graphSpecies.get(j - 1));
												data.set(j, data.get(j - 1));
												j = j - 1;
											}
											graphSpecies.set(j, index);
											data.set(j, index2);
										}
										allData
												.put(g.getRunNumber() + " " + g.getDirectory(),
														data);
									}
									graphData.add(new XYSeries(g.getSpecies()));
									if (data.size() != 0) {
										for (int i = 0; i < (data.get(0)).size(); i++) {
											graphData.get(graphData.size() - 1).add(
													(data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
								else {
									unableToGraph.add(g);
									thisOne--;
								}
							}
						}
					}
				}
				for (GraphSpecies g : unableToGraph) {
					graphed.remove(g);
				}
				XYSeriesCollection dataset = new XYSeriesCollection();
				for (int i = 0; i < graphData.size(); i++) {
					dataset.addSeries(graphData.get(i));
				}
				fixGraph(title.getText().trim(), x.getText().trim(), y.getText().trim(), dataset);
				chart.getXYPlot().setRenderer(rend);
				XYPlot plot = chart.getXYPlot();
				if (resize.isSelected()) {
					resize(dataset);
				}
				else {
					NumberAxis axis = (NumberAxis) plot.getRangeAxis();
					axis.setAutoTickUnitSelection(false);
					axis.setRange(minY, maxY);
					axis.setTickUnit(new NumberTickUnit(scaleY));
					axis = (NumberAxis) plot.getDomainAxis();
					axis.setAutoTickUnitSelection(false);
					axis.setRange(minX, maxX);
					axis.setTickUnit(new NumberTickUnit(scaleX));
				}
			}
			else {
				selected = "";
				int size = graphed.size();
				for (int i = 0; i < size; i++) {
					graphed.remove();
				}
				for (GraphSpecies g : old) {
					graphed.add(g);
				}
			}
			// WindowListener w = new WindowListener() {
			// public void windowClosing(WindowEvent arg0) {
			// cancel.doClick();
			// }
			// public void windowOpened(WindowEvent arg0) {
			// }
			// public void windowClosed(WindowEvent arg0) {
			// }
			// public void windowIconified(WindowEvent arg0) {
			// }
			// public void windowDeiconified(WindowEvent arg0) {
			// }
			// public void windowActivated(WindowEvent arg0) {
			// }
			// public void windowDeactivated(WindowEvent arg0) {
			// }
			// };
			// f.addWindowListener(w);
			// f.setContentPane(all);
			// f.pack();
			// Dimension screenSize;
			// try {
			// Toolkit tk = Toolkit.getDefaultToolkit();
			// screenSize = tk.getScreenSize();
			// }
			// catch (AWTError awe) {
			// screenSize = new Dimension(640, 480);
			// }
			// Dimension frameSize = f.getSize();
			// if (frameSize.height > screenSize.height) {
			// frameSize.height = screenSize.height;
			// }
			// if (frameSize.width > screenSize.width) {
			// frameSize.width = screenSize.width;
			// }
			// int xx = screenSize.width / 2 - frameSize.width / 2;
			// int yy = screenSize.height / 2 - frameSize.height / 2;
			// f.setLocation(xx, yy);
			// f.setVisible(true);
		}
	}

	private void refreshTree() {
		tree = new JTree(simDir);
		if (!topLevel && learnSpecs == null) {
			tree.addMouseListener(this);
		}
		tree.putClientProperty("JTree.icons", makeIcons());
		tree.setCellRenderer(new IconNodeRenderer());
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setLeafIcon(MetalIconFactory.getTreeLeafIcon());
		renderer.setClosedIcon(MetalIconFactory.getTreeFolderIcon());
		renderer.setOpenIcon(MetalIconFactory.getTreeFolderIcon());
	}

	private void addTreeListener() {
		boolean stop = false;
		int selectionRow = 1;
		for (int i = 1; i < tree.getRowCount(); i++) {
			tree.setSelectionRow(i);
			if (selected.equals(lastSelected)) {
				stop = true;
				selectionRow = i;
				break;
			}
		}
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				node = (IconNode) e.getPath().getLastPathComponent();
				if (!directories.contains(node.getName())
						&& node.getParent() != null
						&& !directories.contains(((IconNode) node.getParent()).getName()
								+ separator + node.getName())) {
					selected = node.getName();
					int select;
					if (selected.equals("Average")) {
						select = 0;
					}
					else if (selected.equals("Variance")) {
						select = 1;
					}
					else if (selected.equals("Standard Deviation")) {
						select = 2;
					}
					else if (selected.contains("-run")) {
						select = 0;
					}
					else {
						try {
							if (selected.contains("run-")) {
								select = Integer.parseInt(selected.substring(4)) + 2;
							}
							else {
								select = -1;
							}
						}
						catch (Exception e1) {
							select = -1;
						}
					}
					if (select != -1) {
						specPanel.removeAll();
						if (node.getParent().getParent() != null
								&& directories.contains(((IconNode) node.getParent().getParent())
										.getName()
										+ separator + ((IconNode) node.getParent()).getName())) {
							specPanel.add(fixGraphChoices(((IconNode) node.getParent().getParent())
									.getName()
									+ separator + ((IconNode) node.getParent()).getName()));
						}
						else if (directories.contains(((IconNode) node.getParent()).getName())) {
							specPanel.add(fixGraphChoices(((IconNode) node.getParent()).getName()));
						}
						else {
							specPanel.add(fixGraphChoices(""));
						}
						specPanel.revalidate();
						specPanel.repaint();
						for (int i = 0; i < series.size(); i++) {
							series.get(i).setText(graphSpecies.get(i + 1));
							series.get(i).setSelectionStart(0);
							series.get(i).setSelectionEnd(0);
						}
						for (int i = 0; i < boxes.size(); i++) {
							boxes.get(i).setSelected(false);
						}
						if (node.getParent().getParent() != null
								&& directories.contains(((IconNode) node.getParent().getParent())
										.getName()
										+ separator + ((IconNode) node.getParent()).getName())) {
							for (GraphSpecies g : graphed) {
								if (g.getRunNumber().equals(selected)
										&& g.getDirectory().equals(
												((IconNode) node.getParent().getParent()).getName()
														+ separator
														+ ((IconNode) node.getParent()).getName())) {
									XVariable.setSelectedIndex(g.getXNumber());
									boxes.get(g.getNumber()).setSelected(true);
									series.get(g.getNumber()).setText(g.getSpecies());
									series.get(g.getNumber()).setSelectionStart(0);
									series.get(g.getNumber()).setSelectionEnd(0);
									colorsCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getPaintName());
									shapesCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getShapeName());
									connected.get(g.getNumber()).setSelected(g.getConnected());
									visible.get(g.getNumber()).setSelected(g.getVisible());
									filled.get(g.getNumber()).setSelected(g.getFilled());
								}
							}
						}
						else if (directories.contains(((IconNode) node.getParent()).getName())) {
							for (GraphSpecies g : graphed) {
								if (g.getRunNumber().equals(selected)
										&& g.getDirectory().equals(
												((IconNode) node.getParent()).getName())) {
									XVariable.setSelectedIndex(g.getXNumber());
									boxes.get(g.getNumber()).setSelected(true);
									series.get(g.getNumber()).setText(g.getSpecies());
									series.get(g.getNumber()).setSelectionStart(0);
									series.get(g.getNumber()).setSelectionEnd(0);
									colorsCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getPaintName());
									shapesCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getShapeName());
									connected.get(g.getNumber()).setSelected(g.getConnected());
									visible.get(g.getNumber()).setSelected(g.getVisible());
									filled.get(g.getNumber()).setSelected(g.getFilled());
								}
							}
						}
						else {
							for (GraphSpecies g : graphed) {
								if (g.getRunNumber().equals(selected)
										&& g.getDirectory().equals("")) {
									XVariable.setSelectedIndex(g.getXNumber());
									boxes.get(g.getNumber()).setSelected(true);
									series.get(g.getNumber()).setText(g.getSpecies());
									series.get(g.getNumber()).setSelectionStart(0);
									series.get(g.getNumber()).setSelectionEnd(0);
									colorsCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getPaintName());
									shapesCombo.get(g.getNumber()).setSelectedItem(
											g.getShapeAndPaint().getShapeName());
									connected.get(g.getNumber()).setSelected(g.getConnected());
									visible.get(g.getNumber()).setSelected(g.getVisible());
									filled.get(g.getNumber()).setSelected(g.getFilled());
								}
							}
						}
						boolean allChecked = true;
						boolean allCheckedVisible = true;
						boolean allCheckedFilled = true;
						boolean allCheckedConnected = true;
						for (int i = 0; i < boxes.size(); i++) {
							if (!boxes.get(i).isSelected()) {
								allChecked = false;
								String s = "";
								s = ((IconNode) e.getPath().getLastPathComponent()).toString();
								if (node.getParent().getParent() != null
										&& directories.contains(((IconNode) node.getParent()
												.getParent()).getName()
												+ separator
												+ ((IconNode) node.getParent()).getName())) {
									if (s.equals("Average")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ s + ")";
									}
								}
								else if (directories.contains(((IconNode) node.getParent())
										.getName())) {
									if (s.equals("Average")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ s + ")";
									}
								}
								else {
									if (s.equals("Average")) {
										s = "(" + (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "(" + (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "(" + (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "(" + s + ")";
									}
								}
								String text = graphSpecies.get(i + 1);
								String end = "";
								if (text.length() >= s.length()) {
									for (int j = 0; j < s.length(); j++) {
										end = text.charAt(text.length() - 1 - j) + end;
									}
									if (!s.equals(end)) {
										text += " " + s;
									}
								}
								else {
									text += " " + s;
								}
								boxes.get(i).setName(text);
								series.get(i).setText(text);
								series.get(i).setSelectionStart(0);
								series.get(i).setSelectionEnd(0);
								colorsCombo.get(i).setSelectedIndex(0);
								shapesCombo.get(i).setSelectedIndex(0);
							}
							else {
								String s = "";
								s = ((IconNode) e.getPath().getLastPathComponent()).toString();
								if (node.getParent().getParent() != null
										&& directories.contains(((IconNode) node.getParent()
												.getParent()).getName()
												+ separator
												+ ((IconNode) node.getParent()).getName())) {
									if (s.equals("Average")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ", "
												+ s + ")";
									}
								}
								else if (directories.contains(((IconNode) node.getParent())
										.getName())) {
									if (s.equals("Average")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "(" + ((IconNode) node.getParent()).getName() + ", "
												+ s + ")";
									}
								}
								else {
									if (s.equals("Average")) {
										s = "(" + (char) 967 + ")";
									}
									else if (s.equals("Variance")) {
										s = "(" + (char) 948 + (char) 178 + ")";
									}
									else if (s.equals("Standard Deviation")) {
										s = "(" + (char) 948 + ")";
									}
									else {
										if (s.endsWith("-run")) {
											s = s.substring(0, s.length() - 4);
										}
										else if (s.startsWith("run-")) {
											s = s.substring(4);
										}
										s = "(" + s + ")";
									}
								}
								String text = series.get(i).getText();
								String end = "";
								if (text.length() >= s.length()) {
									for (int j = 0; j < s.length(); j++) {
										end = text.charAt(text.length() - 1 - j) + end;
									}
									if (!s.equals(end)) {
										text += " " + s;
									}
								}
								else {
									text += " " + s;
								}
								boxes.get(i).setName(text);
							}
							if (!visible.get(i).isSelected()) {
								allCheckedVisible = false;
							}
							if (!connected.get(i).isSelected()) {
								allCheckedConnected = false;
							}
							if (!filled.get(i).isSelected()) {
								allCheckedFilled = false;
							}
						}
						if (allChecked) {
							use.setSelected(true);
						}
						else {
							use.setSelected(false);
						}
						if (allCheckedVisible) {
							visibleLabel.setSelected(true);
						}
						else {
							visibleLabel.setSelected(false);
						}
						if (allCheckedFilled) {
							filledLabel.setSelected(true);
						}
						else {
							filledLabel.setSelected(false);
						}
						if (allCheckedConnected) {
							connectedLabel.setSelected(true);
						}
						else {
							connectedLabel.setSelected(false);
						}
					}
				}
				else {
					specPanel.removeAll();
					specPanel.revalidate();
					specPanel.repaint();
				}
			}
		});
		if (!stop) {
			tree.setSelectionRow(0);
			tree.setSelectionRow(1);
		}
		else {
			tree.setSelectionRow(0);
			tree.setSelectionRow(selectionRow);
		}
	}

	private JPanel fixGraphChoices(final String directory) {
		if (directory.equals("")) {
			if (selected.equals("Average") || selected.equals("Variance")
					|| selected.equals("Standard Deviation")) {
				if (selected.equals("Average")
						&& new File(outDir + separator + "mean" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + "mean" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else if (selected.equals("Variance")
						&& new File(outDir + separator + "variance" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + "variance" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else if (selected.equals("Standard Deviation")
						&& new File(outDir + separator + "standard_deviation" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + "standard_deviation" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else {
					int nextOne = 1;
					while (!new File(outDir + separator + "run-" + nextOne + "."
							+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
						nextOne++;
					}
					readGraphSpecies(outDir + separator + "run-" + nextOne + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
			}
			else {
				readGraphSpecies(outDir + separator + selected + "."
						+ printer_id.substring(0, printer_id.length() - 8));
			}
		}
		else {
			if (selected.equals("Average") || selected.equals("Variance")
					|| selected.equals("Standard Deviation")) {
				if (selected.equals("Average")
						&& new File(outDir + separator + directory + separator + "mean" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + directory + separator + "mean" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else if (selected.equals("Variance")
						&& new File(outDir + separator + directory + separator + "variance" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + directory + separator + "variance" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else if (selected.equals("Standard Deviation")
						&& new File(outDir + separator + directory + separator
								+ "standard_deviation" + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					readGraphSpecies(outDir + separator + directory + separator
							+ "standard_deviation" + "."
							+ printer_id.substring(0, printer_id.length() - 8));
				}
				else {
					int nextOne = 1;
					while (!new File(outDir + separator + directory + separator + "run-" + nextOne
							+ "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
						nextOne++;
					}
					readGraphSpecies(outDir + separator + directory + separator + "run-" + nextOne
							+ "." + printer_id.substring(0, printer_id.length() - 8));
				}
			}
			else {
				readGraphSpecies(outDir + separator + directory + separator + selected + "."
						+ printer_id.substring(0, printer_id.length() - 8));
			}
		}
		for (int i = 2; i < graphSpecies.size(); i++) {
			String index = graphSpecies.get(i);
			int j = i;
			while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
				graphSpecies.set(j, graphSpecies.get(j - 1));
				j = j - 1;
			}
			graphSpecies.set(j, index);
		}
		updateXNumber = false;
		XVariable.removeAllItems();
		for (int i = 0; i < graphSpecies.size(); i++) {
			XVariable.addItem(graphSpecies.get(i));
		}
		updateXNumber = true;
		JPanel speciesPanel1 = new JPanel(new GridLayout(graphSpecies.size(), 1));
		JPanel speciesPanel2 = new JPanel(new GridLayout(graphSpecies.size(), 3));
		JPanel speciesPanel3 = new JPanel(new GridLayout(graphSpecies.size(), 3));
		use = new JCheckBox("Use");
		JLabel specs;
		if (biomodelsim.lema || biomodelsim.atacs) {
			specs = new JLabel("Variables");
		}
		else {
			specs = new JLabel("Species");
		}
		JLabel color = new JLabel("Color");
		JLabel shape = new JLabel("Shape");
		connectedLabel = new JCheckBox("Connect");
		visibleLabel = new JCheckBox("Visible");
		filledLabel = new JCheckBox("Fill");
		connectedLabel.setSelected(true);
		visibleLabel.setSelected(true);
		filledLabel.setSelected(true);
		boxes = new ArrayList<JCheckBox>();
		series = new ArrayList<JTextField>();
		colorsCombo = new ArrayList<JComboBox>();
		shapesCombo = new ArrayList<JComboBox>();
		connected = new ArrayList<JCheckBox>();
		visible = new ArrayList<JCheckBox>();
		filled = new ArrayList<JCheckBox>();
		use.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (use.isSelected()) {
					for (JCheckBox box : boxes) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				}
				else {
					for (JCheckBox box : boxes) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		connectedLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (connectedLabel.isSelected()) {
					for (JCheckBox box : connected) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				}
				else {
					for (JCheckBox box : connected) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		visibleLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (visibleLabel.isSelected()) {
					for (JCheckBox box : visible) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				}
				else {
					for (JCheckBox box : visible) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		filledLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (filledLabel.isSelected()) {
					for (JCheckBox box : filled) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				}
				else {
					for (JCheckBox box : filled) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		speciesPanel1.add(use);
		speciesPanel2.add(specs);
		speciesPanel2.add(color);
		speciesPanel2.add(shape);
		speciesPanel3.add(connectedLabel);
		speciesPanel3.add(visibleLabel);
		speciesPanel3.add(filledLabel);
		final HashMap<String, Shape> shapey = this.shapes;
		final HashMap<String, Paint> colory = this.colors;
		for (int i = 0; i < graphSpecies.size() - 1; i++) {
			JCheckBox temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					if (((JCheckBox) e.getSource()).isSelected()) {
						node.setIcon(TextIcons.getIcon("g"));
						node.setIconName("" + (char) 10003);
						IconNode n = ((IconNode) node.getParent());
						while (n != null) {
							n.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
							n.setIconName("" + (char) 10003);
							if (n.getParent() == null) {
								n = null;
							}
							else {
								n = ((IconNode) n.getParent());
							}
						}
						tree.revalidate();
						tree.repaint();
						String s = series.get(i).getText();
						((JCheckBox) e.getSource()).setSelected(false);
						int[] cols = new int[34];
						int[] shaps = new int[10];
						for (int k = 0; k < boxes.size(); k++) {
							if (boxes.get(k).isSelected()) {
								if (colorsCombo.get(k).getSelectedItem().equals("Red")) {
									cols[0]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue")) {
									cols[1]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Green")) {
									cols[2]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Yellow")) {
									cols[3]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Magenta")) {
									cols[4]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan")) {
									cols[5]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Tan")) {
									cols[6]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Gray (Dark)")) {
									cols[7]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red (Dark)")) {
									cols[8]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue (Dark)")) {
									cols[9]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Green (Dark)")) {
									cols[10]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Dark)")) {
									cols[11]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Dark)")) {
									cols[12]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan (Dark)")) {
									cols[13]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Black")) {
									cols[14]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red ")) {
									cols[15]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue ")) {
									cols[16]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Green ")) {
									cols[17]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Yellow ")) {
									cols[18]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Magenta ")) {
									cols[19]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan ")) {
									cols[20]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Gray (Light)")) {
									cols[21]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Red (Extra Dark)")) {
									cols[22]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Blue (Extra Dark)")) {
									cols[23]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Extra Dark)")) {
									cols[24]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Extra Dark)")) {
									cols[25]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Extra Dark)")) {
									cols[26]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Cyan (Extra Dark)")) {
									cols[27]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red (Light)")) {
									cols[28]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Blue (Light)")) {
									cols[29]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Light)")) {
									cols[30]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Light)")) {
									cols[31]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Light)")) {
									cols[32]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Cyan (Light)")) {
									cols[33]++;
								}
								if (shapesCombo.get(k).getSelectedItem().equals("Square")) {
									shaps[0]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals("Circle")) {
									shaps[1]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals("Triangle")) {
									shaps[2]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals("Diamond")) {
									shaps[3]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals(
										"Rectangle (Horizontal)")) {
									shaps[4]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals(
										"Triangle (Upside Down)")) {
									shaps[5]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals(
										"Circle (Half)")) {
									shaps[6]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals("Arrow")) {
									shaps[7]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals(
										"Rectangle (Vertical)")) {
									shaps[8]++;
								}
								else if (shapesCombo.get(k).getSelectedItem().equals(
										"Arrow (Backwards)")) {
									shaps[9]++;
								}
							}
						}
						for (GraphSpecies graph : graphed) {
							if (graph.getShapeAndPaint().getPaintName().equals("Red")) {
								cols[0]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Blue")) {
								cols[1]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Green")) {
								cols[2]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Yellow")) {
								cols[3]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Magenta")) {
								cols[4]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Cyan")) {
								cols[5]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Tan")) {
								cols[6]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Gray (Dark)")) {
								cols[7]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Red (Dark)")) {
								cols[8]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Blue (Dark)")) {
								cols[9]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Green (Dark)")) {
								cols[10]++;
							}
							else if (graph.getShapeAndPaint().getPaintName()
									.equals("Yellow (Dark)")) {
								cols[11]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Dark)")) {
								cols[12]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Cyan (Dark)")) {
								cols[13]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Black")) {
								cols[14]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Red ")) {
								cols[15]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Blue ")) {
								cols[16]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Green ")) {
								cols[17]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Yellow ")) {
								cols[18]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Magenta ")) {
								cols[19]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Cyan ")) {
								cols[20]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Gray (Light)")) {
								cols[21]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Red (Extra Dark)")) {
								cols[22]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Blue (Extra Dark)")) {
								cols[23]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Green (Extra Dark)")) {
								cols[24]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Yellow (Extra Dark)")) {
								cols[25]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Extra Dark)")) {
								cols[26]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Cyan (Extra Dark)")) {
								cols[27]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Red (Light)")) {
								cols[28]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Blue (Light)")) {
								cols[29]++;
							}
							else if (graph.getShapeAndPaint().getPaintName()
									.equals("Green (Light)")) {
								cols[30]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Yellow (Light)")) {
								cols[31]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Light)")) {
								cols[32]++;
							}
							else if (graph.getShapeAndPaint().getPaintName().equals("Cyan (Light)")) {
								cols[33]++;
							}
							if (graph.getShapeAndPaint().getShapeName().equals("Square")) {
								shaps[0]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals("Circle")) {
								shaps[1]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals("Triangle")) {
								shaps[2]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals("Diamond")) {
								shaps[3]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals(
									"Rectangle (Horizontal)")) {
								shaps[4]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals(
									"Triangle (Upside Down)")) {
								shaps[5]++;
							}
							else if (graph.getShapeAndPaint().getShapeName()
									.equals("Circle (Half)")) {
								shaps[6]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals("Arrow")) {
								shaps[7]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals(
									"Rectangle (Vertical)")) {
								shaps[8]++;
							}
							else if (graph.getShapeAndPaint().getShapeName().equals(
									"Arrow (Backwards)")) {
								shaps[9]++;
							}
						}
						((JCheckBox) e.getSource()).setSelected(true);
						series.get(i).setText(s);
						series.get(i).setSelectionStart(0);
						series.get(i).setSelectionEnd(0);
						int colorSet = 0;
						for (int j = 1; j < cols.length; j++) {
							if (cols[j] < cols[colorSet]) {
								colorSet = j;
							}
						}
						int shapeSet = 0;
						for (int j = 1; j < shaps.length; j++) {
							if (shaps[j] < shaps[shapeSet]) {
								shapeSet = j;
							}
						}
						DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
						for (int j = 0; j < colorSet; j++) {
							draw.getNextPaint();
						}
						Paint paint = draw.getNextPaint();
						Object[] set = colory.keySet().toArray();
						for (int j = 0; j < set.length; j++) {
							if (paint == colory.get(set[j])) {
								colorsCombo.get(i).setSelectedItem(set[j]);
							}
						}
						for (int j = 0; j < shapeSet; j++) {
							draw.getNextShape();
						}
						Shape shape = draw.getNextShape();
						set = shapey.keySet().toArray();
						for (int j = 0; j < set.length; j++) {
							if (shape == shapey.get(set[j])) {
								shapesCombo.get(i).setSelectedItem(set[j]);
							}
						}
						boolean allChecked = true;
						for (JCheckBox temp : boxes) {
							if (!temp.isSelected()) {
								allChecked = false;
							}
						}
						if (allChecked) {
							use.setSelected(true);
						}
						graphed.add(new GraphSpecies(shapey.get(shapesCombo.get(i)
								.getSelectedItem()), colory.get(colorsCombo.get(i)
								.getSelectedItem()), filled.get(i).isSelected(), visible.get(i)
								.isSelected(), connected.get(i).isSelected(), selected, boxes
								.get(i).getName(), series.get(i).getText().trim(), XVariable
								.getSelectedIndex(), i, directory));
					}
					else {
						boolean check = false;
						for (JCheckBox b : boxes) {
							if (b.isSelected()) {
								check = true;
							}
						}
						if (!check) {
							node.setIcon(MetalIconFactory.getTreeLeafIcon());
							node.setIconName("");
							boolean check2 = false;
							IconNode parent = ((IconNode) node.getParent());
							while (parent != null) {
								for (int j = 0; j < parent.getChildCount(); j++) {
									if (((IconNode) parent.getChildAt(j)).getIconName().equals(
											"" + (char) 10003)) {
										check2 = true;
									}
								}
								if (!check2) {
									parent.setIcon(MetalIconFactory.getTreeFolderIcon());
									parent.setIconName("");
								}
								check2 = false;
								if (parent.getParent() == null) {
									parent = null;
								}
								else {
									parent = ((IconNode) parent.getParent());
								}
							}
							tree.revalidate();
							tree.repaint();
						}
						ArrayList<GraphSpecies> remove = new ArrayList<GraphSpecies>();
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								remove.add(g);
							}
						}
						for (GraphSpecies g : remove) {
							graphed.remove(g);
						}
						use.setSelected(false);
						colorsCombo.get(i).setSelectedIndex(0);
						shapesCombo.get(i).setSelectedIndex(0);
					}
				}
			});
			boxes.add(temp);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					if (((JCheckBox) e.getSource()).isSelected()) {
						boolean allChecked = true;
						for (JCheckBox temp : visible) {
							if (!temp.isSelected()) {
								allChecked = false;
							}
						}
						if (allChecked) {
							visibleLabel.setSelected(true);
						}
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setVisible(true);
							}
						}
					}
					else {
						visibleLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setVisible(false);
							}
						}
					}
				}
			});
			visible.add(temp);
			visible.get(i).setSelected(true);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					if (((JCheckBox) e.getSource()).isSelected()) {
						boolean allChecked = true;
						for (JCheckBox temp : filled) {
							if (!temp.isSelected()) {
								allChecked = false;
							}
						}
						if (allChecked) {
							filledLabel.setSelected(true);
						}
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setFilled(true);
							}
						}
					}
					else {
						filledLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setFilled(false);
							}
						}
					}
				}
			});
			filled.add(temp);
			filled.get(i).setSelected(true);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					if (((JCheckBox) e.getSource()).isSelected()) {
						boolean allChecked = true;
						for (JCheckBox temp : connected) {
							if (!temp.isSelected()) {
								allChecked = false;
							}
						}
						if (allChecked) {
							connectedLabel.setSelected(true);
						}
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setConnected(true);
							}
						}
					}
					else {
						connectedLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setConnected(false);
							}
						}
					}
				}
			});
			connected.add(temp);
			connected.get(i).setSelected(true);
			JTextField seriesName = new JTextField(graphSpecies.get(i + 1), 20);
			seriesName.setName("" + i);
			seriesName.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyReleased(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyTyped(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}
			});
			series.add(seriesName);
			Object[] col = this.colors.keySet().toArray();
			Arrays.sort(col);
			Object[] shap = this.shapes.keySet().toArray();
			Arrays.sort(shap);
			JComboBox colBox = new JComboBox(col);
			colBox.setActionCommand("" + i);
			colBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setPaint((String) ((JComboBox) e.getSource()).getSelectedItem());
						}
					}
				}
			});
			JComboBox shapBox = new JComboBox(shap);
			shapBox.setActionCommand("" + i);
			shapBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setShape((String) ((JComboBox) e.getSource()).getSelectedItem());
						}
					}
				}
			});
			colorsCombo.add(colBox);
			shapesCombo.add(shapBox);
			speciesPanel1.add(boxes.get(i));
			speciesPanel2.add(series.get(i));
			speciesPanel2.add(colorsCombo.get(i));
			speciesPanel2.add(shapesCombo.get(i));
			speciesPanel3.add(connected.get(i));
			speciesPanel3.add(visible.get(i));
			speciesPanel3.add(filled.get(i));
		}
		JPanel speciesPanel = new JPanel(new BorderLayout());
		speciesPanel.add(speciesPanel1, "West");
		speciesPanel.add(speciesPanel2, "Center");
		speciesPanel.add(speciesPanel3, "East");
		return speciesPanel;
	}

	private void fixGraph(String title, String x, String y, XYSeriesCollection dataset) {
		curData = dataset;
		chart = ChartFactory.createXYLineChart(title, x, y, dataset, PlotOrientation.VERTICAL,
				true, true, false);
		chart.setBackgroundPaint(java.awt.Color.WHITE);
		chart.addProgressListener(this);
		ChartPanel graph = new ChartPanel(chart);
		if (graphed.isEmpty()) {
			graph.setLayout(new GridLayout(1, 1));
			JLabel edit = new JLabel("Double click here to create graph");
			edit.addMouseListener(this);
			Font font = edit.getFont();
			font = font.deriveFont(Font.BOLD, 42.0f);
			edit.setFont(font);
			edit.setHorizontalAlignment(SwingConstants.CENTER);
			graph.add(edit);
		}
		graph.addMouseListener(this);
		JPanel ButtonHolder = new JPanel();
		run = new JButton("Save and Run");
		save = new JButton("Save Graph");
		saveAs = new JButton("Save As");
		saveAs.addActionListener(this);
		export = new JButton("Export");
		refresh = new JButton("Refresh");
		// exportJPeg = new JButton("Export As JPEG");
		// exportPng = new JButton("Export As PNG");
		// exportPdf = new JButton("Export As PDF");
		// exportEps = new JButton("Export As EPS");
		// exportSvg = new JButton("Export As SVG");
		// exportCsv = new JButton("Export As CSV");
		run.addActionListener(this);
		save.addActionListener(this);
		export.addActionListener(this);
		refresh.addActionListener(this);
		// exportJPeg.addActionListener(this);
		// exportPng.addActionListener(this);
		// exportPdf.addActionListener(this);
		// exportEps.addActionListener(this);
		// exportSvg.addActionListener(this);
		// exportCsv.addActionListener(this);
		if (reb2sac != null) {
			ButtonHolder.add(run);
		}
		ButtonHolder.add(save);
		ButtonHolder.add(saveAs);
		ButtonHolder.add(export);
		ButtonHolder.add(refresh);
		// ButtonHolder.add(exportJPeg);
		// ButtonHolder.add(exportPng);
		// ButtonHolder.add(exportPdf);
		// ButtonHolder.add(exportEps);
		// ButtonHolder.add(exportSvg);
		// ButtonHolder.add(exportCsv);
		// JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// ButtonHolder, null);
		// splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		// this.add(splitPane, "South");
		this.revalidate();
	}

	/**
	 * This method saves the graph as a jpeg or as a png file.
	 */
	public void export() {
		try {
			int output = 2; /* Default is currently pdf */
			int width = -1;
			int height = -1;
			JPanel sizePanel = new JPanel(new GridLayout(2, 2));
			JLabel heightLabel = new JLabel("Desired pixel height:");
			JLabel widthLabel = new JLabel("Desired pixel width:");
			JTextField heightField = new JTextField("400");
			JTextField widthField = new JTextField("650");
			sizePanel.add(widthLabel);
			sizePanel.add(widthField);
			sizePanel.add(heightLabel);
			sizePanel.add(heightField);
			Object[] options2 = { "Export", "Cancel" };
			int value;
			String export = "Export";
			if (timeSeries) {
				export += " TSD";
			}
			else {
				export += " Probability";
			}
			File file;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
				file = null;
			}
			else {
				file = new File(biosimrc.get("biosim.general.export_dir", ""));
			}
			String filename = Buttons.browse(biomodelsim.frame(), file, null,
					JFileChooser.FILES_ONLY, export, output);
			if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".jpg"))) {
				output = 0;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".png"))) {
				output = 1;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".pdf"))) {
				output = 2;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".eps"))) {
				output = 3;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".svg"))) {
				output = 4;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".csv"))) {
				output = 5;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".dat"))) {
				output = 6;
			}
			else if ((filename.length() > 4)
					&& (filename.substring((filename.length() - 4), filename.length())
							.equals(".tsd"))) {
				output = 7;
			}
			if (!filename.equals("")) {
				file = new File(filename);
				biosimrc.put("biosim.general.export_dir", filename);
				boolean exportIt = true;
				if (file.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + " Overwrite?", "File Already Exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					exportIt = false;
					if (value == JOptionPane.YES_OPTION) {
						exportIt = true;
					}
				}
				if (exportIt) {
					if ((output != 5) && (output != 6) && (output != 7)) {
						value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
								"Enter Size Of File", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
						if (value == JOptionPane.YES_OPTION) {
							while (value == JOptionPane.YES_OPTION && (width == -1 || height == -1))
								try {
									width = Integer.parseInt(widthField.getText().trim());
									height = Integer.parseInt(heightField.getText().trim());
									if (width < 1 || height < 1) {
										JOptionPane.showMessageDialog(biomodelsim.frame(),
												"Width and height must be positive integers!",
												"Error", JOptionPane.ERROR_MESSAGE);
										width = -1;
										height = -1;
										value = JOptionPane.showOptionDialog(biomodelsim.frame(),
												sizePanel, "Enter Size Of File",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, options2,
												options2[0]);
									}
								}
								catch (Exception e2) {
									JOptionPane.showMessageDialog(biomodelsim.frame(),
											"Width and height must be positive integers!", "Error",
											JOptionPane.ERROR_MESSAGE);
									width = -1;
									height = -1;
									value = JOptionPane.showOptionDialog(biomodelsim.frame(),
											sizePanel, "Enter Size Of File",
											JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
											null, options2, options2[0]);
								}
						}
						if (value == JOptionPane.NO_OPTION) {
							return;
						}
					}
					if (output == 0) {
						ChartUtilities.saveChartAsJPEG(file, chart, width, height);
					}
					else if (output == 1) {
						ChartUtilities.saveChartAsPNG(file, chart, width, height);
					}
					else if (output == 2) {
						Rectangle pagesize = new Rectangle(width, height);
						Document document = new Document(pagesize, 50, 50, 50, 50);
						FileOutputStream out = new FileOutputStream(file);
						PdfWriter writer = PdfWriter.getInstance(document, out);
						document.open();
						PdfContentByte cb = writer.getDirectContent();
						PdfTemplate tp = cb.createTemplate(width, height);
						Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
						chart.draw(g2, new java.awt.Rectangle(width, height));
						g2.dispose();
						cb.addTemplate(tp, 0, 0);
						document.close();
						out.close();
					}
					else if (output == 3) {
						Graphics2D g = new EpsGraphics2D();
						chart.draw(g, new java.awt.Rectangle(width, height));
						Writer out = new FileWriter(file);
						out.write(g.toString());
						out.close();
					}
					else if (output == 4) {
						DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
						org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
						SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
						chart.draw(svgGenerator, new java.awt.Rectangle(width, height));
						boolean useCSS = true;
						FileOutputStream outStream = new FileOutputStream(file);
						Writer out = new OutputStreamWriter(outStream, "UTF-8");
						svgGenerator.stream(out, useCSS);
						out.close();
						outStream.close();
					}
					else if ((output == 5) || (output == 6) || (output == 7)) {
						exportDataFile(file, output);
					}
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Export File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void export(int output) {
		// jpg = 0
		// png = 1
		// pdf = 2
		// eps = 3
		// svg = 4
		// csv = 5 (data)
		// dat = 6 (data)
		// tsd = 7 (data)
		try {
			int width = -1;
			int height = -1;
			JPanel sizePanel = new JPanel(new GridLayout(2, 2));
			JLabel heightLabel = new JLabel("Desired pixel height:");
			JLabel widthLabel = new JLabel("Desired pixel width:");
			JTextField heightField = new JTextField("400");
			JTextField widthField = new JTextField("650");
			sizePanel.add(widthLabel);
			sizePanel.add(widthField);
			sizePanel.add(heightLabel);
			sizePanel.add(heightField);
			Object[] options2 = { "Export", "Cancel" };
			int value;
			String export = "Export";
			if (timeSeries) {
				export += " TSD";
			}
			else {
				export += " Probability";
			}
			File file;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
				file = null;
			}
			else {
				file = new File(biosimrc.get("biosim.general.export_dir", ""));
			}
			String filename = Buttons.browse(biomodelsim.frame(), file, null,
					JFileChooser.FILES_ONLY, export, output);
			if (!filename.equals("")) {
				file = new File(filename);
				biosimrc.put("biosim.general.export_dir", filename);
				boolean exportIt = true;
				if (file.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + " Overwrite?", "File Already Exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					exportIt = false;
					if (value == JOptionPane.YES_OPTION) {
						exportIt = true;
					}
				}
				if (exportIt) {
					if ((output != 5) && (output != 6) && (output != 7)) {
						value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
								"Enter Size Of File", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
						if (value == JOptionPane.YES_OPTION) {
							while (value == JOptionPane.YES_OPTION && (width == -1 || height == -1))
								try {
									width = Integer.parseInt(widthField.getText().trim());
									height = Integer.parseInt(heightField.getText().trim());
									if (width < 1 || height < 1) {
										JOptionPane.showMessageDialog(biomodelsim.frame(),
												"Width and height must be positive integers!",
												"Error", JOptionPane.ERROR_MESSAGE);
										width = -1;
										height = -1;
										value = JOptionPane.showOptionDialog(biomodelsim.frame(),
												sizePanel, "Enter Size Of File",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, options2,
												options2[0]);
									}
								}
								catch (Exception e2) {
									JOptionPane.showMessageDialog(biomodelsim.frame(),
											"Width and height must be positive integers!", "Error",
											JOptionPane.ERROR_MESSAGE);
									width = -1;
									height = -1;
									value = JOptionPane.showOptionDialog(biomodelsim.frame(),
											sizePanel, "Enter Size Of File",
											JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
											null, options2, options2[0]);
								}
						}
						if (value == JOptionPane.NO_OPTION) {
							return;
						}
					}
					if (output == 0) {
						ChartUtilities.saveChartAsJPEG(file, chart, width, height);
					}
					else if (output == 1) {
						ChartUtilities.saveChartAsPNG(file, chart, width, height);
					}
					else if (output == 2) {
						Rectangle pagesize = new Rectangle(width, height);
						Document document = new Document(pagesize, 50, 50, 50, 50);
						FileOutputStream out = new FileOutputStream(file);
						PdfWriter writer = PdfWriter.getInstance(document, out);
						document.open();
						PdfContentByte cb = writer.getDirectContent();
						PdfTemplate tp = cb.createTemplate(width, height);
						Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
						chart.draw(g2, new java.awt.Rectangle(width, height));
						g2.dispose();
						cb.addTemplate(tp, 0, 0);
						document.close();
						out.close();
					}
					else if (output == 3) {
						Graphics2D g = new EpsGraphics2D();
						chart.draw(g, new java.awt.Rectangle(width, height));
						Writer out = new FileWriter(file);
						out.write(g.toString());
						out.close();
					}
					else if (output == 4) {
						DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
						org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
						SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
						chart.draw(svgGenerator, new java.awt.Rectangle(width, height));
						boolean useCSS = true;
						FileOutputStream outStream = new FileOutputStream(file);
						Writer out = new OutputStreamWriter(outStream, "UTF-8");
						svgGenerator.stream(out, useCSS);
						out.close();
						outStream.close();
					}
					else if ((output == 5) || (output == 6) || (output == 7)) {
						exportDataFile(file, output);
					}
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Export File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void exportDataFile(File file, int output) {
		try {
			int count = curData.getSeries(0).getItemCount();
			for (int i = 1; i < curData.getSeriesCount(); i++) {
				if (curData.getSeries(i).getItemCount() != count) {
					JOptionPane.showMessageDialog(biomodelsim.frame(),
							"Data series do not have the same number of points!",
							"Unable to Export Data File", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			for (int j = 0; j < count; j++) {
				Number Xval = curData.getSeries(0).getDataItem(j).getX();
				for (int i = 1; i < curData.getSeriesCount(); i++) {
					if (!curData.getSeries(i).getDataItem(j).getX().equals(Xval)) {
						JOptionPane.showMessageDialog(biomodelsim.frame(),
								"Data series time points are not the same!",
								"Unable to Export Data File", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			FileOutputStream csvFile = new FileOutputStream(file);
			PrintWriter csvWriter = new PrintWriter(csvFile);
			if (output == 7) {
				csvWriter.print("((");
			}
			else if (output == 6) {
				csvWriter.print("#");
			}
			csvWriter.print("\"Time\"");
			count = curData.getSeries(0).getItemCount();
			int pos = 0;
			for (int i = 0; i < curData.getSeriesCount(); i++) {
				if (output == 6) {
					csvWriter.print(" ");
				}
				else {
					csvWriter.print(",");
				}
				csvWriter.print("\"" + curData.getSeriesKey(i) + "\"");
				if (curData.getSeries(i).getItemCount() > count) {
					count = curData.getSeries(i).getItemCount();
					pos = i;
				}
			}
			if (output == 7) {
				csvWriter.print(")");
			}
			else {
				csvWriter.println("");
			}
			for (int j = 0; j < count; j++) {
				if (output == 7) {
					csvWriter.print(",");
				}
				for (int i = 0; i < curData.getSeriesCount(); i++) {
					if (i == 0) {
						if (output == 7) {
							csvWriter.print("(");
						}
						csvWriter.print(curData.getSeries(pos).getDataItem(j).getX());
					}
					XYSeries data = curData.getSeries(i);
					if (j < data.getItemCount()) {
						XYDataItem item = data.getDataItem(j);
						if (output == 6) {
							csvWriter.print(" ");
						}
						else {
							csvWriter.print(",");
						}
						csvWriter.print(item.getY());
					}
					else {
						if (output == 6) {
							csvWriter.print(" ");
						}
						else {
							csvWriter.print(",");
						}
					}
				}
				if (output == 7) {
					csvWriter.print(")");
				}
				else {
					csvWriter.println("");
				}
			}
			if (output == 7) {
				csvWriter.println(")");
			}
			csvWriter.close();
			csvFile.close();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Export File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private ArrayList<ArrayList<Double>> calculateAverageVarianceDeviation(String startFile,
			String fileStem, int choice, String directory, boolean warning) {
		warn = warning;
		ArrayList<ArrayList<Double>> average = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> variance = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> deviation = new ArrayList<ArrayList<Double>>();
		// TSDParser p = new TSDParser(startFile, biomodelsim, false);
		ArrayList<ArrayList<Double>> data = readData(startFile, "", directory, warn);
		averageOrder = graphSpecies;
		boolean first = true;
		int runsToMake = 1;
		String[] findNum = startFile.split(separator);
		String search = findNum[findNum.length - 1];
		int firstOne = Integer.parseInt(search.substring(4, search.length() - 4));
		if (directory == null) {
			for (String f : new File(outDir).list()) {
				if (f.contains(fileStem)) {
					try {
						int tempNum = Integer.parseInt(f.substring(fileStem.length(),
								f.length() - 4));
						if (tempNum > runsToMake) {
							runsToMake = tempNum;
						}
					}
					catch (Exception e) {
					}
				}
			}
		}
		else {
			for (String f : new File(outDir + separator + directory).list()) {
				if (f.contains(fileStem)) {
					try {
						int tempNum = Integer.parseInt(f.substring(fileStem.length(),
								f.length() - 4));
						if (tempNum > runsToMake) {
							runsToMake = tempNum;
						}
					}
					catch (Exception e) {
					}
				}
			}
		}
		for (int i = 0; i < graphSpecies.size(); i++) {
			average.add(new ArrayList<Double>());
			variance.add(new ArrayList<Double>());
		}
		HashMap<Double, Integer> dataCounts = new HashMap<Double, Integer>();
		// int count = 0;
		int skip = firstOne;
		for (int j = 0; j < runsToMake; j++) {
			if (!first) {
				if (firstOne != 1) {
					j--;
					firstOne = 1;
				}
				boolean loop = true;
				while (loop && j < runsToMake && (j + 1) != skip) {
					if (directory == null) {
						if (new File(outDir + separator + fileStem + (j + 1) + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							ArrayList<ArrayList<Double>> newData = readData(outDir + separator
									+ fileStem + (j + 1) + "."
									+ printer_id.substring(0, printer_id.length() - 8), "",
									directory, warn);
							data = new ArrayList<ArrayList<Double>>();
							for (int i = 0; i < averageOrder.size(); i++) {
								for (int k = 0; k < graphSpecies.size(); k++) {
									if (averageOrder.get(i).equals(graphSpecies.get(k))) {
										data.add(newData.get(k));
										break;
									}
								}
							}
							loop = false;
							// count++;
						}
						else {
							j++;
						}
					}
					else {
						if (new File(outDir + separator + directory + separator + fileStem
								+ (j + 1) + "." + printer_id.substring(0, printer_id.length() - 8))
								.exists()) {
							ArrayList<ArrayList<Double>> newData = readData(outDir + separator
									+ directory + separator + fileStem + (j + 1) + "."
									+ printer_id.substring(0, printer_id.length() - 8), "",
									directory, warn);
							data = new ArrayList<ArrayList<Double>>();
							for (int i = 0; i < averageOrder.size(); i++) {
								for (int k = 0; k < graphSpecies.size(); k++) {
									if (averageOrder.get(i).equals(graphSpecies.get(k))) {
										data.add(newData.get(k));
										break;
									}
								}
							}
							loop = false;
							// count++;
						}
						else {
							j++;
						}
					}
				}
			}
			// ArrayList<ArrayList<Double>> data = p.getData();
			for (int k = 0; k < data.get(0).size(); k++) {
				if (first) {
					double put;
					if (k == data.get(0).size() - 1 && k >= 2) {
						if (data.get(0).get(k) - data.get(0).get(k - 1) != data.get(0).get(k - 1)
								- data.get(0).get(k - 2)) {
							put = data.get(0).get(k - 1)
									+ (data.get(0).get(k - 1) - data.get(0).get(k - 2));
							dataCounts.put(put, 1);
						}
						else {
							put = (data.get(0)).get(k);
							dataCounts.put(put, 1);
						}
					}
					else {
						put = (data.get(0)).get(k);
						dataCounts.put(put, 1);
					}
					for (int i = 0; i < data.size(); i++) {
						if (i == 0) {
							variance.get(i).add((data.get(i)).get(k));
							average.get(i).add(put);
						}
						else {
							variance.get(i).add(0.0);
							average.get(i).add((data.get(i)).get(k));
						}
					}
				}
				else {
					int index = -1;
					double put;
					if (k == data.get(0).size() - 1 && k >= 2) {
						if (data.get(0).get(k) - data.get(0).get(k - 1) != data.get(0).get(k - 1)
								- data.get(0).get(k - 2)) {
							put = data.get(0).get(k - 1)
									+ (data.get(0).get(k - 1) - data.get(0).get(k - 2));
						}
						else {
							put = (data.get(0)).get(k);
						}
					}
					else if (k == data.get(0).size() - 1 && k == 1) {
						if (average.get(0).size() > 1) {
							put = (average.get(0)).get(k);
						}
						else {
							put = (data.get(0)).get(k);
						}
					}
					else {
						put = (data.get(0)).get(k);
					}
					if (average.get(0).contains(put)) {
						index = average.get(0).indexOf(put);
						int count = dataCounts.get(put);
						dataCounts.put(put, count + 1);
						for (int i = 1; i < data.size(); i++) {
							double old = (average.get(i)).get(index);
							(average.get(i)).set(index, old
									+ (((data.get(i)).get(k) - old) / (count + 1)));
							double newMean = (average.get(i)).get(index);
							double vary = (((count - 1) * (variance.get(i)).get(index)) + ((data
									.get(i)).get(k) - newMean)
									* ((data.get(i)).get(k) - old))
									/ count;
							(variance.get(i)).set(index, vary);
						}
					}
					else {
						dataCounts.put(put, 1);
						for (int a = 0; a < average.get(0).size(); a++) {
							if (average.get(0).get(a) > put) {
								index = a;
								break;
							}
						}
						if (index == -1) {
							index = average.get(0).size() - 1;
						}
						average.get(0).add(put);
						variance.get(0).add(put);
						for (int a = 1; a < average.size(); a++) {
							average.get(a).add(data.get(a).get(k));
							variance.get(a).add(0.0);
						}
						if (index != average.get(0).size() - 1) {
							for (int a = average.get(0).size() - 2; a >= 0; a--) {
								if (average.get(0).get(a) > average.get(0).get(a + 1)) {
									for (int b = 0; b < average.size(); b++) {
										double temp = average.get(b).get(a);
										average.get(b).set(a, average.get(b).get(a + 1));
										average.get(b).set(a + 1, temp);
										temp = variance.get(b).get(a);
										variance.get(b).set(a, variance.get(b).get(a + 1));
										variance.get(b).set(a + 1, temp);
									}
								}
								else {
									break;
								}
							}
						}
					}
				}
			}
			first = false;
		}
		deviation = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < variance.size(); i++) {
			deviation.add(new ArrayList<Double>());
			for (int j = 0; j < variance.get(i).size(); j++) {
				deviation.get(i).add(variance.get(i).get(j));
			}
		}
		for (int i = 1; i < deviation.size(); i++) {
			for (int j = 0; j < deviation.get(i).size(); j++) {
				deviation.get(i).set(j, Math.sqrt(deviation.get(i).get(j)));
			}
		}
		averageOrder = null;
		if (choice == 0) {
			return average;
		}
		else if (choice == 1) {
			return variance;
		}
		else {
			return deviation;
		}
	}

	public void run() {
		reb2sac.getRunButton().doClick();
	}

	public void save() {
		if (timeSeries) {
			Properties graph = new Properties();
			graph.setProperty("title", chart.getTitle().getText());
			graph.setProperty("x.axis", chart.getXYPlot().getDomainAxis().getLabel());
			graph.setProperty("y.axis", chart.getXYPlot().getRangeAxis().getLabel());
			graph.setProperty("x.min", XMin.getText());
			graph.setProperty("x.max", XMax.getText());
			graph.setProperty("x.scale", XScale.getText());
			graph.setProperty("y.min", YMin.getText());
			graph.setProperty("y.max", YMax.getText());
			graph.setProperty("y.scale", YScale.getText());
			graph.setProperty("auto.resize", "" + resize.isSelected());
			graph.setProperty("LogX", "" + LogX.isSelected());
			graph.setProperty("LogY", "" + LogY.isSelected());
			for (int i = 0; i < graphed.size(); i++) {
				graph.setProperty("species.connected." + i, "" + graphed.get(i).getConnected());
				graph.setProperty("species.filled." + i, "" + graphed.get(i).getFilled());
				graph.setProperty("species.xnumber." + i, "" + graphed.get(i).getXNumber());
				graph.setProperty("species.number." + i, "" + graphed.get(i).getNumber());
				graph.setProperty("species.run.number." + i, graphed.get(i).getRunNumber());
				graph.setProperty("species.name." + i, graphed.get(i).getSpecies());
				graph.setProperty("species.id." + i, graphed.get(i).getID());
				graph.setProperty("species.visible." + i, "" + graphed.get(i).getVisible());
				graph.setProperty("species.paint." + i, graphed.get(i).getShapeAndPaint()
						.getPaintName());
				graph.setProperty("species.shape." + i, graphed.get(i).getShapeAndPaint()
						.getShapeName());
				graph.setProperty("species.directory." + i, graphed.get(i).getDirectory());
			}
			try {
				FileOutputStream store = new FileOutputStream(new File(outDir + separator
						+ graphName));
				graph.store(store, "Graph Data");
				store.close();
				log.addText("Creating graph file:\n" + outDir + separator + graphName + "\n");
				change = false;
			}
			catch (Exception except) {
				JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save Graph!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			Properties graph = new Properties();
			graph.setProperty("title", chart.getTitle().getText());
			graph.setProperty("x.axis", chart.getCategoryPlot().getDomainAxis().getLabel());
			graph.setProperty("y.axis", chart.getCategoryPlot().getRangeAxis().getLabel());
			for (int i = 0; i < probGraphed.size(); i++) {
				graph.setProperty("species.number." + i, "" + probGraphed.get(i).getNumber());
				graph.setProperty("species.name." + i, probGraphed.get(i).getSpecies());
				graph.setProperty("species.id." + i, probGraphed.get(i).getID());
				graph.setProperty("species.paint." + i, probGraphed.get(i).getPaintName());
				graph.setProperty("species.directory." + i, probGraphed.get(i).getDirectory());
			}
			try {
				FileOutputStream store = new FileOutputStream(new File(outDir + separator
						+ graphName));
				graph.store(store, "Probability Data");
				store.close();
				log.addText("Creating graph file:\n" + outDir + separator + graphName + "\n");
				change = false;
			}
			catch (Exception except) {
				JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save Graph!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void saveAs() {
		if (timeSeries) {
			String graphName = JOptionPane.showInputDialog(biomodelsim.frame(),
					"Enter Graph Name:", "Graph Name", JOptionPane.PLAIN_MESSAGE);
			if (graphName != null && !graphName.trim().equals("")) {
				graphName = graphName.trim();
				if (graphName.length() > 3) {
					if (!graphName.substring(graphName.length() - 4).equals(".grf")) {
						graphName += ".grf";
					}
				}
				else {
					graphName += ".grf";
				}
				File f;
				if (topLevel) {
					f = new File(outDir + separator + graphName);
				}
				else {
					f = new File(outDir.substring(0, outDir.length()
							- outDir.split(separator)[outDir.split(separator).length - 1].length())
							+ separator + graphName);
				}
				if (f.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + "\nDo you want to overwrite?", "Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						File del;
						if (topLevel) {
							del = new File(outDir + separator + graphName);
						}
						else {
							del = new File(outDir.substring(0, outDir.length()
									- outDir.split(separator)[outDir.split(separator).length - 1]
											.length())
									+ separator + graphName);
						}
						if (del.isDirectory()) {
							biomodelsim.deleteDir(del);
						}
						else {
							del.delete();
						}
						for (int i = 0; i < biomodelsim.getTab().getTabCount(); i++) {
							if (biomodelsim.getTab().getTitleAt(i).equals(graphName)) {
								biomodelsim.getTab().remove(i);
							}
						}
					}
					else {
						return;
					}
				}
				Properties graph = new Properties();
				graph.setProperty("title", chart.getTitle().getText());
				graph.setProperty("x.axis", chart.getXYPlot().getDomainAxis().getLabel());
				graph.setProperty("y.axis", chart.getXYPlot().getRangeAxis().getLabel());
				graph.setProperty("x.min", XMin.getText());
				graph.setProperty("x.max", XMax.getText());
				graph.setProperty("x.scale", XScale.getText());
				graph.setProperty("y.min", YMin.getText());
				graph.setProperty("y.max", YMax.getText());
				graph.setProperty("y.scale", YScale.getText());
				graph.setProperty("auto.resize", "" + resize.isSelected());
				graph.setProperty("LogX", "" + LogX.isSelected());
				graph.setProperty("LogY", "" + LogY.isSelected());
				for (int i = 0; i < graphed.size(); i++) {
					graph.setProperty("species.connected." + i, "" + graphed.get(i).getConnected());
					graph.setProperty("species.filled." + i, "" + graphed.get(i).getFilled());
					graph.setProperty("species.number." + i, "" + graphed.get(i).getNumber());
					graph.setProperty("species.run.number." + i, graphed.get(i).getRunNumber());
					graph.setProperty("species.name." + i, graphed.get(i).getSpecies());
					graph.setProperty("species.id." + i, graphed.get(i).getID());
					graph.setProperty("species.visible." + i, "" + graphed.get(i).getVisible());
					graph.setProperty("species.paint." + i, graphed.get(i).getShapeAndPaint()
							.getPaintName());
					graph.setProperty("species.shape." + i, graphed.get(i).getShapeAndPaint()
							.getShapeName());
					if (topLevel) {
						graph.setProperty("species.directory." + i, graphed.get(i).getDirectory());
					}
					else {
						if (graphed.get(i).getDirectory().equals("")) {
							graph.setProperty("species.directory." + i,
									outDir.split(separator)[outDir.split(separator).length - 1]);
						}
						else {
							graph.setProperty("species.directory." + i,
									outDir.split(separator)[outDir.split(separator).length - 1]
											+ "/" + graphed.get(i).getDirectory());
						}
					}
				}
				try {
					FileOutputStream store = new FileOutputStream(f);
					graph.store(store, "Graph Data");
					store.close();
					log.addText("Creating graph file:\n" + f.getAbsolutePath() + "\n");
					change = false;
					biomodelsim.refreshTree();
				}
				catch (Exception except) {
					JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save Graph!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else {
			String graphName = JOptionPane.showInputDialog(biomodelsim.frame(),
					"Enter Probability Graph Name:", "Probability Graph Name",
					JOptionPane.PLAIN_MESSAGE);
			if (graphName != null && !graphName.trim().equals("")) {
				graphName = graphName.trim();
				if (graphName.length() > 3) {
					if (!graphName.substring(graphName.length() - 4).equals(".prb")) {
						graphName += ".prb";
					}
				}
				else {
					graphName += ".prb";
				}
				File f;
				if (topLevel) {
					f = new File(outDir + separator + graphName);
				}
				else {
					f = new File(outDir.substring(0, outDir.length()
							- outDir.split(separator)[outDir.split(separator).length - 1].length())
							+ separator + graphName);
				}
				if (f.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + "\nDo you want to overwrite?", "Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						File del;
						if (topLevel) {
							del = new File(outDir + separator + graphName);
						}
						else {
							del = new File(outDir.substring(0, outDir.length()
									- outDir.split(separator)[outDir.split(separator).length - 1]
											.length())
									+ separator + graphName);
						}
						if (del.isDirectory()) {
							biomodelsim.deleteDir(del);
						}
						else {
							del.delete();
						}
						for (int i = 0; i < biomodelsim.getTab().getTabCount(); i++) {
							if (biomodelsim.getTab().getTitleAt(i).equals(graphName)) {
								biomodelsim.getTab().remove(i);
							}
						}
					}
					else {
						return;
					}
				}
				Properties graph = new Properties();
				graph.setProperty("title", chart.getTitle().getText());
				graph.setProperty("x.axis", chart.getCategoryPlot().getDomainAxis().getLabel());
				graph.setProperty("y.axis", chart.getCategoryPlot().getRangeAxis().getLabel());
				for (int i = 0; i < probGraphed.size(); i++) {
					graph.setProperty("species.number." + i, "" + probGraphed.get(i).getNumber());
					graph.setProperty("species.name." + i, probGraphed.get(i).getSpecies());
					graph.setProperty("species.id." + i, probGraphed.get(i).getID());
					graph.setProperty("species.paint." + i, probGraphed.get(i).getPaintName());
					if (topLevel) {
						graph.setProperty("species.directory." + i, probGraphed.get(i)
								.getDirectory());
					}
					else {
						if (probGraphed.get(i).getDirectory().equals("")) {
							graph.setProperty("species.directory." + i,
									outDir.split(separator)[outDir.split(separator).length - 1]);
						}
						else {
							graph.setProperty("species.directory." + i,
									outDir.split(separator)[outDir.split(separator).length - 1]
											+ "/" + probGraphed.get(i).getDirectory());
						}
					}
				}
				try {
					FileOutputStream store = new FileOutputStream(f);
					graph.store(store, "Probability Graph Data");
					store.close();
					log.addText("Creating probability graph file:\n" + f.getAbsolutePath() + "\n");
					change = false;
					biomodelsim.refreshTree();
				}
				catch (Exception except) {
					JOptionPane
							.showMessageDialog(biomodelsim.frame(),
									"Unable To Save Probability Graph!", "Error",
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void open(String filename) {
		if (timeSeries) {
			Properties graph = new Properties();
			try {
				FileInputStream load = new FileInputStream(new File(filename));
				graph.load(load);
				load.close();
				XMin.setText(graph.getProperty("x.min"));
				XMax.setText(graph.getProperty("x.max"));
				XScale.setText(graph.getProperty("x.scale"));
				YMin.setText(graph.getProperty("y.min"));
				YMax.setText(graph.getProperty("y.max"));
				YScale.setText(graph.getProperty("y.scale"));
				chart.setTitle(graph.getProperty("title"));
				chart.getXYPlot().getDomainAxis().setLabel(graph.getProperty("x.axis"));
				chart.getXYPlot().getRangeAxis().setLabel(graph.getProperty("y.axis"));
				if (graph.getProperty("auto.resize").equals("true")) {
					resize.setSelected(true);
				}
				else {
					resize.setSelected(false);
				}
				if (graph.containsKey("LogX") && graph.getProperty("LogX").equals("true")) {
					LogX.setSelected(true);
				}
				else {
					LogX.setSelected(false);
				}
				if (graph.containsKey("LogY") && graph.getProperty("LogY").equals("true")) {
					LogY.setSelected(true);
				}
				else {
					LogY.setSelected(false);
				}
				int next = 0;
				while (graph.containsKey("species.name." + next)) {
					boolean connected, filled, visible;
					if (graph.getProperty("species.connected." + next).equals("true")) {
						connected = true;
					}
					else {
						connected = false;
					}
					if (graph.getProperty("species.filled." + next).equals("true")) {
						filled = true;
					}
					else {
						filled = false;
					}
					if (graph.getProperty("species.visible." + next).equals("true")) {
						visible = true;
					}
					else {
						visible = false;
					}
					int xnumber = 0;
					if (graph.containsKey("species.xnumber." + next)) {
						xnumber = Integer.parseInt(graph.getProperty("species.xnumber." + next));
					}
					graphed.add(new GraphSpecies(shapes.get(graph.getProperty("species.shape."
							+ next)), colors.get(graph.getProperty("species.paint." + next)),
							filled, visible, connected, graph.getProperty("species.run.number."
									+ next), graph.getProperty("species.id." + next), graph
									.getProperty("species.name." + next), xnumber, Integer
									.parseInt(graph.getProperty("species.number." + next)), graph
									.getProperty("species.directory." + next)));
					next++;
				}
				updateXNumber = false;
				XVariable.addItem("time");
				refresh();
			}
			catch (IOException except) {
				JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Load Graph!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			Properties graph = new Properties();
			try {
				FileInputStream load = new FileInputStream(new File(filename));
				graph.load(load);
				load.close();
				chart.setTitle(graph.getProperty("title"));
				chart.getCategoryPlot().getDomainAxis().setLabel(graph.getProperty("x.axis"));
				chart.getCategoryPlot().getRangeAxis().setLabel(graph.getProperty("y.axis"));
				int next = 0;
				while (graph.containsKey("species.name." + next)) {
					probGraphed.add(new GraphProbs(colors.get(graph.getProperty("species.paint."
							+ next)), graph.getProperty("species.paint." + next), graph
							.getProperty("species.id." + next), graph.getProperty("species.name."
							+ next), Integer.parseInt(graph.getProperty("species.number." + next)),
							graph.getProperty("species.directory." + next)));
					next++;
				}
				refreshProb();
			}
			catch (Exception except) {
				JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Load Graph!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public boolean isTSDGraph() {
		return timeSeries;
	}

	public void refresh() {
		if (timeSeries) {
			if (learnSpecs != null) {
				updateSpecies();
			}
			double minY = 0;
			double maxY = 0;
			double scaleY = 0;
			double minX = 0;
			double maxX = 0;
			double scaleX = 0;
			try {
				minY = Double.parseDouble(YMin.getText().trim());
				maxY = Double.parseDouble(YMax.getText().trim());
				scaleY = Double.parseDouble(YScale.getText().trim());
				minX = Double.parseDouble(XMin.getText().trim());
				maxX = Double.parseDouble(XMax.getText().trim());
				scaleX = Double.parseDouble(XScale.getText().trim());
				/*
				 * NumberFormat num = NumberFormat.getInstance();
				 * num.setMaximumFractionDigits(4); num.setGroupingUsed(false);
				 * minY = Double.parseDouble(num.format(minY)); maxY =
				 * Double.parseDouble(num.format(maxY)); scaleY =
				 * Double.parseDouble(num.format(scaleY)); minX =
				 * Double.parseDouble(num.format(minX)); maxX =
				 * Double.parseDouble(num.format(maxX)); scaleX =
				 * Double.parseDouble(num.format(scaleX));
				 */
			}
			catch (Exception e1) {
			}
			ArrayList<XYSeries> graphData = new ArrayList<XYSeries>();
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
			int thisOne = -1;
			for (int i = 1; i < graphed.size(); i++) {
				GraphSpecies index = graphed.get(i);
				int j = i;
				while ((j > 0)
						&& (graphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
					graphed.set(j, graphed.get(j - 1));
					j = j - 1;
				}
				graphed.set(j, index);
			}
			ArrayList<GraphSpecies> unableToGraph = new ArrayList<GraphSpecies>();
			HashMap<String, ArrayList<ArrayList<Double>>> allData = new HashMap<String, ArrayList<ArrayList<Double>>>();
			for (GraphSpecies g : graphed) {
				if (g.getDirectory().equals("")) {
					thisOne++;
					rend.setSeriesVisible(thisOne, true);
					rend.setSeriesLinesVisible(thisOne, g.getConnected());
					rend.setSeriesShapesFilled(thisOne, g.getFilled());
					rend.setSeriesShapesVisible(thisOne, g.getVisible());
					rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
					rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
					if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance")
							&& !g.getRunNumber().equals("Standard Deviation")) {
						if (new File(outDir + separator + g.getRunNumber() + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								readGraphSpecies(outDir + separator + g.getRunNumber() + "."
										+ printer_id.substring(0, printer_id.length() - 8));
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
								updateXNumber = false;
								XVariable.removeAllItems();
								for (int i = 0; i < graphSpecies.size(); i++) {
									XVariable.addItem(graphSpecies.get(i));
								}
								updateXNumber = true;
							}
							else {
								data = readData(outDir + separator + g.getRunNumber() + "."
										+ printer_id.substring(0, printer_id.length() - 8), g
										.getRunNumber(), null, false);
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
					else {
						boolean ableToGraph = false;
						try {
							for (String s : new File(outDir).list()) {
								if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
									ableToGraph = true;
								}
							}
						}
						catch (Exception e) {
							ableToGraph = false;
						}
						if (!ableToGraph) {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
						}
						if (ableToGraph) {
							int nextOne = 1;
							// while (!new File(outDir + separator + "run-" +
							// nextOne + "."
							// + printer_id.substring(0, printer_id.length() -
							// 8)).exists()) {
							// nextOne++;
							// }
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + "standard_deviation"
											+ "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else {
									while (!new File(outDir + separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										nextOne++;
									}
									readGraphSpecies(outDir + separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
								updateXNumber = false;
								XVariable.removeAllItems();
								for (int i = 0; i < graphSpecies.size(); i++) {
									XVariable.addItem(graphSpecies.get(i));
								}
								updateXNumber = true;
							}
							else {
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + "mean."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + "variance."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + "standard_deviation."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else {
									while (!new File(outDir + separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										nextOne++;
									}
									data = readData(outDir + separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
				}
				else {
					thisOne++;
					rend.setSeriesVisible(thisOne, true);
					rend.setSeriesLinesVisible(thisOne, g.getConnected());
					rend.setSeriesShapesFilled(thisOne, g.getFilled());
					rend.setSeriesShapesVisible(thisOne, g.getVisible());
					rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
					rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
					if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance")
							&& !g.getRunNumber().equals("Standard Deviation")) {
						if (new File(outDir + separator + g.getDirectory() + separator
								+ g.getRunNumber() + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								readGraphSpecies(outDir + separator + g.getDirectory() + separator
										+ g.getRunNumber() + "."
										+ printer_id.substring(0, printer_id.length() - 8));
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
								updateXNumber = false;
								XVariable.removeAllItems();
								for (int i = 0; i < graphSpecies.size(); i++) {
									XVariable.addItem(graphSpecies.get(i));
								}
								updateXNumber = true;
							}
							else {
								data = readData(outDir + separator + g.getDirectory() + separator
										+ g.getRunNumber() + "."
										+ printer_id.substring(0, printer_id.length() - 8), g
										.getRunNumber(), g.getDirectory(), false);
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
					else {
						boolean ableToGraph = false;
						try {
							for (String s : new File(outDir + separator + g.getDirectory()).list()) {
								if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
									ableToGraph = true;
								}
							}
						}
						catch (Exception e) {
							ableToGraph = false;
						}
						if (!ableToGraph) {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + separator + g.getDirectory() + separator
											+ "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
						}
						if (ableToGraph) {
							int nextOne = 1;
							// while (!new File(outDir + separator +
							// g.getDirectory() + separator
							// + "run-" + nextOne + "."
							// + printer_id.substring(0, printer_id.length() -
							// 8)).exists()) {
							// nextOne++;
							// }
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + g.getDirectory()
											+ separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + g.getDirectory()
											+ separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + separator + g.getDirectory()
											+ separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else {
									while (!new File(outDir + separator + g.getDirectory()
											+ separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										nextOne++;
									}
									readGraphSpecies(outDir + separator + g.getDirectory()
											+ separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
								updateXNumber = false;
								XVariable.removeAllItems();
								for (int i = 0; i < graphSpecies.size(); i++) {
									XVariable.addItem(graphSpecies.get(i));
								}
								updateXNumber = true;
							}
							else {
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "mean."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "variance."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + separator + g.getDirectory()
												+ separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "standard_deviation."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else {
									while (!new File(outDir + separator + g.getDirectory()
											+ separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										nextOne++;
									}
									data = readData(outDir + separator + g.getDirectory()
											+ separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), g.getDirectory(), false);
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1)
											&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphData.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										graphData.get(graphData.size() - 1).add(
												(data.get(g.getXNumber())).get(i),
												(data.get(g.getNumber() + 1)).get(i));
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
				}
			}
			for (GraphSpecies g : unableToGraph) {
				graphed.remove(g);
			}
			XYSeriesCollection dataset = new XYSeriesCollection();
			for (int i = 0; i < graphData.size(); i++) {
				dataset.addSeries(graphData.get(i));
			}
			fixGraph(chart.getTitle().getText(), chart.getXYPlot().getDomainAxis().getLabel(),
					chart.getXYPlot().getRangeAxis().getLabel(), dataset);
			chart.getXYPlot().setRenderer(rend);
			XYPlot plot = chart.getXYPlot();
			if (resize.isSelected()) {
				resize(dataset);
			}
			else {
				NumberAxis axis = (NumberAxis) plot.getRangeAxis();
				axis.setAutoTickUnitSelection(false);
				axis.setRange(minY, maxY);
				axis.setTickUnit(new NumberTickUnit(scaleY));
				axis = (NumberAxis) plot.getDomainAxis();
				axis.setAutoTickUnitSelection(false);
				axis.setRange(minX, maxX);
				axis.setTickUnit(new NumberTickUnit(scaleX));
			}
		}
		else {
			BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
			int thisOne = -1;
			for (int i = 1; i < probGraphed.size(); i++) {
				GraphProbs index = probGraphed.get(i);
				int j = i;
				while ((j > 0)
						&& (probGraphed.get(j - 1).getSpecies().compareToIgnoreCase(
								index.getSpecies()) > 0)) {
					probGraphed.set(j, probGraphed.get(j - 1));
					j = j - 1;
				}
				probGraphed.set(j, index);
			}
			ArrayList<GraphProbs> unableToGraph = new ArrayList<GraphProbs>();
			DefaultCategoryDataset histDataset = new DefaultCategoryDataset();
			for (GraphProbs g : probGraphed) {
				if (g.getDirectory().equals("")) {
					thisOne++;
					rend.setSeriesPaint(thisOne, g.getPaint());
					if (new File(outDir + separator + "sim-rep.txt").exists()) {
						readProbSpecies(outDir + separator + "sim-rep.txt");
						double[] data = readProbs(outDir + separator + "sim-rep.txt");
						for (int i = 1; i < graphProbs.size(); i++) {
							String index = graphProbs.get(i);
							double index2 = data[i];
							int j = i;
							while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
								graphProbs.set(j, graphProbs.get(j - 1));
								data[j] = data[j - 1];
								j = j - 1;
							}
							graphProbs.set(j, index);
							data[j] = index2;
						}
						if (graphProbs.size() != 0) {
							if (graphProbs.contains(g.getID())) {
								for (int i = 0; i < graphProbs.size(); i++) {
									if (g.getID().equals(graphProbs.get(i))) {
										g.setNumber(i);
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
					}
					else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
				else {
					thisOne++;
					rend.setSeriesPaint(thisOne, g.getPaint());
					if (new File(outDir + separator + g.getDirectory() + separator + "sim-rep.txt")
							.exists()) {
						readProbSpecies(outDir + separator + g.getDirectory() + separator
								+ "sim-rep.txt");
						double[] data = readProbs(outDir + separator + g.getDirectory() + separator
								+ "sim-rep.txt");
						for (int i = 1; i < graphProbs.size(); i++) {
							String index = graphProbs.get(i);
							double index2 = data[i];
							int j = i;
							while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
								graphProbs.set(j, graphProbs.get(j - 1));
								data[j] = data[j - 1];
								j = j - 1;
							}
							graphProbs.set(j, index);
							data[j] = index2;
						}
						if (graphProbs.size() != 0) {
							String compare = g.getID().replace(" (", "~");
							if (graphProbs.contains(compare.split("~")[0].trim())) {
								for (int i = 0; i < graphProbs.size(); i++) {
									if (compare.split("~")[0].trim().equals(graphProbs.get(i))) {
										g.setNumber(i);
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
					}
					else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
			}
			for (GraphProbs g : unableToGraph) {
				probGraphed.remove(g);
			}
			fixProbGraph(chart.getTitle().getText(), chart.getCategoryPlot().getDomainAxis()
					.getLabel(), chart.getCategoryPlot().getRangeAxis().getLabel(), histDataset,
					rend);
		}
	}

	private class ShapeAndPaint {
		private Shape shape;

		private Paint paint;

		private ShapeAndPaint(Shape s, Paint p) {
			shape = s;
			paint = p;
		}

		private Shape getShape() {
			return shape;
		}

		private Paint getPaint() {
			return paint;
		}

		private String getShapeName() {
			Object[] set = shapes.keySet().toArray();
			for (int i = 0; i < set.length; i++) {
				if (shape == shapes.get(set[i])) {
					return (String) set[i];
				}
			}
			return "Unknown Shape";
		}

		private String getPaintName() {
			Object[] set = colors.keySet().toArray();
			for (int i = 0; i < set.length; i++) {
				if (paint == colors.get(set[i])) {
					return (String) set[i];
				}
			}
			return "Unknown Color";
		}

		public void setPaint(String paint) {
			this.paint = colors.get(paint);
		}

		public void setShape(String shape) {
			this.shape = shapes.get(shape);
		}
	}

	private class GraphSpecies {
		private ShapeAndPaint sP;

		private boolean filled, visible, connected;

		private String runNumber, species, directory, id;

		private int xnumber, number;

		private GraphSpecies(Shape s, Paint p, boolean filled, boolean visible, boolean connected,
				String runNumber, String id, String species, int xnumber, int number,
				String directory) {
			sP = new ShapeAndPaint(s, p);
			this.filled = filled;
			this.visible = visible;
			this.connected = connected;
			this.runNumber = runNumber;
			this.species = species;
			this.xnumber = xnumber;
			this.number = number;
			this.directory = directory;
			this.id = id;
		}

		private void setDirectory(String directory) {
			this.directory = directory;
		}

		private void setXNumber(int xnumber) {
			this.xnumber = xnumber;
		}

		private void setNumber(int number) {
			this.number = number;
		}

		private void setSpecies(String species) {
			this.species = species;
		}

		private void setPaint(String paint) {
			sP.setPaint(paint);
		}

		private void setShape(String shape) {
			sP.setShape(shape);
		}

		private void setVisible(boolean b) {
			visible = b;
		}

		private void setFilled(boolean b) {
			filled = b;
		}

		private void setConnected(boolean b) {
			connected = b;
		}

		private int getXNumber() {
			return xnumber;
		}

		private int getNumber() {
			return number;
		}

		private String getSpecies() {
			return species;
		}

		private ShapeAndPaint getShapeAndPaint() {
			return sP;
		}

		private boolean getFilled() {
			return filled;
		}

		private boolean getVisible() {
			return visible;
		}

		private boolean getConnected() {
			return connected;
		}

		private String getRunNumber() {
			return runNumber;
		}

		private String getDirectory() {
			return directory;
		}

		private String getID() {
			return id;
		}
	}

	public void setDirectory(String newDirectory) {
		outDir = newDirectory;
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	public boolean hasChanged() {
		return change;
	}

	private void probGraph(String label) {
		chart = ChartFactory.createBarChart(label, "", "Percent", new DefaultCategoryDataset(),
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(java.awt.Color.WHITE);
		ChartPanel graph = new ChartPanel(chart);
		graph.setLayout(new GridLayout(1, 1));
		JLabel edit = new JLabel("Double click here to create graph");
		edit.addMouseListener(this);
		Font font = edit.getFont();
		font = font.deriveFont(Font.BOLD, 42.0f);
		edit.setFont(font);
		edit.setHorizontalAlignment(SwingConstants.CENTER);
		graph.add(edit);
		graph.addMouseListener(this);
		change = false;

		// creates the buttons for the graph frame
		JPanel ButtonHolder = new JPanel();
		run = new JButton("Save and Run");
		save = new JButton("Save Graph");
		saveAs = new JButton("Save As");
		export = new JButton("Export");
		refresh = new JButton("Refresh");
		run.addActionListener(this);
		save.addActionListener(this);
		saveAs.addActionListener(this);
		export.addActionListener(this);
		refresh.addActionListener(this);
		if (reb2sac != null) {
			ButtonHolder.add(run);
		}
		ButtonHolder.add(save);
		ButtonHolder.add(saveAs);
		ButtonHolder.add(export);
		ButtonHolder.add(refresh);

		// puts all the components of the graph gui into a display panel
		// JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// ButtonHolder, null);
		// splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		// this.add(splitPane, "South");
	}

	private void editProbGraph() {
		final ArrayList<GraphProbs> old = new ArrayList<GraphProbs>();
		for (GraphProbs g : probGraphed) {
			old.add(g);
		}
		final JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("Title:");
		JLabel xLabel = new JLabel("X-Axis Label:");
		JLabel yLabel = new JLabel("Y-Axis Label:");
		final JTextField title = new JTextField(chart.getTitle().getText(), 5);
		final JTextField x = new JTextField(chart.getCategoryPlot().getDomainAxis().getLabel(), 5);
		final JTextField y = new JTextField(chart.getCategoryPlot().getRangeAxis().getLabel(), 5);
		String simDirString = outDir.split(separator)[outDir.split(separator).length - 1];
		simDir = new IconNode(simDirString, simDirString);
		simDir.setIconName("");
		String[] files = new File(outDir).list();
		for (int i = 1; i < files.length; i++) {
			String index = files[i];
			int j = i;
			while ((j > 0) && files[j - 1].compareToIgnoreCase(index) > 0) {
				files[j] = files[j - 1];
				j = j - 1;
			}
			files[j] = index;
		}
		boolean add = false;
		final ArrayList<String> directories = new ArrayList<String>();
		for (String file : files) {
			if (file.length() > 3 && file.substring(file.length() - 4).equals(".txt")) {
				if (file.contains("sim-rep")) {
					add = true;
				}
			}
			else if (new File(outDir + separator + file).isDirectory()) {
				boolean addIt = false;
				for (String getFile : new File(outDir + separator + file).list()) {
					if (getFile.length() > 3
							&& getFile.substring(getFile.length() - 4).equals(".txt")
							&& getFile.contains("sim-rep")) {
						addIt = true;
					}
					else if (new File(outDir + separator + file + separator + getFile)
							.isDirectory()) {
						for (String getFile2 : new File(outDir + separator + file + separator
								+ getFile).list()) {
							if (getFile2.length() > 3
									&& getFile2.substring(getFile2.length() - 4).equals(".txt")
									&& getFile2.contains("sim-rep")) {
								addIt = true;
							}
						}
					}
				}
				if (addIt) {
					directories.add(file);
					IconNode d = new IconNode(file, file);
					d.setIconName("");
					String[] files2 = new File(outDir + separator + file).list();
					for (int i = 1; i < files2.length; i++) {
						String index = files2[i];
						int j = i;
						while ((j > 0) && files2[j - 1].compareToIgnoreCase(index) > 0) {
							files2[j] = files2[j - 1];
							j = j - 1;
						}
						files2[j] = index;
					}
					boolean add2 = false;
					for (String f : files2) {
						if (f.equals("sim-rep.txt")) {
							add2 = true;
						}
						else if (new File(outDir + separator + file + separator + f).isDirectory()) {
							boolean addIt2 = false;
							for (String getFile : new File(outDir + separator + file + separator
									+ f).list()) {
								if (getFile.length() > 3
										&& getFile.substring(getFile.length() - 4).equals(".txt")
										&& getFile.contains("sim-rep")) {
									addIt2 = true;
								}
							}
							if (addIt2) {
								directories.add(file + separator + f);
								IconNode d2 = new IconNode(f, f);
								d2.setIconName("");
								for (String f2 : new File(outDir + separator + file + separator + f)
										.list()) {
									if (f2.equals("sim-rep.txt")) {
										IconNode n = new IconNode(f2.substring(0, f2.length() - 4),
												f2.substring(0, f2.length() - 4));
										d2.add(n);
										n.setIconName("");
										for (GraphProbs g : probGraphed) {
											if (g.getDirectory().equals(
													d.getName() + separator + d2.getName())) {
												n.setIcon(TextIcons.getIcon("g"));
												n.setIconName("" + (char) 10003);
												d2.setIcon(MetalIconFactory
														.getFileChooserUpFolderIcon());
												d2.setIconName("" + (char) 10003);
												d.setIcon(MetalIconFactory
														.getFileChooserUpFolderIcon());
												d.setIconName("" + (char) 10003);
												simDir.setIcon(MetalIconFactory
														.getFileChooserUpFolderIcon());
												simDir.setIconName("" + (char) 10003);
											}
										}
									}
								}
								d.add(d2);
							}
						}
					}
					if (add2) {
						IconNode n = new IconNode("sim-rep", "sim-rep");
						d.add(n);
						n.setIconName("");
						for (GraphProbs g : probGraphed) {
							if (g.getDirectory().equals(d.getName())) {
								n.setIcon(TextIcons.getIcon("g"));
								n.setIconName("" + (char) 10003);
								d.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								d.setIconName("" + (char) 10003);
								simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
								simDir.setIconName("" + (char) 10003);
							}
						}
					}
					simDir.add(d);
				}
			}
		}
		if (add) {
			IconNode n = new IconNode("sim-rep", "sim-rep");
			simDir.add(n);
			n.setIconName("");
			for (GraphProbs g : probGraphed) {
				if (g.getDirectory().equals("")) {
					n.setIcon(TextIcons.getIcon("g"));
					n.setIconName("" + (char) 10003);
					simDir.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
					simDir.setIconName("" + (char) 10003);
				}
			}
		}
		if (simDir.getChildCount() == 0) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "No data to graph."
					+ "\nPerform some simulations to create some data first.", "No Data",
					JOptionPane.PLAIN_MESSAGE);
		}
		else {
			tree = new JTree(simDir);
			tree.putClientProperty("JTree.icons", makeIcons());
			tree.setCellRenderer(new IconNodeRenderer());
			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
			renderer.setLeafIcon(MetalIconFactory.getTreeLeafIcon());
			renderer.setClosedIcon(MetalIconFactory.getTreeFolderIcon());
			renderer.setOpenIcon(MetalIconFactory.getTreeFolderIcon());
			final JPanel all = new JPanel(new BorderLayout());
			final JScrollPane scroll = new JScrollPane();
			tree.addTreeExpansionListener(new TreeExpansionListener() {
				public void treeCollapsed(TreeExpansionEvent e) {
					JScrollPane scrollpane = new JScrollPane();
					scrollpane.getViewport().add(tree);
					all.removeAll();
					all.add(titlePanel, "North");
					all.add(scroll, "Center");
					all.add(scrollpane, "West");
					all.revalidate();
					all.repaint();
				}

				public void treeExpanded(TreeExpansionEvent e) {
					JScrollPane scrollpane = new JScrollPane();
					scrollpane.getViewport().add(tree);
					all.removeAll();
					all.add(titlePanel, "North");
					all.add(scroll, "Center");
					all.add(scrollpane, "West");
					all.revalidate();
					all.repaint();
				}
			});
			// for (int i = 0; i < tree.getRowCount(); i++) {
			// tree.expandRow(i);
			// }
			JScrollPane scrollpane = new JScrollPane();
			scrollpane.getViewport().add(tree);
			scrollpane.setPreferredSize(new Dimension(175, 100));
			final JPanel specPanel = new JPanel();
			boolean stop = false;
			int selectionRow = 1;
			for (int i = 1; i < tree.getRowCount(); i++) {
				tree.setSelectionRow(i);
				if (selected.equals(lastSelected)) {
					stop = true;
					selectionRow = i;
					break;
				}
			}
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					node = (IconNode) e.getPath().getLastPathComponent();
					if (!directories.contains(node.getName())) {
						selected = node.getName();
						int select;
						if (selected.equals("sim-rep")) {
							select = 0;
						}
						else {
							select = -1;
						}
						if (select != -1) {
							specPanel.removeAll();
							if (node.getParent().getParent() != null
									&& directories.contains(((IconNode) node.getParent()
											.getParent()).getName()
											+ separator + ((IconNode) node.getParent()).getName())) {
								specPanel.add(fixProbChoices(((IconNode) node.getParent()
										.getParent()).getName()
										+ separator + ((IconNode) node.getParent()).getName()));
							}
							else if (directories.contains(((IconNode) node.getParent()).getName())) {
								specPanel.add(fixProbChoices(((IconNode) node.getParent())
										.getName()));
							}
							else {
								specPanel.add(fixProbChoices(""));
							}
							specPanel.revalidate();
							specPanel.repaint();
							for (int i = 0; i < series.size(); i++) {
								series.get(i).setText(graphProbs.get(i));
								series.get(i).setSelectionStart(0);
								series.get(i).setSelectionEnd(0);
							}
							for (int i = 0; i < boxes.size(); i++) {
								boxes.get(i).setSelected(false);
							}
							if (node.getParent().getParent() != null
									&& directories.contains(((IconNode) node.getParent()
											.getParent()).getName()
											+ separator + ((IconNode) node.getParent()).getName())) {
								for (GraphProbs g : probGraphed) {
									if (g.getDirectory().equals(
											((IconNode) node.getParent().getParent()).getName()
													+ separator
													+ ((IconNode) node.getParent()).getName())) {
										boxes.get(g.getNumber()).setSelected(true);
										series.get(g.getNumber()).setText(g.getSpecies());
										series.get(g.getNumber()).setSelectionStart(0);
										series.get(g.getNumber()).setSelectionEnd(0);
										colorsCombo.get(g.getNumber()).setSelectedItem(
												g.getPaintName());
									}
								}
							}
							else if (directories.contains(((IconNode) node.getParent()).getName())) {
								for (GraphProbs g : probGraphed) {
									if (g.getDirectory().equals(
											((IconNode) node.getParent()).getName())) {
										boxes.get(g.getNumber()).setSelected(true);
										series.get(g.getNumber()).setText(g.getSpecies());
										series.get(g.getNumber()).setSelectionStart(0);
										series.get(g.getNumber()).setSelectionEnd(0);
										colorsCombo.get(g.getNumber()).setSelectedItem(
												g.getPaintName());
									}
								}
							}
							else {
								for (GraphProbs g : probGraphed) {
									if (g.getDirectory().equals("")) {
										boxes.get(g.getNumber()).setSelected(true);
										series.get(g.getNumber()).setText(g.getSpecies());
										series.get(g.getNumber()).setSelectionStart(0);
										series.get(g.getNumber()).setSelectionEnd(0);
										colorsCombo.get(g.getNumber()).setSelectedItem(
												g.getPaintName());
									}
								}
							}
							boolean allChecked = true;
							for (int i = 0; i < boxes.size(); i++) {
								if (!boxes.get(i).isSelected()) {
									allChecked = false;
									String s = "";
									if (node.getParent().getParent() != null
											&& directories.contains(((IconNode) node.getParent()
													.getParent()).getName()
													+ separator
													+ ((IconNode) node.getParent()).getName())) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ")";
									}
									else if (directories.contains(((IconNode) node.getParent())
											.getName())) {
										s = "(" + ((IconNode) node.getParent()).getName() + ")";
									}
									String text = series.get(i).getText();
									String end = "";
									if (!s.equals("")) {
										if (text.length() >= s.length()) {
											for (int j = 0; j < s.length(); j++) {
												end = text.charAt(text.length() - 1 - j) + end;
											}
											if (!s.equals(end)) {
												text += " " + s;
											}
										}
										else {
											text += " " + s;
										}
									}
									boxes.get(i).setName(text);
									series.get(i).setText(text);
									series.get(i).setSelectionStart(0);
									series.get(i).setSelectionEnd(0);
									colorsCombo.get(i).setSelectedIndex(0);
								}
								else {
									String s = "";
									if (node.getParent().getParent() != null
											&& directories.contains(((IconNode) node.getParent()
													.getParent()).getName()
													+ separator
													+ ((IconNode) node.getParent()).getName())) {
										s = "("
												+ ((IconNode) node.getParent().getParent())
														.getName() + separator
												+ ((IconNode) node.getParent()).getName() + ")";
									}
									else if (directories.contains(((IconNode) node.getParent())
											.getName())) {
										s = "(" + ((IconNode) node.getParent()).getName() + ")";
									}
									String text = graphProbs.get(i);
									String end = "";
									if (!s.equals("")) {
										if (text.length() >= s.length()) {
											for (int j = 0; j < s.length(); j++) {
												end = text.charAt(text.length() - 1 - j) + end;
											}
											if (!s.equals(end)) {
												text += " " + s;
											}
										}
										else {
											text += " " + s;
										}
									}
									boxes.get(i).setName(text);
								}
							}
							if (allChecked) {
								use.setSelected(true);
							}
							else {
								use.setSelected(false);
							}
						}
					}
					else {
						specPanel.removeAll();
						specPanel.revalidate();
						specPanel.repaint();
					}
				}
			});
			if (!stop) {
				tree.setSelectionRow(0);
				tree.setSelectionRow(1);
			}
			else {
				tree.setSelectionRow(0);
				tree.setSelectionRow(selectionRow);
			}
			scroll.setPreferredSize(new Dimension(1050, 500));
			JPanel editPanel = new JPanel(new BorderLayout());
			editPanel.add(specPanel, "Center");
			scroll.setViewportView(editPanel);
			final JButton deselect = new JButton("Deselect All");
			deselect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int size = probGraphed.size();
					for (int i = 0; i < size; i++) {
						probGraphed.remove();
					}
					IconNode n = simDir;
					while (n != null) {
						if (n.isLeaf()) {
							n.setIcon(MetalIconFactory.getTreeLeafIcon());
							n.setIconName("");
							IconNode check = (IconNode) ((DefaultMutableTreeNode) n.getParent())
									.getChildAfter(n);
							if (check == null) {
								n = (IconNode) n.getParent();
								if (n.getParent() == null) {
									n = null;
								}
								else {
									IconNode check2 = (IconNode) ((DefaultMutableTreeNode) n
											.getParent()).getChildAfter(n);
									if (check2 == null) {
										n = (IconNode) n.getParent();
										if (n.getParent() == null) {
											n = null;
										}
										else {
											n = (IconNode) ((DefaultMutableTreeNode) n.getParent())
													.getChildAfter(n);
										}
									}
									else {
										n = check2;
									}
								}
							}
							else {
								n = check;
							}
						}
						else {
							n.setIcon(MetalIconFactory.getTreeFolderIcon());
							n.setIconName("");
							n = (IconNode) n.getChildAt(0);
						}
					}
					tree.revalidate();
					tree.repaint();
					if (tree.getSelectionCount() > 0) {
						int selectedRow = tree.getSelectionRows()[0];
						tree.setSelectionRow(0);
						tree.setSelectionRow(selectedRow);
					}
				}
			});
			JPanel titlePanel1 = new JPanel(new GridLayout(1, 6));
			JPanel titlePanel2 = new JPanel(new GridLayout(1, 6));
			titlePanel1.add(titleLabel);
			titlePanel1.add(title);
			titlePanel1.add(xLabel);
			titlePanel1.add(x);
			titlePanel1.add(yLabel);
			titlePanel1.add(y);
			titlePanel2.add(new JPanel());
			JPanel deselectPanel = new JPanel();
			deselectPanel.add(deselect);
			titlePanel2.add(deselectPanel);
			titlePanel2.add(new JPanel());
			titlePanel2.add(new JPanel());
			titlePanel2.add(new JPanel());
			titlePanel2.add(new JPanel());
			titlePanel.add(titlePanel1, "Center");
			titlePanel.add(titlePanel2, "South");
			all.add(titlePanel, "North");
			all.add(scroll, "Center");
			all.add(scrollpane, "West");
			Object[] options = { "Ok", "Cancel" };
			int value = JOptionPane.showOptionDialog(biomodelsim.frame(), all,
					"Edit Probability Graph", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				change = true;
				lastSelected = selected;
				selected = "";
				BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
				int thisOne = -1;
				for (int i = 1; i < probGraphed.size(); i++) {
					GraphProbs index = probGraphed.get(i);
					int j = i;
					while ((j > 0)
							&& (probGraphed.get(j - 1).getSpecies().compareToIgnoreCase(
									index.getSpecies()) > 0)) {
						probGraphed.set(j, probGraphed.get(j - 1));
						j = j - 1;
					}
					probGraphed.set(j, index);
				}
				ArrayList<GraphProbs> unableToGraph = new ArrayList<GraphProbs>();
				DefaultCategoryDataset histDataset = new DefaultCategoryDataset();
				for (GraphProbs g : probGraphed) {
					if (g.getDirectory().equals("")) {
						thisOne++;
						rend.setSeriesPaint(thisOne, g.getPaint());
						if (new File(outDir + separator + "sim-rep.txt").exists()) {
							readProbSpecies(outDir + separator + "sim-rep.txt");
							double[] data = readProbs(outDir + separator + "sim-rep.txt");
							for (int i = 1; i < graphProbs.size(); i++) {
								String index = graphProbs.get(i);
								double index2 = data[i];
								int j = i;
								while ((j > 0)
										&& graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
									graphProbs.set(j, graphProbs.get(j - 1));
									data[j] = data[j - 1];
									j = j - 1;
								}
								graphProbs.set(j, index);
								data[j] = index2;
							}
							if (graphProbs.size() != 0) {
								for (int i = 0; i < graphProbs.size(); i++) {
									if (g.getID().equals(graphProbs.get(i))) {
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
					else {
						thisOne++;
						rend.setSeriesPaint(thisOne, g.getPaint());
						if (new File(outDir + separator + g.getDirectory() + separator
								+ "sim-rep.txt").exists()) {
							readProbSpecies(outDir + separator + g.getDirectory() + separator
									+ "sim-rep.txt");
							double[] data = readProbs(outDir + separator + g.getDirectory()
									+ separator + "sim-rep.txt");
							for (int i = 1; i < graphProbs.size(); i++) {
								String index = graphProbs.get(i);
								double index2 = data[i];
								int j = i;
								while ((j > 0)
										&& graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
									graphProbs.set(j, graphProbs.get(j - 1));
									data[j] = data[j - 1];
									j = j - 1;
								}
								graphProbs.set(j, index);
								data[j] = index2;
							}
							if (graphProbs.size() != 0) {
								for (int i = 0; i < graphProbs.size(); i++) {
									String compare = g.getID().replace(" (", "~");
									if (compare.split("~")[0].trim().equals(graphProbs.get(i))) {
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
				}
				for (GraphProbs g : unableToGraph) {
					probGraphed.remove(g);
				}
				fixProbGraph(title.getText().trim(), x.getText().trim(), y.getText().trim(),
						histDataset, rend);
			}
			else {
				selected = "";
				int size = probGraphed.size();
				for (int i = 0; i < size; i++) {
					probGraphed.remove();
				}
				for (GraphProbs g : old) {
					probGraphed.add(g);
				}
			}
		}
	}

	private JPanel fixProbChoices(final String directory) {
		if (directory.equals("")) {
			readProbSpecies(outDir + separator + "sim-rep.txt");
		}
		else {
			readProbSpecies(outDir + separator + directory + separator + "sim-rep.txt");
		}
		for (int i = 1; i < graphProbs.size(); i++) {
			String index = graphProbs.get(i);
			int j = i;
			while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
				graphProbs.set(j, graphProbs.get(j - 1));
				j = j - 1;
			}
			graphProbs.set(j, index);
		}
		JPanel speciesPanel1 = new JPanel(new GridLayout(graphProbs.size() + 1, 1));
		JPanel speciesPanel2 = new JPanel(new GridLayout(graphProbs.size() + 1, 2));
		use = new JCheckBox("Use");
		JLabel specs = new JLabel("Constraint");
		JLabel color = new JLabel("Color");
		boxes = new ArrayList<JCheckBox>();
		series = new ArrayList<JTextField>();
		colorsCombo = new ArrayList<JComboBox>();
		use.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (use.isSelected()) {
					for (JCheckBox box : boxes) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				}
				else {
					for (JCheckBox box : boxes) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		speciesPanel1.add(use);
		speciesPanel2.add(specs);
		speciesPanel2.add(color);
		final HashMap<String, Paint> colory = this.colors;
		for (int i = 0; i < graphProbs.size(); i++) {
			JCheckBox temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					if (((JCheckBox) e.getSource()).isSelected()) {
						node.setIcon(TextIcons.getIcon("g"));
						node.setIconName("" + (char) 10003);
						IconNode n = ((IconNode) node.getParent());
						while (n != null) {
							n.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
							n.setIconName("" + (char) 10003);
							if (n.getParent() == null) {
								n = null;
							}
							else {
								n = ((IconNode) n.getParent());
							}
						}
						tree.revalidate();
						tree.repaint();
						String s = series.get(i).getText();
						((JCheckBox) e.getSource()).setSelected(false);
						int[] cols = new int[34];
						for (int k = 0; k < boxes.size(); k++) {
							if (boxes.get(k).isSelected()) {
								if (colorsCombo.get(k).getSelectedItem().equals("Red")) {
									cols[0]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue")) {
									cols[1]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Green")) {
									cols[2]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Yellow")) {
									cols[3]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Magenta")) {
									cols[4]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan")) {
									cols[5]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Tan")) {
									cols[6]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Gray (Dark)")) {
									cols[7]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red (Dark)")) {
									cols[8]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue (Dark)")) {
									cols[9]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Green (Dark)")) {
									cols[10]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Dark)")) {
									cols[11]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Dark)")) {
									cols[12]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan (Dark)")) {
									cols[13]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Black")) {
									cols[14]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red ")) {
									cols[15]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Blue ")) {
									cols[16]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Green ")) {
									cols[17]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Yellow ")) {
									cols[18]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Magenta ")) {
									cols[19]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Cyan ")) {
									cols[20]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Gray (Light)")) {
									cols[21]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Red (Extra Dark)")) {
									cols[22]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Blue (Extra Dark)")) {
									cols[23]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Extra Dark)")) {
									cols[24]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Extra Dark)")) {
									cols[25]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Extra Dark)")) {
									cols[26]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Cyan (Extra Dark)")) {
									cols[27]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals("Red (Light)")) {
									cols[28]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Blue (Light)")) {
									cols[29]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Light)")) {
									cols[30]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Light)")) {
									cols[31]++;
								}
								else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Light)")) {
									cols[32]++;
								}
								else if (colorsCombo.get(k).getSelectedItem()
										.equals("Cyan (Light)")) {
									cols[33]++;
								}
							}
						}
						for (GraphProbs graph : probGraphed) {
							if (graph.getPaintName().equals("Red")) {
								cols[0]++;
							}
							else if (graph.getPaintName().equals("Blue")) {
								cols[1]++;
							}
							else if (graph.getPaintName().equals("Green")) {
								cols[2]++;
							}
							else if (graph.getPaintName().equals("Yellow")) {
								cols[3]++;
							}
							else if (graph.getPaintName().equals("Magenta")) {
								cols[4]++;
							}
							else if (graph.getPaintName().equals("Cyan")) {
								cols[5]++;
							}
							else if (graph.getPaintName().equals("Tan")) {
								cols[6]++;
							}
							else if (graph.getPaintName().equals("Gray (Dark)")) {
								cols[7]++;
							}
							else if (graph.getPaintName().equals("Red (Dark)")) {
								cols[8]++;
							}
							else if (graph.getPaintName().equals("Blue (Dark)")) {
								cols[9]++;
							}
							else if (graph.getPaintName().equals("Green (Dark)")) {
								cols[10]++;
							}
							else if (graph.getPaintName().equals("Yellow (Dark)")) {
								cols[11]++;
							}
							else if (graph.getPaintName().equals("Magenta (Dark)")) {
								cols[12]++;
							}
							else if (graph.getPaintName().equals("Cyan (Dark)")) {
								cols[13]++;
							}
							else if (graph.getPaintName().equals("Black")) {
								cols[14]++;
							}
							else if (graph.getPaintName().equals("Red ")) {
								cols[15]++;
							}
							else if (graph.getPaintName().equals("Blue ")) {
								cols[16]++;
							}
							else if (graph.getPaintName().equals("Green ")) {
								cols[17]++;
							}
							else if (graph.getPaintName().equals("Yellow ")) {
								cols[18]++;
							}
							else if (graph.getPaintName().equals("Magenta ")) {
								cols[19]++;
							}
							else if (graph.getPaintName().equals("Cyan ")) {
								cols[20]++;
							}
							else if (graph.getPaintName().equals("Gray (Light)")) {
								cols[21]++;
							}
							else if (graph.getPaintName().equals("Red (Extra Dark)")) {
								cols[22]++;
							}
							else if (graph.getPaintName().equals("Blue (Extra Dark)")) {
								cols[23]++;
							}
							else if (graph.getPaintName().equals("Green (Extra Dark)")) {
								cols[24]++;
							}
							else if (graph.getPaintName().equals("Yellow (Extra Dark)")) {
								cols[25]++;
							}
							else if (graph.getPaintName().equals("Magenta (Extra Dark)")) {
								cols[26]++;
							}
							else if (graph.getPaintName().equals("Cyan (Extra Dark)")) {
								cols[27]++;
							}
							else if (graph.getPaintName().equals("Red (Light)")) {
								cols[28]++;
							}
							else if (graph.getPaintName().equals("Blue (Light)")) {
								cols[29]++;
							}
							else if (graph.getPaintName().equals("Green (Light)")) {
								cols[30]++;
							}
							else if (graph.getPaintName().equals("Yellow (Light)")) {
								cols[31]++;
							}
							else if (graph.getPaintName().equals("Magenta (Light)")) {
								cols[32]++;
							}
							else if (graph.getPaintName().equals("Cyan (Light)")) {
								cols[33]++;
							}
						}
						((JCheckBox) e.getSource()).setSelected(true);
						series.get(i).setText(s);
						series.get(i).setSelectionStart(0);
						series.get(i).setSelectionEnd(0);
						int colorSet = 0;
						for (int j = 1; j < cols.length; j++) {
							if (cols[j] < cols[colorSet]) {
								colorSet = j;
							}
						}
						DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
						for (int j = 0; j < colorSet; j++) {
							draw.getNextPaint();
						}
						Paint paint = draw.getNextPaint();
						Object[] set = colory.keySet().toArray();
						for (int j = 0; j < set.length; j++) {
							if (paint == colory.get(set[j])) {
								colorsCombo.get(i).setSelectedItem(set[j]);
							}
						}
						boolean allChecked = true;
						for (JCheckBox temp : boxes) {
							if (!temp.isSelected()) {
								allChecked = false;
							}
						}
						if (allChecked) {
							use.setSelected(true);
						}
						probGraphed.add(new GraphProbs(colory.get(colorsCombo.get(i)
								.getSelectedItem()), (String) colorsCombo.get(i).getSelectedItem(),
								boxes.get(i).getName(), series.get(i).getText().trim(), i,
								directory));
					}
					else {
						boolean check = false;
						for (JCheckBox b : boxes) {
							if (b.isSelected()) {
								check = true;
							}
						}
						if (!check) {
							node.setIcon(MetalIconFactory.getTreeLeafIcon());
							node.setIconName("");
							boolean check2 = false;
							IconNode parent = ((IconNode) node.getParent());
							while (parent != null) {
								for (int j = 0; j < parent.getChildCount(); j++) {
									if (((IconNode) parent.getChildAt(j)).getIconName().equals(
											"" + (char) 10003)) {
										check2 = true;
									}
								}
								if (!check2) {
									parent.setIcon(MetalIconFactory.getTreeFolderIcon());
									parent.setIconName("");
								}
								check2 = false;
								if (parent.getParent() == null) {
									parent = null;
								}
								else {
									parent = ((IconNode) parent.getParent());
								}
							}
							tree.revalidate();
							tree.repaint();
						}
						ArrayList<GraphProbs> remove = new ArrayList<GraphProbs>();
						for (GraphProbs g : probGraphed) {
							if (g.getNumber() == i && g.getDirectory().equals(directory)) {
								remove.add(g);
							}
						}
						for (GraphProbs g : remove) {
							probGraphed.remove(g);
						}
						use.setSelected(false);
						colorsCombo.get(i).setSelectedIndex(0);
					}
				}
			});
			boxes.add(temp);
			JTextField seriesName = new JTextField(graphProbs.get(i));
			seriesName.setName("" + i);
			seriesName.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphProbs g : probGraphed) {
						if (g.getNumber() == i && g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyReleased(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphProbs g : probGraphed) {
						if (g.getNumber() == i && g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyTyped(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphProbs g : probGraphed) {
						if (g.getNumber() == i && g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}
			});
			series.add(seriesName);
			Object[] col = this.colors.keySet().toArray();
			Arrays.sort(col);
			JComboBox colBox = new JComboBox(col);
			colBox.setActionCommand("" + i);
			colBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					for (GraphProbs g : probGraphed) {
						if (g.getNumber() == i && g.getDirectory().equals(directory)) {
							g.setPaintName((String) ((JComboBox) e.getSource()).getSelectedItem());
							g.setPaint(colory.get(((JComboBox) e.getSource()).getSelectedItem()));
						}
					}
				}
			});
			colorsCombo.add(colBox);
			speciesPanel1.add(boxes.get(i));
			speciesPanel2.add(series.get(i));
			speciesPanel2.add(colorsCombo.get(i));
		}
		JPanel speciesPanel = new JPanel(new BorderLayout());
		speciesPanel.add(speciesPanel1, "West");
		speciesPanel.add(speciesPanel2, "Center");
		return speciesPanel;
	}

	private void readProbSpecies(String file) {
		graphProbs = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		try {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String[] ss = s.nextLine().split(" ");
				if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination")
						&& ss[3].equals("count:") && ss[4].equals("0")) {
					return;
				}
				if (data.size() == 0) {
					for (String add : ss) {
						data.add(add);
					}
				}
				else {
					for (int i = 0; i < ss.length; i++) {
						data.set(i, data.get(i) + " " + ss[i]);
					}
				}
			}
		}
		catch (Exception e) {
		}
		for (String s : data) {
			if (!s.split(" ")[0].equals("#total")) {
				graphProbs.add(s.split(" ")[0]);
			}
		}
	}

	private double[] readProbs(String file) {
		ArrayList<String> data = new ArrayList<String>();
		try {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String[] ss = s.nextLine().split(" ");
				if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination")
						&& ss[3].equals("count:") && ss[4].equals("0")) {
					return new double[0];
				}
				if (data.size() == 0) {
					for (String add : ss) {
						data.add(add);
					}
				}
				else {
					for (int i = 0; i < ss.length; i++) {
						data.set(i, data.get(i) + " " + ss[i]);
					}
				}
			}
		}
		catch (Exception e) {
		}
		double[] dataSet = new double[data.size()];
		double total = 0;
		int i = 0;
		if (data.get(0).split(" ")[0].equals("#total")) {
			total = Double.parseDouble(data.get(0).split(" ")[1]);
			i = 1;
		}
		for (; i < data.size(); i++) {
			if (total == 0) {
				dataSet[i] = Double.parseDouble(data.get(i).split(" ")[1]);
			}
			else {
				dataSet[i - 1] = 100 * ((Double.parseDouble(data.get(i).split(" ")[1])) / total);
			}
		}
		return dataSet;
	}

	private void fixProbGraph(String label, String xLabel, String yLabel,
			DefaultCategoryDataset dataset, BarRenderer rend) {
		chart = ChartFactory.createBarChart(label, xLabel, yLabel, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(java.awt.Color.WHITE);
		chart.getCategoryPlot().setRenderer(rend);
		ChartPanel graph = new ChartPanel(chart);
		if (probGraphed.isEmpty()) {
			graph.setLayout(new GridLayout(1, 1));
			JLabel edit = new JLabel("Double click here to create graph");
			edit.addMouseListener(this);
			Font font = edit.getFont();
			font = font.deriveFont(Font.BOLD, 42.0f);
			edit.setFont(font);
			edit.setHorizontalAlignment(SwingConstants.CENTER);
			graph.add(edit);
		}
		graph.addMouseListener(this);
		JPanel ButtonHolder = new JPanel();
		run = new JButton("Save and Run");
		save = new JButton("Save Graph");
		saveAs = new JButton("Save As");
		export = new JButton("Export");
		refresh = new JButton("Refresh");
		run.addActionListener(this);
		save.addActionListener(this);
		saveAs.addActionListener(this);
		export.addActionListener(this);
		refresh.addActionListener(this);
		if (reb2sac != null) {
			ButtonHolder.add(run);
		}
		ButtonHolder.add(save);
		ButtonHolder.add(saveAs);
		ButtonHolder.add(export);
		ButtonHolder.add(refresh);
		// JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// ButtonHolder, null);
		// splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		// this.add(splitPane, "South");
		this.revalidate();
	}

	public void refreshProb() {
		BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
		int thisOne = -1;
		for (int i = 1; i < probGraphed.size(); i++) {
			GraphProbs index = probGraphed.get(i);
			int j = i;
			while ((j > 0)
					&& (probGraphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
				probGraphed.set(j, probGraphed.get(j - 1));
				j = j - 1;
			}
			probGraphed.set(j, index);
		}
		ArrayList<GraphProbs> unableToGraph = new ArrayList<GraphProbs>();
		DefaultCategoryDataset histDataset = new DefaultCategoryDataset();
		for (GraphProbs g : probGraphed) {
			if (g.getDirectory().equals("")) {
				thisOne++;
				rend.setSeriesPaint(thisOne, g.getPaint());
				if (new File(outDir + separator + "sim-rep.txt").exists()) {
					readProbSpecies(outDir + separator + "sim-rep.txt");
					double[] data = readProbs(outDir + separator + "sim-rep.txt");
					for (int i = 1; i < graphProbs.size(); i++) {
						String index = graphProbs.get(i);
						double index2 = data[i];
						int j = i;
						while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
							graphProbs.set(j, graphProbs.get(j - 1));
							data[j] = data[j - 1];
							j = j - 1;
						}
						graphProbs.set(j, index);
						data[j] = index2;
					}
					if (graphProbs.size() != 0) {
						for (int i = 0; i < graphProbs.size(); i++) {
							if (g.getID().equals(graphProbs.get(i))) {
								g.setNumber(i);
								histDataset.setValue(data[i], g.getSpecies(), "");
							}
						}
					}
				}
				else {
					unableToGraph.add(g);
					thisOne--;
				}
			}
			else {
				thisOne++;
				rend.setSeriesPaint(thisOne, g.getPaint());
				if (new File(outDir + separator + g.getDirectory() + separator + "sim-rep.txt")
						.exists()) {
					readProbSpecies(outDir + separator + g.getDirectory() + separator
							+ "sim-rep.txt");
					double[] data = readProbs(outDir + separator + g.getDirectory() + separator
							+ "sim-rep.txt");
					for (int i = 1; i < graphProbs.size(); i++) {
						String index = graphProbs.get(i);
						double index2 = data[i];
						int j = i;
						while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
							graphProbs.set(j, graphProbs.get(j - 1));
							data[j] = data[j - 1];
							j = j - 1;
						}
						graphProbs.set(j, index);
						data[j] = index2;
					}
					if (graphProbs.size() != 0) {
						for (int i = 0; i < graphProbs.size(); i++) {
							String compare = g.getID().replace(" (", "~");
							if (compare.split("~")[0].trim().equals(graphProbs.get(i))) {
								histDataset.setValue(data[i], g.getSpecies(), "");
							}
						}
					}
				}
				else {
					unableToGraph.add(g);
					thisOne--;
				}
			}
		}
		for (GraphProbs g : unableToGraph) {
			probGraphed.remove(g);
		}
		fixProbGraph(chart.getTitle().getText(),
				chart.getCategoryPlot().getDomainAxis().getLabel(), chart.getCategoryPlot()
						.getRangeAxis().getLabel(), histDataset, rend);
	}

	private void updateSpecies() {
		String background;
		try {
			Properties p = new Properties();
			String[] split = outDir.split(separator);
			FileInputStream load = new FileInputStream(new File(outDir + separator
					+ split[split.length - 1] + ".lrn"));
			p.load(load);
			load.close();
			if (p.containsKey("genenet.file")) {
				String[] getProp = p.getProperty("genenet.file").split(separator);
				background = outDir
						.substring(0, outDir.length() - split[split.length - 1].length())
						+ separator + getProp[getProp.length - 1];
			}
			else if (p.containsKey("learn.file")) {
				String[] getProp = p.getProperty("learn.file").split(separator);
				background = outDir
						.substring(0, outDir.length() - split[split.length - 1].length())
						+ separator + getProp[getProp.length - 1];
			}
			else {
				background = null;
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable to load background file.",
					"Error", JOptionPane.ERROR_MESSAGE);
			background = null;
		}
		learnSpecs = new ArrayList<String>();
		if (background != null) {
			if (background.contains(".gcm")) {
				GCMFile gcm = new GCMFile(biomodelsim.getRoot());
				gcm.load(background);
				HashMap<String, Properties> speciesMap = gcm.getSpecies();
				for (String s : speciesMap.keySet()) {
					learnSpecs.add(s);
				}
			}
			else if (background.contains(".lpn")) {
				LhpnFile lhpn = new LhpnFile(biomodelsim.log);
				lhpn.load(background);
				HashMap<String, Properties> speciesMap = lhpn.getContinuous();
				/*for (String s : speciesMap.keySet()) {
					learnSpecs.add(s);
				}*/
				//ADDED BY SB.
				TSDParser extractVars;
				ArrayList<String> datFileVars = new ArrayList<String>();
				//ArrayList<String> allVars = new ArrayList<String>();
				Boolean varPresent = false;
				//Finding the intersection of all the variables present in all data files.
				for (int i = 1; (new File(outDir + separator + "run-" + i + ".tsd")).exists(); i++) {
					extractVars = new TSDParser(outDir + separator + "run-" + i + ".tsd", biomodelsim,false);
					datFileVars = extractVars.getSpecies();
					if (i == 1){
						learnSpecs.addAll(datFileVars);
					}
					for (String s : learnSpecs){
						varPresent = false;
						for (String t : datFileVars){
							if (s.equalsIgnoreCase(t)){
								varPresent = true;
								break;
							}
						}
						if (!varPresent){
							learnSpecs.remove(s);
						}
					}
				}
				//END ADDED BY SB.
			}
			else {
				SBMLDocument document = BioSim.readSBML(background);
				Model model = document.getModel();
				ListOf ids = model.getListOfSpecies();
				for (int i = 0; i < model.getNumSpecies(); i++) {
					learnSpecs.add(((Species) ids.get(i)).getId());
				}
			}
		}
		for (int i = 0; i < learnSpecs.size(); i++) {
			String index = learnSpecs.get(i);
			int j = i;
			while ((j > 0) && learnSpecs.get(j - 1).compareToIgnoreCase(index) > 0) {
				learnSpecs.set(j, learnSpecs.get(j - 1));
				j = j - 1;
			}
			learnSpecs.set(j, index);
		}
	}

	public boolean getWarning() {
		return warn;
	}

	private class GraphProbs {
		private Paint paint;

		private String species, directory, id, paintName;

		private int number;

		private GraphProbs(Paint paint, String paintName, String id, String species, int number,
				String directory) {
			this.paint = paint;
			this.paintName = paintName;
			this.species = species;
			this.number = number;
			this.directory = directory;
			this.id = id;
		}

		private Paint getPaint() {
			return paint;
		}

		private void setPaint(Paint p) {
			paint = p;
		}

		private String getPaintName() {
			return paintName;
		}

		private void setPaintName(String p) {
			paintName = p;
		}

		private String getSpecies() {
			return species;
		}

		private void setSpecies(String s) {
			species = s;
		}

		private String getDirectory() {
			return directory;
		}

		private String getID() {
			return id;
		}

		private int getNumber() {
			return number;
		}

		private void setNumber(int n) {
			number = n;
		}
	}

	private Hashtable makeIcons() {
		Hashtable<String, Icon> icons = new Hashtable<String, Icon>();
		icons.put("floppyDrive", MetalIconFactory.getTreeFloppyDriveIcon());
		icons.put("hardDrive", MetalIconFactory.getTreeHardDriveIcon());
		icons.put("computer", MetalIconFactory.getTreeComputerIcon());
		icons.put("c", TextIcons.getIcon("c"));
		icons.put("java", TextIcons.getIcon("java"));
		icons.put("html", TextIcons.getIcon("html"));
		return icons;
	}
}

class IconNodeRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -940588131120912851L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		Icon icon = ((IconNode) value).getIcon();

		if (icon == null) {
			Hashtable icons = (Hashtable) tree.getClientProperty("JTree.icons");
			String name = ((IconNode) value).getIconName();
			if ((icons != null) && (name != null)) {
				icon = (Icon) icons.get(name);
				if (icon != null) {
					setIcon(icon);
				}
			}
		}
		else {
			setIcon(icon);
		}

		return this;
	}
}

class IconNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2887169888272379817L;

	protected Icon icon;

	protected String iconName;

	private String hiddenName;

	public IconNode() {
		this(null, "");
	}

	public IconNode(Object userObject, String name) {
		this(userObject, true, null, name);
	}

	public IconNode(Object userObject, boolean allowsChildren, Icon icon, String name) {
		super(userObject, allowsChildren);
		this.icon = icon;
		hiddenName = name;
	}

	public String getName() {
		return hiddenName;
	}

	public void setName(String name) {
		hiddenName = name;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getIconName() {
		if (iconName != null) {
			return iconName;
		}
		else {
			String str = userObject.toString();
			int index = str.lastIndexOf(".");
			if (index != -1) {
				return str.substring(++index);
			}
			else {
				return null;
			}
		}
	}

	public void setIconName(String name) {
		iconName = name;
	}

}

class TextIcons extends MetalIconFactory.TreeLeafIcon {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1623303213056273064L;

	protected String label;

	private static Hashtable<String, String> labels;

	protected TextIcons() {
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		super.paintIcon(c, g, x, y);
		if (label != null) {
			FontMetrics fm = g.getFontMetrics();

			int offsetX = (getIconWidth() - fm.stringWidth(label)) / 2;
			int offsetY = (getIconHeight() - fm.getHeight()) / 2 - 2;

			g.drawString(label, x + offsetX, y + offsetY + fm.getHeight());
		}
	}

	public static Icon getIcon(String str) {
		if (labels == null) {
			labels = new Hashtable<String, String>();
			setDefaultSet();
		}
		TextIcons icon = new TextIcons();
		icon.label = (String) labels.get(str);
		return icon;
	}

	public static void setLabelSet(String ext, String label) {
		if (labels == null) {
			labels = new Hashtable<String, String>();
			setDefaultSet();
		}
		labels.put(ext, label);
	}

	private static void setDefaultSet() {
		labels.put("c", "C");
		labels.put("java", "J");
		labels.put("html", "H");
		labels.put("htm", "H");
		labels.put("g", "" + (char) 10003);

		// and so on
		/*
		 * labels.put("txt" ,"TXT"); labels.put("TXT" ,"TXT"); labels.put("cc"
		 * ,"C++"); labels.put("C" ,"C++"); labels.put("cpp" ,"C++");
		 * labels.put("exe" ,"BIN"); labels.put("class" ,"BIN");
		 * labels.put("gif" ,"GIF"); labels.put("GIF" ,"GIF");
		 * 
		 * labels.put("", "");
		 */
	}
}
