package edu.ucdenver.ccp.nlp.pipelines.runner;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class RunCatalogCollectionReader_neo4j extends RunCatalogCollectionReader {

	public static final String PARAM_CATALOG_DIRECTORY = "catalogDirectory";
	@ConfigurationParameter(mandatory = true, description = "The path to the neo4j run catalog instance.")
	protected File catalogDirectory;

	@Override
	protected RunCatalog initRunCatalog(UimaContext context) {
		return new Neo4jRunCatalog(catalogDirectory);
	}

	public static CollectionReaderDescription createDescription(TypeSystemDescription tsd, File catalogDirectory,
			CharacterEncoding encoding, String pipelineRunKey, String documentCollectionName,
			FileVersion docFileVersion, String language, boolean disableProgress, int num2process, int num2skip,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, String viewName)
			throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(RunCatalogCollectionReader_neo4j.class, tsd,
				PARAM_CATALOG_DIRECTORY, catalogDirectory.getAbsolutePath(), PARAM_ENCODING, encoding.name(),
				PARAM_PIPELINE_RUN_KEY, pipelineRunKey, PARAM_DOCUMENT_COLLECTION_NAME, documentCollectionName,
				PARAM_DOCUMENT_FILE_VERSION, docFileVersion.name(), PARAM_DISABLE_PROGRESS, disableProgress,
				PARAM_LANGUAGE, language, PARAM_NUM2PROCESS, num2process, PARAM_NUM2SKIP, num2skip, PARAM_VIEWNAME,
				viewName, PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass.getName());
	}

}
