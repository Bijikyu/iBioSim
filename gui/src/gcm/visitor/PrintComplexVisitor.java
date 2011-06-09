package gcm.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import gcm.network.ComplexSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.Influence;
import gcm.network.SpeciesInterface;
import gcm.parser.CompatibilityFixer;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document, HashMap<String, SpeciesInterface> species,
			ArrayList<String> compartments, HashMap<String, ArrayList<Influence>> complexMap, 
			HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartments = compartments;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface s : species.values()) {
			if (!complexAbstraction || (!s.isAbstractable() && !s.isSequesterAbstractable()))
				s.accept(this);
		}
	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction("Complex_formation_" + specie.getId());
		r.setCompartment(compartment);
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		String compExpression = "";
		String boundExpression = specie.getId();
		String kcompId = kcompString + "__" + specie.getId();
		String ncSum = "";
		double stoich = 0;
		if (complexAbstraction) {
			compExpression = abstractComplex(specie.getId(), 1);
			int index = compExpression.indexOf('*');
			compExpression = compExpression.substring(index, compExpression.length());
			for (Influence infl : complexMap.get(specie.getId())) {
				stoich += infl.getCoop();
				String partId = infl.getInput();
				String nId = coopString + "__" + partId + "_" + specie.getId();
				ncSum = ncSum + nId + "+";
			}
			if (specie.isSequesterable())
				boundExpression = sequesterSpecies(specie.getId(), 0);
		} else {
			kl.addParameter(Utility.Parameter(kcompId, kcomp,
					GeneticNetwork.getMoleParameter(2)));
			for (Influence infl : complexMap.get(specie.getId())) {
				String partId = infl.getInput();
				stoich += infl.getCoop();
				r.addReactant(Utility.SpeciesReference(partId, infl.getCoop()));
				String nId = coopString + "__" + partId + "_" + specie.getId();
				kl.addParameter(Utility.Parameter(nId, infl.getCoop(), "dimensionless"));
				ncSum = ncSum + nId + "+";
				compExpression = compExpression + "*" + "(" + partId + ")^" + nId;
			}
		}
		if (stoich == 1)
			kl.addParameter(Utility.Parameter("kf", kf, GeneticNetwork.getMoleTimeParameter(1)));
		else 
			kl.addParameter(Utility.Parameter("kf", kf, GeneticNetwork.getMoleTimeParameter(2)));
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(compExpression, boundExpression, kcompId, ncSum, stoich));
		Utility.addReaction(document, r);
	}
	
	private String generateLaw(String compExpression, String boundExpression, String kcompId, String ncSum, double stoich) {
		String law = "";
		if (stoich == 1 || stoich == 2)
			law = "kf" + compExpression + "-kr*" + boundExpression;
		else
			law = "kf*" + kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-2)" + compExpression + "-kr*" + boundExpression;
		return law;
	}
	
	//Checks whether equilibrium constants are given as forward and reverse rate constants before loading values
	private void loadValues(SpeciesInterface s) {
		double[] kcompArray = s.getKc();
		kf = kcompArray[0];
		if (kcompArray.length == 2) {
			kcomp = kcompArray[0]/kcompArray[1];
			kr = kcompArray[1];
		} else {
			kcomp = kcompArray[0];
			kr = 1;
		}
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = document.getModel().getCompartment(0).getId();
		String[] splitted = species.split("__");
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
		return compartment;
	}
	
	private ArrayList<String> compartments;
	
	private double kf;
	private double kcomp;
	private double kr;

	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	
}
