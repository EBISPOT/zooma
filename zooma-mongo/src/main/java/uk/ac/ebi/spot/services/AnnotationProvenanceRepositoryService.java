package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleAnnotationProvenance;
import uk.ac.ebi.spot.repositories.AnnotationProvenanceRepository;

import java.util.List;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Service
public class AnnotationProvenanceRepositoryService implements RepositoryService<SimpleAnnotationProvenance> {

    @Autowired
    AnnotationProvenanceRepository annotationProvenanceRepository;

    @Override
    public List<SimpleAnnotationProvenance> getAllDocuments() {
        return annotationProvenanceRepository.findAll();
    }

    @Override
    public List<SimpleAnnotationProvenance> getAllDocuments(Sort sort) {
        return annotationProvenanceRepository.findAll(sort);
    }

    @Override
    public Page<SimpleAnnotationProvenance> getAllDocuments(Pageable pageable) {
        return annotationProvenanceRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleAnnotationProvenance document) throws RuntimeException {
        annotationProvenanceRepository.delete(document);
    }

    @Override
    public SimpleAnnotationProvenance create(SimpleAnnotationProvenance document) throws RuntimeException {
        return annotationProvenanceRepository.insert(document);
    }

    @Override
    public SimpleAnnotationProvenance save(SimpleAnnotationProvenance document) throws RuntimeException {
        return annotationProvenanceRepository.save(document);
    }

    @Override
    public SimpleAnnotationProvenance update(SimpleAnnotationProvenance document) throws RuntimeException {
        return annotationProvenanceRepository.save(document);
    }

    @Override
    public SimpleAnnotationProvenance get(String documentId) {
        return annotationProvenanceRepository.findOne(documentId);
    }
}
