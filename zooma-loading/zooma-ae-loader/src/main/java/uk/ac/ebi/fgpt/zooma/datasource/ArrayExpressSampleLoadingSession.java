package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An arrayexpress annotation loading session that can generate URIs specific to arrayexpress samples
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class ArrayExpressSampleLoadingSession extends ArrayExpressLoadingSession {
    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.AE_RESOURCE.getURI().toString() + "experiment/" +
                                  encode(studyAccessions[0]) + "#sample-" + bioentityID);
    }
}
