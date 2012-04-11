/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.ExplicitSentenceCasInserter;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.LingPipeSentenceDetector_AE;
import edu.ucdenver.ccp.nlp.ext.uima.shims.document.impl.CcpDocumentMetaDataExtractor;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftEvaluationPipeline;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftConceptType;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftVersion;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.PipelineEvaluation;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineCmdOpts;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineCmdOpts.DictionaryParameterOperation;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineFactory;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
// @Category(PipelineEvaluation.class)
public class CraftConceptMapperEvaluations extends DefaultTestCase {

	/**
	 * 
	 */
	private static final CraftVersion CRAFT_VERSION = CraftVersion.RELEASE;

	// return
	// TypeSystemDescriptionFactory.createTypeSystemDescription("edu.ucdenver.ccp.nlp.core.uima.TypeSystem",
	// "edu.ucdenver.ccp.nlp.wrapper.conceptmapper.TypeSystem",
	// "analysis_engine.primitive.DictTerm",
	// "org.apache.uima.conceptMapper.support.tokenizer.TokenAnnotation",
	// "edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.TypeSystem");

	private static final String SENTENCE_DETECTOR_TYPE_SYSTEM_STR = "edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.TypeSystem";

	/**
	 * @param tsd
	 * @return a sentence detector {@link AnalysisEngineDescription}
	 * @throws ResourceInitializationException
	 */
	private static AnalysisEngineDescription getSentenceDetectorDescription(TypeSystemDescription tsd)
			throws ResourceInitializationException {
		return LingPipeSentenceDetector_AE.createAnalysisEngineDescription(tsd, ExplicitSentenceCasInserter.class);
	}

	@Test
	public void testCmGoCcPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> conceptTypes = EnumSet.of(CraftConceptType.GO_CC);
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		for (CraftConceptType conceptType : conceptTypes) {
			annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		}
		Collection<String> typeSystemStrs = new ArrayList<String>(CraftConceptType.getTypeSystemStrs(conceptTypes));
		typeSystemStrs.add(SENTENCE_DETECTOR_TYPE_SYSTEM_STR);
		typeSystemStrs.addAll(ConceptMapperPipelineFactory.CONCEPTMAPPER_TYPE_SYSTEM_STRS);
		TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(typeSystemStrs
				.toArray(new String[typeSystemStrs.size()]));
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, conceptTypes, tsd,
				SpanComparatorType.STRICT, MentionComparatorType.IDENTICAL, CcpDocumentMetaDataExtractor.class,
				annotationTypeRegexes);
		ConceptMapperPipelineCmdOpts cmdOptions = new ConceptMapperPipelineCmdOpts();
		cmdOptions.setDictionaryFile(folder.newFolder("cm-go-cc-dict-dir"));
		cmdOptions.setSpanClass(ExplicitSentenceCasInserter.SENTENCE_ANNOTATION_CLASS);

		AnalysisEngineDescription sentenceDetectorDesc = getSentenceDetectorDescription(tsd);
		evalPipeline.addPipelineComponent(sentenceDetectorDesc);
		// evalPipeline.addPipelineComponents(ConceptMapperPipelineFactory.getCellTypePipelineAeDescriptions(tsd,
		// cmdOptions, DictionaryParameterOperation.TREAT_AS_DIRECTORY));

		evalPipeline.run();

	}
}
