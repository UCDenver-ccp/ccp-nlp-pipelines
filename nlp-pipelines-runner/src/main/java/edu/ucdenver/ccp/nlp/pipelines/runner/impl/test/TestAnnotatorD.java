package edu.ucdenver.ccp.nlp.pipelines.runner.impl.test;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

public class TestAnnotatorD extends JCasAnnotator_ImplBase {

	private Logger logger;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		logger = context.getLogger();
		logger.log(Level.INFO, "!=!=!=! Initializing " + this.getClass().getSimpleName());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new ResourceInitializationException(e);
		}
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		logger.log(Level.INFO, "Processing by " + this.getClass().getSimpleName());
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public void destroy() {
		logger.log(Level.INFO, "!=!=!=! Destroying " + this.getClass().getSimpleName());
		super.destroy();
	}

	@Override
	public void reconfigure() throws ResourceConfigurationException, ResourceInitializationException {
		logger.log(Level.INFO, "!=!=!=! Reconfiguring " + this.getClass().getSimpleName());
		super.reconfigure();
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(TestAnnotatorD.class);
	}

}