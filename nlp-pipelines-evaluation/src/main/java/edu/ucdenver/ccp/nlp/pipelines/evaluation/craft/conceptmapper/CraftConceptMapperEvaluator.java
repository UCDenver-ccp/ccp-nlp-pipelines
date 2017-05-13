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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil.FileSuffixEnforcement;
import edu.ucdenver.ccp.common.file.FileWriterUtil.WriteMode;
import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil.SynonymType;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperPipelineCmdOpts;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperPipelineCmdOpts.DictionaryParameterOperation;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperPipelineFactory;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.CraftEvaluationPipeline;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.converter.OboNamespaceRemover_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE.SlotRemovalOption;
import edu.ucdenver.ccp.nlp.uima.annotators.sentence_detection.ExplicitSentenceCasInserter;
import edu.ucdenver.ccp.nlp.uima.annotators.sentence_detection.OpenNlpSentenceDetectorAE;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.ConceptMapperPermutationFactory;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;

/**
 * Evaluates the Concept Mapper using dictionaries built from the original OBO
 * files used during the annotation of CRAFT. A "default" configuration is used
 * for the Concept Mapper as specified in the nlp-pipelines-core project.
 * 
 * @author Colorado Computational Pharmacology, UC Denver;
 *         ccpsupport@ucdenver.edu
 * 
 */
public class CraftConceptMapperEvaluator {

	/**
	 * Indicates that the CRAFT initial public "release" set of 67 documents
	 * should be used for this evaluation
	 */
	private static final CraftRelease CRAFT_VERSION = CraftRelease.MAIN;

	/**
	 * The type system that contains the SentenceAnnotation class that is
	 * produced by the sentence detector being used
	 * {@link #getSentenceDetectorDescription(TypeSystemDescription)} and
	 * processed by the ConceptMapper
	 */
	private static final String SENTENCE_DETECTOR_TYPE_SYSTEM_STR = "edu.ucdenver.ccp.nlp.uima.annotators.TypeSystem";

	public static void evaluateCmPipelineAgainstCraft(DictionaryNamespace dictNamespace,
			Set<CraftConceptType> craftConceptTypes, File dictionaryDirectory, File evalResultsFile,
			int paramValuesIndex, boolean cleanDictFile,
			List<AnalysisEngineDescription> postProcessingComponentDescriptions,
			DictionaryEntryModifier dictEntryModifier) throws IOException {
		SynonymType synonymType = ConceptMapperPermutationFactory.getSynonymType(paramValuesIndex);
		ConceptMapperPipelineCmdOpts cmdOptions = getCmdOpts(dictNamespace, dictionaryDirectory, cleanDictFile,
				synonymType, Integer.toString(paramValuesIndex), dictEntryModifier);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		try {
			// List<AnalysisEngineDescription> cmPipelineDescs =
			// ConceptMapperPipelineFactory
			// .getCellTypePipelineAeDescriptions(tsd, cmdOptions,
			// DictionaryParameterOperation.USE,
			// CleanDirectory.NO, paramValuesIndex);
			List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory.getPipelineAeDescriptions(
					tsd, cmdOptions, DictionaryParameterOperation.USE, dictNamespace, CleanDirectory.NO,
					paramValuesIndex, dictEntryModifier, postProcessingComponentDescriptions);
			runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd, evalResultsFile);
			appendParameterValuesToEvalResultsFile(paramValuesIndex, evalResultsFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (UIMAException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Appends the Concept Mapper parameter values used to the end of the eval
	 * results file
	 * 
	 * @param paramValuesIndex
	 * @param evalResultsFile
	 * @throws IOException
	 */
	private static void appendParameterValuesToEvalResultsFile(int paramValuesIndex, File evalResultsFile)
			throws IOException {
		List<String> paramLines = CollectionsUtil
				.createList(ConceptMapperPermutationFactory.PARAM_COMBINATIONS.get(paramValuesIndex).toString());
		FileWriterUtil.printLines(paramLines, evalResultsFile, CharacterEncoding.UTF_8, WriteMode.APPEND,
				FileSuffixEnforcement.OFF);
	}

	/**
	 * @param dictNamespace
	 * @param dictionaryDirectory
	 * @param synonymType
	 * @param dictId
	 * @param dictEntryModifier
	 * @return a {@link ConceptMapperPipelineCmdOpts} with the Concept Mapper
	 *         dictionary and span class both specified
	 * 
	 */
	private static ConceptMapperPipelineCmdOpts getCmdOpts(DictionaryNamespace dictNamespace, File dictionaryDirectory,
			boolean cleanDictFile, SynonymType synonymType, String dictId, DictionaryEntryModifier dictEntryModifier)
			throws IOException {
		ConceptMapperPipelineCmdOpts cmdOptions = new ConceptMapperPipelineCmdOpts();
		File cmDictFile = CraftOntologiesDictionaryFactory.createDictionaryFile(dictNamespace, dictionaryDirectory,
				cleanDictFile, synonymType, dictId, dictEntryModifier);
		cmdOptions.setDictionaryFile(cmDictFile);
		cmdOptions.setSpanClass(ExplicitSentenceCasInserter.SENTENCE_ANNOTATION_CLASS);
		return cmdOptions;
	}

	/**
	 * @param craftConceptTypes
	 * @param conceptMapperDescriptions
	 * @param tsd
	 * @param evalResultsFile
	 * @param postProcessingComponents
	 * @throws UIMAException
	 * @throws IOException
	 */
	private static void runConceptMapperEvaluationAgainstCraft(Set<CraftConceptType> craftConceptTypes,
			List<AnalysisEngineDescription> conceptMapperDescriptions, TypeSystemDescription tsd, File evalResultsFile)
			throws UIMAException, IOException {
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		/*
		 * Include all CRAFT types so that all concepts from ontologies with
		 * mixed types, e.g. MOP contains MOP and CHEBI, are evaluated. Collect
		 * regular expressions used to identify concepts for the specified
		 * CraftConceptTypes. For example, CHEBI:\\d+ is used to identify terms
		 * from the CHEBI ontology
		 */
		// for (CraftConceptType conceptType : CraftConceptType.values()) {
		// annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		// }
		// annotationTypeRegexes.add("[A-Za-z]+:\\d+");
		annotationTypeRegexes.add(".*");
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, craftConceptTypes, tsd,
				SpanComparatorType.STRICT, MentionComparatorType.IDENTICAL, annotationTypeRegexes);

		if (evalResultsFile != null) {
			evalPipeline.setEvalResultsOutputFile(evalResultsFile);
		}

		AnalysisEngineDescription sentenceDetectorDesc = getSentenceDetectorDescription(tsd);
		AnalysisEngineDescription namespaceRemoverDesc = getNamespaceRemoverDescription(tsd);

		evalPipeline.addPipelineComponent(sentenceDetectorDesc);
		evalPipeline.addPipelineComponents(conceptMapperDescriptions);
		evalPipeline.addPipelineComponent(namespaceRemoverDesc);
		evalPipeline.run(SlotRemovalOption.REMOVE_ALL);
	}

	/**
	 * @param tsd
	 * @return a sentence detector {@link AnalysisEngineDescription}
	 * @throws ResourceInitializationException
	 */
	private static AnalysisEngineDescription getSentenceDetectorDescription(TypeSystemDescription tsd)
			throws ResourceInitializationException {
		boolean treatLineBreaksAsSentenceBoundaries = true;
		return OpenNlpSentenceDetectorAE.createAnalysisEngineDescription(tsd, ExplicitSentenceCasInserter.class,
				treatLineBreaksAsSentenceBoundaries);
	}

	private static AnalysisEngineDescription getNamespaceRemoverDescription(TypeSystemDescription tsd)
			throws ResourceInitializationException {

		return OboNamespaceRemover_AE.getDescription();
	}

	private static TypeSystemDescription createConceptMapperTypeSystem() {
		Collection<String> typeSystemStrs = new ArrayList<String>();
		typeSystemStrs.add(TypeSystemUtil.CCP_TYPE_SYSTEM);
		typeSystemStrs.add(SENTENCE_DETECTOR_TYPE_SYSTEM_STR);
		typeSystemStrs.addAll(ConceptMapperPipelineFactory.CONCEPTMAPPER_TYPE_SYSTEM_STRS);
		TypeSystemDescription tsd = TypeSystemDescriptionFactory
				.createTypeSystemDescription(typeSystemStrs.toArray(new String[typeSystemStrs.size()]));
		return tsd;
	}

}
