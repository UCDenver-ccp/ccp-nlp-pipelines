package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.entitydetection.dictionary.LinnaeusTagger_AE;

public class LinnaeusPipelineAeFactory {

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
			LinnaeusPipelineCmdOpts cmdOptions) throws UIMAException, IOException, IllegalArgumentException {

		File dictionaryFile = cmdOptions.getDictionaryFile();
		FileUtil.validateFile(dictionaryFile);

		AnalysisEngineDescription linnaeusAeDesc = LinnaeusTagger_AE.getAnalysisEngineDescription(tsd,
				cmdOptions.getDictionaryFile(), CollectionsUtil.createList(""));

		/* @formatter:off */
		return CollectionsUtil.createList(
				linnaeusAeDesc); 
		/* @formatter:on */
	}

	/**
	 * 
	 * @param tsd
	 * @return An {@link AnalysisEngineDescription} initialized for species detection using
	 *         resources that come with the Linnaeus distribution.
	 * @throws UIMAException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static List<AnalysisEngineDescription> getDefaultSpeciesPipelineAeDescriptions(TypeSystemDescription tsd)
			throws UIMAException, IOException, IllegalArgumentException {
		AnalysisEngineDescription linnaeusAeDesc = LinnaeusTagger_AE.getDefaultSpeciesAnalysisEngineDescription(tsd);

		/* @formatter:off */
		return CollectionsUtil.createList(
				linnaeusAeDesc); 
		/* @formatter:on */
	}

}
