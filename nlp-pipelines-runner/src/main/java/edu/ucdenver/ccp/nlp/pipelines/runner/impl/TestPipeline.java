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
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileVersion;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runner.DeploymentParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineBase;
import edu.ucdenver.ccp.nlp.pipelines.runner.PipelineParams;
import edu.ucdenver.ccp.nlp.pipelines.runner.impl.test.TestAnnotatorA;
import edu.ucdenver.ccp.nlp.pipelines.runner.impl.test.TestAnnotatorB;
import edu.ucdenver.ccp.nlp.pipelines.runner.impl.test.TestAnnotatorC;
import edu.ucdenver.ccp.nlp.pipelines.runner.impl.test.TestAnnotatorD;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.uima.util.View;

public class TestPipeline extends PipelineBase {

	private static final Logger logger = Logger.getLogger(TestPipeline.class);

	public TestPipeline(File catalogDirectory, File configDir, int numToProcess, String brokerUrl, int casPoolSize)
			throws Exception {
		super(new PipelineParams(new PMC_OA_DocumentCollection().getShortname(), FileVersion.SOURCE,
				CharacterEncoding.UTF_8, View.XML.viewName(), "TestPipelineKey", "test pipeline", catalogDirectory,
				numToProcess, 0, brokerUrl, casPoolSize), configDir);
	}

	@Override
	protected DeploymentParams getPipelineDeploymentParams() {
		String serviceName = "TestPipeline";
		String endpoint = "testpipelineQ";
		int scaleup = 1;
		int errorThresholdCount = 1;

		return new DeploymentParams(serviceName, "test pipeline", scaleup, errorThresholdCount, endpoint,
				getPipelineParams().getBrokerUrl());
	}

	@Override
	protected List<ServiceEngine> createServiceEngines() throws ResourceInitializationException {
		List<ServiceEngine> engines = new ArrayList<ServiceEngine>();
		{
			String annotatorName = "annotatorA";
			AnalysisEngineDescription aeDesc = TestAnnotatorA.getDescription();

			int scaleup = 1;
			int errorThreshold = 0;
			String endpoint = annotatorName + "Q";

			DeploymentParams deployParams = new DeploymentParams(annotatorName, annotatorName + " description", scaleup,
					errorThreshold, endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine engine = new ServiceEngine(aeDesc, deployParams, annotatorName, DescriptorType.PRIMITIVE);
			engines.add(engine);
		}
		{
			String annotatorName = "annotatorB";
			AnalysisEngineDescription aeDesc = TestAnnotatorB.getDescription();

			int scaleup = 1;
			int errorThreshold = 0;
			String endpoint = annotatorName + "Q";

			DeploymentParams deployParams = new DeploymentParams(annotatorName, annotatorName + " description", scaleup,
					errorThreshold, endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine engine = new ServiceEngine(aeDesc, deployParams, annotatorName, DescriptorType.PRIMITIVE);
			engines.add(engine);
		}
		{
			String annotatorName = "annotatorC";
			AnalysisEngineDescription aeDesc = TestAnnotatorC.getDescription();

			int scaleup = 3;
			int errorThreshold = 0;
			String endpoint = annotatorName + "Q";

			DeploymentParams deployParams = new DeploymentParams(annotatorName, annotatorName + " description", scaleup,
					errorThreshold, endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine engine = new ServiceEngine(aeDesc, deployParams, annotatorName, DescriptorType.PRIMITIVE);
			engines.add(engine);
		}
		{
			String annotatorName = "annotatorD";
			AnalysisEngineDescription aeDesc = TestAnnotatorD.getDescription();

			int scaleup = 1;
			int errorThreshold = 0;
			String endpoint = annotatorName + "Q";

			DeploymentParams deployParams = new DeploymentParams(annotatorName, annotatorName + " description", scaleup,
					errorThreshold, endpoint, getPipelineParams().getBrokerUrl());
			ServiceEngine engine = new ServiceEngine(aeDesc, deployParams, annotatorName, DescriptorType.PRIMITIVE);
			engines.add(engine);
		}
		return engines;
	}

	@Override
	protected TypeSystemDescription getPipelineTypeSystem() {
		return TypeSystemUtil.getCcpTypeSystem();
	}

	@Override
	protected String getAggregateDescriptorPath() {
		return "/pipeline_descriptors/test_pipeline_aggregate.xml";
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setAdditivity(false);
		File catalogDirectory = new File(args[0]);
		File configDirectory = new File(args[1]);
		String brokerUrl = args[2];
		int numToProcess = 2; // <0 = process all
		int casPoolSize = 10;
		try {
			TestPipeline pipeline = new TestPipeline(catalogDirectory, configDirectory, numToProcess, brokerUrl,
					casPoolSize);

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
