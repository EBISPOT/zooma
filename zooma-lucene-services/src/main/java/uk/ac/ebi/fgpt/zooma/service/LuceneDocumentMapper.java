package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;

/**
 * A mapper interface for mapping lucene documents into any type of object.
 *
 * @author Tony Burdett
 * @date 28/05/12
 */
public interface LuceneDocumentMapper<T> {
    /**
     * Maps the supplied document into an object of the appropriate type, T.  This method will map all documents with
     * an equal rank score (i.e. 1).  A runtime exception from Lucene will be thrown if this mapper expects fields to
     * be present on the document which are missing.
     *
     * @param d the document to map
     * @return the mapped object
     */
    T mapDocument(Document d);

    /**
     * Maps the supplied document into an object of the appropriate type, T.  This method will map the given document
     * considering the ranking obtained from the lucene search that returned this document. A runtime exception from
     * Lucene will be thrown if this mapper expects fields to be present on the document which are missing.
     *
     * @param d    the document to map
     * @param rank the rank (i.e. position) in a lucene search in which this document was returned
     * @return the mapped object
     */
    T mapDocument(Document d, int rank);

    /**
     * Returns a measure of the 'quality' of the underlying document.  This is an intrinsic measure of the quality of
     * the stored document, not a measure of the fitness against a query.  This method assumes a rank of 1 for every
     * document.
     *
     * @param d the document to score
     * @return a float indicating a quality score for this document
     */
    float getDocumentQuality(Document d);

    /**
     * Returns a measure of the 'quality' of the underlying document.  This is an intrinsic measure of the quality of
     * the stored document, not a measure of the fitness against a query.  This method considers the ranking obtained
     * from the lucene search that returned this document
     *
     * @param d the document to score
     * @return a float indicating a quality score for this document
     */
    float getDocumentQuality(Document d, int rank);
}
