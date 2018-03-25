package edu.ucdenver.ccp.nlp.pipelines.runner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.nlp.doc2txt.pmc.PmcDocumentConverterAE;
import edu.ucdenver.ccp.nlp.doc2txt.pmc.PmcMetadataImportAE;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runner.DeploymentParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializerAE;
import edu.ucdenver.ccp.nlp.uima.serialization.txt.DocumentMetaDataSerializerAE;
import edu.ucdenver.ccp.nlp.uima.serialization.txt.DocumentTextSerializerAE;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.View;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class PmcNxml2TxtPipeline extends PipelineBase {

	public static final Class<? extends DocumentMetadataHandler> DOCTXTSERIALIZER_DOCUMENT_METADATAHANDLER_CLASS = CcpDocumentMetadataHandler.class;
	public static final String DOCTXTSERIALIZER_SOURCE_VIEW_NAME = View.XML.viewName();
	public static final boolean DOCTXTSERIALIZER_COMPRESS_OUTPUT_FLAG = true;
	public static final String DOCTXTSERIALIZER_OUTPUT_FILE_SUFFIX = ".txt";

	public static final Class<? extends DocumentMetadataHandler> ANNOTSERIALIZER_DOCUMENT_METADATAHANDLER_CLASS = CcpDocumentMetadataHandler.class;
	public static final String ANNOTSERIALIZER_SOURCE_VIEW_NAME = View.DEFAULT.viewName();
	public static final boolean ANNOTSERIALIZER_COMPRESS_OUTPUT_FLAG = true;
	public static final String ANNOTSERIALIZER_OUTPUT_FILE_INFIX = "sections";

	private static final Logger logger = Logger.getLogger(PmcNxml2TxtPipeline.class);
	private static final String AGGREGATE_DESCRIPTOR_PATH_ON_CLASSPATH = "/pipeline_descriptors/pmc_nxml2txt_aggregate.xml";

	public PmcNxml2TxtPipeline(File catalogDirectory, File configDir, int numToProcess, String brokerUrl,
			int casPoolSize) throws Exception {
		/*
		 * this pipeline processes the native .nxml files, so we read in the
		 * FileVersion.SOURCE version
		 */
		super(new PipelineParams(new PMC_OA_DocumentCollection().getShortname(), FileVersion.SOURCE,
				CharacterEncoding.UTF_8, View.XML.viewName(), "sections",
				"Generate plain text versions of each PMC nxml document in the corpus. Serialize the plain text "
						+ "to file as well as an annotation file containing document section boundaries.",
				catalogDirectory, numToProcess, 0, brokerUrl, casPoolSize), configDir);
	}

	/**
	 * @return the path to the aggregate descriptor on the classpath
	 */
	@Override
	protected String getAggregateDescriptorPath() {
		return AGGREGATE_DESCRIPTOR_PATH_ON_CLASSPATH;
	}

	@Override
	public TypeSystemDescription getPipelineTypeSystem() {
		return TypeSystemUtil.getCcpTypeSystem();
	}

	@Override
	protected DeploymentParams getPipelineDeploymentParams() {
		String serviceName = "PMC_NXML2TXT";
		String serviceDescription = "Converts the PMC NXML file format into a plain text version. "
				+ "Outputs annotations for document headings and sections.";
		String endpoint = "nxml2txt_pipelineQ";
		int scaleup = 1;
		int errorThresholdCount = 1;

		return new DeploymentParams(serviceName, serviceDescription, scaleup, errorThresholdCount, endpoint,
				getPipelineParams().getBrokerUrl());
	}

	@Override
	public List<ServiceEngine> createServiceEngines() throws ResourceInitializationException {
		List<ServiceEngine> engines = new ArrayList<ServiceEngine>();
		int casPoolSize = getPipelineParams().getCasPoolSize();
		{
			/* configure the XML2TXT AE */
			AnalysisEngineDescription xml2txtAeDesc = PmcDocumentConverterAE.getDescription(getPipelineTypeSystem(),
					CharacterEncoding.UTF_8, View.XML.viewName());

			int xml2txt_scaleup = casPoolSize;
			int xml2txt_errorThreshold = 0;
			String xml2txt_endpoint = "nxml2txtQ";

			DeploymentParams xml2txtDeployParams = new DeploymentParams("NXML2TXT", "Converts PMC NXML to plain text.",
					xml2txt_scaleup, xml2txt_errorThreshold, xml2txt_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine xml2txtEngine = new ServiceEngine(xml2txtAeDesc, xml2txtDeployParams, "xml2txtAE",
					DescriptorType.PRIMITIVE);
			engines.add(xml2txtEngine);
		}
		{
			/* configure the metadata import AE */
			AnalysisEngineDescription pmcMetadataImportAeDesc = PmcMetadataImportAE.getDescription(getPipelineTypeSystem(),
					CharacterEncoding.UTF_8, View.XML.viewName());

			int metaImport_scaleup = casPoolSize;
			int metaImport_errorThreshold = 0;
			String metaImport_endpoint = "pmcMetadataImportQ";

			DeploymentParams metaImportDeployParams = new DeploymentParams("PMCMETAIMPORT", "Imports PMC metadata from XML to CAS.",
					metaImport_scaleup, metaImport_errorThreshold, metaImport_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine pmcMetaImportEngine = new ServiceEngine(pmcMetadataImportAeDesc, metaImportDeployParams, "pmcMetaImportAE",
					DescriptorType.PRIMITIVE);
			engines.add(pmcMetaImportEngine);
		}
		{
			/* configure the plain text file output AE */

			AnalysisEngineDescription txtSerializerAeDesc = DocumentTextSerializerAE
					.getDescription_SaveToSourceFileDirectory(getPipelineTypeSystem(),
							DOCTXTSERIALIZER_DOCUMENT_METADATAHANDLER_CLASS, DOCTXTSERIALIZER_SOURCE_VIEW_NAME,
							View.DEFAULT.viewName(), DOCTXTSERIALIZER_COMPRESS_OUTPUT_FLAG,
							DOCTXTSERIALIZER_OUTPUT_FILE_SUFFIX);

			int txtSerializer_scaleup = casPoolSize;
			int txtSerializer_errorThreshold = 0;
			String txtSerializer_endpoint = "txtSerializerQ";

			DeploymentParams txtSerializerDeployParams = new DeploymentParams("TXTSerializer",
					"Serializes the document text to a file.", txtSerializer_scaleup, txtSerializer_errorThreshold,
					txtSerializer_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine txtSerializerEngine = new ServiceEngine(txtSerializerAeDesc, txtSerializerDeployParams,
					"docTxtSerializerAE", DescriptorType.PRIMITIVE);
			engines.add(txtSerializerEngine);
		}
		{
			/* configure the document metadata file output AE */

			AnalysisEngineDescription docMetaDataSerializerAeDesc = DocumentMetaDataSerializerAE
					.getDescription_SaveToSourceFileDirectory(getPipelineTypeSystem(),
							DOCTXTSERIALIZER_DOCUMENT_METADATAHANDLER_CLASS, DOCTXTSERIALIZER_SOURCE_VIEW_NAME,
							View.DEFAULT.viewName(), DOCTXTSERIALIZER_COMPRESS_OUTPUT_FLAG);

			int metadataSerializer_scaleup = casPoolSize;
			int metadataSerializer_errorThreshold = 0;
			String metadataSerializer_endpoint = "metadataSerializerQ";

			DeploymentParams metadataSerializerDeployParams = new DeploymentParams("METADATASerializer",
					"Serializes the document metadata to a file.", metadataSerializer_scaleup, metadataSerializer_errorThreshold,
					metadataSerializer_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine txtSerializerEngine = new ServiceEngine(docMetaDataSerializerAeDesc, metadataSerializerDeployParams,
					"docMetadataSerializerAE", DescriptorType.PRIMITIVE);
			engines.add(txtSerializerEngine);
		}
		{
			/* serialize the section annotations */
			String sourceViewName = ANNOTSERIALIZER_SOURCE_VIEW_NAME;
			String outputViewName = View.DEFAULT.viewName();
			boolean compressOutput = ANNOTSERIALIZER_COMPRESS_OUTPUT_FLAG;
			String outputFileInfix = ANNOTSERIALIZER_OUTPUT_FILE_INFIX;
			AnalysisEngineDescription annotSerializerDesc = AnnotationSerializerAE
					.getDescription_SaveToSourceFileDirectory(getPipelineTypeSystem(),
							ANNOTSERIALIZER_DOCUMENT_METADATAHANDLER_CLASS, sourceViewName, outputViewName,
							compressOutput, outputFileInfix, IncludeCoveredText.NO);

			int annotSerializer_scaleup = casPoolSize;
			int annotSerializer_errorThreshold = 0;
			String annotSerializer_endpoint = "annotSerializerQ";

			DeploymentParams annotSerializerDeployParams = new DeploymentParams("AnnotSerializer",
					"Serializes the annotations to file.", annotSerializer_scaleup, annotSerializer_errorThreshold,
					annotSerializer_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine annotSerializerEngine = new ServiceEngine(annotSerializerDesc, annotSerializerDeployParams,
					"annotSerializerAE", DescriptorType.PRIMITIVE);
			engines.add(annotSerializerEngine);
		}
		
		// {
		// /* configure catalog AE */
		// AnalysisEngineDescription catalogAeDesc =
		// RunCatalogAE.getDescription(getPipelineTypeSystem(),
		// getPipelineParams().getCatalogDirectory(),
		// CcpDocumentMetadataHandler.class,
		// getPipelineParams().getPipelineKey());
		//
		// int catalogAe_scaleup = 1;
		// int catalogAe_errorThreshold = 0;
		// String catalogAe_endpoint = "catalogAeQ";
		//
		// DeploymentParams catalogAeDeployParams = new
		// DeploymentParams("RunCatalog",
		// "Catalogs new annotation-output and document files.",
		// catalogAe_scaleup, catalogAe_errorThreshold,
		// catalogAe_endpoint, getPipelineParams().getBrokerUrl());
		// ServiceEngine catalogAeEngine = new ServiceEngine(catalogAeDesc,
		// catalogAeDeployParams, "runCatalogAE",
		// DescriptorType.PRIMITIVE);
		// engines.add(catalogAeEngine);
		// }
		return engines;

	}

	/**
	 * @param args
	 *            args[0] = catalog directory <br>
	 *            args[1] = config directory (a work directory where UIMA
	 *            descriptor files will be written)<br>
	 *            args[2] = broker url<br>
	 *            args[3] = num to process <br>
	 *            args[4] = cas pool size
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setAdditivity(false);
		File catalogDirectory = new File(args[0]);
		File configDirectory = new File(args[1]);
		String brokerUrl = args[2];
		int numToProcess = Integer.parseInt(args[3]); // <0 = process all
		int casPoolSize = Integer.parseInt(args[4]);
		logger.info("Starting PmcNxml2TxtPipeline...\nCatalog directory=" + catalogDirectory.getAbsolutePath()
				+ "\nConfig directory=" + configDirectory.getAbsolutePath() + "\nNum-to-process=" + numToProcess
				+ "\nBroker URL: " + brokerUrl);

		try {
			PmcNxml2TxtPipeline pipeline = new PmcNxml2TxtPipeline(catalogDirectory, configDirectory, numToProcess,
					brokerUrl, casPoolSize);
			pipeline.configurePipeline();

			/* turn on debugging mode */
			pipeline.setDebugFlag(true);

			logger.info("Deploying pipeline components...");
			pipeline.deployPipeline();
			logger.info("Running pipeline...");
			pipeline.runPipeline();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
