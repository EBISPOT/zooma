package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation of an {@link uk.ac.ebi.fgpt.zooma.datasource.AnnotationSourceDAO} that uses the LODEStar sparql
 * framework to fetch AnnotationSources from a triplestore.
 *
 * @author Tony Burdett
 * @date 18/12/13
 */
public class SparqlAnnotationSourceDAO implements AnnotationSourceDAO {
    private final static String underscore = "_";

    private JenaQueryExecutionService queryService;
    private QueryManager queryManager;

    private Logger log = LoggerFactory.getLogger(getClass());

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

    protected Logger getLog() {
        return log;
    }

    @Override public String getDatasourceName() {
        return "zooma";
    }

    @Override public int count() {
        return read().size();
    }

    @Override public void create(AnnotationSource identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Creation of new annotation sources is not yet implemented");
    }

    @Override public Collection<AnnotationSource> read() {
        return read(-1, -1);
    }

    @Override public List<AnnotationSource> read(int size, int start) {
        String query = getQueryManager().getSparqlQuery("AnnotationSource.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        if (size > -1) {
            q1.setLimit(size);
        }
        if (start > -1) {
            q1.setOffset(start);
            q1.addOrderBy(underscore + QueryVariables.ANNOTATION_ID.toString(), Query.ORDER_DEFAULT);
        }
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1, false);
            ResultSet results = execute.execSelect();
            return evaluateQueryResults(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation sources", e);
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

    @Override public AnnotationSource read(URI uri) {
        String query = getQueryManager().getSparqlQuery("AnnotationSource.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.DATABASEID.toString(), new ResourceImpl(uri.toString()));

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<AnnotationSource> sources = evaluateQueryResults(results);
            if (sources.size() > 1) {
                getLog().error("Too many results looking for annotation source <" + uri.toString() + ">");
                throw new TooManyResultsException("Expected one result, got " + sources.size() + " for <" + uri + ">");
            }
            else {
                if (sources.size() == 0) {
                    return null;
                }
                else {
                    return sources.get(0);
                }
            }

        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation source", e);
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

    @Override public Collection<AnnotationSource> readBySourceName(String sourceName) {
        String query = getQueryManager().getSparqlQuery("AnnotationSource.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.SOURCENAME.toString(),
                           ModelFactory.createDefaultModel().createLiteral(sourceName));

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            return evaluateQueryResults(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation sources", e);
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

    @Override public void update(AnnotationSource object) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Updating of annotation sources is not yet implemented");
    }

    @Override public void delete(AnnotationSource object) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Deletion of annotation sources is not yet implemented");

    }

    private List<AnnotationSource> evaluateQueryResults(ResultSet result) {
        List<AnnotationSource> annotationSources = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution solution = result.next();
            annotationSources.add(getAnnotationSourceFromBindingSet(solution));
        }
        Collections.sort(annotationSources, new Comparator<AnnotationSource>() {
            @Override public int compare(AnnotationSource o1, AnnotationSource o2) {
                return o1.getURI().toString().compareTo(o2.getURI().toString());
            }
        });
        return annotationSources;
    }

    private AnnotationSource getAnnotationSourceFromBindingSet(QuerySolution solution) {
        Resource database = solution.getResource(QueryVariables.DATABASEID.toString());
        Resource sourceType = solution.getResource(QueryVariables.SOURCETYPE.toString());
        Literal sourceName = solution.getLiteral(QueryVariables.SOURCENAME.toString());

        AnnotationSource source = null;
        if (sourceType != null) {
            URI sourceAsUri = URI.create(sourceType.getURI());
            String sourceTypeName = URIBindingUtils.getName(sourceAsUri);
            AnnotationSource.Type sourceT = AnnotationSource.Type.lookup(sourceTypeName);

            if (sourceT == AnnotationSource.Type.ONTOLOGY) {
                source = new SimpleOntologyAnnotationSource(URI.create(database.toString()),
                                                            sourceName.getLexicalForm());
            }
            else if (sourceT == AnnotationSource.Type.DATABASE) {
                source = new SimpleDatabaseAnnotationSource(URI.create(database.toString()),
                                                            sourceName.getLexicalForm());
            }
        }
        return source;
    }
}
