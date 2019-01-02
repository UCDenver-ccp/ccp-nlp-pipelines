package edu.ucdenver.ccp.nlp.pipelines.runner.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.string.RegExPatterns;
import edu.ucdenver.ccp.common.string.StringConstants;
import edu.ucdenver.ccp.common.string.StringUtil;
import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.PrimitiveSlotMention;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import lombok.Data;

public class AnnotationSerializerImpl implements AnnotationSerializer {

	private static final Logger logger = Logger.getLogger(AnnotationSerializerImpl.class);

	protected static final String DELIMITER = StringConstants.TAB;
	protected static final String DELIMITER_REGEX = RegExPatterns.TAB;

	private IncludeCoveredText includeCoveredText;

	private IncludeAnnotator includeAnnotator;

	private IncludeSlots includeSlots;

	private String docIdSuffixToRemove;

	private Gson gson;

	/**
	 * It can sometimes be helpful to not include the covered text when
	 * serializing annotations, for example for entire document sections
	 */
	public enum IncludeCoveredText {
		YES, NO
	}

	public enum IncludeAnnotator {
		YES, NO
	}

	public enum IncludeSlots {
		YES, NO
	}

	public AnnotationSerializerImpl(IncludeCoveredText includeCoveredText, IncludeAnnotator includeAnnotator,
			IncludeSlots includeSlots, String docIdSuffixToRemove) {
		this.includeCoveredText = includeCoveredText;
		this.includeAnnotator = includeAnnotator;
		this.includeSlots = includeSlots;
		this.docIdSuffixToRemove = docIdSuffixToRemove;
		this.gson = new Gson();
	}

	/**
	 * @param annot
	 * @return simple serialization string for annotation storage using the
	 *         format:
	 */
	@Override
	public List<String> toString(TextAnnotation annot, String documentText) {

		String documentId = annot.getDocumentID();
		if (docIdSuffixToRemove != null) {
			try {
				documentId = StringUtil.removeSuffix(documentId, docIdSuffixToRemove);
			} catch (IllegalArgumentException e) {
				logger.warn("Unable to remove document ID suffix. " + e.getMessage());
			}
		}

		String annotatorName = annot.getAnnotator().getFirstName();
		if (includeAnnotator == IncludeAnnotator.NO) {
			annotatorName = null;
		}

		String coveredText = annot.getCoveredText().replaceAll("\\n", " ");
		if (includeCoveredText == IncludeCoveredText.NO) {
			coveredText = null;
		}

		String annotType = annot.getClassMention().getMentionName();

		int[][] spans = convertSpansToIntegerArray(annot.getSpans());

		SerializableAnnotation serializableAnnot = new SerializableAnnotation(documentId, annotType, spans,
				coveredText);
		serializableAnnot.setAnnotatorName(annotatorName);

		if (includeSlots == IncludeSlots.YES) {
			if (annot.getClassMention().getPrimitiveSlotMentions() != null) {
				for (PrimitiveSlotMention<?> slot : annot.getClassMention().getPrimitiveSlotMentions()) {
					String slotName = slot.getMentionName();
					for (String slotValue : CollectionsUtil.toString(slot.getSlotValues())) {
						serializableAnnot.addSlotValue(slotName, slotValue);
					}
				}
			}
		}

		return CollectionsUtil.createList(gson.toJson(serializableAnnot));

	}

	private int[][] convertSpansToIntegerArray(List<Span> spans) {
		List<int[]> arraySpans = new ArrayList<int[]>();
		for (Span span : spans) {
			arraySpans.add(new int[] { span.getSpanStart(), span.getSpanEnd() });
		}
		return arraySpans.toArray(new int[spans.size()][]);
	}

	private static List<Span> convertIntegerArrayToSpans(int[][] intSpans) {
		List<Span> spans = new ArrayList<Span>();
		for (int i = 0; i < intSpans.length; i++) {
			spans.add(new Span(intSpans[i][0], intSpans[i][1]));
		}
		return spans;
	}

	public static TextAnnotation fromString(String line) {
		Gson gson = new Gson();
		SerializableAnnotation annot = gson.fromJson(line, SerializableAnnotation.class);

		List<Span> spans = convertIntegerArrayToSpans(annot.getSpans());

		TextAnnotation ta = new DefaultTextAnnotation(spans);

		ta.setDocumentID(annot.getDocumentId());
		if (annot.getCoveredText() != null) {
			ta.setCoveredText(annot.getCoveredText());
		}
		if (annot.getAnnotatorName() != null) {
			ta.setAnnotator(new Annotator(-1, annot.getAnnotatorName(), "", ""));
		}
		ClassMention cm = new DefaultClassMention(annot.getType());
		ta.setClassMention(cm);

		// add primitive slots here
		if (annot.getSlots() != null) {
			for (Entry<String, Set<String>> entry : annot.getSlots().entrySet()) {
				for (String value : entry.getValue()) {
					cm.addPrimitiveSlotMention(cm.createPrimitiveSlotMention(entry.getKey(), value));
				}
			}
		}
		return ta;
	}

	@Data
	public static class SerializableAnnotation {
		private final String documentId;
		private String annotatorName;
		private final String type;
		private final int[][] spans;
		private final String coveredText;
		private Map<String, Set<String>> slots;

		public void addSlotValue(String slotName, String slotValue) {
			if (slots == null) {
				slots = new HashMap<String, Set<String>>();
			}
			CollectionsUtil.addToOne2ManyUniqueMap(slotName, slotValue, slots);
		}
	}

}
