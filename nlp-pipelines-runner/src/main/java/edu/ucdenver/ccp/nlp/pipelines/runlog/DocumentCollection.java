package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class DocumentCollection {
	private final String shortname;
	private final String longname;
	private final String description;
	private Set<String> runKeys;

	public void addRunKey(String runKey) {
		if (runKeys == null) {
			runKeys = new HashSet<String>();
		}
		this.runKeys.add(runKey);
	}

	public static class PMC_OA_DocumentCollection extends DocumentCollection {
		public PMC_OA_DocumentCollection() {
			super("PMC_OA", "PubMed Central Open Access",
					"The subset of PubMed Central designated as Open Access and made "
							+ "available for bulk downloading. This corpus comprises ~1.5 million "
							+ "full text articles. Further details are available here: "
							+ "https://www.ncbi.nlm.nih.gov/pmc/tools/openftlist/");
		}
	}

}
