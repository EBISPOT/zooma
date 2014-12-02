package uk.ac.ebi.fgpt.zooma.datasource;

import java.net.URI;

/**
 * An arrayexpress annotation loading session that can generate URIs specific to arrayexpress assays
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class ArrayExpressAssayLoadingSession extends ArrayExpressLoadingSession {
    protected ArrayExpressAssayLoadingSession() {
        super("arrayexpress.assays",
              URI.create("http://purl.obolibrary.org/obo/OBI_0000070"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }
}
