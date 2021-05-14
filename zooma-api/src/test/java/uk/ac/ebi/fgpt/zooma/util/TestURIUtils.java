package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    public void setUp() {
        URL url = getClass().getClassLoader().getResource("config/naming/prefix.properties");
        String path = url != null ? url.toString().replace("file:", "").replace("config/naming/prefix.properties", "") : "";
        System.setProperty("zooma.home", new File(path).getAbsolutePath());

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

    @Test
    public void testGetPrefixMappings() {
        Map<String, String> prefixMappings = URIUtils.getPrefixMappings();
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config/naming/prefix.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't load prefix properties");
        }
        assertEquals(properties.size(), prefixMappings.size(), "Unexpected number of prefixes");
        assertTrue(prefixMappings.containsKey("slash"),"Could not find 'slash' prefix");
        assertTrue(prefixMappings.containsKey("hash"), "Could not find 'hash' prefix");
        System.out.println("Contents of prefix mappings: " + prefixMappings.toString());
    }

    @Test
    public void testGetShortform() {
        String result;
        result = URIUtils.getShortform(uri1);
        System.out.println("Shortened " + uri1 + " -> " + result);
        assertEquals(prefix1 + ":term", result, "Unexpected shortened form");
        result = URIUtils.getShortform(uri2);
        System.out.println("Shortened " + uri2 + " -> " + result);
        assertEquals(prefix2 + ":term", result, "Unexpected shortened form");
    }

    @Test
    public void testGetShortformCustomMappings() {
        Map<String, String> pm = new HashMap<>();
        pm.put(prefix1, namespace2);
        pm.put(prefix2, namespace3);
        pm.put(prefix3, namespace4);

        assertEquals(prefix1 + ":term", URIUtils.getShortform(pm, uri1), "Unexpected shortened form");
        assertEquals(prefix2 + ":term", URIUtils.getShortform(pm, uri2), "Unexpected shortened form");
        assertEquals(prefix3 + ":term", URIUtils.getShortform(pm, uri3), "Unexpected shortened form");
    }


    @Test
    public void testGetURI() {
        Map<String, String> pm = new HashMap<>();
        pm.put(prefix1, namespace2);
        pm.put(prefix2, namespace3);

        assertEquals(uri1, URIUtils.getURI(pm, shortform1), "Unexpected lengthened form");
        assertEquals(uri2, URIUtils.getURI(pm, shortform2), "Unexpected lengthened form");
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
        URIUtils.PrefixCreationMode prefixCreationMode = URIUtils.PrefixCreationMode.CREATE_AND_CACHE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.STRICT;

        String shortform;
        URI uri;

        // first pass
        shortform = URIUtils.getShortform(uri4, strictness, prefixCreationMode);
        System.out.println("Shortened " + uri4 + " -> " + shortform);
        assertEquals("anoth:term", shortform, "Unexpected shortened form");
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + uri4 + " -> " + uri);
        assertEquals(uri4, uri, "Unexpected uri");

        // second pass, should reuse cached prefix zooma1
        shortform = URIUtils.getShortform(uri4, strictness, prefixCreationMode);
        System.out.println("Shortened " + uri4 + " -> " + shortform);
        assertEquals("anoth:term", shortform, "Unexpected shortened form");
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + shortform + " -> " + uri);
        assertEquals(uri4, uri, "Unexpected uri");

        // test namespace ends in # instead of /
        shortform = URIUtils.getShortform(uri5, strictness, prefixCreationMode);
        System.out.println("Shortened " + uri5 + " -> " + shortform);
        assertEquals("yetan:term", shortform, "Unexpected shortened form");
        uri = URIUtils.getURI(shortform);
        System.out.println("Lengthened " + shortform + " -> " + uri);
        assertEquals(uri5, uri,"Unexpected uri");
    }

    @Test
    public void testCompoundingStrict() {
        // this tests strict creation of a new shortform
        URIUtils.PrefixCreationMode prefixCreationMode = URIUtils.PrefixCreationMode.CREATE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.STRICT;

        String result;
        result = URIUtils.getShortform(uri6, strictness, prefixCreationMode);
        System.out.println("Shortened " + uri6 + " -> " + result);
        assertFalse(result.contains("/") || result.contains("#"), "Short form results in an invalid localname");
    }

    @Test
    public void testCompoundingRelaxed() {
        // this tests strict creation of a new shortform
        URIUtils.PrefixCreationMode prefixCreationMode = URIUtils.PrefixCreationMode.DO_NOT_CREATE;
        URIUtils.ShortformStrictness strictness = URIUtils.ShortformStrictness.ALLOW_HASHES;

        String result;
        result = URIUtils.getShortform(uri9, strictness, prefixCreationMode);
        System.out.println("Shortened " + uri9 + " -> " + result);
        assertFalse(result.contains("/"), "Short form results in an invalid localname");
    }

    @Test
    public void testNamespaceOnly() {
        String result, localName;
        result = URIUtils.getShortform(uri7);
        localName = result.replace(result.substring(0, result.lastIndexOf(":") + 1), "");
        System.out.println("Shortened " + uri7 + " -> " + result);
        assertFalse(localName.isEmpty(), "Short form results in an invalid localname '" + localName + "'");

        result = URIUtils.getShortform(uri7);
        localName = result.replace(result.substring(0, result.lastIndexOf(":") + 1), "");
        System.out.println("Shortened " + uri7 + " -> " + result);
        assertFalse(localName.isEmpty(), "Short form results in an invalid localname '" + localName + "'");
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
        assertEquals(inferredPrefix + ":term", result, "Unexpected shortened form");

        assertEquals(inferredURI, URIUtils.getURI(inferredShortform), "Unexpected lengthened form");
    }
}
