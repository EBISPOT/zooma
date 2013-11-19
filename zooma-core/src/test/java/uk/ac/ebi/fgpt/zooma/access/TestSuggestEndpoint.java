package uk.ac.ebi.fgpt.zooma.access;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestSuggestEndpoint {
    private String element;
    private String elementID;
    private String elementName;
    private String elementTypeID;
    private String elementTypeName;

    private SuggestEndpoint<String, URI> suggestEndpoint;

    @Before
    public void setUp() {
        element = "foo";
        elementID = "12345";
        elementName = element;
        elementTypeID = "/test/string";
        elementTypeName = "String";

        // create a mock implementation of suggest endpoint
        SuggestEndpoint mockEndpoint = new MockEndpoint();
        // and spy on this mock implementation
        suggestEndpoint = spy(mockEndpoint);
    }

    @After
    public void tearDown() {
        suggestEndpoint = null;
    }

    @Test
    public void testConvertToFlyoutResponse() {
        FlyoutResponse response = suggestEndpoint.convertToFlyoutResponse(element);
        verify(suggestEndpoint, atLeastOnce()).extractElementID(element);
        assertNotNull("HTML for flyout response should not be null", response.getHtml());
        assertTrue("Flyout HTML should start with '<div ...'", response.getHtml().startsWith("<div"));
    }

    @Test
    public void testConvertToSearchResponse() {
        SearchResponse response = suggestEndpoint.convertToSearchResponse("f", Collections.singleton(element));
        verify(suggestEndpoint, atLeastOnce()).extractElementName(element);
        verify(suggestEndpoint, atLeastOnce()).extractElementID(element);
        assertNotSame("Response should contain at least one result", 0, response.getResult().length);
    }

    @Test
    public void testConvertToSuggestResponse() {
        SuggestResponse response = suggestEndpoint.convertToSuggestResponse("f", Collections.singleton(element));
        verify(suggestEndpoint, atLeastOnce()).extractElementName(element);
        verify(suggestEndpoint, atLeastOnce()).extractElementID(element);
        assertNotSame("Response should contain at least one result", 0, response.getResults().length);
    }

    private class MockEndpoint extends SuggestEndpoint<String, URI> {
        @Override protected String extractElementID(String s) {
            return elementID;
        }

        @Override protected String extractElementName(String s) {
            return elementName;
        }

        @Override protected String extractElementTypeID() {
            return elementTypeID;
        }

        @Override protected String extractElementTypeName() {
            return elementTypeName;
        }

        @Override public FlyoutResponse flyout(URI id) {
            return null;
        }

        @Override public SearchResponse search(String query,
                                               String type,
                                               Boolean exact,
                                               Integer limit,
                                               Integer start,
                                               Boolean prefixed,
                                               String lang,
                                               String domain,
                                               String filter,
                                               Boolean html_escape,
                                               Boolean indent,
                                               String mql_output) {
            return null;
        }

        @Override public SuggestResponse suggest(String prefix,
                                                 String type,
                                                 String type_strict,
                                                 Integer limit,
                                                 Integer start) {
            return null;
        }
    }
}
