package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 - 2017 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

//packages for parsing xml dictionary files to get the canonical name of the concept
import java.io.*;
import java.util.ArrayList;
//import java.io.File;
//import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CharMatcher;

import ca.drugbank.SynonymListType;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;


public class MaylaPostProcessingComponent extends JCasAnnotator_ImplBase {

	/*
	 * If you need input parameters, then you can add them using the Java
	 * Annotation shown below. By convention, the parameter name always starts
	 * with PARAM_, e.g. as shown below PARAM_ONTOLOGY_NAME is used for the
	 * parameter name. Note that the string "ontologyName" must match exactly
	 * the name of the variable "ontologyName". Parameters don't have to be
	 * Strings. They can be most non-collection types, e.g. Integer, File, etc.
	 * If you need a collection, use an array.
	 */
	public static final String PARAM_DICTIONARYFILE = "dictionaryFile";
	@ConfigurationParameter()
	private File dictionaryFile = null;
	private static final Logger logger = Logger.getLogger(MaylaCraftConceptMapperEvaluatorMain.class);
	
	
	Map<String, String> conceptIdToLabelMap = null;
	
	class WordChecker {
	    public boolean check_for_word(String word) {
	        // System.out.println(word);
	        try {
	            BufferedReader in = new BufferedReader(new FileReader(
	                    "~/Library/Spelling/LocalDictionary"));
	            String str;
	            while ((str = in.readLine()) != null) {
	                if (str.indexOf(word) != -1) {
	                    return true;
	                }
	            }
	            in.close();
	        } catch (IOException e) {
	        }

	        return false;
	    }
	}
	
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		if (conceptIdToLabelMap == null) {
			try {
				conceptIdToLabelMap = loadConceptIdToLabelMap(dictionaryFile);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new ResourceInitializationException(e);
			}
		}
	}


	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		
		
		String documentText = jCas.getDocumentText();
		int documentTextLength = documentText.length();
		
		/*
		 * any parameter will be automatically initialized and will be available
		 * in this method
		 */
//		System.out.println("The ontology name parameter is set to: " + ontologyName);

		/*
		 * UIMA stores annotations in a data structure called a CAS (Common
		 * Annotation Structure), and this method takes as input a Java wrapper
		 * of the CAS, so a JCas. There is one JCas per document so this
		 * component works on a single document at a time. When the pipeline
		 * runs it will be fed each document successively. The following shows
		 * how to iterate through the annotations produced by the ConceptMapper:
		 */

		for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jCas, CCPTextAnnotation.class); annotIter
				.hasNext();) {
			CCPTextAnnotation annot = annotIter.next();
			WrappedCCPTextAnnotation wrappedTa = new WrappedCCPTextAnnotation(annot);
			int start = wrappedTa.getAggregateSpan().getSpanStart();
			int end = wrappedTa.getAggregateSpan().getSpanEnd();
			Integer annotatorID = wrappedTa.getAnnotator().getAnnotatorID();
			if (annotatorID != 99099099) {	//99099099 - gold standard, do everything in if statement block
				//type from annotation text
				String type = "http://purl.obolibrary.org/obo/" + annot.getClassMention().getMentionName().replaceAll(":", "_"); 
//				System.out.println("type: " + type);
				String documentBeforeTextWord;
				String label = conceptIdToLabelMap.get(type); //label from the ontology_dict file - only element in list
//				System.out.println("label: " + label);
				String textword = documentText.substring(start, end); //from annotation of text, could be acronym -> smaller
				if ((end+label.length()+5) > documentText.length() ) {
					documentBeforeTextWord = documentText.substring(0,documentText.length()).toLowerCase();
				}else{
					documentBeforeTextWord = documentText.substring(0,(end+label.length()+5)).toLowerCase();
				}
//				System.out.println("document text: "+ documentBeforeTextWord);
				int textwordFrequency = StringUtils.countMatches(documentText,textword);
//				System.out.println(textword + " count in text: " + textwordFrequency);
				
				//really high precision but super low recall (treatment 1) 
//				(!CharMatcher.JAVA_UPPER_CASE.matchesAllOf(textword) && !Character.isUpperCase(textword.charAt(0)))
				
				//Not super high precision but not terrible recall (treatment 2) with different conceptfrequency per ontology
//				textwordFrequency < conceptFreq) && !textword.equals(label)))
				
				//not needed I guess cuz the other stuff gets rid of a lot more
//				|| (documentBeforeTextWord.contains((label + " ( " + textword + " ) ").toLowerCase())
//						&& !documentBeforeTextWord.contains((textword + " ( " + label + " ) ").toLowerCase()))
				
				
				if (  (!CharMatcher.JAVA_UPPER_CASE.matchesAllOf(textword) && !Character.isUpperCase(textword.charAt(0))) ) {
//					if ((end+30) < documentTextLength && (start-30) > 0) {
//						String documentSentence = documentText.substring(start-30, end+30);
//						System.out.println("Problematic term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					} else if ((end+30) >= documentTextLength && (start-30) > 0) {
//						String documentSentence = documentText.substring(start-30, documentTextLength);
//						System.out.println("Problematic term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					
//					} else if ((end+30) < documentTextLength && (start-30) <= 0){
//						String documentSentence = documentText.substring(0, end+30);
//						System.out.println("Problematic term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					} else { //((end+30) >= documentTextLength && (start-30) <= 0)
//						String documentSentence = documentText.substring(0, documentTextLength);
//						System.out.println("Problematic term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//						
//					}
					annot.removeFromIndexes(); //remove annotation 
					
//				}else if (!label.equals(textword) && (textwordLength < fourthLabelLength) 
//						&& (documentBeforeTextWord.contains((label + " ( " + textword + " ) ").toLowerCase()) 
//						|| documentBeforeTextWord.contains((textword + " ( " + label + " ) ").toLowerCase()))) {
//					System.out.println("made it here");
//				}else{
//					if ((end+30) < documentTextLength && (start-30) > 0) {
//						String documentSentence = documentText.substring(start-30, end+30);
//						System.out.println("Kept term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					} else if ((end+30) >= documentTextLength && (start-30) > 0) {
//						String documentSentence = documentText.substring(start-30, documentTextLength);
//						System.out.println("Kept term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					
//					} else if ((end+30) < documentTextLength && (start-30) <= 0){
//						String documentSentence = documentText.substring(0, end+30);
//						System.out.println("Kept term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//					} else { //((end+30) >= documentTextLength && (start-30) <= 0)
//						String documentSentence = documentText.substring(0, documentTextLength);
//						System.out.println("Kept term: " + label + ", " + textword);
//						System.out.println("Sentence: " + documentSentence);
//						
//					}
				}
			}
			
		}
	}


	
	private Map<String, String> loadConceptIdToLabelMap(File inputFile) throws ParserConfigurationException, SAXException, IOException {
	    Map<String, String> conceptIdToLabelMap = new HashMap<String, String>();    
//	    System.out.println("ontology name: " + ontologyName);
//		File inputFile = new File("/tmp/cm-evals/cm-dicts/cmDict-FUNK_GO_MF.xml");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(inputFile);
	    doc.getDocumentElement().normalize();
	    NodeList nList = doc.getElementsByTagName("token");
//	         System.out.println("Nodes: " + nList);
	    for (int temp = 0; temp < nList.getLength(); temp++) {
	    	int k = temp % 10000;
	    	if (k == 0) {
	    		logger.info("==== "+ temp + " done ====");
	    	}
	        Node nNode = nList.item(temp);
//	             System.out.println("\nToken :" + nNode.getNodeName());
	        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        	Element eElement = (Element) nNode;
	            String label = eElement.getAttribute("canonical");
	            String id = eElement.getAttribute("id");

	   	            
	            conceptIdToLabelMap.put(id, label);
	            }
	         }
	         return conceptIdToLabelMap;
		
	}
	
	private boolean check_for_word(String word) {
        // System.out.println(word);
        try {
            BufferedReader in = new BufferedReader(new FileReader(
                    "/usr/share/dict/american-english"));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.indexOf(word) != -1) {
                    return true;
                }
            }
            in.close();
        } catch (IOException e) {
        }

        return false;
    }

	/**
	 * @param ontologyName
	 *            This demonstrates how to pass a parameter into the
	 *            {@link AnalysisEngineDescription} such that it initializes it
	 *            properly
	 * @return a {@link AnalysisEngineDescription} for this component
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription getDescription(File dictionaryFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(MaylaPostProcessingComponent.class,
				TypeSystemUtil.getCcpTypeSystem(), PARAM_DICTIONARYFILE, dictionaryFile.getAbsolutePath());
	}

}
