package uk.ac.ebi.fgpt.zooma.access;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeSearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestZoomaPropertyTypeSearchEngine {
    private ZoomaPropertyTypeSearcher propertyTypeSearchEngine;

    private PropertyTypeSearchService propertyTypeSearchService;

    private Sorter<String> propertyTypeSorter;
    private Limiter<String> propertyTypeLimiter;

    private List<String> types;
    private Map<String, Float> scoredTypes;
    private List<String> limitedTypes;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @SuppressWarnings("unchecked") @Before
    public void setUp() {
        // create the greek alphabet/numeric type
        String numberType = "greek numerals";
        String letterType = "greek alphabet";

        // create a collection of types
        types = new ArrayList<>();
        types.add(numberType);
        types.add(letterType);

        scoredTypes = new HashMap<>();
        scoredTypes.put(numberType, 1.0f);
        scoredTypes.put(letterType, 1.0f);

        // create "limited" collections of 1 result
        limitedTypes = new ArrayList<>();
        limitedTypes.add(numberType);

        // create mock search services
        PropertyTypeService propertyTypeService = mock(PropertyTypeService.class);
        propertyTypeSearchService = mock(PropertyTypeSearchService.class);

        // create mock sort/limiters
        propertyTypeSorter = (Sorter<String>) mock(Sorter.class);
        propertyTypeLimiter = (Limiter<String>) mock(Limiter.class);

        // create test stubs
        when(propertyTypeSearchService.searchByPrefix(argThat(new TypePrefixMatcher(types))))
                .thenReturn(types);
        when(propertyTypeSearchService.searchByPrefix(argThat(new TypePrefixMatcher(types))))
                .thenReturn(types);
        when(propertyTypeSorter.sort(anyCollection()))
                .thenReturn(types);
        when(propertyTypeSorter.sort(anyMap()))
                .thenReturn(types);
        when(propertyTypeLimiter.limit(anyList(), anyInt()))
                .thenReturn(limitedTypes);
        when(propertyTypeLimiter.limit(anyList(), anyInt(), anyInt()))
                .thenReturn(limitedTypes);

        // create search engine
        propertyTypeSearchEngine = new ZoomaPropertyTypeSearcher(propertyTypeService,
                                                                 propertyTypeSearchService,
                                                                 propertyTypeSorter,
                                                                 propertyTypeLimiter);
    }

    @After
    public void tearDown() {
        // don't need to do anything here
    }

    @Test
    public void testQueryForTypePrefix() {
        for (String type : types) {
            String prefix = type.substring(0, 3);
            getLog().debug("Testing query for type " + type);
            Collection<String> searchResults = propertyTypeSearchEngine.query(prefix);
            assertSame("Unexpected prefix search results returned for " + prefix, types, searchResults);
            verify(propertyTypeSearchService, atLeastOnce()).searchByPrefix(prefix);
        }
    }

    @Test
    public void testQueryForTypeLimited() {
        int i = 1;
        for (String type : types) {
            String prefix = type.substring(0, 3);
            getLog().debug("Testing query for type " + type + ", 10, 0");
            Collection<String> searchResults = propertyTypeSearchEngine.query(prefix, 10, 0);
            assertSame("Unexpected prefix search results returned for " + prefix, limitedTypes, searchResults);
            verify(propertyTypeSearchService, atLeastOnce()).searchByPrefix(prefix);
            verify(propertyTypeSorter, times(i)).sort(types);
            verify(propertyTypeLimiter, times(i++)).limit(types, 10, 0);
        }
    }

    private class TypePrefixMatcher extends ArgumentMatcher<String> {
        private Collection<String> knownTypes;

        private TypePrefixMatcher(Collection<String> knownTypes) {
            this.knownTypes = knownTypes;
        }

        public boolean matches(Object o) {
            if (o != null) {
                String prefix = o.toString();
                for (String p : knownTypes) {
                    if (p.startsWith(prefix)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
