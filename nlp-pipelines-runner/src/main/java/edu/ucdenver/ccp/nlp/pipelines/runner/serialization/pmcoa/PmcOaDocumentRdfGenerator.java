package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import edu.ucdenver.ccp.common.string.StringUtil;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.DocumentRdfGenerator;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

public class PmcOaDocumentRdfGenerator implements DocumentRdfGenerator {

	private final Logger logger = Logger.getLogger(PmcOaDocumentRdfGenerator.class);

	@Override
	public URI getDocumentUri(JCas jCas, DocumentMetadataHandler documentMetadataHandler) {
		String documentId = documentMetadataHandler.extractDocumentId(jCas);
		// remove .xml.gz.txt.gz
		documentId = StringUtil.removeSuffix(documentId, ".nxml.gz.txt.gz");
		return new URIImpl("http://kabob.ucdenver.edu/iao/document_" + documentId);
	}

	@Override
	public Collection<Statement> generateRdf(JCas jCas, DocumentMetadataHandler documentMetadataHandler) {
		List<Statement> stmts = new ArrayList<Statement>();
		// JCas xmlView;
		// try {
		// xmlView = View_Util.getView(jCas, View.XML.viewName());
		URI xmlDocumentUri = getDocumentUri(jCas, documentMetadataHandler);

		/* xmlDoc --rdf:type--> iao:publication */
		stmts.add(new StatementImpl(xmlDocumentUri, RDF.TYPE, IaoClass.PUBLICATION.uri()));
		return stmts;

		// } catch (CASException e) {
		// logger.warn("Exception while extracting views in cas: ", e);
		// return null;
		// }

	}

}
