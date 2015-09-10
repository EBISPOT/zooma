package uk.ac.ebi.fgpt.zooma.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

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

    public AbstractAnnotationFactory(S annotationLoadingSession) {
        this.annotationLoadingSession = annotationLoadingSession;
    }

    public S getAnnotationLoadingSession() {
        return annotationLoadingSession;
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
            if (be.getURI() == null) {
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
                                       URI studyType,
                                       String bioentityName,
                                       URI bioentityURI,
                                       String bioentityID,
                                       String bioentityTypeName,
                                       URI bioentityTypeURI,
                                       String propertyType,
                                       String propertyValue,
                                       URI propertyURI,
                                       String propertyID,
                                       URI semanticTag,
                                       String annotator,
                                       Date annotationDate) {
        Collection<URI> studyTypes = new HashSet<>();
        if (studyType != null) {
            studyTypes.add(studyType);
        }

        Study s;
        if (studyURI != null) {
            if (studyType != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession,
                                                                   studyURI,
                                                                   studyTypes);
            }
            else {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyTypes);
            }
        }
        else {
            if (studyID != null) {
                s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyID, studyTypes);
            }
            else {
                if (studyAccession != null) {
                    s = getAnnotationLoadingSession().getOrCreateStudy(studyAccession, studyTypes);
                }
                else {
                    s = null;
                }
            }
        }


        BiologicalEntity be;
        Collection<String> bioEntityTypeNames = new HashSet<>();
        if (bioentityTypeName != null) {
            bioEntityTypeNames.add(bioentityTypeName);
        }
        Collection<URI> bioEntityTypeURIs = new HashSet<>();
        if (bioentityTypeURI != null) {
            bioEntityTypeURIs.add(bioentityTypeURI);
        }

        if (s != null) {
            if (bioentityURI != null) {
                be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                               bioentityURI,
                                                                               bioEntityTypeNames,
                                                                               bioEntityTypeURIs,
                                                                               s);
            }
            else {
                if (bioentityID != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   bioEntityTypeNames,
                                                                                   bioEntityTypeURIs,
                                                                                   s);
                }
                else {
                    if (bioentityName != null) {
                        be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                       bioEntityTypeNames,
                                                                                       bioEntityTypeURIs,
                                                                                       s);
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
                                                                               bioentityURI,
                                                                               bioEntityTypeNames,
                                                                               bioEntityTypeURIs);
            }
            else {
                if (bioentityID != null) {
                    be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                   bioentityID,
                                                                                   bioEntityTypeNames,
                                                                                   bioEntityTypeURIs);
                }
                else {
                    if (bioentityName != null) {
                        be = getAnnotationLoadingSession().getOrCreateBiologicalEntity(bioentityName,
                                                                                       bioEntityTypeNames,
                                                                                       bioEntityTypeURIs);
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
