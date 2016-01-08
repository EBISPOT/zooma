package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A cache that can be used to store any 'inferred' {@link uk.ac.ebi.fgpt.zooma.model.AnnotationSummary} objects - that
 * is, an AnnotationSummary that has been inferred to exist from previous searches, as opposed to one that is directly
 * matched by a search.  As these AnnotationSummaries are only inferred to exist, they have no ids, and therefore the
 * {@link uk.ac.ebi.fgpt.zooma.view.SearchResponse} object that ZOOMA returns does not contain any IDs that clients can
 * use to retrieve further data about this summary.  This cache provides a mechanism to temporarily store inferred
 * AnnotationSummaries, keyed by HttpSession, enabling subsequent lookup.  The cache is emptied once
 *
 * @author Tony Burdett
 * @date 27/05/14
 */
public class InferredAnnotationSummaryCache extends TransientCacheable {
    private AtomicInteger counter;
    private final SessionMap sessionMap;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public InferredAnnotationSummaryCache() {
        super(600, 60);
        counter = new AtomicInteger(1);
        sessionMap = new SessionMap();
    }

    @Override protected boolean createCaches() {
        return true;
    }

    @Override protected synchronized boolean clearCaches() {
        getLog().debug("Clearing temporary AnnotationSummary caches");
        sessionMap.clear();
        return true;
    }

    public synchronized Collection<AnnotationSummary> retrieveAnnotationSummaries() {
        // notify cache to retain results
        ping();

        String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
        if (sessionMap.containsKey(sessionID)) {
            return sessionMap.get(sessionID).values();
        }
        else {
            return Collections.emptySet();
        }
    }

    public synchronized AnnotationSummary retrieveAnnotationSummary(String annotationSummaryID) {
        // notify cache to retain results
        ping();

        String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
        // retrieve from the same session; this insulates user requests and helps performance
        if (sessionMap.containsKey(sessionID)) {
            return sessionMap.get(sessionID).get(annotationSummaryID);
        }
        else {
            // no result for the current session; scan all
            getLog().warn("Attempting to retrieve annotation summary '" + annotationSummaryID + "' " +
                                  "for session '" + sessionID + "' yielded no results.  Checking all sessions...");
            for (AnnotationSummaryMap map : sessionMap.values()) {
                if (map.containsKey(annotationSummaryID)) {
                    return map.get(annotationSummaryID);
                }
            }
            getLog().error("Request for annotation summary '" + annotationSummaryID + "' was not found");
            return null;
        }
    }

    public synchronized AnnotationSummary cacheAnnotationSummary(AnnotationSummary annotationSummary) {
        if (annotationSummary.getID() != null) {
            throw new IllegalArgumentException(
                    "Only inferred annotation summaries (i.e. those with no ID) can be cached");
        }

        // notify cache to retain results
        ping();

        // generate a new annotation summary, this time with a temp ID
        String id = "temp:" + counter.getAndIncrement();
        AnnotationSummary cacheable = new SimpleAnnotationSummary(id,
                                                                  null,
                                                                  annotationSummary.getAnnotatedPropertyType(),
                                                                  annotationSummary.getAnnotatedPropertyValue(),
                                                                  annotationSummary.getSemanticTags(),
                                                                  annotationSummary.getAnnotationURIs(),
                                                                  annotationSummary.getQuality(),
                                                                  annotationSummary.getAnnotationSourceURIs());

        String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
        synchronized (sessionMap) {
            if (!sessionMap.containsKey(sessionID)) {
                sessionMap.put(sessionID, new AnnotationSummaryMap());
            }
            getLog().debug("Caching temporary annotation summary '" + id + "' for session '" + sessionID + "'");
            sessionMap.get(sessionID).put(id, cacheable);
        }
        return cacheable;
    }

    private class SessionMap extends HashMap<String, AnnotationSummaryMap> {
    }

    private class AnnotationSummaryMap extends HashMap<String, AnnotationSummary> {
    }
}
