package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * An abstract decorator of a {@link AnnotationSearchService}.  You should subclass this decorator to create different
 * decorations that add functionality to annotation searches.
 *
 * @author Tony Burdett
 * @date 02/08/13
 * @see AnnotationSearchService
 */
public abstract class AnnotationSearchServiceDecorator implements AnnotationSearchService {
    private final AnnotationSearchService _annotationSearchService;

    protected AnnotationSearchServiceDecorator(AnnotationSearchService annotationSearchService) {
        this._annotationSearchService = annotationSearchService;
    }

    @Override public Collection<Annotation> search(String propertyValuePattern) {
        return _annotationSearchService.search(propertyValuePattern);
    }

    @Override public Collection<Annotation> search(String propertyType, String propertyValuePattern) {
        return _annotationSearchService.search(propertyType, propertyValuePattern);
    }

    @Override public Collection<Annotation> searchByPrefix(String propertyValuePrefix) {
        return _annotationSearchService.searchByPrefix(propertyValuePrefix);
    }

    @Override public Collection<Annotation> searchByPrefix(String propertyType, String propertyValuePrefix) {
        return _annotationSearchService.searchByPrefix(propertyType, propertyValuePrefix);
    }
}
