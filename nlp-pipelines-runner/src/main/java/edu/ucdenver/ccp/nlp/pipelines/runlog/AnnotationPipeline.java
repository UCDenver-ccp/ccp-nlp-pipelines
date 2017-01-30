package edu.ucdenver.ccp.nlp.pipelines.runlog;

import lombok.Data;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;

@Data
public class AnnotationPipeline {
	private final String name;
	private final String description;
	private final Class<? extends PipelineBase> pipelineCls;
	private final String version;
}
