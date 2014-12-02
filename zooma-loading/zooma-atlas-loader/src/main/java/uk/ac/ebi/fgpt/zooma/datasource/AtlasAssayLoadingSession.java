package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas assays
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class AtlasAssayLoadingSession extends AtlasLoadingSession {
    protected AtlasAssayLoadingSession() {
        super(URI.create("http://purl.obolibrary.org/obo/OBI_0000070"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }

    @Override protected URI mintBioentityURI(String bioentityID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gxa/" +
                                  encode(studyAccessions[0]) + "#assay-" + bioentityID);

    }
}
