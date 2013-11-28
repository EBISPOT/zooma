package uk.ac.ebi.fgpt.zooma.util;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.*;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/09/12
 */
public class TestURIUtils {
    private String prefix1;
    private String prefix2;
    private String prefix3;

    private String inferredPrefix;

    private String namespace1;
    private String namespace2;
    private String namespace3;
    private String namespace4;
    private String namespace5;
    private String namespace6;

    private String inferredNamespace;

    private String shortform1;
    private String shortform2;

    private String inferredShortform;

    private URI uri1;
    private URI uri2;
    private URI uri3;
    private URI uri4;
    private URI uri5;
    private URI uri6;
    private URI uri7;
    private URI uri8;
    private URI uri9;

    private URI inferredURI;

    @Before
    public void setUp() {
        prefix1 = "slash";
        prefix2 = "hash";
        prefix3 = "baz";

        inferredPrefix = "fooresource";

        namespace1 = "http://www.test.com/";
        namespace2 = "http://www.test.com/foo/";
        namespace3 = "http://www.test.com/bar#";
        namespace4 = "http://www.test.com/foo/bar/baz#";

        inferredNamespace = "http://rdf.ebi.ac.uk/resource/zooma/foo/";

        shortform1 = prefix1 + ":term";
        shortform2 = prefix2 + ":term";

        inferredShortform = inferredPrefix + ":term";

        uri1 = URI.create(namespace2 + "term");
        uri2 = URI.create(namespace3 + "term");
        uri3 = URI.create(namespace4 + "term");

        namespace5 = "http://www.anothertest.com/foo/";
        namespace6 = "http://www.yetanothertest.com/foo#";

        uri4 = URI.create(namespace5 + "term");
        uri5 = URI.create(namespace6 + "term");

        uri6 = URI.create(namespace2 + "bar/term");
        uri7 = URI.create(namespace1 + "term");
        uri8 = URI.create(namespace1);
        uri9 = URI.create(namespace2 + "bar#term");

        inferredURI = URI.create(inferredNamespace + "term");

        // reload "clean" prefix mappings
        URIUtils.loadPrefixMappings();
    }

    public void tearDown() {
        URIUtils.PREFIX_CREATION_MODE = URIUtils.DEFAULT_PREFIX_CREATION_MODE;
    }

    @Test
    public void testGetPrefixMappings() {
        Map<String, String> prefixMappings = URIUtils.getPrefixMappings();
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("zooma/prefix.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't load prefix properties");
        }
        assertEquals("Unexpected number of prefixes", properties.size(), prefixMappings.size());
        assertTrue("Could not find 'slash' prefix", prefixMappings.containsKey("slash"));
        assertTrue("Could not find 'hash' prefix", prefixMappings.containsKey("hash"));
        System.out.println("Contents of prefix mappings: " + prefixMappings.toString());
    }

    @Test
    public void testGetShortform() {
        String result;
        result = URIUtils.getShortform(uri1);
        System.out.println("Shortened " + uri1 + " -> " + result);
        assertEquals("Unexpected shortened form", prefix1 + ":term", result);
        result = URIUtils.getShortform(uri2);
        System.out.println("Shortened " + uri2 + " -> " + result);
        assertEquals("Unexpected shortened form", prefix2 + ":term", result);
    }

    @Test
    public void testGetShortformCustomMappings() {
        Map<String, String> pm = new HashMap<>();
        pm.put(prefix1, namespace2);
        pm.put(prefix2, namespace3);
        pm.put(prefix3, namespace4);

        assertEquals("Unexpected shortened form", prefix1 + ":term", URIUtils.getShortform(pm, uri1));
        assertEquals("Unexpected shortened form", prefix2 + ":term", URIUtils.getShortform(pm, uri2));
        assertEquals("Unexpected shortened form", prefix3 + ":term", URIUtils.getShortform(pm, uri3));
    }


    @Test
    public void testGetURI() {
        Map<String, String> pm = new HashMap<>();
        pm.put(prefix1, namespace2);
        pm.put(prefix2, namespace3);

        assertEquals("Unexpected lengthened form", uri1, URIUtils.getURI(pm, shortform1));
        assertEquals("Unexpected lengthened form", uri2, URIUtils.getURI(pm, shortform2));
    }

    @Test
    public void testGetUnknownStrict() {
        try {
            // no namespace for uri4 - default configuration should cause this to fail
            String shortform = URIUtils.getShortform(uri4);
            System.out.println("Shortened " + uri4 + " -> " + shortform);
            fail("Shortened " + uri4 + " in strict mode with missing namespace, this should cause an error");
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Expected IllegalArgumentException, actually got " + e.getClass().getSimpleName() + " " +
                         "(" + e.getMessage() + ")");
        }
    }

    @Test
    public void testGetUnknownRoundTripWithCaching() {
        // this test requires strict creation of a new, cached shortform to work
        URIUtils.PREFIX_CREATION_MODE = URIUtils.PrefixCreation.CREATE_AND_CACHE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.STRICT;

        String shortform;
        URI uri;

        // first pass
        shortform = URIUtils.getShortform(uri4, strictness);
        System.out.println("Shortened " + uri4 + " -> " + shortform);
        assertEquals("Unexpected shortened form", "anoth:term", shortform);
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + uri4 + " -> " + uri);
        assertEquals("Unexpected uri", uri4, uri);

        // second pass, should reuse cached prefix zooma1
        shortform = URIUtils.getShortform(uri4, strictness);
        System.out.println("Shortened " + uri4 + " -> " + shortform);
        assertEquals("Unexpected shortened form", "anoth:term", shortform);
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + shortform + " -> " + uri);
        assertEquals("Unexpected uri", uri4, uri);

        // test namespace ends in # instead of /
        shortform = URIUtils.getShortform(uri5, strictness);
        System.out.println("Shortened " + uri5 + " -> " + shortform);
        assertEquals("Unexpected shortened form", "yetan:term", shortform);
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + shortform + " -> " + uri);
        assertEquals("Unexpected uri", uri5, uri);
    }

    @Test
    public void testCompoundingStrict() {
        // this tests strict creation of a new shortform
        URIUtils.PREFIX_CREATION_MODE = URIUtils.PrefixCreation.CREATE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.STRICT;

        String result;
        result = URIUtils.getShortform(uri6, strictness);
        System.out.println("Shortened " + uri6 + " -> " + result);
        assertFalse("Short form results in an invalid localname", result.contains("/") || result.contains("#"));
    }

    @Test
    public void testCompoundingRelaxed() {
        // this tests strict creation of a new shortform
        URIUtils.PREFIX_CREATION_MODE = URIUtils.PrefixCreation.DO_NOT_CREATE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.ALLOW_HASHES;

        String result;
        result = URIUtils.getShortform(uri9, strictness);
        System.out.println("Shortened " + uri9 + " -> " + result);
        assertFalse("Short form results in an invalid localname", result.contains("/"));
    }

    @Test
    public void testNamespaceOnly() {
        String result, localName;
        result = URIUtils.getShortform(uri7);
        localName = result.replace(result.substring(0, result.lastIndexOf(":") + 1), "");
        System.out.println("Shortened " + uri7 + " -> " + result);
        assertFalse("Short form results in an invalid localname '" + localName + "'", localName.isEmpty());

        result = URIUtils.getShortform(uri7);
        localName = result.replace(result.substring(0, result.lastIndexOf(":") + 1), "");
        System.out.println("Shortened " + uri7 + " -> " + result);
        assertFalse("Short form results in an invalid localname '" + localName + "'", localName.isEmpty());
    }

    @Test
    public void testGetNamespace() {
        URI result;
        result = URIUtils.extractNamespace(uri1);
        System.out.println("Namespace for <" + uri1 + "> is " + result);
        assertEquals(URI.create(namespace2), result);
        assertTrue(URIUtils.isNamespaceKnown(result));

        result = URIUtils.extractNamespace(uri4);
        System.out.println("Namespace for <" + uri4 + "> is " + result);
        assertEquals(URI.create(namespace5), result);
        assertFalse(URIUtils.isNamespaceKnown(result));
    }

    @Test
    public void testExpandCollapseInferred() {
        String result;
        result = URIUtils.getShortform(inferredURI);
        System.out.println("Shortened " + inferredURI + " -> " + result);
        assertEquals("Unexpected shortened form", inferredPrefix + ":term", result);

        assertEquals("Unexpected lengthened form", inferredURI, URIUtils.getURI(inferredShortform));
    }
}
