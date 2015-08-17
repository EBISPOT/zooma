package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.TransactionalAnnotationFactory;
import uk.ac.ebi.fgpt.zooma.exception.AnonymousUserNotAllowedException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.AnnotationUpdate;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUsers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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

    @Override
    public Collection<Annotation> getAnnotations() {
        return getAnnotationDAO().read();
    }

    @Override
    public Collection<Annotation> getAnnotations(int limit, int start) {
        return getAnnotationDAO().read(limit, start);
    }

    @Override
    public Collection<Annotation> getAnnotationsByStudy(Study study) {
        return getAnnotationDAO().readByStudy(study);
    }

    @Override
    public Collection<Annotation> getAnnotationsByBiologicalEntity(BiologicalEntity biologicalEntity) {
        return getAnnotationDAO().readByBiologicalEntity(biologicalEntity);
    }

    @Override
    public Collection<Annotation> getAnnotationsByProperty(Property property) {
        return getAnnotationDAO().readByProperty(property);
    }

    @Override
    public Collection<Annotation> getAnnotationsBySemanticTag(String shortname) {
        return getAnnotationsBySemanticTag(getURIFromShortname(shortname));
    }

    @Override
    public Collection<Annotation> getAnnotationsBySemanticTag(URI semanticTagURI) {
        return getAnnotationDAO().readBySemanticTag(semanticTagURI);
    }

    @Override
    public Annotation getAnnotation(String shortname) {
        return getAnnotation(getURIFromShortname(shortname));
    }

    @Override
    public Annotation getAnnotation(URI uri) {
        return getAnnotationDAO().read(uri);
    }

    @Override
    public Annotation saveAnnotation(Annotation annotation) throws ZoomaUpdateException {
        Collection<Annotation> newAnnotations = saveAnnotations(Collections.singleton(annotation));
        if (newAnnotations.size() == 1) {
            return newAnnotations.iterator().next();
        }
        throw new ZoomaUpdateException(
                "Saving annotation " + annotation.getURI() + " returned " + newAnnotations.size() +
                        " when only 1 was expected");
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
                ZoomaUser user = ZoomaUsers.getAuthenticatedUser();
                if (annotation.getProvenance().getAnnotator() != null) {
                    username = annotation.getProvenance().getAnnotator();
                }
                else {
                    username = user.getFullName();
                }

                AnnotationProvenanceTemplate template = getAnnotationFactory().getAnnotationLoadingSession()
                        .getAnnotationProvenanceTemplate()
                        .annotatorIs(username)
                        .annotationDateIs(new Date());

                Annotation newAnnotation = getAnnotationFactory().createAnnotation(
                        annotation.getAnnotatedBiologicalEntities(),
                        annotation.getAnnotatedProperty(),
                        template.build(),
                        annotation.getSemanticTags(),
                        annotation.getReplaces());
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
            }
            catch (ResourceAlreadyExistsException e) {
                throw new ZoomaUpdateException("Couldn't create new annotation as the annotation URI already existed",
                                               e);
            }

        }
        catch (InterruptedException e) {
            throw new ZoomaUpdateException("Update previous annotation operation was interrupted", e);
        }
        finally {
            getAnnotationFactory().release();
        }
        return newAnnotations;
    }

    @Override
    public Collection<Annotation> updatePreviousAnnotations(Collection<Annotation> annotationsToUpdate,
                                                            AnnotationUpdate update) throws ZoomaUpdateException {

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

                Property newProperty;
                if (update.getPropertyType() != null && update.getPropertyValue() != null) {
                    // both fields have changed
                    newProperty = new SimpleTypedProperty(update.getPropertyType(), update.getPropertyValue());
                }
                else if (update.getPropertyType() != null && update.getPropertyValue() == null) {
                    // if just the property type has changed
                    newProperty = new SimpleTypedProperty(update.getPropertyType(),
                                                          previousAnnotation.getAnnotatedProperty().getPropertyValue());
                }
                else if (update.getPropertyValue() != null) {
                    // if just the property has changed, get the old property value
                    newProperty =
                            previousAnnotation.getAnnotatedProperty() instanceof TypedProperty ?
                                    new SimpleTypedProperty(((TypedProperty) previousAnnotation.getAnnotatedProperty()).getPropertyType(),
                                                            update.getPropertyValue())
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

                AnnotationProvenanceTemplate template = getAnnotationFactory().getAnnotationLoadingSession()
                        .getAnnotationProvenanceTemplate()
                        .annotatorIs(ZoomaUsers.getAuthenticatedUser().getFullName())
                        .annotationDateIs(new Date());

                Annotation newAnnotation = getAnnotationFactory().createAnnotation(
                        previousAnnotation.getAnnotatedBiologicalEntities(),
                        newProperty,
                        template.build(),
                        semanticTags,
                        Collections.singleton(previousAnnotation.getURI()));
                newAnnotations.add(newAnnotation);

                crossLinkAnnotations(previousAnnotation, newAnnotation);


            }

            // save new and update old
            try {
                getAnnotationDAO().create(newAnnotations);
                getAnnotationDAO().update(annotationsToUpdate);
            }
            catch (ResourceAlreadyExistsException e) {
                throw new ZoomaUpdateException("Couldn't create new annotation as the annotation URI already existed",
                                               e);
            }


        }
        catch (InterruptedException e) {
            throw new ZoomaUpdateException("Update previous annotation operation was interrupted", e);
        }
        catch (AnonymousUserNotAllowedException e) {
            throw new ZoomaUpdateException("You must be authenticated to perform update operations", e);
        }
        finally {
            getAnnotationFactory().release();
        }
        return newAnnotations;
    }


    private void crossLinkAnnotations(Annotation replacedAnnotation, Annotation newAnnotation)
            throws ZoomaUpdateException {
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
}
