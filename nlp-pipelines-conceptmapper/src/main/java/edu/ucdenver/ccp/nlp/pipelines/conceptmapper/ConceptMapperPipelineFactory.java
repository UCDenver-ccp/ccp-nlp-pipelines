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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil.SynonymType;
import edu.ucdenver.ccp.nlp.core.mention.ClassMentionType;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperPipelineCmdOpts.DictionaryParameterOperation;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.ClassMentionRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.ConceptMapperPermutationFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.typesystem.ConceptMapper2CCPTypeSystemConverter_AE;

/**
 * @author Center for Computational Pharmacology, UC Denver;
 *         ccpsupport@ucdenver.edu
 * 
 */
public class ConceptMapperPipelineFactory {

	private static final Logger logger = Logger.getLogger(ConceptMapperPipelineFactory.class);

	/**
	 * a collection of relevant type systems represented as strings that are
	 * relevant to running the ConceptMapper
	 */
	public static Collection<String> CONCEPTMAPPER_TYPE_SYSTEM_STRS = CollectionsUtil.createList(
			"edu.ucdenver.ccp.nlp.wrapper.conceptmapper.TypeSystem", "analysis_engine.primitive.DictTerm",
			"org.apache.uima.conceptMapper.support.tokenizer.TokenAnnotation");

	/**
	 * Returns an aggregate: sentence detector, offset tokenizer, conceptmapper
	 * 
	 * @param tsd
	 * @param cmdOptions
	 * @param postProcessingComponents
	 * @return
	 * @throws IOException
	 * @throws UIMAException
	 */
	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, int parameterValuesIndex,
			List<? extends AnalysisEngineDescription> postProcessingComponents) throws UIMAException, IOException {

		File cmDictionaryFile = cmdOptions.getDictionaryFile();
		FileUtil.validateFile(cmDictionaryFile);

		AnalysisEngineDescription conceptMapperAggregateDesc = ConceptMapperPermutationFactory
				.buildConceptMapperAggregatePermutation(parameterValuesIndex, tsd, cmDictionaryFile,
						cmdOptions.getSpanClass());

		/*
		 * Converts from the CM OntologyTerm annotation class to
		 * CCPTextAnnotation classes
		 */
		AnalysisEngineDescription cmToCcpTypeSystemConverterDesc = ConceptMapper2CCPTypeSystemConverter_AE
				.createAnalysisEngineDescription(tsd);

		/*
		 * Removes all token annotations as we don't want them to be output as
		 * RDF
		 */
		AnalysisEngineDescription tokenRemovalDesc = ClassMentionRemovalFilter_AE.createAnalysisEngineDescription(tsd,
				new String[] { ClassMentionType.TOKEN.typeName() });

		List<AnalysisEngineDescription> descList = CollectionsUtil.createList(conceptMapperAggregateDesc,
				cmToCcpTypeSystemConverterDesc, tokenRemovalDesc);

		if (postProcessingComponents != null && !postProcessingComponents.isEmpty()) {
			descList.addAll(postProcessingComponents);
		}
		return descList;
	}

	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp,
			DictionaryNamespace dictNamespace, CleanDirectory workDirectoryOp, int parameterCombinationIndex,
			DictionaryEntryModifier dictEntryModifier,
			List<? extends AnalysisEngineDescription> postProcessingComponents) throws UIMAException, IOException {

		File workDirectory = null;
		if (dictParamOp.equals(DictionaryParameterOperation.IGNORE)) {
			workDirectory = FileUtil.createTemporaryDirectory("cmDictBuildDirectory");
		} else if (dictParamOp.equals(DictionaryParameterOperation.TREAT_AS_DIRECTORY)) {
			workDirectory = cmdOptions.getDictionaryFile();
			FileUtil.validateDirectory(workDirectory);
		}

		/*
		 * If the workDirectory is null at this point, then
		 * cmdOptions.getDictionaryFile() is a reference to the dictionary file
		 * to use, so no need to create a new dictionary file
		 */
		if (workDirectory != null) {
			logger.info("Creating ConceptMapper dictionary file in " + workDirectory.getAbsolutePath());
			SynonymType synonymType = ConceptMapperPermutationFactory.getSynonymType(parameterCombinationIndex);
			File cmDictFile = ConceptMapperDictionaryFileFactory.createDictionaryFile(dictNamespace, workDirectory,
					workDirectoryOp, synonymType, dictEntryModifier);
			logger.info("Concept Mapper dictionary file: " + cmDictFile);
			cmdOptions.setDictionaryFile(cmDictFile);
		}

		return getPipelineAeDescriptions(tsd, cmdOptions, parameterCombinationIndex, postProcessingComponents);
	}

}
