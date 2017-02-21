package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uimafit.util.JCasUtil;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.reflection.ConstructorUtil;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.log.AnnotationOutputLog;
import edu.ucdenver.ccp.nlp.pipelines.log.ProcessingErrorLog;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.shims.ShimDefaults;
import edu.ucdenver.ccp.nlp.uima.util.View_Util;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class AnnotationSerializerAE extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
	@ConfigurationParameter(mandatory = false, description = "")
	private File outputDirectory;

	/**
	 * If the outputDirectory is not specified, then this view will be used to
	 * search for the source-document-path, which will then be used as the
	 * serialization directory for the output of this annotator.
	 */
	public static final String PARAM_SOURCE_VIEW_NAME = "sourceViewName";
	@ConfigurationParameter(mandatory = false, description = "")
	private String sourceViewName;

	/**
	 * Signifies the view whose annotations will be serialized
	 */
	public static final String PARAM_OUTPUT_VIEW_NAME = "outputViewName";
	@ConfigurationParameter(mandatory = false, description = "")
	private String outputViewName;

	/**
	 * If true, the output file will be compressed using gzip
	 */
	public static final String PARAM_COMPRESS_OUTPUT_FLAG = "compressOutput";
	@ConfigurationParameter(mandatory = false, description = "", defaultValue = "true")
	private boolean compressOutput;

	/**
	 * If true, the serialized annotations will include the covered text,
	 * otherwise the column will be empty; default=true
	 */
	public static final String PARAM_INCLUDE_COVERED_TEXT = "includeCoveredText";
	@ConfigurationParameter(mandatory = false, description = "", defaultValue = "true")
	private boolean includeCoveredText;

	public static final String PARAM_OUTPUT_FILENAME_INFIX = "outputFilenameInfix";
	@ConfigurationParameter(mandatory = false, description = "An option string that, if not null, is appended to the output file. "
			+ "This can be useful for identifying the type of annotations containined in an XMI file, for example.")
	private String outputFilenameInfix;

	public static final String PARAM_DOCUMENT_METADATA_HANDLER_CLASS = "documentMetadataHandlerClassName";
	private static final String ANNOT_FILE_SUFFIX = ".annot";
	@ConfigurationParameter(mandatory = false, description = "name of the DocumentMetadataHandler implementation to use", defaultValue = ShimDefaults.CCP_DOCUMENT_METADATA_HANDLER_CLASS_NAME)
	private String documentMetadataHandlerClassName;
	private DocumentMetadataHandler documentMetaDataHandler;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		documentMetaDataHandler = (DocumentMetadataHandler) ConstructorUtil
				.invokeConstructor(documentMetadataHandlerClassName);
		logger = aContext.getLogger();
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		/* If an error has been reported, then do not process this CAS. */
		if (JCasUtil.select(jCas, ProcessingErrorLog.class).isEmpty()) {
			String documentId = documentMetaDataHandler.extractDocumentId(jCas);
			File outputFile = getOutputFile(jCas, documentId);
			JCas view = null;
			try {
				view = View_Util.getView(jCas, sourceViewName);
			} catch (CASException e) {
				throw new AnalysisEngineProcessException(e);
			}
			serializeAnnotations(view, outputFile);
			logSerializedFile(jCas, outputFile);
		}
	}

	public static DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTime();
	private Logger logger;

	private void logSerializedFile(JCas jCas, File outputFile) {
		AnnotationOutputLog aoLog = new AnnotationOutputLog(jCas);
		aoLog.setAnnotationCount(jCas.getAnnotationIndex().size());
		aoLog.setLocalAnnotationFile(outputFile.getAbsolutePath());
		aoLog.setRunDate(DATE_FORMATTER.print(new DateTime()));
		aoLog.setRunKey(outputFilenameInfix);
		aoLog.addToIndexes();
	}

	/**
	 * Serializes the document text to file and populates the
	 * source-document-path metadata field with the location of the serialized
	 * file
	 * 
	 * @param jCas
	 * @param documentText
	 * @param outputFile
	 * @throws AnalysisEngineProcessException
	 */
	private void serializeAnnotations(JCas jCas, File outputFile) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Serializing annotations to file: " + outputFile.getAbsolutePath());

		logger.log(Level.INFO, "Annot count: " + JCasUtil.select(jCas, CCPTextAnnotation.class).size());

		try (BufferedWriter writer = (compressOutput)
				? new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile))))
				: FileWriterUtil.initBufferedWriter(outputFile)) {

			for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jCas, CCPTextAnnotation.class); annotIter
					.hasNext();) {
				CCPTextAnnotation annot = annotIter.next();
				WrappedCCPTextAnnotation ta = new WrappedCCPTextAnnotation(annot);
				String storageString = AnnotationSerializer.toString(ta,
						(includeCoveredText ? IncludeCoveredText.YES : IncludeCoveredText.NO));
				writer.write(storageString + "\n");
			}

		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Given a document Id, this method returns the name of the corresponding
	 * XMI file that will be created
	 * 
	 * @param documentId
	 * 
	 * @return
	 */
	public String getOutputFileName(String documentId) {
		return getOutputFileName(documentId, outputFilenameInfix, compressOutput);
	}

	/**
	 * @param documentId
	 * @param infix
	 *            optional text string to insert as part of the filename
	 * @return
	 */
	public static String getOutputFileName(String documentId, String infix, boolean compressOutput) {
		if (infix == null) {
			return documentId + ANNOT_FILE_SUFFIX + ((compressOutput) ? ".gz" : "");
		} else {
			return documentId + "-" + infix + ANNOT_FILE_SUFFIX + ((compressOutput) ? ".gz" : "");
		}
	}

	/**
	 * @param jCas
	 * @param documentId
	 * @return a reference to the output file where the document text will be
	 *         saved
	 * @throws AnalysisEngineProcessException
	 */
	private File getOutputFile(JCas jCas, String documentId) throws AnalysisEngineProcessException {
		String outputFilename = getOutputFileName(documentId);
		File outputFile = null;
		if (outputDirectory != null) {
			outputFile = new File(outputDirectory, outputFilename);
		} else {
			/*
			 * look for a reference to the source file in the specified source
			 * view and use that directory as the output directory
			 */
			JCas view = null;
			if (sourceViewName == null) {
				view = jCas;
			} else {
				try {
					view = View_Util.getView(jCas, sourceViewName);
				} catch (CASException e) {
					throw new AnalysisEngineProcessException(e);
				}
			}
			File sourceDocumentFile = documentMetaDataHandler.extractSourceDocumentPath(view);
			if (sourceDocumentFile != null) {
				outputFile = new File(sourceDocumentFile.getParentFile(), outputFilename);
			}
		}
		if (outputFile == null) {
			throw new AnalysisEngineProcessException(
					"Unable to determine output directory for document text serialization.", null);
		}
		FileUtil.mkdir(outputFile.getParentFile());
		return outputFile;
	}

	public static AnalysisEngineDescription getDescription(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, File outputDirectory,
			String sourceViewName, String outputViewName, boolean compressOutput, String outputFileInfix,
			IncludeCoveredText includeCoveredText) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationSerializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_OUTPUT_DIRECTORY,
				outputDirectory.getAbsolutePath(), PARAM_SOURCE_VIEW_NAME, sourceViewName, PARAM_OUTPUT_VIEW_NAME,
				outputViewName, PARAM_COMPRESS_OUTPUT_FLAG, compressOutput, PARAM_OUTPUT_FILENAME_INFIX,
				outputFileInfix, PARAM_INCLUDE_COVERED_TEXT, includeCoveredText == IncludeCoveredText.YES);
	}

	public static AnalysisEngineDescription getDescription_SaveToSourceFileDirectory(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, String sourceViewName,
			String outputViewName, boolean compressOutput, String outputFileInfix,
			IncludeCoveredText includeCoveredText) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationSerializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_SOURCE_VIEW_NAME,
				sourceViewName, PARAM_OUTPUT_VIEW_NAME, outputViewName, PARAM_COMPRESS_OUTPUT_FLAG, compressOutput,
				PARAM_OUTPUT_FILENAME_INFIX, outputFileInfix, PARAM_INCLUDE_COVERED_TEXT,
				includeCoveredText == IncludeCoveredText.YES);
	}

}
