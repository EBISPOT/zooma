package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.datasource.AbstractAnnotationLoadingSession;

import java.net.URI;

/**
 * An annotation loading session that is capable of minting URIs specific to the Expression Atlas
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public abstract class ExpressionAtlasLoadingSession extends AbstractAnnotationLoadingSession {
    protected ExpressionAtlasLoadingSession() {
        super();
    }

    protected ExpressionAtlasLoadingSession(URI defaultBiologicalEntityUri,
                                            URI defaultStudyEntityUri) {
        super(defaultBiologicalEntityUri, defaultStudyEntityUri);
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "atlas/" + encode(studyAccession));
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "atlas/" + annotationID);
    }
}
