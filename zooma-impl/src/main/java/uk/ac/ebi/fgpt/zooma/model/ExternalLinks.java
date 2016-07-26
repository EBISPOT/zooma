package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Set;

/**
 * Created by olgavrou on 22/07/2016.
 */
public class ExternalLinks implements Links {

    private Set<SemanticTag> olsLinks;

    public ExternalLinks(Set<SemanticTag> olsLinks){
        this.olsLinks = olsLinks;
    }


    @Override
    public Set<SemanticTag> getOLSLinks() {
        return olsLinks;
    }

    @Override
    public void setOlsLinks(Set<SemanticTag> olsLinks) {
        this.olsLinks = olsLinks;
    }


    @Override
    public String toString() {
        return "ExternalLinks{" +
                "olsLink=" + olsLinks +
                '}';
    }

    public void addPrefixToAllOLSLinks(String prefix){
        for (SemanticTag olsLink : this.olsLinks){
            String semanticTag = olsLink.getSemanticTag().toString();
            olsLink.setHref(URI.create(prefix + semanticTag));
        }
    }

}
