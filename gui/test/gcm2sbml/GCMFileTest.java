package gcm2sbml;


import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.GlobalConstants;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Before;

public class GCMFileTest extends TestCase{

	@Before
	public void setUp() throws Exception {
		System.loadLibrary("sbmlj");
	}
	
	public void testLoadandSave() { 
//		GCMFile file = new GCMFile();		
//		file.load("gcm"+ File.separator +"bar.gcm");
//		file.setSBMLFile("foo.sbml");
//		file.save("gcm"+ File.separator +"baz.gcm");
	}
	
	public void testMerge() {
		GCMParser parser = new GCMParser("gcm"+ File.separator +"baz.gcm");
		GeneticNetwork network = parser.buildNetwork();				
		network.mergeSBML("gcm"+ File.separator +"baz.sbml");
	}
	
	public void testAddProperty() {
//		GCMFile file = new GCMFile();
//		file.load(filename);
//		Properties property = new Properties();
//		property.put("hi", "fun");
//		file.addInfluences("g -> a", property);
//		file.save("nand3.dot");
	}
	
	public void testParseInfluence() {
		GCMFile file = new GCMFile();
		String name = "input -> output, Promoter promo";
		assertEquals("Should get correct input", "input", file.getInput(name));
		assertEquals("Should get correct output", "output", file.getOutput(name));
		assertEquals("Should get correct promoter", "promo", file.getPromoter(name));
	}
	
	public void testProperties() {
		Properties prop = new Properties();
		prop.put(GlobalConstants.MAX_DIMER_STRING, GlobalConstants.MAX_DIMER_VALUE);
		assertTrue("Couldn't find value", prop.containsKey(GlobalConstants.MAX_DIMER_STRING));
	}
	
	String filename = "nand.dot";

}
