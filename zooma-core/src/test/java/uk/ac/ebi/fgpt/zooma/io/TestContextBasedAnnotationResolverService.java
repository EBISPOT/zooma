package uk.ac.ebi.fgpt.zooma.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Tony Burdett
 * @date 23/10/12
 */
public class TestContextBasedAnnotationResolverService {
    private BiologicalEntity biologicalEntity;

    private Annotation resolvableAnnotation;
    private Annotation unresolvableAnnotation;
    private Annotation updatedAnnotation;
    private Annotation modifiedAnnotation;
    private Annotation emptyAnnotation;

    private URI incrementTestFirst;
    private URI incrementTestRepeat;
    private URI incrementTestRepeat1;
    private URI incrementTestRepeat2;

    private BiologicalEntityDAO biologicalEntityDAO;
    private AnnotationDAO annotationDAO;

    private ContextBasedAnnotationResolver resolver;
//    private ContextBasedAnnotationResolver parallelResolver;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @BeforeEach
    public void setup() {
        URI studyURI = URI.create("http://www.ebi.ac.uk/zooma/test/study");
        URI biologicalEntityURI = URI.create("http://www.ebi.ac.uk/zooma/test/biological_entity");
        URI sourceURI = URI.create("http://www.ebi.ac.uk/zooma/test");

        URI resolvablePropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/resolvable_property");
        URI unresolvablePropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/unresolvable_property");
        URI modifiedPropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/modified_property");

        URI semanticTagURI = URI.create("http://www.ebi.ac.uk/zooma/test/semantic_tag");

        URI resolvableAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/resolvable_annotation");
        URI unresolvableAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/unresolvable_annotation");
        URI modifiedAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/modified_annotation");
        URI emptyAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/empty_annotation");

        AnnotationSource source = new SimpleDatabaseAnnotationSource(sourceURI, "test");
        AnnotationProvenance provenance = new SimpleAnnotationProvenance(
                source, AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED, "creator", new Date());
        Study study = new SimpleStudy(studyURI, "study");
        biologicalEntity = new SimpleBiologicalEntity(biologicalEntityURI, "bioentity", study);

        Property resolvableProperty = new SimpleTypedProperty(resolvablePropertyURI, "T", "resolvable");
        Property unresolvableProperty = new SimpleTypedProperty(unresolvablePropertyURI, "S", "unresolvable");
        Property modifiedProperty = new SimpleTypedProperty(modifiedPropertyURI, "T", "resolvable (modified)");

        Collection<BiologicalEntity> biologicalEntities = Collections.singleton(biologicalEntity);


        resolvableAnnotation = new SimpleAnnotation(resolvableAnnotationURI,
                                                    biologicalEntities,
                                                    resolvableProperty,
                                                    provenance,
                                                    semanticTagURI);
        unresolvableAnnotation = new SimpleAnnotation(unresolvableAnnotationURI,
                                                      biologicalEntities,
                                                      unresolvableProperty,
                                                      provenance,
                                                      semanticTagURI);
        updatedAnnotation = new SimpleAnnotation(resolvableAnnotationURI,
                                                 biologicalEntities,
                                                 modifiedProperty,
                                                 provenance,
                                                 semanticTagURI);
        modifiedAnnotation = new SimpleAnnotation(modifiedAnnotationURI,
                                                  biologicalEntities,
                                                  modifiedProperty,
                                                  provenance,
                                                  semanticTagURI);
        emptyAnnotation = new SimpleAnnotation(emptyAnnotationURI,
                                               biologicalEntities,
                                               resolvableProperty,
                                               provenance);

        incrementTestFirst = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_first");
        incrementTestRepeat = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat");
        incrementTestRepeat1 = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat_1");
        incrementTestRepeat2 = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat_2");

        annotationDAO = mock(AnnotationDAO.class);
        when(annotationDAO.read(resolvableAnnotationURI)).thenReturn(resolvableAnnotation);
        when(annotationDAO.readByBiologicalEntity(biologicalEntity)).thenReturn(Collections.singleton(
                resolvableAnnotation));
        when(annotationDAO.read(incrementTestFirst)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat1)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat2)).thenReturn(resolvableAnnotation);

        biologicalEntityDAO = mock(BiologicalEntityDAO.class);
        when(biologicalEntityDAO.read(biologicalEntityURI)).thenReturn(biologicalEntity);

        resolver = new ContextBasedAnnotationResolver(1);
        resolver.setZoomaAnnotationDAO(annotationDAO);
        resolver.setZoomaBiologicalEntityDAO(biologicalEntityDAO);
    }

    @AfterEach
    public void teardown() {
        resolver.shutdown();
        resolver = null;
    }

    @Test
    public void testResolveAllAnnotations() {
        getLog().info("Testing resolve()");

        try {
            Collection<Annotation> annotations1 = new HashSet<>();
            Collections.addAll(annotations1,
                               resolvableAnnotation,
                               unresolvableAnnotation,
                               modifiedAnnotation,
                               emptyAnnotation);
            Collection<Annotation> results1 = resolver.resolve("Test 1", annotations1);
            // expect 2 results = unresolvable and modified
            assertEquals(2, results1.size(), "Unexpected number of results");
            // resolvable annotation already exists, so should be absent
            assertFalse(results1.contains(resolvableAnnotation), "Results contains unexpected annotation");
            // unresolvable annotation does not exist so should be present
            assertTrue(results1.contains(unresolvableAnnotation), "Results is missing expected annotation");
            // modified annotation is new, so should be present
            assertTrue(results1.contains(modifiedAnnotation), "Results is missing expected annotation");
            // empty annotation has no semantic tag so should be excluded
            assertFalse(results1.contains(emptyAnnotation), "Results contains unexpected annotation");


            Collection<Annotation> annotations2 = new HashSet<>();
            Collections.addAll(annotations2,
                               updatedAnnotation);
            Collection<Annotation> results2 = resolver.resolve("Test 2", annotations2);
            // expect 1 result = new version of updated
            assertEquals(1, results2.size(), "Unexpected number of results");
            // updated OR resolvable annotation already exists, so should be absent
            assertFalse(results2.contains(updatedAnnotation), "Results contains unexpected annotation");
            assertFalse(results2.contains(resolvableAnnotation), "Results contains unexpected annotation");
            // updated annotation should cause a new annotation to be added
            boolean foundRelated = false;
            for (Annotation a : results2) {
                if (resolver.wasModified(a)) {
                    if (!annotations2.contains(a)) {
                        // this annotation is new
                        getLog().debug("Related annotation found: " + a.toString());
                        foundRelated = true;
                        break;
                    }
                }
            }
            assertTrue(foundRelated, "Results is missing expected annotation");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testResolveAnnotation() {
        getLog().info("Testing resolve()");

        Annotation result;
        try {
            // if annotation resolves, should get null result
            result = resolver.resolve(resolvableAnnotation);
            getLog().debug("Testing resolvableAnnotation does resolve");
            assertNull(result, "Result of annotation resolving is not null");
            getLog().debug("Result was null as expected");

            // if annotation does not resolve, should get exact annotation back as result
            result = resolver.resolve(unresolvableAnnotation);
            getLog().debug("Testing unresolvableAnnotation does not resolve");
            assertSame(result, unresolvableAnnotation, "Result of annotation resolving is not the same");
            getLog().debug("Result was same as expected");

            // if annotation exists (by URI) but is updated, we should get back a new annotation linked to the old one
            result = resolver.resolve(updatedAnnotation);
            getLog().debug("Testing updatedAnnotation resolves and returns an updated form");
            assertNotSame(result, updatedAnnotation, "Result is the same annotation as that resolved");
            getLog().debug("Result was not the same as expected");
            getLog().debug("Annotation comparison:" +
                                   "\n\tResult:  \t" + result.toString() +
                                   "\n\tOriginal:\t" + updatedAnnotation.toString());
            assertTrue(result.getReplaces().contains(resolvableAnnotation.getURI()),
                    "Result is not replaced by updatedAnnotation");

            // if annotation doesn't exist but is a modified version of an old one, we should get back our original annotation, but linked to an old one
            result = resolver.resolve(modifiedAnnotation);
            assertTrue(result.getReplaces().contains(resolvableAnnotation.getURI()),
                    "No relation between result and resolvableAnnotation");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testFilterAnnotations() {
        getLog().info("Testing filter()");

        Collection<Annotation> results;
        try {
            results = resolver.filter(Collections.singleton(resolvableAnnotation));
            assertTrue(results.contains(resolvableAnnotation), "resolvableAnnotation was unexpectedly filtered");

            results = resolver.filter(Collections.singleton(emptyAnnotation));
            assertFalse(results.contains(emptyAnnotation), "emptyAnnotation was unexpectedly preserved");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAnnotationExists() {
        getLog().info("Testing exists()");
        assertTrue(resolver.exists(resolvableAnnotation), "Annotation does not exist as expected");
        assertFalse(resolver.exists(unresolvableAnnotation), "Annotation exists, but shouold not");
    }

    @Test
    public void testIsAnnotationUpdated() {
        getLog().info("Testing isUpdated()");
        assertTrue(resolver.isUpdated(updatedAnnotation, resolvableAnnotation),
                "Annotation not detected as updated");
        assertTrue(resolver.isUpdated(modifiedAnnotation, resolvableAnnotation),
                "Annotation not detected as updated");
        assertFalse(resolver.isUpdated(resolvableAnnotation, resolvableAnnotation),
                "Annotation detected as updated unexpectedly");
    }

    @Test
    public void testWasAnnotationModified() {
        getLog().info("Testing wasModified()");
        assertTrue(resolver.wasModified(updatedAnnotation),
                "Annotation not detected as modified");
        assertTrue(resolver.wasModified(modifiedAnnotation),
                "Annotation not detected as modified");
        assertFalse(resolver.wasModified(unresolvableAnnotation), "Annotation not detected as modified");
    }

    @Test
    public void testFindModifiedAnnotation() {
        getLog().info("Testing findModified()");
        assertSame(resolver.findModified(updatedAnnotation),
                   resolvableAnnotation, "Annotation not detected as modified");
        assertSame(resolver.findModified(modifiedAnnotation),
                   resolvableAnnotation, "Annotation not detected as modified");
        assertNull(resolver.findModified(unresolvableAnnotation), "Annotation detected as modified unexpectedly");
    }

    @Test
    public void testGetModification() {
        getLog().info("Testing getModification()");
        Annotation result;
        try {
            result = resolver.findModified(updatedAnnotation);
            assertEquals(ZoomaResolver.Modification.PROPERTY_VALUE_MODIFICATION,
                         resolver.getModification(updatedAnnotation, result),
                    "Annotation modification does not match expected");
            result = resolver.findModified(modifiedAnnotation);
            assertEquals(ZoomaResolver.Modification.PROPERTY_VALUE_MODIFICATION,
                         resolver.getModification(modifiedAnnotation, result),
                    "Annotation modification does not match expected");
            result = resolver.findModified(resolvableAnnotation);
            assertEquals(ZoomaResolver.Modification.NO_MODIFICATION,
                         resolver.getModification(resolvableAnnotation, result),
                    "Annotation modification does not match expected");
            result = resolver.findModified(unresolvableAnnotation);
            assertNull(result, "Annotation modification does not match expected");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Caught unexpected exception (" + e.getMessage() + ")");
        }
    }

    @Test
    public void testIncrementAnnotationURI() {
        getLog().info("Testing incrementAnnotationURI()");

        URI result;
        try {
            result = resolver.incrementAnnotationURI(incrementTestFirst);
            assertTrue(result.toString().equals(incrementTestFirst.toString() + "_1"));

            result = resolver.incrementAnnotationURI(incrementTestRepeat);
            assertFalse(result.toString().equals(incrementTestRepeat1.toString()),
                    "Result should not match a pre-existing one");
            assertFalse(result.toString().equals(incrementTestRepeat2.toString()),
                    "Result should not match a pre-existing one");
            assertTrue(result.toString().equals(incrementTestRepeat.toString() + "_3"));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
