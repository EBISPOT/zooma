package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleBiologicalEntity;
import uk.ac.ebi.spot.repositories.BiologicalEntityRepository;

import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Service
public class BiologicalEntityRepositoryService implements RepositoryService<SimpleBiologicalEntity> {

    @Autowired
    BiologicalEntityRepository biologicalEntityRepository;

    @Override
    public List<SimpleBiologicalEntity> getAllDocuments() {
        return biologicalEntityRepository.findAll();
    }

    @Override
    public List<SimpleBiologicalEntity> getAllDocuments(Sort sort) {
        return biologicalEntityRepository.findAll(sort);
    }

    @Override
    public Page<SimpleBiologicalEntity> getAllDocuments(Pageable pageable) {
        return biologicalEntityRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleBiologicalEntity document) throws RuntimeException {
        biologicalEntityRepository.delete(document);
    }

    @Override
    public SimpleBiologicalEntity create(SimpleBiologicalEntity document) throws RuntimeException {
        return biologicalEntityRepository.insert(document);
    }

    @Override
    public SimpleBiologicalEntity save(SimpleBiologicalEntity document) throws RuntimeException {
        return biologicalEntityRepository.save(document);
    }

    @Override
    public SimpleBiologicalEntity update(SimpleBiologicalEntity document) throws RuntimeException {
        return biologicalEntityRepository.save(document);
    }

    @Override
    public SimpleBiologicalEntity get(String documentId) {
        return biologicalEntityRepository.findOne(documentId);
    }
}
