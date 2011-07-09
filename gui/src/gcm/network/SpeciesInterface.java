package gcm.network;

import java.util.Properties;

import gcm.visitor.VisitableSpecies;


/**
 * This interface describes a species. Species are nodes in the graph
 * 
 * @author Nam Nguyen
 * 
 */
public interface SpeciesInterface extends VisitableSpecies {

	/**
	 * Returns the id of the species
	 * 
	 * @return the id of the species
	 */
	public String getId();
	/**
	 * Sets the id of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setId(String name);

	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	public String getName();
	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setName(String name);

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	public String getStateName();

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	public void setStateName(String stateName);
	
	/**
	 * Sets the properties of the specie
	 * @param properties the property to set
	 */
	public void setProperties(Properties properties);
	
	/**
	 * Returns the property of the specie
	 * @return the property of the specie
	 */
	public Properties getProperties();

	/**
	 * Adds a new value to the specie
	 * @param key the key of the value
	 * @param value the value to add
	 */
	public void addProperty(String key, String value);
	
	/**
	 * Returns the property with the given key
	 * @param key the key of the property
	 * @return
	 */
	public String getProperty(String key);
	
	public double getInitialAmount();
	
	public double getInitialConcentration();
	
	public double getDecay();
	
	public double[] getKc();
		
	public double[] getKmdiff();
	
	public boolean isActivator();
	
	public void setActivator(boolean set);
	
	public boolean isRepressor();
	
	public void setRepressor(boolean set);
	
	public boolean isAbstractable();
	
	public void setAbstractable(boolean set);
	
	public boolean isSequesterAbstractable();
	
	public void setSequesterAbstractable(boolean set);

	public boolean isSequesterable();
	
	public void setSequesterable(boolean set);
	
}
