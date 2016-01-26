package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

/**
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class EntityFinderTest extends DefaultUIMATestCase {

	AnalysisEngineDescription aed;
	
	private static final String inputTextFiles = "src" + File.separator + "test" + File.separator 
									+ "resources" + File.separator + "testInputTextFile";
	private static final String oboFilesDir = "src" + File.separator + "test" + File.separator 
									+ "resources" + File.separator + "oboFiles";
	
	private static final TypeSystemDescription tsd = TypeSystemUtil.getCcpTypeSystem();
	private static final Logger logger = Logger.getLogger(EntityFinderTest.class
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
	public void testEntityFinderWithCRAFTOntology() throws UIMAException, IOException {
		String inputTextFileDir = inputTextFiles;
		String outputDir = testFolder.newFolder("tempOutputDir").getAbsolutePath();
		String ontology = "CL";
		String oboFile = oboFilesDir + File.separator + "cl-basic.obo";
		String outputDictDir = testFolder.newFolder("tempDictDir").getAbsolutePath();
		String cleanDictFile = "true";
		
		String [] args = {inputTextFileDir, outputDir, ontology, oboFile, outputDictDir, cleanDictFile};
		
		EntityFinder.main(args);
		
		File knownAnnotations = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "17608565_CL.a1");
		File outputAnnFile = new File(outputDir).listFiles()[0];
		
		int linesKnown = 0;
		int linesNew = 0;
		BufferedReader readerKnown = new BufferedReader(new FileReader(knownAnnotations));
		BufferedReader readerNew = new BufferedReader(new FileReader(outputAnnFile));
		while(readerKnown.readLine() != null) linesKnown++;
		readerKnown.close();
		while(readerNew.readLine() != null) linesNew++;
		readerNew.close();
		
		assertTrue("Annotation file produced is what is expected", linesKnown == linesNew);
	}
	
	@Test
	public void testEntityFinderWithNonCRAFTOntology() throws UIMAException, IOException {
		String inputTextFileDir = inputTextFiles;
		String outputDir = testFolder.newFolder("tempOutputDir").getAbsolutePath();
		String ontology = "OBO";
		String oboFile = oboFilesDir + File.separator + "doid.obo";
		String outputDictDir = testFolder.newFolder("tempDictDir").getAbsolutePath();
		String cleanDictFile = "true";
		
		String [] args = {inputTextFileDir, outputDir, ontology, oboFile, outputDictDir, cleanDictFile};
		
		EntityFinder.main(args);
		
		File knownAnnotations = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "17608565_DOID.a1");
		File outputAnnFile = new File(outputDir).listFiles()[0];
		
		int linesKnown = 0;
		int linesNew = 0;
		BufferedReader readerKnown = new BufferedReader(new FileReader(knownAnnotations));
		BufferedReader readerNew = new BufferedReader(new FileReader(outputAnnFile));
		while(readerKnown.readLine() != null) linesKnown++;
		readerKnown.close();
		while(readerNew.readLine() != null) linesNew++;
		readerNew.close();
		
		assertTrue("Annotation file produced is what is expected", linesKnown == linesNew);
	}
	
}