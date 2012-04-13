/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.core.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.ext.uima.collections.file.ClasspathCollectionReader;
import edu.ucdenver.ccp.nlp.ext.uima.serialization.xmi.XmiLoaderAE;
import edu.ucdenver.ccp.nlp.ext.uima.serialization.xmi.XmiLoaderAE.XmiFileCompressionType;
import edu.ucdenver.ccp.nlp.ext.uima.serialization.xmi.XmiLoaderAE.XmiPathType;
import edu.ucdenver.ccp.nlp.ext.uima.shims.document.DocumentMetaDataExtractor;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class CraftPipelineUtil {

	private final static String CLEARTK_SYNTAX_TYPESYSTEM = "org.cleartk.syntax.TypeSystem";

	public enum CraftVersion {
		RELEASE("craft/release/txt", "craft/release/xmi"),
		RESERVE_A("craft/reserve_a/txt", "craft/reserve_a/xmi"),
		RESERVE_B("craft/reserve_b/txt", "craft/reserve_b/xmi");

		private final String txtPath;
		private final String xmiPath;

		private CraftVersion(String txtPath, String xmiPath) {
			this.txtPath = txtPath;
			this.xmiPath = xmiPath;
		}

		/**
		 * @return the txtPath
		 */
		public String txtPath() {
			return txtPath;
		}

		/**
		 * @return the xmiPath
		 */
		public String xmiPath() {
			return xmiPath;
		}

	}

	/**
	 * TODO: check regex collections for each type
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 *
	 */
	public enum CraftConceptType {
		ALL(new String[] { TypeSystemUtil.CCP_TYPE_SYSTEM, CLEARTK_SYNTAX_TYPESYSTEM }, "all", null),
		CHEBI(TypeSystemUtil.CCP_TYPE_SYSTEM, "chebi", CollectionsUtil.createList("CHEBI:\\d+")),
		CL(TypeSystemUtil.CCP_TYPE_SYSTEM, "cl", CollectionsUtil.createList("CL:\\d+")),
		EG(TypeSystemUtil.CCP_TYPE_SYSTEM, "eg", CollectionsUtil.createList("EG:\\d+")),
		GO_BP_MF(TypeSystemUtil.CCP_TYPE_SYSTEM, "go_bpmf", CollectionsUtil.createList("GO:\\d+")),
		GO_CC(TypeSystemUtil.CCP_TYPE_SYSTEM, "go_cc", CollectionsUtil.createList("GO:\\d+")),
		NCBITAXON(TypeSystemUtil.CCP_TYPE_SYSTEM, "ncbitaxon", CollectionsUtil.createList("NCBITaxon:\\d+")),
		PRO(TypeSystemUtil.CCP_TYPE_SYSTEM, "pro", CollectionsUtil.createList("PR:\\d+","GO:\\d+","CHEBI:\\d+","SO:\\d+")),
		SO(TypeSystemUtil.CCP_TYPE_SYSTEM, "so", CollectionsUtil.createList("SO:\\d+")),
		TREEBANK(CLEARTK_SYNTAX_TYPESYSTEM, "treebank", null);

		private final String[] typeSystems;
		private final String xmiPath;
		private final Collection<String> conceptTypeRegexes;

		private CraftConceptType(String typeSystem, String xmiPath, Collection<String> conceptTypeRegexes) {
			this.conceptTypeRegexes = conceptTypeRegexes;
			this.typeSystems = new String[] { typeSystem };
			this.xmiPath = xmiPath;
		}

		private CraftConceptType(String[] typeSystems, String xmiPath,Collection<String> conceptTypeRegexes) {
			this.typeSystems = typeSystems;
			this.xmiPath = xmiPath;
			this.conceptTypeRegexes = conceptTypeRegexes;
		}

		/**
		 * @return the typeSystem
		 */
		public String[] typeSystems() {
			return typeSystems;
		}

		public static TypeSystemDescription getTypeSystem(EnumSet<CraftConceptType> types) {
			Collection<String> typeSystemStrs = getTypeSystemStrs(types);
			return TypeSystemDescriptionFactory.createTypeSystemDescription(new ArrayList<String>(typeSystemStrs)
					.toArray(new String[0]));
		}
		
		public static Collection<String> getTypeSystemStrs(EnumSet<CraftConceptType> types) {
			Set<String> typeSystemStrs = new HashSet<String>();
			for (CraftConceptType type : types) {
				typeSystemStrs.addAll(Arrays.asList(type.typeSystems()));
			}
			return typeSystemStrs;
		}

		/**
		 * @return the xmiPath
		 */
		public String xmiPath(CraftVersion version) {
			return version.xmiPath() + "/" + xmiPath;
		}

		/**
		 * @return the conceptTypeRegexes
		 */
		public Collection<String> conceptTypeRegexes() {
			return conceptTypeRegexes;
		}

	}

	/**
	 * @param craftVersion
	 * @param tsd
	 * @param documentMetadataExtractorClass
	 * @return a {@link CollectionReader} component for the specified {@link CraftVersion}
	 * @throws ResourceInitializationException
	 */
	public static CollectionReader getCraftCollectionReader(CraftVersion craftVersion, TypeSystemDescription tsd,
			Class<? extends DocumentMetaDataExtractor> documentMetadataExtractorClass)
			throws ResourceInitializationException {
		return ClasspathCollectionReader.createCollectionReader(tsd, craftVersion.txtPath(), 0, -1,
				documentMetadataExtractorClass);
	}

	/**
	 * @param craftVersion
	 * @param conceptTypesToLoad
	 * @param tsd
	 * @param documentMetaDataExtractorClass
	 * @return a collection of {@link AnalysisEngineDescription} representing {@link XmiLoaderAE}
	 *         components set up to load the specified {@link CraftConceptType} types.
	 * @throws ResourceInitializationException
	 */
	public static List<AnalysisEngineDescription> getCraftAnnotationLoaderDescriptions(CraftVersion craftVersion,
			EnumSet<CraftConceptType> conceptTypesToLoad, TypeSystemDescription tsd,
			Class<? extends DocumentMetaDataExtractor> documentMetaDataExtractorClass)
			throws ResourceInitializationException {
		List<AnalysisEngineDescription> descList = new ArrayList<AnalysisEngineDescription>();
		for (CraftConceptType conceptType : conceptTypesToLoad) {
			String xmiPath = conceptType.xmiPath(craftVersion);
			descList.add(XmiLoaderAE.createAnalysisEngineDescription(tsd, documentMetaDataExtractorClass,
					XmiPathType.CLASSPATH, XmiFileCompressionType.GZ, xmiPath));
		}
		return descList;
	}

}
