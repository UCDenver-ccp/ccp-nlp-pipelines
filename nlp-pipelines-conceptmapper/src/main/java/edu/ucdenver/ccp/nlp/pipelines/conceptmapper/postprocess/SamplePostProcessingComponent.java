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

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.ucdenver.ccp.nlp.core.uima.annotation.CCPTextAnnotation;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

public class SamplePostProcessingComponent extends JCasAnnotator_ImplBase {

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

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

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

			String type = annot.getClassMention().getMentionName();
//			System.out.println("Observed annotation of type: " + type);
		}
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
		return AnalysisEngineFactory.createEngineDescription(SamplePostProcessingComponent.class,
				TypeSystemUtil.getCcpTypeSystem(), PARAM_ONTOLOGY_NAME, ontologyName);
	}

}
