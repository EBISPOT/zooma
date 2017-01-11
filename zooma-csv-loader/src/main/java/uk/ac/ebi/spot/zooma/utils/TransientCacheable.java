package uk.ac.ebi.spot.zooma.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * An abstract class that enables transient caching and periodic cleanup on those caches on implementing classes.
 * Implementations are free to define their own object caches and store objects in them, but when using this class a
 * daemon thread will be created that periodically (depending on the expiry time parameter) calls {@link #clearCaches()}
 * to empty any cached objects.
 *
 * @author Tony Burdett
 * @date 27/05/14
 */
@Component
public abstract class TransientCacheable {
    private Thread t;
    private boolean cachesCreated = false;

    private final int timeout;
    private final int monitoringInterval;
    private long lastRequestTime = -1;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    /**
     * Creates a new instance of a TransientCacheable object.  These objects, once activated, start a background thread
     * that will automatically empty the cache <code>timeout</code> seconds after the last request was made to this
     * object using the {@link #ping()} method.  This constructor will set a default timeout and monitoring interval of
     * 60 seconds each.
     */
    protected TransientCacheable() {
        this(60, 60);
    }

    /**
     * Creates a new instance of a TransientCacheable object.  These objects, once activated, start a background thread
     * that will automatically empty the cache <code>timeout</code> seconds after the last request was made to this
     * object using the {@link #ping()} method.  This constructor will set a default monitoring interval of 60 seconds
     *
     * @param timeout the time, in seconds, after the last ping before caches can be emptied
     */
    protected TransientCacheable(int timeout) {
        this(timeout, 60);
    }

    /**
     * Creates a new instance of a TransientCacheable object.  These objects, once activated, start a background thread
     * that will automatically empty the cache <code>timeout</code> seconds after the last request was made to this
     * object using the {@link #ping()} method.  The background thread will check the status of the last ping every
     * <code>monitoringInterval</code> seconds
     *
     * @param timeout            the time, in seconds, after the last ping before caches can be emptied
     * @param monitoringInterval the frequency with which the daemon thread started by this class should monitor the
     *                           last ping
     */
    protected TransientCacheable(int timeout, int monitoringInterval) {
        this.timeout = timeout;
        this.monitoringInterval = monitoringInterval;
    }

    private Class<?> getConcreteClass() {
        return getClass();
    }

    private synchronized void cacheMonitoring() {
        if (t == null || !t.isAlive()) {
            t = new Thread(new Runnable() {
                @Override public void run() {
                    getLog().debug("Starting cache monitoring daemon thread " +
                                           "'" + Thread.currentThread().getName() + "'");
                    boolean cleanup = false;
                    while (!cleanup) {
                        // a request has been made
                        if (lastRequestTime > -1) {
                            // if the last request was more than 1 minute ago, clear the cache
                            long time = System.currentTimeMillis() - lastRequestTime;
                            String estimate = new DecimalFormat("#,###").format(((float) time) / 1000);
                            getLog().debug("Polling for cache cleanup - last request was " + estimate + "s ago.");
                            if (System.currentTimeMillis() - lastRequestTime > (timeout * 1000)) {
                                // if so, clear caches and allow to exit
                                cleanup = clearCaches();
                            }
                        }

                        if (!cleanup) {
                            // build in a delay
                            synchronized (this) {
                                try {
                                    wait(monitoringInterval * 1000);
                                }
                                catch (InterruptedException e) {
                                    // just continue
                                }
                            }
                        }
                    }
                }
            }, getConcreteClass().getSimpleName() + "-Cache-Daemon");
            t.setDaemon(true);
            t.start();
        }
        this.lastRequestTime = System.currentTimeMillis();
    }

    protected void ping() {
        if (!cachesCreated) {
            cachesCreated = createCaches();
        }
        cacheMonitoring();
    }

    /**
     * Creates any structures required for caching objects.
     *
     * @return true if cache creation succeeded, or if all required objects have already been created, and false if
     * caches could to be created
     */
    protected abstract boolean createCaches();

    /**
     * Clears any cached references currently held.  This will be automatically invoked after the expiry time has
     * passed. Implementations should return true if the cache was correctly emptied, and false otherwise
     *
     * @return true if the caches are successfully cleared, false otherwise
     */
    protected abstract boolean clearCaches();
}
