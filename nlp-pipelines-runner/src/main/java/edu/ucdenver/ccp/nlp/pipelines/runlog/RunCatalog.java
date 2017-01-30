package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;

public interface RunCatalog {

	public void addDocumentCollection(DocumentCollection dc);

	public void addDocument(Document d, DocumentCollection dc);

	public void addAnnotationPipeline(AnnotationPipeline ap);

	public void addAnnotationOutput(Document d, AnnotationOutput ao);

	/**
	 * @param collection
	 * @param runKey
	 * @return given a run-key and a document collection, this method returns a
	 *         list of files that are awaiting processing for the specified
	 *         run-key
	 */
	public List<File> getFilesToProcess(DocumentCollection collection, String runKey);

	/**
	 * This method returns the work that needs to be done to get the run catalog
	 * up-to-date. It assumes that all run-keys are to be run on all documents
	 * from a given collection.
	 * 
	 * @param collection
	 * @return a mapping from run-key to file lists indicating lists of files
	 *         that are awaiting processing by the specified run-key.
	 */
	public Map<String, Set<Document>> getMissingRunsMap(DocumentCollection collection);

	public Set<File> getAnnotationFilesForDocumentId(ExternalIdentifierType idType, String documentId, String runKey);

	public Document getDocumentById(ExternalIdentifierType idType, String documentId);

	public Set<DocumentCollection> getDocumentCollections();

	Set<AnnotationPipeline> getAnnotationPipelines();

	DocumentCollection getDocumentCollectionByShortName(String shortname);

	/**
	 * @param collection
	 * @return a list of run-keys associated with the specified
	 *         DocumentCollection
	 */
	List<String> getDocumentCollectionRunKeys(String docCollectionShortName);

}
