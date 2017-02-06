package edu.ucdenver.ccp.nlp.pipelines.runner;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.reflection.ConstructorUtil;
import edu.ucdenver.ccp.nlp.pipelines.log.AnnotationOutputLog;
import edu.ucdenver.ccp.nlp.pipelines.log.SerializedFileLog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.AnnotationOutput;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.ExternalIdentifier.ExternalIdentifierType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog;
import edu.ucdenver.ccp.nlp.uima.shims.ShimDefaults;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

/**
 * Designed to be the end of a pipeline, this AE catalogs new files associated
 * with documents, e.g. new annotation files, text conversions of native XML,
 * etc.
 */
public class RunCatalogAE extends JCasAnnotator_ImplBase {

	public static final Logger logger = Logger.getLogger(RunCatalogAE.class);

	public static final String PARAM_CATALOG_DIRECTORY = "catalogDirectory";
	@ConfigurationParameter(mandatory = true, description = "The base directory for the RunCatalog.")
	private File catalogDirectory;

	public static final String PARAM_DOCUMENT_METADATA_HANDLER_CLASS = "documentMetadataHandlerClassName";
	@ConfigurationParameter(mandatory = false, description = "name of the DocumentMetadataHandler implementation to use", defaultValue = ShimDefaults.CCP_DOCUMENT_METADATA_HANDLER_CLASS_NAME)
	private String documentMetadataHandlerClassName;
	private DocumentMetadataHandler documentMetaDataHandler;

	// private RunCatalog catalog = null;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		documentMetaDataHandler = (DocumentMetadataHandler) ConstructorUtil
				.invokeConstructor(documentMetadataHandlerClassName);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		/*
		 * Initialize the catalog here instead of within the initialize() method
		 * to ensure there is no conflict with the RunCatalogCollectionReader.
		 * The catalog can have only a single open connection, so this AE waits
		 * until the RunCatalogCollectionReader has closed its connection before
		 * attempting to open a connection to the catalog.
		 */
		// if (catalog == null) {
		// catalog = new Neo4jRunCatalog(catalogDirectory);
		// }

		try (RunCatalog catalog = new Neo4jRunCatalog(catalogDirectory)) {
			String documentId = documentMetaDataHandler.extractDocumentId(jCas);
			Document document = catalog.getDocumentById(ExternalIdentifierType.PMC, documentId);

			if (JCasUtil.exists(jCas, SerializedFileLog.class)) {
				for (Iterator<SerializedFileLog> aoIter = JCasUtil.iterator(jCas, SerializedFileLog.class); aoIter
						.hasNext();) {
					SerializedFileLog sfLog = aoIter.next();
					logger.info("SerializedLog is null? " + (sfLog == null));
					logger.info("Adding serialized file to catalog. file=" + sfLog.getSerializedFile()
							+ " fileversion: " + sfLog.getFileVersion());
					catalog.addFileVersionToDocument(document, new File(sfLog.getSerializedFile()),
							FileVersion.valueOf(sfLog.getFileVersion()));
				}
			}

			if (JCasUtil.exists(jCas, AnnotationOutputLog.class)) {
				for (Iterator<AnnotationOutputLog> aoIter = JCasUtil.iterator(jCas, AnnotationOutputLog.class); aoIter
						.hasNext();) {
					AnnotationOutputLog aoLog = aoIter.next();
					AnnotationOutput ao = new AnnotationOutput(new File(aoLog.getLocalAnnotationFile()),
							aoLog.getRunKey(), RunCatalog.DATE_FORMATTER.parseDateTime(aoLog.getRunDate()),
							aoLog.getAnnotationCount());
					catalog.addAnnotationOutput(document, ao);
				}
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		// catalog.close();
	}

	public static AnalysisEngineDescription getDescription(TypeSystemDescription tsd, File catalogDirectory,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(RunCatalogAE.class, tsd, PARAM_CATALOG_DIRECTORY,
				catalogDirectory.getAbsolutePath(), PARAM_DOCUMENT_METADATA_HANDLER_CLASS,
				documentMetadataHandlerClass);
	}

}
