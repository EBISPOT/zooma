package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A service that allows retrieval of the set of {@link AnnotationSummary} objects known to ZOOMA.  This uses a Lucene
 * index, as this is the only place annotation summaries are stored.
 *
 * @author Tony Burdett
 * @date 10/07/13
 */
public class LuceneAnnotationSummaryService extends ZoomaLuceneSearchService
        implements AnnotationSummaryService {
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

    @Override public Collection<AnnotationSummary> getAnnotationSummaries() {
        return getAnnotationSummaries(Integer.MAX_VALUE, 0);
    }

    @Override public Collection<AnnotationSummary> getAnnotationSummaries(int limit, int start) {
        try {
            initOrWait();

            Collection<AnnotationSummary> results = new ArrayList<>();
            IndexReader reader = IndexReader.open(getAnnotationIndex());
            for (int i = start; i < limit && i < reader.maxDoc(); i++) {
                if (reader.isDeleted(i)) {
                    continue;
                }

                Document doc = reader.document(i);
                AnnotationSummary as = getMapper().mapDocument(doc);
                results.add(as);
            }
            return results;
        }
        catch (IOException e) {
            throw new SearchException("Problems retrieving annotation summaries from lucene index", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - reading process was interrupted", e);
        }
    }

    @Override public AnnotationSummary getAnnotationSummary(String annotationSummaryID) {
        try {
            initOrWait();

            // build a query
            Query q = formulateExactQuery("id", annotationSummaryID);

            // do the query
            Collection<AnnotationSummary> results = doQuery(q, getMapper());
            if (results.size() == 1) {
                return results.iterator().next();
            }
            else {
                if (results.size() == 0) {
                    return null;
                }
                else {
                    throw new SearchException(
                            "An unexpected number of results for Annotation Summary ID '" + annotationSummaryID + "' " +
                                    "indicates the indexes need rebuilding (got " + results.size() + " records)");
                }
            }
        }
        catch (QueryCreationException | IOException e) {
            throw new SearchException("Problems creating query for '" + annotationSummaryID + "'", e);
        }
        catch (InterruptedException e) {
            throw new SearchException("Failed to perform query - indexing process was interrupted", e);
        }
    }
}
