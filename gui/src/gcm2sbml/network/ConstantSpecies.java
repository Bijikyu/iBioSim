package gcm2sbml.network;

import java.util.Properties;
import gcm2sbml.visitor.SpeciesVisitor;

/**
 * This represents a constant species.
 * @author Nam
 *
 */
public class ConstantSpecies extends AbstractSpecies {
	public ConstantSpecies(String name, String stateName, Properties properties) {
		this.properties = properties;
		this.id = name;
		this.stateName = stateName;
	}
	
	public ConstantSpecies() {
		super();
	}
	
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitConstantSpecies(this);
	}
}
