package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.NullSpecies;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p,
			double act, double kdimer, double kcoop, double kbio, double dimer) {
		super(document);
		this.promoter = p;
		this.defaultact = act;
		this.defaultkdimer = kdimer;
		this.defaultkcoop = kcoop;
		this.defaultkbio = kbio;
		this.defaultdimer = dimer;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getActivators()) {
			specie.accept(this);
		}
	}

	public void visitNullSpecies(NullSpecies specie) {
	}

	public void visitSpecies(SpeciesInterface specie) {
	}

	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		String actName = "";
		if (dimerizationAbstraction) {
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork
					.getMoleParameter((int) dimer)));
			actName = kdimerString + "*" + "(" + specie.getMonomer() + ")^"
					+ reaction.getDimer();
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(),
					dimer * coop));
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(actName, coop));
		}
		kl.addParameter(Utility.Parameter(actString, Math.pow(act, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		String actName = "";
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		if (biochemicalAbstraction) {
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), coop));
				names = names + "*" + s.getId();
			}

			kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork
					.getMoleParameter((int) specie.getInputs().size())));
			actName = "(" + kbioString + names + ")^" + coop;
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter(actString, Math.pow(act, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(actString, Math.pow(act, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(actString, Math.pow(act, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(actString, Math.pow(act, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	private void loadValues(Properties property) {
		kdimer = getProperty(GlobalConstants.KASSOCIATION_STRING, property,
				defaultkdimer);
		kbio = getProperty(GlobalConstants.KBIO_STRING, property, defaultkbio);
		kcoop = getProperty(GlobalConstants.COOPERATIVITY_STRING, property,
				defaultkcoop);
		act = getProperty(GlobalConstants.KACT_STRING, property, defaultact);
		dimer = getProperty(GlobalConstants.MAX_DIMER_STRING, property,
				defaultdimer);
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
	private String generateLaw(String specieName, String actMolecule) {
		String law = actString + "*" + "RNAP*" + actMolecule + "^"
				+ kcoopString + "*" + promoter.getId() + "-kr*" + "RNAP_"
				+ promoter.getId() + "_" + specieName;
		return law;
	}

	private String kdimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String kbioString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KBIO_STRING);
	private String kcoopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String actString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KACT_STRING);
	private String dimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.MAX_DIMER_STRING);

	private Promoter promoter = null;

	private double defaultkdimer = .5;
	private double defaultkbio = .05;
	private double defaultkcoop = 1;
	private double defaultact = .033;
	private double defaultdimer = 1;

	private double dimer = 1;
	private double kdimer = .5;
	private double kbio = .05;
	private double kcoop = 1;
	private double act = .033;
}
