package edu.ucdenver.ccp.nlp.pipelines.runlog.init;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.ucdenver.ccp.nlp.pipelines.runlog.Neo4jRunCatalog;

public class Main_LoadCatalog_PMC_OA_neo4j {

	private static final Logger logger = Logger.getLogger(Main_LoadCatalog_PMC_OA_neo4j.class);

	/**
	 * @param args
	 *            args[0] = libraryBaseDirectory: the base file path where
	 *            articles in the catalog are to be stored<br>
	 *            args[1] = catalogDirectory: the base directory containing the
	 *            neo4j repository<br>
	 *            args[2] = pmcBulkDirectory: the base directory housing the
	 *            unpacked, but compressed, PMC OA .nxml files
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();

		File libraryBaseDirectory = new File(args[0]);
		File catalogDirectory = new File(args[1]);
		File pmcBulkDirectory = new File(args[2]);

		/* open a connection to the Neo4j Embedded DB */
		try (Neo4jRunCatalog catalog = new Neo4jRunCatalog(libraryBaseDirectory, catalogDirectory);) {

			CatalogLoader_PMC_OA loader = new CatalogLoader_PMC_OA(catalog, libraryBaseDirectory);
			loader.initCatalogWithBulkPmc(pmcBulkDirectory);

		} catch (IOException e) {
			logger.error("Exception thrown (possibly during neo4j close() operation)...", e);
		}

	}

}
