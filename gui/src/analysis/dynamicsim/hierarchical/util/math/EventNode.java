package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.List;

public class EventNode
{

	private HierarchicalNode	triggerValue;
	private HierarchicalNode	delayValue;
	private HierarchicalNode	priorityValue;

	private boolean				isPersistent;
	private boolean				useTriggerValue;

	private boolean				isEnabled;
	private double				fireTime;
	private double				maxDisabledTime;
	private double				minEnabledTime;
	private double				priority;

	private double[]			assignmentValues;
	List<EventAssignmentNode>	eventAssignments;

	public EventNode()
	{
		maxDisabledTime = Double.NEGATIVE_INFINITY;
		minEnabledTime = Double.POSITIVE_INFINITY;
		fireTime = Double.POSITIVE_INFINITY;
	}

	public HierarchicalNode getTriggerValue()
	{
		return triggerValue;
	}

	public void setTriggerValue(HierarchicalNode triggerValue)
	{
		this.triggerValue = triggerValue;
	}

	public HierarchicalNode getDelayValue()
	{
		return delayValue;
	}

	public void setDelayValue(HierarchicalNode delayValue)
	{
		this.delayValue = delayValue;
	}

	public boolean isPersistent()
	{
		return isPersistent;
	}

	public void setPersistent(boolean isPersistent)
	{
		this.isPersistent = isPersistent;
	}

	public boolean isUseTriggerValue()
	{
		return useTriggerValue;
	}

	public void setUseTriggerValue(boolean useTriggerValue)
	{
		this.useTriggerValue = useTriggerValue;
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled = isEnabled;
	}

	public double getFireTime()
	{
		return fireTime;
	}

	public void setFireTime(double fireTime)
	{
		this.fireTime = fireTime;
	}

	public double getMaxDisabledTime()
	{
		return maxDisabledTime;
	}

	public void setMaxDisabledTime(double maxDisabledTime)
	{
		this.maxDisabledTime = maxDisabledTime;
	}

	public double getMinEnabledTime()
	{
		return minEnabledTime;
	}

	public void setMinEnabledTime(double minEnabledTime)
	{
		this.minEnabledTime = minEnabledTime;
	}

	public void addEventAssignment(EventAssignmentNode eventAssignmentNode)
	{
		if (eventAssignments == null)
		{
			eventAssignments = new ArrayList<EventAssignmentNode>();
		}

		eventAssignments.add(eventAssignmentNode);
	}

	public boolean computeEnabled(double time)
	{
		if (maxDisabledTime >= 0 && maxDisabledTime <= minEnabledTime && minEnabledTime <= time)
		{
			isEnabled = true;
			if (priorityValue != null)
			{
				priority = Evaluator.evaluateExpressionRecursive(priorityValue);
			}
			if (delayValue != null)
			{
				fireTime = time + Evaluator.evaluateExpressionRecursive(delayValue);
			}
			else
			{
				fireTime = time;
			}
			if (useTriggerValue)
			{
				computeEventAssignmentValues(time);
			}
		}

		return isEnabled;
	}

	public void fireEvent(double time)
	{
		if (isEnabled && fireTime <= time)
		{
			isEnabled = false;

			maxDisabledTime = Double.NEGATIVE_INFINITY;
			minEnabledTime = Double.POSITIVE_INFINITY;

			if (!isPersistent)
			{
				if (!computeTrigger())
				{
					maxDisabledTime = time;
					return;
				}
			}

			if (!useTriggerValue)
			{
				computeEventAssignmentValues(time);
			}
			for (int i = 0; i < eventAssignments.size(); i++)
			{
				EventAssignmentNode eventAssignmentNode = eventAssignments.get(i);
				VariableNode variable = eventAssignmentNode.getVariable();
				variable.setValue(assignmentValues[i]);
			}

			isTriggeredAtTime(time);
		}

	}

	private void computeEventAssignmentValues(double time)
	{
		assignmentValues = new double[eventAssignments.size()];
		for (int i = 0; i < eventAssignments.size(); i++)
		{
			EventAssignmentNode eventAssignmentNode = eventAssignments.get(i);
			VariableNode variable = eventAssignmentNode.getVariable();
			HierarchicalNode math = eventAssignmentNode.getMath();
			double value = Evaluator.evaluateExpressionRecursive(math, false);
			if (variable.isSpecies())
			{
				SpeciesNode species = (SpeciesNode) variable;
				if (!species.hasOnlySubstance())
				{
					assignmentValues[i] = value * species.getCompartment().getValue();
					continue;
				}
			}
			assignmentValues[i] = value;
		}
	}

	public boolean computeTrigger()
	{
		double triggerResult = Evaluator.evaluateExpressionRecursive(triggerValue);
		return triggerResult != 0;
	}

	public boolean isTriggeredAtTime(double time)
	{
		boolean trigger = computeTrigger();
		if (trigger)
		{
			if (maxDisabledTime >= 0 && time > maxDisabledTime && time < minEnabledTime)
			{
				minEnabledTime = time;
			}
			return maxDisabledTime >= 0 && minEnabledTime <= time;
		}
		else
		{
			if (time > maxDisabledTime)
			{
				maxDisabledTime = time;
			}

			return false;
		}
	}

	public double getPriority()
	{
		if (isEnabled)
		{
			return priority;
		}
		return 0;
	}

	public HierarchicalNode getPriorityValue()
	{
		return priorityValue;
	}

	public void setPriorityValue(HierarchicalNode priorityValue)
	{
		this.priorityValue = priorityValue;
	}

	public List<EventAssignmentNode> getEventAssignments()
	{
		return eventAssignments;
	}

	public void setEventAssignments(List<EventAssignmentNode> eventAssignments)
	{
		this.eventAssignments = eventAssignments;
	}
}
