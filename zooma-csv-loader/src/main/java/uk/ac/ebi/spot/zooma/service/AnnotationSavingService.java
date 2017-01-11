package uk.ac.ebi.spot.zooma.service;

/**
 * Created by olgavrou on 02/10/2016.
 */
public interface AnnotationSavingService<T> {
    void save(T domainObject);
}
