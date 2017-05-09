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

import java.util.HashSet;
import java.util.Set;

import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.Concept;

public class CHEBIDictionaryEntryModifier implements DictionaryEntryModifier {

	@Override
	public Concept modifyConcept(Concept inputConcept) {

		/*
		 * This sample dictionary entry modifier makes no changes but shows how
		 * you might print the contents of a Concept
		 */

//		System.out.println("id: " + inputConcept.getIdentifier());
//		System.out.println("name: " + inputConcept.getName());
//		if (inputConcept.getOfficialSynonyms() != null) {
//		System.out.println("synonyms: " + inputConcept.getOfficialSynonyms().toString());
//		}
//		if (inputConcept.getDynamicallyGeneratedSynonyms() != null) {
//		System.out.println(
//				"dynamically-generated synonyms: " + inputConcept.getDynamicallyGeneratedSynonyms().toString());
//		}
		
		//DELETE ALL TERMS BELOW
		//Delete (1+) - not in CRAFT
		if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_90880")) {
			return null;
		}
		
		// Delete group
		if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_24433")) {
			return null;
		}
		
		// Delete role - not in dict
		if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_50906")) {
			return null;
		}		
		
		// Delete application
		if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_33232")) {
			return null;
		}
		
		
		
		//Modify dictionary
//		Set<String> modSyns = new HashSet<String>();
//		for (String syn : inputConcept.getOfficialSynonyms()) {
//			if (!syn.contains(" RNA ")) {
//				modSyns.add(syn);
//			}
//		}
//		return new Concept(inputConcept.getIdentifier(), inputConcept.getName(), modSyns,
//				inputConcept.getDynamicallyGeneratedSynonyms());
		return inputConcept;
	}

}