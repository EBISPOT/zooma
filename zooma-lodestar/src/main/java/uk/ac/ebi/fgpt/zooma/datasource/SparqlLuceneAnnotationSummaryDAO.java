package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This partial implementation of the AnnotationSummaryDAO is designed specifically to support the efficient creation of
 * the Zooma lucene indexes
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 08/11/2013 Functional Genomics Group EMBL-EBI
 */
public class SparqlLuceneAnnotationSummaryDAO implements AnnotationSummaryDAO {

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

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, query, new QuerySolutionMap(), false);
            ResultSet results = execute.execSelect();
            return calculateSummaries(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation summaries", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
    }

    private Collection<AnnotationSummary> calculateSummaries(ResultSet results) {
        // maps annotation URI to property URI
        Map<String, String> annotationToPropertyMap = new HashMap<>();
        // maps property URI to property type/value {0: type, 1: value}
        Map<String, String[]> propertyToLiteralsMap = new HashMap<>();
        // maps annotation URI to all semantic tags
        Map<String, Collection<String>> annotationToSemanticTagsMap = new HashMap<>();
        // maps annotations to their sources
        Map<String, String> annotationsToSourceMap = new HashMap<>();

        // process results into maps
        while (results.hasNext()) {
            QuerySolution solution = results.next();

            Resource annotationID = solution.getResource(QueryVariables.ANNOTATION_ID.toString());
            String annotationURI = annotationID.getURI();

            Resource propertyID = solution.getResource(QueryVariables.PROPERTY_VALUE_ID.toString());
            Literal propertyTypeLiteral = solution.getLiteral(QueryVariables.PROPERTY_NAME.toString());
            Literal propertyValueLiteral = solution.getLiteral(QueryVariables.PROPERTY_VALUE.toString());
            String propertyURI = propertyID.getURI();
            String propertyType = propertyTypeLiteral.getLexicalForm();
            String propertyValue = propertyValueLiteral.getLexicalForm();

            Resource oldAnnotationID = solution.getResource(QueryVariables.PREV_ANNOTATION_ID.toString());
            Resource oldPropertyID = solution.getResource(QueryVariables.PREV_PROPERTY_ID.toString());
            Literal oldPropertyTypeLiteral = solution.getLiteral(QueryVariables.PREV_PROPERTY_NAME.toString());
            Literal oldPropertyValueLiteral = solution.getLiteral(QueryVariables.PREV_PROPERTY_VALUE.toString());

            Resource database = solution.getResource(QueryVariables.DATABASEID.toString());

            Resource semanticTagResource = solution.getResource(QueryVariables.SEMANTIC_TAG.toString());
            String semanticTag = semanticTagResource.getURI();

            if (!annotationToPropertyMap.containsKey(annotationURI)) {
                annotationToPropertyMap.put(annotationURI, propertyURI);
            }
            if (!propertyToLiteralsMap.containsKey(propertyURI)) {
                propertyToLiteralsMap.put(propertyURI, new String[]{propertyType, propertyValue});
            }
            if (!annotationToSemanticTagsMap.containsKey(annotationURI)) {
                annotationToSemanticTagsMap.put(annotationURI, new HashSet<String>());
            }

            if (database != null) {
                annotationsToSourceMap.put(annotationURI, database.getURI());
            }

            // handle previous terms that have mapped to this semantic tag if the property is different
            if (oldPropertyID != null && !propertyID.equals(oldPropertyID)) {
                if (!annotationToPropertyMap.containsKey(oldAnnotationID.getURI())) {
                    annotationToPropertyMap.put(oldAnnotationID.getURI(), oldPropertyID.getURI());
                }
                if (!propertyToLiteralsMap.containsKey(oldPropertyID.getURI())) {
                    propertyToLiteralsMap.put(oldPropertyID.getURI(), new String[]{oldPropertyTypeLiteral.getLexicalForm(), oldPropertyValueLiteral.getLexicalForm()});
                }
                if (!annotationToSemanticTagsMap.containsKey(oldAnnotationID.getURI())) {
                    annotationToSemanticTagsMap.put(oldAnnotationID.getURI(), new HashSet<String>());
                }
                annotationToSemanticTagsMap.get(oldAnnotationID.getURI()).add(semanticTag);
                if (database != null) {
                    annotationsToSourceMap.put(oldAnnotationID.getURI(), database.getURI());
                }
            }

            annotationToSemanticTagsMap.get(annotationURI).add(semanticTag);
        }

        // now, process mapped data into summaries
        Map<String, AnnotationSummary> hashedIDToSummaryMap = new HashMap<>();
        for (String annotationURI : annotationToPropertyMap.keySet()) {
            // get property URI and all semantic tag URIs, sort and hash
            String propertyURI = annotationToPropertyMap.get(annotationURI);
            Collection<String> semanticTagURIs = annotationToSemanticTagsMap.get(annotationURI);
            List<String> uris = new ArrayList<>();
            uris.add(propertyURI);
            uris.addAll(semanticTagURIs);
            Collections.sort(uris);
            String hash = ZoomaUtils.generateHashEncodedID(uris.toArray(new String[uris.size()]));

            if (!hashedIDToSummaryMap.containsKey(hash)) {
                String[] propertyLiterals = propertyToLiteralsMap.get(propertyURI);
                hashedIDToSummaryMap.put(hash, new SimpleLuceneSummary(URI.create(propertyURI), propertyLiterals[0], propertyLiterals[1]));
            }
            SimpleLuceneSummary summary = (SimpleLuceneSummary) hashedIDToSummaryMap.get(hash);
            summary.addAnnotationURI(URI.create(annotationURI));
            summary.addAnnotationSourceURIs(URI.create(annotationsToSourceMap.get(annotationURI)));
            for (String semanticTagURI : semanticTagURIs) {
                summary.addSemanticTag(URI.create(semanticTagURI));
            }
        }

        // finally, return the summaries
        return hashedIDToSummaryMap.values();
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
        private final URI propertyUri;
        private final String propertyType;
        private final String propertyValue;
        private final Set<URI> semanticTags;
        private final Set<URI> annotationURIs;
        private final Set<URI> annotationSourceURIs;

        public SimpleLuceneSummary(URI propertyUri, String propertyType, String propertyValue) {
            this.propertyUri = propertyUri;
            this.propertyType = propertyType;
            this.propertyValue = propertyValue;
            this.semanticTags = new HashSet<>();
            this.annotationURIs = new HashSet<>();
            this.annotationSourceURIs = new HashSet<>();
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
        public URI getAnnotatedPropertyUri() {
            return propertyUri;
        }

        @Override
        public String getAnnotatedPropertyValue() {
            return propertyValue;
        }

        @Override
        public String getAnnotatedPropertyType() {
            return propertyType;
        }

        @Override
        public Collection<URI> getSemanticTags() {
            return Collections.unmodifiableCollection(semanticTags);
        }

        public void addSemanticTag(URI semanticTag) {
            this.semanticTags.add(semanticTag);
        }

        @Override
        public Collection<URI> getAnnotationURIs() {
            return Collections.unmodifiableCollection(annotationURIs);
        }

        public void addAnnotationURI(URI annotationURI) {
            this.annotationURIs.add(annotationURI);
        }

        @Override
        public float getQuality() {
            return 0;
        }

        @Override
        public Collection<URI> getAnnotationSourceURIs() {
            return Collections.unmodifiableCollection(annotationSourceURIs);
        }

        public void addAnnotationSourceURIs(URI sourceUri) {
            this.annotationSourceURIs.add(sourceUri);
        }

        @Override
        public URI getURI() {
            return null;
        }

        @Override
        public String toString() {
            return "SimpleLuceneSummary{" +
                    ", propertyUri='" + propertyUri + '\'' +
                    ", propertyType='" + propertyType + '\'' +
                    ", propertyValue='" + propertyValue + '\'' +
                    ", semanticTags=" + semanticTags +
                    ", annotationURIs=" + annotationURIs +
                    ", annotationSources=" + annotationSourceURIs +
                    '}';
        }
    }
}
