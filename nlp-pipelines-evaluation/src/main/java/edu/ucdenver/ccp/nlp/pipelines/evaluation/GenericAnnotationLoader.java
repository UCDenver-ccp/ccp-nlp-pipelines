package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import edu.ucdenver.ccp.nlp.uima.util.UIMA_Annotation_Util;
import edu.ucdenver.ccp.nlp.uima.util.UIMA_Util;

public class GenericAnnotationLoader extends JCasAnnotator_ImplBase {

	private static final Logger logger = Logger.getLogger(GenericAnnotationLoader.class);
	
	public static final String ANNOTATION_DIRECTORY = ConfigurationParameterFactory.createConfigurationParameterName(
			GenericAnnotationLoader.class, "directory");
	@ConfigurationParameter(description="directory where annotation files live, e.g. Sequence Ontology " +
			"might reside in ../path/to/annotations/SO", 
			mandatory=true, defaultValue= "null")
	private String directory;
	
	public static final String FILTER = ConfigurationParameterFactory.createConfigurationParameterName(
			GenericAnnotationLoader.class, "filter");
	@ConfigurationParameter(description="only imports annotations with scores >= filter", 
			mandatory=false, defaultValue= "0")
	private int filter;
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String docID = UIMA_Util.getDocumentID(jcas);
		File file = new File(directory + File.separator + docID);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			
			while ((text = reader.readLine()) != null) {
				String [] elements = text.split("\t");
				String id = elements[0];
				String coveredText = elements[1];
				int span_start = Integer.parseInt(elements[2]);
				int span_end = Integer.parseInt(elements[3]);
				
				if(elements.length == 5) {
					Integer score = Integer.parseInt(elements[4]);
				
					if(score >= filter) {
						logger.debug("Adding annotation: " + id + "  Start/End: " + span_start + " / " + span_end + " with score " + score);
						UIMA_Annotation_Util.createCCPTextAnnotation(id, span_start, span_end, jcas);
					}
				} else {
					logger.debug("Adding annotation: " + id + "  Start/End: " + span_start + " / " + span_end);
					UIMA_Annotation_Util.createCCPTextAnnotation(id, span_start, span_end, jcas);
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns an initialized MetaMapAnnotationLoader {@link AnalysisEngine}
	 * 
	 * @param documentMetaDataHandlerClass
	 * @param tsd
	 * @param directory
	 * @param filter
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			String directory, int filter) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(GenericAnnotationLoader.class, tsd,
				GenericAnnotationLoader.ANNOTATION_DIRECTORY, directory, GenericAnnotationLoader.FILTER, filter);
	}
	
	/**
	 * Returns an initialized MetaMapAnnotationLoader {@link AnalysisEngine}
	 * 
	 * @param documentMetaDataHandlerClass
	 * @param tsd
	 * @param directory
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			String directory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(GenericAnnotationLoader.class, tsd,
				GenericAnnotationLoader.ANNOTATION_DIRECTORY, directory);
	}

}