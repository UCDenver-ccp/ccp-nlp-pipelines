/**
 * 
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.snp;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.AnalysisEngineFactory;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.entitydetection.snp.SnpIdDetector_AAE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.ClassMentionRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.pipelines.PipelineCmdOptsBase;

/**
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class SnpExtractionPipelineAeFactory {

	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			PipelineCmdOptsBase options) throws UIMAException, IllegalArgumentException {

		AnalysisEngineDescription snpIdDetector = SnpIdDetector_AAE.getAggregateDescription(tsd);

		AnalysisEngineDescription sentenceRemover = AnalysisEngineFactory.createPrimitiveDescription(
				ClassMentionRemovalFilter_AE.class, tsd,
				ClassMentionRemovalFilter_AE.PARAM_CLASS_MENTION_TYPES_TO_REMOVE_LIST, new String[] { "sentence" });

		/* @formatter:off */
		return CollectionsUtil.createList(
				snpIdDetector,
				sentenceRemover); 
		/* @formatter:on */
	}

}