package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.util.ArrayList;
import java.util.List;

import edu.ucdenver.ccp.common.string.RegExPatterns;
import edu.ucdenver.ccp.common.string.StringConstants;
import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;

public class AnnotationSerializer {

	private static final String DELIMITER = StringConstants.TAB;
	private static final String DELIMITER_REGEX = RegExPatterns.TAB;

	/**
	 * It can sometimes be helpful to not include the covered text when
	 * serializing annotations, for example for entire document sections
	 */
	public enum IncludeCoveredText {
		YES, NO
	}

	/**
	 * @param annot
	 * @return simple serialization string for annotation storage using the
	 *         format:
	 */
	public static String toString(TextAnnotation annot, IncludeCoveredText includeCoveredText) {
		/* ensure the annotation is a simple concept (no slots) */
		if (annot.getClassMention().getComplexSlotMentions().isEmpty()
				&& annot.getClassMention().getPrimitiveSlotMentions().isEmpty()) {
			return getStorageLine(annot.getDocumentID(), annot.getAnnotator().getFirstName(),
					annot.getClassMention().getMentionName(), annot.getCoveredText(), annot.getSpans(),
					includeCoveredText);
		} else {
			throw new IllegalStateException("Detected annotations with slots. This serialization "
					+ "format does not handle slot fillers currently: " + annot.toString());
		}

	}

	private static String getStorageLine(String documentId, String annotatorFirstName, String mentionName,
			String coveredText, List<Span> spans, IncludeCoveredText includeCoveredText) {
		String outLine = documentId + DELIMITER + annotatorFirstName + DELIMITER + mentionName + DELIMITER
				+ ((includeCoveredText == IncludeCoveredText.YES) ? coveredText : "");
		for (Span span : spans) {
			outLine += (DELIMITER + span.toString());
		}
		return outLine;
	}

	public static TextAnnotation fromString(String line) {
		String[] toks = line.split(DELIMITER_REGEX);
		int index = 0;

		String documentId = toks[index++];
		String annotatorName = toks[index++];
		String mentionName = toks[index++];
		String coveredText = toks[index++];
		List<Span> spans = new ArrayList<Span>();
		for (int i = index; i < toks.length; i++) {
			Span span = Span.fromString(toks[i]);
			spans.add(span);
		}

//		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		// TODO: eventually use: TextAnnotationFactory.createFromString(s) when
		// we need more complicated representation
		TextAnnotation annot = new DefaultTextAnnotation(spans);
		annot.setDocumentID(documentId);
		annot.setCoveredText(coveredText);
		annot.setAnnotator(new Annotator(-1, annotatorName, "", ""));
		ClassMention cm = new DefaultClassMention(mentionName);
		annot.setClassMention(cm);

		return annot;
	}

}
