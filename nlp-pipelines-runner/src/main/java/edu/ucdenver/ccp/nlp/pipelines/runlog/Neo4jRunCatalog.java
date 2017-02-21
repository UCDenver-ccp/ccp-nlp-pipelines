package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.Closeable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.string.StringUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;

public class Neo4jRunCatalog implements RunCatalog, Closeable {

	private static final Logger logger = Logger.getLogger(Neo4jRunCatalog.class);

	private GraphDatabaseService graphDb;

	private IndexDefinition documentIndexByPmid;
	private IndexDefinition documentIndexByPmcid;

	private final File catalogDirectory;

	private static enum Relation implements RelationshipType {
		HAS_MEMBER, HAS_ANNOTATIONS, CREATED_BY, HAS_DOCUMENT_IDENTIFIER
	}

	private static enum NodeType {
		DOCUMENT_COLLECTION, DOCUMENT, ANNOTATION_PIPELINE, ANNOTATION_OUTPUT, EXTERNAL_IDENTIFIER
	}

	private static enum DocCollectionNodeProperty {
		SHORTNAME, LONGNAME, DESCRIPTION, RUN_KEYS
	}

	private static enum DocNodeProperty {
		PMID, PMCID, LOCAL_TEXT_FILE, LOCAL_SOURCE_FILE, REMOTE_SOURCE_FILE, SOURCE_FILE_TYPE, SOURCE_FILE_LICENSE, JOURNAL, CITATION, ERROR_MESSAGE, ERROR_STACKTRACE, ERROR_COMPONENT_AT_FAULT, ERROR_PIPELINE_KEY
	}

	private static enum AnnotPipelineNodeProperty {
		NAME, DESCRIPTION, CLASS, VERSION
	}

	private static enum AnnotOutputNodeProperty {
		LOCAL_ANNOTATION_FILE, RUN_KEY, RUN_DATE, ANNOTATION_COUNT
	}

	private static enum ExternalIdentifierNodeProperty {
		SOURCE, ID
	}

	boolean catalogIsUp = false;

	public Neo4jRunCatalog(File catalogDirectory) {
		logger.info("==============================================================================================");
		logger.info("================================== Initializing Run Catalog ==================================");
		logger.info("==============================================================================================");
		this.catalogDirectory = catalogDirectory;
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(catalogDirectory);
		catalogIsUp = true;
		resetNeo4jConnection();
		registerShutdownHook(graphDb);
	}

	private void resetNeo4jConnection() {
		if (!catalogIsUp) {
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(catalogDirectory);
		}
		// if (graphDb != null) {
		// graphDb.shutdown();
		// graphDb = new
		// GraphDatabaseFactory().newEmbeddedDatabase(catalogDirectory);
		// } else {
		// graphDb = new
		// GraphDatabaseFactory().newEmbeddedDatabase(catalogDirectory);
		// }
		// if (graphDb == null || !graphDb.isAvailable(5000)) {
		// graphDb = new
		// GraphDatabaseFactory().newEmbeddedDatabase(catalogDirectory);
		// }
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Override
	public void close() {
		logger.info("Shutting down Neo4j run catalog...");
		graphDb.shutdown();
		catalogIsUp = false;
	}

	public void initializeIndexes() {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			documentIndexByPmid = schema.indexFor(Label.label(NodeType.DOCUMENT.name())).on(DocNodeProperty.PMID.name())
					.create();
			documentIndexByPmcid = schema.indexFor(Label.label(NodeType.DOCUMENT.name()))
					.on(DocNodeProperty.PMCID.name()).create();
			tx.success();
		}

	}

	@Override
	public void addDocumentCollection(DocumentCollection dc) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = graphDb.findNode(Label.label(NodeType.DOCUMENT_COLLECTION.name()),
					DocCollectionNodeProperty.SHORTNAME.name(), dc.getShortname());
			if (dcNode == null) {
				Label label = Label.label(NodeType.DOCUMENT_COLLECTION.name());
				dcNode = graphDb.createNode(label);
				dcNode.setProperty(DocCollectionNodeProperty.SHORTNAME.name(), dc.getShortname());
				dcNode.setProperty(DocCollectionNodeProperty.LONGNAME.name(), dc.getLongname());
				dcNode.setProperty(DocCollectionNodeProperty.DESCRIPTION.name(), dc.getDescription());

				if (dc.getRunKeys() != null && !dc.getRunKeys().isEmpty()) {
					dcNode.setProperty(DocCollectionNodeProperty.RUN_KEYS.name(),
							dc.getRunKeys().toArray(new String[dc.getRunKeys().size()]));
				}

				tx.success();
			}
		}
	}

	private DocumentCollection toDocumentCollection(Node dcNode) {
		if (dcNode == null) {
			return null;
		}

		String shortname = dcNode.getProperty(DocCollectionNodeProperty.SHORTNAME.name()).toString();
		String longname = dcNode.getProperty(DocCollectionNodeProperty.LONGNAME.name()).toString();
		String description = dcNode.getProperty(DocCollectionNodeProperty.DESCRIPTION.name()).toString();
		DocumentCollection dc = new DocumentCollection(shortname, longname, description);

		if (dcNode.hasProperty(DocCollectionNodeProperty.RUN_KEYS.name())) {
			Object runKeysArrayObject = dcNode.getProperty(DocCollectionNodeProperty.RUN_KEYS.name());
			String[] runKeysArray = (String[]) runKeysArrayObject;
			for (String key : runKeysArray) {
				dc.addRunKey(key);
			}
		}

		return dc;
	}

	@Override
	public void addRunKeyToDocumentCollection(String shortname, String newKey) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = getDocumentCollectionNodeByShortName(shortname);
			updateRunKeysForDocCollectionNode(newKey, dcNode);
			tx.success();
		}
	}

	@Override
	public void removeRunKeyFromDocumentCollection(String docCollectionShortName, String keyToRemove) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = getDocumentCollectionNodeByShortName(docCollectionShortName);
			DocumentCollection dc = toDocumentCollection(dcNode);
			Set<String> runKeys = dc.getRunKeys();
			if (runKeys != null && runKeys.contains(keyToRemove)) {
				runKeys.remove(keyToRemove);
				dcNode.removeProperty(DocCollectionNodeProperty.RUN_KEYS.name());
				dcNode.setProperty(DocCollectionNodeProperty.RUN_KEYS.name(),
						runKeys.toArray(new String[runKeys.size()]));
			}
			tx.success();
		}
	}

	/**
	 * Adds the new run key to the specified DocumentCollection node
	 * 
	 * @param newKey
	 * @param dcNode
	 */
	private void updateRunKeysForDocCollectionNode(String newKey, Node dcNode) {
		DocumentCollection dc = toDocumentCollection(dcNode);
		Set<String> runKeys = dc.getRunKeys();
		if (runKeys == null) {
			runKeys = new HashSet<String>();
		}
		runKeys.add(newKey);

		dcNode.setProperty(DocCollectionNodeProperty.RUN_KEYS.name(), runKeys.toArray(new String[runKeys.size()]));
	}

	@Override
	public Set<DocumentCollection> getDocumentCollections() {
		Set<DocumentCollection> dcSet = new HashSet<DocumentCollection>();
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			try (ResourceIterator<Node> dcIter = graphDb.findNodes(Label.label(NodeType.DOCUMENT_COLLECTION.name()))) {
				while (dcIter.hasNext()) {
					Node dcNode = dcIter.next();
					dcSet.add(toDocumentCollection(dcNode));
				}
			}
		}
		return dcSet;
	}

	@Override
	public DocumentCollection getDocumentCollectionByShortName(String shortname) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = getDocumentCollectionNodeByShortName(shortname);
			return toDocumentCollection(dcNode);
		}
	}

	/**
	 * @param dc
	 * @return the document collection node, creates it if it doesn't exist
	 */
	private Node getDocumentCollectionNode(DocumentCollection dc) {
		Node dcNode = graphDb.findNode(Label.label(NodeType.DOCUMENT_COLLECTION.name()),
				DocCollectionNodeProperty.SHORTNAME.name(), dc.getShortname());
		if (dcNode == null) {
			addDocumentCollection(dc);
		}
		dcNode = graphDb.findNode(Label.label(NodeType.DOCUMENT_COLLECTION.name()),
				DocCollectionNodeProperty.SHORTNAME.name(), dc.getShortname());
		return dcNode;
	}

	/**
	 * @param documentCollectionShortName
	 * @return the document collection node with the specified name. Returns
	 *         null if it doesn't exist.
	 */
	private Node getDocumentCollectionNode(String documentCollectionShortName) {
		Node dcNode = graphDb.findNode(Label.label(NodeType.DOCUMENT_COLLECTION.name()),
				DocCollectionNodeProperty.SHORTNAME.name(), documentCollectionShortName);
		return dcNode;
	}

	private Node getDocumentCollectionNodeByShortName(String shortname) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = graphDb.findNode(Label.label(NodeType.DOCUMENT_COLLECTION.name()),
					DocCollectionNodeProperty.SHORTNAME.name(), shortname);
			tx.success();
			return dcNode;
		}
	}

	@Override
	public void addDocument(Document d, DocumentCollection dc) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {

			Node dcNode = getDocumentCollectionNode(dc);

			Label label = Label.label(NodeType.DOCUMENT.name());
			Node dNode = graphDb.createNode(label);

			// connect the document to the collection
			dcNode.createRelationshipTo(dNode, Relation.HAS_MEMBER);

			// for (ExternalIdentifier id : d.getIds()) {
			// Node idNode =
			// graphDb.createNode(Label.label(NodeType.EXTERNAL_IDENTIFIER.name()));
			// idNode.setProperty(ExternalIdentifierNodeProperty.SOURCE.name(),
			// id.getType().name());
			// idNode.setProperty(ExternalIdentifierNodeProperty.ID.name(),
			// id.getId());
			// dNode.createRelationshipTo(idNode,
			// Relationship.HAS_DOCUMENT_IDENTIFIER);
			// }

			if (d.getPmid() != null) {
				dNode.setProperty(DocNodeProperty.PMID.name(), d.getPmid());
			}
			dNode.setProperty(DocNodeProperty.PMCID.name(), d.getPmcid());
			dNode.setProperty(DocNodeProperty.CITATION.name(), d.getCitation());
			dNode.setProperty(DocNodeProperty.JOURNAL.name(), d.getJournal());
			dNode.setProperty(DocNodeProperty.LOCAL_SOURCE_FILE.name(), d.getLocalSourceFile().getAbsolutePath());
			dNode.setProperty(DocNodeProperty.SOURCE_FILE_TYPE.name(), d.getSourceFileType().name());
			if (d.getSourceFileLicense() != null) {
				dNode.setProperty(DocNodeProperty.SOURCE_FILE_LICENSE.name(), d.getSourceFileLicense());
			}
			if (d.getLocalTextFile() != null) {
				dNode.setProperty(DocNodeProperty.LOCAL_TEXT_FILE.name(), d.getLocalTextFile().getAbsolutePath());
			}
			if (d.getRemoteSourceFile() != null) {
				dNode.setProperty(DocNodeProperty.REMOTE_SOURCE_FILE.name(), d.getRemoteSourceFile().toString());
			}
			tx.success();
		}
	}

	@Override
	public void addAnnotationPipeline(AnnotationPipeline ap) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Label label = Label.label(NodeType.ANNOTATION_PIPELINE.name());
			Node apNode = graphDb.createNode(label);

			apNode.setProperty(AnnotPipelineNodeProperty.NAME.name(), ap.getName());
			apNode.setProperty(AnnotPipelineNodeProperty.DESCRIPTION.name(), ap.getDescription());
			apNode.setProperty(AnnotPipelineNodeProperty.CLASS.name(), ap.getPipelineCls().getName());
			apNode.setProperty(AnnotPipelineNodeProperty.VERSION.name(), ap.getVersion());

			tx.success();
		}
	}

	@Override
	public Set<AnnotationPipeline> getAnnotationPipelines() {
		Set<AnnotationPipeline> apSet = new HashSet<AnnotationPipeline>();
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			try (ResourceIterator<Node> apIter = graphDb.findNodes(Label.label(NodeType.ANNOTATION_PIPELINE.name()))) {
				while (apIter.hasNext()) {
					Node apNode = apIter.next();
					AnnotationPipeline ap = toAnnotationPipeline(apNode);
					if (ap != null) {
						apSet.add(ap);
					}
				}
			}
		}
		return apSet;
	}

	private AnnotationPipeline toAnnotationPipeline(Node apNode) {
		if (apNode == null) {
			return null;
		}
		String name = apNode.getProperty(AnnotPipelineNodeProperty.NAME.name()).toString();
		String description = apNode.getProperty(AnnotPipelineNodeProperty.DESCRIPTION.name()).toString();
		Class<? extends PipelineBase> pipelineCls;
		String className = apNode.getProperty(AnnotPipelineNodeProperty.CLASS.name()).toString();
		try {
			pipelineCls = (Class<? extends PipelineBase>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			logger.warn("Class Not Found for pipeline: " + className + ". Pipeline class set to null.");
			pipelineCls = null;
		}
		String version = apNode.getProperty(AnnotPipelineNodeProperty.VERSION.name()).toString();

		return new AnnotationPipeline(name, description, pipelineCls, version);
	}

	@Override
	public void addAnnotationOutput(Document doc, AnnotationOutput ao) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Label label = Label.label(NodeType.ANNOTATION_OUTPUT.name());
			Node aoNode = graphDb.createNode(label);

			aoNode.setProperty(AnnotOutputNodeProperty.RUN_KEY.name(), ao.getRunKey());
			aoNode.setProperty(AnnotOutputNodeProperty.RUN_DATE.name(), DATE_FORMATTER.print(ao.getRunDate()));
			aoNode.setProperty(AnnotOutputNodeProperty.LOCAL_ANNOTATION_FILE.name(),
					ao.getLocalAnnotationFile().getAbsolutePath());
			aoNode.setProperty(AnnotOutputNodeProperty.ANNOTATION_COUNT.name(),
					Integer.toString(ao.getAnnotationCount()));

			Node docNode = getDocumentNodeById(ExternalIdentifierType.PMC, doc.getPmcid());

			docNode.createRelationshipTo(aoNode, Relation.HAS_ANNOTATIONS);

			/*
			 * update any document_collection that this doc is a member of with
			 * the appropriate run-key
			 */
			for (Relationship r : docNode.getRelationships(Relation.HAS_MEMBER)) {
				Node dcNode = r.getOtherNode(docNode);
				updateRunKeysForDocCollectionNode(ao.getRunKey(), dcNode);
			}

			tx.success();
		}
	}

	private AnnotationOutput toAnnotationOutput(Node aoNode) {
		if (aoNode == null) {
			return null;
		}
		String runKey = aoNode.getProperty(AnnotOutputNodeProperty.RUN_KEY.name()).toString();
		DateTime runDate = DATE_FORMATTER
				.parseDateTime(aoNode.getProperty(AnnotOutputNodeProperty.RUN_DATE.name()).toString());
		File localAnnotationFile = new File(
				aoNode.getProperty(AnnotOutputNodeProperty.LOCAL_ANNOTATION_FILE.name()).toString());
		int annotationCount = Integer
				.parseInt(aoNode.getProperty(AnnotOutputNodeProperty.ANNOTATION_COUNT.name()).toString());

		return new AnnotationOutput(localAnnotationFile, runKey, runDate, annotationCount);
	}

	@Override
	public List<File> getFilesToProcess(DocumentCollection collection, String runKey) {
		List<File> filesToProcess = new ArrayList<File>();
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			// Database operations go here
			tx.success();
		}
		throw new UnsupportedOperationException("getFilesToProcess() has not yet been implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog#getRunsMap(edu.ucdenver.
	 * ccp.nlp.pipelines.runlog.DocumentCollection)
	 */
	@Override
	public Map<String, Map<RunStatus, Set<Document>>> getRunsMap(DocumentCollection dc) {
		return getRunsMap(dc.getShortname());
	}

	/*
	 * map from runkey to runstatus (completed, outstanding, or error) to
	 * documents
	 */
	@Override
	public Map<String, Map<RunStatus, Set<Document>>> getRunsMap(String docCollectionShortName) {

		if (getDocumentCollectionNodeByShortName(docCollectionShortName) == null) {
			logger.warn("Cannot return runs map for collection: " + docCollectionShortName
					+ " as there is no collection by that name.");
			return null;
		}

		Map<String, Map<RunStatus, Set<Document>>> map = new HashMap<String, Map<RunStatus, Set<Document>>>();
		Set<String> runKeys = new HashSet<String>(getDocumentCollectionRunKeys(docCollectionShortName));
		for (String key : runKeys) {
			map.put(key, new HashMap<RunStatus, Set<Document>>());
		}
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = getDocumentCollectionNode(docCollectionShortName);
			for (Relationship r : dcNode.getRelationships(Relation.HAS_MEMBER)) {
				Node docNode = r.getOtherNode(dcNode);
				Document d = toDocument(docNode);
				Set<String> docRunKeys = new HashSet<String>();
				for (Relationship hasAnnot : docNode.getRelationships(Relation.HAS_ANNOTATIONS)) {
					Node aoNode = hasAnnot.getOtherNode(docNode);
					String runkey = toAnnotationOutput(aoNode).getRunKey();
					docRunKeys.add(runkey);
				}
				docRunKeys.forEach(key -> CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.COMPLETE, d, map.get(key)));
				Set<String> possibleRunKeys = new HashSet<String>(runKeys);
				possibleRunKeys.removeAll(docRunKeys);
				if (docNode.hasProperty(DocNodeProperty.ERROR_PIPELINE_KEY.name())) {
					String key = docNode.getProperty(DocNodeProperty.ERROR_PIPELINE_KEY.name()).toString();
					CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.ERROR, d, map.get(key));
					possibleRunKeys.remove(key);
				}
				possibleRunKeys
						.forEach(key -> CollectionsUtil.addToOne2ManyUniqueMap(RunStatus.OUTSTANDING, d, map.get(key)));

			}
		}
		return map;
	}

	@Override
	public List<String> getDocumentCollectionRunKeys(String docCollectionShortName) {
		DocumentCollection dc = getDocumentCollectionByShortName(docCollectionShortName);
		if (dc.getRunKeys() != null) {
			return new ArrayList<String>(dc.getRunKeys());
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Document getDocumentById(ExternalIdentifierType idType, String documentId) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node docNode = getDocumentNodeById(idType, documentId);
			return toDocument(docNode);
		}
	}

	private Node getDocumentNodeById(ExternalIdentifierType idType, String documentId) {
		if (documentId.endsWith(".nxml.gz")) {
			documentId = StringUtil.removeSuffix(documentId, ".nxml.gz");
		} else if (documentId.endsWith(".nxml.gz.txt.gz")) {
			documentId = StringUtil.removeSuffix(documentId, ".nxml.gz.txt.gz");
		}
		Node docNode = null;
		switch (idType) {
		case PMC:
			docNode = graphDb.findNode(Label.label(NodeType.DOCUMENT.name()), DocNodeProperty.PMCID.name(), documentId);
			break;
		case PUBMED:
			docNode = graphDb.findNode(Label.label(NodeType.DOCUMENT.name()), DocNodeProperty.PMID.name(), documentId);
			break;
		default:
			throw new UnsupportedOperationException("Document retrieval using identifiers of type " + idType.name()
					+ " is not yet supported. Code revision is necessary.");
		}
		return docNode;
	}

	private Document toDocument(Node docNode) {
		if (docNode == null) {
			return null;
		}

		String pmid = null;
		if (docNode.hasProperty(DocNodeProperty.PMID.name())) {
			pmid = docNode.getProperty(DocNodeProperty.PMID.name()).toString();
		}
		String pmcid = docNode.getProperty(DocNodeProperty.PMCID.name()).toString();
		File localSourceFile = new File(docNode.getProperty(DocNodeProperty.LOCAL_SOURCE_FILE.name()).toString());
		FileType sourceFileType = FileType
				.valueOf(docNode.getProperty(DocNodeProperty.SOURCE_FILE_TYPE.name()).toString());
		String journal = docNode.getProperty(DocNodeProperty.JOURNAL.name()).toString();
		String citation = docNode.getProperty(DocNodeProperty.CITATION.name()).toString();
		Document d = new Document(pmid, pmcid, localSourceFile, sourceFileType, journal, citation);

		if (docNode.hasProperty(DocNodeProperty.LOCAL_TEXT_FILE.name())) {
			File localTextFile = new File(docNode.getProperty(DocNodeProperty.LOCAL_TEXT_FILE.name()).toString());
			d.setLocalTextFile(localTextFile);
		}

		if (docNode.hasProperty(DocNodeProperty.REMOTE_SOURCE_FILE.name())) {
			String urlStr = docNode.getProperty(DocNodeProperty.REMOTE_SOURCE_FILE.name()).toString();
			try {
				URL remoteSourceFile = new URL(urlStr);
				d.setRemoteSourceFile(remoteSourceFile);
			} catch (MalformedURLException e) {
				logger.warn("Ignoring remote URL for document: PMID=" + pmid
						+ ". Unable to retrieve invalid remote source URL from catalog: " + urlStr);
			}
		}

		if (docNode.hasProperty(DocNodeProperty.SOURCE_FILE_LICENSE.name())) {
			String sourceFileLicense = docNode.getProperty(DocNodeProperty.SOURCE_FILE_LICENSE.name()).toString();
			d.setSourceFileLicense(sourceFileLicense);
		}

		return d;
	}

	@Override
	public Set<File> getAnnotationFilesForDocumentId(ExternalIdentifierType idType, String documentId, String runKey) {
		Set<File> annotFiles = new HashSet<File>();
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node docNode = getDocumentNodeById(idType, documentId);
			for (Relationship r : docNode.getRelationships(Relation.HAS_ANNOTATIONS)) {
				Node aoNode = r.getOtherNode(docNode);
				AnnotationOutput ao = toAnnotationOutput(aoNode);
				annotFiles.add(ao.getLocalAnnotationFile());
			}
		}
		return annotFiles;
	}

	@Override
	public void addFileVersionToDocument(Document d, File newFile, FileVersion fileVersion) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node docNode = getDocumentNodeById(ExternalIdentifierType.PMC, d.getPmcid());
			switch (fileVersion) {
			case SOURCE:
				if (docNode.hasProperty(DocNodeProperty.LOCAL_SOURCE_FILE.name())) {
					logger.error("Document (" + d.getPmcid() + ") already has a local source file ("
							+ d.getLocalSourceFile().getAbsolutePath() + "). Cannot assign another: "
							+ newFile.getAbsolutePath());
				} else {
					docNode.setProperty(DocNodeProperty.LOCAL_SOURCE_FILE.name(), newFile.getAbsolutePath());
				}
				break;
			case LOCAL_TEXT:
				if (docNode.hasProperty(DocNodeProperty.LOCAL_TEXT_FILE.name())) {
					logger.error("Document (" + d.getPmcid() + ") already has a local text file ("
							+ d.getLocalTextFile().getAbsolutePath() + "). Cannot assign another: "
							+ newFile.getAbsolutePath());
				} else {
					docNode.setProperty(DocNodeProperty.LOCAL_TEXT_FILE.name(), newFile.getAbsolutePath());
				}
				break;
			default:
				throw new IllegalArgumentException(
						"Unhandled FileVersion: " + fileVersion.name() + ". Code changes required.");
			}
			tx.success();
		}
	}

	@Override
	public int getDocumentCount(DocumentCollection dc) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node dcNode = getDocumentCollectionNodeByShortName(dc.getShortname());
			Set<Document> uniqueDocs = new HashSet<Document>();
			for (Relationship r : dcNode.getRelationships(Relation.HAS_MEMBER)) {
				Node docNode = r.getOtherNode(dcNode);
				uniqueDocs.add(toDocument(docNode));
			}
			return uniqueDocs.size();
		}

	}

	@Override
	public void removeEmptyDocumentCollections() {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Set<Node> emptyDocCollections = new HashSet<Node>();
			for (ResourceIterator<Node> dcNodeIter = graphDb
					.findNodes(Label.label(NodeType.DOCUMENT_COLLECTION.name())); dcNodeIter.hasNext();) {
				Node dcNode = dcNodeIter.next();
				if (!dcNode.getRelationships(Relation.HAS_MEMBER).iterator().hasNext()) {
					emptyDocCollections.add(dcNode);
				}
			}

			for (Node dcNode : emptyDocCollections) {
				logger.warn("Deleting empty document collection: " + toDocumentCollection(dcNode).getShortname());
				dcNode.delete();
			}
			tx.success();
		}
	}

	@Override
	public void logError(String pipelineKey, ExternalIdentifierType idType, String documentId, String componentAtFault,
			String errorMessage, String stackTrace) {
		resetNeo4jConnection();
		try (Transaction tx = graphDb.beginTx()) {
			Node docNode = getDocumentNodeById(idType, documentId);
			docNode.setProperty(DocNodeProperty.ERROR_MESSAGE.name(), errorMessage);
			docNode.setProperty(DocNodeProperty.ERROR_COMPONENT_AT_FAULT.name(), componentAtFault);
			docNode.setProperty(DocNodeProperty.ERROR_PIPELINE_KEY.name(), pipelineKey);
			if (stackTrace != null) {
				docNode.setProperty(DocNodeProperty.ERROR_STACKTRACE.name(), stackTrace);
			}
			tx.success();
		}

	}

}
