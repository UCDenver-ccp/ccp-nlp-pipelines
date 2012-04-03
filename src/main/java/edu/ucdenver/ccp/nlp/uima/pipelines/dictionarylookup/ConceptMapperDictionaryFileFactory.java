/**
 * 
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.geneontology.oboedit.dataadapter.OBOParseException;

import edu.ucdenver.ccp.fileparsers.obo.CellTypeOntologyClassIterator;
import edu.ucdenver.ccp.fileparsers.obo.ChebiOntologyClassIterator;
import edu.ucdenver.ccp.fileparsers.obo.SequenceOntologyClassIterator;
import edu.ucdenver.ccp.fileparsers.pro.ProOntologyClassIterator;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.eg.EntrezGeneDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory.GoNamespace;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary;

/**
 * Always creates a new dictionary file by downloading a new data file
 * 
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class ConceptMapperDictionaryFileFactory {

	public enum DictionaryNamespace {
		GO_CC,
		GO_MF,
		GO_BP,
		CL,
		CHEBI,
		PR,
		SO,
		EG
	}

	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File outputDirectory) {
		try {
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						true);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						true);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						true);
			case CHEBI:
				File chebiCmDictFile = new File(outputDirectory, "cmDict-CHEBI.xml");
				ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(outputDirectory, true);
				OboToDictionary.buildDictionary(chebiCmDictFile, chebiIter, null);
				return chebiCmDictFile;
			case CL:
				File clCmDictFile = new File(outputDirectory, "cmDict-CL.xml");
				CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(outputDirectory, true);
				OboToDictionary.buildDictionary(clCmDictFile, clIter, null);
				return clCmDictFile;
			case PR:
				File prCmDictFile = new File(outputDirectory, "cmDict-PR.xml");
				ProOntologyClassIterator prIter = new ProOntologyClassIterator(outputDirectory, true);
				OboToDictionary.buildDictionary(prCmDictFile, prIter, null);
				return prCmDictFile;
			case SO:
				File soCmDictFile = new File(outputDirectory, "cmDict-SO.xml");
				SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(outputDirectory, true);
				OboToDictionary.buildDictionary(soCmDictFile, soIter, null);
				return soCmDictFile;
			case EG:
				return EntrezGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(outputDirectory, true);
			default:
				throw new IllegalArgumentException("Unknown concept mapper dictionary namespace: " + dictNamespace.name());
			}
		} catch (OBOParseException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		} catch (IOException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

}
