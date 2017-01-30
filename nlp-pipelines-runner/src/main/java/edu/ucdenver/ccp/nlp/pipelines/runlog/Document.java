package edu.ucdenver.ccp.nlp.pipelines.runlog;

import java.io.File;
import java.net.URL;
import java.util.Set;

import lombok.Data;

@Data
public class Document {
//	private final Set<ExternalIdentifier> ids;
	private final String pmid;
	private final String pmcid;
	private File localTextFile;
	private final File localSourceFile;
	private URL remoteSourceFile;
	private final FileType sourceFileType;
	private String sourceFileLicense;
	private final String journal;
	private final String citation;
	
	
	public enum FileType {
		XML, TXT
	}
	
}
