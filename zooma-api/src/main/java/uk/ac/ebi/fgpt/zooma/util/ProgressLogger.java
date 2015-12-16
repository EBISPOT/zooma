package uk.ac.ebi.fgpt.zooma.util;

import java.io.PrintStream;

/**
 * A simple abstract class for logging progress to a supplied {@link java.io.PrintStream}.  This class will simply print
 * a single full stop character every so often until progress is reported as complete (making use of the {@link #test()}
 * method to determine when progress is complete).  You should override this method in implementations.
 *
 * @author Tony Burdett
 * @date 16/12/15
 */
public abstract class ProgressLogger {
    private final Object lock;

    private final PrintStream out;
    private final String preamble;
    private final int interval;

    /**
     * Create a new progress logger
     *
     * @param out      the PrintStream to log output to
     * @param preamble an initial string that will be written to the PrintStream before progress starts being logged
     *                 (e.g. "Starting...")
     * @param interval the time (in seconds) to print a dot whilst progress continues
     */
    public ProgressLogger(PrintStream out, String preamble, int interval) {
        this.lock = new Object();

        this.out = out;
        this.preamble = preamble;
        this.interval = interval * 1000;
    }

    /**
     * Starts this progress logger.  This causes a new thread to be created which logs progress "dots" to the print
     * stream until {@link #test()} fails.
     */
    public void start() {
        // create a thread to print to standard out while invoker is running
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                out.print(preamble);
                int chars = preamble.length();

                while (test()) {
                    synchronized (lock) {
                        chars++;
                        if (chars % 40 == 0) {
                            out.println(".");
                        }
                        else {
                            out.print(".");
                        }
                        try {
                            lock.wait(interval);
                        }
                        catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                }
                System.out.println("ok!");
            }
        });
    }

    /**
     * Pings this progress logger, which has the effect of waking any threads from a wait and causing them to
     * immediately log a new event (or exit is progress is complete) instead of waiting for the rest of the interval.
     */
    public void ping() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * Implement this method to control when progress should be logged.  If this method returns true, progress will be
     * logged.  If this method returns false, the progress logger will report "ok" and exit.
     *
     * @return true whilst progress is continuing, false to stop logging
     */
    public abstract boolean test();
}
