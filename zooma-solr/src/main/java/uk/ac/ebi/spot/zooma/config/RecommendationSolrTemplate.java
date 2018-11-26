package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.CollectionCallback;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.server.SolrClientFactory;

public class RecommendationSolrTemplate extends SolrTemplate {

    String solrCore = "recommendations";

    public RecommendationSolrTemplate(SolrClient solrClient) {
        super(solrClient);
    }

    public RecommendationSolrTemplate(SolrClient solrClient, String core) {

        super(solrClient, core);
    }

    public RecommendationSolrTemplate(SolrClient solrClient, String core, RequestMethod requestMethod) {
        super(solrClient, core, requestMethod);
    }

    public RecommendationSolrTemplate(SolrClientFactory solrClientFactory) {
        super(solrClientFactory);
    }

    public RecommendationSolrTemplate(SolrClientFactory solrClientFactory, String defaultCore) {
        super(solrClientFactory, defaultCore);
    }

    public RecommendationSolrTemplate(SolrClientFactory solrClientFactory, RequestMethod requestMethod) {
        super(solrClientFactory, requestMethod);
    }

    public RecommendationSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter) {
        super(solrClientFactory, solrConverter);
    }

    public RecommendationSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter, RequestMethod defaultRequestMethod) {
        super(solrClientFactory, solrConverter, defaultRequestMethod);
    }

    @Override
    public <T> T execute(String collection, CollectionCallback<T> action) {
        return super.execute(this.solrCore, action);
    }


}
