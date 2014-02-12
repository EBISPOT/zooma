package uk.ac.ebi.fgpt.zooma.model;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.Service;

import java.net.URI;
import java.rmi.UnexpectedException;
import java.text.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 20/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class SimpleAnnotationUpdate implements AnnotationUpdate {

    String propertyType;
    String propertyValue;
    Collection<URI> semanticTags;
    boolean retainSemanticTags;

    public SimpleAnnotationUpdate() {
        this.semanticTags = Collections.emptySet();
        this.retainSemanticTags = true;
    }

    @Override
    public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    @Override
    public void setSemanticTags(Collection<URI> semanticTags) {
        this.semanticTags = semanticTags;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    @Override
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean isRetainSemanticTags() {
        return retainSemanticTags;
    }

    @Override
    public void setRetainSemanticTags(boolean retainSemanticTags) {
        this.retainSemanticTags = retainSemanticTags;
    }

    @Override
    public void apply(Collection<Annotation> zoomaObjects, Service<Annotation> service) throws ZoomaUpdateException {
        if (service instanceof AnnotationService) {
            AnnotationService annotationService = (AnnotationService) service;
            annotationService.updatePreviousAnnotations(zoomaObjects, this);
        }
        else {
            throw new IllegalArgumentException("Unsupported service type " + service.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return "SimpleAnnotationUpdate{" +
                "propertyType='" + propertyType + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", semanticTags=" + semanticTags +
                ", retainSemanticTags=" + retainSemanticTags +
                '}';
    }
}
