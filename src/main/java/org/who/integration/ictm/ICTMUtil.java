package org.who.integration.ictm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICTMUtil {
	
	public static final String ICTM_TOP_CLASS = "http://who.int/ictm#ICTMCategory";
	public static final String EXT_REF_TERM_CLASS = "http://who.int/icd#ExternalReferenceTerm";
	public static final String CODES_PROP = "http://who.int/ictm#codes";
	
	//TODO: edit this to match the class in the new content model, under which
	//the classes should be imported
	public static final String ICTM_HANG_CLASS = "http://who.int/icd#ICTMCategory";
	
	public static final Map<String, String> ICTM2ICDTypes = new HashMap<String, String>() {{
		put("http://who.int/icd#InclusionTerm", "http://who.int/icd#BaseInclusionTerm");
		put("http://who.int/icd#ExclusionTerm","http://who.int/icd#BaseExclusionTerm");
	}};
	
	private static Collection<RDFSNamedClass> metaclses = new ArrayList<RDFSNamedClass>();
	private static Collection<RDFResource> linViewsTopCls;
	
	private static RDFProperty codesProp;
	
	
	public static boolean isICTMOntology(OWLModel owlModel) {
		return owlModel.getRDFSNamedClass(ICTM_TOP_CLASS) != null;
	}
	
	public static Collection<RDFResource> getEnglishTerms(OWLModel owlModel, RDFSNamedClass cls, RDFProperty prop) {
		List<RDFResource> terms = new ArrayList<RDFResource>();
		
		RDFProperty langProp = owlModel.getRDFProperty(ICDContentModelConstants.LANG_PROP);
		
		Collection<RDFResource> vals = cls.getPropertyValues(prop);
		for (RDFResource term : vals) {
			String lang = (String) term.getPropertyValue(langProp);
			if (lang == null || lang.length() == 0 || "en".equals(lang)) {
				terms.add(term);
			}
		}
		
		return terms;
	}
	
	public static RDFProperty getCodesProp(OWLModel owlModel) {
		if (codesProp == null) {
			codesProp = owlModel.getRDFProperty(CODES_PROP);
		}
		return codesProp;
	}
	
	public static String getICDCorrespondentType(String ictmType) {
		String icdType = ICTM2ICDTypes.get(ictmType);
		return icdType == null ? ictmType : icdType;
	}
	
	public static Collection<RDFSNamedClass> getMetaclasses(OWLModel owlModel) {
		if (metaclses.size() < 0) {
			return metaclses;
		}
		metaclses = new ArrayList<RDFSNamedClass>();
		metaclses.add(owlModel.getRDFSNamedClass("http://who.int/icd#DefinitionSection"));
		metaclses.add(owlModel.getRDFSNamedClass("http://who.int/icd#TermSection"));
		metaclses.add(owlModel.getRDFSNamedClass("http://who.int/icd#LinearizationSection"));
		//metaclses.add(owlModel.getRDFSNamedClass("http://who.int/icd#PostcoordinationSection"));
		//TODO: add an ICTM-specific metaclass
		return metaclses;
	}
	
	public static RDFSNamedClass getOrCreateCls(OWLModel owlModel, String name) {
		RDFSNamedClass cls = owlModel.getRDFSNamedClass(name);
		if (cls != null) {
			return cls;
		}
		cls = owlModel.createOWLNamedClass(name);
		
		for (RDFSNamedClass metacls : getMetaclasses(owlModel)) {
			cls.addRDFType(metacls);
		}
		
		return cls;
	}
	
	
	public static List<RDFSNamedClass> getSortedNamedSubclasses(RDFSNamedClass sourceCls) {
		List<RDFSNamedClass> namedSubclses = new ArrayList<RDFSNamedClass>();
		for (Object subcls : sourceCls.getSubclasses(false)) {
			if (subcls instanceof RDFSNamedClass) {
				namedSubclses.add((RDFSNamedClass) subcls);
			}
		}
		Collections.sort(namedSubclses, new Comparator<RDFSNamedClass>() {
			//works for ICTM, because it uses the sortingLabel prop for sorting
			@Override
			public int compare(RDFSNamedClass c1, RDFSNamedClass c2) {
				return c1.getBrowserText().compareTo(c2.getBrowserText());
			}
		});
		return namedSubclses;
	}
	
	
	public static Collection<RDFResource> getTopLevelLinViews(ICDContentModel cm) {
		if (linViewsTopCls == null) {
			linViewsTopCls = getLinearizationViewsFromCls(cm, cm.getICDCategoryClass(), cm.getLinearizationProperty());
		}
		return linViewsTopCls;
	}
	
	private static Collection<RDFResource> getLinearizationViewsFromCls(ICDContentModel cm, RDFSNamedClass cls, RDFProperty linProp) {
        Collection<RDFResource> linViews = new ArrayList<RDFResource>();
        Collection<RDFResource> linearizationSpecs = cls.getPropertyValues(linProp);

        for (RDFResource linearizationSpec : linearizationSpecs) {
            RDFResource linearizationView = (RDFResource) linearizationSpec.getPropertyValue(cm.getLinearizationViewProperty());
            if (linearizationView != null) {
                linViews.add(linearizationView);
            }
        }

        return linViews;
    }

	
	
}
