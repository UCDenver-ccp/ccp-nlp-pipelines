package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import org.openrdf.model.impl.URIImpl;

public enum SoftwareOntologyClass {

	// @formatter:off
	FORMATTING(Namespace.EDAM, "operation_0335"), 
	XML_FORMAT(Namespace.EDAM,"format_2332"), 
	PLAIN_TEXT_FORMAT(Namespace.SWO,"SWO_3000043"); 
	// @formatter:on

	public enum Namespace {
		SWO, EDAM
	}

	public static final String SOFTWARE_ONTOLOGY_NAMESPACE = "http://www.ebi.ac.uk/swo/";
	public static final String EDAM_ONTOLOGY_NAMESPACE = "http://edamontology.org/";
	private final String propertyName;
	private final Namespace ns;

	private SoftwareOntologyClass(Namespace ns, String propertyName) {
		this.propertyName = propertyName;
		this.ns = ns;
	}

	public URIImpl uri() {
		String namespace = null;
		switch (this.ns) {
		case SWO:
			namespace = SOFTWARE_ONTOLOGY_NAMESPACE;
			break;
		case EDAM:
			namespace = EDAM_ONTOLOGY_NAMESPACE;
			break;
		default:
			throw new IllegalArgumentException("Unhandled namespace: " + this.ns.name());
		}
		return new URIImpl(namespace + propertyName);
	}

}
