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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Token;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.craft.uima.cr.CcpCraftCollectionReader;
import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.annotation.impl.WrappedCCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

public class PosTaggingPostProcessingComponent extends JCasAnnotator_ImplBase {

	/*
	 * If you need input parameters, then you can add them using the Java
	 * Annotation shown below. By convention, the parameter name always starts
	 * with PARAM_, e.g. as shown below PARAM_ONTOLOGY_NAME is used for the
	 * parameter name. Note that the string "ontologyName" must match exactly
	 * the name of the variable "ontologyName". Parameters don't have to be
	 * Strings. They can be most non-collection types, e.g. Integer, File, etc.
	 * If you need a collection, use an array.
	 */
	public static final String PARAM_ONTOLOGY_NAME = "ontologyName";
	@ConfigurationParameter()
	private String ontologyName = null;

	/*
	 * 
	 * POS tag distribution reader
	 */
	public static Map<String, Map<String, Integer>> serializedHashMapReader() {

		Map<String, Map<String, Integer>> map = null;
		try {
			FileInputStream fis = new FileInputStream(
					"/Users/negacy/workspace/syntactic_variation/pattern-based-pos-dist.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			// return;
		}

		return map;
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		System.out.println("NEGACY");
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		/*
		 * any parameter will be automatically initialized and will be available
		 * in this method
		 */
		System.out.println("The ontology name parameter is set to: " + ontologyName);

		/*
		 * UIMA stores annotations in a data structure called a CAS (Common
		 * Annotation Structure), and this method takes as input a Java wrapper
		 * of the CAS, so a JCas. There is one JCas per document so this
		 * component works on a single document at a time. When the pipeline
		 * runs it will be fed each document successively. The following shows
		 * how to iterate through the annotations produced by the ConceptMapper:
		 */
		Map<String, Map<String, Integer>> map = serializedHashMapReader();
		// System.out.println(map.get("<http://purl.obolibrary.org/obo/GO_0031349>"));
		for (Iterator<CCPTextAnnotation> annotIter = JCasUtil.iterator(jCas, CCPTextAnnotation.class); annotIter
				.hasNext();) {
			CCPTextAnnotation annot = annotIter.next();
			WrappedCCPTextAnnotation wrappedTa = new WrappedCCPTextAnnotation(annot);
			Integer annotatorID = wrappedTa.getAnnotator().getAnnotatorID();
			if (annotatorID != 99099099) {	//99099099 - gold standard
				String type = annot.getClassMention().getMentionName();
				System.out.println("Observed annotation of type: " + type);
				String uri = "<http://purl.obolibrary.org/obo/" + type.replace(":", "_") + ">";
				//System.out.println("uri: " + uri + "--" + map.get(uri));
				String posPattern = "";
				for (Token token : JCasUtil.selectCovered(jCas, Token.class, annot)) {
					System.out.println(token.getCoveredText() + " TODO " + token.getPos());
					posPattern = posPattern + token.getPos() + "|";
				}
				if (posPattern.length() != 0) {
					// remove last character, which is `|` from POS pattern
					posPattern = posPattern.substring(0, posPattern.length() - 1);
					System.out.println("pos pattern: " + posPattern);
				}
				if (map.get(uri) != null && map.get(uri).containsKey(posPattern)) { // Enterez
																					// Gene
																					// not
																					// supported.
					double sum = 0.0;
					for (String key : map.get(uri).keySet()) {
						sum += map.get(uri).get(key);
					}
					System.out.println("numbers: ");
					System.out.println(sum);
					System.out.println(map.get(uri).get(posPattern));
					System.out.println(map.get(uri).get(posPattern) / sum );
					/*
					if (map.get(uri).get(posPattern) / sum < 0.5) {
						//annot.removeFromIndexes();
						System.out.print(map.get(uri).get(posPattern) / sum);
						System.out.println("Remove: " + uri);

					}*/
				}
			}
		}

		/*
		 * 
		 * get pos tags
		 */
		/*
		 * for (CCPTextAnnotation ccpTa : JCasUtil.select(jCas,
		 * CCPTextAnnotation.class)) { System.out.println(ccpTa.getCoveredText()
		 * + " -- " + ccpTa.getClassMention().getMentionName());
		 * //annotationCount += ccpTa.getCoveredText().split(" ").length; for
		 * (Token token : JCasUtil.selectCovered(jCas, Token.class, ccpTa)) {
		 * System.out.println(token.getCoveredText() + " *** " +
		 * token.getPos()); } }
		 */
	}

	/**
	 * @param ontologyName
	 *            This demonstrates how to pass a parameter into the
	 *            {@link AnalysisEngineDescription} such that it initializes it
	 *            properly
	 * @return a {@link AnalysisEngineDescription} for this component
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription getDescription(String ontologyName) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(PosTaggingPostProcessingComponent.class,
				TypeSystemUtil.getCcpTypeSystem(), PARAM_ONTOLOGY_NAME, ontologyName);
	}
	 
	 public static void main(String[] args) throws AnalysisEngineProcessException, UIMAException, IOException{
		System.out.println("...test...."); 
		PosTaggingPostProcessingComponent obj = new PosTaggingPostProcessingComponent();
		  
		CollectionReaderDescription crDesc =
			CcpCraftCollectionReader.getDescription(CraftRelease.MAIN,
			CollectionsUtil.createSet(CraftConceptType.TREEBANK,
			CraftConceptType.CHEBI, CraftConceptType.CL, CraftConceptType.EG,
			CraftConceptType.GOCC, CraftConceptType.GOBP, CraftConceptType.GOMF,
			CraftConceptType.NCBITAXON, CraftConceptType.PR, CraftConceptType.SO));
		 
		CollectionReader cr = CollectionReaderFactory.createCollectionReader(crDesc);
	 
		for (JCas jCas : new JCasIterable(cr)) { obj.process(jCas); break; } }
	 

}
