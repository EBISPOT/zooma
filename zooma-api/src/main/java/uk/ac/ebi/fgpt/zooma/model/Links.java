package uk.ac.ebi.fgpt.zooma.model;

import java.util.Set;

/**
 * Created by olgavrou on 22/07/2016.
 */
public interface Links {
    Set<SemanticTag> getOLSLinks();
    void setOlsLinks(Set<SemanticTag> olsLink);
}
