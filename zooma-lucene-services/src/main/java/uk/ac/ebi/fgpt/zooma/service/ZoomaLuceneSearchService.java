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
import org.apache.lucene.search.Similarity;
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
    private Directory index;
    private Analyzer analyzer;
    private Similarity similarity;

    private IndexReader reader;
    private IndexSearcher searcher;

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

    public Similarity getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    @Override
    protected void doInitialization() throws IOException {
        // initialize searcher and query parser from index
        this.reader = IndexReader.open(getIndex());
        this.searcher = new IndexSearcher(reader);
        if (similarity != null) {
            this.searcher.setSimilarity(similarity);
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
        float boost = valueQuery.getBoost();
        valueQuery.setBoost(1f);
        Query untypedQuery = (Query) valueQuery.clone();
        untypedQuery.setBoost(20f);
        Query typedQuery = formulateCombinedQuery(true, false, untypedQuery, typeQuery);
        typedQuery.setBoost(1f);
        Query q = formulateCombinedQuery(true, false, valueQuery, typedQuery);
        q.setBoost(boost);
        return q;
    }

    protected Query formulateTypedQuery(Query typeQuery, Collection<Query> processedValueQueries)
            throws QueryCreationException {
        Set<Query> queries = new HashSet<>();
        for (Query processedValueQuery : processedValueQueries) {
            queries.add(formulateTypedQuery(typeQuery, processedValueQuery));
        }
        return formulateCombinedQuery(false, false, queries.toArray(new Query[queries.size()]));
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

    protected Collection<Query> generateProcessedQueries(String fieldName,
                                                         String fieldToProcess,
                                                         Collection<SearchStringProcessor> searchStringProcessors)
            throws QueryCreationException {
        // combine queries for any string processors that can process our string
        Collection<Query> queries = new HashSet<>();
        for (SearchStringProcessor processor : searchStringProcessors) {
            if (processor.canProcess(fieldToProcess)) {
                for (String processedString : processor.processSearchString(fieldToProcess)) {
                    if (!processedString.isEmpty()) {
                        Query q = formulateQuery(fieldName, processedString);
                        q.setBoost(processor.getBoostFactor());
                        queries.add(q);
                    }
                }
            }
        }
        return queries;
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
    protected Query formulateCombinedQuery(boolean firstMustOccur, boolean allMustOccur, Query... queries)
            throws QueryCreationException {
        if (queries.length == 1) {
            return queries[0];
        }
        else {
            // unify them with a boolean query
            BooleanQuery q = new BooleanQuery();
            BooleanClause.Occur bco = allMustOccur ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
            int index = 0;
            for (Query nextQuery : queries) {
                if (index == 0 && firstMustOccur) {
                    q.add(nextQuery, BooleanClause.Occur.MUST);
                }
                else {
                    q.add(nextQuery, bco);
                }
                index++;
            }
            return q;
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
     * @param mapper the document mapper to use to extract the URI from resulting lucene documents
     * @param dao       the zooma dao that can be used to do the lookup of matching objects
     * @param <T>       the type of object to lookup - the ZoomaDAO supplied declares this type
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T extends Identifiable> Collection<T> doQuery(Query q,
                                                             LuceneDocumentMapper<URI> mapper,
                                                             ZoomaDAO<T> dao) throws IOException {
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
                        URI uri = mapper.mapDocument(doc);
                        T t = dao.read(uri);
                        if (t != null) {
                            results.add(t);
                        }
                        else {
                            getLog().warn("Failed to retrieve result for <" + uri + "> in DAO for " +
                                                  dao.getDatasourceName());
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

    /**
     * Performs a lucene query, and uses the supplied mapper to convert the resulting lucene document into the relevant
     * object type.  All results that match the given query are iterated over, in batches of 100, and put into a
     * collection of objects (of type matching the type of the mapper) that is returned.
     * <p/>
     * The score is obtained using the supplied scorer to evaluate the quality of the result.
     *
     * @param q      the lucene query to perform
     * @param mapper the document mapper to use to generate the result objects
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T> Map<T, Float> doQueryAndScore(Query q, LuceneDocumentMapper<T> mapper) throws IOException {
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
                TopDocs topDocs = collector.topDocs();
                ScoreDoc[] hits = topDocs.scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    if (getLog().isDebugEnabled()) {
                        if (hits.length > 0 && results.isEmpty()) {
                            getLog().debug("Best matched document has a lucene score of " + hits[0].score);
                            getLog().debug("The following explanation was provided:\n" +
                                                   getSearcher().explain(q, hits[0].doc).toString());
                        }
                    }

                    // map each document using the supplied mapper, and associate result score
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        float summaryScore = mapper.getDocumentQuality(doc);
                        float luceneScore = hit.score;
                        float totalScore = summaryScore * luceneScore;
                        if (getLog().isTraceEnabled()) {
                            getLog().trace("Next document has a quality score of: " +
                                                   summaryScore + " x " + luceneScore + " = " +
                                                   (summaryScore * luceneScore));
                        }
                        results.put(mapper.mapDocument(doc), totalScore);
                    }
                }
            }
            getLog().debug(
                    "Query '" + q.toString() + "'\n...gives the following " + results.size() + " results:\n" + results);
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }

    /**
     * Performs a lucene query, and obtains a collection of objects by using the supplied DAO to perform a lookup once
     * the URI of the object has been retrieved from the index.  A document mapper capable of extracting the URI from
     * the resulting document must be supplying; extracted URIs are then passed to the DAO.  All results that match the
     * given query are iterated over, in batches of 100, and put into a collection of objects that is returned.  This
     * collection is typed by the type of DAO that is supplied.
     *
     * @param q      the lucene query to perform
     * @param mapper the document mapper to use to extract the URI from resulting lucene documents
     * @param dao    the zooma dao that can be used to do the lookup of matching objects
     * @param <T>    the type of object to lookup - the ZoomaDAO supplied declares this type
     * @return a collection of results
     * @throws IOException if reading from the index failed
     */
    protected <T extends Identifiable> Map<T, Float> doQueryAndScore(Query q,
                                                                     LuceneDocumentMapper<URI> mapper,
                                                                     ZoomaDAO<T> dao)
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
                TopDocs topDocs = collector.topDocs();
                ScoreDoc[] hits = topDocs.scoreDocs;

                if (hits.length == 0) {
                    complete = true;
                }
                else {
                    // map each document using the supplied mapper, and associate result score
                    for (ScoreDoc hit : hits) {
                        lastScoreDoc = hit;
                        Document doc = getSearcher().doc(hit.doc);
                        float summaryScore = mapper.getDocumentQuality(doc);
                        float luceneScore = hit.score;
                        float totalScore = summaryScore * luceneScore;
                        getLog().debug("Next document has a quality score of: " +
                                               summaryScore + " x " + luceneScore + " = " +
                                               (summaryScore * luceneScore));
                        URI uri = mapper.mapDocument(doc);
                        T t = dao.read(uri);
                        if (t != null) {
                            results.put(t, totalScore);
                        }
                        else {
                            getLog().warn("Failed to retrieve result for <" + uri + "> in DAO for " +
                                                  dao.getDatasourceName());
                        }
                    }
                }
            }
            getLog().debug(
                    "Query '" + q.toString() + "'\n...gives the following " + results.size() + " results:\n" + results);
            return results;
        }
        catch (InterruptedException e) {
            throw new IOException("Failed to perform query - indexing process was interrupted", e);
        }
    }
}

