package edu.ucdenver.ccp.nlp.pipelines.conceptmapper.postprocess;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

public class MaylaPostProcessingComponent extends JCasAnnotator_ImplBase {

	public static final String PARAM_DICTIONARYFILE = "dictionaryFile";
	@ConfigurationParameter()
	private File dictionaryFile = null;

	public static final String PARAM_CONCEPTFREQ = "conceptFreq";
	@ConfigurationParameter(mandatory = false)
	private Integer conceptFreq = null;

	private static final Logger logger = Logger.getLogger(MaylaPostProcessingComponent.class);

	private Map<String, String> conceptIdToLabelMap = null;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
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

		for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jCas, CCPTextAnnotation.class); annotIter
				.hasNext();) {
			CCPTextAnnotation annot = annotIter.next();
			WrappedCCPTextAnnotation wrappedTa = new WrappedCCPTextAnnotation(annot);
			int start = wrappedTa.getAggregateSpan().getSpanStart();
			int end = wrappedTa.getAggregateSpan().getSpanEnd();
			String annotatorID = wrappedTa.getAnnotator().getAnnotatorID();
			if (!annotatorID.equals("99099099")) { // 99099099 - gold standard
				String type = "http://purl.obolibrary.org/obo/"
						+ annot.getClassMention().getMentionName().replaceAll(":", "_");
				// label from the ontology_dict file - only element in list
				String label = conceptIdToLabelMap.get(type);
				// from annotation of text, could be acronym -> smaller
				String textword = documentText.substring(start, end);
				int textwordFrequency = StringUtils.countMatches(documentText, textword);

				if ((conceptFreq == null && (!CharMatcher.JAVA_UPPER_CASE.matchesAllOf(textword)
						&& !Character.isUpperCase(textword.charAt(0))))
						|| (conceptFreq != null && ((textwordFrequency < conceptFreq) && !textword.equals(label)))) {
					annot.removeFromIndexes(); // remove annotation
				}
			}

		}
	}

	private Map<String, String> loadConceptIdToLabelMap(File inputFile)
			throws ParserConfigurationException, SAXException, IOException {
		Map<String, String> conceptIdToLabelMap = new HashMap<String, String>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("token");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			int k = temp % 10000;
			if (k == 0) {
				logger.debug("==== " + temp + " done ====");
			}
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String label = eElement.getAttribute("canonical");
				String id = eElement.getAttribute("id");
				conceptIdToLabelMap.put(id, label);
			}
		}
		return conceptIdToLabelMap;
	}

	public static Integer getConceptFrequency(DictionaryNamespace dictionaryNamespace) {
		if (dictionaryNamespace == null) {
			return null;
		}
		switch (dictionaryNamespace) {
		case CHEBI:
			return 4;
		case PR:
			return 40;
		case FUNK_GO_MF:
			return 1;
		case FUNK_GO_BP:
			return 20;
		case FUNK_GO_CC:
			return 15;
		case CL:
			return 2;
		case SO:
			return 1;
		case NCBI_TAXON:
			return 26;
		case GO_MF:
			return 4;
		case GO_BP:
			return 7;
		case GO_CC:
			return 10;
		default:
			throw new IllegalArgumentException("Unhandled dictionary namespace: " + dictionaryNamespace.name());
		}
	}

	/**
	 * Create a AE description that does not use the concept-frequency check for
	 * annotation removal
	 * 
	 * @param dictionaryFile
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription getDescription(File dictionaryFile) throws ResourceInitializationException {
		return getDescription(dictionaryFile, null);
	}

	/**
	 * @return a {@link AnalysisEngineDescription} for this component
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription getDescription(File dictionaryFile, DictionaryNamespace dictionaryNamespace)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(MaylaPostProcessingComponent.class,
				TypeSystemUtil.getCcpTypeSystem(), PARAM_DICTIONARYFILE, dictionaryFile.getAbsolutePath(),
				PARAM_CONCEPTFREQ, getConceptFrequency(dictionaryNamespace));
	}

	public static AnalysisEngineDescription getDescription(File dictionaryFile, int conceptFreq)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(MaylaPostProcessingComponent.class,
				TypeSystemUtil.getCcpTypeSystem(), PARAM_DICTIONARYFILE, dictionaryFile.getAbsolutePath(),
				PARAM_CONCEPTFREQ, conceptFreq);
	}

}
