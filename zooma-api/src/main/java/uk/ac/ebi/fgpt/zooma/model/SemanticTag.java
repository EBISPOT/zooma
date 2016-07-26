package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * Represents a set of uris
 * the semantic tag and an href for that semantic tag
 *
 * Created by olgavrou on 25/07/2016.
 */
public interface SemanticTag {
    URI getSemanticTag();
    void setSemanticTag(URI uri);
    URI getHref();
    void setHref(URI link);
}
