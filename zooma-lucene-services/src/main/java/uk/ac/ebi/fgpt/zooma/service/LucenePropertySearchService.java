package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessorProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * A service that allows searching over the set of {@link Property}s known to ZOOMA.  Prefix-based and pattern-based
 * matches are supported using a Lucene index to rapidly identify matching properties.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class LucenePropertySearchService extends ZoomaLuceneSearchService implements PropertySearchService {
    private PropertyDAO propertyDAO;

    private SearchStringProcessorProvider searchStringProcessorProvider;

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    public SearchStringProcessorProvider getSearchStringProcessorProvider() {
        return searchStringProcessorProvider;
    }

    public void setSearchStringProcessorProvider(SearchStringProcessorProvider searchStringProcessorProvider) {
        this.searchStringProcessorProvider = searchStringProcessorProvider;
    }

    @Override public Collection<Property> search(String propertyValuePattern) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("name", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("name",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }

            // unify processed queries into a single query
            Query q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));

            // do the query
            return doQuery(q, new SingleFieldURIMapper("uri"), getPropertyDAO());
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
                // first, formulate query for original propertyValuePattern
                Query pq = formulateQuery("name", propertyValuePattern);

                // then generate a series of queries from the processed property value, using available search string processors
                Collection<Query> pqs = new HashSet<>();
                pqs.add(pq);
                if (getSearchStringProcessorProvider() != null) {

                    pqs.addAll(generateProcessedQueries("name",
                                                        propertyValuePattern,
                                                        getSearchStringProcessorProvider().getFilteredProcessors(
                                                                propertyType)));
                }

                // build a property type query
                Query ptq = formulateQueryConserveOrderIfMultiword("type", propertyType);

                // unify the type query with each value query
                q = formulateTypedQuery(ptq, pqs);
            }

            // do the query
            return doQuery(q, new SingleFieldURIMapper("uri"), getPropertyDAO());
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

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("name", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("name",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }

            // unify processed queries into a single query
            Query q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));

            // do the query
            return doQueryAndScore(q, new SingleFieldURIMapper(), getPropertyDAO());
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
                // first, formulate query for original propertyValuePattern
                Query pq = formulateQuery("name", propertyValuePattern);

                // then generate a series of queries from the processed property value, using available search string processors
                Collection<Query> pqs = new HashSet<>();
                pqs.add(pq);
                if (getSearchStringProcessorProvider() != null) {
                    pqs.addAll(generateProcessedQueries("name",
                                                        propertyValuePattern,
                                                        getSearchStringProcessorProvider().getFilteredProcessors(
                                                                propertyType)));
                }

                // build a property type query
                Query ptq = formulateQueryConserveOrderIfMultiword("type", propertyType);

                // unify the type query with each value query
                q = formulateTypedQuery(ptq, pqs);
            }

            // do the query
            return doQueryAndScore(q, new SingleFieldURIMapper(), getPropertyDAO());
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
