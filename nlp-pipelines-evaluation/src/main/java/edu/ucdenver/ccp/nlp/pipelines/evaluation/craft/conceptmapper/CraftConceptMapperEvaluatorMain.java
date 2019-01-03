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
import java.util.EnumSet;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.uima.collections.craft.CraftConceptType;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;

public class CraftConceptMapperEvaluatorMain {
	private static final Logger logger = Logger.getLogger(CraftConceptMapperEvaluatorMain.class);

	@Option(name = "-m", required = true, usage = "ConceptMapper dictionary to use")
	private DictionaryNamespace dictionaryNamespace;

	@Option(name = "-t", required = true, usage = "CRAFT ontology to evaluate against")
	private CraftConceptType craftConceptType;

	@Option(name = "-d", required = true, usage = "dictionary directory to use")
	private File dictionaryDirectory;

	@Option(name = "-i", required = true, usage = "ConceptMapper parameter index to use")
	private int conceptMapperParameterIndex;

	@Option(name = "-o", required = true, usage = "output directory")
	private File outputDirectory;

	@Option(name = "-p", required = true, usage = "results file prefix")
	private String resultsFilePrefix;

	@Option(name = "-c", usage = "clean dictionary files")
	private String cleanDictFilesFlag;

	public void startProcessing(String[] args) {
		long time = System.currentTimeMillis();
		CmdLineParser parser = new CmdLineParser(this);

		/* parse the arguments. */
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			logger.error("Error while parsing PmcOaPipelineMain arguments: " + e.getMessage());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			// print the list of available options
			parser.printUsage(stream);
			logger.error("Usage: " + stream.toString());
			try {
				stream.close();
			} catch (IOException e1) {
				logger.error("Exception closing Usage stream:" + e1.getMessage());
			}
			System.exit(-1);
		}

		if (!outputDirectory.exists()) {
			FileUtil.mkdir(outputDirectory);
		}

		if (!dictionaryDirectory.exists()) {
			FileUtil.mkdir(dictionaryDirectory);
		}
		
		boolean cleanDictFiles = Boolean.parseBoolean(cleanDictFilesFlag);

		File evalResultsFile = new File(outputDirectory, resultsFilePrefix + "." + conceptMapperParameterIndex);

		try {
			AnalysisEngineDescription postProcessingComponentDescription = null;

			/*
			 * By implementing the DictionaryEntryModifier interface you can
			 * hook into the ConceptMapper dictionary construction process and
			 * modify a concept's id, name, and synonyms prior to it being
			 * serialized in the dictionary
			 */
			DictionaryEntryModifier dictEntryModifier = null;
			/*
			 * a sample implementation of the DictionaryEntryModifier interface
			 * is available as shown in the commented line below
			 */
			// DictionaryEntryModifier dictEntryModifier = new
			// SampleDictionaryEntryModifier();

			/*
			 * To add a sample post-processing component to the ConceptMapper
			 * pipeline, uncomment the following line and swap the catch line
			 * below the one that is commented:
			 */
			// postProcessingComponentDescriptions.add(SamplePostProcessingComponent.getDescription("my_ontology_name"));
			CraftConceptMapperEvaluator.evaluateCmPipelineAgainstCraft(dictionaryNamespace,
					EnumSet.of(craftConceptType), dictionaryDirectory, evalResultsFile, conceptMapperParameterIndex,
					cleanDictFiles, postProcessingComponentDescription, dictEntryModifier);
			// } catch (IOException | ResourceInitializationException e) {
			
			logger.info("Evaluation elapsed time: " + ((System.currentTimeMillis()-time)/1000) + "s");
		} catch (IOException e) {
			logger.error("Error during ConceptMapper evaluation over CRAFT!!", e);
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		new CraftConceptMapperEvaluatorMain().startProcessing(args);
	}
}
