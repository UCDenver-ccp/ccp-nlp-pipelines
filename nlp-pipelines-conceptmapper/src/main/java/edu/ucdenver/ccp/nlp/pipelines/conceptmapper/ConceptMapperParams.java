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
	FUNK_GO_CC(31, 31, DictionaryNamespace.FUNK_GO_CC, "go.owl"), 
	FUNK_GO_MF(111,	111, DictionaryNamespace.FUNK_GO_MF, "go.owl"), 
	FUNK_GO_BP(28, 47, DictionaryNamespace.FUNK_GO_BP, "go.owl"), 
//	GO(30,	DictionaryNamespace.GO, "go.owl"), 
	SO(31, 191, DictionaryNamespace.SO, "so.owl"), 
	PR(478, 286, DictionaryNamespace.PR, "pr.owl"), 
	CL(31, 143,	DictionaryNamespace.CL, "cl.owl"), 
	NCBI_TAXON(535,279, DictionaryNamespace.NCBI_TAXON, "ncbitaxon.owl"), 
	CHEBI(13,189, DictionaryNamespace.CHEBI, "chebi.owl"),
	DOID(31, 47, DictionaryNamespace.DOID, "doid.owl"),
	UBERON(31, 47, DictionaryNamespace.UBERON, "ext.owl");
/* @formatter:on */

	public enum ConceptMapperOptimization {
		F_SCORE, PRECISION
	}

	private final int fscoreOptimizedParamIndex;
	private final int precisionOptimizedParamIndex;
	private final DictionaryNamespace dictNamespace;
	private final String ontologyFileName;

	private ConceptMapperParams(int fscoreOptimizedParamIndex, int precisionOptimizedParamIndex,
			DictionaryNamespace dictNamespace, String ontologyFileName) {
		this.fscoreOptimizedParamIndex = fscoreOptimizedParamIndex;
		this.precisionOptimizedParamIndex = precisionOptimizedParamIndex;
		this.dictNamespace = dictNamespace;
		this.ontologyFileName = ontologyFileName;
	}

	public int optimizedParamIndex(ConceptMapperOptimization opt) {
		if (opt == ConceptMapperOptimization.F_SCORE) {
			return fscoreOptimizedParamIndex;
		}
		return precisionOptimizedParamIndex;
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
