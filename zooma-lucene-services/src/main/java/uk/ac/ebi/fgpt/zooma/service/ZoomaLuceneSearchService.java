package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Identifiable;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    private Directory index;
    private Analyzer analyzer;

    private IndexReader reader;
    private IndexSearcher searcher;

    private Collection<SearchStringProcessor> searchStringProcessors;


    protected enum QUERY_TYPE {
        FULL,
        PREFIX,
        SUFFIX
    }

    public Directory getIndex() {
        return index;
    }

    public void setIndex(Directory index) {
        this.index = index;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Collection<SearchStringProcessor> getSearchStringProcessors() {
        return searchStringProcessors;
    }

    public void setSearchStringProcessors(Collection<SearchStringProcessor> searchStringProcessors) {
        this.searchStringProcessors = searchStringProcessors;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    @Override
    protected void doInitialization() throws IOException {
        // initialize searcher and query parser from index
        this.reader = IndexReader.open(getIndex());
        this.searcher = new IndexSearcher(reader);

        if (searchStringProcessors == null) {
            getLog().debug("There are no search string processors registered - " +
                                   "only the raw search string will be used");
            searchStringProcessors = new HashSet<>();
        }
    }

    @Override
    protected void doTermination() throws Exception {
        // close index reader
        searcher.close();
        reader.close();
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
    protected Query formulateQuery(String field, String pattern) throws QueryCreationException {
        return formulateQuery(field, pattern, QUERY_TYPE.FULL, false);
    }

    protected Query formulatePrefixQuery(String field, String prefix) throws QueryCreationException {
        return formulateQuery(field, prefix, QUERY_TYPE.PREFIX, false);
    }

    protected Query formulateSuffixQuery(String field, String suffix) throws QueryCreationException {
        return formulateQuery(field, suffix, QUERY_TYPE.SUFFIX, false);
    }


    /**
     * Generate a lucene query from the supplied field and pattern, possibly processing the pattern first using any
     * {@link SearchStringProcessor}s that may be able to expand the query.
     *
     * @param field   the field to query
     * @param pattern the pattern to search for
     * @param type    typing information about the search pattern
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateProcessedQuery(String field, String pattern, String type)
            throws QueryCreationException {
        // first, formulate original query
        Query originalQuery = formulateQuery(field, pattern);

        // combine queries for any string processors that can process our string
        Map<Query, Float> processedQueries = new HashMap<>();
        if (processedQueries.isEmpty()) {
            return originalQuery;
        }
        else {
            processedQueries.put(originalQuery, 1.0f);
            return formulateAlternativesBooleanQuery(processedQueries);
        }
    }


    /**
     * Generate a series of lucene queries from the supplied field and pattern, possibly processing the pattern first
     * using any {@link SearchStringProcessor}s that maye be able to expand the query.
     *
     * @param field   the field to query
     * @param pattern the pattern to search for
     * @param type    typing information about the search pattern
     * @return the parsed query
     * @throws QueryCreationException if the query could not be created
     */
    protected Collection<Query> formulateProcessedQueries(String field, String pattern, String type)
            throws QueryCreationException {
        ArrayList<Query> queries = new ArrayList<>();

        // first, formulate original query
        Query originalQuery = formulateQuery(field, pattern);
        queries.add(originalQuery);

        // now, attempt to process the pattern and if successful generate extra queries
        for (SearchStringProcessor processor : getSearchStringProcessors()) {
            if (processor.canProcess(pattern, type)) {
                Collection<String> processedStrings = processor.processSearchString(pattern);
                for (String processedString : processedStrings) {
                    if (!processedString.isEmpty()) {
                        float boost = processor.getBoostFactor();
                        Query query_aux = formulateQuery(field, processedString);
                        query_aux.setBoost(boost);
                        queries.add(query_aux);
                    }
                }
            }
        }
        return queries;
    }

    /**
     * Generates a lucene query that unifies the multiple supplied queries into a boolean query.  If you supply a
     * <code>compulsoryQuery</code> parameter, it is assumed that the <code>otherQueries</code> provided are optional.
     * If <code>compulsoryQuery</code> is null, this method assumes all <code>otherQueries</code> must occur.  It is
     * possible to combine several different common must/should occur queries using multiple invocations of this
     * method.
     *
     * @param compulsoryQuery the queries that MUST occur in the results
     * @param otherQueries    any queries that may optionally occur in the results
     * @return the unified query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateBooleanQuery(Query compulsoryQuery, Query... otherQueries)
            throws QueryCreationException {
        return formulateBooleanQuery(false, compulsoryQuery, otherQueries);
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
    protected Query formulateTypedQuery(Query typeQuery, Query valueQuery) throws QueryCreationException {
        Query q = (Query) valueQuery.clone();
        q.setBoost(20f);
        return formulateBooleanQuery(false, q, typeQuery);
    }

    protected Query formulateQueryConserveOrderIfMultiword(String field, String pattern) throws QueryCreationException {
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
    protected Query formulateQuery(String field, String pattern, QUERY_TYPE queryType, boolean conserveOrderIfMultiword)
            throws QueryCreationException {
        try {
            Query q;

            // tokenize the pattern using the given analyzer
            List<String> terms = new ArrayList<>();
            TokenStream stream = analyzer.tokenStream(field, new StringReader(QueryParser.escape(pattern)));
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            while (stream.incrementToken()) {
                terms.add(termAtt.toString());
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
                        case FULL:
                            q = new TermQuery(new Term(field, term));
                            break;
                        case PREFIX:
                            q = new PrefixQuery(new Term(field, term));
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
                                       String... terms) throws QueryCreationException {
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
     * Generates a lucene query that unifies the multiple supplied queries into a boolean query.  The
     * <code>compulsoryQuery</code> clause is required (i.e. MUST occur) and will always appear in documents that are
     * returned.  The remaining queries may be optional, depending on the <code>allMustOccur</code> parameter - true
     * implies all terms must occur, false indicates the values in <code>queries</code> are optional.
     *
     * @param compulsoryQuery the query against a field value that must appear in the results
     * @param queries         the queries to unite
     * @return the unified query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateBooleanQuery(boolean allMustOccur,
                                          Query compulsoryQuery,
                                          Query... queries)
            throws QueryCreationException {
        // unify them with a boolean query
        BooleanQuery q = new BooleanQuery();
        q.add(compulsoryQuery, BooleanClause.Occur.MUST);
        BooleanClause.Occur bco = allMustOccur ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
        for (Query nextQuery : queries) {
            q.add(nextQuery, bco);
        }
        return q;
    }

    /**
     * Generates a lucene query that unifies the multiple supplied queries into a boolean query and describes whether or
     * not all of these terms must occur.
     *
     * @param queries the queries to unite
     * @return the unified query
     * @throws QueryCreationException if the query could not be created
     */
    protected Query formulateUniversalBooleanQuery(boolean allMustOccur, Query... queries)
            throws QueryCreationException {
        // unify them with a boolean query
        BooleanQuery q = new BooleanQuery();
        BooleanClause.Occur bco = allMustOccur ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
        for (Query nextQuery : queries) {
            q.add(nextQuery, bco);
        }
        return q;
    }

    /**
     * Generate a lucene query that serves as an "OR" query over the supplied queries, and assigns the supplied boost
     * factor to each query
     *
     * @param queriesToCombine the queries to combine with BooleanClause.Occur.SHOULD, mapped to the boost factor each
     *                         query should be assigned
     * @return the combined query
     * @throws QueryCreationException
     */
    protected Query formulateAlternativesBooleanQuery(Map<Query, Float> queriesToCombine)
            throws QueryCreationException {
        BooleanQuery q = new BooleanQuery();
        for (Query nextQuery : queriesToCombine.keySet()) {
            nextQuery.setBoost(queriesToCombine.get(nextQuery));
            q.add(nextQuery, BooleanClause.Occur.SHOULD);
        }
        return q;
    }

    /**
     * Performs a lucene query, and obtains the field that matches the given fieldname.  All results that match the
     * given query are iterated over, in batches of 100, and put into a collection of strings that is returned.
     *
     * @param q         the lucene query to perform
     * @param fieldname the name of the field to acquire results for
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected Collection<String> doQuery(Query q, String fieldname) throws IOException {
        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            List<String> results = new ArrayList<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        String s = doc.get(fieldname);
                        results.add(s);
                    }
                }
            }
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    /**
     * Performs a lucene query, and uses the supplied mapper to convert the resulting lucene document into the relevant
     * object type.  All results that match the given query are iterated over, in batches of 100, and put into a
     * collection of objects (of type matching the type of the mapper) that is returned.
     *
     * @param q the lucene query to perform
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T> Collection<T> doQuery(Query q, LuceneDocumentMapper<T> mapper) throws IOException {
        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            Collection<T> results = new HashSet<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        results.add(mapper.mapDocument(doc));
                    }
                }
            }
            getLog().debug("Query '" + q.toString() + "' returned " + results.size() + " results");
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    /**
     * Performs a lucene query, and obtains a collection of objects by using the supplied DAO to perform a lookup once
     * the URI of the object has been retrieved from the index.  The name of the field that describes the URI must be
     * specified by supplying the fieldname.  All results that match the given query are iterated over, in batches of
     * 100, and put into a collection of objects that is returned.  This collection is typed by the type of DAO that is
     * supplied.
     *
     * @param q         the lucene query to perform
     * @param fieldname the name of the field to acquire results for
     * @param dao       the zooma dao that can be used to do the lookup of matching objects
     * @param <T>       the type of object to lookup - the ZoomaDAO supplied declares this type
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T extends Identifiable> Collection<T> doQuery(Query q, String fieldname, ZoomaDAO<T> dao)
            throws IOException {
        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            List<T> results = new ArrayList<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        String s = doc.get(fieldname);
                        if (s != null) {
                            T t = dao.read(URI.create(s));
                            if (t != null) {
                                results.add(t);
                            }
                        }
                    }
                }
            }
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    protected Map<String, Float> doQueryAndScore(Query q, String fieldname) throws IOException {
        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            Map<String, Float> results = new HashMap<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        String s = doc.get(fieldname);
                        results.put(s, hit.score);
                    }
                }
            }
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    /* Performs several lucene queries, one for each query, accumulating the results in one hashMap.  */
    protected <T> Map<T, Float> doQueriesAndScore(Collection<Query> queries, LuceneDocumentMapper<T> mapper)
            throws IOException {

        Map<T, Float> results = new HashMap<>();

        for (Query q : queries) {

            try {
                // init, to make sure searcher is available
                initOrWait();

                // perform queries in blocks until there are no more hits
                ScoreDoc lastScoreDoc = null;

                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                TopDocs topDocs = collector.topDocs();

                ScoreDoc[] hits = topDocs.scoreDocs;

                getLog().debug("We have " + hits.length + " hits");

                if (hits.length > 0) {

                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        try {
                            float summaryScore = mapper.getDocumentQuality(doc);
                            float luceneScore = hit.score;
                            float totalScore = summaryScore * luceneScore;

                            //Here, we update totalScore using the boots
                            totalScore = totalScore * q.getBoost();

                            getLog().debug("Next document has a quality score of: " +
                                                   summaryScore + " x " + luceneScore + " = " +
                                                   (summaryScore * luceneScore));

                            //We accumulate annotations of all queries..
                            results.put(mapper.mapDocument(doc), totalScore);

                        }
                        catch (Exception e) {
                            results.put(mapper.mapDocument(doc), hit.score);
                        }
                    }
                }

                getLog().debug(
                        "Query '" + q.toString() + "' gives the following " + results.size() + " results:\n" + results);

                getLog().debug("Returning results");

            }
            catch (InterruptedException e) {
                throw new IOException("Failed to perform query - indexing process was interrupted", e);
            }
        }

        return results;

    }

    protected <T> Map<T, Float> doQueryAndScore(Query q, LuceneDocumentMapper<T> mapper) throws IOException {

        getLog().debug("Starting doQueryAndScore... ");


        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            Map<T, Float> results = new HashMap<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            //while (!complete) {
            // create a collector to obtain query results
            TopScoreDocCollector collector = lastScoreDoc == null
                    ? TopScoreDocCollector.create(100, true)
                    : TopScoreDocCollector.create(100, lastScoreDoc, true);

            // perform query
            getSearcher().search(q, collector);
            TopDocs topDocs = collector.topDocs();


            ScoreDoc[] hits = topDocs.scoreDocs;

            getLog().debug("We have " + hits.length + " hits");

            if (hits.length == 0) {
                complete = true;
            }
            else {
                // get URI and readByProperty property, add to results
                for (ScoreDoc hit : hits) {
                    lastScoreDoc = hit;
                    Document doc = getSearcher().doc(hit.doc);
                    try {
                        float summaryScore = mapper.getDocumentQuality(doc);
                        float luceneScore = hit.score;
                        float totalScore = summaryScore * luceneScore;
                        getLog().debug("Next document has a quality score of: " +
                                               summaryScore + " x " + luceneScore + " = " +
                                               (summaryScore * luceneScore));
                        results.put(mapper.mapDocument(doc), totalScore);
                    }
                    catch (Exception e) {
                        results.put(mapper.mapDocument(doc), hit.score);
                    }
                }
            }
            //}
            getLog().debug(
                    "Query '" + q.toString() + "' gives the following " + results.size() + " results:\n" + results);

            getLog().debug("Returning results");


            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    protected <T extends Identifiable> Map<T, Float> doQueryAndScore(Query q, String fieldname, ZoomaDAO<T> dao)
            throws IOException {
        try {
            // init, to make sure searcher is available
            initOrWait();

            // create the list to collect results in
            Map<T, Float> results = new HashMap<>();

            // perform queries in blocks until there are no more hits
            ScoreDoc lastScoreDoc = null;
            boolean complete = false;
            while (!complete) {
                // create a collector to obtain query results
                TopScoreDocCollector collector = lastScoreDoc == null
                        ? TopScoreDocCollector.create(100, true)
                        : TopScoreDocCollector.create(100, lastScoreDoc, true);

                // perform query
                getSearcher().search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // get URI and readByProperty property, add to results
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        String s = doc.get(fieldname);
                        if (s != null) {
                            T t = dao.read(URI.create(s));
                            if (t != null) {
                                results.put(t, hit.score);
                            }
                        }
                    }
                }
            }
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }


    private <T> Float obtainScoreAnnotationSummary(Map<T, Float> results, String id) {

        for (Map.Entry e : results.entrySet()) {

            T ann = (T) e.getKey();

            if (ann instanceof AnnotationSummary) {

                AnnotationSummary annotationSummary = (AnnotationSummary) ann;

                if (annotationSummary.getID().contentEquals(id)) {
                    return (Float) e.getValue();
                }
            }
        }
        return -1.0f;

    }

}

