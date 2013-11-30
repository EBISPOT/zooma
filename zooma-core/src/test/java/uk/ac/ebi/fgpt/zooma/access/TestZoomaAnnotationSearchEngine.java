package uk.ac.ebi.fgpt.zooma.access;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSearchService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Scorer;
import uk.ac.ebi.fgpt.zooma.util.Sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

public class TestZoomaAnnotationSearchEngine {
    private ZoomaAnnotationSearcher annotationSearchEngine;

    private AnnotationSearchService annotationSearchService;

    private Sorter<Annotation> annotationSorter;
    private Limiter<Annotation> annotationLimiter;
    private Scorer<Annotation> annotationScorer;

    private List<Annotation> annotations;
    private List<Annotation> limitedAnnotations;

    private Map<Annotation, Float> scoredAnnotations;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @SuppressWarnings("unchecked") @Before
    public void setUp() {
        // create the greek alphabet/numeric system
        String alpha = "alpha";
        String numberType = "greek numerals";

        // create typed number properties
        TypedProperty one = mock(TypedProperty.class);

        // create invocation stubs
        when(one.getPropertyValue()).thenReturn(alpha);
        when(one.getPropertyType()).thenReturn(numberType);

        // create annotations for each property
        Annotation a = mock(Annotation.class);

        // create invocation stubs
        when(a.getAnnotatedProperty()).thenReturn(one);

        // create a collection of annotations
        annotations = new ArrayList<>();
        annotations.add(a);

        // create a map of annotations to scores
        scoredAnnotations = new HashMap<>();
        scoredAnnotations.put(a, 1.0f);

        // create "limited" collections of 1 result
        limitedAnnotations = new ArrayList<>();
        limitedAnnotations.add(a);

        // create mock search services
        AnnotationService annotationService = mock(AnnotationService.class);
        annotationSearchService = mock(AnnotationSearchService.class);

        // create mock sort/limiters
        annotationSorter = (Sorter<Annotation>) mock(Sorter.class);
        annotationLimiter = (Limiter<Annotation>) mock(Limiter.class);
        annotationScorer = (Scorer<Annotation>)mock(Scorer.class);

        // create test stubs
        when(annotationService.getAnnotationsByProperty(one)).thenReturn(annotations);
        when(annotationSearchService.searchByPrefix(anyString())).thenReturn(annotations);
        when(annotationSearchService.searchByPrefix(anyString(), anyString())).thenReturn(annotations);
        when(annotationSorter.sort(anyCollection())).thenReturn(annotations);
        when(annotationSorter.sort(anyMap())).thenReturn(annotations);
        when(annotationLimiter.limit(anyList(), anyInt())).thenReturn(limitedAnnotations);
        when(annotationLimiter.limit(anyList(), anyInt(), anyInt())).thenReturn(limitedAnnotations);
        when(annotationScorer.score(anyCollection())).thenReturn(scoredAnnotations);
        when(annotationScorer.score(anyCollection(), anyString())).thenReturn(scoredAnnotations);
        when(annotationScorer.score(anyCollection(), anyString(), anyString())).thenReturn(scoredAnnotations);

        // create annotation search engine
        annotationSearchEngine = new ZoomaAnnotationSearcher(annotationService,
                                                             annotationSearchService,
                                                             annotationSorter,
                                                             annotationLimiter);
    }

    @After
    public void tearDown() {
        // don't need to do anything here
    }

    @Test
    public void testQueryPrefix() {
        for (Annotation number : annotations) {
            Property property = number.getAnnotatedProperty();
            String prefix = property.getPropertyValue().substring(0, 3);
            getLog().debug("Testing query for " + prefix);
            Collection<Annotation> searchResults = annotationSearchEngine.query(prefix);
            verify(annotationSearchService).searchByPrefix(prefix);
            assertEquals("Unexpected prefix search results returned for " + prefix, annotations, searchResults);
        }
    }

    @Test
    public void testQueryPrefixAndType() {
        int i = 1;
        for (Annotation number : annotations) {
            Property property = number.getAnnotatedProperty();
            String prefix = property.getPropertyValue().substring(0, 3);
            String type = ((TypedProperty) property).getPropertyType();
            getLog().debug("Testing query for " + prefix + ", " + type);
            Collection<Annotation> searchResults = annotationSearchEngine.query(prefix, type);
            verify(annotationSearchService).searchByPrefix(type, prefix);
            verify(annotationSorter, times(i++)).sort(annotations);
            assertEquals("Unexpected prefix and type search results returned for " + prefix,
                         annotations,
                         searchResults);
        }
    }

    @Test
    public void testQueryPrefixAndTypeLimited() {
        int i = 1;
        for (Annotation number : annotations) {
            Property property = number.getAnnotatedProperty();
            String prefix = property.getPropertyValue().substring(0, 3);
            String type = ((TypedProperty) property).getPropertyType();
            getLog().debug("Testing query for " + prefix + ", " + type + ", 10, 0");
            Collection<Annotation> searchResults = annotationSearchEngine.query(prefix, type, 10, 0);
            verify(annotationSearchService).searchByPrefix(type, prefix);
            verify(annotationSorter, times(i)).sort(annotations);
            verify(annotationLimiter, times(i++)).limit(annotations, 10, 0);
            assertEquals("Unexpected prefix, type, limit search results returned for " + prefix,
                         limitedAnnotations,
                         searchResults);
        }
    }
}
