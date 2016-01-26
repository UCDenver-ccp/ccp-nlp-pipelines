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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileArchiveUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.craft.CraftConceptType;
import edu.ucdenver.ccp.craft.CraftOntology;
import edu.ucdenver.ccp.craft.CraftRelease;
import edu.ucdenver.ccp.nlp.pipelines.evaluation.GenericAnnotationLoader;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.MentionComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.comparison.AnnotationComparator_AE.SpanComparatorType;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.OntologyClassRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.uima.annotators.filter.SlotRemovalFilter_AE.SlotRemovalOption;
import edu.ucdenver.ccp.nlp.uima.shims.annotation.impl.CcpAnnotationDataExtractor;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;

public class CraftRunGenericComparison {
	private static final CraftRelease CRAFT_VERSION = CraftRelease.MAIN;
	private static final TypeSystemDescription tsd = TypeSystemUtil.getCcpTypeSystem();
	
	private static final Logger logger = Logger.getLogger(CraftRunGenericComparison.class);
	
	public enum GoAnnotationFilterOp {
		REMOVE_GO_BPMF,
		REMOVE_GO_CCMF,
		REMOVE_GO_CCBP,
		NONE
	}
	
	// Variables to remove GO nodes
	public static final String GO_BP_ROOT_ID = "GO:0008150";
	public static final String GO_MF_ROOT_ID = "GO:0003674";
	public static final String GO_CC_ROOT_ID = "GO:0005575";

	/**
	 * @param tsd
	 * @param spanComparatorType
	 * @param conceptTypesToLoad
	 * @param inputDir
	 * @param outputFile
	 * @param paramIndex
	 * @throws UIMAException
	 * @throws IOException
	 */
	private static void runMetaMapEvaluationAgainstCraft(TypeSystemDescription tsd, SpanComparatorType 
			spanComparatorType,  EnumSet<CraftConceptType> conceptTypesToLoad,
			String inputDir, File outputFile, int cutoff) throws UIMAException, IOException {
		
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		
		/*
		 * Collect regular expressions used to identify concepts for the specified
		 * CraftConceptTypes. For example, CHEBI:\\d+ is used to identify terms from the CHEBI
		 * ontology
		 */
		for (CraftConceptType conceptType : conceptTypesToLoad) {
			annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		}
		
		// Adding annotations from the directory
		AnalysisEngineDescription genericAnnotations = null;
		if(cutoff == -1) {
			genericAnnotations= GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir);
		} else {
			genericAnnotations = GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir, cutoff);
		}
		
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, conceptTypesToLoad, tsd,
				spanComparatorType, MentionComparatorType.IDENTICAL, annotationTypeRegexes);
		
		evalPipeline.addPipelineComponent(genericAnnotations);
		if (outputFile != null) {
			evalPipeline.setEvalResultsOutputFile(outputFile);
		}
		
		/* Removes all SLOT MENTIONS */
		AnalysisEngineDescription removeSlot = SlotRemovalFilter_AE.getDescription(tsd, SlotRemovalOption.REMOVE_ALL);
		
		evalPipeline.addPipelineComponent(removeSlot);
		evalPipeline.run(SlotRemovalOption.REMOVE_ALL);
		
	}
	
	/**
	 * @param tsd
	 * @param spanComparatorType
	 * @param conceptTypesToLoad
	 * @param inputDir
	 * @param outputFile
	 * @param paramIndex
	 * @param annotationFilterOp
	 * @throws UIMAException
	 * @throws IOException
	 */
	private static void runMetaMapEvaluationAgainstCraft(TypeSystemDescription tsd, SpanComparatorType 
			spanComparatorType,  EnumSet<CraftConceptType> conceptTypesToLoad,
			String inputDir, File outputFile, int cutoff,
			GoAnnotationFilterOp filter) throws UIMAException, IOException {
		
		Collection<String> annotationTypeRegexes = new ArrayList<String>();
		
		/*
		 * Collect regular expressions used to identify concepts for the specified
		 * CraftConceptTypes. For example, CHEBI:\\d+ is used to identify terms from the CHEBI
		 * ontology
		 */
		for (CraftConceptType conceptType : conceptTypesToLoad) {
			annotationTypeRegexes.addAll(conceptType.conceptTypeRegexes());
		}
		
		// Adding MetaMap annotations
		// Adding annotations from the directory
		AnalysisEngineDescription genericAnnotations = null;
		if(cutoff == -1) {
			genericAnnotations= GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir);
		} else {
			genericAnnotations = GenericAnnotationLoader.createAnalysisEngineDescription(tsd, inputDir, cutoff);
		}
		
		CraftEvaluationPipeline evalPipeline = new CraftEvaluationPipeline(CRAFT_VERSION, conceptTypesToLoad, tsd,
				spanComparatorType, MentionComparatorType.IDENTICAL, annotationTypeRegexes);
		
		evalPipeline.addPipelineComponent(genericAnnotations);
		evalPipeline.addPipelineComponents(getFilterAeDescription(filter));
		
		if (outputFile != null) {
			evalPipeline.setEvalResultsOutputFile(outputFile);
		}
		
		/* Removes all SLOT MENTIONS */
		AnalysisEngineDescription removeSlot = SlotRemovalFilter_AE.getDescription(tsd, SlotRemovalOption.REMOVE_ALL);
		
		evalPipeline.addPipelineComponent(removeSlot);
		evalPipeline.run(SlotRemovalOption.REMOVE_ALL);
	}
	
	/**
	 * @param annotFilterOp
	 * @return an {@link OntologyClassRemovalFilter_AE} initialized based on the input
	 *         {@link GoAnnotationFilterOp}
	 * @throws ResourceInitializationException
	 */
	private static List<AnalysisEngineDescription> getFilterAeDescription(GoAnnotationFilterOp annotFilterOp)
			throws ResourceInitializationException {
		List<AnalysisEngineDescription> descList = new ArrayList<AnalysisEngineDescription>();
		File oboFile = getGoOboFileReference();
		switch (annotFilterOp) {
		case REMOVE_GO_BPMF:
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_BP_ROOT_ID, oboFile));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_MF_ROOT_ID, oboFile));
			return descList;
		case REMOVE_GO_CCBP:
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_CC_ROOT_ID, oboFile));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_BP_ROOT_ID, oboFile));
			return descList;
		case REMOVE_GO_CCMF:
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_CC_ROOT_ID, oboFile));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_MF_ROOT_ID, oboFile));
			return descList;
		case NONE:
			return descList;

		default:
			throw new IllegalArgumentException("Unknown filter Op: " + annotFilterOp.name());
		}
	}
	
	/**
	 * @return a reference to the GO obo file. It will be copied from the classpath to a temporary
	 *         file.
	 */
	private static File getGoOboFileReference() {
		try {
			File oboFile = File.createTempFile("goOboFile", "obo.gz");
			ClassPathUtil.copyClasspathResourceToFile(CraftOntology.GO.oboFilePath(), oboFile);
			return FileArchiveUtil.unzip(oboFile, oboFile.getParentFile(), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param args
	 * 		args[0] - ontology (one of: GO_CC, GO_BP, GO_MF, CL, NCBI_TAXON, SO, CHEBI, PR, EG)
	 * 		args[1] - input annotation directory
	 * 		args[2] - output comparison file
	 * 		args[3] - span comparitor type (one of: STRICT, OVERLAP, SHARED_START, SHARED_END,
	 *            SHARED_START_OR_END, IGNORE_SPAN, SUB_SPAN)
	 *		args[4] - OPTIONAL: if annotations come with confidence scores can specify a cutoff, where only
	 *			  annotations with scores >= cutoff provided are used     
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws UIMAException, IOException {
		long time = System.currentTimeMillis();
		BasicConfigurator.configure();
		
		TypeSystemDescription tsd = TypeSystemUtil.getCcpTypeSystem();
		
		String ontology = args[0];
		String inputAnnotationPath = args[1];
		String outputResultsPath = args[2];
		
		SpanComparatorType spanComparatorType = SpanComparatorType.valueOf(args[3]);	

		int cutoff = -1;
		if(args.length == 5) {
			cutoff = Integer.parseInt(args[4]);
		}
		
		System.out.println("Annotation Directory: " + inputAnnotationPath);
		System.out.println("Output Result Directory: " + outputResultsPath);
		
		File outputResultsDir = new File(outputResultsPath);
		
		// Giving correct ontology
		if(ontology.equals("GO_CC")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.GOCC), 
					inputAnnotationPath, outputResultsDir, cutoff, GoAnnotationFilterOp.REMOVE_GO_BPMF);
		} else if(ontology.equals("GO_MF")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.GOMF), 
					inputAnnotationPath, outputResultsDir, cutoff, GoAnnotationFilterOp.REMOVE_GO_CCBP);
		} else if(ontology.equals("GO_BP")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.GOBP), 
					inputAnnotationPath, outputResultsDir, cutoff, GoAnnotationFilterOp.REMOVE_GO_CCMF);
		} else if (ontology.equals("CHEBI")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.CHEBI), 
					inputAnnotationPath, outputResultsDir, cutoff);
		} else if (ontology.equals("SO")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.SO), 
					inputAnnotationPath, outputResultsDir, cutoff);
		} else if (ontology.equals("CL")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.CL), 
					inputAnnotationPath, outputResultsDir, cutoff);
		} else if (ontology.equals("NCBITAXON")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.NCBITAXON), 
					inputAnnotationPath, outputResultsDir, cutoff);
		} else if (ontology.equals("PR")) {
			runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.PR), 
					inputAnnotationPath, outputResultsDir, cutoff);
		} else if (ontology.equals("EG")) {
				runMetaMapEvaluationAgainstCraft(tsd, spanComparatorType, EnumSet.of(CraftConceptType.EG), 
						inputAnnotationPath, outputResultsDir, cutoff);
		} else {
			throw new IllegalArgumentException("Ontology: " + ontology
					+ " is not a valid input argument. It is not annotated in CRAFT, use one of GO_CC, GO_BP, GO_MF, CL, NCBITAXON, SO, CHEBI, PR, EG.");			
		}
		
		logger.info("Run time = " + ((System.currentTimeMillis() - time) / 1000) + "s");
	}

}
	