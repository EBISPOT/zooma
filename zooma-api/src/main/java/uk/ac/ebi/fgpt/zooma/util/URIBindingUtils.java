package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Simon Jupp
 * @date 30/10/2013 Functional Genomics Group EMBL-EBI
 */
public class URIBindingUtils {

    private static final Map<String, String> prefixMappings;
    private static final String uninitializedKey = "UNINITIALIZED";
    private static Logger log = LoggerFactory.getLogger(URIBindingUtils.class);

    protected static Logger getLog() {
        return log;
    }

    static {
        // initialize final map prefixMappings...
        prefixMappings = Collections.synchronizedMap(new HashMap<String, String>());
        // ...and add a key to indicate it has not yet been loaded
        prefixMappings.put(uninitializedKey, "true");
    }

    /**
     * Loads types to namespace mappings into a map, and returns them.  Mappings are loaded from any zooma/types
     * .properties files present on the classpath when this code is executed; this means prefix properties can be
     * updated inside a running application.
     *
     * @return a mapping of prefix to namespace values
     */
    public synchronized static Map<String, String> getPrefixMappings() {
        // if prefixMappings contains the key that indicates it has never been loaded, then load
        if (prefixMappings.containsKey(uninitializedKey)) {
            loadPrefixMappings();
        }
        return prefixMappings;
    }

    public synchronized static Map<String, String> loadPrefixMappings() {
        getLog().debug("Attempting to load type mappings from properties files...");
        prefixMappings.clear();
        try {
            Properties prefixProperties = new Properties();
            Enumeration<URL> prefixPropertyFiles = URIBindingUtils.class.getClassLoader().getResources(
                    "zooma/types.properties");
            while (prefixPropertyFiles.hasMoreElements()) {
                URL prefixPropertyFile = prefixPropertyFiles.nextElement();
                getLog().debug("Loading type mappings from " + prefixPropertyFile.toString());
                prefixProperties.load(prefixPropertyFile.openStream());
            }

            for (String prefix : prefixProperties.stringPropertyNames()) {
                String namespace = prefixProperties.getProperty(prefix);
                getLog().debug("Next type mapping: " + prefix + " = " + namespace);
                prefixMappings.put(prefix, namespace);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to read zooma type properties", e);
        }
        return prefixMappings;
    }

    /**
     * Gets the full URI of the given shortened form, first looking for any declared prefix mappings on the classpath.
     * This is equivalent to calling {@link #getPrefixMappings()} and passing the results to {@link
     * #getURI(java.util.Map, java.lang.String)} as the first parameter.
     *
     * @param shortform the shortform to find the URI for
     * @return the full URI
     */
    public static URI getURI(String shortform) {
        return getURI(getPrefixMappings(), shortform);
    }

    public static URI getURI(final Map<String, String> prefixMappings, String shortform) {

        synchronized (prefixMappings) {
            if (prefixMappings.containsKey(shortform)) {
                String namespace = prefixMappings.get(shortform);
                return URI.create(namespace);
            }
            else {
                // if we get to here, we cannot resolve prefix
                throw new IllegalArgumentException("Unknown type '" + shortform + "' - it is not " +
                                                           "possible to reconstruct this URI");
            }
        }

    }

    /**
     * Check the supplied array of URIs, and if any of them do not have a corresponding name binding in the known prefix
     * mappings file then return false.  Otherwise, if all URIs are recognised, return true.  You cna use this method to
     * validate that all URIs in a SPARQL query are recognised bindings before attempting to map the result to avoid
     * exceptions.
     * <p/>
     * This is equivalent to calling {@link #getPrefixMappings()} and passing the results to {@link
     * #validateNamesExist(java.util.Map, java.net.URI...)} as the first parameter.
     *
     * @param uris the URIs to validate
     * @return true if all URIs supplied have name bindings, false otherwise
     */
    public static boolean validateNamesExist(URI... uris) {
        return validateNamesExist(getPrefixMappings(), uris);
    }

    public static boolean validateNamesExist(final Map<String, String> prefixMappings, URI... uris) {
        for (URI uri : uris) {
            if (!prefixMappings.containsValue(uri.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the Zooma name for a given URI declared in a type.properties file. This is equivalent to calling {@link
     * #getPrefixMappings()} and passing the results to {@link #getName(java.util.Map, java.net.URI)} as the first
     * parameter.
     *
     * @param sourceAsUri the shortform to find the URI for
     * @return the full URI
     */
    public static String getName(URI sourceAsUri) {
        return getName(getPrefixMappings(), sourceAsUri);
    }

    public static String getName(final Map<String, String> prefixMappings, URI sourceAsUri) {
        synchronized (prefixMappings) {
            if (prefixMappings.containsValue(sourceAsUri.toString())) {
                List<String> set = new ArrayList<String>();
                for (String s : prefixMappings.keySet()) {
                    if (prefixMappings.get(s).equals(sourceAsUri.toString())) {
                        set.add(s);
                    }
                }
                if (set.size() == 1) {
                    return set.get(0);
                }
            }
            // if we get to here, we cannot resolve prefix
            throw new IllegalArgumentException("Unable to get key for '" + sourceAsUri + "' - it is not " +
                                                       "possible to reconstruct the key name from this URI");
        }
    }
}
