package uk.ac.ebi.spot.datasource;


import uk.ac.ebi.spot.exception.NoSuchResourceException;
import uk.ac.ebi.spot.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.spot.model.Identifiable;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * A data access identifiable that defines methods to create, readByProperty, update and delete identifiable objects
 * from a ZOOMA datasource.
 *
 * @param <I> the type of identifiable identifiable this DAO can work with
 * @author Tony Burdett
 * @date 30/03/12
 */
public interface ZoomaDAO<I extends Identifiable> {
    /**
     * Retrieves a name that represents the datasource that this DAO retrieves data from.  Although this is not treated
     * as a unique key, it is best to take care to ensure that implementations define a reasonably unique name so as to
     * avoid confusion when users attempt to identify DAOs using this field.
     * <p/>
     * If you want to categorize datasources into subsets, the recommended separator to use is a period (".") to split
     * into Java-like packages.
     *
     * @return the user-friendly name of this datasource
     */
    String getDatasourceName();

    /**
     * Performs a count query that returns the total number of objects that will be retrieved by this DAO when a {@link
     * #read()} query is performed
     *
     * @return the total number of objects that can be read from this DAO
     */
    int count();

    /**
     * Inserts the supplied identifiable into the zooma datasource.  The provided identifiable must be a new
     * identifiable that zooma has not seen before.  If an identifiable with the same URI as the supplied one already
     * exists in zooma, this operation will fail with an {@link uk.ac.ebi.spot.exception.ResourceAlreadyExistsException}.
     *
     * @param identifiable the identifiable to add to zooma
     * @throws uk.ac.ebi.spot.exception.ResourceAlreadyExistsException
     *          if an identifiable with a matching URI to the supplied identifiable already exists
     */
    void create(I identifiable) throws ResourceAlreadyExistsException;

    /**
     * Retrieves all objects from a zooma datasource.  No ordering is assumed, and repeat calls to this method are not
     * obliged to return results in the same order.
     *
     * @return the collection of all objects
     */
    Collection<I> read();

    /**
     * Retrieves a collection of objects from a zooma datasource, limited to the given size starting at the given index.
     * Ordering should be consistent across repeat calls of this method.
     *
     * @return a list of objects of the given size, starting from the supplied index
     */
    List<I> read(int size, int start);

    /**
     * Retrieves an identifiable from a zooma datasource given it's URI.
     *
     * @param uri the identifier of this property
     * @return the property with this URI
     */
    I read(URI uri);

    /**
     * Updates any fields on the supplied identifiable in the zooma datasource that do not match the current state of
     * this identifiable.  This is done by comparing URIs - if an identifiable exists with a URI that matches the
     * supplied identifiable, the existing identifiable is updated to match the supplied one.  If no such identifiable
     * exists, this operation will fail with an {@link uk.ac.ebi.spot.exception.NoSuchResourceException}.
     *
     * @param object the identifiable in zooma to update (identity is assumed by matching URI)
     * @throws uk.ac.ebi.spot.exception.NoSuchResourceException
     *          if an identifiable with a matching URI to the supplied identifiable does not already exist
     */
    void update(I object) throws NoSuchResourceException;

    /**
     * Updates an identifiable in the zooma datasource that match the supplied identifiable.  This is done by comparing
     * URIs - if an identifiable exists with a URI that matches the supplied identifiable, the existing identifiable is
     * updated to match the supplied one.  If no such identifiable exists, this operation will fail with an {@link
     * NoSuchResourceException}.
     *
     * @param object the identifiable to delete
     * @throws NoSuchResourceException if the identifiable to be deleted does not already exist
     */
    void delete(I object) throws NoSuchResourceException;
}
