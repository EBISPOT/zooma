package uk.ac.ebi.fgpt.zooma.model;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 16/07/14
 */
public class TestSimpleTypedProperty {
    private URI testUri = URI.create("http://www.test.org/test");

    private SimpleTypedProperty property;
    private SimpleTypedProperty thatProperty;
    private SimpleTypedProperty duplicateProperty;

    @Test
    public void testNullURI() {
        property = new SimpleTypedProperty("type", "value");
        thatProperty = new SimpleTypedProperty(testUri, "type", "value");

        try {
            boolean equals = property.equals(thatProperty);
            assertFalse(equals, "Objects should not be equal");

            int hashcode = property.hashCode();
            int thatHashcode = thatProperty.hashCode();

            assertFalse(hashcode == thatHashcode, "hashcodes should not be equal");

            duplicateProperty = new SimpleTypedProperty("type", "value");
            Set<Property> propertys = new HashSet<>();
            propertys.add(property);
            propertys.add(thatProperty);
            propertys.add(duplicateProperty);

            assertTrue(propertys.size() == 3);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCompare() {
        property = new SimpleTypedProperty("type", "value");
        thatProperty = new SimpleTypedProperty(testUri, "type", "value");

        assertTrue(property.compareTo(thatProperty) == 0);
        assertTrue(property.matches(thatProperty));
    }
}
