package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;

/**
 * Maps a the value of a single field into an object of type <code>T</code>.  The name of the field to map should be
 * supplied to the constructor - this class extracts the string value from lucene and supplies it to the method {@link
 * #convertResult(String)}.  Implementing classes should override this method to determine what should be done with the
 * results.
 *
 * @param <T>
 * @author Tony Burdett
 * @date 20/11/13
 */
public abstract class AbstractSingleFieldMapper<T> implements LuceneDocumentMapper<T> {
    private final String fieldname;

    public AbstractSingleFieldMapper(String fieldname) {
        this.fieldname = fieldname;
    }

    @Override public T mapDocument(Document d) {
        return convertResult(d.get(fieldname));
    }

    @Override
    public T mapDocument(Document d, int rank) {
        return convertResult(d.get(fieldname));
    }

    /**
     * This implementation returns 1 for all documents; the inherent quality of a document is not assessed in this
     * implementation.  If your document can be quality scored, you can override this implementation to tailor the
     * values returned.
     *
     * @param d the document to score
     * @return 1 for all documents
     */
    @Override public float getDocumentQuality(Document d) {
        return 1;
    }

    /**
     * This implementation returns 1 for all documents; the inherent quality of a document is not assessed in this
     * implementation.  If your document can be quality scored, you can override this implementation to tailor the
     * values returned.
     *
     * @param d the document to score
     * @return 1 for all documents
     */
    @Override public float getDocumentQuality(Document d, int rank) {
        return 1;
    }

    protected abstract T convertResult(String fieldValue);
}
