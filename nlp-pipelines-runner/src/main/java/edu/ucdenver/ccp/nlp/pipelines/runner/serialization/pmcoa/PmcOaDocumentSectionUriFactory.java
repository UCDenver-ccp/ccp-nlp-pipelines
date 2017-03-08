package edu.ucdenver.ccp.nlp.pipelines.runner.serialization.pmcoa;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.tcas.Annotation;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.ucdenver.ccp.datasource.identifiers.DataSource;
import edu.ucdenver.ccp.datasource.rdfizer.rdf.ice.RdfUtil;
import edu.ucdenver.ccp.nlp.doc2txt.CcpXmlParser.DocumentElement;
import edu.ucdenver.ccp.nlp.uima.serialization.rdf.UriFactory;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;

/**
 * returns IRIs for document section types contained in the PMC OA corpus
 */
public class PmcOaDocumentSectionUriFactory implements UriFactory {

	private final Logger logger = Logger.getLogger(PmcOaDocumentSectionUriFactory.class);

	@Override
	public URI getResourceUri(AnnotationDataExtractor annotationDataExtractor, Annotation annotation) {
		String annotationType = annotationDataExtractor.getAnnotationType(annotation);

		if (annotationType.startsWith("http://")) {
			return new URIImpl(annotationType);
		}

		if (annotationType.equalsIgnoreCase("sentence")) {
			return new URIImpl("http://purl.org/linguistics/gold/OrthographicSentence");
		}

		DocumentElement documentElement = null;
		try {
			documentElement = DocumentElement.valueOf(annotationType.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.warn("Unknown/unhandled PMC OA document section: " + annotationType);
			return null;
		}
		switch (documentElement) {
		case ABSTRACT:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "abstract").toString());
		case ARTICLE_TITLE:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "article_title").toString());
		case BOLD:
			// to save space we are not serializing the typography annotations
			return null;
		// return new URIImpl(RdfUtil.createUri(DataSource.KIAO,
		// "typeface_bold").toString());
		case CAPTION:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "caption").toString());
		case COPYRIGHT:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "copyright_statement").toString());
		case DOCUMENT:
			// excluding the document annotation for now. not sure it is
			// necessary.
			return null;
		case ITALIC:
			// to save space we are not serializing the typography annotations
			return null;
		// return new URIImpl(RdfUtil.createUri(DataSource.KIAO,
		// "typeface_italic").toString());
		case KEYWORD:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "keyword").toString());
		case PARAGRAPH:
			return new URIImpl("http://purl.org/linguistics/gold/Paragraph");
		case SECTION:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "document_section").toString());
		case SOURCE:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "document_source").toString());
		case SUB:
			// to save space we are not serializing the typography annotations
			return null;
		// return new URIImpl(RdfUtil.createUri(DataSource.KIAO,
		// "typeface_subscript").toString());
		case SUP:
			// to save space we are not serializing the typography annotations
			return null;
		// return new URIImpl(RdfUtil.createUri(DataSource.KIAO,
		// "typeface_superscript").toString());
		case TITLE:
			return new URIImpl(RdfUtil.createUri(DataSource.KIAO, "title").toString());
		default:
			throw new IllegalArgumentException(
					"Unhandled document section case (" + documentElement.name() + "). Code changes required.");
		}
	}

}
