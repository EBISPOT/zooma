package uk.ac.ebi.spot.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.datasource.AnnotationFactory;
import uk.ac.ebi.spot.datasource.AnnotationLoadingSession;
import uk.ac.ebi.spot.model.*;

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
                                       Collection<URI> semanticTags,
                                       Collection<URI> replaces) {

        for (BiologicalEntity be : annotatedBiologicalEntities) {
            SimpleBiologicalEntity sbe = (SimpleBiologicalEntity) be;
            if (sbe.getId() == null) {
                throw new IllegalArgumentException(
                        "A biological entity without a URI was submitted, can't create annotation");
                // todo handle this case better
            }
        }

        // create new property using regular URI minting strategy
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
    public Annotation createAnnotation(URI annotationURI,
                                       String annotationID,
                                       String studyAccession,
                                       URI studyURI,
                                       String studyID,
                                       String bioentityName,
                                       URI bioentityURI,
                                       String bioentityID,
                                       String propertyType,
                                       String propertyValue,
                                       URI propertyURI,
                                       String propertyID,
                                       URI semanticTag,
                                       String annotator,
                                       Date annotationDate) {

        Study s;
        if (studyURI != null) {
            s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession);
        }
        else {
            if (studyID != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyID);
            }
            else {
                if (studyAccession != null) {
                    s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession);
                }
                else {
                    s = null;
                }
            }
        }


        BiologicalEntity be;

        if (s != null) {
            if (bioentityURI != null) {
                ArrayList<Study> studies = new ArrayList<>();
                studies.add(s);
                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                               bioentityURI.toString(),
                                                                               studies);
            }
            else {
                if (bioentityID != null) {
                    ArrayList<Study> studies = new ArrayList<>();
                    studies.add(s);
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   studies);
                }
                else {
                    if (bioentityName != null) {
                        ArrayList<Study> studies = new ArrayList<>();
                        studies.add(s);
                        be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                       studies);
                    }
                    else {
                        be = null;
                    }
                }
            }
        }
        else {
            if (bioentityURI != null) {

                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                               bioentityURI.toString(),
                                                                               null);
            }
            else {
                if (bioentityID != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   null);
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
        }

        Property p;
        if (propertyURI != null) {
            p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue, propertyURI);
        }
        else {
            if (propertyID != null) {
                p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue, propertyID);
            }
            else {
                p = getAnnotationLoadingSession().getOrCreateProperty(propertyType, propertyValue);
            }
        }

        // get or create provenance
        AnnotationProvenance prov = getAnnotationLoadingSession().getOrCreateAnnotationProvenance(annotator,
                                                                                                  annotationDate);

        // and return the complete annotation
        Annotation a;
        if (annotationURI != null) {
            a = getAnnotationLoadingSession().getOrCreateAnnotation(
                    annotationURI,
                    be != null ? Collections.singleton(be) : Collections.<BiologicalEntity>emptySet(),
                    p,
                    prov,
                    semanticTag != null ? Collections.singleton(semanticTag) : Collections.<URI>emptySet());
        }
        else {
            if (annotationID != null) {
                a = getAnnotationLoadingSession().getOrCreateAnnotation(
                        annotationID,
                        be != null ? Collections.singleton(be) : Collections.<BiologicalEntity>emptySet(),
                        p,
                        prov,
                        semanticTag != null ? Collections.singleton(semanticTag) : Collections.<URI>emptySet());
            }
            else {
                a = getAnnotationLoadingSession().getOrCreateAnnotation(
                        be != null ? Collections.singleton(be) : Collections.<BiologicalEntity>emptySet(),
                        p,
                        prov,
                        semanticTag != null ? Collections.singleton(semanticTag) : Collections.<URI>emptySet());
            }
        }
        return a;
    }
}
