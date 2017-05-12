package edu.ucdenver.ccp.nlp.pipelines.runner.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.uima.util.FileUtils;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil.SynonymType;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperParams;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperParams.ConceptMapperOptimization;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.dictmod.DictionaryEntryModifierFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.ConceptMapperPermutationFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;

public class UpdateConceptMapperDictionaryFiles {

	private static final Logger logger = Logger.getLogger(UpdateConceptMapperDictionaryFiles.class);

	public enum CleanOntologyFile {
		YES, NO
	}

	public enum CleanDictionaryFile {
		YES, NO
	}

	public enum UseDictEntryModifierIfAvailable {
		YES, NO
	}

	private File dictionaryDirectory;

	public UpdateConceptMapperDictionaryFiles(File dictionaryDirectory) {
		this.dictionaryDirectory = dictionaryDirectory;
		FileUtils.mkdir(dictionaryDirectory);
	}

	public void createDictionaryFiles(CleanDictionaryFile cleanDictionaryFile, ConceptMapperOptimization cmOpt,
			UseDictEntryModifierIfAvailable useDictEntryMod) throws MalformedURLException, IOException {
		/*
		 * ontologies are downloaded by a separate script:
		 * nlp-pipelines/scripts/pipelines/concept-mapper/download-ontologies.sh
		 */
		File ontologyDirectory = new File(dictionaryDirectory, "ontologies");
		FileUtil.validateDirectory(ontologyDirectory);
		for (ConceptMapperParams cmParams : ConceptMapperParams.values()) {
			logger.info("Creating/updating ConceptMapper dictionary file for: " + cmParams.name());
			File ontologyFile = cmParams.getOntologyFile(ontologyDirectory);
			DictionaryEntryModifier dictEntryModifier = null;
			if (useDictEntryMod == UseDictEntryModifierIfAvailable.YES) {
				dictEntryModifier = DictionaryEntryModifierFactory
						.getDictionaryEntryModifier(cmParams.dictionaryNamespace());
			}
			createDictionaryFile(ontologyFile, cmParams, cmOpt, cleanDictionaryFile, dictEntryModifier);
		}
	}

	public File createDictionaryFile(File ontologyFile, ConceptMapperParams cmParams, ConceptMapperOptimization cmOpt,
			CleanDictionaryFile cleanDictionaryFile, DictionaryEntryModifier dictEntryModifier) {
		SynonymType synonymType = ConceptMapperPermutationFactory.getSynonymType(cmParams.optimizedParamIndex(cmOpt));
		File cmDictFile = ConceptMapperDictionaryFileFactory.createDictionaryFileFromOBO(cmParams.dictionaryNamespace(),
				ontologyFile, dictionaryDirectory, cleanDictionaryFile == CleanDictionaryFile.YES, synonymType, null,
				dictEntryModifier);
		return cmDictFile;
	}

	public static File getDictionaryFile(File dictionaryDirectory, ConceptMapperParams cmParams) {
		return ConceptMapperDictionaryFileFactory.getDictionaryFile(dictionaryDirectory, cmParams.dictionaryNamespace(),
				null);
	}

	/**
	 * @param args
	 *            args[0] = dictionary directory <br>
	 *            args[1] = clean dictionary files (YES/NO) <br>
	 *            args[2] = use dictionary entry modifier if available (YES/NO)
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		File dictionaryDirectory = new File(args[0]);
		CleanDictionaryFile cleanDictionaryFile = CleanDictionaryFile.valueOf(args[1]);
		ConceptMapperOptimization cmOpt = ConceptMapperOptimization.valueOf(args[2]);
		UpdateConceptMapperDictionaryFiles updater = new UpdateConceptMapperDictionaryFiles(dictionaryDirectory);
		UseDictEntryModifierIfAvailable useDictMod = UseDictEntryModifierIfAvailable.valueOf(args[3]);
		try {
			updater.createDictionaryFiles(cleanDictionaryFile, cmOpt, useDictMod);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
