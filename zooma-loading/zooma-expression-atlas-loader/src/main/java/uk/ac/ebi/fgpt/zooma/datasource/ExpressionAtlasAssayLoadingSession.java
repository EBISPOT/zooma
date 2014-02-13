package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas assays
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public class ExpressionAtlasAssayLoadingSession extends ExpressionAtlasLoadingSession {
    protected ExpressionAtlasAssayLoadingSession() {
        super(URI.create("http://purl.obolibrary.org/obo/OBI_0000070"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "atlas/" +
                                  encode(studyAccessions[0]) + "#assay-" + bioentityID);
    }
}
