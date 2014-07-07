package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.pipeline.SimplePipeline;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationGroup;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.SimpleAnnotationComparator_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.converter.AnnotationSetOverrider_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.converter.AnnotatorOverrider_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE.SlotRemovalOption;

/**
 * Generic class representing a simple NLP component evaluation pipeline.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class EvaluationPipeline {

	/**
	 * Allows specific handling of slot mentions during annotation comparison
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public enum SlotHandling {
		/**
		 * Remove all slot mentions
		 */
		REMOVE_ALL,
		/**
		 * No Operation, i.e. do nothing
		 */
		NOP
	}

	private final SpanComparatorType spanComparatorType;
	private final MentionComparatorType mentionComparatorType;
	private final CollectionReader collectionReader;
	private final TypeSystemDescription tsd;
	private final List<AnalysisEngineDescription> goldStandardAnnotationLoaderDescriptions;
	private final Collection<String> annotationTypeRegexes;

	private List<AnalysisEngineDescription> pipelineToEvaluate;
	private File evalResultsOutputFile = null;

	/**
	 * @param collectionReader
	 * @param spanComparatorType
	 * @param mentionComparatorType
	 * @throws ResourceInitializationException
	 */
	public EvaluationPipeline(TypeSystemDescription tsd, CollectionReader collectionReader,
			SpanComparatorType spanComparatorType, MentionComparatorType mentionComparatorType,
			Collection<String> annotationTypeRegexes) throws ResourceInitializationException {
		this(tsd, collectionReader, new ArrayList<AnalysisEngineDescription>(), spanComparatorType,
				mentionComparatorType, annotationTypeRegexes);
	}

	public EvaluationPipeline(TypeSystemDescription tsd, CollectionReader collectionReader,
			List<AnalysisEngineDescription> goldStandardAnnotationLoaderDescriptions,
			SpanComparatorType spanComparatorType, MentionComparatorType mentionComparatorType,
			Collection<String> annotationTypeRegexes) throws ResourceInitializationException {
		this.collectionReader = collectionReader;
		this.spanComparatorType = spanComparatorType;
		this.mentionComparatorType = mentionComparatorType;
		this.tsd = tsd;
		this.goldStandardAnnotationLoaderDescriptions = goldStandardAnnotationLoaderDescriptions;
		this.annotationTypeRegexes = annotationTypeRegexes;
		pipelineToEvaluate = new ArrayList<AnalysisEngineDescription>();
	}

	public void addPipelineComponent(AnalysisEngineDescription aed) {
		this.pipelineToEvaluate.add(aed);
	}

	public void addPipelineComponents(List<AnalysisEngineDescription> aedList) {
		this.pipelineToEvaluate.addAll(aedList);
	}

	public void setPipelineComponents(List<AnalysisEngineDescription> aedList) {
		this.pipelineToEvaluate = new ArrayList<AnalysisEngineDescription>(aedList);
	}

	public void clearPipelineComponents() {
		this.pipelineToEvaluate = new ArrayList<AnalysisEngineDescription>();
	}

	public void setEvalResultsOutputFile(File file) {
		this.evalResultsOutputFile = file;
	}

	public void disableEvalResultsFileOutput() {
		this.evalResultsOutputFile = null;
	}

	/**
	 * Adds components that apply the proper annotation set IDs and annotator IDs, as well as the
	 * component that will do the actual comparison
	 * 
	 * @param slotRemovalOption
	 *            allows specific handling of slot mentions prior to annotation comparison, valid
	 *            options are {@link SlotRemovalOption#REMOVE_ALL},
	 *            {@link SlotRemovalOption#REMOVE_PRIMITIVE},
	 *            {@link SlotRemovalOption#REMOVE_COMPLEX}, or null to leave the slots as they are.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public void run(SlotRemovalOption slotRemovalOption) throws UIMAException, IOException {
		List<AnalysisEngineDescription> aeDescs = new ArrayList<AnalysisEngineDescription>();
		aeDescs.addAll(goldStandardAnnotationLoaderDescriptions);
		aeDescs.addAll(getGoldStandardAnnotationAssignerAes());
		aeDescs.addAll(pipelineToEvaluate);
		aeDescs.addAll(getAnnotationComparisonComponents(slotRemovalOption));
		SimplePipeline.runPipeline(collectionReader, aeDescs.toArray(new AnalysisEngineDescription[aeDescs.size()]));
	}

	/**
	 * @return
	 * @throws ResourceInitializationException
	 */
	private List<AnalysisEngineDescription> getGoldStandardAnnotationAssignerAes()
			throws ResourceInitializationException {
		/* Assign all annotations to the gold annotator */
		AnalysisEngineDescription goldAnnotatorOverriderDescription = AnnotatorOverrider_AE
				.createGoldAnnotatorAnalysisEngineDescription(tsd);

		/* Assign all annotations to the gold annotation set */
		AnalysisEngineDescription goldAnnotationSetOverriderDescription = AnnotationSetOverrider_AE
				.createGoldSetOverriderDescription(tsd);

		return CollectionsUtil.createList(goldAnnotatorOverriderDescription, goldAnnotationSetOverriderDescription);
	}

	/**
	 * Set up for strict comparison currently.
	 * 
	 * @param slotRemovalOption
	 *            allows specific handling of slot mentions prior to annotation comparison
	 * 
	 * @return
	 * @throws ResourceInitializationException
	 */
	private List<AnalysisEngineDescription> getAnnotationComparisonComponents(SlotRemovalOption slotRemovalOption)
			throws ResourceInitializationException {

		/* Assign all non-gold annotations to the eval annotator */
		AnalysisEngineDescription evalAnnotatorOverriderDescription = AnnotatorOverrider_AE
				.createEvalAnnotatorAnalysisEngineDescription(tsd);

		/* Assign all non-gold annotations to the eval annotation set */
		AnalysisEngineDescription evalAnnotationSetOverriderDescription = AnnotationSetOverrider_AE
				.createEvalSetOverriderDescription(tsd);

		AnnotationGroup goldGroup = new AnnotationGroup(AnnotationSetOverrider_AE.GOLD_ANNOTATION_SET_ID,
				AnnotatorOverrider_AE.GOLD_ANNOTATOR_ID, AnnotationSetOverrider_AE.GOLD_ANNOTATION_SET_ID);
		AnnotationGroup evalGroup = new AnnotationGroup(AnnotationSetOverrider_AE.EVAL_ANNOTATION_SET_ID,
				AnnotatorOverrider_AE.EVAL_ANNOTATOR_ID, AnnotationSetOverrider_AE.EVAL_ANNOTATION_SET_ID);
		for (String annotationTypeRegex : annotationTypeRegexes) {
			goldGroup.addAnnotationTypeRegex(annotationTypeRegex);
			evalGroup.addAnnotationTypeRegex(annotationTypeRegex);
		}

		AnalysisEngineDescription annotationComparatorDescription = SimpleAnnotationComparator_AE
				.createAnalysisEngineDescription(tsd, spanComparatorType, mentionComparatorType, evalResultsOutputFile,
						goldGroup, evalGroup);

		List<AnalysisEngineDescription> aeList = new ArrayList<AnalysisEngineDescription>();
		if (slotRemovalOption != null) {
			aeList.add(SlotRemovalFilter_AE.getDescription(tsd, slotRemovalOption));
		}

		aeList.add(evalAnnotatorOverriderDescription);
		aeList.add(evalAnnotationSetOverriderDescription);
		aeList.add(annotationComparatorDescription);

		return aeList;
	}
}
