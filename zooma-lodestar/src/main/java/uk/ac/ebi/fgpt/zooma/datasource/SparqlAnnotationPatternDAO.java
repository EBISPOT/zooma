package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
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

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 28/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public class SparqlAnnotationPatternDAO implements AnnotationPatternDAO {

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

    public Collection<AnnotationPattern> read() {
        getLog().debug("Reading all annotation patterns");
        String query = getQueryManager().getSparqlQuery("AnnotationPatterns.read");
        Graph g = getQueryService().getDefaultGraph();

        // no filter
        String filter = "";
        query = query.replace("filter", filter);

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, query, new QuerySolutionMap(), false);
            ResultSet results = execute.execSelect();
            return evaluatePatterns(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation patterns", e);
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

    @Override
    public Collection<AnnotationPattern> readByProperty(Property property) {
        getLog().debug("Reading all annotation patterns by property " + property.toString());

        if (property.getURI() == null) {
            return Collections.emptySet();
        }

        String query = getQueryManager().getSparqlQuery("AnnotationPatterns.read");
        Graph g = getQueryService().getDefaultGraph();

        // no filter
        String filter = "FILTER (str(?" + QueryVariables.PROPERTY_VALUE_ID.toString() + ") = '" + property.getURI() + "')";
        query = query.replace("filter", filter);
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, query, new QuerySolutionMap(), false);
            ResultSet results = execute.execSelect();
            return evaluatePatterns(results);
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

    @Override
    public Collection<AnnotationPattern> matchByProperty(String type, String value) {
        getLog().debug("Reading all annotation patterns by property value " + value);

        String query = getQueryManager().getSparqlQuery("AnnotationPatterns.read");
        Graph g = getQueryService().getDefaultGraph();

        String filter = "";
        if (value != null) {
            // todo filter seems to be faster than searching the text index
//            filter = "?" + QueryVariables.PROPERTY_VALUE.toString() + " bif:contains '\"" + value + "\"' . \n" ;
            filter = "FILTER regex(?" + QueryVariables.PROPERTY_VALUE.toString() + ", \"" + value + "\", \"i\")\n";
        }
        if (type != null) {
            filter += "FILTER (str(?" + QueryVariables.PROPERTY_NAME.toString() + ") = '" + type + "')";
        }
        // no filter
        query = query.replace("filter", filter);
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, query, new QuerySolutionMap(), false);
            ResultSet results = execute.execSelect();
            return evaluatePatterns(results);
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

    @Override
    public Collection<AnnotationPattern> matchByProperty(String value) {
        return matchByProperty(null, value);
    }

    @Override
    public Collection<AnnotationPattern> matchBySematicTag(URI semanticTagURI) {
        getLog().debug("Reading all annotation patterns by semantic tag");
        String query = getQueryManager().getSparqlQuery("AnnotationPatterns.read");
        Graph g = getQueryService().getDefaultGraph();

        // no filter
        String filter = "?annotationid oac:hasBody <" + semanticTagURI + "> .\n";
        query = query.replace("filter", filter);

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, query, new QuerySolutionMap(), false);
            ResultSet results = execute.execSelect();
            return evaluatePatterns(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation patterns", e);
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

    private Collection<AnnotationPattern> evaluatePatterns(ResultSet results) {

        // maps annotation URI to property URI
        Map<String, String> annotationToPropertyMap = new HashMap<>();
        // maps property URI to property type/value {0: type, 1: value}
        Map<String, String[]> propertyToLiteralsMap = new HashMap<>();
        // maps annotation URI to all semantic tags
        Map<String, Collection<String>> annotationToSemanticTagsMap = new HashMap<>();
        // maps annotations to their sources
        Map<String, AnnotationSource> annotationsToSourceMap = new HashMap<>();

        Set<String> replacedAnnotations = new HashSet<>();

        while (results.hasNext()) {
            QuerySolution solution = results.next();
            Resource sourceType = solution.getResource(QueryVariables.SOURCETYPE.toString());
            if (!URIBindingUtils.validateNamesExist(URI.create(sourceType.getURI()))) {
                getLog().debug("QuerySolution binding failed: unrecognised type <" + sourceType.getURI() + ">. " +
                        "Result will be null.");
                continue;
            }

            Resource annotationID = solution.getResource(QueryVariables.ANNOTATION_ID.toString());
            String annotationURI = annotationID.getURI();

            Resource propertyID = solution.getResource(QueryVariables.PROPERTY_VALUE_ID.toString());
            Literal propertyTypeLiteral = solution.getLiteral(QueryVariables.PROPERTY_NAME.toString());
            Literal propertyValueLiteral = solution.getLiteral(QueryVariables.PROPERTY_VALUE.toString());
            String propertyURI = propertyID.getURI();
            String propertyType = propertyTypeLiteral.getLexicalForm();
            String propertyValue = propertyValueLiteral.getLexicalForm();

            Resource database = solution.getResource(QueryVariables.DATABASEID.toString());
            Literal sourceName = solution.getLiteral(QueryVariables.SOURCENAME.toString());

            Resource semanticTagResource = solution.getResource(QueryVariables.SEMANTIC_TAG.toString());

            Literal newAnnotationId = solution.getLiteral(QueryVariables.NEXT_ANNOTATION_ID.toString());
            if (!newAnnotationId.getLexicalForm().equals("false")) {
                replacedAnnotations.add(annotationURI);
            }

            if (!annotationToPropertyMap.containsKey(annotationURI)) {
                annotationToPropertyMap.put(annotationURI, propertyURI);
            }
            if (!propertyToLiteralsMap.containsKey(propertyURI)) {
                propertyToLiteralsMap.put(propertyURI, new String[]{propertyType, propertyValue});
            }
            if (!annotationToSemanticTagsMap.containsKey(annotationURI)) {
                annotationToSemanticTagsMap.put(annotationURI, new HashSet<String>());
            }

            AnnotationSource source = null;
            if (database != null && sourceName !=null && sourceType != null)  {
                URI sourceAsUri = URI.create(sourceType.getURI());
                String sourceTypeName = URIBindingUtils.getName(sourceAsUri);
                AnnotationSource.Type sourceT = AnnotationSource.Type.lookup(sourceTypeName);
                if (sourceT == AnnotationSource.Type.ONTOLOGY) {
                    source = new SimpleOntologyAnnotationSource(URI.create(database.toString()),
                            sourceName.getLexicalForm());
                }
                else {
                    source = new SimpleDatabaseAnnotationSource(URI.create(database.toString()),
                            sourceName.getLexicalForm());
                }
                annotationsToSourceMap.put(annotationURI, source);
            }
            if (semanticTagResource != null) {
                annotationToSemanticTagsMap.get(annotationURI).add(semanticTagResource.getURI());
            }
        }


        // now, process mapped data into summaries
        Map<String, AnnotationPattern> hashedIDToPattern = new HashMap<>();
        for (String annotationURI : annotationToPropertyMap.keySet()) {
            // get property URI and all semantic tag URIs, and datasource info sort and hash
            String propertyURI = annotationToPropertyMap.get(annotationURI);
            Collection<String> semanticTagURIs = annotationToSemanticTagsMap.get(annotationURI);
            AnnotationSource source = annotationsToSourceMap.get(annotationURI);

            List<String> uris = new ArrayList<>();
            uris.add(propertyURI);
            uris.addAll(semanticTagURIs);
            uris.add(source.getURI().toString());
            uris.add(String.valueOf(replacedAnnotations.contains(annotationURI)));
            Collections.sort(uris);
            String hash = ZoomaUtils.generateHashEncodedID(uris.toArray(new String[uris.size()]));

            if (!hashedIDToPattern.containsKey(hash)) {
                String[] propertyLiterals = propertyToLiteralsMap.get(propertyURI);
                hashedIDToPattern.put(hash, new SimpleAnnotationPattern(
                        URI.create(propertyURI),
                        propertyLiterals[0],
                        propertyLiterals[1],
                        new HashSet<URI>(),
                        source,
                        replacedAnnotations.contains(annotationURI)));
            }
            SimpleAnnotationPattern pattern = (SimpleAnnotationPattern) hashedIDToPattern.get(hash);
            for (String semanticTagURI : semanticTagURIs) {
                pattern.getSemanticTags().add(URI.create(semanticTagURI));
            }
        }

        // finally, return the summaries
        return hashedIDToPattern.values();

    }


    @Override
    public List<AnnotationPattern> read(int size, int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationPattern read(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(AnnotationPattern object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(AnnotationPattern object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(AnnotationPattern identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

}
