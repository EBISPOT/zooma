package uk.ac.ebi.fgpt.zooma.model;

import org.junit.jupiter.api.Test;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests equals and hashcode for AbstractIdentifiable
 *
 * @author Tony Burdett
 * @date 16/07/14
 */
public class TestAbstractIdentifiable {
    private URI testUri = URI.create("http://www.test.org/test");

    private MyIdentifiableTester identifiable;
    private MyIdentifiableTester thatIdentifiable;
    private MyIdentifiableTester duplicateIdentifiable;

    @Test
    public void testNullURI() {
        identifiable = new MyIdentifiableTester(null);
        thatIdentifiable = new MyIdentifiableTester(testUri);

        try {
            boolean equals = identifiable.equals(thatIdentifiable);
            assertFalse(equals, "Objects should not be equal");
            equals = thatIdentifiable.equals(identifiable);
            assertFalse(equals, "Objects should not be equal");

            int hashcode = identifiable.hashCode();
            int thatHashcode = thatIdentifiable.hashCode();

            assertFalse(hashcode == thatHashcode, "hashcodes should not be equal");

            duplicateIdentifiable = new MyIdentifiableTester(null);
            Set<Identifiable> identifiables = new HashSet<>();
            identifiables.add(identifiable);
            identifiables.add(thatIdentifiable);
            identifiables.add(duplicateIdentifiable);

            assertTrue(identifiables.size() == 3);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private class MyIdentifiableTester extends AbstractIdentifiable {
        public MyIdentifiableTester(URI uri) {
            super(uri);
        }
    }
}
