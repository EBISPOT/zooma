package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author olgavrou
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveTermQuery {

    @JsonProperty("_embedded")
    private TermEmbeddedSearchResult response;

    public TermEmbeddedSearchResult getResponse() {
        return response;
    }

    public void setResponse(TermEmbeddedSearchResult response) {
        this.response = response;
    }
}
