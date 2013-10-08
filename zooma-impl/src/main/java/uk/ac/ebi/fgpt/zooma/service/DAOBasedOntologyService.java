package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.OntologyDAO;

import java.net.URI;
import java.util.Set;

/**
 * An ontology service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.OntologyDAO} to search for
 * ontology information.
 *
 * @author Tony Burdett
 * @date 13/09/12
 */
public class DAOBasedOntologyService extends AbstractShortnameResolver implements OntologyService {
    private OntologyDAO ontologyDAO;

    public OntologyDAO getOntologyDAO() {
        return ontologyDAO;
    }

    public void setOntologyDAO(OntologyDAO ontologyDAO) {
        this.ontologyDAO = ontologyDAO;
    }

    @Override public String getLabel(String semanticTagShortname) {
        return getLabel(getURIFromShortname(semanticTagShortname));
    }

    @Override public String getLabel(URI semanticTag) {
        return getOntologyDAO().getSemanticTagLabel(semanticTag);
    }

    @Override public Set<String> getSynonyms(URI semanticTag) {
        return getOntologyDAO().getSemanticTagSynonyms(semanticTag);
    }
}
