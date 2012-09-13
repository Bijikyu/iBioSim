package lpn.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

//import org.apache.batik.svggen.font.table.Program;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Constraint;
//import org.sbml.libsbml.Delay;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.libsbml;

import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.util.GlobalConstants;

import lpn.parser.ExprTree;
import main.Gui;

/**
 * This class converts a lph file to a sbml file
 * 
 * @author Zhen Zhang
 * 
 */
public class Translator {
	private String filename;
	private SBMLDocument document;
	public static boolean isSteadyState = false;
	public static boolean isHSF = false;
	
	public void oldBuildTemplate(String lhpnFilename, String property) {
		this.filename = lhpnFilename.replace(".lpn", ".xml");
		// load lhpn file
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		//document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		document = new SBMLDocument(3,1);
		Model m = document.createModel();
		Compartment c = m.createCompartment();
		m.setId(filename.replace(".xml", ""));
		c.setId("default");
		c.setSize(1.0);
		c.setConstant(true);
		c.setSpatialDimensions(3);
		
		// Create bitwise operators for sbml
		createFunction(m, "rate", "Rate", "lambda(a,a)");
		createFunction(m, "BIT", "bit selection", "lambda(a,b,a*b)");
		createFunction(m, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITOR", "Bitwise OR", "lambda(a,b,a*b)");
		createFunction(m, "BITNOT", "Bitwise NOT", "lambda(a,b,a*b)");
		createFunction(m, "BITXOR", "Bitwise XOR", "lambda(a,b,a*b)");
		createFunction(m, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
		//createFunction(m, "and", "Logical AND", "lambda(a,b,a*b)");
		createFunction(m, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(m, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(m, "exponential", "Exponential distribution", "lambda(l,1/l)");
		createFunction(m, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(m, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(m, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(m, "laplace", "Laplace distribution", "lambda(a,0)");
		createFunction(m, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(m, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
		createFunction(m, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(m, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(m, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
		
		// translate from lhpn to sbml
		// ----variables -> parameters-----	
		for (String v: lhpn.getVariables()){
			if (v != null){
				String initVal = lhpn.getInitialVal(v);
				if (lhpn.isContinuous(v) || lhpn.isInteger(v)){
					Parameter var = m.createParameter(); 
					var.setConstant(false);
					var.setId(v);				
					Pattern initVarIsIntPattern = Pattern.compile(Int);
					Matcher initVarIsIntMatcher = initVarIsIntPattern.matcher(initVal);
					boolean initVarIsInt = initVarIsIntMatcher.matches();
					Pattern initVarIsRangePattern = Pattern.compile(Range);
					Matcher initVarIsRangeMatcher = initVarIsRangePattern.matcher(initVal);
					boolean initVarIsRange = initVarIsRangeMatcher.matches();
					Pattern initRangeBoundPattern = Pattern.compile(Range);
					Matcher initRangeBoundMatcher = initRangeBoundPattern.matcher(initVal); 
					boolean initVarRangeFound = initRangeBoundMatcher.find();
					if (initVarIsInt && !initVarIsRange) {
						double initValDouble = Double.parseDouble(initVal);
						var.setValue(initValDouble);
					}
					if (!initVarIsInt && initVarIsRange && initVarRangeFound) {
						var.setValue(0);
						InitialAssignment initAssign = m.createInitialAssignment();
						initAssign.setSymbol(var.getId());
						String initVarAssignRHS = "uniform(";
						for (int i=1; i<=initRangeBoundMatcher.groupCount();i++) {
							initVarAssignRHS = initVarAssignRHS + initRangeBoundMatcher.group(i);
							//initAssignRHS = initAssignRHS + initRangeBoundMatcher.group(i);
							if (i==1)
								initVarAssignRHS = initVarAssignRHS + ",";
						}
						initVarAssignRHS = initVarAssignRHS + ")";
						initAssign.setMath(SBMLutilities.myParseFormula(initVarAssignRHS));
					}
					// For each continuous variable v, create rate rule dv/dt and set its initial value to lhpn.getInitialRate(v). 
					if (lhpn.isContinuous(v)){
						Parameter rateVar = m.createParameter();
						rateVar.setConstant(false);
						rateVar.setId(v + "_rate");
						RateRule rateRule = m.createRateRule();
						rateRule.setVariable(v);
						rateRule.setMath(SBMLutilities.myParseFormula(rateVar.getId()));
						String initRate= lhpn.getInitialRate(v);
						boolean initRateIsInt = Pattern.matches(Int, initRate);
						boolean initRateIsRange = Pattern.matches(Range, initRate);
						Pattern initRateRangePattern = Pattern.compile(Range);
						Matcher initRateRangeMatcher = initRateRangePattern.matcher(initRate); 
						boolean initRateRangeFound = initRateRangeMatcher.find();
						
						if (initRateIsInt && !initRateIsRange) {
							double initRateDouble = Double.parseDouble(initRate);
							rateVar.setValue(initRateDouble);
						}
						if (!initRateIsInt && initRateIsRange && initRateRangeFound) {
							rateVar.setValue(0);
							InitialAssignment initAssign = m.createInitialAssignment();
							initAssign.setSymbol(rateVar.getId());
							initAssign.setMath(SBMLutilities.myParseFormula(initRate));
							String initRateAssignRHS = "uniform(";
							for (int i=1; i<=initRateRangeMatcher.groupCount();i++) {
								initRateAssignRHS = initRateAssignRHS + initRateRangeMatcher.group(i);
								if (i==1)
									initRateAssignRHS = initRateAssignRHS + ",";
							}
							initRateAssignRHS = initRateAssignRHS + ")";
							initAssign.setMath(SBMLutilities.myParseFormula(initRateAssignRHS));
						}
					}
				}
				else  // boolean variable 
				{
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(v);
					String initValue = lhpn.getInitialVal(v);
//					System.out.println(v + "=" + initValue);
					// check initValue type; if boolean, set parameter value as 0 or 1.
					if (initValue.equals("true")){
						p.setValue(1);
					}
					else if (initValue.equals("false")){
						p.setValue(0);
					}
					else if (initValue.equals("unknown")){
						p.setValue(0);
					}
					else {
//							double initVal_dbl = Double.parseDouble(initValue);
//							p.setValue(initVal_dbl);
							System.out.println("It should be a boolean variable.");
							System.exit(0);
					}
				}			
			}
		}
				
		// ----places -> species-----	
		for (String p: lhpn.getPlaceList()){
			Boolean initMarking = lhpn.getPlace(p).isMarked();
//			System.out.println(p + "=" + initMarking);
			Species sp = m.createSpecies();
			sp.setId(p);
			sp.setCompartment("default");
			sp.setBoundaryCondition(false);
			sp.setConstant(false);
			sp.setHasOnlySubstanceUnits(false);
			if (initMarking){
				sp.setInitialAmount(1);
			}
			else {
				sp.setInitialAmount(0);
			}
			sp.setUnits("");	
		}
		
		// ----convert transitions -----
		// if transition rate is not null, use reaction and event;
		// else use event only
		int counter = lhpn.getTransitionList().length - 1;
		for (String t : lhpn.getTransitionList()) {
			if(lhpn.getTransition(t).getTransitionRate()!=null){
				Species spT = m.createSpecies();
				spT.setId("Event_"+t);
				spT.setCompartment("default");
				spT.setBoundaryCondition(false);
				spT.setConstant(false);
				spT.setHasOnlySubstanceUnits(false);
				spT.setInitialAmount(0);
				spT.setUnits("");

				Reaction r = m.createReaction();
				r.setReversible(false);
				r.setFast(false);
				r.setId("r" + counter);

				//test En(t)
				String EnablingTestNull = lhpn.getTransition(t).getEnabling();
				String Enabling;
				String EnablingBool = null;
				if (EnablingTestNull == null){
					EnablingBool = "true";
					Enabling = "1"; // Enabling is true
				}
				else {
					EnablingBool = lhpn.getEnablingTree(t).getElement("SBML");
					Enabling = "piecewise(1, " + EnablingBool + ", 0)";
				}

				//test Preset(t)
				String CheckPreset = null;
				int indexPreset = 0;

				// Check if all the presets of transition t are marked
				// Transition t can fire only when all its preset are marked.
				for (String x:lhpn.getPreset(t)){
					if (indexPreset == 0){
						CheckPreset = "eq(" + x + ",1)";
					}
					else {
						CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
					}	
					indexPreset ++;
				}

				String modifierStr = "";
				String reactantStr = "";
				for (String x : lhpn.getPreset(t)){
					// Is transition persistent?
					if (lhpn.getTransition(t).isPersistent()){
						// transition is persistent
						// Create a rule for the persistent transition t. 
						AssignmentRule rulePersis = m.createAssignmentRule();
						String rulePersisSpeciesStr = "rPersis_" + t + x;
						// Create a parameter (id = rulePersisTriggName). 
						Species rulePersisSpecies = m.createSpecies();
						rulePersisSpecies.setId(rulePersisSpeciesStr);
						rulePersisSpecies.setCompartment("default");
						rulePersisSpecies.setBoundaryCondition(false);
						rulePersisSpecies.setConstant(false);
						rulePersisSpecies.setHasOnlySubstanceUnits(false);
						rulePersisSpecies.setUnits("");

						String ruleExpBool = "or(and(" + CheckPreset + "," + EnablingBool + "), and(" + CheckPreset + "," + "eq(" + rulePersisSpeciesStr + ", 1)" +"))";
						String ruleExpReal = "piecewise(1, " + ruleExpBool + ", 0)";
						rulePersis.setVariable(rulePersisSpeciesStr);
						rulePersis.setMath(SBMLutilities.myParseFormula(ruleExpReal));
						rulePersisSpecies.setInitialAmount(0);
						ModifierSpeciesReference modifier = r.createModifier();
						modifier.setSpecies(rulePersisSpeciesStr);
						// create the part of Kinetic law expression involving modifiers 
						modifierStr = modifierStr + modifier.getSpecies().toString() + "*";
					}
					else {
						// get the preset of a transition and set each as a reactant
						SpeciesReference reactant = r.createReactant();
						reactant.setSpecies(x);
						reactant.setStoichiometry(1.0);
						reactant.setConstant(true);
						reactantStr =  reactantStr + reactant.getSpecies().toString() + "*";
					}
				}


				SpeciesReference product  = r.createProduct();
				product.setSpecies(t);
				product.setStoichiometry(1.0);
				product.setConstant(true);

				KineticLaw rateReaction = r.createKineticLaw(); // rate of reaction
				//Parameter p_local = rateReaction.createParameter();
				//p_local.setConstant(false);
				//p_local.setId("rate" + counter);
				// get the transition rate from LHPN
				//System.out.println("transition rate = " + lhpn.getTransitionRate(t));
				//double tRate = Double.parseDouble(lhpn.getTransitionRate(t));	
				//p_local.setValue(tRate);
				//lhpn.getTransitionRateTree(t)

				// create exp for KineticLaw
				if (lhpn.getTransition(t).isPersistent())
					rateReaction.setFormula("(" + modifierStr + Enabling + "*" + lhpn.getTransitionRateTree(t).getElement("SBML") + ")"); 
				else
					rateReaction.setFormula("(" + reactantStr + Enabling + "*" + lhpn.getTransitionRateTree(t).getElement("SBML") + ")"); 

				Event e = m.createEvent();
				//					e.setId("event" + counter);		
				e.setId("Event_"+t);
				Trigger trigger = e.createTrigger();
				trigger.setMath(SBMLutilities.myParseFormula("eq(" + product.getSpecies() + ",1)"));
				// For persistent transition, it does not matter whether the trigger is persistent or not, because the delay is set to 0. 
				trigger.setPersistent(false);
				e.setUseValuesFromTriggerTime(false);
				trigger.setInitialValue(false);

				// t_postSet = 1
				for (String x : lhpn.getPostset(t)){
					EventAssignment assign0 = e.createEventAssignment();
					assign0.setVariable(x);
					assign0.setMath(SBMLutilities.myParseFormula("1"));
					//				System.out.println("transition: " + t + " postset: " + x);
				}

				// product = 0
				EventAssignment assign1 = e.createEventAssignment();
				assign1.setVariable(product.getSpecies());
				assign1.setMath(SBMLutilities.myParseFormula("0"));

				//					if (lhpn.getTransition(t).isPersistent()){
				//						// t_preSet = 0
				//						for (String x : lhpn.getPreset(t)){
				//							EventAssignment assign0 = e.createEventAssignment();
				//							assign0.setVariable(x);
				//							assign0.setMath(SBMLutilities.myParseFormula("0"));
				//			//				System.out.println("transition: " + t + " preset: " + x);
				//						}
				//					}

				// assignment <A>
				// continuous assignment
				if (lhpn.getContVars(t) != null){
					for (String var : lhpn.getContVars(t)){
						if (lhpn.getContAssign(t, var) != null) {
							ExprTree assignContTree = lhpn.getContAssignTree(t, var);	
							String assignCont = assignContTree.toString("SBML");
							//								System.out.println("continuous assign: "+ assignCont);
							EventAssignment assign2 = e.createEventAssignment();
							assign2.setVariable(var);
							assign2.setMath(SBMLutilities.myParseFormula(assignCont));
						}
					}
				}

				// integer assignment
				if (lhpn.getIntVars()!= null){
					for (String var : lhpn.getIntVars()){
						if (lhpn.getIntAssign(t, var) != null) {
							ExprTree assignIntTree = lhpn.getIntAssignTree(t, var);
							String assignInt = assignIntTree.toString("SBML");
							//								System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
							EventAssignment assign3 = e.createEventAssignment();
							assign3.setVariable(var);
							assign3.setMath(SBMLutilities.myParseFormula(assignInt));
						}
					}
				}

				// boolean assignment
				if (lhpn.getBooleanVars(t)!= null){
					for (String var :lhpn.getBooleanVars(t)){
						if (lhpn.getBoolAssign(t, var) != null) {
							ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
							String assignBool_tmp = assignBoolTree.toString("SBML");
							String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
							//								System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
							EventAssignment assign4 = e.createEventAssignment();
							assign4.setVariable(var);
							assign4.setMath(SBMLutilities.myParseFormula(assignBool));
						}
					}
				}

				// rate assignment
				if (lhpn.getRateVars(t)!= null){
					for (String var : lhpn.getRateVars(t)){
						//							 System.out.println("rate var: "+ var);
						if (lhpn.getRateAssign(t, var) != null) {
							ExprTree assignRateTree = lhpn.getRateAssignTree(t, var);
							String assignRate = assignRateTree.toString("SBML");
							//						System.out.println("rate assign: "+ assignRate);

							EventAssignment assign5 = e.createEventAssignment();
							assign5.setVariable(var + "_rate");
							assign5.setMath(SBMLutilities.myParseFormula(assignRate));
						}
					}
				}
				// When translating a fail transition into an event, that event should assign to a special "fail" variable a value of 1.  
				// This "fail" variable should have initial value of 0.  The SBML model should have a constraint "eq(fail,0)".
				if (lhpn.getTransition(t).isFail()){
					Parameter failVar = m.createParameter();
					failVar.setConstant(false);
					failVar.setId("failvar_" + t);
					failVar.setValue(0);
					EventAssignment assign6 = e.createEventAssignment();
					assign6.setVariable(failVar.getId());
					assign6.setMath(SBMLutilities.myParseFormula("1"));
					Constraint failVarConstraint = m.createConstraint();
					failVarConstraint.setMetaId("failtrans_" + t);
					failVarConstraint.setMath(SBMLutilities.myParseFormula("eq(" + failVar.getId() + ", 0)"));
				}
			}

			else {			// Transition rate = null. Only use event. Transitions only have ranges.
				//					System.out.println("Event Only");
				Event e = m.createEvent();
				//					e.setId("event" + counter);	
				e.setId("Event_"+t);
				Trigger trigger = e.createTrigger();

				//trigger = CheckPreset(t) && En(t);
				//test En(t)
				String EnablingTestNull = lhpn.getTransition(t).getEnabling();
				String Enabling;
				if (EnablingTestNull == null){
					Enabling = "true"; // Enabling is true (Boolean)
				}
				else {
					Enabling = lhpn.getEnablingTree(t).getElement("SBML");
				}
				//					System.out.println("Enabling = " + Enabling);

				//test Preset(t)
				String CheckPreset = null;
				int indexPreset = 0;

				// Check if all the presets of transition t are marked
				// Transition t can fire only when all its preset are marked.
				for (String x:lhpn.getPreset(t)){
					if (indexPreset == 0){
						CheckPreset = "eq(" + x + ",1)";
					}
					else {
						CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
					}	
					indexPreset ++;
				}

				// Is transition persistent?
				if (!lhpn.getTransition(t).isPersistent() || (lhpn.getTransition(t).isPersistent() && !lhpn.getTransition(t).hasConflictSet())){
					if (!lhpn.getTransition(t).isPersistent()) {
						trigger.setPersistent(false);
					}
					else {
						trigger.setPersistent(true);
					}

					trigger.setMath(SBMLutilities.myParseFormula("and(" + CheckPreset + "," + Enabling + ")"));
				}
				else { // transition is persistent
					// Create a rule for the persistent transition t. 
					AssignmentRule rulePersisTrigg = m.createAssignmentRule();
					String rulePersisTriggName = GlobalConstants.TRIGGER + "_" + t;
					// Create a parameter (id = rulePersisTriggName). 
					Parameter rulePersisParam = m.createParameter();
					rulePersisParam.setId(rulePersisTriggName);
					rulePersisParam.setValue(0);
					rulePersisParam.setConstant(false);
					rulePersisParam.setUnits("");
					String ruleExpBool = "or(and(" + CheckPreset + "," + Enabling + "), and(" + CheckPreset + "," + "eq(" + rulePersisTriggName + ", 1)" +"))";
					String ruleExpReal = "piecewise(1, " + ruleExpBool + ", 0)";
					rulePersisTrigg.setVariable(rulePersisTriggName);
					rulePersisTrigg.setMath(SBMLutilities.myParseFormula(ruleExpReal));
					trigger.setPersistent(false);
					trigger.setMath(SBMLutilities.myParseFormula("eq(" + rulePersisTriggName + ", 1)"));
				}

				// TriggerInitiallyFalse
				//					trigger.setAnnotation("<TriggerInitiallyFalse/>");
				trigger.setInitialValue(false);

				// use values at trigger time = false
				e.setUseValuesFromTriggerTime(false);

				// Priority and delay
				if (lhpn.getTransition(t).getDelay()!=null) {
					e.createDelay();
					e.getDelay().setMath(SBMLutilities.myParseFormula(lhpn.getTransition(t).getDelay()));
				}
				if (lhpn.getTransition(t).getPriority()!=null) {
					e.createPriority();
					e.getPriority().setMath(SBMLutilities.myParseFormula(lhpn.getTransition(t).getPriority()));
				}
				/*
					if (lhpn.getTransition(t).getPriority()==null) {
						if (lhpn.getTransition(t).getDelay()!=null) {
							e.createDelay();
							e.getDelay().setMath(SBMLutilities.myParseFormula(lhpn.getTransition(t).getDelay()));
						}
					}
					else {
						if (lhpn.getTransition(t).getDelay()!=null) {
							e.createDelay();
							e.getDelay().setMath(SBMLutilities.myParseFormula("priority(" + lhpn.getTransition(t).getDelay() + "," + lhpn.getTransition(t).getPriority() + ")"));
						} 
						else {
						e.createDelay();
						e.getDelay().setMath(SBMLutilities.myParseFormula("priority(0," + lhpn.getTransition(t).getPriority() + ")"));
						}
					}
				 */

				// Check if there is any self-loop. If the intersection between lhpn.getPreset(t) and lhpn.getPostset(t)
				// is not empty, self-loop exists. 
				List<String> t_postset = Arrays.asList(lhpn.getPostset(t));
				List<String> t_intersect = new ArrayList<String>(); // intersection of t_preset and t_postset
				List<String> t_NoIntersect = new ArrayList<String>(); // t_NoIntersect = t_postset - t_preset
				Boolean selfLoopFlag = false;

				// Check if there is intersection between the preset and postset. 
				for (String x : lhpn.getPreset(t)){
					if (t_postset.contains(x)){
						selfLoopFlag = true;
						t_intersect.add(x);
					}
				}
				if (selfLoopFlag) {
					t_NoIntersect.removeAll(t_intersect);
					// t_preset = 0
					for (String x : lhpn.getPreset(t)){
						EventAssignment assign0 = e.createEventAssignment();
						assign0.setVariable(x);
						assign0.setMath(SBMLutilities.myParseFormula("0"));
						//				System.out.println("transition: " + t + " preset: " + x);
					}

					// t_NoIntersect  = 1
					for (String x : t_NoIntersect){
						EventAssignment assign1 = e.createEventAssignment();
						assign1.setVariable(x);
						assign1.setMath(SBMLutilities.myParseFormula("1"));
						//				System.out.println("transition: " + t + " postset: " + x);
					}

				}
				else {			// no self-loop 
					// t_preSet = 0
					for (String x : lhpn.getPreset(t)){
						EventAssignment assign0 = e.createEventAssignment();
						assign0.setVariable(x);
						assign0.setMath(SBMLutilities.myParseFormula("0"));
						//				System.out.println("transition: " + t + " preset: " + x);
					}

					// t_postSet = 1
					for (String x : lhpn.getPostset(t)){
						EventAssignment assign1 = e.createEventAssignment();
						assign1.setVariable(x);
						assign1.setMath(SBMLutilities.myParseFormula("1"));
						//				System.out.println("transition: " + t + " postset: " + x);
					}
				}

				// assignment <A>
				// continuous assignment
				if (lhpn.getContVars(t) != null){
					for (String var : lhpn.getContVars(t)){
						if (lhpn.getContAssign(t, var) != null) {
							ExprTree assignContTree = lhpn.getContAssignTree(t, var);	
							String assignCont = assignContTree.toString("SBML");
							//								System.out.println("continuous assign: "+ assignCont);
							EventAssignment assign2 = e.createEventAssignment();
							assign2.setVariable(var);
							assign2.setMath(SBMLutilities.myParseFormula(assignCont));
						}
					}
				}

				// integer assignment
				if (lhpn.getIntVars()!= null){
					for (String var : lhpn.getIntVars()){
						if (lhpn.getIntAssign(t, var) != null) {
							ExprTree assignIntTree = lhpn.getIntAssignTree(t, var);
							String assignInt = assignIntTree.toString("SBML");
							//							    System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
							EventAssignment assign3 = e.createEventAssignment();
							assign3.setVariable(var);
							assign3.setMath(SBMLutilities.myParseFormula(assignInt));
						}
					}
				}

				// boolean assignment
				if (selfLoopFlag) {
					// if self-loop exists, create a new variable, extraVar, and a new event
					String extraVar = t.concat("_extraVar");
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(extraVar);
					p.setValue(0);

					EventAssignment assign4ex = e.createEventAssignment();
					assign4ex.setVariable(extraVar);
					assign4ex.setMath(SBMLutilities.myParseFormula("1"));

					// Create other boolean assignments
					if (lhpn.getBooleanVars(t)!= null){
						for (String var :lhpn.getBooleanVars(t)){
							if (lhpn.getBoolAssign(t, var) != null) {
								ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
								String assignBool_tmp = assignBoolTree.toString("SBML");
								String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
								//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
								EventAssignment assign4 = e.createEventAssignment();
								assign4.setVariable(var);
								assign4.setMath(SBMLutilities.myParseFormula(assignBool));
							}
						}
					}

					// create a new event 
					Event extraEvent = m.createEvent();
					extraEvent.setId("extra_" + t);	
					Trigger triggerExtra = extraEvent.createTrigger();
					//triggerExtra.setMath(SBMLutilities.myParseFormula("and(gt(t,0),eq(" + extraVar + ",1))"));
					triggerExtra.setMath(SBMLutilities.myParseFormula("eq(" + extraVar + ",1)"));
					//triggerExtra.setAnnotation("<TriggerInitiallyFalse/>");
					triggerExtra.setPersistent(true);
					triggerExtra.setInitialValue(false);
					extraEvent.setUseValuesFromTriggerTime(false);
					// assignments
					EventAssignment assign5ex2 = extraEvent.createEventAssignment();
					for (String var : t_intersect){
						EventAssignment assign5ex1 = extraEvent.createEventAssignment();
						assign5ex1.setVariable(var);
						assign5ex1.setMath(SBMLutilities.myParseFormula("1"));
					}
					assign5ex2.setVariable(extraVar);
					assign5ex2.setMath(SBMLutilities.myParseFormula("0"));
				}
				else {
					if (lhpn.getBooleanVars(t)!= null){
						for (String var :lhpn.getBooleanVars(t)){
							if (lhpn.getBoolAssign(t, var) != null) {
								ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
								String assignBool_tmp = assignBoolTree.toString("SBML");
								String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
								//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
								EventAssignment assign4 = e.createEventAssignment();
								assign4.setVariable(var);
								assign4.setMath(SBMLutilities.myParseFormula(assignBool));
							}
						}
					}
				}

				// rate assignment
				if (lhpn.getRateVars(t)!= null){
					for (String var : lhpn.getRateVars(t)){
						//							 System.out.println("rate var: "+ var);
						if (lhpn.getRateAssign(t, var) != null) {
							ExprTree assignRateTree = lhpn.getRateAssignTree(t, var);
							String assignRate = assignRateTree.toString("SBML");
							//						System.out.println("rate assign: "+ assignRate);

							EventAssignment assign5 = e.createEventAssignment();
							assign5.setVariable(var + "_rate");
							assign5.setMath(SBMLutilities.myParseFormula(assignRate));
						}
					}
				}
				// When translating a fail transition into an event, that event should assign to a special "fail" variable a value of 1.  
				// This "fail" variable should have initial value of 0.  The SBML model should have a constraint "eq(fail,0)".
				if (lhpn.getTransition(t).isFail()){
					Parameter failVar = m.createParameter();
					failVar.setConstant(false);
					failVar.setId("failvar_" + t);
					failVar.setValue(0);
					EventAssignment assign6 = e.createEventAssignment();
					assign6.setVariable(failVar.getId());
					assign6.setMath(SBMLutilities.myParseFormula("1"));
					Constraint failVarConstraint = m.createConstraint();
					failVarConstraint.setMetaId("failtrans_" + t);
					failVarConstraint.setMath(SBMLutilities.myParseFormula("eq(" + failVar.getId() + ", 0)"));
				}
			}
			counter --;
		}

		// Property parsing is dealt with in PropertyPanel.java
		// translate the LPN property to SBML constraints
		document = generateSBMLConstraints(document, property, lhpn);
	}
	
	public void convertLPN2SBML(String lhpnFilename, String property) {
		this.filename = lhpnFilename.replace(".lpn", ".xml");
		// load lhpn file
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		//document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		document = new SBMLDocument(3,1);
		Model m = document.createModel();
		m.setId(filename.replace(".xml", ""));
		
		// Create bitwise operators for sbml
		createFunction(m, "rate", "Rate", "lambda(a,a)");
		createFunction(m, "BIT", "bit selection", "lambda(a,b,a*b)");
		createFunction(m, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITOR", "Bitwise OR", "lambda(a,b,a*b)");
		createFunction(m, "BITNOT", "Bitwise NOT", "lambda(a,b,a*b)");
		createFunction(m, "BITXOR", "Bitwise XOR", "lambda(a,b,a*b)");
		createFunction(m, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
		//createFunction(m, "and", "Logical AND", "lambda(a,b,a*b)");
		createFunction(m, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(m, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(m, "exponential", "Exponential distribution", "lambda(l,1/l)");
		createFunction(m, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(m, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(m, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(m, "laplace", "Laplace distribution", "lambda(a,0)");
		createFunction(m, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(m, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
		createFunction(m, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(m, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(m, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
		
		// translate from lhpn to sbml
		// ----variables -> parameters-----	
		for (String v: lhpn.getVariables()){
			if (v != null){
				String initVal = lhpn.getInitialVal(v);
				if (lhpn.isInteger(v)){
					Parameter var = m.createParameter(); 
					var.setConstant(false);
					var.setId(v);				
					Pattern initVarIsIntPattern = Pattern.compile(Int);
					Matcher initVarIsIntMatcher = initVarIsIntPattern.matcher(initVal);
					boolean initVarIsInt = initVarIsIntMatcher.matches();
					Pattern initVarIsRangePattern = Pattern.compile(Range);
					Matcher initVarIsRangeMatcher = initVarIsRangePattern.matcher(initVal);
					boolean initVarIsRange = initVarIsRangeMatcher.matches();
					Pattern initRangeBoundPattern = Pattern.compile(Range);
					Matcher initRangeBoundMatcher = initRangeBoundPattern.matcher(initVal); 
					boolean initVarRangeFound = initRangeBoundMatcher.find();
					if (initVarIsInt && !initVarIsRange) {
						double initValDouble = Double.parseDouble(initVal);
						var.setValue(initValDouble);
					}
					if (!initVarIsInt && initVarIsRange && initVarRangeFound) {
						var.setValue(0);
						InitialAssignment initAssign = m.createInitialAssignment();
						initAssign.setSymbol(var.getId());
						String initVarAssignRHS = "uniform(";
						for (int i=1; i<=initRangeBoundMatcher.groupCount();i++) {
							initVarAssignRHS = initVarAssignRHS + initRangeBoundMatcher.group(i);
							if (i==1)
								initVarAssignRHS = initVarAssignRHS + ",";
						}
						initVarAssignRHS = initVarAssignRHS + ")";
						initAssign.setMath(SBMLutilities.myParseFormula(initVarAssignRHS));
					}
				} else if (lhpn.isBoolean(v)) { // boolean variable 
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(v);
					p.setSBOTerm(GlobalConstants.SBO_BOOLEAN);
					String initValue = lhpn.getInitialVal(v);
					// check initValue type; if boolean, set parameter value as 0 or 1.
					if (initValue.equals("true")){
						p.setValue(1);
					}
					else {
						p.setValue(0);
					}
				}			
			}
		}
		
		for (String v: lhpn.getContinuous().keySet()){
			String initVal = lhpn.getInitialVal(v);
			Parameter var = m.createParameter(); 
			var.setConstant(false);
			var.setId(v);				
			Pattern initVarIsIntPattern = Pattern.compile(Int);
			Matcher initVarIsIntMatcher = initVarIsIntPattern.matcher(initVal);
			boolean initVarIsInt = initVarIsIntMatcher.matches();
			Pattern initVarIsRangePattern = Pattern.compile(Range);
			Matcher initVarIsRangeMatcher = initVarIsRangePattern.matcher(initVal);
			boolean initVarIsRange = initVarIsRangeMatcher.matches();
			Pattern initRangeBoundPattern = Pattern.compile(Range);
			Matcher initRangeBoundMatcher = initRangeBoundPattern.matcher(initVal); 
			boolean initVarRangeFound = initRangeBoundMatcher.find();
			if (initVarIsInt && !initVarIsRange) {
				double initValDouble = Double.parseDouble(initVal);
				var.setValue(initValDouble);
			}
			if (!initVarIsInt && initVarIsRange && initVarRangeFound) {
				var.setValue(0);
				InitialAssignment initAssign = m.createInitialAssignment();
				initAssign.setSymbol(var.getId());
				String initVarAssignRHS = "uniform(";
				for (int i=1; i<=initRangeBoundMatcher.groupCount();i++) {
					initVarAssignRHS = initVarAssignRHS + initRangeBoundMatcher.group(i);
					if (i==1)
						initVarAssignRHS = initVarAssignRHS + ",";
				}
				initVarAssignRHS = initVarAssignRHS + ")";
				initAssign.setMath(SBMLutilities.myParseFormula(initVarAssignRHS));
			}
			Parameter rateVar = m.createParameter();
			rateVar.setConstant(false);
			rateVar.setId(v + "_" + GlobalConstants.RATE);
			RateRule rateRule = m.createRateRule();
			rateRule.setMetaId(GlobalConstants.RULE+"_"+v+"_"+GlobalConstants.RATE);
			rateRule.setVariable(v);
			rateRule.setMath(SBMLutilities.myParseFormula(rateVar.getId()));
			String initRate= lhpn.getInitialRate(v);
			boolean initRateIsInt = Pattern.matches(Int, initRate);
			boolean initRateIsRange = Pattern.matches(Range, initRate);
			Pattern initRateRangePattern = Pattern.compile(Range);
			Matcher initRateRangeMatcher = initRateRangePattern.matcher(initRate); 
			boolean initRateRangeFound = initRateRangeMatcher.find();
			if (initRateIsInt && !initRateIsRange) {
				double initRateDouble = Double.parseDouble(initRate);
				rateVar.setValue(initRateDouble);
			}
			if (!initRateIsInt && initRateIsRange && initRateRangeFound) {
				rateVar.setValue(0);
				InitialAssignment initAssign = m.createInitialAssignment();
				initAssign.setSymbol(rateVar.getId());
				initAssign.setMath(SBMLutilities.myParseFormula(initRate));
				String initRateAssignRHS = "uniform(";
				for (int i=1; i<=initRateRangeMatcher.groupCount();i++) {
					initRateAssignRHS = initRateAssignRHS + initRateRangeMatcher.group(i);
					if (i==1)
						initRateAssignRHS = initRateAssignRHS + ",";
				}
				initRateAssignRHS = initRateAssignRHS + ")";
				initAssign.setMath(SBMLutilities.myParseFormula(initRateAssignRHS));
			}
		}
		
		// ----places -> species-----	
		for (String p: lhpn.getPlaceList()){
			Boolean initMarking = lhpn.getPlace(p).isMarked();
//			System.out.println(p + "=" + initMarking);
			Parameter v = m.createParameter();
			v.setId(p);
			v.setSBOTerm(GlobalConstants.SBO_PLACE);
			v.setConstant(false);
			if (initMarking){
				v.setValue(1);
			} else {
				v.setValue(0);
			}
		}
		
		// ----convert transitions -----
		for (String t : lhpn.getTransitionList()) {
			Event e = m.createEvent();
			e.setId(t);
			e.setSBOTerm(GlobalConstants.SBO_TRANSITION);
			Trigger trigger = e.createTrigger();
			String EnablingTestNull = lhpn.getTransition(t).getEnabling();
			String Enabling;
			if (EnablingTestNull == null){
				Enabling = "true"; // Enabling is true (Boolean)
			} else {
				Enabling = lhpn.getEnablingTree(t).getElement("SBML");
			}

			//test Preset(t)
			String CheckPreset = null;
			int indexPreset = 0;

			// Check if all the presets of transition t are marked
			// Transition t can fire only when all its preset are marked.
			for (String x:lhpn.getPreset(t)){
				if (indexPreset == 0){
					CheckPreset = "eq(" + x + ",1)";
				}
				else {
					CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
				}	
				indexPreset ++;
			}

			// Is transition persistent?
			if (!lhpn.getTransition(t).isPersistent() || (lhpn.getTransition(t).isPersistent() && !lhpn.getTransition(t).hasConflictSet())){
				if (!lhpn.getTransition(t).isPersistent()) {
					trigger.setPersistent(false);
				}
				else {
					trigger.setPersistent(true);
				}

				trigger.setMath(SBMLutilities.myParseFormula("and(" + Enabling + "," + CheckPreset + ")"));
			}
			else { // transition is persistent
				// Create a rule for the persistent transition t. 
				AssignmentRule rulePersisTrigg = m.createAssignmentRule();
				rulePersisTrigg.setMetaId(GlobalConstants.TRIGGER+"_"+GlobalConstants.RULE+"_"+t);
				String rulePersisTriggName = GlobalConstants.TRIGGER + "_" + t;
				// Create a parameter (id = rulePersisTriggName). 
				Parameter rulePersisParam = m.createParameter();
				rulePersisParam.setId(rulePersisTriggName);
				rulePersisParam.setValue(0);
				rulePersisParam.setConstant(false);
				rulePersisParam.setUnits("");
				String ruleExpBool = "or(and(" + Enabling + "," + CheckPreset + "), and(" + "eq(" + rulePersisTriggName + ", 1)," + CheckPreset + "))";
				String ruleExpReal = "piecewise(1, " + ruleExpBool + ", 0)";
				rulePersisTrigg.setVariable(rulePersisTriggName);
				rulePersisTrigg.setMath(SBMLutilities.myParseFormula(ruleExpReal));
				trigger.setPersistent(false);
				trigger.setMath(SBMLutilities.myParseFormula("eq(" + rulePersisTriggName + ", 1)"));
			}

			// TriggerInitiallyFalse
			//					trigger.setAnnotation("<TriggerInitiallyFalse/>");
			trigger.setInitialValue(false);

			// use values at trigger time = false
			e.setUseValuesFromTriggerTime(false);

			// Priority and delay
			if (lhpn.getTransition(t).getDelay()!=null) {
				e.createDelay();
				e.getDelay().setMath(SBMLutilities.myParseFormula(lhpn.getTransition(t).getDelay()));
			}
			if (lhpn.getTransition(t).getPriority()!=null) {
				e.createPriority();
				e.getPriority().setMath(SBMLutilities.myParseFormula(lhpn.getTransition(t).getPriority()));
			}

			// Check if there is any self-loop. If the intersection between lhpn.getPreset(t) and lhpn.getPostset(t)
			// is not empty, self-loop exists. 
			List<String> t_postset = Arrays.asList(lhpn.getPostset(t));
			List<String> t_intersect = new ArrayList<String>(); // intersection of t_preset and t_postset
			List<String> t_NoIntersect = new ArrayList<String>(); // t_NoIntersect = t_postset - t_preset
			Boolean selfLoopFlag = false;

			// Check if there is intersection between the preset and postset. 
			for (String x : lhpn.getPreset(t)){
				if (t_postset.contains(x)){
					selfLoopFlag = true;
					t_intersect.add(x);
				}
			}
			if (selfLoopFlag) {
				t_NoIntersect.removeAll(t_intersect);
				// t_preset = 0
				for (String x : lhpn.getPreset(t)){
					EventAssignment assign0 = e.createEventAssignment();
					assign0.setVariable(x);
					assign0.setMath(SBMLutilities.myParseFormula("0"));
					//				System.out.println("transition: " + t + " preset: " + x);
				}

				// t_NoIntersect  = 1
				for (String x : t_NoIntersect){
					EventAssignment assign1 = e.createEventAssignment();
					assign1.setVariable(x);
					assign1.setMath(SBMLutilities.myParseFormula("1"));
					//				System.out.println("transition: " + t + " postset: " + x);
				}

			}
			else {			// no self-loop 
				// t_preSet = 0
				for (String x : lhpn.getPreset(t)){
					EventAssignment assign0 = e.createEventAssignment();
					assign0.setVariable(x);
					assign0.setMath(SBMLutilities.myParseFormula("0"));
					//				System.out.println("transition: " + t + " preset: " + x);
				}

				// t_postSet = 1
				for (String x : lhpn.getPostset(t)){
					EventAssignment assign1 = e.createEventAssignment();
					assign1.setVariable(x);
					assign1.setMath(SBMLutilities.myParseFormula("1"));
					//				System.out.println("transition: " + t + " postset: " + x);
				}
			}

			// assignment <A>
			// continuous assignment
			if (lhpn.getContVars(t) != null){
				for (String var : lhpn.getContVars(t)){
					if (lhpn.getContAssign(t, var) != null) {
						ExprTree assignContTree = lhpn.getContAssignTree(t, var);	
						String assignCont = assignContTree.toString("SBML");
						//								System.out.println("continuous assign: "+ assignCont);
						EventAssignment assign2 = e.createEventAssignment();
						assign2.setVariable(var);
						assign2.setMath(SBMLutilities.myParseFormula(assignCont));
					}
				}
			}

			// integer assignment
			if (lhpn.getIntVars()!= null){
				for (String var : lhpn.getIntVars()){
					if (lhpn.getIntAssign(t, var) != null) {
						ExprTree assignIntTree = lhpn.getIntAssignTree(t, var);
						String assignInt = assignIntTree.toString("SBML");
						//							    System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
						EventAssignment assign3 = e.createEventAssignment();
						assign3.setVariable(var);
						assign3.setMath(SBMLutilities.myParseFormula(assignInt));
					}
				}
			}

			// boolean assignment
			if (selfLoopFlag) {
				// if self-loop exists, create a new variable, extraVar, and a new event
				String extraVar = t.concat("_extraVar");
				Parameter p = m.createParameter(); 
				p.setConstant(false);
				p.setId(extraVar);
				p.setValue(0);

				EventAssignment assign4ex = e.createEventAssignment();
				assign4ex.setVariable(extraVar);
				assign4ex.setMath(SBMLutilities.myParseFormula("1"));

				// Create other boolean assignments
				if (lhpn.getBooleanVars(t)!= null){
					for (String var :lhpn.getBooleanVars(t)){
						if (lhpn.getBoolAssign(t, var) != null) {
							ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
							String assignBool_tmp = assignBoolTree.toString("SBML");
							String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
							//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
							EventAssignment assign4 = e.createEventAssignment();
							assign4.setVariable(var);
							assign4.setMath(SBMLutilities.myParseFormula(assignBool));
						}
					}
				}

				// create a new event 
				Event extraEvent = m.createEvent();
				extraEvent.setId("extra_" + t);	
				Trigger triggerExtra = extraEvent.createTrigger();
				//triggerExtra.setMath(SBMLutilities.myParseFormula("and(gt(t,0),eq(" + extraVar + ",1))"));
				triggerExtra.setMath(SBMLutilities.myParseFormula("eq(" + extraVar + ",1)"));
				//triggerExtra.setAnnotation("<TriggerInitiallyFalse/>");
				triggerExtra.setPersistent(true);
				triggerExtra.setInitialValue(false);
				extraEvent.setUseValuesFromTriggerTime(false);
				// assignments
				EventAssignment assign5ex2 = extraEvent.createEventAssignment();
				for (String var : t_intersect){
					EventAssignment assign5ex1 = extraEvent.createEventAssignment();
					assign5ex1.setVariable(var);
					assign5ex1.setMath(SBMLutilities.myParseFormula("1"));
				}
				assign5ex2.setVariable(extraVar);
				assign5ex2.setMath(SBMLutilities.myParseFormula("0"));
			}
			else {
				if (lhpn.getBooleanVars(t)!= null){
					for (String var :lhpn.getBooleanVars(t)){
						if (lhpn.getBoolAssign(t, var) != null) {
							ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
							String assignBool_tmp = assignBoolTree.toString("SBML");
							String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
							//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
							EventAssignment assign4 = e.createEventAssignment();
							assign4.setVariable(var);
							assign4.setMath(SBMLutilities.myParseFormula(assignBool));
						}
					}
				}
			}

			// rate assignment
			if (lhpn.getRateVars(t)!= null){
				for (String var : lhpn.getRateVars(t)){
					//							 System.out.println("rate var: "+ var);
					if (lhpn.getRateAssign(t, var) != null) {
						ExprTree assignRateTree = lhpn.getRateAssignTree(t, var);
						String assignRate = assignRateTree.toString("SBML");
						//						System.out.println("rate assign: "+ assignRate);

						EventAssignment assign5 = e.createEventAssignment();
						assign5.setVariable(var + "_" + GlobalConstants.RATE);
						assign5.setMath(SBMLutilities.myParseFormula(assignRate));
					}
				}
			}
			// When translating a fail transition into an event, that event should assign to a special "fail" variable a value of 1.  
			// This "fail" variable should have initial value of 0.  The SBML model should have a constraint "eq(fail,0)".
			if (lhpn.getTransition(t).isFail()){
				Parameter failVar = m.createParameter();
				failVar.setConstant(false);
				failVar.setId(GlobalConstants.FAIL);
				failVar.setValue(0);
				EventAssignment assign6 = e.createEventAssignment();
				assign6.setVariable(failVar.getId());
				assign6.setMath(SBMLutilities.myParseFormula("1"));
				Constraint failVarConstraint = m.createConstraint();
				failVarConstraint.setMetaId(GlobalConstants.FAIL_TRANSITION);
				failVarConstraint.setMath(SBMLutilities.myParseFormula("eq(" + failVar.getId() + ", 0)"));
			}
		}
		// Property parsing is dealt with in PropertyPanel.java
		// translate the LPN property to SBML constraints
		document = generateSBMLConstraints(document, property, lhpn);
	}
	
	private void createFunction(Model model, String id, String name, String formula) {
	if (document.getModel().getFunctionDefinition(id) == null) {
		FunctionDefinition f = model.createFunctionDefinition();
		f.setId(id);
		f.setName(name);
		f.setMath(libsbml.parseFormula(formula));
	}
}

	public void outputSBML() {
		SBMLutilities.pruneUnusedSpecialFunctions(document);
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, filename);
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
	
	public static SBMLDocument generateSBMLConstraints(SBMLDocument doc, String property, LhpnFile lhpn) {
		String probprop = "";
		String[] probpropParts = new String[4];
		if(!(property == null) && !property.equals("") && !property.equals("none")){
			Model m = doc.getModel();
			probprop=getProbpropExpression(property);
			if (!isSteadyState && !isHSF) {
				probpropParts=getProbpropParts(probprop);
				// Convert extracted property parts into SBML constraints
				// probpropParts=[probpropLeft, probpropRight, lowerBound, upperBound]
				Constraint constraintFail = m.createConstraint();	
				Constraint constraintSucc = m.createConstraint();
				constraintFail.setMetaId("Fail");
				constraintSucc.setMetaId("Success");
				ExprTree probpropLeftTree = null;
				ExprTree probpropRightTree = null;
				ExprTree lowerBoundTree = null;
				ExprTree upperBoundTree = null;
				String probpropLeftSBML = null;
				String probpropRightSBML = null;
				String lowerBoundSBML = null;
				String upperBoundSBML = null;
				
				if (!probpropParts[0].equals(""))
					probpropLeftTree = String2ExprTree(lhpn,probpropParts[0]);
				if (!probpropParts[1].equals(""))
					probpropRightTree = String2ExprTree(lhpn,probpropParts[1]);
				if (!probpropParts[2].equals(""))
					lowerBoundTree = String2ExprTree(lhpn,probpropParts[2]);
				if (!probpropParts[3].equals(""))
					upperBoundTree = String2ExprTree(lhpn,probpropParts[3]);
				if (!(probpropLeftTree == null))
					probpropLeftSBML = probpropLeftTree.toString("SBML");
				if (!(probpropRightTree == null))
					probpropRightSBML = probpropRightTree.toString("SBML");
				if (!(lowerBoundTree == null))
					lowerBoundSBML = lowerBoundTree.toString("SBML");
				if (!(upperBoundTree == null))
					upperBoundSBML = upperBoundTree.toString("SBML");
				
				String LTlower = "lt(t," + lowerBoundSBML + ")";
				String LEQlower = "leq(t," + lowerBoundSBML + ")";
				String GTlower = "gt(t," + lowerBoundSBML + ")";
				String GEQlower = "geq(t," + lowerBoundSBML + ")";
				String EQlower = "eq(t," + lowerBoundSBML + ")";
				String LEQupper = "leq(t," + upperBoundSBML + ")";
				String LTupper = "lt(t," + upperBoundSBML + ")";
				String GTupper = "gt(t," + upperBoundSBML + ")";
				String GEQupper = "geq(t," + upperBoundSBML + ")";
				
				int relopType = Integer.parseInt(probpropParts[4]);
				// construct the SBML constraints
				if (property.contains("PU")){   // A PU B (Time bounds are specified below.)
					switch (relopType) {
						case 0:{	// [l,u]   
							// Fail: (A || (B && (t>=l))) && (t<=u)
							// Success: !B || (t<l) || (t>u)
							constraintFail.setMath(SBMLutilities.myParseFormula("and(" + "or("+probpropLeftSBML + "," + "and(" + probpropRightSBML + "," + GEQlower + ")" + ")" + "," + LEQupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "or(" + "not(" + probpropRightSBML + ")" + "," + LTlower + ")" + "," + GTupper + ")"));
							break;
						}
						case 1: {  // [<=u]
							// Fail: (A || B) && (t<=u)
							// Success: (!B) || (t>u)
							constraintFail.setMath(SBMLutilities.myParseFormula("and(" + "or(" + probpropLeftSBML + "," + probpropRightSBML + ")" + "," + LEQupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + GTupper + ")"));
							break;
						}
						case 2: {	// [<u]
							// Fail: (A || B) && (t<u)
							// Success:(!B) || (t>=u)
							constraintFail.setMath(SBMLutilities.myParseFormula("and(" + "or(" + probpropLeftSBML + "," + probpropRightSBML + ")" + "," + LTupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + GEQupper + ")"));
							break;
						}
						case 3: {	// [>=l]
							// Fail: A || (B && (t>=l))
							// Success: (!B) || (t<l)
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropLeftSBML + "," + "and(" + probpropRightSBML +"," + GEQlower + ")" + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + LTlower + ")"));
							break;
						}
						case 4: {	// [>l]
							// Fail: A || (B && (t>l))
							// Success: !B || (t<=l)
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropLeftSBML + "," + "and(" + probpropRightSBML + "," + GTlower + ")" + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + LEQlower + ")"));
							break;
						}
						case 5: {	// [=k] (k was stored as lowerBound)
							// Fail: (A && (t<=k)) || (B && (t=k))
							// Success: !B || (t>k) || (t<k)
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + "and(" + probpropLeftSBML + "," + LEQlower + ")" + "," + "and(" + probpropRightSBML + "," + EQlower + ")" + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "or(" + "not(" + probpropRightSBML + ")" + "," + GTlower + ")" + "," + LTlower + ")"));
							break;
						}
					}
				}
				if (property.contains("PF")){ // PF A (Time bounds are specified below.)
					switch (relopType) {
						case 0:{	// [l,u]   
							// Fail: t<=u
							// Success: !A || (t<l) || (t>u)
							constraintFail.setMath(SBMLutilities.myParseFormula(LEQupper));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "or(" + "not(" + probpropRightSBML + ")" + "," + LTlower + ")" + "," + GTupper + ")"));
							break;
						}
						case 1: {  // [<=u]
							// Fail: t<=u
							// Success: !A || (t>u)
							constraintFail.setMath(SBMLutilities.myParseFormula(LEQupper));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + GTupper + ")"));
							break;
						}
						case 2: {	// [<u]
							// Fail: t<u
							// Success: !A || (t>=u)
							constraintFail.setMath(SBMLutilities.myParseFormula(LTupper));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + GEQupper + ")"));
							break;
						}
						case 3: {	// [>=l]
							// Fail: ---
							// Success: !A || (t<l)
							m.removeConstraint(0);
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + LTlower + ")"));
							break;
						}
						case 4: {	// [>l]
							// Fail: ---
							// Success: !A || (t<=l)
							m.removeConstraint(0);
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "not(" + probpropRightSBML + ")" + "," + LEQlower + ")"));
							break;
						}
						case 5: {	// [=k]
							// Fail: t<=k
							// Success: !A || (t<k) || (t>k)
							constraintFail.setMath(SBMLutilities.myParseFormula(EQlower));
							constraintSucc.setMath(SBMLutilities.myParseFormula("or(" + "or(" + "not(" + probpropRightSBML + ")" + "," + LTlower + ")" + "," + GTlower + ")"));
							break;
						}
					}
				}
				if (property.contains("PG")){
					switch (relopType) {
						case 0:{	// [l,u]   
							// Fail: A || (t<l) || (t>u)
							// Success: t<=u
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + "or(" + probpropRightSBML + "," + LTlower + ")" + "," + GTupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula(LEQupper));
							break;
						}
						case 1: {  // [<=u]
							// Fail: A || (t>u)
							// Success: t<=u
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropRightSBML + "," + GTupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula(LEQupper));
							break;
						}
						case 2: {	// [<u]
							// Fail: A || (t>=u)
							// Success: t<u
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropRightSBML + "," + GEQupper + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula(LTupper));
							break;
						}
						case 3: {	// [>=l]
							// Fail: A || (t<l)
							// Success: ---
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropRightSBML + "," + LTlower + ")"));
							m.removeConstraint(1);
							break;
						}
						case 4: {	// [>l]
							// Fail: A || (t<=l)
							// Success: ---
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + probpropRightSBML + "," + LEQlower + ")"));
							m.removeConstraint(1);
							break;
						}
						case 5: {	// [=k]
							// Fail: A || (t>k) || (t<k)
							// Success: t<=k
							constraintFail.setMath(SBMLutilities.myParseFormula("or(" + "or(" + probpropRightSBML + "," + GTlower + ")" + "," + LTlower + ")"));
							constraintSucc.setMath(SBMLutilities.myParseFormula(LEQlower));
							break;
						}
					}
				}
			}
		}
		return doc;
	}
	
	// getProbpropExpression strips off the "Pr"("St"), relop and REAL parts of a property
	// Example: Pr>=0.99{(true)PU[3,10]((A<8)&&(R>4))]}
	public static String getProbpropExpression(String property){
		//System.out.println("property (getProbpropExpression)= " + property);
		String probprop="";
		// probproperty 
		if (property.startsWith("Pr") || property.startsWith("St")){
			if(property.startsWith("St"))
				isSteadyState = true;
			// remove Pr/St from the property spec
			property=property.substring(2);
			boolean relopFlag = property.startsWith(">")
								|| property.startsWith(">=")
								|| property.startsWith("<")
								|| property.startsWith("<=")
								|| (property.startsWith("=") && !property.contains("?"));
			if (relopFlag){
				if(property.startsWith(">=") || property.startsWith("<=")){
					property=property.substring(2);
				}
				else{
					property=property.substring(1);
				}
				// check the probability value after relop
				String probabilityVal = property.substring(0,property.indexOf("{"));
				Pattern ProbabilityValuePattern = Pattern.compile(probabilityValue);
				Matcher ProbabilityValueMatcher = ProbabilityValuePattern.matcher(probabilityVal);
				boolean correctProbabilityValue = ProbabilityValueMatcher.matches();
				if(correctProbabilityValue) {
					property=property.replaceFirst(probabilityVal, "");
					property=property.replace("{", "");
					property=property.replace("}", "");
					probprop=property;
				}
				else{
					JOptionPane.showMessageDialog(Gui.frame, "Invalid probability value",
							"Error in Property", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(property.startsWith("{")) { // shorthand version: Pr{Psi} and St{Psi}
				property=property.substring(1);
				property=property.substring(0,property.lastIndexOf('}')) + property.substring(property.lastIndexOf('}')+1);
				probprop=property;
			}
			else if(property.startsWith("=") && property.contains("?")){ // full version: Pr=?{Psi} and St=?{Psi}
				property=property.substring(3);
				property=property.substring(0,property.lastIndexOf('}')) + property.substring(property.lastIndexOf('}')+1);
				probprop=property;
			}
		}
		else { // hsf
			isHSF = true;
		}		
		return probprop;
	}
	
	public static String convertProperty(ASTNode prop) {
		String property = "Pr=?{";
		String operator = "";
		if (prop.getName().equals("G")) {
			operator += "PG";
		}
		else if (prop.getName().equals("F")) {
			operator += "PF";
		}
		else if (prop.getName().equals("U")) {
			operator += "PU";
		}
		ASTNode node = prop.getChild(0);
		if (node.getName().equals("and")) {
			String min = "0";
			String max = "inf";
			ASTNode child;
			for (int i = 0; i < node.getNumChildren(); i++) {
				child = node.getChild(i);
				if (child.getName().equals("lt")) {
					if (child.getChild(0).isNumber()) {
						max = SBMLutilities.myFormulaToString(child.getChild(0));
					}
					else if (child.getChild(1).isNumber()) {
						min = SBMLutilities.myFormulaToString(child.getChild(1));
					}
				}
				else if (child.getName().equals("leq")) {
					if (child.getChild(0).isNumber()) {
						max = SBMLutilities.myFormulaToString(child.getChild(0));
					}
					else if (child.getChild(1).isNumber()) {
						min = SBMLutilities.myFormulaToString(child.getChild(1));
					}
				}
				else if (child.getName().equals("gt")) {
					if (child.getChild(0).isNumber()) {
						min = SBMLutilities.myFormulaToString(child.getChild(0));
					}
					else if (child.getChild(1).isNumber()) {
						max = SBMLutilities.myFormulaToString(child.getChild(1));
					}
				}
				else if (child.getName().equals("geq")) {
					if (child.getChild(0).isNumber()) {
						min = SBMLutilities.myFormulaToString(child.getChild(0));
					}
					else if (child.getChild(1).isNumber()) {
						max = SBMLutilities.myFormulaToString(child.getChild(1));
					}
				}
			}
			operator += "[" + min + "," + max + "]";
		}
		else if (node.getName().equals("lt")) {
			if (node.getChild(0).isNumber()) {
				operator += "[>" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
			}
			else if (node.getChild(1).isNumber()) {
				operator += "[<" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
			}
		}
		else if (node.getName().equals("leq")) {
			if (node.getChild(0).isNumber()) {
				operator += "[>=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
			}
			else if (node.getChild(1).isNumber()) {
				operator += "[<=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
			}
		}
		else if (node.getName().equals("gt")) {
			if (node.getChild(0).isNumber()) {
				operator += "[<" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
			}
			else if (node.getChild(1).isNumber()) {
				operator += "[>" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
			}
		}
		else if (node.getName().equals("geq")) {
			if (node.getChild(0).isNumber()) {
				operator += "[<=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
			}
			else if (node.getChild(1).isNumber()) {
				operator += "[>=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
			}
		}
		else if (node.getName().equals("eq")) {
			if (node.getChild(0).isNumber()) {
				operator += "[=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
			}
			else if (node.getChild(1).isNumber()) {
				operator += "[=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
			}
		}
		else {
			operator += "[0,inf]";
		}
		if (prop.getName().equals("G") || prop.getName().equals("F")) {
			property += operator + convertHelper(prop.getChild(1));
		}
		else if (prop.getName().equals("U")) {
			property += convertHelper(prop.getChild(1)) + operator + convertHelper(prop.getChild(2));
		}
		property += "}";
		return property;
	}

	private static String convertHelper(ASTNode op) {
		String convert = "";
		if (op.getName() != null) {
			if (op.getName().equals("and")) {
				convert = "(" + convertHelper(op.getChild(0));
				for (int i = 1; i < op.getNumChildren(); i++) {
					convert += "&" + convertHelper(op.getChild(i));
				}
				convert += ")";
				return convert;
			}
			else if (op.getName().equals("or")) {
				convert = "(" + convertHelper(op.getChild(0));
				for (int i = 1; i < op.getNumChildren(); i++) {
					convert += "|" + convertHelper(op.getChild(i));
				}
				convert += ")";
				return convert;
			}
			else if (op.getName().equals("not")) {
				convert = "~(" + convertHelper(op.getChild(0)) + ")";
				return convert;
			}
			else if (op.getName().equals("lt")) {
				convert = "(" + convertHelper(op.getChild(0)) + "<" + convertHelper(op.getChild(1)) + ")";
				return convert;
			}
			else if (op.getName().equals("leq")) {
				convert = "(" + convertHelper(op.getChild(0)) + "<=" + convertHelper(op.getChild(1)) + ")";
				return convert;
			}
			else if (op.getName().equals("gt")) {
				convert = "(" + convertHelper(op.getChild(0)) + ">" + convertHelper(op.getChild(1)) + ")";
				return convert;
			}
			else if (op.getName().equals("geq")) {
				convert = "(" + convertHelper(op.getChild(0)) + ">=" + convertHelper(op.getChild(1)) + ")";
				return convert;
			}
			else if (op.getName().equals("eq")) {
				convert = "(" + convertHelper(op.getChild(0)) + "=" + convertHelper(op.getChild(1)) + ")";
				return convert;
			}
			else if (op.getName().equals("PG") || op.getName().equals("PF") || op.getName().equals("PU")) {
				String operator = "";
				if (op.getName().equals("PG")) {
					operator += "G";
				}
				else if (op.getName().equals("PF")) {
					operator += "F";
				}
				else {
					operator += "U";
				}
				ASTNode node = op.getChild(0);
				if (node.getName().equals("and")) {
					String min = "0";
					String max = "inf";
					ASTNode child;
					for (int i = 0; i < node.getNumChildren(); i++) {
						child = node.getChild(i);
						if (child.getName().equals("lt")) {
							if (child.getChild(0).isNumber()) {
								max = SBMLutilities.myFormulaToString(child.getChild(0));
							}
							else if (child.getChild(1).isNumber()) {
								min = SBMLutilities.myFormulaToString(child.getChild(1));
							}
						}
						else if (child.getName().equals("leq")) {
							if (child.getChild(0).isNumber()) {
								max = SBMLutilities.myFormulaToString(child.getChild(0));
							}
							else if (child.getChild(1).isNumber()) {
								min = SBMLutilities.myFormulaToString(child.getChild(1));
							}
						}
						else if (child.getName().equals("gt")) {
							if (child.getChild(0).isNumber()) {
								min = SBMLutilities.myFormulaToString(child.getChild(0));
							}
							else if (child.getChild(1).isNumber()) {
								max = SBMLutilities.myFormulaToString(child.getChild(1));
							}
						}
						else if (child.getName().equals("geq")) {
							if (child.getChild(0).isNumber()) {
								min = SBMLutilities.myFormulaToString(child.getChild(0));
							}
							else if (child.getChild(1).isNumber()) {
								max = SBMLutilities.myFormulaToString(child.getChild(1));
							}
						}
					}
					operator += "[" + min + "," + max + "]";
				}
				else if (node.getName().equals("lt")) {
					if (node.getChild(0).isNumber()) {
						operator += "[>" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
					}
					else if (node.getChild(1).isNumber()) {
						operator += "[<" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
					}
				}
				else if (node.getName().equals("leq")) {
					if (node.getChild(0).isNumber()) {
						operator += "[>=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
					}
					else if (node.getChild(1).isNumber()) {
						operator += "[<=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
					}
				}
				else if (node.getName().equals("gt")) {
					if (node.getChild(0).isNumber()) {
						operator += "[<" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
					}
					else if (node.getChild(1).isNumber()) {
						operator += "[>" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
					}
				}
				else if (node.getName().equals("geq")) {
					if (node.getChild(0).isNumber()) {
						operator += "[<=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
					}
					else if (node.getChild(1).isNumber()) {
						operator += "[>=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
					}
				}
				else if (node.getName().equals("eq")) {
					if (node.getChild(0).isNumber()) {
						operator += "[=" + SBMLutilities.myFormulaToString(node.getChild(0)) + "]";
					}
					else if (node.getChild(1).isNumber()) {
						operator += "[=" + SBMLutilities.myFormulaToString(node.getChild(1)) + "]";
					}
				}
				else {
					operator += "[0,inf]";
				}
				if (op.getName().equals("PG") || op.getName().equals("PF")) {
					return "Pr=?{" + operator + convertHelper(op.getChild(1)) + "}";
				}
				else {
					return "Pr=?{" + convertHelper(op.getChild(1)) + operator + convertHelper(op.getChild(2)) + "}";
				}
			}
			else {
				String finalString = SBMLutilities.myFormulaToString(op);
				String[] opers = new String[] { "and(", "or(", "not(", "lt(", "leq(", "gt(", "geq(", "eq(", "PG(", "PF(", "PU(" };
				for (String o : opers) {
					while (finalString.contains(o)) {
						String temp = finalString.substring(finalString.indexOf(o) + o.length());
						String formula = "o";
						int paren = 1;
						while (paren != 0) {
							char c = temp.charAt(0);
							if (c == '(') {
								paren++;
							}
							else if (c == ')') {
								paren--;
							}
							formula += c;
							temp.substring(1);
						}
						finalString = finalString.substring(0, finalString.indexOf(o))
								+ convertHelper(SBMLutilities.myParseFormula(formula)) + temp;
					}
				}
				return finalString;
			}
		}
		else {
			String finalString = SBMLutilities.myFormulaToString(op);
			String[] opers = new String[] { "and(", "or(", "not(", "lt(", "leq(", "gt(", "geq(", "eq(", "PG(", "PF(", "PU(" };
			for (String o : opers) {
				while (finalString.contains(o)) {
					String temp = finalString.substring(finalString.indexOf(o) + o.length());
					String formula = "o";
					int paren = 1;
					while (paren != 0) {
						char c = temp.charAt(0);
						if (c == '(') {
							paren++;
						}
						else if (c == ')') {
							paren--;
						}
						formula += c;
						temp.substring(1);
					}
					finalString = finalString.substring(0, finalString.indexOf(o))
							+ convertHelper(SBMLutilities.myParseFormula(formula)) + temp;
				}
			}
			return finalString;
		}
	}
	
	// getProbpropParts extracts the expressions before and after the PU (after PG and PF)
	// and the time bound from the probprop
	// Currently, we assume no nested until property
	public static String[] getProbpropParts(String probprop){
		String symbol = "@";
		String[] probpropParts;
		probpropParts = new String[5];
		boolean PUFlag = probprop.contains("PU");
		boolean PFFlag = probprop.contains("PF");
		boolean PGFlag = probprop.contains("PG");
		String operator;
		if (PUFlag) {
			operator = "PU";
		}
		else if (PFFlag) {
			operator = "PF";
		}
		else {
			operator = "PG";
		}
		String probpropRight="";
		String probpropLeft="";
		String timeBound="";
		String upperBound="";
		String lowerBound="";
		String relopType = "";
//		probprop.replaceAll("\\W", "");
		if (!probprop.contains(" ")) {
			if (!probprop.equals("")){
				// property should be in this format at this stage: probprop
				// obtain the hsf AFTER bound
				probpropRight= probprop.substring(probprop.indexOf(operator)).substring(probprop.substring(probprop.indexOf(operator)).indexOf("]")+1, probprop.substring(probprop.indexOf(operator)).length());			
				// obtain the time bound
				timeBound= probprop.substring(probprop.indexOf(operator)).substring(probprop.substring(probprop.indexOf(operator)).indexOf("["), probprop.substring(probprop.indexOf(operator)).indexOf("]")+1);							 
				// bound: [lower, upper]
				if (timeBound.contains(",")){
					relopType = "0";
					lowerBound = timeBound.substring(timeBound.indexOf("[")+1, timeBound.indexOf(","));
					upperBound = timeBound.substring(timeBound.indexOf(",")+1, timeBound.indexOf("]"));		 						
				}
				// bound: [<=upper]
				else if(timeBound.contains("<=")){
					relopType = "1";
					lowerBound = "0";
					upperBound = timeBound.substring(timeBound.indexOf("<")+2, timeBound.indexOf("]"));			    
				}
				// bound: [<upper]
				else if (timeBound.contains("<") && !timeBound.contains("=")){
					relopType = "2";
					lowerBound = "0";
					upperBound = timeBound.substring(timeBound.indexOf("<")+1, timeBound.indexOf("]"));			    
				}
				// bound: [>=lower]
				else if (timeBound.contains(">=")) {
					relopType = "3";
					lowerBound = timeBound.substring(timeBound.indexOf(">")+2, timeBound.indexOf("]"));
					upperBound = "inf";
				}
				// bound: [>lower]
				else if (timeBound.contains(">") && !timeBound.contains("=")){
					relopType = "4";
					lowerBound = timeBound.substring(timeBound.indexOf(">")+1, timeBound.indexOf("]"));
					upperBound = "inf";
				}
				// bound: [=k] (k is treated as lowerBound)
				else if (timeBound.contains("=") && !timeBound.contains("<") && !timeBound.contains(">")) {
					relopType = "5";
					lowerBound = timeBound.substring(timeBound.indexOf("=")+1, timeBound.indexOf("]"));
					upperBound = lowerBound;
				}
				if(PUFlag){
					probprop = probprop.replace("PU",symbol);
					// obtain the logic BEFORE the temporal operator
					probpropLeft= probprop.substring(0, probprop.indexOf(symbol));
					// if probpropLeft has a pair of outermost parentheses, remove them
					if (probpropLeft.startsWith("(") && probpropLeft.endsWith(")")){
						probpropLeft=probprop.substring(1,probpropLeft.length()-1);
					}
					if (probpropRight.startsWith("(") && probpropRight.endsWith(")")) {
						probpropRight=probpropRight.substring(1,probpropRight.length()-1);
					}
				}
				if(PFFlag){
					// Remove the outermost parentheses. At this point, probpropRight = (hsf)
					if (probpropRight.startsWith("(")) {
						probpropRight=probpropRight.substring(1,probpropRight.length()-1);
					}
				}
				if(PGFlag){
					// Remove the outermost parentheses. At this point, probpropRight = (hsf)
					if (probpropRight.startsWith("(")) {
						probpropRight=probpropRight.substring(1,probpropRight.length()-1);
					}
				}		
			}
			else { // isHSF = true	
				JOptionPane.showMessageDialog(Gui.frame, "Property does not contain the until operator",
						"Warning in Property", JOptionPane.WARNING_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(Gui.frame, "Property contains white space",
					"Error in Property", JOptionPane.ERROR_MESSAGE);
		}
		probpropParts[0]=probpropLeft;
		probpropParts[1]=probpropRight;
		probpropParts[2]=lowerBound;
		probpropParts[3]=upperBound;
		probpropParts[4]=relopType;
		return probpropParts;
	}
	
	public static ExprTree String2ExprTree(LhpnFile lhpn, String str) {
		boolean retVal;
		ExprTree result = new ExprTree(lhpn);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(str);
		retVal = expr.intexpr_L(str);
		if (retVal) {
			result = expr;
		}
		return result;
	}
	
	private static final String Int = "(-*[\\d]+)";
	private static final String Range = "\\[(-*[\\d]+),(-*[\\d]+)\\]";//"\\[([\\w-]+?),([\\w-]+?)\\]";
	private static final String probabilityValue = "(0\\.[0-9]+)";
}