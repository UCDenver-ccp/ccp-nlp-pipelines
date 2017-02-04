package edu.ucdenver.ccp.nlp.pipelines.runner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.flow.FlowControllerDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.nlp.doc2txt.pmc.PmcDocumentConverterAE;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runner.DeploymentParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineKey;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.RunCatalogAE;
import edu.ucdenver.ccp.nlp.uima.serialization.txt.DocumentTextSerializerAE;
import edu.ucdenver.ccp.nlp.uima.serialization.xmi.XmiPrinterAE;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.View;

public class PmcNxml2TxtPipeline extends PipelineBase {

	private static final Logger logger = Logger.getLogger(PmcNxml2TxtPipeline.class);

	public PmcNxml2TxtPipeline(File catalogDirectory, File configDir, int numToProcess, String brokerUrl)
			throws Exception {
		/*
		 * this pipeline processes the native .nxml files, so we read in the
		 * FileVersion.SOURCE version
		 */
		super(new PipelineParams(new PMC_OA_DocumentCollection().getShortname(), FileVersion.SOURCE,
				CharacterEncoding.UTF_8, View.XML.viewName(), PipelineKey.XML2TXT, "pipeline description",
				catalogDirectory, numToProcess, 0, brokerUrl), configDir);
	}

	@Override
	protected AnalysisEngineDescription getPipelineDescription() throws ResourceInitializationException {
		List<AnalysisEngineDescription> aeDescList = new ArrayList<AnalysisEngineDescription>();
		List<String> componentNames = new ArrayList<String>();
		int index = 0;
		for (ServiceEngine se : getServiceEngines()) {
			AnalysisEngineDescription aeDescription = se.getAeDescription();
			aeDescList.add(aeDescription);
			componentNames.add(aeDescription.getAnnotatorImplementationName() + "-" + index++);
		}

		FlowControllerDescription flowControllerDescription = getFlowControllerDescription(
				componentNames.toArray(new String[componentNames.size()]));
		return AnalysisEngineFactory.createEngineDescription(flowControllerDescription,
				aeDescList.toArray(new AnalysisEngineDescription[aeDescList.size()]));

	}

	@Override
	protected TypeSystemDescription getPipelineTypeSystem() {
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
	protected List<ServiceEngine> getServiceEngines() throws ResourceInitializationException {
		List<ServiceEngine> engines = new ArrayList<ServiceEngine>();
		{
			/* configure the XML2TXT AE */
			AnalysisEngineDescription xml2txtAeDesc = PmcDocumentConverterAE.getDescription(getPipelineTypeSystem(),
					CharacterEncoding.UTF_8, View.XML.viewName());

			int xml2txt_scaleup = 1;
			int xml2txt_errorThreshold = 0;
			String xml2txt_endpoint = "nxml2txtQ";

			DeploymentParams xml2txtDeployParams = new DeploymentParams("NXML2TXT", "Converts PMC NXML to plain text.",
					xml2txt_scaleup, xml2txt_errorThreshold, xml2txt_endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine xml2txtEngine = new ServiceEngine(xml2txtAeDesc, xml2txtDeployParams, "xml2txtAE",
					DescriptorType.PRIMITIVE);
			engines.add(xml2txtEngine);
		}
		{
			/* configure the plain text file output AE */
			AnalysisEngineDescription txtSerializerAeDesc = DocumentTextSerializerAE
					.getDescription_SaveToSourceFileDirectory(getPipelineTypeSystem(), CcpDocumentMetadataHandler.class,
							View.XML.viewName(), View.DEFAULT.viewName(), true, ".txt");

			int txtSerializer_scaleup = 1;
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
			/* configure the XMI-output AE */
			AnalysisEngineDescription xmiPrinterAeDesc = XmiPrinterAE.getDescription_SaveToSourceFileDirectory(
					getPipelineTypeSystem(), CcpDocumentMetadataHandler.class, "sections", true);

			int xmiPrinter_scaleup = 1;
			int xmiPrinter_errorThreshold = 0;
			String xmiPrinter_endpoint = "xmiPrinterQ";

			DeploymentParams xmiPrinterDeployParams = new DeploymentParams("XMIPrinter", "Serializes the CAS to XMI.",
					xmiPrinter_scaleup, xmiPrinter_errorThreshold, xmiPrinter_endpoint,
					getPipelineParams().getBrokerUrl());
			ServiceEngine xmiPrinterEngine = new ServiceEngine(xmiPrinterAeDesc, xmiPrinterDeployParams, "xmiPrinterAE",
					DescriptorType.PRIMITIVE);
			engines.add(xmiPrinterEngine);
		}
		{
			/* configure catalog AE */
			AnalysisEngineDescription catalogAeDesc = RunCatalogAE.getDescription(getPipelineTypeSystem(),
					getPipelineParams().getCatalogDirectory(), CcpDocumentMetadataHandler.class);

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
		File catalogDirectory = new File(args[0]);
		File configDirectory = new File(args[1]);
		String brokerUrl = args[2];
		int numToProcess = 1; // <0 = process all
		logger.info("Starting PmcNxml2TxtPipeline...\nCatalog directory=" + catalogDirectory.getAbsolutePath()
				+ "\nConfig directory=" + configDirectory.getAbsolutePath() + "\nNum-to-process=" + numToProcess
				+ "\nBroker URL: " + brokerUrl);
		try {
			PmcNxml2TxtPipeline pipeline = new PmcNxml2TxtPipeline(catalogDirectory, configDirectory, numToProcess,
					brokerUrl);
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
