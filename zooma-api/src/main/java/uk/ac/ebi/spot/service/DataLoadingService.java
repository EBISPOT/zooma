package uk.ac.ebi.spot.service;


import uk.ac.ebi.spot.datasource.ZoomaDAO;
import uk.ac.ebi.spot.model.Identifiable;
import uk.ac.ebi.spot.model.Update;

import java.util.Collection;
import java.util.Date;

/**
 * A service that allows data to be pulled from any available datasources and loaded into ZOOMA.  A "datasource" in this
 * context is a ZoomaDAO, and these DAOs should be indicated as available for loading.
 * <p/>
 * This is a high level interface that abstracts over specific loader implementations.  Implementations of this class
 * can load whichever sets of ZOOMA objects they chose to implement, and can do so in whichever way they chose.  The
 * outcome of using this class should be to take bespoke data and store it in the ZOOMA datasource.  Note that this
 * makes no guarantees about the liveness of this data: indexes may not be dynamically computed so loaded data may not
 * be immediately available in the interface.
 *
 * @author Tony Burdett
 * @date 07/06/13
 */
public interface DataLoadingService<T extends Identifiable> {
    /**
     * Returns the full set of available datasources that are currently available for loading into ZOOMA.
     *
     * @return datasources that are available for ZOOMA to load from
     */
    Collection<ZoomaDAO<T>> getAvailableDatasources();

    /**
     * Makes the supplied datasource available for loading into ZOOMA.
     *
     * @param datasource the new datasource to make available for loading into ZOOMA
     */
    void addDatasource(ZoomaDAO<T> datasource);

    /**
     * Loads all available data from all available datasources.  This method is asynchronous, and should return a {@link
     * Receipt} as soon as the request to load data has been received.  The resulting receipt should have a load type of
     * {@link LoadType#LOAD_ALL}.
     * <p/>
     * Once the client has obtained a receipt, they can then use this receipt to wait until a load is complete using
     * {@link service.DataLoadingService.Receipt#waitUntilCompletion()}
     */
    Receipt load();

    /**
     * Loads all available data from the specified datasource, if available.  This method is asynchronous, and should
     * return a {@link Receipt} as soon as the request to load data has been received.  The resulting receipt should
     * have a load type of {@link LoadType#LOAD_DATASOURCE}.
     * <p/>
     * Once the client has obtained a receipt, they can then use this receipt to wait until a load is complete using
     * {@link service.DataLoadingService.Receipt#waitUntilCompletion()}
     *
     * @param datasource the datasource to load from
     */
    Receipt load(ZoomaDAO<T> datasource);

    /**
     * Loads the set of data items supplied to this method.  This is a convenience method that delegates to {@link
     * #load(java.util.Collection, String)}, generating a dataset name from the timestamp and user making this request
     *
     * @param dataItems the set of data items that should be loaded into ZOOMA
     */
    Receipt load(Collection<T> dataItems);

    /**
     * Loads the set of data items supplied to this method, with the supplied dataset name.  This method is
     * asynchronous, and should return a {@link Receipt} as soon as the request to load data has been received.  The
     * resulting receipt should have a load type of {@link LoadType#LOAD_DATAITEMS}.
     * <p/>
     * Once the client has obtained a receipt, they can then use this receipt to wait until a load is complete using
     * {@link service.DataLoadingService.Receipt#waitUntilCompletion()}
     *
     * @param dataItems the set of data items that should be loaded into ZOOMA
     */
    Receipt load(Collection<T> dataItems, String datasetName);

    /**
     * This is a special Zooma function for loading updates to annotations. All Zooma annotation updates require a
     * set of annotations to update and information about the fields if the annotations that have changed.
     * Zooma only allows properties and/or semantic tags to be updated for a given annotation. Un update operation involves
     * the creation of a new Annotation with the supplied fields that links back to the previous annotation is replaces.
     * This methods is asynchronous, and should return a {@link Receipt} as soon as the request to load data has been received.  The
     * resulting receipt should have a load type of {@link LoadType#UPDATE_DATAITEMS}.
     * <p/>
     * Once the client has obtained a receipt, they can then use this receipt to wait until a load is complete using
     * {@link service.DataLoadingService.Receipt#waitUntilCompletion()}
     *
     * @param dataItems the set of data items that should be updated in Zooma
     * @param update the updates to be performed
     */
    Receipt update(Collection<T> dataItems, Update<T> update);

    /**
     * Returns a message describing the current status of this data loading service
     */
    String getServiceStatus();

    /**
     * Returns a receipt status describing the current status of the task that the receipt with the given ID was issued
     * for
     *
     * @param receiptID the ID of the receipt that was issued as a result of a load request
     * @return the receipt status
     */
    ReceiptStatus getReceiptStatus(String receiptID);

    /**
     * A receipt confirming that a request to load data via a {@link DataLoadingService} has been received.  Acquisition
     * of a receipt confirms that some data will be loaded, although this may be at some point in the future.  A receipt
     * is not the same as a {@link java.util.concurrent.Future} as it does not contain a result of any computational
     * process - it merely confirms that a request to perform an asynchronous computation (in this case, data loading)
     * was correctly received.
     */
    public interface Receipt {
        String getID();

        String getDatasourceName();

        LoadType getLoadType();

        void waitUntilCompletion() throws InterruptedException;

        Date getSubmissionDate();

        Date getCompletionDate();
    }

    /**
     * A lightweight object that allows a lookup of the progress of a task for which a {@link
     * service.DataLoadingService.Receipt} was issued.  Once client code has issued a request that
     * returns a receipt, subsequent lookups by receipt ID can return a ReceiptStatus that indicates whether the task
     * the receipt was issued for is complete or not.
     */
    public interface ReceiptStatus {
        String getReceiptID();

        boolean isComplete();

        boolean isSuccessful();

        String getErrorMessage();
    }

    /**
     * A service that can be used to lookup the progress of a task for which a {@link
     * service.DataLoadingService.Receipt} was issued.
     */
    public interface ReceiptService {
        /**
         * Register an issued {@link Receipt} with this receipt service, so that it's status can subsequently be looked
         * up.
         *
         * @param receipt the receipt to register
         */
        void registerReceipt(Receipt receipt);

        /**
         * Get the status of a task for which a receipt was issued, encapsulated as a {@link
         * service.DataLoadingService.ReceiptStatus}
         *
         * @param receiptID the ID of the receipt to check the status for
         * @return the ReceiptStatus that indicates the current progress of the task for this receipt
         */
        ReceiptStatus getReceiptStatus(String receiptID);
    }

    /**
     * The type of load submitted to an {@link DataLoadingService}
     */
    public enum LoadType {
        /**
         * @see service.DataLoadingService#load()
         */
        LOAD_ALL,
        /**
         * @see service.DataLoadingService#load(datasource.ZoomaDAO)
         */
        LOAD_DATASOURCE,
        /**
         * @see service.DataLoadingService#load(java.util.Collection)
         */
        LOAD_DATAITEMS,
        /**
         * @see service.DataLoadingService#update(java.util.Collection, model.Update)
         */
        UPDATE_DATAITEMS
    }
}