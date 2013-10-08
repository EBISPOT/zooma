package uk.ac.ebi.fgpt.zooma.service;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.RDFRepositoryException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;

import java.util.Collections;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 17/05/2012 Functional Genomics Group EMBL-EBI
 */
public class SesameRepositoryManager {
    private String serverUrl;
    private String repositoryId;

    private RepositoryManager manager;
    private QueryManager queryManager;

    private PropertiesMapAdapter propertiesMapAdapter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public PropertiesMapAdapter getPropertiesMapAdapter() {
        return propertiesMapAdapter;
    }

    public void setPropertiesMapAdapter(PropertiesMapAdapter propertiesMapAdapter) {
        this.propertiesMapAdapter = propertiesMapAdapter;
    }

    protected Logger getLog() {
        return log;
    }

    public RepositoryManager getManager() {
        if (manager == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " has not been successfully initialised - " +
                                                    "call initialise() before using this class");
        }
        return manager;
    }

    public void initialise() {
        try {
            manager = new RemoteRepositoryManager(serverUrl);
            manager.initialize();
        }
        catch (RepositoryException e) {
            getLog().error("Failed to connect to repository: " + e.getMessage());
        }
    }

    public void disconnect() {
        getManager().shutDown();
    }

    public TupleQueryResult evaluateQuery(String query) {
        return evaluateQuery(query, Collections.<String, Value>emptyMap());
    }

    public TupleQueryResult evaluateQuery(String query, Map<String, Value> bindings) {
        getLog().trace("Evaluating SPARQL query '" + query + "' " +
                               "against " + getServerUrl() + " " +
                               "with binding map {" + bindings + "}...");
        RepositoryConnection connection = null;
        try {
            connection = getRepository().getConnection();
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, getPrefix() + "\n" + query);
            // add additional bindings
            for (String key : bindings.keySet()) {
                tupleQuery.setBinding(key, bindings.get(key));
            }
            return evaluateQuery(tupleQuery);
        }
        catch (RepositoryException | RepositoryConfigException e) {
            throw new RDFRepositoryException("Failed to connect to ZOOMA repository", e);
        }
        catch (MalformedQueryException e) {
            throw new SPARQLQueryException("Unable to process SPARQL query", e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (RepositoryException e) {
                    // tried our best!
                    getLog().error("Unable to close ZOOMA repository connection", e);
                }
            }
        }
    }

    private TupleQueryResult evaluateQuery(TupleQuery sparqlQuery) {
        try {
            return sparqlQuery.evaluate();
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Unable to process SPARQL query", e);
        }
    }

    public String getPrefix() {
        // add prefixes for all loaded namespaces
        StringBuilder sb = new StringBuilder();
        Map<String, String> propMap = getPropertiesMapAdapter().getPropertyMap();
        for (String prefix : propMap.keySet()) {
            sb.append("PREFIX ").append(prefix).append(":<").append(propMap.get(prefix)).append(">\n");
        }
        return sb.toString();
    }

    public Repository getRepository() throws RepositoryException, RepositoryConfigException {
        return getManager().getRepository(getRepositoryId());
    }

    public ValueFactory getValueFactory() throws RDFRepositoryException {
        RepositoryConnection connection = null;
        try {
            connection = getRepository().getConnection();
            return connection.getValueFactory();
        }
        catch (RepositoryException | RepositoryConfigException e) {
            throw new RDFRepositoryException("Failed to connect to ZOOMA repository", e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (RepositoryException e) {
                    // tried our best!
                    getLog().error("Unable to close ZOOMA repository connection", e);
                }
            }
        }
    }
}
