package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.repositories.AnnotationRepository;

import java.net.URI;
import java.util.*;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Service
public class AnnotationRepositoryService implements RepositoryService<SimpleAnnotation> {

    @Autowired
    AnnotationRepository annotationRepository;

    public List<SimpleAnnotation> getByAnnotatedPropertyValue(String propertyValue) {
        return annotationRepository.findByAnnotatedPropertyPropertyValue(propertyValue);
    }

    public List<SimpleAnnotation> getBySemanticTags(Collection<URI> semanticTags) {
        return annotationRepository.findBySemanticTagsIn(semanticTags);
    }

    public List<SimpleAnnotation> getByAnnotatedBiologicalEntitiesStudiesAccession(String accession){
        return annotationRepository.findByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }

    public List<SimpleAnnotation> getByAnnotatedBiologicalEntitiesName(String name){
        return annotationRepository.findByAnnotatedBiologicalEntitiesName(name);
    }

    public List<SimpleAnnotation> getByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(String name, String accession){
        return annotationRepository.findByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(name, accession);
    }

    public List<SimpleAnnotation> getByAnnotatedProperty(Property property){
        return annotationRepository.findByAnnotatedProperty(property);
    }

    public Collection<BiologicalEntity> getAllBiologicalEntities(){

        HashMap<String, BiologicalEntity> biologicalEntities = new HashMap<>();

        List<SimpleAnnotation> annotations = this.getAllDocuments();

        if (annotations != null && !annotations.isEmpty()){
            for (SimpleAnnotation annotation : annotations){
                Collection<BiologicalEntity> retrievedEntities = annotation.getAnnotatedBiologicalEntities();
                for (BiologicalEntity entity : retrievedEntities){
                    SimpleBiologicalEntity simpleBiologicalEntity = (SimpleBiologicalEntity) entity;
                    if(biologicalEntities.get(simpleBiologicalEntity.getId()) == null){
                        //if it hasn't already been added
                        biologicalEntities.put(simpleBiologicalEntity.getId(), simpleBiologicalEntity);
                    }
                }
            }
        }

        return biologicalEntities.values();
    }

    public Collection<Study> getAllStudies(){
        HashMap<String, Study> studies = new HashMap<>();
        Collection<BiologicalEntity> biologicalEntities = this.getAllBiologicalEntities();

        for (BiologicalEntity biologicalEntity : biologicalEntities){
            Collection<Study> retrievedStudies = biologicalEntity.getStudies();
            for (Study retrievedStudy : retrievedStudies){
                SimpleStudy simpleStudy = (SimpleStudy) retrievedStudy;
                if (studies.get(simpleStudy.getId()) == null){
                    //if it hasn't already been added
                    studies.put(simpleStudy.getId(), simpleStudy);
                }
            }
        }

        return studies.values();
    }


    public List<SimpleAnnotation> getByProvenanceSource(AnnotationSource source, Pageable pageable) {
        return annotationRepository.findByProvenanceSource(source, pageable);
    }

    public List<AnnotationProvenance> getProvenanceByProvenanceSource(AnnotationSource source, Pageable pageable){

        List<SimpleAnnotation> simpleAnnotations = annotationRepository.findByProvenanceSource(source, pageable);
        List<AnnotationProvenance> annotationProvenances = new ArrayList<>();

        if (simpleAnnotations != null && !simpleAnnotations.isEmpty()){
            for(SimpleAnnotation simpleAnnotation : simpleAnnotations){
                AnnotationProvenance provenance = simpleAnnotation.getProvenance();

                annotationProvenances.add(provenance);


            }
        }

        return annotationProvenances;
    }

    public List<SimpleAnnotation> getByProvenanceSourceName(String name, Pageable pageable){
        return annotationRepository.findByProvenanceSourceName(name, pageable);
    }

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


}
