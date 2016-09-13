package uk.ac.ebi.spot.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.Namespaces;
import uk.ac.ebi.spot.datasource.AnnotationLoadingSession;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.util.ZoomaUtils;
import uk.ac.ebi.spot.utils.TransientCacheable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
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
    private Map<URI, Study> studyCache;
    private Map<URI, BiologicalEntity> biologicalEntityCache;
    private Map<URI, Property> propertyCache;
    private Map<URI, SimpleAnnotation> annotationCache;
    private AnnotationProvenance annotationProvenanceCache;

    private MessageDigest messageDigest;

    private AnnotationProvenanceTemplate annotationProvenanceTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }


    public void init(){
        this.studyCache = Collections.synchronizedMap(new HashMap<URI, Study>());
        this.biologicalEntityCache = Collections.synchronizedMap(new HashMap<URI, BiologicalEntity>());
        this.propertyCache = Collections.synchronizedMap(new HashMap<URI, Property>());
        this.annotationCache = Collections.synchronizedMap(new HashMap<URI, SimpleAnnotation>());

        this.messageDigest = ZoomaUtils.generateMessageDigest();
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
        return getOrCreateStudy(studyAccession, generateIDFromContent(studyAccession));
    }

    @Override
    public synchronized Study getOrCreateStudy(String studyAccession, String studyID) {
        return getOrCreateStudy(studyAccession, mintStudyURI(studyID));
    }

    @Override
    public Study getOrCreateStudy(String studyAccession, URI studyURI) {
        // ping to keep caches alive
        ping();

        if (!studyCache.containsKey(studyURI)) {
                studyCache.put(studyURI, new SimpleStudy(studyURI.toString(), studyAccession));
        }
        return studyCache.get(studyURI);
    }

    @Override
    public synchronized BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                                     Collection<Study> studies) {
        List<String> ids = new ArrayList<>();
        if (studies != null) {
            for (Study s : studies) {
                if (s != null) {
                    ids.add(s.getAccession());
                }
            }
        } else {
            //no studies provided
            studies = new ArrayList<>();
        }
        ids.add(bioentityName);
        String hashID = generateIDFromContent(ids.toArray(new String[ids.size()]));
        return getOrCreateBiologicalEntity(bioentityName, hashID, studies);
    }

    @Override
    public synchronized BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                                     String bioentityID,
                                                                     Collection<Study> studies) {
        String[] studyAccs = new String[studies.size()];
        int i = 0;
        for (Study study : studies) {
            studyAccs[i] = study.getAccession();
            i++;
        }
        return getOrCreateBiologicalEntity(bioentityName,
                                           mintBioentityURI(bioentityID),
                                           studies);
    }

    @Override
    public BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                        URI bioentityURI,
                                                        Collection<Study> studies) {
        // ping to keep caches alive
        ping();

        if (!biologicalEntityCache.containsKey(bioentityURI)) {

            biologicalEntityCache.put(bioentityURI,
                                      new SimpleBiologicalEntity(bioentityURI.toString(),
                                                                 bioentityName,
                                                                 studies));
        }
        return biologicalEntityCache.get(bioentityURI);
    }

    @Override
    public synchronized Property getOrCreateProperty(String propertyType, String propertyValue) {
        if (propertyType != null && !propertyType.equals("")) {
            String normalizedType = ZoomaUtils.normalizePropertyTypeString(propertyType);
            return getOrCreateProperty(propertyType,
                                       propertyValue,
                                       generateIDFromContent(normalizedType, propertyValue));
        }
        else {
            return getOrCreateProperty(null,
                                       propertyValue,
                                       generateIDFromContent(propertyValue));
        }
    }

    @Override
    public synchronized Property getOrCreateProperty(String propertyType,
                                                     String propertyValue,
                                                     String propertyID) {
        if (propertyType != null && !propertyType.equals("")) {
            return getOrCreateProperty(propertyType,
                                       propertyValue,
                                       mintPropertyURI(propertyID));
        }
        else {
            return getOrCreateProperty(null,
                                       propertyValue,
                                       mintPropertyURI(propertyID));
        }
    }


    @Override
    public Property getOrCreateProperty(String propertyType,
                                        String propertyValue,
                                        URI propertyURI) {
        // ping to keep caches alive
        ping();

        Property property;
        if (propertyType != null && !propertyType.equals("")) {
            String normalizedType = ZoomaUtils.normalizePropertyTypeString(propertyType);
            property = new SimpleTypedProperty(propertyURI.toString(), normalizedType, propertyValue);
        }
        else {
            property = new SimpleUntypedProperty(propertyURI.toString(), propertyValue);
        }
        if (!propertyCache.containsKey(propertyURI)) {
            propertyCache.put(propertyURI, property);
        }
        return propertyCache.get(propertyURI);
    }

    @Override
    public synchronized Annotation getOrCreateAnnotation(Collection<BiologicalEntity> biologicalEntities,
                                                         Property property,
                                                         AnnotationProvenance annotationProvenance,
                                                         Collection<URI> semanticTags) {
        List<String> idContents = new ArrayList<>();
        for (BiologicalEntity biologicalEntity : biologicalEntities) {
            for (Study s : biologicalEntity.getStudies()) {
                idContents.add(s.getAccession());
            }
            if (biologicalEntity.getName() != null) {
                idContents.add(biologicalEntity.getName());
            }
        }
        if (property instanceof TypedProperty) {
            idContents.add(((TypedProperty) property).getPropertyType());
        }
        idContents.add(property.getPropertyValue());
        for (URI semanticTag : semanticTags) {
            idContents.add(semanticTag.toString());
        }
        idContents.add(annotationProvenance.getAnnotator() != null ? annotationProvenance.getAnnotator() : "");
//        idContents.add(
//                annotationProvenance.getGeneratedDate() != null ? annotationProvenance.getGeneratedDate().toString() :
//                        "");
        idContents.add(
                annotationProvenance.getAnnotationDate() != null ? annotationProvenance.getAnnotationDate().toString() :
                        "");
        idContents.add(annotationProvenance.getEvidence() != null ? annotationProvenance.getEvidence().toString() : "");
        idContents.add(
                annotationProvenance.getSource() != null ? annotationProvenance.getSource().toString() : "");

        String annotationID = generateIDFromContent(idContents.toArray(new String[idContents.size()]));

        return getOrCreateAnnotation(annotationID, biologicalEntities, property, annotationProvenance, semanticTags);
    }

    @Override
    public synchronized Annotation getOrCreateAnnotation(String annotationID,
                                                         Collection<BiologicalEntity> biologicalEntities,
                                                         Property property,
                                                         AnnotationProvenance annotationProvenance,
                                                         Collection<URI> semanticTags) {
        return getOrCreateAnnotation(mintAnnotationURI(annotationID),
                                     biologicalEntities,
                                     property,
                                     annotationProvenance,
                                     semanticTags);
    }

    @Override
    public Annotation getOrCreateAnnotation(URI annotationURI,
                                            Collection<BiologicalEntity> biologicalEntities,
                                            Property property,
                                            AnnotationProvenance annotationProvenance,
                                            Collection<URI> semanticTags) {
        // ping to keep caches alive
        ping();

        if (!annotationCache.containsKey(annotationURI)) {
            // create and cache a new annotation
            if (semanticTags.isEmpty()) {
                annotationCache.put(annotationURI, new SimpleAnnotation(annotationURI.toString(),
                                                                        biologicalEntities,
                                                                        property,
                                                                        new ArrayList<URI>(), annotationProvenance, null, null));
            }
            annotationCache.put(annotationURI, new SimpleAnnotation(annotationURI.toString(), biologicalEntities,
                                                                    property,
                                                                    semanticTags, annotationProvenance, null, null));
        }
        else {
            getLog().debug("Annotation <" + annotationURI + "> already exists; merging additional data");
            // retrieve previous annotation and merge fields
            SimpleAnnotation annotation = annotationCache.get(annotationURI);
            // merge bioentities
            Collection<BiologicalEntity> bes = annotation.getAnnotatedBiologicalEntities();
            bes.addAll(biologicalEntities);
            // merge semantic tags
            annotation.getSemanticTags().addAll(semanticTags);
            // update provenance
            if (annotationProvenance != null) {
                annotation.setProvenance(annotationProvenance);
            }
        }
        return annotationCache.get(annotationURI);
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
        studyCache.clear();
        biologicalEntityCache.clear();
        propertyCache.clear();
        annotationCache.clear();
        annotationProvenanceCache = null;
        return true;
    }

    protected String encode(String s) {
        try {
            return URLEncoder.encode(s.trim(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // this is extremely unlikely - UTF-8 not supported? If somehow this does happen, throw a runtime exception
            throw new RuntimeException(
                    "Bad ZOOMA environment - you should ensure UTF-8 encoding is supported on this platform", e);
        }
    }

    protected URI mintStudyURI(String studyID) {
        return URI.create(
                Namespaces.ZOOMA_RESOURCE.getURI().toString() + getDatasourceName() + "/" + studyID);
    }

    protected URI mintBioentityURI(String bioentityID) {
        return URI.create(
                Namespaces.ZOOMA_RESOURCE.getURI().toString() + getDatasourceName() + "/" + encode(bioentityID));
    }

    protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + getDatasourceName() + "/" + annotationID);
    }

    protected URI mintPropertyURI(String propertyID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + propertyID);
    }

    private String generateIDFromContent(String... contents) {
        boolean hasNulls = false;
        for (String s : contents) {
            if (s == null) {
                hasNulls = true;
                break;
            }
        }
        if (hasNulls) {
            StringBuilder sb = new StringBuilder();
            for (String s : contents) {
                sb.append(s).append(";");
            }
            getLog().error("Attempting to generate new ID from content containing nulls: " + sb.toString());
        }
        synchronized (messageDigest) {
            return ZoomaUtils.generateHashEncodedID(messageDigest, contents);
        }
    }
}
