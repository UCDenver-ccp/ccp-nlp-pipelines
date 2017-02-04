package edu.ucdenver.ccp.nlp.pipelines.runner;

import lombok.Data;

@Data
public class DeploymentParams {
	private final String serviceName;
	private final String serviceDescription; 
	private final int scaleup;
	private final int errorThresholdCount;
	/**
	 * Queue name
	 */
	private final String endpoint;
	
	private final String brokerUrl;
}
