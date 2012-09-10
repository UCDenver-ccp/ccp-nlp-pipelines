/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileArchiveUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.nlp.core.uima.util.TypeSystemUtil;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.converters.SlotValueToClassMentionPromoter_AE;
import edu.ucdenver.ccp.nlp.ext.uima.annotators.filter.OntologyClassRemovalFilter_AE;
import edu.ucdenver.ccp.nlp.ext.uima.collections.file.ClasspathCollectionReader;
import edu.ucdenver.ccp.nlp.uima.serialization.xmi.XmiLoaderAE;
import edu.ucdenver.ccp.nlp.uima.serialization.xmi.XmiLoaderAE.XmiFileCompressionType;
import edu.ucdenver.ccp.nlp.uima.serialization.xmi.XmiLoaderAE.XmiPathType;
//import edu.ucdenver.ccp.nlp.ext.uima.shims.annotation.impl.CcpAnnotationDataExtractor;
import edu.ucdenver.ccp.nlp.uima.shims.annotation.impl.CcpAnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.document.DocumentMetadataHandler;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class CraftPipelineUtil {

	private final static String CLEARTK_SYNTAX_TYPESYSTEM = "org.cleartk.syntax.TypeSystem";
	private static final String ORGANISM_CLASS_NAME = "organism";
	private static final String ORGANISM_TAXONOMY_ID_SLOT_NAME = "taxonomy ID";
	private static final String GO_BP_ROOT_ID = "GO:0008150";
	private static final String GO_MF_ROOT_ID = "GO:0003674";
	private static final String GO_CC_ROOT_ID = "GO:0005575";

	public static final String CHEBI_OBO_PATH = "/craft/ontologies/CHEBI.obo.gz";
	public static final String CL_OBO_PATH = "/craft/ontologies/CL.obo.gz";
	public static final String GO_OBO_PATH = "/craft/ontologies/GO.obo.gz";
	public static final String NCBI_TAXON_OBO_PATH = "/craft/ontologies/NCBITaxon.obo.gz";
	public static final String PR_OBO_PATH = "/craft/ontologies/PR.obo.gz";
	public static final String SO_OBO_PATH = "/craft/ontologies/SO.obo.gz";

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
	 * 
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
		PRO(TypeSystemUtil.CCP_TYPE_SYSTEM, "pro", CollectionsUtil.createList("PR:\\d+", "GO:\\d+", "CHEBI:\\d+",
				"SO:\\d+")),
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

		private CraftConceptType(String[] typeSystems, String xmiPath, Collection<String> conceptTypeRegexes) {
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

		public static TypeSystemDescription getTypeSystem(Set<CraftConceptType> types) {
			Collection<String> typeSystemStrs = getTypeSystemStrs(types);
			return TypeSystemDescriptionFactory.createTypeSystemDescription(new ArrayList<String>(typeSystemStrs)
					.toArray(new String[0]));
		}

		public static Collection<String> getTypeSystemStrs(Set<CraftConceptType> types) {
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

	public enum CraftAnnotationFilterOp {
		REMOVE_GO_BP,
		REMOVE_GO_MF,
		REMOVE_GO_BPMF,
		REMOVE_GO_CCMF,
		REMOVE_GO_CCBP,
		NONE
	}

	/**
	 * @param craftVersion
	 * @param tsd
	 * @param documentMetadataExtractorClass
	 * @return a {@link CollectionReader} component for the specified {@link CraftVersion}
	 * @throws ResourceInitializationException
	 */
	public static CollectionReader getCraftCollectionReader(CraftVersion craftVersion, TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetadataExtractorClass)
			throws ResourceInitializationException {
		return ClasspathCollectionReader.createCollectionReader(tsd, craftVersion.txtPath(), 0, -1,
				documentMetadataExtractorClass);
	}

	/**
	 * @param craftVersion
	 * @param conceptTypesToLoad
	 * @param tsd
	 * @param documentMetaDataExtractorClass
	 * @param annotFilterOp
	 *            used to filter out GO_BP or GO_MF terms (b/c they were initially annotated
	 *            together and are therefore stored together)
	 * @return a collection of {@link AnalysisEngineDescription} representing {@link XmiLoaderAE}
	 *         components set up to load the specified {@link CraftConceptType} types.
	 * @throws ResourceInitializationException
	 */
	public static List<AnalysisEngineDescription> getCraftAnnotationLoaderDescriptions(CraftVersion craftVersion,
			Set<CraftConceptType> conceptTypesToLoad, TypeSystemDescription tsd,
			Class<? extends DocumentMetadataHandler> documentMetaDataExtractorClass,
			CraftAnnotationFilterOp annotFilterOp) throws ResourceInitializationException {
		List<AnalysisEngineDescription> descList = new ArrayList<AnalysisEngineDescription>();
		for (CraftConceptType conceptType : conceptTypesToLoad) {
			String xmiPath = conceptType.xmiPath(craftVersion);
			descList.add(XmiLoaderAE.createAnalysisEngineDescription(tsd, documentMetaDataExtractorClass,
					XmiPathType.CLASSPATH, XmiFileCompressionType.GZ, xmiPath));
			if (conceptType.equals(CraftConceptType.NCBITAXON)) {
				descList.add(getNcbiTaxonomyIdentifierPromoterAe(tsd));
			}
		}

		if (annotFilterOp.equals(CraftAnnotationFilterOp.REMOVE_GO_BP)) {
			File oboFile = getGoOboFileReference();
			descList.add(OntologyClassRemovalFilter_AE.getDescription(
				tsd, CcpAnnotationDataExtractor.class,
					GO_BP_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
		} else if (annotFilterOp.equals(CraftAnnotationFilterOp.REMOVE_GO_MF)) {
			File oboFile = getGoOboFileReference();
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_MF_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
		} else if (annotFilterOp.equals(CraftAnnotationFilterOp.REMOVE_GO_BPMF)) {
			File oboFile = getGoOboFileReference();
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_BP_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_MF_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
		} else if (annotFilterOp.equals(CraftAnnotationFilterOp.REMOVE_GO_CCMF)) {
			File oboFile = getGoOboFileReference();
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_CC_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_MF_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
		} else if(annotFilterOp.equals(CraftAnnotationFilterOp.REMOVE_GO_CCBP)) {
			File oboFile = getGoOboFileReference();
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_CC_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
			descList.add(OntologyClassRemovalFilter_AE.getDescription(tsd, CcpAnnotationDataExtractor.class,
					GO_BP_ROOT_ID, oboFile, CharacterEncoding.UTF_8));
		}
		return descList;
	}

	/**
	 * @return a reference to the GO obo file. It will be copied from the classpath to a temporary
	 *         file.
	 */
	private static File getGoOboFileReference() {
		try {
			File oboFile = FileUtil.createTempFile("goOboFile", "obo.gz");
			ClassPathUtil.copyClasspathResourceToFile(GO_OBO_PATH, oboFile);
			return FileArchiveUtil.unzip(oboFile, oboFile.getParentFile(), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The taxonomy Ids are stored as slot values to "organism" annotations in the CRAFT NCBITaxon
	 * project. Here we add a component that promotes the slot values (taxonomy IDs) to full-fledged
	 * annotations. The organism annotations are then removed from the CAS.
	 * 
	 * @param tsd
	 * 
	 * @return
	 * @throws ResourceInitializationException
	 */
	private static AnalysisEngineDescription getNcbiTaxonomyIdentifierPromoterAe(TypeSystemDescription tsd)
			throws ResourceInitializationException {
		boolean transferSlotValues = false;
		boolean deleteSourceAnnotation = true;
		return SlotValueToClassMentionPromoter_AE.createAnalysisEngineDescription(tsd, ORGANISM_TAXONOMY_ID_SLOT_NAME,
				ORGANISM_CLASS_NAME, transferSlotValues, deleteSourceAnnotation, "NCBITaxon:");
	}

}
