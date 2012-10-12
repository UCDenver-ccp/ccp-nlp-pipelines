/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.util.Collection;
import java.util.Set;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.CollectionReaderFactory;

import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.craft.uima.cr.CcpCraftCollectionReader;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class CraftEvaluationPipeline extends EvaluationPipeline {

	/**
	 * @param craftRelease
	 * @param conceptTypesToLoad
	 * @param spanComparatorType
	 * @param mentionComparatorType
	 * @param tsd
	 * @param annotationTypeRegexes
	 * @throws ResourceInitializationException
	 */
	public CraftEvaluationPipeline(CraftRelease craftRelease, Set<CraftConceptType> conceptTypesToLoad,
			TypeSystemDescription tsd, SpanComparatorType spanComparatorType,
			MentionComparatorType mentionComparatorType, Collection<String> annotationTypeRegexes)
			throws ResourceInitializationException {
		super(tsd, getCraftCollectionReader(craftRelease, conceptTypesToLoad), spanComparatorType,
				mentionComparatorType, annotationTypeRegexes);
	}

	/**
	 * @param craftRelease
	 * @param conceptTypesToLoad
	 * @return a {@link CollectionReader} component for the specified {@link CraftRelease}
	 *         initialized to include the {@link CraftConceptType}s noted in the input
	 * @throws ResourceInitializationException
	 */
	private static CollectionReader getCraftCollectionReader(CraftRelease craftRelease,
			Set<CraftConceptType> conceptTypesToLoad) throws ResourceInitializationException {
		CollectionReaderDescription crDesc = CcpCraftCollectionReader.getDescription(craftRelease, conceptTypesToLoad);
		return CollectionReaderFactory.createCollectionReader(crDesc);
	}

}
