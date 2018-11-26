package uk.ac.ebi.spot.zooma.config;

public class HttpSolrClient extends  org.apache.solr.client.solrj.impl.HttpSolrClient {
    private static final long serialVersionUID = 1973496789131415484L;


    public HttpSolrClient() {
        this, , );
    }

    public HttpSolrClient(String baseURL,
                          HttpClient client,
                          ResponseParser parser) {
        super(baseURL, client, parser);
    }


    public HttpSolrClient(String baseURL, HttpClient client) {
        super(baseURL, client);
    }


    public HttpSolrClient(String baseURL) {
        super(baseURL);
    }


    @SuppressWarnings("rawtypes")
    @Override
    protected HttpRequestBase createMethod(SolrRequest request,
                                           String collection) throws IOException, SolrServerException {
        String col = (collection != null && baseUrl.endsWith(collection)) ? null : collection;
        return super.createMethod(request, col);
    }
}