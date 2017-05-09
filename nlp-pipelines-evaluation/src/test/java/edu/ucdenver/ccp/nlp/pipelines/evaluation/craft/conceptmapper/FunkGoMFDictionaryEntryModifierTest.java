package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 - 2017 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.OboToDictionary.Concept;

import java.util.ArrayList;
import java.util.List;


public class FunkGoMFDictionaryEntryModifierTest {

	/**
	 * The following test demonstrates how you might test a
	 * DictionaryEntryModifier
	 */
	@Test
	public void testFunkGoMFIDictEntryModifier() {
		FunkGoMFDictionaryEntryModifier dictModifier = new FunkGoMFDictionaryEntryModifier();
		List<String> conceptList = new ArrayList<String>();
		conceptList.add("http://purl.obolibrary.org/obo/GO_0003677");
//		conceptList.add("http://purl.obolibrary.org/obo/GO_0043498");
//		conceptList.add("http://purl.obolibrary.org/obo/GO_0005141");
		

				
		//"DNA binding","binds","microtubule/chromatin interaction",
//		"binding of a DNA", "bound DNA","DNA bound","binding of DNA","bound","binding","binds DNA",
//		"re-bind DNA","plasmid binding","unbind DNA","bind","bind DNA","rebind","unbind","DNA bindable",
//		"binding DNA","rebind DNA","re-bind"
		/* create your input concept */
		Concept inputConcept = new Concept("http://purl.obolibrary.org/obo/GO_0003677", "label", 
			CollectionsUtil.createSet("DNA binding"),null);
		/* modify the input concept using the dictionary entry modifier */
		Concept modifiedConcept = dictModifier.modifyConcept(inputConcept);
	
		/*
		 * Create the concept you expect to be returned by the dictionary entry
		 * modifier. In this case there are no changes as the
			* SampleDictionaryEntryModifier makes no changes.
		*/
		Concept expectedModifiedConcept = new Concept("http://purl.obolibrary.org/obo/GO_0003677", "label",
			CollectionsUtil.createSet("DNA binding","microtubule/chromatin interaction",
			"binding of a DNA", "bound DNA","DNA bound","binding of DNA","binds DNA",
			"re-bind DNA","plasmid binding","unbind DNA","bind DNA",
			"DNA bindable","binding DNA","rebind DNA"), null);
	
		/*
		* The assert statement tests that the expected modified concept is
		* equal to what was actually returned by the dictionary entry modifier
		*/
		assertEquals(expectedModifiedConcept, modifiedConcept);
//		}
	}

}
//java.lang.AssertionError: expected:<OboToDictionary.Concept(identifier=http://purl.obolibrary.org/obo/GO_0003677, name=label, officialSynonyms=[microtubule/chromatin interaction, binding of a DNA, bound DNA, DNA bound, binding of DNA, binds DNA, DNA binding, re-bind DNA, plasmid binding, unbind DNA, bind DNA, DNA bindable, binding DNA, rebind DNA], dynamicallyGeneratedSynonyms=null)> but was:<OboToDictionary.Concept(identifier=http://purl.obolibrary.org/obo/GO_0003677, name=label, officialSynonyms=[binds, microtubule/chromatin interaction, binding of a DNA, bound DNA, DNA bound, binding of DNA, bound, binding, binds DNA, DNA binding, re-bind DNA, plasmid binding, unbind DNA, bind, bind DNA, rebind, unbind, DNA bindable, binding DNA, rebind DNA, re-bind], dynamicallyGeneratedSynonyms=null)>
//	at org.junit.Assert.fail(Assert.java:91)
//	at org.junit.Assert.failNotEquals(Assert.java:645)
//	at org.junit.Assert.assertEquals(Assert.java:126)
//	at org.junit.Assert.assertEquals(Assert.java:145)
//	at edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper.FunkGoMFDictionaryEntryModifierTest.testCHEBIDictEntryModifier(FunkGoMFDictionaryEntryModifierTest.java:56)
//	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
//	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//	at java.lang.reflect.Method.invoke(Method.java:498)
//	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)
//	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)
//	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)
//	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)
//	at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)
//	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)
//	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)
//	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
//	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
//	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
//	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
//	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
//	at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
//	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
//	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
//	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:459)
//	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:675)
//	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:382)
//	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)



