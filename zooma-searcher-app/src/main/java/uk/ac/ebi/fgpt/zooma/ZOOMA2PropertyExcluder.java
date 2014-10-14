package uk.ac.ebi.fgpt.zooma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class that can randomly sample from a set of properties to return a collection of the required size
 *
 * @author Tony Burdett
 * @date 28/03/13
 */
public class ZOOMA2PropertyExcluder {
    private final String excludedTypesResourceName = "zooma-exclusions.properties";
    private final Set<String> excludedTypes;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMA2PropertyExcluder() {
        excludedTypes = new HashSet<>();

        // read excluded types from file, if it exists
        try {
            URL excludedTypesResource = getClass().getClassLoader().getResource(excludedTypesResourceName);
            if (excludedTypesResource != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(excludedTypesResource.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#") && !line.isEmpty()) {
                        String s = line.toLowerCase();
                        excludedTypes.add(s);
                    }
                }
            }
            else {
                getLog().warn("Failed to load properties: could not locate file " +
                                       "'" + excludedTypesResourceName + "'.  No properties will be excluded");
            }
        }
        catch (IOException e) {
            getLog().error("Failed to load properties: could not read file '" + excludedTypesResourceName + "'.  " +
                                   "No properties will be excluded");
        }
    }

    public <T extends Collection<Property>> T sampleProperties(T properties, int sampleSize) {
        try {
            Property[] propertyArray = properties.toArray(new Property[properties.size()]);
            T result = (T) properties.getClass().newInstance();
            for (int i = 0; i < sampleSize; i++) {
                // generate a random number, multiply by collection size and sample
                int index = (int) Math.round(Math.random() * properties.size());
                result.add(propertyArray[index]);
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to sample properties - " + e.getMessage());
        }
    }

    public void removeExcludedTypes(Collection<Property> properties) {
        Iterator<Property> propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            if (property instanceof TypedProperty) {
                String type = ((TypedProperty) property).getPropertyType();
                String normalizedType = ZoomaUtils.normalizePropertyTypeString(type);

                // excluded type?
                for (String excludedType : excludedTypes) {
                    if (normalizedType.equals(ZoomaUtils.normalizePropertyTypeString(excludedType))) {
                        propertyIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private void excludeIneligibleProperties(List<Property> properties) {
        excludeIneligibleProperties(properties, new HashMap<Property, List<String>>());
    }

    private void excludeIneligibleProperties(List<Property> properties, Map<Property, List<String>> propertyContexts) {
        // firstly, remove any excluded types
        ZOOMA2PropertyExcluder sampler = new ZOOMA2PropertyExcluder();
        sampler.removeExcludedTypes(properties);

        // now, check for strings of a bad length and numeric values
        NumberFormat nf = NumberFormat.getInstance();
        Iterator<Property> propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            String propertyValue = property.getPropertyValue();

            // if property value is 3 characters or less, or 500 characters or more in length, exclude
            if (propertyValue.length() <= 3 || propertyValue.length() >= 500) {
                getLog().debug("Property value '" + propertyValue + "' has a length " +
                                       "(" + propertyValue.length() + " characters) " +
                                       "that makes it ineligible for ZOOMA search");
                propertyIterator.remove();
                if (propertyContexts.containsKey(property)) {
                    propertyContexts.remove(property);
                }
            }
            else {
                // if property value is numeric, exclude
                try {
                    nf.parse(property.getPropertyValue());
                    getLog().debug("Property value '" + propertyValue + "' is a numeric value. " +
                                           "This makes it ineligible for ZOOMA search");
                    propertyIterator.remove();
                    if (propertyContexts.containsKey(property)) {
                        propertyContexts.remove(property);
                    }
                }
                catch (java.text.ParseException e) {
                    // if parse exception is thrown, we can continue without removing
                }
            }
        }
    }
}
