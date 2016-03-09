package sbol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

//import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbolstandard.core2.*;

import uk.ac.ncl.intbio.core.io.CoreIoException;
import biomodel.util.GlobalConstants;

public class SBOLUtility2 
{
	private static SBOLDocument SBOLDOC; 

	public static SBOLDocument loadSBOLFiles(HashSet<String> sbolFilePaths) {
		SBOLDocument sbolDoc = new SBOLDocument();
		Preferences biosimrc = Preferences.userRoot();
		
		for (String filePath : sbolFilePaths) 
		{
			File f = new File(filePath);
			String fileName = f.getName().replace(".sbol", "");
			sbolDoc.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,"") + "/" + fileName);
			SBOLReader.setDropObjectsWithDuplicateURIs(true);
			
			
			SBOLReader.setURIPrefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,"") + "/" + fileName);
			try
			{
				String sbolRDF = filePath.replace(".sbol", ".rdf");
				SBOLDocument newSbolDoc = SBOLReader.read(sbolRDF);
				for(ComponentDefinition c : newSbolDoc.getComponentDefinitions())
				{
//					SBOLDOC.createCopy(c);
					sbolDoc.createCopy(c);
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			
//			try
//			{
//				sbolDoc.read(filePath);
//				print(sbolDoc);
//			}
//			catch (FileNotFoundException e)
//			{
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(Gui.frame, "SBOL file not found at " + filePath + ".", 
//						"File Not Found", JOptionPane.ERROR_MESSAGE);
//			}
//			catch (CoreIoException e)
//			{
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(Gui.frame, "Error reading SBOL file at " + filePath + ".", 
//						"Input/Output Error", JOptionPane.ERROR_MESSAGE);
//			}
//			catch (XMLStreamException e)
//			{
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(Gui.frame, "Error reading SBOL file at " + filePath + ".", 
//						"Invalid XML format", JOptionPane.ERROR_MESSAGE);
//			}
//			catch (FactoryConfigurationError e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return sbolDoc;
	}
	
	
	/**
	 * Return the SBOLDocument parsed by the given file
	 * @param filePath
	 * @return
	 */
	public static SBOLDocument loadSBOLFile(String filePath) 
	{
		SBOLDocument sbolDoc = null; 
//		try
//		{
			File f = new File(filePath);
			String fileName = f.getName().replace(".sbol", "");
			Preferences biosimrc = Preferences.userRoot();
			SBOLReader.setURIPrefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,"") + "/" + fileName);
			try
			{
				sbolDoc = SBOLReader.read(new FileInputStream(filePath));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "SBOL file not found at " + filePath + ".", 
						"File Not Found", JOptionPane.ERROR_MESSAGE);
			}
			catch (CoreIoException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Error reading SBOL file at " + filePath + ".", 
						"Input/Output Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Error reading SBOL file at " + filePath + ".", 
						"Invalid XML format", JOptionPane.ERROR_MESSAGE);
			}
			catch (FactoryConfigurationError e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return sbolDoc; 
	}
	
	public static void writeSBOLDocument(String filePath, SBOLDocument sbolDoc) 
	{
		try
		{
			SBOLWriter.write(sbolDoc, new FileOutputStream(filePath));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Error writing SBOL file at " + filePath + ".", 
					"Input/Output Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (XMLStreamException e)
		{
			// TODO Auto-generated catch block
			//TODO: What exceptions are these?
			e.printStackTrace();
		}
		catch (FactoryConfigurationError e)
		{
			// TODO Auto-generated catch block
			//TODO: What exceptions are these?
			e.printStackTrace();
		}
		catch (CoreIoException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Error writing SBOL file at " + filePath + ".", 
					"Input/Output Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// Adds given DNA component and all its subcomponents to top level of SBOL Document if not already present
	// Subcomponents of the given component are replaced by components of matching URIs from the SBOL document to avoid having
	// multiple data structures of the same URI
	public static void addDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc, boolean flatten) 
	{
		dnac = replaceDNAComponent(dnac, sbolDoc);
		Set<String> topURIs = new HashSet<String>();
//		for (SBOLRootObject sbolObj : sbolDoc.getContents())
//			if (sbolObj instanceof DnaComponent)
//				topURIs.add(sbolObj.getURI().toString());
		for (TopLevel sbolObj : sbolDoc.getTopLevels())
			if (sbolObj instanceof ComponentDefinition)
				topURIs.add(sbolObj.getIdentity().toString());
		
		if (flatten)
			flattenDNAComponent(dnac, sbolDoc, topURIs);
//		else if (!topURIs.contains(dnac.getURI().toString()))
//			sbolDoc.addContent(dnac);
		else if (!topURIs.contains(dnac.getIdentity().toString()))
		{
			sbolDoc.createCopy(dnac); 
		}
	}
	
	public static void addSequence(Sequence seq, SBOLDocument sbolDoc, boolean flatten) 
	{
		Set<String> topURIs = new HashSet<String>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels())
			if (sbolObj instanceof Sequence)
				topURIs.add(sbolObj.getIdentity().toString());
		
		if (!topURIs.contains(seq.getIdentity().toString()))
			sbolDoc.createCopy(seq); 
	}
	
	// Adds given DNA component and all its subcomponents to top level of SBOL Document if not already present
	private static void flattenDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc, Set<String> topURIs) {
		if (!topURIs.contains(dnac.getIdentity())) {
//			sbolDoc.addContent(dnac);
			sbolDoc.createCopy(dnac); 
			topURIs.add(dnac.getIdentity().toString());
		}
		Set<SequenceAnnotation> annos = dnac.getSequenceAnnotations(); 
		if (annos != null && annos.size() > 0)
			flattenSubComponents(annos, sbolDoc, topURIs);
	}
	
	// Recursively adds annotation subcomponents to top level of SBOL document if not already present
	private static void flattenSubComponents(Set<SequenceAnnotation> annos, SBOLDocument sbolDoc, Set<String> topURIs) 
	{
		for (SequenceAnnotation anno : annos) {
//			DnaComponent subDnac = anno.getSubComponent();
			ComponentDefinition subDnac = anno.getComponent().getDefinition();
			if (subDnac != null) 
			{
				if (!topURIs.contains(subDnac.getIdentity())) {
					sbolDoc.createCopy(subDnac); 
					topURIs.add(subDnac.getIdentity().toString());
				}
//				List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
				Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations(); 
				if (subAnnos != null && subAnnos.size() > 0)
					flattenSubComponents(subAnnos, sbolDoc, topURIs);
			}
		}
	}
	
	// Replaces DNA component and its subcomponents with components from SBOL document if their URIs match
	// Used to avoid conflict of multiple data structures of same URI in a single SBOL document
	public static ComponentDefinition replaceDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc) 
	{
//		SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) flattenSBOLDocument(sbolDoc);
//		UriResolver<DnaComponent> flattenedResolver = flattenedDoc.getComponentUriResolver();
//		DnaComponent resolvedDnac = flattenedResolver.resolve(dnac.getURI());
		
		
//		ComponentDefinition resolvedDnac = SBOLDOC.getComponentDefinition(dnac.getIdentity());
		ComponentDefinition resolvedDnac = sbolDoc.getComponentDefinition(dnac.getIdentity());
		if (resolvedDnac != null)
			return resolvedDnac;
//		DisplayIdResolver<DnaComponent> flattenedIdResolver = flattenedDoc.getComponentDisplayIdResolver();
//		resolvedDnac = flattenedIdResolver.resolve(dnac.getDisplayId());
//		if (resolvedDnac != null)
//			return resolvedDnac;
		
//		List<SequenceAnnotation> annos = dnac.getAnnotations();
		Set<SequenceAnnotation> annos = dnac.getSequenceAnnotations(); 
		if (annos != null && annos.size() > 0)
		{
//			replaceSubComponents(annos, flattenedResolver);
			replaceSubComponents(annos);
		}
		return dnac;
	}
	
	private static void replaceSubComponents(Set<SequenceAnnotation> annos) 
	{
		for (SequenceAnnotation anno : annos) 
		{
//			DnaComponent subDnac = anno.getSubComponent();
			ComponentDefinition subDnac = anno.getComponentDefinition();
			if (subDnac != null) 
			{
//				DnaComponent resolvedSubDnac = flattenedResolver.resolve(subDnac.getURI());
				ComponentDefinition resolvedSubDnac = SBOLDOC.getComponentDefinition(subDnac.getIdentity());
				if (resolvedSubDnac != null)
				{
//					anno.setSubComponent(resolvedSubDnac);
					
				}
				else 
				{
//					List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
					Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
					{
//						replaceSubComponents(subAnnos, flattenedResolver);
						replaceSubComponents(subAnnos);
					}
				}
			}
		}
	}
	
	// Creates SBOL document in which every DNA component is accessible from the top level
	public static SBOLDocument flattenSBOLDocument(SBOLDocument sbolDoc) 
	{
//		SBOLDocument flattenedDoc = SBOLFactory.createDocument();
//		Set<String> topURIs = new HashSet<String>();
//		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
//			if (sbolObj instanceof DnaComponent) 
//				flattenDNAComponent((DnaComponent) sbolObj, flattenedDoc, topURIs);
//			 else if (sbolObj instanceof org.sbolstandard.core.Collection) 
//			 {
//				flattenedDoc.addContent(sbolObj);
//				for (DnaComponent dnac : ((org.sbolstandard.core.Collection) sbolObj).getComponents())
//					flattenDNAComponent(dnac, flattenedDoc, topURIs);
//			}
//		return flattenedDoc;
		SBOLDocument flattenedDoc = new SBOLDocument();
		Set<String> topURIs = new HashSet<String>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels())
		{
			if (sbolObj instanceof ComponentDefinition) 
				flattenDNAComponent((ComponentDefinition) sbolObj, flattenedDoc, topURIs);
			 else if (sbolObj instanceof Collection) 
			 {
				flattenedDoc.createCopy(sbolObj);
				for (TopLevel dnac : ((Collection) sbolObj).getMembers())
					flattenDNAComponent((ComponentDefinition)dnac, flattenedDoc, topURIs);
			}
		}
		return flattenedDoc;
	}
	
	// Replaces all DNA components of the merging URI with the given DNA component
	// Subcomponents of the given component are replaced by components of matching URIs from the SBOL document to avoid having
	// multiple data structures of the same URI
/*	public static void mergeDNAComponent(URI mergingURI, ComponentDefinition mergingDnac, SBOLDocument sbolDoc) 
	{
//		SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) flattenSBOLDocument(sbolDoc);
//		UriResolver<DnaComponent> flattenedResolver = flattenedDoc.getComponentUriResolver();
//		replaceSubComponents(mergingDnac.getAnnotations(), flattenedResolver);
		SBOLDOC = new SBOLDocument();
		for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
			SBOLDOC.createCopy(c);
		
		replaceSubComponents(mergingDnac.getSequenceAnnotations());
		
//		List<DnaComponent> intersections = intersectDNAComponent(mergingURI, sbolDoc);
		List<ComponentDefinition> intersections = intersectDNAComponent(mergingURI, sbolDoc);
		for (ComponentDefinition intersectedDnac : intersections) {
			if (intersectedDnac.getIdentity().equals(mergingURI)) 
			{
				sbolDoc.removeContent(intersectedDnac);
				sbolDoc.addContent(mergingDnac);
			} 
			else {
				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					DnaComponent subDnac = anno.getSubComponent();
					if (subDnac != null && subDnac.getURI().toString().equals(mergingURI.toString())) 
						intersectedAnnos.add(anno);
				}
				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
					intersectedAnno.setSubComponent(mergingDnac);
				DnaSequence intersectedSeq = intersectedDnac.getDnaSequence();
				String nucleotides = "";
				int position = 0;
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					String subNucleotides;
					if (anno.getStrand() != null && 
							anno.getStrand().getSymbol().equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
						subNucleotides = ((DnaSequenceImpl) anno.getSubComponent().getDnaSequence()).getReverseComplementaryNucleotides();
					else
						subNucleotides = anno.getSubComponent().getDnaSequence().getNucleotides();
					nucleotides = nucleotides + subNucleotides;
					anno.setBioStart(position + 1);
					position = position + subNucleotides.length();
					anno.setBioEnd(position);
				}
				intersectedSeq.setNucleotides(nucleotides);
			}
		}
	}
	*/
	
	// Deletes all DNA components that have URIs matching the given URI and updates the annotations
	// and DNA sequences of composite components using the deleted components
	public static void deleteDNAComponent(URI deletingURI, SBOLDocument sbolDoc) {
//		List<DnaComponent> intersections = intersectDNAComponent(deletingURI, sbolDoc);
//		for (DnaComponent intersectedDnac : intersections) {
//			if (intersectedDnac.getURI().toString().equals(deletingURI.toString())) 
//				sbolDoc.removeContent(intersectedDnac);
//			else {
//				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
//				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
//					DnaComponent subDnac = anno.getSubComponent();
//					if (subDnac != null && subDnac.getURI().toString().equals(deletingURI.toString())) 
//						intersectedAnnos.add(anno);
//				}
//				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
//					intersectedDnac.removeAnnotation(intersectedAnno);
//				DnaSequence intersectedSeq = intersectedDnac.getDnaSequence();
//				String nucleotides = "";
//				int position = 0;
//				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
//					String subNucleotides = anno.getSubComponent().getDnaSequence().getNucleotides();
//					nucleotides = nucleotides + subNucleotides;
//					anno.setBioStart(position + 1);
//					position = position + subNucleotides.length();
//					anno.setBioEnd(position);
//				}
//				intersectedSeq.setNucleotides(nucleotides);
//			}
//		}
		
		List<ComponentDefinition> intersections = intersectDNAComponent(deletingURI, sbolDoc);
		for (ComponentDefinition intersectedDnac : intersections) {
			if (intersectedDnac.getIdentity().toString().equals(deletingURI.toString())) 
			{
				if(SBOLDOC.getComponentDefinition(intersectedDnac.getIdentity()) != null)
				{
					SBOLDOC.removeComponentDefinition(intersectedDnac);
					sbolDoc.removeComponentDefinition(intersectedDnac);
				}
			}
			else 
			{
				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
				for (SequenceAnnotation anno : intersectedDnac.getSequenceAnnotations()) {
					ComponentDefinition subDnac = anno.getComponent().getDefinition();
					if (subDnac != null && subDnac.getIdentity().toString().equals(deletingURI.toString())) 
						intersectedAnnos.add(anno);
				}
				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
				{
					intersectedDnac.removeSequenceAnnotation(intersectedAnno);
				}
				//Note: Assume each compDef has 1 Sequence
				Sequence intersectedSeq = intersectedDnac.getSequences().iterator().next();
				String nucleotides = "";
				int position = 0;
				for (SequenceAnnotation anno : intersectedDnac.getSequenceAnnotations()) {
					String subNucleotides = anno.getComponent().getDefinition().getSequences().iterator().next().getElements();
					nucleotides = nucleotides + subNucleotides;
					int start = position + 1;
					position = position + subNucleotides.length();
					int end = position; 
					anno.addRange(intersectedSeq.getDisplayId()+"_range", start, end);
				}
				intersectedSeq.setElements(nucleotides);
			}
		}
	}
	
	
	// Returns all DNA components from SBOL document that have URIs matching the given URI
	// or that have subcomponents matching the given URI
	public static List<ComponentDefinition> intersectDNAComponent(URI intersectingURI, SBOLDocument sbolDoc) {
//		List<ComponentDefinition> intersections = new LinkedList<ComponentDefinition>();
//		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
//			if (sbolObj instanceof DnaComponent) 
//				if (sbolObj.getURI().toString().equals(intersectingURI.toString()) ) 
//					intersections.add((DnaComponent) sbolObj);
//				else {
//					List<SequenceAnnotation> annos = ((DnaComponent) sbolObj).getAnnotations();
//					if (annos != null && annos.size() > 0)
//						intersections.addAll(intersectWithSubComponents(intersectingURI, (DnaComponent) sbolObj, annos));
//				}
		
		List<ComponentDefinition> intersections = new LinkedList<ComponentDefinition>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof ComponentDefinition) 
				if (sbolObj.getIdentity().toString().equals(intersectingURI.toString()) ) 
					intersections.add((ComponentDefinition) sbolObj);
				else {
					Set<SequenceAnnotation> annos = ((ComponentDefinition) sbolObj).getSequenceAnnotations();
					if (annos != null && annos.size() > 0)
						intersections.addAll(intersectWithSubComponents(intersectingURI, (ComponentDefinition) sbolObj, annos));
				}
		return intersections;
	}
	
	
	private static List<ComponentDefinition> intersectWithSubComponents(URI intersectingURI, ComponentDefinition intersectedDnac, Set<SequenceAnnotation> intersectedAnnos) {
//		List<DnaComponent> subIntersections = new LinkedList<DnaComponent>();
//		Iterator<SequenceAnnotation> annoIterator = intersectedAnnos.iterator();
//		while (subIntersections.size() == 0 && annoIterator.hasNext()) {
//			DnaComponent subDnac = annoIterator.next().getSubComponent();
//			if (subDnac != null && subDnac.getURI().toString().equals(intersectingURI.toString()))
//				subIntersections.add(intersectedDnac);
//		}
//		if (subIntersections.size() == 0) {
//			for (SequenceAnnotation intersectedAnno : intersectedAnnos) {
//				DnaComponent subDnac = intersectedAnno.getSubComponent();
//				if (subDnac != null) {
//					List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
//					if (subAnnos != null && subAnnos.size() > 0)
//						subIntersections.addAll(intersectWithSubComponents(intersectingURI, subDnac, subAnnos));
//				}
//			}
//			if (subIntersections.size() > 0)
//				subIntersections.add(intersectedDnac);
//		}
		
		List<ComponentDefinition> subIntersections = new LinkedList<ComponentDefinition>();
		Iterator<SequenceAnnotation> annoIterator = intersectedAnnos.iterator();
		while (subIntersections.size() == 0 && annoIterator.hasNext()) {
			ComponentDefinition subDnac = annoIterator.next().getComponent().getDefinition();
			if (subDnac != null && subDnac.getIdentity().toString().equals(intersectingURI.toString()))
				subIntersections.add(intersectedDnac);
		}
		if (subIntersections.size() == 0) {
			for (SequenceAnnotation intersectedAnno : intersectedAnnos) {
				ComponentDefinition subDnac = intersectedAnno.getComponent().getDefinition();
				if (subDnac != null) {
					Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
						subIntersections.addAll(intersectWithSubComponents(intersectingURI, subDnac, subAnnos));
				}
			}
			if (subIntersections.size() > 0)
				subIntersections.add(intersectedDnac);
		}
		return subIntersections;
	}
	
	
	
//	public static List<String> loadDNAComponentTypes(DnaComponent dnaComp) {
//		List<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
//		dnaComps.add(dnaComp);
//		return loadDNAComponentTypes(dnaComps);
//	}
	
//	public static List<String> loadDNAComponentTypes(List<DnaComponent> dnaComps) {
//		List<String> types = new LinkedList<String>();
//		for (DnaComponent lowestComp : loadLowestSubComponents(dnaComps))
//			if (lowestComp.getTypes().size() > 0) {
//				types.add(convertURIToSOType(lowestComp.getTypes().iterator().next()));
//			}
//		return types;
//	}
	
	public static List<SequenceAnnotation> orderSequenceAnnotations(Set<SequenceAnnotation> unsorted) {
		List<SequenceAnnotation> sorted = new LinkedList<SequenceAnnotation>();
		for (SequenceAnnotation anno : unsorted)
			for (int i = 0; i <= sorted.size(); i++)
			{
//				if (i == sorted.size() 
//						|| anno.getBioStart().compareTo(sorted.get(i).getBioStart()) <= 0) {
//					sorted.add(i, anno);
//					i = sorted.size();
//				} 
				
				//Assume that each SeqAnnot. has one location and that it is a Range?
				Range anno_range = (Range) anno.getLocations().iterator().next();
				Range sorted_range = (Range) sorted.get(i).getLocations().iterator().next();
				if (i == sorted.size() 
						|| anno_range.getStart() <= sorted_range.getStart()) 
				{
					sorted.add(i, anno);
					i = sorted.size();
				} 
			}
		return sorted;
	}

//	public static List<String> loadDNAComponentTypes(DnaComponent dnaComp, String strand) {
//		List<String> types = new LinkedList<String>();
//		if (dnaComp.getAnnotations().size() == 0 && dnaComp.getTypes().size() > 0) {
//			types.add(strand);
//			types.add(convertURIToSOType(dnaComp.getTypes().iterator().next()));
//		} else {
//			List<SequenceAnnotation> annos = sortSequenceAnnotations(dnaComp.getAnnotations());
//			String prevSubStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//			int minusIndex = 0;
//			for (SequenceAnnotation anno : annos) {
//				String subStrand;
//				if (anno.getStrand() == null) {
//					subStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//				} else
//					subStrand = anno.getStrand().getSymbol();
//				List<String> nextTypes;
//				if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
//					if (subStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
//						nextTypes = loadDNAComponentTypes(anno.getSubComponent(), 
//								GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
//					else 
//						nextTypes = loadDNAComponentTypes(anno.getSubComponent(), strand);
//				else
//					nextTypes = loadDNAComponentTypes(anno.getSubComponent(), subStrand);
//				if (subStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND)) {
//					if (!subStrand.equals(prevSubStrand))
//						minusIndex = types.size();
//					types.addAll(minusIndex, nextTypes);
//				} else
//					types.addAll(nextTypes);
//				prevSubStrand = subStrand;
//			}
//		}
//		return types;
//	}
//	
//	public static List<String> loadDNAComponentTypes(DnaComponent dnaComp) {
//		return loadDNAComponentTypes(dnaComp, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
//	}
//
//	public static List<String> loadDNAComponentTypes(List<DnaComponent> dnaComps) {
//		List<String> types = new LinkedList<String>();
//		for (DnaComponent dnaComp : dnaComps) {
//			types.addAll(loadDNAComponentTypes(dnaComp, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND));
//		}
//		return types;
//	}
//	
//	public static List<String> loadNodeTypes(AssemblyNode assemblyNode) {
//		return loadDNAComponentTypes(assemblyNode.getDNAComponents());
//	}
//	
//	public static List<String> loadNodeTypes(List<AssemblyNode> assemblyNodes) {
//		List<String> types = new LinkedList<String>();
//		for (AssemblyNode assemblyNode : assemblyNodes)
//			types.addAll(loadNodeTypes(assemblyNode));
//		return types;
//	}
//	
	
	public static List<URI> loadDNAComponentURIs(List<ComponentDefinition> dnaComps) {
		List<URI> uris = new LinkedList<URI>();
		for (ComponentDefinition lowestComp : loadLowestSubComponents(dnaComps))
			uris.add(lowestComp.getIdentity());
		return uris;
	}
	
	
//	public static List<URI> loadNodeURIs(AssemblyNode assemblyNode) {
//		return loadDNAComponentURIs(assemblyNode.getDNAComponents());
//	}
//	
//	public static List<URI> loadNodeURIs(List<AssemblyNode> assemblyNodes) {
//		List<URI> uris = new LinkedList<URI>();
//		for (AssemblyNode assemblyNode : assemblyNodes)
//			uris.addAll(loadNodeURIs(assemblyNode));
//		return uris;
//	}

	public static List<ComponentDefinition> loadLowestSubComponents(List<ComponentDefinition> dnaComps) {
		List<ComponentDefinition> subComps = new LinkedList<ComponentDefinition>();
		List<ComponentDefinition> tempComps = new LinkedList<ComponentDefinition>(dnaComps);
//		while (tempComps.size() > 0) {
//			if (tempComps.get(0).getAnnotations().size() > 0)
//				for (SequenceAnnotation anno : tempComps.remove(0).getAnnotations())
//					tempComps.add(anno.getSubComponent());
//			else
//				subComps.add(tempComps.remove(0));
//		}
		
		while (tempComps.size() > 0) 
		{
			if (tempComps.get(0).getSequenceAnnotations().size() > 0)
				for (SequenceAnnotation anno : tempComps.remove(0).getSequenceAnnotations())
					tempComps.add(anno.getComponentDefinition());
			else
				subComps.add(tempComps.remove(0));
		}
		return subComps;
	}
	
//	public static List<DnaComponent> filterDNAComponents(List<DnaComponent> dnaComps, Set<String> soFilterTypes) {
//		List<DnaComponent> subComps = new LinkedList<DnaComponent>();
//		List<DnaComponent> tempComps = new LinkedList<DnaComponent>(dnaComps);
//		while (tempComps.size() > 0) {
//			DnaComponent tempComp = tempComps.remove(0);
//			if (tempComp.getTypes().size() > 0 && soFilterTypes.contains(convertURIToSOTerm(tempComp.getTypes().iterator().next())))
//				subComps.add(tempComp);
//			if (tempComp.getAnnotations().size() > 0)
//				for (SequenceAnnotation anno : tempComp.getAnnotations())
//					tempComps.add(anno.getSubComponent());
//		}
//		return subComps;
//	}
	
//	public static List<DnaComponent> filterDNAComponents(List<DnaComponent> dnaComps, Set<String> soFilterNums) {
//		List<DnaComponent> filteredComps = new LinkedList<DnaComponent>();
//		for (DnaComponent dnaComp : dnaComps) {
//			String soNum = SBOLUtility2.loadSONumber(dnaComp);
//			if (soNum != null && soFilterNums.contains(soNum))
//				filteredComps.add(dnaComp);
//		}
//		return filteredComps;
//	}
//	
//	public static boolean compareDNAComponents(List<DnaComponent> dnaComps1, List<DnaComponent> dnaComps2) {
//		if (dnaComps1.size() == dnaComps2.size()) {
//			for (int i = 0; i < dnaComps1.size(); i++)
//				if (!dnaComps1.get(i).equals(dnaComps2.get(i)))
//					return false;
//			return true;
//		}
//		return false;
//	}
//	
//	public static boolean compareDNAComponents(Set<DnaComponent> dnaComps1, Set<DnaComponent> dnaComps2) {
//		for (DnaComponent dnaComp1 : dnaComps1) {
//			Iterator<DnaComponent> compIterator2 = dnaComps2.iterator();
//			boolean match = false;
//			while (compIterator2.hasNext() && !match)
//				match = dnaComp1.equals(compIterator2.next());
//			if (!match)
//				return false;
//		}
//		if (dnaComps1.size() == dnaComps2.size())
//			return true;
//		for (DnaComponent dnaComp2 : dnaComps2) {
//			Iterator<DnaComponent> compIterator1 = dnaComps1.iterator();
//			boolean match = false;
//			while (compIterator1.hasNext() && !match)
//				match = dnaComp2.equals(compIterator1.next());
//			if (!match)
//				return false;
//		}
//		return true;	
//	}
//	
//	public static boolean shareDNAComponent(Set<DnaComponent> dnaComps1, Set<DnaComponent> dnaComps2) {
//		if (dnaComps1.size() == 0 || dnaComps2.size() == 0)
//			return false;
//		for (DnaComponent dnaComp1 : dnaComps1) {
//			for (DnaComponent dnaComp2 : dnaComps2)
//				if (dnaComp1.equals(dnaComp2))
//					return true;
//		}
//		return false;	
//	}
//	
//	public static int countNucleotides(List<DnaComponent> dnaComps) {
//		int nucleotideCount = 0;
//		for (DnaComponent dnaComp : dnaComps) {
//			if (dnaComp.getDnaSequence() != null)
//				nucleotideCount += dnaComp.getDnaSequence().getNucleotides().length();
//		} 
//		return nucleotideCount;
//	}
	
	// Converts global constant SBOL type to corresponding set of SO types
	public static Set<String> soSynonyms(String sbolType) {
		Set<String> types = new HashSet<String>();
		if (sbolType.equals(GlobalConstants.SBOL_CDS)) {
			types.add(SequenceOntology.CDS.toString()); // CDS
		} else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER)) {
			types.add(SequenceOntology.PROMOTER.toString()); // promoter
			types.add(SequenceOntology.type("SO_0001203").toString()); // RNA_polymerase_promoter
		} else if (sbolType.equals(GlobalConstants.SBOL_RBS)) {
			types.add(SequenceOntology.type("SO_0000139").toString()); // ribosome_entry_site
		} else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR)) {
			types.add(SequenceOntology.TERMINATOR.toString()); // terminator
			types.add(SequenceOntology.type("SO_0000614").toString()); // bacterial_terminator
		}
		return types;
	}
	
//	public static String convertURIToSOType(URI uri) {
//		String temp = uri.toString();
//		if (temp.equals(SequenceOntology.PROMOTER.toString()))
//			return "promoter";
//		else if (temp.equals(SequenceOntology.type("SO_0001203").toString()))
//			return "RNA polymerase promoter";
//		else if (temp.equals(SequenceOntology.type("SO_0000139").toString()))
//			return "ribosome entry site";
//		else if (temp.equals(SequenceOntology.CDS.toString()))
//			return "coding sequence";
//		else if (temp.equals(SequenceOntology.TERMINATOR.toString()))
//			return "terminator";
//		else if (temp.equals(SequenceOntology.type("SO_0000614").toString()))
//			return "bacterial terminator";
//		else if (temp.equals(SequenceOntology.type("SO_0000804").toString()))
//			return "engineered region";
//		else
//			return "N/A";
//	}
	
	public static String convertURIToSOTerm(URI uri) 
	{
		if (uri.equals(SequenceOntology.PROMOTER))
			return "promoter";
		else if (uri.equals(SequenceOntology.type(GlobalConstants.SO_RBS)))
			return "ribosome entry site";
		else if (uri.equals(SequenceOntology.CDS))
			return "coding sequence";
		else if (uri.equals(SequenceOntology.ENGINEERED_GENE))
			return "engineered gene";
		else if (uri.equals(SequenceOntology.FIVE_PRIME_UTR))
			return "five prime utr";
		else if (uri.equals(SequenceOntology.GENE))
			return "gene";
		else if (uri.equals(SequenceOntology.INSULATOR))
			return "insulator";
		else if (uri.equals(SequenceOntology.MRNA))
			return "messenger RNA";
		else if (uri.equals(SequenceOntology.OPERATOR))
			return "operator";
		else if (uri.equals(SequenceOntology.ORIGIN_OF_REPLICATION))
			return "origin of replication";
		else if (uri.equals(SequenceOntology.PRIMER_BINDING_SITE))
			return "primer binding site";
		else if (uri.equals(SequenceOntology.RESTRICTION_ENZYME_RECOGNITION_SITE))
			return "restriction enzyme recognition site";
		else if (uri.equals(SequenceOntology.RIBOSOME_ENTRY_SITE))
			return "ribosome entry site";
		else if (uri.equals(SequenceOntology.TERMINATOR))
			return "terminator";
		else if (uri.equals(SequenceOntology.ENGINEERED_REGION))
			return "engineered region";
		else if (uri.equals(SequenceOntology.type("SO:0000805")))
			return "engineered foreign region";
		else if (uri.equals(SequenceOntology.type("SO:0001203")))
			return "RNA polymerase promoter";
		else if (uri.equals(SequenceOntology.type("SO:0000552")))
			return "Shine Dalgarno sequence";
		else if (uri.equals(SequenceOntology.type("SO:0000614"))) 
			return "bacterial terminator";
		else 
		{
			String path = uri.getPath();
			if (path != null && path.length() > 0) 
			{
				String[] splitPath = path.split("/");
				return splitPath[splitPath.length - 1];
			}
			return "N/A";
		}
	}
	
	
	public static String convertRegexSOTermsToNumbers(String regex) 
	{
		regex = regex.replaceAll("promoter", "SO_0000167");
		regex = regex.replaceAll("ribosome entry site", "SO_0000139");
		regex = regex.replaceAll("coding sequence", "SO_0000316");
		regex = regex.replaceAll("terminator", "SO_0000141");
		return regex;
	}
	
	public static String loadSONumber(ComponentDefinition dnaComp) {
		for (URI uri : dnaComp.getRoles()) {
			String authority = uri.getAuthority();
//			if (authority != null && authority.equals(GlobalConstants.SO_AUTHORITY)) {
			if (authority != null && authority.equals(GlobalConstants.SO_AUTHORITY2)) {
				String path = uri.getPath();
				if (path != null && path.length() > 0) {
					String[] splitPath = path.split("/");
					if (splitPath[splitPath.length - 1].startsWith("SO"))
						return splitPath[splitPath.length - 1];
				}
			}
		}
		return null;
	}
	
//	public static String soTypeToGrammarTerminal(String soType) {
//		if (soType.equals("promoter") || soType.equals("RNA polymerase promoter"))
//			return "p";
//		else if (soType.equals("ribosome entry site"))
//			return "r";
//		else if (soType.equals("coding sequence"))
//			return "c";
//		else if (soType.equals("terminator") || soType.equals("bacterial terminator"))
//			return "t";
//		else if (soType.equals("engineered region"))
//			return "e";
//		else
//			return "x";
//	}
//	
//	public static Properties loadSBOLSynthesisProperties(String synthFilePath, String separator, JFrame frame) {
//		Properties synthProps = new Properties();
//		for (String synthFileID : new File(synthFilePath).list())
//			if (synthFileID.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)) {
//				try {
//					FileInputStream propStreamIn = new FileInputStream(new File(synthFilePath + separator + synthFileID));
//					synthProps.load(propStreamIn);
//					propStreamIn.close();
//					return synthProps;
//				} catch (IOException e) {
//					JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
//				}
//			}	
//		return synthProps;
//	}
	
}
