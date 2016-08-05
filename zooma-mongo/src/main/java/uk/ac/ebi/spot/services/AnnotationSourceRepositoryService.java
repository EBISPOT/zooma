package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleAnnotationSource;
import uk.ac.ebi.spot.repositories.AnnotationSourceRepository;

import java.util.List;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Service
public class AnnotationSourceRepositoryService implements RepositoryService<SimpleAnnotationSource> {


    @Autowired
    AnnotationSourceRepository annotationSourceRepository;

    @Override
    public List<SimpleAnnotationSource> getAllDocuments() {
        return annotationSourceRepository.findAll();
    }

    @Override
    public List<SimpleAnnotationSource> getAllDocuments(Sort sort) {
        return annotationSourceRepository.findAll(sort);
    }

    @Override
    public Page<SimpleAnnotationSource> getAllDocuments(Pageable pageable) {
        return annotationSourceRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleAnnotationSource document) throws RuntimeException {
        annotationSourceRepository.delete(document);
    }

    @Override
    public SimpleAnnotationSource create(SimpleAnnotationSource document) throws RuntimeException {
        return annotationSourceRepository.insert(document);
    }

    @Override
    public SimpleAnnotationSource save(SimpleAnnotationSource document) throws RuntimeException {
        return annotationSourceRepository.save(document);
    }

    @Override
    public SimpleAnnotationSource update(SimpleAnnotationSource document) throws RuntimeException {
        return annotationSourceRepository.save(document);
    }

    @Override
    public SimpleAnnotationSource get(String documentId) {
        return annotationSourceRepository.findOne(documentId);
    }
}
