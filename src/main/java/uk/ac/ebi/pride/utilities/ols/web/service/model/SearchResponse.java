package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 03/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

    @JsonProperty("numFound")
    int numFound;

    @JsonProperty("start")
    int currentPage;

    @JsonProperty("docs")
    SearchResult[] searchResults;

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public SearchResult[] getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResult[] searchResults) {
        this.searchResults = searchResults;
    }
}
