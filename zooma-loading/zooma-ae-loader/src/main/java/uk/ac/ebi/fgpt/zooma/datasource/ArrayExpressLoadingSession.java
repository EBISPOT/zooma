package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collection;

/**
 * An annotation loading session that is capable of minting URIs specific to ArrayExpress
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public abstract class ArrayExpressLoadingSession extends AbstractAnnotationLoadingSession {
    protected ArrayExpressLoadingSession() {
        super();
    }

    protected ArrayExpressLoadingSession(URI defaultBiologicalEntityUri,
                                         URI defaultStudyEntityUri) {
        super(defaultBiologicalEntityUri, defaultStudyEntityUri);
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "arrayexpress/" + encode(studyAccession));
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "arrayexpress/" + annotationID);
    }
}
