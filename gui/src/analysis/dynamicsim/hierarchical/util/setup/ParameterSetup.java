package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class ParameterSetup
{
	/**
	 * puts parameter-related information into data structures
	 */
	public static void setupParameters(ModelState modelstate, Model model)
	{

		for (Parameter parameter : model.getListOfParameters())
		{
			if (modelstate.isDeletedBySId(parameter.getId()))
			{
				continue;
			}
			setupSingleParameter(modelstate, parameter, parameter.getId());
		}

		// for (Parameter parameter : model.getListOfParameters())
		// {
		// if (modelstate.isDeletedBySId(parameter.getId()))
		// {
		// continue;
		// }
		// setupArrayParameter(modelstate, parameter, parameter.getId());
		// }
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private static void setupSingleParameter(ModelState modelstate, Parameter parameter, String parameterID)
	{
		double value = parameter.getValue();

		if (Double.isNaN(value))
		{
			value = 0;
		}

		VariableNode node = new VariableNode(parameter.getId(), value);

		if (parameter.isConstant())
		{
			modelstate.addConstant(node);
		}
		else
		{
			modelstate.addVariable(node);
		}

	}

	/**
	 * 
	 * @param modelstate
	 * @param parameter
	 * @param parameterID
	 */
	private static void setupArrayParameter(ModelState modelstate, Parameter parameter, String parameterID)
	{
		ArraysSetup.setupArrays(modelstate, parameter, modelstate.getNode(parameterID));
	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	public static void setupLocalParameters(ModelState modelstate, KineticLaw kineticLaw, Reaction reaction)
	{

		String reactionID = reaction.getId();

		if (kineticLaw != null)
		{
			for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters())
			{

				String id = localParameter.getId();

				if (modelstate.isDeletedBySId(id))
				{
					continue;
				}
				else if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaId(localParameter.getMetaId()))
				{
					continue;
				}

				String parameterID = reactionID + "_" + id;

				modelstate.addConstant(parameterID, localParameter.getValue());

				HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), id, parameterID);
			}
		}
	}

}
