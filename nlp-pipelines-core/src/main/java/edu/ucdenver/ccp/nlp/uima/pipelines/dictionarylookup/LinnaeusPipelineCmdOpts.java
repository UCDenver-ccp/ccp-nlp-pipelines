package edu.ucdenver.ccp.nlp.uima.pipelines.dictionarylookup;

import java.io.File;

import org.kohsuke.args4j.Option;

import edu.ucdenver.ccp.nlp.uima.pipelines.PipelineCmdOptsBase;

public class LinnaeusPipelineCmdOpts extends PipelineCmdOptsBase {

	@Option(name = "-dict", usage = "the Linnaeus dictionary file path")
	private String dictionaryFile;

	@Option(name = "-outputFilePrefix", usage = "prefix to append to the output files")
	private String outputFilePrefix;

	/**
	 * @return the Dictionary File
	 */
	public File getDictionaryFile() {
		return new File(dictionaryFile);
	}

	public String getOutputFilePrefix() {
		return outputFilePrefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LinnaeusPipelineCmdOpts [dictionaryFile=" + dictionaryFile + ", outputFilePrefix=" + outputFilePrefix
				+ ", getNumToSkip()=" + getNumToSkip() + ", getNumToProcess()=" + getNumToProcess()
				+ ", getBatchNumber()=" + getBatchNumber() + ", getInputFileOrDirectoryPath()="
				+ getInputFileOrDirectoryPath() + ", getOutputFileOrDirectoryPath()=" + getOutputFileOrDirectoryPath()
				+ ", requirePresenceOfAbstract()=" + requirePresenceOfAbstract() + "]";
	}

}
