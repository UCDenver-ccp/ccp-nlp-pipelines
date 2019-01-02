package edu.ucdenver.ccp.nlp.pipelines.runlog;

import lombok.Data;

@Data
public class ExternalIdentifier {
	private final ExternalIdentifierType type;
	private final String id;

	public enum ExternalIdentifierType {
		PUBMED, PMC
	}

}
