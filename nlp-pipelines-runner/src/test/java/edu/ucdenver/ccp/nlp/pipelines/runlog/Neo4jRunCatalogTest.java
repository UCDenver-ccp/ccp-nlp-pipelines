package edu.ucdenver.ccp.nlp.pipelines.runlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog.RunStatus;
import edu.ucdenver.ccp.nlp.pipelines.runner.PmcPipelineBase;

public class Neo4jRunCatalogTest extends DefaultTestCase {

	private static final DocumentCollection DC = new PMC_OA_DocumentCollection();

	private static final Document D1 = new Document("1234567", "PMC1234567", new File("/local/source1.xml"),
			FileType.XML, "BMC Bio.", "BMC Bio. v5 55-66. 2017.");
	private static final Document D2 = new Document("78787878", "PMC78787878", new File("/local/source2.xml"),
			FileType.XML, "BMC Bio.", "BMC Bio. v6 77-88. 2017.");

	private static final AnnotationOutput AO1 = new AnnotationOutput(new File("/local/source1.CM_CL_v0.5.4.ann"),
			"CM_CL_v0.5.4", new DateTime(), 127);
	private static final AnnotationOutput AO2 = new AnnotationOutput(new File("/local/source2.CM_CL_v0.5.4.ann"),
			"CM_CL_v0.5.4", new DateTime(), 33);
	private static final AnnotationOutput AO3 = new AnnotationOutput(new File("/local/source1.CM_HP_v0.5.4.ann"),
			"CM_HP_v0.5.4", new DateTime(), 12);

	private static final AnnotationPipeline AP = new AnnotationPipeline("ConceptMapper + CL",
			"ConceptMapper using the CL ontology as the input dictionary.", PmcPipelineBase.class, "v0.5.4");

	@Test
	public void testAddDocCollection() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			PMC_OA_DocumentCollection dc = new PMC_OA_DocumentCollection();
			dc.addRunKey("CM_CL_v0.5.4");
			dc.addRunKey("CM_HP_v0.5.4");
			catalog.addDocumentCollection(dc);

			Set<DocumentCollection> collections = catalog.getDocumentCollections();
			Set<DocumentCollection> expectedCollections = CollectionsUtil.createSet(dc);
			assertEquals(expectedCollections, collections);
		}
	}

	@Test
	public void testAddRunKeyToDocCollection() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			PMC_OA_DocumentCollection dc = new PMC_OA_DocumentCollection();
			dc.addRunKey("CM_CL_v0.5.4");
			dc.addRunKey("CM_HP_v0.5.4");
			catalog.addDocumentCollection(dc);

			Set<DocumentCollection> collections = catalog.getDocumentCollections();
			Set<DocumentCollection> expectedCollections = CollectionsUtil.createSet(dc);
			assertEquals(expectedCollections, collections);

			String newKey = "NEW RUN";
			catalog.addRunKeyToDocumentCollection(dc.getShortname(), newKey);

			dc.addRunKey(newKey);
			DocumentCollection dcFromCatalog = catalog.getDocumentCollectionByShortName(dc.getShortname());
			assertEquals(dc, dcFromCatalog);

		}
	}

	@Test
	public void testAddDocument() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addDocument(D1, DC);

			Document retrievedDoc = catalog.getDocumentById(ExternalIdentifierType.PUBMED, "1234567");
			assertEquals(D1, retrievedDoc);

			assertNotNull(catalog.getDocumentCollectionByShortName(DC.getShortname()));
		}
	}

	@Test
	public void testAddPipeline() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addAnnotationPipeline(AP);

			Set<AnnotationPipeline> pipelines = catalog.getAnnotationPipelines();
			assertEquals(CollectionsUtil.createSet(AP), pipelines);
		}
	}

	@Test
	public void testAddAnnotationOutput() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addDocument(D1, DC);
			catalog.addAnnotationOutput(D1, AO1);
			catalog.addAnnotationOutput(D1, AO3);

			Document retrievedDoc = catalog.getDocumentById(ExternalIdentifierType.PUBMED, "1234567");
			assertEquals(D1, retrievedDoc);

			Set<File> expectedFiles = CollectionsUtil.createSet(new File("/local/source1.CM_CL_v0.5.4.ann"),
					new File("/local/source1.CM_HP_v0.5.4.ann"));
			Set<File> annotationFiles = catalog.getAnnotationFilesForDocumentId(ExternalIdentifierType.PUBMED,
					"1234567", "CM_CL_v0.5.4");
			assertEquals(expectedFiles, annotationFiles);

		}
	}

	@Test
	public void testGetRunKeysForCollection() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addDocument(D1, DC);
			catalog.addAnnotationOutput(D1, AO1);
			catalog.addAnnotationOutput(D1, AO3);

			catalog.addDocument(D2, DC);
			catalog.addAnnotationOutput(D2, AO2);

			Set<String> expectedRunKeys = CollectionsUtil.createSet("CM_CL_v0.5.4", "CM_HP_v0.5.4");
			List<String> runKeys = catalog.getDocumentCollectionRunKeys(new PMC_OA_DocumentCollection().getShortname());
			assertEquals(expectedRunKeys, new HashSet<String>(runKeys));

		}
	}

	@Test
	public void testGetRunsMap() throws IOException {
		File catalogDirectory = folder.newFolder("catalog");
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory);) {
			catalog.addDocument(D1, DC);
			catalog.addAnnotationOutput(D1, AO1);
			catalog.addAnnotationOutput(D1, AO3);

			catalog.addDocument(D2, DC);
			catalog.addAnnotationOutput(D2, AO2);

			Map<String, Map<RunStatus, Set<Document>>> runsMap = catalog.getRunsMap(new PMC_OA_DocumentCollection());

			Map<String, Map<RunStatus, Set<Document>>> expectedRunsMap = new HashMap<String, Map<RunStatus, Set<Document>>>();
			expectedRunsMap.put("CM_CL_v0.5.4", new HashMap<RunStatus, Set<Document>>());
			expectedRunsMap.put("CM_HP_v0.5.4", new HashMap<RunStatus, Set<Document>>());

			CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.COMPLETE, D1, expectedRunsMap.get("CM_CL_v0.5.4"));
			CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.COMPLETE, D2, expectedRunsMap.get("CM_CL_v0.5.4"));
			CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.COMPLETE, D1, expectedRunsMap.get("CM_HP_v0.5.4"));
			CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.OUTSTANDING, D2, expectedRunsMap.get("CM_HP_v0.5.4"));

			assertEquals(expectedRunsMap, runsMap);
			RunCatalogUtil.getCatalogRunSummary(catalog);
		}
	}

}
