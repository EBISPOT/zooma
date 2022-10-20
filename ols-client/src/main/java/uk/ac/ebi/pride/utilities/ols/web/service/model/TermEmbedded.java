package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ypriverol
 */

@JsonIgnoreProperties
public class TermEmbedded {

    @JsonProperty("terms")
    private Term[] terms;

    public Term[] getTerms() {
        return terms;
    }

    public void setTerms(Term[] terms) {
        this.terms = terms;
    }
}
