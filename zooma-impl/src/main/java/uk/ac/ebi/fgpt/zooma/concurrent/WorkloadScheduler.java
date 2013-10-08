package uk.ac.ebi.fgpt.zooma.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A class that provides a mechanism for dispatching a series of jobs constituting a single workload in a parallelized
 * manner.  This class will schedule tasks
 *
 * @author Tony Burdett
 * @date 29/10/12
 */
public abstract class WorkloadScheduler {
    private static int schedulerThreadCount = 1;
    private static int monitorThreadCount = 1;

    private final String workloadName;

    private final ExecutorService executorService;
    private final int iterations;

    private final WorkloadCounter counter;

    private Boolean isComplete = false;
    private Boolean isAborted = false;
    private RuntimeException abortiveException = null;

    private Logger log = LoggerFactory.getLogger(WorkloadScheduler.class);

    public WorkloadScheduler(ExecutorService executorService, int iterations, String workloadName) {
        this.executorService = executorService;
        this.iterations = iterations;

        this.workloadName = workloadName;

        this.counter = new WorkloadCounter(iterations);
    }

    protected Logger getLog() {
        return log;
    }

    public void start() {
        scheduleTasks();
        monitorTasks();
    }

    public synchronized void waitUntilComplete() throws InterruptedException {
        while (!isComplete) {
            wait();
        }

        // if an exception was thrown, throw it here
        if (isAborted && abortiveException != null) {
            throw abortiveException;
        }
    }

    private void scheduleTasks() {
        new Thread(new Runnable() {
            @Override public void run() {
                int iteration = 1;
                while (!counter.isAborted() && iteration <= iterations) {
                    int tries = 1;
                    getLog().debug("Scheduling task for " + workloadName + ", round " + iteration + "/" + iterations);
                    while (tries < 6) {
                        try {
                            if (tries != 1) {
                                getLog().debug("Reattempting to schedule a task, try " + tries);
                            }
                            scheduleTask(iteration);
                            iteration++;
                            break;
                        }
                        catch (RejectedExecutionException e) {
                            // rejected execution, try to determine reason and maybe resubmit?
                            synchronized (counter) {
                                if (executorService instanceof ThreadPoolExecutor) {
                                    BlockingQueue<Runnable> queue =
                                            ((ThreadPoolExecutor) executorService).getQueue();
                                    getLog().debug("Current queue size (" + workloadName + "): " + queue.size());
                                    if (queue.remainingCapacity() == 0) {
                                        // reached maximum queue capacity
                                        getLog().trace("Reached maximum queue capacity (" + queue.size() + "), " +
                                                               "waiting for space to submit next task...");
                                        try {
                                            // wake up periodically, in case our executor is used outside of this scheduler
                                            counter.wait(60000);
                                        }
                                        catch (InterruptedException e1) {
                                            getLog().debug("Interrupted whilst waiting on executor queue");
                                        }
                                    }
                                    else {
                                        // rejected for a reason other than queue size
                                        getLog().debug("Failed to submit a task despite remaining queue capacity " +
                                                               "(tried " + tries + " times)", e);
                                        tries++;
                                    }
                                }
                                else {
                                    // rejected for a reason other than queue size, so throw this exception
                                    getLog().debug("Failed to submit a task despite remaining queue capacity " +
                                                           "(tried " + tries + " times)", e);
                                    tries++;
                                }

                                if (tries > 5) {
                                    // no more tries remaining
                                    getLog().error("Task failed: " + workloadName + ", " +
                                                           "round " + iteration + "/" + iterations + " " +
                                                           "failed to submit " + tries + " times without success.");
                                    counter.abort(e);
                                }
                            }
                        }
                    }
                }
                getLog().debug("Scheduling of tasks for " + workloadName + " is complete");
            }
        }, workloadName + "-Scheduler-" + schedulerThreadCount++).start();
    }

    private void monitorTasks() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    synchronized (counter) {
                        while (counter.getRemainingCount() > 0) {
                            try {
                                getLog().debug(
                                        "Waiting for tasks for " + workloadName + " to complete - " +
                                                "done " + counter.getTally() + ", " +
                                                "remaining " + counter.getRemainingCount());

                                counter.wait();
                            }
                            catch (InterruptedException e) {
                                getLog().debug(
                                        "Interrupted whilst waiting for " + workloadName + " to finish, continuing");
                            }
                        }
                    }
                }
                catch (RuntimeException e) {
                    // capture exception so scheduler can return it when waitUntilComplete() is called
                    isAborted = true;
                    abortiveException = e;
                    getLog().error("Workload monitoring detected a runtime exception", e);
                }
                finally {
                    // update isComplete flag
                    isComplete = true;
                    synchronized (WorkloadScheduler.this) {
                        WorkloadScheduler.this.notifyAll();
                    }
                    getLog().debug("Monitoring of tasks for " + workloadName + " is complete");
                }
            }
        }, workloadName + "-Monitor-" + monitorThreadCount++).start();
    }

    private void scheduleTask(final int iteration) {
        executorService.execute(new Runnable() {
            @Override public void run() {
                try {
                    getLog().debug("Executing task for " + workloadName + ", round " + iteration + "/" + iterations);
                    executeTask(iteration);
                    counter.increment();
                }
                catch (Exception e) {
                    getLog().error("Task " + iteration + "/" + iterations + " failed for " + workloadName, e);
                    counter.abort(e);
                }
            }
        });
    }

    protected abstract void executeTask(int iteration) throws Exception;

    private class WorkloadCounter {
        private int tally;
        private final int target;
        private boolean doAbort;
        private Throwable abortiveException;

        public WorkloadCounter(int target) {
            this.tally = 0;
            this.target = target;
            this.doAbort = false;
        }

        public synchronized void increment() {
            tally++;
            notifyAll();
        }

        public synchronized void abort(Throwable t) {
            this.doAbort = true;
            this.abortiveException = t;
            notifyAll();
        }

        public synchronized boolean isAborted() {
            return doAbort;
        }

        public synchronized int getTally() throws RuntimeException {
            if (doAbort) {
                throw new RuntimeException("Exception in one of the scheduled tasks caused this scheduler to abort",
                                           abortiveException);
            }
            return tally;
        }

        public synchronized int getRemainingCount() {
            if (doAbort) {
                throw new RuntimeException("Exception in one of the scheduled tasks caused this scheduler to abort",
                                           abortiveException);
            }
            return target - tally;
        }
    }
}
