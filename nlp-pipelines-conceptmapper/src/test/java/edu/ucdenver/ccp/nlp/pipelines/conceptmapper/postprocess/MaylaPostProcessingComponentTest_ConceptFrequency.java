package edu.ucdenver.ccp.nlp.pipelines.conceptmapper.postprocess;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 - 2017 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.io.File;
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
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.postprocess.MaylaPostProcessingComponent;
import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;

public class MaylaPostProcessingComponentTest_ConceptFrequency extends DefaultUIMATestCase {

	/**
	 * Initialize a JCas to use in the text by assigning the document text and
	 * adding annotations. This JCas will be consumed by the UIMA component that
	 * is being tested.
	 */
	@Override
	protected void initJCas() throws UIMAException, IOException {
		// ____________________________ 1 _______ 2 _______ 3 _______ 4 _
		// __________________ 0123456789012345678901234567890123456789012
		jcas.setDocumentText("As we look at the structure of TRF-10, we see how large it is "
				+ "and want to make sure that Kit (stem cell factor receptor activity) is not on. Next we look at Kit "
				+ "to determine how many times Kit and TRF-10 colocalize. We found that TRF-10 and Kit colocalize 50% of the time.");

		// the chemical annotation covers character offset 33 to 41
		addTextAnnotationToJCas(31, 37, "PR_1"); // TRF-10 - 3 times
		addTextAnnotationToJCas(42, 45, "PR_2"); // see - freq = 1 -> removed
													// annotation
		addTextAnnotationToJCas(89, 92, "GO_1"); // kit - 4 times
		addTextAnnotationToJCas(94, 128, "GO_1"); // stem cell factor receptor
													// activity - textword is
													// the label
		addTextAnnotationToJCas(157, 160, "GO_1"); // kit
		addTextAnnotationToJCas(189, 192, "GO_1"); // kit
		addTextAnnotationToJCas(197, 203, "PR_1"); // TRF-10
		addTextAnnotationToJCas(230, 236, "PR_1"); // TRF-10
		addTextAnnotationToJCas(241, 244, "GO_1"); // kit

	}

	/**
	 * Test your post-processing component by processing the JCas initialized
	 * above and verifying its contents
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 * @throws IOException
	 */
	@Test
	public void testMaylaPostProcessingComponent()
			throws ResourceInitializationException, AnalysisEngineProcessException, IOException {
		/*
		 * instantiate your post-processing component and create an
		 * AnalysisEngine
		 */
		File inputFile = copyClasspathResourceToTemporaryFile(getClass(), "cmDict-Eval_condition1_hplr.xml");
		System.out.println("INPUT FILE: " + inputFile.getAbsolutePath());
		// File inputFile = new
		// File("/tmp/cm-evals/cm-dicts/cmDict-Eval_condition1_hplr.xml");
		Integer conceptFreq = 3;
		// AnalysisEngineDescription aeDesc =
		// MaylaPostProcessingComponent.getDescription(inputFile);
		AnalysisEngineDescription aeDesc = MaylaPostProcessingComponent.getDescription(inputFile, conceptFreq);

		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(aeDesc);

		/* process the JCas that was initialized above */
		engine.process(jcas);
		/*
		 * verify the JCas contents; for example you might want to check that
		 * the expected number of annotations exist
		 */
		int annotCount = JCasUtil.select(jcas, CCPTextAnnotation.class).size();
		int expectedAnnotCount = 8;
		System.out.println("annotation count " + annotCount);
		assertEquals(expectedAnnotCount, annotCount);

		/*
		 * you could also iterate through the annotations to check them one by
		 * one if you wanted
		 */
		for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jcas, CCPTextAnnotation.class); annotIter
				.hasNext();) {
			CCPTextAnnotation annot = annotIter.next();
			// do something here to test each individual annotation
			// assert(annot.getClassMention().getMentionName();
			annot.getClassMention().getMentionName();
			System.out.println("annotation: " + annot.getClassMention().getMentionName() + " -- " + annot.getBegin() + ".." + annot.getEnd());
//			System.out.println("annotation: " + annot);

		}
	}

}
