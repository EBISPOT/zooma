package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleAnnotation;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.repositories.AnnotationRepository;

import java.util.List;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Service
public class AnnotationRepositoryService implements RepositoryService<SimpleAnnotation> {

    @Autowired
    AnnotationRepository annotationRepository;


    @Override
    public List<SimpleAnnotation> getAllDocuments() {
        return annotationRepository.findAll();
    }

    @Override
    public List<SimpleAnnotation> getAllDocuments(Sort sort) {
        return annotationRepository.findAll(sort);
    }

    @Override
    public Page<SimpleAnnotation> getAllDocuments(Pageable pageable) {
        return annotationRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleAnnotation document) throws RuntimeException {
        annotationRepository.delete(document);
    }

    @Override
    public SimpleAnnotation create(SimpleAnnotation document) throws RuntimeException {
        return annotationRepository.insert(document);
    }

    @Override
    public SimpleAnnotation save(SimpleAnnotation document) throws RuntimeException {
        return annotationRepository.save(document);
    }

    @Override
    public SimpleAnnotation update(SimpleAnnotation document) throws RuntimeException {
        return annotationRepository.save(document);
    }

    @Override
    public SimpleAnnotation get(String documentId) {
        return annotationRepository.findOne(documentId);
    }

    public SimpleAnnotation getByAnnotatedProperty(Property property) {
        return annotationRepository.findByAnnotatedProperty(property);
    }

}
