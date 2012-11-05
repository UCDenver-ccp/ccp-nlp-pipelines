/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.ucdenver.ccp.common.file.FileUtil.CleanDirectory;
import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.ExplicitSentenceCasInserter;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.LingPipeSentenceDetector_AE;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftEvaluationPipeline;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.PipelineEvaluation;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineCmdOpts;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineCmdOpts.DictionaryParameterOperation;
import edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup.ConceptMapperPipelineFactory;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
@Category(PipelineEvaluation.class)
public class CraftConceptMapperEvaluations extends DefaultTestCase {

	/**
	 * 
	 */
	private static final CraftRelease CRAFT_VERSION = CraftRelease.MAIN;

	// return
	// TypeSystemDescriptionFactory.createTypeSystemDescription("edu.ucdenver.ccp.nlp.core.uima.TypeSystem",
	// "edu.ucdenver.ccp.nlp.wrapper.conceptmapper.TypeSystem",
	// "analysis_engine.primitive.DictTerm",
	// "org.apache.uima.conceptMapper.support.tokenizer.TokenAnnotation",
	// "edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.TypeSystem");

	private static final String SENTENCE_DETECTOR_TYPE_SYSTEM_STR = "edu.ucdenver.ccp.nlp.ext.uima.annotators.sentencedetectors.TypeSystem";

	private ConceptMapperPipelineCmdOpts cmdOptions;

	@Before
	public void setUp() {
		cmdOptions = new ConceptMapperPipelineCmdOpts();
		// File workDirectory = folder.newFolder("cm-dict-dir");
		File workDirectory = new File("/tmp/cm");
		cmdOptions.setDictionaryFile(workDirectory);
		cmdOptions.setSpanClass(ExplicitSentenceCasInserter.SENTENCE_ANNOTATION_CLASS);
	}

	/**
	 * Evaluates the default ConceptMapper Cell Type Ontology pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void evaluateCmClPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.CL);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory
				.getCellTypePipelineAeDescriptions(tsd, cmdOptions, DictionaryParameterOperation.TREAT_AS_DIRECTORY,
						CleanDirectory.YES, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	/**
	 * Evaluates the default ConceptMapper GO-CC pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void evaluateCmGoCcPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.GOCC);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory.getGoCcPipelineAeDescriptions(
				tsd, cmdOptions, DictionaryParameterOperation.TREAT_AS_DIRECTORY, CleanDirectory.YES, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	// /**
	// * Evaluates the default ConceptMapper GO-BP+MF pipeline against the CRAFT corpus
	// *
	// * @throws UIMAException
	// * @throws IOException
	// */
	// @Test
	// public void evaluateCmGoBpMfPipelineAgainstCraft() throws UIMAException, IOException {
	// EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.GO_BP_MF);
	// TypeSystemDescription tsd = createConceptMapperTypeSystem(craftConceptTypes);
	// List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory
	// .getGoBpMfPipelineAeDescriptions(tsd, cmdOptions,
	// DictionaryParameterOperation.TREAT_AS_DIRECTORY,
	// CleanDirectory.YES);
	// runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	// }

	/**
	 * Evaluates the default ConceptMapper CHEBI pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void evaluateCmChebiPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.CHEBI);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory.getChebiPipelineAeDescriptions(
				tsd, cmdOptions, DictionaryParameterOperation.TREAT_AS_DIRECTORY, CleanDirectory.YES, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	/**
	 * Evaluates the default ConceptMapper NCBI Taxonomy pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void evaluateCmNcbiTaxonPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.NCBITAXON);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		cmdOptions.setDictionaryFile(new File("/tmp/cm/cmDict-NCBITAXON.xml"));
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory
				.getNcbiTaxonPipelineAeDescriptions(tsd, cmdOptions, DictionaryParameterOperation.USE,
						CleanDirectory.NO, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	/**
	 * Evaluates the default ConceptMapper Protein Ontology pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void evaluateCmProteinOntologyPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.PR);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory
				.getProteinOntologyPipelineAeDescriptions(tsd, cmdOptions,
						DictionaryParameterOperation.TREAT_AS_DIRECTORY, CleanDirectory.YES, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	/**
	 * Evaluates the default ConceptMapper NCBI Taxonomy pipeline against the CRAFT corpus
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void evaluateCmSoPipelineAgainstCraft() throws UIMAException, IOException {
		EnumSet<CraftConceptType> craftConceptTypes = EnumSet.of(CraftConceptType.SO);
		TypeSystemDescription tsd = createConceptMapperTypeSystem();
		List<AnalysisEngineDescription> cmPipelineDescs = ConceptMapperPipelineFactory
				.getSequenceOntologyPipelineAeDescriptions(tsd, cmdOptions,
						DictionaryParameterOperation.TREAT_AS_DIRECTORY, CleanDirectory.YES, 0);
		runConceptMapperEvaluationAgainstCraft(craftConceptTypes, cmPipelineDescs, tsd);
	}

	/**
	 * @param craftConceptTypes
	 * @param conceptMapperDescriptions
	 * @param tsd
	 * @throws UIMAException
	 * @throws IOException
	 */
	private void runConceptMapperEvaluationAgainstCraft(EnumSet<CraftConceptType> craftConceptTypes,
			List<AnalysisEngineDescription> conceptMapperDescriptions, TypeSystemDescription tsd) throws UIMAException,
			IOException {
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		/*
		 * Collect regular expressions used to identify concepts for the specified
		 * CraftConceptTypes. For example, CHEBI:\\d+ is used to identify terms from the CHEBI
		 * ontology
		 */
		for (CraftConceptType conceptType : craftConceptTypes) {
			annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		}
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, craftConceptTypes, tsd,
				SpanComparatorType.STRICT, MentionComparatorType.IDENTICAL,
				annotationTypeRegexes);

		// File evalResultsFile = folder.newFile("evalResults.out");
		// evalPipeline.setEvalResultsOutputFile(evalResultsFile);

		AnalysisEngineDescription sentenceDetectorDesc = getSentenceDetectorDescription(tsd);
		evalPipeline.addPipelineComponent(sentenceDetectorDesc);
		evalPipeline.addPipelineComponents(conceptMapperDescriptions);
		evalPipeline.run(null);

		// assertTrue(evalResultsFile.exists());
	}

	/**
	 * @param tsd
	 * @return a sentence detector {@link AnalysisEngineDescription}
	 * @throws ResourceInitializationException
	 */
	private static AnalysisEngineDescription getSentenceDetectorDescription(TypeSystemDescription tsd)
			throws ResourceInitializationException {
		return LingPipeSentenceDetector_AE.createAnalysisEngineDescription(tsd, ExplicitSentenceCasInserter.class);
	}

	/**
	 * @param craftConceptTypes
	 * @return the {@link TypeSystemDescription} cased on the input {@link CraftConceptType} set
	 */
	private static TypeSystemDescription createConceptMapperTypeSystem() {
		Collection<String> typeSystemStrs = new ArrayList<String>();
		typeSystemStrs.add(TypeSystemUtil.CCP_TYPE_SYSTEM);
		typeSystemStrs.add(SENTENCE_DETECTOR_TYPE_SYSTEM_STR);
		typeSystemStrs.addAll(ConceptMapperPipelineFactory.CONCEPTMAPPER_TYPE_SYSTEM_STRS);
		TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(typeSystemStrs
				.toArray(new String[typeSystemStrs.size()]));
		return tsd;
	}

}
