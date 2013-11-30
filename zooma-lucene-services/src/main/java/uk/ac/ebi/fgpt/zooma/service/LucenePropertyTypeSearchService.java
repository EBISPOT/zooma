package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A service that allows searching over the set of property types known to ZOOMA.  Prefix-based and pattern-based
 * matches are supported using a Lucene index to rapidly identify matching properties.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class LucenePropertyTypeSearchService extends ZoomaLuceneSearchService implements PropertyTypeSearchService {
    @Override public Collection<String> search(String propertyTypePattern) {
        try {
            initOrWait();

            // build a query
            Query q = formulateQuery("name", propertyTypePattern);

            // do the query
            return doQuery(q, new SingleFieldStringMapper("name"));
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyTypePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<String> searchByPrefix(String propertyTypePrefix) {
        return search(propertyTypePrefix + "*");
    }

    @Override public Map<String, Float> searchAndScore(String propertyTypePattern) {
        try {
            initOrWait();

            // build a query
            Query q = formulateQuery("name", propertyTypePattern);

            // do the query
            return doQueryAndScore(q, new SingleFieldStringMapper("name"));
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyTypePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<String, Float> searchAndScoreByPrefix(String propertyTypePrefix) {
        return searchAndScore(propertyTypePrefix + "*");
    }
}
