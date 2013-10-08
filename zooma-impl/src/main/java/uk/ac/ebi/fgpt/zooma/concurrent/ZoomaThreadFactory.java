package uk.ac.ebi.fgpt.zooma.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple implementation of a {@link ThreadFactory} that prepends a standard string to every thread created
 *
 * @author Tony Burdett
 * @date 29/10/12
 */
public class ZoomaThreadFactory implements ThreadFactory {
    private final ThreadGroup threadGroup;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String baseName;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaThreadFactory(String baseName) {
        SecurityManager s = System.getSecurityManager();
        ThreadGroup parentGroup = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.threadGroup = new ThreadGroup(parentGroup, baseName + "-Pool");
        this.baseName = baseName;
    }

    public Thread newThread(Runnable r) {
        getLog().debug("newThread() called from " + Thread.currentThread().getName());
        Thread t = new Thread(threadGroup, r, baseName + "-Thread-" + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        getLog().debug("Created new thread '" + t.getName() + "'");
        return t;
    }
}
