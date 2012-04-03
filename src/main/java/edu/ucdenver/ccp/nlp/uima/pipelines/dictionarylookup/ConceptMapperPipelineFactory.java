/**
 * 
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp.core.mention.ClassMentionTypes;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.entitydetection.dictionary.ConceptMapperAaeFactory;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.filter.ClassMentionRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineCmdOpts.DictionaryParameterOperation;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.ConceptMapperFactory.SearchStrategyParamValue;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.ConceptMapperFactory.TokenNormalizerConfigParam.CaseMatchParamValue;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.typesystem.ConceptMapper2CCPTypeSystemConverter_AE;

/**
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class ConceptMapperPipelineFactory {

	/**
	 * Returns an aggregate: sentence detector, offset tokenizer, conceptmapper
	 * 
	 * @param tsd
	 * @param cmdOptions
	 * @return
	 * @throws IOException
	 * @throws UIMAException
	 */
	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions) throws UIMAException, IOException {

		File cmDictionaryFile = cmdOptions.getDictionaryFile();
		FileUtil.validateFile(cmDictionaryFile);

		CaseMatchParamValue caseMatchParamValue = CaseMatchParamValue.CASE_FOLD_DIGITS;
		SearchStrategyParamValue searchStrategyParamValue = SearchStrategyParamValue.CONTIGUOUS_MATCH;
		Class<? extends Annotation> spanFeatureStructureClass = cmdOptions.getSpanClass();
		AnalysisEngineDescription conceptMapperAggregateDesc = ConceptMapperAaeFactory
				.getOffsetTokenizerConceptMapperPipelineDescription(tsd, cmDictionaryFile, caseMatchParamValue,
						searchStrategyParamValue, spanFeatureStructureClass);

		/* Converts from the CM OntologyTerm annotation class to CCPTextAnnotation classes */
		AnalysisEngineDescription cmToCcpTypeSystemConverterDesc = ConceptMapper2CCPTypeSystemConverter_AE
				.createAnalysisEngineDescription(tsd);

		/* Removes all token annotations as we don't want them to be output as RDF */
		AnalysisEngineDescription tokenRemovalDesc = ClassMentionRemovalFilter_AE.createAnalysisEngineDescription(tsd,
				new String[] { ClassMentionTypes.TOKEN });

		/* @formatter:off */
		return CollectionsUtil.createList(
				conceptMapperAggregateDesc,
				cmToCcpTypeSystemConverterDesc,
				tokenRemovalDesc); 
		/* @formatter:on */
	}

	public static List<AnalysisEngineDescription> getEntrezGenePipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.EG);
	}

	public static List<AnalysisEngineDescription> getCellTypePipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.CL);
	}

	public static List<AnalysisEngineDescription> getChebiPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.CHEBI);
	}

	public static List<AnalysisEngineDescription> getSequenceOntologyPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.SO);
	}

	public static List<AnalysisEngineDescription> getProteinOntologyPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.PR);
	}

	public static List<AnalysisEngineDescription> getGoCcPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.GO_CC);
	}

	public static List<AnalysisEngineDescription> getGoBpPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.GO_BP);
	}

	public static List<AnalysisEngineDescription> getGoMfPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp) throws UIMAException,
			IOException {
		return getPipelineAeDescriptions(tsd, cmdOptions, dictParamOp, DictionaryNamespace.GO_MF);
	}

	private static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			ConceptMapperPipelineCmdOpts cmdOptions, DictionaryParameterOperation dictParamOp,
			DictionaryNamespace oboNamespace) throws UIMAException, IOException {

		File workDirectory = null;
		if (dictParamOp.equals(DictionaryParameterOperation.IGNORE)) {
			workDirectory = FileUtil.createTemporaryDirectory("cmDictBuildDirectory");
		} else if (dictParamOp.equals(DictionaryParameterOperation.TREAT_AS_DIRECTORY)) {
			workDirectory = cmdOptions.getDictionaryFile();
			FileUtil.validateDirectory(workDirectory);
		}

		if (workDirectory != null) {
			File cmDictFile = ConceptMapperDictionaryFileFactory.createDictionaryFile(oboNamespace, workDirectory);
			cmdOptions.setDictionaryFile(cmDictFile);
		}

		return getPipelineAeDescriptions(tsd, cmdOptions);
	}

}
