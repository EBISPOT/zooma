package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author olgavrou
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsoleteTerm extends Term {

    @JsonProperty("is_obsolete")
    private boolean obsolete;

    @JsonProperty("term_replaced_by")
    private String termReplacedBy;

    public ObsoleteTerm(Identifier iri, String label, String[] description,
                        Identifier shortForm, Identifier oboId, String ontologyName, String score, String ontologyIri,
                        boolean definedOntology,
                        OboDefinitionCitation[] oboDefinitionCitation,
                        Annotation annotation, boolean obsolete, String termReplacedBy) {
        super(iri, label, description, shortForm, oboId, ontologyName, score, ontologyIri, definedOntology, oboDefinitionCitation, annotation);
        this.obsolete = obsolete;
        this.termReplacedBy = termReplacedBy;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getTermReplacedBy() {
        return termReplacedBy;
    }

    public void setTermReplacedBy(String termReplacedBy) {
        this.termReplacedBy = termReplacedBy;
    }
}
