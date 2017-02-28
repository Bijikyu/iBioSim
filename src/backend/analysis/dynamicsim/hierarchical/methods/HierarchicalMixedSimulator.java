package backend.analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import dataModels.util.exceptions.BioSimException;
import backend.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import backend.analysis.dynamicsim.hierarchical.io.HierarchicalWriter;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

public final class HierarchicalMixedSimulator extends HierarchicalSimulation
{

	private HierarchicalFBASimulator	fbaSim;
	private HierarchicalODERKSimulator	odeSim;

	// private HierarchicalSimulation ssaSim;

	public HierarchicalMixedSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction, double initialTime, double outputStartTime) throws IOException, XMLStreamException, BioSimException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime, SimType.MIXED);

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(0);
			ModelSetup.setupModels(this, ModelType.HODE);
			computeFixedPoint();

			setupForOutput(runNumber);
			HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
			isInitialized = true;
		}

	}

	@Override
	public void simulate()
	{

		if (!isInitialized)
		{
			try
			{
				initialize(0, getCurrentRun());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
			}
		}
		double nextEndTime = currentTime.getValue(0);
		while (currentTime.getValue(0) < timeLimit)
		{
			nextEndTime = currentTime.getValue(0) + getMaxTimeStep();

			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			if (nextEndTime > getTimeLimit())
			{
				nextEndTime = getTimeLimit();
			}

			odeSim.setTimeLimit(nextEndTime);
			fbaSim.simulate();

			computeAssignmentRules();

			odeSim.simulate();

			currentTime.setValue(0, nextEndTime);

			printToFile();
		}

		try
		{
			getBufferedTSDWriter().write(')');
			getBufferedTSDWriter().flush();
		}
		catch (IOException e)
		{
		}

	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void clear()
	{
	}

	@Override
	public void setupForNewRun(int newRun)
	{
	}

	public void createODESim(HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels)
	{
		try
		{
			odeSim = new HierarchicalODERKSimulator(this, topmodel, submodels);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
	}

	public void createSSASim(HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels)
	{
		// TODO:
	}

	public void createFBASim(HierarchicalModel topmodel, Model model)
	{
		fbaSim = new HierarchicalFBASimulator(this, topmodel);
		fbaSim.setFBA(model);
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

}