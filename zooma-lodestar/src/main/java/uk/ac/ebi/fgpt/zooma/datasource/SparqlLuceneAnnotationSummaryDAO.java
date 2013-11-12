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
 */
public class SparqlLuceneAnnotationSummaryDAO implements AnnotationSummaryDAO{

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private QueryManager queryManager;

    private PropertyDAO propertyDAO;

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

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

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

//    public Map<URI, Collection<URI>> readDistinctPropertyToSemanticTag() {
//        getLog().debug("Reading all distinct property to semantic tags");
//        String query = getQueryManager().getSparqlQuery("PropertySemanticTag");
//        Graph g = getQueryService().getDefaultGraph();
//        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
//
//        Map<URI, Collection<URI>> propertyTagMap;
//
//        QueryExecution execute = null;
//        try {
//            execute = getQueryService().getQueryExecution(g, q1, false);
//            ResultSet results = execute.execSelect();
//            propertyTagMap = evaluateQueryResults(results);
//        } catch (LodeException e) {
//            throw new SPARQLQueryException("Failed to retrieve annotation", e);
//        }
//        finally {
//            if (execute !=  null)  {
//                execute.close();
//                if (g != null ) {
//                    g.close();
//                }
//            }
//        }
//        return propertyTagMap;
//    }

//    private Map<URI, Collection<URI>> evaluateQueryResults(ResultSet results) {
//        Map<URI, Collection<URI>> propertyTagMap = new HashMap<URI,Collection<URI>>();
//        while (results.hasNext()) {
//            QuerySolution solution = (QuerySolution) results.next();
//            getPropertyTagsFromBindingSet(propertyTagMap, solution);
//        }
//        return propertyTagMap;
//    }

//    private Map<URI, Collection<URI>> getPropertyTagsFromBindingSet(Map<URI, Collection<URI>> propertyTagMap, QuerySolution solution) {
//
//        // ?propertyvalueid ?propertyname ?propertyvalue ?semantictag
//        Resource propertyvalueid = solution.getResource(QueryVariables.PROPERTY_VALUE_ID.toString());
//        Resource semantictag = solution.getResource(QueryVariables.SEMANTIC_TAG.toString());
//
//        URI p = URI.create(propertyvalueid.getURI());
//
//        if (!propertyTagMap.containsKey(p)) {
//            propertyTagMap.put(p, new HashSet<URI>());
//        }
//        propertyTagMap.get(p).add(URI.create(semantictag.getURI()));
//
//        return propertyTagMap;
//    }

//    public Collection<URI> readAnnotationsByPropertyToSemanticTag(URI property, Collection<URI> semanticTag) {
//        String query = getQueryManager().getSparqlQuery("AnnotationByPropertySemanticTag");
//        Graph g = getQueryService().getDefaultGraph();
//        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
//
//        Collection<URI> annotations = new HashSet<>();
//
//        QueryExecution execute = null;
//        try {
//            for (URI uri : semanticTag) {
//                QuerySolutionMap initialBinding = new QuerySolutionMap();
//                initialBinding.add(QueryVariables.PROPERTY_VALUE_ID.toString(), new ResourceImpl(property.toString()));
//                initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(uri.toString()));
//                execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
//                ResultSet results = execute.execSelect();
//                annotations.addAll(evaluateAnnotationByPropertyQueryResults(results));
//            }
//        }
//        catch (LodeException e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (execute !=  null)  {
//                execute.close();
//                if (g != null ) {
//                    g.close();
//                }
//            }
//        }
//        return annotations;
//    }

    private Collection<URI> evaluateAnnotationByPropertyQueryResults(ResultSet results) {
        Collection<URI> annotations = new HashSet<>();
        while (results.hasNext()) {
            QuerySolution solution = (QuerySolution) results.next();
            URI a = getAnnotationsFromBindingSet(solution);
            if (a != null) {
                annotations.add(a);
            }
        }
        return annotations;
    }

    private URI getAnnotationsFromBindingSet(QuerySolution solution) {
        Resource annotationUri = solution.getResource(QueryVariables.ANNOTATION_ID.toString());
        return URI.create(annotationUri.getURI());
    }

    @Override
    public String getDatasourceName() {
        return "zooma";
    }

    @Override
    public int count() {
        return 0;
    }


//    @Override
//    public Collection<AnnotationSummary> read() {
//        Map<URI, Collection<URI>> ptos = readDistinctPropertyToSemanticTag();
//        Set<AnnotationSummary> summary = new HashSet<>();
//
//        for (URI propertyUri : ptos.keySet()) {
//            Collection<URI> annotationURIs = readAnnotationsByPropertyToSemanticTag(propertyUri, ptos.get(propertyUri));
//            Property property  = getPropertyDAO().read(propertyUri);
//
//            summary.add(new SimpleAnnotationSummary(
//                    null,
//                    property instanceof TypedProperty ? ((TypedProperty) property).getPropertyType() : null,
//                    property.getPropertyValue(),
//                    ptos.get(propertyUri),
//                    annotationURIs,
//                    0));
//        }
//        return summary;
//    }

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
