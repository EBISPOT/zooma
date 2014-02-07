package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessorProvider;

import java.io.IOException;
import java.net.URI;
import java.util.*;

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

    @Override public Collection<Property> search(String propertyValuePattern, URI... sources) {
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

            Query q;
            if (sources.length > 0) {
                q = formulateExactCombinedQuery(pqs.toArray(new Query[pqs.size()]), "source", sources);
            }
            else {
                // unify processed queries into a single query
                q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));
            }

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

    @Override public Collection<Property> search(String propertyType, String propertyValuePattern, URI... sources) {
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

            if (sources.length > 0) {
                q = formulateExactCombinedQuery(new Query[] {q}, "source", sources);
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

    @Override public Collection<Property> searchByPrefix(String propertyValuePrefix, URI... sources) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulatePrefixQuery("name", propertyValuePrefix);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("name",
                        propertyValuePrefix,
                        getSearchStringProcessorProvider().getProcessors()));
            }

            Query q;
            if (sources.length > 0) {
                q = formulateExactCombinedQuery(pqs.toArray(new Query[pqs.size()]), "source", sources);
            }
            else {
                // unify processed queries into a single query
                q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));
            }

            // do the query
            return doQuery(q, new SingleFieldURIMapper("uri"), getPropertyDAO());
        }
        catch (IOException | QueryCreationException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePrefix + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<Property> searchByPrefix(String propertyType, String propertyValuePrefix, URI... sources) {

        if (propertyValuePrefix.isEmpty()) {
            return search(propertyType, "", sources);
        }
        else if (propertyType.isEmpty()) {
            return searchByPrefix(propertyValuePrefix, sources);

        }
        else {
            try {
                initOrWait();

                // build a query
                Query q;
                if (propertyValuePrefix.isEmpty()) {
                    q = formulateQuery("type", propertyType);
                }
                else {
                    // first, formulate query for original propertyValuePattern
                    Query pq = formulatePrefixQuery("name", propertyValuePrefix);

                    // then generate a series of queries from the processed property value, using available search string processors
                    Collection<Query> pqs = new HashSet<>();
                    pqs.add(pq);
                    if (getSearchStringProcessorProvider() != null) {
                        pqs.addAll(generateProcessedQueries("name",
                                propertyValuePrefix,
                                getSearchStringProcessorProvider().getFilteredProcessors(
                                        propertyType)));
                    }

                    // build a property type query
                    Query ptq = formulateQueryConserveOrderIfMultiword("type", propertyType);

                    // unify the type query with each value query
                    q = formulateTypedQuery(ptq, pqs);
                }

                if (sources.length > 0) {
                    q = formulateExactCombinedQuery(new Query[] {q}, "source", sources);
                }


                // do the query
                return doQuery(q, new SingleFieldURIMapper("uri"), getPropertyDAO());
            }
            catch (QueryCreationException | IOException e) {
                throw new SearchException(
                        "Problems creating query for '" + propertyValuePrefix + "' ['" + propertyType + "']", e);
            }
            catch (InterruptedException e) {
                throw new SearchException("Failed to perform query - indexing process was interrupted", e);
            }
        }
    }
}
