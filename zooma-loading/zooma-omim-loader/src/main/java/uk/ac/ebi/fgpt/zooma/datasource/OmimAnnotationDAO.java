package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimAnnotationDAO implements AnnotationDAO {
    @Override public Collection<Annotation> readByStudy(Study study) {
        return null;
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        return null;
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        return null;
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        return null;
    }

    @Override public String getDatasourceName() {
        return null;
    }

    @Override public int count() {
        return 0;
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
    }

    @Override public Collection<Annotation> read() {
        return null;
    }

    @Override public List<Annotation> read(int size, int start) {
        return null;
    }

    @Override public Annotation read(URI uri) {
        return null;
    }

    @Override public void update(Annotation object) throws NoSuchResourceException {
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
    }
}
