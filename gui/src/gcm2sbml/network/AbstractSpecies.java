package gcm2sbml.network;

import gcm2sbml.util.GlobalConstants;

import java.util.Properties;

/**
 * This class describes an abstract species class. Species are nodes in the
 * graph. All species are derived from this class.
 * 
 * @author Nam Nguyen
 * 
 */
public abstract class AbstractSpecies implements SpeciesInterface {

	/**
	 * Empty constructor.
	 * 
	 */
	public AbstractSpecies() {
		properties = new Properties();
	}
	
	/**
	 * Returns the id of the species
	 * 
	 * @return the id of the species
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the species
	 * 
	 * @param id
	 *            the id of the species
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	public String getStateName() {
		return stateName;
	}

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public String toString() {
		return getId();
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();			
		}
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return null;
		}
		return properties.getProperty(key);
	}
	
	public boolean containsKey(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return false;
		}
		return true;
	}
	
	public double getInit() {
		return Double.parseDouble(getProperty(GlobalConstants.INITIAL_STRING));
	}
	
	public double getDecay() {
		return Double.parseDouble(getProperty(GlobalConstants.KDECAY_STRING));
	}
	
	public double getKc() {
		return Double.parseDouble(getProperty(GlobalConstants.KCOMPLEX_STRING));
	}
	
	protected Properties properties;
	
	// The id of the species
	protected String id;

	// The name of the species
	protected String name;

	// The state associated with the species
	protected String stateName;
}
