package uk.ac.ebi.fgpt.zooma.model;

import java.util.Set;

/**
 *
 * Represents the external _links for the API
 *
 * Created by olgavrou on 22/07/2016.
 */
public interface Links {

    /*
     Contains a Set of olsLinks each which is a SemanticTag.
     Should have a SemanticTag with an href to the Ontology Lookup Service term for each semantic tag predicted
     */
    Set<SemanticTag> getOLSLinks();
    void setOlsLinks(Set<SemanticTag> olsLink);
}
