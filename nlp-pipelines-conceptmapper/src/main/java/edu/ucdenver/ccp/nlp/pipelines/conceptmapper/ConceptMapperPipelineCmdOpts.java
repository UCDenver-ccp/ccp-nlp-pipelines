package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

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

import java.io.File;

import org.apache.uima.jcas.tcas.Annotation;
import org.kohsuke.args4j.Option;

import edu.ucdenver.ccp.nlp.pipelines.PipelineCmdOptsBase;


public class ConceptMapperPipelineCmdOpts extends PipelineCmdOptsBase {

	public enum DictionaryParameterOperation {
		/**
		 * Indicates that the dictionary parameter should be used as is
		 */
		USE,
		/**
		 * Indicates that the dictionary parameter should be ignored, and typically a new dictionary
		 * is created from scratch
		 */
		IGNORE,
		/**
		 * Indicates that the dictionary parameter is a directory (where a new dictionary can be
		 * built)
		 */
		TREAT_AS_DIRECTORY
	}

	@Option(name = "-dict", required = true, usage = "the ConceptMapper dictionary file path")
	private String cmDictionaryFile;

	/**
	 * @return the cmDictionaryFile
	 */
	public File getDictionaryFile() {
		return new File(cmDictionaryFile);
	}

	public void setDictionaryFile(File dictFile) {
		this.cmDictionaryFile = dictFile.getAbsolutePath();
	}

	@Option(name = "-spanClass", required = true, usage = "the class that the ConceptMapper will use to chunk the input, e.g. a sentence")
	private String cmSpanClassName;

	/**
	 * @return the annotation class that the concept mapper will use for chunking the input. This
	 *         will be used to set the spanFeatureStructureClass input parameter.
	 */
	public Class<? extends Annotation> getSpanClass() {
		try {
			return (Class<? extends Annotation>) Class.forName(cmSpanClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public void setSpanClass(Class<? extends Annotation> spanClass) {
		this.cmSpanClassName = spanClass.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConceptMapperPipelineCmdOpts [cmDictionaryFile=" + cmDictionaryFile + ", getNumToSkip()="
				+ getNumToSkip() + ", getNumToProcess()=" + getNumToProcess() + ", getBatchNumber()="
				+ getBatchNumber() + ", getInputFileOrDirectoryPath()=" + getInputFileOrDirectoryPath()
				+ ", getOutputFileOrDirectoryPath()=" + getOutputFileOrDirectoryPath()
				+ ", requirePresenceOfAbstract()=" + requirePresenceOfAbstract() + "]";
	}

}
