package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A ZOOMA annotation search service that can be used to search over the set of {@link Annotation}s known to ZOOMA.
 * Searches can either be performed by looking for text present in the annotation (including property names, annotation
 * URIs, labels or provenance information) or explicitly by property name or type if present.
 * <p/>
 * This implementation requires a {@link PropertySearchService} to be set in order to defer property-based searches to
 * that service.  Once the properties are acquired, this implementation uses an {@link AnnotationService} to retrieve
 * annotations by property.
 *
 * @author Tony Burdett
 * @date 03/04/12
 * @see AnnotationService
 * @see PropertySearchService
 */
public class PropertyBasedAnnotationSearchService implements AnnotationSearchService {
    private AnnotationService annotationService;
    private PropertySearchService propertySearchService;

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    public PropertySearchService getPropertySearchService() {
        return propertySearchService;
    }

    public void setPropertySearchService(PropertySearchService propertySearchService) {
        this.propertySearchService = propertySearchService;
    }

    @Override public Collection<Annotation> search(String propertyValuePattern) {
        // find most relevant properties
        Collection<Property> properties = getPropertySearchService().search(propertyValuePattern);

        // create a list of annotations to preserve property-relevance order
        List<Annotation> results = new ArrayList<>();

        // find annotations for each of these properties
        for (Property property : properties) {
            Collection<Annotation> annotations = getAnnotationService().getAnnotationsByProperty(property);
            results.addAll(annotations);
        }
        return results;
    }

    @Override public Collection<Annotation> search(String propertyType, String propertyValuePattern) {
        // find most relevant properties
        Collection<Property> properties = getPropertySearchService().search(propertyType, propertyValuePattern);

        // create a list of annotations to preserve property-relevance order
        List<Annotation> results = new ArrayList<>();

        // find annotations for each of these properties
        for (Property property : properties) {
            Collection<Annotation> annotations = getAnnotationService().getAnnotationsByProperty(property);
            results.addAll(annotations);
        }
        return results;
    }

    @Override public Collection<Annotation> searchPrefix(String propertyValuePrefix) {
        // find most relevant properties
        Collection<Property> properties = getPropertySearchService().searchByPrefix(propertyValuePrefix);

        // create a list of annotations to preserve property-relevance order
        List<Annotation> results = new ArrayList<>();

        // find annotations for each of these properties
        for (Property property : properties) {
            Collection<Annotation> annotations = getAnnotationService().getAnnotationsByProperty(property);
            results.addAll(annotations);
        }
        return results;
    }

    @Override public Collection<Annotation> searchPrefix(String propertyType, String propertyValuePrefix) {
        // find most relevant properties
        Collection<Property> properties = getPropertySearchService().searchByPrefix(propertyType, propertyValuePrefix);

        // create a list of annotations to preserve property-relevance order
        List<Annotation> results = new ArrayList<>();

        // find annotations for each of these properties
        for (Property property : properties) {
            Collection<Annotation> annotations = getAnnotationService().getAnnotationsByProperty(property);
            results.addAll(annotations);
        }
        return results;
    }
}
