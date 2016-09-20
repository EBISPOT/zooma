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
    MongoAnnotationRepository annotationRepository;

    /*
        For SimpleAnnotations
     */

    public List<MongoAnnotation> getBySemanticTags(Collection<URI> semanticTags) {
        return annotationRepository.findBySemanticTagsIn(semanticTags);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesStudiesAccession(String accession){
        return annotationRepository.findByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesName(String name){
        return annotationRepository.findByAnnotatedBiologicalEntitiesName(name);
    }

    public List<MongoAnnotation> getByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(String name, String accession){
        return annotationRepository.findByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(name, accession);
    }

    public List<MongoAnnotation> getByAnnotatedProperty(Property property){
        return annotationRepository.findByAnnotatedProperty(property);
    }

    public List<MongoAnnotation> getByAnnotatedPropertyValue(String propertyValue) {
        return annotationRepository.findByAnnotatedPropertyPropertyValue(propertyValue);
    }

    public List<MongoAnnotation> getByProvenanceSource(AnnotationSource source, Pageable pageable) {
        return annotationRepository.findByProvenanceSource(source, pageable);
    }

    public List<MongoAnnotation> getByProvenanceSourceName(String name, Pageable pageable){
        return annotationRepository.findByProvenanceSourceName(name, pageable);
    }

    /*
        For BiologicalEntities
     */

    public List<BiologicalEntity> getAllBiologicalEntities(){
        return annotationRepository.findDistinctAnnotatedBiologicalEntities();
    }

    public List<BiologicalEntity> getAllBiologicalEntitiesByStudyAccession(String accession){
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }

    /*
        For Studies
     */

    public List<Study> getAllStudies(){
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudies();
    }

    public List<Study> getStudiesBySemanticTags(URI... semanticTags){
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesBySemanticTags(semanticTags);
    }

    public List<Study> getStudiesByAccession(String accession){
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesByAccession(accession);
    }

    public List<Study> getStudiesByProperty(Property property){
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesByAnnotatedProperty(property);
    }

    /*
        For Properties
     */

    public List<Property> getAllProperties(){
        return annotationRepository.findDistinctAnnotatedProperties();
    }

    public List<String> getAllPropertyTypes(){
        return annotationRepository.findAllPropertyTypes();
    }

    public List<Property> getPropertiesByPropertyType(String type){
        return annotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyType(type);
    }

    public List<Property> getPropertiesByPropertyValue(String value){
        return annotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyValue(value);
    }

    public List<Property> getPropertiesByPropertyTypeAndPropertyValue(String type, String value){
        return annotationRepository.findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyTypeAndByAnnotatedPropertyPropertyValue(type, value);
    }

    /*
        General repository services
     */

    @Override
    public List<MongoAnnotation> getAllDocuments() {
        return annotationRepository.findAll();
    }

    @Override
    public List<MongoAnnotation> getAllDocuments(Sort sort) {
        return annotationRepository.findAll(sort);
    }

    @Override
    public Page<MongoAnnotation> getAllDocuments(Pageable pageable) {
        return annotationRepository.findAll(pageable);
    }

    @Override
    public void delete(MongoAnnotation document) throws RuntimeException {
        annotationRepository.delete(document);
    }

    @Override
    public MongoAnnotation create(MongoAnnotation document) throws RuntimeException {
        return annotationRepository.insert(document);
    }

    @Override
    public MongoAnnotation save(MongoAnnotation document) throws RuntimeException {
        return annotationRepository.save(document);
    }

    @Override
    public MongoAnnotation update(MongoAnnotation document) throws RuntimeException {
        return annotationRepository.save(document);
    }

    @Override
    public MongoAnnotation get(String documentId) {
        return annotationRepository.findOne(documentId);
    }


}
