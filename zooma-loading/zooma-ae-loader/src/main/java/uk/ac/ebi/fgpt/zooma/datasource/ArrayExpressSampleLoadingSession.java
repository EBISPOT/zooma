package uk.ac.ebi.fgpt.zooma.datasource;

import java.net.URI;

/**
 * An arrayexpress annotation loading session that can generate URIs specific to arrayexpress samples
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class ArrayExpressSampleLoadingSession extends ArrayExpressLoadingSession {
    protected ArrayExpressSampleLoadingSession() {
        super("arrayexpress.samples",
              URI.create("http://purl.obolibrary.org/obo/OBI_0000747"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }
}
