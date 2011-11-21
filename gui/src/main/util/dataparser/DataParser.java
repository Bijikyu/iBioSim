package main.util.dataparser;

import java.awt.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.*;

import main.*;

public class DataParser {

	protected ArrayList<String> species;

	protected ArrayList<ArrayList<Double>> data;

	protected Component component;

	protected boolean warning;

	public DataParser(ArrayList<String> species, ArrayList<ArrayList<Double>> data) {
		this.species = species;
		this.data = data;
		component = Gui.frame;
	}

	public ArrayList<String> getSpecies() {
		return species;
	}

	public ArrayList<ArrayList<Double>> getData() {
		return data;
	}

	public void setSpecies(ArrayList<String> species) {
		this.species = species;
	}

	public void setData(ArrayList<ArrayList<Double>> data) {
		this.data = data;
	}

	public boolean getWarning() {
		return warning;
	}

	public void outputTSD(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write("(");
			out.write("(");
			if (species.size() > 0) {
				out.write("\"" + species.get(0) + "\"");
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(",\"" + species.get(i) + "\"");
			}
			out.write(")");
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write(",(");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write("," + data.get(j).get(i));
				}
				out.write(")");
			}
			out.write(")");
			out.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A TSD File!", "Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void outputCSV(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			if (species.size() > 0) {
				out.write(species.get(0));
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(", " + species.get(i));
			}
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write("\n");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write(", " + data.get(j).get(i));
				}
			}
			out.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A CVS File!", "Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void outputDAT(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write("#");
			if (species.size() > 0) {
				out.write("\"" + species.get(0) + "\"");
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(" \"" + species.get(i) + "\"");
			}
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write("\n");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write(" " + data.get(j).get(i));
				}
			}
			out.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A DAT File!", "Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * returns the number of samples in the data.
	 * 
	 * @return
	 */
	public int getNumSamples() {
		if (this.data.size() == 0)
			return 0;
		return this.data.get(0).size();
	}

	/**
	 * get the data in a different format
	 * 
	 * @return
	 */
	public HashMap<String, ArrayList<Double>> getHashMap() {
		HashMap<String, ArrayList<Double>> out = new HashMap<String, ArrayList<Double>>();
		for (int i = 0; i < data.size(); i++) {
			out.put(species.get(i), data.get(i));
		}
		return out;
	}

	/**
	 * A helper function. Read a file into a string. Thanks to erickson at
	 * http:/
	 * /stackoverflow.com/questions/326390/how-to-create-a-java-string-from
	 * -the-contents-of-a-file
	 * 
	 * @param path
	 *            : the path to the file
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}

}
