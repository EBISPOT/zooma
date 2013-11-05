package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * An annotation loading session that is capable of minting URIs specific to the Atlas
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public abstract class AtlasLoadingSession extends AbstractAnnotationLoadingSession {

    protected AtlasLoadingSession() {
        super();
    }

    protected AtlasLoadingSession(Collection<URI> defaultBiologicalEntityUris, Collection<URI> defaultStudyEntityUris) {
        super(defaultBiologicalEntityUris, defaultStudyEntityUris);
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" + encode(studyAccession));
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" + annotationID);
    }

}
