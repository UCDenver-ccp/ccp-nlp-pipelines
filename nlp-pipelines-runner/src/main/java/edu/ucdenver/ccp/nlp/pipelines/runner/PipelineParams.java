package edu.ucdenver.ccp.nlp.pipelines.runner;

import java.io.File;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import lombok.Data;

@Data
public class PipelineParams {
	private final String documentCollectionName;
	private final FileVersion docFileVersion;
	private final CharacterEncoding encoding;
	/**
	 * name of the view that will be populated by the CollectionReader
	 */
	private final String viewName;
	private final PipelineKey pipelineKey;
	private final String description;
	private final File catalogDirectory;
	private final int numToProcess;
	private final int numToSkip;
	
	
}
