package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.datasource.OntologyDAO;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 06/08/2013
 * Functional Genomics Group EMBL-EBI
 */
public class SparqlOntologyDAO implements OntologyDAO {
    private JenaQueryExecutionService queryService;

    private QueryManager queryManager;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${zooma.ontology.synonym.predicates}")
    private String synonymPredicates;

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



    @Override public String getSemanticTagLabel(URI semanticTagURI) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.label");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        getLog().trace("Formulating query for label of <" + semanticTagURI + ">");

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(semanticTagURI.toString()));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            String label = evaluateLabelQueryResult(results);

            if (label == null) {
                throw new NullPointerException("No rdfs:label present for <" + semanticTagURI + ">");
            }
            else {
                return label;
            }

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
    }

    @Override public Set<String> getSemanticTagSynonyms(URI synonymTypeURI, URI semanticTagURI) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.synonyms");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        getLog().trace("Formulating query for label of <" + semanticTagURI + ">");

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.SYNONYM_PROPERTY.toString(), new ResourceImpl(synonymTypeURI.toString()));
        initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(semanticTagURI.toString()));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            return evaluateSynonymQueryResult(results);

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
    }

    private Set<String> evaluateSynonymQueryResult(ResultSet result) {
        Set<String> synonyms = new HashSet<>();
        while (result.hasNext()) {
            QuerySolution  solution = (QuerySolution) result.next();
            String s = getSynonymFromBindingSet(solution);
            if (s != null) {
                synonyms.add(s);
            }
        }
        return synonyms;

    }

    private String getSynonymFromBindingSet(QuerySolution solution) {
        Literal synonym = solution.getLiteral(QueryVariables.SEMANTIC_TAG_SYNONYM.toString());
        if (synonym != null) {
            return synonym.getLexicalForm();
        }
        else {
            getLog().warn("no synonym found for URI lookup");
            return null;
        }
    }




    @Override public Set<String> getSemanticTagSynonyms(URI semanticTagURI) {
        Set<String> synonyms = new HashSet<String>();
        synonyms.addAll(getSemanticTagSynonyms(URI.create("http://www.ebi.ac.uk/efo/alternative_term"), semanticTagURI));

        if (synonymPredicates != null) {
            String [] syns = synonymPredicates.split(",");
            for (String s : syns) {
                synonyms.addAll(getSemanticTagSynonyms(URI.create(s), semanticTagURI));
            }
        }
        return synonyms;
    }

    private String evaluateLabelQueryResult(ResultSet result) {
        if (result.hasNext()) {
            QuerySolution  solution = (QuerySolution) result.next();
            return getLabelFromBindingSet(solution);
        }
        else {
            getLog().debug("Empty result set acquired for label tuple query");
            return null;
        }

    }

    private String getLabelFromBindingSet(QuerySolution solution) {
        Literal labelValue = solution.getLiteral(QueryVariables.SEMANTIC_TAG_LABEL.toString());
        if (labelValue != null) {
            return labelValue.getLexicalForm();
        }
        else {
            getLog().debug("Empty value for label from binding set");
            return null;
        }
    }

}
