package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 28/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class SimpleAnnotationPattern implements AnnotationPattern {

    private URI propertyURI;
    private String propertyType;
    private String propertyValue;
    private Collection<URI> semanticTags;
    private AnnotationSource annotationSource;
    private boolean isReplaced;


    public SimpleAnnotationPattern(URI propertyURI, String propertyType, String propertyValue, Collection<URI> semanticTags, AnnotationSource annotationSource, boolean isReplaced) {
        this.propertyURI = propertyURI;
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
        this.semanticTags = semanticTags;
        this.annotationSource = annotationSource;
        this.isReplaced = isReplaced;
    }

    @Override
    public URI getPropertyURI() {
        return propertyURI;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    @Override
    public AnnotationSource getAnnotationSource() {
        return annotationSource;
    }

    @Override
    public boolean isReplaced() {
        return isReplaced;
    }

    @Override
    public URI getURI() {
        return null;
    }

    @Override
    public String toString() {
        return "SimpleAnnotationPattern{" +
                "propertyURI=" + propertyURI +
                ", propertyType='" + propertyType + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", semanticTags=" + semanticTags +
                ", annotationSource=" + annotationSource.toString() +
                ", isReplaced=" + isReplaced +
                '}';
    }
}
