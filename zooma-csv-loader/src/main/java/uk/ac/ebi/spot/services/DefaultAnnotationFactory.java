package uk.ac.ebi.spot.services;

import uk.ac.ebi.spot.datasource.AnnotationLoadingSession;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 01/12/14
 */
public class DefaultAnnotationFactory extends AbstractAnnotationFactory<AnnotationLoadingSession> {

    private AnnotationLoadingSession annotationLoadingSession;

    @Override
    public AnnotationLoadingSession getAnnotationLoadingSession() {
        return annotationLoadingSession;
    }

    @Override
    public void setAnnotationLoadingSession(AnnotationLoadingSession annotationLoadingSession) {
        this.annotationLoadingSession = annotationLoadingSession;
    }
}
