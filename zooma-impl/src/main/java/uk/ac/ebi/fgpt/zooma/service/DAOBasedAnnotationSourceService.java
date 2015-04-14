package uk.ac.ebi.fgpt.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSourceDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

import java.net.URI;
import java.util.Collection;

/**
 * An annotation source service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.AnnotationSourceDAO}
 * to retrieve annotation sources
 *
 * @author Tony Burdett
 * @date 18/12/13
 */
public class DAOBasedAnnotationSourceService implements AnnotationSourceService {
    private AnnotationSourceDAO annotationSourceDAO;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AnnotationSourceDAO getAnnotationSourceDAO() {
        return annotationSourceDAO;
    }

    public void setAnnotationSourceDAO(AnnotationSourceDAO annotationSourceDAO) {
        this.annotationSourceDAO = annotationSourceDAO;
    }

    @Override public Collection<AnnotationSource> getAnnotationSources() {
        return getAnnotationSourceDAO().read();
    }

    @Override public AnnotationSource getAnnotationSource(String sourceName) {
        Collection<AnnotationSource> annotationSources = getAnnotationSourceDAO().readBySourceName(sourceName);
        if (annotationSources.size() > 0) {
            if (annotationSources.size() > 1) {
                getLog().warn("There are more than one AnnotationSources " +
                                      "with the name '" + sourceName + "' registered; " +
                                      "only the first will be returned");
            }
            return annotationSources.iterator().next();
        }
        else {
            return null;
        }
    }

    @Override public AnnotationSource getAnnotationSource(URI uri) {
        return getAnnotationSourceDAO().read(uri);
    }
}
