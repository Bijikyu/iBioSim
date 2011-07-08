package parser;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class DATParser extends Parser {
	public DATParser(String filename, boolean warn) {
		super(new ArrayList<String>(), new ArrayList<ArrayList<Double>>());
		try {
			warning = warn;
			boolean warning2 = warning;
			boolean warning3 = warning;
			FileInputStream fileInput = new FileInputStream(new File(filename));
			ProgressMonitorInputStream prog = new ProgressMonitorInputStream(component, "Reading Reb2sac Output Data From "
					+ new File(filename).getName(), fileInput);
			InputStream input = new BufferedInputStream(prog);
			boolean reading = true;
			char cha = 0;
			boolean rightAfterPound = false;
			boolean usingQuotes = false;
			boolean junkLine = false;
			boolean endJunk = false;
			while (reading) {
				String word = "";
				boolean readWord = true;
				boolean withinWord = false;
				boolean moveToData = false;
				boolean withinParens = false;
				while (readWord && !moveToData) {
					int read = input.read();
					if (read == -1) {
						reading = false;
						readWord = false;
					}
					cha = (char) read;
					if (withinWord) {
						if (usingQuotes) {
							if (cha == '\"') {
								withinWord = false;
								readWord = false;
							}
							else {
								word += cha;
							}
						}
						else if (withinParens) {
							if (cha == ')') {
								withinWord = false;
								readWord = false;
								withinParens = false;
							}
							else if (cha == ' ' && word.equals("")) {
							}
							else {
								word += cha;
							}
						}
						else {
							if (Character.isWhitespace(cha)) {
								withinWord = false;
								readWord = false;
								if (cha == '\n') {
									if (junkLine) {
										endJunk = true;
									}
									else {
										moveToData = true;
									}
									junkLine = false;
								}
							}
							else {
								word += cha;
							}
						}
					}
					else {
						if (cha == '\n') {
							if (!junkLine) {
								moveToData = true;
							}
							junkLine = false;
						}
						else if (Character.isWhitespace(cha)) {
						}
						else if (cha == '#') {
							rightAfterPound = true;
						}
						else if (cha == '\"') {
							withinWord = true;
							if (rightAfterPound) {
								usingQuotes = true;
								rightAfterPound = false;
							}
						}
						else if (cha == '(') {
							withinParens = true;
							if (rightAfterPound) {
								usingQuotes = false;
								rightAfterPound = false;
							}
						}
						else if (cha == ',' && withinParens) {
							if (!usingQuotes) {
								withinWord = true;
							}
						}
						else if (cha == ',' && !withinParens) {
						}
						else {
							withinWord = true;
							word += cha;
						}
					}
				}
				if (word.equals("Variables:") || word.equals("Points:")) {
					junkLine = true;
				}
				if (!word.equals("") && !junkLine) {
					if (!endJunk) {
						species.add(word);
					}
					endJunk = false;
				}
				if (moveToData) {
					for (int i = 0; i < species.size(); i++) {
						data.add(new ArrayList<Double>());
					}
				}
				int counter = 0;
				int dataPoints = 0;
				while (moveToData) {
					word = "";
					readWord = true;
					int read;
					while (readWord) {
						read = input.read();
						cha = (char) read;
						while (!Character.isWhitespace(cha) && cha != ',' && cha != ':' && cha != ';' && cha != '!' && cha != '?' && cha != '\"'
								&& cha != '\'' && cha != '(' && cha != ')' && cha != '{' && cha != '}' && cha != '[' && cha != ']' && cha != '<'
								&& cha != '>' && cha != '_' && cha != '*' && cha != '=' && read != -1) {
							word += cha;
							read = input.read();
							cha = (char) read;
						}
						if (read == -1) {
							reading = false;
							moveToData = false;
						}
						readWord = false;
					}
					int insert;
					if (!word.equals("")) {
						if (word.equals("nan")) {
							if (!warning) {
								JOptionPane.showMessageDialog(component, "Found NAN in data." + "\nReplacing with 0s.", "NAN In Data",
										JOptionPane.WARNING_MESSAGE);
								warning = true;
							}
							word = "0";
						}
						if (word.equals("inf")) {
							if (!warning2) {
								JOptionPane.showMessageDialog(component, "Found INF in data." + "\nReplacing with " + Double.MAX_VALUE + ".",
										"INF In Data", JOptionPane.WARNING_MESSAGE);
								warning2 = true;
							}
							word = "" + Double.MAX_VALUE;
						}
						if (counter < species.size()) {
							insert = counter;
						}
						else {
							insert = counter % species.size();
						}
						(data.get(insert)).add(Double.parseDouble(word));
						counter++;
						dataPoints++;
						if (cha == '\n') {
							if (dataPoints > species.size()) {
								JOptionPane.showMessageDialog(component, "Time point includes more data than number of species", "Extra Data",
										JOptionPane.ERROR_MESSAGE);
								throw new ArrayIndexOutOfBoundsException();
							}
							if (dataPoints < species.size()) {
								JOptionPane.showMessageDialog(component, "Time point includes less data than number of species", "Missing Data",
										JOptionPane.ERROR_MESSAGE);
								throw new ArrayIndexOutOfBoundsException();
							}
							dataPoints = 0;
						}
					}
				}
			}
			input.close();
			prog.close();
			fileInput.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(component, "Error Reading Data!" + "\nThere was an error reading the simulation data file.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(component, "Error Reading Data!" + "\nNon-numeric data found in the simulation file.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
			species.clear();
			data.clear();
		}
	}
}
