package uk.ac.ebi.fgpt.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A basic, in memory caching implementation of a {@link uk.ac.ebi.fgpt.zooma.service.DataLoadingService.ReceiptService}
 * that stores every registered receipt in memory.
 *
 * @author Tony Burdett
 * @date 29/01/14
 */
public class InMemoryReceiptService implements DataLoadingService.ReceiptService {
    private final Map<String, DataLoadingService.ReceiptStatus> receiptStatusCache;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public InMemoryReceiptService() {
        receiptStatusCache = new HashMap<>();
    }

    @Override public void registerReceipt(DataLoadingService.Receipt receipt) {
        synchronized (receiptStatusCache) {
            receiptStatusCache.put(receipt.getID(), new ReceiptStatusImpl(receipt.getID(), false, false, ""));
        }
        monitorProgress(receipt);
    }

    @Override public DataLoadingService.ReceiptStatus getReceiptStatus(String receiptID) {
        synchronized (receiptStatusCache) {
            if (receiptStatusCache.containsKey(receiptID)) {
                return receiptStatusCache.get(receiptID);
            }
            else {
                // unknown receipt?
                throw new IllegalArgumentException("Unknown or unregistered receipt '" + receiptID + "', " +
                                                           "status is not known");
            }
        }
    }

    protected void monitorProgress(final DataLoadingService.Receipt receipt) {
        final String id = receipt.getID();
        new Thread(new Runnable() {
            @Override public void run() {
                boolean isComplete = false;
                while (!isComplete) {
                    try {
                        try {
                            receipt.waitUntilCompletion();
                            getLog().debug("Waiting for receipt '" + id + " to complete.");
                            synchronized (receiptStatusCache) {
                                receiptStatusCache.remove(id);
                                receiptStatusCache.put(id, new ReceiptStatusImpl(id, true, true, ""));
                            }
                        }
                        catch (RuntimeException e) {
                            // capture exception so scheduler can return it when waitUntilComplete() is called
                            getLog().error("Receipt monitor caught a runtime exception", e);
                            synchronized (receiptStatusCache) {
                                receiptStatusCache.remove(id);
                                receiptStatusCache.put(id, new ReceiptStatusImpl(id, true, false, e.getMessage()));
                            }
                        }
                        finally {
                            // update completion date
                            getLog().debug("Monitoring of receipt '" + id + "' is complete");
                            isComplete = true;
                        }
                    }
                    catch (InterruptedException e) {
                        getLog().debug("Interrupted whilst waiting for receipt '" + id + "' to finish, continuing");
                    }
                }
            }
        }, "receipt-monitor-" + id).start();
    }

    private class ReceiptStatusImpl implements DataLoadingService.ReceiptStatus {
        private final String receiptID;
        private final boolean complete;
        private final boolean successful;
        private final String errorMessage;

        private ReceiptStatusImpl(String receiptID, boolean complete, boolean successful, String errorMessage) {
            this.receiptID = receiptID;
            this.complete = complete;
            this.successful = successful;
            this.errorMessage = errorMessage;
        }

        public String getReceiptID() {
            return receiptID;
        }

        public boolean isComplete() {
            return complete;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
