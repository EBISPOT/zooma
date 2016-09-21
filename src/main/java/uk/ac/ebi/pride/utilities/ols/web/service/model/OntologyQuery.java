package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 01/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class OntologyQuery extends QueryResult{

    @JsonProperty("_embedded")
    OntologyEmbedded ontologyEmbedded;

    public Ontology[] getOntolgoies() {
        if(ontologyEmbedded != null)
            return ontologyEmbedded.getTerms();
        return null;
    }

    public OntologyEmbedded getOntologyEmbedded() {
        return ontologyEmbedded;
    }

    public void setOntologyEmbedded(OntologyEmbedded ontologyEmbedded) {
        this.ontologyEmbedded = ontologyEmbedded;
    }
}
