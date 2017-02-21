package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeAnnotator;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;

public class AnnotationSerializerTest {

	@Test
	public void testRoundtripSerialization() {
		TextAnnotation ta = new DefaultTextAnnotation(3, 5, "an", new Annotator(-1, "annotatorName", "", ""), null, -1,
				-1, "12345", -1, new DefaultClassMention("token"));

		String serializedAnnot = AnnotationSerializer.toString(ta, IncludeCoveredText.YES, IncludeAnnotator.YES, null);

		String expectedSerializedAnnot = "12345\tannotatorName\ttoken\tan\t[3..5]";
		assertEquals(expectedSerializedAnnot, serializedAnnot);

		TextAnnotation deserializedTa = AnnotationSerializer.fromString(expectedSerializedAnnot);

		assertEquals(ta, deserializedTa);
	}

}
