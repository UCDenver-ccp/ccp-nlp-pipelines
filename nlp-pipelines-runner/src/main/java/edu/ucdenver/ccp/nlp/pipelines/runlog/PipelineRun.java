package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.util.Calendar;

import lombok.Data;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;

@Data
public class PipelineRun {
	private final String runKey;
	private final String runDescription;
	private final Calendar runDate;
	private final Class<? extends PipelineBase> pipelineClass;
}
