package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.util.List;

import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public interface AnnotationSerializer {
	
	public List<String> toString(TextAnnotation ta, String documentText);
	
}
