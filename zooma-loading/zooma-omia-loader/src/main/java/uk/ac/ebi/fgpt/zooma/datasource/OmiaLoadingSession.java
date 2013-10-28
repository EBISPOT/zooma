package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * An OMIA annotation loading session that can generate URIs specific to annotations from OMIA.
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.PUBMED.getURI().toString() + studyAccession);
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/" + bioentityID);
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/" + annotationID);
    }

    @Override
    protected Collection<URI> mintBioentityURITypes(Collection<String> bioentityTypeName) {

        for (String name : bioentityTypeName) {
            if (name != null) {
                if (name.equals("gene")) {
                    return Collections.singleton(URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/" + "OMIA_GENE"));
                }
                else if (name.equals("phenotype")) {
                    return Collections.singleton(URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omia/" + "OMIA_PHENOTYPE"));
                }
            }
        }
        return Collections.emptySet();
    }
}
