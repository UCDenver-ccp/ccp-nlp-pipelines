package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
import org.uimafit.util.JCasUtil;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.common.reflection.ConstructorUtil;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
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

	public static final String PARAM_INPUT_FILENAME_INFIX = "inputFilenameInfix";
	@ConfigurationParameter(mandatory = false, description = "An option string that, if not null, is appended to the output file. "
			+ "This can be useful for identifying the type of annotations containined in an XMI file, for example.")
	private String inputFilenameInfix;

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
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String documentId = documentMetaDataHandler.extractDocumentId(jCas);
		File inputFile = getInputFile(jCas, documentId);

		loadAnnotations(jCas, inputFile);
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
				TextAnnotation annot = AnnotationSerializer.fromString(line.getText());
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
	 * Given a document Id, this method returns the name of the corresponding
	 * XMI file that will be created
	 * 
	 * @param documentId
	 * 
	 * @return
	 */
	public String getInputFileName(String documentId) {
		return getInputFileName(documentId, inputFilenameInfix);
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
	 */
	private File getInputFile(JCas jCas, String documentId) throws AnalysisEngineProcessException {
		String inputFilename = getInputFileName(documentId);
		File inputFile = null;
		if (loadDirectory != null) {
			inputFile = new File(loadDirectory, inputFilename);
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
				inputFile = new File(sourceDocumentFile.getParentFile(), inputFilename);
			}
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

	public static AnalysisEngineDescription getDescription(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, File loadDirectory,
			String loadViewName, String inputFileInfix) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationDeserializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_LOAD_DIRECTORY,
				loadDirectory.getAbsolutePath(), PARAM_LOAD_VIEW_NAME, loadViewName, PARAM_INPUT_FILENAME_INFIX,
				inputFileInfix);
	}

	public static AnalysisEngineDescription getDescription_SaveToSourceFileDirectory(TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataHandlerClass, String sourceViewName,
			String loadViewName, String inputFileInfix) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AnnotationDeserializerAE.class, tsd,
				PARAM_DOCUMENT_METADATA_HANDLER_CLASS, documentMetadataHandlerClass, PARAM_SOURCE_VIEW_NAME,
				sourceViewName, PARAM_LOAD_VIEW_NAME, loadViewName, PARAM_INPUT_FILENAME_INFIX, inputFileInfix);
	}

}
