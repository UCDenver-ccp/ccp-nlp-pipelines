package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;

public class SamplePostProcessingComponentTest extends DefaultUIMATestCase {

	/**
	 * Initialize a JCas to use in the text by assigning the document text and
	 * adding annotations. This JCas will be consumed by the UIMA component that
	 * is being tested.
	 */
	@Override
	protected void initJCas() throws UIMAException, IOException {
		// ____________________________ 1 _______ 2 _______ 3 _______ 4 _
		// __________________ 0123456789012345678901234567890123456789012
		jcas.setDocumentText("This is some sample text about a chemical.");

		// the chemical annotation covers character offset 33 to 41
		addTextAnnotationToJCas(33, 41, "http://purl.obolibrary.org/obo/CHEBI_1234");
	}

	/**
	 * Test your post-processing component by processing the JCas initialized
	 * above and verifying its contents
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	@Test
	public void testSamplePostProcessingComponent()
			throws ResourceInitializationException, AnalysisEngineProcessException {
		/*
		 * instantiate your post-processing component and create an
		 * AnalysisEngine
		 */
		AnalysisEngineDescription aeDesc = SamplePostProcessingComponent.getDescription("ontology name");
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(aeDesc);

		/* process the JCas that was initialized above */
		engine.process(jcas);

		/*
		 * verify the JCas contents; for example you might want to check that
		 * the expected number of annotations exist
		 */
		int annotCount = JCasUtil.select(jcas, CCPTextAnnotation.class).size();
		int expectedAnnotCount = 1;
		assertEquals(expectedAnnotCount, annotCount);

		/*
		 * you could also iterate through the annotations to check them one by
		 * one if you wanted
		 */
		for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jcas, CCPTextAnnotation.class); annotIter
				.hasNext();) {
			CCPTextAnnotation annot = annotIter.next();
			// do something here to test each individual annotation
		}
	}

}
