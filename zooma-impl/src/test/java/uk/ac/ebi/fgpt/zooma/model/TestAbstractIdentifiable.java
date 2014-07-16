package uk.ac.ebi.fgpt.zooma.model;

import org.junit.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
            assertFalse("Objects should not be equal", equals);
            equals = thatIdentifiable.equals(identifiable);
            assertFalse("Objects should not be equal", equals);

            int hashcode = identifiable.hashCode();
            int thatHashcode = thatIdentifiable.hashCode();

            assertFalse("hashcodes should not be equal", hashcode == thatHashcode);

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
