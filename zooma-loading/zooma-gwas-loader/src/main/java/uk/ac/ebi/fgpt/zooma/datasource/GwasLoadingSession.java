package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;

/**
 * An annotation loading session that is capable of minting URIs specific to GWAS data
 *
 * @author Dani Welter
 * @date 06/11/12
 */
public class GwasLoadingSession extends AbstractAnnotationLoadingSession {


    protected GwasLoadingSession() {
        super(Collections.singleton(URI.create("http://purl.obolibrary.org/obo/SO_0000694")), new HashSet<URI>());
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.PUBMED.getURI().toString() + studyAccession);
    }

    @Override
    protected URI mintBioentityURI(String bioentityID,
                                   String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gwas/" + encode(bioentityName));
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gwas/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "gwas/" + annotationID);
    }
}
