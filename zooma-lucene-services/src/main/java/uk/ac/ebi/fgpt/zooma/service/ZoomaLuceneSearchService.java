package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.ExitableDirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.QueryTimeoutImpl;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchResourcesUnavailableException;
import uk.ac.ebi.fgpt.zooma.exception.SearchTimeoutException;
import uk.ac.ebi.fgpt.zooma.model.Identifiable;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * An abstract implementation of a lucene search service for ZOOMA.  This class provides a convenience method for
 * performing lucene queries against any index that contains documents with a "uri" field, and resolving these documents
 * against a provided {@link ZoomaDAO}.  All results obtained from the index are returned by iterating over pages of
 * results and collecting the results into a list.
 * <p/>
 * Implementations are free to concentrate on the business of generating the queries without worrying about performing
 * them and collecting results.  The one stipulation is that you should always call {@link #init()} on this class once
 * the index has been set in order to
 *
 * @author Tony Burdett
 * @date 03/04/12
 */
public abstract class ZoomaLuceneSearchService extends Initializable {
    // Max lucene query time - if zooma.search.timeout in properties, this is set to 1/5 of that value
    private long luceneQueryTimeout = 1000; // Default 1 second lucene query timeout

    private Directory index;
    private Similarity similarity;

    protected enum QUERY_TYPE {
        EXACT,
        FULL,
        PREFIX,
        SUFFIX
    }

    @Autowired
    public void setConfigurationProperties(@Qualifier("configurationProperties") Properties configuration) {
        this.luceneQueryTimeout =
                ((Float) (Float.parseFloat(configuration.getProperty("zooma.search.timeout")) * 200))
                        .longValue();
    }

    public void setIndex(Directory index) {
        this.index = index;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
    }

    protected IndexReader getReader() {
        try {
            return ExitableDirectoryReader.wrap(DirectoryReader.open(index), new QueryTimeoutImpl(luceneQueryTimeout));
        }
        catch (IOException e) {
            throw new SearchResourcesUnavailableException("Unable to read lucene index", e);
        }
    }

    private IndexSearcher getSearcher(IndexReader reader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        if (similarity != null) {
            searcher.setSimilarity(similarity);
        }
        return searcher;
    }

    @Override
    protected void doInitialization() throws IOException {
        // required initialization goes here
    }

    @Override
    protected void doTermination() throws Exception {
        // required shutdown hooks go here
    }

    /**
     * Generate a lucene query from the supplied field and pattern.  Queries are constrained to hit only documents that
     * contain the supplied pattern within the given field.
     *
     * @param field   the field to query
     * @param pattern the pattern to search for
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateQuery(String field, String pattern) {
        return formulateQuery(field, pattern, QUERY_TYPE.FULL, false);
    }

    /**
     * Generate a lucene query from the supplied field and string.  Queries are constrained to hit only documents that
     * contain the full, exact string withing the given field.
     *
     * @param field  the field to query
     * @param string the exact string to search for
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateExactQuery(String field, String string) {
        return formulateQuery(field, string, QUERY_TYPE.EXACT, false);
    }

    /**
     * Generate a lucene query from the supplied field and prefix.  Queries are constrained to hit only documents that
     * contain the supplied pattern within the given field.
     *
     * @param field  the field to query
     * @param prefix the prefix to search for
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulatePrefixQuery(String field, String prefix) {
        return formulateQuery(field, prefix, QUERY_TYPE.PREFIX, false);
    }

    /**
     * Generate a lucene query from the supplied field and suffix.  Queries are constrained to hit only documents that
     * contain the supplied pattern within the given field.
     *
     * @param field  the field to query
     * @param suffix the suffix to search for
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateSuffixQuery(String field, String suffix) {
        return formulateQuery(field, suffix, QUERY_TYPE.SUFFIX, false);
    }


    /**
     * Generates a lucene query that functions as a specialised form of boolean query.  The two queries are unified into
     * a boolean query, with an assumption that typing SHOULD occur, although with a boost to customize the importance
     * of typing so as not to punish too severely type mismatches.
     *
     * @param typeQuery  the query representing typing information
     * @param valueQuery the query representing the value
     * @return the unified query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateTypedQuery(Query typeQuery, Query valueQuery) {
        float boost = valueQuery.getBoost();
        valueQuery.setBoost(1f);
        Query untypedQuery = valueQuery.clone();
        untypedQuery.setBoost(20f);
        Query typedQuery = formulateCombinedQuery(true, false, untypedQuery, typeQuery);
        typedQuery.setBoost(1f);
        Query q = formulateCombinedQuery(true, false, valueQuery, typedQuery);
        q.setBoost(boost);
        return q;
    }

    protected Query formulateTypedQuery(Query typeQuery, Collection<Query> processedValueQueries) {
        Set<Query> queries = new HashSet<>();
        for (Query processedValueQuery : processedValueQueries) {
            queries.add(formulateTypedQuery(typeQuery, processedValueQuery));
        }
        return formulateCombinedQuery(false, false, queries.toArray(new Query[queries.size()]));
    }

    protected Query formulateQueryConserveOrderIfMultiword(String field, String pattern) {
        return formulateQuery(field, pattern, QUERY_TYPE.FULL, true);
    }

    /**
     * Generate a lucene query from the supplied field and pattern.  Queries are constrained to hit only documents that
     * contain the supplied pattern within the given field.  If the pattern contains several terms, separated by
     * whitespace, calling this method will tokenise the supplied <code>pattern</code> string and delegate to {@link
     * #formulateSpanQuery(String, uk.ac.ebi.fgpt.zooma.service.ZoomaLuceneSearchService.QUERY_TYPE, int, boolean,
     * String...)}.
     *
     * @param field                    the field to query
     * @param pattern                  the pattern to search for
     * @param queryType                the type of query (full, prefix, suffix) to be formulated
     * @param conserveOrderIfMultiword whether word order should be preserved in the case of multiword queries
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateQuery(String field,
                                   String pattern,
                                   QUERY_TYPE queryType,
                                   boolean conserveOrderIfMultiword) {
        try {
            Query q;

            List<String> terms;
            if (queryType != QUERY_TYPE.EXACT) {
                // tokenize the pattern using the given analyzer
                terms = new ArrayList<>();
                // todo - creating a new analyzer here means we run the risk of using different analyzers for indexing and query
                Analyzer analyzer = new EnglishAnalyzer(CharArraySet.EMPTY_SET);
                try (TokenStream stream = analyzer.tokenStream(field, new StringReader(QueryParser.escape(pattern)))) {
                    stream.reset();
                    CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
                    while (stream.incrementToken()) {
                        terms.add(termAtt.toString());
                    }
                }
            }
            else {
                terms = Collections.singletonList(pattern);
            }

            if (terms.size() == 0) {
                throw new QueryCreationException(
                        "Unable to create query - no searchable terms in pattern '" + pattern + "'");
            }
            else {
                if (terms.size() == 1) {
                    // construct single term query
                    String term = terms.get(0);
                    switch (queryType) {
                        case EXACT:
                        case FULL:
                            q = new TermQuery(new Term(field, term));
                            break;
                        case PREFIX:
                            q = new SpanFirstQuery(
                                    new SpanMultiTermQueryWrapper<>(
                                            new PrefixQuery(new Term(field, term))), 1);
                            break;
                        case SUFFIX:
                            q = new TermQuery(new Term(field, "*" + term));
                            break;
                        default:
                            throw new QueryCreationException(
                                    "Cannot create query - unknown Query Type '" + queryType.toString() + "'");
                    }
                }
                else {
                    // multiple terms, delegate to span query
                    q = formulateSpanQuery(field,
                                           queryType,
                                           1,
                                           conserveOrderIfMultiword,
                                           terms.toArray(new String[terms.size()]));
                }
                return q;
            }
        }
        catch (IOException e) {
            throw new QueryCreationException(
                    "Failed to create query - could not tokenize and read pattern '" + pattern + "'", e);
        }
    }

    protected Collection<Query> generateProcessedQueries(String fieldName,
                                                         String fieldToProcess,
                                                         Collection<SearchStringProcessor> searchStringProcessors) {
        // combine queries for any string processors that can process our string
        Collection<Query> queries = new HashSet<>();
        for (SearchStringProcessor processor : searchStringProcessors) {
            if (processor.canProcess(fieldToProcess)) {
                for (String processedString : processor.processSearchString(fieldToProcess)) {
                    if (!processedString.isEmpty()) {
                        try {
                            Query q = formulateQuery(fieldName, processedString);
                            q.setBoost(processor.getBoostFactor());
                            queries.add(q);
                        }
                        catch (QueryCreationException e) {
                            // this processed string was one lucene cannot query for (probably +,-, or something similar)
                            // so exclude from results but continue
                            getLog().debug("Query string '" + fieldToProcess + "' was processed and resulted " +
                                                   "in clause '" + processedString + "', which cannot be used to " +
                                                   "create a lucene query");
                        }
                    }
                }
            }
        }
        return queries;
    }

    /**
     * Given an existing query, formulate a combined query of exact matches for the supplied items and combine with the
     * initial query
     *
     * @param q         Array of original queries
     * @param fieldName the fieldname for querying extact matches in the items collection
     * @param items     collection of items to combine in the query
     * @return new combined query
     * @throws QueryCreationException
     */
    public Query formulateExactCombinedQuery(Query[] q, String fieldName, Object[] items) {

        // unify processed queries into a single query
        Query uq = formulateCombinedQuery(true, false, q);

        // next generate a series of source queries
        List<Query> sqs = new ArrayList<>();
        for (Object item : items) {
            sqs.add(formulateExactQuery(fieldName, item.toString()));
        }
        // unify source queries into a single query
        Query sq = formulateCombinedQuery(false, false, sqs.toArray(new Query[sqs.size()]));

        // unify property and source queries into a single query
        return formulateCombinedQuery(true, true, uq, sq);
    }

    /**
     * Generate a lucene query from the supplied field and pattern.  Queries are constrained to hit only documents that
     * contain the supplied pattern within the given field.  Queries are constrained by the given proximity (or lucene
     * "slop factor") - so only documents that contain the searched terms within <code>proximity</code> terms of each
     * other are returned.
     *
     * @param field         the field to query
     * @param conserveOrder whether word order in the pattern is important and should be constrained in results with
     *                      multipel words
     * @param terms         the terms to search for
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateSpanQuery(String field,
                                       QUERY_TYPE queryType,
                                       int proximity,
                                       boolean conserveOrder,
                                       String... terms) {
        List<SpanQuery> stqs = new ArrayList<>();
        // create span queries for each term apart from the last one
        for (int i = 0; i < terms.length - 1; i++) {
            String term = terms[i];
            if (!term.isEmpty()) {
                stqs.add(new SpanTermQuery(new Term(field, term.toLowerCase())));
            }
        }

        // create span query for the last term
        String lastTerm = terms[terms.length - 1];
        switch (queryType) {
            case FULL:
                stqs.add(new SpanTermQuery(new Term(field, lastTerm)));
                break;
            case PREFIX:
                PrefixQuery pq = new PrefixQuery(new Term(field, lastTerm.toLowerCase()));
                stqs.add(new SpanMultiTermQueryWrapper<>(pq));
                break;
            case SUFFIX:
                stqs.add(new SpanTermQuery(new Term(field, "*" + lastTerm)));
                break;
            default:
                throw new QueryCreationException(
                        "Cannot create query - unknown Query Type '" + queryType.toString() + "'");
        }

        // create a span query that looks for documents where terms occur next to one another (order not important)
        return new SpanNearQuery(stqs.toArray(new SpanQuery[stqs.size()]), proximity, conserveOrder);
    }

    /**
     * Generates a lucene query that unifies multiple supplied queries into a single boolean query.  The flags supplied
     * indicate which queries in the supplied set must or should occur, using Lucene's indicator {@link
     * org.apache.lucene.search.BooleanClause.Occur}.
     * <p/>
     * If <code>firstMustOccur</code> is true, the first query is compulsory {@link
     * org.apache.lucene.search.BooleanClause.Occur#MUST}.  If <code>allMustOccur</code> is true, the remainder of the
     * supplied queries will also be combined with the {@link org.apache.lucene.search.BooleanClause.Occur#MUST} clause,
     * whereas if this flag is false, the remaining queries may be optional.
     * <p/>
     * For example; calling <code>formulateCombinedQuery(true, false, q1, q2, q3);</code> indicates that q1 MUST occur,
     * whereas q2 and q3 SHOULD occur - results that satisfy all three queries will be scored more highly but there may
     * be results which do not satisfy q2 and/or q3.  Calling <code>formulateCombinedQuery(false, true, q1, q2);</code>
     * is somewhat redundant.
     *
     * @param firstMustOccur whether the first query in the supplied set of queries must be present
     * @param queries        the queries to unite
     * @return the unified query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateCombinedQuery(boolean firstMustOccur, boolean allMustOccur, Query... queries) {
        if (queries.length == 1) {
            return queries[0];
        }
        else {
            // unify them with a boolean query
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            BooleanClause.Occur bco = allMustOccur ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
            int index = 0;
            for (Query nextQuery : queries) {
                if (index == 0 && firstMustOccur) {
                    builder.add(nextQuery, BooleanClause.Occur.MUST);
                }
                else {
                    builder.add(nextQuery, bco);
                }
                index++;
            }
            return builder.build();
        }
    }

    /**
     * Performs a lucene query, and uses the supplied mapper to convert the resulting lucene document into the relevant
     * object type.  All results that match the given query are iterated over, in batches of 100, and put into a
     * collection of objects (of type matching the type of the mapper) that is returned.
     *
     * @param q the lucene query to perform
     * @return a collection of results
     */
    protected <T> List<T> doQuery(Query q, LuceneDocumentMapper<T> mapper) {
        return doQuery(q, mapper, -1);
    }

    /**
     * Performs a lucene query, and uses the supplied mapper to convert the resulting lucene document into the relevant
     * object type.  All results that match the given query are iterated over, in batches of 100, and put into a
     * collection of objects (of type matching the type of the mapper) that is returned.
     *
     * @param q the lucene query to perform
     * @return a collection of results
     * @throws SearchResourcesUnavailableException if reading from the index failed
     */
    protected <T> List<T> doQuery(Query q, LuceneDocumentMapper<T> mapper, int limit) {
        try (IndexReader reader = getReader()) {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            List<T> results = new ArrayList<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            int rank = 1;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector topScoreCollector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100)
                        : TopScoreDocCollector.create(100, lastScoreDoc);
                TimeLimitingCollector collector = new TimeLimitingCollector(
                        topScoreCollector, TimeLimitingCollector.getGlobalCounter(), (luceneQueryTimeout));

                // perform query
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Acquiring searcher for query '" + q + "'");
                }
                IndexSearcher searcher = getSearcher(reader);
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Dispatching search for query '" + q + "'");
                }
                searcher.search(q, collector);
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Collating results for query '" + q + "'");
                }
                ScoreDoc[] hits = topScoreCollector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = searcher.doc(hit.doc);
                        if (limit == -1 || results.size() < limit) {
                            results.add(mapper.mapDocument(doc, rank));
                        }
                        else {
                            complete = true;
                            break;
                        }
                    }
                }
                rank++;
            }
            getLog().debug("Query '" + q.toString() + "' returned " + results.size() + " results");
            return results;
        }
        catch (IOException e) {
            throw new SearchResourcesUnavailableException("Failed to read index", e);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                                                          e);
        }
        catch (TimeLimitingCollector.TimeExceededException e) {
            throw new SearchTimeoutException("Failed to collect result of Lucene query [" + q + "] - " +
                                                     "timeout after " + luceneQueryTimeout + "ms.", e);
        }
        catch (ExitableDirectoryReader.ExitingReaderException e) {
            throw new SearchTimeoutException("Failed to perform Lucene query [" + q + "] - " +
                                                     "timeout after " + luceneQueryTimeout + "ms.", e);
        }
    }

    /**
     * Performs a lucene query, and obtains a collection of objects by using the supplied DAO to perform a lookup once
     * the URI of the object has been retrieved from the index.  The name of the field that describes the URI must be
     * specified by supplying the fieldname.  All results that match the given query are iterated over, in batches of
     * 100, and put into a collection of objects that is returned.  This collection is typed by the type of DAO that is
     * supplied.
     *
     * @param q      the lucene query to perform
     * @param mapper the document mapper to use to extract the URI from resulting lucene documents
     * @param dao    the zooma dao that can be used to do the lookup of matching objects
     * @param <T>    the type of object to lookup - the ZoomaDAO supplied declares this type
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T extends Identifiable> List<T> doQuery(Query q,
                                                       LuceneDocumentMapper<URI> mapper,
                                                       ZoomaDAO<T> dao) throws IOException {
        return doQuery(q, mapper, dao, -1);
    }

    /**
     * Performs a lucene query, and obtains a collection of objects by using the supplied DAO to perform a lookup once
     * the URI of the object has been retrieved from the index.  The name of the field that describes the URI must be
     * specified by supplying the fieldname.  All results that match the given query are iterated over, in batches of
     * 100, and put into a collection of objects that is returned.  This collection is typed by the type of DAO that is
     * supplied.
     *
     * @param q      the lucene query to perform
     * @param mapper the document mapper to use to extract the URI from resulting lucene documents
     * @param dao    the zooma dao that can be used to do the lookup of matching objects
     * @param <T>    the type of object to lookup - the ZoomaDAO supplied declares this type
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T extends Identifiable> List<T> doQuery(Query q,
                                                       LuceneDocumentMapper<URI> mapper,
                                                       ZoomaDAO<T> dao,
                                                       int limit) throws IOException {
        try (IndexReader reader = getReader()) {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            List<T> results = new ArrayList<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            int rank = 1;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector topScoreCollector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100)
                        : TopScoreDocCollector.create(100, lastScoreDoc);
                TimeLimitingCollector collector = new TimeLimitingCollector(
                        topScoreCollector, TimeLimitingCollector.getGlobalCounter(), luceneQueryTimeout);

                // perform query
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Acquiring searcher for query '" + q + "'");
                }
                IndexSearcher searcher = getSearcher(reader);
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Dispatching search for query '" + q + "'");
                }
                searcher.search(q, collector);
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Collating results for query '" + q + "'");
                }
                ScoreDoc[] hits = topScoreCollector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = searcher.doc(hit.doc);
                        URI uri = mapper.mapDocument(doc, rank);
                        T t = dao.read(uri);
                        if (t != null) {
                            if (limit == -1 || results.size() < limit) {
                                results.add(t);
                            }
                            else {
                                complete = true;
                                break;
                            }
                        }
                        else {
                            getLog().warn("Failed to retrieve result for <" + uri + "> in DAO for " +
                                                  dao.getDatasourceName());
                        }
                    }
                }
                rank++;
            }
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
        catch (TimeLimitingCollector.TimeExceededException e) {
            throw new SearchTimeoutException("Failed to collect result of Lucene query [" + q + "] - " +
                                                     "timeout after " + luceneQueryTimeout + "ms.", e);
        }
        catch (ExitableDirectoryReader.ExitingReaderException e) {
            throw new SearchTimeoutException("Failed to perform Lucene query [" + q + "] - " +
                                                     "timeout after " + luceneQueryTimeout + "ms.", e);
        }
    }
}

