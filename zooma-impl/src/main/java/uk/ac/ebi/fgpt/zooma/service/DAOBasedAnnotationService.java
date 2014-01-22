package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.TransactionalAnnotationFactory;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

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
        try {
            getAnnotationFactory().acquire(annotation.getProvenance().getSource());
            Annotation newAnnotation = mintNewAnnotationFromRequest(annotation);
            Collection<Annotation> annotationsToUpdate = new HashSet<Annotation>();
            if (!annotation.getReplaces().isEmpty()) {
                for (URI replacedAnnotationURI : annotation.getReplaces()) {
                    Annotation replacedAnnotation = getAnnotationDAO().read(replacedAnnotationURI);
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
                    annotationsToUpdate.add(replacedAnnotation);
                }
            }
            try {
                getAnnotationDAO().create(newAnnotation);
            }
            catch (ResourceAlreadyExistsException e) {
                URI newURI = URIUtils.incrementURI(getAnnotationDAO(), annotation.getURI());
                newAnnotation = new SimpleAnnotation(newURI,
                        newAnnotation.getAnnotatedBiologicalEntities(),
                        newAnnotation.getAnnotatedProperty(),
                        newAnnotation.getProvenance(), newAnnotation.getSemanticTags().toArray(new URI[newAnnotation.getSemanticTags().size()]),
                        new URI[0],
                        new URI[]{annotation.getURI()});

            }
            // assuming the new annotation is created successfully, now update any of the previous annotations.
            for (Annotation replacedAnnotation : annotationsToUpdate) {
                getAnnotationDAO().update(replacedAnnotation);
            }
            return newAnnotation;
        }
        catch (InterruptedException e) {
            throw new ZoomaUpdateException("Save operation was interrupted", e);
        }
        finally {
            getAnnotationFactory().release();
        }
    }

    public Annotation updateAnnotation(Annotation oldAnnotation, Annotation newAnnotation) {
        throw new UnsupportedOperationException("This operation is not supported.");
        // todo - need to think about what is supported for this operation

//        Annotation clonedAnnotation = newAnnotation;
//        if (newAnnotation.getURI() == null) {
//            URI newUri = URIUtils.incrementURI(getAnnotationDAO(), oldAnnotation.getURI());
//            clonedAnnotation = new SimpleAnnotation(
//                    newUri,
//                    newAnnotation.getAnnotatedBiologicalEntities(),
//                    newAnnotation.getAnnotatedProperty(),
//                    newAnnotation.getProvenance(),
//                    newAnnotation.getSemanticTags().toArray(new URI[newAnnotation.getSemanticTags().size()]),
//                    new URI[0],
//                    new URI [] {oldAnnotation.getURI()});
//
//        }
//
//
//        // save the newAnnotation (if it doesn't already exist)
//        clonedAnnotation = saveAnnotation(clonedAnnotation);
//
//        // and link the old one to the new
//        oldAnnotation.setReplacedBy(clonedAnnotation.getURI());
//        getAnnotationDAO().update(oldAnnotation);
//        return newAnnotation;
    }

    @Override
    public void deleteAnnotation(Annotation annotation) throws ZoomaUpdateException {
        getAnnotationDAO().delete(annotation);
    }

    @Override public void replacePropertyForAnnotation(Annotation annotation, Property newProperty)
            throws ZoomaUpdateException {
        // get the list of old semantic tags
        Collection<URI> semanticTags = annotation.getSemanticTags();

        // create new annotation provenance
        AnnotationSource zoomaSource =
                new SimpleDatabaseAnnotationSource(URI.create("http://www.ebi.ac.uk/fgpt/zooma"), "zooma");

        // todo specify annotator based on user
        AnnotationProvenance provenance =
                new SimpleAnnotationProvenance(zoomaSource,
                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                        AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                        "ZOOMA",
                        new Date(),
                        "ANNOTATOR",
                        new Date());


        // create the new annotation
        URI newURI = URIUtils.incrementURI(getAnnotationDAO(), annotation.getURI());
        Annotation newAnnotation = new SimpleAnnotation(newURI,
                annotation.getAnnotatedBiologicalEntities(),
                newProperty,
                provenance, semanticTags.toArray(new URI[semanticTags.size()]),
                new URI[0],
                new URI[]{annotation.getURI()}
        );

        // update annotations
        updateAnnotation(annotation, newAnnotation);
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
                                    request.getProvenance().getAnnotator(),
                                    // todo - obtain annotator from security context?
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
