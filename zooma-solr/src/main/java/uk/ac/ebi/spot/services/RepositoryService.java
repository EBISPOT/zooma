package uk.ac.ebi.spot.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by olgavrou on 03/08/2016.
 */
public interface RepositoryService<T> {

    List<T> getAllDocuments();

    List<T> getAllDocuments(Sort sort);

    Page<T> getAllDocuments(Pageable pageable);

    void delete(T document) throws RuntimeException;

    T create(T document) throws RuntimeException;

    T save(T document) throws RuntimeException;

    T update(T document) throws RuntimeException;

    T get(String documentId);

}
