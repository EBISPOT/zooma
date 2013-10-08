package uk.ac.ebi.fgpt.zooma.datasource;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.service.SesameRepositoryManager;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A zooma annotation DAO implementation that uses a SPARQL endpoint to expose ontology information in response to
 * canned queries.
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 13/09/12
 */
public class SparqlOntologyDAO implements OntologyDAO {
    private SesameRepositoryManager manager;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public SesameRepositoryManager getManager() {
        return manager;
    }

    public void setManager(SesameRepositoryManager manager) {
        this.manager = manager;
    }

    @Override public String getSemanticTagLabel(URI semanticTagURI) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.label");

        getLog().trace("Formulating query for label of <" + semanticTagURI + ">");
        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.SEMANTIC_TAG.toString(), factory.createURI(semanticTagURI.toString()));
        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        String label = evaluateLabelQueryResult(result);
        if (label == null) {
            throw new NullPointerException("No rdfs:label present for <" + semanticTagURI + ">");
        }
        else {
            return label;
        }
    }

    @Override public Set<String> getSemanticTagSynonyms(URI semanticTagURI) {
        return getSemanticTagSynonyms(URI.create("http://www.ebi.ac.uk/efo/alternative_term"), semanticTagURI);
    }

    @Override public Set<String> getSemanticTagSynonyms(URI synonymTypeURI, URI semanticTagURI) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.synonyms");

        getLog().trace("Formulating query for synonyms of <" + semanticTagURI + ">");
        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.SYNONYM_PROPERTY.toString(), factory.createURI(synonymTypeURI.toString()));
        bindingMap.put(QueryVariables.SEMANTIC_TAG.toString(), factory.createURI(semanticTagURI.toString()));
        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateSynonymQueryResult(result);
    }

    private String evaluateLabelQueryResult(TupleQueryResult result) {
        try {
            if (result.hasNext()) {
                return getLabelFromBindingSet(result.next());
            }
            else {
                getLog().debug("Empty result set acquired for label tuple query");
                return null;
            }
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve ontology term label", e);
        }
    }

    private String getLabelFromBindingSet(BindingSet bindingSet) {
        Value labelValue = bindingSet.getValue(QueryVariables.SEMANTIC_TAG_LABEL.toString());
        if (labelValue != null) {
            return labelValue.stringValue();
        }
        else {
            getLog().debug("Empty value for label from binding set");
            return null;
        }
    }

    private Set<String> evaluateSynonymQueryResult(TupleQueryResult result) {
        Set<String> synonyms = new HashSet<>();
        try {
            while (result.hasNext()) {
                String s = getSynonymFromBindingSet(result.next());
                if (s != null) {
                    synonyms.add(s);
                }
            }
            return synonyms;
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve ontology term synonyms", e);
        }
    }

    private String getSynonymFromBindingSet(BindingSet bindingSet) {
        Value synonym = bindingSet.getValue(QueryVariables.SEMANTIC_TAG_SYNONYM.toString());
        if (synonym != null) {
            return synonym.stringValue();
        }
        else {
            getLog().warn("no synonym found for URI lookup");
            return null;
        }
    }
}
