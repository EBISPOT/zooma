package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaResolutionException;

import java.util.Collection;

/**
 * Resolves a collection of new ZOOMA model objects against those already present in ZOOMA
 *
 * @author Tony Burdett
 * @date 11/06/13
 */
public interface ZoomaResolver<T> {
    /**
     * For the given set of objects, resolve them all to objects in ZOOMA.  This method will create new objects, create
     * new objects linked to older versions in cases of modifications, update old objects in the reference source if new
     * provenance is available, or exclude objects with no modifications.
     * <p/>
     * The resulting set of objects is the resolved and filtered set of objects which can subsequently are ultimately
     * eligible for loading into ZOOMA.
     *
     * @param datasourceName   the name of the datasource the collection of objects was obtained from (or some other
     *                         name to use in logging for identification purposes)
     * @param objectsToResolve the collection of objects requiring resolving
     * @return an update collection of objects that can be loaded into ZOOMA
     */
    Collection<T> resolve(String datasourceName, Collection<T> objectsToResolve)
            throws ZoomaResolutionException;

    /**
     * For the given object, resolve it to objects in ZOOMA.  This method will either create a new object, create a new
     * object linked to older versions in cases of modifications, update an old object in ZOOMA if new provenance is
     * available, or exclude objects with no modifications.
     * <p/>
     * If this resolves cleanly, without modifications, to a pre-existing object then the result returned is null and
     * should be handled by the caller.
     * <p/>
     *
     * @param objectToResolve the object to resolve
     * @return an updated object that can be loaded into ZOOMA, or null if this object pre-exists exactly in it's
     *         current form
     */
    T resolve(T objectToResolve) throws ZoomaResolutionException;

    /**
     * Returns a filtered set of objects, excluding all those that link to an empty set of semantic tags. Usually you
     * would call this method after resolving objects against those already in ZOOMA, making this a set of objects that
     * are eligible for loading.
     *
     * @param objects the set of objects to filter
     * @return a filtered set of objects excluding those which do not declare any semantic tags
     */
    Collection<T> filter(Collection<T> objects);

    /**
     * Determines whether the supplied object is present in ZOOMA. This assumes that the object exists in the reference
     * source if and only if an object with the same URI exists in the alternative source, but does not necessarily
     * imply that the object in the alternative source is equal to the supplied object.
     *
     * @param object the object to perform a lookup for
     * @return true if an object with the same URI exists, false otherwise
     */
    boolean exists(T object);

    /**
     * Determines whether, for the two supplied objects, updates have occurred.
     * <p/>
     * The object supplied can be considered to be an updated version of the reference object irrespective of whether
     * the URIs of the two objects are the same, although normally you would only use this method on two versions of an
     * object with the same URI.
     * <p/>
     * This method performs a deep comparison of the two objects (considering annotated properties, biological entities,
     * semantic tags and provenance).  Any changes to these considered fields result in this method returning true. Take
     * note that comparing two completely unrelated objects with this method will always return a positive result.
     * <p/>
     *
     * @param object          the new object - this is inspected for updates with respect to the reference object
     * @param referenceObject the reference object, that should be present in ZOOMA
     * @return true if object is updated with respect to referenceobject
     */
    boolean isUpdated(T object, T referenceObject);

    /**
     * Determines whether, for the given object, an older version of the same object exists somewhere in ZOOMA.
     * <p/>
     * Detected "modified" objects can have the same or different URIs - it is possible to perform a modification in the
     * original source which results in a new URI being minted.  Normally, however, you would only use this in cases
     * where {@link #exists(Object)} returns false, meaning there is no object in ZOOMA with a matching URI.  If calling
     * this method then returns true, it therefore follows that some update has occurred that has caused the object to
     * obtain a new URI.
     * <p/>
     * As an example of this, if a source defines a URI minting strategy that considers the semantic tag of the object,
     * and the semantic tag is updated in the original source, this will result in a new URI being assigned. However,
     * these two objects are clearly related.  In this scenario we inspect objects in ZOOMA, using a strategy defined by
     * our implementation, and attempt to identify possible updates that may have occurred.
     * <p/>
     * By contract, this method should return true if and only if {@link #findModified(Object)} will return a non-null
     * result, and likewise false if null would be returned.  As such, a simple implementation of this method could
     * simply delegate to {@link #findModified(Object)} and return the corresponding result.  This method is included to
     * allow smarter datasources to optimize with quick modification lookups if possible.
     *
     * @param object the object to examine
     * @return true if this is shown to be an update, false otherwise
     */
    boolean wasModified(T object);

    /**
     * Identifies an object, if present, that represents the unmodified version of the supplied object.  Usually you
     * would use this method when {@link #wasModified(Object)} returns true, in order to extract the object from ZOOMA
     * prior to modification.
     *
     * @param object the new supplied object, which is assumed to be a modified form of an object already present in
     *               ZOOMA
     * @return the unmodified object from ZOOMA
     */
    T findModified(T object);

    /**
     * Returns the type of update that occurred in this object, or null if there was none.
     *
     * @param object          the object to examine
     * @param referenceObject the unmodified version of the new object
     * @return the type of object modification that was shown to occur
     */
    Modification getModification(T object, T referenceObject);

    /**
     * Types of modification that may occur between versions of ZOOMA model objects
     */
    public enum Modification {
        PROPERTY_TYPE_MODIFICATION,
        PROPERTY_VALUE_MODIFICATION,
        BIOLOGICAL_ENTITY_MODIFICATION,
        SEMANTIC_TAG_MODIFICATION,
        PROVENANCE_MODIFICATION,
        NO_MODIFICATION
    }
}
