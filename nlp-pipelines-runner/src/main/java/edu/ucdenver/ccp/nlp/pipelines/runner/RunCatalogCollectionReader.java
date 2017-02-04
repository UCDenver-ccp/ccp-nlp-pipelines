package edu.ucdenver.ccp.nlp.pipelines.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp.core.document.GenericDocument;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog.RunStatus;
import edu.ucdenver.ccp.nlp.uima.collections.BaseTextCollectionReader;

public abstract class RunCatalogCollectionReader extends BaseTextCollectionReader {

	public static final String PARAM_PIPELINE_RUN_KEY = "pipelineRunKey";
	@ConfigurationParameter(mandatory = true, description = "The pipeline run key. Will be used to determine "
			+ "which documents need to be processed.")
	protected String pipelineRunKey;

	public static final String PARAM_DOCUMENT_COLLECTION_NAME = "documentCollectionShortName";
	@ConfigurationParameter(mandatory = true, description = "The name of the document collection to process.")
	protected String documentCollectionShortName;

	public static final String PARAM_DOCUMENT_FILE_VERSION = "documentFileVersion";
	@ConfigurationParameter(mandatory = true, description = "The version of the document to use, e.g. SOURCE, LOCAL_TEXT.")
	protected FileVersion documentFileVersion;

	private Iterator<File> fileToProcessIter = null;

	private int fileToProcessCount = -1;

	@Override
	protected void initializeImplementation(UimaContext context) throws ResourceInitializationException {
		try (RunCatalog catalog = initRunCatalog(context)) {
			Map<RunStatus, Set<Document>> runMap = catalog.getRunsMap(documentCollectionShortName).get(pipelineRunKey);
			// TODO: probably need handling for case when pipelinerunkey doesn't yet exist -- so add it and process all docs
			if (runMap.containsKey(RunStatus.OUTSTANDING)) {
				Set<Document> docsToProcess = runMap.get(RunStatus.OUTSTANDING);
				List<File> filesToProcess = new ArrayList<File>();
				for (Document d : docsToProcess) {
					switch (documentFileVersion) {
					case SOURCE:
						filesToProcess.add(d.getLocalSourceFile());
						break;
					case LOCAL_TEXT:
						filesToProcess.add(d.getLocalTextFile());
						break;
					default:
						throw new IllegalArgumentException(
								"Unhandled document-file-version: " + documentFileVersion.name());
					}
				}
				fileToProcessCount = filesToProcess.size();
				fileToProcessIter = filesToProcess.iterator();
			}
		}
	}

	protected abstract RunCatalog initRunCatalog(UimaContext context);

	@Override
	protected int countDocumentsInCollection() throws IOException {
		return fileToProcessCount;
	}

	@Override
	protected void skip(int numberToSkip) throws ResourceInitializationException {
		int count = 0;
		while (count < numberToSkip && fileToProcessIter.hasNext()) {
			count++;
			fileToProcessIter.next();
		}

	}

	@Override
	protected boolean hasNextDocument() throws IOException, CollectionException {
		return fileToProcessIter.hasNext();
	}

	@Override
	protected GenericDocument getNextDocument() throws CollectionException, IOException {
		File file = fileToProcessIter.next();
		String documentId = file.getName();
		String text = null;
		if (file.getName().endsWith(".gz")) {
			try (BufferedReader gzreader = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
				text = "";
				for (String line; (line = gzreader.readLine()) != null;) {
					if (text.equals("")) {
						text = line;
					} else {
						text = text + " " + line;
					}
				}
			}
		} else {
			text = FileUtil.copyToString(file, this.encoding);
		}
		GenericDocument gd = new GenericDocument(documentId);
		gd.setDocumentText(text);
		gd.setSourceFile(file);
		return gd;
	}
}
