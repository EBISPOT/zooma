package uk.ac.ebi.fgpt.zooma.access;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.service.PropertySearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestZoomaPropertySearchEngine {
    private ZoomaProperties propertySearchEngine;

    private PropertySearchService propertySearchService;

    private Sorter<Property> propertySorter;
    private Limiter<Property> propertyLimiter;

    private List<Property> numbers;
    private Map<Property, Float> scoredNumbers;
    private List<Property> limitedProperties;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @SuppressWarnings("unchecked") @Before
    public void setUp() {
        // create the greek alphabet/numeric system
        String alpha = "alpha";
        String beta = "beta";
        String gamma = "gamma";
        String delta = "delta";
        String epsilon = "epsilon";
        String digamma = "digamma";
        String zeta = "zeta";
        String eta = "eta";
        String theta = "theta";
        String iota = "iota";

        // create typed number properties
        TypedProperty one = mock(TypedProperty.class);
        TypedProperty two = mock(TypedProperty.class);
        TypedProperty three = mock(TypedProperty.class);
        TypedProperty four = mock(TypedProperty.class);
        TypedProperty five = mock(TypedProperty.class);
        TypedProperty six = mock(TypedProperty.class);
        TypedProperty seven = mock(TypedProperty.class);
        TypedProperty eight = mock(TypedProperty.class);
        TypedProperty nine = mock(TypedProperty.class);
        TypedProperty ten = mock(TypedProperty.class);

        // create invocation stubs
        when(one.getPropertyValue()).thenReturn(alpha);
        when(two.getPropertyValue()).thenReturn(beta);
        when(three.getPropertyValue()).thenReturn(gamma);
        when(four.getPropertyValue()).thenReturn(delta);
        when(five.getPropertyValue()).thenReturn(epsilon);
        when(six.getPropertyValue()).thenReturn(digamma);
        when(seven.getPropertyValue()).thenReturn(zeta);
        when(eight.getPropertyValue()).thenReturn(eta);
        when(nine.getPropertyValue()).thenReturn(theta);
        when(ten.getPropertyValue()).thenReturn(iota);

        // create a collection of numbers
        numbers = new ArrayList<>();
        numbers.add(one);
        numbers.add(two);
        numbers.add(three);
        numbers.add(four);
        numbers.add(five);
        numbers.add(six);
        numbers.add(seven);
        numbers.add(eight);
        numbers.add(nine);
        numbers.add(ten);

        // create a scored map of numbers
        scoredNumbers = new HashMap<>();
        scoredNumbers.put(one, 1.0f);
        scoredNumbers.put(two, 1.0f);
        scoredNumbers.put(three, 1.0f);
        scoredNumbers.put(four, 1.0f);
        scoredNumbers.put(five, 1.0f);
        scoredNumbers.put(six, 1.0f);
        scoredNumbers.put(seven, 1.0f);
        scoredNumbers.put(eight, 1.0f);
        scoredNumbers.put(nine, 1.0f);
        scoredNumbers.put(ten, 1.0f);

        // create "limited" collections of 1 result
        limitedProperties = new ArrayList<>();
        limitedProperties.add(one);

        // create mock search services
        PropertyService propertyService = mock(PropertyService.class);
        propertySearchService = mock(PropertySearchService.class);

        // create mock sort/limiters
        propertySorter = (Sorter<Property>) mock(Sorter.class);
        propertyLimiter = (Limiter<Property>) mock(Limiter.class);

        // create test stubs
        when(propertySearchService.searchByPrefix(argThat(new PropertyPrefixMatcher(numbers))))
                .thenReturn(numbers);
        when(propertySearchService.searchByPrefix(anyString(), argThat(new PropertyPrefixMatcher(numbers))))
                .thenReturn(numbers);
        when(propertySorter.sort(anyCollection()))
                .thenReturn(numbers);
        when(propertySorter.sort(anyMap()))
                .thenReturn(numbers);
        when(propertyLimiter.limit(anyList(), anyInt(), anyInt()))
                .thenReturn(limitedProperties);
        when(propertyLimiter.limit(anyList(), anyInt()))
                .thenReturn(limitedProperties);

        // create search engine
        propertySearchEngine = new ZoomaProperties(propertyService,
                                                         propertySearchService,
                                                         propertySorter,
                                                         propertyLimiter);
    }

    @After
    public void tearDown() {
        // don't need to do anything here
    }

    @Test
    public void testQueryPrefix() {
        for (Property number : numbers) {
            String prefix = number.getPropertyValue().substring(0, 3);
            getLog().debug("Testing query for " + prefix);
            Collection<Property> searchResults = propertySearchEngine.query(prefix);
            assertSame("Unexpected prefix search results returned for " + prefix, numbers, searchResults);
            verify(propertySearchService).searchByPrefix(prefix);
        }
    }

    @Test
    public void testQueryPrefixAndType() {
        for (Property number : numbers) {
            String prefix = number.getPropertyValue().substring(0, 3);
            String type = ((TypedProperty) number).getPropertyType();
            getLog().debug("Testing query for " + prefix + ", " + type);
            Collection<Property> searchResults = propertySearchEngine.query(prefix, type);
            assertSame("Unexpected prefix and type search results returned for " + prefix, numbers, searchResults);
            verify(propertySearchService).searchByPrefix(type, prefix);
        }
    }

    @Test
    public void testQueryPrefixAndTypeLimited() {
        int i = 1;
        for (Property number : numbers) {
            String prefix = number.getPropertyValue().substring(0, 3);
            String type = ((TypedProperty) number).getPropertyType();
            getLog().debug("Testing query for " + prefix + ", " + type + ", 10, 0");
            Collection<Property> searchResults = propertySearchEngine.query(prefix, type, 10, 0);
            assertSame("Unexpected prefix, type, limit search results returned for " + prefix,
                       limitedProperties,
                       searchResults);
            verify(propertySearchService).searchByPrefix(type, prefix);
            verify(propertySorter, times(i)).sort(numbers);
            verify(propertyLimiter, times(i++)).limit(numbers, 10, 0);
        }
    }

    private class PropertyPrefixMatcher extends ArgumentMatcher<String> {
        private Collection<Property> knownProperties;

        private PropertyPrefixMatcher(Collection<Property> knownProperties) {
            this.knownProperties = knownProperties;
        }

        @Override
        public boolean matches(Object o) {
            if (o != null) {
                String prefix = o.toString();
                for (Property p : knownProperties) {
                    if (p.getPropertyValue().startsWith(prefix)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
