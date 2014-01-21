package uk.ac.ebi.fgpt.zooma.view;

import uk.ac.ebi.fgpt.zooma.model.*;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 20/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class AnnotationUpdateRequest {

    String propertyType;
    String propertyValue;
    Collection<URI> semanticTags;
    boolean retainSemanticTags;

    public AnnotationUpdateRequest() {
        this.semanticTags = Collections.emptySet();
        this.retainSemanticTags = true;
    }

    public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    public void setSemanticTags(Collection<URI> semanticTags) {
        this.semanticTags = semanticTags;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public boolean isRetainSemanticTags() {
        return retainSemanticTags;
    }

    public void setRetainSemanticTags(boolean retainSemanticTags) {
        this.retainSemanticTags = retainSemanticTags;
    }
}
