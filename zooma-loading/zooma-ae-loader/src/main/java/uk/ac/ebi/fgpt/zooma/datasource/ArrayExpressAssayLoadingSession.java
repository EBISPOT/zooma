package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collections;

/**
 * An arrayexpress annotation loading session that can generate URIs specific to arrayexpress assays
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class ArrayExpressAssayLoadingSession extends ArrayExpressLoadingSession {
    protected ArrayExpressAssayLoadingSession() {
        super(Collections.<URI>singleton(URI.create("http://purl.obolibrary.org/obo/OBI_0000070")),
              Collections.<URI>singleton(URI.create("http://www.ebi.ac.uk/efo/EFO_0004033")));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "arrayexpress/" +
                                  encode(studyAccessions[0]) + "#assay-" + bioentityID);
    }
}
