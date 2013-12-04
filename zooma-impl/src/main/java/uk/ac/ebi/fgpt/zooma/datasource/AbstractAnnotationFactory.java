package uk.ac.ebi.fgpt.zooma.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.InvalidDataFormatException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * An all-purpose factory class that can generate fully formed annotation objects and their dependants from a series of
 * strings.  Each factory instance should be configured with an AnnotationLoadingSession
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 01/10/12
 */
public abstract class AbstractAnnotationFactory implements AnnotationFactory {
    private AnnotationLoadingSession annotationLoadingSession;

    private long lastRequestTime = -1;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AbstractAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        this.annotationLoadingSession = annotationLoadingSession;
    }

    public AnnotationLoadingSession getAnnotationLoadingSession() {
        return annotationLoadingSession;
    }

    protected Logger getLog() {
        return log;
    }

    @Override public Annotation createAnnotation(URI annotationURI,
                                                 String annotationID,
                                                 String studyAccession,
                                                 URI studyURI,
                                                 String studyID,
                                                 URI studyType,
                                                 String bioentityName,
                                                 URI bioentityURI,
                                                 String bioentityID,
                                                 String bioentityTypeName,
                                                 URI bioentityTypeURI,
                                                 String propertyType,
                                                 String propertyValue,
                                                 URI propertyURI,
                                                 String propertyID,
                                                 URI semanticTag,
                                                 String annotator,
                                                 Date annotationDate) {
        // monitor for cache cleanup
        cacheMonitoring();

        Collection<URI> studyTypes = new HashSet<URI>();
        if (studyType!=null) {
            studyTypes.add(studyType);
        }

        Study s;
        if (studyURI != null) {
            if (studyType != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession,
                                                                   studyURI,
                                                                   studyTypes);
            }
            else {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyTypes);
            }
        }
        else {
            if (studyID != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyID, studyTypes);
            }
            else {
                if (studyAccession != null) {
                    s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyTypes);
                }
                else {
                    s = null;
                }
            }
        }


        BiologicalEntity be;
        Collection<String> bioEntityTypeNames = new HashSet<String>();
        if (bioentityTypeName != null) {
            bioEntityTypeNames.add(bioentityTypeName);
        }
        Collection<URI> bioEntityTypeURIs = new HashSet<URI>();
        if (bioentityTypeURI != null) {
            bioEntityTypeURIs.add(bioentityTypeURI);
        }

        if (s != null) {
            if (bioentityURI != null) {
                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                               bioentityURI,
                                                                               bioEntityTypeNames,
                                                                               bioEntityTypeURIs,
                                                                               s);
            }
            else {
                if (bioentityID != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   bioEntityTypeNames,
                                                                                   bioEntityTypeURIs,
                                                                                   s);
                }
                else {
                    if (bioentityName != null) {
                        be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                       bioEntityTypeNames,
                                                                                       bioEntityTypeURIs,
                                                                                       s);
                    }
                    else {
                        be = null;
                    }
                }
            }
        }
        else {
            if (bioentityURI != null) {
                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                               bioentityURI,
                                                                               bioEntityTypeNames,
                                                                               bioEntityTypeURIs);
            }
            else {
                if (bioentityID != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   bioEntityTypeNames,
                                                                                   bioEntityTypeURIs);
                }
                else {
                    if (bioentityName != null) {
                        be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                       bioEntityTypeNames,
                                                                                       bioEntityTypeURIs);
                    }
                    else {
                        be = null;
                    }
                }
            }
        }

        Property p;
        if (propertyURI != null) {
            p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue, propertyURI);
        }
        else {
            if (propertyID != null) {
                p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue, propertyID);
            }
            else {
                p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue);
            }
        }

        AnnotationProvenance prov;
        if (annotator != null) {
            if (annotationDate != null) {
                prov = getAnnotationProvenance(annotator, annotationDate);
            }
            else {
                throw new InvalidDataFormatException("ANNOTATOR supplied without a corresponding ANNOTATION_DATE");
            }
        }
        else {
            prov = getAnnotationProvenance();
        }

        // and return the complete annotation
        Annotation a;
        if (be != null) {
            if (annotationURI != null) {
                a = getAnnotationLoadingSession().getOrCreateAnnotation(p,
                                                                        prov,
                                                                        semanticTag,
                                                                        annotationURI,
                                                                        be);
            }
            else {
                if (annotationID != null) {
                    a = getAnnotationLoadingSession().getOrCreateAnnotation(p,
                                                                            prov,
                                                                            semanticTag,
                                                                            annotationID,
                                                                            be);
                }
                else {
                    a = getAnnotationLoadingSession().getOrCreateAnnotation(p,
                                                                            prov,
                                                                            semanticTag,
                                                                            be);
                }
            }
        }
        else {
            if (annotationURI != null) {
                a = getAnnotationLoadingSession().getOrCreateAnnotation(p,
                                                                        prov,
                                                                        semanticTag,
                                                                        annotationURI);
            }
            else {
                if (annotationID != null) {
                    a = getAnnotationLoadingSession().getOrCreateAnnotation(p,
                                                                            prov,
                                                                            semanticTag,
                                                                            annotationID);
                }
                else {
                    a = getAnnotationLoadingSession().getOrCreateAnnotation(p, prov, semanticTag);
                }
            }
        }
        return a;

    }

    protected abstract AnnotationProvenance getAnnotationProvenance();

    protected abstract AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate);

    protected abstract AnnotationProvenance getAnnotationProvenance(String annotator,
                                                                    AnnotationProvenance.Accuracy accuracy,
                                                                    Date annotationDate);

    private Thread t;

    private synchronized void cacheMonitoring() {
        if (t == null || !t.isAlive()) {
            t = new Thread(new Runnable() {
                @Override public void run() {
                    getLog().debug("Starting cache monitoring daemon thread " +
                                           "'" + Thread.currentThread().getName() + "'");
                    boolean cleanup = false;
                    while (!cleanup) {
                        // a request has been made
                        if (lastRequestTime > -1) {
                            // if the last request was more than 1 minute ago, clear the cache
                            long time = System.currentTimeMillis() - lastRequestTime;
                            String estimate = new DecimalFormat("#,###").format(((float) time) / 1000);
                            getLog().debug("Polling for cache cleanup - last request was " + estimate + "s ago.");
                            if (System.currentTimeMillis() - lastRequestTime > 60000) {
                                // if so, clear caches and allow to exit
                                getAnnotationLoadingSession().clearCaches();
                                cleanup = true;
                            }
                        }

                        if (!cleanup) {
                            // build in a delay
                            synchronized (this) {
                                try {
                                    wait(60000);
                                }
                                catch (InterruptedException e) {
                                    // just continue
                                }
                            }
                        }
                    }
                }
            }, AbstractAnnotationFactory.this.getClass().getSimpleName() + "-Cache-Daemon");
            t.setDaemon(true);
            t.start();
        }
        this.lastRequestTime = System.currentTimeMillis();
    }
}
