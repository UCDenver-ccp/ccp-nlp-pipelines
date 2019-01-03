/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 - 2017 Regents of the University of Colorado
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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucdenver.ccp.common.file.FileArchiveUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.common.string.StringConstants;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil.SynonymType;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.uima.collections.craft.CraftOntology;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;

/**
 * This class builds Concept Mapper dictionaries using the ontologies that were
 * used to annotate the CRAFT corpus. These ontologies are available on the
 * classpath in the jar file that contains the XMI files storing the CRAFT
 * annotations.
 * 
 * @author Colorado Computational Pharmacology, UC Denver;
 *         ccpsupport@ucdenver.edu
 * 
 */
public class CraftOntologiesDictionaryFactory {

	private static final Logger logger = Logger.getLogger(CraftOntologiesDictionaryFactory.class);

	public static final Map<DictionaryNamespace, String> namespacePathMap = new HashMap<DictionaryNamespace, String>() {
		{
			put(DictionaryNamespace.GO, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.GO_CC, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.GO_BP, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.GO_MF, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.FUNK_GO_CC, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.FUNK_GO_BP, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.FUNK_GO_MF, CraftOntology.GO.oboFilePath());
			put(DictionaryNamespace.CHEBI, CraftOntology.CHEBI.oboFilePath());
			put(DictionaryNamespace.CL, CraftOntology.CL.oboFilePath());
			put(DictionaryNamespace.NCBI_TAXON, CraftOntology.NCBI_TAXON.oboFilePath());
			put(DictionaryNamespace.PR, CraftOntology.PR.oboFilePath());
			put(DictionaryNamespace.SO, CraftOntology.SO.oboFilePath());
			put(DictionaryNamespace.UBERON, CraftOntology.UBERON.oboFilePath());
			put(DictionaryNamespace.MOP, CraftOntology.MOP.oboFilePath());
		}
	};

	/**
	 * Creates Concept Mapper dictionary files using archived ontologies (OBO
	 * files) that were used to annotate the CRAFT corpus. The ontology files
	 * are retrieved from the classpath.
	 * 
	 * @param dictionaryNamespace
	 * @param outputDirectory
	 * @param cleanDictionaryFile
	 *            if true then any pre-existing dictionary files are deleted, if
	 *            false then pre-existing dictionaries are used
	 * @param dictId
	 * @param dictEntryModifier
	 * @return a reference to a newly created Concept Mapper dictionary file
	 * @throws IllegalArgumentException
	 *             if {@link DictionaryNamespace#EG} is used as input - this
	 *             dictionary is not supported
	 */
	public static File createDictionaryFile(DictionaryNamespace dictionaryNamespace, File outputDirectory,
			boolean cleanDictionaryFile, SynonymType synonymType, String dictId,
			DictionaryEntryModifier dictEntryModifier) {
		long startTime = System.currentTimeMillis();
		try {
			if (dictionaryNamespace == DictionaryNamespace.EG) {
				throw new UnsupportedOperationException(
						"EntrezGene dictionary creation is not supported from the original EG data "
								+ "used to annotated CRAFT. That data was not archived since the "
								+ "NCBI Gene website was used extensively during annotation.");
			}

			String path = namespacePathMap.get(dictionaryNamespace);
			if (path == null) {
				throw new IllegalArgumentException(
						"Unknown concept mapper dictionary namespace: " + dictionaryNamespace.name());
			}
			File oboFile = copyOboFileFromClasspath(path, outputDirectory, cleanDictionaryFile);
			File dictionaryFile = ConceptMapperDictionaryFileFactory.createDictionaryFileFromOBO(dictionaryNamespace,
					oboFile, outputDirectory, cleanDictionaryFile, synonymType, dictId, dictEntryModifier);
			logger.info("Dictionary creation complete. Elapsed time: "
					+ ((System.currentTimeMillis() - startTime) / 1000) + "s");
			return dictionaryFile;
		} catch (IOException e) {
			throw new RuntimeException("Error while constructing ConceptMapper dictionary.", e);
		}
	}

	/**
	 * Gets the gzipped OBO file from the classpath, copies it to the specified
	 * directory and unzips it. This occurs if cleanDictFile = true, or if the
	 * obo file does not exist locally.
	 * 
	 * @param oboPath
	 * 
	 * @param goOboPath
	 * @param outputDirectory
	 * @param cleanDictFile
	 *            if set to true then we re-download the obo file and re-build
	 *            the dictionary. If false, then the obo file is only downloaded
	 *            if it is not present.
	 * @return a reference to the unzipped OBO file
	 * @throws IOException
	 */
	private static File copyOboFileFromClasspath(String oboPath, File outputDirectory, boolean cleanDictFile)
			throws IOException {
		String resourceName = oboPath.substring(oboPath.lastIndexOf(StringConstants.FORWARD_SLASH) + 1);
		File resourceFile = FileArchiveUtil.getUnzippedFileReference(new File(outputDirectory, resourceName), null);
		if (!resourceFile.exists()) {
			logger.info("Clean dictionary flag set to true OR the OBO file does not exist locally. "
					+ "Downloading the OBO file from the classpath to: " + resourceFile.getAbsolutePath());
			File zippedOboFile = ClassPathUtil.copyClasspathResourceToDirectory(oboPath, outputDirectory);
			resourceFile = FileArchiveUtil.unzip(zippedOboFile, outputDirectory, null);
		}
		return resourceFile;
	}

	public static File getOboFile(DictionaryNamespace ns, File directory) {
		String path = namespacePathMap.get(ns);
		if (path == null) {
			throw new IllegalArgumentException("Unknown concept mapper dictionary namespace: " + ns.name());
		}
		String resourceName = path.substring(path.lastIndexOf(StringConstants.FORWARD_SLASH) + 1);
		return FileArchiveUtil.getUnzippedFileReference(new File(directory, resourceName), null);
	}

}
