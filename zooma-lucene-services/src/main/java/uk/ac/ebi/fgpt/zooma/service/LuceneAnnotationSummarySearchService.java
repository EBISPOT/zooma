package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessorProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * A service that allows searching over the set of {@link AnnotationSummary} objects known to ZOOMA.  Prefix-based and
 * pattern-based matches are supported using a Lucene index to rapidly identify matching properties.
 *
 * @author Tony Burdett
 * @date 28/05/12
 */
public class LuceneAnnotationSummarySearchService extends ZoomaLuceneSearchService
        implements AnnotationSummarySearchService {
    private Directory annotationIndex;
    private AnnotationSummaryMapper mapper;

    private SearchStringProcessorProvider searchStringProcessorProvider;

    public void setAnnotationIndex(Directory annotationIndex) {
        this.annotationIndex = annotationIndex;
    }

    public Directory getAnnotationIndex() {
        return annotationIndex;
    }

    public SearchStringProcessorProvider getSearchStringProcessorProvider() {
        return searchStringProcessorProvider;
    }

    public void setSearchStringProcessorProvider(SearchStringProcessorProvider searchStringProcessorProvider) {
        this.searchStringProcessorProvider = searchStringProcessorProvider;
    }

    public AnnotationSummaryMapper getMapper() {
        return mapper;
    }

    @Override protected void doInitialization() throws IOException {
        IndexReader reader = IndexReader.open(getAnnotationIndex());
        getLog().debug("Total number of annotations in zooma: " + reader.numDocs());
        this.mapper = new AnnotationSummaryMapper(reader.numDocs());
        reader.close();
        super.doInitialization();
    }

    @Override public Collection<AnnotationSummary> search(String propertyValuePattern) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }

            // unify processed queries into a single query
            Query q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getFilteredProcessors(
                                                            propertyType)));
            }

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify the type query with each value query
            Query q = formulateTypedQuery(ptq, pqs);

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException(
                    "Problems creating query for '" + propertyValuePattern + "' ['" + propertyType + "']", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix) {
        try {
            initOrWait();

            Query q = formulatePrefixQuery("property", propertyValuePrefix);

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePrefix + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix) {
        try {
            initOrWait();

            Query pq = formulatePrefixQuery("property", propertyValuePrefix);

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify them with boolean, both terms must occur
            Query q = formulateTypedQuery(ptq, pq);

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePrefix + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<AnnotationSummary> searchBySemanticTags(String... semanticTagShortnames) {
        try {
            initOrWait();

            // build a query
            Query[] queries = new Query[semanticTagShortnames.length];
            for (int i = 0; i < semanticTagShortnames.length; i++) {
                queries[i] = formulateSuffixQuery("semanticTag", semanticTagShortnames[i]);
            }
            Query q = formulateCombinedQuery(true, true, queries);

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating semantic tag shortname query", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Collection<AnnotationSummary> searchBySemanticTags(URI... semanticTags) {
        try {
            initOrWait();

            // build a query
            Query[] queries = new Query[semanticTags.length];
            for (int i = 0; i < semanticTags.length; i++) {
                queries[i] = formulateSuffixQuery("semanticTag", semanticTags[i].toString());
            }
            Query q = formulateCombinedQuery(true, true, queries);

            // do the query
            return doQuery(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating semantic tag URI query", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }


    @Override public Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }
            // unify processed queries into a single query
            Query q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));

            // do the query
            getLog().debug("Calling doQueryAndScore... ");
            return doQueryAndScore(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<AnnotationSummary, Float> searchAndScore(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            Collection<Query> pqs = new HashSet<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getFilteredProcessors(
                                                            propertyType)));
            }

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify the type query with each value query
            Query tq = formulateTypedQuery(ptq, pqs);

            // do the query
            return doQueryAndScore(tq, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException(
                    "Problems creating query for '" + propertyValuePattern + "' ['" + propertyType + "']", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyValuePrefix) {
        try {
            initOrWait();

            Query q = formulatePrefixQuery("property", propertyValuePrefix);

            // do the query
            return doQueryAndScore(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePrefix + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyType,
                                                                          String propertyValuePrefix) {
        try {
            initOrWait();

            Query pq = formulatePrefixQuery("property", propertyValuePrefix);

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify them with boolean, both terms must occur
            Query q = formulateTypedQuery(ptq, pq);

            // do the query
            return doQueryAndScore(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException(
                    "Problems creating query for '" + propertyValuePrefix + "' ['" + propertyType + "']", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames) {
        try {
            initOrWait();

            // build a query
            Query[] queries = new Query[semanticTagShortnames.length];
            for (int i = 0; i < semanticTagShortnames.length; i++) {
                queries[i] = formulateSuffixQuery("semanticTag", semanticTagShortnames[i]);
            }
            Query q = formulateCombinedQuery(true, true, queries);

            // do the query
            return doQueryAndScore(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating semantic tag shortname query", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }

    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(URI... semanticTags) {
        try {
            initOrWait();

            // build a query
            Query[] queries = new Query[semanticTags.length];
            for (int i = 0; i < semanticTags.length; i++) {
                queries[i] = formulateSuffixQuery("semanticTag", semanticTags[i].toString());
            }
            Query q = formulateCombinedQuery(true, true, queries);

            // do the query
            return doQueryAndScore(q, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating semantic tag URI query", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }
}
