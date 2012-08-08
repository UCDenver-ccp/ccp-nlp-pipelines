package edu.ucdenver.ccp.nlp.uima.pipelines.proteins;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.AnalysisEngineFactory;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.converters.SlotValueToClassMentionPromoter_AE;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.entitynormalization.protein.ProOntologyProteinNormalization_AAE;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.entitynormalization.protein.ProTermDisambiguatorAE;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.filter.ClassMentionRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.shims.annotation.entity.bio.impl.CcpGeneIdAnnotationDecorator;

public class ProteinNormalizationPipelineFactory {

	/**
	 * Returns a list of {@link AnalysisEngineDescription}
	 * 
	 * @param tsd
	 * @param cmdOptions
	 * @return
	 * @throws UIMAException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			ProteinNormalizationPipelineCmdOpts cmdOptions, AnalysisEngineDescription sentenceDetectorDescription)
			throws UIMAException, IOException, IllegalArgumentException {
		System.out.println("Protein detection pipeline begins ...");

		File proOntologyDictionaryDirectory = cmdOptions.getProDictionaryDirectory();
		FileUtil.validateDirectory(proOntologyDictionaryDirectory);

		File proOboFile = cmdOptions.getProOboFile();
		FileUtil.validateFile(proOboFile);

		AnalysisEngineDescription proNormalizer = ProOntologyProteinNormalization_AAE.getAggregateDescription(tsd,
				proOntologyDictionaryDirectory, sentenceDetectorDescription);

		AnalysisEngineDescription proTermDisambiguator = AnalysisEngineFactory.createPrimitiveDescription(
				ProTermDisambiguatorAE.class, tsd, ProTermDisambiguatorAE.PARAM_PRO_OBO_FILE,
				proOboFile.getAbsolutePath());

		AnalysisEngineDescription proTermPromoter = AnalysisEngineFactory.createPrimitiveDescription(
				SlotValueToClassMentionPromoter_AE.class, tsd,
				SlotValueToClassMentionPromoter_AE.PARAM_CLASS_MENTION_NAME_REGEX, "protein",
				SlotValueToClassMentionPromoter_AE.PARAM_SLOT_NAME_TO_PROMOTE,
				CcpGeneIdAnnotationDecorator.PRO_ID_SLOT_NAME,
				SlotValueToClassMentionPromoter_AE.PARAM_TRANSFER_SLOT_VALUES, false);

		AnalysisEngineDescription proteinRemover = AnalysisEngineFactory.createPrimitiveDescription(
				ClassMentionRemovalFilter_AE.class, tsd,
				ClassMentionRemovalFilter_AE.PARAM_CLASS_MENTION_TYPES_TO_REMOVE_LIST, new String[] { "protein" });

		/* @formatter:off */
		return CollectionsUtil.createList(
				proNormalizer,
				proTermDisambiguator,
				proTermPromoter,
				proteinRemover); 
		/* @formatter:on */
	}
}
