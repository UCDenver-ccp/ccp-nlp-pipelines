package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import org.openrdf.model.impl.URIImpl;

public enum IaoClass {

	// @formatter:off
	PUBLICATION("IAO_0000311"); 
	// @formatter:on

	public static final String OBO_NAMESPACE = "http://purl.obolibrary.org/obo/";
	private final String propertyName;

	private IaoClass(String propertyName) {
		this.propertyName = propertyName;
	}

	public URIImpl uri() {
		return new URIImpl(OBO_NAMESPACE + propertyName);
	}

}
