package gcm.visitor;



import java.util.ArrayList;
import java.util.HashMap;

import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.NullSpecies;
import gcm.network.Promoter;
import gcm.network.Influence;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import org.sbml.libsbml.SBMLDocument;

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p, HashMap<String, SpeciesInterface> species, 
			String compartment, 
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.promoter = p;
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartment = compartment;
		if (compartment.equals(document.getModel().getCompartment(0).getId()))
			rnapId = "RNAP";
		else
			rnapId = compartment + "__RNAP";
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getActivators()) {
			String activator = specie.getId();
			String[] splitted = activator.split("__");
			if (splitted.length > 1)
				activator = splitted[1];
			boundId = promoter.getId() + "_" + activator + "_RNAP";
			reactionId = "R_RNAP_binding_" + promoter.getId() + "_" + activator;
			specie.accept(this);
		}
	}

	@Override
	public void visitNullSpecies(NullSpecies specie) {
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapId, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kf_ao", kf, GeneticNetwork.getMoleTimeParameter(2)));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actExpression = "";
		//When abstracting, addition of reactants and modifiers to the reaction is handled by AbstractionEngine
		if (complexAbstraction && specie.isAbstractable()) {
			actExpression = abstractComplex(specie.getId(), coop);
		} else if (complexAbstraction && specie.isSequesterable()) {
			actExpression = sequesterSpecies(specie.getId(), coop);
		} else {
			actExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter("kr_ao", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(actExpression));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapId, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kf_ao", kf, GeneticNetwork.getMoleTimeParameter(2)));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			actExpression = actExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			actExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter("kr_ao", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(actExpression));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapId, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kf_ao", kf, GeneticNetwork.getMoleTimeParameter(2)));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));;
		String actExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			actExpression = actExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			actExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter("kr_ao", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(actExpression));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapId, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kf_ao", kf, GeneticNetwork.getMoleTimeParameter(2)));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			actExpression = actExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			actExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter("kr_ao", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(actExpression));
		Utility.addReaction(document, r);
	}

	/**
	 * Generates a kinetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repMolecule
	 *            repressor molecule
	 * @return
	 */
	//Forward and reverse rate constants are of those of the activated equilibrium binding constant for RNA polymerase
	private String generateLaw(String actExpression) {
		String law = "kf_ao*" + "(" + kactString + "*" + actExpression + ")" + "^"
				+ coopString + "*" + rnapId + "*" + promoter.getId() + "-kr_ao*" + boundId;
		return law;
	}
	
	// Checks if binding parameters are specified as forward and reverse rate constants or
	// as equilibrium binding constants before loading values
	private void loadValues(SpeciesInterface s) {
		double[] kArnapArray = promoter.getKArnap();
		kf = kArnapArray[0];
		if (kArnapArray.length == 2) {
			kr = kArnapArray[1];
		} else {
			kr = 1;
		}	
		Influence r = promoter.getActivationMap().get(s.getId());
		coop = r.getCoop();
		double[] kactArray = r.getAct();
		if (kactArray.length == 2)
			kact = kactArray[0]/kactArray[1];
		 else 
			kact = kactArray[0];
	}

	private Promoter promoter;

	private double coop;
	private double kf;
	private double kr;
	private double kact;
	
	private String kactString = GlobalConstants.KACT_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	

	private String boundId;
	private String reactionId;
	private String rnapId;
	private String compartment;
}
