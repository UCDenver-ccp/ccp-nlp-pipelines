package edu.ucdenver.ccp.nlp.pipelines.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.examples.flow.AdvancedFixedFlowController;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.flow.FlowControllerDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resourceSpecifier.factory.DelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory;
import org.apache.uima.resourceSpecifier.factory.ErrorHandlingSettings;
import org.apache.uima.resourceSpecifier.factory.ServiceContext;
import org.apache.uima.resourceSpecifier.factory.UimaASAggregateDeploymentDescriptor;
import org.apache.uima.resourceSpecifier.factory.UimaASPrimitiveDeploymentDescriptor;
import org.apache.uima.resourceSpecifier.factory.impl.ProcessErrorHandlingSettingsImpl;
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl;
import org.xml.sax.SAXException;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.nlp.uima.shims.document.impl.CcpDocumentMetadataHandler;
import edu.ucdenver.ccp.nlp.uima.util.TypeSystemUtil;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public abstract class PipelineBase {

	private static final Logger logger = Logger.getLogger(PipelineBase.class);

	public enum DescriptorType {
		PRIMITIVE, AGGREGATE
	}

	// protected static final String BROKER_URL = "tcp://localhost:61616";

	/**
	 * path to saxon8.jar file (relative to $UIMA_HOME)
	 */
	private static final String SAXON_JAR_PATH = "/saxon/saxon8.jar";

	/**
	 * path to dd2spring.xsl file (relative to $UIMA_HOME)
	 */
	private static final String DD2SPRING_XSL_PATH = "/bin/dd2spring.xsl";

	private static final String UIMA_HOME = "UIMA_HOME";

	@Getter
	private final PipelineParams pipelineParams;

	@Getter
	private final File configDir;

	private List<File> pipelineComponentDeployDescriptorFiles;

	private File pipelineDeploymentDescriptorFile;

	private UimaAsynchronousEngine uimaAsEngine;

	private Set<String> deployedServiceIds;

	private static long pipelineRunStartTime;

	@Setter
	private boolean debugFlag = false;

	@Getter
	private final List<ServiceEngine> serviceEngines;

	/**
	 * Creates an asynchronous analysis engine.
	 * 
	 * @throws Exception
	 */
	public PipelineBase(PipelineParams params, File configDir) throws Exception {
		this.pipelineParams = params;
		this.configDir = configDir;
		this.serviceEngines = createServiceEngines();

		configurePipeline();
	}

	private void configurePipeline()
			throws ResourceInitializationException, FileNotFoundException, IOException, SAXException {
		/*
		 * create deployment descriptor files for all pipeline component
		 * analysis engines
		 */
		pipelineComponentDeployDescriptorFiles = new ArrayList<File>();
		logger.info("Creating descriptor XML for pipeline component analysis engines...");
		List<DelegateConfiguration> delegateConfigurations = new ArrayList<DelegateConfiguration>();
		for (ServiceEngine se : serviceEngines) {
			logger.info("Configuring " + se.getAeDescription().getAnnotatorImplementationName() + " scaleup = "
					+ se.getDeployParams().getScaleup());
			ErrorHandlingSettings errorSettings = new ProcessErrorHandlingSettingsImpl();
			delegateConfigurations.add(DeploymentDescriptorFactory.createPrimitiveDelegateConfiguration(
					se.getAeDescription().getAnnotatorImplementationName(), errorSettings));
			File deploymentDescriptorFile = createDeploymentDescriptorFile(se, null);
			pipelineComponentDeployDescriptorFiles.add(deploymentDescriptorFile);
		}

		logger.info("Configuring AGGREGATE pipeline description...");
		pipelineDeploymentDescriptorFile = createDeploymentDescriptorFile(getPipelineDescription(),
				getPipelineDeploymentParams(), DescriptorType.AGGREGATE, delegateConfigurations);

	}

	protected abstract AnalysisEngineDescription getPipelineDescription() throws ResourceInitializationException;

	protected abstract DeploymentParams getPipelineDeploymentParams();

	protected abstract List<ServiceEngine> createServiceEngines() throws ResourceInitializationException;

	protected abstract TypeSystemDescription getPipelineTypeSystem();

	protected CollectionReaderDescription getCollectionReaderDescription(PipelineParams params)
			throws ResourceInitializationException {
		return RunCatalogCollectionReader_neo4j.createDescription(TypeSystemUtil.getCcpTypeSystem(),
				params.getCatalogDirectory(), params.getEncoding(), params.getPipelineKey(),
				params.getDocumentCollectionName(), params.getDocFileVersion(), "en", false, params.getNumToProcess(),
				params.getNumToSkip(), CcpDocumentMetadataHandler.class, params.getViewName());
	}

	/**
	 * 
	 * @param flow
	 * @return
	 * @throws ResourceInitializationException
	 */
	protected FlowControllerDescription getFlowControllerDescription(String[] flow)
			throws ResourceInitializationException {
		/*
		 * default = null. Other possible values are "continue", "stop", "drop",
		 * "dropIfNewCasProduced".
		 */
		String actionAfterCasMultiplier = null;
		String[] allowContinueOnFailure = null;
		return FlowControllerFactory.createFlowControllerDescription(AdvancedFixedFlowController.class,
				AdvancedFixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, actionAfterCasMultiplier,
				AdvancedFixedFlowController.PARAM_ALLOW_CONTINUE_ON_FAILURE, allowContinueOnFailure,
				AdvancedFixedFlowController.PARAM_FLOW, flow);
	}

	public void deployPipeline() throws Exception {
		deployedServiceIds = new HashSet<String>();
		uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		uimaAsEngine.addStatusCallbackListener(new DefaultCallbackListener(uimaAsEngine));

		CollectionReaderDescription crDesc = getCollectionReaderDescription(pipelineParams);
		uimaAsEngine.setCollectionReader(CollectionReaderFactory.createReader(crDesc));

		Map<String, Object> deployCtx = new HashMap<String, Object>();
		deployCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath, System.getenv(UIMA_HOME) + DD2SPRING_XSL_PATH);
		deployCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + System.getenv(UIMA_HOME) + SAXON_JAR_PATH);

		logger.info("Deploying pipeline component analysis engines...");
		for (File ddFile : pipelineComponentDeployDescriptorFiles) {
			String deployedServiceId = uimaAsEngine.deploy(ddFile.getAbsolutePath(), deployCtx);
			deployedServiceIds.add(deployedServiceId);
		}

		logger.info("Deploying pipeline analysis engine...");
		try {
			String deployedServiceId = uimaAsEngine.deploy(pipelineDeploymentDescriptorFile.getAbsolutePath(),
					deployCtx);
			deployedServiceIds.add(deployedServiceId);
		} catch (Exception e) {
			logger.error("########################################################################", e);
			logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

		logger.info("Initialize pipeline as UIMA AS service...");
		deployCtx.put(UimaAsynchronousEngine.ServerUri, getPipelineDeploymentParams().getBrokerUrl());
		deployCtx.put(UimaAsynchronousEngine.ENDPOINT, getPipelineDeploymentParams().getEndpoint());
		deployCtx.put(UimaAsynchronousEngine.CasPoolSize, 10);
		deployCtx.put(UimaAsynchronousEngine.UimaEeDebug, debugFlag);
		uimaAsEngine.initialize(deployCtx);
	}

	public void runPipeline() {
		try {
			pipelineRunStartTime = System.nanoTime() / 1000000;

			uimaAsEngine.process();
			uimaAsEngine.collectionProcessingComplete();
			if (deployedServiceIds != null) {
				for (String id : deployedServiceIds) {
					uimaAsEngine.undeploy(id);
				}
			}
			uimaAsEngine.stop();
		} catch (Exception e) {
			e.printStackTrace();
			Runtime.getRuntime().halt(-1);
		}
		System.exit(0);
	}

	private File createDeploymentDescriptorFile(ServiceEngine se, List<DelegateConfiguration> delegateConfigurations)
			throws ResourceInitializationException, FileNotFoundException, IOException, SAXException {
		File aeDescriptorFile = serializeDescriptionToFile(se.getAeDescription(), se.getDeployParams());
		se.setAeDescriptorFile(aeDescriptorFile);
		ServiceContext context = createServiceContext(aeDescriptorFile, se.getDeployParams());
		if (se.getDescriptorType() == DescriptorType.PRIMITIVE) {
			logger.info("Returning primitive deployment descriptor");
			return createPrimitiveDeploymentDescriptor(context, se.getDeployParams());
		}
		logger.info("Returning aggregate deployment descriptor");
		return createAggregateDeploymentDescriptor(context, se.getDeployParams(), delegateConfigurations);
	}

	/**
	 * @param aeDescription
	 * @param params
	 * @param delegateConfigurations
	 * @return a reference to the serialized
	 *         {@link UimaASPrimitiveDeploymentDescriptor} file for the
	 *         specified {@link AnalysisEngine} description
	 * @throws ResourceInitializationException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SAXException
	 */
	private File createDeploymentDescriptorFile(AnalysisEngineDescription aeDescription, DeploymentParams params,
			DescriptorType descriptorType, List<DelegateConfiguration> delegateConfigurations)
			throws ResourceInitializationException, FileNotFoundException, IOException, SAXException {
		File aeDescriptorFile = serializeDescriptionToFile(aeDescription, params);
		ServiceContext context = createServiceContext(aeDescriptorFile, params);
		if (descriptorType == DescriptorType.PRIMITIVE) {
			logger.info("Returning primitive deployment descriptor");
			return createPrimitiveDeploymentDescriptor(context, params);
		}
		logger.info("Returning aggregate deployment descriptor");
		return createAggregateDeploymentDescriptor(context, params, delegateConfigurations);
	}

	private File createPrimitiveDeploymentDescriptor(ServiceContext context, DeploymentParams params)
			throws ResourceInitializationException, FileNotFoundException, IOException {
		UimaASPrimitiveDeploymentDescriptor dd = DeploymentDescriptorFactory
				.createPrimitiveDeploymentDescriptor(context);

		dd.getProcessErrorHandlingSettings().setThresholdCount(params.getErrorThresholdCount());
		dd.setScaleup(params.getScaleup());
		return serializeDescriptionToFile(dd.toXML(), params);
	}

	private File createAggregateDeploymentDescriptor(ServiceContext context, DeploymentParams params,
			List<DelegateConfiguration> delegateConfigs)
			throws ResourceInitializationException, FileNotFoundException, IOException {
		UimaASAggregateDeploymentDescriptor dd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				context, delegateConfigs.toArray(new DelegateConfiguration[delegateConfigs.size()]));
		// dd.set
		return serializeDescriptionToFile(dd.toXML(), params);
	}

	/**
	 * @param aeDescriptorFile
	 * @param params
	 * @return an initialized {@link ServiceContext} for the specified
	 *         {@link AnalysisEngine} descriptor file
	 */
	private ServiceContext createServiceContext(File aeDescriptorFile, DeploymentParams params) {
		return new ServiceContextImpl(params.getServiceName().replaceAll(" ", "_"), params.getServiceDescription(),
				aeDescriptorFile.getAbsolutePath(), params.getEndpoint(), params.getBrokerUrl());
	}

	/**
	 * @param description
	 * @param params
	 * @return a reference to the serialized AnalysisEngine XML descriptor file
	 *         which will be placed in the configDir/service_name directory.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SAXException
	 */
	private File serializeDescriptionToFile(String xml, DeploymentParams params)
			throws FileNotFoundException, IOException {
		String filename = params.getServiceName().replaceAll(" ", "_") + "_deploy.xml";
		File outputDirectory = new File(configDir, params.getServiceName().replaceAll(" ", "_"));
		FileUtil.mkdir(outputDirectory);
		File outputFile = new File(outputDirectory, filename);
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			writer.write(xml);
		}
		return outputFile;
	}

	/**
	 * @param description
	 * @param params
	 * @return a reference to the serialized AnalysisEngine XML descriptor file
	 *         which will be placed in the configDir/service_name directory.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SAXException
	 */
	private File serializeDescriptionToFile(AnalysisEngineDescription description, DeploymentParams params)
			throws FileNotFoundException, IOException, SAXException {
		File outputFile = getEngineDescriptorFile(params, configDir);
		FileUtil.mkdir(outputFile.getParentFile());
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			description.toXML(writer);
		}
		return outputFile;
	}

	/**
	 * @param params
	 * @param configDir
	 * @return a reference to the AnalysisEngine descriptor file given a
	 *         configDirectory and deployment parameters
	 */
	protected static File getEngineDescriptorFile(DeploymentParams params, File configDir) {
		String filename = params.getServiceName().replaceAll(" ", "_") + "_engine.xml";
		File outputDirectory = new File(configDir, params.getServiceName().replaceAll(" ", "_"));
		File outputFile = new File(outputDirectory, filename);
		return outputFile;
	}

	@Data
	public static class ServiceEngine {
		private final AnalysisEngineDescription aeDescription;
		private File aeDescriptorFile;
		private final DeploymentParams deployParams;
		private final String componentName;
		private final DescriptorType descriptorType;
	}

	@RequiredArgsConstructor
	public static class DefaultCallbackListener extends UimaAsBaseCallbackListener {

		private final UimaAsynchronousEngine engine;

		private int entityCount = 0;

		@Override
		public void initializationComplete(EntityProcessStatus aStatus) {
			super.initializationComplete(aStatus);

			if (aStatus != null && aStatus.isException()) {
				System.err.println("Error during service initialization:");
				List<?> exceptions = aStatus.getExceptions();
				for (int i = 0; i < exceptions.size(); i++) {
					((Throwable) exceptions.get(i)).printStackTrace();
				}
				System.err.println("Terminating Client...");
				stop();

			}
			System.out.println("UIMA AS Service Initialization Complete");
		}

		private void stop() {
			try {
				engine.stop();
			} catch (Exception e) {

			}
			System.exit(0);
		}

		@Override
		public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
			super.entityProcessComplete(aCas, aStatus);

			if (aStatus != null) {
				if (aStatus.isException()) {
					System.err.println("Error on process CAS call to remote service:");
					List<?> exceptions = aStatus.getExceptions();
					for (int i = 0; i < exceptions.size(); i++) {
						((Throwable) exceptions.get(i)).printStackTrace();
					}
					System.err.println("Terminating Client...");
					stop();
				}
			}
			if (entityCount++ % 50 == 0) {
				logger.info("Documents processed: " + (entityCount - 1));
			}

		}

		@Override
		public void collectionProcessComplete(EntityProcessStatus aStatus) {
			super.collectionProcessComplete(aStatus);
			if (aStatus != null && aStatus.isException()) {
				System.err.println("Error on collection process complete call to remote service:");
				List<?> exceptions = aStatus.getExceptions();
				for (int i = 0; i < exceptions.size(); i++) {
					((Throwable) exceptions.get(i)).printStackTrace();
				}
				System.err.println("Terminating Client...");
				stop();
			}
			System.out.print("Completed " + entityCount + " documents");
			System.out.println();
			long elapsedTime = System.nanoTime() / 1000000 - pipelineRunStartTime;
			System.out.println("Time Elapsed : " + elapsedTime + " ms ");

			String perfReport = engine.getPerformanceReport();
			if (perfReport != null) {
				System.out.println("\n\n ------------------ PERFORMANCE REPORT ------------------\n");
				System.out.println(engine.getPerformanceReport());
			}
		}
	}

}
