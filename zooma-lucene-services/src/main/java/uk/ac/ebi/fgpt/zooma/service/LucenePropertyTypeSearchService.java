package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.exception.SearchResourcesUnavailableException;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * A service that allows searching over the set of property types known to ZOOMA.  Prefix-based and pattern-based
 * matches are supported using a Lucene index to rapidly identify matching properties.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class LucenePropertyTypeSearchService extends ZoomaLuceneSearchService implements PropertyTypeSearchService {
    @Override public Collection<String> search(String propertyTypePattern, URI... sources) {
        try {
            initOrWait();

            // build a query
            Query q = formulateQuery("name", propertyTypePattern);

            if (sources.length > 0) {
                q = formulateExactCombinedQuery(new Query[]{q}, "source", sources);
            }

            // do the query
            return doQuery(q, new SingleFieldStringMapper("name"));
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                                                          e);
        }
    }

    @Override public Collection<String> searchByPrefix(String propertyTypePrefix, URI... sources) {

        try {
            initOrWait();

            // build a query
            Query q = formulatePrefixQuery("name", propertyTypePrefix);

            if (sources.length > 0) {
                q = formulateExactCombinedQuery(new Query[]{q}, "source", sources);
            }

            // do the query
            return doQuery(q, new SingleFieldStringMapper("name"));
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                                                          e);
        }
    }
}


