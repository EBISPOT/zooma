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
        IndexReader reader = IndexReader.open(getAnnotationIndex());
        getLog().debug("Total number of annotations in zooma: " + reader.numDocs());
        this.mapper = new AnnotationSummaryMapper(reader.numDocs(), getReader().numDeletedDocs());
        reader.close();
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
            Query q = formulateQuery("id", annotationSummaryID);

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
