package uk.ac.ebi.fgpt.zooma.util;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/07/14
 */
public class TestParenthesesProcessor {
    private String noBrackets = "something";
    private String brackets = "something (something else)";
    private String multiBrackets = "something (something else) (and something else)";
    private String backwardsBrackets = "something) and (something else";
    private String combi = "something (something else) and some more content) and more ( with (more)";

    private String hyperploidyExample = "hyperdiploidy,-X,-1q,+2,-3,-4q,+5,+5(2),+5(3),+12,-14q,+16,-19";

    private ParenthesesProcessor processor;

    @Before
    public void setUp() {
        processor = new ParenthesesProcessor();
    }

    @Test
    public void testNoBrackets() {
        assertFalse(processor.canProcess(noBrackets));
    }

    @Test
    public void testBrackets() {
        try {
            assertTrue(processor.canProcess(brackets));
            List<String> results = processor.processSearchString(brackets);
            assertEquals(1, results.size());
            assertEquals(noBrackets, results.get(0));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testProcessMultiBrackets() {
        try {
            assertTrue(processor.canProcess(multiBrackets));
            List<String> results = processor.processSearchString(multiBrackets);
            assertEquals(1, results.size());
            assertEquals(noBrackets, results.get(0));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testBackwardsBrackets() {
        try {
            assertTrue(processor.canProcess(backwardsBrackets));
            List<String> results = processor.processSearchString(backwardsBrackets);
            assertEquals(0, results.size());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCombi() {
        try {
            assertTrue(processor.canProcess(combi));
            List<String> results = processor.processSearchString(combi);
            assertEquals(1, results.size());
            assertEquals("something  and some more content) and more ( with", results.get(0));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testHyperploidyExample() {
        try {
            assertFalse(processor.canProcess(hyperploidyExample)); // should look like a chemical compound
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
