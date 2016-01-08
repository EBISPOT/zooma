package uk.ac.ebi.fgpt.zooma.service;

/**
 * A generic interface for scoring query results that are returned from a Lucene search.  The result being scored is the
 * object that is returned from any form of the <code>doQueryAndScore()</code> method on {@link
 * ZoomaLuceneSearchService}.  This may include objects that have been type mapped by a {@link LuceneDocumentMapper}.
 *
 * @author Tony Burdett
 * @date 20/11/13
 */
public interface LuceneResultScorer<T> {
    float scoreResult(T result, float luceneScore);
}
