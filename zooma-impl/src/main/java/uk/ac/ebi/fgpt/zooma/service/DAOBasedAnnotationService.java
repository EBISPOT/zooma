package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.TransactionalAnnotationFactory;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUsers;

import java.net.URI;
import java.util.*;

/**
 * An annotation service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO} to
 * retrieve annotation instances.
 *
 * @author Tony Burdett
 * @date 03/04/12
 */
public class DAOBasedAnnotationService extends AbstractShortnameResolver implements AnnotationService {
    private AnnotationDAO annotationDAO;

    private TransactionalAnnotationFactory annotationFactory;

    public AnnotationDAO getAnnotationDAO() {
        return annotationDAO;
    }

    public void setAnnotationDAO(AnnotationDAO annotationDAO) {
        this.annotationDAO = annotationDAO;
    }

    public TransactionalAnnotationFactory getAnnotationFactory() {
        return annotationFactory;
    }

    public void setAnnotationFactory(TransactionalAnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    @Override public Collection<Annotation> getAnnotations() {
        return getAnnotationDAO().read();
    }

    @Override public Collection<Annotation> getAnnotations(int limit, int start) {
        return getAnnotationDAO().read(limit, start);
    }

    @Override public Collection<Annotation> getAnnotationsByStudy(Study study) {
        return getAnnotationDAO().readByStudy(study);
    }

    @Override public Collection<Annotation> getAnnotationsByBiologicalEntity(BiologicalEntity biologicalEntity) {
        return getAnnotationDAO().readByBiologicalEntity(biologicalEntity);
    }

    @Override public Collection<Annotation> getAnnotationsByProperty(Property property) {
        return getAnnotationDAO().readByProperty(property);
    }

    @Override public Collection<Annotation> getAnnotationsBySemanticTag(String shortname) {
        return getAnnotationsBySemanticTag(getURIFromShortname(shortname));
    }

    @Override public Collection<Annotation> getAnnotationsBySemanticTag(URI semanticTagURI) {
        return getAnnotationDAO().readBySemanticTag(semanticTagURI);
    }

    @Override public Annotation getAnnotation(String shortname) {
        return getAnnotation(getURIFromShortname(shortname));
    }

    @Override public Annotation getAnnotation(URI uri) {
        return getAnnotationDAO().read(uri);
    }

    @Override public Annotation saveAnnotation(Annotation annotation) throws ZoomaUpdateException {
        Collection<Annotation> newAnnotations = saveAnnotations(Collections.singleton(annotation));
        if (newAnnotations.size() == 1) {
            return newAnnotations.iterator().next();
        }
        throw new ZoomaUpdateException("Saving annotation " + annotation.getURI() + " returned " + newAnnotations.size() + " when only 1 was expected");
    }

    @Override
    public Collection<Annotation> saveAnnotations(Collection<Annotation> annotations) throws ZoomaUpdateException {

        List<Annotation> annotationsList = new ArrayList<>(annotations);
        Collections.sort(annotationsList, new Comparator<Annotation>() {
            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getProvenance().getSource().getURI().compareTo(o2.getProvenance().getSource().getURI());
            }
        });

        Collection<Annotation> newAnnotations = new HashSet<>();
        Collection<Annotation> previousAnnotations = new HashSet<>();

        try {

            AnnotationSource currentSource = annotationsList.get(0).getProvenance().getSource();
            getAnnotationFactory().acquire(currentSource);

            for (Annotation annotation : annotationsList) {

                if (!annotation.getProvenance().getSource().getURI().equals(currentSource.getURI())) {
                    getAnnotationFactory().release();
                    currentSource = annotation.getProvenance().getSource();
                    getAnnotationFactory().acquire(currentSource);
                }

                String username;
                ZoomaUser user = ZoomaUsers.getCurrentUser();
                if (annotation.getProvenance().getAnnotator() != null) {
                    username = annotation.getProvenance().getAnnotator();
                }
                else if (user != null) {
                    username = user.getFullName();
                }
                else {
                    username = "unknown";
                }

                Annotation newAnnotation = getAnnotationFactory().createAnnotation(
                        annotation.getAnnotatedBiologicalEntities(),
                        annotation.getAnnotatedProperty(),
                        annotation.getSemanticTags(),
                        annotation.getReplaces(),
                        username,
                        new Date());
                newAnnotations.add(newAnnotation);

                for (URI previousAnnotationUri : annotation.getReplaces()) {
                    Annotation previousAnnotation = getAnnotationDAO().read(previousAnnotationUri);
                    crossLinkAnnotations(previousAnnotation, newAnnotation);
                    previousAnnotations.add(previousAnnotation);
                }

            }

            // save new and update old
            try {
                getAnnotationDAO().create(newAnnotations);
                getAnnotationDAO().update(previousAnnotations);
            } catch (ResourceAlreadyExistsException e) {
                throw new ZoomaUpdateException("Couldn't create new annotation as the annotation URI already existed", e);
            }

        } catch (InterruptedException e) {
            throw new ZoomaUpdateException("Update previous annotation operation was interrupted", e);
        }
        finally {
            getAnnotationFactory().release();
        }
        return newAnnotations;
    }




    private Annotation cloneIfExists(Annotation annotation) {
        URI newURI = URIUtils.incrementURI(getAnnotationDAO(), annotation.getURI());
        if(!newURI.equals(annotation.getURI())) {
            return new SimpleAnnotation(newURI,
                    annotation.getAnnotatedBiologicalEntities(),
                    annotation.getAnnotatedProperty(),
                    annotation.getProvenance(), annotation.getSemanticTags().toArray(new URI[annotation.getSemanticTags().size()]),
                    new URI[0],
                    new URI[]{annotation.getURI()});
        }
        return annotation;
    }


    @Override
    public Collection<Annotation> updatePreviousAnnotations(Collection<Annotation> annotationsToUpdate, AnnotationUpdate update) throws ZoomaUpdateException {

        List<Annotation> annotationsList = new ArrayList<>(annotationsToUpdate);
        Collections.sort(annotationsList, new Comparator<Annotation>() {
            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getProvenance().getSource().getURI().compareTo(o2.getProvenance().getSource().getURI());
            }
        });

        Collection<Annotation> newAnnotations = new HashSet<>();

        try {

            AnnotationSource currentSource = annotationsList.get(0).getProvenance().getSource();
            getAnnotationFactory().acquire(currentSource);

            for (Annotation previousAnnotation : annotationsList) {

                if (!previousAnnotation.getProvenance().getSource().getURI().equals(currentSource.getURI())) {
                    getAnnotationFactory().release();
                    currentSource = previousAnnotation.getProvenance().getSource();
                    getAnnotationFactory().acquire(currentSource);
                }

                Property newProperty = null;
                if (update.getPropertyType() != null && update.getPropertyValue() != null) {
                    // both fields have changed
                    newProperty = new SimpleTypedProperty(update.getPropertyType(), update.getPropertyValue());
                }
                else if (update.getPropertyType() != null && update.getPropertyValue() == null) {
                    // if just the property type has changed
                    newProperty = new SimpleTypedProperty(update.getPropertyType(), previousAnnotation.getAnnotatedProperty().getPropertyValue());
                }
                else if (update.getPropertyValue() != null) {
                    // if just the property has changed, get the old property value
                    newProperty =
                            previousAnnotation.getAnnotatedProperty() instanceof TypedProperty ?
                                    new SimpleTypedProperty(((TypedProperty) previousAnnotation.getAnnotatedProperty()).getPropertyType(), update.getPropertyValue())
                            : new SimpleUntypedProperty(update.getPropertyValue());
                }
                else {
                    // if  both are null, use the old property as it is
                    newProperty = previousAnnotation.getAnnotatedProperty();
                }

                Collection<URI> semanticTags = new HashSet<>();
                if (update.isRetainSemanticTags() && previousAnnotation.getSemanticTags() != null) {
                    semanticTags.addAll(previousAnnotation.getSemanticTags());
                }

                if (update.getSemanticTags() != null) {
                    semanticTags.addAll(update.getSemanticTags());
                }

                Annotation newAnnotation = getAnnotationFactory().createAnnotation(
                        previousAnnotation.getAnnotatedBiologicalEntities(),
                        newProperty,
                        semanticTags,
                        Collections.singleton(previousAnnotation.getURI()),
                        ZoomaUsers.getCurrentUser().getFullName(),
                        new Date());
                newAnnotations.add(newAnnotation);

                crossLinkAnnotations(previousAnnotation, newAnnotation);


            }

            // save new and update old
            try {
                getAnnotationDAO().create(newAnnotations);
                getAnnotationDAO().update(annotationsToUpdate);
            } catch (ResourceAlreadyExistsException e) {
                throw new ZoomaUpdateException("Couldn't create new annotation as the annotation URI already existed", e);
            }


        } catch (InterruptedException e) {
            throw new ZoomaUpdateException("Update previous annotation operation was interrupted", e);
        } catch (NullPointerException e) {
            throw new ZoomaUpdateException("Update previous annotation operation failed for user " + ZoomaUsers.getCurrentUser(), e);
        }
        finally {
            getAnnotationFactory().release();
        }
        return newAnnotations;
    }


    private void crossLinkAnnotations (Annotation replacedAnnotation,Annotation newAnnotation) throws ZoomaUpdateException {
        if (replacedAnnotation == null) {
            throw new ZoomaUpdateException("New annotation replaces an annotation that does not exist");
        }
        else {
            if (replacedAnnotation.getReplacedBy().isEmpty()) {
                replacedAnnotation.setReplacedBy(newAnnotation.getURI());
            }
            else {
                replacedAnnotation.getReplacedBy().add(newAnnotation.getURI());
            }
            if (newAnnotation.getReplaces().isEmpty()) {
                newAnnotation.setReplaces(replacedAnnotation.getURI());
            }
            else {
                newAnnotation.getReplaces().add(replacedAnnotation.getURI());
            }
        }
    }

    /**
     * Create a new annotation, using this services annotation factory to handle URI minting and session optimizations.
     * The annotation supplied as a parameter is interpreted as a new annotation request; relevant fields are extracted
     * but not all fields supplied by the user will be preserved.  Essentially, this method contains the logic that
     * determines which fields it is save trust from newly supplied annotations.
     *
     * @param request an annotation request that has been supplied, some fields of which (for example, the URI) will be
     *                ignored
     * @return a newly minted annotation object
     */
    protected Annotation mintNewAnnotationFromRequest(Annotation request) {
        Annotation annotation = null;

        String propertyType = request.getAnnotatedProperty() instanceof TypedProperty
                ? ((TypedProperty) request.getAnnotatedProperty()).getPropertyType()
                : null;
        String propertyValue = request.getAnnotatedProperty().getPropertyValue();

        // flatten request into a series of single values - should always only be a single annotation
        for (URI semanticTag : flattenOnSemanticTags(request)) {
            for (BiologicalEntity biologicalEntity : flattenOnBioentities(request)) {
                for (URI bioentityTypeURI : flattenOnBioentityTypes(biologicalEntity)) {
                    for (Study study : flattenOnStudies(biologicalEntity)) {
                        for (URI studyTypeURI : flattenOnStudyTypes(study)) {
                            // now we've collected fields, generate annotation using annotation factory
                            annotation = getAnnotationFactory().createAnnotation(
                                    null,
                                    null,
                                    study != null ? study.getAccession() : null,
                                    study != null ? study.getURI(): null,
                                    null,
                                    studyTypeURI,
                                    biologicalEntity.getName(),
                                    biologicalEntity.getURI(),
                                    null,
                                    null,
                                    bioentityTypeURI,
                                    propertyType,
                                    propertyValue,
                                    null,
                                    null,
                                    semanticTag,
                                    ZoomaUsers.getCurrentUser().getFullName(),
                                    new Date());
                        }
                    }
                }
            }
        }

        return annotation;
    }

    private Collection<URI> flattenOnSemanticTags(Annotation request) {
        if (request.getSemanticTags().isEmpty()) {
            return Collections.singleton(null);
        }
        else {
            return request.getSemanticTags();
        }
    }

    private Collection<BiologicalEntity> flattenOnBioentities(Annotation request) {
        if (request.getAnnotatedBiologicalEntities().isEmpty()) {
            return Collections.singleton(null);
        }
        else {
            return request.getAnnotatedBiologicalEntities();
        }
    }

    private Collection<URI> flattenOnBioentityTypes(BiologicalEntity biologicalEntity) {
        if (biologicalEntity == null || biologicalEntity.getTypes().isEmpty()) {
            return Collections.singleton(null);
        }
        else {
            return biologicalEntity.getTypes();
        }
    }

    private Collection<Study> flattenOnStudies(BiologicalEntity biologicalEntity) {
        if (biologicalEntity == null || biologicalEntity.getStudies().isEmpty()) {
            return Collections.singleton(null);
        }
        else {
            return biologicalEntity.getStudies();
        }
    }

    private Collection<URI> flattenOnStudyTypes(Study study) {
        if (study == null || study.getTypes().isEmpty()) {
            return Collections.singleton(null);
        }
        else {
            return study.getTypes();
        }
    }
}
