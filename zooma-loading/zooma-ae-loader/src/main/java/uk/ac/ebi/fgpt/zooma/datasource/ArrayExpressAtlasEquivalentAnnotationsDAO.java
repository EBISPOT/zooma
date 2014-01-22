package uk.ac.ebi.fgpt.zooma.datasource;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.OntologyAccessionUtils;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An annotation DAO that reads Annotations from ArrayExpress and Atlas and detects equivalences between them.  Once
 * equivalences are identified, semantic tags that are assigned in the Atlas are applied to any equivalent annotations
 * in ArrayExpress and given an evidence code.
 * <p/>
 * This DAO should be initialized using init() at startup to preload all Atlas Annotations, then on every read operation
 * they are cross-resolved against the preloaded Atlas set
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class ArrayExpressAtlasEquivalentAnnotationsDAO extends Initializable implements AnnotationDAO {
    private AnnotationDAO atlasAnnotationDAO;
    private AnnotationDAO arrayexpressAnnotationDAO;

    private final Map<String, Map<String, Set<Annotation>>> studyToBiologicalEntityToAnnotationMap;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ArrayExpressAtlasEquivalentAnnotationsDAO() {
        this("http://www.ebi.ac.uk/efo/efo.owl");
    }

    public ArrayExpressAtlasEquivalentAnnotationsDAO(Resource efoResource) throws IOException {
        this.studyToBiologicalEntityToAnnotationMap =
                Collections.synchronizedMap(new HashMap<String, Map<String, Set<Annotation>>>());
        try {
            OntologyAccessionUtils.loadOntology(efoResource.getURL());
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error loading EFO from resource " + efoResource.toString(), e);
        }
    }

    public ArrayExpressAtlasEquivalentAnnotationsDAO(String efoURL) {
        this.studyToBiologicalEntityToAnnotationMap = Collections.synchronizedMap(
                new HashMap<String, Map<String, Set<Annotation>>>());
        try {
            URL efo = new URL(efoURL);
            OntologyAccessionUtils.loadOntology(efo);
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error forming EFO URL", e);
        }
    }

    public AnnotationDAO getAtlasAnnotationDAO() {
        return atlasAnnotationDAO;
    }

    public void setAtlasAnnotationDAO(AnnotationDAO atlasAnnotationDAO) {
        this.atlasAnnotationDAO = atlasAnnotationDAO;
    }

    public AnnotationDAO getArrayExpressAnnotationDAO() {
        return arrayexpressAnnotationDAO;
    }

    public void setArrayExpressAnnotationDAO(AnnotationDAO arrayexpressAnnotationDAO) {
        this.arrayexpressAnnotationDAO = arrayexpressAnnotationDAO;
    }

    @Override public String getDatasourceName() {
        return getArrayExpressAnnotationDAO().getDatasourceName();
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return mapAllEquivalence(getArrayExpressAnnotationDAO().readByStudy(study));
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return mapAllEquivalence(getArrayExpressAnnotationDAO().readByBiologicalEntity(biologicalEntity));
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return mapAllEquivalence(getArrayExpressAnnotationDAO().readByProperty(property));
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return mapAllEquivalence(getArrayExpressAnnotationDAO().readBySemanticTag(semanticTagURI));
    }

    @Override public int count() {
        return getArrayExpressAnnotationDAO().count();
    }

    @Override public void create(Annotation identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("This operation is not supported in this DAO implementation");
    }

    @Override public void create(Collection<Annotation> identifiable) throws ResourceAlreadyExistsException {
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
        return mapAllEquivalence(getArrayExpressAnnotationDAO().read());
    }

    @Override public List<Annotation> read(int size, int start) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        getLog().debug("DAO is initialized, dispatching query from " + start + " (size " + size + ")");
        return mapAllEquivalence(getArrayExpressAnnotationDAO().read(size, start));
    }

    @Override public Annotation read(URI uri) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            getLog().warn("Interrupted whilst waiting for initialization");
        }
        return mapEquivalence(getArrayExpressAnnotationDAO().read(uri));
    }

    @Override public void update(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException("This operation is not supported in this DAO implementation");
    }

    @Override public void delete(Annotation object) throws NoSuchResourceException {
        throw new UnsupportedOperationException("This operation is not supported in this DAO implementation");
    }

    @Override protected void doInitialization() throws Exception {
        getLog().debug("Preloading all atlas annotations...");
        Collection<Annotation> atlasAnnotations = getAtlasAnnotationDAO().read();
        getLog().debug("Atlas annotations loaded and ready for equivalence resolving");

        getLog().debug("Mapping atlas annotations by study accession and biological entity name...");
        for (Annotation atlasAnnotation : atlasAnnotations) {
            for (BiologicalEntity biologicalEntity : atlasAnnotation.getAnnotatedBiologicalEntities()) {
                for (Study study : biologicalEntity.getStudies()) {
                    String studyAcc = study.getAccession();
                    if (!studyToBiologicalEntityToAnnotationMap.containsKey(studyAcc)) {
                        studyToBiologicalEntityToAnnotationMap.put(studyAcc, new HashMap<String, Set<Annotation>>());
                    }
                    Map<String, Set<Annotation>> beToAnnotationMap =
                            studyToBiologicalEntityToAnnotationMap.get(studyAcc);

                    String beName = ZoomaUtils.normalizePropertyTypeString(biologicalEntity.getName());
                    if (!beToAnnotationMap.containsKey(beName)) {
                        beToAnnotationMap.put(beName, new HashSet<Annotation>());
                    }
                    Set<Annotation> annotations = beToAnnotationMap.get(beName);
                    annotations.add(atlasAnnotation);
                }
            }
        }
        getLog().debug("Mapping complete!");
    }

    @Override protected void doTermination() throws Exception {
        getLog().debug("Nothing to terminate");
    }

    private List<Annotation> mapAllEquivalence(Collection<Annotation> annotations) {
        // map equivalences
        List<Annotation> results = new ArrayList<>();
        for (Annotation aeAnno : annotations) {
            Annotation resultAnno = mapEquivalence(aeAnno);
            if (resultAnno != null) {
                results.add(resultAnno);
                getLog().debug(
                        "Used atlas data to map " + resultAnno.getURI() + " to terms '" + resultAnno.getSemanticTags() +
                                "'");
            }
        }
        return results;
    }

    private Annotation mapEquivalence(Annotation annotation) {
        String study;
        String biologicalEntity;
        String propertyType;
        String propertyValue;

        Annotation resultAnnotation = null;
        for (BiologicalEntity be : annotation.getAnnotatedBiologicalEntities()) {
            // do a quick screen - if this biological entity has multiple typed properties of the same type,
            // do not try to map equivalence

            for (Study s : be.getStudies()) {
                if (annotation.getAnnotatedProperty() instanceof TypedProperty) {
                    study = s.getAccession();
                    biologicalEntity = ZoomaUtils.normalizePropertyTypeString(be.getName());
                    propertyType = ZoomaUtils.normalizePropertyTypeString(
                            ((TypedProperty) annotation.getAnnotatedProperty()).getPropertyType());
                    propertyValue = annotation.getAnnotatedProperty().getPropertyValue();

                    Annotation atlasAnnotation = lookupAtlasAnnotation(study, biologicalEntity, propertyType);

                    if (atlasAnnotation != null) {
                        AnnotationProvenance originalProvenance = annotation.getProvenance();
                        AnnotationProvenance resultProvenance =
                                new SimpleAnnotationProvenance(originalProvenance.getSource(),
                                        AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                                        "ZOOMA",
                                        originalProvenance.getGeneratedDate());

                        Set<URI> semanticTags = new HashSet<>();
                        for (URI uri : atlasAnnotation.getSemanticTags()) {
                            if (uri != null) {
                                IRI iri = OntologyAccessionUtils.getIRIFromAccession(uri.toString());
                                if (iri != null) {
                                    semanticTags.add(iri.toURI());
                                }
                            }
                        }

                        URI[] resultSemanticTags = semanticTags.toArray(new URI[semanticTags.size()]);
                        if (resultSemanticTags.length > 0) {
                            resultAnnotation = new SimpleAnnotation(annotation.getURI(),
                                    annotation.getAnnotatedBiologicalEntities(),
                                    annotation.getAnnotatedProperty(),
                                    resultProvenance,
                                    resultSemanticTags);
                            getLog().debug("Generated new annotation using mapping to atlas...\n\t" +
                                    "Study: " + study + ",\n\t" +
                                    "Biological Entity: " + biologicalEntity + ",\n\t" +
                                    "Property Type: " + propertyType + ",\n\t" +
                                    "Property Value: " + propertyValue + ",\n\t" +
                                    "Semantic Tags: " + Arrays.toString(resultSemanticTags) + "\n" +
                                    "Annotation: " + resultAnnotation);
                        }
                    }
                }
            }
        }

        return resultAnnotation;
    }

    private Annotation lookupAtlasAnnotation(String study, String biologicalEntity, String propertyType) {
        Set<Annotation> matchedTypeAnnotations = new HashSet<>();
        if (studyToBiologicalEntityToAnnotationMap.containsKey(study)) {
            Map<String, Set<Annotation>> beToAnnotationsMap = studyToBiologicalEntityToAnnotationMap.get(study);
            if (beToAnnotationsMap.containsKey(biologicalEntity)) {
                for (Annotation annotation : beToAnnotationsMap.get(biologicalEntity)) {
                    if (annotation.getAnnotatedProperty() instanceof TypedProperty) {
                        String candidateType = ((TypedProperty) annotation.getAnnotatedProperty()).getPropertyType();
                        if (propertyType.equals(ZoomaUtils.normalizePropertyTypeString(candidateType))) {
                            matchedTypeAnnotations.add(annotation);
                        }
                    }
                }
            }
        }

        if (matchedTypeAnnotations.size() == 1) {
            return matchedTypeAnnotations.iterator().next();
        }
        else {
            return null;
        }
    }
}
