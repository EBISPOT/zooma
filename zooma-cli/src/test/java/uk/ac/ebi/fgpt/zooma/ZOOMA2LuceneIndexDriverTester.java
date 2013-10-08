package uk.ac.ebi.fgpt.zooma;

import java.io.IOException;

/**
 * Test application for building lucene indexes
 *
 * @author Tony Burdett
 * @date 28/06/13
 */
public class ZOOMA2LuceneIndexDriverTester {
    public static void main(String[] args) {
        System.setProperty("zooma.home", "/tmp/zooma/index/");

        ZOOMA2LuceneIndexDriver driver = new ZOOMA2LuceneIndexDriver();
        try {
            driver.generateIndices();

            // test for completion
            String preamble = "Building ZOOMA indices...";
            System.out.print(preamble);
            int chars = preamble.length();
            final Object lock = new Object();
            while (!driver.isComplete()) {
                synchronized (lock) {
                    chars++;
                    if (chars % 40 == 0) {
                        System.out.println(".");
                    }
                    else {
                        System.out.print(".");
                    }
                    try {
                        lock.wait(15000);
                    }
                    catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
            System.out.println("ok!");
            System.out.println("ZOOMA indices completed successfully.");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
