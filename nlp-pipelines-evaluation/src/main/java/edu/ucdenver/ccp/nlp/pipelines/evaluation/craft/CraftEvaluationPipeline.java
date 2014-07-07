package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 Regents of the University of Colorado
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

import java.util.Collection;
import java.util.Set;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.CollectionReaderFactory;

import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.craft.uima.cr.CcpCraftCollectionReader;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.EvaluationPipeline;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class CraftEvaluationPipeline extends EvaluationPipeline {

	/**
	 * @param craftRelease
	 * @param conceptTypesToLoad
	 * @param spanComparatorType
	 * @param mentionComparatorType
	 * @param tsd
	 * @param annotationTypeRegexes
	 * @throws ResourceInitializationException
	 */
	public CraftEvaluationPipeline(CraftRelease craftRelease, Set<CraftConceptType> conceptTypesToLoad,
			TypeSystemDescription tsd, SpanComparatorType spanComparatorType,
			MentionComparatorType mentionComparatorType, Collection<String> annotationTypeRegexes)
			throws ResourceInitializationException {
		super(tsd, getCraftCollectionReader(craftRelease, conceptTypesToLoad), spanComparatorType,
				mentionComparatorType, annotationTypeRegexes);
	}

	/**
	 * @param craftRelease
	 * @param conceptTypesToLoad
	 * @return a {@link CollectionReader} component for the specified {@link CraftRelease}
	 *         initialized to include the {@link CraftConceptType}s noted in the input
	 * @throws ResourceInitializationException
	 */
	public static CollectionReader getCraftCollectionReader(CraftRelease craftRelease,
			Set<CraftConceptType> conceptTypesToLoad) throws ResourceInitializationException {
		CollectionReaderDescription crDesc = CcpCraftCollectionReader.getDescription(craftRelease, conceptTypesToLoad);
		return CollectionReaderFactory.createCollectionReader(crDesc);
	}

}
