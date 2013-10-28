package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * An arrayexpress annotation loading session that can generate URIs specific to arrayexpress samples
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class ArrayExpressSampleLoadingSession extends ArrayExpressLoadingSession {

    protected ArrayExpressSampleLoadingSession( ) {
        super( Collections.<URI>singleton(URI.create("http://purl.obolibrary.org/obo/OBI_0000747")));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "arrayexpress/" +
                                  encode(studyAccessions[0]) + "#sample-" + bioentityID);
    }


}
