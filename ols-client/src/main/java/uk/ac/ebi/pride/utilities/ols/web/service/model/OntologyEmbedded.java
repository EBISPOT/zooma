package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 02/03/2016
 */
public class OntologyEmbedded {

    @JsonProperty("ontologies")
    private Ontology[] ontologies;

    public Ontology[] getTerms() {
        return ontologies;
    }

    public void setOntologies(Ontology[] ontologies) {
        this.ontologies = ontologies;
    }
}
