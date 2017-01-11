package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.zooma.datasource.AnnotationLoadingSession;
import uk.ac.ebi.spot.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;
import uk.ac.ebi.spot.zooma.model.api.Property;
import uk.ac.ebi.spot.zooma.model.mongo.*;
import uk.ac.ebi.spot.zooma.utils.TransientCacheable;
import uk.ac.ebi.spot.zooma.utils.ZoomaUtils;

import java.util.*;

/**
 * An annotation loading session that caches objects that have been previously seen so as to avoid creating duplicates
 * of objects that should be reused.
 * <p/>
 * It is assumed that within a single session, objects with the same sets of parameters used in their creation are
 * identical.  Care should therefore be taken to reuse the same method of construction for each object, and to ensure
 * that enough information is supplied to prevent duplicates being inadvertently created.
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 28/09/12
 */
public abstract class AbstractAnnotationLoadingSession extends TransientCacheable implements AnnotationLoadingSession {
//    private Map<URI, Study> studyCache;
//    private Map<URI, BiologicalEntity> biologicalEntityCache;
//    private Map<URI, Property> propertyCache;
//    private Map<URI, Annotation> annotationCache;
    private AnnotationProvenance annotationProvenanceCache;

    private AnnotationProvenanceTemplate annotationProvenanceTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }


    public void init(){
//        this.studyCache = Collections.synchronizedMap(new HashMap<String, Study>());
//        this.biologicalEntityCache = Collections.synchronizedMap(new HashMap<String, BiologicalEntity>());
//        this.propertyCache = Collections.synchronizedMap(new HashMap<String, Property>());
//        this.annotationCache = Collections.synchronizedMap(new HashMap<String, Annotation>());

    }

    public void setAnnotationProvenanceTemplate(AnnotationProvenanceTemplate annotationProvenanceTemplate) {
        this.annotationProvenanceTemplate = annotationProvenanceTemplate;
    }

    public String getDatasourceName() {
        if (annotationProvenanceTemplate != null) {
            return annotationProvenanceTemplate.getSource().getName();
        }
        else {
            return null;
        }
    }

    @Override
    public synchronized Study getOrCreateStudy(String studyAccession) {
        return new Study(studyAccession);
    }


    @Override
    public Study getOrCreateStudy(String studyAccession, String studyURI) {
//        // ping to keep caches alive
//        ping();
//
//        if (!studyCache.containsKey(studyURI)) {
//                studyCache.put(studyURI, new Study(studyAccession, studyURI));
//        }
//        return studyCache.get(studyURI);
        Study study = new Study(studyAccession);
        study.setStudyUri(studyURI);
        return study;
    }

    @Override
    public synchronized BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                                     Collection<Study> studies) {
        return new BiologicalEntity(bioentityName, studies);
    }


    @Override
    public BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                                                   String bioentityURI,
                                                                                   Collection<Study> studies) {
//        // ping to keep caches alive
//        ping();
//
//        if (!biologicalEntityCache.containsKey(bioentityURI)) {
//
//            biologicalEntityCache.put(bioentityURI,
//                                      new BiologicalEntity(bioentityName, studies, bioentityURI));
//        }
//        return biologicalEntityCache.get(bioentityURI);
        BiologicalEntity biologicalEntity = new BiologicalEntity(bioentityName, studies);
        biologicalEntity.setBioEntityUri(bioentityURI);
        return biologicalEntity;
    }

    @Override
    public synchronized Property getOrCreateProperty(String propertyType, String propertyValue) {
        if (propertyType != null && !propertyType.equals("")) {
            String normalizedType = ZoomaUtils.normalizePropertyTypeString(propertyType);
            return new TypedProperty(normalizedType, propertyValue);
        }
        else {
            return new UntypedProperty(propertyValue);
        }
    }


    @Override
    public synchronized Annotation getOrCreateAnnotation(Collection<BiologicalEntity> biologicalEntities,
                                                                                    Property property,
                                                                                    AnnotationProvenance annotationProvenance,
                                                                                    Collection<String> semanticTags) {

        //TODO: this is where we will calculate replacedBy etc?
        return new Annotation(biologicalEntities, (TypedProperty) property, semanticTags, (MongoAnnotationProvenance) annotationProvenance, true);
    }


    @Override public AnnotationProvenance getOrCreateAnnotationProvenance(String annotator, Date annotationDate) {
        if (annotationProvenanceCache != null) {
            if (annotationProvenanceCache.getAnnotator() != null && annotationProvenanceCache.getAnnotator().equals(
                    annotator)) {
                if (annotationProvenanceCache.getAnnotationDate() != null &&
                        annotationProvenanceCache.getAnnotationDate().equals(annotationDate)) {
                    return annotationProvenanceCache;
                }
            }
        }
        annotationProvenanceCache =
                annotationProvenanceTemplate.annotatorIs(annotator).annotationDateIs(annotationDate).build();
        return annotationProvenanceCache;
    }

    @Override
    protected boolean createCaches() {
        // caches are final, created in constructor, so nothing to do here
        return true;
    }

    @Override
    public synchronized boolean clearCaches() {
        getLog().debug("Clearing caches for " + getClass().getSimpleName());
//        studyCache.clear();
//        biologicalEntityCache.clear();
//        propertyCache.clear();
//        annotationCache.clear();
        annotationProvenanceCache = null;
        return true;
    }


}
