package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.exception.ExceptionUtils;
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

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.common.reflection.ConstructorUtil;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.log.ProcessingErrorLog;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerImpl.IncludeAnnotator;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerImpl.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.uima.shims.ShimDefaults;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Util;
import edu.ucdenver.ccp.nlp.uima.util.View_Util;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class AnnotationDeserializerAE extends JCasAnnotator_ImplBase {

	public static final String PARAM_LOAD_DIRECTORY = "loadDirectory";
	@ConfigurationParameter(mandatory = false, description = "")
	private File loadDirectory;

	/**
	 * If the outputDirectory is not specified, then this view will be used to
	 * search for the source-document-path, which will then be used as the
	 * serialization directory for the output of this annotator.
	 */
	public static final String PARAM_SOURCE_VIEW_NAME = "sourceViewName";
	@ConfigurationParameter(mandatory = false, description = "")
	private String sourceViewName;

	/**
	 * Signifies the view where the annotations will be loaded
	 */
	public static final String PARAM_LOAD_VIEW_NAME = "loadViewName";
	@ConfigurationParameter(mandatory = false, description = "")
	private String loadViewName;

	public static final String PARAM_INPUT_FILENAME_INFIXES = "inputFilenameInfixes";
	@ConfigurationParameter(mandatory = false, description = "An array of infixes to use to identify annotations files to load.")
	private String[] inputFilenameInfixes;

	public static final String PARAM_INPUT_FILENAME_SUFFIXES = "inputFilenameSuffixes";
	@ConfigurationParameter(mandatory = false, description = "An array of suffixes to use to identify annotations files to load.")
	private String[] inputFilenameSuffixes;

	public static final String PARAM_DOCUMENT_METADATA_HANDLER_CLASS = "documentMetadataHandlerClassName";
	private static final String ANNOT_FILE_SUFFIX = ".annot";
	@ConfigurationParameter(mandatory = false, description = "name of the DocumentMetadataHandler implementation to use", defaultValue = ShimDefaults.CCP_DOCUMENT_METADATA_HANDLER_CLASS_NAME)
	private String documentMetadataHandlerClassName;
	private DocumentMetadataHandler documentMetaDataHandler;

	private Logger logger;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		documentMetaDataHandler = (DocumentMetadataHandler) ConstructorUtil
				.invokeConstructor(documentMetadataHandlerClassName);
		logger = aContext.getLogger();

		if (inputFilenameInfixes != null && inputFilenameSuffixes != null) {
			String message = "Both the input_filename_infixes and input_filename_suffixes parameters have been "
					+ "set simultaneously. Please only set one of those parameters at a time.";
			logger.log(Level.SEVERE, message);
			throw new ResourceInitializationException(new IllegalArgumentException(message));
		}

		if (inputFilenameInfixes != null) {
			logger.log(Level.INFO, "AnnotationDeserializer initialized with the following file infixes: "
					+ Arrays.toString(inputFilenameInfixes));
		}
		if (inputFilenameSuffixes != null) {
			logger.log(Level.INFO, "AnnotationDeserializer initialized with the following file suffixes: "
					+ Arrays.toString(inputFilenameSuffixes));
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String documentId = documentMetaDataHandler.extractDocumentId(jCas);
		List<File> filesToLoad;
		try {
			filesToLoad = getFilesToLoad(documentId, jCas);
		} catch (CASException | IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		for (File inputFile : filesToLoad) {
			try {
				loadAnnotations(jCas, inputFile);
			} catch (Exception e) {
				ProcessingErrorLog errorLog = new ProcessingErrorLog(jCas);
				errorLog.setErrorMessage(e.getMessage());
				errorLog.setStackTrace(ExceptionUtils.getStackTrace(e));
				errorLog.setComponentAtFault(this.getClass().getName());
				errorLog.addToIndexes();
				logger.log(Level.WARNING,
						"Error during annotation loading for document: " + UIMA_Util.getDocumentID(jCas) + " -- file: "
								+ inputFile.getAbsolutePath() + " -- " + e.getMessage() + " -- ");
				e.printStackTrace();
			}
		}
	}

	private List<File> getFilesToLoad(String documentId, JCas jCas)
			throws AnalysisEngineProcessException, CASException, IOException {
		List<File> filesToLoad = new ArrayList<File>();
		if (inputFilenameInfixes != null) {
			for (String infix : inputFilenameInfixes) {
				File inputFile = getInputFile(jCas, documentId, infix);
				filesToLoad.add(inputFile);
			}
		} else if (inputFilenameSuffixes != null) {
			File inputFileDirectory = getLoadDirectory(jCas);
			for (Iterator<File> fileIterator = FileUtil.getFileIterator(inputFileDirectory, false,
					inputFilenameSuffixes); fileIterator.hasNext();) {
				File file = fileIterator.next();
				if (file.getName().startsWith(documentId)) {
					filesToLoad.add(file);
				}
			}
		}
		return filesToLoad;
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
	private void loadAnnotations(JCas jCas, File inputFile) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Loading annotation file: " + inputFile.getAbsolutePath());
		StreamLineIterator lineIter = null;
		List<TextAnnotation> textAnnotations = new ArrayList<TextAnnotation>();
		try {
			for (lineIter = (inputFile.getName().endsWith(".gz"))
					? new StreamLineIterator(new GZIPInputStream(new FileInputStream(inputFile)),
							CharacterEncoding.UTF_8, null)
					: new StreamLineIterator(inputFile, CharacterEncoding.UTF_8, null); lineIter.hasNext();) {
				Line line = lineIter.next();
				TextAnnotation annot = AnnotationSerializerImpl.fromString(line.getText());
				textAnnotations.add(annot);
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		} finally {
			if (lineIter != null) {
				lineIter.close();
			}
		}

		UIMA_Util uimaUtil = new UIMA_Util();
		uimaUtil.putTextAnnotationsIntoJCas(jCas, textAnnotations);
	}

	/**
	 * @param documentId
	 * @param infix
	 *            optional text string to insert as part of the filename
	 * @return
	 */
	public static String getInputFileName(String documentId, String infix) {
		if (infix == null) {
			return documentId + ANNOT_FILE_SUFFIX;
		} else {
			return documentId + "-" + infix + ANNOT_FILE_SUFFIX;
		}
	}

	/**
	 * @param jCas
	 * @param documentId
	 * @return a reference to the output file where the document text will be
	 *         saved
	 * @throws AnalysisEngineProcessException
	 * @throws CASException
	 */
	private File getInputFile(JCas jCas, String documentId, String infix)
			throws AnalysisEngineProcessException, CASException {
		String inputFilename = getInputFileName(documentId, infix);
		File inputFile = null;
		File inputFileDirectory = getLoadDirectory(jCas);
		if (inputFileDirectory != null) {
			inputFile = new File(inputFileDirectory, inputFilename);
		}

		if (inputFile == null) {
			throw new AnalysisEngineProcessException(
					"Unable to determine output directory for document text serialization.", null);
		}

		if (!inputFile.exists()) {
			/* check for the compressed version */
			inputFile = new File(inputFile.getAbsolutePath() + ".gz");
			if (!inputFile.exists()) {
				throw new IllegalStateException("Cannot find input file to load: " + inputFile.getAbsolutePath());
			}
		}

		return inputFile;
	}

	private File getLoadDirectory(JCas jCas) throws CASException {
		if (loadDirectory != null) {
			return loadDirectory;
		}
		/*
		 * look for a reference to the source file in the specified source view
		 * and use that directory as the output directory
		 */
		JCas view = null;
		if (sourceViewName == null) {
			view = jCas;
		} else {
			view = View_Util.getView(jCas, sourceViewName);
		}
		File sourceDocumentFile = documentMetaDataHandler.extractSourceDocumentPath(view);
		if (sourceDocumentFile != null) {
			return sourceDocumentFile.getParentFile();
		}
		return null;
	}

	public static AnalysisEngineDescription getDescription(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, File loadDirectory,
			String loadViewName, IncludeCoveredText includeCoveredText, IncludeAnnotator includeAnnotator,
			String[] inputFileInfixes, String[] inputFileSuffixes) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationDeserializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_LOAD_DIRECTORY,
				loadDirectory.getAbsolutePath(), PARAM_LOAD_VIEW_NAME, loadViewName, PARAM_INPUT_FILENAME_INFIXES,
				inputFileInfixes, PARAM_INPUT_FILENAME_SUFFIXES, inputFileSuffixes);
	}

	public static AnalysisEngineDescription getDescription_LoadFromSourceFileDirectory(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, String sourceViewName,
			String loadViewName, IncludeCoveredText includeCoveredText, IncludeAnnotator includeAnnotator,
			String[] inputFileInfixes, String[] inputFileSuffixes) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationDeserializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_SOURCE_VIEW_NAME,
				sourceViewName, PARAM_LOAD_VIEW_NAME, loadViewName, PARAM_INPUT_FILENAME_INFIXES, inputFileInfixes,
				PARAM_INPUT_FILENAME_SUFFIXES, inputFileSuffixes);
	}

}
