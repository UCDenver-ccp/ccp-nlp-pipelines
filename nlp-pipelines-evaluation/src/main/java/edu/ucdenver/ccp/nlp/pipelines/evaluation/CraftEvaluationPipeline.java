/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.shims.document.DocumentMetaDataExtractor;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftConceptType;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.CraftPipelineUtil.CraftVersion;

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
	public CraftEvaluationPipeline(CraftVersion craftVersion, EnumSet<CraftConceptType> conceptTypesToLoad,
			TypeSystemDescription tsd, SpanComparatorType spanComparatorType,
			MentionComparatorType mentionComparatorType,
			Class<? extends DocumentMetaDataExtractor> documentMetadataExtractorClass,
			Collection<String> annotationTypeRegexes) throws ResourceInitializationException {
		super(tsd, CraftPipelineUtil.getCraftCollectionReader(craftVersion, tsd, documentMetadataExtractorClass),
				CraftPipelineUtil.getCraftAnnotationLoaderDescriptions(craftVersion, conceptTypesToLoad, tsd,
						documentMetadataExtractorClass), spanComparatorType, mentionComparatorType,
				annotationTypeRegexes);
	}

}
