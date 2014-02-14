package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationUpdate;
import uk.ac.ebi.fgpt.zooma.model.Update;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;

import java.util.Collection;

/**
 * A {@link uk.ac.ebi.fgpt.zooma.io.ZoomaLoader} for {@link Annotation}s that utilizes a standard {@link
 * uk.ac.ebi.fgpt.zooma.service.AnnotationService} to load annotations via the {@link
 * uk.ac.ebi.fgpt.zooma.service.AnnotationService#saveAnnotation(uk.ac.ebi.fgpt.zooma.model.Annotation)} method.
 *
 * @author Tony Burdett
 * @date 09/01/14
 */
@Deprecated
public class ServiceBasedAnnotationLoader implements ZoomaLoader<Annotation> {
    private AnnotationService annotationService;

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override public void load(String datasourceName, Collection<Annotation> annotations)
            throws ZoomaLoadingException {
        try {
            getAnnotationService().saveAnnotations(annotations);
        }
        catch (ZoomaUpdateException e) {
            throw new ZoomaLoadingException("Failed to load new annotation", e);
        }
    }

    @Override public void load(Annotation annotation) throws ZoomaLoadingException {
        try {
            getAnnotationService().saveAnnotation(annotation);
        }
        catch (ZoomaUpdateException e) {
            throw new ZoomaLoadingException("Failed to load new annotation", e);
        }
    }

    @Override
    public void update(Collection<Annotation> zoomaObjects, Update<Annotation> update) throws ZoomaLoadingException {
        try {
            update.apply(zoomaObjects, getAnnotationService());
        } catch (ZoomaUpdateException e) {
            throw new ZoomaLoadingException(e);
        }
    }
}
