package service;


/**
 * A ZOOMA service that indicates the status of backend service.  This is used to indicate whether ZOOMA is ready to
 * serve requests, or force a reinitialization of backend infrastructure to ZOOMA.
 * <p/>
 * Any service implementations that require initialization, startup or indexing should implement this service to only
 * return true if all startup is successful.
 * <p/>
 * If you have implemented service classes in this package in such a way that no initialization is required, it is
 * reasonable to build a default simple implementation that always returns true when the status is checked.
 *
 * @author Tony Burdett
 * @date 20/02/13
 */
public interface StatusService {
    /**
     * Check the status of ZOOMA to determine if the ZOOMA architecture is ready to serve requests.  Returns true if and
     * only if any request made of a ZOOMA service can be served.
     *
     * @return true if ZOOMA is ready to serve all requests, false otherwise
     */
    boolean checkStatus();

    /**
     * Forces any startup procedure to be rerun, and ZOOMA status to be reset.  If initialization requires the
     * acquisition of a database connection, or a cache or and index to be built, this should force any old reasources
     * to be released or deleted and reacquired.
     * <p/>
     * This method may be asynchronous, so it is not required that this method blocks until reinitialization is
     * complete, but if a reinitialization is started this method should not provoke repeat requests, and should instead
     * return an appropriate status message.
     *
     * @return a string that can indicate
     */
    String reinitialize();
}