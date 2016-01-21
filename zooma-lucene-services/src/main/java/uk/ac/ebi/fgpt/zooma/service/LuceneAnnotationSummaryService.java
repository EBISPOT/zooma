package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.exception.SearchResourcesUnavailableException;
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
    private AnnotationDAO annotationDAO;
    private AnnotationSummaryMapper mapper;

    public AnnotationDAO getAnnotationDAO() {
        return annotationDAO;
    }

    public void setAnnotationDAO(AnnotationDAO annotationDAO) {
        this.annotationDAO = annotationDAO;
    }

    public AnnotationSummaryMapper getMapper() {
        return mapper;
    }

    @Override protected void doInitialization() throws IOException {
        super.doInitialization();
        int numAnnotations = getAnnotationDAO().count();
        int numSummaries = getReader().numDocs();
        getLog().debug("Total number of annotations in zooma: " + numAnnotations);
        getLog().debug("Total number of summaries in zooma: " + numSummaries);
        AnnotationSummaryMapper preMapper = new AnnotationSummaryMapper(numAnnotations, numSummaries);
        Set<Float> allScores = new HashSet<>();
        for (int i = 0; i < numSummaries; i++) {
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
            IndexReader reader = DirectoryReader.open(getIndex());
            for (int i = start; i < limit && i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                AnnotationSummary as = getMapper().mapDocument(doc);
                results.add(as);
            }
            return results;
        }
        catch (IOException e) {
            throw new SearchResourcesUnavailableException("Problems retrieving annotation summaries from lucene index",
                                                          e);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - reading process was interrupted",
                                                          e);
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
                    throw new SearchResourcesUnavailableException(
                            "An unexpected number of results for Annotation Summary ID '" + annotationSummaryID + "' " +
                                    "indicates the indexes need rebuilding (got " + results.size() + " records)");
                }
            }
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                                                          e);
        }
    }
}
