/**
 * 
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.geneontology.oboedit.dataadapter.OBOParseException;

import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.fileparsers.ncbi.taxonomy.NcbiTaxonomyClassIterator;
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

	// private static final Logger logger = Logger.getLogger(ConceptMapperPipelineFactory.class);

	public enum DictionaryNamespace {
		GO_CC,
		GO_MF,
		GO_BP,
		GO_BPMF,
		CL,
		CHEBI,
		NCBI_TAXON,
		PR,
		SO,
		EG
	}

	/**
	 * @param dictNamespace
	 * @param outputDirectory
	 * @param outputDirectoryOp
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File outputDirectory,
			CleanDirectory outputDirectoryOp) {
		try {
			boolean cleanOutputDirectory = outputDirectoryOp.equals(CleanDirectory.YES);
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						outputDirectoryOp);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						outputDirectoryOp);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						outputDirectoryOp);
			case GO_BPMF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF, GoNamespace.BP),
						outputDirectory, outputDirectoryOp);
			case CHEBI:
				ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildChebiDictionary(outputDirectory, chebiIter);
			case CL:
				CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildCellTypeDictionary(outputDirectory, clIter);
			case NCBI_TAXON:
				NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildNcbiTaxonDictionary(outputDirectory, taxonIter);
			case PR:
				ProOntologyClassIterator prIter = new ProOntologyClassIterator(outputDirectory, cleanOutputDirectory);
				return buildProteinOntologyDictionary(outputDirectory, prIter);
			case SO:
				SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildSequenceOntologyDictionary(outputDirectory, soIter);
			case EG:
				return EntrezGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(outputDirectory,
						outputDirectoryOp);
			default:
				throw new IllegalArgumentException("Unknown concept mapper dictionary namespace: "
						+ dictNamespace.name());
			}
		} catch (OBOParseException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		} catch (IOException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

	/**
	 * 
	 * @param dictNamespace
	 * @param inputFile
	 * @param outputDirectory
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File inputFile, File outputDirectory) {
		try {
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), inputFile,
						outputDirectory);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), inputFile,
						outputDirectory);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), inputFile,
						outputDirectory);
			case GO_BPMF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF, GoNamespace.BP),
						inputFile, outputDirectory);
			case CHEBI:
				ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(inputFile);
				return buildChebiDictionary(outputDirectory, chebiIter);
			case CL:
				CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(inputFile);
				return buildCellTypeDictionary(outputDirectory, clIter);
			case NCBI_TAXON:
				NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(inputFile);
				return buildNcbiTaxonDictionary(outputDirectory, taxonIter);
			case PR:
				ProOntologyClassIterator prIter = new ProOntologyClassIterator(inputFile);
				return buildProteinOntologyDictionary(outputDirectory, prIter);
			case SO:
				SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(inputFile);
				return buildSequenceOntologyDictionary(outputDirectory, soIter);
			case EG:
				return EntrezGeneDictionaryFactory
						.buildModelOrganismConceptMapperDictionary(inputFile, outputDirectory);
			default:
				throw new IllegalArgumentException("Unknown concept mapper dictionary namespace: "
						+ dictNamespace.name());
			}
		} catch (OBOParseException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		} catch (IOException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

	/**
	 * @param outputDirectory
	 * @param soIter
	 * @return
	 * @throws IOException
	 */
	private static File buildSequenceOntologyDictionary(File outputDirectory, SequenceOntologyClassIterator soIter)
			throws IOException {
		File soCmDictFile = new File(outputDirectory, "cmDict-SO.xml");
		OboToDictionary.buildDictionary(soCmDictFile, soIter, null);
		return soCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param prIter
	 * @return
	 * @throws IOException
	 */
	private static File buildProteinOntologyDictionary(File outputDirectory, ProOntologyClassIterator prIter)
			throws IOException {
		File prCmDictFile = new File(outputDirectory, "cmDict-PR.xml");
		OboToDictionary.buildDictionary(prCmDictFile, prIter, null);
		return prCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param taxonIter
	 * @return
	 * @throws IOException
	 */
	private static File buildNcbiTaxonDictionary(File outputDirectory, NcbiTaxonomyClassIterator taxonIter)
			throws IOException {
		File taxonCmDictFile = new File(outputDirectory, "cmDict-NCBITAXON.xml");
		OboToDictionary.buildDictionary(taxonCmDictFile, taxonIter, null);
		return taxonCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param clIter
	 * @return
	 * @throws IOException
	 */
	private static File buildCellTypeDictionary(File outputDirectory, CellTypeOntologyClassIterator clIter)
			throws IOException {
		File clCmDictFile = new File(outputDirectory, "cmDict-CL.xml");
		OboToDictionary.buildDictionary(clCmDictFile, clIter, null);
		return clCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param chebiIter
	 * @return
	 * @throws IOException
	 */
	private static File buildChebiDictionary(File outputDirectory, ChebiOntologyClassIterator chebiIter)
			throws IOException {
		File chebiCmDictFile = new File(outputDirectory, "cmDict-CHEBI.xml");
		OboToDictionary.buildDictionary(chebiCmDictFile, chebiIter, null);
		return chebiCmDictFile;
	}

}
