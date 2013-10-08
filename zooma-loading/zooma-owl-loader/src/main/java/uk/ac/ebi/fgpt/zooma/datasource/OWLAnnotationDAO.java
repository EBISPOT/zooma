package uk.ac.ebi.fgpt.zooma.datasource;

import org.semanticweb.owlapi.model.IRI;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An annotation DAO that converts an ontology into a series of annotations using the OWLAPI.
 * <p/>
 * Annotations in the ZOOMA sense are derived from an ontology by converting label and synonym OWL annotation properties
 * into ZOOMA untyped properties and annotating them against the URI of the class they are associated with.  Whilst this
 * process is slightly redundant, doing so allows us to attach search more easily over ontology labels and also allows
 * for the attachment of provenance information about the annotation and additional information that is not encoded in
 * the label itself but may be stored in the ontology elsewhere.
 *
 * @author Tony Burdett
 * @author James Malone
 * @date 23/10/12
 */
public class OWLAnnotationDAO extends Initializable implements AnnotationDAO {
    private AnnotationFactory annotationFactory;
    private OntologyLoader owlLoader;
    private String datasourceName;

    private List<Annotation> annotations;

    public OWLAnnotationDAO(AnnotationFactory annotationFactory, OntologyLoader owlLoader, String datasourceName) {
        this.datasourceName = datasourceName;
        this.annotationFactory = annotationFactory;
        this.owlLoader = owlLoader;
        this.annotations = Collections.synchronizedList(new ArrayList<Annotation>());
    }

    @Override public String getDatasourceName() {
        return datasourceName;
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        throw new UnsupportedOperationException("Ontology-based datasources do not contain studies");
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        throw new UnsupportedOperationException("Ontology-based datasources do not contain biological entities");
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        throw new UnsupportedOperationException("Property lookup not yet implemented");
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Semantic tag lookup not yet implemented");
    }

    @Override protected void doInitialization() throws Exception {
        // get the IRI to label mappings from the owlLoader
        Map<IRI, String> iriToLabelMap = owlLoader.getOntologyClassLabels();

        // get the IRI to types mappings from the owlLoader
        Map<IRI, Set<String>> iriToTypeLabelsMap = owlLoader.getOntologyClassTypeLabels();

        // get the IRI to synonym mappings from the owlLoader
        Map<IRI, Set<String>> iriToSynonymMap = owlLoader.getOntologyClassSynonyms();

        //check to see if there is anything in the map
        if (iriToLabelMap.isEmpty()) {
            getLog().error("No label to URI mappings found");
        }
        else {
            // create annotations for each label
            for (IRI classIRI : iriToLabelMap.keySet()) {
                URI semanticTag = classIRI.toURI();
                for (String propertyType : iriToTypeLabelsMap.get(classIRI)) {
                    String propertyValue = iriToLabelMap.get(classIRI);

                    // almost all params are null, because for ontology annotations we have no context -
                    // just values and semantic tags
                    // property type is a canned value
                    Annotation singleAnnotation = annotationFactory.createAnnotation(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            propertyType,
                            propertyValue,
                            null,
                            null,
                            semanticTag,
                            null,
                            null);

                    // add this annotation to the annotations collection
                    annotations.add(singleAnnotation);
                }
            }

            // and create annotations for each synonym
            for (IRI classIRI : iriToSynonymMap.keySet()) {
                URI semanticTag = classIRI.toURI();
                for (String propertyType : iriToTypeLabelsMap.get(classIRI)) {

                    for (String propertyValue : iriToSynonymMap.get(classIRI)) {
                        // almost all params are null, because for ontology annotations we have no context -
                        // just values and semantic tags
                        // property type is a canned value
                        Annotation singleAnnotation = annotationFactory.createAnnotation(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                propertyType,
                                propertyValue,
                                null,
                                null,
                                semanticTag,
                                null,
                                null);

                        // add this annotation to the annotations collection
                        annotations.add(singleAnnotation);
                    }
                }
            }
            getLog().debug("Loaded " + annotations.size() + " annotations from " + getDatasourceName());
        }
    }

    @Override protected void doTermination() throws Exception {
        getLog().debug("Nothing to terminate");
    }

    @Override public int count() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return annotations.size();
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " is a read-only annotation DAO, creation not supported");
    }

    @Override public Collection<Annotation> read() {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return annotations;
    }

    @Override public List<Annotation> read(int size, int start) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        if (start + size > annotations.size()) {
            return annotations.subList(start, annotations.size());
        }
        else {
            return annotations.subList(start, start + size);
        }
    }

    @Override public Annotation read(URI uri) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support URI based lookups");
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
