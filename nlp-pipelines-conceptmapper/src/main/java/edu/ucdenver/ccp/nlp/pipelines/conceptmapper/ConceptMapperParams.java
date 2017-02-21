package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

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

import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;

/**
 * 
 */
public enum ConceptMapperParams {
	/* @formatter:off */
//	DEFAULT(31, DictionaryNamespace.OBO, null), 
	GO_CC(31, DictionaryNamespace.GO_CC, "go.owl"), 
	GO_MF(111,	DictionaryNamespace.GO_MF, "go.owl"), 
	GO_BP(28, DictionaryNamespace.GO_BP, "go.owl"), 
//	GO(30,	DictionaryNamespace.GO, "go.owl"), 
	SO(31, DictionaryNamespace.SO, "so.owl"), 
	PR(478, DictionaryNamespace.PR, "pr.owl"), 
	CL(31,	DictionaryNamespace.CL, "cl.owl"), 
	NCBI_TAXON(535, DictionaryNamespace.NCBI_TAXON, "ncbitaxon.owl"), 
	CHEBI(13, DictionaryNamespace.CHEBI, "chebi.owl"),
	DOID(31, DictionaryNamespace.DOID, "doid.owl"),
	UBERON(31, DictionaryNamespace.UBERON, "ext.owl");
	
/* @formatter:on */

	private final int paramIndex;
	private final DictionaryNamespace dictNamespace;
	private final String ontologyFileName;

	private ConceptMapperParams(int paramIndex, DictionaryNamespace dictNamespace, String ontologyFileName) {
		this.paramIndex = paramIndex;
		this.dictNamespace = dictNamespace;
		this.ontologyFileName = ontologyFileName;
	}

	public int paramIndex() {
		return paramIndex;
	}

	public DictionaryNamespace dictionaryNamespace() {
		return dictNamespace;
	}

	public String ontologyFileName() {
		return ontologyFileName;
	}

	public File getOntologyFile(File ontologyDirectory) {
		return new File(ontologyDirectory, ontologyFileName);
	}
}
