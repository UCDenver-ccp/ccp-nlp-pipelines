package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.Concept;
import java.util.ArrayList;
import java.util.List;


public class CHEBIDictionaryEntryModifierTest {

	/**
	 * The following test demonstrates how you might test a
	 * DictionaryEntryModifier
	 */
	@Test
	public void testCHEBIDictEntryModifier() {
		CHEBIDictionaryEntryModifier dictModifier = new CHEBIDictionaryEntryModifier();
		List<String> conceptList = new ArrayList<String>();
		conceptList.add("http://purl.obolibrary.org/obo/CHEBI_90880");
		conceptList.add("http://purl.obolibrary.org/obo/CHEBI_24433");
		conceptList.add("http://purl.obolibrary.org/obo/CHEBI_50906");
		conceptList.add("http://purl.obolibrary.org/obo/CHEBI_33232");
		
		
		for (int i = 0; i < conceptList.size(); i++) {
			/* create your input concept */
			Concept inputConcept = new Concept(conceptList.get(i), "label", CollectionsUtil.createSet("synonym1", "synonym2"),
					null);
			/* modify the input concept using the dictionary entry modifier */
			Concept modifiedConcept = dictModifier.modifyConcept(inputConcept);
	
			/*
			 * Create the concept you expect to be returned by the dictionary entry
			 * modifier. In this case there are no changes as the
			 * SampleDictionaryEntryModifier makes no changes.
			 */
	//		Concept expectedModifiedConcept = new Concept("GO:0001234", "label",
	//				CollectionsUtil.createSet("synonym1", "synonym2"), null);
	
			/*
			 * The assert statement tests that the expected modified concept is
			 * equal to what was actually returned by the dictionary entry modifier
			 */
	//		assertEquals(expectedModifiedConcept, modifiedConcept);
			assertNull(modifiedConcept);
		}
	}

}


////DELETE ALL TERMS BELOW
////Delete (1+) - not in CRAFT
//if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_90880")) {
//	return null;
//}
//
//// Delete group
//if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_24433")) {
//	return null;
//}
//
//// Delete role - not in dict
//if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_50906")) {
//	return null;
//}		
//
//// Delete application
//if (inputConcept.getIdentifier().equals("http://purl.obolibrary.org/obo/CHEBI_33232")) {
//	return null;
//}