package edu.ucdenver.ccp.nlp.pipelines.conceptmapper.dictmod;

import edu.ucdenver.ccp.nlp.pipelines.conceptmapper.ConceptMapperDictionaryFileFactory.DictionaryNamespace;
import edu.ucdenver.ccp.nlp.wrapper.conceptmapper.dictionary.obo.DictionaryEntryModifier;

public class DictionaryEntryModifierFactory {

	public static DictionaryEntryModifier getDictionaryEntryModifier(DictionaryNamespace ns) {
		switch (ns) {
		case CHEBI:
			return new CHEBIDictionaryEntryModifier();
		case FUNK_GO_MF:
			return new FunkGoMFDictionaryEntryModifier();
		case PR:
			return new PRDictionaryEntryModifier();
		default:
			return null;
		}
	}

}
