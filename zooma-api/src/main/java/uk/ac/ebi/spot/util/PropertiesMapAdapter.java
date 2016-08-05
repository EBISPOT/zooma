package uk.ac.ebi.spot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A simple bean class that can be used to convert a java properties file into a string to string map
 *
 * @author Tony Burdett
 * @date 17/09/12
 */
public class PropertiesMapAdapter {
    public Properties properties;
    public Map<String, String> prefixMappings;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void init() {
        Map<String, String> prefixMappings = new HashMap<>();
        getLog().debug("Initializing prefixMap using properties: " + properties);
        for (String prefix : getProperties().stringPropertyNames()) {
            String namespace = getProperties().getProperty(prefix);
            prefixMappings.put(prefix, namespace);
            getLog().debug("Next prefix mapping: " + prefix + " = " + namespace);
        }
        this.prefixMappings = Collections.unmodifiableMap(prefixMappings);
    }

    public Map<String, String> getPropertyMap() {
        return prefixMappings;
    }
}
