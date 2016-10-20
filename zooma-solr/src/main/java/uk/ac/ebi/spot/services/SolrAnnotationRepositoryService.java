package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.repositories.SolrAnnotationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Service
public class SolrAnnotationRepositoryService implements RepositoryService<SolrAnnotation> {

    @Autowired
    SolrAnnotationRepository annotationSummaryRepository;


    public List<SolrAnnotation> getByAnnotatedPropertyValue(String annotatedPropertyValue){
       return annotationSummaryRepository.findByAnnotatedPropertyValue(annotatedPropertyValue);
    }

    public List<AnnotationSummary> getByAnnotatedPropertyValueGroupBySemanticTags(String annotatedPropertyValue){
        return annotationSummaryRepository.findByAnnotatedPropertyValueGroupBySemanticTags(annotatedPropertyValue);
    }


    @Override
    public List<SolrAnnotation> getAllDocuments() {
        List<SolrAnnotation> documents = new ArrayList<>();
        Iterable<SolrAnnotation> i = annotationSummaryRepository.findAll();
        i.forEach(documents::add);
        return documents;
    }

    @Override
    public List<SolrAnnotation> getAllDocuments(Sort sort) {
        List<SolrAnnotation> documents = new ArrayList<>();
        Iterable<SolrAnnotation> i = annotationSummaryRepository.findAll(sort);
        i.forEach(documents::add);
        return documents;
    }

    @Override
    public Page<SolrAnnotation> getAllDocuments(Pageable pageable) {
        return annotationSummaryRepository.findAll(pageable);
    }

    @Override
    public void delete(SolrAnnotation document) throws RuntimeException {
        annotationSummaryRepository.delete(document);
    }

    @Override
    public SolrAnnotation create(SolrAnnotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public SolrAnnotation save(SolrAnnotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public SolrAnnotation update(SolrAnnotation document) throws RuntimeException {
        return annotationSummaryRepository.save(document);
    }

    @Override
    public SolrAnnotation get(String documentId) {
        return annotationSummaryRepository.findOne(documentId);
    }
}
