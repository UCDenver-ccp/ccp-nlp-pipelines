package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.Concept;

public class SampleDictionaryEntryModifierTest {

	/**
	 * The following test demonstrates how you might test a
	 * DictionaryEntryModifier
	 */
	@Test
	public void testSampleDictEntryModifier() {
		SampleDictionaryEntryModifier dictModifier = new SampleDictionaryEntryModifier();

		/* create your input concept */
		Concept inputConcept = new Concept("GO:0001234", "label", CollectionsUtil.createSet("synonym1", "synonym2"),
				null);
		/* modify the input concept using the dictionary entry modifier */
		Concept modifiedConcept = dictModifier.modifyConcept(inputConcept);

		/*
		 * Create the concept you expect to be returned by the dictionary entry
		 * modifier. In this case there are no changes as the
		 * SampleDictionaryEntryModifier makes no changes.
		 */
		Concept expectedModifiedConcept = new Concept("GO:0001234", "label",
				CollectionsUtil.createSet("synonym1", "synonym2"), null);

		/*
		 * The assert statement tests that the expected modified concept is
		 * equal to what was actually returned by the dictionary entry modifier
		 */
		assertEquals(expectedModifiedConcept, modifiedConcept);

	}

}
