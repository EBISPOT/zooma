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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        super.doInitialization();
        IndexReader annotationReader = IndexReader.open(getAnnotationIndex());
        int numAnnotations = annotationReader.numDocs();
        int numSummaries = getReader().numDocs();
        annotationReader.close();
        getLog().debug("Total number of annotations in zooma: " + numAnnotations);
        getLog().debug("Total number of summaries in zooma: " + numSummaries);
        AnnotationSummaryMapper preMapper = new AnnotationSummaryMapper(numAnnotations, numSummaries);
        Set<Float> allScores = new HashSet<>();
        for (int i = 0; i < numSummaries; i++) {
            if (getReader().isDeleted(i)) {
                continue;
            }
            allScores.add(preMapper.mapDocument(getReader().document(i)).getQuality());
        }
        float maxScore = Collections.max(allScores);
        getLog().debug("Maximum summary quality score = " + maxScore);
        this.mapper = new AnnotationSummaryMapper(numAnnotations,
                                                  numSummaries,
                                                  maxScore);
        getLog().debug("Annotation Summary mapper calibration complete");
    }

    @Override public Collection<AnnotationSummary> search(String propertyValuePattern, URI... sources) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            List<Query> pqs = new ArrayList<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }

            Query q;
            if (sources.length > 0) {
                // unify processed queries into a single query
                Query uq = formulateCombinedQuery(true, false, pqs.toArray(new Query[pqs.size()]));

                // next generate a series of source queries
                List<Query> qs = new ArrayList<>();
                qs.add(uq);
                for (URI source : sources) {
                    qs.add(formulateQuery("source", source.toString()));
                }

                // unify queries into a single query
                q = formulateCombinedQuery(true, true, qs.toArray(new Query[qs.size()]));
            }
            else {
                // unify processed queries into a single query
                q = formulateCombinedQuery(false, false, pqs.toArray(new Query[pqs.size()]));
            }

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

    @Override public Collection<AnnotationSummary> search(String propertyType,
                                                          String propertyValuePattern,
                                                          URI... sources) {
        try {
            initOrWait();

            // check for null type
            if (propertyType == null) {
                return search(propertyValuePattern, sources);
            }

            // first, formulate query for original propertyValuePattern
            Query pq = formulateQuery("property", propertyValuePattern);

            // then generate a series of queries from the processed property value, using available search string processors
            List<Query> pqs = new ArrayList<>();
            pqs.add(pq);
            if (getSearchStringProcessorProvider() != null) {
                pqs.addAll(generateProcessedQueries("property",
                                                    propertyValuePattern,
                                                    getSearchStringProcessorProvider().getProcessors()));
            }

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            Query q;
            if (sources.length > 0) {
                // unify the type query with each value query
                Query tq = formulateTypedQuery(ptq, pqs);

                // next generate a series of source queries
                List<Query> qs = new ArrayList<>();
                qs.add(tq);
                for (URI source : sources) {
                    qs.add(formulateQuery("source", source.toString()));
                }

                // unify queries into a single query
                q = formulateCombinedQuery(true, true, qs.toArray(new Query[qs.size()]));
            }
            else {
                q = formulateTypedQuery(ptq, pqs);
            }

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

    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix, URI... sources) {
        try {
            initOrWait();

            // first, formulate query for original propertyValuePattern
            Query pq = formulatePrefixQuery("property", propertyValuePrefix);

            Query q;
            if (sources.length > 0) {
                // next generate a series of source queries
                List<Query> qs = new ArrayList<>();
                qs.add(pq);
                for (URI source : sources) {
                    qs.add(formulateQuery("source", source.toString()));
                }

                // unify queries into a single query
                q = formulateCombinedQuery(true, true, qs.toArray(new Query[qs.size()]));

            }
            else {
                q = pq;
            }

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

    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyType,
                                                        String propertyValuePrefix,
                                                        URI... sources) {
        try {
            initOrWait();

            // check for null type
            if (propertyType == null) {
                return searchByPrefix(propertyValuePrefix, sources);
            }

            Query pq = formulatePrefixQuery("property", propertyValuePrefix);

            // build a property type query
            Query ptq = formulateQueryConserveOrderIfMultiword("propertytype", propertyType);

            // unify them with boolean, both terms must occur
            Query tq = formulateTypedQuery(ptq, pq);

            Query q;
            if (sources.length > 0) {
                // next generate a series of source queries
                List<Query> qs = new ArrayList<>();
                qs.add(tq);
                for (URI source : sources) {
                    qs.add(formulateQuery("source", source.toString()));
                }

                // unify queries into a single query
                q = formulateCombinedQuery(true, true, qs.toArray(new Query[qs.size()]));
            }
            else {
                q = tq;
            }

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

    @Override public Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern,
                                                                            List<URI> preferredSources,
                                                                            URI... requiredSources) {
        // todo - implement source ranking!
        throw new UnsupportedOperationException("Searching with preferred source is not yet supported");
    }

    @Override public Collection<AnnotationSummary> searchByPreferredSources(String propertyType,
                                                                            String propertyValuePattern,
                                                                            List<URI> preferredSources,
                                                                            URI... requiredSources) {
        // check for null type
        if (propertyType == null) {
            return searchByPreferredSources(propertyValuePattern, preferredSources, requiredSources);
        }

        // todo - implement source ranking!
        throw new UnsupportedOperationException("Searching with preferred source is not yet supported");
    }
}
