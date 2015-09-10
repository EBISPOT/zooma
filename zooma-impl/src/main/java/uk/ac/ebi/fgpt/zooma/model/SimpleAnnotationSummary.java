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
    private URI propertyUri;
    private String propertyType;
    private String propertyValue;
    private Collection<URI> semanticTags;
    private Collection<URI> annotationURIs;

    private Collection<URI> annotationSourceURIs;
    private float qualityScore;


    public SimpleAnnotationSummary(String id,
                                   URI propertyUri,
                                   String propertyType,
                                   String propertyValue,
                                   Collection<URI> semanticTags,
                                   Collection<URI> annotationURIs,
                                   float qualityScore,
                                   Collection<URI> annotationSourceURIs) {
        this.id = id;
        this.propertyUri = propertyUri;
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
        this.semanticTags = semanticTags;
        this.annotationURIs = annotationURIs;
        this.qualityScore = qualityScore;
        this.annotationSourceURIs = annotationSourceURIs;
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

    @Override public float getQuality() {
        return qualityScore;
    }

    @Override public Collection<URI> getAnnotationSourceURIs() { return annotationSourceURIs; }

    @Override public URI getAnnotatedPropertyUri() { return propertyUri; }

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
        if (annotationSourceURIs != null
                ? !CollectionUtils.compareCollectionContents(annotationSourceURIs, that.annotationSourceURIs)
                : that.annotationSourceURIs != null) {
            return false;
        }
        if (propertyUri != null ? !propertyUri.equals(that.propertyUri) : that.propertyUri != null) {
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
        result = 31 * result + (propertyUri != null ? propertyUri.hashCode() : 0);
        result = 31 * result + (propertyValue != null ? propertyValue.hashCode() : 0);
        result = 31 * result + (semanticTags != null ? semanticTags.hashCode() : 0);
        result = 31 * result + (annotationURIs != null ? annotationURIs.hashCode() : 0);
        result = 31 * result + (annotationSourceURIs != null ? annotationSourceURIs.hashCode() : 0);
        return result;
    }

    @Override
    public URI getURI() {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI() + "annotation_summary/" + getID());
    }


    @Override
    public String toString() {
        return "SimpleAnnotationSummary{" +
                "id='" + id + '\'' +
                ", propertyTUri='" + propertyUri + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", semanticTags=" + semanticTags +
                ", annotationURIs=" + annotationURIs +
                ", qualityScore=" + qualityScore +
                ", sources=" + annotationSourceURIs +
                '}';
    }

}
