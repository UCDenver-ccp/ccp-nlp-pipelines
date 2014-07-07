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
import org.geneontology.oboedit.dataadapter.OBOParseException;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OboUtil.ObsoleteTermHandling;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.CellTypeOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.ChebiOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.GenericOboClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.NcbiTaxonomyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.obo.impl.SequenceOntologyClassIterator;
import edu.ucdenver.ccp.datasource.fileparsers.pro.ProOntologyClassIterator;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.eg.EntrezGeneDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.GoDictionaryFactory.GoNamespace;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.SynonymType;



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
		CL,
		CHEBI,
		NCBI_TAXON,
		PR,
		SO,
		EG,
		OBO
	}

	/**
	 * @param dictNamespace
	 * @param outputDirectory
	 * @param outputDirectoryOp
	 * @return a reference to a newly created Concept Mapper dictionary file
	 */
	public static File createDictionaryFile(DictionaryNamespace dictNamespace, File outputDirectory,
			CleanDirectory outputDirectoryOp, SynonymType synonymType) {
		try {
			boolean cleanOutputDirectory = outputDirectoryOp.equals(CleanDirectory.YES);
			// if we are downloading new source files, then we want to create a new dictionary file
			// in case one already exists, if not then no need to
			boolean cleanDictFile = outputDirectoryOp.equals(CleanDirectory.YES);
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), outputDirectory,
						outputDirectoryOp, synonymType);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), outputDirectory,
						outputDirectoryOp, synonymType);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), outputDirectory,
						outputDirectoryOp, synonymType);
			case CHEBI:
				return buildChebiDictionary(outputDirectory, cleanDictFile, synonymType);
			case CL:
				return buildCellTypeDictionary(outputDirectory, cleanDictFile, synonymType);
			case NCBI_TAXON:
				return buildNcbiTaxonDictionary(outputDirectory, cleanDictFile, synonymType);
			case PR:
				return buildProteinOntologyDictionary(outputDirectory, cleanDictFile, synonymType);
			case SO:
				return buildSequenceOntologyDictionary(outputDirectory, cleanDictFile, synonymType);
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
	public static File createDictionaryFileFromOBO(DictionaryNamespace dictNamespace, File inputFile, File outputDirectory,
			boolean cleanDictFile, SynonymType synonymType, CharacterEncoding charE) {
		try {
			switch (dictNamespace) {
			case GO_CC:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.CC), inputFile,
						outputDirectory, cleanDictFile, synonymType);
			case GO_BP:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.BP), inputFile,
						outputDirectory, cleanDictFile, synonymType);
			case GO_MF:
				return GoDictionaryFactory.buildConceptMapperDictionary(EnumSet.of(GoNamespace.MF), inputFile,
						outputDirectory, cleanDictFile, synonymType);
			case CHEBI:
				return buildChebiDictionary(inputFile, outputDirectory, cleanDictFile, synonymType);
			case CL:
				return buildCellTypeDictionary(inputFile, outputDirectory, cleanDictFile, synonymType);
			case NCBI_TAXON:
				return buildNcbiTaxonDictionary(inputFile, outputDirectory, cleanDictFile, synonymType);
			case PR:
				return buildProteinOntologyDictionary(inputFile, outputDirectory, cleanDictFile, synonymType);
			case SO:
				return buildSequenceOntologyDictionary(inputFile, outputDirectory, cleanDictFile, synonymType);
			case EG:
				return EntrezGeneDictionaryFactory.buildModelOrganismConceptMapperDictionary(inputFile,
						outputDirectory, cleanDictFile);
			case OBO:
				return buildDictionaryFromOBO(inputFile, outputDirectory, cleanDictFile, synonymType, charE);
				
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
	 * @throws OBOParseException
	 */
	private static File buildSequenceOntologyDictionary(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(inputOboFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(soCmDictFile, soIter, null, synonymType);
		return soCmDictFile;
	}

	private static File buildSequenceOntologyDictionary(File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		SequenceOntologyClassIterator soIter = new SequenceOntologyClassIterator(outputDirectory, cleanDictFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(soCmDictFile, soIter, null, synonymType);
		return soCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param prIter
	 * @return
	 * @throws IOException
	 * @throws OBOParseException
	 */
	private static File buildProteinOntologyDictionary(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		ProOntologyClassIterator prIter = new ProOntologyClassIterator(inputOboFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(prCmDictFile, prIter, null, synonymType);
		return prCmDictFile;
	}

	private static File buildProteinOntologyDictionary(File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		ProOntologyClassIterator prIter = new ProOntologyClassIterator(outputDirectory, cleanDictFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(prCmDictFile, prIter, null, synonymType);
		return prCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param taxonIter
	 * @return
	 * @throws IOException
	 * @throws OBOParseException
	 */
	private static File buildNcbiTaxonDictionary(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(inputOboFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(taxonCmDictFile, taxonIter, null, synonymType);
		return taxonCmDictFile;
	}

	private static File buildNcbiTaxonDictionary(File outputDirectory, boolean cleanDictFile, SynonymType synonymType)
			throws IOException, OBOParseException {
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
		NcbiTaxonomyClassIterator taxonIter = new NcbiTaxonomyClassIterator(outputDirectory, cleanDictFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(taxonCmDictFile, taxonIter, null, synonymType);
		return taxonCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param clIter
	 * @return
	 * @throws IOException
	 * @throws OBOParseException
	 */
	private static File buildCellTypeDictionary(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(inputOboFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(clCmDictFile, clIter, null, synonymType);
		return clCmDictFile;
	}

	private static File buildCellTypeDictionary(File outputDirectory, boolean cleanDictFile, SynonymType synonymType)
			throws IOException, OBOParseException {
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
		CellTypeOntologyClassIterator clIter = new CellTypeOntologyClassIterator(outputDirectory, cleanDictFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(clCmDictFile, clIter, null, synonymType);
		return clCmDictFile;
	}

	/**
	 * @param outputDirectory
	 * @param chebiIter
	 * @return
	 * @throws IOException
	 * @throws OBOParseException
	 */
	private static File buildChebiDictionary(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType) throws IOException, OBOParseException {
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
		ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(inputOboFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(chebiCmDictFile, chebiIter, null, synonymType);
		return chebiCmDictFile;
	}

	private static File buildChebiDictionary(File outputDirectory, boolean cleanDictFile, SynonymType synonymType)
			throws IOException, OBOParseException {
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
		ChebiOntologyClassIterator chebiIter = new ChebiOntologyClassIterator(outputDirectory, cleanDictFile, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(chebiCmDictFile, chebiIter, null, synonymType);
		return chebiCmDictFile;
	}
	
	private static File buildDictionaryFromOBO(File inputOboFile, File outputDirectory, boolean cleanDictFile,
			SynonymType synonymType, CharacterEncoding charE) throws IOException, OBOParseException {
		File CmDictFile = new File(outputDirectory, "cmDict-OBO.xml");
		if (CmDictFile.exists()) {
			if (cleanDictFile) {
				FileUtil.deleteFile(CmDictFile);
			} else {
				logger.info("Using pre-existing dictionary file: " + CmDictFile);
				return CmDictFile;
			}
		}
		logger.info("Building dictionary file: " + CmDictFile);
		GenericOboClassIterator genIter = new GenericOboClassIterator(inputOboFile, charE, ObsoleteTermHandling.EXCLUDE_OBSOLETE_TERMS);
		OboToDictionary.buildDictionary(CmDictFile, genIter, null, synonymType);
		return CmDictFile;
	}

}
