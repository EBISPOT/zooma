package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * Created by dwelter on 28/05/14.
 */
public class ChemblLoadingSession extends AbstractAnnotationLoadingSession {


    protected ChemblLoadingSession() {
        super(URI.create("http://purl.obolibrary.org/obo/CLO_0000031"), null);
    }

    @Override protected URI mintStudyURI(String studyID) {
        return URI.create(Namespaces.PUBMED.getURI().toString() + studyAccession);
    }

    @Override
    protected URI mintBioentityURI(String bioentityID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "chembl/" + encode(bioentityName));
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "chembl/" + annotationID);
    }
}
