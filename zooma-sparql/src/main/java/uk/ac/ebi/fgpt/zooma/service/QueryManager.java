package uk.ac.ebi.fgpt.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QueryManager handles retrieval of sparql queries based on query id from a specified file
 * <p/>
 * Input file expects format ^[query id] followed by the sparql query hash comments (#) are allowed
 *
 * @author Simon Jupp
 * @date 17/05/2012 Functional Genomics Group EMBL-EBI
 */
public class QueryManager {
    private Resource sparqlQueryResource;
    private String[] queries;

    public PropertiesMapAdapter getPropertiesMapAdapter() {
        return propertiesMapAdapter;
    }

    public void setPropertiesMapAdapter(PropertiesMapAdapter propertiesMapAdapter) {
        this.propertiesMapAdapter = propertiesMapAdapter;
    }

    private PropertiesMapAdapter propertiesMapAdapter;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Resource getSparqlQueryResource() {
        return sparqlQueryResource;
    }

    public void setSparqlQueryResource(Resource sparqlQueryResource) {
        this.sparqlQueryResource = sparqlQueryResource;
    }

    public void init() {
        try {
            this.queries = collectQueries(getSparqlQueryResource().getInputStream());
        }
        catch (IOException e) {
            throw new RuntimeException(
                    "Unable to load SPARQL queries from resource " + getSparqlQueryResource().getDescription(), e);
        }
    }

    public String getSparqlQuery(String queryId) {
        for (String query : queries) {
            final String name = query.substring(0, query.indexOf(":"));

            if (name.equals(queryId)) {
                return getPrefix() + "\n" + query.substring(name.length() + 2).trim();
            }
        }

        getLog().warn("No query for " + queryId + " found");
        return null;
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

    private String[] collectQueries(InputStream in) throws IOException {
        getLog().debug("Loading SPARQL queries...");
        List<String> queries = new ArrayList<>();
        BufferedReader inp = new BufferedReader(new InputStreamReader(in));
        String nextLine = null;

        while (true) {
            String line = nextLine;
            nextLine = null;
            if (line == null) {
                line = inp.readLine();
            }
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("^[") && line.endsWith("]")) {
                StringBuilder buff = new StringBuilder(line.substring(2, line.length() - 1));
                buff.append(": ");

                for (; ; ) {
                    line = inp.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("^[")) {
                        nextLine = line;
                        break;
                    }
                    buff.append(line);
                    buff.append(System.getProperty("line.separator"));
                }

                queries.add(buff.toString());
            }
        }

        String[] result = new String[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            getLog().debug("Adding query '" + queries.get(i) + "'");
            result[i] = queries.get(i);
        }
        return result;
    }
}
