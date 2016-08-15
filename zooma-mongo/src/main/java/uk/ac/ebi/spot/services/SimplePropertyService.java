package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.SimpleProperty;
import uk.ac.ebi.spot.repositories.PropertyRepository;

import java.util.List;

/**
 * Created by olgavrou on 09/08/2016.
 */
@Service
public class SimplePropertyService implements RepositoryService<SimpleProperty>{

    @Autowired
    PropertyRepository propertyRepository;

    @Override
    public List<SimpleProperty> getAllDocuments() {
        return propertyRepository.findAll();
    }

    @Override
    public List<SimpleProperty> getAllDocuments(Sort sort) {
        return propertyRepository.findAll(sort);
    }

    @Override
    public Page<SimpleProperty> getAllDocuments(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Override
    public void delete(SimpleProperty document) throws RuntimeException {
        propertyRepository.delete(document);
    }

    @Override
    public SimpleProperty create(SimpleProperty document) throws RuntimeException {
        return propertyRepository.insert(document);
    }

    @Override
    public SimpleProperty save(SimpleProperty document) throws RuntimeException {
        return propertyRepository.save(document);
    }

    @Override
    public SimpleProperty update(SimpleProperty document) throws RuntimeException {
        return propertyRepository.save(document);
    }

    @Override
    public SimpleProperty get(String documentId) {
        return propertyRepository.findOne(documentId);
    }
}
