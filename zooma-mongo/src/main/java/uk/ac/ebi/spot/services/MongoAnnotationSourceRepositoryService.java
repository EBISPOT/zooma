package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.MongoAnnotationSource;
import uk.ac.ebi.spot.repositories.MongoAnnotationSourceRepository;

import java.util.List;

/**
 * Created by olgavrou on 23/11/2016.
 */
@Service
public class MongoAnnotationSourceRepositoryService implements RepositoryService<MongoAnnotationSource> {

    @Autowired
    private MongoAnnotationSourceRepository mongoAnnotationSourceRepository;

    @Override
    public List<MongoAnnotationSource> getAllDocuments() {
        return mongoAnnotationSourceRepository.findAll();
    }

    @Override
    public List<MongoAnnotationSource> getAllDocuments(Sort sort) {
        return null;
    }

    @Override
    public Page<MongoAnnotationSource> getAllDocuments(Pageable pageable) {
        return null;
    }

    @Override
    public void delete(MongoAnnotationSource document) throws RuntimeException {

    }

    @Override
    public MongoAnnotationSource create(MongoAnnotationSource document) throws RuntimeException {
        return null;
    }

    @Override
    public MongoAnnotationSource save(MongoAnnotationSource document) throws RuntimeException {
        return null;
    }

    @Override
    public MongoAnnotationSource update(MongoAnnotationSource document) throws RuntimeException {
        MongoAnnotationSource source = mongoAnnotationSourceRepository.findByName(document.getName());
        if (source != null){
            document.setId(source.getId());
        }
        return mongoAnnotationSourceRepository.save(document);
    }

    @Override
    public MongoAnnotationSource get(String documentId) {
        return null;
    }
}
