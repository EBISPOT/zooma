package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An implementation of {@link StatusService} that reports on the status of a Lucene index and can rebuild indices if
 * requested.
 *
 * @author Tony Burdett
 * @date 20/02/13
 */
public class LuceneStatusService implements StatusService {
    private ZoomaLuceneIndexer zoomaLuceneIndexer;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaLuceneIndexer getZoomaLuceneIndexer() {
        return zoomaLuceneIndexer;
    }

    public void setZoomaLuceneIndexer(ZoomaLuceneIndexer zoomaLuceneIndexer) {
        this.zoomaLuceneIndexer = zoomaLuceneIndexer;
    }

    @Override public boolean checkStatus() {
        getLog().trace("Status check - initialization status is " + getZoomaLuceneIndexer().isInitialized());
        try {
            IndexReader reader;
            if (DirectoryReader.indexExists(getZoomaLuceneIndexer().getAnnotationCountIndex())) {
                reader = DirectoryReader.open(getZoomaLuceneIndexer().getAnnotationCountIndex());
                reader.close();
                getLog().trace("Status check - annotation count index present at " +
                                       getZoomaLuceneIndexer().getAnnotationCountIndex().toString());
            }
            else {
                getLog().trace("Status check - no annotation count index");
                return false;
            }
            if (DirectoryReader.indexExists(getZoomaLuceneIndexer().getAnnotationIndex())) {
                reader = DirectoryReader.open(getZoomaLuceneIndexer().getAnnotationIndex());
                reader.close();
                getLog().trace("Status check - annotation index present at " +
                                       getZoomaLuceneIndexer().getAnnotationIndex().toString());
            }
            else {
                getLog().trace("Status check - no annotation index");
                return false;
            }
            if (DirectoryReader.indexExists(getZoomaLuceneIndexer().getAnnotationSummaryIndex())) {
                reader = DirectoryReader.open(getZoomaLuceneIndexer().getAnnotationSummaryIndex());
                reader.close();
                getLog().trace("Status check - annotation summary index present at " +
                                       getZoomaLuceneIndexer().getAnnotationSummaryIndex().toString());
            }
            else {
                getLog().trace("Status check - no annotation summary index");
                return false;
            }
            if (DirectoryReader.indexExists(getZoomaLuceneIndexer().getPropertyIndex())) {
                reader = DirectoryReader.open(getZoomaLuceneIndexer().getPropertyIndex());
                reader.close();
                getLog().trace("Status check - property index present at " +
                                       getZoomaLuceneIndexer().getPropertyIndex().toString());
            }
            else {
                getLog().trace("Status check - no property index");
                return false;
            }
            if (DirectoryReader.indexExists(getZoomaLuceneIndexer().getPropertyTypeIndex())) {
                reader = DirectoryReader.open(getZoomaLuceneIndexer().getPropertyTypeIndex());
                reader.close();
                getLog().trace("Status check - property type index present at " +
                                       getZoomaLuceneIndexer().getPropertyTypeIndex().toString());
            }
            else {
                getLog().trace("Status check - no property type index");
                return false;
            }
            // opening all indices was successful, so status is good
            return true;
        }
        catch (CorruptIndexException e) {
            getLog().debug("Status check - corrupt index", e);
            return false;
        }
        catch (IOException e) {
            getLog().debug("Status check - i/o troubles", e);
            return false;
        }

    }

    @Override public String reinitialize() {
        // re-initialize the zooma lucene indexer
        try {
            getZoomaLuceneIndexer().init();
            return "ZOOMA indices have started building.";
        }
        catch (Exception e) {
            getLog().error("Reindexing caught unexpected exception", e);
            return "ZOOMA re-indexing failed: " + e.getMessage();
        }
    }
}
