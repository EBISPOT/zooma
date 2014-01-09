package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;

/**
 * A {@link uk.ac.ebi.fgpt.zooma.io.ZoomaLoader} for {@link Annotation}s that utilizes a standard {@link
 * uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO} to load annotations via {@link uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO#create(uk.ac.ebi.fgpt.zooma.model.Identifiable)}.
 *
 * @author Tony Burdett
 * @date 09/01/14
 */
public class DAOBasedAnnotationLoader implements ZoomaLoader<Annotation> {
    private AnnotationDAO annotationDAO;

    public AnnotationDAO getAnnotationDAO() {
        return annotationDAO;
    }

    public void setAnnotationDAO(AnnotationDAO annotationDAO) {
        this.annotationDAO = annotationDAO;
    }

    @Override public void load(String datasourceName, Collection<Annotation> annotations)
            throws ZoomaLoadingException {
        for (Annotation annotation : annotations) {
            load(annotation);
        }
    }

    @Override public void load(Annotation annotation) throws ZoomaLoadingException {
        getAnnotationDAO().create(annotation);
    }
}
