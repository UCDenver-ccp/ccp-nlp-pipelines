package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.File;

import lombok.Data;

import org.joda.time.DateTime;

@Data
public class AnnotationOutput {
	private final File localAnnotationFile;
	private final String runKey;
	private final DateTime runDate;
	private final int annotationCount;
}
