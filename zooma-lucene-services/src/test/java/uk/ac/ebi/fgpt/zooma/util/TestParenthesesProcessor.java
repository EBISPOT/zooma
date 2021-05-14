package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    //Before bug fixing this use to loop infinitely, just making sure that the bug doesn't come back.
    private String infiniteLoop = "BATF (from Ken Murphy [rabbit], not commercial available)";
    private String processedInfiniteLoop = "BATF";

    private String hyperploidyExample = "hyperdiploidy,-X,-1q,+2,-3,-4q,+5,+5(2),+5(3),+12,-14q,+16,-19";

//    private  boolean throughInterruptedException = false;
    private ParenthesesProcessor processor;

    @BeforeEach
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
            assertEquals(results.get(0), "something  and some more content) and more ( with");
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
    @Test
    public void testInfiniteLoop(){
        try{
            boolean canProcess = processor.canProcess(infiniteLoop);
            System.out.println("canProcess = "  + canProcess);
            assertTrue(canProcess);
            List<String> results = processor.processSearchString(infiniteLoop);
            assertEquals(1, results.size());
            assertEquals(processedInfiniteLoop,  results.get(0));

        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }
//    @Test
//    public void testInterruptedException() throws InterruptedException {
//        Thread thread = (new Thread(new ProcessorRunnable()));
//        thread.start();
//        thread.interrupt();
//
//    }
//
//
//    public class ProcessorRunnable implements Runnable {
//        @Override
//        public void run() {
//            boolean isTrue = true;
//            while(isTrue) {
//                try {
//                    processor.processSearchString(infiniteLoop);
//                } catch (InterruptedException e) {
//                    throughInterruptedException = true;
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        }
//    }
}
