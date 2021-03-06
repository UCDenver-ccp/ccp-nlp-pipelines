package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil.SynonymType;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.CellTypeOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.ChebiOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.NcbiTaxonomyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.SequenceOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.pro.ProOntologyClassIterator;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.ncbi.NcbiGeneDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory.GoNamespace;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory.IncludeFunkSynonyms;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.IncludeExt;

/**
 * Always creates a new dictionary file by downloading a new data file
 * 
 * @author Center for Computational Pharmacology, UC Denver;
 *         ccpsupport@ucdenver.edu
 * 
 */
public class ConceptMapperDictionaryFileFactory {

	private static final Logger logger = Logger.getLogger(ConceptMapperPipelineFactory.class);

	public enum DictionaryNamespace {
		GO, GO_CC, GO_MF, GO_BP, FUNK_GO, FUNK_GO_CC, FUNK_GO_MF, FUNK_GO_BP, CL, CHEBI, MOP, MOP_EXT, NCBI_TAXON, PR,
		SO, EG, OBO, DOID, UBERON, UBERON_EXT, MI, SUPPLEMENTARY_CHEMICALS, HP
	}

	/**
	 * @param dictNamespace
	 * @param outputDirectory
	 * @param outputDirectoryOp
	 * @param dictEntryModifier
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File outputDirectory,
			CleanDirectory outputDirectoryOp, SynonymType synonymType, DictionaryEntryModifier dictEntryModifier,
			IncludeExt includeExt) {
		try {
			// if we are downloading new source files, then we want to create a
			// new dictionary file
			// in case one already exists, if not then no need to
			boolean cleanDictFile = outputDirectoryOp.equals(CleanDirectory.YES);
			switch (dictNamespace) {
			case GO:
				return GoDictionaryFactory.buildConceptMapperDictionary(
						EnumSet.of(GoNamespace.CC, GoNamespace.BP, GoNamespace.MF), outputDirectory, outputDirectoryOp,
						synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case FUNK_GO:
				return GoDictionaryFactory.buildConceptMapperDictionary(
						EnumSet.of(GoNamespace.CC, GoNamespace.BP, GoNamespace.MF), outputDirectory, outputDirectoryOp,
						synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						outputDirectoryOp, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case CHEBI:
				return buildChebiDictionary(outputDirectory, cleanDictFile, synonymType, dictEntryModifier, includeExt);
			case CL:
				return buildCellTypeDictionary(outputDirectory, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case NCBI_TAXON:
				return buildNcbiTaxonDictionary(outputDirectory, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case PR:
				return buildProteinOntologyDictionary(outputDirectory, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case SO:
				return buildSequenceOntologyDictionary(outputDirectory, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case EG:
				return NcbiGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(outputDirectory,
						outputDirectoryOp);

			default:
				throw new IllegalArgumentException(
						"Concept mapper dictionary namespace not handled: " + dictNamespace.name());
			}
		} catch (IOException | OWLOntologyCreationException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

	/**
	 * 
	 * @param dictNamespace
	 * @param inputFile
	 * @param outputDirectory
	 * @param cleanDictFile     if true, an already existing dictionary file is
	 *                          overwritten. If false, then the pre-existing
	 *                          dictionary file is used and the dictionary building
	 *                          step is therefore skipped
	 * @param dictEntryModifier
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFileFromOBO(DictionaryNamespace dictNamespace, File inputFile,
			File outputDirectory, boolean cleanDictFile, SynonymType synonymType, String dictId,
			DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt) {
		try {
			File dictionaryFile = getDictionaryFile(outputDirectory, dictNamespace, dictId);
			switch (dictNamespace) {
			case GO:
				return GoDictionaryFactory.buildConceptMapperDictionary(
						EnumSet.of(GoNamespace.CC, GoNamespace.BP, GoNamespace.MF), outputDirectory, inputFile,
						cleanDictFile, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.NO, dictEntryModifier, includeExt);
			case FUNK_GO:
				return GoDictionaryFactory.buildConceptMapperDictionary(
						EnumSet.of(GoNamespace.CC, GoNamespace.BP, GoNamespace.MF), outputDirectory, inputFile,
						cleanDictFile, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case FUNK_GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						inputFile, cleanDictFile, synonymType, IncludeFunkSynonyms.YES, dictEntryModifier, includeExt);
			case CHEBI:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case CL:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case NCBI_TAXON:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case PR:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case SO:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case DOID:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case HP:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case UBERON:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case UBERON_EXT:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case MOP:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case MOP_EXT:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case MI:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);
			case EG:
				return NcbiGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(inputFile, dictionaryFile,
						cleanDictFile);
			case OBO:
				return buildDictionary(inputFile, dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
						includeExt);

			default:
				throw new IllegalArgumentException(
						"Unknown concept mapper dictionary namespace: " + dictNamespace.name());
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

	/**
	 * @param outputDirectory
	 * @param dictNamespace
	 * @param id              if non-null this can be used to make the dictionary
	 *                        file unique. useful in cases when processing in
	 *                        parallel using multiple dictionaries
	 * @return
	 */
	public static File getDictionaryFile(File outputDirectory, DictionaryNamespace dictNamespace, String id) {
		return new File(outputDirectory, "cmDict-" + dictNamespace.name() + ((id != null) ? "." + id : "") + ".xml");
	}

	/**
	 * @param inputOboFile
	 * @param dictionaryFile
	 * @param cleanDictFile
	 * @param synonymType
	 * @param dictEntryModifier
	 * @param includeExt
	 * @return
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	 */
	private static File buildDictionary(File inputOboFile, File dictionaryFile, boolean cleanDictFile,
			SynonymType synonymType, DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IOException, OWLOntologyCreationException {
		if (dictionaryFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(dictionaryFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + dictionaryFile);
				return dictionaryFile;
			}
		}
		logger.info("Building dictionary file: " + dictionaryFile);
		long time = System.currentTimeMillis();
		OntologyUtil ontUtil = new OntologyUtil(inputOboFile);
		logger.info("Elapsed time to load ontology: " + ((System.currentTimeMillis() - time) / 1000) + "s");
		OboToDictionary.buildDictionary(dictionaryFile, ontUtil, null, synonymType, dictEntryModifier, includeExt);
		return dictionaryFile;
	}

	private static File buildSequenceOntologyDictionary(File dictionaryFile, boolean cleanDictFile,
			SynonymType synonymType, DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IllegalArgumentException, IllegalAccessException, OWLOntologyCreationException, IOException {
		SequenceOntologyClassIterator soIter = null;
		try {
			soIter = new SequenceOntologyClassIterator(dictionaryFile.getParentFile(), cleanDictFile);
			return buildDictionary(soIter.getOboFile(), dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
					includeExt);
		} finally {
			if (soIter != null) {
				soIter.close();
			}
		}
	}

	private static File buildProteinOntologyDictionary(File dictionaryFile, boolean cleanDictFile,
			SynonymType synonymType, DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IOException, OWLOntologyCreationException, IllegalArgumentException, IllegalAccessException {
		ProOntologyClassIterator prIter = null;
		try {
			prIter = new ProOntologyClassIterator(dictionaryFile.getParentFile(), cleanDictFile);
			return buildDictionary(prIter.getProOntologyOboFile(), dictionaryFile, cleanDictFile, synonymType,
					dictEntryModifier, includeExt);
		} finally {
			if (prIter != null) {
				prIter.close();
			}
		}
	}

	private static File buildNcbiTaxonDictionary(File dictionaryFile, boolean cleanDictFile, SynonymType synonymType,
			DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IOException, OWLOntologyCreationException, IllegalArgumentException, IllegalAccessException {
		NcbiTaxonomyClassIterator taxonIter = null;
		try {
			taxonIter = new NcbiTaxonomyClassIterator(dictionaryFile.getParentFile(), cleanDictFile);
			return buildDictionary(taxonIter.getOboFile(), dictionaryFile, cleanDictFile, synonymType,
					dictEntryModifier, includeExt);
		} finally {
			if (taxonIter != null) {
				taxonIter.close();
			}
		}
	}

	private static File buildCellTypeDictionary(File dictionaryFile, boolean cleanDictFile, SynonymType synonymType,
			DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IOException, OWLOntologyCreationException, IllegalArgumentException, IllegalAccessException {
		CellTypeOntologyClassIterator clIter = null;
		try {
			clIter = new CellTypeOntologyClassIterator(dictionaryFile.getParentFile(), cleanDictFile);
			return buildDictionary(clIter.getOboFile(), dictionaryFile, cleanDictFile, synonymType, dictEntryModifier,
					includeExt);
		} finally {
			if (clIter != null) {
				clIter.close();
			}
		}
	}

	private static File buildChebiDictionary(File dictionaryFile, boolean cleanDictFile, SynonymType synonymType,
			DictionaryEntryModifier dictEntryModifier, IncludeExt includeExt)
			throws IOException, OWLOntologyCreationException, IllegalArgumentException, IllegalAccessException {
		ChebiOntologyClassIterator chebiIter = null;
		try {
			chebiIter = new ChebiOntologyClassIterator(dictionaryFile.getParentFile(), cleanDictFile);
			return buildDictionary(chebiIter.getOboFile(), dictionaryFile, cleanDictFile, synonymType,
					dictEntryModifier, includeExt);
		} finally {
			if (chebiIter != null) {
				chebiIter.close();
			}
		}
	}

}
