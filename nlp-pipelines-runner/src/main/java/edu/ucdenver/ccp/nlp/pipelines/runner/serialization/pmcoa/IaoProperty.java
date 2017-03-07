package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import org.openrdf.model.impl.URIImpl;

public enum IaoProperty {
	// @formatter:off
	DENOTES("IAO_0000219");
	// @formatter:on

	private final String propertyName;

	private IaoProperty(String propertyName) {
		this.propertyName = propertyName;
	}

	public URIImpl uri() {
		return new URIImpl(IaoClass.OBO_NAMESPACE + propertyName);
	}

}
