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
    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName,
                                             String... studyAccessions) {

        return URI.create(Namespaces.GXA_RESOURCE.getURI().toString() + "experiment/" +
                                  encode(studyAccessions[0]) + "#assay-" + bioentityID);

    }
}
