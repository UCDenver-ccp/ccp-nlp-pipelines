package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.collections.CollectionsUtil.SortOrder;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog.RunStatus;

public class RunCatalogUtil {

	public static void getCatalogRunSummary(RunCatalog catalog) {
		/*
		 * for each document collection get the missing runs map and output
		 * counts; completed run count would be nice too
		 */
		List<DocumentCollection> documentCollections = getSortedDocumentCollections(catalog);
		System.out.println("-----------------  Document Collections  -----------------\n");
		documentCollections.forEach(dc -> System.out
				.println(dc.getShortname() + "\t" + dc.getLongname() + "\t" + catalog.getDocumentCount(dc)));
		System.out.println();
		System.out.println("-----------------  Pipeline Run Summaries  -----------------\n");
		for (DocumentCollection dc : documentCollections) {
			System.out.println("  ========== " + dc.getLongname() + " ==========");
			Map<String, Map<RunStatus, Set<Document>>> missingRunsMap = catalog.getRunsMap(dc);
			Map<String, Map<RunStatus, Set<Document>>> sortedMap = CollectionsUtil.sortMapByKeys(missingRunsMap,
					SortOrder.ASCENDING);
			for (Entry<String, Map<RunStatus, Set<Document>>> entry : sortedMap.entrySet()) {
				int completeCount = 0;
				if (entry.getValue().containsKey(RunStatus.COMPLETE)) {
					completeCount = entry.getValue().get(RunStatus.COMPLETE).size();
				}
				int outstandingCount = 0;
				if (entry.getValue().containsKey(RunStatus.OUTSTANDING)) {
					outstandingCount = entry.getValue().get(RunStatus.OUTSTANDING).size();
				}
				int totalCount = completeCount + outstandingCount;
				System.out.println(entry.getKey() + " Total: " + totalCount + " Complete: " + completeCount
						+ " Outstanding: " + outstandingCount);

			}
			System.out.println();
		}
		System.out.println("------------------------------------------------------------");
	}

	private static List<DocumentCollection> getSortedDocumentCollections(RunCatalog catalog) {
		List<DocumentCollection> documentCollections = new ArrayList<DocumentCollection>(
				catalog.getDocumentCollections());
		/* sort the doc collections list for reproducible output */
		Collections.sort(documentCollections, new Comparator<DocumentCollection>() {
			@Override
			public int compare(DocumentCollection dc1, DocumentCollection dc2) {
				return dc1.getShortname().compareTo(dc2.getShortname());
			}
		});
		return documentCollections;
	}

	public static void removeEmptyDocumentCollections(RunCatalog catalog) {
		catalog.removeEmptyDocumentCollections();
	}

}
