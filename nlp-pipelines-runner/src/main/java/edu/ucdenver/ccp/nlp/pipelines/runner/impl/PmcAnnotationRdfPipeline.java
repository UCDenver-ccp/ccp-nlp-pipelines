package edu.ucdenver.ccp.nlp.pipelines.runner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.datasource.rdfizer.rdf.ice.RdfUtil.RdfFormat;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runner.DeploymentParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineKey;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.RunCatalogAE;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationDeserializerAE;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeAnnotator;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.AnnotationSerializer.IncludeCoveredText;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa.PmcOaDocumentRdfGenerator;
import edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa.PmcOaDocumentSectionUriFactory;
import edu.ucdenver.ccp.nlp.uima.annotators.sentence_detection.CcpSentenceCasInserter;
import edu.ucdenver.ccp.nlp.uima.annotators.sentence_detection.OpenNlpSentenceDetectorAE;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.AnnotationRdfGenerator;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.DocumentRdfGenerator;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.RdfSerialization_AE;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.UriFactory;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.webannot.WebAnnotationRdfGenerator.TextPositionWebAnnotationRdfGenerator;
import edu.ucdenver.ccp.nlp.uima.shims.annotation.impl.CcpAnnotationDataExtractor;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.View;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

/**
 * Creates stand-off annotation files for the annotations in the PMC document
 * collection
 */
public class PmcAnnotationRdfPipeline extends PipelineBase {

	private static final Logger logger = Logger.getLogger(PmcAnnotationRdfPipeline.class);
	private static final String AGGREGATE_DESCRIPTOR_PATH_ON_CLASSPATH = "/pipeline_descriptors/pmc_annot_rdf_aggregate.xml";
	private static final String PIPELINE_DESCRIPTION = "Serializes to RDF the annotations in the PMC document collection.";
	private final String[] annotFileInfixes;

	public PmcAnnotationRdfPipeline(File catalogDirectory, File configDir, int numToProcess, String brokerUrl,
			int casPoolSize, String... annotFileInfixes) throws Exception {
		super(new PipelineParams(new PMC_OA_DocumentCollection().getShortname(), FileVersion.LOCAL_TEXT,
				CharacterEncoding.UTF_8, View.DEFAULT.viewName(), PipelineKey.ANNOT_SERIALIZATION.name(),
				PIPELINE_DESCRIPTION, catalogDirectory, numToProcess, 0, brokerUrl, casPoolSize), configDir);
		this.annotFileInfixes = annotFileInfixes;
	}

	/**
	 * @return the path to the aggregate descriptor on the classpath
	 */
	@Override
	protected String getAggregateDescriptorPath() {
		return AGGREGATE_DESCRIPTOR_PATH_ON_CLASSPATH;
	}

	@Override
	protected TypeSystemDescription getPipelineTypeSystem() {
		return TypeSystemUtil.getCcpTypeSystem();
	}

	@Override
	protected DeploymentParams getPipelineDeploymentParams() {
		String serviceName = "PMC_ANNOT_RDF";
		String endpoint = "pmc_annot_rdf_pipelineQ";
		int scaleup = 1;
		int errorThresholdCount = 1;

		return new DeploymentParams(serviceName, PIPELINE_DESCRIPTION, scaleup, errorThresholdCount, endpoint,
				getPipelineParams().getBrokerUrl());
	}

	@Override
	protected List<ServiceEngine> createServiceEngines() throws ResourceInitializationException {
		List<ServiceEngine> engines = new ArrayList<ServiceEngine>();

		int casPoolSize = getPipelineParams().getCasPoolSize();
		{
			/* configure the sentence detector AE */
			boolean treatLineBreaksAsSentenceBoundaries = true;
			AnalysisEngineDescription sentenceDetecterDesc = OpenNlpSentenceDetectorAE.createAnalysisEngineDescription(
					getPipelineTypeSystem(), CcpSentenceCasInserter.class, treatLineBreaksAsSentenceBoundaries);

			int sentdetect_scaleup = casPoolSize;
			int sentdetect_errorThreshold = 0;
			String sentdetect_endpoint = "sentdetectQ";

			DeploymentParams sentdetectDeployParams = new DeploymentParams("sentdetect",
					"Adds sentence annotations to the CAS.", sentdetect_scaleup, sentdetect_errorThreshold,
					sentdetect_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine xml2txtEngine = new ServiceEngine(sentenceDetecterDesc, sentdetectDeployParams,
					"sentDetectAE", DescriptorType.PRIMITIVE);
			engines.add(xml2txtEngine);
		}
		{
			/* load the sections and all concept mapper annotations */
			AnalysisEngineDescription annotLoaderEngineDesc = AnnotationDeserializerAE
					.getDescription_LoadFromSourceFileDirectory(getPipelineTypeSystem(),
							CcpDocumentMetadataHandler.class, View.DEFAULT.viewName(), View.DEFAULT.viewName(),
							IncludeCoveredText.NO, IncludeAnnotator.YES, annotFileInfixes);

			int annotLoader_scaleup = casPoolSize;
			int annotLoader_errorThreshold = 0;
			String annotLoader_endpoint = "annotLoaderQ";

			DeploymentParams annotLoaderDeployParams = new DeploymentParams("AnnotationLoader",
					"Loads serialized annotation files into the CAS.", annotLoader_scaleup, annotLoader_errorThreshold,
					annotLoader_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine annotLoaderEngine = new ServiceEngine(annotLoaderEngineDesc, annotLoaderDeployParams,
					"annotLoaderAE", DescriptorType.PRIMITIVE);
			engines.add(annotLoaderEngine);
		}
		{
			/* serialize the annotations to RDF */
			String sourceViewName = View.DEFAULT.viewName();
			String outputViewName = View.DEFAULT.viewName();
			boolean compressOutput = true;
			String outputFileInfix = getPipelineParams().getPipelineKey();
			RdfFormat format = RdfFormat.NTRIPLES;
			Class<? extends DocumentMetadataHandler> documentMetaDataExtractorClass = CcpDocumentMetadataHandler.class;
			Class<? extends AnnotationDataExtractor> annotationDataExtractorClass = CcpAnnotationDataExtractor.class;
			Class<? extends AnnotationRdfGenerator> annotationRdfGeneratorClass = TextPositionWebAnnotationRdfGenerator.class;
			Class<? extends DocumentRdfGenerator> documentRdfGeneratorClass = PmcOaDocumentRdfGenerator.class;
			Class<? extends UriFactory> uriFactoryClass = PmcOaDocumentSectionUriFactory.class;

			AnalysisEngineDescription annotSerializerDesc = RdfSerialization_AE
					.createDescription_SaveToSourceFileDirectory(getPipelineTypeSystem(), outputFileInfix, format,
							compressOutput, documentMetaDataExtractorClass, annotationDataExtractorClass,
							annotationRdfGeneratorClass, documentRdfGeneratorClass, uriFactoryClass, sourceViewName,
							outputViewName);

			int annotSerializer_scaleup = (casPoolSize > 1) ? casPoolSize / 2 : 1;
			int annotSerializer_errorThreshold = 0;
			String annotSerializer_endpoint = "annotRdfSerializerQ";

			DeploymentParams annotSerializerDeployParams = new DeploymentParams("AnnotRDFSerializer",
					"Serializes the annotations to RDF.", annotSerializer_scaleup, annotSerializer_errorThreshold,
					annotSerializer_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine annotSerializerEngine = new ServiceEngine(annotSerializerDesc, annotSerializerDeployParams,
					"annotRDFSerializerAE", DescriptorType.PRIMITIVE);
			engines.add(annotSerializerEngine);
		}
		{
			/* configure catalog AE */
			AnalysisEngineDescription catalogAeDesc = RunCatalogAE.getDescription(getPipelineTypeSystem(),
					getPipelineParams().getCatalogDirectory(), CcpDocumentMetadataHandler.class,
					getPipelineParams().getPipelineKey());

			int catalogAe_scaleup = 1;
			int catalogAe_errorThreshold = 0;
			String catalogAe_endpoint = "catalogAeQ";

			DeploymentParams catalogAeDeployParams = new DeploymentParams("RunCatalog",
					"Catalogs new annotation-output and document files.", catalogAe_scaleup, catalogAe_errorThreshold,
					catalogAe_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine catalogAeEngine = new ServiceEngine(catalogAeDesc, catalogAeDeployParams, "runCatalogAE",
					DescriptorType.PRIMITIVE);
			engines.add(catalogAeEngine);
		}
		return engines;
	}

	/**
	 * @param args
	 *            args[0] = catalog directory <br>
	 *            args[1] = config directory (a work directory where UIMA
	 *            descriptor files will be written)
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setAdditivity(false);
		File catalogDirectory = new File(args[0]);
		File configDirectory = new File(args[1]);
		String brokerUrl = args[2];
		int numToProcess = -1; // <0 = process all
		int casPoolSize = Integer.parseInt(args[3]);
		String[] annotFileInfixes = Arrays.copyOfRange(args, 4, args.length);
		logger.info(
				"Starting PMC Annot Serialization Pipeline...\nCatalog directory=" + catalogDirectory.getAbsolutePath()
						+ "\nConfig directory=" + configDirectory.getAbsolutePath() + "\nNum-to-process=" + numToProcess
						+ "\nBroker URL: " + brokerUrl + "\nAnnot File Infixes: " + Arrays.toString(annotFileInfixes));
		try {
			PmcAnnotationRdfPipeline pipeline = new PmcAnnotationRdfPipeline(catalogDirectory, configDirectory,
					numToProcess, brokerUrl, casPoolSize, annotFileInfixes);

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
