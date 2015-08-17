package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

/**
 * A concrete implementation of an annotation factory that supports acquiring and releasing methods, enabling clients to
 * lock the factory to a particular datasource whilst generating annotations
 *
 * @author Tony Burdett
 * @date 10/01/14
 */
public class TransactionalAnnotationFactory extends AbstractAnnotationFactory<TransactionalAnnotationLoadingSession> {
    public TransactionalAnnotationFactory(TransactionalAnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
    }

    @Override public TransactionalAnnotationLoadingSession getAnnotationLoadingSession() {
        return super.getAnnotationLoadingSession();
    }

    public synchronized void acquire(AnnotationSource annotationSource) throws InterruptedException {
        getAnnotationLoadingSession().acquire(annotationSource);
    }

    public synchronized void release() {
        getAnnotationLoadingSession().release();
    }
}
