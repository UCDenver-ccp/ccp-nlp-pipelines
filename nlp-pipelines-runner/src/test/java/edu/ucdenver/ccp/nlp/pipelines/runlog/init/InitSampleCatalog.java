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
			+ "oa_package/b0/ac/PMC13901.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13901\tPMID:11250747\tNO-CC CODE";

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

		File doc1Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml");
		File doc2Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml");

		FileUtil.mkdir(doc1Nxml.getParentFile());

		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc1Nxml);
		ClassPathUtil.copyClasspathResourceToFile("/edu/ucdenver/ccp/nlp/pipelines/runner/impl/sample_pmc.xml", doc2Nxml);
		FileArchiveUtil.gzipFile(doc1Nxml);
		FileArchiveUtil.gzipFile(doc2Nxml);

		File doc1Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml.gz");
		File doc2Nxml_compressed = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml.gz");

		assertTrue(doc1Nxml_compressed.exists());
		assertTrue(doc1Nxml_compressed.length() > 2000);
		assertTrue(doc2Nxml_compressed.exists());
		assertTrue(doc2Nxml_compressed.length() > 2000);

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
