package edu.ucdenver.ccp.nlp.pipelines;

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

import org.kohsuke.args4j.Option;

public class PipelineCmdOptsBase {

	@Option(name = "-i", usage = "the path to the input file or directory", required = true)
	private String inputFileOrDirectoryPath;

	@Option(name = "-o", usage = "the path to the output file or directory", required = true)
	private String outputFileOrDirectoryPath;

	@Option(name = "-s", usage = "the number of files to skip before processing starts")
	private int numToSkip;

	@Option(name = "-n", usage = "the number of files to process")
	private int numToProcess;

	@Option(name = "-b", usage = "the batch number (used in the output file name)", required = true)
	private int batchNumber;

	@Option(name = "-r", usage = "Used in Medline processing pipelines. If set to true, all Medline records missing abstracts are left un-processed.")
	private boolean requirePresenceOfAbstract = false;

	@Option(name = "-z", usage = "the obo file to use if hierarchical evaluation is being conducted", required = false)
	private File oboFile;
	
	
	public int getNumToSkip() {
		return numToSkip;
	}
	
	public void setNumToSkip(int numToSkip) {
		this.numToSkip = numToSkip;
	}

	public int getNumToProcess() {
		return numToProcess;
	}

	public void setNumToProcess(int numToProcess) {
		this.numToProcess = numToProcess;
	}
	
	/**
	 * @return the batchNumber
	 */
	public int getBatchNumber() {
		return batchNumber;
	}
	
	public void setBatchNumber(int batchNumber) {
		this.batchNumber = batchNumber;
	}

	/**
	 * @return the inputFileOrDirectoryPath
	 */
	public File getInputFileOrDirectoryPath() {
		return new File(inputFileOrDirectoryPath);
	}
	
	public void setInputFileOrDirectoryPath(File inputPath) {
		this.inputFileOrDirectoryPath = inputPath.getAbsolutePath();
	}

	/**
	 * @return the outputFileOrDirectoryPath
	 */
	public File getOutputFileOrDirectoryPath() {
		return new File(outputFileOrDirectoryPath);
	}

	public void setOutputFileOrDirectoryPath(File outputPath) {
		this.outputFileOrDirectoryPath = outputPath.getAbsolutePath();
	}
	
	/**
	 * @return the requirePresenceOfAbstract
	 */
	public boolean requirePresenceOfAbstract() {
		return requirePresenceOfAbstract;
	}
	
	public void setRequirePresenceOfAbstract(boolean requireAbstractPresence) {
		this.requirePresenceOfAbstract = requireAbstractPresence;
	}

	/**
	 * @return the oboFile
	 */
	public File getOboFile() {
		return oboFile;
	}

	/**
	 * @param oboFile the oboFile to set
	 */
	public void setOboFile(File oboFile) {
		this.oboFile = oboFile;
	}

}
