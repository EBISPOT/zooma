package uk.ac.ebi.fgpt.zooma.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Tony Burdett
 * @date 05/06/13
 */
public class TestTagBasedAnnotationResolverService {
    private Annotation resolvableAnnotation;
    private Annotation unresolvableAnnotation;
    private Annotation updatedAnnotation;
    private Annotation modifiedAnnotation;
    private Annotation emptyAnnotation;

    private URI incrementTestFirst;
    private URI incrementTestRepeat;
    private URI incrementTestRepeat1;
    private URI incrementTestRepeat2;

    private AnnotationDAO annotationDAO;

    private TagBasedAnnotationResolver resolver;
//    private ContextBasedAnnotationResolver parallelResolver;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Before
    public void setup() {
        URI studyURI = URI.create("http://www.ebi.ac.uk/zooma/test/study");
        URI sourceURI = URI.create("http://www.ebi.ac.uk/zooma/test");

        URI resolvablePropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/resolvable_property");
        URI unresolvablePropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/unresolvable_property");
        URI modifiedPropertyURI = URI.create("http://www.ebi.ac.uk/zooma/test/modified_property");

        URI semanticTagURI = URI.create("http://www.ebi.ac.uk/zooma/test/semantic_tag");

        URI resolvableAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/resolvable_annotation");
        URI unresolvableAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/unresolvable_annotation");
        URI modifiedAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/modified_annotation");
        URI emptyAnnotationURI = URI.create("http://www.ebi.ac.uk/zooma/test/empty_annotation");

        AnnotationSource source = new SimpleDatabaseAnnotationSource(sourceURI);
        AnnotationProvenance provenance = new SimpleAnnotationProvenance(
                source,
                AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                "creator",
                new Date());

        Property resolvableProperty = new SimpleTypedProperty(resolvablePropertyURI, "T", "resolvable");
        Property unresolvableProperty = new SimpleTypedProperty(unresolvablePropertyURI, "S", "unresolvable");
        Property modifiedProperty = new SimpleTypedProperty(modifiedPropertyURI, "T", "resolvable (modified)");

        resolvableAnnotation = new SimpleAnnotation(resolvableAnnotationURI,
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    resolvableProperty,
                                                    provenance,
                                                    semanticTagURI);
        unresolvableAnnotation = new SimpleAnnotation(unresolvableAnnotationURI,
                                                      Collections.<BiologicalEntity>emptySet(),
                                                      unresolvableProperty,
                                                      provenance,
                                                      semanticTagURI);
        updatedAnnotation = new SimpleAnnotation(resolvableAnnotationURI,
                                                 Collections.<BiologicalEntity>emptySet(),
                                                 modifiedProperty,
                                                 provenance,
                                                 semanticTagURI);
        modifiedAnnotation = new SimpleAnnotation(modifiedAnnotationURI,
                                                  Collections.<BiologicalEntity>emptySet(),
                                                  modifiedProperty,
                                                  provenance,
                                                  semanticTagURI);
        emptyAnnotation = new SimpleAnnotation(emptyAnnotationURI,
                                               Collections.<BiologicalEntity>emptySet(),
                                               resolvableProperty,
                                               provenance);

        incrementTestFirst = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_first");
        incrementTestRepeat = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat");
        incrementTestRepeat1 = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat_1");
        incrementTestRepeat2 = URI.create("http://www.ebi.ac.uk/zooma/test/increment_test_repeat_2");

        annotationDAO = mock(AnnotationDAO.class);
        when(annotationDAO.read(resolvableAnnotationURI)).thenReturn(resolvableAnnotation);
        when(annotationDAO.readBySemanticTag(semanticTagURI)).thenReturn(Collections.singleton(resolvableAnnotation));
        when(annotationDAO.read(incrementTestFirst)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat1)).thenReturn(resolvableAnnotation);
        when(annotationDAO.read(incrementTestRepeat2)).thenReturn(resolvableAnnotation);

        resolver = new TagBasedAnnotationResolver(1);
        resolver.setZoomaAnnotationDAO(annotationDAO);
    }

    @After
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
            assertEquals("Unexpected number of results", 2, results1.size());
            // resolvable annotation already exists, so should be absent
            assertFalse("Results contains unexpected annotation", results1.contains(resolvableAnnotation));
            // unresolvable annotation does not exist so should be present
            assertTrue("Results is missing expected annotation", results1.contains(unresolvableAnnotation));
            // modified annotation is new, so should be present
            assertTrue("Results is missing expected annotation", results1.contains(modifiedAnnotation));
            // empty annotation has no semantic tag so should be excluded
            assertFalse("Results contains unexpected annotation", results1.contains(emptyAnnotation));


            Collection<Annotation> annotations2 = new HashSet<>();
            Collections.addAll(annotations2,
                               updatedAnnotation);
            Collection<Annotation> results2 = resolver.resolve("Test 2", annotations2);
            // expect 1 result = new version of updated
            assertEquals("Unexpected number of results", 1, results2.size());
            // updated OR resolvable annotation already exists, so should be absent
            assertFalse("Results contains unexpected annotation", results2.contains(updatedAnnotation));
            assertFalse("Results contains unexpected annotation", results2.contains(resolvableAnnotation));
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
            assertTrue("Results is missing expected annotation", foundRelated);
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
            assertNull("Result of annotation resolving is not null", result);
            getLog().debug("Result was null as expected");

            // if annotation does not resolve, should get exact annotation back as result
            result = resolver.resolve(unresolvableAnnotation);
            getLog().debug("Testing unresolvableAnnotation does not resolve");
            assertSame("Result of annotation resolving is not the same", result, unresolvableAnnotation);
            getLog().debug("Result was same as expected");

            // if annotation exists (by URI) but is updated, we should get back a new annotation linked to the old one
            result = resolver.resolve(updatedAnnotation);
            getLog().debug("Testing updatedAnnotation resolves and returns an updated form");
            assertNotSame("Result is the same annotation as that resolved", result, updatedAnnotation);
            getLog().debug("Result was not the same as expected");
            getLog().debug("Annotation comparison:" +
                                   "\n\tResult:  \t" + result.toString() +
                                   "\n\tOriginal:\t" + updatedAnnotation.toString());
            assertTrue("Result is not replaced by updatedAnnotation", result.replaces().contains(resolvableAnnotation.getURI()));

            // if annotation doesn't exist but is a modified version of an old one, we should get back our original annotation, but linked to an old one
            result = resolver.resolve(modifiedAnnotation);
            assertTrue("No relation between result and resolvableAnnotation", result.replaces().contains(resolvableAnnotation.getURI()));
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
            assertTrue("resolvableAnnotation was unexpectedly filtered", results.contains(resolvableAnnotation));

            results = resolver.filter(Collections.singleton(emptyAnnotation));
            assertFalse("emptyAnnotation was unexpectedly preserved", results.contains(emptyAnnotation));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAnnotationExists() {
        getLog().info("Testing exists()");
        assertTrue("Annotation does not exist as expected", resolver.exists(resolvableAnnotation));
        assertFalse("Annotation exists, but shouold not", resolver.exists(unresolvableAnnotation));
    }

    @Test
    public void testIsAnnotationUpdated() {
        getLog().info("Testing isUpdated()");
        assertTrue("Annotation not detected as updated",
                   resolver.isUpdated(updatedAnnotation, resolvableAnnotation));
        assertTrue("Annotation not detected as updated",
                   resolver.isUpdated(modifiedAnnotation, resolvableAnnotation));
        assertFalse("Annotation detected as updated unexpectedly",
                    resolver.isUpdated(resolvableAnnotation, resolvableAnnotation));
    }

    @Test
    public void testWasAnnotationModified() {
        getLog().info("Testing wasModified()");
        assertTrue("Annotation not detected as modified", resolver.wasModified(updatedAnnotation));
        assertTrue("Annotation not detected as modified", resolver.wasModified(modifiedAnnotation));
        assertFalse("Annotation detected as modified unexpectedly",
                    resolver.wasModified(unresolvableAnnotation));
    }

    @Test
    public void testFindModifiedAnnotation() {
        getLog().info("Testing findModified()");
        assertSame("Annotation not detected as modified",
                   resolver.findModified(updatedAnnotation),
                   resolvableAnnotation);
        assertSame("Annotation not detected as modified",
                   resolver.findModified(modifiedAnnotation),
                   resolvableAnnotation);
        assertNull("Annotation detected as modified unexpectedly",
                   resolver.findModified(unresolvableAnnotation));
    }

    @Test
    public void testGetAnnotationModification() {
        getLog().info("Testing getAnnotationModification()");
        Annotation result;
        try {
            result = resolver.findModified(updatedAnnotation);
            assertEquals("Annotation modification does not match expected",
                         ZoomaResolver.Modification.PROPERTY_VALUE_MODIFICATION,
                         resolver.getModification(updatedAnnotation, result));
            result = resolver.findModified(modifiedAnnotation);
            assertEquals("Annotation modification does not match expected",
                         ZoomaResolver.Modification.PROPERTY_VALUE_MODIFICATION,
                         resolver.getModification(modifiedAnnotation, result));
            result = resolver.findModified(resolvableAnnotation);
            assertEquals("Annotation modification does not match expected",
                         ZoomaResolver.Modification.NO_MODIFICATION,
                         resolver.getModification(resolvableAnnotation, result));
            result = resolver.findModified(unresolvableAnnotation);
            assertNull("Annotation modification does not match expected", result);
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
            assertFalse("Result should not match a pre-existing one",
                        result.toString().equals(incrementTestRepeat1.toString()));
            assertFalse("Result should not match a pre-existing one",
                        result.toString().equals(incrementTestRepeat2.toString()));
            assertTrue(result.toString().equals(incrementTestRepeat.toString() + "_3"));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
