package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 08/11/2013
 * Functional Genomics Group EMBL-EBI
 *
 * This partial implementation of the AnnotationSummaryDAO is designed
 * specifically to support the efficient creation of the Zooma lucene indexes
 */
public class SparqlLuceneAnnotationSummaryDAO implements AnnotationSummaryDAO{

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private QueryManager queryManager;

    protected Logger getLog() {
        return log;
    }

    public JenaQueryExecutionService getQueryService() {
        return queryService;
    }

    public void setQueryService(JenaQueryExecutionService queryService) {
        this.queryService = queryService;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    @Override
    public String getDatasourceName() {
        return "zooma";
    }

    @Override
    public int count() {
        return 0;
    }

    public Collection<AnnotationSummary> read() {

        getLog().debug("Reading all annotation summaries");
        String query = getQueryManager().getSparqlQuery("AnnotationSummaries.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        Map<URI, AnnotationSummary> propertyToSummary = null;

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1, false);
            ResultSet results = execute.execSelect();
            propertyToSummary = evaluateQueryResults(results);
        } catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (execute !=  null)  {
                execute.close();
                if (g != null ) {
                    g.close();
                }
            }
        }
        return propertyToSummary.values();
    }

    private Map<URI, AnnotationSummary> evaluateQueryResults(ResultSet results) {
        Map<URI, AnnotationSummary> propertyToSummary = new HashMap<URI, AnnotationSummary>();
        while (results.hasNext()) {
            QuerySolution solution = (QuerySolution) results.next();
            getSummaryFromBindings(propertyToSummary, solution);
        }
        return propertyToSummary;
    }

    final static String underscore = "_";

    public void getSummaryFromBindings( Map<URI, AnnotationSummary> propertyToSummary, QuerySolution solution) {

        Resource annotationid = solution.getResource(QueryVariables.ANNOTATION_ID.toString());
        Resource propertyvalueid = solution.getResource(QueryVariables.PROPERTY_VALUE_ID.toString());
        Resource semantictag = solution.getResource(QueryVariables.SEMANTIC_TAG.toString());
        Literal propertyNameValue = solution.getLiteral(QueryVariables.PROPERTY_NAME.toString());
        Literal propertyValueValue = solution.getLiteral(QueryVariables.PROPERTY_VALUE.toString());


        URI annotationUri = URI.create(annotationid.getURI());
        URI pvUri = URI.create(propertyvalueid.getURI());

        if (!propertyToSummary.containsKey(pvUri)) {
            propertyToSummary.put(pvUri, new SimpleLuceneSummary(
                    propertyNameValue != null ? propertyNameValue.getLexicalForm() : null,
                    propertyValueValue.getLexicalForm()));
        }

        if (semantictag != null) {
            propertyToSummary.get(pvUri).getSemanticTags().add(URI.create(semantictag.getURI()));
        }

        propertyToSummary.get(pvUri).getAnnotationURIs().add(annotationUri);

    }

    @Override
    public List<AnnotationSummary> read(int size, int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationSummary read(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(AnnotationSummary object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(AnnotationSummary object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(AnnotationSummary identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    public class SimpleLuceneSummary implements AnnotationSummary {


        private String id = null;
        private String propertyType;
        private String propertyValue;
        private Collection<URI> semanticTags;
        private Collection<URI> annotationURIs;
        private float qualityScore = 0;

        public SimpleLuceneSummary(String propertyType, String propertyValue) {
            this.propertyType = propertyType;
            this.propertyValue = propertyValue;
            this.semanticTags = new HashSet<>();
            this.annotationURIs = new HashSet<>();
        }

        @Override
        public String getAnnotationSummaryTypeID() {
            return id;
        }

        @Override
        public String getAnnotationSummaryTypeName() {
            return null;
        }

        @Override
        public String getID() {
            return null;
        }

        @Override
        public String getAnnotatedPropertyValue() {
            return propertyValue;
        }

        @Override
        public String getAnnotatedPropertyType() {
            return propertyType;
        }

        public Collection<URI> getSemanticTags() {
            return semanticTags;
        }


        public Collection<URI> getAnnotationURIs() {
            return annotationURIs;
        }


        public float getQualityScore() {
            return qualityScore;
        }

        @Override
        public URI getURI() {
            return null;
        }

        @Override
        public String toString() {
            return "SimpleLuceneSummary{" +
                    ", propertyType='" + propertyType + '\'' +
                    ", propertyValue='" + propertyValue + '\'' +
                    ", semanticTags=" + semanticTags +
                    ", annotationURIs=" + annotationURIs +
                    '}';
        }
    }
}
