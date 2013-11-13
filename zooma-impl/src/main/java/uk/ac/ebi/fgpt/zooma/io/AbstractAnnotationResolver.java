package uk.ac.ebi.fgpt.zooma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract implementation of a resolver service that provides all common, generic annotation resolving methods.
 * Implementing classes should determine how annotation resolving is invoked and implement the comparison methods {@link
 * #wasModified(Object)} and {@link #findModified(Object)}.
 *
 * @author Tony Burdett
 * @date 05/06/13
 */
public abstract class AbstractAnnotationResolver implements ZoomaResolver<Annotation> {
    private AnnotationDAO zoomaAnnotationDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AnnotationDAO getZoomaAnnotationDAO() {
        return zoomaAnnotationDAO;
    }

    public void setZoomaAnnotationDAO(AnnotationDAO zoomaAnnotationDAO) {
        this.zoomaAnnotationDAO = zoomaAnnotationDAO;
    }

    @Override
    public Annotation resolve(Annotation annotationToResolve) {
        // does this annotation already exist?
        if (exists(annotationToResolve)) {
            // if so, check for modifications
            getLog().trace("Annotation " + annotationToResolve.getURI() + " already exists in ZOOMA");
            Annotation zoomaAnnotation = getZoomaAnnotationDAO().read(annotationToResolve.getURI());
            if (zoomaAnnotation != null) {
                if (isUpdated(annotationToResolve, zoomaAnnotation)) {
                    // there are updates, so we need to assign a new URI and link
                    getLog().trace("Annotation " + annotationToResolve.getURI() + " is updated");
                    URI newURI = incrementAnnotationURI(annotationToResolve.getURI());
                    return createReplacementAnnotation(annotationToResolve, newURI);
                }
                else {
                    // there are no modifications, so return null
                    getLog().trace("Annotation " + annotationToResolve.getURI() + " exists, but is unchanged");
                    return null;
                }
            }
        }
        else {
            // there was no existing annotation, so we DO need to retain annotationToResolve
            // but is this a modified version?
            getLog().trace("Annotation " + annotationToResolve.getURI() + " " +
                                   "does not exist in ZOOMA, attempting to resolve");
            Annotation modifiedAnnotation = findModified(annotationToResolve);
            if (modifiedAnnotation != null) {
                // this biological entity contains a version of the "same" annotation, so link the two
                linkAnnotations(annotationToResolve, modifiedAnnotation);
                getLog().debug("Annotation " + annotationToResolve.getURI() + " is a modified version of " +
                                       modifiedAnnotation.getURI());
            }
        }

        // this annotation is novel (either completely new or is linked to an old one)
        getLog().trace("Next resolved annotation: " + annotationToResolve.getURI());
        return annotationToResolve;
    }

    @Override
    public Collection<Annotation> filter(Collection<Annotation> annotations) {
        getLog().trace("Filtering untagged annotations");
        Collection<Annotation> filteredAnnotations = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (annotation.getSemanticTags() != null && !annotation.getSemanticTags().isEmpty()) {
                filteredAnnotations.add(annotation);
            }
        }
        return filteredAnnotations;
    }

    @Override
    public boolean exists(Annotation annotation) {
        getLog().trace("Querying for annotation " + annotation.getURI());
        return getZoomaAnnotationDAO().read(annotation.getURI()) != null;
    }

    @Override
    public boolean isUpdated(Annotation annotation, Annotation referenceAnnotation) {
        // consider properties, biological entities, semantic tags, provenance
        getLog().trace("Doing update check on " + annotation.getURI());

        // compare properties
        boolean propertiesSame =
                compareProperties(annotation.getAnnotatedProperty(),
                                  referenceAnnotation.getAnnotatedProperty());

        // compare bioentities
        boolean biologicalEntitiesSame =
                compareBiologicalEntities(annotation.getAnnotatedBiologicalEntities(),
                                          referenceAnnotation.getAnnotatedBiologicalEntities());

        // compare semantic tags
        boolean semanticTagsSame =
                compareSemanticTags(annotation.getSemanticTags(),
                                    referenceAnnotation.getSemanticTags());

        boolean provenanceSame = annotation.getProvenance().equals(referenceAnnotation.getProvenance());
        getLog().trace("Update check report: " +
                               "properties " + (propertiesSame ? "unchanged" : "updated") + "; " +
                               "biologicalEntities: " + (biologicalEntitiesSame ? "unchanged" : "updated") + "; " +
                               "semanticTags " + (semanticTagsSame ? "unchanged" : "updated") + "; " +
                               "provenance " + (provenanceSame ? "unchanged" : "updated"));
        return !(propertiesSame && biologicalEntitiesSame && semanticTagsSame && provenanceSame);
    }

    @Override
    public Modification getModification(Annotation annotation,
                                        Annotation referenceAnnotation) {
        // compare properties
        boolean propertiesSame =
                compareProperties(annotation.getAnnotatedProperty(),
                                  referenceAnnotation.getAnnotatedProperty());

        if (!propertiesSame) {
            // compare types if possible
            Property p1 = annotation.getAnnotatedProperty();
            Property p2 = referenceAnnotation.getAnnotatedProperty();
            if (p1 instanceof TypedProperty && p2 instanceof TypedProperty) {
                TypedProperty tp1 = (TypedProperty) p1;
                TypedProperty tp2 = (TypedProperty) p2;
                if (!tp1.getPropertyType().equalsIgnoreCase(tp2.getPropertyType())) {
                    return Modification.PROPERTY_TYPE_MODIFICATION;
                }
            }

            return Modification.PROPERTY_VALUE_MODIFICATION;
        }

        // compare bioentities
        boolean biologicalEntitiesSame =
                compareBiologicalEntities(annotation.getAnnotatedBiologicalEntities(),
                                          referenceAnnotation.getAnnotatedBiologicalEntities());
        if (!biologicalEntitiesSame) {
            return Modification.BIOLOGICAL_ENTITY_MODIFICATION;
        }


        // compare semantic tags
        boolean semanticTagsSame =
                compareSemanticTags(annotation.getSemanticTags(),
                                    referenceAnnotation.getSemanticTags());
        if (!semanticTagsSame) {
            return Modification.SEMANTIC_TAG_MODIFICATION;
        }

        // compare annotation provenance
        boolean provenanceSame = annotation.getProvenance().equals(referenceAnnotation.getProvenance());
        if (!provenanceSame) {
            return Modification.PROVENANCE_MODIFICATION;
        }

        // if we get to here there was no modification
        return Modification.NO_MODIFICATION;
    }

    protected void linkAnnotations(Annotation newAnnotation, Annotation oldAnnotation) {
        newAnnotation.setReplaces(oldAnnotation.getURI());
        oldAnnotation.setReplacedBy(newAnnotation.getURI());
    }

    protected URI incrementAnnotationURI(URI originalURI) {
        return URIUtils.incrementURI(getZoomaAnnotationDAO(), originalURI);
    }

    protected Annotation createReplacementAnnotation(Annotation annotationToReplace, URI newURI) {
        Collection<URI> oldAnnotationSemanticTags = annotationToReplace.getSemanticTags();
        URI[] semanticTags = oldAnnotationSemanticTags.toArray(new URI[annotationToReplace.getSemanticTags().size()]);
        return new SimpleAnnotation(newURI,
                                    annotationToReplace.getAnnotatedBiologicalEntities(),
                                    annotationToReplace.getAnnotatedProperty(),
                                    semanticTags,
                                    new URI[0],
                                    new URI[]{annotationToReplace.getURI()},
                                    annotationToReplace.getProvenance());
    }

    protected boolean compareProperties(Property property, Property referenceProperty) {
        return property.matches(referenceProperty) && referenceProperty.matches(property);
    }

    protected boolean compareBiologicalEntities(Collection<BiologicalEntity> biologicalEntities,
                                                Collection<BiologicalEntity> referenceBiologicalEntities) {
        Set<String> studyBEPairs = new HashSet<>();
        Set<String> rStudyBEPairs = new HashSet<>();

        for (BiologicalEntity be : biologicalEntities) {
            for (Study s : be.getStudies()) {
                String studyBEPair = s.getAccession() + "|" + be.getName();
                studyBEPairs.add(studyBEPair);
            }
        }
        for (BiologicalEntity be : referenceBiologicalEntities) {
            for (Study s : be.getStudies()) {
                String studyBEPair = s.getAccession() + "|" + be.getName();
                rStudyBEPairs.add(studyBEPair);
            }
        }
        boolean biologicalEntitiesSame = true;
        if (studyBEPairs.size() != 0 && rStudyBEPairs.size() != 0) {
            if (studyBEPairs.size() == rStudyBEPairs.size()) {
                for (String studyBEPair : studyBEPairs) {
                    if (!rStudyBEPairs.contains(studyBEPair)) {
                        biologicalEntitiesSame = false;
                        break;
                    }
                }
            }
            else {
                biologicalEntitiesSame = false;
            }
        }
        return biologicalEntitiesSame;
    }

    protected boolean compareSemanticTags(Collection<URI> semanticTags, Collection<URI> referenceSemanticTags) {
        boolean semanticTagsSame = true;
        if (semanticTags.size() != 0 && referenceSemanticTags.size() != 0) {
            if (semanticTags.size() == referenceSemanticTags.size()) {
                for (URI semanticTag : semanticTags) {
                    if (!referenceSemanticTags.contains(semanticTag)) {
                        semanticTagsSame = false;
                        break;
                    }
                }
            }
        }
        return semanticTagsSame;
    }
}
