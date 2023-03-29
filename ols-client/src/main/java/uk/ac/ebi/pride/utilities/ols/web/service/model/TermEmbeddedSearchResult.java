package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author olgavrou
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TermEmbeddedSearchResult{

    @JsonProperty("terms")
    private SearchResult[] searchResults;

    public SearchResult[] getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResult[] searchResults) {
        this.searchResults = searchResults;
    }
}
