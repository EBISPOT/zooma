package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A service that allows searching over the set of {@link Property}s known to ZOOMA.  Prefix-based and pattern-based
 * matches are supported using a Lucene index to rapidly identify matching properties.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class LucenePropertySearchService extends ZoomaLuceneSearchService implements PropertySearchService {
    private PropertyDAO propertyDAO;

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override public Collection<Property> search(String propertyValuePattern) {
        try {
            initOrWait();

            // build a query
            Query q = formulateProcessedQuery("name", propertyValuePattern,null);

            // do the query
            return doQuery(q, "uri", getPropertyDAO());
        }
        catch (IOException | QueryCreationException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<Property> search(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();

            // build a query
            Query q;
            if (propertyValuePattern.isEmpty()) {
                q = formulateQuery("type", propertyType);
            }
            else {
                Query pq = formulateProcessedQuery("name", propertyValuePattern,propertyType);
                Query ptq = formulateQuery("type", propertyType);
                q = formulateTypedQuery(ptq, pq);
            }

            // do the query
            return doQuery(q, "uri", getPropertyDAO());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException(
                    "Problems creating query for '" + propertyValuePattern + "' ['" + propertyType + "']", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<Property> searchByPrefix(String propertyValuePrefix) {
        return search(propertyValuePrefix + "*");
    }

    @Override public Collection<Property> searchByPrefix(String propertyType, String propertyValuePrefix) {
        if (propertyValuePrefix.isEmpty()) {
            return search(propertyType, "");
        }
        else {
            return search(propertyType, propertyValuePrefix + "*");
        }
    }

    @Override public Map<Property, Float> searchAndScore(String propertyValuePattern) {
        try {
            initOrWait();

            // build a query
            Query q = formulateProcessedQuery("name", propertyValuePattern,null);

            // do the query
            return doQueryAndScore(q, "uri", getPropertyDAO());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<Property, Float> searchAndScore(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();

            Query q;
            if (propertyValuePattern.isEmpty()) {
                q = formulateQuery("type", propertyType);
            }
            else {
                Query pq = formulateProcessedQuery("name", propertyValuePattern,propertyType);
                Query ptq = formulateQuery("type", propertyType);
                q = formulateTypedQuery(ptq, pq);
            }

            // do the query
            return doQueryAndScore(q, "uri", getPropertyDAO());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException(
                    "Problems creating query for '" + propertyValuePattern + "' ['" + propertyType + "']", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<Property, Float> searchAndScoreByPrefix(String propertyValuePrefix) {
        return searchAndScore(propertyValuePrefix + "*");
    }

    @Override public Map<Property, Float> searchAndScoreByPrefix(String propertyType, String propertyValuePrefix) {
        return searchAndScore(propertyType, propertyValuePrefix + "*");
    }
}
