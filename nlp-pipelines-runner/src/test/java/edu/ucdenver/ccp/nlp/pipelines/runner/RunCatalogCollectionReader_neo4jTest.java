package edu.ucdenver.ccp.nlp.pipelines.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileArchiveUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalogUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.init.CatalogLoader_PMC_OA;
import edu.ucdenver.ccp.nlp.pipelines.runlog.init.CatalogLoader_PMC_OA.DocumentMetadata;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.View;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class RunCatalogCollectionReader_neo4jTest extends DefaultTestCase {

	private static String pmcOaFileListSampleLines = "2017-01-17 15:07:51\n"
			+ "oa_package/08/e0/PMC13900.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13900\tPMID:11250746\tNO-CC CODE\n"
			+ "oa_package/b0/ac/PMC13901.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13901\tPMID:11250747\tNO-CC CODE\n"
			+ "oa_package/b0/ac/PMC13902.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13902\t\tNO-CC CODE";

	@Test
	public void testCollectionReader() throws IOException, CASException, UIMAException {
		InputStream sampleStream = IOUtils.toInputStream(pmcOaFileListSampleLines);
		Map<String, DocumentMetadata> map = CatalogLoader_PMC_OA.parsePmcOaMetadata(sampleStream);

		File libraryBaseDir = folder.newFolder("library_base");
		File catalogDir = folder.newFolder("catalog_base");
		File pmcBulkBaseDir = folder.newFolder("pmc_bulk");

		File doc1Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml");
		File doc2Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml");

		FileUtil.mkdir(doc1Nxml.getParentFile());
		assertTrue(doc1Nxml.createNewFile());
		assertTrue(doc2Nxml.createNewFile());
		FileArchiveUtil.gzipFile(doc1Nxml);
		FileArchiveUtil.gzipFile(doc2Nxml);

		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDir);) {
			CatalogLoader_PMC_OA.initCatalog(pmcBulkBaseDir, libraryBaseDir, catalog, map);
			RunCatalogUtil.getCatalogRunSummary(catalog);
		}

		File expectedDoc1InLibrary = new File(libraryBaseDir, "oa_package/08/e0/PMC13900.nxml.gz");
		File expectedDoc2InLibrary = new File(libraryBaseDir, "oa_package/b0/ac/PMC13901.nxml.gz");
		assertTrue(expectedDoc1InLibrary.exists());
		assertTrue(expectedDoc2InLibrary.exists());

		PipelineKey pipelineRunKey = PipelineKey.XML2TXT;
		String documentCollectionName = new PMC_OA_DocumentCollection().getShortname();
		FileVersion docFileVersion = FileVersion.SOURCE;
		String language = "en";
		boolean disableProgress = false;
		int num2process = -1;
		int num2skip = 0;
		Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass = CcpDocumentMetadataHandler.class;
		String viewName = View.XML.viewName();
		CollectionReaderDescription desc = RunCatalogCollectionReader_neo4j.createDescription(
				TypeSystemUtil.getCcpTypeSystem(), catalogDir, CharacterEncoding.UTF_8, pipelineRunKey.name(),
				documentCollectionName, docFileVersion, language, disableProgress, num2process, num2skip,
				documentMetadataHandlerClass, viewName);

		int casCount = 0;
		for (JCas jcas : new JCasIterable(desc)) {
			// check views' names for XML
			for (Iterator<JCas> viewIter = jcas.getViewIterator(); viewIter.hasNext();) {
				JCas view = viewIter.next();
				if (view.getViewName().equals(View.XML.viewName())) {
					casCount++;
				}
			}

		}

		assertEquals(2, casCount);

	}

}
