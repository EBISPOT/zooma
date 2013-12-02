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
    private AnnotationFactory annotationFactory;

    public OmimAnnotationDAO() {
        this(new OmimAnnotationFactory(new OmimLoadingSession()));
    }

    public OmimAnnotationDAO(AnnotationFactory annotationFactory) {
        this.annotationFactory = annotationFactory;
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        throw new UnsupportedOperationException("Cannot query OMIM by study");
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        throw new UnsupportedOperationException("Cannot query OMIM by biological entity");
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        return null;
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        return null;
    }

    @Override public String getDatasourceName() {
        return "omim";
    }

    @Override public int count() {
        return 0;
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
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
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, updates not supported");
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, deletions not supported");
    }
}
