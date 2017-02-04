package edu.ucdenver.ccp.nlp.pipelines.runner.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;
import edu.ucdenver.ccp.nlp.uima.test.DefaultUIMATestCase;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Util;
import edu.ucdenver.ccp.nlp.uima.util.View;
import edu.ucdenver.ccp.nlp.uima.util.View_Util;

public class PmcNxml2TxtPipelineTest extends DefaultUIMATestCase {

	private File sourceFolder;

	@Test
	public void testDescriptorFileConstruction() throws Exception {
		// File configDir = folder.newFolder("conf");
		File configDir = new File("/tmp/conf");
		File catalogDirectory = folder.newFolder("catalog");
		PmcNxml2TxtPipeline pipeline = new PmcNxml2TxtPipeline(catalogDirectory, configDir, 1);
	}

	private static final DocumentCollection DC = new PMC_OA_DocumentCollection();

	@Test
	public void testAggregateOnSampleDocument() throws Exception {
		File configDir = folder.newFolder("conf");
		File catalogDirectory = folder.newFolder("catalog");

		/* set up catalog */
		Document D1 = new Document("1234", "PMC1234", new File(sourceFolder, "PMC1234.xml"), FileType.XML, "BMC Bio.",
				"BMC Bio. v5 55-66. 2017.");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addDocument(D1, DC);
		}
		
		PmcNxml2TxtPipeline pipeline = new PmcNxml2TxtPipeline(catalogDirectory, configDir, 1);

		File aggregateAeDescriptorFile = new File(configDir, "PMC_NXML2TXT/PMC_NXML2TXT_engine.xml");
		IOUtils.copy(new FileInputStream(aggregateAeDescriptorFile),
				new FileOutputStream(new File("/tmp/conf/aggregate.xml")));
		AnalysisEngine engine = AnalysisEngineFactory.createEngineFromPath(aggregateAeDescriptorFile.getAbsolutePath());

		engine.process(jcas);
		engine.collectionProcessComplete();

		/*
		 * output should include the "sections" annotation file as well as a
		 * .txt file in the same directory as the source nxml
		 */

		File expectedLocalTextFile = new File(sourceFolder, "PMC1234.txt.gz");
		assertTrue(expectedLocalTextFile.exists());
		
		System.out.println("SOURCE FOLDER CONTENTS: " + Arrays.toString(sourceFolder.list()));
		
		File expectedAnnotFile = new File(sourceFolder, "PMC1234-sections.xmi.gz");
		assertTrue(expectedAnnotFile.exists());

		/*
		 * query catalog to make sure new txt file and annotation file were
		 * logged
		 */
		
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			Document d = catalog.getDocumentById(ExternalIdentifierType.PMC, "PMC1234");
			assertNotNull(d);
			assertEquals(expectedLocalTextFile, d.getLocalTextFile());
			Set<File> annotationFilesForDocumentId = catalog.getAnnotationFilesForDocumentId(ExternalIdentifierType.PMC, "PMC1234", "sections");
			Set<File> expectedAnnotFiles = CollectionsUtil.createSet(expectedAnnotFile);
			assertEquals(expectedAnnotFiles, annotationFilesForDocumentId);
		}
		

	}

	@Override
	protected void initJCas() throws UIMAException, IOException {
		sourceFolder = folder.newFolder("source");
		/* load PMC XML into the XML view */
		JCas xmlView = View_Util.getView(jcas, View.XML.viewName());
		UIMA_Util.setDocumentID(xmlView, "PMC1234");
		UIMA_Util.setSourceDocumentPath(xmlView, new File(sourceFolder, "PMC1234.xml"));
		String samplePmcNxmlDocument = ClassPathUtil.getContentsFromClasspathResource(getClass(), "14607334.xml",
				CharacterEncoding.UTF_8);
		xmlView.setDocumentText(samplePmcNxmlDocument);
	}

}
