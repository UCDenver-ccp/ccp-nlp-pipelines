package edu.ucdenver.ccp.nlp.pipelines.evaluation;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 Regents of the University of Colorado
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.pipeline.SimplePipeline;

import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.CraftEvaluationPipeline;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE.SlotRemovalOption;
import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Util;

/**
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class GenericAnnotationTest extends DefaultUIMATestCase {

	AnalysisEngineDescription aed;
	
	private static final String singleAnnotation = "src" + File.separator + "test" + File.separator 
			+ "resources" + File.separator + "singleCLAnnotation";
	private static final String CLannotations = "src" + File.separator + "test" + File.separator 
			+ "resources" + File.separator + "genericCLAnnotations";
	
	private static final CraftRelease CRAFT_VERSION = CraftRelease.MAIN;
	private static final TypeSystemDescription tsd = TypeSystemUtil.getCcpTypeSystem();
	private static final Logger logger = Logger.getLogger(GenericAnnotationTest.class
			.getName());
	
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws UIMAException, IOException {
		super.setUp();
	}
	
	@Override
	protected void initJCas() throws UIMAException, IOException {
		
	}
	
	@Test
	public void testGenericAnnotationLoaderWithoutScore() throws UIMAException, IOException {
		aed = GenericAnnotationLoader.createAnalysisEngineDescription(getTypeSystem(), singleAnnotation);
		
		UIMA_Util.setDocumentID(jcas, "17083276.txt");
		SimplePipeline.runPipeline(jcas, aed);
		
		AnnotationIndex<?> index = jcas.getAnnotationIndex(CCPTextAnnotation.type);
        FSIterator<?> iterator = index.iterator();
        int count = 0;
        while (iterator.hasNext()) {
        	Object o = iterator.next();
        	if (o.getClass().getName().equals("edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation")) {
            	count++;
        	}
        }
        assertTrue("loaded all annotations",count == 384);
	}
	
	@Test
	public void testGenericAnnotationLoaderWithScore() throws UIMAException, IOException {
		aed = GenericAnnotationLoader.createAnalysisEngineDescription(getTypeSystem(), singleAnnotation, 0);
		
		UIMA_Util.setDocumentID(jcas, "17083276.txt");
		SimplePipeline.runPipeline(jcas, aed);
		
		AnnotationIndex<?> index = jcas.getAnnotationIndex(CCPTextAnnotation.type);
        FSIterator<?> iterator = index.iterator();
        int count = 0;
        while (iterator.hasNext()) {
        	Object o = iterator.next();
        	if (o.getClass().getName().equals("edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation")) {
            	count++;
        	}
        }
        assertTrue("loaded all annotations",count == 384);
	}
	
	@Test
	public void testGenericAnnotationLoaderWithScore_2() throws UIMAException, IOException {
		aed = GenericAnnotationLoader.createAnalysisEngineDescription(getTypeSystem(), singleAnnotation, 1000);
		
		UIMA_Util.setDocumentID(jcas, "17083276.txt");
		SimplePipeline.runPipeline(jcas, aed);
		
		AnnotationIndex<?> index = jcas.getAnnotationIndex(CCPTextAnnotation.type);
        FSIterator<?> iterator = index.iterator();
        int count = 0;
        while (iterator.hasNext()) {
        	Object o = iterator.next();
        	if (o.getClass().getName().equals("edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation")) {
            	count++;
        	}
        }
        assertTrue("loaded only annotations with score >= 1000",count == 1);
	}
	
	@Test
	public void testGenericCLAnnotationPipelineWithScore() throws UIMAException, IOException {
		File goldStandardFile = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "CLAnnotationsWithScore.txt");
		
		File tmpResults = testFolder.newFile("tmpResults");
		
		runMetaMapEvaluationAgainstCraftTest(tsd, SpanComparatorType.valueOf("STRICT"), EnumSet.of(CraftConceptType.CL), 
				CLannotations, tmpResults, 1000);
		
		assertTrue("Gold standard and new results file are same exact size", goldStandardFile.length() == tmpResults.length());
	}
	
	
	@Test
	public void testGenericCLAnnotationPipelineWithoutScore() throws UIMAException, IOException {
		File goldStandardFile = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "CLAnnotationsWithoutScore.txt");
		
		File tmpResults = testFolder.newFile("tmpResults");
		
		runMetaMapEvaluationAgainstCraftTest(tsd, SpanComparatorType.valueOf("STRICT"), EnumSet.of(CraftConceptType.CL), 
				CLannotations, tmpResults, -1);
		
		assertTrue("Gold standard and new results file are same exact size", goldStandardFile.length() == tmpResults.length());
	}
	
	private static void runMetaMapEvaluationAgainstCraftTest(TypeSystemDescription tsd, SpanComparatorType 
			spanComparatorType,  EnumSet<CraftConceptType> conceptTypesToLoad,
			String inputDir, File outputFile, int cutoff) throws UIMAException, IOException {
		
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		
		/*
		 * Collect regular expressions used to identify concepts for the specified
		 * CraftConceptTypes. For example, CHEBI:\\d+ is used to identify terms from the CHEBI
		 * ontology
		 */
		for (CraftConceptType conceptType : conceptTypesToLoad) {
			annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		}
		
		// Adding annotations from the directory
		AnalysisEngineDescription genericAnnotations = null;
		if(cutoff == -1) {
			genericAnnotations= GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir);
		} else {
			genericAnnotations = GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir, cutoff);
		}
		
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, conceptTypesToLoad, tsd,
				spanComparatorType, MentionComparatorType.IDENTICAL, annotationTypeRegexes);
		
		evalPipeline.addPipelineComponent(genericAnnotations);
		if (outputFile != null) {
			evalPipeline.setEvalResultsOutputFile(outputFile);
		}
		
		/* Removes all SLOT MENTIONS */
		AnalysisEngineDescription removeSlot = SlotRemovalFilter_AE.getDescription(tsd, SlotRemovalOption.REMOVE_ALL);
		
		evalPipeline.addPipelineComponent(removeSlot);
		evalPipeline.run(SlotRemovalOption.REMOVE_ALL);	
	}
	
}