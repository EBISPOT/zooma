package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.*;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.server.SolrClientFactory;

/**
 * Temporary custom SolrTemplate class to override the spring bug
 * for spring data solr
 * Issue: https://github.com/spring-projects/spring-boot/issues/8327
 * Created by olgavrou on 28/02/2017.
 */
public class AnnotationSolrTemplate extends SolrTemplate {

    String solrCore = "annotations";

    public AnnotationSolrTemplate(SolrClient solrClient) {
        super(solrClient);
    }

    public AnnotationSolrTemplate(SolrClient solrClient, String core) {
        super(solrClient, core);
    }

    public AnnotationSolrTemplate(SolrClient solrClient, String core, RequestMethod requestMethod) {
        super(solrClient, core, requestMethod);
    }

    public AnnotationSolrTemplate(SolrClientFactory solrClientFactory) {
        super(solrClientFactory);
    }

    public AnnotationSolrTemplate(SolrClientFactory solrClientFactory, String defaultCore) {
        super(solrClientFactory, defaultCore);
    }

    public AnnotationSolrTemplate(SolrClientFactory solrClientFactory, RequestMethod requestMethod) {
        super(solrClientFactory, requestMethod);
    }

    public AnnotationSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter) {
        super(solrClientFactory, solrConverter);
    }

    public AnnotationSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter, RequestMethod defaultRequestMethod) {
        super(solrClientFactory, solrConverter, defaultRequestMethod);
    }

    @Override
    public <T> T execute(String collection, CollectionCallback<T> action) {
        return super.execute(this.solrCore, action);
    }
}
