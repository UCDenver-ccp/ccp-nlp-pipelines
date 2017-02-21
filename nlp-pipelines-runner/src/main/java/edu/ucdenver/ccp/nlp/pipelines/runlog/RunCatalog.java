package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;

public interface RunCatalog extends AutoCloseable {

	public static DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTime();

	public enum RunStatus {
		COMPLETE, OUTSTANDING, ERROR
	}

	public void addDocumentCollection(DocumentCollection dc);

	public void addDocument(Document d, DocumentCollection dc);

	public void addFileVersionToDocument(Document d, File newFile, FileVersion fileVersion);

	public void addAnnotationPipeline(AnnotationPipeline ap);

	public void addAnnotationOutput(Document d, AnnotationOutput ao);

	public void close();

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
	public Map<String, Map<RunStatus, Set<Document>>> getRunsMap(DocumentCollection collection);

	public Map<String, Map<RunStatus, Set<Document>>> getRunsMap(String docCollectionShortName);

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

	public void addRunKeyToDocumentCollection(String docCollectionShortName, String runKey);

	public void removeRunKeyFromDocumentCollection(String docCollectionShortName, String runKey);

	public int getDocumentCount(DocumentCollection dc);

	public void removeEmptyDocumentCollections();

	public void logError(String pipelineKey, ExternalIdentifierType idType, String documentId, String componentAtFault,
			String errorMessage, String stackTrace);

}
