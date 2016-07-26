package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * Created by olgavrou on 25/07/2016.
 */
public class SimpleSemanticTag implements SemanticTag {

    private URI href;
    private URI semanticTag;

    public SimpleSemanticTag(URI semanticTag, URI href){
        this.semanticTag = semanticTag;
        this.href = href;
    }

    @Override
    public URI getSemanticTag() {
        return this.semanticTag;
    }

    @Override
    public void setSemanticTag(URI semanticTag) {
        this.semanticTag = semanticTag;
    }

    @Override
    public URI getHref() {
        return href;
    }

    @Override
    public void setHref(URI href) {
        this.href = href;
    }

}
