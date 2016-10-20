package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.repositories.MongoAnnotationRepository;

import java.net.URI;
import java.util.*;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Service
public class MongoAnnotationRepositoryService implements RepositoryService<MongoAnnotation> {

    @Autowired
    MongoAnnotationRepository mongoAnnotationRepository;

    /*
        For SimpleAnnotations
     */

    public List<MongoAnnotation> getBySemanticTags(Collection<URI> semanticTags) {
        return mongoAnnotationRepository.findBySemanticTagsIn(semanticTags);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesStudiesAccession(String accession){
        return mongoAnnotationRepository.findByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesName(String name){
        return mongoAnnotationRepository.findByAnnotatedBiologicalEntitiesName(name);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(String name, String accession){
        return mongoAnnotationRepository.findByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(name, accession);
    }

    public List<MongoAnnotation> getByAnnotatedProperty(Property property){
        return mongoAnnotationRepository.findByAnnotatedProperty(property);
    }

    public List<MongoAnnotation> getByAnnotatedPropertyValue(String propertyValue) {
        return mongoAnnotationRepository.findByAnnotatedPropertyPropertyValue(propertyValue);
    }

    public List<MongoAnnotation> getByProvenanceSource(AnnotationSource source, Pageable pageable) {
        return mongoAnnotationRepository.findByProvenanceSource(source, pageable);
    }

    public List<MongoAnnotation> getByProvenanceSourceName(String name, Pageable pageable){
        return mongoAnnotationRepository.findByProvenanceSourceName(name, pageable);
    }

    /*
        For BiologicalEntities
     */

    public List<BiologicalEntity> getAllBiologicalEntities(){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntities();
    }

    public List<BiologicalEntity> getAllBiologicalEntitiesByStudyAccession(String accession){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntitiesByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }

    /*
        For Studies
     */

    public List<Study> getAllStudies(){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntitiesStudies();
    }

    public List<Study> getStudiesBySemanticTags(URI... semanticTags){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesBySemanticTags(semanticTags);
    }

    public List<Study> getStudiesByAccession(String accession){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesByAccession(accession);
    }

    public List<Study> getStudiesByProperty(Property property){
        return mongoAnnotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesByAnnotatedProperty(property);
    }

    /*
        For Properties
     */

    public List<Property> getAllProperties(){
        return mongoAnnotationRepository.findDistinctAnnotatedProperties();
    }

    public List<String> getAllPropertyTypes(){
        return mongoAnnotationRepository.findAllPropertyTypes();
    }

    public List<Property> getPropertiesByPropertyType(String type){
        return mongoAnnotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyType(type);
    }

    public List<Property> getPropertiesByPropertyValue(String value){
        return mongoAnnotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyValue(value);
    }

    public List<Property> getPropertiesByPropertyTypeAndPropertyValue(String type, String value){
        return mongoAnnotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyTypeAndByAnnotatedPropertyPropertyValue(type, value);
    }

    /*
        General repository services
     */

    @Override
    public List<MongoAnnotation> getAllDocuments() {
        return mongoAnnotationRepository.findAll();
    }

    @Override
    public List<MongoAnnotation> getAllDocuments(Sort sort) {
        return mongoAnnotationRepository.findAll(sort);
    }

    @Override
    public Page<MongoAnnotation> getAllDocuments(Pageable pageable) {
        return mongoAnnotationRepository.findAll(pageable);
    }

    @Override
    public void delete(MongoAnnotation document) throws RuntimeException {
        mongoAnnotationRepository.delete(document);
    }

    @Override
    public MongoAnnotation create(MongoAnnotation document) throws RuntimeException {
        return mongoAnnotationRepository.insert(document);
    }

    @Override
    public MongoAnnotation save(MongoAnnotation document) throws RuntimeException {
        return mongoAnnotationRepository.save(document);
    }

    @Override
    public MongoAnnotation update(MongoAnnotation document) throws RuntimeException {
        return mongoAnnotationRepository.save(document);
    }

    @Override
    public MongoAnnotation get(String documentId) {
        return mongoAnnotationRepository.findOne(documentId);
    }


}
