package uk.ac.ebi.spot.zooma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.SolrAnnotationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Service
public class SolrAnnotationRepositoryService implements RepositoryService<Annotation> {

    @Autowired
    SolrAnnotationRepository annotationSummaryRepository;


    public List<Annotation> getByAnnotatedPropertyValue(String annotatedPropertyValue){
       return annotationSummaryRepository.findByPropertyValue(annotatedPropertyValue);
    }

    public List<AnnotationSummary> getAnnotationSummariesByPropertyValue(String annotatedPropertyValue){
        return annotationSummaryRepository.findAnnotationSummariesByPropertyValue(annotatedPropertyValue);
    }

    public List<AnnotationSummary> getAnnotationSummariesByPropertyValue(String annotatedPropertyValue, List<String> sourceNames){
        return annotationSummaryRepository.findAnnotationSummariesByPropertyValue(annotatedPropertyValue, sourceNames);
    }

    public List<AnnotationSummary> getAnnotationSummariesByPropertyValueAndPropertyType(String annotatedPropertyType, String annotatedPropertyValue){
        return annotationSummaryRepository.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue);
    }

    public List<AnnotationSummary> getAnnotationSummariesByPropertyValueAndPropertyType(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames){
        return annotationSummaryRepository.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue, sourceNames);
    }


    @Override
    public List<Annotation> getAllDocuments() {
        List<Annotation> documents = new ArrayList<>();
        Iterable<Annotation> i = annotationSummaryRepository.findAll();
        i.forEach(documents::add);
        return documents;
    }

    @Override
    public List<Annotation> getAllDocuments(Sort sort) {
        List<Annotation> documents = new ArrayList<>();
        Iterable<Annotation> i = annotationSummaryRepository.findAll(sort);
        i.forEach(documents::add);
        return documents;
    }

    @Override
    public Page<Annotation> getAllDocuments(Pageable pageable) {
        return annotationSummaryRepository.findAll(pageable);
    }

    @Override
    public void delete(Annotation document) throws RuntimeException {
        annotationSummaryRepository.delete(document);
    }

    @Override
    public Annotation create(Annotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public Annotation save(Annotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public Annotation update(Annotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public Annotation get(String documentId) {
        return annotationSummaryRepository.findOne(documentId);
    }
}
