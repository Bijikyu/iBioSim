/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.learn.genenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * This class is used for running the Java version of the GeneNet learning algorithm.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Run
{

  private static int	experiment;

  /**
   * Runs GeneNet with custom values.
   * 
   * @param Ta - activation threshold
   * @param Tr - repression threshold
   * @param Ti - influence threshold
   * @param Tt - relaxing of activation repression threshold.
   * @param bins - number of bins
   * @param filename - the input sbml file
   * @param directory - the directory of the project.
   * @return true if learn was completed. False otherwise.
   * @throws BioSimException - if something wrong happens with the learn procedure.
   */
  public static boolean run(double Ta, double Tr, double Ti, double Tt, int bins, String filename, String directory) throws BioSimException
  {
    
    SpeciesCollection S = new SpeciesCollection();
    Experiments E = new Experiments();
    Encodings L = new Encodings();
    Thresholds T = new Thresholds(Ta, Tr, Ti, Tt);
    NetCon C = new NetCon();
    init(filename, S);
    loadExperiments(directory, S, E);
    if(experiment < 1)
    {
      return false;
    }
    Learn learn = new Learn(bins);
    learn.learnNetwork(S, E, C, T, L);
    learn.getDotFile("method.gcm", directory, S, C);
    learn.getDotFile("method.dot", directory, S, C);

    return true;
  }
  
  /**
   * Runs GeneNet with default values.
   * 

   * @param filename - the input sbml file
   * @param directory - the directory of the project.
   * @return true if learn was completed. False otherwise.
   * @throws BioSimException - if something wrong happens with the learn procedure.
   */
  public static boolean run(String filename, String directory) throws BioSimException
  {
    
    SpeciesCollection S = new SpeciesCollection();
    Experiments E = new Experiments();
    Encodings L = new Encodings();
    Thresholds T = new Thresholds();
    NetCon C = new NetCon();
    init(filename, S);
    loadExperiments(directory, S, E);
    if(experiment < 1)
    {
      return false;
    }
    Learn learn = new Learn(3);
    learn.learnNetwork(S, E, C, T, L);
    learn.getDotFile("method.gcm", directory, S, C);
    learn.getDotFile("method.dot", directory, S, C);

    return true;
  }

  /**
   * Reads in time-series data to an {@link Experiments} object.
   * 
   * @param directory - where the experiments are located.
   * @param S - the interesting species.
   * @param E - where the data is stored.
   * @throws BioSimException - if there is a problem reading the data.
   */
  public static void loadExperiments(String directory, SpeciesCollection S, Experiments E) throws BioSimException
  {
    File path = new File(directory);
    experiment = 0;
    for (File file : path.listFiles())
    {
      String name = file.getAbsolutePath();
      if (name.endsWith(".tsd"))
      {
        parse(name, S, E);
        experiment++;
      }
      else if (name.endsWith(".csv"))
      {
        parseCSV(name, S, E);
        experiment++;
      }
    }
  }

  /**
   * Retrieves the interesting species of a model.
   * 
   * @param filename - an SBML model.
   * @param S - where the interesting species are stored.
   */
  public static void init(String filename, SpeciesCollection S)
  {
    try
    {
      SBMLDocument doc = SBMLReader.read(new File(filename));

      Model model = doc.getModel();

      for (Species species : model.getListOfSpecies())
      {
        if(species.isSetSBOTerm() && species.getSBOTerm() == 590)
        {
          continue;
        }
        S.addInterestingSpecies(species.getId());
      }
    }

    catch (XMLStreamException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private static void parseCSV(String filename, SpeciesCollection S, Experiments E)
  {
    Scanner scan = null;
    boolean isFirst = true;
    try
    {
      scan = new Scanner(new File(filename));
      int row = 0;
      while (scan.hasNextLine())
      {
        String line = scan.nextLine();

        String[] values = line.split(",");

        if (isFirst)
        {
          for (int i = 0; i < values.length; i++)
          {
            S.addSpecies(values[i], i);
          }
          isFirst = false;
        }
        else
        {
          for (int i = 0; i < values.length; i++)
          {
            E.addExperiment(experiment, row, i, Double.parseDouble(values[i]));
          }
          row++;
        }
      }
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Could not find the file!");
    }
    finally
    {
      if (scan != null)
      {
        scan.close();
      }

    }
  }

  private static void parse(String filename, SpeciesCollection S, Experiments E) throws BioSimException
  {
    InputStream input = null;

    try
    {

      String buffer = "";
      char state = 1;
      char read = 0;
      int row = 0;
      input = new FileInputStream(filename);

      int data = input.read();
      while (data != -1)
      {
        data = input.read();
        read = (char) data;

        if (read == ' ' || read == '\n')
        {
          continue;
        }

        switch (state)
        {
        case 1:
          if (read == '(')
          {
            state = 2;
            buffer = "";
            break;
          }
          else
          {
            return;
          }
        case 2:
          if (read == ')')
          {
            state = 3;

            row = 0;
            String[] ids = buffer.split(",");

            if (experiment == 0)
            {
              for (int i = 0; i < ids.length; i++)
              {
                S.addSpecies(ids[i].substring(1, ids[i].length() - 1), i);
              }
            }

            break;
          }
          else
          {
            buffer += read;

            break;
          }
        case 3:
          if (read == '(')
          {
            state = 4;
            buffer = "";
            break;
          }
          if (read == ',')
          {
            break;
          }
          if (read == ')')
          {
            return;
          }
          else
          {
            return;
          }
        default:
          if (read == ')')
          {
            state = 3;
            String[] values = buffer.replace(" ", "").split(",");

            for (int i = 0; i < values.length; i++)
            {
              E.addExperiment(experiment, row, i, Double.parseDouble(values[i]));
            }

            row++;
          }
          else
          {
            buffer += read;
          }
        }
      }
    }
    catch (FileNotFoundException e)
    {
      throw new BioSimException("Could not find the file!", "Error in Learning");
    }
    catch (IOException e)
    {
      throw new BioSimException("There was a problem when reading the file!", "Error in Learning");
    }
    finally
    {
      try
      {
        if (input != null)
        {
          input.close();
        }
      }
      catch (IOException e)
      {
        throw new BioSimException("Failed to close input stream", "Error in Learning");
      }

    }
  }
}
