package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

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

    protected AtlasLoadingSession(URI defaultBiologicalEntityUri, URI defaultStudyEntityUri) {
        super(defaultBiologicalEntityUri, defaultStudyEntityUri);
    }

    @Override protected URI mintStudyURI(String studyID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" + encode(studyAccession));
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" + annotationID);
    }
}
