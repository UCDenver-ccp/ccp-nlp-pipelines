/**
 * 
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.OBOParseException;

import edu.ucdenver.ccp.common.file.FileUtil;
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

	private static final Logger logger = Logger.getLogger(ConceptMapperPipelineFactory.class);

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
			// if we are downloading new source files, then we want to create a new dictionary file
			// in case one already exists, if not then no need to
			boolean cleanDictFile = outputDirectoryOp.equals(CleanDirectory.YES);
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
				return buildChebiDictionary(outputDirectory, chebiIter, cleanDictFile);
			case CL:
				CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildCellTypeDictionary(outputDirectory, clIter, cleanDictFile);
			case NCBI_TAXON:
				NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildNcbiTaxonDictionary(outputDirectory, taxonIter, cleanDictFile);
			case PR:
				ProOntologyClassIterator prIter = new ProOntologyClassIterator(outputDirectory, cleanOutputDirectory);
				return buildProteinOntologyDictionary(outputDirectory, prIter, cleanDictFile);
			case SO:
				SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(outputDirectory,
						cleanOutputDirectory);
				return buildSequenceOntologyDictionary(outputDirectory, soIter, cleanDictFile);
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
	 * @param cleanDictFile
	 *            if true, an already existing dictionary file is overwritten. If false, then the
	 *            pre-existing dictionary file is used and the dictionary building step is therefore
	 *            skipped
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File inputFile, File outputDirectory,
			boolean cleanDictFile) {
		try {
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), inputFile,
						outputDirectory, cleanDictFile);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), inputFile,
						outputDirectory, cleanDictFile);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), inputFile,
						outputDirectory, cleanDictFile);
			case GO_BPMF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF, GoNamespace.BP),
						inputFile, outputDirectory, cleanDictFile);
			case CHEBI:
				ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(inputFile);
				return buildChebiDictionary(outputDirectory, chebiIter, cleanDictFile);
			case CL:
				CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(inputFile);
				return buildCellTypeDictionary(outputDirectory, clIter, cleanDictFile);
			case NCBI_TAXON:
				NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(inputFile);
				return buildNcbiTaxonDictionary(outputDirectory, taxonIter, cleanDictFile);
			case PR:
				ProOntologyClassIterator prIter = new ProOntologyClassIterator(inputFile);
				return buildProteinOntologyDictionary(outputDirectory, prIter, cleanDictFile);
			case SO:
				SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(inputFile);
				return buildSequenceOntologyDictionary(outputDirectory, soIter, cleanDictFile);
			case EG:
				return EntrezGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(inputFile,
						outputDirectory, cleanDictFile);
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
	private static File buildSequenceOntologyDictionary(File outputDirectory, SequenceOntologyClassIterator soIter,
			boolean cleanDictFile) throws IOException {
		File soCmDictFile = new File(outputDirectory, "cmDict-SO.xml");
		if (soCmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(soCmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + soCmDictFile);
				return soCmDictFile;
			}
		}
		logger.info("Building dictionary file: " + soCmDictFile);
		OboToDictionary.buildDictionary(soCmDictFile, soIter, null);
		return soCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param prIter
	 * @return
	 * @throws IOException
	 */
	private static File buildProteinOntologyDictionary(File outputDirectory, ProOntologyClassIterator prIter,
			boolean cleanDictFile) throws IOException {
		File prCmDictFile = new File(outputDirectory, "cmDict-PR.xml");
		if (prCmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(prCmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + prCmDictFile);
				return prCmDictFile;
			}
		}
		logger.info("Building dictionary file: " + prCmDictFile);
		OboToDictionary.buildDictionary(prCmDictFile, prIter, null);
		return prCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param taxonIter
	 * @return
	 * @throws IOException
	 */
	private static File buildNcbiTaxonDictionary(File outputDirectory, NcbiTaxonomyClassIterator taxonIter,
			boolean cleanDictFile) throws IOException {
		File taxonCmDictFile = new File(outputDirectory, "cmDict-NCBITAXON.xml");
		if (taxonCmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(taxonCmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + taxonCmDictFile);
				return taxonCmDictFile;
			}
		}
		logger.info("Building dictionary file: " + taxonCmDictFile);
		OboToDictionary.buildDictionary(taxonCmDictFile, taxonIter, null);
		return taxonCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param clIter
	 * @return
	 * @throws IOException
	 */
	private static File buildCellTypeDictionary(File outputDirectory, CellTypeOntologyClassIterator clIter,
			boolean cleanDictFile) throws IOException {
		File clCmDictFile = new File(outputDirectory, "cmDict-CL.xml");
		if (clCmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(clCmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + clCmDictFile);
				return clCmDictFile;
			}
		}
		logger.info("Building dictionary file: " + clCmDictFile);
		OboToDictionary.buildDictionary(clCmDictFile, clIter, null);
		return clCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param chebiIter
	 * @return
	 * @throws IOException
	 */
	private static File buildChebiDictionary(File outputDirectory, ChebiOntologyClassIterator chebiIter,
			boolean cleanDictFile) throws IOException {
		File chebiCmDictFile = new File(outputDirectory, "cmDict-CHEBI.xml");
		if (chebiCmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(chebiCmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + chebiCmDictFile);
				return chebiCmDictFile;
			}
		}
		logger.info("Building dictionary file: " + chebiCmDictFile);
		OboToDictionary.buildDictionary(chebiCmDictFile, chebiIter, null);
		return chebiCmDictFile;
	}

}
