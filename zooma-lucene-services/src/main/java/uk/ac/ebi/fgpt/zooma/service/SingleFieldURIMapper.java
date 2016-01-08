package uk.ac.ebi.fgpt.zooma.service;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 20/11/13
 */
public class SingleFieldURIMapper extends AbstractSingleFieldMapper<URI> {
    public SingleFieldURIMapper() {
        this("uri");
    }

    public SingleFieldURIMapper(String uriFieldName) {
        super(uriFieldName);
    }

    @Override protected URI convertResult(String fieldValue) {
        return URI.create(fieldValue);
    }
}
