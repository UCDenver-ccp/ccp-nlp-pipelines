package edu.ucdenver.ccp.nlp.pipelines.runlog.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.common.ftp.FTPUtil;
import edu.ucdenver.ccp.common.string.StringUtil;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document;
import edu.ucdenver.ccp.nlp.pipelines.runlog.Document.FileType;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.DocumentCollection.PMC_OA_DocumentCollection;
import edu.ucdenver.ccp.nlp.pipelines.runlog.RunCatalog;
import lombok.Data;

/**
 * Populates a RunCatalog with the PMC Open Access corpus. Assumes the files
 * have been bulk-downloaded previously.
 */
public class CatalogLoader_PMC_OA {

	private static final Logger logger = Logger.getLogger(CatalogLoader_PMC_OA.class);

	private static final String PMC_LIBRARY_DIR = "pmc";

	/**
	 * The path to where the PMC .nxml and (when created) .txt files are to be
	 * stored
	 */
	private final File pmcLibraryBaseDirectory;

	private RunCatalog catalog;

	public CatalogLoader_PMC_OA(RunCatalog catalog, File libraryBaseDirectory) {
		this.catalog = catalog;
		this.pmcLibraryBaseDirectory = new File(libraryBaseDirectory, PMC_LIBRARY_DIR);
	}

	public void initCatalogWithBulkPmc(File bulkPmcBaseDirectory) throws IOException {
		Map<String, DocumentMetadata> pmcid2MetadataMap = loadPmcOaMetadataMap(bulkPmcBaseDirectory);
		initCatalog(bulkPmcBaseDirectory, pmcLibraryBaseDirectory, catalog, pmcid2MetadataMap);
	}

	static void initCatalog(File bulkPmcBaseDirectory, File libraryBaseDirectory, RunCatalog catalog,
			Map<String, DocumentMetadata> pmcid2MetadataMap) throws IOException {
		DocumentCollection dc = new PMC_OA_DocumentCollection();
		for (Iterator<File> fileIter = FileUtil.getFileIterator(bulkPmcBaseDirectory, true, ".nxml.gz"); fileIter
				.hasNext();) {
			File file = fileIter.next();
			String pmcId = StringUtils.removeEnd(file.getName(), ".nxml.gz");
			if (pmcid2MetadataMap.containsKey(pmcId)) {
				DocumentMetadata dm = pmcid2MetadataMap.get(pmcId);

				/*
				 * move the file into the correct 2-level randomized directory
				 * as specified in the metadata
				 */
				String localPath = dm.getRemotePath().substring(0, dm.getRemotePath().lastIndexOf('/'));
				File storagePath = new File(libraryBaseDirectory, localPath + File.separator + file.getName());
				FileUtil.mkdir(storagePath);
				if (!file.renameTo(storagePath)) {
					logger.warn("Failed to move file to library storage location: " + storagePath.getAbsolutePath()
							+ " from: " + file.getAbsolutePath());
				}

				/* add the document to the catalog */
				Document d = new Document(dm.getPmid(), dm.getPmcid(), storagePath, FileType.XML, dm.getJournal(),
						dm.getCitation());
				d.setSourceFileLicense(dm.getLicense());

				catalog.addDocument(d, dc);
			} else {
				logger.warn("No PMC OA metadata available for file: " + file.getAbsolutePath());
			}
		}
	}

	/**
	 * Downloads and parses the PMC OA file list file from:
	 * ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_file_list.txt
	 * 
	 * @param workDirectory
	 * @return a mapping from PMC ID to metadata about the document parsed from
	 *         the downloaded file
	 * @throws IOException
	 */
	private Map<String, DocumentMetadata> loadPmcOaMetadataMap(File workDirectory) throws IOException {
		File pmcOaListFile = FTPUtil.downloadFile("ftp.ncbi.nlm.nih.gov", "pub/pmc", "oa_file_list.txt",
				edu.ucdenver.ccp.common.ftp.FTPUtil.FileType.ASCII, workDirectory);
		try (InputStream is = new FileInputStream(pmcOaListFile)) {
			return parsePmcOaMetadata(is);
		}
	}

	/**
	 * @param is
	 * @return a mapping from PMC ID to metadata about the document parsed from
	 *         the specified input stream
	 * @throws IOException
	 */
	static Map<String, DocumentMetadata> parsePmcOaMetadata(InputStream is) throws IOException {
		Map<String, DocumentMetadata> map = new HashMap<String, DocumentMetadata>();
		int count = 0;
		for (StreamLineIterator lineIter = new StreamLineIterator(is, CharacterEncoding.UTF_8, null); lineIter
				.hasNext();) {
			if (count++ % 10000 == 0) {
				logger.info("PMC OA Metadata loading progress: " + (count - 1));
			}
			Line line = lineIter.next();
			/* first line is a time stamp */
			if (line.getLineNumber() == 0) {
				continue;
			}
			String[] toks = line.getText().split("\\t");
			String remotePath = toks[0];
			String citation = toks[1];
			String journal = citation.substring(0, citation.indexOf('.') + 1);
			String pmcid = toks[2];
			String pmid = StringUtil.removePrefix(toks[3], "PMID:");
			String license = toks[4];

			DocumentMetadata dm = new DocumentMetadata(remotePath, citation, journal, pmcid, pmid, license);
			map.put(pmcid, dm);
		}
		return map;
	}

	@Data
	static class DocumentMetadata {
		private final String remotePath;
		private final String citation;
		private final String journal;
		private final String pmcid;
		private final String pmid;
		private final String license;
	}

}
