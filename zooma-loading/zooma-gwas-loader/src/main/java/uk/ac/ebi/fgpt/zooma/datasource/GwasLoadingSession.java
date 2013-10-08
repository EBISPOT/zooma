package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An annotation loading session that is capable of minting URIs specific to GWAS data
 *
 * @author Dani Welter
 * @date 06/11/12
 */
public class GwasLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.PUBMED.getURI().toString() + studyAccession);
    }

    @Override
    protected URI mintBioentityURI(String bioentityID,
                                   String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.GWAS_RESOURCE.getURI().toString() + "snp/" + encode(bioentityName));
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.GWAS_RESOURCE.getURI().toString() + "property/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.GWAS_RESOURCE.getURI().toString() + "annotation/" + annotationID);
    }
}
