package edu.ucdenver.ccp.nlp.pipelines.runlog.init;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import edu.ucdenver.ccp.common.file.FileArchiveUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalogUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.init.CatalogLoader_PMC_OA.DocumentMetadata;

public class InitSampleCatalog {

	private static String pmcOaFileListSampleLines = "2017-01-17 15:07:51\n"
			+ "oa_package/08/e0/PMC13900.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13900\tPMID:11250746\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13901.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13901\tPMID:11250747\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13902.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13902\tPMID:11250748\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13903.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13903\tPMID:11250749\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13904.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13904\tPMID:11250750\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13905.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13905\tPMID:11250751\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13906.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13906\tPMID:11250752\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13907.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13907\tPMID:11250753\tNO-CC CODE\n"
			+ "oa_package/08/e0/PMC13908.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13908\tPMID:11250754\tNO-CC CODE\n"
			+ "oa_package/b0/ac/PMC13909.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13909\tPMID:11250755\tNO-CC CODE";

	@Ignore
	@Test
	public void testBuildSampleCatalog() throws IOException {
		File baseDir = new File("/tmp");

		File libraryBaseDir = new File(baseDir, "library_base");
		File catalogDir = new File(baseDir, "catalog_base");
		File pmcBulkBaseDir = new File(baseDir, "pmc_bulk");

		FileUtil.cleanDirectory(libraryBaseDir);
		FileUtil.cleanDirectory(catalogDir);
		FileUtil.cleanDirectory(pmcBulkBaseDir);

		File doc0Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml");
		File doc1Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml");
		File doc2Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13902.nxml");
		File doc3Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13903.nxml");
		File doc4Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13904.nxml");
		File doc5Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13905.nxml");
		File doc6Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13906.nxml");
		File doc7Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13907.nxml");
		File doc8Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13908.nxml");
		File doc9Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13909.nxml");

		FileUtil.mkdir(doc0Nxml.getParentFile());

		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc0Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc1Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc2Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc3Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc4Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc5Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc6Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc7Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc8Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc9Nxml);
		FileArchiveUtil.gzipFile(doc0Nxml);
		FileArchiveUtil.gzipFile(doc1Nxml);
		FileArchiveUtil.gzipFile(doc2Nxml);
		FileArchiveUtil.gzipFile(doc3Nxml);
		FileArchiveUtil.gzipFile(doc4Nxml);
		FileArchiveUtil.gzipFile(doc5Nxml);
		FileArchiveUtil.gzipFile(doc6Nxml);
		FileArchiveUtil.gzipFile(doc7Nxml);
		FileArchiveUtil.gzipFile(doc8Nxml);
		FileArchiveUtil.gzipFile(doc9Nxml);

		File doc0Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml.gz");
		File doc1Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml.gz");
		File doc2Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13902.nxml.gz");
		File doc3Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13903.nxml.gz");
		File doc4Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13904.nxml.gz");
		File doc5Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13905.nxml.gz");
		File doc6Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13906.nxml.gz");
		File doc7Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13907.nxml.gz");
		File doc8Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13908.nxml.gz");
		File doc9Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13909.nxml.gz");

		assertTrue(doc0Nxml_compressed.exists() && doc0Nxml_compressed.length() > 2000);
		assertTrue(doc1Nxml_compressed.exists() && doc1Nxml_compressed.length() > 2000);
		assertTrue(doc2Nxml_compressed.exists() && doc2Nxml_compressed.length() > 2000);
		assertTrue(doc3Nxml_compressed.exists() && doc3Nxml_compressed.length() > 2000);
		assertTrue(doc4Nxml_compressed.exists() && doc4Nxml_compressed.length() > 2000);
		assertTrue(doc5Nxml_compressed.exists() && doc5Nxml_compressed.length() > 2000);
		assertTrue(doc6Nxml_compressed.exists() && doc6Nxml_compressed.length() > 2000);
		assertTrue(doc7Nxml_compressed.exists() && doc7Nxml_compressed.length() > 2000);
		assertTrue(doc8Nxml_compressed.exists() && doc8Nxml_compressed.length() > 2000);
		assertTrue(doc9Nxml_compressed.exists() && doc9Nxml_compressed.length() > 2000);

		InputStream sampleStream = IOUtils.toInputStream(pmcOaFileListSampleLines);
		Map<String, DocumentMetadata> map = CatalogLoader_PMC_OA.parsePmcOaMetadata(sampleStream);

		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDir);) {
			CatalogLoader_PMC_OA.initCatalog(pmcBulkBaseDir, libraryBaseDir, catalog, map);
			RunCatalogUtil.getCatalogRunSummary(catalog);
		}
	}
	
	@Ignore
	@Test
	public void removeRunKey() {
		File baseDir = new File("/tmp");
		File catalogDir = new File(baseDir, "catalog_base");
		
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDir);) {
			catalog.removeRunKeyFromDocumentCollection(new PMC_OA_DocumentCollection().getShortname(), "ANNOT_SERIALIZATION");
//			catalog.removeRunKeyFromDocumentCollection(new PMC_OA_DocumentCollection().getShortname(), "CONCEPTMAPPER_NCBI_TAXON");
		}
	}
	
	@Ignore
	@Test
	public void getCatalogRunSummary() {
		File baseDir = new File("/tmp");
		File catalogDir = new File(baseDir, "catalog_base");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDir);) {
			RunCatalogUtil.getCatalogRunSummary(catalog);
		}
	}

}
