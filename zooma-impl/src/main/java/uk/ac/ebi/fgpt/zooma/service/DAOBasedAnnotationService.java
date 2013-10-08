package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

/**
 * An annotation service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO} to
 * retrieve annotation instances.
 *
 * @author Tony Burdett
 * @date 03/04/12
 */
public class DAOBasedAnnotationService extends AbstractShortnameResolver implements AnnotationService {
    private AnnotationDAO annotationDAO;

    public AnnotationDAO getAnnotationDAO() {
        return annotationDAO;
    }

    public void setAnnotationDAO(AnnotationDAO annotationDAO) {
        this.annotationDAO = annotationDAO;
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

    @Override public Annotation saveAnnotation(Annotation annotation) {
        if (annotation.getURI() != null && getAnnotationDAO().read(annotation.getURI()) != null) {
            getAnnotationDAO().update(annotation);
        }
        else {
            getAnnotationDAO().create(annotation);
        }
        return annotation;
    }

    public Annotation updateAnnotation(Annotation oldAnnotation, Annotation newAnnotation) {
        // save the newAnnotation (if it doesn't already exist)
        newAnnotation = saveAnnotation(newAnnotation);

        // and link the old one to the new
        oldAnnotation.setReplacedBy(newAnnotation.getURI());
        getAnnotationDAO().update(oldAnnotation);
        return newAnnotation;
    }

    @Override
    public void deleteAnnotation(Annotation annotation) throws ZoomaUpdateException {
        getAnnotationDAO().delete(annotation);
    }

    @Override public void replacePropertyForAnnotation(Annotation annotation, Property newProperty) {
        // get the list of old semantic tags
        Collection<URI> semanticTags = annotation.getSemanticTags();

        // create new annotation provenance
        AnnotationSource zoomaSource =
                new SimpleDatabaseAnnotationSource(URI.create("http://www.ebi.ac.uk/fgpt/zooma"));

        AnnotationProvenance provenance =
                new SimpleAnnotationProvenance(zoomaSource,
                                               AnnotationProvenance.Evidence.MANUAL_CURATED,
                                               "GENERATOR",
                                               new Date(),
                                               "ANNOTATOR",
                                               new Date());


        // create the new annotation
        URI newURI = URIUtils.incrementURI(getAnnotationDAO(), annotation.getURI());
        Annotation newAnnotation = new SimpleAnnotation(newURI,
                                                        annotation.getAnnotatedBiologicalEntities(),
                                                        newProperty,
                                                        semanticTags.toArray(new URI[semanticTags.size()]),
                                                        new URI[0],
                                                        new URI[]{annotation.getURI()},
                                                        provenance);

        // update annotations
        updateAnnotation(annotation, newAnnotation);
    }
}
