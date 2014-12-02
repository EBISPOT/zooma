package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas samples
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public class ExpressionAtlasSampleLoadingSession extends ExpressionAtlasLoadingSession {
    protected ExpressionAtlasSampleLoadingSession() {
        super(URI.create("http://purl.obolibrary.org/obo/OBI_0000747"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }

    @Override protected URI mintBioentityURI(String bioentityID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "atlas/" +
                                  encode(studyAccessions[0]) + "#sample-" + bioentityID);
    }
}
