package uk.ac.ebi.fgpt.zooma.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

    private AnnotationSource currentAnnotationSource;
    private URI currentNamespace;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public TransactionalAnnotationLoadingSession() {
        this.lock = new Semaphore(1);
    }

    protected Logger getLog() {
        return log;
    }

    public AnnotationSource getCurrentAnnotationSource() {
        return currentAnnotationSource;
    }

    public synchronized void acquire(AnnotationSource annotationSource) throws InterruptedException {
        lock.acquire();
        try {
            this.currentAnnotationSource = annotationSource;
            this.currentNamespace = URI.create(
                    Namespaces.ZOOMA_RESOURCE.getURI().toString() +
                            URLEncoder.encode(annotationSource.getName().trim(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            getLog().warn("Couldn't create currentNamespace URI for " + annotationSource.getName());
        }
    }

    public synchronized void release() {
        lock.release();
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(currentNamespace.toString() + "/" + encode(studyAccession));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(currentNamespace.toString() + "/" + encode(studyAccessions[0]) + "/" + bioentityID);

    }

    @Override
    protected Collection<URI> mintBioentityURITypes(Collection<String> bioentityTypeName) {
        Set<URI> typeUris = new HashSet<URI>();
        for (String name : bioentityTypeName) {
            try {
                typeUris.add(URI.create(this.currentNamespace + URLEncoder.encode(name, "UTF-8")));
            }
            catch (UnsupportedEncodingException e) {
                getLog().error("Couldn't create a URI from bioentity type name: " + name);
            }

        }
        return typeUris;
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(currentNamespace.toString() + "/" + annotationID);
    }
}
