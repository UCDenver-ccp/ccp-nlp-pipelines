package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.Concept;

import java.util.ArrayList;
import java.util.List;


public class PRDictionaryEntryModifierTest {

	/**
	 * The following test demonstrates how you might test a
	 * DictionaryEntryModifier
	 */
	@Test
	public void testPRDictEntryModifier() {
		PRDictionaryEntryModifier dictModifier = new PRDictionaryEntryModifier();
		
		/* create your input concept */
		Concept inputConcept = new Concept("http://purl.obolibrary.org/obo/PR_000015574", "label", 
			CollectionsUtil.createSet("small proline-rich protein 2A","SPR-2A","SPRR2A","2-1"),null);
		/* modify the input concept using the dictionary entry modifier */
		Concept modifiedConcept = dictModifier.modifyConcept(inputConcept);
	
		/*
		 * Create the concept you expect to be returned by the dictionary entry
		 * modifier. In this case there are no changes as the
			* SampleDictionaryEntryModifier makes no changes.
		*/
		Concept expectedModifiedConcept = new Concept("http://purl.obolibrary.org/obo/PR_000015574", "label",
			CollectionsUtil.createSet("small proline-rich protein 2A","SPR-2A","SPRR2A"), null);
	
		/*
		* The assert statement tests that the expected modified concept is
		* equal to what was actually returned by the dictionary entry modifier
		*/
		assertEquals(expectedModifiedConcept, modifiedConcept);
//		}
	}
	
	@Test
	public void testPRDictEntryModifier_2() {
		PRDictionaryEntryModifier dictModifier = new PRDictionaryEntryModifier();


		/* create your input concept */
		Concept inputConcept = new Concept("http://purl.obolibrary.org/obo/PR_000015198", "label", 
			CollectionsUtil.createSet("low affinity cationic amino acid transporter 2","TEA","CAT2",
					"solute carrier family 7 member 2","ATRC2","CAT-2",
					"T-cell early activation protein","SLC7A2","20.5"),null);
		/* modify the input concept using the dictionary entry modifier */
		Concept modifiedConcept = dictModifier.modifyConcept(inputConcept);
	
		/*
		 * Create the concept you expect to be returned by the dictionary entry
		 * modifier. In this case there are no changes as the
			* SampleDictionaryEntryModifier makes no changes.
		*/
		Concept expectedModifiedConcept = new Concept("http://purl.obolibrary.org/obo/PR_000015198", "label",
			CollectionsUtil.createSet("low affinity cationic amino acid transporter 2","TEA","CAT2",
					"solute carrier family 7 member 2","ATRC2","CAT-2",
					"T-cell early activation protein","SLC7A2"), null);
	
		/*
		* The assert statement tests that the expected modified concept is
		* equal to what was actually returned by the dictionary entry modifier
		*/
		assertEquals(expectedModifiedConcept, modifiedConcept);
//		}
	}


}
