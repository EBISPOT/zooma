package uk.ac.ebi.fgpt.zooma.model;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.util.CollectionUtils;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

/**
 * A basic implementaton of an Annotation Summary.  This object collects unqiue combinations of properties and URIs and
 * exposes them.
 *
 * @author Tony Burdett
 * @date 28/05/12
 */
public class SimpleAnnotationSummary implements AnnotationSummary {
    private String id;
    private String propertyType;
    private String propertyValue;
    private Collection<URI> semanticTags;
    private Collection<URI> annotationURIs;
    private float qualityScore;



    public SimpleAnnotationSummary(String id,
                                   String propertyType,
                                   String propertyValue,
                                   Collection<URI> semanticTags,
                                   Collection<URI> annotationURIs,
                                   float qualityScore) {
        this.id = id;
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
        this.semanticTags = semanticTags;
        this.annotationURIs = annotationURIs;
        this.qualityScore = qualityScore;
    }

    @Override public String getAnnotationSummaryTypeID() {
        StringBuilder typeName = new StringBuilder();
        typeName.append("summary_from_").append(getAnnotatedPropertyType()).append("_to_");
        Iterator<URI> uriIt = getSemanticTags().iterator();
        while (uriIt.hasNext()) {
            URI uri = uriIt.next();
            String shortform = URIUtils.extractFragment(uri);
            typeName.append(shortform);
            if (uriIt.hasNext()) {
                typeName.append("+");
            }
        }
        return typeName.toString();
    }

    @Override public String getAnnotationSummaryTypeName() {
        StringBuilder typeName = new StringBuilder();
        typeName.append(getAnnotatedPropertyType()).append("; ");
        Iterator<URI> uriIt = getSemanticTags().iterator();
        while (uriIt.hasNext()) {
            URI uri = uriIt.next();
            String shortform = URIUtils.extractFragment(uri);
            typeName.append(shortform);
            if (uriIt.hasNext()) {
                typeName.append(", ");
            }
        }
        return typeName.toString();
    }

    @Override public String getID() {
        return id;
    }

    @Override public String getAnnotatedPropertyValue() {
        return propertyValue;
    }

    @Override public String getAnnotatedPropertyType() {
        return propertyType;
    }

    @Override public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    @Override public Collection<URI> getAnnotationURIs() {
        return annotationURIs;
    }

    @Override public float getQualityScore() {
        return qualityScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleAnnotationSummary that = (SimpleAnnotationSummary) o;

        if (annotationURIs != null
            ? !CollectionUtils.compareCollectionContents(annotationURIs, that.annotationURIs)
            : that.annotationURIs != null) {
            return false;
        }
        if (semanticTags != null
            ? !CollectionUtils.compareCollectionContents(semanticTags, that.semanticTags)
            : that.semanticTags != null) {
            return false;
        }
        if (propertyType != null ? !propertyType.equals(that.propertyType) : that.propertyType != null) {
            return false;
        }
        if (propertyValue != null ? !propertyValue.equals(that.propertyValue) : that.propertyValue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyType != null ? propertyType.hashCode() : 0;
        result = 31 * result + (propertyValue != null ? propertyValue.hashCode() : 0);
        result = 31 * result + (semanticTags != null ? semanticTags.hashCode() : 0);
        result = 31 * result + (annotationURIs != null ? annotationURIs.hashCode() : 0);
        return result;
    }

    @Override
    public URI getURI() {
        return URI.create(Namespaces.ZOOMA_RESOURCE + "annotation_summary/" + getID());
    }

    @Override
    public String toString() {
        return "SimpleAnnotationSummary{" +
                "id='" + id + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", semanticTags=" + semanticTags +
                ", annotationURIs=" + annotationURIs +
                ", qualityScore=" + qualityScore +
                '}';
    }

}
