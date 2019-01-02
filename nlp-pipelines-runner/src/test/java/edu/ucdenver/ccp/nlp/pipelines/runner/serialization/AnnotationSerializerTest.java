package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerImpl.IncludeAnnotator;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerImpl.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerImpl.IncludeSlots;

public class AnnotationSerializerTest {

	@Test
	public void testRoundtripSerialization() {
		String documentText = "This is some document text.";
		TextAnnotation ta = new DefaultTextAnnotation(3, 5, "an", new Annotator(-1, "annotatorName", "", ""), null, -1,
				-1, "12345", -1, new DefaultClassMention("token"));

		List<String> serializedAnnot = new AnnotationSerializerImpl(IncludeCoveredText.YES, IncludeAnnotator.YES,
				IncludeSlots.NO, null).toString(ta, documentText);

		String expectedSerializedAnnot = "{\"documentId\":\"12345\",\"annotatorName\":\"annotatorName\",\"type\":\"token\",\"spans\":[[3,5]],\"coveredText\":\"an\"}";
		assertEquals(expectedSerializedAnnot, serializedAnnot.get(0));

		TextAnnotation deserializedTa = AnnotationSerializerImpl.fromString(expectedSerializedAnnot);

		assertEquals(ta, deserializedTa);
	}

}
