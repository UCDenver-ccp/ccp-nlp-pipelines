package edu.ucdenver.ccp.nlp.pipelines.conceptmapper;

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
