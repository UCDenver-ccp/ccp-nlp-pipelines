package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Main_CatalogSummary_neo4j {

	private static final Logger logger = Logger.getLogger(Main_CatalogSummary_neo4j.class);

	/**
	 * @param args
	 *            args[0] = neo4j catalog base directory
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		File catalogDirectory = new File(args[0]);
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(catalogDirectory)) {
			RunCatalogUtil.removeEmptyDocumentCollections(catalog);
			RunCatalogUtil.getCatalogRunSummary(catalog);
		} catch (IOException e) {
			logger.error("Exception thrown (possibly during neo4j close() operation)...", e);
			System.exit(-1);
		}

	}

}
