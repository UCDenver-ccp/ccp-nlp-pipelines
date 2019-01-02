package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import org.openrdf.model.impl.URIImpl;

public enum SoftwareOntologyProperty {
	// @formatter:off
	HAS_FORMAT_SPECIFICATION("SWO_0004002"), 
	HAS_DOWNLOAD_LOCATION("SWO_0000046"), 
	HAS_SPECIFIED_DATA_INPUT("SWO_0000086"), 
	HAS_SPECIFIED_DATA_OUTPUT("SWO_0000087"); 
	// @formatter:on

	private final String propertyName;

	private SoftwareOntologyProperty(String propertyName) {
		this.propertyName = propertyName;
	}

	public URIImpl uri() {
		return new URIImpl(SoftwareOntologyClass.SOFTWARE_ONTOLOGY_NAMESPACE + propertyName);
	}

}
