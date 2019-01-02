package edu.ucdenver.ccp.nlp.pipelines.runlog.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalogUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.init.CatalogLoader_PMC_OA.DocumentMetadata;

public class CatalogLoader_PMC_AO_Test extends DefaultTestCase {

	private static String pmcOaFileListSampleLines = "2017-01-17 15:07:51\n"
			+ "oa_package/08/e0/PMC13900.tar.gz\tBreast Cancer Res. 2001 Nov 2; 3(1):55-60\tPMC13900\tPMID:11250746\tNO-CC CODE\n"
			+ "oa_package/b0/ac/PMC13901.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13901\tPMID:11250747\tNO-CC CODE\n"
			+ "oa_package/b0/ac/PMC13902.tar.gz\tBreast Cancer Res. 2001 Nov 9; 3(1):61-65\tPMC13902\t\tNO-CC CODE";

	@Test
	public void testParsePmcOaFileListTxt() throws IOException {
		InputStream sampleStream = IOUtils.toInputStream(pmcOaFileListSampleLines);
		Map<String, DocumentMetadata> map = CatalogLoader_PMC_OA.parsePmcOaMetadata(sampleStream);

		assertEquals(3, map.size());

		DocumentMetadata dm1 = new DocumentMetadata("oa_package/08/e0/PMC13900.tar.gz",
				"Breast Cancer Res. 2001 Nov 2; 3(1):55-60", "Breast Cancer Res.", "PMC13900", "11250746",
				"NO-CC CODE");
		DocumentMetadata dm2 = new DocumentMetadata("oa_package/b0/ac/PMC13901.tar.gz",
				"Breast Cancer Res. 2001 Nov 9; 3(1):61-65", "Breast Cancer Res.", "PMC13901", "11250747",
				"NO-CC CODE");

		assertEquals(dm1, map.get("PMC13900"));
		assertEquals(dm2, map.get("PMC13901"));
	}

	@Test
	public void testInitCatalog() throws IOException {
		InputStream sampleStream = IOUtils.toInputStream(pmcOaFileListSampleLines);
		Map<String, DocumentMetadata> map = CatalogLoader_PMC_OA.parsePmcOaMetadata(sampleStream);

		File libraryBaseDir = folder.newFolder("library_base");
		File catalogDir = folder.newFolder("catalog_base");
		File pmcBulkBaseDir = folder.newFolder("pmc_bulk");

		File doc1Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13900.nxml.gz");
		File doc2Nxml = new File(pmcBulkBaseDir, "A-B/Breast_Cancer_Res/PMC13901.nxml.gz");

		FileUtil.mkdir(doc1Nxml.getParentFile());
		assertTrue(doc1Nxml.createNewFile());
		assertTrue(doc2Nxml.createNewFile());

		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDir);) {
			CatalogLoader_PMC_OA.initCatalog(pmcBulkBaseDir, libraryBaseDir, catalog, map);
			RunCatalogUtil.getCatalogRunSummary(catalog);
		}

		File expectedDoc1InLibrary = new File(libraryBaseDir, "oa_package/08/e0/PMC13900.nxml.gz");
		File expectedDoc2InLibrary = new File(libraryBaseDir, "oa_package/b0/ac/PMC13901.nxml.gz");
		assertTrue(expectedDoc1InLibrary.exists());
		assertTrue(expectedDoc2InLibrary.exists());
		

	}

}
