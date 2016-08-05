package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleStudy;
import uk.ac.ebi.spot.repositories.StudyRepository;

import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Service
public class StudyRepositoryService implements RepositoryService<SimpleStudy> {

    @Autowired
    StudyRepository studyRepository;

    @Override
    public List<SimpleStudy> getAllDocuments() {
        return studyRepository.findAll();
    }

    @Override
    public List<SimpleStudy> getAllDocuments(Sort sort) {
        return studyRepository.findAll(sort);
    }

    @Override
    public Page<SimpleStudy> getAllDocuments(Pageable pageable) {
        return studyRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleStudy document) throws RuntimeException {
        studyRepository.delete(document);
    }

    @Override
    public SimpleStudy create(SimpleStudy document) throws RuntimeException {
        return studyRepository.insert(document);
    }

    @Override
    public SimpleStudy save(SimpleStudy document) throws RuntimeException {
        return studyRepository.save(document);
    }

    @Override
    public SimpleStudy update(SimpleStudy document) throws RuntimeException {
        return studyRepository.save(document);
    }

    @Override
    public SimpleStudy get(String documentId) {
        return studyRepository.findOne(documentId);
    }
}
