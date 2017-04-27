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

import org.apache.log4j.Logger;

public class RunConceptMapperCraftEvals {

	private static final Logger logger = Logger.getLogger(RunConceptMapperCraftEvals.class);
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 577; i++) {
			logger.info("================================");
			logger.info("================================");
			logger.info("================================");
			logger.info("    " + i);
			logger.info("================================");
			logger.info("================================");
			logger.info("================================");
			String[] evalArgs = new String[] {"-i", Integer.toString(i), 
"-t", "UBERON_EXT",
"-m", "UBERON_EXT",
"-d", "/Users/bill/Dropbox/work/projects/craft-uberon-eval-for-mike/work/cm-dict",
"-o", "/Users/bill/Dropbox/work/projects/craft-uberon-eval-for-mike/work/cm-eval-out",
"-p", "cm-uberon_ext-eval",
"-c", "true"};
			CraftConceptMapperEvaluatorMain.main(evalArgs);
		}
		
	}
	
}
