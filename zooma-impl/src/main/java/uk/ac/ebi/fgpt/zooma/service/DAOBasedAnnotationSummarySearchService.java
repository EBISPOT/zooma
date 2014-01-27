package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationSummaryDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 24/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class DAOBasedAnnotationSummarySearchService implements AnnotationSummarySearchService {

    private AnnotationSummaryDAO annotationSummaryDAO;

    public AnnotationSummaryDAO getAnnotationSummaryDAO() {
        return annotationSummaryDAO;
    }

    public void setAnnotationSummaryDAO(AnnotationSummaryDAO annotationSummaryDAO) {
        this.annotationSummaryDAO = annotationSummaryDAO;
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyValuePattern, URI... sources) {
        return search(null, propertyValuePattern, sources);
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern, URI... sources) {
        Collection<AnnotationSummary> summaries = new HashSet<>();
        for (AnnotationSummary summary : annotationSummaryDAO.matchByProperty(propertyType, propertyValuePattern)) {
            if (CollectionUtils.containsAny(summary.getAnnotationSourceURIs(), Arrays.asList(sources))) {
                summaries.add(summary);
            }
        }
        return summaries;
    }

    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix, URI... sources) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix, URI... sources) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<AnnotationSummary> searchBySemanticTags(String... semanticTagShortnames) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<AnnotationSummary> searchBySemanticTags(URI... semanticTags) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern, List<URI> preferredSources, URI... requiredSources) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<AnnotationSummary> searchByPreferredSources(String propertyType, String propertyValuePattern, List<URI> preferredSources, URI... requiredSources) {
        throw new NotImplementedException();
    }
}
