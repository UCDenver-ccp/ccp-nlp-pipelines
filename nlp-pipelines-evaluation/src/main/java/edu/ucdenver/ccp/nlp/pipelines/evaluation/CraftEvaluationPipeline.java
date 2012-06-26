/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.util.Collection;
import java.util.Set;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftAnnotationFilterOp;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftConceptType;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftVersion;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class CraftEvaluationPipeline extends EvaluationPipeline {

	/**
	 * @param craftVersion
	 * @param conceptTypesToLoad
	 * @param spanComparatorType
	 * @param mentionComparatorType
	 * @param tsd
	 * @param documentMetadataExtractorClass
	 * @throws ResourceInitializationException
	 */
	public CraftEvaluationPipeline(CraftVersion craftVersion, Set<CraftConceptType> conceptTypesToLoad,
			TypeSystemDescription tsd, SpanComparatorType spanComparatorType,
			MentionComparatorType mentionComparatorType,
			Class<? extends DocumentMetadataHandler> documentMetadataExtractorClass,
			Collection<String> annotationTypeRegexes, CraftAnnotationFilterOp annotFilterOp)
			throws ResourceInitializationException {
		super(tsd, CraftPipelineUtil.getCraftCollectionReader(craftVersion, tsd, documentMetadataExtractorClass),
				CraftPipelineUtil.getCraftAnnotationLoaderDescriptions(craftVersion, conceptTypesToLoad, tsd,
						documentMetadataExtractorClass, annotFilterOp), spanComparatorType, mentionComparatorType,
				annotationTypeRegexes);
	}

}
