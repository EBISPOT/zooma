package uk.ac.ebi.fgpt.zooma.model;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Simple implementation of Links
 *
 * Created by olgavrou on 22/07/2016.
 */
public class ExternalLinks implements Links {

    /*
     Each SemanticTag object represents a predicted term and it's link
     to the Ontology Lookup Service
     */
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

    /*
     This method will append the given prefix to all the href's of the SemanticTags
     that belong to the olsLinks Set
    */
    public void addPrefixToAllOLSLinks(String prefix){
        for (SemanticTag olsLink : this.olsLinks){
            String href = olsLink.getHref().toString();
            try {
                olsLink.setHref(URI.create(prefix + URLEncoder.encode(href, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                olsLink.setHref(URI.create(prefix + href));
            }
        }
    }

}
