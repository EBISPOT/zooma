package uk.ac.ebi.fgpt.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This loading service is responsible for retrieving all the semantic tags from the Zooma Annotations,
 * finding their term labels from the Ontology Lookup Service through the {@link OLSSearchService}, and
 * loading them into the graph.
 *
 * Created by olgavrou on 05/10/2016.
 */
public class LoadOLSLabelsService {

    private OntologyService ontologyServiceDAO;
    private OLSSearchService olsSearch;
    private Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    public OntologyService getOntologyServiceDAO() {
        return ontologyServiceDAO;
    }

    public void setOntologyServiceDAO(OntologyService ontologyServiceDAO) {
        this.ontologyServiceDAO = ontologyServiceDAO;
    }

    public OLSSearchService getOlsSearch() {
        return olsSearch;
    }

    public void setOlsSearch(OLSSearchService olsSearch) {
        this.olsSearch = olsSearch;
    }

    public void findAndLoad(){
        Map<String, String> tagToLabelMap = new HashMap<>();

        getLog().info("Getting all the annotations's semantic tags");
        Set<String> semanticTags = getOntologyServiceDAO().getSemanticTags();
        getLog().info("Finding labels for each semantic tag using OLS starting...");
        for (String semanticTag : semanticTags) {
            tagToLabelMap.put(semanticTag, getOlsSearch().getLabelByIri(semanticTag));
        }
        getLog().info("Finding labels for each semantic tag using OLS finished...");
        getLog().info("Inserting the labels into the graph starting...");
        getOntologyServiceDAO().insertLabels(tagToLabelMap);
        getLog().info("Inserting the labels into the graph finished...");
        getLog().info("Loaded: " + semanticTags.size() + " labels");
    }

}
