package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileReaderUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Annotation_Util;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Util;
import edu.ucdenver.ccp.nlp.uima.util.View;

public class AnnotationSerializerAeTest extends DefaultUIMATestCase {

	private static final String DOCUMENT_TEXT = "This is some document text.";

	@Override
	protected void initJCas() throws UIMAException, IOException {
		UIMA_Util.setDocumentID(jcas, "12345");
		jcas.setDocumentText(DOCUMENT_TEXT);
	}

	@Test
	public void testSerializeAnnotations()
			throws ResourceInitializationException, AnalysisEngineProcessException, IOException {

		/* add annotation to cas */
		CCPTextAnnotation annot = UIMA_Annotation_Util.createCCPTextAnnotation("token", 0, 4, jcas);
		Annotator annotator = new Annotator(-1, "annotator name", "", "");
		UIMA_Annotation_Util.setAnnotator(annot, annotator, jcas);
		annot.addToIndexes();

		File outputDirectory = folder.newFolder("output");
		String outputViewName = View.DEFAULT.viewName();
		String sourceViewName = View.DEFAULT.viewName();
		boolean compressOutput = false;
		String outputFileInfix = "tokens";
		AnalysisEngineDescription aeDesc = AnnotationSerializerAE.getDescription(tsd, CcpDocumentMetadataHandler.class,
				outputDirectory, sourceViewName, outputViewName, compressOutput, outputFileInfix,
				IncludeCoveredText.YES);
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(aeDesc);
		engine.process(jcas);
		engine.destroy();

		File expectedOutputFile = new File(outputDirectory, "12345-tokens.annot");
		assertTrue(expectedOutputFile.exists());

		String expectedLine = "12345\tannotator name\ttoken\tThis\t[0..4]";
		List<String> lines = FileReaderUtil.loadLinesFromFile(expectedOutputFile, CharacterEncoding.UTF_8, null);
		assertEquals(1, lines.size());
		assertEquals(expectedLine, lines.get(0));
	}

	@Test
	public void testDeserializeAnnotations()
			throws IOException, ResourceInitializationException, AnalysisEngineProcessException {
		File directory = folder.newFolder("output");
		File annotationFile = new File(directory, "12345-tokens.annot");
		String expectedLine = "12345\tannotator name\ttoken\tThis\t[0..4]";
		FileWriterUtil.printLines(CollectionsUtil.createList(expectedLine), annotationFile, CharacterEncoding.UTF_8);

		assertTrue(annotationFile.exists());

		AnalysisEngineDescription aeDesc = AnnotationDeserializerAE.getDescription(tsd,
				CcpDocumentMetadataHandler.class, directory, View.DEFAULT.viewName(), "tokens");
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(aeDesc);
		engine.process(jcas);

		Collection<CCPTextAnnotation> annots = JCasUtil.select(jcas, CCPTextAnnotation.class);

		assertEquals(1, annots.size());

		TextAnnotation expectedAnnot = new DefaultTextAnnotation(0, 4);
		expectedAnnot.setCoveredText("This");
		expectedAnnot.setDocumentID("12345");
		expectedAnnot.setDocumentCollectionID(0);
		ClassMention cm = new DefaultClassMention("token");
		expectedAnnot.setClassMention(cm);

		CCPTextAnnotation annot = annots.iterator().next();
		assertEquals(expectedAnnot, new WrappedCCPTextAnnotation(annot));

		engine.destroy();

	}

}
