package edu.ucdenver.ccp.nlp.uima.pipelines.proteins;

import java.io.File;

import org.kohsuke.args4j.Option;

import edu.ucdenver.ccp.nlp.uima.pipelines.PipelineCmdOptsBase;

public class ProteinNormalizationPipelineCmdOpts extends PipelineCmdOptsBase {

	@Option(name = "-dict", usage = "the directory where the pro dictionary is")
	private String proDictionaryDirectory;

	@Option(name = "-obo", usage = "the path to the pro.obo file")
	private String proOboFile;

	/**
	 * @return the proDictionaryDirectory
	 */
	public File getProDictionaryDirectory() {
		return new File(proDictionaryDirectory);
	}

	/**
	 * @return the proOboFile
	 */
	public File getProOboFile() {
		return new File(proOboFile);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProteinExtractionPipelineCmdOpts [proDictionaryDirectory=" + proDictionaryDirectory + ", proOboFile="
				+ proOboFile + ", getNumToSkip()=" + getNumToSkip() + ", getNumToProcess()=" + getNumToProcess()
				+ ", getBatchNumber()=" + getBatchNumber() + ", getInputFileOrDirectoryPath()="
				+ getInputFileOrDirectoryPath() + ", getOutputFileOrDirectoryPath()=" + getOutputFileOrDirectoryPath()
				+ ", requirePresenceOfAbstract()=" + requirePresenceOfAbstract() + "]";
	}

	
	
}
