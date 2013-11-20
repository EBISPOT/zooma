package uk.ac.ebi.fgpt.zooma.service;

/**
 * An {@link LuceneDocumentMapper} that maps the value in a single field into a string result.  The name of the field to
 * be mapped to the result should be specified in the constructor.
 *
 * @author Tony Burdett
 * @date 20/11/13
 */
public class SingleFieldStringMapper extends AbstractSingleFieldMapper<String> {
    public SingleFieldStringMapper(String fieldname) {
        super(fieldname);
    }

    /**
     * Returns the string that was directly acquired from lucene.
     *
     * @param fieldValue the value in the lucene index for the given field
     * @return fieldValue
     */
    @Override protected String convertResult(String fieldValue) {
        return fieldValue;
    }
}
