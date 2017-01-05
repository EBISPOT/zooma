package uk.ac.ebi.spot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.datasource.AnnotationFactory;
import uk.ac.ebi.spot.datasource.AnnotationLoadingSession;

import java.net.URI;
import java.util.*;

/**
 * An all-purpose factory class that can generate fully formed annotation objects and their dependants from a series of
 * strings.  Each factory instance should be configured with an AnnotationLoadingSession
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 01/10/12
 */
public abstract class AbstractAnnotationFactory<S extends AnnotationLoadingSession> implements AnnotationFactory {

    private S annotationLoadingSession;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AnnotationLoadingSession getAnnotationLoadingSession() {
        return annotationLoadingSession;
    }

    public void setAnnotationLoadingSession(S annotationLoadingSession) {
        this.annotationLoadingSession = annotationLoadingSession;
    }

    protected Logger getLog() {
        return log;
    }


    @Override
    public String getDatasourceName() {
        return getAnnotationLoadingSession().getDatasourceName();
    }

    @Override
    public Annotation createAnnotation(Collection<BiologicalEntity> annotatedBiologicalEntities,
                                       Property annotatedProperty,
                                       AnnotationProvenance annotationProvenance,
                                       Collection<String> semanticTags,
                                       Collection<URI> replaces) {

        // create new property
        Property newProperty = annotatedProperty instanceof TypedProperty ?
                getAnnotationLoadingSession().getOrCreateProperty(((TypedProperty) annotatedProperty).getPropertyType(),
                                                                  annotatedProperty.getPropertyValue())
                : getAnnotationLoadingSession().getOrCreateProperty("", annotatedProperty.getPropertyValue());

        // and return the complete annotation
        return getAnnotationLoadingSession().getOrCreateAnnotation(
                annotatedBiologicalEntities,
                newProperty,
                annotationProvenance,
                semanticTags);
    }

    @Override
    public Annotation createAnnotation(String studyAccession,
                                       URI studyURI,
                                       String bioentityName,
                                       URI bioentityURI,
                                       String propertyType,
                                       String propertyValue,
                                       String semanticTag,
                                       String annotator,
                                       Date annotationDate) {

        Study s;
        if (studyURI != null) {
            s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession);
        }
        else {
            if (studyAccession != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession);
            }
            else {
                s = null;
            }
        }


        BiologicalEntity be;

        if (s != null) {
            if (bioentityURI != null) {
                ArrayList<Study> studies = new ArrayList<>();
                studies.add(s);
                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName, bioentityURI, studies);
            }
            else {
                if (bioentityName != null) {
                    ArrayList<Study> studies = new ArrayList<>();
                    studies.add(s);
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName, studies);
                }
                else {
                    be = null;
                }
            }
        }
        else {
            if (bioentityURI != null) {

                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName, bioentityURI, null);
            }
            else {
                if (bioentityName != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName, null);
                }
                else {
                    be = null;
                }
            }
        }

        Property p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue);

        // get or create provenance
        AnnotationProvenance prov = getAnnotationLoadingSession().getOrCreateAnnotationProvenance(annotator,
                                                                                                  annotationDate);

        // and return the complete annotation
        Annotation a = getAnnotationLoadingSession().getOrCreateAnnotation(
                        be != null ? Collections.singleton(be) : Collections.<BiologicalEntity>emptySet(),
                        p,
                        prov,
                        semanticTag != null ? Collections.singleton(semanticTag) : Collections.<String>emptySet());

        return a;
    }
}
