package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collections;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas samples
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class AtlasSampleLoadingSession extends AtlasLoadingSession {
    protected AtlasSampleLoadingSession() {
        super(URI.create("http://purl.obolibrary.org/obo/OBI_0000747"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName,
                                             String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" +
                                  encode(studyAccessions[0]) + "#sample-" + bioentityID);
    }
}
