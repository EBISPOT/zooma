package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 28/07/14
 */
public class TestTimeProcessor {
    private String noTime = "something";
    private String day = "1 day";
    private String hours = "12 hours";
    private String week = "1 week";
    private String decimal = "2.5 days";

    private String hyperploidyExample = "hyperdiploidy,-X,-1q,+2,-3,-4q,+5,+5(2),+5(3),+12,-14q,+16,-19";

    private TimeProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new TimeProcessor();
    }

    @AfterEach
    public void tearDown() {
        processor = null;
    }

    @Test
    public void testCanProcessTimes() {
        assertFalse(processor.canProcess(noTime));
        assertTrue(processor.canProcess(day));
        assertTrue(processor.canProcess(hours));
        assertTrue(processor.canProcess(week));
        assertTrue(processor.canProcess(decimal));
//        assertFalse(processor.canProcess(hyperploidyExample));
    }

    @Test
    public void testProcessTimes() {
        try {
            List<String> results;
            results = processor.processSearchString(day);
            assertEquals(1, results.size());
            assertEquals("day", results.get(0));

            results = processor.processSearchString(hours);
            assertEquals(1, results.size());
            assertEquals("hours", results.get(0));

            results = processor.processSearchString(week);
            assertEquals(1, results.size());
            assertEquals("week", results.get(0));

            results = processor.processSearchString(decimal);
            assertEquals(1, results.size());
            assertEquals("days", results.get(0));

            results = processor.processSearchString(hyperploidyExample);
            assertEquals(1, results.size());
            // hyperdiploidy example could be the unit for the numeric range that follows it; expect search string with numbers ripped out
            assertEquals("hyperdiploidy,-X,-q,+,-,-q,+,+(),+(),+,-q,+,-", results.get(0));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
