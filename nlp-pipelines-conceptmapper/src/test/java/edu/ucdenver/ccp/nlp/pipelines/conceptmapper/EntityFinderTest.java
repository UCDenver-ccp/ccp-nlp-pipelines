package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

import static org.junit.Assert.assertTrue;

import java.io.File;
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
		
		String [] args = {inputTextFileDir, outputDir, ontology, oboFile, outputDictDir};
		
		EntityFinder.main(args);
		
		File knownAnnotations = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "17608565_CL.a1");
		File outputAnnFile = new File(outputDir).listFiles()[0];
		assertTrue("Annotation file produced is what is expected", knownAnnotations.length() == outputAnnFile.length());
	}
	
	@Test
	public void testEntityFinderWithNonCRAFTOntology() throws UIMAException, IOException {
		String inputTextFileDir = inputTextFiles;
		String outputDir = testFolder.newFolder("tempOutputDir").getAbsolutePath();
		String ontology = "CL";
		String oboFile = oboFilesDir + File.separator + "doid.obo";
		String outputDictDir = testFolder.newFolder("tempDictDir").getAbsolutePath();
		
		String [] args = {inputTextFileDir, outputDir, ontology, oboFile, outputDictDir};
		
		EntityFinder.main(args);
		
		File knownAnnotations = new File("src" + File.separator + "test" + File.separator 
				+ "resources" + File.separator + "17608565_DOID.a1");
		File outputAnnFile = new File(outputDir).listFiles()[0];
		assertTrue("Annotation file produced is what is expected", knownAnnotations.length() == outputAnnFile.length());
	}
	
}