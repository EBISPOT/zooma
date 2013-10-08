package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    public void setAnnotationIndex(Directory annotationIndex) {
        this.annotationIndex = annotationIndex;
    }

    public Directory getAnnotationIndex() {
        return annotationIndex;
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

            // build a property query
            Query q = formulateProcessedQuery("property", propertyValuePattern,null);

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

            // build a property query
            Query pq = formulateProcessedQuery("property", propertyValuePattern,propertyType);

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify them with boolean, both terms must occur
            Query q = formulateTypedQuery(ptq, pq);

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
            Query q = formulateUniversalBooleanQuery(true, queries);

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
            Query q = formulateUniversalBooleanQuery(true, queries);

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

            // build a property query
            Query q = formulateProcessedQuery("property", propertyValuePattern,null);

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
    
 
    @Override public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyValuePattern) {
        try {
            initOrWait();

            // gets a collection of queries rather a boolean query with alternative terms
            Collection<Query> queries = formulateProcessedQueries("property", propertyValuePattern,null);

            // do the query
            getLog().debug("Calling doQueryAndScore_MaxScore... ");
            return doQueriesAndScore(queries, getMapper());
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + propertyValuePattern + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }
    
    //Original
    @Override public Map<AnnotationSummary, Float> searchAndScore(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();

            // build a property query
            Query pq = formulateProcessedQuery("property", propertyValuePattern,propertyType);

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify the two queries
            Query q = formulateTypedQuery(ptq, pq);

            // now combine pq (i.e. untyped) with q (i.e. typed) so we search typed or untyped, preferring typed
            Query tq = formulateTypedQuery(q, pq);

            
            getLog().debug("Calling doQueryAndScore... ");
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
    
    @Override public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyType, String propertyValuePattern) {
        
        try {
            initOrWait();

            Collection<Query> queries = formulateProcessedQueries("property", propertyValuePattern,propertyType);
            
            ArrayList<Query> final_queries = new ArrayList<Query>();
            
            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);
            
            for(Query query: queries){
                
                float boost_query = query.getBoost();
                query.setBoost(1.0f);
                
                // unify the two queries
                Query q =  formulateTypedQuery(ptq, query);
                
                Query tq = formulateTypedQuery(q, query);
                
                tq.setBoost(boost_query);
                
                final_queries.add(tq);
            }

            getLog().debug("Calling doQueryAndScore_MaxScore... ");
            // do the query
            return doQueriesAndScore(final_queries, getMapper());
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
            Query q = formulateUniversalBooleanQuery(true, queries);

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
            Query q = formulateUniversalBooleanQuery(true, queries);

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
