package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;

import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * A concrete implementation of an annotation loading session that supports acquiring and releasing methods, enabling
 * clients to lock this session to a particular datasource whilst minting URIs.
 *
 * @author Tony Burdett
 * @date 10/01/14
 */
public class TransactionalAnnotationLoadingSession extends AbstractAnnotationLoadingSession {
    private final Semaphore lock;

    private AnnotationProvenanceTemplate currentProvenanceTemplate;

    protected TransactionalAnnotationLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(null,
                                                     AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null));
        this.lock = new Semaphore(1);
    }

    @Override
    public AnnotationProvenanceTemplate getAnnotationProvenanceTemplate() {
        // check lock owner?


        return this.currentProvenanceTemplate;
    }

    public synchronized void acquire(AnnotationSource annotationSource) throws InterruptedException {
        currentProvenanceTemplate = super.getAnnotationProvenanceTemplate().sourceIs(annotationSource);
        lock.acquire();
    }

    public synchronized void release() {
        lock.release();
    }
}
