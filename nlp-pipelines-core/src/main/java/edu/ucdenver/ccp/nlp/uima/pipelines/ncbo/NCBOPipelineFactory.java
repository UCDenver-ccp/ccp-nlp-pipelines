/**
 *
 */
package edu.ucdenver.ccp.nlp.uima.pipelines.ncbo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.uima.serialization.xmi.XmiPrinterAE;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.wrapper.ncbo.annotator.NCBOPermutationFactory;

public class NCBOPipelineFactory {
	
	private static final Logger logger = Logger.getLogger(NCBOPipelineFactory.class);
	
	public static List<AnalysisEngineDescription> getPipelineAeDescriptions(TypeSystemDescription tsd,
			int parameterValuesIndex, File outputDir, String [] ontId) throws UIMAException, IOException {
		
		AnalysisEngineDescription NCBOAggregateDesc = NCBOPermutationFactory.
			buildNCBOAggregatePermutation(parameterValuesIndex, tsd, ontId);
		
		AnalysisEngineDescription XmiPrinter = XmiPrinterAE
			.getDescription(tsd, CcpDocumentMetadataHandler.class, outputDir);
		
		return CollectionsUtil.createList(
				NCBOAggregateDesc,
				XmiPrinter); 
	
	}
	
	public static AnalysisEngineDescription getPipelineAeDescriptions(TypeSystemDescription tsd,
			int parameterValuesIndex, String [] ontId) throws UIMAException, IOException {
		
		AnalysisEngineDescription NCBOAggregateDesc = NCBOPermutationFactory.
			buildNCBOAggregatePermutation(parameterValuesIndex, tsd, ontId);
		
		return NCBOAggregateDesc;
	
	}
	
}