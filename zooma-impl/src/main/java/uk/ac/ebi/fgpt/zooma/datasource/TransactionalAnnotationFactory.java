package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;

import java.util.Date;

/**
 * A concrete implementation of an annotation factory that supports acquiring and releasing methods, enabling clients to
 * lock the factory to a particular datasource whilst generating annotations
 *
 * @author Tony Burdett
 * @date 10/01/14
 */
public class TransactionalAnnotationFactory extends AbstractAnnotationFactory {
    public TransactionalAnnotationFactory(TransactionalAnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
    }

    public synchronized void acquire(AnnotationSource annotationSource) throws InterruptedException {
        ((TransactionalAnnotationLoadingSession) getAnnotationLoadingSession()).acquire(annotationSource);
    }

    public synchronized void release() {
        ((TransactionalAnnotationLoadingSession) getAnnotationLoadingSession()).release();
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        throw new UnsupportedOperationException("Unable to generate provenance without annotator information");
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return getAnnotationProvenance(annotator, AnnotationProvenance.Accuracy.NOT_SPECIFIED, annotationDate);
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator,
                                                                     AnnotationProvenance.Accuracy accuracy,
                                                                     Date annotationDate) {
        AnnotationSource annotationSource =
                ((TransactionalAnnotationLoadingSession) getAnnotationLoadingSession()).getCurrentAnnotationSource();
        return new SimpleAnnotationProvenance(annotationSource,
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              accuracy,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
